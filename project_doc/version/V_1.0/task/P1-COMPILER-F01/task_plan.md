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
      "EVD-000289",
      "EVD-000292"
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
      "version/V_1.0/work.md",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/evidence_index.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/reviews.jsonl"
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
      "verify TDD-P1-T01-R01 executable RED and supporting baseline/dependency gates"
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
  },
  {
    "id": "TASK-P1-T01-DEV-SKELETON",
    "logical_task_id": "LOGICAL-TASK-P1-T01-DEVELOPMENT",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-DEVELOPMENT-008",
    "iteration_no": 8,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-DEVELOPMENT-007",
    "revision_reason": "wk -ar 骨架 DEVSKEL-P1-T01-R01 已由 ArchitectureReviewAgent 与 SpecComplianceReviewAgent 通过；当前进入具体实现 iteration。",
    "title": "实现 T01 Context 中立不可变公共契约并转 GREEN",
    "objective": "在已通过 DEVSKEL-P1-T01-R01 骨架上实现 31 个 Java 8 不可变公共类型，修复 TDD RED，保持 context→compiler 零反向依赖并补充语义回归。",
    "phase": "development",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-T01"
    ],
    "owner_agent": "DevelopAgent",
    "reviewer_agents": [
      "TDDReviewAgent"
    ],
    "input_revisions": {
      "tdd": "TDD-P1-T01-R01@4ebeed4dad6a",
      "design": "DESIGN-R05@0b37a9b4dd48",
      "implementation_plan": "TP-P1-COMPILER-F01-R01@88b56e6caa64"
    },
    "allowed_files": [
      "dec-core-context/src/main/java/dec/core/context/CoreConfigProjection.java",
      "dec-core-context/src/main/java/dec/core/context/EngineContext.java",
      "dec-core-context/src/main/java/dec/core/context/model/AbstractDefinitionKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/ActionKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/BusinessScopeKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/CompiledDefinition.java",
      "dec-core-context/src/main/java/dec/core/context/model/CompiledModelSet.java",
      "dec-core-context/src/main/java/dec/core/context/model/ConnectionKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/DataKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/DataSourceKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/DeferredDefinition.java",
      "dec-core-context/src/main/java/dec/core/context/model/DeferredKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/DeferredKind.java",
      "dec-core-context/src/main/java/dec/core/context/model/DeferredRegistry.java",
      "dec-core-context/src/main/java/dec/core/context/model/DefinitionKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/Diagnostic.java",
      "dec-core-context/src/main/java/dec/core/context/model/DiagnosticCode.java",
      "dec-core-context/src/main/java/dec/core/context/model/DiagnosticSeverity.java",
      "dec-core-context/src/main/java/dec/core/context/model/DigestPair.java",
      "dec-core-context/src/main/java/dec/core/context/model/DirectoryKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/ImmutableDeferredRegistry.java",
      "dec-core-context/src/main/java/dec/core/context/model/ImmutableRegistry.java",
      "dec-core-context/src/main/java/dec/core/context/model/InformationKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/NormalizedBody.java",
      "dec-core-context/src/main/java/dec/core/context/model/ProduceKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/Registry.java",
      "dec-core-context/src/main/java/dec/core/context/model/RequiredStage.java",
      "dec-core-context/src/main/java/dec/core/context/model/RuleViewKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/SourceRef.java",
      "dec-core-context/src/main/java/dec/core/context/model/SystemKey.java",
      "dec-core-context/src/main/java/dec/core/context/model/ViewKey.java",
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_state.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_attempts.md",
      "version/V_1.0/task/P1-COMPILER-F01/stage_outcomes.md",
      "version/V_1.0/task/P1-COMPILER-F01/acceptance_assertions.json",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "version/V_1.0/task/P1-COMPILER-F01/handoff.md",
      "version/V_1.0/task/P1-COMPILER-F01/resume_context.md",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/evidence_index.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/reviews.jsonl",
      "version/V_1.0/work_record.md",
      "version/V_1.0/work.md",
      "dec-core-context/src/test/java/dec/core/context/tdd/ContextContractBehaviorTest.java"
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
      "verify TASK-P1-T01 concrete implementation GREEN, module regression and dependency direction"
    ],
    "expected_results": [
      "三个冻结 TDD 合同测试全部 GREEN",
      "BaseDataContractTest 与新增语义测试全部 GREEN",
      "dec-core-context verify 成功并使用 Java 8 release",
      "dec-core-context 不依赖 dec-core-compiler",
      "所有集合防御性复制且无 public mutator/static current",
      "DiagnosticCode 与 DeferredDefinition/InformationKey 对齐 DESIGN-R05"
    ],
    "stop_conditions": [
      "需要改变已通过骨架的包归属或公共调用边界",
      "实现引入 context 到 compiler 反向依赖",
      "通过删除/弱化 TDD 断言或跳过测试获得 GREEN",
      "提前实现 T02-T15 runtime 行为"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "DEV-P1-T01-R01@de1adfd37c9b",
    "validation_evidence_ids": [
      "EVD-000295",
      "EVD-000296"
    ]
  },
  {
    "id": "TASK-P1-T01-CODE-REVIEW",
    "logical_task_id": "LOGICAL-TASK-P1-T01-CODE-REVIEW",
    "iteration_id": "ITER-P1-COMPILER-F01-CODE-REVIEW-008",
    "iteration_no": 8,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-CODE-REVIEW-007",
    "revision_reason": "Stage Closure 独立返修复核已确认 FND-P1-STAGE-003 与 FND-P1-STAGE-004 在 75559ecc2e4791eddee166cf3010128130e27078 上关闭，且代码级 P0/P1 为 0；但 canonical code_review/testing/completion 仍绑定旧 T01 revision（FND-P1-STAGE-002），因此从 code_review 合法重开并重新绑定本次 Stage Closure Review。",
    "title": "审查 T01 Context 中立不可变公共契约",
    "objective": "独立验证 DEV-P1-T01-R01 对 DESIGN-R05/TDD-R01 的符合性、工程质量、架构边界和测试可靠性。",
    "phase": "code_review",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-T01-DEV-SKELETON"
    ],
    "owner_agent": "ProjectManagerAgent",
    "reviewer_agents": [
      "ArchitectureReviewAgent",
      "CrossModuleIntegrationReviewAgent",
      "EngineeringStandardsReviewAgent",
      "ImpactAnalysisReviewAgent",
      "PerformanceReviewAgent",
      "SecurityReviewAgent",
      "SpecComplianceReviewAgent"
    ],
    "input_revisions": {
      "development": "DEV-P1-T01-R01@de1adfd37c9b",
      "design": "DESIGN-R05@0b37a9b4dd48",
      "tdd": "TDD-P1-T01-R01@4ebeed4dad6a"
    },
    "allowed_files": [
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_state.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_attempts.md",
      "version/V_1.0/task/P1-COMPILER-F01/stage_outcomes.md",
      "version/V_1.0/task/P1-COMPILER-F01/acceptance_assertions.json",
      "version/V_1.0/task/P1-COMPILER-F01/review_issues.md",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "version/V_1.0/task/P1-COMPILER-F01/handoff.md",
      "version/V_1.0/task/P1-COMPILER-F01/resume_context.md",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/evidence_index.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/reviews.jsonl",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/code-review-p1-t01-r01/**",
      "version/V_1.0/work_record.md",
      "version/V_1.0/work.md",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/code-review-p1-t01-r01/review-report.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/code-review-p1-t01-r01/command-result.json"
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
      "verify DEV-P1-T01-R01 final code review scope, standards, architecture and detected risk classifications"
    ],
    "expected_results": [
      "Spec Review confirms no missing requirement or scope drift",
      "Engineering Review confirms immutable implementation and tests are maintainable",
      "Architecture Review confirms context remains compiler-neutral and failure-isolated",
      "No open P0/P1 findings"
    ],
    "stop_conditions": [
      "Any reviewer returns NEEDS_CHANGES or BLOCKED",
      "Open P0/P1 finding exists",
      "Review requires changing frozen DESIGN-R05 contract"
    ],
    "risk_triggers": [
      "architecture_change",
      "cross_module_integration",
      "impact_analysis",
      "performance",
      "security"
    ],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "CODEREVIEW-P1-STAGE-CLOSURE-R01@75559ecc2e47",
    "validation_evidence_ids": [
      "EVD-000363",
      "EVD-000364",
      "EVD-000366",
      "EVD-000367",
      "EVD-000368"
    ],
    "feature_id": "P1-COMPILER-F01"
  },
  {
    "id": "TASK-P1-T01-TESTING",
    "logical_task_id": "LOGICAL-TASK-P1-T01-TESTING",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-TESTING-009",
    "iteration_no": 9,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-TESTING-008",
    "revision_reason": "Code Review I008 已完成并绑定 Stage Closure revision；Testing I008 仍携带返修前 Code Review 输入 revision。按状态机重新打开 Testing，使当前测试任务绑定最新 code_review revision，并让 Completion 在后续推进时再绑定新 testing revision。",
    "title": "验证 T01 Context 不可变契约与主干回归",
    "objective": "在真实 PR 源码树上验证 T01 合同、行为、模块回归、依赖方向、标准 P0 和故意失败阻断门禁。",
    "phase": "testing",
    "status": "PASSED",
    "depends_on": [
      "TASK-P1-T01-CODE-REVIEW"
    ],
    "owner_agent": "TestAgent",
    "reviewer_agents": [
      "TestEvidenceReviewAgent"
    ],
    "input_revisions": {
      "code_review": "CODEREVIEW-P1-STAGE-CLOSURE-R01@75559ecc2e47"
    },
    "allowed_files": [
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_state.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_attempts.md",
      "version/V_1.0/task/P1-COMPILER-F01/stage_outcomes.md",
      "version/V_1.0/task/P1-COMPILER-F01/acceptance_assertions.json",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "version/V_1.0/task/P1-COMPILER-F01/handoff.md",
      "version/V_1.0/task/P1-COMPILER-F01/resume_context.md",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/evidence_index.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/reviews.jsonl",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/testing-p1-t01-r01/**",
      "version/V_1.0/work_record.md",
      "version/V_1.0/work.md",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/testing-p1-t01-r01/test-report-manifest.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/testing-p1-t01-r01/p0-run.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/testing-p1-t01-r01/command-result.json"
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
      "verify TASK-P1-T01 R02 GREEN, Context module verify, dependency direction, standard P0 core build/tests, and intentional failure blocking"
    ],
    "expected_results": [
      "R02 9 tests 全绿",
      "标准 P0 normal suites 全绿",
      "故意失败测试被构建门禁正确阻断",
      "dec-core-context 不依赖 dec-core-compiler",
      "MySQL 对 T01 明确为不适用而非伪造通过"
    ],
    "stop_conditions": [
      "任何普通测试 failure/error",
      "故意失败门禁未能阻断构建",
      "依赖方向出现 context→compiler",
      "Evidence 无法绑定真实 run/head/artifact"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "TESTING-P1-STAGE-CLOSURE-R01@75559ecc2e47",
    "validation_evidence_ids": [
      "EVD-000371",
      "EVD-000372",
      "EVD-000373"
    ]
  },
  {
    "id": "TASK-P1-T01-COMPLETION",
    "logical_task_id": "LOGICAL-TASK-P1-T01-COMPLETION",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-COMPLETION-VERIFICATION-009",
    "iteration_no": 9,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-COMPLETION-VERIFICATION-008",
    "revision_reason": "Code Review I008 已完成并绑定 Stage Closure revision；Testing I008 仍携带返修前 Code Review 输入 revision。按状态机重新打开 Testing，使当前测试任务绑定最新 code_review revision，并让 Completion 在后续推进时再绑定新 testing revision。",
    "title": "验证 TASK-P1-T01 全流程完成与可合并性",
    "objective": "确认 TDD、架构骨架、具体实现、Code Review、Testing、Evidence、Git checkpoint 与最终干净 PR 全部闭环。",
    "phase": "completion_verification",
    "status": "REWORK",
    "depends_on": [
      "TASK-P1-T01-TESTING"
    ],
    "owner_agent": "CompletionVerificationAgent",
    "reviewer_agents": [],
    "input_revisions": {
      "testing": "TESTING-P1-STAGE-CLOSURE-R01@75559ecc2e47"
    },
    "allowed_files": [
      "version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_state.md",
      "version/V_1.0/task/P1-COMPILER-F01/task_attempts.md",
      "version/V_1.0/task/P1-COMPILER-F01/stage_outcomes.md",
      "version/V_1.0/task/P1-COMPILER-F01/acceptance_assertions.json",
      "version/V_1.0/task/P1-COMPILER-F01/traceability.md",
      "version/V_1.0/task/P1-COMPILER-F01/handoff.md",
      "version/V_1.0/task/P1-COMPILER-F01/resume_context.md",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/evidence_index.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/reviews.jsonl",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t01-r01/**",
      "version/V_1.0/work_record.md",
      "version/V_1.0/work.md",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t01-r01/completion-report.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t01-r01/p0-run.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t01-r01/clean-tree-manifest.json",
      "version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t01-r01/command-result.json"
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
      "verify TASK-P1-T01 lifecycle closure, final clean PR tree and final P0 Build Gate"
    ],
    "expected_results": [
      "所有阶段 Revision 与 Evidence ACTIVE 且可回查",
      "无开放 P0/P1 finding",
      "最终 PR 不包含临时 workflow/payload/chunk/trigger",
      "最终干净 Head 标准 P0 全绿",
      "T01 状态 PASSED 并交接到 TASK-P1-T02 TDD"
    ],
    "stop_conditions": [
      "任何阶段未 PASSED",
      "Evidence 摘要或 Git 引用不可解析",
      "最终 PR 含临时文件",
      "最终 P0 非成功",
      "存在开放 P0/P1"
    ],
    "risk_triggers": [],
    "attempts": 0,
    "max_attempts": 3,
    "output_revision": "",
    "validation_evidence_ids": []
  }
]
```
