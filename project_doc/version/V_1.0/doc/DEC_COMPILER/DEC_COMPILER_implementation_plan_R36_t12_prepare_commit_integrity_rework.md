# TP-P1-COMPILER-F01-R36 — TASK-P1-T12 I003 最终门禁与事实保真返工计划

- Revision：`TP-P1-COMPILER-F01-R36@P1-T12-REWORK-I003`
- Design：`DESIGN-R40@P1-T12-REWORK-I003`
- Status：`PASSED`
- Base：`PR27@749d010e47fe23f283d119a48a7904ebcf0f64d2`
- Invalidated Completion：`COMPLETION-P1-T12-R02@5d5a7d72119b`

## Sequential workflow

1. 将 PR #27 转为 Draft，并将 R02 标记为失效历史、I003 返工中。
2. 保留 R38～R39、R34～R35、I001/I002 RED、Architecture、Review、Completion、CI 与 Artifact，不删除、不覆盖。
3. 冻结 R40/R36，记录 first commit/blob，且必须早于 I003 有效 RED。
4. 新增 I003 Task、Review invalidation 与独立 Review 输入，Open P0/P1/P2=`0/2/3`。
5. 新增能在 I002 Head 编译的 I003 行为 Oracle，确认失败仅命中五项新 Finding。
6. Architecture Review 冻结 Publication prepare/commit、Clock/timing 结果边界、start timestamp Deadline 和 artifact collision 语义。
7. 将 `PublicationPassContext` 改为无 publisher 的候选准备 Context；Pipeline 独占 `PublicationRequest` 和 commit。
8. 最终 Pass 返回后先聚合全部 Diagnostic，再执行最后基础设施门禁和 publisher 调用。
9. 增加普通与最终 Pass 的 start timestamp Deadline 复核和溢出安全 timing 登记。
10. 增加 Map/Set 冻结后 equality collision 检测，禁止静默覆盖或去重。
11. 更新 I001/I002 测试夹具到 prepare/commit API，不削弱原历史 Oracle。
12. 首轮 GREEN 后执行独立 Review，补充 warning 保留、publication prepare 生命周期、极值 Clock、identity Map/Set 与异常边界 Oracle。
13. 运行 I003 定向、T12 全量、Compiler 全量、12 模块 `clean verify`、Java 8 与故意失败门禁。
14. 下载 Artifact，独立计算 SHA-256 并解析全部 Surefire XML。
15. 形成 clean-code Head；其后只写 I003 Evidence、Review、Revision Lock、Completion、handoff/resume。
16. 对 final documented Head 再运行 P0 和 Artifact 校验。
17. 更新同一 PR #27 并标记 Ready for Review；不创建替代 PR、不自动合并，T13 保持阻断。

## Acceptance gates

- PublicationPassContext 不持有 PublicationRequest 或 ContextPublisher；
- final Pass 所有 Context/PassResult Diagnostic 在 publisher 前完整可见；
- final ERROR、取消、超时、Clock/timing 故障、异常时 publisher=0；
- final WARNING/INFO 在成功结果中保留；
- 成功路径 publisher=1，PUBLISHED 后 Observer 故障不降级；
- 任意 long start/end 组合不能让异常越过 Pipeline；
- preflight 未到期但 start timestamp 到期时 Pass 调用数、executedPasses、timings、publisher 均为 0；
- Map/Set 冻结后 equality collision 稳定 fail-closed；
- I001/I002 原有 54 项 T12 Oracle 不回归；
- Open P0/P1/P2=`0/0/0`；
- 所有 `@Override` 独占一行，方法与重要逻辑使用中文注释；
- Java 8、全 Reactor、故意失败门禁和 Artifact Evidence 通过。

## Validation

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineReworkI003Test test

./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineOrderTest,CompilationSessionStateTest,SessionIsolationTest,CompilerPipelineIndependentReviewTest,CompilerPipelineReworkI002Test,CompilerPipelineReworkI002IndependentReviewTest,CompilerPipelineReworkI002HardeningTest,CompilerPipelineReworkI003Test test

./mvnw -pl dec-core-compiler -am test
./mvnw clean verify
```
