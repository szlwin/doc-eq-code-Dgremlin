# P1-COMPILER-F01 任务计划

> `TP-P1-COMPILER-F01-R01@88b56e6caa64` 已通过四项串行计划 Review；下一阶段为 `TDD-I007`，首个增量为 `TASK-P1-T01`。

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
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-ANALYSIS-005",
    "iteration_no": 5,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-ANALYSIS-004",
    "revision_reason": "独立设计复核发现 REQAN-R04 的 Atomic exposure owner 与需求正文/BM-R04 不一致，且 dependency_impact.yaml 仍为旧 2.42 结构；需形成新 REQAN Revision 并重建下游。",
    "title": "重新分析 mix 源图与跨阶段影响",
    "objective": "基于 REQCONF-R04 分析 System-owned Information、System-local View、显式跨 View 映射、common 跨 System Information expression 及 BusinessScope 编排边界对 P1 与 P2～P7 的影响",
    "phase": "requirement_analysis",
    "status": "PASSED",
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
      "version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py",
      "project_doc/docs/_relations/dependency_impact.yaml",
      "project_doc/docs/_relations/dependency_graph.md",
      "project_doc/version/V_1.0/doc/_flows/COMPILER/changes/001-layout-migration.yaml",
      "project_doc/version/V_1.0/doc/_flows/COMPILER/generated/COMPILER_flow.preview.yaml",
      "project_doc/version/V_1.0/doc/_flows/COMPILER/generated/COMPILER_flow.preview.md",
      "docs/_relations/dependency_impact.yaml",
      "docs/_relations/dependency_graph.md",
      "version/V_1.0/doc/_flows/COMPILER/changes/001-layout-migration.yaml",
      "version/V_1.0/doc/_flows/COMPILER/generated/COMPILER_flow.preview.yaml",
      "version/V_1.0/doc/_flows/COMPILER/generated/COMPILER_flow.preview.md"
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
      "REQAN-R05 对 REQCONF-R04 的 20 条业务规则、9 项 AC、7 个异常场景、9 条追踪及实际 mix 源图完成可追踪分析",
      "明确 InformationKey 以 SystemKey 为所有权边界，BusinessScope 不拥有 Information",
      "明确同一 System 内 expression 可组合本 System InformationKey；跨 System expression 必须由 common System 拥有并只引用 system-qualified InformationKey",
      "systems.xml 中 common.paySuccess 与 common.payError 分别组合 payment 与 order Information，业务目录引用同步更新",
      "明确 model-access read/write/ref 的一对多映射、冲突、缺失与失败规则",
      "BusinessModelAgent、DesignAgent、TestDesignAgent 对同一 REQAN-R05 独立 Review 均为 PASSED",
      "requirement_analysis StageOutcome 为 PASSED，且无开放 P0/P1 issue"
    ],
    "stop_conditions": [
      "dec-expand-declaration or second runtime reintroduced"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "REQAN-R05@7de35e8dc15b",
    "validation_evidence_ids": [
      "EVD-000280"
    ]
  },
  {
    "id": "TASK-P1-BMODEL-001",
    "logical_task_id": "LOGICAL-SUPERSEDED-TASK-P1-BMODEL-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-BUSINESS-MODEL-005",
    "iteration_no": 5,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-BUSINESS-MODEL-004",
    "revision_reason": "独立设计复核发现 REQAN-R04 的 Atomic exposure owner 与需求正文/BM-R04 不一致，且 dependency_impact.yaml 仍为旧 2.42 结构；需形成新 REQAN Revision 并重建下游。",
    "title": "形成 REQAN-R05 对应的 Compiler 业务模型",
    "objective": "基于 REQAN-R05 建立 CompilationSession、RawDefinitionSet、TypedKey、ModelAccessBinding、DeferredDefinition、CompiledModelSet、EngineContext、Diagnostic 及 compiler-owned 原子发布的一致业务模型",
    "phase": "business_model",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-REQAN-001"
    ],
    "owner_agent": "BusinessModelAgent",
    "reviewer_agents": [
      "BusinessModelReviewAgent",
      "DesignReviewAgent",
      "RequirementReviewAgent",
      "TestDesignAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent"
    ],
    "input_revisions": {
      "requirement_analysis": "REQAN-R05@7de35e8dc15b"
    },
    "allowed_files": [
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md",
      "project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml",
      "project_doc/version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "project_doc/version/V_1.0/task/P1-COMPILER-F01/task_plan.md"
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
      "BM-R05 以结构化 YAML 与等价 Markdown 建立 CompilationSession 和 PublishedContext 两个聚合边界，覆盖 RawDefinition、TypedKey、Deferred、Diagnostic 与 compiler-owned 原子发布",
      "InformationKey 以 SystemKey 为所有权边界；BusinessScope 不拥有 Information；common 只允许跨 System expression Information且不拥有 Data/View/RuleView/ModelAccess",
      "ModelAccessBinding 明确 source path 与 target selector 分离，selector 先精确匹配 target-main，未命中时才逐段精确解析 property path，歧义或缺失均阻断发布",
      "P2～P8 的 DeferredDefinition、责任阶段、已解析 Key、SourceRef、失败与恢复边界完整可追踪",
      "九条 TR 均引用 BM-R05 稳定模型 ID，且六个适用 Reviewer 对同一 Revision 独立 PASSED",
      "business_model StageOutcome 为 PASSED，无开放 P0/P1，并创建 Git checkpoint"
    ],
    "stop_conditions": [
      "不得重新引入 dec-expand-declaration、兼容 Adapter 或第二运行时",
      "不得让 BusinessScope 拥有 Information 或普通 System 组合跨 System expression",
      "不得使用 root-property、模糊匹配、跨 View 搜索或静默降级",
      "不得在 P1 提前实现 P3 Information DAG/evaluation、P4-P7 运行语义"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "BM-R05@4ecb1f8c09f4",
    "validation_evidence_ids": [
      "EVD-000282"
    ]
  },
  {
    "id": "TASK-P1-DESIGN-001",
    "logical_task_id": "LOGICAL-SUPERSEDED-TASK-P1-DESIGN-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-DESIGN-007",
    "iteration_no": 7,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-DESIGN-006",
    "revision_reason": "独立设计复核发现 REQAN-R04 的 Atomic exposure owner 与需求正文/BM-R04 不一致，且 dependency_impact.yaml 仍为旧 2.42 结构；需形成新 REQAN Revision 并重建下游。",
    "title": "形成 BM-R05 对应的 Compiler 技术设计",
    "objective": "基于 BM-R05 设计统一源图、Canonical Frontend、RawDefinition/Symbol/Deferred Pipeline、ModelAccess selector、Diagnostic，以及由 Compiler 在同一次调用内协调 CompiledModelSet、EngineContext 原子发布与只读投影接口",
    "phase": "design",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-BMODEL-001"
    ],
    "owner_agent": "DesignAgent",
    "reviewer_agents": [
      "ArchitectureReviewAgent",
      "BusinessModelReviewAgent",
      "DevelopAgent",
      "RequirementReviewAgent",
      "TestDesignAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent"
    ],
    "input_revisions": {
      "requirement_analysis": "REQAN-R05@7de35e8dc15b",
      "business_model": "BM-R05@4ecb1f8c09f4"
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
      "TR-P1-COMPILER-007",
      "TR-P1-COMPILER-008",
      "TR-P1-COMPILER-009"
    ],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [],
    "expected_results": [
      "DESIGN-R05 将 BM-R05 的 CompilationSession、PublishedContext、TypedKey、DeferredDefinition 与 Diagnostic 转为 Java 8 兼容的模块、接口和数据流设计",
      "XML/YAML Frontend 只产出 CanonicalDocumentNode，不修改全局 Config；Compiler 不依赖 demo、SQL 或旧 declaration runtime",
      "ModelAccessBinding 保持 source path 与 target selector 分离，并实现 target-main 精确优先、property path 精确回退与稳定错误",
      "ModelCompiler 在同一次 compileAndPublish 调用中通过显式 ContextPublisher/expectedCurrent 原子发布不可变 EngineContext；失败不暴露部分 Registry，调用方持有的旧 Context 保持有效",
      "九条 TR、设计接缝、并发/安全/兼容测试和跨模块实现步骤均可追踪到 BM-R05 稳定 ID，七个适用 Reviewer 对同一 DESIGN-R05 独立 PASSED"
    ],
    "stop_conditions": [
      "不得重新引入 dec-expand-declaration、兼容 Adapter、静态 current Context 或第二 Registry",
      "不得让 BusinessScope 拥有 Information，或让普通 System 接受跨 System expression",
      "不得使用 root-property、模糊 selector、跨 View 搜索或静默降级",
      "不得在 P1 设计中提前实现 P3～P7 运行时语义"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "DESIGN-R05@0b37a9b4dd48",
    "validation_evidence_ids": [
      "EVD-000284"
    ]
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
    "iteration_id": "ITER-P1-COMPILER-F01-TEST-DESIGN-007",
    "iteration_no": 7,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-TEST-DESIGN-006",
    "revision_reason": "DESIGN-R05 已修复 DESIGN-R04 的四项 P1 finding，并通过七类独立 Review；旧测试设计输入已失效，TEST_DESIGN I007 必须只绑定 DESIGN-R05。",
    "title": "形成 DESIGN-R05 的可执行测试设计",
    "objective": "基于 DESIGN-R05 将 9 条 TR、23 个稳定业务错误、源图、Canonical/Raw、TypedKey、Deferred、Diagnostic、原子发布、并发、安全和退役边界转为可执行 Case 与非测试验证。",
    "phase": "test_design",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-DESIGN-001"
    ],
    "owner_agent": "TestDesignAgent",
    "reviewer_agents": [
      "DesignReviewAgent",
      "RequirementReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "input_revisions": {
      "requirement_confirmation": "REQCONF-R04@c186ce681e1e",
      "requirement_analysis": "REQAN-R05@7de35e8dc15b",
      "business_model": "BM-R05@4ecb1f8c09f4",
      "design": "DESIGN-R05@0b37a9b4dd48"
    },
    "allowed_files": [
      "version/V_1.0/doc/P1-COMPILER-F01/test_case.md",
      "version/V_1.0/doc/P1-COMPILER-F01/P1_COMPILER_analysis_test_matrix.md",
      "version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_test_seams.md",
      "version/V_1.0/task/P1-COMPILER-F01/acceptance_assertions.json",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "version/V_1.0/task/P1-COMPILER-F01/handoff.md",
      "version/V_1.0/work_record.md"
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
    "flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "flow_step_refs": [],
    "validation_commands": [
      "validate TESTDESIGN-R01 machine block and evidence command result"
    ],
    "expected_results": [
      "每条适用 TR 至少映射一个可执行 Case，Case 可反向追溯到需求、设计 seam 和 acceptance assertion",
      "覆盖正常、边界、异常、安全、并发、兼容、超时、取消、CAS conflict 和失败不发布",
      "精确验证 10 个 SourceManifest source、7 条 declaration edge 和固定 mix inventory，不从实现输出反推 expected",
      "验证 System-owned Information、common 跨 System expression、ModelAccess target-main 优先/property path 回退及所有禁止降级路径",
      "明确 TDD RED 接缝、测试数据、预期结果、禁止副作用和开发后 Evidence 采集方式",
      "DesignReviewAgent、RequirementReviewAgent、TDDReviewAgent、TestEvidenceReviewAgent 对同一 TESTDESIGN Revision 串行独立 PASSED"
    ],
    "stop_conditions": [
      "不得复用 DESIGN-R04、DESIGN-R02-DRAFT 或旧测试设计 Evidence",
      "不得在 test_design 阶段编写生产实现或提前实现 P2～P7 运行语义",
      "不得遗漏失败路径、禁止副作用、Case 反向追踪或当前 DESIGN-R05 输入绑定"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "TESTDESIGN-R01@ba7779cf089b",
    "validation_evidence_ids": [
      "EVD-000286",
      "EVD-000287"
    ]
  },
  {
    "id": "TASK-P1-R2-006",
    "logical_task_id": "LOGICAL-TASK-P1-R2-006",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-IMPLEMENTATION-PLAN-007",
    "iteration_no": 7,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-IMPLEMENTATION-PLAN-006",
    "revision_reason": "DESIGN-R05 已通过，旧实施计划输入已失效；本任务必须等待 TEST_DESIGN I007 通过后再绑定其正式 Revision。",
    "title": "形成 P1-T01～T15 实施计划",
    "objective": "基于 DESIGN-R05 和后续通过的 TESTDESIGN Revision，将 P1 编译骨架拆分为可独立验证的纵向任务，固定依赖、允许文件、TDD 接缝、验证命令、停止条件和 Reviewer。",
    "phase": "implementation_plan",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-R2-005"
    ],
    "owner_agent": "ImplementationPlanAgent",
    "reviewer_agents": [
      "ArchitectureReviewAgent",
      "DevelopAgent",
      "PlanReviewAgent",
      "TestDesignAgent"
    ],
    "input_revisions": {
      "requirement_analysis": "REQAN-R05@7de35e8dc15b",
      "business_model": "BM-R05@4ecb1f8c09f4",
      "design": "DESIGN-R05@0b37a9b4dd48",
      "test_design": "TESTDESIGN-R01@ba7779cf089b"
    },
    "allowed_files": [
      "version/V_1.0/task/P1-COMPILER-F01/development_tasks.yaml",
      "version/V_1.0/task/P1-COMPILER-F01/development_tasks.md",
      "version/V_1.0/task/P1-COMPILER-F01/development_task_reviews.jsonl",
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "version/V_1.0/task/P1-COMPILER-F01/handoff.md",
      "version/V_1.0/work_record.md"
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
    "flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "flow_step_refs": [],
    "validation_commands": [
      "python3 /mnt/data/common-develop/scripts/task_plan.py validate -g ImplementationPlanAgent --task-dir project_doc/version/V_1.0/task/P1-COMPILER-F01 --require-revision",
      "python3 /mnt/data/common-develop/scripts/task_plan.py status -g ProjectManagerAgent --task-dir project_doc/version/V_1.0/task/P1-COMPILER-F01"
    ],
    "expected_results": [
      "计划覆盖 P1-T01～T15 与 9 条 TR，依赖无环且同一时刻只允许一个任务或 Review 运行",
      "每项任务声明目标、实现方式、验收标准、允许文件、输入输出、TDD/测试命令、预期结果和停止条件",
      "实施顺序先建立 context 中立契约和 compiler 模块，再接入 XML/YAML frontend、starter、只读投影和退役门禁",
      "不得提前实现 P2～P7 runtime；dec-expand-declaration 的删除作为实现验收任务而非当前文档修复动作",
      "ArchitectureReviewAgent、DevelopAgent、PlanReviewAgent、TestDesignAgent 对同一 TP Revision 串行独立 PASSED"
    ],
    "stop_conditions": [
      "TEST_DESIGN I007 尚未通过或未生成正式 Revision",
      "任务存在占位符、循环依赖、无验证结束条件或跨越允许文件范围",
      "计划引入第二运行时、兼容 Adapter、静态 current Context 或提前实现 P2～P7"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "TP-P1-COMPILER-F01-R01@88b56e6caa64",
    "validation_evidence_ids": [
      "EVD-000288",
      "EVD-000289"
    ]
  },
  {
    "id": "TASK-P1-T01",
    "logical_task_id": "LOGICAL-TASK-P1-T01-TDD",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-TDD-007",
    "iteration_no": 7,
    "supersedes_iteration_id": "",
    "revision_reason": "基于 TP-P1-COMPILER-F01-R01 启动首个纵向增量的 TDD RED，固定 Context 中立不可变公共契约。",
    "title": "建立 Context 中立不可变编译契约的有效 RED",
    "objective": "为 dec-core-context 的中立值对象、只读 Registry、EngineContext 和 CoreConfigProjection 建立可编译、可执行、可归因的 RED 门禁。",
    "phase": "tdd",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-R2-006"
    ],
    "owner_agent": "TddAgent",
    "reviewer_agents": [
      "TDDReviewAgent"
    ],
    "input_revisions": {
      "implementation_plan": "TP-P1-COMPILER-F01-R01@88b56e6caa64",
      "test_design": "TESTDESIGN-R01@ba7779cf089b",
      "design": "DESIGN-R05@0b37a9b4dd48"
    },
    "allowed_files": [
      "dec-core-context/src/test/java/dec/core/context/tdd/ContractReflectionAssertions.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/ContextValueContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/RegistryImmutabilityTest.java",
      "dec-core-context/src/test/java/dec/core/context/tdd/EngineContextApiTest.java",
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_state.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_attempts.md",
      "version/V_1.0/task/P1-COMPILER-F01/stage_outcomes.md",
      "version/V_1.0/task/P1-COMPILER-F01/acceptance_assertions.json",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "version/V_1.0/task/P1-COMPILER-F01/handoff.md",
      "version/V_1.0/task/P1-COMPILER-F01/resume_context.md",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/**",
      "version/V_1.0/work_record.md",
      "version/V_1.0/work.md"
    ],
    "acceptance_trace_ids": [
      "TR-P1-COMPILER-003",
      "TR-P1-COMPILER-004",
      "TR-P1-COMPILER-005",
      "TR-P1-COMPILER-006"
    ],
    "flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "flow_step_refs": [],
    "validation_commands": [
      "./mvnw -pl dec-core-context -am -DskipTests test-compile",
      "./mvnw -pl dec-core-context -am -Dtest=BaseDataContractTest test",
      "./mvnw -pl dec-core-context dependency:tree -Dincludes=doc.eq.code:dec-core-compiler",
      "./mvnw -pl dec-core-context -am -Dtest=ContextValueContractTest,RegistryImmutabilityTest,EngineContextApiTest test"
    ],
    "expected_results": [
      "测试源码编译通过，现有 BaseDataContractTest 保持 GREEN",
      "dec-core-context dependency tree 不包含 dec-core-compiler",
      "三项新测试实际执行为 3 failures、0 errors、0 skipped，退出码非零且失败均由冻结公共契约尚未实现导致",
      "日志中不存在 compilation、dependency、test selection 或 environment failure",
      "TDDReviewAgent 对同一 TDD Revision 独立 PASSED"
    ],
    "stop_conditions": [
      "需要改变 DESIGN-R05 的包归属、状态语义或公共字段",
      "现有依赖迫使 context 反向依赖 compiler",
      "RED 只能由测试无法编译、依赖、选择或环境错误产生"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "TDD-P1-T01-R01@4ebeed4dad6a",
    "validation_evidence_ids": [
      "EVD-000290",
      "EVD-000291"
    ]
  }
]
```
