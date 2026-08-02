# DEC_COMPILER Design R11 — TASK-P1-T02 REWORK I004

## 1. 修订原因与事实源

独立 Review 对 `TASK-P1-T02/I003` 的 Source/Frontend 数据通路执行了完整闭包检查，确认当前公共 API 虽能注入 `DocumentSourceProvider` 与 `FrontendRegistry`，但无法把安全 Source 事实转换为 Canonical 解析产物：

1. `DocumentSource` 缠少 `uri`、`format`、`allowedRoot`；
2. `FrontendResult` 只有成功标志和 diagnostics，没有 Canonical 产物；
3. Test Oracle 未证明 Provider → Frontend → Canonical 的最小数据流可编译。

因此 `COMPLETION-P1-T02-R03@122ffc28165f` 被本次独立 Review 推翻并作为历史保留。本修订创建 `TASK-P1-T02/I004`，事实解释顺序为：

1. 本 R11 对 Source/Frontend 闭包的明确收敛；
2. `DESIGN-R10@P1-T02-REWORK-I003` 未被本修订改变的部分；
3. `DEC_COMPILER_api_contract.md`；
4. `DESIGN-R05@0b37a9b4dd48`；
5. 最终 T01 发布模型合同。

## 2. 目标

1. 冻结无需 T03 再修改的 Source 安全事实和值语义；
2. 冻结 Frontend 成功结果到 Canonical 根节点的完整数据闭包；
3. 失败结果不得携带 Canonical 候选，并且至少有一个 ERROR Diagnostic；
4. 不暴露 DOM、YAML Node、Parser Context 等具体 Frontend 实现类型；
5. 通过最小 Provider → Frontend → Canonical 编译与行为 Oracle 证明 T03 可直接接入；
6. 保持 Java release 8、实例级注入和 `dec-core-compiler` 仅依赖 `dec-core-context`。

## 3. DocumentSource 安全来源事实

`DocumentSource` 收敛为 final immutable value object，而不是无法约束实现的接口：

```java
public final class DocumentSource {
    public DocumentSource(
        String sourceId,
        URI uri,
        DocumentFormat format,
        AllowedRoot allowedRoot,
        byte[] content,
        String contentDigest);

    public String sourceId();
    public URI uri();
    public DocumentFormat format();
    public AllowedRoot allowedRoot();
    public byte[] content();
    public String contentDigest();
}
```

约束：

- `sourceId` 与 `contentDigest` 非空白；
- `uri` 必须是绝对 URI，并执行 `normalize()`；
- `format`、`allowedRoot`、`content` 均非 null；
- `allowedRoot` 必须包含当前 `uri`；
- 构造器和 `content()` 均执行字节数组防御性复制；
- `equals/hashCode` 基于完整来源事实和内容；
- `toString()` 不泄露原始文档内容，以内容长度和摘要表示 payload。

## 4. AllowedRoot

`AllowedRoot` 是 Provider 已执行来源策略判断后传递给 Compiler 的不可变根事实：

```java
public final class AllowedRoot {
    public AllowedRoot(URI uri);
    public URI uri();
    public boolean contains(URI candidate);
}
```

约束：

- 根 URI 必须绝对、规范化，且不允许 query 或 fragment；
- `contains` 要求 scheme、authority 一致，并使用规范化路径边界判断，避免字符串前缀误判；
- 本值对象只冻结规范化和词法根边界，不执行文件系统 IO 或符号链接解析；实际 Provider 仍负责真实路径与策略校验。

## 5. CanonicalDocumentNode

新增格式中立的 final immutable Canonical 节点：

```java
public final class CanonicalDocumentNode {
    public CanonicalDocumentNode(
        String name,
        Map<String, String> attributes,
        Optional<String> scalar,
        List<CanonicalDocumentNode> children,
        SourceRef sourceRef,
        DocumentFormat format,
        String schemaVersion);

    public String name();
    public Map<String, String> attributes();
    public Optional<String> scalar();
    public List<CanonicalDocumentNode> children();
    public SourceRef sourceRef();
    public DocumentFormat format();
    public String schemaVersion();
}
```

约束：

