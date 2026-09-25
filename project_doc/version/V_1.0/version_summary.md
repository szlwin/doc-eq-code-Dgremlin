<!-- generated-by: common-develop/document_portal.py -->
# V_1.0 版本文档摘要

> 本页是当前版本的人类阅读入口，由现有版本事实确定性生成；不保存新的需求、设计、Review 或运行事实。

## 快速入口

- [项目文档首页](../../docs/README.md)
- [研发进度](project_process.md)
- [工作清单](work.md)
- [工作记录](work_record.md)

## 本版本需求

| 目标 | 需求文档 |
| --- | --- |
| FEATURE-DESC-3361AD2E54FC | [需求正文](requirement/FEATURE-DESC-3361AD2E54FC/requirement.md) / [测试设计](requirement/FEATURE-DESC-3361AD2E54FC/test_case.md) |
| FEATURE-DESC-4AB41AC241A1 | [需求正文](requirement/FEATURE-DESC-4AB41AC241A1/requirement.md) / [测试设计](requirement/FEATURE-DESC-4AB41AC241A1/test_case.md) |
| FEATURE-DESC-C0EDEFBD6D71 | [需求正文](requirement/FEATURE-DESC-C0EDEFBD6D71/requirement.md) |
| P1-COMPILER-CR01 | [需求正文](requirement/P1-COMPILER-CR01/requirement.md) |
| P1-COMPILER-CR02 | [需求正文](requirement/P1-COMPILER-CR02/requirement.md) |
| P1-COMPILER-CR03 | [需求正文](requirement/P1-COMPILER-CR03/requirement.md) |
| P1-COMPILER-F01 | [需求正文](requirement/P1-COMPILER-F01/requirement.md) / [测试设计](requirement/P1-COMPILER-F01/test_case.md) |

## 模块、设计与业务模型

| 模块 | 文档 |
| --- | --- |
| COMPILER | [模块说明](module/COMPILER/COMPILER_desc.md) / [设计](module/COMPILER/COMPILER_design.md) / [业务模型事实](module/COMPILER/COMPILER_business_model.yaml) |
| DEC_COMPILER | [设计](module/DEC_COMPILER/DEC_COMPILER_design.md) / [业务模型事实](module/DEC_COMPILER/DEC_COMPILER_business_model.yaml) |
| MODEL | [模块说明](module/MODEL/MODEL_desc.md) |

## 流程与 API/DB 增量

- [module/COMPILER/changes/changeset.template.yaml](module/COMPILER/changes/changeset.template.yaml)
- [module/COMPILER/changes/p1-archive-business-model.yaml](module/COMPILER/changes/p1-archive-business-model.yaml)
- [module/COMPILER/changes/p2-business-model-lineage-readability.yaml](module/COMPILER/changes/p2-business-model-lineage-readability.yaml)
- [module/COMPILER/changes/p2-business-model-operation-bound-capability-remediation-r11.yaml](module/COMPILER/changes/p2-business-model-operation-bound-capability-remediation-r11.yaml)
- [module/COMPILER/changes/p2-business-model-review-remediation-r09.yaml](module/COMPILER/changes/p2-business-model-review-remediation-r09.yaml)
- [module/COMPILER/changes/p2-business-model-runtime-binding-proof-remediation-r10.yaml](module/COMPILER/changes/p2-business-model-runtime-binding-proof-remediation-r10.yaml)
- [module/COMPILER/changes/p2-business-model-unified-protected-access-remediation-r12.yaml](module/COMPILER/changes/p2-business-model-unified-protected-access-remediation-r12.yaml)
- [module/COMPILER/changes/p2-independent-review-remediation-r08.yaml](module/COMPILER/changes/p2-independent-review-remediation-r08.yaml)
- [module/COMPILER/changes/p2-simple-runtime-model-r06.yaml](module/COMPILER/changes/p2-simple-runtime-model-r06.yaml)
- [module/COMPILER/changes/p2-simple-runtime-model.dependency-impact.changeset.yaml](module/COMPILER/changes/p2-simple-runtime-model.dependency-impact.changeset.yaml)
- [module/COMPILER/changes/p2-system-ruleview-business-model.yaml](module/COMPILER/changes/p2-system-ruleview-business-model.yaml)
- [module/MODEL/changes/changeset.template.yaml](module/MODEL/changes/changeset.template.yaml)
- [module/MODEL/changes/dependency-impact.changeset.template.yaml](module/MODEL/changes/dependency-impact.changeset.template.yaml)
- [module/MODEL/changes/p3-information.business-model.changeset.yaml](module/MODEL/changes/p3-information.business-model.changeset.yaml)
- [module/MODEL/changes/p3-information.dependency-impact.changeset.yaml](module/MODEL/changes/p3-information.dependency-impact.changeset.yaml)
- [module/MODEL/changes/p3-information.rebaseline.dependency-impact.changeset.yaml](module/MODEL/changes/p3-information.rebaseline.dependency-impact.changeset.yaml)
- [module/_flows/COMPILER/changes/001-layout-migration.yaml](module/_flows/COMPILER/changes/001-layout-migration.yaml)
- [module/_flows/COMPILER/changes/004-p2-simple-runtime-model.yaml](module/_flows/COMPILER/changes/004-p2-simple-runtime-model.yaml)
- [module/_flows/COMPILER/changes/005-p3-information-evaluation.yaml](module/_flows/COMPILER/changes/005-p3-information-evaluation.yaml)
- [module/_flows/COMPILER/changes/006-p3-information-rebaseline.yaml](module/_flows/COMPILER/changes/006-p3-information-rebaseline.yaml)

## 长任务摘要

| 任务 | 阅读入口 |
| --- | --- |
| FEATURE-DESC-4AB41AC241A1 | [大需求实施计划](task/FEATURE-DESC-4AB41AC241A1/development_task_plan.json) |

## 归档说明

- 本目录保留版本研发事实和增量；项目当前事实从[项目文档首页](../../docs/README.md)进入。
- API/DB 不生成项目级全量文档，版本增量保留在各模块 `changes/` 目录。
- Review、Evidence、Assertion、StageOutcome 和 `task_events.jsonl` 属于机器治理/审计层，普通阅读优先使用任务摘要和 `wk status/next/context`。
