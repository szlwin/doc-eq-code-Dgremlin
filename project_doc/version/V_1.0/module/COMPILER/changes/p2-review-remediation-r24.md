# P2 Review remediation R24

> Review baseline: `d0c86e41b2b156e5d692fc8c6aeca78fca253407`
> Candidate after remediation: `BM-R20 -> FLOW-R10 -> DESIGN-P2-R24 -> TESTDESIGN-P2-R25`; parallel CrossModule projection `P2-IMPACT-R23`.
> Lifecycle: candidate-only; historical PASSED state is unchanged.

This remediation is intentionally narrow:

1. Fixes `RuntimeModelSession` Java contract to `extends AutoCloseable` and repairs the runtime-model-session anchor.
2. Introduces explicit starter-owned `RuntimeModelRegistrationInput(TargetKey, CompiledTargetBinding, ModelData)` production assembly provenance.
3. `ProtectedAccessRuntimeFactory` validates every typed association against the exact captured EngineContext before RuntimeModelSession registration/seal. Association facts cannot grant permission; PolicyIndex/Guard remains the only READ/WRITE authority.
4. Forbids production inference of binding from ModelData name, ViewData/property tree, list order, first-match, raw definitions, selector parsing/normalization, or a global mutable runtime map.
5. Advances Dependency Impact to `P2-IMPACT-R23` and models compiler -> context compiled-binding transport plus starter -> model registration provenance.
6. Keeps BM-R20/FLOW-R10 business semantics but removes stale downstream exact Design/TestDesign revisions from their canonical projections; exact downstream revisions live in central trace/dependency projections.
7. Advances TestDesign to `TESTDESIGN-P2-R25`, preserving the 19 exact TestClasses and adding `CASE-P2-TD-PRODUCTION-RUNTIME-REGISTRATION-BINDING-001` for a total of 69 blocking cases.
8. Keeps all 20 P1 findings OPEN; no FND-021, risk scan, TDD execution, implementation plan or development is claimed.
