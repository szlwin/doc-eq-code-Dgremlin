# FEATURE-DESC-3361AD2E54FC 实施任务计划

```json task-plan
[
  {
    "id": "TASK-P2-REQCONF-001",
    "logical_task_id": "LOGICAL-P2-SYSTEM-RULEVIEW-REQUIREMENT-CONFIRMATION",
    "feature_id": "P2-SYSTEM-RULEVIEW-F01",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-CONFIRMATION-002",
    "iteration_no": 2,
    "supersedes_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-CONFIRMATION-001",
    "revision_reason": "RC9 Git checkpoint 对新 Markdown 尾随空格执行 diff --check；当前模板验收占位符含 Markdown 硬换行。保持 R01 证据历史，创建新 iteration 仅规范化格式，不改变 P2 语义。",
    "title": "确认 P2 System 与 RuleView 归属需求边界",
    "objective": "锁定 System 一等编译实体、RuleView (system,name) 归属以及 model-access 静态/运行时权限屏障的目标、范围、失败语义和完成标准。",
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
      "version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md",
      "version/V_1.0/requirement_list.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_state.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_attempts.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/stage_outcomes.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/acceptance_assertions.json",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/review_issues.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/decision_log.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/**",
      "version/V_1.0/work_record.md"
    ],
    "acceptance_trace_ids": [],
    "flow_refs": [],
    "flow_step_refs": [],
    "validation_commands": [
      "python3 /mnt/data/common-develop/scripts/requirement_doc.py validate -g RequirementConfirmationAgent --file project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md --stage confirmation --json",
      "python3 /mnt/data/common-develop/scripts/long_task.py validate -g RequirementConfirmationAgent --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC"
    ],
    "expected_results": [
      "明确 P2 当前行为、目标行为、业务原因、范围内外、依赖、失败模式和恢复边界",
      "System 必须成为一等编译实体，不允许仅作为文档字段或按包名推断",
      "RuleView 必须使用 (system,name) 复合身份注册、解析和调用，禁止裸名称全局查找",
      "model-access 必须形成默认最小权限、写入默认拒绝的静态与运行时权限屏障",
      "systems.xml、同名 RuleView 隔离、合法/非法 model-access 均具有可观察完成标准",
      "declaration System 只定义迁移边界，P2 不删除旧入口",
      "RequirementAnalysisAgent 与 TestDesignAgent 对同一需求确认 revision 独立 Review 均通过"
    ],
    "stop_conditions": [
      "出现需要用户选择且会改变范围、权限默认值或兼容策略的未决决策",
      "需求扩大到 P3 Information 求值、P4 Action/Produce、P5 Directory、P6 QueryPlan 或 P7 runtime 收敛",
      "方案默认允许共享模型写入、按包名推断 System、按裸名称解析 RuleView 或在 P2 删除 declaration 入口"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "REQCONF-P2-R02@ef30059b327d",
    "validation_evidence_ids": [
      "EVD-000009"
    ]
  },
  {
    "id": "TASK-P2-REQAN-001",
    "logical_task_id": "LOGICAL-P2-SYSTEM-RULEVIEW-REQUIREMENT-ANALYSIS",
    "feature_id": "P2-SYSTEM-RULEVIEW-F01",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-ANALYSIS-002",
    "iteration_no": 2,
    "supersedes_iteration_id": "",
    "revision_reason": "REQCONF-P2-R02 已通过；在当前 downstream I002 首次完成 P2 结构化需求分析。",
    "title": "分析 P2 System、RuleView 与 model-access 业务语义",
    "objective": "将已确认 P2 边界收敛为稳定功能、规则、异常、验收、流程与追踪关系，为业务模型、设计和测试设计提供同一输入 Revision。",
    "phase": "requirement_analysis",
    "status": "PASSED",
    "depends_on": [
      "TASK-P2-REQCONF-001"
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
      "requirement_confirmation": "REQCONF-P2-R02@ef30059b327d"
    },
    "allowed_files": [
      "version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md",
      "version/V_1.0/requirement_list.md",
      "version/V_1.0/doc/_flows/COMPILER/**",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_state.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_attempts.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/stage_outcomes.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/acceptance_assertions.json",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/review_issues.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/decision_log.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/**",
      "version/V_1.0/work_record.md",
      "version/V_1.0/doc/_flows/COMPILER/changes/002-p2-system-ruleview-access.yaml",
      "project_doc/version/V_1.0/doc/_flows/COMPILER/changes/002-p2-system-ruleview-access.yaml"
    ],
    "acceptance_trace_ids": [
      "TR-P2-SYSTEM-RULEVIEW-001",
      "TR-P2-SYSTEM-RULEVIEW-002",
      "TR-P2-SYSTEM-RULEVIEW-003",
      "TR-P2-SYSTEM-RULEVIEW-004",
      "TR-P2-SYSTEM-RULEVIEW-005",
      "TR-P2-SYSTEM-RULEVIEW-006",
      "TR-P2-SYSTEM-RULEVIEW-007",
      "TR-P2-SYSTEM-RULEVIEW-008",
      "TR-P2-SYSTEM-RULEVIEW-009",
      "TR-P2-SYSTEM-RULEVIEW-010"
    ],
    "flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "flow_step_refs": [
      "STEP-CONFIG-COMPILE-01",
      "STEP-CONFIG-COMPILE-03",
      "STEP-CONFIG-COMPILE-04",
      "STEP-CONFIG-COMPILE-05",
      "STEP-CONFIG-COMPILE-06",
      "STEP-CONFIG-COMPILE-07"
    ],
    "validation_commands": [
      "python3 /mnt/data/common-develop/scripts/requirement_doc.py validate -g RequirementAnalysisAgent --file project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md --stage analysis --json",
      "python3 /mnt/data/common-develop/scripts/requirement_doc.py finalize-requirement-analysis -g RequirementAnalysisAgent --file project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC --json",
      "python3 /mnt/data/common-develop/scripts/long_task.py validate -g RequirementAnalysisAgent --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC"
    ],
    "expected_results": [
      "第 5～13 节不再含占位符，P2 功能、规则、异常、状态、权限、幂等、并发与部分失败语义完整",
      "System 显式一等身份、RuleView (system,name) 与 model-access 最小权限/WRITE 默认拒绝形成可测试业务规则",
      "静态非法引用/路径/权限阻断发布，动态合法路径必须运行时 Guard 且任何变更入口不得旁路",
      "同名 RuleView 隔离、系统文件多源顺序无关、编译原子性与旧 Context 保留均有稳定验收标准",
      "FLOW-CONFIG-COMPILE 被复用为 P2 业务流程关联，不新建并行事实源",
      "所有功能、BR/CR、AC 均进入 traceability，适用项标记 impact、business flow 与 cross-module 影响",
      "BusinessModelAgent、DesignAgent、TestDesignAgent、ImpactAnalysisReviewAgent、CrossModuleIntegrationReviewAgent 对同一 Revision 独立 Review 通过"
    ],
    "stop_conditions": [
      "发现必须改变已冻结 System / RuleView / model-access 核心语义的选择",
      "需求扩大到 P3～P8 的完整运行语义或提前删除 declaration runtime",
      "需要默认允许未声明 WRITE、裸 RuleView 名称回退或隐式 System 推断"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "REQAN-P2-R01@d08612768131",
    "validation_evidence_ids": [
      "EVD-000015",
      "EVD-000016"
    ]
  },
  {
    "id": "TASK-P2-BMODEL-001",
    "logical_task_id": "LOGICAL-P2-SYSTEM-RULEVIEW-BUSINESS-MODEL",
    "feature_id": "P2-SYSTEM-RULEVIEW-F01",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-BUSINESS-MODEL-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-BUSINESS-MODEL-003",
    "revision_reason": "RC9 machine-state migration: bind already-completed semantic authority chain BM-R20 -> DESIGN-P2-R30 -> TESTDESIGN-P2-R31 into new lifecycle iterations without rewriting historical PASSED revisions or redoing semantic content.",
    "title": "建立 P2 System、RuleView 与 model-access 业务模型",
    "objective": "RC9 machine-state migration: re-register the already-completed BM-R20 semantic snapshot as the current business_model artifact without changing BM-R20 content.",
    "phase": "business_model",
    "status": "PASSED",
    "depends_on": [
      "TASK-P2-REQAN-001"
    ],
    "owner_agent": "BusinessModelAgent",
    "reviewer_agents": [
      "BusinessModelReviewAgent",
      "RequirementReviewAgent",
      "DesignReviewAgent",
      "TestDesignAgent",
      "ImpactAnalysisReviewAgent",
      "CrossModuleIntegrationReviewAgent"
    ],
    "input_revisions": {
      "requirement_analysis": "REQAN-P2-R01@d08612768131"
    },
    "allowed_files": [
      "version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml",
      "version/V_1.0/doc/COMPILER/COMPILER_business_model.md",
      "version/V_1.0/doc/COMPILER/changes/p2-system-ruleview-business-model.yaml",
      "project_doc/version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml",
      "project_doc/version/V_1.0/doc/COMPILER/COMPILER_business_model.md",
      "project_doc/version/V_1.0/doc/COMPILER/changes/p2-system-ruleview-business-model.yaml",
      "docs/_relations/dependency_impact.yaml",
      "docs/_relations/dependency_graph.md",
      "project_doc/docs/_relations/dependency_impact.yaml",
      "project_doc/docs/_relations/dependency_graph.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md",
      "project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md",
      "project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md",
      "version/V_1.0/doc/COMPILER/changes/p2-business-model-lineage-readability.yaml",
      "version/V_1.0/requirement_list.md",
      "project_doc/version/V_1.0/doc/COMPILER/changes/p2-business-model-lineage-readability.yaml"
    ],
    "acceptance_trace_ids": [
      "TR-P2-SYSTEM-RULEVIEW-001",
      "TR-P2-SYSTEM-RULEVIEW-002",
      "TR-P2-SYSTEM-RULEVIEW-003",
      "TR-P2-SYSTEM-RULEVIEW-004",
      "TR-P2-SYSTEM-RULEVIEW-005",
      "TR-P2-SYSTEM-RULEVIEW-006",
      "TR-P2-SYSTEM-RULEVIEW-007",
      "TR-P2-SYSTEM-RULEVIEW-008",
      "TR-P2-SYSTEM-RULEVIEW-009",
      "TR-P2-SYSTEM-RULEVIEW-010"
    ],
    "flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "flow_step_refs": [
      "STEP-CONFIG-COMPILE-01",
      "STEP-CONFIG-COMPILE-03",
      "STEP-CONFIG-COMPILE-04",
      "STEP-CONFIG-COMPILE-05",
      "STEP-CONFIG-COMPILE-06",
      "STEP-CONFIG-COMPILE-07"
    ],
    "validation_commands": [
      "python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g BusinessModelAgent --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC",
      "python3 -c \"import yaml,json,jsonschema; d=yaml.safe_load(open('project_doc/version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml',encoding='utf-8')); assert d.get('revision')=='BM-R20'; jsonschema.validate(d,json.load(open('/home/oai/skills/common-develop/assets/structured-docs/business-model.schema.json',encoding='utf-8')))\"",
      "python3 -c \"import json; d=json.load(open('project_doc/version/V_1.0/doc/_flows/COMPILER/changes/003-p2-system-ruleview-protected-access.yaml',encoding='utf-8')); assert d['changeSet']['resultRevision'].startswith('FLOW-R11')\"",
      "python3 -c \"import json,yaml,jsonschema; d=yaml.safe_load(open('project_doc/docs/_relations/dependency_impact.yaml',encoding='utf-8')); assert d.get('revision')=='P2-IMPACT-R29'; jsonschema.validate(d,json.load(open('/home/oai/skills/common-develop/assets/structured-docs/dependency-impact.schema.json',encoding='utf-8')))\"",
      "python3 /home/oai/skills/common-develop/scripts/render_relationships.py -g BusinessModelAgent --input project_doc/docs/_relations/dependency_impact.yaml --check",
      "git diff --check"
    ],
    "expected_results": [
      "BM-R20 remains the complete current P2 business-model snapshot; no semantic rewrite is introduced by migration.",
      "REQAN-P2-R01 + Overlay R04 -> BM-R20 and FLOW-R11/P2-IMPACT-R29 cross-document authority remains internally consistent.",
      "Historical BM-R07 PASSED iteration remains archived; current iteration binds BM-R20 with current-revision Evidence and independent Reviews."
    ],
    "stop_conditions": [
      "BM-R05 stable IDs 不得丢失、重命名或静默覆盖",
      "DEC_COMPILER 与 COMPILER 必须明确为同一逻辑模块文档谱系，不得形成第二 runtime authority",
      "BM-R06 已确认的 P2 System/RuleView/model-access 语义不得在可读性修订中发生未声明变化",
      "六项独立 Review 任一不是 PASSED 时停止，不得进入 Design"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "BM-R20",
    "validation_evidence_ids": [
      "EVD-000122",
      "EVD-000123",
      "EVD-000124",
      "EVD-000125",
      "EVD-000126",
      "EVD-000127",
      "EVD-000128"
    ]
  },
  {
    "id": "TASK-P2-DESIGN-001",
    "logical_task_id": "LOGICAL-P2-SYSTEM-RULEVIEW-DESIGN",
    "feature_id": "P2-SYSTEM-RULEVIEW-F01",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-DESIGN-004",
    "iteration_no": 4,
    "supersedes_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-DESIGN-003",
    "revision_reason": "RC9 machine-state migration: bind already-completed semantic authority chain BM-R20 -> DESIGN-P2-R30 -> TESTDESIGN-P2-R31 into new lifecycle iterations without rewriting historical PASSED revisions or redoing semantic content.",
    "title": "设计 P2 System、RuleView 与 model-access 编译/运行边界",
    "objective": "RC9 machine-state migration: re-register the already-completed DESIGN-P2-R30 artifact and P2-IMPACT-R29 relation without changing design semantics.",
    "phase": "design",
    "status": "PASSED",
    "depends_on": [
      "TASK-P2-BMODEL-001"
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
      "business_model": "BM-R20"
    },
    "allowed_files": [
      "version/V_1.0/doc/COMPILER/COMPILER_design.md",
      "version/V_1.0/doc/COMPILER/COMPILER_api_contract.md",
      "version/V_1.0/doc/COMPILER/COMPILER_architecture.md",
      "version/V_1.0/doc/COMPILER/COMPILER_test_seams.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_state.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_attempts.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/stage_outcomes.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/acceptance_assertions.json",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/review_issues.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/decision_log.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/**",
      "version/V_1.0/work_record.md",
      "project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md",
      "project_doc/version/V_1.0/doc/COMPILER/COMPILER_api_contract.md",
      "project_doc/version/V_1.0/doc/COMPILER/COMPILER_architecture.md",
      "project_doc/version/V_1.0/doc/COMPILER/COMPILER_test_seams.md"
    ],
    "acceptance_trace_ids": [
      "TR-P2-SYSTEM-RULEVIEW-001",
      "TR-P2-SYSTEM-RULEVIEW-002",
      "TR-P2-SYSTEM-RULEVIEW-003",
      "TR-P2-SYSTEM-RULEVIEW-004",
      "TR-P2-SYSTEM-RULEVIEW-005",
      "TR-P2-SYSTEM-RULEVIEW-006",
      "TR-P2-SYSTEM-RULEVIEW-007",
      "TR-P2-SYSTEM-RULEVIEW-008",
      "TR-P2-SYSTEM-RULEVIEW-009",
      "TR-P2-SYSTEM-RULEVIEW-010"
    ],
    "flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "flow_step_refs": [
      "STEP-CONFIG-COMPILE-01",
      "STEP-CONFIG-COMPILE-03",
      "STEP-CONFIG-COMPILE-04",
      "STEP-CONFIG-COMPILE-05",
      "STEP-CONFIG-COMPILE-06",
      "STEP-CONFIG-COMPILE-07"
    ],
    "validation_commands": [
      "python3 -c \"from pathlib import Path; s=Path('project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md').read_text(); assert 'DESIGN-P2-R30' in s and 'BM-R20' in s and 'FLOW-R11' in s and 'P2-IMPACT-R29' in s\"",
      "python3 -c \"from pathlib import Path; s=Path('project_doc/version/V_1.0/doc/COMPILER/COMPILER_api_contract.md').read_text(); assert len(s)>100\"",
      "python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g DesignAgent --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC",
      "git diff --check"
    ],
    "expected_results": [
      "DESIGN-P2-R30 remains current and consumes BM-R20/FLOW-R11/P2-IMPACT-R29.",
      "Current trusted ModelData/Container/Guard/effect boundaries remain unchanged by migration.",
      "Historical DESIGN-P2-R01 PASSED iteration remains archived; current iteration binds DESIGN-P2-R30 with current-revision Evidence and independent Reviews."
    ],
    "stop_conditions": [
      "设计要求裸 RuleView 名称全局 fallback 或隐式 System 推断",
      "设计默认允许未声明 WRITE/EXECUTE 或允许 Guard 后置于副作用",
      "设计要求在 P2 删除 declaration runtime 或实现 P3～P8 语义",
      "出现必须改变 BM-R07 已冻结业务语义的 P0/P1 决策"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "DESIGN-P2-R30",
    "validation_evidence_ids": [
      "EVD-000133",
      "EVD-000134",
      "EVD-000135",
      "EVD-000136",
      "EVD-000137",
      "EVD-000138",
      "EVD-000139",
      "EVD-000140",
      "EVD-000141"
    ]
  },
  {
    "id": "TASK-P2-TESTDESIGN-001",
    "logical_task_id": "LOGICAL-P2-SYSTEM-RULEVIEW-TEST-DESIGN",
    "feature_id": "P2-SYSTEM-RULEVIEW-F01",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-TEST-DESIGN-006",
    "iteration_no": 6,
    "supersedes_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-TEST-DESIGN-005",
    "revision_reason": "Clarify existing nested ModelPath semantics with explicit TestDesign oracles; P1 implementation, BM-R20 and DESIGN-P2-R30 remain unchanged.",
    "title": "设计 P2 System、RuleView 与 model-access 可执行测试矩阵",
    "objective": "RC9 machine-state migration: re-register the already-completed TESTDESIGN-P2-R31 95-case/23-class test design without changing its test semantics.",
    "phase": "test_design",
    "status": "PASSED",
    "depends_on": [
      "TASK-P2-DESIGN-001"
    ],
    "owner_agent": "TestDesignAgent",
    "reviewer_agents": [
      "RequirementReviewAgent",
      "DesignReviewAgent",
      "TDDReviewAgent",
      "TestEvidenceReviewAgent"
    ],
    "input_revisions": {
      "design": "DESIGN-P2-R30"
    },
    "allowed_files": [
      "version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md",
      "project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md",
      "project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md",
      "project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_state.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_attempts.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/stage_outcomes.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/acceptance_assertions.json",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/review_issues.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/decision_log.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/**",
      "version/V_1.0/work_record.md"
    ],
    "acceptance_trace_ids": [
      "TR-P2-SYSTEM-RULEVIEW-001",
      "TR-P2-SYSTEM-RULEVIEW-002",
      "TR-P2-SYSTEM-RULEVIEW-003",
      "TR-P2-SYSTEM-RULEVIEW-004",
      "TR-P2-SYSTEM-RULEVIEW-005",
      "TR-P2-SYSTEM-RULEVIEW-006",
      "TR-P2-SYSTEM-RULEVIEW-007",
      "TR-P2-SYSTEM-RULEVIEW-008",
      "TR-P2-SYSTEM-RULEVIEW-009",
      "TR-P2-SYSTEM-RULEVIEW-010"
    ],
    "flow_refs": [
      "FLOW-CONFIG-COMPILE"
    ],
    "flow_step_refs": [
      "STEP-CONFIG-COMPILE-01",
      "STEP-CONFIG-COMPILE-03",
      "STEP-CONFIG-COMPILE-04",
      "STEP-CONFIG-COMPILE-05",
      "STEP-CONFIG-COMPILE-06",
      "STEP-CONFIG-COMPILE-07"
    ],
    "validation_commands": [
      "python3 -c \"from pathlib import Path; import re; s=Path('project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md').read_text(); req=['CASE-P2-TD-NESTED-OBJECT-PATH-001','CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001','CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001','CASE-P2-TD-NESTED-COLLECTION-PATH-001','CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001','CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001']; assert 'TESTDESIGN-P2-R32' in s; assert len(set(re.findall(r'CASE-P2-TD-[A-Z0-9-]+-001',s)))==101; assert len(set(re.findall(r'\\| ([A-Za-z0-9_]+Test) \\|',s)))==23; assert all(x in s for x in req)\"",
      "python3 -c \"import json,re; from pathlib import Path; s=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md').read_text(); marker=chr(96)*3+'json traceability'+chr(10); i=s.index(marker)+len(marker); j=s.index(chr(10)+chr(96)*3,i); a=json.loads(s[i:j]); by={x['id']:x for x in a}; req=['CASE-P2-TD-NESTED-OBJECT-PATH-001','CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001','CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001','CASE-P2-TD-NESTED-COLLECTION-PATH-001','CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001','CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001']; assert len(a)==10; assert all(x in by['TR-P2-SYSTEM-RULEVIEW-005']['test_case_ids'] for x in req); assert req[-1] in by['TR-P2-SYSTEM-RULEVIEW-004']['test_case_ids']\"",
      "python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g TestDesignAgent --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC",
      "git diff --check"
    ],
    "expected_results": [
      "TESTDESIGN-P2-R31 remains current with 95 blocking cases mapped to 23 exact TestClasses.",
      "All ten stable trace IDs are COVERED and map to current R31 cases.",
      "Historical TESTDESIGN-P2-R02 PASSED iteration remains archived; current iteration binds TESTDESIGN-P2-R31 with current-revision Evidence and independent Reviews."
    ],
    "stop_conditions": [
      "Design Revision 变化或被重开",
      "测试需要接受裸名称 fallback、默认 WRITE allow 或 Guard fail-open",
      "测试设计扩大到 P3～P7 完整运行语义",
      "任一 required Reviewer 非 PASSED 或形成开放 P0/P1"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "TESTDESIGN-P2-R32",
    "validation_evidence_ids": [
      "EVD-000193",
      "EVD-000194",
      "EVD-000195",
      "EVD-000196",
      "EVD-000197",
      "EVD-000198",
      "EVD-000199"
    ]
  },
  {
    "id": "TASK-P2-IMPLEMENTATION-PLAN-001",
    "logical_task_id": "LOGICAL-P2-SYSTEM-RULEVIEW-IMPLEMENTATION-PLAN",
    "feature_id": "P2-SYSTEM-RULEVIEW-F01",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-IMPLEMENTATION-PLAN-008",
    "iteration_no": 8,
    "supersedes_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-IMPLEMENTATION-PLAN-007",
    "revision_reason": "Clarify existing nested ModelPath semantics with explicit TestDesign oracles; P1 implementation, BM-R20 and DESIGN-P2-R30 remain unchanged.",
    "title": "形成 P2 System、RuleView 与 model-access 可执行 Implementation Plan",
    "objective": "Rebind the unchanged nine-slice Implementation Plan to TESTDESIGN-P2-R32 and explicitly bind the six nested ModelPath/exact-authorization oracles to DEV-03 without changing architecture or task DAG.",
    "phase": "implementation_plan",
    "status": "PASSED",
    "depends_on": [
      "TASK-P2-TESTDESIGN-001"
    ],
    "owner_agent": "ImplementationPlanAgent",
    "reviewer_agents": [
      "PlanReviewAgent",
      "ArchitectureReviewAgent",
      "TestDesignAgent",
      "DevelopAgent"
    ],
    "input_revisions": {
      "design": "DESIGN-P2-R30",
      "test_design": "TESTDESIGN-P2-R32"
    },
    "allowed_files": [
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_task_reviews.jsonl",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_state.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_attempts.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/stage_outcomes.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/review_issues.md",
      "version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/**",
      "version/V_1.0/work_record.md",
      "project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml",
      "project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.md",
      "project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md"
    ],
    "acceptance_trace_ids": [
      "TR-P2-SYSTEM-RULEVIEW-001",
      "TR-P2-SYSTEM-RULEVIEW-002",
      "TR-P2-SYSTEM-RULEVIEW-003",
      "TR-P2-SYSTEM-RULEVIEW-004",
      "TR-P2-SYSTEM-RULEVIEW-005",
      "TR-P2-SYSTEM-RULEVIEW-006",
      "TR-P2-SYSTEM-RULEVIEW-007",
      "TR-P2-SYSTEM-RULEVIEW-008",
      "TR-P2-SYSTEM-RULEVIEW-009",
      "TR-P2-SYSTEM-RULEVIEW-010"
    ],
    "flow_refs": [
      "FLOW-CONFIG-COMPILE",
      "FLOW-PROTECTED-ACCESS-EXECUTE"
    ],
    "flow_step_refs": [
      "STEP-P2-COMPILE-01",
      "STEP-P2-COMPILE-02",
      "STEP-P2-COMPILE-03",
      "STEP-P2-COMPILE-04",
      "STEP-P2-ACCESS-01",
      "STEP-P2-ACCESS-02",
      "STEP-P2-ACCESS-03",
      "STEP-P2-ACCESS-04",
      "STEP-P2-ACCESS-05",
      "STEP-P2-ACCESS-06"
    ],
    "validation_commands": [
      "python3 /home/oai/skills/common-develop/scripts/task_plan.py validate -g ImplementationPlanAgent --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC --require-revision",
      "python3 -c \"import yaml,re,collections; p=yaml.safe_load(open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml')); c=collections.defaultdict(list); [c[re.match(r'SRC-(P2-T\\\\d{2})(?:-|$)',a['id']).group(1)].append(t['task_id']) for t in p['tasks'] for a in t['acceptance_criteria'] if re.match(r'SRC-(P2-T\\\\d{2})(?:-|$)',a['id'])]; e={f'P2-T{i:02d}' for i in range(1,13)}; assert set(c)==e and all(c[x] for x in e); print('P2 source-scope mapping 12/12 PASSED')\"",
      "python3 -c \"import yaml; p=yaml.safe_load(open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml')); b={t['task_id']:t for t in p['tasks']}; d4=b['TASK-P2-DEV-04-CONTEXT-MATERIALIZATION']; d7=b['TASK-P2-DEV-07-STARTER-GUARDED-ACCESS']; d8=b['TASK-P2-DEV-08-PRODUCTION-COMPOSITION-CONCURRENCY']; assert 'dec-core-compiler/src/main/java/dec/core/compiler/pass/CompiledModelSetBuilder.java' in d4['implementation']['affected_files']; assert 'dec-core-starter/pom.xml' in d7['implementation']['affected_files']; assert any('不得承担首次 dec-core-model dependency wiring' in x for x in d8['implementation']['steps']); print('R04 bounded-slice P1 closure preserved')\"",
      "python3 -c \"import yaml; p=yaml.safe_load(open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml')); assert len(p['tasks'])==9; assert p['input_revisions']['test_design']=='TESTDESIGN-P2-R32'; d=next(t for t in p['tasks'] if t['task_id']=='TASK-P2-DEV-03-MODEL-ACCESS-POLICY'); req=['CASE-P2-TD-NESTED-OBJECT-PATH-001','CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001','CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001','CASE-P2-TD-NESTED-COLLECTION-PATH-001','CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001','CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001']; text=' '.join(d['implementation']['steps']); assert all(x in text for x in req); assert any('TargetKeyModelPathContractTest' in c and 'ModelAccessPolicyContractTest' in c for c in d['validation_commands']); print('R32 nested ModelPath plan mapping 6/6 PASSED')\"",
      "python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ImplementationPlanAgent --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC",
      "git diff --check"
    ],
    "expected_results": [
      "development_tasks.yaml preserves an explicit machine-parseable P2-T01..P2-T12 -> nine Development Task mapping; every source item has at least one exact implementation/test/compatibility destination.",
      "development_tasks.yaml contains the same nine dependency-ordered vertical tasks covering all ten stable P2 trace IDs and the 23 exact TESTDESIGN-P2-R32 TestClasses; the six new nested ModelPath cases are explicitly owned by DEV-03.",
      "The exact R05 revision passes PlanReviewAgent, ArchitectureReviewAgent, TestDesignAgent and DevelopAgent serial task-plan reviews.",
      "R05 preserves both R04 bounded-slice P1 closures while only rebinding TestDesign authority from R31 to R32.",
      "Implementation Plan is machine-valid and executable without starting TDD or Development."
    ],
    "stop_conditions": [
      "任一当前输入 Revision 被重开或变为 STALE。",
      "任一 Plan Review 产生 P0/P1 finding 且当前 TP revision 尚未修复。",
      "计划要求改变已冻结业务/设计语义或提前进入 TDD/Development。"
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "TP-FEATURE-DESC-3361AD2E54FC-R05@b71685a8d84a",
    "validation_evidence_ids": [
      "EVD-000200",
      "EVD-000201",
      "EVD-000202",
      "EVD-000203",
      "EVD-000204",
      "EVD-000205",
      "EVD-000206",
      "EVD-000207",
      "EVD-000208",
      "EVD-000209"
    ]
  },
  {
    "id": "TASK-P2-TDD-RED-001",
    "logical_task_id": "LOGICAL-P2-TDD-RED-BASELINE",
    "feature_id": "FEATURE-DESC-3361AD2E54FC",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-TDD-008",
    "iteration_no": 8,
    "supersedes_iteration_id": "",
    "revision_reason": "Materialize the missing standard-mode TDD task for TDD-I008 after PASSED R05/R32; create executable test-only pre-development baseline without production implementation.",
    "title": "P2 R32/R05 开发前 TDD RED 基线",
    "objective": "Materialize all 23 exact TestClasses and 101 TESTDESIGN-P2-R32 blocking Cases as executable test code, preserve inherited P1 characterization, and freeze attributable target RED evidence before any Development attempt.",
    "phase": "tdd",
    "status": "PASSED",
    "depends_on": [
      "TASK-P2-IMPLEMENTATION-PLAN-001"
    ],
    "owner_agent": "TddAgent",
    "reviewer_agents": [
      "TDDReviewAgent"
    ],
    "input_revisions": {
      "implementation_plan": "TP-FEATURE-DESC-3361AD2E54FC-R05@b71685a8d84a",
      "test_design": "TESTDESIGN-P2-R32"
    },
    "allowed_files": [
      "dec-core-compiler/src/test/java/dec/core/compiler/system/SystemCompilationContractTest.java",
      "dec-core-compiler/src/test/java/dec/core/compiler/ruleview/RuleViewCompilationContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/runtime/ProtectedAccessContextApiContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/runtime/RuntimeFactValueContractTest.java",
      "dec-core-context/src/test/java/dec/core/context/runtime/OpaqueRuntimeIdContractTest.java",
      "dec-core-compiler/src/test/java/dec/core/compiler/contract/P2CompilerContextConstructibilityContractTest.java",
      "dec-core-compiler/src/test/java/dec/core/compiler/model/access/TargetKeyModelPathContractTest.java",
      "dec-core-compiler/src/test/java/dec/core/compiler/model/access/ModelAccessPolicyContractTest.java",
      "dec-core-compiler/src/test/java/dec/core/compiler/publication/AtomicPublicationContractTest.java",
      "dec-core-compiler/src/test/java/dec/core/compiler/diagnostic/P2DiagnosticDeterminismTest.java",
      "dec-core-model/src/test/java/dec/core/model/runtime/ProtectedAccessModelApiContractTest.java",
      "dec-core-model/src/test/java/dec/core/model/runtime/RuntimeModelMaterializationIntegrationTest.java",
      "dec-core-model/src/test/java/dec/core/model/runtime/RuntimeObjectLocatorIntegrationTest.java",
      "dec-core-model/src/test/java/dec/core/model/runtime/ProtectedWriteTransactionIntegrationTest.java",
      "dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessStarterApiContractTest.java",
      "dec-core-starter/src/test/java/dec/core/starter/access/ProtectedWriteIntentResolutionTest.java",
      "dec-core-starter/src/test/java/dec/core/starter/access/ProtectedRuntimeModelAdapterIntegrationTest.java",
      "dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessProductionCompositionTest.java",
      "dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessConcurrencyTest.java",
      "dec-core-starter/src/test/java/dec/core/starter/architecture/ProtectedAccessDependencyDirectionTest.java",
      "dec-demo/src/test/java/dec/demo/p2/P2RealFixtureIntegrationTest.java",
      "dec-core-compiler/src/test/java/dec/core/compiler/compat/P2DeclarationCompatibilityContractTest.java",
      "dec-core-compiler/src/test/java/dec/core/compiler/contract/P2RevisionDependencyDagContractTest.java",
      "project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/commands/tdd-p2-r01/validate_tdd_red.py"
    ],
    "acceptance_trace_ids": [
      "TR-P2-SYSTEM-RULEVIEW-001",
      "TR-P2-SYSTEM-RULEVIEW-002",
      "TR-P2-SYSTEM-RULEVIEW-004",
      "TR-P2-SYSTEM-RULEVIEW-008",
      "TR-P2-SYSTEM-RULEVIEW-003",
      "TR-P2-SYSTEM-RULEVIEW-005",
      "TR-P2-SYSTEM-RULEVIEW-009",
      "TR-P2-SYSTEM-RULEVIEW-006",
      "TR-P2-SYSTEM-RULEVIEW-007",
      "TR-P2-SYSTEM-RULEVIEW-010"
    ],
    "flow_refs": [
      "FLOW-CONFIG-COMPILE",
      "FLOW-PROTECTED-ACCESS-EXECUTE"
    ],
    "flow_step_refs": [
      "STEP-P2-COMPILE-01",
      "STEP-P2-COMPILE-02",
      "STEP-P2-COMPILE-03",
      "STEP-P2-COMPILE-04",
      "STEP-P2-ACCESS-01",
      "STEP-P2-ACCESS-02",
      "STEP-P2-ACCESS-03",
      "STEP-P2-ACCESS-06",
      "STEP-P2-ACCESS-04",
      "STEP-P2-ACCESS-05"
    ],
    "validation_commands": [
      "python3 project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/commands/tdd-p2-r01/validate_tdd_red.py",
      "git diff --check"
    ],
    "expected_results": [
      "All 23 exact R32 TestClasses and all 101 blocking Cases are executable and revision-bound; every non-zero target run is a compile-clean P2 RED attributable to missing target behavior, inherited already-correct P1 contracts may remain characterization GREEN, no production source is modified, and the complete pre-development baseline is independently PASSED by TDDReviewAgent."
    ],
    "stop_conditions": [
      "Any Java/Maven compile, dependency-resolution, fixture, environment or setup error is INVALID_RED and blocks TDD completion.",
      "Any production source/config change during this TddAgent attempt blocks completion and must be removed.",
      "Any stale R05/R32 input revision or Case/TestClass mapping mismatch requires reopening the proper upstream phase."
    ],
    "risk_triggers": [],
    "attempts": 1,
    "max_attempts": 3,
    "output_revision": "TDD-P2-R01@3f282bb4e1f6",
    "validation_evidence_ids": [
      "EVD-000210",
      "EVD-000211",
      "EVD-000212",
      "EVD-000213",
      "EVD-000214",
      "EVD-000238",
      "EVD-000215",
      "EVD-000216",
      "EVD-000217",
      "EVD-000218",
      "EVD-000219",
      "EVD-000220",
      "EVD-000221",
      "EVD-000222",
      "EVD-000223",
      "EVD-000224",
      "EVD-000225",
      "EVD-000226",
      "EVD-000227",
      "EVD-000228",
      "EVD-000229",
      "EVD-000230",
      "EVD-000231",
      "EVD-000232",
      "EVD-000233",
      "EVD-000234",
      "EVD-000235",
      "EVD-000236",
      "EVD-000237",
      "EVD-000239",
      "EVD-000240"
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
- `risk_triggers` 只能填写 `assets/runtime-contract.json#riskReviewerCatalog` 中的风险 Key，不得填写 Reviewer 名或自由文本。
- 当前支持的风险 Key：`security`、`performance`、`data_migration`、`api_contract`、`concurrency`、`test_evidence`、`maintainability`、`impact_analysis`、`cross_module_integration`、`architecture_change`。
- 风险 Key 还必须允许用于当前任务阶段；可执行 `long_task.py list-risk-triggers --phase {PHASE}` 查询。
