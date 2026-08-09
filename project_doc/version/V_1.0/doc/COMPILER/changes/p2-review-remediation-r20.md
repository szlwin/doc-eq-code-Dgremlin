# P2 Review remediation R20

> Base PR36 head: `7f266763eb5e92041c8828aa8c5f56fbf19a6123`
> Candidate chain after this remediation: `BM-R18 -> FLOW-R08 -> DESIGN-P2-R20 -> TESTDESIGN-P2-R21`
> Lifecycle: candidate-only; no PASSED claim.

This remediation addresses the independent Review blockers without changing Overlay R04 decisions:

1. Restores common-develop 2.44 structured BM / Flow changeset / dependency-impact schema shape.
2. Adds `FND-P2-REV-020` and restores P1-compatible shared `ViewKey` source-model identity; authorization owner System remains separate.
3. Freezes WRITE intent selection to deterministic 0/1/N before Guard; the single intent is immutable and never reselected after Guard.
4. Freezes the production runtime operation architecture: starter enforcement/assembly delegates real object/path operations to a dec-core-model production adapter through a neutral context contract.
5. Closes the candidate `RuntimeFactValue` and opaque runtime ID value contracts.
6. Updates TestDesign with exact planned Maven module/class/path/commands, full stable Case IDs, write-intent/value/production-adapter blockers.
7. Updates dependency graph and traceability to current revisions.

Not completed by this documentation remediation: current-revision risk scan, independent Review/Evidence, TDD RED, implementation plan, production Java or development. Historical PASSED task state remains untouched.
