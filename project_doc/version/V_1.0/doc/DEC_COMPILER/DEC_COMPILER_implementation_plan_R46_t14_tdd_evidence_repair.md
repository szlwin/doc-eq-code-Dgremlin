# DEC_COMPILER IMPLEMENTATION PLAN R46 — TASK-P1-T14 / I003

- Plan ID：`TP-P1-COMPILER-F01-R46@P1-T14-REWORK-I003`
- Design：`DESIGN-R50@P1-T14-REWORK-I003`
- Status：`FROZEN`

## Step 1 — Invalidate and preserve

- 新建 I003 invalidation 记录；
- 将 `TDD-P1-T14-R02@1df0a14f2a74`、`CODEREVIEW-P1-T14-R03@668d865b0189`、`COMPLETION-P1-T14-R02@668d865b0189` 标记为 `INVALIDATED / PRESERVED`；
- 不修改、删除或覆盖 I001/I002 原证据；
- 当前 Gate 维持 `NEEDS_CHANGES / 0/1/1`，直到 I003 Completion 成立。

## Step 2 — Architecture review

- 复核当前 atomic provenance、request binding、Registry snapshot 和 publication capability 边界；
- 确认 I003 不需要生产架构修改；
- mutation harness 只能作为 CI 测试能力，不能进入生产包；
- 冻结 `DEVSKEL-P1-T14-R03`。

## Step 3 — TDD_REPAIR mutation proof

新增可复现脚本，依次执行：

1. 临时短路 request mismatch 门禁；
2. 运行 `CandidateContextT14Test#requestMismatchFailsWithExactDiagnostic`；
3. 验证 1 test / 1 assertion failure / 0 error；
4. 恢复源码；
5. 临时跳过 raw/published Source closure 门禁；
6. 运行 `CandidateContextT14IndependentReviewTest#sourceManifestClosureMismatchFailsClosed`；
7. 验证 1 test / 1 assertion failure / 0 error；
8. 恢复源码并重跑两个目标测试为 GREEN。

禁止将 testCompile、测试未执行或环境失败登记为 mutation proof。

## Step 4 — P0 integration

- 在正常 `clean verify` 后增加 mutation proof step；
- Artifact 上传 `target/t14-mutation-proof`；
- 保持原 intentional failure gate；
- 不改变 MySQL workflow 语义。

## Step 5 — Review and validation

- 独立读取 mutation 日志和 XML；
- 验证 mutation A/B 均为 behavior failure；
- 验证恢复后的目标测试为 GREEN；
- 验证 T14、T13、T12、Compiler、全 Reactor 和 Java release 8；
- 验证最终 Artifact SHA-256、正常 Surefire 统计和 mutation proof 统计；
- 验证 Code/Test revision 后仅有 `project_doc` 变化。

## Step 6 — Completion and PR

- 新建 TDD R03、Architecture R03、Development R03、Code Review R05、Testing R03 和 Completion R03；
- 新建 Revision Lock、Completion report 和版本 completion JSON；
- 更新 TASK、handoff、resume context；
- 更新 PR #29 正文到 I003；
- PR 保持 Open、Ready for Review、Not Merged；
- `TASK-P1-T15` 保持阻断。

## Style

- Java release 8；
- 新增 Java `@Override` 独占一行；
- 方法、mutation、恢复和证据校验逻辑使用中文注释；
- 不引入新依赖、反射、sleep、wall-clock 或共享可变测试状态。
