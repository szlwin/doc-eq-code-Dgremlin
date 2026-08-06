# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T14 / I003` 已完成
- 当前有效 Completion：`COMPLETION-P1-T14-R03@37fb814b39c5`
- 失效但保留：`TDD-P1-T14-R02@1df0a14f2a74`、`CODEREVIEW-P1-T14-R03@668d865b0189`、`COMPLETION-P1-T14-R02@668d865b0189`
- Dependency：`COMPLETION-P1-T13-R03@5075793d06cc`
- 状态：`COMPLETED / PASSED`
- Mode：`TDD_REPAIR / ORACLE_HARDENING`
- Base：`dev_all@3e4da420d2ef5ada8398aefbbeabb37964e384ce`
- Branch：`feature/p1-t14-candidate-context-20260805-2324`
- PR：`#29 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Design：`DESIGN-R50@P1-T14-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R46@P1-T14-REWORK-I003`
- TDD：`TDD-P1-T14-R03@37fb814b39c5`
- Architecture：`DEVSKEL-P1-T14-R03@dc4f0f5cc566`
- Development：`DEV-P1-T14-R03@37fb814b39c5`
- Code Review：`CODEREVIEW-P1-T14-R05@37fb814b39c5`
- Testing：`TESTING-P1-T14-R03@37fb814b39c5`
- Reviews：`REV-000747`～`REV-000759`
- Evidence：`EVD-001092`～`EVD-001101`
- Open P0/P1/P2：`0 / 0 / 0`

## Current production contract

- atomic bind 同时冻结 raw/published Source、Definitions、Deferred 与版本域；
- T13 Source/Semantic Digest 使用同一冻结闭包计算；
- raw/published sourceId 集合必须完全一致；
- Builder 不公开分离式 source/registry/version/digest API；
- 正式 Digest 必须为 64 位小写 SHA-256；
- Publication Pass 绑定当前 request schema/options；
- provenance mismatch 和 missing input 产生精确 ERROR Diagnostic；
- 失败路径 publisher=0、artifacts empty；
- 正常路径完整 candidate 精确传给唯一 Publisher；
- Definition/Deferred 快照完整性门禁均有直接负向 Oracle；
- T15 与 P2～P7 runtime 未实现。

## I003 evidence repair

- I002 RED 因 `testCompile` 失败已失效保留；
- mutation A 短路 request binding，目标测试产生 1 个 assertion failure、0 error；
- mutation B 跳过 Source closure binding，目标测试产生 1 个 assertion failure、0 error；
- 两个 mutation 均成功编译并实际执行；
- 恢复正确源码后两个目标测试各 1/1 GREEN；
- mutation 源码未进入 Git；
- mutation XML、日志和 JSON 摘要独立归档；
- 完整 5 项/11 项 Surefire XML 在上传前恢复；
- PR #29 正文已更新至 I003。

## Validation

- Code/Test Revision：`37fb814b39c54e6260fd65d13cb31e817bc0fe92`
- P0 Run：`31073434459` — SUCCESS
- Artifact/SHA：`8956534261` / `3266e2b475bbcdf0f6dc24b3de097c84efbc40853ae77bec8432e6feaa7207e5`
- Surefire XML：109；T14：18/18；T13：34/34；T12：133/133；Compiler：504/504；Normal：624/624
- All records：625；intentional failure：1；Errors/Skipped：0/0
- Java 8、12 modules、mutation gate、intentional failure gate：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Revision integrity

- R50 first commit/blob：`b97bedbbadda21e9c7e0cfc68ae755d34019b724` / `050364ad99ae63067929f3b94473105579de484d`
- R46 first commit/blob：`445dbb496638d66a39a20a2174b8f4507957d6a7` / `d6c21d456b467b5efd543eb289196663697a683d`
- Code/Test Revision：`37fb814b39c54e6260fd65d13cb31e817bc0fe92`
- Code/Test Revision 后只允许 `project_doc` 更新；
- I003 生产和 JUnit 测试源码变更均为 0。

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t14-r03/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t14-r05.md`
- Invalidation：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t14-r04-invalidation.md`
- TDD：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/tdd-p1-t14-r03.md`
- Testing：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/testing-p1-t14-r03.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t14-r03.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t14_r03_completion.json`
- 既有 `@Override` 独占一行；脚本方法和重要逻辑使用中文注释；
- 仅在用户明确授权后合并 PR #29；
- TASK-P1-T15：`BLOCKED_UNTIL_PR_29_MERGE`。
