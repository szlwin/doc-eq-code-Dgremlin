# P1-COMPILER-F01 需求—模型—设计—计划—实现—测试追踪矩阵

```json traceability
[
  {
    "id": "TR-P1-COMPILER-001",
    "description": "统一编译上下文骨架: BR-P1-COMPILER-001, BR-P1-COMPILER-002, BR-P1-COMPILER-003, CR-P1-COMPILER-001 -> AC-P1-COMPILER-001",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-001"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-001",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-002",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-003",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#CR-P1-COMPILER-001",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-001",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-001"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml#IMP-P1-COMPILER-001"
    ],
    "business_flow_required": true,
    "business_flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#TR-P1-COMPILER-001",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_architecture.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md"
    ],
    "test_case_ids": [
      "CASE-P1-CANONICAL-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001"
    ],
    "contract_refs": [],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent"
    ],
    "notes": "P1 只要求最小 YAML 路径；完整对等在 P8。"
  },
  {
    "id": "TR-P1-COMPILER-002",
    "description": "统一编译上下文骨架: BR-P1-COMPILER-004, BR-P1-COMPILER-005, BR-P1-COMPILER-006, CR-P1-COMPILER-004 -> AC-P1-COMPILER-002",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-002"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-004",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-005",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-006",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#CR-P1-COMPILER-004",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-002",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-002"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml#IMP-P1-COMPILER-002"
    ],
    "business_flow_required": true,
    "business_flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#TR-P1-COMPILER-002",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_architecture.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md"
    ],
    "test_case_ids": [
      "CASE-P1-DIAGNOSTIC-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001"
    ],
    "contract_refs": [],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent"
    ],
    "notes": "禁止部分成功或空成功。"
  },
  {
    "id": "TR-P1-COMPILER-003",
    "description": "统一编译上下文骨架: BR-P1-COMPILER-007, BR-P1-COMPILER-008 -> AC-P1-COMPILER-003",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-003"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-007",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-008",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-003",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-003"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml#IMP-P1-COMPILER-003"
    ],
    "business_flow_required": true,
    "business_flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#TR-P1-COMPILER-003",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_architecture.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md"
    ],
    "test_case_ids": [
      "CASE-P1-SYMBOL-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001"
    ],
    "contract_refs": [],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent"
    ],
    "notes": "RuleViewKey 在 P1 仅预留 (system,name)，P2 才启用完整 System 语义。"
  },
  {
    "id": "TR-P1-COMPILER-004",
    "description": "统一编译上下文骨架: BR-P1-COMPILER-009, BR-P1-COMPILER-010, BR-P1-COMPILER-011, CR-P1-COMPILER-002, CR-P1-COMPILER-003 -> AC-P1-COMPILER-004",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-004"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-009",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-010",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-011",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#CR-P1-COMPILER-002",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#CR-P1-COMPILER-003",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-004",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-004"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml#IMP-P1-COMPILER-004"
    ],
    "business_flow_required": true,
    "business_flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#TR-P1-COMPILER-004",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_architecture.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md"
    ],
    "test_case_ids": [
      "CASE-P1-CONTEXT-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001"
    ],
    "contract_refs": [],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent"
    ],
    "notes": "EngineContext 不得成为新的全局 current Context。"
  },
  {
    "id": "TR-P1-COMPILER-005",
    "description": "统一编译上下文骨架: BR-P1-COMPILER-012, CR-P1-COMPILER-005 -> AC-P1-COMPILER-005",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-005"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-012",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#CR-P1-COMPILER-005",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-005",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-005"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml#IMP-P1-COMPILER-005"
    ],
    "business_flow_required": true,
    "business_flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#TR-P1-COMPILER-005",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_architecture.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md"
    ],
    "test_case_ids": [
      "CASE-P1-LEGACY-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001"
    ],
    "contract_refs": [],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent"
    ],
    "notes": "兼容窗口与删除阶段在后续阶段明确。"
  },
  {
    "id": "TR-P1-COMPILER-006",
    "description": "统一编译上下文骨架: BR-P1-COMPILER-013, CR-P1-COMPILER-006 -> AC-P1-COMPILER-006",
    "status": "PENDING",
    "acceptance_criteria": [
      "AC-P1-COMPILER-006"
    ],
    "requirement_refs": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#P1-COMPILER-F01",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#BR-P1-COMPILER-013",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#CR-P1-COMPILER-006",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#AC-P1-COMPILER-006",
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md#TR-P1-COMPILER-006"
    ],
    "impact_required": true,
    "dependency_impact_refs": [
      "docs/_relations/dependency_impact.yaml#IMP-P1-COMPILER-006"
    ],
    "business_flow_required": true,
    "business_flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "cross_module_implementation_required": true,
    "cross_module_implementation_refs": [
      "docs/_relations/dependency_impact.yaml#CMI-P1-COMPILER-001"
    ],
    "business_model_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml#TR-P1-COMPILER-006",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md"
    ],
    "design_refs": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_architecture.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md"
    ],
    "test_case_ids": [
      "CASE-P1-SCOPE-001"
    ],
    "plan_task_ids": [
      "TASK-P1-REQAN-001",
      "TASK-P1-BMODEL-001",
      "TASK-P1-DESIGN-001"
    ],
    "contract_refs": [],
    "implementation_refs": [],
    "verification_evidence_ids": [],
    "verified_by_agents": [
      "RequirementAnalysisAgent",
      "BusinessModelAgent",
      "DesignAgent"
    ],
    "notes": "未支持语义必须显式诊断或标记 deferred，不能返回空成功。"
  }
]
```

## 使用说明

字段集合以 `assets/long-task/record-contract.json#records.traceabilityItem` 为准。

每个业务规则、验收项或重要非功能约束建立稳定 ID：

```json
{
  "id": "TR-MOD0001-001",
  "description": "已取消订单不可支付",
  "status": "PENDING",
  "acceptance_criteria": ["AC-ORDER-007"],
  "requirement_refs": [],
  "impact_required": false,
  "dependency_impact_refs": [],
  "business_flow_required": false,
  "business_flow_refs": [],
  "cross_module_implementation_required": false,
  "cross_module_implementation_refs": [],
  "business_model_refs": [],
  "design_refs": [],
  "test_case_ids": [],
  "plan_task_ids": [],
  "contract_refs": [],
  "implementation_refs": [],
  "verification_evidence_ids": [],
  "verified_by_agents": [],
  "notes": ""
}
```

状态只使用 `PENDING`、`COVERED`、`GAP`、`STALE`、`NOT_APPLICABLE`。`verification_evidence_ids` 保存 evidence ID；每个适用 `acceptance_criteria` 必须在 `acceptance_assertions.json` 中有结构化 assertion。

## 关联影响规则

- 删除、取消、失效、迁移、替代、跨状态联动或需求/功能依赖存在时，`impact_required=true`，并填写 `dependency_impact_refs`。
- 涉及三步以上、状态流转、变体、回退/补偿或端到端场景时，`business_flow_required=true`，并填写稳定 `FLOW-*` 到 `business_flow_refs`。
- 涉及两个及以上业务模块的技术协作时，`cross_module_implementation_required=true`，并填写 `cross_module_implementation_refs`，引用 `dependency_impact.yaml` 中的跨模块实现映射。
- 无关联影响时保持 `false` 和空数组，不得为了过门禁生成虚假关系。
