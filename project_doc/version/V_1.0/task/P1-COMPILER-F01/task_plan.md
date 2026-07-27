# P1-COMPILER-F01 任务计划

> 当前登记 R04 活动任务；R01～R03 历史保留在 StageOutcome、Review 和 Evidence 中。

```json task-plan
[
  {
    "id": "TASK-P1-REQCONF-001",
    "logical_task_id": "LOGICAL-SUPERSEDED-TASK-P1-REQCONF-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-003",
    "revision_reason": "用户补充 ModelAccess 映射解析规则：ref@property 首先精确匹配目标 View 的 target-main；未匹配时再按 View property path 精确查找。该规则影响需求、验收、诊断与测试，需保留 R03 并形成新需求确认 Revision。",
    "title": "将稳定需求确认逻辑任务对齐到 REQCONF-R04",
    "objective": "确认稳定需求确认逻辑任务复用 REQCONF-R04 的需求、Review、Evidence 与 StageOutcome，不产生并行事实",
    "phase": "requirement_confirmation",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-R2-001"
    ],
    "owner_agent": "RequirementConfirmationAgent",
    "reviewer_agents": [
      "RequirementAnalysisAgent",
      "TestDesignAgent"
    ],
    "input_revisions": {},
    "allowed_files": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md",
      "version/V_1.0/doc/P1-COMPILER-CR01/requirement_change.md",
      "version/V_1.0/requirement_list.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "version/V_1.0/task/P1-COMPILER-F01/handoff.md",
      "version/V_1.0/task/P1-COMPILER-F01/acceptance_assertions.json",
      "version/V_1.0/doc/P1-COMPILER-CR01/requirement.md"
    ],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-001",
      "TR-P1-COMPILER-002",
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006",
      "TR-P1-COMPILER-007",
      "TR-P1-COMPILER-008",
      "TR-P1-COMPILER-009"
    ],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [],
    "expected_results": [
      "稳定逻辑任务绑定 REQCONF-R04@c186ce681e1e，复用同一机器校验、Review 与 Evidence，不产生第二份需求事实"
    ],
    "stop_conditions": [
      "不得生成与 REQCONF-R04 并行的需求确认 Revision"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "REQCONF-R04@c186ce681e1e",
    "validation_evidence_ids": [
      "EVD-000243",
      "EVD-000244",
      "EVD-000245",
      "EVD-000250",
      "EVD-000251"
    ]
  },
  {
    "id": "TASK-P1-REQAN-001",
    "logical_task_id": "LOGICAL-SUPERSEDED-TASK-P1-REQAN-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-ANALYSIS-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-ANALYSIS-003",
    "revision_reason": "用户已确认 REQCONF-R04 的 ModelAccess selector 规则，并进一步明确：跨多个 System 的 Information expression 由独立 common System 统一拥有；common Information 只组合 system-qualified InformationKey，不直接拥有 Data、View 或 RuleView。",
    "title": "重新分析 mix 源图与跨阶段影响",
    "objective": "基于 REQCONF-R04 分析 System-owned Information、System-local View、显式跨 View 映射、common 跨 System Information expression 及 BusinessScope 编排边界对 P1 与 P2～P7 的影响",
    "phase": "requirement_analysis",
    "status": "REWORK",
    "depends_on": [
      "TASK-P1-REQCONF-001"
    ],
    "owner_agent": "RequirementAnalysisAgent",
    "reviewer_agents": [
      "BusinessModelAgent",
      "DesignAgent",
      "TestDesignAgent"
    ],
    "input_revisions": {
      "requirement_confirmation": "REQCONF-R04@c186ce681e1e"
    },
    "allowed_files": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "dec-demo/src/main/resources/mix/system/systems.xml",
      "dec-demo/src/main/resources/mix/business/order-business.xml",
      "dec-demo/src/test/resources/mix/system/systems.xml",
      "dec-demo/src/test/resources/mix/business/order-business.xml",
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java",
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py"
    ],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-001",
      "TR-P1-COMPILER-002",
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006",
      "TR-P1-COMPILER-007",
      "TR-P1-COMPILER-008",
      "TR-P1-COMPILER-009"
    ],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [],
    "expected_results": [
      "REQAN-R04 对 REQCONF-R04 的 20 条业务规则、9 项 AC、7 个异常场景、9 条追踪及实际 mix 源图完成可追踪分析",
      "明确 InformationKey 以 SystemKey 为所有权边界，BusinessScope 不拥有 Information",
      "明确同一 System 内 expression 可组合本 System InformationKey；跨 System expression 必须由 common System 拥有并只引用 system-qualified InformationKey",
      "systems.xml 中 common.paySuccess 与 common.payError 分别组合 payment 与 order Information，业务目录引用同步更新",
      "明确 model-access read/write/ref 的一对多映射、冲突、缺失与失败规则",
      "BusinessModelAgent、DesignAgent、TestDesignAgent 对同一 REQAN-R04 独立 Review 均为 PASSED",
      "requirement_analysis StageOutcome 为 PASSED，且无开放 P0/P1 issue"
    ],
    "stop_conditions": [
      "dec-expand-declaration or second runtime reintroduced"
    ],
    "risk_triggers": [],
    "attempts": 0,
    "max_attempts": 3,
    "output_revision": "",
    "validation_evidence_ids": []
  },
  {
    "id": "TASK-P1-BMODEL-001",
    "logical_task_id": "LOGICAL-SUPERSEDED-TASK-P1-BMODEL-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-BUSINESS-MODEL-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-BUSINESS-MODEL-003",
    "revision_reason": "用户补充 ModelAccess 映射解析规则：ref@property 首先精确匹配目标 View 的 target-main；未匹配时再按 View property path 精确查找。该规则影响需求、验收、诊断与测试，需保留 R03 并形成新需求确认 Revision。",
    "title": "重建 RawDefinition、CompiledModelSet 与 Deferred 模型",
    "objective": "重建 RawDefinition、CompiledModelSet 与 Deferred 模型",
    "phase": "business_model",
    "status": "REWORK",
    "depends_on": [
      "TASK-P1-REQAN-001"
    ],
    "owner_agent": "BusinessModelAgent",
    "reviewer_agents": [],
    "input_revisions": {
      "change_requirement": "P1-COMPILER-CR01",
      "draft_design": "DESIGN-R02-DRAFT"
    },
    "allowed_files": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml",
      "project_doc/version/V_1.0/task/P1-COMPILER-F01/traceability.md"
    ],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-001",
      "TR-P1-COMPILER-002",
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006",
      "TR-P1-COMPILER-007"
    ],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [],
    "expected_results": [
      "R02 artifacts reviewed and internally consistent"
    ],
    "stop_conditions": [
      "dec-expand-declaration or second runtime reintroduced"
    ],
    "risk_triggers": [],
    "attempts": 0,
    "max_attempts": 3,
    "output_revision": "",
    "validation_evidence_ids": []
  },
  {
    "id": "TASK-P1-DESIGN-001",
    "logical_task_id": "LOGICAL-SUPERSEDED-TASK-P1-DESIGN-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-DESIGN-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-DESIGN-003",
    "revision_reason": "用户补充 ModelAccess 映射解析规则：ref@property 首先精确匹配目标 View 的 target-main；未匹配时再按 View property path 精确查找。该规则影响需求、验收、诊断与测试，需保留 R03 并形成新需求确认 Revision。",
    "title": "评审统一源图、Compiler Pipeline 与只读投影设计",
    "objective": "评审统一源图、Compiler Pipeline 与只读投影设计",
    "phase": "design",
    "status": "REWORK",
    "depends_on": [
      "TASK-P1-BMODEL-001"
    ],
    "owner_agent": "DesignAgent",
    "reviewer_agents": [],
    "input_revisions": {
      "change_requirement": "P1-COMPILER-CR01",
      "draft_design": "DESIGN-R02-DRAFT"
    },
    "allowed_files": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_architecture.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_architecture.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md",
      "project_doc/version/V_1.0/task/P1-COMPILER-F01/traceability.md"
    ],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-001",
      "TR-P1-COMPILER-002",
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006",
      "TR-P1-COMPILER-007"
    ],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [],
    "expected_results": [
      "R02 artifacts reviewed and internally consistent"
    ],
    "stop_conditions": [
      "dec-expand-declaration or second runtime reintroduced"
    ],
    "risk_triggers": [],
    "attempts": 0,
    "max_attempts": 3,
    "output_revision": "",
    "validation_evidence_ids": []
  },
  {
    "id": "TASK-P1-R2-001",
    "logical_task_id": "LOGICAL-TASK-P1-R2-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-003",
    "revision_reason": "用户补充 ModelAccess 映射解析规则：ref@property 首先精确匹配目标 View 的 target-main；未匹配时再按 View property path 精确查找。该规则影响需求、验收、诊断与测试，需保留 R03 并形成新需求确认 Revision。",
    "title": "确认 System-owned Information、跨 View 映射与 target-main 解析契约",
    "objective": "确认 Information 归属 System、仅关联本 System View，BusinessScope 只负责编排，并以 model-access read/ref 映射共享模型路径",
    "phase": "requirement_confirmation",
    "status": "PASSED",
    "depends_on": [],
    "owner_agent": "RequirementConfirmationAgent",
    "reviewer_agents": [
      "RequirementAnalysisAgent",
      "TestDesignAgent"
    ],
    "input_revisions": {},
    "allowed_files": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md",
      "version/V_1.0/doc/P1-COMPILER-CR01/requirement_change.md",
      "version/V_1.0/requirement_list.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "version/V_1.0/task/P1-COMPILER-F01/handoff.md",
      "version/V_1.0/task/P1-COMPILER-F01/acceptance_assertions.json",
      "version/V_1.0/doc/P1-COMPILER-CR01/requirement.md",
      "version/V_1.0/doc/P1-COMPILER-CR02/requirement.md",
      "version/V_1.0/doc/P1-COMPILER-CR02/requirement_change.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_mix_contract_inventory.md",
      "dec-demo/src/main/resources/mix/system/systems.xml",
      "dec-demo/src/main/resources/mix/view/orm-view.xml",
      "dec-demo/src/main/resources/mix/rule/user-rule.xml",
      "dec-demo/src/main/resources/mix/business/order-business.xml",
      "dec-demo/src/test/resources/mix/system/systems.xml",
      "dec-demo/src/test/resources/mix/view/orm-view.xml",
      "dec-demo/src/test/resources/mix/rule/user-rule.xml",
      "dec-demo/src/test/resources/mix/business/order-business.xml",
      "dec-demo/src/test/java/dec/demo/contract/MixContractTest.java",
      "version/V_1.0/task/P1-COMPILER-F01/decision_log.md",
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py",
      "version/V_1.0/doc/P1-COMPILER-CR03/requirement.md",
      "version/V_1.0/doc/P1-COMPILER-CR03/requirement_change.md",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md"
    ],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-001",
      "TR-P1-COMPILER-002",
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006",
      "TR-P1-COMPILER-007",
      "TR-P1-COMPILER-008",
      "TR-P1-COMPILER-009"
    ],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [
      "python3 scripts/requirement_doc.py validate -g RequirementConfirmationAgent --file project_doc/version/V_1.0/doc/P1-COMPILER-F01/requirement.md --stage confirmation --json"
    ],
    "expected_results": [
      "REQCONF-R04 明确 Information 归属 System 且只能关联该 System view-info 中声明的 View",
      "BusinessScope 不再拥有 Information，仅通过 system-qualified information-ref 编排 Directory/Action/Produce",
      "user System 不再声明 OrderInfo，并通过 model-access/read/ref 将 OrderInfo.user 映射到 UserInfo.user",
      "主资源与测试资源 XML 同步且契约测试验证所有 Information 的 System/View 归属和跨 View 映射",
      "RequirementAnalysisAgent 与 TestDesignAgent 对同一 REQCONF-R04 独立 Review 均为 PASSED",
      "ref@property first matches the selected View target-main exactly",
      "property path lookup is used only when target-main does not match",
      "missing or ambiguous selector fails without fuzzy or cross-View fallback"
    ],
    "stop_conditions": [
      "Information remains owned by BusinessScope",
      "Information references a View not declared by its owning System",
      "dec-expand-declaration or second runtime reintroduced",
      "ModelAccess selector skips target-main and directly guesses properties",
      "ModelAccess selector performs fuzzy/global View matching",
      "root-property is reintroduced as a second root alias"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "REQCONF-R04@c186ce681e1e",
    "validation_evidence_ids": [
      "EVD-000243",
      "EVD-000244",
      "EVD-000245",
      "EVD-000246",
      "EVD-000247",
      "EVD-000248",
      "EVD-000249",
      "EVD-000250",
      "EVD-000251"
    ]
  },
  {
    "id": "TASK-P1-R2-005",
    "logical_task_id": "LOGICAL-TASK-P1-R2-005",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-TEST-DESIGN-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-TEST-DESIGN-003",
    "revision_reason": "用户补充 ModelAccess 映射解析规则：ref@property 首先精确匹配目标 View 的 target-main；未匹配时再按 View property path 精确查找。该规则影响需求、验收、诊断与测试，需保留 R03 并形成新需求确认 Revision。",
    "title": "形成七项 AC 的可执行测试设计",
    "objective": "形成七项 AC 的可执行测试设计",
    "phase": "test_design",
    "status": "REWORK",
    "depends_on": [
      "TASK-P1-DESIGN-001"
    ],
    "owner_agent": "TestDesignAgent",
    "reviewer_agents": [],
    "input_revisions": {
      "change_requirement": "P1-COMPILER-CR01",
      "draft_design": "DESIGN-R02-DRAFT"
    },
    "allowed_files": [],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-001",
      "TR-P1-COMPILER-002",
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006",
      "TR-P1-COMPILER-007"
    ],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [],
    "expected_results": [
      "R02 artifacts reviewed and internally consistent"
    ],
    "stop_conditions": [
      "dec-expand-declaration or second runtime reintroduced"
    ],
    "risk_triggers": [],
    "attempts": 0,
    "max_attempts": 3,
    "output_revision": "",
    "validation_evidence_ids": []
  },
  {
    "id": "TASK-P1-R2-006",
    "logical_task_id": "LOGICAL-TASK-P1-R2-006",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-IMPLEMENTATION-PLAN-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-IMPLEMENTATION-PLAN-003",
    "revision_reason": "用户补充 ModelAccess 映射解析规则：ref@property 首先精确匹配目标 View 的 target-main；未匹配时再按 View property path 精确查找。该规则影响需求、验收、诊断与测试，需保留 R03 并形成新需求确认 Revision。",
    "title": "形成 P1-T01～T15 实施计划",
    "objective": "形成 P1-T01～T15 实施计划",
    "phase": "implementation_plan",
    "status": "REWORK",
    "depends_on": [
      "TASK-P1-R2-005"
    ],
    "owner_agent": "ImplementationPlanAgent",
    "reviewer_agents": [],
    "input_revisions": {
      "change_requirement": "P1-COMPILER-CR01",
      "draft_design": "DESIGN-R02-DRAFT"
    },
    "allowed_files": [],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-001",
      "TR-P1-COMPILER-002",
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006",
      "TR-P1-COMPILER-007"
    ],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [],
    "expected_results": [
      "R02 artifacts reviewed and internally consistent"
    ],
    "stop_conditions": [
      "dec-expand-declaration or second runtime reintroduced"
    ],
    "risk_triggers": [],
    "attempts": 0,
    "max_attempts": 3,
    "output_revision": "",
    "validation_evidence_ids": []
  }
]
```
