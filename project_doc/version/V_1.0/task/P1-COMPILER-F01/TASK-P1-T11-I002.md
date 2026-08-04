# TASK-P1-T11 / I002 — Deferred 完整性与批次快照返工

- Status：`IN_PROGRESS / TDD_RED`
- Trigger：独立 Review `NEEDS_CHANGES / REWORK`
- Reviewed Head：`ae35e1cc745bf096f35c20ba73dc4909286e7a3b`
- Invalidated：`COMPLETION-P1-T11-R01@f09d9786fad8`
- Preserved History：I001 的 Design、Plan、RED、Architecture、Review、Completion、CI、Artifact 与 rejected attempt
- Design：`DESIGN-R37@P1-T11-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R33@P1-T11-REWORK-I002`
- Branch：`feature/p1-t11-deferred-classification-20260804-2058`
- PR：`#26`
- Open P0/P1/P2：`0 / 1 / 1`

## Findings

- `FND-P1-T11-I002-001` `[P1][BLOCKER]`：null `resolvedReferences` 容器被伪装成显式空列表；
- `FND-P1-T11-I002-002` `[P2]`：`DeferredDefinitionBuilder.build()` 直接遍历调用方批次 List，没有先形成快照。

## Allowed changes

- `dec-core-compiler/src/main/java/dec/core/compiler/deferred/DeferredClassificationInput.java`
- `dec-core-compiler/src/main/java/dec/core/compiler/deferred/DeferredDefinitionBuilder.java`
- `dec-core-compiler/src/test/java/dec/core/compiler/deferred/**`
- 本任务对应的 `project_doc` Design、Plan、Review、Evidence、Completion 与恢复文件

## Stop conditions

- R01 历史被覆盖或删除；
- 修改 T06～T10、Context 或 Compiler 公共 API；
- 未形成有效 RED 即修改生产行为；
- Open P0/P1 未关闭；
- 未完成最终 P0、Artifact 与独立 Review；
- 用户未明确授权时合并 PR #26 或启动 TASK-P1-T12。
