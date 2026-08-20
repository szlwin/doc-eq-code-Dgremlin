# COMPILER P2 Design/API Review Remediation

> Effective Design Revision: `DESIGN-P2-R02` (base `DESIGN-P2-R01@8875f042898c`).
> Input Business Model: `BM-R08` change pack `p2-independent-review-remediation-r08.yaml`.
> Status: `NEEDS_REVIEW`. This file is a normative P2 delta; where it conflicts with DESIGN-P2-R01 or the existing P2 API text, this revision wins.

## 1. Unified Guard boundary

Every protected runtime model access uses one sequence, regardless of static/dynamic policy:

```text
build ModelAccessRequest
-> ModelAccessGuard.authorize(request)
-> ALLOW ? perform READ/WRITE/EXECUTE : perform nothing and return DENY
```

There is no caller-side `if RuntimeGuardRequired then authorize` branch. `STATIC_ALLOW` is an optimization **inside** `ModelAccessGuard`; it still proves that the request reached the common Guard seam. For `STATIC_ALLOW`, `RuntimeFactEvaluator` is not invoked. Rule, change, custom action, query/read and any future protected entry point may not substitute or bypass the Guard.

Fail closed to `DENY` before side effects for: missing Guard/evaluator when required, missing policy, Context mismatch, exception, `null`, timeout/deadline expiry, unknown/undecidable result, invalid request identity, or any non-ALLOW evaluator outcome.

## 2. Frozen Java-facing P2 contracts

The following signatures/types are the minimum frozen contract. Implementation may add internal helpers but must not change these semantics during Development.

```java
public record ModelAccessRuleKey(
    SystemKey systemKey,
    DefinitionKey targetKey,
    CanonicalModelPath modelPath,
    AccessOperation operation
) {}

public record CompiledSystem(
    SystemKey key,
    SourceRef sourceRef,
    List<DefinitionKey> memberKeys,
    List<RuleViewKey> ruleViewKeys,
    List<ModelAccessRuleKey> accessRuleKeys
) {}

public record RuntimeFacts(Map<String, Object> values) {
    public RuntimeFacts {
        values = Map.copyOf(values);
    }
    public static RuntimeFacts empty() { return new RuntimeFacts(Map.of()); }
}

public record ModelAccessRequest(
    String engineContextId,
    SystemKey systemKey,
    DefinitionKey targetKey,
    CanonicalModelPath modelPath,
    AccessOperation operation,
    RuntimeFacts runtimeFacts,
    Instant deadline
) {}

public enum ModelAccessDecisionCode { ALLOW, DENY }

public record ModelAccessDecision(
    ModelAccessDecisionCode decision,
    String reasonCode,
    Optional<ModelAccessRuleKey> policyKey,
    Optional<SourceRef> sourceRef
) {
    public ModelAccessDecision {
        policyKey = policyKey == null ? Optional.empty() : policyKey;
        sourceRef = sourceRef == null ? Optional.empty() : sourceRef;
    }
}

public interface RuntimeFactEvaluator {
    ModelAccessDecision evaluate(CompiledSystem compiledSystem, ModelAccessRequest request);
}

public interface ModelAccessGuard {
    ModelAccessDecision authorize(ModelAccessRequest request);
}
```

### 2.1 Timeout/deadline ownership

`ModelAccessGuard` owns the timeout boundary. The caller supplies an absolute `Instant deadline` generated from the request's execution budget; the Guard owns a monotonic elapsed-time check around evaluator invocation and treats an already-expired deadline or evaluator completion after the deadline as DENY. The evaluator does not create, extend or reinterpret the deadline. A Guard implementation may use an injected `Clock`/monotonic time source internally for deterministic tests; this timing dependency is owned by the Guard composition, not by Rule/change/custom-action callers.

A `RUNTIME_GUARD_REQUIRED` policy with no evaluator is `DENY` (`RUNTIME_EVALUATOR_UNAVAILABLE`). A `STATIC_ALLOW` policy does not require an evaluator and invokes it zero times.

### 2.2 Optional fields

`policyKey` and `sourceRef` are always non-null `Optional` values. They are present when an exact policy was resolved; they may be empty for failures that occur before policy resolution (for example Context identity mismatch). `reasonCode` is non-null and stable for both ALLOW and DENY.

## 3. EngineContext P2 read surface

The public P2 read surface is frozen to owner-qualified lookup; no bare-name overload is permitted:

```java
public interface EngineContext {
    String contextId();
    Optional<CompiledSystem> findSystem(SystemKey systemKey);
    Optional<CompiledRuleView> findRuleView(RuleViewKey ruleViewKey);
    default Optional<CompiledRuleView> findRuleView(SystemKey systemKey, String name) {
        return findRuleView(new RuleViewKey(systemKey, name));
    }
    PolicyCompilationStatus policyCompilationStatus(ModelAccessRuleKey ruleKey);
    ModelAccessGuard modelAccessGuard();
}
```

`EngineContext` exposes the Guard associated with the same published `CompiledModelSet`; a Guard from another Context is invalid and must DENY on identity mismatch. `findRuleView(String bareName)` is forbidden for new P2 consumers.

## 4. Stable denial reasons

At minimum the Guard contract must distinguish:

- `POLICY_NOT_FOUND`
- `CONTEXT_IDENTITY_MISMATCH`
- `GUARD_UNAVAILABLE` (composition/startup failure; protected access must not execute)
- `RUNTIME_EVALUATOR_UNAVAILABLE`
- `RUNTIME_EVALUATOR_EXCEPTION`
- `RUNTIME_EVALUATOR_NULL`
- `RUNTIME_EVALUATOR_TIMEOUT`
- `RUNTIME_EVALUATOR_UNKNOWN`
- `STATIC_ALLOW`
- `RUNTIME_ALLOW`
- `RUNTIME_DENY`

## 5. Risk-driven Review closure

The current detector output is authoritative and must not be silently erased. For the new Design revision:

| Risk | Required action before Design PASSED |
|---|---|
| `concurrency` confidence 8 | `ConcurrencyReviewAgent` independent review; verify Context/Guard immutable publication and timeout/concurrent authorization semantics. |
| `data_migration` confidence 9 | `DataMigrationReviewAgent` independent review, or a formal waiver proving detector hit is only the P7 legacy-boundary wording and no data migration is performed in P2. |
| `api_contract` | `ApiContractReviewAgent` independent review because this revision freezes cross-module public contracts. |
| architecture / impact / cross-module | preserve and rerun required reviewers on `DESIGN-P2-R02`. |
| security | carry into the stage(s) allowed by the RC9 reviewer catalog, especially Test Design/TDD; do not claim it was executed at an unsupported Design stage. |

`risk_triggers` for this revision are therefore non-empty: `concurrency`, `data_migration`, `api_contract`, `architecture_change`, `impact_analysis`, `cross_module_integration`, `security`.

Because the local common-develop RC9 baseline guard reports `INVALID_BASELINE` due to missing tag `common-develop-v2.44-rc9`, this remediation does **not** fabricate these specialist Review results. The revision remains `NEEDS_REVIEW` until they can be executed under a valid baseline or an allowed waiver is recorded.

## 6. Declaration compatibility boundary

The P2 surviving compatibility surface is the read-only legacy `ConfigInfo.getRuleViewInfo(String)` / `DataUtil.getRuleViewInfo(String)` boundary. It is not the retired `DEC-EXPAND-DECLARATION` module. P2 must not restore the retired module, add it back to the reactor, or create a second declaration runtime. Retirement of the surviving read adapter remains a P7 concern.
