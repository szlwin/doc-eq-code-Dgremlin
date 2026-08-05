# TASK-P1-T13 I002 独立 Review

- Code Review：`CODEREVIEW-P1-T13-R03@7d39c3bc0ab4`
- Iteration：`TASK-P1-T13 / I002`
- Code/Test Revision：`7d39c3bc0ab45b6cd3c8ab637c10ae40a15e07b8`
- Production Revision：`e2842eb888651858770202c560b1f4cd5932e7d7`
- Design：`DESIGN-R46@P1-T13-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R42@P1-T13-REWORK-I002`
- Result：`PASSED`
- Open P0/P1/P2：`0 / 0 / 0`
- Reviews：`REV-000672`～`REV-000692`

## Review conclusion

R01 重新 Review 发现的 P1/P2 已完成闭环：

- `FND-P1-T13-I002-001` — `CLOSED`；
- `FND-P1-T13-I002-002` — `CLOSED`。

`CODEREVIEW-P1-T13-R01@74672ee1367b` 与 `COMPLETION-P1-T13-R01@74672ee1367b` 保持 `INVALIDATED / PRESERVED`，不得恢复为当前事实。

## FND-P1-T13-I002-001 closure

生产代码只修改 `CompilerDigestService.java`：

- Source identity 不再使用宽松 `String.getBytes(UTF_8)`；
- 每次调用创建独立 `CharsetEncoder`；
- malformed 与 unmappable 均使用 `CodingErrorAction.REPORT`；
- `CharacterCodingException` 转换为稳定 `IllegalArgumentException`；
- message 固定为 `sourceId must contain valid Unicode`；
- cause 保留；
- 编码后的 `ByteBuffer.remaining()` 被复制为精确独立字节数组；
- 服务继续无状态，可安全并发复用。

兼容性 Review：

- `DEC-SOURCE-DIGEST-V1` domain 未变；
- Source count 和四字节大端长度前缀未变；
- Unicode code point 排序未变；
- content 原始字节未变；
- SHA-256 和小写 hex 未变；
- ASCII/BMP/supplementary 三组已知向量全部保持。

## FND-P1-T13-I002-002 closure

新增 FAILED transition Observer exception Oracle，证明：

- 最终状态仍为 FAILED；
- 原 `test.pass.error / ERROR` 保留；
- 新增 `MIX_OBSERVER_FAILURE / WARNING`；
- Observer exception 不传播；
- publisher 调用次数为 0；
- 失败 artifacts 为空。

Pipeline、CompilationSession 与 PipelineDiagnostics 无需生产修改，说明该 Finding 是缺失冻结证据而不是当前实现缺陷。

## Independent Review matrix

- 单独 high surrogate：PASSED / fail-closed；
- 单独 low surrogate：PASSED / fail-closed；
- malformed 位于首、中、尾：PASSED；
- 稳定 exception/message/cause：PASSED；
- 合法 surrogate pair：PASSED；
- ASCII/BMP/supplementary known vectors：PASSED；
- 失败后合法调用恢复：PASSED；
- 同一无状态服务 64 路并发复用：PASSED；
- Source 顺序与长度前缀：PASSED；
- FAILED Observer 原 ERROR/Warning/publisher/artifacts：PASSED；
- T12 Deadline/Cancel/Clock/Publication：PASSED；
- T14/T15 范围扫描：PASSED。

## Style and maintainability

- 所有 `@Override` 独占一行；
- strict UTF-8 helper、Encoder 生命周期、ByteBuffer 复制和异常转换均有中文注释；
- 无默认 Charset；
- 无共享 mutable Encoder；
- 无新依赖、反射或公共 API；
- 生产改动严格限定为一个文件。

## Validation

### Valid RED

- Head：`83c66072849c8017beb74adbb539820a15bb515e`
- Run：`31011478257` — `FAILURE / EXPECTED_RED`
- Artifact：`8932629734`
- SHA-256：`dff13bcc110615bf1648e2df535b3cd1149045851f5c3f2cbcb0cfa5e4a9642c`
- Compiler：483 tests / 3 expected failures / 0 errors。

### First GREEN

- Head：`e2842eb888651858770202c560b1f4cd5932e7d7`
- Run：`31011691306` — SUCCESS
- Artifact：`8932726363`
- SHA-256：`473c25ed28e6ab58ff29471f658390597d7aba6e4722d567df2377b8c6b3dfc9`

### Clean-code / Independent Review

- Head：`7d39c3bc0ab45b6cd3c8ab637c10ae40a15e07b8`
- Run：`31011874941` — SUCCESS
- Artifact：`8932801028`
- SHA-256：`679600735885f589a6370b0ad54845c909a24b2749b7b5edc4ac231822a8bf05`
- Surefire XML：106；
- T13：34/34；
- T12：133/133；
- Compiler：486/486；
- 正常测试：606/606；
- intentional failure：1 项按预期失败；
- Errors/Skipped：0/0；
- Java release 8、12 modules Reactor：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Review profiles

| Profile | Result |
|---|---|
| SpecComplianceReviewAgent | PASSED |
| EngineeringStandardsReviewAgent | PASSED |
| PerformanceReviewAgent | PASSED |
| TestEvidenceReviewAgent | PASSED |
| ArchitectureReviewAgent | PASSED |
| MaintainabilityReviewAgent | PASSED |
| SecurityReviewAgent | PASSED |

## Gate

I002 可以进入 Completion 文档阶段。Code/Test Revision 后只允许 `project_doc` 更新；未经用户授权不得合并 PR #28。
