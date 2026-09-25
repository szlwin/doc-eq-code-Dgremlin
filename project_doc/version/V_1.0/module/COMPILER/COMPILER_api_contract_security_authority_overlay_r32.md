# COMPILER P2 API Contract — Authority Boundary / Context Provenance Overlay

> Revision `DESIGN-P2-R32`; base `DESIGN-P2-R30 + R31`.

## Public-surface rule

No supported public API may let an ordinary consumer acquire a raw `RuntimeModelOperationPort` and execute READ/WRITE without a successful `ExactModelAccessGuard` decision.

The following current seams are remediation targets, not frozen public compatibility requirements:

```text
RuntimeModelAccessScope.beginSession()
RuntimeModelAccessScope.effectProvider()
RuntimeModelEffectBindingResult.operationPort()
ResolvedProtectedReadAccess.of(target,path)
ResolvedProtectedWriteAccess.of(target,path,value,stamp)
```

Implementation may reduce visibility, replace them with internal/package-private equivalents, or require an opaque Guard-minted authorization. Compatibility is subordinate to the P0 authority invariant.

## Opaque authorization

Conceptually:

```java
// Name is illustrative; concrete name may differ.
final class ModelAccessAuthorization {
  // no public constructor/factory
  // binds READ|WRITE, rule key/path, target, session/object and context binding
}
```

Only STARTER after exact Guard ALLOW may mint it. MODEL accepts it only through an internal composition seam and must revalidate all bound facts before effect.

## Exact context binding

Conceptually:

```java
final class RuntimeContextBinding {
  // opaque exact runtime/context identity
}
```

A `RuntimeModelAccessScope`, frame/handle provenance and STARTER production composition must refer to the same exact binding. `RuntimeBindingPlan.equals()` is never sufficient for this check.

## Consumer-visible invariants

- READ requires explicit READ permission.
- WRITE requires explicit WRITE permission and the R31 frozen `RuntimeFactValue` + mutation stamp.
- READ does not imply WRITE; WRITE does not imply READ.
- undeclared operation defaults to DENY.
- `EXECUTE` remains absent/N/A for current P2.
- cross-context mismatch fails before Guard/effect composition with deterministic provenance failure.
