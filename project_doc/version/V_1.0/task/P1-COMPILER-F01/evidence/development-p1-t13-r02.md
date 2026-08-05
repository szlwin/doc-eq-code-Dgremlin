# DEV-P1-T13-R02 — I002 Development Evidence

- Development：`DEV-P1-T13-R02@7d39c3bc0ab4`
- Evidence：`EVD-001020`～`EVD-001025`
- Iteration：`TASK-P1-T13 / I002`
- Architecture：`DEVSKEL-P1-T13-R02@83c66072849c`
- Production Revision：`e2842eb888651858770202c560b1f4cd5932e7d7`
- Code/Test Revision：`7d39c3bc0ab45b6cd3c8ab637c10ae40a15e07b8`

## Production change

唯一生产文件：

```text
dec-core-compiler/src/main/java/dec/core/compiler/compiled/CompilerDigestService.java
```

实现：

- 新增 private `strictUtf8(String value, String name)`；
- 每次调用创建独立 `CharsetEncoder`；
- malformed/unmappable 均 REPORT；
- 编码 `CharBuffer` 后复制 `ByteBuffer.remaining()`；
- `CharacterCodingException` 转换为稳定 `IllegalArgumentException`；
- Source identity 调用改为 `strictUtf8(sourceId, "sourceId")`。

## Test change

- `SemanticDigestIndependentReviewTest`：新增 high/low surrogate、稳定异常、合法 supplementary 与已知 vector；
- `CompilationObserverIndependentReviewTest`：新增 FAILED transition Observer exception 组合；
- `SemanticDigestStrictUnicodeI002ReviewTest`：新增位置排列、失败恢复和并发无状态 Review。

## Scope integrity

Production Revision 到 Code/Test Revision 仅新增独立 Review 测试文件；没有追加生产修改。未修改：

- `DocumentSource` 或其他 Source 模型；
- `CanonicalJsonWriter` / `SemanticDigestInput`；
- `CompilerPipeline` / `CompilationSession`；
- `ContextPublisher` / `PublicationRequest` / `EngineContext` CAS；
- Starter、T14、T15 或 P2～P7 runtime。

## Style

- 所有 `@Override` 独占一行；
- strict UTF-8、Encoder 生命周期、字节复制与异常转换使用中文注释；
- 无默认 Charset、共享 Encoder、新依赖或反射。
