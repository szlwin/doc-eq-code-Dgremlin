# COMPILER P2 Architecture

> Revision `DESIGN-P2-R27`; inputs `BM-R20 / FLOW-R11`; parallel `P2-IMPACT-R26`; status `NEEDS_REVIEW / MACHINE_BLOCKED`.

## Compile publication boundary

`COMPILER -> CONTEXT`: compiler resolves selectors and View materialization semantics once and publishes `RuntimeBindingPlan + CompiledViewMaterializationPlan + policy` in the same immutable Context candidate. `CompiledViewMaterializationPlan` is a neutral field/relation tree; MODEL is forbidden to reconstruct it from `CompiledDefinition.normalizedBody`, XML/YAML, ViewData, ModelData.name or default Context.

## Existing production MODEL boundary

```text
real production origin object
  -> CONTEXT ModelDataFactory.createData(compiled materialization plan, origin object)
  -> actual ModelData used by existing MODEL ModelLoader/ModelContainer
  -> MODEL package-private binder freezes exact RuntimeBindingPlan + same ModelData in RuntimeModelHandle
  -> active MODEL execution root mints RuntimeModelAccessScope(frame/owner/cursor + handles)
  -> STARTER consumes the scope
```

No detached RuntimeFactValue source-copy runtime exists in current R27. Existing non-Map `originData` copy-back and Map live-value behavior remain the real production WRITE destination.

## Runtime scope / effect

MODEL mints scope IDs from the active execution root; public code cannot construct/relabel scope/frame/handles. STARTER validates the frame, begins a session from the same scope, registers trusted handles and seals it exactly as FLOW-R11 requires. STARTER owns resolver/intent/capability/Guard; MODEL owns session implementation/locator/coordination and the actual READ/WRITE + real write-back.

## Dependency direction

```text
compiler -> context        allowed
model    -> context        allowed
starter  -> context+model  allowed
context  -> compiler/model/starter forbidden
model    -> starter        forbidden
P3/P4/P6 core -> context   allowed
P3/P4/P6 core -> starter   forbidden
```

## Compatibility anchors

Existing production anchors remain the intended integration points: `ModelDataFactory.createData(name,Object)` semantics, `ModelLoader.load(String,ModelData,...)`, and `ModelContainer` success copy-back/commit/rollback lifecycle. P2 adds a typed materialization overload and trusted internal binding around those semantics; it does not replace them with a second runtime.

## Gate

No production Java/TDD/risk Evidence is claimed. Same-revision specialist Review and machine closure remain required.
