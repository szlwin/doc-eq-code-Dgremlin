# FEATURE-DESC-3361AD2E54FC 需求—模型—设计—测试追踪矩阵

> Rebuilt for `REQAN-P2-R01 + decision overlay / BM-R13 / DESIGN-P2-R15 / TESTDESIGN-P2-R16`。  
> 所有条目保持 `PENDING`：current refs/cases 已 materialize，但 exact Review、risk detection、machine lifecycle 与执行 Evidence 尚未闭环。

```json traceability
[
  {
    "id": "TR-P2-SYSTEM-RULEVIEW-001",
    "description": "AC-001 System deterministic compile plus first-class ownership/version snapshot",
    "status": "PENDING",
    "acceptance_criteria": ["AC-P2-SYSTEM-RULEVIEW-001"],
    "requirement_refs": [
      "version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#AC-P2-SYSTEM-RULEVIEW-001",
      "version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement_decision_overlay_20260809.md"
    ],
    "impact_required": true,
    "dependency_impact_refs": ["REL-P2-SYSTEM-RULEVIEW-COMPILER", "IMP-P2-SYSTEM-OWNERSHIP"],
    "business_flow_required": true,
    "business_flow_refs": ["FLOW-CONFIG-COMPILE"],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": ["CMI-P2-SYSTEM-RULEVIEW-001"],
    "business_model_refs": [
      "version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#ENT-COMPILED-SYSTEM",
      "version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#INV-COMPILER-016A",
      "version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#INV-COMPILER-016C"
    ],
    "design_refs": ["version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-system"],
    "test_case_ids": [
      "CASE-P2-TD-SYSTEM-DETERMINISM-001",
      "CASE-P2-TD-SYSTEM-DUPLICATE-001",
      "CASE-P2-TD-SYSTEM-FORWARD-REF-001",
      "CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001",
      "CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001",
      "CASE-P2-TD-SYSTEM-OWNERSHIP-REAL-FIXTURE-001"
    ],
    "plan_task_ids": [], "contract_refs": [], "implementation_refs": [], "verification_evidence_ids": [], "verified_by_agents": [],
    "notes": "Candidate coverage now includes version/source identity and explicit ownership sets; exact Review pending."
  },
  {
    "id": "TR-P2-SYSTEM-RULEVIEW-002",
    "description": "AC-002 RuleView System required, duplicate/isolation and resolved View relation",
    "status": "PENDING",
    "acceptance_criteria": ["AC-P2-SYSTEM-RULEVIEW-002"],
    "requirement_refs": ["version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#AC-P2-SYSTEM-RULEVIEW-002"],
    "impact_required": true,
    "dependency_impact_refs": ["REL-P2-SYSTEM-RULEVIEW-CONTEXT"],
    "business_flow_required": true,
    "business_flow_refs": ["FLOW-CONFIG-COMPILE"],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": ["CMI-P2-SYSTEM-RULEVIEW-001"],
    "business_model_refs": ["version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#ENT-COMPILED-RULEVIEW", "version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#INV-COMPILER-017A"],
    "design_refs": ["version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-ruleview"],
    "test_case_ids": [
      "CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001",
      "CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001",
      "CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001",
      "CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001",
      "CASE-P2-TD-RULEVIEW-VIEW-REAL-FIXTURE-001"
    ],
    "plan_task_ids": [], "contract_refs": [], "implementation_refs": [], "verification_evidence_ids": [], "verified_by_agents": [],
    "notes": "Resolved View relation is now part of current candidate rather than an implementation choice."
  },
  {
    "id": "TR-P2-SYSTEM-RULEVIEW-003",
    "description": "AC-003 composite RuleView lookup and no new bare-name fallback",
    "status": "PENDING",
    "acceptance_criteria": ["AC-P2-SYSTEM-RULEVIEW-003"],
    "requirement_refs": ["version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#AC-P2-SYSTEM-RULEVIEW-003"],
    "impact_required": true,
    "dependency_impact_refs": ["REL-P2-SYSTEM-RULEVIEW-CONTEXT", "REL-P2-SYSTEM-RULEVIEW-STARTER"],
    "business_flow_required": true,
    "business_flow_refs": ["FLOW-CONFIG-COMPILE"],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": ["CMI-P2-SYSTEM-RULEVIEW-001"],
    "business_model_refs": ["version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#VO-RULEVIEW-KEY"],
    "design_refs": ["version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-ruleview-resolver", "version/V_1.0/doc/COMPILER/COMPILER_api_contract.md#7-ruleviewresolver"],
    "test_case_ids": ["CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001", "CASE-P2-TD-RULEVIEW-BARE-NAME-REJECT-001", "CASE-P2-TD-LEGACY-NO-NEW-BARE-FALLBACK-001"],
    "plan_task_ids": [], "contract_refs": [], "implementation_refs": [], "verification_evidence_ids": [], "verified_by_agents": [],
    "notes": "Current anchors and case IDs exist; review pending."
  },
  {
    "id": "TR-P2-SYSTEM-RULEVIEW-004",
    "description": "AC-004 READ/WRITE/EXECUTE minimum permission and non-implication",
    "status": "PENDING",
    "acceptance_criteria": ["AC-P2-SYSTEM-RULEVIEW-004"],
    "requirement_refs": ["version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#AC-P2-SYSTEM-RULEVIEW-004", "project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/decision_log.md#DEC-P2-DIRECT-BRIDGE-AUTHORITY-001"],
    "impact_required": true,
    "dependency_impact_refs": ["IMP-P2-MODEL-ACCESS-AUTHORIZATION"],
    "business_flow_required": true,
    "business_flow_refs": ["FLOW-CONFIG-COMPILE"],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": ["CMI-P2-SYSTEM-RULEVIEW-001"],
    "business_model_refs": ["version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#INV-COMPILER-018A"],
    "design_refs": ["version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-model-access"],
    "test_case_ids": ["CASE-P2-TD-ACCESS-READ-MATRIX-001", "CASE-P2-TD-ACCESS-WRITE-MATRIX-001", "CASE-P2-TD-ACCESS-EXECUTE-MATRIX-001", "CASE-P2-TD-ACCESS-NON-IMPLICATION-001", "CASE-P2-TD-STATIC-DENY-001"],
    "plan_task_ids": [], "contract_refs": [], "implementation_refs": [], "verification_evidence_ids": [], "verified_by_agents": [],
    "notes": "Cross-operation negative matrix added; still no execution evidence."
  },
  {
    "id": "TR-P2-SYSTEM-RULEVIEW-005",
    "description": "AC-005 one canonical ModelPath across rule/change/query-contract/model-access",
    "status": "PENDING",
    "acceptance_criteria": ["AC-P2-SYSTEM-RULEVIEW-005"],
    "requirement_refs": ["version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#AC-P2-SYSTEM-RULEVIEW-005"],
    "impact_required": true,
    "dependency_impact_refs": ["IMP-P2-SHARED-MODEL-PATH"],
    "business_flow_required": true,
    "business_flow_refs": ["FLOW-CONFIG-COMPILE"],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": ["CMI-P2-SYSTEM-RULEVIEW-001"],
    "business_model_refs": ["version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#INV-COMPILER-018"],
    "design_refs": ["version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-model-path"],
    "test_case_ids": ["CASE-P2-TD-MODEL-PATH-UNKNOWN-001", "CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001", "CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001"],
    "plan_task_ids": [], "contract_refs": [], "implementation_refs": [], "verification_evidence_ids": [], "verified_by_agents": [],
    "notes": "Query is tested at compile/IR contract only; P6 execution is not pulled into P2."
  },
  {
    "id": "TR-P2-SYSTEM-RULEVIEW-006",
    "description": "AC-006 legal dynamic access classification and runtime proof",
    "status": "PENDING",
    "acceptance_criteria": ["AC-P2-SYSTEM-RULEVIEW-006"],
    "requirement_refs": ["version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#AC-P2-SYSTEM-RULEVIEW-006"],
    "impact_required": true,
    "dependency_impact_refs": ["IMP-P2-MODEL-ACCESS-AUTHORIZATION"],
    "business_flow_required": true,
    "business_flow_refs": ["FLOW-CONFIG-COMPILE"],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": ["CMI-P2-SYSTEM-RULEVIEW-001"],
    "business_model_refs": ["version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#SVC-MODEL-ACCESS-AUTHORIZATION"],
    "design_refs": ["version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-runtime-guard"],
    "test_case_ids": ["CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001", "CASE-P2-TD-RUNTIME-BINDING-PROOF-001", "CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001", "CASE-P2-SOURCE-TO-OPERATION-001-R16"],
    "plan_task_ids": [], "contract_refs": [], "implementation_refs": [], "verification_evidence_ids": [], "verified_by_agents": [],
    "notes": "CANDIDATE_COVERED / NOT_YET_VERIFIED."
  },
  {
    "id": "TR-P2-SYSTEM-RULEVIEW-007",
    "description": "AC-007 current P2 interpretation: unique production protected-access seam with no legal bypass",
    "status": "PENDING",
    "acceptance_criteria": ["AC-P2-SYSTEM-RULEVIEW-007"],
    "requirement_refs": [
      "version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#AC-P2-SYSTEM-RULEVIEW-007",
      "version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement_decision_overlay_20260809.md#2-ac-007-stage-boundary-delta",
      "project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/decision_log.md#DEC-P2-AC007-STAGE-BOUNDARY-001"
    ],
    "impact_required": true,
    "dependency_impact_refs": ["IMP-P2-PROTECTED-SEAM"],
    "business_flow_required": true,
    "business_flow_refs": ["FLOW-CONFIG-COMPILE"],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": ["CMI-P2-SYSTEM-RULEVIEW-001", "CMI-P2-DOWNSTREAM-PROTECTED-CONSUMERS"],
    "business_model_refs": ["version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#INV-COMPILER-020"],
    "design_refs": ["version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-runtime-guard"],
    "test_case_ids": ["CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001", "CASE-P2-TD-GUARD-NO-BYPASS-001", "CASE-P2-TD-STATIC-ALLOW-GUARD-PATH-001", "CASE-P2-TD-DIRECT-BRIDGE-REACHABILITY-001"],
    "plan_task_ids": [], "contract_refs": [], "implementation_refs": [], "verification_evidence_ids": [], "verified_by_agents": [],
    "notes": "P2 seam acceptance candidate; concrete P3/P4/P6 consumer integration is explicit downstream obligation, not current verification evidence."
  },
  {
    "id": "TR-P2-SYSTEM-RULEVIEW-008",
    "description": "AC-008 atomic publication and Context isolation",
    "status": "PENDING",
    "acceptance_criteria": ["AC-P2-SYSTEM-RULEVIEW-008"],
    "requirement_refs": ["version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#AC-P2-SYSTEM-RULEVIEW-008"],
    "impact_required": true,
    "dependency_impact_refs": ["IMP-P2-ATOMIC-PUBLICATION"],
    "business_flow_required": true,
    "business_flow_refs": ["FLOW-CONFIG-COMPILE"],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": ["CMI-P2-SYSTEM-RULEVIEW-001"],
    "business_model_refs": ["version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#INV-COMPILER-019"],
    "design_refs": ["version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-context", "version/V_1.0/doc/COMPILER/COMPILER_architecture.md#2-发布闭包"],
    "test_case_ids": ["CASE-P2-TD-ATOMIC-PUBLICATION-001", "CASE-P2-TD-CONTEXT-ISOLATION-001", "CASE-P2-TD-POLICY-INDEX-PUBLICATION-001", "CASE-P2-TD-POLICY-PUBLICATION-COMPATIBILITY-001"],
    "plan_task_ids": [], "contract_refs": [], "implementation_refs": [], "verification_evidence_ids": [], "verified_by_agents": [],
    "notes": "Ownership/RuleView/PolicyIndex/digest are one candidate closure."
  },
  {
    "id": "TR-P2-SYSTEM-RULEVIEW-009",
    "description": "AC-009 deterministic compile Diagnostic and runtime denial provenance",
    "status": "PENDING",
    "acceptance_criteria": ["AC-P2-SYSTEM-RULEVIEW-009"],
    "requirement_refs": ["version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#AC-P2-SYSTEM-RULEVIEW-009"],
    "impact_required": true,
    "dependency_impact_refs": ["IMP-P2-DIAGNOSTIC-DENIAL"],
    "business_flow_required": true,
    "business_flow_refs": ["FLOW-CONFIG-COMPILE"],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": ["CMI-P2-SYSTEM-RULEVIEW-001"],
    "business_model_refs": ["version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#INV-COMPILER-021"],
    "design_refs": ["version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-runtime-denial", "version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-diagnostics"],
    "test_case_ids": ["CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001", "CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001"],
    "plan_task_ids": [], "contract_refs": [], "implementation_refs": [], "verification_evidence_ids": [], "verified_by_agents": [],
    "notes": "Runtime denial repeatability is now an explicit independent oracle."
  },
  {
    "id": "TR-P2-SYSTEM-RULEVIEW-010",
    "description": "AC-010 declaration/P7 migration boundary",
    "status": "PENDING",
    "acceptance_criteria": ["AC-P2-SYSTEM-RULEVIEW-010"],
    "requirement_refs": ["version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md#AC-P2-SYSTEM-RULEVIEW-010"],
    "impact_required": true,
    "dependency_impact_refs": ["IMP-P2-DECLARATION-BOUNDARY"],
    "business_flow_required": true,
    "business_flow_refs": ["FLOW-CONFIG-COMPILE"],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": ["CMI-P2-SYSTEM-RULEVIEW-001"],
    "business_model_refs": ["version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml#INV-COMPILER-022"],
    "design_refs": ["version/V_1.0/doc/COMPILER/COMPILER_design.md#p2-compatibility", "version/V_1.0/doc/COMPILER/COMPILER_architecture.md#4-迁移架构"],
    "test_case_ids": ["CASE-P2-TD-DECLARATION-BOUNDARY-001"],
    "plan_task_ids": [], "contract_refs": [], "implementation_refs": [], "verification_evidence_ids": [], "verified_by_agents": [],
    "notes": "P2 does not perform P7 deletion."
  }
]
```

## 状态规则

- `PENDING`：current refs/cases 已存在，但当前 revision Review/Evidence 未闭环。
- 不得因为 Case 被设计出来就改成 `COVERED`。
- `verification_evidence_ids` 只有实际 current-revision execution/review Evidence 才能填写。
- historical BM-R07 / DESIGN-R01 / TESTDESIGN-R02 Evidence 不自动证明 BM-R13 / DESIGN-R15 / TESTDESIGN-R16。
