# DEC Compiler Design R12 — T02 Source Policy Closure

- Revision：`DESIGN-R12@P1-T02-REWORK-I005`
- 输入：`DESIGN-R11@P1-T02-REWORK-I004`、`DESIGN-R10@P1-T02-REWORK-I003`、`DEC_COMPILER_api_contract.md`、`BM-R05@4ecb1f8c09f4`
- 任务：`TASK-P1-T02 / I005`
- 状态：`PASSED`

## 1. 目标

在不实现 T03 Pipeline 的前提下，冻结不会因 URI 规范化扩大安全根、不会遗漏 opaque URI query、并能区分单 Source 与文件集解析的最终公共合同。T03 必须能够直接调用该合同验证第三方 Provider 返回值，无需再次修改 T02 公共 API。

## 2. AllowedRoot 安全验证顺序

`AllowedRoot` 继续支持层次 URI 和 `classpath:` 等 opaque URI，但必须遵循以下不可交换顺序：

1. 对原始 URI 执行非空和绝对 URI 检查；
2. 在调用 `URI.normalize()` 前检查原始 raw location 与解码 location；
3. 拒绝字面量或百分号编码后形成的独立 `..` 路径段；
4. 层次 URI 通过 `getQuery()/getFragment()` 拒绝 query 和 fragment；
5. opaque URI 额外检查 raw 与解码 scheme-specific part 中的 `?`，因为 `URI.getQuery()` 对 opaque URI 返回 null；
6. 只有原始输入通过安全检查后才能规范化；
7. 对规范化结果再次执行相同安全检查，避免实现差异引入新的非法结构。

构造器遇到非法根时抛出 `IllegalArgumentException`。`contains(...)` 对非法候选返回 false，不向 Compiler 泄漏预期的来源策略异常。

## 3. URI location 规则

- 层次 URI raw location 使用 `getRawPath()`，解码 location 使用 `getPath()`；
- opaque URI raw location 使用 `getRawSchemeSpecificPart()`，解码 location 使用 `getSchemeSpecificPart()`；
- 路径比较统一反斜杠为 `/`，并只在安全验证通过后移除非根位置的尾部 `/`；
- scheme 与 authority 必须相同；
- `/config` 与 `/config/` 是等价边界，但 `/configuration` 不是 `/config` 的后代；
- 原始输入只要包含显式父目录穿越，即使规范化后重新落回根内，也必须拒绝。

## 4. SourceResolution 成功工厂

移除语义不明确的：

```java
resolved(List<DocumentSource>, List<Diagnostic>)
```

冻结两个明确工厂：

```java
public static SourceResolutionResult resolvedSingle(
        DocumentSource source,
        List<Diagnostic> diagnostics);

public static SourceResolutionResult resolvedFileSet(
        List<DocumentSource> sources,
        List<Diagnostic> diagnostics);
```

不变量：

- `resolvedSingle` 恰好携带一个非 null Source；
- `resolvedFileSet` 至少携带一个非 null Source；
- 成功 diagnostics 不得包含 ERROR；
- Source 按 `sourceId` 稳定排序；
- 任意重复 `sourceId` 都是合同违规，即使 URI、format 和 digest 相同也不得静默去重；
- 同一 `sourceId` 对应不同 URI、format 或 contentDigest 同样必须明确拒绝；
- 所有集合均防御性复制并不可变。

`failed(...)` 继续要求不携带 Source 且至少包含一个 ERROR。

## 5. 第三方 Provider 防御性验证

仅依赖工厂不足以约束第三方自定义 `SourceResolutionResult`。因此冻结：

```java
public static SourceResolutionResult validateSingle(
        SourceReference reference,
        SourceResolutionResult result);

public static SourceResolutionResult validateFileSet(
        SourceReference reference,
        SourceResolutionResult result);
```

验证器行为：

- 对合法 RESOLVED 结果重新复制、排序并返回规范化不可变结果；
- 对合法 FAILED 结果重新复制并保持无 Source；
- 对 null result、null status、null 集合、null 元素、错误基数、重复 sourceId、成功结果含 ERROR、失败结果含部分 Source、失败结果无 ERROR 等违规，统一返回 FAILED；
- 合同违规 FAILED 至少包含一个 `DiagnosticCode.MIX_SOURCE_POLICY`、`DiagnosticSeverity.ERROR`；
- 违规转换不得携带任何部分 Source 候选；
- 验证器不依赖具体 Provider 实现，不抛出预期的 Provider 合同异常。

T03 调用顺序冻结为：

```text
provider.resolve(...)        → validateSingle(reference, result)
provider.resolveFileSet(...) → validateFileSet(reference, result)
```

## 6. Test Oracle

必须覆盖：

- 根 `file:///workspace/config/..` 拒绝；
- 根 `file:///workspace/config/../secret` 拒绝；
- 根 `classpath:config/?raw=true` 拒绝；
- opaque 候选 `classpath:config/root.xml?raw=true` 返回 false；
- 编码 traversal 根与候选拒绝；
- 单源成功工厂只产生一个 Source；
- 文件集至少一个且稳定排序；
- 重复 sourceId 拒绝，包括不同 digest/URI/format；
- RESOLVED 含 ERROR 拒绝；
- FAILED 无 ERROR 拒绝；
- 第三方单源返回 0 或 2 个 Source 转为 `MIX-SOURCE-POLICY`；
- 第三方文件集重复 sourceId 转为 `MIX-SOURCE-POLICY`；
- 第三方 FAILED 携带部分 Source 转为无候选 FAILED；
- Java release 8、完整 12 模块 Reactor 和故意失败阻断门禁继续通过。

## 7. 范围边界

本 Revision 不实现文件系统 real-path、符号链接解析、网络访问、SourceGraph、MixSourceResolver、真实 XML/YAML Frontend、RawDefinitionSet 或 Compiler Pipeline。真实 IO 与策略授权仍由 Provider 负责，T02 只冻结不可变事实和防御性公共合同。
