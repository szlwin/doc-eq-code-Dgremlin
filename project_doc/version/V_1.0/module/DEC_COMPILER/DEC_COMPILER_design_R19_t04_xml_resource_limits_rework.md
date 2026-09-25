# DEC_COMPILER Design R19 — T04 XML 资源预算 Rework

- Revision：`DESIGN-R19@P1-T04-REWORK-I002`
- 任务：`TASK-P1-T04 / I002`
- 输入 Review：`REV-000207`
- 被推翻 Completion：`COMPLETION-P1-T04-R01@ba472906c719`（不可变历史保留）
- Rework base：`7edf31fca334cdd7e6342ed31b80e40d5bacb68d`
- 前置 Completion：`COMPLETION-P1-T03-R05@91271c9a1c20`
- 状态：`PASSED`

## 1. Finding 确认

R18 的 Canonical nodePath 为每个节点保留完整绝对 local-name 路径。I001 在每次 `START_ELEMENT` 上遍历全部祖先并重新构造路径，缺少深度、节点数和累计路径字符预算。极深但很小的 XML 因此可以产生二次方级路径字符量，并在显式失败前耗尽堆内存。该 Finding 为 P1 安全阻断项，R01 Completion 不再是当前有效 Completion。

## 2. 冻结生产预算

XML Frontend 使用不可变内部安全策略 `XmlFrontendLimits`。生产默认值冻结如下：

| 预算 | 默认值 |
| --- | ---: |
| `maxDocumentBytes` | 1,048,576 |
| `maxElementDepth` | 256 |
| `maxNodeCount` | 65,536 |
| `maxCumulativeNodePathChars` | 4,194,304 |
| `maxAttributesPerElement` | 256 |
| `maxDirectTextCharsPerElement` | 262,144 |
| `maxCumulativeDirectTextChars` | 1,048,576 |

这些预算是 T04 Canonical 可用性安全边界，不由 XML 内容、systemId 或全局状态覆盖。后续如需调整，必须创建新的 Design Revision，不得静默改变。

## 3. 门禁执行顺序

1. 取得 `DocumentSource.content()` 后、创建 StAX reader 前检查 `maxDocumentBytes`；
2. 每个 `START_ELEMENT` 上，在创建 `SourceRef`、属性 Map 和 `NodeBuilder` 前检查：
   - 新深度；
   - 新节点总数；
   - 当前元素属性数；
   - 新 nodePath 长度及累计 nodePath 字符数；
3. 只有全部检查通过后，使用父节点已经冻结的 nodePath 加一次 `"/" + localName` 构造当前路径；禁止再次遍历完整祖先栈；
4. 追加 `CHARACTERS`、`CDATA` 或 `SPACE` 前检查当前元素直接文本长度和全文件累计直接文本长度；
5. 任一预算超限立即抛出内部受控失败，并映射为：
   - `status = FAILED`；
   - `diagnostic = MIX_FRONTEND_XML_UNSAFE`；
   - `canonicalRoot = empty`；
   - 外部 I/O 探针保持 0。

显式预算负责在危险分配前终止。不得通过构造真实 OOM 或捕获 `OutOfMemoryError` 证明安全。

## 4. 路径和定位复杂度

- nodePath 构建不再遍历 `List<NodeBuilder>`；每个节点只基于父路径进行一次有预算的字符串构造；
- `StartTagLocator` 每个事件只定位一次；
- 行号定位使用二分查找，避免多行多节点输入出现线性重复扫描；
- 深度上限 256 同时约束后续递归 `equals/hashCode/toString` 的栈风险。

## 5. 可测试策略

生产公共构造器始终使用冻结默认预算。同包测试可通过 package-private 构造器注入更小的 `XmlFrontendLimits`，只用于确定性边界 Oracle，不改变公共 API，也不把测试策略暴露给业务调用方。

直接 Oracle 必须覆盖：

- 所有限制边界内成功；
- 文档字节、深度、节点数、累计 nodePath、属性数、单节点直接文本和累计直接文本分别超限失败；
- 每种失败均无部分 root、无外部访问；
- 测试输入保持小型，不制造 OOM。

## 6. P2 合同收敛

- `xsi:schemaLocation` 与 `xsi:noNamespaceSchemaLocation` 继续按照 Design R18 立即失败；R14 中“作为普通属性”的相反描述被 R15 明确取代；
- 增加直接 Oracle 验证普通文本—CDATA—普通文本的顺序拼接；
- 根、子节点和孙节点继承同一 `schemaVersion`；
- `source == null` 与 `options == null` 返回稳定 FAILED、`MIX_FRONTEND_XML_UNSAFE`、空 root。

## 7. 保持不变的安全与范围

- DOCTYPE、内部实体、外部通用实体、外部参数实体和外部 schema 均拒绝；
- 网络、文件、SchemaFactory 和 XInclude 外部访问保持 0；
- XML 模块继续单向依赖 compiler canonical API；
- 不修改 `dec-core-context` 生产代码；
- 不实现 T05、RawDefinitionSet、Symbol 或 Compiler Pipeline；
- `@Override` 独占一行，公共方法、构造器和关键安全逻辑使用中文注释；
- 开放 P0/P1 阻断 Completion；未经明确授权不得合并 PR #19。
