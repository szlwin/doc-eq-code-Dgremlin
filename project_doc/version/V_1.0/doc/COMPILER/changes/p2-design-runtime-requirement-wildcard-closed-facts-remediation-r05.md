# COMPILER P2 Design/API Remediation R05

> Effective Design Revision: `DESIGN-P2-R05`.
> Base: `DESIGN-P2-R04` (`p2-design-runtimefacts-timeout-remediation-r04.md`).
> Input Business Model: `BM-R08`.
> Status: `NEEDS_REVIEW`.
> This is a normative delta. Where it conflicts with R04/R03/R02/R01 or earlier P2 API examples, R05 wins. Historical revisions and Review Evidence remain immutable history.

## 1. Why R05 is required

Independent Review of `DESIGN-P2-R04` found three implementation-critical gaps:

1. Guard selected an exact model-access policy but `RuntimeFactEvaluator.evaluate(CompiledSystem, ModelAccessRequest)` did not receive the selected rule or its runtime requirement. Development would have to rediscover policy or invent hidden state.
2. The real contract fixture `dec-demo/src/main/resources/mix/system/systems.xml` contains `<read path="*"/>` for `order` and `payment`, while runtime `ModelPath`/policy lookup is exact-only. The source wildcard-to-runtime exact-fact transformation was not frozen.
3. `public abstract class RuntimeFactValue` did not actually close the set of value implementations under Java 8; external subclasses could violate framework-owned immutability and visitor exhaustiveness.

R05 fixes these gaps without changing the R04 Java-8/EngineContext/Guard timeout decisions.

## 2. Selected rule and runtime requirement are explicit evaluator inputs

### 2.1 Business declaration versus compiled authorization rule

`ModelAccessRule` remains the Business Model declaration fact from BM-R08. Design introduces an immutable compiled representation used by Guard:

```java
public final class CompiledModelAccessRule {
    private final ModelAccessRuleKey key;
    private final AccessCompilationStatus status;
    private final RuntimeAccessRequirement runtimeRequirement; // nullable only for STATIC_ALLOW
    private final SourceRef sourceRef;

    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public SourceRef sourceRef();
}
```

Invariants:

- `STATIC_ALLOW` => `runtimeRequirement()` is empty.
- `RUNTIME_GUARD_REQUIRED` => `runtimeRequirement()` contains exactly one immutable `RuntimeAccessRequirement`.
- a rule may not reach `RUNTIME_GUARD_REQUIRED` without a deterministic requirement. If dynamic authorization is required but no explicit/derivable requirement exists, compilation emits `MIX-MODEL-ACCESS-RUNTIME-REQUIREMENT-MISSING` and blocks publication.
- `CompiledModelAccessRule.key()` is the exact key already selected by Guard; it is not a hint for a second lookup.

### 2.2 Closed, deterministic runtime requirement model

P2 runtime authorization MUST NOT execute arbitrary user-provided Java predicates. The compiled requirement is a framework-owned immutable value tree:

```java
public final class RuntimeAccessRequirement {
    private final RuntimeRequirementKey key;
    private final RuntimePredicate predicate;
    private final SourceRef sourceRef;

    private RuntimeAccessRequirement(
        RuntimeRequirementKey key,
        RuntimePredicate predicate,
        SourceRef sourceRef);

    public static RuntimeAccessRequirement of(
        RuntimePredicate predicate,
        SourceRef sourceRef);
    public RuntimeRequirementKey key();
    public RuntimePredicate predicate();
    public SourceRef sourceRef();
    public String canonicalForm();
}

public final class RuntimeRequirementKey {
    private final String value;

    private RuntimeRequirementKey(String value);
    public String value();
}

public final class RuntimePredicate {
    public enum Kind {
        FACT_PRESENT,
        FACT_EQUALS,
        ALL_OF,
        ANY_OF,
        NOT
    }

    private final Kind kind;
    private final String factName;
    private final RuntimeFactValue expectedValue;
    private final List<RuntimePredicate> operands;

    private RuntimePredicate(Kind kind, String factName,
                             RuntimeFactValue expectedValue,
                             List<RuntimePredicate> operands);

    public static RuntimePredicate factPresent(String factName);
    public static RuntimePredicate factEquals(String factName, RuntimeFactValue expectedValue);
    public static RuntimePredicate allOf(List<RuntimePredicate> operands);
    public static RuntimePredicate anyOf(List<RuntimePredicate> operands);
    public static RuntimePredicate not(RuntimePredicate operand);

    public Kind kind();
    public Optional<String> factName();
    public Optional<RuntimeFactValue> expectedValue();
    public List<RuntimePredicate> operands();
    public String canonicalForm();
}
```

