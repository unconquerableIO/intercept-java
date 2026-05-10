# interceptJ-xgboost

XGBoost inference module for the `interceptJ` library. Provides a type-safe, exception-free pipeline for loading an XGBoost model, running batch prediction, decoding raw scores into typed predictions, and mapping the result to a domain decision.

---

## Table of Contents

- [Core Concepts](#core-concepts)
- [Pipeline Overview](#pipeline-overview)
- [Quick Start](#quick-start)
- [Loading a Model](#loading-a-model)
- [The Prediction Pipeline](#the-prediction-pipeline)
  - [Predictor](#stage-1--predictor)
  - [Predicted](#stage-2--predicted)
  - [Decoded](#stage-3--decoded)
  - [Interpreted (terminal)](#stage-4--interpreted-terminal)
- [Built-in Decoders](#built-in-decoders)
- [Built-in Normalizers](#built-in-normalizers)
- [Outcome](#outcome)
- [Escape Hatches](#escape-hatches)
- [Custom Decoders](#custom-decoders)
- [Custom Interpreters](#custom-interpreters)
- [Integration with interceptJ-core](#integration-with-interceptj-core)
- [API Reference](#api-reference)

---

## Core Concepts

| Type | Role |
|---|---|
| `XGBoostPredictor` | Loads and holds a `Booster`; runs `predict(DMatrix)` returning `Outcome` |
| `Predictor<T>` | Type-state pipeline entry point; wires feature extraction, prediction, decoding, and interpretation |
| `Outcome<O, E>` | Sealed type carrying either a success value or an error — never throws |
| `PredictionsDecoder<T, P>` | Converts raw `float[][]` scores into a typed `Predictions` batch |
| `Decoders` | Factory class for all built-in `PredictionsDecoder` implementations |
| `Predictions<T, P>` | Ordered batch of `Prediction<T>` values — one per input row |
| `Normalizer<I, O>` | Transforms a raw score to a more interpretable form (sigmoid, min-max, ranking) |
| `Interpreter<I, D>` | Maps a decoded `Predictions` batch or a `PredictionError` to an `Inference<D>` |
| `Inference<D>` | Wraps the final `Decision`; may carry additional context such as confidence scores |
| `Decision` | Marker interface for the domain outcome (e.g. `BLOCK`, `ALLOW`, `REVIEW`) |

---

## Pipeline Overview

```
XGBoostPredictor (model loader)
        │
        ▼
Predictor<T>
    .translate(T → DMatrix)      — feature extraction
    .predict()                   — runs XGBoost inference
        │
        ▼
    Predicted
    .decode(PredictionsDecoder)  — converts float[][] to typed Predictions
        │
        ▼
    Decoded<OT, P>
    .interpret(Interpreter)      — maps Predictions to Inference<D>
        │
        ▼
    Interpreted<OT, P, D>
    .orError(Interpreter)        — handles PredictionError; returns Inference<D>
        │
        ▼
    Inference<D>  ──►  D decision()
```

Each stage returns a distinct type. Out-of-order calls fail at compile time. Any XGBoost or decoder exception is captured as an `Outcome.Failure` — no exception escapes the pipeline.

---

## Quick Start

```java
// 1. Load the model once at startup
XGBoostPredictor model = new XGBoostPredictor(
        new FileModelLoader(new ModelSource("/models/fraud.ubj", "fraud-v2", "2.1.0")));

// 2. Run the pipeline for each request
Inference<FraudDecision> inference = Predictor.predictor(model, request)
        .translate(req -> buildMatrix(req.getFeatures()))
        .predict()
        .decode(Decoders.binaryLogistic())
        .interpret(predictions -> {
            double score = predictions.at(0).value();
            return new FraudInference(score > 0.75 ? FraudDecision.BLOCK : FraudDecision.ALLOW);
        })
        .orError(error -> new FraudInference(FraudDecision.ALLOW)); // safe fallback

FraudDecision decision = inference.decision();
```

---

## Loading a Model

`XGBoostPredictor` loads and deserializes the model eagerly at construction time via a `ModelLoader`. The built-in `FileModelLoader` reads an XGBoost binary (`.ubj`, `.json`, or `.model`) from the local file system.

```java
ModelSource source = new ModelSource(
        "/opt/models/fraud-detector.ubj",   // file path
        "fraud-detector",                    // logical name
        "3.0.0");                            // version

XGBoostPredictor predictor = new XGBoostPredictor(new FileModelLoader(source));
```

If the file cannot be read or deserialized, `FileModelLoader` throws `ModelLoaderException` at construction time — not at prediction time.

### Custom model loader

Implement `ModelLoader` to load from a database, object storage, or a model registry:

```java
public class S3ModelLoader implements ModelLoader {

    @Override
    public Booster load() {
        byte[] bytes = s3Client.getObject(bucket, key);
        return XGBoost.loadModel(bytes);
    }
}
```

---

## The Prediction Pipeline

### Stage 1 — Predictor

Create the entry point with `Predictor.predictor(model, input)`, then register the feature extractor with `translate`.

```java
Predictor<PaymentRequest> stage1 = Predictor.predictor(model, request);

// translate() returns this — same Predictor<T> instance
Predictor<PaymentRequest> stage1b = stage1.translate(req ->
        new DMatrix(req.getFeatureVector(), 1, req.featureCount(), Float.NaN));
```

`translate` is required before `predict()`. If skipped, `predict()` captures the omission as an `Outcome.Failure` with the message `"No features found"` and the pipeline continues to the error handler in `orError`.

### Stage 2 — Predicted

`predict()` applies the feature extractor and runs `XGBoostPredictor.predict(DMatrix)`. Any XGBoost exception is captured as an `Outcome.Failure<PredictionError>` — no exception propagates.

```java
Predictor.Predicted predicted = stage1b.predict();
```

The `Predicted` stage exposes an escape hatch for consumers that need the raw outcome before decoding:

```java
Outcome<DefaultPrediction<float[][]>, PredictionError> raw = predicted.outcome();
```

### Stage 3 — Decoded

`decode(PredictionsDecoder)` registers the decoder. The decoder is applied lazily — it runs when `decoded()` or `orError()` is called.

```java
Predictor.Decoded<Double, DefaultPrediction<Double>> decoded =
        predicted.decode(Decoders.binaryLogistic());
```

The `Decoded` stage exposes two escape hatches:

```java
// Raw outcome — same as Predicted.outcome()
Outcome<DefaultPrediction<float[][]>, PredictionError> raw = decoded.outcome();

// Typed decoded outcome — runs the decoder now; any exception is captured
Outcome<Predictions<Double, DefaultPrediction<Double>>, PredictionError> typed = decoded.decoded();
```

### Stage 4 — Interpreted (terminal)

`interpret(Interpreter)` registers the success interpreter. `orError(Interpreter)` registers the error interpreter, executes the full pipeline, and returns `Inference<D>`.

```java
Inference<FraudDecision> result = decoded
        .interpret(predictions -> new FraudInference(classify(predictions)))
        .orError(error -> {
            log.warn("Prediction failed: {}", error.message());
            return new FraudInference(FraudDecision.ALLOW);
        });
```

---

## Built-in Decoders

Use `Decoders` as the single entry point for all built-in decoder implementations.

| Factory method | XGBoost objective | Output type | Notes |
|---|---|---|---|
| `Decoders.binaryLogistic()` | `binary:logistic` | `Double` probability | Sigmoid applied internally by XGBoost; no normalizer needed |
| `Decoders.binaryLogitraw(normalizer)` | `binary:logitraw` | `Double` probability | Apply `SigmoidNormalizer` to convert raw log-odds |
| `Decoders.multiSoftMax(targetClassIndex)` | `multi:softmax` | `Integer` (0 or 1) | `1` if row label matches `targetClassIndex`, else `0` |
| `Decoders.multiSoftProb(targetClassIndex)` | `multi:softprob` | `Double` probability | Extracts the probability at `targetClassIndex` per row |
| `Decoders.regressionAbsoluteError(normalizer)` | `reg:absoluteerror` | `Double` | Apply `MinMaxNormalizer` to rescale to `[0, 1]` |
| `Decoders.regressionSquaredError(normalizer)` | `reg:squarederror` | `Double` | Apply `MinMaxNormalizer` to rescale to `[0, 1]` |
| `Decoders.regressionLogistic(normalizer)` | `reg:logistic` | `Double` | Apply `MinMaxNormalizer` or `SigmoidNormalizer` if needed |
| `Decoders.rankingDecoder(normalizer)` | `rank:pairwise`, `rank:ndcg`, `rank:map` | `Double` | Apply `RankingNormalizer` to rescale scores across the batch |

```java
// Binary classification — probability already in [0, 1]
.decode(Decoders.binaryLogistic())

// Binary — raw log-odds; apply sigmoid
.decode(Decoders.binaryLogitraw(new SigmoidNormalizer()))

// Multi-class — which class won?
.decode(Decoders.multiSoftMax(targetClassIndex))

// Multi-class — probability of the target class
.decode(Decoders.multiSoftProb(targetClassIndex))

// Regression — normalise to [0, 1] given known score bounds
.decode(Decoders.regressionSquaredError(new MinMaxNormalizer(0.0, 100.0)))

// Ranking — rescale scores across the whole batch
.decode(Decoders.rankingDecoder(new RankingNormalizer()))
```

---

## Built-in Normalizers

Normalizers are applied by decoders that do not output calibrated probabilities directly.

| Class | Input → Output | Formula | Use with |
|---|---|---|---|
| `SigmoidNormalizer` | `Float → Double` | `1 / (1 + e^−x)` | `binaryLogitraw`, raw log-odds models |
| `MinMaxNormalizer(min, max)` | `Float → Double` | `clamp((x − min) / (max − min), 0, 1)` | Regression objectives with known score bounds |
| `RankingNormalizer` | `float[] → double[]` | Min-max across the full batch | Ranking objectives |

All built-in normalizers are immutable records. Implement `Normalizer<I, O>` to supply a custom transform:

```java
Normalizer<Float, Double> logScaler = x -> Math.log1p(x) / Math.log1p(maxExpectedValue);

.decode(Decoders.regressionAbsoluteError(logScaler))
```

---

## Outcome

`Outcome<O, E>` is a sealed interface with two variants — `Success<O, E>` and `Failure<O, E>`. It is the primary error-handling mechanism in this module: exceptions from XGBoost and from decoders are captured as `Failure` instances rather than propagating up the call stack.

```java
Outcome<DefaultPrediction<float[][]>, PredictionError> outcome = predictor.predict(matrix);

// Pattern 1 — fold (preferred)
String message = outcome.fold(
        prediction -> "Score: " + prediction.value()[0][0],
        error      -> "Failed: " + error.message());

// Pattern 2 — map then fold
outcome
    .map(p -> decoder.decode(p.value()))
    .fold(predictions -> handleSuccess(predictions),
          error        -> handleError(error));

// Pattern 3 — branch
if (outcome.isSuccess()) { ... }
if (outcome.isFailure()) { ... }
```

`Outcome` supports `map` (transforms the success value) and `flatMap` (chains steps that themselves return an `Outcome`).

### PredictionError

`PredictionError` is the sole implementation of the `Error` interface. It carries a human-readable message and an optional root-cause `Throwable`.

```java
PredictionError.of("Model not ready");          // message only
PredictionError.of(exception);                  // wraps a caught exception
```

---

## Escape Hatches

Every stage exposes accessors for consumers with edge cases that don't fit the standard pipeline.

| Stage | Method | Returns |
|---|---|---|
| `Predicted` | `outcome()` | `Outcome<DefaultPrediction<float[][]>, PredictionError>` — raw XGBoost output |
| `Decoded` | `outcome()` | Same raw outcome, available after a decoder is registered |
| `Decoded` | `decoded()` | `Outcome<Predictions<OT, P>, PredictionError>` — applies the decoder immediately; captures any decoder exception |

```java
// Inspect the raw float[][] before decoding
Outcome<DefaultPrediction<float[][]>, PredictionError> raw =
        Predictor.predictor(model, request)
                .translate(req -> buildMatrix(req))
                .predict()
                .outcome();

// Decode and inspect without an interpreter chain
Outcome<Predictions<Double, DefaultPrediction<Double>>, PredictionError> decoded =
        Predictor.predictor(model, request)
                .translate(req -> buildMatrix(req))
                .predict()
                .decode(Decoders.binaryLogistic())
                .decoded();

int batchSize = decoded.fold(Predictions::size, error -> -1);
```

---

## Custom Decoders

Implement `PredictionsDecoder<T, P>` to handle objectives or post-processing logic not covered by the built-ins.

```java
public record ThresholdBinaryDecoder(double threshold)
        implements PredictionsDecoder<Boolean, DefaultPrediction<Boolean>> {

    @Override
    public Predictions<Boolean, DefaultPrediction<Boolean>> decode(float[][] rawResult) {
        return new Predictions<>(Arrays.stream(rawResult)
                .map(row -> new DefaultPrediction<>(row[0] >= threshold))
                .toList());
    }
}

// Use it
.decode(new ThresholdBinaryDecoder(0.5))
.interpret(predictions -> {
    boolean fraud = predictions.at(0).value();
    return new FraudInference(fraud ? FraudDecision.BLOCK : FraudDecision.ALLOW);
})
```

---

## Custom Interpreters

Implement `Interpreter<I, D>` to map decoded predictions (or errors) to your domain decision type. Both the success and error interpreters supplied to `interpret()` and `orError()` are `@FunctionalInterface` instances and are typically supplied as lambdas.

```java
// Success interpreter — receives Predictions<Double, DefaultPrediction<Double>>
Interpreter<Predictions<Double, DefaultPrediction<Double>>, FraudDecision> successInterp =
        predictions -> {
            double score = predictions.at(0).value();
            FraudDecision decision = score > 0.75 ? FraudDecision.BLOCK
                                   : score > 0.40 ? FraudDecision.REVIEW
                                                  : FraudDecision.ALLOW;
            return new FraudInference(decision, score);
        };

// Error interpreter — receives PredictionError
Interpreter<PredictionError, FraudDecision> errorInterp =
        error -> {
            alerting.notify(error.message(), error.cause().orElse(null));
            return new FraudInference(FraudDecision.ALLOW);  // safe default
        };
```

Your `Decision` and `Inference` implementations:

```java
public enum FraudDecision implements Decision { BLOCK, REVIEW, ALLOW }

public record FraudInference(FraudDecision decision, double score) implements Inference<FraudDecision> {
    public FraudInference(FraudDecision decision) { this(decision, -1.0); }
}
```

---

## Integration with interceptJ-core

Use the XGBoost pipeline as the implementation of an `interceptJ-core` `Detector`:

```java
public class XGBoostFraudDetector implements Detector<PaymentRequest> {

    private final XGBoostPredictor model;

    @Override
    public String name() {
        return "xgboost-fraud";
    }

    @Override
    public Detected<PaymentRequest> detect(PaymentRequest request) {
        Inference<FraudDecision> inference = Predictor.predictor(model, request)
                .translate(req -> buildMatrix(req.getFeatureVector()))
                .predict()
                .decode(Decoders.binaryLogistic())
                .interpret(p -> new FraudInference(p.at(0).value()))
                .orError(e -> new FraudInference(-1.0));

        double score = inference.decision().score();
        return new DetectedScore<>(name(), request, BigDecimal.valueOf(score));
    }
}
```

Then wire it into the core pipeline as any other detector:

```java
Interceptor.interceptor()
    .detect(request, new XGBoostFraudDetector(model))
    .detect(request.getDeviceId(), deviceBlocklistDetector)
    .decide(decider)
    .onBlock(()     -> Response.status(403).build())
    .onProceed(()   -> service.handle(request))
    .result();
```

---

## API Reference

### Predictor pipeline

| Type | Package | Description |
|---|---|---|
| `Predictor<T>` | `predictor` | Entry point; `translate` + `predict` |
| `Predictor.Predicted` | `predictor` | Stage after `predict()`; `outcome()` + `decode()` |
| `Predictor.Decoded<OT,P>` | `predictor` | Stage after `decode()`; `outcome()` + `decoded()` + `interpret()` |
| `Predictor.Interpreted<OT,P,D>` | `predictor` | Terminal stage; `orError()` executes the pipeline |
| `XGBoostPredictor` | `predictor` | Holds the `Booster`; wraps XGBoost exceptions in `Outcome` |

### Prediction types

| Type | Package | Description |
|---|---|---|
| `Outcome<O, E>` | `prediction` | Sealed success/failure type |
| `PredictionError` | `prediction` | Carries error message and optional cause |
| `Predictions<T, P>` | `prediction` | Ordered batch of typed predictions |
| `Prediction<T>` | `prediction` | Single prediction value marker interface |
| `DefaultPrediction<T>` | `prediction` | General-purpose `Prediction` record |
| `PredictionsDecoder<T, P>` | `prediction.decoder` | Decoder contract |
| `Decoders` | `prediction.decoder` | Factory for all built-in decoders |

### Normalizers

| Type | Package | Description |
|---|---|---|
| `Normalizer<I, O>` | `normalizer` | Normalizer contract |
| `SigmoidNormalizer` | `normalizer` | Sigmoid function (`Float → Double`) |
| `MinMaxNormalizer` | `normalizer` | Min-max rescaling (`Float → Double`) |
| `RankingNormalizer` | `normalizer` | Batch min-max rescaling (`float[] → double[]`) |

### Inference types

| Type | Package | Description |
|---|---|---|
| `Decision` | `inference` | Marker interface for domain outcomes |
| `Inference<D>` | `inference` | Wraps a `Decision`; may carry extra context |
| `Interpreter<I, D>` | `interpreter` | Maps input to `Inference<D>` |

### Model loading

| Type | Package | Description |
|---|---|---|
| `ModelLoader` | `model` | Strategy interface for loading a `Booster` |
| `FileModelLoader` | `model` | Loads a model from the local file system |
| `ModelSource` | `model` | Holds path, name, and version of a model file |
| `ModelLoaderException` | `model` | Thrown when a model cannot be loaded |