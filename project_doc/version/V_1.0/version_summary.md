<!-- generated-by: common-develop/document_portal.py -->
# V_1.0 版本文档摘要

> 本页是当前版本的人类阅读入口，由现有版本事实确定性生成；不保存新的需求、设计、Review 或运行事实。

## 快速入口

- [项目文档首页](../../docs/README.md)
- [本版本需求列表](requirement_list.md)
- [研发进度](project_process.md)
- [工作清单](work.md)
- [工作记录](work_record.md)

## 本版本需求

| 目标 | 需求文档 |
| --- | --- |
| FEATURE-DESC-3361AD2E54FC | [需求正文](doc/FEATURE-DESC-3361AD2E54FC/requirement.md) / [测试设计](doc/FEATURE-DESC-3361AD2E54FC/test_case.md) |
| FEATURE-DESC-4AB41AC241A1 | [需求正文](doc/FEATURE-DESC-4AB41AC241A1/requirement.md) / [测试设计](doc/FEATURE-DESC-4AB41AC241A1/test_case.md) |
| P1-COMPILER-CR01 | [需求正文](doc/P1-COMPILER-CR01/requirement.md) |
| P1-COMPILER-CR02 | [需求正文](doc/P1-COMPILER-CR02/requirement.md) |
| P1-COMPILER-CR03 | [需求正文](doc/P1-COMPILER-CR03/requirement.md) |
| P1-COMPILER-F01 | [需求正文](doc/P1-COMPILER-F01/requirement.md) / [测试设计](doc/P1-COMPILER-F01/test_case.md) |

## 模块、设计与业务模型

| 模块 | 文档 |
| --- | --- |
| COMPILER | [模块说明](doc/COMPILER/COMPILER_desc.md) / [设计](doc/COMPILER/COMPILER_design.md) / [业务模型事实](doc/COMPILER/COMPILER_business_model.yaml) |
| DEC_COMPILER | [设计](doc/DEC_COMPILER/DEC_COMPILER_design.md) / [业务模型事实](doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml) |
| FEATURE-DESC-3361AD2E54FC | 暂无可读文档 |
| FEATURE-DESC-4AB41AC241A1 | [设计](doc/FEATURE-DESC-4AB41AC241A1/FEATURE-DESC-4AB41AC241A1_design.md) / [业务模型事实](doc/FEATURE-DESC-4AB41AC241A1/FEATURE-DESC-4AB41AC241A1_business_model.yaml) |
| MODEL | [模块说明](doc/MODEL/MODEL_desc.md) |
| P1-COMPILER-CR01 | 暂无可读文档 |
| P1-COMPILER-CR02 | 暂无可读文档 |
| P1-COMPILER-CR03 | 暂无可读文档 |
| P1-COMPILER-F01 | 暂无可读文档 |
| P1-GOVERNANCE-REPAIR | 暂无可读文档 |

## 流程与 API/DB 增量

- [doc/COMPILER/changes/changeset.template.yaml](doc/COMPILER/changes/changeset.template.yaml)
- [doc/COMPILER/changes/p1-archive-business-model.yaml](doc/COMPILER/changes/p1-archive-business-model.yaml)
- [doc/COMPILER/changes/p2-business-model-lineage-readability.yaml](doc/COMPILER/changes/p2-business-model-lineage-readability.yaml)
- [doc/COMPILER/changes/p2-business-model-operation-bound-capability-remediation-r11.yaml](doc/COMPILER/changes/p2-business-model-operation-bound-capability-remediation-r11.yaml)
- [doc/COMPILER/changes/p2-business-model-review-remediation-r09.yaml](doc/COMPILER/changes/p2-business-model-review-remediation-r09.yaml)
- [doc/COMPILER/changes/p2-business-model-runtime-binding-proof-remediation-r10.yaml](doc/COMPILER/changes/p2-business-model-runtime-binding-proof-remediation-r10.yaml)
- [doc/COMPILER/changes/p2-business-model-unified-protected-access-remediation-r12.yaml](doc/COMPILER/changes/p2-business-model-unified-protected-access-remediation-r12.yaml)
- [doc/COMPILER/changes/p2-independent-review-remediation-r08.yaml](doc/COMPILER/changes/p2-independent-review-remediation-r08.yaml)
- [doc/COMPILER/changes/p2-simple-runtime-model-r06.yaml](doc/COMPILER/changes/p2-simple-runtime-model-r06.yaml)
- [doc/COMPILER/changes/p2-simple-runtime-model.dependency-impact.changeset.yaml](doc/COMPILER/changes/p2-simple-runtime-model.dependency-impact.changeset.yaml)
- [doc/COMPILER/changes/p2-system-ruleview-business-model.yaml](doc/COMPILER/changes/p2-system-ruleview-business-model.yaml)
- [doc/MODEL/changes/changeset.template.yaml](doc/MODEL/changes/changeset.template.yaml)
- [doc/MODEL/changes/dependency-impact.changeset.template.yaml](doc/MODEL/changes/dependency-impact.changeset.template.yaml)
- [doc/_flows/COMPILER/changes/001-layout-migration.yaml](doc/_flows/COMPILER/changes/001-layout-migration.yaml)
- [doc/_flows/COMPILER/changes/004-p2-simple-runtime-model.yaml](doc/_flows/COMPILER/changes/004-p2-simple-runtime-model.yaml)

## 长任务摘要

| 任务 | 阅读入口 |
| --- | --- |
| FEATURE-DESC-4AB41AC241A1 | [大需求实施计划](task/FEATURE-DESC-4AB41AC241A1/development_task_plan.json) |

## 归档说明

- 本目录保留版本研发事实和增量；项目当前事实从[项目文档首页](../../docs/README.md)进入。
- API/DB 不生成项目级全量文档，版本增量保留在各模块 `changes/` 目录。
- Review、Evidence、Assertion、StageOutcome 和 `task_events.jsonl` 属于机器治理/审计层，普通阅读优先使用任务摘要和 `wk status/next/context`。
