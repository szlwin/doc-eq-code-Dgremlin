# COMPILER P2 Design/API Compatibility Remediation R03

> Effective Design Revision: `DESIGN-P2-R03`.
> Base: `DESIGN-P2-R02` (`p2-design-api-review-remediation-r02.md`).
> Input Business Model: `BM-R08`.
> Status: `NEEDS_REVIEW`.
> This is a normative delta. Where this file conflicts with DESIGN-P2-R02/R01 or earlier P2 API examples, this revision wins. Historical revisions and review evidence remain immutable history.

## 1. Compatibility baseline

P2 public production contracts MUST compile under the repository root setting `maven.compiler.release=8`. Java 9+ collection factories and Java 14+ records are forbidden in production API contracts for this project.

The following are therefore not valid P2 production-contract syntax:

- `record` declarations;
- `Map.of`, `Map.copyOf`, `List.of`, `List.copyOf`, `Set.of`, `Set.copyOf`;
- any other API unavailable to Java 8 bytecode/source compatibility.

P2 value objects use Java 8 `final class` implementations with:

- `private final` fields;
- constructor validation;
- record-style accessor names where useful for continuity (`systemKey()`, `operation()`, etc.);
- value-based `equals`, `hashCode`, and diagnostic-safe `toString`;
- defensive copy at construction;
- `Collections.unmodifiableList/Map/Set` over fresh copies for published collections.

## 2. Java 8 frozen value-object shape

The exact package may follow the existing `dec-core-context` package layout, but the public semantic shape below is frozen.

```java
public final class ModelAccessRuleKey {
    private final SystemKey systemKey;
    private final DefinitionKey targetKey;
    private final CanonicalModelPath modelPath;
    private final AccessOperation operation;

    public ModelAccessRuleKey(
            SystemKey systemKey,
            DefinitionKey targetKey,
            CanonicalModelPath modelPath,
            AccessOperation operation) { /* require non-null */ }

    public SystemKey systemKey() { return systemKey; }
    public DefinitionKey targetKey() { return targetKey; }
    public CanonicalModelPath modelPath() { return modelPath; }
    public AccessOperation operation() { return operation; }

    // value equals/hashCode; safe toString
}
```

```java
public final class CompiledSystem {
    private final SystemKey key;
    private final SourceRef sourceRef;
    private final List<DefinitionKey> memberKeys;
    private final List<RuleViewKey> ruleViewKeys;
    private final List<ModelAccessRuleKey> accessRuleKeys;

    public CompiledSystem(
            SystemKey key,
            SourceRef sourceRef,
            List<DefinitionKey> memberKeys,
            List<RuleViewKey> ruleViewKeys,
            List<ModelAccessRuleKey> accessRuleKeys) {
        // require non-null elements; copy each list into a new ArrayList
        // and expose only Collections.unmodifiableList(copy)
    }

    public SystemKey key() { return key; }
    public SourceRef sourceRef() { return sourceRef; }
    public List<DefinitionKey> memberKeys() { return memberKeys; }
    public List<RuleViewKey> ruleViewKeys() { return ruleViewKeys; }
    public List<ModelAccessRuleKey> accessRuleKeys() { return accessRuleKeys; }

    // value equals/hashCode; safe toString
}
```

`CompiledSystem.accessRuleKeys()` is therefore explicitly `List<ModelAccessRuleKey>` and not an untyped list.

## 3. Deeply immutable RuntimeFacts contract

`Map<String,Object>` is NOT the P2 public contract because an unmodifiable outer map does not make nested mutable values immutable.

P2 freezes runtime facts as:

```java
public final class RuntimeFacts {
    private final Map<String, RuntimeFactValue> values;

    public RuntimeFacts(Map<String, RuntimeFactValue> values) {
        // reject null key/value; copy into new LinkedHashMap;
        // expose Collections.unmodifiableMap(copy)
    }

    public Map<String, RuntimeFactValue> values() { return values; }

    public static RuntimeFacts empty() {
        return new RuntimeFacts(Collections.<String, RuntimeFactValue>emptyMap());
    }
}
```

`RuntimeFactValue` is a framework-owned deeply immutable canonical value, not an arbitrary caller-provided object:

