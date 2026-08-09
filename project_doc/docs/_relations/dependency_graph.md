# P2 Dependency Graph

- Project: `doc-eq-code`
- Current candidate: `BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28`
- Status: `NEEDS_REVIEW / MACHINE_BLOCKED`
- Decisions: Direct Bridge ACTIVE; AC-007 Option B ACTIVE; AccessOperation READ/WRITE-only

```text
REQAN-P2-R01@d08612768131 + Overlay R04
        |
        v
BM-R20
        |
        v
FLOW-R11
        |------------------> P2-IMPACT-R26 (parallel projection)
        v
DESIGN-P2-R27
        |
        v
TESTDESIGN-P2-R28
```

BM-R20 and FLOW-R11 remain the authoritative business semantics. R27 withdraws the R26 fresh-snapshot/open lifecycle and returns to the FLOW-R11 precondition: a MODEL-owned trusted runtime frame already exists before protected access begins.

## Compile-to-production identity chain

```text
COMPILER resolves View semantics once
 -> CONTEXT CompiledViewMaterializationPlan(ViewKey + immutable field/relation shape)
 -> existing MODEL production lifecycle receives exact RuntimeBindingPlan + real origin object
 -> CONTEXT ModelDataFactory.createData(compiledPlan, originObject) (no default Context / no NormalizedBody parse)
 -> MODEL freezes RuntimeModelHandle(plan + same actual ModelData) while loading that ModelData into the active ModelContainer
 -> MODEL execution root mints RuntimeModelAccessScope(frameId/owner/cursor + handles)
 -> STARTER validates scope.frame against captured EngineContext
 -> STARTER begins session from scope, registers trusted handles, seals session
 -> exact target -> one-shot capability -> Guard
 -> MODEL actual READ / rollback-safe WRITE over the same ModelData
 -> existing ModelContainer success path writes committed values back to the same originData object
```

Forbidden: R26 `RuntimeFactValue sourceSnapshot` as model-object source, public frame/scope identity input, runtime parsing of `CompiledDefinition.normalizedBody`, XML/YAML/ViewData/default Context identity inference, public handle wrap/rebind, or Guard bypass.

Current CMIs: `CMI-P2-COMPILE-004`, `CMI-P2-PROTECTED-ACCESS-006`.
