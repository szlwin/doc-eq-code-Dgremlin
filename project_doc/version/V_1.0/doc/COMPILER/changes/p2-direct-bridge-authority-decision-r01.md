# P2 Direct Bridge Authority Decision

<a id="DEC-P2-DIRECT-BRIDGE-AUTHORITY-001"></a>
## DEC-P2-DIRECT-BRIDGE-AUTHORITY-001

- Status: `USER_CONFIRMED / ACTIVE`
- Date: 2026-08-09
- Authority decision: direct `bridge.execute(ruleKey, operation, frame, owner, cursor)`; no execution-token/claim model
- Operation scope: current P2 `AccessOperation = READ | WRITE` per `DEC-P2-ACCESS-OPERATIONS-001`
- Supersedes: R12 execution-token/recognizes/claim invocation authority model
- Does not supersede: compiler-published PolicyIndex, unified Guard, fail-closed policy miss, actual-target/operation capability binding

## Decision

```java
bridge.execute(
    requestedRuleKey,
    operation,
    frameId,
    ownerResolutionId,
    optionalCursorId);
```

Current caller may choose an exact compiler-published `ModelAccessRuleKey` and current `AccessOperation` (`READ` or `WRITE`) on each invocation. `AccessConsumerIrKey` remains provenance/diagnostic context and is not part of the authorization key.

## Authorization boundary retained

ALLOW remains possible only when the exact requested key exists in the current immutable compiler-published `ModelAccessPolicyIndex`, operation matches, policy state is valid, required runtime proof succeeds, and actual target + operation remain bound through the same one-shot capability/Gateway/Guard path. Missing or invalid authority DENY before effects.

## Requirement delta

The direct-caller trust model is an explicit user Decision. It does not permit caller-created permissions, fuzzy key fallback, alternate operation fallback, PolicyIndex mutation or Guard bypass.

## Consequences that remain ACTIVE

- `ProtectedExecutionToken`, `recognizes`, token claim/replay/lease semantics remain removed.
- Same bridge arguments invoked twice are independent invocations; business idempotency is not provided by P2.
- Consumer provenance does not alter authorization key/equality semantics.
- FND-019 concerns actual-target/operation/one-shot capability binding and concurrency, not token claim atomicity.

## Amendment — AC-007 consequence partially superseded

The original version of this file also said:

> AC-007 remains contract-only for future P3-P7 executor integration; no P3-P7 business executor implementation is pulled into P2.

That **future-only/contract-only AC-007 consequence is SUPERSEDED** by the later user Decision `DEC-P2-AC007-STAGE-BOUNDARY-001:OPTION_B`.

Current effective AC-007 is:

- P2 must provide real main-source `RuleProtectedAccessEntry`, `ChangeProtectedAccessEntry`, `CustomActionProtectedAccessEntry` representative consumers;
- they must be acquired through normal P2 production composition and execute allow/deny/no-bypass/parity acceptance through the same Bridge/Gateway/Guard seam;
- full P3/P4/P6 business engines remain downstream.

This amendment **does not supersede the Direct Bridge authority decision itself**.

## Future change rule

Per-consumer permission isolation or any operation beyond READ/WRITE requires a new explicit Requirement/Decision Review; Development must not introduce it silently.

## Gate

This Decision is a user-authorized candidate input, not Review Evidence or machine PASSED state.
