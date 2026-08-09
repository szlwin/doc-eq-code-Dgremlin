# COMPILER P2 Architecture

> Revision：`DESIGN-P2-R21`。Base：`DESIGN-P2-R20`。Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## Module direction

```text
dec-core-compiler -> dec-core-context

dec-core-model -> dec-core-context
               -> dec-core-datasource / existing model dependencies

dec-core-starter -> dec-core-compiler / frontends
                 -> dec-core-model        # new production assembly edge

P3/P4/P6 core -> dec-core-context : allowed
P3/P4/P6 core -> dec-core-starter : forbidden
application/demo -> dec-core-starter      : allowed
```

Current source supports this direction: model already consumes context; starter does not currently consume model, so adding starter->model produces no model->starter cycle.

## Authority boundaries

- source identity：shared `ViewKey -> TargetKey`；
- authorization/write authority：`ModelAccessRuleKey`；
- permission truth：immutable `ModelAccessPolicyIndex`；
- runtime object location：composition/frame-scoped sealed `RuntimeModelSession`；
- WRITE target/path：single `ModelAccessRuleKey` inside `ResolvedWriteIntent`；
- actual mutation/transaction/version：`dec-core-model`；
- enforcement/one-shot/Guard/assembly：starter。

## Runtime session lifecycle

```text
starter composition root
 -> create model session
 -> register current ModelData/runtime handles for frame
 -> seal
 -> construct production model adapter bound to session
 -> expose ProtectedAccessComposition
 -> close composition/session
```

No static/global object registry is introduced. Cross-session RuntimeObjectId lookup is invalid.

## Transaction boundary

P2 WRITE integrates with existing model transaction capability but strengthens observable-state ordering: data-source commit and working mutation must succeed before committed state is published to the externally observable origin object. Failure rolls back/restores and returns no receipt.

## Concurrency boundary

RuntimeModelSession owns per `(RuntimeObjectId, ModelPath)` serialization + `RuntimeMutationVersion`. Same-version competing writes yield exactly one commit and stale loser(s). Different object/path keys can execute independently.

## Gate

Architecture/Impact/CrossModule/Concurrency independent Review and risk scan remain required; no implementation is claimed.
