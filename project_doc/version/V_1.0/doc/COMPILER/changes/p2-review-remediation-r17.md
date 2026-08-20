# P2 Review Remediation — Ownership / Path / Seam / Denial

> Remediation revision：`P2-REVIEW-REMEDIATION-R17`  
> Reviewed head：`5f163434e7349cda49bfdf5cd5aaf3c8ea0b4217`  
> Reviewed P0：`#1490 SUCCESS`  
> New candidates：`BM-R13 / DESIGN-P2-R15 / TESTDESIGN-P2-R16`

## Review findings accepted

1. `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` was missing from persistent `decision_log.md`.
2. CompiledSystem was too weak (`key + sourceRef`) to prove first-class System ownership/version semantics.
3. CompiledRuleView did not freeze resolved `view-ref` relation.
4. AC-005 lacked a cross-consumer rule/change/query/model-access ModelPath equivalence case.
5. AC-007 literal concrete-executor acceptance conflicted with the explicit P3/P4/P6 stage boundary.
6. AC-004 lacked operation non-implication cross matrix.
7. AC-009 lacked deterministic repeated runtime DENY provenance oracle.

No FND-020 is created; findings are folded into existing FND-004/FND-007/FND-013 and existing related open findings.

## Candidate remediation

- persistent decisions added:
  - `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`;
  - `DEC-P2-AC007-STAGE-BOUNDARY-001`;
- Requirement R01 history remains unchanged; current decision delta is materialized in `requirement_decision_overlay_20260809.md`;
- BM-R13 adds SystemVersionIdentity + ownership snapshot and CompiledRuleView resolved View/rules;
- Design R15 adds matching public/query contracts, shared ModelPath compiler, P2 no-bypass seam and runtime denial contract;
- TestDesign R16 adds the five missing blocking test families and retains all R15 coverage;
- traceability stays `PENDING` and points only to current existing refs/cases;
- dependency impact creates explicit P3/P4/P6 downstream obligations rather than claiming concrete consumers are already implemented.

## Machine truth preserved

This remediation does not modify or claim completion for:

- `risk_detection.json` (`NOT_SCANNED`);
- historical `task_state.md` / `stage_outcomes.md` current machine revisions;
- historical VERIFIED acceptance assertions/Evidence;
- Implementation Plan / TDD / Development.

The local `$common-develop` installation currently contains only `SKILL.md` and `CHANGELOG.md`; declared lifecycle/review scripts are unavailable, so no RC9 reopen/publish/risk scan is fabricated.
