# P1-COMPILER-F01 追踪矩阵

> REQCONF-R04 候选。旧 Revision、Review 与 Evidence 保留为历史，不覆盖本矩阵。

```json traceability
[
  {
    "id": "TR-P1-COMPILER-001",
    "description": "实际 mix 源图发现",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-001"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-001"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#4-mixsourceresolver"
    ],
    "test_case_ids": [
      "CASE-P1-MIX-DISCOVERY-001"
    ],
    "plan_task_ids": [
      "TASK-P1-R2-001"
    ],
    "contract_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md"
    ],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [],
    "notes": "R02 草案，待对应阶段和串行 Review。"
  },
  {
    "id": "TR-P1-COMPILER-002",
    "description": "统一前端与 RawDefinitionSet",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-002"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-002"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#5-canonicaldocumentnode"
    ],
    "test_case_ids": [
      "CASE-P1-MIX-RAW-001"
    ],
    "plan_task_ids": [
      "TASK-P1-R2-002"
    ],
    "contract_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md"
    ],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [],
    "notes": "R02 草案，待对应阶段和串行 Review。"
  },
  {
    "id": "TR-P1-COMPILER-003",
    "description": "强类型符号与引用",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-003"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-003"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#7-key-与符号注册"
    ],
    "test_case_ids": [
      "CASE-P1-SYMBOL-001"
    ],
    "plan_task_ids": [
      "TASK-P1-R2-003"
    ],
    "contract_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md"
    ],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [],
    "notes": "R02 草案，待对应阶段和串行 Review。"
  },
  {
    "id": "TR-P1-COMPILER-004",
    "description": "Deferred 阶段边界",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-004"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-004"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#9-deferreddefinition"
    ],
    "test_case_ids": [
      "CASE-P1-DEFERRED-001"
    ],
    "plan_task_ids": [
      "TASK-P1-R2-004"
    ],
    "contract_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md"
    ],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [],
    "notes": "R02 草案，待对应阶段和串行 Review。"
  },
  {
    "id": "TR-P1-COMPILER-005",
    "description": "不可变发布与摘要",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-005"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-005"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#11-compiledmodelset"
    ],
    "test_case_ids": [
      "CASE-P1-CONTEXT-001"
    ],
    "plan_task_ids": [
      "TASK-P1-R2-005"
    ],
    "contract_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md"
    ],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [],
    "notes": "R02 草案，待对应阶段和串行 Review。"
  },
  {
    "id": "TR-P1-COMPILER-006",
    "description": "旧核心只读投影",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-006"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-006"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#13-coreconfigprojection"
    ],
    "test_case_ids": [
      "CASE-P1-PROJECTION-001"
    ],
    "plan_task_ids": [
      "TASK-P1-R2-006"
    ],
    "contract_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md"
    ],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [],
    "notes": "R02 草案，待对应阶段和串行 Review。"
  },
  {
    "id": "TR-P1-COMPILER-007",
    "description": "临时模块整体退役",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-007"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-007"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md#16-dec-expand-declaration-退役"
    ],
    "test_case_ids": [
      "CASE-P1-RETIREMENT-001"
    ],
    "plan_task_ids": [
      "TASK-P1-R2-006"
    ],
    "contract_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md"
    ],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [],
    "notes": "R02 草案，待对应阶段和串行 Review。"
  },
  {
    "id": "TR-P1-COMPILER-008",
    "description": "System-owned Information 与显式跨 View 属性映射",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-008"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-008",
      "version/V_1.0/doc/P1-COMPILER-CR02/requirement.md"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [],
    "business_model_refs": [],
    "design_refs": [],
    "test_case_ids": [
      "CASE-P1-SYSTEM-INFORMATION-001",
      "CASE-P1-MODEL-ACCESS-REF-001"
    ],
    "plan_task_ids": [
      "TASK-P1-R2-001",
      "TASK-P1-REQAN-001"
    ],
    "contract_refs": [
      "dec-demo/src/main/resources/mix/system/systems.xml",
      "dec-demo/src/main/resources/mix/business/order-business.xml"
    ],
    "implementation_refs": [
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java",
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py"
    ],
    "verification_evidence_ids": [],
    "verified_by_agents": [],
    "notes": "REQCONF-R03 已形成 XML 契约和独立结构测试；后续需求分析、业务模型与设计需基于该边界重做。"
  },
  {
    "id": "TR-P1-COMPILER-009",
    "description": "ModelAccess target-main 优先与 property path 回退",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-009"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-009",
      "version/V_1.0/doc/P1-COMPILER-CR03/requirement.md"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml"
    ],
    "business_flow_required": false,
    "business_flow_refs": [],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [],
    "business_model_refs": [],
    "design_refs": [],
    "test_case_ids": [
      "CASE-P1-MODEL-ACCESS-TARGET-MAIN-001",
      "CASE-P1-MODEL-ACCESS-PROPERTY-FALLBACK-001",
      "CASE-P1-MODEL-ACCESS-SELECTOR-ERROR-001"
    ],
    "plan_task_ids": [
      "TASK-P1-R2-001",
      "TASK-P1-REQAN-001"
    ],
    "contract_refs": [
      "dec-demo/src/main/resources/mix/system/systems.xml",
      "dec-demo/src/main/resources/mix/view/orm-view.xml"
    ],
    "implementation_refs": [
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java",
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py"
    ],
    "verification_evidence_ids": [],
    "verified_by_agents": [],
    "notes": "REQCONF-R04 补充 target-main 优先、property path 回退及失败阻断；后续分析和设计需定义 Diagnostic code 与嵌套路径规则。"
  }
]
```
