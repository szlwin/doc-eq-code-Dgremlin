# COMPILER P2 Design/API Remediation R04

> Effective Design Revision: `DESIGN-P2-R04`.
> Base: `DESIGN-P2-R03` (`p2-design-java8-api-compat-remediation-r03.md`).
> Input Business Model: `BM-R08` plus canonical `dependency_impact.yaml` at BM-R08.
> Status: `NEEDS_REVIEW`.
> This is a normative delta. Where it conflicts with R03/R02/R01 or earlier P2 API examples, this revision wins. Historical revisions and Review Evidence remain immutable history.

## 1. Why R04 is required

R03 corrected Java 8 source compatibility and preserved the existing `EngineContext final class`, but two implementation-critical contracts were still incomplete:

1. `RuntimeFactValue` exposed only factories, `kind()` and `canonicalForm()`, leaving `RuntimeFactEvaluator` without a type-safe read contract.
2. a synchronous evaluator followed by an elapsed-time check can only detect a late return; it cannot force a fail-closed timeout when evaluation blocks and never returns.

R04 freezes both contracts so Development does not reinterpret them.

## 2. RuntimeFactValue typed read contract

`canonicalForm()` is for deterministic audit/digest/debug representation only. Authorization logic MUST NOT parse `canonicalForm()` to recover typed values.

The Java 8 public contract is:

```java
public abstract class RuntimeFactValue {
    public enum Kind { STRING, BOOLEAN, DECIMAL, INSTANT, LIST, OBJECT }

    public abstract Kind kind();
    public abstract <R> R accept(RuntimeFactValueVisitor<R> visitor);
    public abstract String canonicalForm();

    public static RuntimeFactValue stringValue(String value);
    public static RuntimeFactValue booleanValue(boolean value);
    public static RuntimeFactValue decimalValue(BigDecimal value);
    public static RuntimeFactValue instantValue(Instant value);
    public static RuntimeFactValue listValue(List<RuntimeFactValue> values);
    public static RuntimeFactValue objectValue(Map<String, RuntimeFactValue> values);
}

public interface RuntimeFactValueVisitor<R> {
    R visitString(String value);
    R visitBoolean(boolean value);
    R visitDecimal(BigDecimal value);
    R visitInstant(Instant value);
    R visitList(List<RuntimeFactValue> value);
    R visitObject(Map<String, RuntimeFactValue> value);
}
```

Normative rules:

- `accept(...)` is the only framework-neutral typed extraction seam required by P2; evaluators may switch on `kind()` only for dispatch but MUST consume the value through the typed visitor contract.
- visitor arguments are deeply immutable values/views. LIST and OBJECT are recursively defensive-copied and unmodifiable.
- factories reject null and arbitrary mutable POJOs/arrays/parser nodes/engine state.
- OBJECT keys are non-null canonical strings and exposed in deterministic key order.
- `BigDecimal` canonicalization uses a normalized decimal representation; `Instant` uses a stable UTC representation; canonical serialization never substitutes for typed evaluation.
- visitor implementations are not retained by a `RuntimeFactValue` after `accept` returns.

`RuntimeFacts` remains `Map<String, RuntimeFactValue>` with defensive copy and unmodifiable deterministic iteration semantics. The minimum lookup API is also frozen:

```java
public final class RuntimeFacts {
    public Map<String, RuntimeFactValue> values();
    public Optional<RuntimeFactValue> find(String key);
    public RuntimeFactValue require(String key); // missing key -> IllegalArgumentException
    public static RuntimeFacts empty();
}
```

A missing authorization fact is represented by an absent key; a missing fact must never silently become a null or default-ALLOW value.

## 3. Enforceable timeout execution boundary

The P2 Guard owns runtime-evaluator execution. It MUST NOT invoke a runtime-required evaluator directly on the protected caller thread.

Java 8 contracts:

```java
public interface GuardTimeSource {
    long nanoTime();
}

public interface GuardEvaluationExecutor {
    Future<ModelAccessDecision> submit(Callable<ModelAccessDecision> evaluation);
}

public interface RuntimeFactEvaluator {
    ModelAccessDecision evaluate(CompiledSystem compiledSystem, ModelAccessRequest request);
}
```

`RuntimeFactEvaluator.evaluate(...)` remains a synchronous pure evaluation function, but it is always invoked by a Guard-owned `GuardEvaluationExecutor` task for `RUNTIME_GUARD_REQUIRED` policies.

### 3.1 Guard algorithm

For a runtime-required policy the minimum algorithm is:

```text
validate request/context/policy
if timeoutBudget <= 0 -> DENY RUNTIME_EVALUATOR_TIMEOUT
submit Callable { evaluator.evaluate(compiledSystem, request) }
wait with Future.get(remainingBudgetNanos, NANOSECONDS)
ALLOW/DENY result before budget -> normalize and return
TimeoutException -> cancel(true), DENY RUNTIME_EVALUATOR_TIMEOUT
ExecutionException -> DENY RUNTIME_EVALUATOR_EXCEPTION
InterruptedException -> restore interrupt, cancel(true), DENY RUNTIME_EVALUATOR_INTERRUPTED
RejectedExecutionException / submit failure -> DENY RUNTIME_EVALUATOR_UNAVAILABLE
null result -> DENY RUNTIME_EVALUATOR_NULL
unknown/invalid result -> DENY RUNTIME_EVALUATOR_UNKNOWN
```

