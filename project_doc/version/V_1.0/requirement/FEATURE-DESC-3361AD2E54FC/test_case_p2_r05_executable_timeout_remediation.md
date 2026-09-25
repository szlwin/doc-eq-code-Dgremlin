# P2 Test Design R05 — Executable Commands + Enforceable Timeout Rebind

> Revision: `TESTDESIGN-P2-R05`.
> Base: `TESTDESIGN-P2-R04`.
> Inputs: `BM-R08`, `DESIGN-P2-R04`.
> Status: `BLOCKED_BY_DESIGN_REVIEW / NEEDS_REVIEW`.
> This revision carries forward all non-conflicting R04/R03 cases and supersedes their conflicting Maven command and timeout-seam examples. Historical evidence is preserved.

## 1. Revision binding

R04 was bound to `DESIGN-P2-R03`. Because independent Review found R03 still incomplete for typed RuntimeFactValue reads and enforceable timeout, R05 rebinds every P2 Test Design oracle to `DESIGN-P2-R04`.

Until `DESIGN-P2-R04` has a valid exact-revision conclusion, R05 remains `BLOCKED_BY_DESIGN_REVIEW / NEEDS_REVIEW`.

## 2. Maven command contract — no reactor false failure

The repository uses Surefire 3.2.5. `failIfNoTests=false` does not disable the separate `-Dtest=...` no-match failure. Therefore a command that combines `-am` with a target-only `-Dtest=SomeTest` is not accepted as the formal evidence command: upstream reactor modules may have no matching specified test and fail before the target module runs.

Formal future unit-test execution uses a two-step pattern:

### Step A — build/install dependencies without running tests

```bash
./mvnw -pl <target-module> -am -DskipTests install
```

### Step B — run the named test in the target module only

```bash
./mvnw -pl <target-module> -Dtest=<TestClass> -Dsurefire.failIfNoSpecifiedTests=true test
```

Normative rules:

1. Step B MUST NOT use `-am`.
2. `surefire.failIfNoSpecifiedTests=true` remains explicit in Step B so a typo/missing target test cannot become a false green.
3. For multiple tests in the same target module, use a Surefire-supported comma-separated pattern in Step B.
4. For tests in different modules, run separate Step A/Step B pairs per owning module.
5. TDD must replace every placeholder with the actual owning module before executable evidence is accepted.
6. `TestEvidenceReviewAgent` must execute the final commands; string inspection alone is not evidence.

A permitted alternative is a deliberately reactor-wide command with `-Dsurefire.failIfNoSpecifiedTests=false`, but it is NOT the default formal P2 oracle because that setting can hide a missing target test unless an additional target-module assertion proves the specified test actually ran. R05 therefore freezes the two-step target-module-only pattern above.

## 3. Corrected representative commands

### Context Guard tests

```bash
./mvnw -pl dec-core-context -am -DskipTests install
./mvnw -pl dec-core-context -Dtest=P2ModelAccessGuardTest -Dsurefire.failIfNoSpecifiedTests=true test

./mvnw -pl dec-core-context -am -DskipTests install
./mvnw -pl dec-core-context -Dtest=P2ModelAccessGuardFailClosedTest -Dsurefire.failIfNoSpecifiedTests=true test
```

### EngineContext compatibility

```bash
./mvnw -pl dec-core-context -am -DskipTests install
./mvnw -pl dec-core-context -Dtest=P2EngineContextCompatibilityTest -Dsurefire.failIfNoSpecifiedTests=true test
```

### RuntimeFacts typed/immutable contract

```bash
./mvnw -pl dec-core-context -am -DskipTests install
./mvnw -pl dec-core-context -Dtest=P2RuntimeFactsContractTest,P2RuntimeFactsImmutabilityTest -Dsurefire.failIfNoSpecifiedTests=true test
```

### Compiler RuleView diagnostic

```bash
./mvnw -pl dec-core-compiler -am -DskipTests install
./mvnw -pl dec-core-compiler -Dtest=P2RuleViewSystemRequiredTest -Dsurefire.failIfNoSpecifiedTests=true test
```

### Legacy declaration boundary architecture

```bash
./mvnw -pl dec-core-compiler -am -DskipTests install
./mvnw -pl dec-core-compiler -Dtest=P2LegacyBoundaryArchitectureTest -Dsurefire.failIfNoSpecifiedTests=true test
```

## 4. RuntimeFactValue typed read case

### CASE-P2-TD-RUNTIME-FACT-VISITOR-001-R05

Purpose: prove runtime authorization can consume facts without reparsing `canonicalForm()` or depending on private representation.

Parameterized kinds:

- STRING
- BOOLEAN
- DECIMAL
- INSTANT
- LIST
- OBJECT

Oracles:

