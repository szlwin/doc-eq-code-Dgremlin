# P1-COMPILER-F01 实施任务计划

```json task-plan
[
  {
    "id": "TASK-P1-REQCONF-001",
    "logical_task_id": "LOGICAL-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-001",
    "iteration_no": 1,
    "supersedes_iteration_id": "",
    "revision_reason": "首次确认",
    "title": "确认 P1 编译骨架目标与范围",
    "objective": "形成无阻塞歧义的需求确认 revision",
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
      "project_doc/version/V_1.0/doc/P1-COMPILER-F01/requirement.md",
      "project_doc/version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_testability_notes.md",
      "project_doc/version/V_1.0/task/P1-COMPILER-F01/decision_log.md",
      "project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/snapshots/requirement-confirmation-R01.md"
    ],
    "acceptance_trace_ids": [],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [],
    "expected_results": [
      "P1 目标、范围、范围外和关键决策无阻塞歧义",
      "完成维度可观察且包含失败和禁止副作用"
    ],
    "stop_conditions": [
      "发现与两份整改文档冲突",
      "P0 基线无法作为设计输入"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "REQCONF-R01@ac6d126dafb3",
    "validation_evidence_ids": [
      "EVD-000001",
      "EVD-000002",
      "EVD-000003"
    ]
  },
  {
    "id": "TASK-P1-REQAN-001",
    "logical_task_id": "LOGICAL-P1-COMPILER-F01-REQUIREMENT-ANALYSIS",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-ANALYSIS-001",
    "iteration_no": 1,
    "supersedes_iteration_id": "",
    "revision_reason": "首次分析",
    "title": "分析 P1 功能、规则、异常与追踪",
    "objective": "形成可设计、可测试的需求分析 revision",
    "phase": "requirement_analysis",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-REQCONF-001"
    ],
    "owner_agent": "RequirementAnalysisAgent",
    "reviewer_agents": [
      "BusinessModelAgent",
      "DesignAgent",
      "TestDesignAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent"
    ],
    "input_revisions": {
      "requirement_confirmation": "REQCONF-R01@ac6d126dafb3"
    },
    "allowed_files": [
      "project_doc/version/V_1.0/doc/P1-COMPILER-F01/requirement.md",
      "project_doc/version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_requirement_analysis.md",
      "project_doc/version/V_1.0/docs/_flows/COMPILER_flow.yaml",
      "project_doc/version/V_1.0/docs/_flows/COMPILER_flow.md",
      "project_doc/version/V_1.0/docs/_relations/dependency_impact.yaml",
      "project_doc/version/V_1.0/docs/_relations/dependency_graph.md",
      "project_doc/version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/snapshots/requirement-analysis-R02.md"
    ],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-001",
      "TR-P1-COMPILER-002",
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006"
    ],
    "flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "flow_step_refs": [
      "STEP-CONFIG-COMPILE-01",
      "STEP-CONFIG-COMPILE-02",
      "STEP-CONFIG-COMPILE-03",
      "STEP-CONFIG-COMPILE-04",
      "STEP-CONFIG-COMPILE-05",
      "STEP-CONFIG-COMPILE-06",
      "STEP-CONFIG-COMPILE-07"
    ],
    "validation_commands": [],
    "expected_results": [
      "功能、规则、异常、非功能要求和验收标准完整",
      "跨模块职责与影响映射可追踪到所有 AC"
    ],
    "stop_conditions": [
      "出现未决 P1 语义边界",
      "需求开始锁定具体实现类而非可观察行为"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "REQAN-R02@d38b7f83f222",
    "validation_evidence_ids": [
      "EVD-000011",
      "EVD-000012",
      "EVD-000013",
      "EVD-000014",
      "EVD-000015",
      "EVD-000016",
      "EVD-000017",
      "EVD-000018",
      "EVD-000019",
      "EVD-000020"
    ]
  },
  {
    "id": "TASK-P1-BMODEL-001",
    "logical_task_id": "LOGICAL-P1-COMPILER-F01-BUSINESS-MODEL",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-BUSINESS-MODEL-001",
    "iteration_no": 1,
    "supersedes_iteration_id": "",
    "revision_reason": "首次建模",
    "title": "建立编译领域模型与不变量",
    "objective": "形成支撑设计的统一语言、对象关系、状态与错误模型",
    "phase": "business_model",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-REQAN-001"
    ],
    "owner_agent": "BusinessModelAgent",
    "reviewer_agents": [
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent"
    ],
    "input_revisions": {
      "requirement_analysis": "REQAN-R02@d38b7f83f222"
    },
    "allowed_files": [
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md",
      "project_doc/version/V_1.0/task/P1-COMPILER-F01/traceability.md"
    ],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-001",
      "TR-P1-COMPILER-002",
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006"
    ],
    "flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "flow_step_refs": [
      "STEP-CONFIG-COMPILE-01",
      "STEP-CONFIG-COMPILE-02",
      "STEP-CONFIG-COMPILE-03",
      "STEP-CONFIG-COMPILE-04",
      "STEP-CONFIG-COMPILE-05",
      "STEP-CONFIG-COMPILE-06",
      "STEP-CONFIG-COMPILE-07"
    ],
    "validation_commands": [],
    "expected_results": [
      "编译领域对象、状态、不变量和错误模型无歧义",
      "模型覆盖全部适用追踪项且不实现 P2+ 语义"
    ],
    "stop_conditions": [
      "模型引入运行时全局单例",
      "模型将 parser 节点泄漏到 Compiled AST"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "BM-R01@52a58f20cb32",
    "validation_evidence_ids": [
      "EVD-000074",
      "EVD-000075",
      "EVD-000076",
      "EVD-000077",
      "EVD-000078",
      "EVD-000079",
      "EVD-000080",
      "EVD-000081"
    ]
  },
  {
    "id": "TASK-P1-DESIGN-001",
    "logical_task_id": "LOGICAL-P1-COMPILER-F01-DESIGN",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-DESIGN-001",
    "iteration_no": 1,
    "supersedes_iteration_id": "",
    "revision_reason": "首次设计",
    "title": "设计 AST、Registry、Compiler 与 EngineContext",
    "objective": "形成可实施、可测试、可演进的 P1 设计 revision",
    "phase": "design",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-BMODEL-001"
    ],
    "owner_agent": "DesignAgent",
    "reviewer_agents": [
      "RequirementReviewAgent",
      "BusinessModelReviewAgent",
      "ArchitectureReviewAgent",
      "TestDesignAgent",
      "DevelopAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent"
    ],
    "input_revisions": {
      "requirement_analysis": "REQAN-R02@d38b7f83f222",
      "business_model": "BM-R01@52a58f20cb32"
    },
    "allowed_files": [
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_architecture.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_api_contract.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md",
      "project_doc/version/V_1.0/docs/_relations/dependency_impact.yaml",
      "project_doc/version/V_1.0/docs/_relations/dependency_graph.md",
      "project_doc/version/V_1.0/task/P1-COMPILER-F01/traceability.md"
    ],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-001",
      "TR-P1-COMPILER-002",
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006"
    ],
    "flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "flow_step_refs": [
      "STEP-CONFIG-COMPILE-01",
      "STEP-CONFIG-COMPILE-02",
      "STEP-CONFIG-COMPILE-03",
      "STEP-CONFIG-COMPILE-04",
      "STEP-CONFIG-COMPILE-05",
      "STEP-CONFIG-COMPILE-06",
      "STEP-CONFIG-COMPILE-07"
    ],
    "validation_commands": [],
    "expected_results": [
      "模块依赖、类型契约、pipeline、失败发布、兼容和测试接缝可直接实施",
      "设计覆盖 P1-T01～T13 且未进入 P2+ 语义"
    ],
    "stop_conditions": [
      "出现模块循环依赖",
      "旧 Config 可写或 Context 全局化",
      "设计依赖 SQL/MySQL/demo"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "DESIGN-R01@a7a6820a381e",
    "validation_evidence_ids": [
      "EVD-000126",
      "EVD-000127",
      "EVD-000128",
      "EVD-000129",
      "EVD-000130",
      "EVD-000131",
      "EVD-000132",
      "EVD-000133"
    ]
  }
]
```

