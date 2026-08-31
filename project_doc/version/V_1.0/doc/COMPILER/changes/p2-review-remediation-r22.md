# P2 Review remediation R22

> Base PR36 head: `d1b864f1ff80d11e3e97d793b1b9bb5103710c54`
> Candidate chain: `BM-R20 -> FLOW-R10 -> P2-IMPACT-R22 -> DESIGN-P2-R22 -> TESTDESIGN-P2-R23`
> Lifecycle: candidate-only; no PASSED claim.

This remediation preserves all semantics explicitly marked PASS by the independent Review and closes only the remaining current-revision completeness/runtime-binding gaps:

1. BM-R20 is a full current business-model snapshot: retained BM-R18 facts + BM-R19 runtime semantics.
2. FLOW-R10 restores responsibility: CONTEXT constructs immutable candidate representation; COMPILER coordinates atomic publication.
3. P2-IMPACT-R22 restores compile-side nodes/relationships and `CMI-P2-COMPILE-003` while retaining runtime CMI as `CMI-P2-PROTECTED-ACCESS-003`.
4. DESIGN-P2-R22 is self-contained with explicit factories/constructors and explicit `EngineContext + RuntimeExecutionFrameSnapshot` composition input.
5. `RuntimeTargetResolver` is the sole plan-to-object selector; invocation context must match composition frame/owner.
6. `RuntimeMutationStamp(sessionId, objectId, path, version)` freezes target/version atomically; explicit `RuntimeModelSessionId` makes scope errors deterministic.
7. One actual ModelData/runtime handle has one coordination cell and at most one active session lease, preventing cross-session alias concurrency bypass.
8. TESTDESIGN-P2-R23 keeps 19 exact TestClasses, grows from 64 to 68 blockers, and makes every current Case behaviorally self-contained.
9. P2 RuntimeModelSession/transaction/version are explicitly bounded away from P7 business/session lifecycle.

Not claimed: production Java, current-revision risk scan, same-revision specialist Review closure, TDD execution or Development.
