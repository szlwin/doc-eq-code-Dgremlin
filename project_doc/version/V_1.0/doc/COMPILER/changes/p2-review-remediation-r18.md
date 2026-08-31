# P2 Review Remediation R18

> Reviewed head: `44edbaa5f4194046c008f448d3eb8df09c0ab9a9`  
> Review result: Requirement/BM/Design/TestDesign `NEEDS_CHANGES / MACHINE_BLOCKED`  
> Target candidates: `REQAN-P2-R01+DEC-OVERLAY-20260809-R02 / BM-R14 / DESIGN-P2-R16 / TESTDESIGN-P2-R17 / FLOW-R04@p2-system-ruleview-protected-access`

## Accepted findings

1. AC-007 stage-boundary choice changes acceptance scope and lacks user authorization. `DEC-P2-AC007-STAGE-BOUNDARY-001` must be `PROPOSED / PENDING_USER_DECISION`; original AC-007 remains effective until user chooses A or B.
2. Business Flow must split compile/publication from protected runtime execution.
3. `SystemVersionIdentity` must unambiguously bind both schema and compiler compatibility; options remain an enclosing compiled-set fact unless explicitly duplicated.
4. `CompiledSystem` ownership snapshot is a derived immutable read index, not an independent authority. Authoritative sources differ by fact family.
5. Existing `SystemKey` / `RuleViewKey` public constructor/accessor compatibility must be retained; new factories/accessors are additive aliases only.
6. P1 `SharedModelPath` / `AccessMode` to P2 `ModelPath` / `AccessOperation` conversion must be one-way and explicit; wildcard never reaches runtime authority; EXECUTE is never inferred from P1.

## Gate

No FND-020. Existing FND-004/FND-007/FND-013/FND-015 and related findings remain OPEN until exact current-revision Review/machine Evidence exists. `risk_detection`, `task_state`, `stage_outcomes`, historical acceptance assertions and Evidence are not rewritten. Implementation Plan/TDD/Development remain BLOCKED.
