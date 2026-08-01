# P1-COMPILER-F01 追踪矩阵

> TESTDESIGN-R01@ba7779cf089b 已通过 test_design 四项串行独立 Review；9 条 TR 均绑定可执行 Case 与 Evidence，下一阶段为 implementation_plan I007。

```json traceability
[
  {
    "id": "TR-P1-COMPILER-001",
    "description": "统一 `mix` 编译骨架: BR-P1-001, BR-P1-002, BR-P1-003, BR-P1-004 -> AC-P1-COMPILER-001",
    "status": "COVERED",
    "acceptance_criteria": [
      "AC-P1-COMPILER-001"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-001",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-002",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-003",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-004",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-001",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-001"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#crossModuleImplementations/CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-COMPILER-SUCCESS",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-SECURE-FRONTEND",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ENT-MIX-SOURCE-GRAPH",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-001",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-002",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-012",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SVC-SOURCE-DISCOVERY",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md#15-追踪映射"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-mix-source-resolver",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-compiler-pipeline",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md#test-mix-contract",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md"
    ],
    "test_case_ids": [
      "CASE-P1-TD-SOURCE-MANIFEST-001",
      "CASE-P1-TD-SOURCE-ORDER-001",
      "CASE-P1-TD-SOURCE-POLICY-001",
      "CASE-P1-TD-SOURCE-NOT-FOUND-001",
      "CASE-P1-TD-SOURCE-SECURITY-001",
      "CASE-P1-TD-SOURCE-DUPLICATE-001",
      "CASE-P1-TD-DIAGNOSTIC-CATALOG-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001",
      "TASK-P1-R2-005"
    ],
    "contract_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/P1-COMPILER-F01/test_case.md"
    ],
    "implementation_refs": [
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py",
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java"
    ],
    "verification_evidence_ids": [
      "EVD-000286",
      "EVD-000287"
    ],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent",
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "ArchitectureReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "DevelopAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "notes": "DESIGN-R05@0b37a9b4dd48 已由七个 design Reviewer 对同一 Revision 独立验证通过：REV-000050、REV-000051、REV-000052、REV-000053、REV-000054、REV-000055、REV-000056；ISSUE-MR-0001～0004 已由 ArchitectureReviewAgent 复核关闭。 TESTDESIGN-R01 已通过四项串行独立 Review：REV-000057, REV-000058, REV-000059, REV-000060。"
  },
  {
    "id": "TR-P1-COMPILER-002",
    "description": "统一 `mix` 编译骨架: BR-P1-004, BR-P1-007, BR-P1-013 -> AC-P1-COMPILER-002",
    "status": "COVERED",
    "acceptance_criteria": [
      "AC-P1-COMPILER-002"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-004",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-007",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-013",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-002",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-002"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#crossModuleImplementations/CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-COMPILER-SUCCESS",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-SECURE-FRONTEND",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ENT-RAW-DEFINITION-SET",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#VO-CANONICAL-DOCUMENT-NODE",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-012",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SVC-CANONICALIZATION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SVC-RAW-BUILDER",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md#15-追踪映射"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-canonical-document-node",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-raw-definition-set",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md"
    ],
    "test_case_ids": [
      "CASE-P1-TD-FRONTEND-XML-001",
      "CASE-P1-TD-FRONTEND-YAML-001",
      "CASE-P1-TD-CANONICAL-PARITY-001",
      "CASE-P1-TD-STRUCTURE-UNKNOWN-001",
      "CASE-P1-TD-RAW-INVENTORY-001",
      "CASE-P1-TD-DIAGNOSTIC-CATALOG-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001",
      "TASK-P1-R2-005"
    ],
    "contract_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/P1-COMPILER-F01/test_case.md"
    ],
    "implementation_refs": [
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py",
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java"
    ],
    "verification_evidence_ids": [
      "EVD-000286",
      "EVD-000287"
    ],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent",
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "ArchitectureReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "DevelopAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "notes": "DESIGN-R05@0b37a9b4dd48 已由七个 design Reviewer 对同一 Revision 独立验证通过：REV-000050、REV-000051、REV-000052、REV-000053、REV-000054、REV-000055、REV-000056；ISSUE-MR-0001～0004 已由 ArchitectureReviewAgent 复核关闭。 TESTDESIGN-R01 已通过四项串行独立 Review：REV-000057, REV-000058, REV-000059, REV-000060。"
  },
  {
    "id": "TR-P1-COMPILER-003",
    "description": "统一 `mix` 编译骨架: BR-P1-005, BR-P1-006, BR-P1-009, BR-P1-016, BR-P1-017 -> AC-P1-COMPILER-003",
    "status": "COVERED",
    "acceptance_criteria": [
      "AC-P1-COMPILER-003"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-005",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-006",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-009",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-016",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-017",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-003",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-003"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#crossModuleImplementations/CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-COMPILER-INVALID-REFERENCE",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ENT-SYMBOL-TABLE",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#VO-TYPED-KEY",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#VO-INFORMATION-KEY",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-003",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-004",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-006",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-015",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SVC-SYMBOL-REGISTRATION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SVC-REFERENCE-RESOLUTION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md#15-追踪映射"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-typed-key-symbol-table",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-information-expression",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-reference-resolution",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md"
    ],
    "test_case_ids": [
      "CASE-P1-TD-SYMBOL-DUPLICATE-001",
      "CASE-P1-TD-REFERENCE-001",
      "CASE-P1-TD-RULE-SYSTEM-001",
      "CASE-P1-TD-INFORMATION-OWNER-001",
      "CASE-P1-TD-PUBLISH-BLOCKED-001",
      "CASE-P1-TD-DIAGNOSTIC-CATALOG-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001",
      "TASK-P1-R2-005",
      "TASK-P1-T01"
    ],
    "contract_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/P1-COMPILER-F01/test_case.md"
    ],
    "implementation_refs": [
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py",
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/ContractReflectionAssertions.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/ContextValueContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/RegistryImmutabilityTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/EngineContextApiTest.java"
    ],
    "verification_evidence_ids": [
      "EVD-000286",
      "EVD-000287",
      "EVD-000290",
      "EVD-000291"
    ],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent",
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "ArchitectureReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "DevelopAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "notes": "DESIGN-R05@0b37a9b4dd48 已由七个 design Reviewer 对同一 Revision 独立验证通过：REV-000050、REV-000051、REV-000052、REV-000053、REV-000054、REV-000055、REV-000056；ISSUE-MR-0001～0004 已由 ArchitectureReviewAgent 复核关闭。 TESTDESIGN-R01 已通过四项串行独立 Review：REV-000057, REV-000058, REV-000059, REV-000060。 TDD-P1-T01-R01@4ebeed4dad6a 已由 REV-000061 验证为有效 RED。"
  },
  {
    "id": "TR-P1-COMPILER-004",
    "description": "统一 `mix` 编译骨架: BR-P1-007, BR-P1-008, BR-P1-015, BR-P1-018, BR-P1-019 -> AC-P1-COMPILER-004",
    "status": "COVERED",
    "acceptance_criteria": [
      "AC-P1-COMPILER-004"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-007",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-008",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-015",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-018",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-019",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-004",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-004"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#crossModuleImplementations/CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-COMMON-EXPRESSION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ENT-DEFERRED-REGISTRY",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#VO-DEFERRED-DEFINITION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-005",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-008",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SVC-DEFERRED-CLASSIFICATION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#POL-DEFERRED-BOUNDARY",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md#15-追踪映射"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-deferred-definition",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-compiler-pipeline",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md"
    ],
    "test_case_ids": [
      "CASE-P1-TD-COMMON-SUCCESS-001",
      "CASE-P1-TD-DEFERRED-COMPLETE-001",
      "CASE-P1-TD-DEFERRED-NO-RUNTIME-001",
      "CASE-P1-TD-DIAGNOSTIC-CATALOG-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001",
      "TASK-P1-R2-005",
      "TASK-P1-T01"
    ],
    "contract_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/P1-COMPILER-F01/test_case.md"
    ],
    "implementation_refs": [
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py",
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/ContractReflectionAssertions.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/ContextValueContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/RegistryImmutabilityTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/EngineContextApiTest.java"
    ],
    "verification_evidence_ids": [
      "EVD-000286",
      "EVD-000287",
      "EVD-000290",
      "EVD-000291"
    ],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent",
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "ArchitectureReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "DevelopAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "notes": "DESIGN-R05@0b37a9b4dd48 已由七个 design Reviewer 对同一 Revision 独立验证通过：REV-000050、REV-000051、REV-000052、REV-000053、REV-000054、REV-000055、REV-000056；ISSUE-MR-0001～0004 已由 ArchitectureReviewAgent 复核关闭。 TESTDESIGN-R01 已通过四项串行独立 Review：REV-000057, REV-000058, REV-000059, REV-000060。 TDD-P1-T01-R01@4ebeed4dad6a 已由 REV-000061 验证为有效 RED。"
  },
  {
    "id": "TR-P1-COMPILER-005",
    "description": "统一 `mix` 编译骨架: BR-P1-009, BR-P1-010, BR-P1-011 -> AC-P1-COMPILER-005",
    "status": "COVERED",
    "acceptance_criteria": [
      "AC-P1-COMPILER-005"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-009",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-010",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-011",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-005",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-005"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#crossModuleImplementations/CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-COMPILER-SUCCESS",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-COMPILER-INVALID-REFERENCE",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-MULTI-CONTEXT-ISOLATION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#AGG-PUBLISHED-CONTEXT",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ENT-COMPILED-MODEL-SET",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ENT-ENGINE-CONTEXT",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#VO-DIGEST-PAIR",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-009",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-010",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-011",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SVC-MODEL-PUBLICATION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#POL-PUBLICATION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md#15-追踪映射"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-compiled-model-set",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-digest",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md"
    ],
    "test_case_ids": [
      "CASE-P1-TD-SOURCE-ORDER-001",
      "CASE-P1-TD-CANONICAL-PARITY-001",
      "CASE-P1-TD-PUBLISH-SUCCESS-001",
      "CASE-P1-TD-PUBLISH-BLOCKED-001",
      "CASE-P1-TD-PUBLISH-TIMEOUT-001",
      "CASE-P1-TD-PUBLISH-CANCEL-001",
      "CASE-P1-TD-CONTEXT-CONSTRUCTION-001",
      "CASE-P1-TD-PUBLISH-CONFLICT-001",
      "CASE-P1-TD-PUBLISH-FAILURE-001",
      "CASE-P1-TD-DIGEST-001",
      "CASE-P1-TD-CONTEXT-ISOLATION-001",
      "CASE-P1-TD-DIAGNOSTIC-CATALOG-001",
      "CASE-P1-TD-OBSERVER-TIMING-001",
      "CASE-P1-TD-JAVA8-MODULE-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001",
      "TASK-P1-R2-005",
      "TASK-P1-T01"
    ],
    "contract_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/P1-COMPILER-F01/test_case.md"
    ],
    "implementation_refs": [
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py",
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/ContractReflectionAssertions.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/ContextValueContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/RegistryImmutabilityTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/EngineContextApiTest.java"
    ],
    "verification_evidence_ids": [
      "EVD-000286",
      "EVD-000287",
      "EVD-000290",
      "EVD-000291"
    ],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent",
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "ArchitectureReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "DevelopAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "notes": "DESIGN-R05@0b37a9b4dd48 已由七个 design Reviewer 对同一 Revision 独立验证通过：REV-000050、REV-000051、REV-000052、REV-000053、REV-000054、REV-000055、REV-000056；ISSUE-MR-0001～0004 已由 ArchitectureReviewAgent 复核关闭。 TESTDESIGN-R01 已通过四项串行独立 Review：REV-000057, REV-000058, REV-000059, REV-000060。 TDD-P1-T01-R01@4ebeed4dad6a 已由 REV-000061 验证为有效 RED。"
  },
  {
    "id": "TR-P1-COMPILER-006",
    "description": "统一 `mix` 编译骨架: BR-P1-011, BR-P1-012 -> AC-P1-COMPILER-006",
    "status": "COVERED",
    "acceptance_criteria": [
      "AC-P1-COMPILER-006"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-011",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-012",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-006",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-006"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#crossModuleImplementations/CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-MULTI-CONTEXT-ISOLATION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ENT-CORE-CONFIG-PROJECTION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#AGG-PUBLISHED-CONTEXT",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-014",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md#15-追踪映射"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-core-config-projection",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md"
    ],
    "test_case_ids": [
      "CASE-P1-TD-CONTEXT-ISOLATION-001",
      "CASE-P1-TD-PROJECTION-001",
      "CASE-P1-TD-DIAGNOSTIC-CATALOG-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001",
      "TASK-P1-R2-005",
      "TASK-P1-T01"
    ],
    "contract_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/P1-COMPILER-F01/test_case.md"
    ],
    "implementation_refs": [
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py",
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/ContractReflectionAssertions.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/ContextValueContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/RegistryImmutabilityTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/EngineContextApiTest.java"
    ],
    "verification_evidence_ids": [
      "EVD-000286",
      "EVD-000287",
      "EVD-000290",
      "EVD-000291"
    ],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent",
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "ArchitectureReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "DevelopAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "notes": "DESIGN-R05@0b37a9b4dd48 已由七个 design Reviewer 对同一 Revision 独立验证通过：REV-000050、REV-000051、REV-000052、REV-000053、REV-000054、REV-000055、REV-000056；ISSUE-MR-0001～0004 已由 ArchitectureReviewAgent 复核关闭。 TESTDESIGN-R01 已通过四项串行独立 Review：REV-000057, REV-000058, REV-000059, REV-000060。 TDD-P1-T01-R01@4ebeed4dad6a 已由 REV-000061 验证为有效 RED。"
  },
  {
    "id": "TR-P1-COMPILER-007",
    "description": "统一 `mix` 编译骨架: BR-P1-014, BR-P1-015 -> AC-P1-COMPILER-007",
    "status": "COVERED",
    "acceptance_criteria": [
      "AC-P1-COMPILER-007"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-014",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-015",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-007",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-007"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#crossModuleImplementations/CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-RETIRE-DECLARATION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-013",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#POL-RETIREMENT",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ERR-MIX-RETIREMENT-RESIDUE",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md#15-追踪映射"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-declaration-retirement",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md"
    ],
    "test_case_ids": [
      "CASE-P1-TD-DIAGNOSTIC-CATALOG-001",
      "CASE-P1-TD-RETIREMENT-001",
      "CASE-P1-TD-JAVA8-MODULE-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001",
      "TASK-P1-R2-005"
    ],
    "contract_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/P1-COMPILER-F01/test_case.md"
    ],
    "implementation_refs": [
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py",
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java"
    ],
    "verification_evidence_ids": [
      "EVD-000286",
      "EVD-000287"
    ],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent",
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "ArchitectureReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "DevelopAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "notes": "DESIGN-R05@0b37a9b4dd48 已由七个 design Reviewer 对同一 Revision 独立验证通过：REV-000050、REV-000051、REV-000052、REV-000053、REV-000054、REV-000055、REV-000056；ISSUE-MR-0001～0004 已由 ArchitectureReviewAgent 复核关闭。 TESTDESIGN-R01 已通过四项串行独立 Review：REV-000057, REV-000058, REV-000059, REV-000060。"
  },
  {
    "id": "TR-P1-COMPILER-008",
    "description": "统一 `mix` 编译骨架: BR-P1-005, BR-P1-006, BR-P1-007, BR-P1-016, BR-P1-017, BR-P1-018, BR-P1-019 -> AC-P1-COMPILER-008",
    "status": "COVERED",
    "acceptance_criteria": [
      "AC-P1-COMPILER-008"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-CR02/requirement.md",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-005",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-006",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-007",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-016",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-017",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-018",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-019",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-008",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-008"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#crossModuleImplementations/CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-COMMON-EXPRESSION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-MODEL-ACCESS-TARGET-MAIN",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#VO-INFORMATION-KEY",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#VO-MODEL-ACCESS-BINDING",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-004",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-005",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-006",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#POL-INFORMATION-OWNERSHIP",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SVC-REFERENCE-RESOLUTION",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md#15-追踪映射"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-information-expression",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-model-access-selector",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-reference-resolution",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md#api-information",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md#api-model-access",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md#test-symbol-information",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md#test-model-access",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md"
    ],
    "test_case_ids": [
      "CASE-P1-TD-RULE-SYSTEM-001",
      "CASE-P1-TD-INFORMATION-OWNER-001",
      "CASE-P1-TD-COMMON-SUCCESS-001",
      "CASE-P1-TD-INFORMATION-CROSS-SYSTEM-001",
      "CASE-P1-TD-COMMON-MEMBER-001",
      "CASE-P1-TD-COMMON-QUALIFIED-001",
      "CASE-P1-TD-VIEW-BOUNDARY-001",
      "CASE-P1-TD-MODEL-ACCESS-TARGET-MAIN-001",
      "CASE-P1-TD-DEFERRED-NO-RUNTIME-001",
      "CASE-P1-TD-DIAGNOSTIC-CATALOG-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001",
      "TASK-P1-R2-005"
    ],
    "contract_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "dec-demo/src/main/resources/mix/system/systems.xml",
      "dec-demo/src/main/resources/mix/business/order-business.xml",
      "dec-demo/src/main/resources/mix/view/orm-view.xml",
      "version/V_1.0/doc/P1-COMPILER-F01/test_case.md"
    ],
    "implementation_refs": [
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java",
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py"
    ],
    "verification_evidence_ids": [
      "EVD-000286",
      "EVD-000287"
    ],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent",
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "ArchitectureReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "DevelopAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "notes": "DESIGN-R05@0b37a9b4dd48 已由七个 design Reviewer 对同一 Revision 独立验证通过：REV-000050、REV-000051、REV-000052、REV-000053、REV-000054、REV-000055、REV-000056；ISSUE-MR-0001～0004 已由 ArchitectureReviewAgent 复核关闭。 TESTDESIGN-R01 已通过四项串行独立 Review：REV-000057, REV-000058, REV-000059, REV-000060。"
  },
  {
    "id": "TR-P1-COMPILER-009",
    "description": "统一 `mix` 编译骨架: BR-P1-018, BR-P1-020 -> AC-P1-COMPILER-009",
    "status": "COVERED",
    "acceptance_criteria": [
      "AC-P1-COMPILER-009"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-CR03/requirement.md",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-018",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-020",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-009",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-009"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#crossModuleImplementations/CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-MODEL-ACCESS-TARGET-MAIN",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#SCN-MODEL-ACCESS-PROPERTY-FALLBACK",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#VO-MODEL-ACCESS-BINDING",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#INV-COMPILER-007",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#POL-MODEL-ACCESS-SELECTOR",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ERR-MIX-MODEL-ACCESS-NOT-FOUND",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ERR-MIX-MODEL-ACCESS-AMBIGUOUS",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#ERR-MIX-MODEL-ACCESS-NON-COMPOSITE",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md#15-追踪映射"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-model-access-selector",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#design-diagnostic-catalog",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md#api-model-access",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md#test-model-access",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md"
    ],
    "test_case_ids": [
      "CASE-P1-TD-VIEW-BOUNDARY-001",
      "CASE-P1-TD-MODEL-ACCESS-TARGET-MAIN-001",
      "CASE-P1-TD-MODEL-ACCESS-PATH-001",
      "CASE-P1-TD-MODEL-ACCESS-NOT-FOUND-001",
      "CASE-P1-TD-MODEL-ACCESS-AMBIGUOUS-001",
      "CASE-P1-TD-MODEL-ACCESS-NON-COMPOSITE-001",
      "CASE-P1-TD-DIAGNOSTIC-CATALOG-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001",
      "TASK-P1-R2-005"
    ],
    "contract_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "dec-demo/src/main/resources/mix/system/systems.xml",
      "dec-demo/src/main/resources/mix/view/orm-view.xml",
      "dec-demo/src/main/resources/mix/business/order-business.xml",
      "version/V_1.0/doc/P1-COMPILER-F01/test_case.md"
    ],
    "implementation_refs": [
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java",
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py"
    ],
    "verification_evidence_ids": [
      "EVD-000286",
      "EVD-000287"
    ],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent",
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "ArchitectureReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "DevelopAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "notes": "DESIGN-R05@0b37a9b4dd48 已由七个 design Reviewer 对同一 Revision 独立验证通过：REV-000050、REV-000051、REV-000052、REV-000053、REV-000054、REV-000055、REV-000056；ISSUE-MR-0001～0004 已由 ArchitectureReviewAgent 复核关闭。 TESTDESIGN-R01 已通过四项串行独立 Review：REV-000057, REV-000058, REV-000059, REV-000060。"
  }
]
```

