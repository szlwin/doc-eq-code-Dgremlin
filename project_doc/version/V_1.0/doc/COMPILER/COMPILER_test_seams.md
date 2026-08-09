# COMPILER P2 Test Seams

> Revision: `DESIGN-P2-R25`
> Inputs: `BM-R20 / FLOW-R11`; Impact projection `P2-IMPACT-R24`
> Status: `NEEDS_REVIEW / MACHINE_BLOCKED`

## Trusted provenance seam

Use a model-package fixture around the mandatory package-private `RuntimeModelFrameAssembler`, producing at least two immutable handles:
- handle A: provenance `(TargetKey A, CompiledTargetBinding A)`, internal actual ModelData A;
- handle B: provenance `(TargetKey B, CompiledTargetBinding B)`, internal actual ModelData B.

Assert public/reflection surface has no public/protected constructor/static wrapper capable of `RuntimeModelHandle(ModelData, binding)` or rebinding provenance, and no public ModelData accessor. `RuntimeExecutionFrameSnapshot` can only derive from trusted `RuntimeModelFrame`.

Negative substitution oracle: plan A + handle B must fail before capability/Guard/effect; handle B cannot be relabeled as A; cross-frame trusted frame cannot be relabeled by caller-supplied frame/owner/cursor; list order/metadata/raw selector inference count=0.

## API seams by owner module

- `dec-core-context`: `ProtectedAccessContextApiContractTest` compiles/reflects all CONTEXT public neutral types/results and explicit public visibility.
- `dec-core-model`: `ProtectedAccessModelApiContractTest` compiles/reflects MODEL public frame/handle/session/locator types, `RuntimeModelSession extends AutoCloseable`, and verifies trusted provenance construction/rebind surfaces are not public.
- `dec-core-starter`: `ProtectedAccessStarterApiContractTest` compiles/reflects STARTER composition/resolver/entry APIs and legally consumes CONTEXT+MODEL public contracts without reverse dependencies.

A compile/setup failure before intended assertion is `INVALID_RED`, not valid RED.

## Runtime/effect seams

Resolver exact-matches sourceTargetKey+compiled binding against sealed trusted handles; 0/1/N deterministic. MODEL-owned actual effect receives same resolved target/stamp after Guard. READ mutates zero; WRITE commits once or rollback/restores. Concurrency uses latch/barrier, never sleep.

## Case-level self-containment

TESTDESIGN-P2-R26 provides Fixture, Action, Expected, Forbidden side effects and current Flow/Failure refs for **every** blocking Case, not only group summaries. Tests must not consult R25/R24 for missing oracle semantics.

## Gate

Risk scan, same-revision TestDesign/TDD/TestEvidence Review and machine Evidence remain required before TDD execution.
