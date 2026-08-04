# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T09 / I002` 已完成
- 当前有效 Completion：`COMPLETION-P1-T09-R02@95b08223083f`
- 被推翻 Completion：`COMPLETION-P1-T09-R01@ecfe3f53bde7`，不可变历史保留
- Dependency：`COMPLETION-P1-T08-R02@bab0993ecfd8`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@e47551e0c79984d8f3fafc0ce379da76ad0d5593`
- Rework Base：`19b14487646c66ab1d7a386e96fc4876581b214c`
- Branch：`feature/p1-t09-engine-context-20260804-1040`
- PR：`#24`
- Design：`DESIGN-R32@P1-T09-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R28@P1-T09-REWORK-I002`
- TDD：`TDD-P1-T09-R02@002594d2cba2`
- Architecture：`DEVSKEL-P1-T09-R02@3efb2d1f0c97`
- Development：`DEV-P1-T09-R02@95b08223083f`
- Code Review：`CODEREVIEW-P1-T09-R02@95b08223083f`
- Testing：`TESTING-P1-T09-R02@95b08223083f`
- Reviews：`REV-000391`～`REV-000407`
- Evidence：`EVD-000646`～`EVD-000668`
- Findings：`FND-P1-T09-I002-001/002/003/004` 全部 CLOSED
- Open P0/P1/P2：`0 / 0 / 0`

## Revision Integrity

- R32 first commit：`d6099f1ab502bfc2e7ee0d81da8010a40c6da0e0`
- R32 blob：`645dae1f3d065e910160fa70e615810cdb9e1ce9`
- R28 first commit：`4b489d32aa123ce0d9dd0854eec9c2b5389be599`
- R28 blob：`3f4004e5913ee19f49394e5c637923c0d4a87880`
- R32/R28 在有效 I002 RED 前创建，clean-code Head blob 不变。
- R27 原无效 SHA：`4483ce64...`；正确 first commit：`e7713c4499271b79b958d0c0e0793c02e6be5428`；blob `20a16d1e...8dca`；位于 R01 RED 前 7 个 commit。

## Current Contract

- common 权限和 Information/System/ModelAccess 限制只根据 canonical SystemKey 判定；raw lexical 保留；
- `SymbolTable.isBuiltFrom` 只返回完整快照一致性 boolean，不暴露内部快照；
- Compiler 在所有 semantic work 前执行门禁；失配只返回 `information.input.snapshot-mismatch`；
- 快照失败 parser/resolver 调用数为 0，且不发布 AST、依赖或 Deferred；
- 128 层括号通过，129 层返回 limit Diagnostic；
- 原 T09 AST、owner、common、P3 Deferred、稳定依赖和全批原子发布合同保持不变；
- 无求值、DAG、循环检测、缓存、I/O、网络、模糊查询或全局状态。

## Validation

- Clean-code Head：`95b08223083f9d6b8573e96cdd12364334c0f234`
- P0 Run：`30882162374` — SUCCESS
- Artifact：`8881702632`
- SHA-256：`2f09baf88333eeff96e34ac7ab6be840c0aba4bfffd20309d9afe6bfad64ce4f`
- Surefire XML：`70`
- I002：`12/12`
- T09：`36/36`
- Symbol：`66/66`
- Compiler：`231/231`
- XML：`30/30`
- YAML：`59/59`
- Context normal：`26/26`
- Demo：`4/4`
- Legacy：`1/1`
- Normal tests：`351/351`
- Intentional failure gate：`recognized`
- Reactor：`12 modules / PASSED`
- Java release 8：`PASSED`
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t09-r02/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t09-r02.md`
- Revision correction：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-correction-p1-t09-r02.md`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t09_r02_completion.json`
- 临时 workflow：已删除；
- `@Override` 独占一行，方法与重要逻辑使用中文注释；
- 下一 Agent：`IndependentReviewAgent`；
- 下一动作：复核 PR #24 最终文档化 Head；仅在用户明确授权后合并；
- TASK-P1-T10：`BLOCKED_UNTIL_PR_MERGE`。
