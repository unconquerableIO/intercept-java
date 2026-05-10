# interceptJ-core

The foundation module of the `interceptJ` library. Provides the **detect-then-decide** pipeline for intercepting and evaluating requests — fraud detection, bot protection, rate limiting, or any scenario where multiple independent signals must be reduced to a single, actionable verdict.

---

## Table of Contents

- [Core Concepts](#core-concepts)
- [Pipeline Overview](#pipeline-overview)
- [Usage](#usage)
  - [Implementing a Detector](#implementing-a-detector)
  - [Implementing a Decider](#implementing-a-decider)
  - [Running the Pipeline](#running-the-pipeline)
  - [Conditional Detectors](#conditional-detectors)
  - [Decision Detail](#decision-detail)
  - [Instrument Types](#instrument-types)
  - [Forwarding Pipeline Results (Sender)](#forwarding-pipeline-results-sender)
- [API Reference](#api-reference)

---

## Core Concepts

The module is built around eight types that form a linear pipeline.

| Type | Role |
|---|---|
| `Interceptor` | Entry point; holds detector registrations and drives the pipeline |
| `Detector<T>` | Analyses a single target value; returns a `Detected` result |
| `Detected` | Output of one detector — either a `DetectedScore` or a `DetectedStatus` |
| `Decider` | Examines all `Detected` results and returns a `Decided` verdict |
| `Decided` | One of four verdict types: `BLOCK`, `PROCEED`, `CHALLENGE`, `DEFER` |
| `Decision<R>` | Maps each verdict type to a caller-supplied handler and returns the result |
| `Sender<R>` | Receives the full pipeline context for forwarding to external systems |
| `InstrumentIdentifier<T>` | Bundles tenant, user, and the typed instrument value into one object |

### Detection result types

| Type | When to use |
|---|---|
| `DetectedScore` | Continuous risk signal (ML score, velocity count, reputation index) |
| `DetectedStatus` | Discrete signal — `DETECTED`, `NOT_DETECTED`, or `SKIPPED` |

### Verdict types

| Verdict | Meaning |
|---|---|
| `BLOCK` | Reject outright — risk exceeds the acceptable threshold |
| `PROCEED` | Allow — all signals within acceptable bounds |
| `CHALLENGE` | Require additional verification (CAPTCHA, OTP, step-up auth) |
| `DEFER` | Hold for asynchronous review by a human analyst or downstream process |

---

## Pipeline Overview

```
Interceptor.interceptor()
    .detect(target, Detector)  ──► Detected
    .detect(target, Detector)  ──► Detected
    .detect(target, Detector)  ──► Detected
                                       │
                                       ▼
                           Decider(List<Detected>)
                                       │
                                       ▼
                                    Decided
                          (BLOCK | PROCEED | CHALLENGE | DEFER)
                                       │
                                       ▼
                                 Decision<R>
                      .onBlock / .onProceed / .onChallenge / .onDefer
                                       │
                           .send(Sender) ──► audit / queue / metrics
```

---

## Usage

### Implementing a Detector

Implement `Detector<T>` for each risk signal. Return `DetectedScore` for quantitative signals and `DetectedStatus` for binary ones.

```java
public class IpReputationDetector implements Detector<String> {

    private final IpReputationService service;

    @Override
    public String name() {
        return "ip-reputation";
    }

    @Override
    public Detected<String> detect(String ipAddress) {
        BigDecimal score = service.riskScoreOf(ipAddress);
        return new DetectedScore<>(name(), ipAddress, score);
    }
}
```

```java
public class DeviceBlocklistDetector implements Detector<String> {

    private final BlocklistService blocklist;

    @Override
    public String name() {
        return "device-blocklist";
    }

    @Override
    public Detected<String> detect(String deviceId) {
        boolean blocked = blocklist.contains(deviceId);
        return new DetectedStatus<>(name(), deviceId, blocked
                ? DetectedStatus.Status.DETECTED
                : DetectedStatus.Status.NOT_DETECTED);
    }
}
```

---

### Implementing a Decider

`Decider` is a `@FunctionalInterface`. Implement it as a lambda for simple policies or as a named class for reusable, testable strategies.

**Lambda — block on any DETECTED signal**

```java
Decider blockOnAnySignal = detections -> detections.stream()
    .filter(d -> d instanceof DetectedStatus ds
            && ds.status() == DetectedStatus.Status.DETECTED)
    .findFirst()
    .<Decided>map(_ -> Decided.decidedToBlock())
    .orElse(Decided.decidedToProceed());
```

**Class — score-based tiered policy**

```java
public class TieredScoreDecider implements Decider {

    private static final BigDecimal BLOCK_THRESHOLD     = new BigDecimal("0.75");
    private static final BigDecimal CHALLENGE_THRESHOLD = new BigDecimal("0.40");

    @Override
    public Decided decide(List<Detected<?>> detections) {
        boolean deviceBlocked = detections.stream()
            .filter(d -> d instanceof DetectedStatus<?> ds
                    && "device-blocklist".equals(ds.detectorName()))
            .map(d -> (DetectedStatus<?>) d)
            .anyMatch(d -> d.status() == DetectedStatus.Status.DETECTED);

        if (deviceBlocked) {
            return Decided.decidedToBlock(new FraudDetail("DEVICE_BLOCKLISTED"));
        }

        BigDecimal totalScore = detections.stream()
            .filter(d -> d instanceof DetectedScore<?>)
            .map(d -> ((DetectedScore<?>) d).score())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalScore.compareTo(BLOCK_THRESHOLD)     > 0) return Decided.decidedToBlock();
        if (totalScore.compareTo(CHALLENGE_THRESHOLD) > 0) return Decided.decidedToChallenge();
        return Decided.decidedToProceed();
    }
}
```

---

### Running the Pipeline

```java
Optional<ApiResponse> response = Interceptor.interceptor()
    .detect(request.getIpAddress(), new IpReputationDetector(ipService))
    .detect(request.getDeviceId(),  new DeviceBlocklistDetector(blocklist))
    .decide(new TieredScoreDecider())
    .onBlock(()     -> ApiResponse.forbidden("Request blocked"))
    .onChallenge(() -> ApiResponse.status(401).header("WWW-Authenticate", "OTP").build())
    .onDefer(()     -> ApiResponse.accepted("Under review"))
    .onProceed(()   -> orderService.submit(request))
    .result();
```

Only the handler matching the active verdict fires. If no handler is registered for the active verdict, `result()` returns `Optional.empty()`.

#### Side-effect handlers

Every verdict also accepts a `Runnable` overload for side-effects — metrics, audit logs, events — without producing a return value.

```java
interceptor()
    .detect(request.getIpAddress(), ipDetector)
    .decide(decider)
    .onBlock(() -> metrics.increment("fraud.blocked"))  // Runnable — side effect only
    .onBlock(() -> ApiResponse.forbidden("Blocked"))    // Supplier — sets result()
    .onProceed(() -> orderService.submit(request))
    .result();
```

Both overloads can target the same verdict. Multiple `Runnable` handlers on the same verdict all fire in registration order.

---

### Conditional Detectors

Wrap any detector with `Detectors.detector(...)` to attach a runtime condition. When the condition is `false` the detector is skipped and returns a `SKIPPED` status, preserving its slot in the result list.

```java
import static io.unconquerable.intercept.detect.Detectors.detector;

Interceptor.interceptor()
    .detect(request.getCardNumber(),
            detector(threeDSecureDetector)
                .when(() -> request.getPaymentMethod() == PaymentMethod.CARD)
                .build())
    .detect(request.getUserId(),
            detector(velocityDetector)
                .when(() -> request.isAuthenticated())
                .and(() -> featureFlags.isVelocityCheckEnabled())
                .build())
    .decide(decider)
    ...
```

Conditions are evaluated lazily at pipeline execution time and support `when()`, `and()`, and `or()` with standard short-circuit semantics.

---

### Decision Detail

Attach structured metadata to any verdict for audit trails or compliance.

```java
public record FraudDetail(String ruleId, String detectorName, BigDecimal score)
        implements DecisionDetail {}
```

Return it from a decider:

```java
return Decided.decidedToBlock(new FraudDetail("VELOCITY_EXCEEDED", "velocity", score));
```

Access it in a handler:

```java
.onBlock(() -> {
    decided.details()
           .filter(d -> d instanceof FraudDetail)
           .map(d -> (FraudDetail) d)
           .ifPresent(d -> auditLog.record(d.ruleId(), d.detectorName()));
    return Response.status(403).build();
})
```

---

### Instrument Types

`InstrumentType` is a marker interface for the domain object being evaluated. `InstrumentIdentifier<T>` wraps it together with the tenant and user context.

```java
public record CreditCard(String number, String holder) implements InstrumentType {
    public String type() { return "credit-card"; }
}

public record PaymentIdentifier(String accountId, String userId, CreditCard instrument)
        implements InstrumentIdentifier<CreditCard> {}
```

The identifier is passed to `.send()` so senders receive full tenant and instrument context alongside the verdict. It is evaluated lazily — only when the send condition is met.

---

### Forwarding Pipeline Results (Sender)

`Sender<R>` is a `@FunctionalInterface` that receives the complete pipeline context: the handler result, the verdict, every detection signal, the instrument identifier, and caller-supplied metadata.

```java
Sender<ApiResponse> auditSender = (result, decided, detections, identifier, metadata) ->
    auditLog.record(AuditEntry.builder()
        .verdict(decided.type())
        .detections(detections)
        .accountId(identifier != null ? identifier.accountId() : null)
        .response(result)
        .build());
```

Wire it into the pipeline after the outcome handlers:

```java
Optional<ApiResponse> response = Interceptor.interceptor()
    .detect(request.getIpAddress(), ipDetector)
    .decide(decider)
    .onBlock(()     -> ApiResponse.forbidden("Blocked"))
    .onProceed(()   -> orderService.submit(request))
    .send(auditSender)
    .result();
```

#### Enriching the sender

```java
var identifier = new PaymentIdentifier("acct-42", "user-99", card);
var meta       = Map.<String, Object>of("traceId", requestId, "channel", "web");

.send(auditSender, () -> identifier)           // identifier only
.send(metricsSender, meta)                     // metadata only
.send(eventBusSender, () -> identifier, meta)  // both
.result();
```

#### Conditional send variants

| Method | Fires when |
|---|---|
| `sendOnBlock(sender)` | verdict is `BLOCK` |
| `sendOnProceed(sender)` | verdict is `PROCEED` |
| `sendOnChallenge(sender)` | verdict is `CHALLENGE` |
| `sendOnDefer(sender)` | verdict is `DEFER` |
| `sendUnlessBlocked(sender)` | verdict is **not** `BLOCK` |
| `sendUnlessProceed(sender)` | verdict is **not** `PROCEED` |
| `sendUnlessDefer(sender)` | verdict is **not** `DEFER` |

All variants accept the same `(sender)`, `(sender, identifier)`, `(sender, metadata)`, and `(sender, identifier, metadata)` overloads.

Multiple senders can be chained:

```java
.send(auditSender)
.sendOnBlock(fraudAlertSender, () -> identifier, Map.of("reason", "high-risk"))
.sendUnlessProceed(anomalySender, () -> identifier)
.result();
```

---

## API Reference

| Class / Interface | Package | Description |
|---|---|---|
| `Interceptor` | `io.unconquerable.intercept` | Pipeline entry point |
| `Detector<T>` | `io.unconquerable.intercept.detect` | Single-signal detector contract |
| `Detectors` | `io.unconquerable.intercept.detect` | Factory for `ConditionalDetector` |
| `ConditionalDetector<T>` | `io.unconquerable.intercept.detect` | Wraps a detector with composable conditions |
| `Detected` | `io.unconquerable.intercept.detect` | Marker for detection results |
| `DetectedScore` | `io.unconquerable.intercept.detect` | Carries a numeric `BigDecimal` score |
| `DetectedStatus` | `io.unconquerable.intercept.detect` | Carries `DETECTED`, `NOT_DETECTED`, or `SKIPPED` |
| `Decider` | `io.unconquerable.intercept.decide` | Reduces detections to a verdict |
| `Decided` | `io.unconquerable.intercept.decide` | Verdict with optional `DecisionDetail` |
| `DecisionDetail` | `io.unconquerable.intercept.decide` | Marker for verdict metadata |
| `Decision<R>` | `io.unconquerable.intercept.decide` | Fluent result handler |
| `Sender<R>` | `io.unconquerable.intercept` | Pipeline context forwarder |
| `InstrumentType` | `io.unconquerable.intercept` | Marker for the evaluated domain object |
| `InstrumentIdentifier<T>` | `io.unconquerable.intercept` | Bundles tenant, user, and instrument |