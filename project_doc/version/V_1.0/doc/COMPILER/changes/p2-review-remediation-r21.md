# P2 Review remediation R21

> Base PR36 head: `7b559fdb01c5def328160b467427bd767fdc4dae`
> Candidate chain: `BM-R19 -> FLOW-R09 -> DESIGN-P2-R21 -> TESTDESIGN-P2-R22`
> Dependency Impact: `P2-IMPACT-R21`
> Lifecycle: candidate-only; no PASSED claim.

This remediation preserves all independently verified R18/R20 fixes and closes only the remaining implementation-uniqueness gaps:

1. Direct Bridge / WRITE intent authority is `ModelAccessRuleKey`; `RuleKey` is optional provenance only.
2. WRITE carries exactly one ModelPath through the authority key; production port accepts one resolved access object, not a second path.
3. Current API contract is fully self-contained in R21.
4. Frame/owner/cursor remain typed; cursor is `Optional<RuntimeCollectionCursorId>`.
5. RuntimeObjectId is resolved by a sealed composition/frame-scoped dec-core-model RuntimeModelSession; no global mutable registry.
6. Guard-approved WRITE has explicit transaction rollback/observable-state semantics; capability remains consumed on operation failure.
7. Same-version different-capability writes to one object/path use serialization + mutation version: at most one commit, stale loser zero mutation.
8. TestDesign R22 maps every blocking Case ID to an exact TestClass/module/source path/bootstrap/target RED command.

Not claimed here: production Java, current-revision risk scan, same-revision specialist Review closure, TDD execution or Development.
