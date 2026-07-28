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
    "revision_reason": "REQCONF-R04 已确认 ModelAccess selector 规则；随后 common System 已落地，跨多个 System 的 Information expression 由 common 统一拥有。I004 需把该实物事实、确定性解析、Diagnostic、测试和 P2～P7 影响分析闭合。",
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
      "docs/_relations/dependency_impact.yaml"
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
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "REQAN-R04@7421b050ed44",
    "validation_evidence_ids": [
      "EVD-000252",
      "EVD-000253",
      "EVD-000254",
      "EVD-000255",
      "EVD-000256"
    ]
  },
  {
    "id": "TASK-P1-BMODEL-001",
    "logical_task_id": "LOGICAL-SUPERSEDED-TASK-P1-BMODEL-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-BUSINESS-MODEL-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-BUSINESS-MODEL-003",
    "revision_reason": "REQAN-R04 已闭合 common 跨 System expression、System-owned Information、ModelAccess target-main 优先解析、稳定 Diagnostic 与 P2～P8 Deferred 边界；I004 必须在保留历史 BM-R01/BM-R02 草案的前提下形成新的 BM-R04。",
    "title": "形成 REQAN-R04 对应的 Compiler 业务模型",
    "objective": "基于 REQAN-R04 建立 CompilationSession、RawDefinitionSet、TypedKey、ModelAccessBinding、DeferredDefinition、CompiledModelSet、EngineContext、Diagnostic 及 common 跨 System expression 的一致业务模型",
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
      "requirement_analysis": "REQAN-R04@7421b050ed44"
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
      "BM-R04 以结构化 YAML 与等价 Markdown 建立 CompilationSession 和 PublishedContext 两个聚合边界，覆盖 RawDefinition、TypedKey、Deferred、Diagnostic 与原子发布",
      "InformationKey 以 SystemKey 为所有权边界；BusinessScope 不拥有 Information；common 只允许跨 System expression Information 且不拥有 Data/View/RuleView/ModelAccess",
      "ModelAccessBinding 明确 source path 与 target selector 分离，selector 先精确匹配 target-main，未命中时才逐段精确解析 property path，歧义或缺失均阻断发布",
      "P2～P8 的 DeferredDefinition、责任阶段、已解析 Key、SourceRef、失败与恢复边界完整可追踪",
      "九条 TR 均引用 BM-R04 稳定模型 ID，且六个适用 Reviewer 对同一 Revision 独立 PASSED",
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
    "output_revision": "BM-R04@1b19a0ba26b6",
    "validation_evidence_ids": [
      "EVD-000268",
      "EVD-000269",
      "EVD-000270",
      "EVD-000271",
      "EVD-000272",
      "EVD-000273",
      "EVD-000274"
    ]
  },
  {
    "id": "TASK-P1-DESIGN-001",
    "logical_task_id": "LOGICAL-SUPERSEDED-TASK-P1-DESIGN-001",
    "feature_id": "P1-COMPILER-F01",
    "iteration_id": "ITER-P1-COMPILER-F01-DESIGN-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-DESIGN-003",
    "revision_reason": "BM-R04 已冻结 CompilationSession/PublishedContext 聚合、TypedKey、common 跨 System expression、ModelAccess target-main 优先解析、DeferredDefinition、Diagnostic 与 P2～P8 责任边界；DESIGN I004 必须基于同一模型形成可实现设计。",
    "title": "形成 BM-R04 对应的 Compiler 技术设计",
    "objective": "基于 BM-R04 设计统一源图、Canonical Frontend、RawDefinition/Symbol/Deferred Pipeline、ModelAccess selector、Diagnostic、CompiledModelSet、EngineContext 原子发布与只读投影接口",
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
      "requirement_analysis": "REQAN-R04@7421b050ed44",
      "business_model": "BM-R04@1b19a0ba26b6"
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
      "DESIGN-R04 将 BM-R04 的 CompilationSession、PublishedContext、TypedKey、DeferredDefinition 与 Diagnostic 转为模块、接口和数据流设计",
      "XML/YAML Frontend 只产出 CanonicalDocumentNode，不修改全局 Config；Compiler 不依赖 demo、SQL 或旧 declaration runtime",
      "ModelAccessBinding 保持 source path 与 target selector 分离，并实现 target-main 精确优先、property path 精确回退与稳定错误",
      "编译成功原子发布不可变 EngineContext；失败不发布部分 Registry，调用方持有的旧 Context 保持有效",
      "九条 TR、设计接缝、并发/安全/兼容测试和跨模块实现步骤均可追踪到 BM-R04 稳定 ID"
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
    "output_revision": "DESIGN-R04@1c14c8e89779",
    "validation_evidence_ids": [
      "EVD-000275",
      "EVD-000276",
      "EVD-000281",
      "EVD-000278",
      "EVD-000279",
      "EVD-000280"
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
    "iteration_id": "ITER-P1-COMPILER-F01-TEST-DESIGN-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-P1-COMPILER-F01-TEST-DESIGN-003",
    "revision_reason": "DESIGN-R04 已冻结 Compiler Pipeline、System-owned Information/common expression、ModelAccess selector、DeferredDefinition、Diagnostic、原子发布及测试接缝；TEST_DESIGN I004 必须覆盖 9 条 TR 的正常、边界、异常、安全、并发、兼容和跨模块路径。",
    "title": "形成 DESIGN-R04 的可执行测试设计",
    "objective": "基于 DESIGN-R04 将 9 条 TR、23 个稳定业务错误、编译主/失败路径、原子发布、common expression 与 ModelAccess selector 转为可执行测试 Case 和非测试验证",
    "phase": "test_design",
    "status": "REWORK",
    "depends_on": [
      "TASK-P1-DESIGN-001"
    ],
    "owner_agent": "TestDesignAgent",
    "reviewer_agents": [],
    "input_revisions": {
      "requirement_analysis": "REQAN-R04@7421b050ed44",
      "business_model": "BM-R04@1b19a0ba26b6",
      "design": "DESIGN-R04@1c14c8e89779"
    },
    "allowed_files": [],
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
      "测试设计覆盖 XML/YAML 同义输入、SourceGraph、RawDefinition、TypedKey/SymbolTable 与稳定 Diagnostic",
      "测试设计覆盖 System-owned Information、common 跨 System expression、未知/裸引用和后续 P3 循环边界",
      "测试设计覆盖 ModelAccess target-main 精确优先、property path 精确回退、未找到、重复、冲突与歧义",
      "测试设计覆盖 CompilationSession 隔离、并发、取消、超时、失败不发布和旧 EngineContext 保留",
      "9 条 TR 均映射正常、边界、异常 Case、验证命令和 Evidence 类型"
    ],
    "stop_conditions": [
      "不得把设计 Review 结论当作动态测试结果",
      "不得省略失败、恢复、并发、安全、兼容或跨模块 Case",
      "不得在测试设计阶段提前修改生产代码",
      "不得重新引入 dec-expand-declaration、root-property、模糊 selector 或第二 Registry"
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
