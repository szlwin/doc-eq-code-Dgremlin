# TP-P1-COMPILER-F01-R42 — TASK-P1-T13 I002 实施计划

- Revision：`TP-P1-COMPILER-F01-R42@P1-T13-REWORK-I002`
- Design：`DESIGN-R46@P1-T13-REWORK-I002`
- Status：`PASSED`
- Base：`PR28@9d180f2d34728cd453c377a6310b01fe1a7659cf`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- Previous Plan：`TP-P1-COMPILER-F01-R41@P1-T13-I001` — `INVALIDATED / PRESERVED`

## Sequential workflow

1. 复核 PR #28 仍 Open、Ready、未合并并锁定 Head；
2. 登记 R01 Review/Completion 为 `INVALIDATED / PRESERVED`，保留全部 I001 历史；
3. 冻结 R46/R42，必须早于 I002 有效 RED；
4. 创建 `TASK-P1-T13-I002`，Open P0/P1/P2=`0/1/1`；
5. 在不修改生产代码的前提下新增 malformed high/low surrogate、稳定异常、合法 supplementary 和三组已知 digest vector Oracle；
6. 新增 Pass ERROR + FAILED transition Observer exception Oracle；
7. 运行 P0，要求新增 malformed Oracle 精确失败、FAILED Observer Oracle为控制通过；
8. 冻结 `DEVSKEL-P1-T13-R02`，明确 strict Encoder 线程安全、异常与兼容边界；
9. 仅修改 `CompilerDigestService`，使用每次调用独立的 UTF-8 CharsetEncoder(REPORT/REPORT)；
10. 所有 `@Override` 独占一行；方法及编码、异常、长度前缀和 Observer 断言使用中文注释；
11. 执行定向 T13、Compiler、T12 回归、全 Reactor、Java 8 与故意失败门禁；
12. 独立 Review 增加 malformed sourceId 全排列、known vector、并发重复计算和 FAILED Observer 组合复核；
13. Open P0/P1/P2 必须清零；
14. 独立下载 clean-code Artifact，核对 ZIP SHA-256、Surefire XML 和测试统计；
15. 登记 Development、Review、Testing、Revision Lock、Completion、Handoff、Resume；
16. clean-code Revision 后只允许 `project_doc` 更新；
17. 对 final documented Head 重跑 P0 和 Artifact 独立解析；
18. 更新 PR #28 标题、正文与 Completion Review，不执行合并；T14 继续阻断。

## TDD matrix

### Source identity

- 两个不同单独 high surrogate 均稳定拒绝；
- 两个不同单独 low surrogate 均稳定拒绝；
- 异常类型与 message 固定；
- 合法 supplementary sourceId 正常计算；
- ASCII/BMP/supplementary 三组 sourceDigest 已知向量保持；
- 顺序无关和长度前缀既有测试继续通过。

### FAILED Observer

- 使用 `PipelineTestSupport.failingPasses(..., failureIndex)` 触发原始 ERROR；
- Observer 仅在 `to=FAILED` 的 transition 抛异常；
- state 仍 FAILED；
- 原始 ERROR 与 Observer Warning 同时存在；
- publisher=0；
- artifacts empty；
- exception 不传播。

## Validation

```text
./mvnw -pl dec-core-compiler -am -Dtest=SemanticDigestIndependentReviewTest,CompilationObserverIndependentReviewTest test
./mvnw -pl dec-core-compiler -am test
./mvnw --batch-mode --no-transfer-progress clean verify
```

MySQL 未被 I002 修改，继续 `SKIPPED_NOT_APPLICABLE`。

## Stop conditions

- RED 不是可编译且原因精确；
- 生产改动超出 `CompilerDigestService`；
- 修改合法 digest vector；
- Observer failure 从 FAILED 路径传播；
- 原 ERROR 被 Warning 替换或降级；
- P0/Artifact/Revision Integrity 未完成；
- PR #28 被合并或 T14 被提前启动。
