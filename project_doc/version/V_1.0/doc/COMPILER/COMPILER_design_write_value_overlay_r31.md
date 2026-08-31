# COMPILER P2 Detailed Design — WRITE Value Transport Overlay

> Revision `DESIGN-P2-R31`; base `DESIGN-P2-R30`.
> Inputs remain `REQAN-P2-R01 + Overlay R04 + BM-R20 + FLOW-R11`.
> Scope: close `P1-P2-DEV07-WRITE-VALUE-CONTRACT-GAP-001` only. All unrelated R30 decisions remain unchanged.

## R31-1 Problem

R30 preserves `ProtectedAccessPort.invoke(ProtectedAccessInvocation)` and the R29 invocation/intent factories, while the already-implemented MODEL `RuntimeModelOperationPort.write` requires a neutral `RuntimeFactValue` replacement value before the real Container effect. The R30/R29 invocation and write-intent factories contain no such value, so a real guarded WRITE cannot be represented end-to-end without inventing an implementation-only value source.

## R31-2 Decision

Add an explicit **neutral write-value transport**. `RuntimeFactValue` remains a closed immutable value DTO and is **not** a credential, ModelData, Container, Guard, provider or operation port.

- Existing R29/R30 factories remain available for compatibility.
- Add `ProtectedAccessInvocation.write(..., RuntimeFactValue writeValue)` and `Optional<RuntimeFactValue> writeValue()`.
- Add an overload `ResolvedWriteIntent.of(..., RuntimeMutationStamp stamp, RuntimeFactValue writeValue)` and `Optional<RuntimeFactValue> writeValue()`.
- `ResolvedProtectedWriteAccess.of(invocationId, intent)` exposes `value()` only when the intent carries the frozen value; missing value fails closed before MODEL effect.
- A READ invocation does not require a write value. A WRITE invocation without one maps to `WRITE_INTENT_NOT_FOUND` and effect count zero.
- Value transport does not participate in authority lookup. Guard still evaluates the exact `ModelAccessRuleKey(owner,target,path,operation)` from the invocation.

## R31-3 Frozen order

```text
ProtectedAccessInvocation
  -> exact ModelAccessRuleKey lookup
  -> exact RuntimeBindingPlan / target resolution
  -> WRITE only: freeze target/path/version + the invocation's immutable RuntimeFactValue
  -> one-shot capability
  -> Guard exact same key/target/path/proof
  -> construct ResolvedProtectedWriteAccess from the same invocation + frozen intent
  -> composition-private MODEL operation port
```

The invariant remains `resolve A -> Guard A -> effect A`. Value transport cannot select or replace target/path/version after capability freeze.

## R31-4 Failure rules

- WRITE with no value -> `WRITE_INTENT_NOT_FOUND`, effect=0.
- 0 candidate target -> `WRITE_INTENT_NOT_FOUND`, effect=0.
- N>1 candidate targets -> `WRITE_INTENT_AMBIGUOUS`, effect=0.
- stale mutation stamp -> `WRITE_INTENT_STALE`, effect=0.
- replayed capability -> `CAPABILITY_ALREADY_CONSUMED`, effect=0.
- policy/key/plan mismatch -> existing R30 denial codes, effect=0.
- invalid/unsupported `RuntimeFactValue` conversion remains fail closed in MODEL and produces no success receipt.

## R31-5 Dependency and security

No dependency direction changes:

```text
compiler -> context
model -> context
starter -> context + model
business consumer -> starter + context
```

Still forbidden: caller ModelData, caller Container, caller Guard, caller EffectProvider, caller OperationPort, business consumer -> MODEL root/effect APIs, and RuleKey/consumer identity substituting for `ModelAccessRuleKey` authority.

## R31-6 Lifecycle impact

`TESTDESIGN-P2-R32` needs one incremental R33 overlay for WRITE-value transport/fail-closed oracles. Implementation Plan R06 needs an R07 rebind so DEV-04R restores the additive CONTEXT factories/getters before DEV-07 concrete resumes. DEV-07 skeleton must be re-reviewed against R31 before concrete implementation.