Construction rules:

- `RuntimeRequirementKey` is compiler-owned and deterministic: `rrq:` + lowercase SHA-256 of `predicate.canonicalForm()`; callers do not choose or override the key.
- fact names are non-empty canonical strings.
- `FACT_PRESENT` has exactly one fact name and no expected value/operands.
- `FACT_EQUALS` has exactly one fact name and one immutable expected value.
- `ALL_OF`/`ANY_OF` have at least one operand, preserve semantic operand order only after deterministic canonical sorting, and contain no leaf fields.
- `NOT` has exactly one operand.
- nested lists are defensively copied and exposed as unmodifiable views.
- canonical form is deterministic and is part of semantic digest input.
- `factName()` is present only for FACT_PRESENT/FACT_EQUALS; `expectedValue()` is present only for FACT_EQUALS; `operands()` is empty for leaf kinds, contains >=1 item for ALL_OF/ANY_OF, and exactly one item for NOT.
- two requirements with different predicate semantics have different `RuntimeRequirementKey` values and are distinct authorization facts even when request target/path/operation and RuntimeFacts are otherwise identical.

This closed P2 predicate set is deliberately small. A later revision may extend it only through a new Requirement/Design revision and tests; Development must not add hidden predicate kinds.

### 2.2.1 Requirement provenance: no hidden dynamic policy

The current P2 `systems.xml` model-access grammar declares permission surfaces (`read`/`write` + path/ref) but does **not** declare a runtime predicate language. Therefore R05 forbids the compiler or evaluator from inventing a runtime requirement from target names, runtime object shape, ambient identity, evaluator configuration, or an implicit PolicyIndex lookup.

For the current P2 source contract:

- an exact declared permission whose authorization is fully determined at compile time becomes `STATIC_ALLOW`;
- if a compiler path concludes that runtime authorization is required, it may emit `RUNTIME_GUARD_REQUIRED` **only when its canonical input already contains an explicit `RuntimeAccessRequirement` declaration accepted by a requirement/design contract**;
- the present `systems.xml` fixture contains no such runtime-requirement declaration, so it must never become `RUNTIME_GUARD_REQUIRED` through a hidden heuristic;
- if runtime authorization is required but no explicit requirement declaration exists, compilation emits `MIX-MODEL-ACCESS-RUNTIME-REQUIREMENT-MISSING` and blocks publication;
- adding XML/YAML syntax for runtime predicates is out of scope for this revision and requires a new Requirement/Design revision before implementation.

This makes the API contract statement “declared runtime requirement + request facts” executable: the selected compiled rule owns the declared requirement, and the evaluator receives that selected rule directly.

### 2.3 Evaluator receives the exact selected rule

R04's evaluator signature is superseded by:

```java
public interface RuntimeFactEvaluator {
    ModelAccessDecision evaluate(
        CompiledSystem system,
        CompiledModelAccessRule selectedRule,
        ModelAccessRequest request);
}
```

Guard sequence for every protected request:

1. validate context identity and owner-qualified request key;
2. perform one exact policy lookup and obtain `CompiledModelAccessRule selectedRule`;
3. verify `selectedRule.key()` equals the request-derived exact key;
4. `STATIC_ALLOW`: return ALLOW inside Guard without evaluator submission;
5. `RUNTIME_GUARD_REQUIRED`: pass **that exact `selectedRule`** plus request to the R04 bounded evaluation executor;
6. evaluator reads only `selectedRule.runtimeRequirement()` and `request.runtimeFacts()` plus immutable `CompiledSystem` facts needed by the declared predicate semantics;
7. evaluator MUST NOT access/re-query `PolicyIndex`, select a different rule, use global mutable policy state, or infer a requirement from target names;
8. missing/malformed requirement, key mismatch, evaluator null/throw/timeout/rejection/unavailable all fail closed before the protected operation.

A different selected requirement over the same RuntimeFacts is allowed to produce a different decision. This is required behavior, not nondeterminism.

## 3. RuntimeFactValue is a closed Java-8 value type

R04's `public abstract class RuntimeFactValue` is superseded. R05 freezes a single final tagged-value class:

```java
public final class RuntimeFactValue {
    public enum Kind {
        STRING,
        BOOLEAN,
        DECIMAL,
        INSTANT,
        LIST,
        OBJECT
    }

    private final Kind kind;
    private final Object payload;

    private RuntimeFactValue(Kind kind, Object payload);

    public static RuntimeFactValue stringValue(String value);
    public static RuntimeFactValue booleanValue(boolean value);
    public static RuntimeFactValue decimalValue(BigDecimal value);
    public static RuntimeFactValue instantValue(Instant value);
    public static RuntimeFactValue listValue(List<RuntimeFactValue> values);
    public static RuntimeFactValue objectValue(Map<String, RuntimeFactValue> values);

    public Kind kind();
    public <R> R accept(RuntimeFactValueVisitor<R> visitor);
    public String canonicalForm();
}
```

Normative constraints:

- class is `public final`; constructor is `private`; there is no protected/package extension constructor.
- the only public construction paths are the six static factories.
- `payload` is never returned as `Object` and is never visible to callers.
- LIST/OBJECT factory input is recursively validated, copied, and frozen; object keys are non-null/non-empty and canonical ordering is deterministic.
- `accept` switches exhaustively on the internal `Kind` and invokes exactly one typed visitor method.
- unknown/custom kinds and external subclasses are impossible without a new framework release.
- `equals`/`hashCode` use kind + canonical immutable semantic payload, not object identity.
- `canonicalForm()` is serialization/digest material only; evaluator logic must use the typed visitor/RuntimeFacts APIs and never parse it.

The R04 `RuntimeFactValueVisitor<R>` typed methods remain normative.

## 4. Source `read path="*"` is compile-time syntax only

### 4.1 Scope of wildcard compatibility

The real contract fixture contains:

- `order / OrderInfo / <read path="*"/>`
- `payment / OrderInfo / <read path="*"/>`

R05 therefore recognizes exactly one source wildcard form for P2 compatibility:

- `SharedModelPath("*")` is legal **only for READ declarations at source/compile time**.
- wildcard WRITE or EXECUTE is rejected with `MIX-MODEL-PATH-WILDCARD-OPERATION-UNSUPPORTED`.
- wildcard never becomes a runtime `ModelPath`, `ModelAccessRuleKey`, or PolicyIndex key.
- runtime lookup remains exact-only; there is no wildcard, prefix, suffix, parent, child, or cross-target fallback.

### 4.2 Deterministic expansion

After the target model/View structure is fully compiled but before authorization rules and `semanticDigest` are frozen, `ModelPathCompiler` obtains an immutable `CompiledTargetPathCatalog` for the exact selected target.

`CompiledTargetPathCatalog` contains every independently addressable canonical path under that target, including exact root when the runtime can issue a root read and all exact container/leaf paths that may be read through the target. Each path is a normal exact `ModelPath`.

For source `<read path="*"/>`:

1. resolve the exact target first; target ambiguity/absence is an ERROR before expansion;
2. enumerate the target catalog only—never another target or System;
3. sort by canonical `ModelPath` string;
4. create one READ `CompiledModelAccessRule` per exact path;
5. deduplicate overlap with explicitly declared identical exact READ rules by `ModelAccessRuleKey` while retaining all contributing SourceRefs as provenance;
6. if the catalog yields no readable exact path, emit `MIX-MODEL-PATH-WILDCARD-EMPTY` and block publication;
7. freeze the resulting exact rule set into PolicyIndex.

No runtime re-expansion is allowed.

### 4.3 Digest and model-shape behavior

Semantic digest input includes:

- the canonical target identity;
- the sorted expanded exact `ModelAccessRuleKey` set;
- the target path-catalog/model-shape digest used for expansion;
- each runtime requirement `RuntimeRequirementKey` + canonical predicate where present;
- stable SourceRef/provenance normalization where required by the existing digest contract.

If target shape changes, the configuration must be recompiled. The new compile repeats expansion against the new frozen catalog and therefore changes the semantic digest whenever the effective exact authorization set changes. A previously published EngineContext is immutable and is never silently widened by a later model-shape change.

## 5. Compile diagnostics and fail-closed rules

R05 adds/fixes the following stable Design diagnostics:

| Code | Condition | Result |
|---|---|---|
| `MIX-MODEL-ACCESS-RUNTIME-REQUIREMENT-MISSING` | rule requires runtime authorization but no deterministic requirement can be compiled | ERROR; no publication |
| `MIX-MODEL-ACCESS-RUNTIME-REQUIREMENT-INVALID` | requirement/predicate shape is invalid | ERROR; no publication |
| `MIX-MODEL-PATH-WILDCARD-OPERATION-UNSUPPORTED` | `*` is used for WRITE/EXECUTE | ERROR; no publication |
| `MIX-MODEL-PATH-WILDCARD-EMPTY` | READ `*` expands to no exact readable path | ERROR; no publication |

Existing R03/R04 runtime fail-closed reasons remain unchanged.

## 6. Compatibility and ownership

- Existing `public final class EngineContext`, its original constructor and original accessors remain unchanged as frozen in R03/R04.
- All public production API remains Java 8 source/bytecode compatible (`maven.compiler.release=8`).
- Guard remains the only protected operation authorization seam.
- PolicyIndex remains immutable and exact-keyed.
- RuntimeFactEvaluator remains pure/read-only and has no authority to perform the protected target operation.
- No new global registry/current context/runtime authority is introduced.
- `LEGACY-DECLARATION-SYSTEM-COMPAT` remains the surviving read-only P7 compatibility boundary; `DEC-EXPAND-DECLARATION` is not restored.

## 7. Required exact-revision review

`DESIGN-P2-R05` is **NEEDS_REVIEW**, not PASSED.

Before Design can pass, exact R05 review must cover at least:

- `ApiContractReviewAgent`: selected-rule/evaluator contract, closed values, Java 8 API compatibility;
- `ConcurrencyReviewAgent`: R04 bounded executor/Future timeout/cancellation remains valid with selected-rule passing;
- `ArchitectureReviewAgent`: Guard/PolicyIndex/compiler ownership and no hidden policy lookup;
- `BusinessModelReviewAgent` and `RequirementReviewAgent`: runtime requirement semantics and real `systems.xml` wildcard compatibility preserve BM/requirement intent;
- `ImpactAnalysisReviewAgent` and `CrossModuleIntegrationReviewAgent`: compiler/context/starter/model-path impacts and current BM-R08 relationship facts;
- `TestDesignAgent`: R06 cases cover requirement selection, wildcard expansion, closed RuntimeFactValue and valid RED;
- `DataMigrationReviewAgent` or a contract-valid waiver if current RC9 risk detection still triggers data migration;
- every additional reviewer triggered by a **machine-valid current-revision RC9 risk scan**.

No existing finding is closed merely because R05 exists. `IMPLEMENTATION_PLAN`, `TDD`, and `DEVELOPMENT` remain blocked until all effective P1 findings close.
