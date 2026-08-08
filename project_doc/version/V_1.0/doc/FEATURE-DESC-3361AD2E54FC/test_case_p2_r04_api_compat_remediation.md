# P2 Test Design R04 — Java 8/API Compatibility Rebind

> Revision: `TESTDESIGN-P2-R04`.
> Base: `TESTDESIGN-P2-R03`.
> Inputs: `BM-R08`, `DESIGN-P2-R03`.
> Status: `BLOCKED_BY_DESIGN_REVIEW / NEEDS_REVIEW`.
> This revision carries forward all non-conflicting R03 cases and rebinds the test design to the corrected Java 8/existing-EngineContext contract. Historical R02/R03 evidence is retained and is not treated as closure evidence for R04.

## 1. Revision-binding rule

`TESTDESIGN-P2-R03` was bound to `DESIGN-P2-R02`. Because R02 is invalidated by `FND-P2-REV-008`, R03 cannot be promoted to PASSED even when its own FND-006/FND-007 remediation content is sound.

R04 binds all future P2 tests to `DESIGN-P2-R03`. Until DESIGN-P2-R03 completes exact-revision independent review, R04 remains `BLOCKED_BY_DESIGN_REVIEW / NEEDS_REVIEW`.

All R03 command rules remain normative:

- use `./mvnw`;
- never use `+` between Surefire tests;
- split commands by module when tests belong to different modules;
- replace placeholders with actual owning modules before executable evidence is accepted.

## 2. Java 8 production API compatibility

### CASE-P2-TD-JAVA8-API-COMPAT-001-R04

**Purpose:** prove P2 public production contracts compile at the repository's `maven.compiler.release=8` target.

Oracles:

1. P2 production source contains no `record` declaration.
2. P2 production source contains no Java 9+ collection factory/copy API (`Map.of`, `Map.copyOf`, `List.of`, `List.copyOf`, `Set.of`, `Set.copyOf`).
3. New P2 value types are ordinary Java 8 `final class`/`enum`/`interface` types as frozen by DESIGN-P2-R03.
4. The owning production modules compile successfully under the root release setting without locally overriding release above 8.

Future commands after TDD creates the concrete types:

```bash
./mvnw -pl dec-core-context -am -DskipTests compile
./mvnw -pl dec-core-compiler -am -DskipTests compile
```

A test may additionally scan the compiled API/source contract, but a Java 17 build JVM is not evidence that Java 14+ source is acceptable; the Maven compiler `release=8` result is the authoritative compile oracle.

## 3. Existing EngineContext compatibility

### CASE-P2-TD-ENGINECONTEXT-COMPAT-001-R04

**Purpose:** prove P2 extends the existing final class instead of replacing it with a new interface contract.

Required reflection/compile-time oracles:

- `dec.core.context.EngineContext` remains a concrete `public final class`.
- existing constructor `EngineContext(CompiledModelSet)` remains callable.
- existing methods remain callable with the same return semantics:
  - `compiledModelSet()`
  - `modelSet()`
  - `projection()`
- P2 adds owner-qualified methods without adding `findRuleView(String bareName)`.
- P2 may add an overloaded constructor for context identity + Guard composition; it must not require old callers to migrate merely to compile.
- existing equality/hashCode behavior based on published model semantics is not silently broken by the new runtime context identity field.

Future command:

```bash
./mvnw -pl dec-core-context -am -Dtest=P2EngineContextCompatibilityTest test
```

## 4. RuntimeFacts deep immutability

### CASE-P2-TD-RUNTIME-FACTS-IMMUTABLE-001-R04

**Purpose:** prove authorization facts cannot be mutated through shallow `Map<String,Object>` aliases.

Parameterized fact kinds:

- STRING
- BOOLEAN
- DECIMAL
- INSTANT
- LIST of RuntimeFactValue
- OBJECT of RuntimeFactValue

Oracles:

1. constructor rejects null keys/values and arbitrary mutable payload objects.
2. mutating caller-owned input map/list after construction does not change `RuntimeFacts` or nested values.
3. returned map/list/object views are unmodifiable.
4. nested LIST/OBJECT values are recursively copied.
5. two semantically equal fact trees produce the same deterministic `canonicalForm()`; object insertion order does not change canonical form.
6. absent fact is represented by absent key, not mutable/null payload state.

Future command:

```bash
./mvnw -pl dec-core-context -am -Dtest=P2RuntimeFactsImmutabilityTest test
```