```java
public final class RuntimeFactValue {
    public enum Kind { STRING, BOOLEAN, DECIMAL, INSTANT, LIST, OBJECT }

    // private constructor; creation only through framework factories
    public static RuntimeFactValue stringValue(String value);
    public static RuntimeFactValue booleanValue(boolean value);
    public static RuntimeFactValue decimalValue(BigDecimal value);
    public static RuntimeFactValue instantValue(Instant value);
    public static RuntimeFactValue listValue(List<RuntimeFactValue> values);
    public static RuntimeFactValue objectValue(Map<String, RuntimeFactValue> values);

    public Kind kind();
    public String canonicalForm();
}
```

Normative rules:

1. null runtime-fact keys and values are rejected; an absent fact is represented by an absent key.
2. `STRING`, `BOOLEAN`, `DECIMAL`, and `INSTANT` use immutable Java 8 value types.
3. `LIST` recursively copies elements and exposes an unmodifiable list.
4. `OBJECT` recursively copies entries, rejects nulls, and exposes an unmodifiable map.
5. arbitrary mutable POJOs, arrays, collections, maps, parser nodes, or engine state objects are not accepted as RuntimeFactValue payloads.
6. `canonicalForm()` is deterministic: object keys sort lexicographically; decimal and instant forms are normalized; list order is preserved.

This contract makes runtime authorization input stable, auditable, and safe to share with an immutable Guard.

## 4. Unified deterministic timeout contract

DESIGN-P2-R02's combination of an absolute `Instant deadline` and a separate monotonic elapsed-time rule is superseded. P2 uses a single monotonic-budget model.

```java
public interface GuardTimeSource {
    long nanoTime();
}
```

```java
public final class ModelAccessRequest {
    private final String engineContextId;
    private final SystemKey systemKey;
    private final DefinitionKey targetKey;
    private final CanonicalModelPath modelPath;
    private final AccessOperation operation;
    private final RuntimeFacts runtimeFacts;
    private final Duration timeoutBudget;

    public ModelAccessRequest(
            String engineContextId,
            SystemKey systemKey,
            DefinitionKey targetKey,
            CanonicalModelPath modelPath,
            AccessOperation operation,
            RuntimeFacts runtimeFacts,
            Duration timeoutBudget) { /* validate */ }

    public String engineContextId() { return engineContextId; }
    public SystemKey systemKey() { return systemKey; }
    public DefinitionKey targetKey() { return targetKey; }
    public CanonicalModelPath modelPath() { return modelPath; }
    public AccessOperation operation() { return operation; }
    public RuntimeFacts runtimeFacts() { return runtimeFacts; }
    public Duration timeoutBudget() { return timeoutBudget; }
}
```

Timeout semantics are frozen:

1. `timeoutBudget` is a non-null Java 8 `Duration`; zero or negative budget fails closed before evaluator invocation with `RUNTIME_EVALUATOR_TIMEOUT`.
2. Guard captures `startNanos = GuardTimeSource.nanoTime()` immediately before runtime evaluation and measures only monotonic elapsed time.
3. evaluator completion at or beyond the budget is DENY with `RUNTIME_EVALUATOR_TIMEOUT`, even when the evaluator returned ALLOW.
4. wall-clock `Instant.now()`, `System.currentTimeMillis()`, and caller sleeps are not part of the authorization oracle.
5. an upstream absolute deadline, if one exists, is converted once to a remaining `Duration` before constructing `ModelAccessRequest`; Guard itself never compares wall clock to monotonic time.
6. production composition may adapt `System.nanoTime()`; tests inject a deterministic `GuardTimeSource`.

## 5. ModelAccessDecision and evaluator/Guard contracts

Java 8 classes replace the R02 records while preserving semantics:

```java
public enum ModelAccessDecisionCode { ALLOW, DENY }

public final class ModelAccessDecision {
    private final ModelAccessDecisionCode decision;
    private final String reasonCode;
    private final Optional<ModelAccessRuleKey> policyKey;
    private final Optional<SourceRef> sourceRef;

    public ModelAccessDecision(
            ModelAccessDecisionCode decision,
            String reasonCode,
            Optional<ModelAccessRuleKey> policyKey,
            Optional<SourceRef> sourceRef) {
        // decision/reasonCode non-null; null Optional arguments normalized to Optional.empty()
    }

    public ModelAccessDecisionCode decision() { return decision; }
    public String reasonCode() { return reasonCode; }
    public Optional<ModelAccessRuleKey> policyKey() { return policyKey; }
    public Optional<SourceRef> sourceRef() { return sourceRef; }
}

public interface RuntimeFactEvaluator {
    ModelAccessDecision evaluate(CompiledSystem compiledSystem, ModelAccessRequest request);
}

public interface ModelAccessGuard {
    ModelAccessDecision authorize(ModelAccessRequest request);
}
```

