# DEC_COMPILER DESIGN R50 — TASK-P1-T14 / I003 TDD Evidence Repair

- Design ID：`DESIGN-R50@P1-T14-REWORK-I003`
- Iteration：`TASK-P1-T14 / I003`
- Status：`FROZEN`
- Base Head：`932e12352d71deaf8fd1e3dc88dfaa0ed0ed0fc9`
- Review Gate 输入：`NEEDS_CHANGES / Open P0/P1/P2 = 0/1/1`

## 1. 问题确认

I002 登记的 `TDD-P1-T14-R02@1df0a14f2a74` 无效：对应 Run 在 `maven-compiler-plugin:testCompile` 阶段失败，Compiler 测试没有执行，Artifact 中没有 T14 测试记录。因此该 Run 不能证明 request mismatch、任意 Digest 注入或 provenance 未绑定的行为缺陷。

当前生产实现、最终 Test Oracle 和最终 GREEN 均已通过 Review，但 GREEN 不能替代缺失的有效 RED。I003 必须保留历史并重建真实、可复现的负向证据。

## 2. 生命周期处理

以下记录只标记为 `INVALIDATED / PRESERVED`，不得删除、覆盖或改写原文件：

- `TDD-P1-T14-R02@1df0a14f2a74`；
- `CODEREVIEW-P1-T14-R03@668d865b0189`；
- `COMPLETION-P1-T14-R02@668d865b0189`。

I003 新建 TDD、Architecture、Development、Code Review、Testing 和 Completion revision。I001、I002 的 Git 历史和 Artifact 保持原样。

## 3. TDD_REPAIR / ORACLE_HARDENING 策略

由于当前正确生产实现已经存在，I003 不伪造“生产代码之前的 RED”，而使用可复现 mutation proof 验证当前 Oracle 能阻断原缺陷。

Mutation proof 必须满足：

1. 测试源码和生产源码均成功编译；
2. 目标 JUnit 测试实际执行；
3. 失败类型必须是 assertion failure，不得是 testCompile、ClassNotFound 或环境失败；
4. 每个 mutation 必须只暂时破坏一个已确认的生产门禁；
5. mutation 执行后必须恢复工作树并再次运行原测试得到 GREEN；
6. mutation XML、完整日志和机器可读摘要必须进入 CI Artifact；
7. 正常 Surefire 报告必须恢复为 GREEN，不能把 mutation failure 混入正常测试统计。

## 4. Mutation A — Request Binding

临时将 `CandidateContextPublicationPass` 的 request schema/options 校验短路，使 mismatch candidate 能继续进入 prepare/publish。

必须执行：

`CandidateContextT14Test#requestMismatchFailsWithExactDiagnostic`

期望：

- Java 编译成功；
- 测试实际执行 1 项；
- 测试因状态、publisher 次数、artifact 或 Diagnostic 断言发生 behavior failure；
- Errors=0；
- Maven 返回非零。

这直接证明 Oracle 能捕获上一轮的“request mismatch 仍进入 PUBLISHED”缺陷。

## 5. Mutation B — Source Closure Binding

临时跳过 `DigestBoundCompiledInput.bind()` 中 raw/published Source identity 闭包一致性校验。

必须执行：

`CandidateContextT14IndependentReviewTest#sourceManifestClosureMismatchFailsClosed`

期望：

- Java 编译成功；
- 测试实际执行 1 项；
- 测试因未抛出预期异常而发生 behavior failure；
- Errors=0；
- Maven 返回非零。

这证明 Oracle 能捕获跨编译 SourceManifest 拼接缺陷。

## 6. 生产边界

I003 不修改 T14 生产语义。以下 I002 生产合同保持：

- atomic `DigestBoundCompiledInput`；
- Builder 只接受 provenance-bound input；
- request schema/options 二次门禁；
- Registry 完整快照；
- Pipeline 唯一持有 Publisher/CAS capability；
- 稳定 Diagnostic identity；
- Java release 8。

## 7. CI 与证据

P0 Workflow 在正常 `clean verify` 成功后运行 T14 mutation proof：

- mutation 报告输出到 `dec-core-compiler/target/t14-mutation-proof/`；
- Artifact 上传该目录；
- mutation 完成后恢复源码并重跑两个目标测试为 GREEN；
- 之后继续执行 intentional failure gate。

最终需独立核验：

- mutation A/B 各有 1 个已执行的 assertion failure、0 error；
- 正常 T14、T13、T12、Compiler 和全 Reactor 全绿；
- intentional failure 仍是唯一正常门禁 Failure；
- Code/Test revision 后仅允许 `project_doc` 更新。

## 8. PR Traceability

PR #29 正文必须更新为 I003，包含：

- 当前 Head、Base 和 revision chain；
- I002 失效记录；
- mutation proof Run/Artifact/SHA；
- 最终 P0 与正常测试统计；
- Finding 状态和 `Open P0/P1/P2 = 0/0/0`；
- 明确 `NOT_MERGED`，T15 继续阻断。

## 9. 编码规范

- 新增脚本的方法、校验步骤和重要逻辑使用中文注释；
- Java 若有新增 `@Override` 必须独占一行；
- 不引入新依赖、反射、sleep、wall-clock 或共享可变测试状态；
- mutation 只能作用于 CI 临时工作树，不能提交被破坏的生产源码。
