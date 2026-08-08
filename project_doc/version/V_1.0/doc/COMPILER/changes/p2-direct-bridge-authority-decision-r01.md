# P2 Direct Bridge Authority Decision

<a id="DEC-P2-DIRECT-BRIDGE-AUTHORITY-001"></a>
## DEC-P2-DIRECT-BRIDGE-AUTHORITY-001

- Status: `USER_CONFIRMED / ACTIVE_FOR_P2_CANDIDATE`
- Date: 2026-08-09
- Applies to: `BM-R12 / DESIGN-P2-R14 / TESTDESIGN-P2-R15`
- Supersedes: R12 execution-token/recognizes/claim trust model for P2 invocation
- Does not supersede: compiler-published PolicyIndex, unified Guard, fail-closed policy miss, actual-target/capability binding

## Decision

Current P2 production API uses a direct invocation shape:

```java
bridge.execute(
    requestedRuleKey,
    operation,
    frameId,
    ownerResolutionId,
    optionalCursorId);
```

The caller is permitted, for current P2, to choose the exact `ModelAccessRuleKey` and `AccessOperation` supplied on each invocation.

`AccessConsumerIrKey` remains available as provenance/diagnostic context but is **not** part of the authorization key and P2 does not require a `consumer -> ruleKey/op` binding check.

## Authorization boundary retained

This decision does **not** make the runtime default-allow. A request can ALLOW only when:

1. the exact requested key is present in the current compiler-published immutable `ModelAccessPolicyIndex`;
2. the explicit operation is consistent with that exact key/rule;
3. STATIC_ALLOW or runtime-required state is valid;
4. runtime-required proof/plan checks pass when applicable;
5. actual target + operation remain bound to the same one-shot capability through Guard/Gateway;
6. Context/frame/cursor/adapter/Guard state is valid at execution time.

Absent/invalid policy remains DENY before protected operation/effects.

## Requirement delta

`REQAN-P2-R01` states that downstream Rule/change/custom-action/future consumers must not expand compiler-declared authorization and that unprovable access must DENY.

For the current P2 candidate, this decision interprets **compiler-declared authorization** at the global exact PolicyIndex level rather than a per-consumer authorization level. Therefore:

```text
caller may select any exact ruleKey/op already published in current PolicyIndex
!=
caller may create a new permission or bypass Guard
```

This is an explicit Requirement/Design decision delta. Documents and Reviews must not claim that REQAN-P2-R01 originally froze this caller-trust model.

## Consequences

- `ProtectedExecutionToken`, `ProtectedExecutionStatePort.recognizes`, token claim/replay/lease semantics remain removed.
- Same scalar bridge arguments called twice are two independent invocations; P2 does not provide duplicate suppression/business idempotency.
- FND-019 remains about capability actual-target/operation atomic binding, not token claim atomicity.
- Test Design must not use “caller chooses another valid compiler-published rule/op” as a forged-authority negative case.
- AC-007 remains contract-only for future P3-P7 executor integration; this decision does not prove those future entrypoints are already non-bypassable.

## Future change rule

If the project later requires per-consumer permission isolation, the project must explicitly revise Requirement/Decision and introduce a consumer-aware policy binding/key or equivalent enforceable contract. That hardening must not be silently introduced during Development under the current decision.

## Gate

This Decision is a documented user-authorized input to the candidate artifacts. It is not Design Review Evidence, machine lifecycle Evidence, or a PASSED state.
