# FEATURE-DESC-3361AD2E54FC 需求—模型—Flow—设计—测试追踪矩阵

> Current candidate：`REQAN-P2-R01 + DEC-OVERLAY-R04 / BM-R16 / FLOW-R06 / DESIGN-P2-R18 / TESTDESIGN-P2-R19`。  
> 所有条目保持 `PENDING`；无 current exact Review / verification Evidence。

```json traceability
[
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-001","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-001"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-001","requirement_decision_overlay_20260809_r04.md"],
    "business_model_refs":["COMPILER_business_model.yaml#ENT-COMPILED-SYSTEM","COMPILER_business_model.yaml#INV-COMPILER-016B"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R06"],"design_refs":["COMPILER_design.md#p2-system"],
    "test_case_ids":["CASE-P2-TD-SYSTEM-DETERMINISM-001","CASE-P2-TD-SYSTEM-DUPLICATE-001","CASE-P2-TD-SYSTEM-FORWARD-REF-001","CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001","CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001","CASE-P2-TD-BM-CANONICAL-PAIR-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-002","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-002"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-002"],"business_model_refs":["COMPILER_business_model.yaml#ENT-COMPILED-RULEVIEW","COMPILER_business_model.yaml#VO-RULE-KEY"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R06"],"design_refs":["COMPILER_design.md#p2-ruleview","COMPILER_design.md#p2-rule-key"],
    "test_case_ids":["CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001","CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001","CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001","CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001","CASE-P2-TD-RULEKEY-CONTRACT-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-003","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-003"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-003"],"business_model_refs":["COMPILER_business_model.yaml#VO-RULEVIEW-KEY"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R06"],"design_refs":["COMPILER_design.md#p2-ruleview-resolver","COMPILER_api_contract.md#7-ruleviewresolver"],
    "test_case_ids":["CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001","CASE-P2-TD-KEY-SOURCE-COMPAT-001","CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-004","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-004"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-004","requirement_decision_overlay_20260809_r04.md#1-accessoperation-scope--read--write-only","decision_log.md#DEC-P2-ACCESS-OPERATIONS-001"],
    "business_model_refs":["COMPILER_business_model.yaml#VO-ACCESS-OPERATION","COMPILER_business_model.yaml#INV-COMPILER-018A"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R06","FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R06"],
    "design_refs":["COMPILER_design.md#p2-model-access","COMPILER_design.md#p2-p1-migration"],
    "test_case_ids":["CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001","CASE-P2-TD-NO-EXECUTE-CONTRACT-001","CASE-P2-TD-STATIC-DENY-001","CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001"],"verification_evidence_ids":[],
    "notes":"Current user-authorized candidate has READ/WRITE only; historical R01 EXECUTE portion is N/A via DEC-P2-ACCESS-OPERATIONS-001."
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-005","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-005"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-005"],"business_model_refs":["COMPILER_business_model.yaml#INV-COMPILER-018","COMPILER_business_model.yaml#INV-COMPILER-023"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R06"],"design_refs":["COMPILER_design.md#p2-model-path","COMPILER_design.md#p2-p1-migration"],
    "test_case_ids":["CASE-P2-TD-MODEL-PATH-UNKNOWN-001","CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001","CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001","CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-006","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-006"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-006"],"business_model_refs":["COMPILER_business_model.yaml#VO-MODEL-ACCESS-RULE"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R06","FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R06"],"design_refs":["COMPILER_design.md#p2-runtime-guard"],
    "test_case_ids":["CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001","CASE-P2-TD-RUNTIME-BINDING-PROOF-001","CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001","CASE-P2-SOURCE-TO-READ-WRITE-OPERATION-001-R19"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-007","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-007"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-007","requirement_decision_overlay_20260809_r04.md#3-ac-007--option-b-active","decision_log.md#DEC-P2-AC007-STAGE-BOUNDARY-001"],
    "business_model_refs":["COMPILER_business_model.yaml#ENT-PROTECTED-ACCESS-COMPOSITION","COMPILER_business_model.yaml#INV-COMPILER-020"],
    "business_flow_refs":["FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R06"],
    "design_refs":["COMPILER_design.md#p2-production-composition","COMPILER_design.md#p2-ac007-consumers","COMPILER_design.md#p2-concurrency"],
    "test_case_ids":["CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001","CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001","CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001","CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001","CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001","CASE-P2-TD-AC007-CONSUMER-PARITY-001","CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001","CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001","CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001"],
    "verification_evidence_ids":[],"notes":"Option B is user-ACTIVE; production composition acquisition is required, seam-only/manual wrapper evidence is insufficient."
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-008","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-008"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-008"],"business_model_refs":["COMPILER_business_model.yaml#INV-COMPILER-019"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R06"],"design_refs":["COMPILER_design.md#p2-context","COMPILER_architecture.md#2-compilepublication-topology"],
    "test_case_ids":["CASE-P2-TD-ATOMIC-PUBLICATION-001","CASE-P2-TD-CONTEXT-ISOLATION-001","CASE-P2-TD-POLICY-INDEX-PUBLICATION-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-009","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-009"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-009"],"business_model_refs":["COMPILER_business_model.yaml#INV-COMPILER-021"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R06","FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R06"],"design_refs":["COMPILER_design.md#p2-runtime-denial"],
    "test_case_ids":["CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001","CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001"],"verification_evidence_ids":[]
  },
  {
    "id":"TR-P2-SYSTEM-RULEVIEW-010","status":"PENDING","acceptance_criteria":["AC-P2-SYSTEM-RULEVIEW-010"],
    "requirement_refs":["requirement.md#AC-P2-SYSTEM-RULEVIEW-010"],"business_model_refs":["COMPILER_business_model.yaml#bareNameCompatibility"],
    "business_flow_refs":["FLOW-CONFIG-COMPILE@FLOW-R06"],"design_refs":["COMPILER_design.md#p2-compatibility"],
    "test_case_ids":["CASE-P2-TD-DECLARATION-BOUNDARY-001","CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001"],"verification_evidence_ids":[]
  }
]
```

## Gate

- Overlay R04：NEEDS_REQUIREMENT_REVIEW / MACHINE_BLOCKED。
- BM-R16：NEEDS_EXACT_REVIEW。
- FLOW-R06：NEEDS BusinessFlow/Impact/CrossModule Review。
- DESIGN-P2-R18：NEEDS specialist Reviews/risk scan。
- TESTDESIGN-P2-R19：BLOCKED_BY_DESIGN + MACHINE_BLOCKED。
- 所有 verification_evidence_ids 为空；不得标记 COVERED/PASSED。
