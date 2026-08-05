# TDD-P1-T13-R02 — I002 有效 RED

- TDD ID：`TDD-P1-T13-R02@83c66072849c`
- Iteration：`TASK-P1-T13 / I002`
- Design：`DESIGN-R46@P1-T13-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R42@P1-T13-REWORK-I002`
- Head：`83c66072849c8017beb74adbb539820a15bb515e`
- P0 Run：`31011478257`
- Result：`FAILURE / EXPECTED_RED`
- Artifact：`8932629734`
- Artifact SHA-256：`dff13bcc110615bf1648e2df535b3cd1149045851f5c3f2cbcb0cfa5e4a9642c`

## RED result

- `SemanticDigestIndependentReviewTest`：13 tests / 3 failures / 0 errors；
- `CompilationObserverIndependentReviewTest`：5/5 passed；
- Compiler records before build stop：483；
- failures：3；errors：0；skipped：0。

精确失败：

1. `malformedHighSurrogateSourceIdsFailClosed`；
2. `malformedLowSurrogateSourceIdsFailClosed`；
3. `malformedSourceIdUsesStableFailure`。

三项均为：期望 `IllegalArgumentException`，但当前宽松 UTF-8 编码没有抛出异常。

## Controls

以下新增控制均通过：

- 合法 supplementary Source ID 已知摘要向量；
- ASCII/BMP/supplementary 三组 sourceDigest 已知向量；
- FAILED transition Observer exception 保留原 ERROR、FAILED、publisher=0、empty artifacts。

所有 I001 canonical JSON、semantic digest、Timing、Deadline、Cancel、Observer success/failure 和 T12 Publication 回归在本次 RED 中未出现额外失败。

## Gate

该运行是可编译、原因精确、仅命中新生产缺口的有效 RED。生产实现前不得修改 RED Oracle。