## 5. Deterministic monotonic timeout seam

### CASE-P2-TD-GUARD-TIME-BUDGET-001-R04

**Purpose:** freeze the single timing model from DESIGN-P2-R03 and prevent wall-clock/monotonic ambiguity.

Use a fake `GuardTimeSource` whose `nanoTime()` can be advanced deterministically without sleeping.

Oracles:

| Scenario | Expected |
|---|---|
| timeout budget is zero | DENY / `RUNTIME_EVALUATOR_TIMEOUT`; evaluator not invoked |
| timeout budget is negative | DENY / `RUNTIME_EVALUATOR_TIMEOUT`; evaluator not invoked |
| evaluator returns before budget | preserve evaluator ALLOW/DENY result |
| evaluator returns ALLOW exactly at/after budget | DENY / `RUNTIME_EVALUATOR_TIMEOUT` |
| fake monotonic time advances; wall clock does not | decision follows monotonic elapsed time only |
| caller has an upstream absolute deadline | adapter converts it to remaining `Duration` before request; Guard does not read wall clock |

Forbidden test mechanism: arbitrary `Thread.sleep` / real-time race as the primary timeout oracle.

Future command:

```bash
./mvnw -pl dec-core-context -am -Dtest=P2ModelAccessGuardTimeBudgetTest test
```

## 6. Observable Guard-unavailable branch

### CASE-P2-TD-GUARD-UNAVAILABLE-001-R04

**Purpose:** make `GUARD_UNAVAILABLE` constructible and observable rather than a textual reason code with no stable API seam.

Oracles:

1. `EngineContext.modelAccessGuard()` is non-null.
2. an `UnavailableModelAccessGuard` (or exact equivalent frozen sentinel type) always returns `DENY / GUARD_UNAVAILABLE`.
3. compatibility construction `new EngineContext(compiledModelSet)` yields a fail-closed unavailable Guard until a P2-aware composition path supplies a real Guard.
4. protected READ/WRITE/EXECUTE invoked through that sentinel performs zero target operation and zero external side effect.
5. `GUARD_UNAVAILABLE` is distinct from `RUNTIME_EVALUATOR_UNAVAILABLE`.
6. no protected caller may catch Guard assembly failure and continue execution.

Future command:

```bash
./mvnw -pl dec-core-context -am -Dtest=P2ModelAccessGuardUnavailableTest test
```

## 7. Carried-forward Guard/no-bypass and fail-closed cases

The following R03 semantics remain mandatory and are rebound to DESIGN-P2-R03:

- every protected RULE / CHANGE / CUSTOM_ACTION / QUERY_READ entry for READ / WRITE / EXECUTE calls Guard exactly once;
- STATIC_ALLOW still calls Guard once and evaluator zero times;
- runtime evaluator branches cover ALLOW, DENY, THROW, NULL, TIMEOUT, UNKNOWN, unavailable;
- context identity mismatch and policy missing fail closed before evaluator;
- all DENY outcomes prove zero business-state mutation and zero external side effect;
- RuleView missing System produces `MIX-RULEVIEW-SYSTEM-REQUIRED` and prevents publication;
- retired `DEC-EXPAND-DECLARATION` is never restored; only the surviving read-only compatibility boundary remains until P7.

Representative future commands remain:

```bash
./mvnw -pl dec-core-context -am -Dtest=P2ModelAccessGuardTest test
./mvnw -pl dec-core-context -am -Dtest=P2ModelAccessGuardFailClosedTest test
./mvnw -pl dec-core-compiler -am -Dtest=P2RuleViewSystemRequiredTest test
./mvnw -pl dec-core-compiler -am -Dtest=P2LegacyBoundaryArchitectureTest test
```

## 8. Exact-revision review gate

Before `TESTDESIGN-P2-R04` can be PASSED:

1. `DESIGN-P2-R03` must first reach a valid exact-revision review conclusion.
2. Then run independent Test Design review against exact `TESTDESIGN-P2-R04` by:
   - `RequirementReviewAgent`
   - `DesignReviewAgent`
   - `TDDReviewAgent`
   - `TestEvidenceReviewAgent`
3. Revalidate all future commands against the actual final module ownership selected by TDD.
4. Close FND-P2-REV-006/007 only after their R04-carried oracles are independently verified.

Until all effective P1 findings close, `IMPLEMENTATION_PLAN`, `TDD`, and `DEVELOPMENT` remain `BLOCKED`.
