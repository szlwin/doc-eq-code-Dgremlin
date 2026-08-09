# FEATURE-DESC-3361AD2E54FC 需求—模型—Flow—设计—测试追踪矩阵

> Current candidate：`REQAN-P2-R01 + DEC-OVERLAY-R02 / BM-R14 / FLOW-R04 / DESIGN-P2-R16 / TESTDESIGN-P2-R17`。  
> 所有条目保持 `PENDING`；AC-007 为 `PENDING_USER_DECISION`。无 current verification Evidence。

```json traceability
[
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-001","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-001"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-001","requirement_decision_overlay_20260809_r02.md"],
    "business_model_refs":["COMPILER_business_model.yaml#ENT-COMPILED-SYSTEM","COMPILER_business_model.yaml#INV-COMPILER-016B"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R04"],
    "design_refs":["COMPILER_design.md#p2-system"],
    "test_case_ids":["CASE-P2-TD-SYSTEM-DETERMINISM-001","CASE-P2-TD-SYSTEM-DUPLICATE-001","CASE-P2-TD-SYSTEM-FORWARD-REF-001","CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001","CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001","CASE-P2-TD-SYSTEM-OWNERSHIP-REAL-FIXTURE-001"],
    "verification_evidence_ids":[],"notes":"System version includes schema+compiler identity; ownership snapshot derives from explicit authorities."
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-002","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-002"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-002"],"business_model_refs":["COMPILER_business_model.yaml#ENT-COMPILED-RULEVIEW"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R04"],"design_refs":["COMPILER_design.md#p2-ruleview"],
    "test_case_ids":["CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001","CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001","CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001","CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001","CASE-P2-TD-RULEVIEW-VIEW-REAL-FIXTURE-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-003","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-003"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-003"],"business_model_refs":["COMPILER_business_model.yaml#VO-RULEVIEW-KEY"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R04"],"design_refs":["COMPILER_design.md#p2-ruleview-resolver","COMPILER_api_contract.md#7-ruleviewresolver"],
    "test_case_ids":["CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001","CASE-P2-TD-RULEVIEW-BARE-NAME-REJECT-001","CASE-P2-TD-LEGACY-NO-NEW-BARE-FALLBACK-001","CASE-P2-TD-KEY-SOURCE-COMPAT-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-004","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-004"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-004","decision_log.md#DEC-P2-DIRECT-BRIDGE-AUTHORITY-001"],
    "business_model_refs":["COMPILER_business_model.yaml#INV-COMPILER-018A"],"business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R04","FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R04"],
    "design_refs":["COMPILER_design.md#p2-model-access","COMPILER_design.md#p2-p1-migration"],
    "test_case_ids":["CASE-P2-TD-ACCESS-READ-MATRIX-001","CASE-P2-TD-ACCESS-WRITE-MATRIX-001","CASE-P2-TD-ACCESS-EXECUTE-MATRIX-001","CASE-P2-TD-ACCESS-NON-IMPLICATION-001","CASE-P2-TD-STATIC-DENY-001","CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-005","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-005"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-005"],"business_model_refs":["COMPILER_business_model.yaml#INV-COMPILER-018","COMPILER_business_model.yaml#INV-COMPILER-023"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R04"],"design_refs":["COMPILER_design.md#p2-model-path","COMPILER_design.md#p2-p1-migration"],
    "test_case_ids":["CASE-P2-TD-MODEL-PATH-UNKNOWN-001","CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001","CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001","CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-006","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-006"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-006"],"business_model_refs":["COMPILER_business_model.yaml#POL-MODEL-ACCESS-AUTHORIZATION"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R04","FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R04"],"design_refs":["COMPILER_design.md#p2-runtime-guard"],
    "test_case_ids":["CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001","CASE-P2-TD-RUNTIME-BINDING-PROOF-001","CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001","CASE-P2-SOURCE-TO-OPERATION-001-R17"],"verification_evidence_ids":[],"notes":"CANDIDATE_COVERED / NOT_YET_VERIFIED."
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-007","status":"PENDING_USER_DECISION","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-007"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-007","requirement_decision_overlay_20260809_r02.md#2-ac-007-当前未决不得由-agent-代替用户选择","decision_log.md#DEC-P2-AC007-STAGE-BOUNDARY-001"],
    "business_model_refs":["COMPILER_business_model.yaml#INV-COMPILER-020"],"business_flow_refs":["FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R04:PENDING_USER_DECISION"],
    "design_refs":["COMPILER_design.md#p2-runtime-guard"],
    "test_case_ids":["CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001","CASE-P2-TD-GUARD-NO-BYPASS-001","CASE-P2-TD-STATIC-ALLOW-GUARD-PATH-001","CASE-P2-TD-DIRECT-BRIDGE-REACHABILITY-001"],
    "verification_evidence_ids":[],"notes":"These are common foundation cases only. Original AC007 is not superseded. User must choose Option A or B before final case set/acceptance can be frozen."
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-008","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-008"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-008"],"business_model_refs":["COMPILER_business_model.yaml#INV-COMPILER-019"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R04"],"design_refs":["COMPILER_design.md#p2-context","COMPILER_architecture.md#2-发布闭包"],
    "test_case_ids":["CASE-P2-TD-ATOMIC-PUBLICATION-001","CASE-P2-TD-CONTEXT-ISOLATION-001","CASE-P2-TD-POLICY-INDEX-PUBLICATION-001","CASE-P2-TD-POLICY-PUBLICATION-COMPATIBILITY-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-009","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-009"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-009"],"business_model_refs":["COMPILER_business_model.yaml#INV-COMPILER-021"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R04","FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R04"],"design_refs":["COMPILER_design.md#p2-diagnostics","COMPILER_design.md#p2-runtime-denial"],
    "test_case_ids":["CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001","CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-010","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-010"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-010"],"business_model_refs":["COMPILER_business_model.yaml#INV-COMPILER-022"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R04"],"design_refs":["COMPILER_design.md#p2-compatibility"],
    "test_case_ids":["CASE-P2-TD-DECLARATION-BOUNDARY-001"],"verification_evidence_ids":[]
  }
]
```

## Gate

- Requirement overlay R02：NEEDS_USER_DECISION / NEEDS_REQUIREMENT_REVIEW。
- BM-R14：NEEDS_EXACT_REVIEW。
- FLOW-R04：NEEDS BusinessFlow/Impact/CrossModule Review。
- DESIGN-P2-R16：NEEDS specialist Reviews/risk scan。
- TESTDESIGN-P2-R17：BLOCKED_BY_DESIGN + AC007 user decision。
- 所有 verification_evidence_ids 为空；不得标记 COVERED/PASSED。