All fail-closed reasons from R02 remain valid.

## 6. Preserve and extend the existing EngineContext final class

The repository's existing `dec.core.context.EngineContext` is a `public final class`. P2 MUST extend that class in a source/binary-compatible direction; it MUST NOT redesign it into an interface in this phase.

The existing public API remains valid:

```java
public final class EngineContext {
    public EngineContext(CompiledModelSet compiledModelSet);
    public CompiledModelSet compiledModelSet();
    public CompiledModelSet modelSet();
    public CoreConfigProjection projection();
}
```

P2 adds methods/constructor overloads to the same final class. The minimum additional read surface is:

```java
public final class EngineContext {
    public EngineContext(
            CompiledModelSet compiledModelSet,
            String contextId,
            ModelAccessGuard modelAccessGuard);

    public String contextId();
    public Optional<CompiledSystem> findSystem(SystemKey systemKey);
    public Optional<CompiledRuleView> findRuleView(RuleViewKey ruleViewKey);
    public Optional<CompiledRuleView> findRuleView(SystemKey systemKey, String name);
    public PolicyCompilationStatus policyCompilationStatus(ModelAccessRuleKey ruleKey);
    public ModelAccessGuard modelAccessGuard();
}
```

Compatibility/identity rules:

1. the existing single-argument constructor is retained; callers are not forced to migrate in P2.
2. the single-argument constructor creates a unique immutable `contextId` and installs the fail-closed unavailable Guard sentinel defined below. Existing `compiledModelSet()`, `modelSet()`, `projection()`, `equals`, `hashCode`, and `toString` behavior must not be broken by P2.
3. the P2 publication/composition path uses the overload that supplies an explicit context identity and the Guard bound to the same published model/policy index.
4. `findRuleView(String bareName)` is not added. Owner-qualified lookup remains mandatory.
5. Guard validates `request.engineContextId()` against its bound context identity before policy/evaluator access; mismatch returns DENY/`CONTEXT_IDENTITY_MISMATCH` and does not invoke evaluator.

Any proposal to turn `EngineContext` into an interface or remove/rename existing public members is a separate breaking API migration and is out of scope for P2 unless a new impact/migration design is approved.

## 7. Observable Guard-unavailable seam

`EngineContext.modelAccessGuard()` is non-null. Guard unavailability is represented by a concrete fail-closed sentinel, not by nullable API state.

```java
public final class UnavailableModelAccessGuard implements ModelAccessGuard {
    private final String boundContextId;

    public ModelAccessDecision authorize(ModelAccessRequest request) {
        // always DENY with GUARD_UNAVAILABLE before any protected operation
    }
}
```

Normative assembly behavior:

- if P2 Guard composition cannot produce a usable Guard, protected access receives `UnavailableModelAccessGuard` and therefore deterministically returns `DENY / GUARD_UNAVAILABLE`;
- no protected caller is allowed to interpret a missing Guard as ALLOW or to execute because Guard construction failed;
- `RUNTIME_EVALUATOR_UNAVAILABLE` remains distinct: a real Guard/policy exists but a runtime-required evaluator does not;
- tests can construct the sentinel directly or use the compatibility `EngineContext(CompiledModelSet)` path to observe `GUARD_UNAVAILABLE` without null injection.

## 8. Review and impact requirements

Before `DESIGN-P2-R03` can be PASSED, independent review must target this exact revision and verify at least:

- `ApiContractReviewAgent`: Java 8 source/API compatibility, existing EngineContext preservation, immutable RuntimeFacts domain, public optionality/reason codes;
- `ConcurrencyReviewAgent`: `GuardTimeSource`, monotonic budget semantics, immutable Guard/context publication, concurrent authorization isolation;
- `ArchitectureReviewAgent`, `BusinessModelReviewAgent`, `DevelopAgent`, `RequirementReviewAgent`, `TestDesignAgent`, `ImpactAnalysisReviewAgent`, `CrossModuleIntegrationReviewAgent` as required by the Design contract;
- `DataMigrationReviewAgent` or a contract-valid waiver for the detector-only P7 migration wording.

`FND-P2-REV-008` remains OPEN until an independent reviewer verifies this exact revision. `FND-P2-REV-004` also remains OPEN because the API contract is not considered closed until R03 is independently reviewed.

Until all P1 findings close, Implementation Plan, TDD, and Development remain BLOCKED.
