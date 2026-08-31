# P2 Review remediation R28

Review baseline: `4a8bfef3f96c37d9b130c01256c7e1cf7645d760`.
Candidate: `BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29`.

This remediation intentionally leaves BM-R20 and FLOW-R11 unchanged and closes only the three remaining implementation-readiness groups accepted from the independent Review:

1. `CompiledViewMaterializationIndex` is now a mandatory `CompiledModelSet` aggregate member with EngineContext delegate accessor, equality/hash/digest closure and compile/publication completeness gate.
2. MODEL production integration is explicit through `RuntimeModelExecutionRoot`: captured Context + owned existing Container + exact plan + real origin object -> typed ModelDataFactory -> existing ModelLoader -> Container.load -> same-ModelData trusted handle -> active scope.
3. Scope/session/composition setup failures have closed stable codes/results so TestDesign can assert exact behavior.

Current user directive: the proposed fourth group, restoration of a POJO/Map already copied before a later legacy commit failure, is **out of scope and must not be modified or treated as a blocking TestDesign requirement**.

No production Java, risk scan, lifecycle transition, TDD execution or Development is claimed. Existing 20 P1 remain OPEN; no FND-P2-REV-021 is created.
