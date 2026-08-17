# P2 Single Runtime Context Decision — Design R33 / TestDesign R37 Traceability Delta

## Effective authority chain

```text
REQAN-P2-R01@d08612768131
  + Requirement Overlay R04
  + DEC-P2-SINGLE-RUNTIME-CONTEXT-001
    -> BM-R20 (unchanged)
    -> FLOW-R11 (unchanged)
    -> DESIGN-P2-R33
    -> TESTDESIGN-P2-R37
```

## Decision delta

`DEC-P2-SINGLE-RUNTIME-CONTEXT-001` establishes:

- one compiler-published `EngineContext` per production runtime lifecycle/generation;
- runtime bind-once during bootstrap;
- no live Context replacement / runtime republish / hot reload;
- configuration change requires shutdown + restart/new runtime generation;
- Compiler/tests/offline sessions may still create multiple candidate EngineContexts;
- no P2 requirement for per-Scope/Handle `RuntimeContextBinding`.

## Finding mapping

| Finding / decision | R32/R36 treatment | R33/R37 treatment |
|---|---|---|
| `P2-CR-001` raw MODEL authority bypass | P0 blocking | **P0 blocking unchanged** |
| `P2-CR-002` same-plan cross-context provenance | P1 blocking; exact RuntimeContextBinding | **SUPERSEDED_BY_ARCH_DECISION**; unsupported within one active runtime |
| `P2-TD-REV-001` future-mechanism RED misclassification | corrected by R35/R36 | preserved |
| `P2-TD-REV-002` RED harness tied to removable APIs | corrected by R36 | preserved for four active REDs |
| single runtime Context / no hot reload | absent | explicit R33 architecture + R37 lifecycle cases |

## Active TestDesign gate

Exactly four `MANDATORY_RED`:

1. `CASE-P2-TD-R34-RAW-MODEL-PORT-PUBLIC-SEAM-001`
2. `CASE-P2-TD-R34-READONLY-RAW-WRITE-BYPASS-001`
3. `CASE-P2-TD-R34-PROOFLESS-READ-ACCESS-NOT-AUTHORITY-001`
4. `CASE-P2-TD-R34-PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY-001`

Historical cases explicitly retired/superseded:

- `CASE-P2-TD-R34-SAME-PLAN-CROSS-CONTEXT-001`
- `CASE-P2-TD-R34-STRUCTURALLY-IDENTICAL-CONTEXT-IDENTITY-001`
- `CASE-P2-TD-R34-CONTEXT-BINDING-LIFETIME-001`

New lifecycle verification:

- `CASE-P2-TD-R37-SINGLE-RUNTIME-CONTEXT-BIND-ONCE-001` — GREEN_ONLY
- `CASE-P2-TD-R37-NO-HOT-RELOAD-001` — GREEN_ONLY
- `CASE-P2-TD-R37-RESTART-NEW-GENERATION-001` — REGRESSION_REQUIRED

## Upstream compatibility

Requirement Overlay R04's `atomic publication / old Context preservation / Context isolation` remains valid.

The clauses are not interpreted as an obligation to mutate a running runtime's bound Context. They cover compiler publication and isolation/preservation across compilation attempts or distinct runtime generations.

Therefore no Requirement/BM/Flow rewrite is required by this architecture decision.

## Code evidence used for Design review

- `dec-core-compiler/.../CompiledModelSetBuilder.java`: each compilation can create a fresh EngineContext candidate.
- `dec-core-starter/.../CompilerBootstrap.java`: compiler bootstrap does not own a global current Context and accepts external publication boundary.
- `dec-core-starter/.../ProtectedAccessRuntimeFactory.java`: one factory instance captures one EngineContext in a final field.
- `dec-demo/.../P2RealFixtureIntegrationTest.java`: tests can compile identical fixture multiple times, proving multi-candidate Compiler behavior without establishing hot reload.
- `dec-core-context/.../EngineContext.java`: EngineContext is immutable but value-equal contexts may be distinct instances.

## Lifecycle impact

Earliest changed lifecycle artifact is **Design**.

Required logical invalidation:

```text
Design R32 historical PASSED
 -> reopen Design
 -> Design R33 candidate + independent reviews
 -> invalidate TestDesign / Implementation Plan / Development / Code Review / Testing / Completion Verification
 -> TestDesign R37 candidate + independent reviews
 -> Implementation Plan refresh only after canonical finalization
```

No production source, test source, runtime config, merge, release or deployment mutation is authorized by this traceability delta.
