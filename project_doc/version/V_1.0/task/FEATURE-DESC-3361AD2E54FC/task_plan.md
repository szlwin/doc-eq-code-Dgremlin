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
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-BUSINESS-MODEL-003",
    "iteration_no": 3,
    "supersedes_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-BUSINESS-MODEL-002",
    "revision_reason": "用户确认 BM-R06 核心语义基本正确，但要求按 BM-R05 完整可读结构重建 Markdown、显式建立 DEC_COMPILER→COMPILER 同一逻辑模块 lineage、验证 stable ID 全量继承，并形成新 Business Model Revision 后重新执行六项独立 Review。",
    "title": "建立 P2 System、RuleView 与 model-access 业务模型",
    "objective": "在已通过的 COMPILER BM-R06 基线上形成 BM-R07，保持 P2 System/RuleView/model-access 核心语义不变，显式建立 DEC_COMPILER→COMPILER 同一逻辑模块 lineage，恢复完整 17 节人类可读业务模型，并证明 BM-R05 stable IDs 全量继承。",
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
      "python3 -c \"import json,yaml,jsonschema; jsonschema.validate(yaml.safe_load(open('project_doc/version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml',encoding='utf-8')),json.load(open('/home/oai/skills/common-develop/assets/structured-docs/business-model.schema.json',encoding='utf-8')))\"",
      "python3 -c \"import json,yaml,jsonschema; jsonschema.validate(yaml.safe_load(open('project_doc/version/V_1.0/doc/COMPILER/changes/p2-business-model-lineage-readability.yaml',encoding='utf-8')),json.load(open('/home/oai/skills/common-develop/assets/structured-docs/changeset.schema.json',encoding='utf-8')))\"",
      "python3 -c \"import json,yaml,jsonschema; jsonschema.validate(yaml.safe_load(open('project_doc/docs/_relations/dependency_impact.yaml',encoding='utf-8')),json.load(open('/home/oai/skills/common-develop/assets/structured-docs/dependency-impact.schema.json',encoding='utf-8')))\"",
      "python3 /home/oai/skills/common-develop/scripts/render_relationships.py -g BusinessModelAgent --input project_doc/docs/_relations/dependency_impact.yaml --check",
      "git diff --check"
    ],
    "expected_results": [
      "BM-R05 stable IDs 全量继承，R07 不删除任何既有 stable ID",
      "DEC_COMPILER→COMPILER lineage 有结构化 relationship 和完整人类可读说明",
      "COMPILER_business_model.md 恢复为可独立阅读的完整 17 节视图且与 YAML 当前事实等价",
      "BM-R07 六项独立 Review 全部 PASSED，无开放 P0/P1"
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
    "output_revision": "BM-R07@7d7bf504ca9d",
    "validation_evidence_ids": [
      "EVD-000049",
      "EVD-000050",
      "EVD-000051",
      "EVD-000052",
      "EVD-000053",
      "EVD-000054",
      "EVD-000055",
      "EVD-000056",
      "EVD-000057",
      "EVD-000058"
    ]
  },
  {
    "id": "TASK-P2-DESIGN-001",
    "logical_task_id": "LOGICAL-P2-SYSTEM-RULEVIEW-DESIGN",
    "feature_id": "P2-SYSTEM-RULEVIEW-F01",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-DESIGN-003",
    "iteration_no": 3,
    "supersedes_iteration_id": "",
    "revision_reason": "BM-R07 已通过并显式冻结 DEC_COMPILER→COMPILER 单一谱系；当前 Design I003 首次将 P2 System、RuleView composite identity、ModelPath 与 model-access 静态/运行时权限边界落到可实现且可测试的模块、接口、状态与失败契约。",
    "title": "设计 P2 System、RuleView 与 model-access 编译/运行边界",
    "objective": "在 P1 编译器既有 SystemKey、RuleViewKey 与不可变 Context 基线上，形成 P2 增量详细设计，明确 CompiledSystem、复合 RuleView lookup、ModelPath、ModelAccessRule、静态授权、运行时 Guard、原子发布和 P7 兼容边界，不建立第二套 Compiler/Registry/Context。",
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
      "business_model": "BM-R07@7d7bf504ca9d"
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
      "python3 -c \"from pathlib import Path; p=Path('project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md'); s=p.read_text(); assert 'DESIGN-P2-R01@8875f042898c' in s; assert all(x in s for x in ['#p2-system','#p2-ruleview','#p2-model-path','#p2-model-access','#p2-runtime-guard','#p2-traceability'])\"",
      "python3 -c \"import json; from pathlib import Path; s=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md').read_text(); i=s.index('[',s.index('traceability')); j=s.index(chr(10)+chr(96)*3,i); a=json.loads(s[i:j]); p=[x for x in a if str(x.get('id','')).startswith('TR-P2-SYSTEM-RULEVIEW-')]; assert len(p)==10 and all(x.get('design_refs') for x in p)\"",
      "python3 /mnt/data/common-develop/scripts/long_task.py validate -g DesignAgent --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC",
      "git diff --check"
    ],
    "expected_results": [
      "ArchitectureReviewAgent 独立确认 P2 复用单一 compiler/context 发布闭包、依赖方向与 Guard 边界，不产生第二 Registry 或反向依赖。",
      "BusinessModelReviewAgent 独立确认详细设计完整承接 BM-R07 的 CompiledSystem、RuleView composite identity、ModelPath、ModelAccessRule 与 fail-closed 不变量。",
      "DevelopAgent 独立确认设计具有明确模块落点、接口职责、算法顺序、失败语义与兼容边界，可在后续计划阶段拆分实现而无需重新决定核心语义。",
      "RequirementReviewAgent 独立确认 Design 覆盖 P2 十条 trace/验收且未扩展到 P3～P8 运行语义。",
      "TestDesignAgent 独立确认 System determinism、RuleView 隔离、ModelPath、权限矩阵、静态拒绝、runtime Guard、原子发布与兼容边界均暴露稳定测试 seam。",
      "ImpactAnalysisReviewAgent 独立确认 compiler/context/frontend/starter/declaration 的影响、依赖顺序、兼容窗口和失败传播完整。",
      "CrossModuleIntegrationReviewAgent 独立确认 P2 跨模块编译—发布—调用链无双写、无裸名称新回退、无第二 runtime authority。"
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
    "output_revision": "DESIGN-P2-R01@8875f042898c",
    "validation_evidence_ids": [
      "EVD-000070",
      "EVD-000071",
      "EVD-000072",
      "EVD-000073",
      "EVD-000074",
      "EVD-000075",
      "EVD-000076",
      "EVD-000077",
      "EVD-000078",
      "EVD-000079",
      "EVD-000080",
      "EVD-000081",
      "EVD-000082",
      "EVD-000083"
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
