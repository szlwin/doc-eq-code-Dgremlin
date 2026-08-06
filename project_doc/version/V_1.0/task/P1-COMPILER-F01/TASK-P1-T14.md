# TASK-P1-T14 — Provenance-bound Candidate EngineContext

- Current Iteration：`I003`
- Status：`COMPLETED / PASSED`
- Mode：`TDD_REPAIR / ORACLE_HARDENING`
- Base：`dev_all@3e4da420d2ef5ada8398aefbbeabb37964e384ce`
- Dependency：`COMPLETION-P1-T13-R03@5075793d06cc`
- Branch：`feature/p1-t14-candidate-context-20260805-2324`
- PR：`#29 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Design：`DESIGN-R50@P1-T14-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R46@P1-T14-REWORK-I003`
- TDD：`TDD-P1-T14-R03@37fb814b39c5`
- Architecture：`DEVSKEL-P1-T14-R03@dc4f0f5cc566`
- Development：`DEV-P1-T14-R03@37fb814b39c5`
- Code Review：`CODEREVIEW-P1-T14-R05@37fb814b39c5`
- Testing：`TESTING-P1-T14-R03@37fb814b39c5`
- Completion：`COMPLETION-P1-T14-R03@37fb814b39c5`
- Reviews：`REV-000747`～`REV-000759`
- Evidence：`EVD-001092`～`EVD-001101`
- Open P0/P1/P2：`0 / 0 / 0`

## Completion history

- `COMPLETION-P1-T14-R01@252024603bfc` — `INVALIDATED / PRESERVED`；
- `COMPLETION-P1-T14-R02@668d865b0189` — `INVALIDATED / PRESERVED`；
- `COMPLETION-P1-T14-R03@37fb814b39c5` — `CURRENT / PASSED`。

## TDD history

- `TDD-P1-T14-R02@1df0a14f2a74` — `INVALIDATED / PRESERVED`；对应 Run 在 `testCompile` 阶段失败，Compiler 测试未执行；
- `TDD-P1-T14-R03@37fb814b39c5` — `CURRENT / PASSED`；采用可复现 mutation proof，不回写历史、不伪造旧基线 RED。

## Delivered production contract

I003 未修改生产代码。I002 已交付并继续保持：

- `CompilerDigestService.bind()` 原子冻结 raw/published Source、Definitions、Deferred 和版本域；
- 同一不可变快照构造 T13 `SemanticDigestInput` 并计算 `DigestPair`；
- raw/published SourceManifest 的 sourceId 集合必须完全一致；
- `DigestBoundCompiledInput` 私有构造，调用方不能注入任意 DigestPair；
- `CompiledModelSetBuilder` 只接受 atomic input，freeze 后永久封闭；
- 正式摘要边界只接受 64 位小写 SHA-256；
- Publication Pass 在 prepare 前比较当前 request schema/options；
- mismatch 返回 `MIX_PUBLICATION_PROVENANCE_MISMATCH / ERROR`；
- missing input 返回 `MIX_PUBLICATION_BLOCKED / ERROR`；
- 失败路径固定 FAILED、publisher=0、artifacts empty；
- Definition/Deferred 快照拒绝 negative size、keys mismatch、duplicate、missing、identity mismatch 和 size drift；
- Pipeline 继续唯一持有 Publisher/CAS capability。

## I003 evidence repair

P0 Workflow 新增 T14 provenance mutation gate：

1. 完整 `clean verify` 先运行并通过；
2. 临时短路 request binding，目标测试产生 1 个 assertion failure、0 error；
3. 临时跳过 Source closure binding，目标测试产生 1 个 assertion failure、0 error；
4. 恢复正确源码后两个目标测试各 1/1 GREEN；
5. mutation XML、日志和 JSON 摘要独立归档；
6. 上传前恢复完整 5 项/11 项正常 Surefire XML；
7. mutation 版本不进入 Git。

## Findings

- `FND-P1-T14-I003-001`：`CLOSED`；
- `FND-P1-T14-I003-002`：`CLOSED`；
- `FND-P1-T14-I003-003`：`CLOSED`；
- Open P0/P1/P2：`0/0/0`。

## Validation

- Code/Test Revision：`37fb814b39c54e6260fd65d13cb31e817bc0fe92`；
- P0 Run：`31073434459` — SUCCESS；
- Artifact/SHA：`8956534261` / `3266e2b475bbcdf0f6dc24b3de097c84efbc40853ae77bec8432e6feaa7207e5`；
- Surefire XML：109；T14：18/18；T13：34/34；T12：133/133；Compiler：504/504；
- Normal：624/624；All：625；intentional failure：1；Errors/Skipped：0/0；
- Mutation A/B：各 1 test / 1 assertion failure / 0 error；恢复后各 1/1 GREEN；
- Java release 8、12 modules Reactor、mutation gate、intentional failure gate：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Revision integrity and style

- R50 first commit/blob：`b97bedbbadda21e9c7e0cfc68ae755d34019b724` / `050364ad99ae63067929f3b94473105579de484d`；
- R46 first commit/blob：`445dbb496638d66a39a20a2174b8f4507957d6a7` / `d6c21d456b467b5efd543eb289196663697a683d`；
- Code/Test Revision 后只允许 `project_doc` 更新；
- I003 生产和 JUnit 测试源码变更均为 0；
- 既有 `@Override` 独占一行；
- 脚本方法、mutation、恢复和重要校验使用中文注释。

PR #29 未执行合并；未经用户明确授权不得合并。`TASK-P1-T15` 保持 `BLOCKED_UNTIL_PR_29_MERGE`。