- 名称和 Schema 版本非空白；
- 属性按 key 稳定排序并冻结；key 非空白，value 非 null；
- scalar 使用非 null `Optional`，不以 null 表达缺失；
- children 防御性复制且不允许 null；顺序保持 Frontend 的文档顺序；
- `sourceRef.sourceId()` 必须与输入 `DocumentSource.sourceId()` 由 Frontend 保持一致，该跨对象约束在最小数据流 Oracle 中验证；
- 节点不包含任何 XML DOM、YAML Node 或第三方 Parser 类型。

## 6. FrontendResult 闭包

新增独立状态：

```java
public enum FrontendStatus {
    PARSED,
    FAILED
}
```

`FrontendResult` 冻结为：

```java
public interface FrontendResult {
    FrontendStatus status();
    Optional<CanonicalDocumentNode> canonicalRoot();
    List<Diagnostic> diagnostics();
}
```

使用 `FrontendResults` 统一创建并强制不变量：

```java
public final class FrontendResults {
    public static FrontendResult parsed(
        CanonicalDocumentNode canonicalRoot,
        List<Diagnostic> diagnostics);

    public static FrontendResult failed(
        List<Diagnostic> diagnostics);
}
```

约束：

- PARSED 恰好携带一个 Canonical 根节点；diagnostics 可包含 INFO/WARNING，但不得包含 ERROR；
- FAILED 的 `canonicalRoot()` 必须为 `Optional.empty()`，diagnostics 至少包含一个 ERROR；
- diagnostics 防御性复制、稳定排序且不可变；
- 工厂不返回 null；
- `DocumentFrontend.parse(DocumentSource, FrontendOptions)` 签名保持不变，T03 无需再修改公共方法。

## 7. 最小数据闭包

T02 必须能够用公共 API 编译并执行以下最小链路：

```text
SourceReference
  → DocumentSourceProvider.resolve(...)
  → SourceResolutionResult.sources()
  → DocumentSource.format()
  → FrontendRegistry.require(format)
  → DocumentFrontend.parse(source, options)
  → FrontendResult.canonicalRoot()
  → CanonicalDocumentNode
```

该链路只验证公共输入输出和不可变事实，不实现 T03 的 SourceGraph、MixSourceResolver、真实 XML/YAML Frontend、RawDefinitionSet 或 Pass Pipeline。

## 8. Test Oracle

TDD 必须直接冻结：

1. `DocumentSource` 是 final 值对象并包含六项构造输入和六个 accessor；
2. URI 规范化、AllowedRoot 边界、防御性内容复制和非法来源拒绝；
3. `CanonicalDocumentNode` 的完整字段、不可变集合和格式中立类型；
4. `FrontendStatus` 只包含 `PARSED|FAILED`；
5. `FrontendResult` 只暴露 `status/canonicalRoot/diagnostics`；
6. PARSED 恰有 Canonical 且无 ERROR；FAILED 无 Canonical 且至少一个 ERROR；
7. Provider → Frontend → Canonical 的最小数据流不使用 downcast、文件后缀猜测或隐藏映射；
8. `CompilerApiContractTest.REQUIRED_TYPES` 包含 `DocumentSource`、`AllowedRoot`、`FrontendResult`、`FrontendStatus`、`CanonicalDocumentNode`；
9. 既有 I003 请求、发布、结果和值语义测试全部保持通过。

## 9. 门禁与范围

- 任一开放 P0/P1 阻断 Completion；
- `FND-P1-T02-I003-004` 重新打开，并在 I004 以完整数据闭包 Oracle 关闭；
- I002、I003 的 Revision、Review、Evidence 和 Completion 均保留，不覆盖或删除；
- 不实现 T03 行为，不修改 `dec-core-context` 生产代码；
- 所有新增或修改的 `@Override` 独占一行；
- 方法、构造器和重要逻辑使用中文注释；
- 完整 12 模块 Reactor、Java 8、故意失败阻断门禁和 Context 回归必须通过；
- PR #17 在 I004 Completion 通过前保持 Draft；
- PR #17 合并前 `TASK-P1-T03` 继续保持阻断。
