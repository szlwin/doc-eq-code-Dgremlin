# COMPILER P2 API Contract — Authority Boundary / Single Runtime Context Overlay

> Revision `DESIGN-P2-R33`; supersedes `COMPILER_api_contract_security_authority_overlay_r32.md` for current P2.
> Decision: `DEC-P2-SINGLE-RUNTIME-CONTEXT-001`.

## Public authority surface

No supported public API may let an ordinary consumer acquire a usable raw `RuntimeModelOperationPort` and execute READ/WRITE without a successful `ExactModelAccessGuard` decision.

The following existing seams remain remediation targets rather than frozen compatibility obligations:

```text
RuntimeModelAccessScope.beginSession()
RuntimeModelAccessScope.effectProvider()
RuntimeModelEffectBindingResult.operationPort()
ResolvedProtectedReadAccess.of(target,path)
ResolvedProtectedWriteAccess.of(target,path,value,stamp)
```

They may be removed, hidden/internalized, or made non-executable without a Guard-minted opaque authorization.

## Opaque authorization

The authorization is non-forgeable and binds:

- exact READ|WRITE operation;
- exact ModelAccessRuleKey / canonical path;
- exact resolved target;
- exact session/object;
- WRITE mutation stamp/version/value where applicable.

It does **not** require a separate exact EngineContext identity field under the single-runtime-context decision.

## Runtime lifecycle contract

For one active production runtime lifecycle/generation:

```text
bootstrap -> bind one compiler-published EngineContext -> immutable until shutdown
```

Supported APIs must not provide in-place live rebind/hot reload for that active runtime.

Configuration update requires:

```text
shutdown old runtime and close old runtime artifacts
-> start a new runtime generation
-> bind one new EngineContext
```

Compiler/test/offline code may produce multiple candidate EngineContexts in independent compilation sessions. Public compiler CAS/publication semantics are not a promise that an already-running runtime may replace its bound Context.

Multiple independent runtime instances in one JVM are allowed; each is a distinct lifecycle and owns exactly one Context.

## Cross-runtime mixing

A Scope/Frame/Handle created by one runtime generation is not a supported input to another runtime generation. Current P2 does not require a `RuntimeContextBinding` value or `Context A + Scope B -> PROVENANCE_MISMATCH` as a blocking runtime behavior.

`RuntimeBindingPlan.equals()` remains structural plan equality only and must never itself grant READ/WRITE permission.

## Consumer-visible invariants

- `ExactModelAccessGuard` remains sole READ/WRITE policy authority.
- READ requires explicit READ; WRITE requires explicit WRITE.
- READ does not imply WRITE; WRITE does not imply READ.
- undeclared operation defaults to DENY.
- protected DENY occurs before effect.
- WRITE preserves R31 mutation/value/version semantics.
- one-shot authorization replay is rejected.
- `EXECUTE` remains absent/N/A.
- active runtime Context cannot be replaced without restart/new generation.