1. `RuntimeFactValue.accept(RuntimeFactValueVisitor<R>)` dispatches exactly one matching typed method.
2. LIST/OBJECT visitor arguments are recursively immutable/unmodifiable.
3. `RuntimeFacts.find(key)` returns `Optional.empty()` for an absent fact.
4. `RuntimeFacts.require(key)` returns the exact immutable value for a present fact and fails explicitly for an absent key.
5. an evaluator can decide using typed visitor values without parsing `canonicalForm()`.
6. semantically equal facts still produce deterministic canonical forms for audit/digest purposes.

Formal command:

```bash
./mvnw -pl dec-core-context -am -DskipTests install
./mvnw -pl dec-core-context -Dtest=P2RuntimeFactVisitorTest -Dsurefire.failIfNoSpecifiedTests=true test
```

## 5. Enforceable timeout/cancellation case

### CASE-P2-TD-GUARD-ENFORCED-TIMEOUT-001-R05

Purpose: prove Guard returns fail-closed even when a runtime evaluator does not return before budget.

Use a deterministic fake `GuardTimeSource` and controllable `GuardEvaluationExecutor/Future`; do not use arbitrary wall-clock sleeps as the primary oracle.

Oracles:

| Scenario | Expected |
|---|---|
| timeout budget <= 0 | DENY / `RUNTIME_EVALUATOR_TIMEOUT`; no task submitted |
| evaluator completes before budget | evaluator ALLOW/DENY preserved |
| Future does not complete before budget | Guard returns DENY / `RUNTIME_EVALUATOR_TIMEOUT` without waiting for evaluator completion |
| timeout occurs | `future.cancel(true)` invoked exactly once |
| timed-out Future later produces ALLOW | result discarded; protected operation remains not executed |
| submit rejected/fails | DENY / `RUNTIME_EVALUATOR_UNAVAILABLE` |
| Future completes exceptionally | DENY / `RUNTIME_EVALUATOR_EXCEPTION` |
| Guard wait interrupted | Future cancelled; current thread interrupt restored; DENY / `RUNTIME_EVALUATOR_INTERRUPTED` |
| STATIC_ALLOW | Guard called once; no evaluator task submitted |

Zero-side-effect oracle remains mandatory for all DENY outcomes.

Formal command:

```bash
./mvnw -pl dec-core-context -am -DskipTests install
./mvnw -pl dec-core-context -Dtest=P2ModelAccessGuardEnforcedTimeoutTest -Dsurefire.failIfNoSpecifiedTests=true test
```

## 6. Executor boundedness/degraded-state case

### CASE-P2-TD-GUARD-EXECUTOR-HEALTH-001-R05

Oracles:

- executor submission is bounded and never falls back to running evaluator on the protected caller thread;
- rejection fails closed;
- a timed-out/cancelled evaluation cannot grant the operation later;
- repeated stuck tasks can transition Guard composition into unavailable/degraded fail-closed state for new protected access;
- replacing an unhealthy executor generation is owned by composition/lifecycle, not business callers;
- authorization requests remain isolated under concurrency.

Formal command:

```bash
./mvnw -pl dec-core-context -am -DskipTests install
./mvnw -pl dec-core-context -Dtest=P2ModelAccessGuardExecutorHealthTest -Dsurefire.failIfNoSpecifiedTests=true test
```

## 7. Carried-forward P2 security/API cases

R05 retains these corrected R04/R03 semantics:

- every protected Rule/change/custom-action/query READ/WRITE/EXECUTE enters Guard exactly once;
- STATIC_ALLOW is a Guard-internal fast path and evaluator task count is zero;
- DENY/THROW/NULL/TIMEOUT/UNKNOWN/evaluator unavailable/Guard unavailable/context mismatch/policy missing all fail closed;
- DENY means zero target operation, state mutation and external side effect;
- `EngineContext` remains the existing Java 8 `public final class` and old public API remains source-compatible;
- RuntimeFacts are deeply immutable canonical facts;
- RuleView without System emits `MIX-RULEVIEW-SYSTEM-REQUIRED` and blocks publication;
- `DEC-EXPAND-DECLARATION` remains retired; only the surviving read-only compatibility boundary exists until P7.

## 8. Review gate

Before `TESTDESIGN-P2-R05` can be PASSED:

1. `DESIGN-P2-R04` must first pass exact-revision review.
2. Run Requirement/Design/TDD/TestEvidence independent Review against exact R05.
3. `TestEvidenceReviewAgent` must actually execute every final command after TDD creates the concrete tests/modules.
4. `FND-P2-REV-006` remains OPEN until reactor-safe commands are independently executed and verified.
5. `FND-P2-REV-007` remains OPEN until the carried fail-closed matrix is independently verified.

Implementation Plan, TDD and Development remain BLOCKED while effective P1 findings remain OPEN.