`remainingBudgetNanos` is calculated only from `ModelAccessRequest.timeoutBudget()` and `GuardTimeSource.nanoTime()`. Wall clock is not part of the Guard oracle.

### 3.2 Cancellation and task-loss contract

Timeout is fail-closed from the protected caller's perspective even when evaluator code is defective. To prevent a timed-out task from becoming a hidden privileged side-effect path:

- evaluator implementations are contractually pure/read-only: no business mutation, no external side effect, no publication and no write to EngineContext/Registry.
- evaluator implementations MUST honor thread interruption/cancellation at blocking boundaries.
- Guard invokes `future.cancel(true)` on timeout/interruption.
- a timed-out/cancelled evaluator result is permanently discarded; a later ALLOW cannot resurrect the protected operation.
- the Guard executor is bounded. Submission rejection fails closed; it is never converted to caller-thread execution.
- executor composition must expose health/metrics for active, queued, rejected and timed-out tasks.
- if composition detects an unhealthy executor generation (for example repeated non-terminating tasks), the Guard enters fail-closed unavailable/degraded state for new protected access until the composition layer replaces the executor generation. It must not keep granting access while capacity is exhausted.

P2 does not attempt unsafe thread termination. The security invariant is that timeout immediately denies the protected operation and any runaway evaluator has no authority to perform that operation itself.

### 3.3 STATIC_ALLOW

`STATIC_ALLOW` still enters `ModelAccessGuard.authorize(...)` exactly once and never submits an evaluator task. Guard-executor invocation count and evaluator invocation count are both zero for STATIC_ALLOW.

## 4. Request and decision contracts

`ModelAccessRequest.timeoutBudget()` remains a non-null Java 8 `Duration`. Conversion from an upstream absolute deadline happens before request construction and is outside `ModelAccessGuard`.

Stable denial reasons include the R03/R02 reasons plus:

- `RUNTIME_EVALUATOR_INTERRUPTED`
- `RUNTIME_EVALUATOR_REJECTED` may be used only if the project chooses to distinguish executor rejection from generic `RUNTIME_EVALUATOR_UNAVAILABLE`; whichever stable code is selected must be frozen before TDD and used consistently by Design/Test Design.

R04 chooses the canonical P2 code `RUNTIME_EVALUATOR_UNAVAILABLE` for submit/rejection/unavailable-executor conditions to avoid an unnecessary public-code split. `RUNTIME_EVALUATOR_INTERRUPTED` remains distinct because the caller thread interrupt is restored.

## 5. EngineContext and Guard composition

R03's existing-API compatibility remains unchanged. `EngineContext` remains the existing `public final class`.

The P2-aware composition path binds together, for one immutable context identity:

- published `CompiledModelSet` / `ModelAccessPolicyIndex`;
- `ModelAccessGuard`;
- `RuntimeFactEvaluator` when runtime policies exist;
- bounded `GuardEvaluationExecutor`;
- `GuardTimeSource`.

The compatibility constructor still supplies the fail-closed `UnavailableModelAccessGuard`. A Guard without a usable executor for a runtime-required rule returns `DENY / RUNTIME_EVALUATOR_UNAVAILABLE`; Guard assembly failure still uses `DENY / GUARD_UNAVAILABLE`.

## 6. Concurrency and ownership invariants

- one authorization request has at most one runtime evaluation Future;
- no retry occurs implicitly inside Guard after timeout, rejection or exception;
- concurrent requests do not share mutable RuntimeFacts or request builders;
- cancellation of one Future cannot cancel another request;
- evaluator tasks cannot mutate the immutable published Context;
- timed-out task completion cannot change an already returned DENY;
- caller interruption is preserved with `Thread.currentThread().interrupt()` before DENY returns;
- executor shutdown/replacement is owned by composition/lifecycle, not Rule/change/custom-action/query callers.

## 7. Impact and review gate

`DESIGN-P2-R04` remains `NEEDS_REVIEW`. Before PASSED, exact-revision Review must include at least:

- `ApiContractReviewAgent`: visitor/lookup contract, Java 8 API, existing EngineContext compatibility;
- `ConcurrencyReviewAgent`: bounded executor, Future timeout/cancel/interruption, rejection and degraded-state behavior;
- Architecture, BusinessModel, Develop, Requirement, TestDesign, Impact and CrossModule reviewers required by the Design contract;
- DataMigration Review or contract-valid waiver if the current risk scan still detects migration wording.

`FND-P2-REV-004` remains OPEN until those reviewers accept this exact revision. `FND-P2-REV-005` remains OPEN until current-revision risk detection and its required Review/waiver closure exist.

Implementation Plan, TDD and Development remain BLOCKED while any effective P1 is OPEN.
