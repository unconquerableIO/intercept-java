# interceptJ

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-25-orange.svg?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/)

> Composable, type-safe fraud detection and ML inference for Java.

`interceptJ` is a modular library built around a **detect-then-decide** pipeline. Plug in any number of risk signals, reduce them to an actionable verdict, and optionally back that verdict with XGBoost model scores — all in a single fluent chain with no exceptions escaping the pipeline.

```java
Optional<Response> response = Interceptor.interceptor()
    .detect(request.getIpAddress(),   ipReputationDetector)
    .detect(request.getDeviceId(),    deviceFingerprintDetector)
    .detect(request.getUserId(),      velocityDetector)
    .decide(fraudDecider)
    .onBlock(()     -> Response.status(403).build())
    .onChallenge(() -> Response.status(429).header("X-Challenge", "captcha").build())
    .onDefer(()     -> Response.status(202).entity("Under review").build())
    .onProceed(()   -> service.handle(request))
    .result();
```

---

## Modules

| Module | Artifact | Description | Docs |
|---|---|---|---|
| `interceptJ-core` | `io.unconquerable:interceptJ-core` | Detect-then-decide pipeline — detectors, deciders, verdicts, senders | [README](interceptJ-core/README.md) |
| `interceptJ-xgboost` | `io.unconquerable:interceptJ-xgboost` | Type-safe XGBoost inference pipeline — loading, prediction, decoding, interpretation | [README](interceptJ-xgboost/README.md) |
| `interceptJ-bom` | `io.unconquerable:interceptJ-bom` | Bill of Materials for version alignment | — |

---

## Installation

Import the BOM to align all module versions, then declare only the modules you need.

**Gradle (Kotlin DSL)**

```kotlin
dependencies {
    implementation(platform("io.unconquerable:interceptJ-bom:0.0.3"))
    implementation("io.unconquerable:interceptJ-core")
    implementation("io.unconquerable:interceptJ-xgboost") // optional
}
```

**Maven**

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.unconquerable</groupId>
            <artifactId>interceptJ-bom</artifactId>
            <version>0.0.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.unconquerable</groupId>
        <artifactId>interceptJ-core</artifactId>
    </dependency>
</dependencies>
```

---

## Requirements

- Java 25 or later
- Gradle 8+ (for building from source)

---

## Building from Source

```bash
git clone https://github.com/unconquerableIO/interceptJ.git
cd interceptJ

# Build and run all tests
./gradlew build

# Install to local Maven repository
./gradlew publishToMavenLocal
```

---

## License

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).