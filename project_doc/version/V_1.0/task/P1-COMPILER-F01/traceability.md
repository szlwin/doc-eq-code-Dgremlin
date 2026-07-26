# P1-COMPILER-F01 追踪矩阵

> R02 草案。旧 R01 Review/Evidence 保留为历史，不覆盖本矩阵。

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
  }
]
```
