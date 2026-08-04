# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T11 / I002` 已完成
- 当前有效 Completion：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- 失效但保留：`COMPLETION-P1-T11-R01@f09d9786fad8`
- Dependency：`COMPLETION-P1-T10-R03@336d309f3748`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@f97b7e47ac0fb40209c4dc512aa15d67c19be44b`
- Branch：`feature/p1-t11-deferred-classification-20260804-2058`
- PR：`#26`
- Design：`DESIGN-R37@P1-T11-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R33@P1-T11-REWORK-I002`
- TDD：`TDD-P1-T11-R02@1297a9dd947f`
- Architecture：`DEVSKEL-P1-T11-R02@86013589b65d`
- Development：`DEV-P1-T11-R02@1f9f887837bd`
- Code Review：`CODEREVIEW-P1-T11-R02@86b55b45d1cd`
- Testing：`TESTING-P1-T11-R02@86b55b45d1cd`
- Reviews：`REV-000476`～`REV-000489`
- Evidence：`EVD-000766`～`EVD-000786`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- 八种 DeferredKind 固定映射至 P2-P7 和稳定 reasonCode；
- 分类输入必须冻结 owner、kind、ordinal、SourceRef、NormalizedBody 和强类型引用；
- null `resolvedReferences` 容器是未提供，显式空列表是已提供；
- 批次在任何元素读取前形成局部不可变快照，复制失败使用 `inputs-snapshot`；
- 缺字段、reason-policy、null typed ref、unresolved lexical、null input 与 duplicate key 使用 `MIX-DEFERRED-INCOMPLETE`；
- 任一错误不发布部分 Registry；空批次发布不可变空 Registry；
- P1 不执行 P2-P7 runtime、SQL、事务、I/O、网络、DAG 或缓存。

## Clean-code validation

- Valid RED：`1297a9dd947fedc3683d2eff1d61d6484e73a351` / Run `30919478960` / `5 failures, 0 errors`
- Architecture：`86013589b65da324d1e237e593b681c482cb6c4c` / Run `30919667799` / `2 controlled failures, 0 errors`
- Clean-code Head：`86b55b45d1cd658401ec541fa12bfd868ef5fadc`
- P0 Run：`30919883791` — SUCCESS
- Artifact：`8896619234`
- SHA-256：`1e37ba710cf47c7f8ff22c1d2e8d7509cadbcc0172c7ed28a30924fcaf9f2294`

## Documented-head validation

- Head：`5d8fbe86d633f9189b7abd8aa4dcab0021b20f14`
- P0 Run：`30920489277` — SUCCESS
- Artifact：`8896877544`
- SHA-256：`57b51013c448fac6d497fb211d8ebee4f1a28c4d88953e5f8492be502726b1f7`
- I002：`8/8`；T11：`34/34`；Compiler：`319/319`；Normal：`439/439`
- Surefire XML：`83`；Errors/Skipped：`0/0`
- 12 modules / Java release 8 / intentional failure gate：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t11-r02/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t11-r02.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t11-r02.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t11_r02_completion.json`
- 重要逻辑使用中文注释，所有 `@Override` 独占一行；
- 本恢复事实更新后的最终 PR Head 需执行 P0，最终 Run/Artifact 记录到 PR #26 描述；
- 仅在用户明确授权后合并 PR #26；
- TASK-P1-T12：`BLOCKED_UNTIL_PR_26_MERGE`。
