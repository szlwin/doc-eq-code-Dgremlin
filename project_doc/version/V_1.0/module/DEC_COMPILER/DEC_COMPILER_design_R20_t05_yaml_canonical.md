# DEC_COMPILER Design R20 — T05 安全 YAML Canonical Frontend

- Revision：`DESIGN-R20@P1-T05-I001`
- 任务：`TASK-P1-T05 / I001`
- Base：`dev_all@09edf814bdf0800e7e9633545ca743200169b377`
- 前置 Completion：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- 输入主计划：`TP-P1-COMPILER-F01-R01@88b56e6caa64`
- 输入测试设计：`TESTDESIGN-R01@ba7779cf089b`
- 状态：`PASSED`

## 1. 目标与边界

T05 在 `dec-context-config-parse-yaml` 中新增 `dec.core.compiler.canonical.yaml.SafeYamlDocumentFrontend`，实现 compiler-owned `DocumentFrontend`。Frontend 只把调用方提供的 YAML `DocumentSource` 转换为格式中立、不可变的 `CanonicalDocumentNode`，不得创建旧 Config、Registry、EngineContext、RawDefinitionSet、Symbol 或运行时对象。

本 Revision 只完成 YAML Canonical Frontend。`TASK-P1-T06` 的 RawDefinitionSet、TypedKey、引用解析和 Compiler Pipeline 均不在范围内。

## 2. YAML 到 Canonical 的冻结映射

YAML 文档必须恰好包含一个 document，且 document root 必须是只含一个字符串 key 的 Mapping。该 key 是 Canonical 根节点名称，其 value 是根节点内容。

节点内容规则：

1. Scalar：形成当前节点的可选 scalar；首尾空白 trim，空串和 YAML null 不发布 scalar；
2. Mapping：
   - `"@attributes"` 是保留 key，value 必须是字符串 key 到 scalar value 的 Mapping；
   - `"#text"` 是保留 key，value 必须是 scalar/null，形成当前节点直接 scalar；
   - 其他 key 是子节点名称；
3. 子节点 value 为 Mapping 或 Scalar/null 时生成一个子节点；
4. 子节点 value 为 Sequence 时，Sequence 中每个 Scalar/null/Mapping item 生成一个同名重复子节点；
5. Sequence 不得作为 document root、节点 body 或 Sequence item；
6. Mapping key 必须是显式字符串 Scalar，复杂 key、空白 key 和重复 key均失败；
7. `<<` merge key、`@attributes`/`#text` 的错误类型和保留 key 冲突均失败。

属性由 `CanonicalDocumentNode` 按 key 稳定排序，普通子节点保持 YAML Mapping/Sequence 的文档顺序。YAML 注释不进入 Canonical。

## 3. Scalar 与 Tag 安全合同

Frontend 使用 SnakeYAML 安全表示树，不调用通用对象加载入口。只允许以下标准节点/标量 tag：

- Mapping：`tag:yaml.org,2002:map`；
- Sequence：`tag:yaml.org,2002:seq`；
- Scalar：`str`、`bool`、`int`、`float`、`null`、`timestamp`。

以下输入必须在发布任何 Canonical root 前失败：

- 任意 Java/object tag，例如 `!!com.example.Type`；
- 任意 local/custom tag，例如 `!custom`；
- binary、set、omap、pairs 和 merge 等非冻结 tag；
- anchor、alias、共享节点和递归结构；
- 显式对象构造或调用任意用户类型构造器。

所有 YAML 安全失败统一返回：

- `status = FAILED`；
- Diagnostic code = `MIX_FRONTEND_YAML_UNSAFE`；
- `canonicalRoot = empty`；
- 不实例化任意用户类型；
- 不访问网络、文件系统或全局配置。

## 4. SourceRef 合同

