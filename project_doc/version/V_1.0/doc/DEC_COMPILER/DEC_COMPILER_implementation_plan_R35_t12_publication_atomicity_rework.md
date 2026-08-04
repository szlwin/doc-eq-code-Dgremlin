# TP-P1-COMPILER-F01-R35 — TASK-P1-T12 I002 Publication 原子性返工计划

- Revision：`TP-P1-COMPILER-F01-R35@P1-T12-REWORK-I002`
- Design：`DESIGN-R39@P1-T12-REWORK-I002`
- Status：`PASSED`
- Base：`PR27@49b9beee65dbc5e5db77302a7128a34a2ab77386`
- Invalidated Completion：`COMPLETION-P1-T12-R01@c6a515820972`

## Sequential workflow

1. 将 PR #27 转为 Draft，并在 PR 描述中标记 I001 Completion 已失效、I002 返工中。
2. 保留 R38/R34、I001 RED/Architecture/Review/Completion、CI 与 Artifact，不覆盖历史文件。
3. 冻结 R39/R35，记录 first commit/blob，且必须早于 I002 有效 RED。
4. 新增 I002 Task、独立 Review 输入和 R01 invalidation 记录，Open P0/P1/P2=`0/3/2`。
5. 建立 Publication 专用 Pass/Context 与 artifact snapshot 的可编译架构骨架，不先修复 Pipeline 行为。
6. 新增 `CompilerPipelineReworkI002Test`，形成有效 RED；errors=0，失败仅对应五个新 Findings。
7. Architecture Review 验证普通 Context 不持有 publisher、Context 生命周期、Session 终态封闭、结果值快照和 commit 原子边界。
8. 实现前九 Pass 与 PublicationPass 分离、publisher 一次调用、commit 后不可降级、clock/token 独立异常边界和真实 executedPass 记录。
9. 更新原有 T12 Oracle 与测试夹具，确保 I001 20 项历史行为仍全部成立。
10. 首轮 GREEN 后执行独立 Review，补充 conflict/null result/null status/double publish、mutable artifact、retained Context 和 post-commit fault Oracle。
11. 运行 I002 定向测试、T12 全量、T11～T07 回归、Compiler 模块全量、12 模块 `clean verify`、Java 8 与故意失败门禁。
12. 下载 Artifact，独立计算 SHA-256 并解析全部 Surefire XML。
13. 形成 clean-code Head；其后只写 I002 Evidence、Review、Revision Lock、Completion、handoff/resume。
14. 对 final documented Head 再运行 P0 和 Artifact 校验。
15. 更新同一 PR #27 并标记 Ready for Review，不创建替代 PR、不自动合并；合并前 T13 保持阻断。

## Acceptance gates

- 普通 PassContext 不能访问 PublicationRequest 或 ContextPublisher；
- 前九 Pass publisher 调用能力为 0；
- 成功路径 publisher 调用数精确为 1；
- 任一发布前 ERROR/cancel/timeout/clock/token/异常路径 publisher 调用数精确为 0；
- publish 返回 PUBLISHED 后状态不可降级；
- retained Context 在关闭、PUBLISHED 或 FAILED 后全部读写拒绝；
- 第二次 execute 不改变第一次结果；
- Result 不保存 Session，并冻结全部返回事实；
- mutable container artifact 被递归快照；
- start-clock 失败时 executedPasses/timings/真实调用数为 0；
- Open P0/P1/P2=`0/0/0`；
- 所有 `@Override` 独占一行，公开方法和重要逻辑使用中文注释；
- Java 8、全 Reactor、故意失败门禁和 Artifact Evidence 通过。

## Validation

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineReworkI002Test test

./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineOrderTest,CompilationSessionStateTest,SessionIsolationTest,CompilerPipelineIndependentReviewTest,CompilerPipelineReworkI002Test test

./mvnw -pl dec-core-compiler -am test
./mvnw clean verify
```
