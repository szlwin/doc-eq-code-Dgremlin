# P1-COMPILER-F01 任务计划

> 当前只登记 R02 活动任务；R01 历史保留在 StageOutcome、Review 和 Evidence 中。

```json task-plan
[
  {
    "id": "TASK-P1-REQCONF-001",
    "logical_task_id": "LOGICAL-SUPERSEDED-TASK-P1-REQCONF-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-002",
    "iteration_no": 2,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-001",
    "revision_reason": "R01 stable logical task reconciled to P1-COMPILER-CR01 / REQCONF-R02",
    "title": "将稳定需求确认逻辑任务对齐到 REQCONF-R02",
    "objective": "确认原稳定逻辑任务已由 REQCONF-R02 正式 Revision、Review 和 StageOutcome 完整替代",
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
      "TR-P1-COMPILER-007"
    ],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [],
    "expected_results": [
      "稳定逻辑任务绑定 REQCONF-R02@d0868f1b679b，复用同一机器校验、Review 与 Evidence，不产生第二份需求事实"
    ],
    "stop_conditions": [
      "不得生成与 REQCONF-R02 并行的需求确认 Revision"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "REQCONF-R02@d0868f1b679b",
    "validation_evidence_ids": [
      "EVD-000220",
      "EVD-000221",
      "EVD-000222",
      "EVD-000223",
      "EVD-000224",
      "EVD-000226",
      "EVD-000227",
      "EVD-000228",
      "EVD-000229"
    ]
  },
  {
    "id": "TASK-P1-REQAN-001",
    "logical_task_id": "LOGICAL-SUPERSEDED-TASK-P1-REQAN-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-ANALYSIS-002",
    "iteration_no": 2,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-ANALYSIS-001",
    "revision_reason": "P1-COMPILER-CR01 + actual mix fixture",
    "title": "重新分析 mix 源图与跨阶段影响",
    "objective": "重新分析 mix 源图与跨阶段影响",
    "phase": "requirement_analysis",
    "status": "READY",
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
      "requirement_confirmation": "REQCONF-R02@d0868f1b679b"
    },
    "allowed_files": [
      "version/V_1.0/doc/P1-COMPILER-F01/requirement.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md"
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
      "REQAN-R03 对 REQCONF-R02 的 15 条业务规则、7 项 AC、5 个异常场景和实际 mix 源图完成可追踪分析",
      "BusinessModelAgent、DesignAgent、TestDesignAgent 对同一 REQAN-R03 独立 Review 均为 PASSED",
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
    "iteration_id": "ITER-P1-COMPILER-F01-BUSINESS-MODEL-002",
    "iteration_no": 2,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-BUSINESS-MODEL-001",
    "revision_reason": "P1-COMPILER-CR01 + actual mix fixture",
    "title": "重建 RawDefinition、CompiledModelSet 与 Deferred 模型",
    "objective": "重建 RawDefinition、CompiledModelSet 与 Deferred 模型",
    "phase": "business_model",
    "status": "WAITING",
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
    "iteration_id": "ITER-P1-COMPILER-F01-DESIGN-002",
    "iteration_no": 2,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-DESIGN-001",
    "revision_reason": "P1-COMPILER-CR01 + actual mix fixture",
    "title": "评审统一源图、Compiler Pipeline 与只读投影设计",
    "objective": "评审统一源图、Compiler Pipeline 与只读投影设计",
    "phase": "design",
    "status": "WAITING",
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
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-002",
    "iteration_no": 2,
    "supersedes_iteration_id": "",
    "revision_reason": "P1-COMPILER-CR01 + actual mix fixture",
    "title": "重新确认实际 mix 与模块退役范围",
    "objective": "重新确认实际 mix 与模块退役范围",
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
      "version/V_1.0/doc/P1-COMPILER-CR01/requirement.md"
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
    "validation_commands": [
      "python3 scripts/requirement_doc.py validate -g RequirementConfirmationAgent --file project_doc/version/V_1.0/doc/P1-COMPILER-F01/requirement.md --stage confirmation --json"
    ],
    "expected_results": [
      "REQCONF-R02 正式 Revision 锁定目标、范围内外、七项验收、失败边界和五项持久决策",
      "RequirementAnalysisAgent 与 TestDesignAgent 对同一 REQCONF-R02 独立 Review 均为 PASSED",
      "requirement_confirmation StageOutcome 为 PASSED，且无开放 P0/P1 issue"
    ],
    "stop_conditions": [
      "dec-expand-declaration or second runtime reintroduced"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "REQCONF-R02@d0868f1b679b",
    "validation_evidence_ids": [
      "EVD-000220",
      "EVD-000221",
      "EVD-000222",
      "EVD-000223",
      "EVD-000224",
      "EVD-000226"
    ]
  },
  {
    "id": "TASK-P1-R2-005",
    "logical_task_id": "LOGICAL-TASK-P1-R2-005",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-TEST-DESIGN-002",
    "iteration_no": 2,
    "supersedes_iteration_id": "",
    "revision_reason": "P1-COMPILER-CR01 + actual mix fixture",
    "title": "形成七项 AC 的可执行测试设计",
    "objective": "形成七项 AC 的可执行测试设计",
    "phase": "test_design",
    "status": "WAITING",
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
    "iteration_id": "ITER-P1-COMPILER-F01-IMPLEMENTATION-PLAN-002",
    "iteration_no": 2,
    "supersedes_iteration_id": "",
    "revision_reason": "P1-COMPILER-CR01 + actual mix fixture",
    "title": "形成 P1-T01～T15 实施计划",
    "objective": "形成 P1-T01～T15 实施计划",
    "phase": "implementation_plan",
    "status": "WAITING",
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