- `sourceId` 来自 `DocumentSource.sourceId()`；
- line/column 使用 SnakeYAML `Mark` 的零基位置加一，形成一基位置；
- 根节点和普通 Mapping 子节点指向声明 key 的首字符；
- Sequence 重复子节点指向每个 item 的起始位置；
- nodePath 为完整 Canonical 路径，例如 `/root/child`；
- 同名重复兄弟共享同一 nodePath，但 line/column 各自不同；
- 失败位置无法确定时使用非负回退位置和 `/`。

SourceRef 只保存来源事实，不保存 SnakeYAML `Node`、`Mark` 或 parser 私有对象。

## 5. 冻结生产预算

YAML Frontend 使用 package-private 不可变策略 `YamlFrontendLimits`。生产值冻结如下：

| 预算 | 默认值 |
| --- | ---: |
| `maxDocumentBytes` | 1,048,576 |
| `maxCodePoints` | 1,048,576 |
| `maxNestingDepth` | 128 |
| `maxNodeCount` | 65,536 |
| `maxCumulativeNodePathChars` | 4,194,304 |
| `maxMappingEntriesPerNode` | 256 |
| `maxSequenceItemsPerNode` | 4,096 |
| `maxScalarCharsPerNode` | 262,144 |
| `maxCumulativeScalarChars` | 1,048,576 |
| `maxAliasesForCollections` | 0 |

生产公共构造器始终使用这些值。调整生产预算必须创建新的 Design Revision，不得由 YAML 内容、system property、全局单例或调用方静默覆盖。

## 6. 资源门禁顺序

1. 取得 `DocumentSource.content()` 后、创建 SnakeYAML parser 前检查文档字节；
2. `LoaderOptions` 在 compose 阶段限制 code point、nesting depth、duplicate key、recursive key、alias 和 tag；
3. Canonical 遍历在分配节点前检查节点数、当前路径长度、累计路径字符、当前 Mapping entry 数和 Sequence item 数；
4. 保存 scalar 前检查单节点及累计 scalar 字符；
5. 所有累计值使用 `long` 溢出安全加法；
6. 任何预算失败映射为 `FAILED / MIX_FRONTEND_YAML_UNSAFE / empty root`；
7. 测试使用可注入的小型预算，不制造真实 OOM，也不捕获 `OutOfMemoryError`。

最大 nesting depth 128 同时约束后续 Canonical 递归 `equals/hashCode/toString` 的栈风险。

## 7. XML/YAML Canonical 等价

同语义 XML/YAML fixture 必须生成相同的：

- 节点名称；
- 稳定属性；
- scalar；
- 子节点顺序与重复节点数量；
- schemaVersion；
- 完整 nodePath。

格式和物理位置属于来源事实，分别保持 `DocumentFormat.XML` / `DocumentFormat.YAML` 及各自 sourceId、line、column，因此等价 Oracle 比较语义树，不直接使用包含格式和物理 SourceRef 的对象 `equals`。

不得通过比较序列化文本、parser 私有对象或原始字节摘要代替语义树比较。

## 8. 参数、文档与失败合同

以下情况稳定失败且不携带部分 root：

- `source == null`；
- `options == null`；
- `source.format() != YAML`；
- 空文档、多 document、非 Mapping root、多 root key；
- malformed YAML；
- 复杂 key、重复 key、保留 key 类型错误；
- 非允许 tag、anchor/alias/recursive graph；
- 任一资源预算超限。

## 9. 架构与编码门禁

- YAML 模块生产依赖 compiler canonical API，compiler 不反向依赖 YAML 模块；
- 不修改 `dec-core-context` 生产代码和 compiler canonical 公共 API；
- Frontend 不持有旧 Config、Registry、EngineContext、DOM 或 SnakeYAML Node 的已发布引用；
- 保持 Java release 8；
- 所有 `@Override` 独占一行；
- 公共方法、构造器以及 tag、资源、SourceRef、映射和失败边界使用中文注释；
- 开放 P0/P1 阻断 Completion；未经明确授权不得合并 T05 PR。
