# P2 Review remediation R25/R26

Baseline: `45119f26e0feee9c7840e4dc94f4bf3e5f3f27ea`.

This candidate addresses the independent Review without reopening Requirement or BM semantics:

- advances changed flow identity to `FLOW-R11` and assigns actual production model operation to MODEL;
- advances Impact to `P2-IMPACT-R24`, keeps current CMI IDs `CMI-P2-COMPILE-004` / `CMI-P2-PROTECTED-ACCESS-004`;
- advances Design to `DESIGN-P2-R25` with trusted model-owned RuntimeModelFrame/RuntimeModelHandle provenance; public callers cannot create/rebind `valid binding A + arbitrary ModelData B`;
- completes all referenced public/cross-module API types and explicit visibility;
- splits API contract RED across CONTEXT/MODEL/STARTER legal dependency boundaries;
- advances TestDesign to `TESTDESIGN-P2-R26`: 72 blocking cases / 21 exact TestClasses, with per-case current-revision Fixture/Action/Expected/Forbidden/Refs;
- no new FND-021; all 20 P1 remain OPEN pending same-revision Review/risk/machine Evidence;
- Implementation Plan/TDD/Development remain BLOCKED.
