# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T14 / I002` 已完成
- 当前有效 Completion：`COMPLETION-P1-T14-R02@668d865b0189`
- 失效但保留：`COMPLETION-P1-T14-R01@252024603bfc`
- Dependency：`COMPLETION-P1-T13-R03@5075793d06cc`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@3e4da420d2ef5ada8398aefbbeabb37964e384ce`
- Branch：`feature/p1-t14-candidate-context-20260805-2324`
- PR：`#29 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Design：`DESIGN-R49@P1-T14-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R45@P1-T14-REWORK-I002`
- TDD：`TDD-P1-T14-R02@1df0a14f2a74`
- Architecture：`DEVSKEL-P1-T14-R02@2c7ddd4f4f96`
- Development：`DEV-P1-T14-R02@668d865b0189`
- Code Review：`CODEREVIEW-P1-T14-R03@668d865b0189`
- Testing：`TESTING-P1-T14-R02@668d865b0189`
- Reviews：`REV-000725`～`REV-000746`
- Evidence：`EVD-001068`～`EVD-001091`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- atomic bind 同时冻结 raw/published Source、Definitions、Deferred 与版本域；
- T13 Source/Semantic Digest 使用同一冻结闭包计算；
- raw/published sourceId 集合必须完全一致；
- Builder 不再公开分离式 source/registry/version/digest API；
- 正式 Digest 必须为 64 位小写 SHA-256；
- Publication Pass 绑定当前 request schema/options；
- provenance mismatch 和 missing input 均产生精确 ERROR Diagnostic；
- 失败路径 publisher=0、artifacts empty；
- 正常路径完整 candidate 精确传给唯一 Publisher；
- Definition/Deferred 全部快照完整性门禁均有直接负向 Oracle；
- T12/T13 Deadline、Observer、Digest 和 commit-wins 保持；
- T15 与 P2～P7 runtime 未实现。

## Validation

- Valid RED：`1df0a14f2a746d6027485a99dcf9cbd3ceeb3899`
- RED Run/Artifact/SHA：`31068551065` / `8954760225` / `7431ba21d9447de5cd60aa2db06cb849a3a045867553e276f7d22f61931d5d15`
- Code/Test Revision：`668d865b0189e9107f25295a1726748968aa7462`
- Clean P0：`31069685120` — SUCCESS
- Artifact/SHA：`8955166219` / `5553810bfb87146c97835dd5d1c2de10b4c2b8405a9ef533e994f110c7b71c6c`
- Surefire XML：109；T14：18/18；T13：34/34；T12：133/133；Compiler：504/504；Normal：624/624
- All records：625；intentional failure：1；Errors/Skipped：0/0
- Java 8、12 modules、intentional failure gate：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Revision Integrity

- R49 first commit：`eda473f06ea8b0dcc1666c0e41c9a179aaf5ad0d`
- R49 final pre-production commit/blob：`2c7ddd4f4f96d6a5c108d8aeca4534d62ace380c` / `023cc974ad5b29e74b13249003c597e341acf738`
- R45 first commit/blob：`331b3f6dc36596051cf2657e81b3d5059724e4e7` / `c80f520b34a409e5f5fa8eaa7166e95087ec9373`
- 行为合同在 RED 前冻结；具体 bind 签名在 RED 后、production 前受控修订；
- Code/Test Revision 后只允许 `project_doc` 更新。

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t14-r02/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t14-r03.md`
- Invalidation：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t14-r02-invalidation.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t14-r02.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t14_r02_completion.json`
- 所有 `@Override` 独占一行，方法和重要逻辑使用中文注释；
- 仅在用户明确授权后合并 PR #29；
- TASK-P1-T15：`BLOCKED_UNTIL_PR_29_MERGE`。
