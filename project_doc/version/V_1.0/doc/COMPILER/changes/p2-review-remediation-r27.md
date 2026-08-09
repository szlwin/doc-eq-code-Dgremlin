# P2 Review Remediation R27/R28

Review baseline: `654012a55e2ba22662e0cd1ba573c152840f829e`.
Candidate: `BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28`.

This revision accepts the independent Review finding that R26 crossed BM/FLOW by introducing `RuntimeFactValue sourceSnapshot -> new ModelData -> MODEL open failure` semantics. It therefore **withdraws** that fresh-snapshot/open seam instead of propagating it upstream.

Changes:
1. BM-R20 and FLOW-R11 remain unchanged.
2. COMPILER/CONTEXT now publish a neutral immutable `CompiledViewMaterializationPlan`; MODEL never reinterprets `NormalizedBody`/XML/YAML/ViewData/default Context.
3. Existing production origin object and existing ModelDataFactory/ModelLoader/ModelContainer lifecycle remain authoritative; typed materialization preserves real originData write-back.
4. MODEL package-private binding freezes exact RuntimeBindingPlan + the same actual ModelData into the trusted handle; no public existing-ModelData wrap/rebind surface.
5. Active MODEL execution root mints an unforgeable `RuntimeModelAccessScope` and frame/owner/cursor facts. No public frame request self-asserts scope identity.
6. STARTER again implements FLOW-R11 literally: validate frame, begin session, register trusted handles, seal, resolve, capability, Guard, MODEL effect.
7. R25 construction factories for cross-module immutable contracts are restored; R27 only adds descriptor/scope contracts.
8. R28 adds production write-back, scope provenance, descriptor/no-reparse, compiler/STARTER constructibility, superseded-R26 API absence and precondition failure-matrix blockers.
9. All 20 formal P1 remain OPEN; no FND-021; risk/TDD/Development remain blocked.