## Implementation Plan TP-P1-COMPILER-F01-R01

Revision：`TP-P1-COMPILER-F01-R01@88b56e6caa64`。以下映射由 `development_tasks.yaml` 生成，作为 TDD/开发任务入口。

| TR | 开发任务 |
|---|---|
| `TR-P1-COMPILER-001` | `TASK-P1-T03` |
| `TR-P1-COMPILER-002` | `TASK-P1-T04`, `TASK-P1-T05`, `TASK-P1-T06` |
| `TR-P1-COMPILER-003` | `TASK-P1-T01`, `TASK-P1-T07`, `TASK-P1-T08` |
| `TR-P1-COMPILER-004` | `TASK-P1-T01`, `TASK-P1-T09`, `TASK-P1-T11` |
| `TR-P1-COMPILER-005` | `TASK-P1-T01`, `TASK-P1-T12`, `TASK-P1-T13`, `TASK-P1-T14` |
| `TR-P1-COMPILER-006` | `TASK-P1-T01`, `TASK-P1-T15` |
| `TR-P1-COMPILER-007` | `TASK-P1-T02`, `TASK-P1-T15` |
| `TR-P1-COMPILER-008` | `TASK-P1-T09`, `TASK-P1-T11` |
| `TR-P1-COMPILER-009` | `TASK-P1-T10` |
