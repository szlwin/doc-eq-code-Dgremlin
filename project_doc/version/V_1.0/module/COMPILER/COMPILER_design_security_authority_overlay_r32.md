# COMPILER P2 Detailed Design — Authority Boundary and Exact Context Provenance Overlay

> Revision `DESIGN-P2-R32`; base `DESIGN-P2-R30 + DESIGN-P2-R31 WRITE Value Transport Overlay`.
> Effective inputs: `REQAN-P2-R01@d08612768131 + Requirement Overlay R04 + BM-R20`.
> Review origin: exact code object `7925ec4f218c167240fc12571336244e1f7849ad`, findings `P2-CR-001(P0)` and `P2-CR-002(P1)`.
> Operation scope remains `READ / WRITE only`; `EXECUTE = N/A`.

## R32-1 Problem statement

Two implementation seams violate already-frozen P2 invariants without changing Requirement/BM semantics:

1. MODEL exposes a raw effect acquisition chain (`RuntimeModelAccessScope -> effectProvider -> RuntimeModelEffectBindingResult.operationPort`) plus proofless resolved-access factories. A caller that reaches MODEL can therefore construct a target/session and invoke READ/WRITE without `ExactModelAccessGuard`.
2. STARTER decides whether a handle belongs to the captured `EngineContext` by structural `RuntimeBindingPlan.equals()`. Two contexts may carry the same plan while having different policy facts; plan equality is not context identity.

The remediation is a security-boundary closure, not a P2 redesign.

## R32-2 Unique authority mint

`ExactModelAccessGuard` remains the unique policy authority. A successful Guard decision must mint a non-forgeable, opaque effect authorization value. Ordinary callers, CONTEXT, MODEL and business consumers may carry or consume that value only through approved package-private/internal seams; they cannot construct, clone, infer or downgrade it.

Conceptual contract:

```text
ProtectedAccessInvocation
  -> exact ModelAccessRuleKey + exact resolved target
  -> ExactModelAccessGuard
  -> ALLOW
  -> mint opaque ModelAccessAuthorization
  -> MODEL effect consumes authorization
  -> revalidate session / object / target / path / operation / context binding
  -> READ or WRITE
```

The authorization binds at least:

- exact operation (`READ` or `WRITE`);
- exact `ModelAccessRuleKey` / canonical path;
- exact resolved runtime target and session/object identity;
- exact runtime-context binding identity;
- for WRITE, the frozen mutation stamp/version/value contract already defined by R31.

A boolean, public enum, caller-supplied token, hash, version number or publicly constructible `guardPassed` marker is forbidden as authority.

## R32-3 Raw MODEL effect closure

Production callers must not be able to obtain a usable raw `RuntimeModelOperationPort` without a Guard-minted authorization.

Required API direction:

- `RuntimeModelAccessScope.frame()` may remain observational if required, but `beginSession()`, raw `effectProvider()` and any raw operation-port extraction must not form a public production bypass chain.
- `RuntimeModelEffectBindingResult.operationPort()` must not expose a raw public production port to ordinary callers.
- MODEL may keep an internal/package-private operation primitive for STARTER composition and MODEL tests, but it is not a supported public consumer API.
- proofless `ResolvedProtectedReadAccess.of(target,path)` and `ResolvedProtectedWriteAccess.of(target,path,value,stamp)` cannot be sufficient executable effect requests. They must be removed/closed from production surface or converted into non-executable internal data that still requires the opaque authorization before effect.
- MODEL must reject absent, mismatched, already-consumed or wrong-operation authorization before any side effect.

Failure is deterministic and effect-free: `effectCount=0`; WRITE leaves model data/version unchanged.

## R32-4 Exact runtime Context binding

Introduce an opaque exact context/runtime binding identity (conceptually `RuntimeContextBinding`). It is minted when a runtime Scope/Frame/Handle is created from one captured `EngineContext`/compiled runtime context and is propagated through provenance.

Properties:

- identity is exact-instance/runtime-generation identity, not structural `RuntimeBindingPlan.equals()`;
- it is not derivable from source target, compiled target binding, policy digest or rule content;
- `RuntimeBindingPlan` remains a structural execution plan and may still use value equality for plan semantics, but never as authority identity;
- `RuntimeModelProvenance` (or its successor) carries the exact context binding in addition to plan/view provenance;
- STARTER must verify `capturedContext.binding == handle/scope.binding` before opening an effect session.

Required behavior:

```text
Context A + Scope A => eligible for Guard evaluation
Context A + Scope B => PROVENANCE_MISMATCH
```

The second case is rejected even when A and B have identical plans, policies or digests.

## R32-5 Authority/effect sequencing

The only legal production sequence is:

```text
capture exact EngineContext
  -> establish exact RuntimeContextBinding
  -> resolve target from that binding
  -> exact Guard lookup using READ/WRITE key
  -> mint one-shot opaque authorization
  -> bind MODEL session/effect to the same context binding
  -> consume authorization exactly once
  -> effect
```

No effect primitive may perform policy discovery on its own or infer permission from plan equality. No caller may replace the target/path/operation/context after Guard approval.

## R32-6 Failure codes and observability

Existing public failure contracts remain preferred where already defined. The remediation must preserve or introduce deterministic mapping for:

- authorization missing / wrong operation / wrong target/path -> DENY before effect;
- `Context A + Scope B` -> `PROVENANCE_MISMATCH`;
- stale/closed/mismatched session -> existing session/effect binding failure;
- stale WRITE mutation stamp -> existing WRITE stale/version failure;
- replayed one-shot authorization -> deterministic already-consumed/deny result.

All denial paths must be observable in tests without relying on exception text only.

## R32-7 Dependency direction and scope

Dependency direction stays:

```text
compiler -> context
model -> context
starter -> context + model
business consumer -> starter + context
```

Affected implementation modules are limited to `dec-core-context`, `dec-core-model`, and `dec-core-starter`, plus tests. No DB/schema, Compiler parsing, RuleView compiler, PolicyIndex ownership or P3/P4/P6 execution semantics change is authorized by this Design.

## R32-8 TDD ordering

Before any production mutation, TestDesign must define and Development must capture genuine RED for both findings on the current code:

1. raw MODEL public seam can perform WRITE without Guard;
2. same-plan cross-context Scope can pass current provenance check.

Production changes are forbidden until the RED evidence is frozen. The later GREEN must also preserve guarded READ, guarded WRITE, default DENY, READ != WRITE, mutation-version checks, Container rollback and Compiler/RuleView regressions.