## 使用说明

字段集合以 `assets/long-task/record-contract.json#records.taskPlanItem` 为准。

每项任务至少包含：

```json
{
  "id": "TASK-001",
  "logical_task_id": "LOGICAL-MOD0001-F01-DEVELOPMENT",
  "feature_id": "MOD0001-F01",
  "iteration_id": "ITER-TARGET-DEVELOPMENT-001",
  "iteration_no": 1,
  "supersedes_iteration_id": "",
  "revision_reason": "首次实施",
  "title": "任务标题",
  "objective": "可观察交付",
  "phase": "development",
  "status": "PENDING",
  "depends_on": [],
  "owner_agent": "DevelopAgent",
  "reviewer_agents": ["SpecComplianceReviewAgent", "EngineeringStandardsReviewAgent"],
  "input_revisions": {},
  "allowed_files": [],
  "acceptance_trace_ids": [],
  "flow_refs": [],
  "flow_step_refs": [],
  "validation_commands": [],
  "expected_results": [],
  "stop_conditions": [],
  "risk_triggers": [],
  "attempts": 0,
  "max_attempts": 3,
  "output_revision": "",
  "validation_evidence_ids": []
}
```

- `depends_on` 不仅必须无环，启动时还要求依赖任务已经 `PASSED/NOT_APPLICABLE`。
- `max_attempts` 只限制当前 iteration 内的失败重试；正常重做必须由 `reopen-phase` 创建新 iteration。
- 全流程只允许 `SEQUENTIAL`；任务顺序只通过 `depends_on` 表达，同一时刻最多一个任务处于 `RUNNING`。
- 涉及结构化业务流程的任务必须填写 `flow_refs` 和 `flow_step_refs`；流程 ID 使用 `FLOW-*`，步骤 ID 使用 `STEP-*`。
- `expected_results` 仅保留自然语言说明；每一项必须通过 `TASK-ID#expected_results/{index}` 被 Acceptance Assertion 的 `source_refs` 覆盖。
- `validation_evidence_ids` 保存 Evidence Registry ID；`PASSED` 必须有 `output_revision` 和当前 revision 的 ACTIVE evidence。
- development 完成后必须生成 `risk_detection.json`，code-review 任务的 `risk_triggers` 覆盖所有未豁免高置信风险。
