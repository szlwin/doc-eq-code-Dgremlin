# COMPILER P2 Test Seams

> Revision `DESIGN-P2-R27`; inputs `BM-R20 / FLOW-R11`; Impact `P2-IMPACT-R26`; status `NEEDS_REVIEW / MACHINE_BLOCKED`.

## Typed materialization seam

Compiler fixture publishes a `CompiledViewMaterializationPlan` for a real ViewKey with nested SCALAR/OBJECT/LIST shape. MODEL test uses the new `ModelDataFactory.createData(compiledPlan, originObject)` path and instruments accesses to prove runtime reads of `CompiledDefinition.normalizedBody`, XML/YAML, legacy ViewData, String-name View lookup and default `ConfigContextUtil` are zero.

## Existing production object / write-back seam

Use a real mutable POJO as origin object. The existing MODEL lifecycle must create the actual ModelData from the compiled plan, retain the same POJO as originData, load that same ModelData into ModelLoader/ModelContainer, and mint the trusted handle around the same reference. After Guard ALLOW and successful WRITE, `ModelContainer` completion writes the committed value back to that exact POJO. Guard DENY, stale version and injected write/commit failure leave the POJO unchanged. A test that only inspects a detached internal copy is invalid.

## Trusted scope provenance seam

A MODEL-package fixture drives the active ModelContainer execution-root integration and obtains one `RuntimeModelAccessScope`. Assert scope/frame/handle have no public/protected construction/rebind surface, frame/owner/cursor are minted by MODEL and cannot be passed into a frame request, and stale/cross-execution scope use fails before capability/Guard/effect. Invocation IDs may be caller-created, but frame/owner/cursor equality is checked against independently minted frame facts.

## FLOW-R11 session seam

STARTER `create(scope)` must perform exactly: validate trusted frame -> `scope.beginSession()` -> register every trusted handle -> seal -> resolve. Instrument MODEL session to prove registration/seal happen under STARTER orchestration and no R26 MODEL-open pre-sealed-session path exists.

## API constructibility seam

- `dec-core-compiler`: compile a contract test that actually invokes public factories for CONTEXT-owned `CompiledModelAccessRule`, `ModelAccessPolicyIndex`, `CompiledViewMaterializationPlan/Index` and related binding values.
- `dec-core-context`: verify all neutral factories/results and the typed `ModelDataFactory` overload.
- `dec-core-model`: verify trusted scope/frame/handle are intentionally non-constructible while session register/seal are public to legal STARTER consumption.
- `dec-core-starter`: compile/invoke public factories for `ProtectedAccessInvocation`, resolved access, denial/result and target-resolution outputs through legal dependencies.

Reflection-only class existence is insufficient; the test must compile and call each required construction surface.

## Failure matrix seam

Current R27 has no public MODEL-open failure algebra. Verify: missing compiled materialization descriptor makes P2 candidate publication invalid; incompatible real origin object fails MODEL load/precondition establishment and emits no trusted frame; duplicate trusted handle registration fails at FLOW STEP-02; stale scope fails before capability; internal MODEL write failure rolls back and writes back nothing. Also assert superseded R26 fresh-snapshot/open types are absent from current API.

## Concurrency/effect

Use barriers/latches, never sleep. Same actual handle/path/version commits at most once. Operation failure restores ModelData and real origin object. READ returns deep immutable `RuntimeFactValue` without mutation.

## Gate

R28 is planned TestDesign only. Risk scan, same-revision specialist Review and machine Evidence remain required before TDD.
