# DESIGN-R46 — TASK-P1-T13 严格 Unicode Source 身份与 FAILED Observer 返工

- Revision：`DESIGN-R46@P1-T13-REWORK-I002`
- Status：`PASSED`
- Base：`PR28@9d180f2d34728cd453c377a6310b01fe1a7659cf`
- Dependency：`CODEREVIEW-P1-T13-R02-INVALIDATION@P1-T13-I002`
- Previous Design：`DESIGN-R45@P1-T13-I001` — `INVALIDATED / PRESERVED`
- Scope：`TASK-P1-T13 / I002`
- Excludes：T14 candidate Context/CAS；T15 Starter/旧模块退役；P2～P7 runtime

## 1. 目标

I002 只修复两个重新 Review Finding：

1. Source digest identity 在宽松 UTF-8 编码前发生的 malformed UTF-16 有损碰撞；
2. 缺少 FAILED 终态 Observer failure 的冻结 Oracle。

R45 其余通过合同继续继承，包括 canonical JSON、semantic digest 输入闭包、Timing、Deadline、Cancel、Publication 和 Observer fail-open 行为。

## 2. 严格 UTF-8 边界

`CompilerDigestService.sourceDigest()` 对每个排序后的 `DocumentSource.sourceId()` 执行严格 UTF-8 编码：

```text
StandardCharsets.UTF_8.newEncoder()
  .onMalformedInput(REPORT)
  .onUnmappableCharacter(REPORT)
```

约束：

- 不允许使用 `String.getBytes(UTF_8)` 处理 Source identity；
- `CharacterCodingException` 必须转换为稳定 `IllegalArgumentException`；
- message 固定为 `sourceId must contain valid Unicode`；
- cause 保留编码异常，便于诊断；
- 每次编码创建独立 Encoder，避免共享可变 CharsetEncoder 的线程安全问题；
- 返回字节必须是精确 remaining 长度的独立数组；
- Source domain、Source count、sourceId/content 四字节大端长度前缀和 SHA-256 算法保持不变。

该边界仅负责进入 `DEC-SOURCE-DIGEST-V1` 的原始 Source identity。R45 canonical JSON 会把未配对 surrogate 显式编码为 `\\udxxx`，该语义路径不存在替代字节碰撞，因此本迭代不重构 SourceRef、PublishedSourceDescriptor 或其他模型构造器。

## 3. 兼容性与已知向量

合法输入必须保持 I001 摘要兼容：

- ASCII：`sourceId=ascii, content=content`；
- BMP：`sourceId=U+E000, content=bmp`；
- supplementary：`sourceId=U+10000, content=supplementary`。

I002 测试固定三组已知 sourceDigest，证明严格 Encoder 没有改变合法 UTF-8 字节、排序、长度前缀或 digest domain。

## 4. malformed 输入合同

以下输入必须稳定 fail-closed：

- 单独 high surrogate：例如 `U+D800`、`U+D801`；
- 单独 low surrogate：例如 `U+DC00`、`U+DC01`；
- 合法 surrogate pair 必须继续成功。

不同 malformed sourceId 不再允许进入摘要计算，因此不得以替代字节形成相同 sourceDigest。

## 5. FAILED Observer Oracle

新增 Pass ERROR 路径：

1. 前置 Pass 返回原始 ERROR；
2. Pipeline 转换到 `FAILED`；
3. Observer 在 `FAILED` transition 回调中抛 `RuntimeException`；
4. Pipeline 捕获并追加 `MIX_OBSERVER_FAILURE / WARNING`；
5. Result seal 发生在 Warning 登记之后。

必须同时断言：

- `state=FAILED`；
- 原始 ERROR 的 code、severity、messageKey 保持；
- Observer Warning 只追加、不替换原 ERROR；
- exception 不传播；
- publisher count=0；
- artifacts 为空；
- 既有 transition/timing 顺序不变。

## 6. 生产改动边界

预计只修改：

- `CompilerDigestService.java`。

预计只新增/修改测试：

- `SemanticDigestIndependentReviewTest.java`；
- `CompilationObserverIndependentReviewTest.java`。

若 RED 证明需要额外生产改动，必须先更新 Design revision，不能静默扩展范围。

## 7. 停止条件

- R46/R42 未先于 RED 提交；
- strict Encoder 不是 REPORT/REPORT；
- malformed sourceId 被静默替换、忽略或规范化；
- 合法已知摘要向量变化；
- FAILED Observer Warning 改变原 ERROR、状态、publisher 或 artifacts；
- 修改 ContextPublisher、PublicationRequest、EngineContext CAS、Starter 或 T14/T15；
- Open P0/P1/P2 未清零；
- final documented P0、Artifact SHA/XML 独立校验与 Revision Integrity 未完成；
- 未经用户授权合并 PR #28。
