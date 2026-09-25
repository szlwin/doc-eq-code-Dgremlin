# DEC Compiler Design R14 — T03 Reference Identity Rework

- Revision：`DESIGN-R14@P1-T03-REWORK-I002`
- 前置有效基线：`DESIGN-R13@P1-T03-I001`
- 被推翻 Completion：`COMPLETION-P1-T03-R01@713848bfa65e`
- 触发 Review：`REV-000152`

## 1. 重开原因

独立 Review 确认三个规格缺口：

1. `SourceReference` 仅 trim，未形成统一 canonical key；opaque `classpath:` URI 的 `.` 段会进入 Provider、SourceGraphEdge、重复键、排序和相等性。
2. 环路栈保存 `sourceId`，却与引用文本比较；`sourceId != uri` 时不能在 Provider 调用前识别环路。
3. StAX `Location.column` 是 start tag 处理后位置，不是声明元素 `<` 的起始列。

## 2. 统一引用身份

`SourceReference.value()` 冻结为 canonical reference key：

- 构造时保留 `..`、query、fragment 和非法 URI 文本，使 `SourcePolicy` 仍能在规范化前拒绝安全违规；
- 只消除路径中的独立 `.` 段；
- 对绝对 URI 的 scheme 使用小写；
- 同时支持 hierarchical URI、opaque URI 和相对路径；
- 不执行 IO、real-path、符号链接或网络解析。

canonical key 必须统一用于：

- Provider `resolve/resolveFileSet` 调用；
- `SourceGraphEdge.targetReference`；
- edge duplicate key；
- ancestor reference stack；
- 排序、图相等性和后续 Digest 输入。

## 3. 身份域分离

- `sourceId`：Manifest 唯一性、Diagnostic Source 身份、重复 Source 检测。
- canonical reference key：Provider 访问、声明边目标和当前解析路径环路检测。
- 两个身份域不得互相替代，也不要求相等。

每次递归前，ancestor stack 只保存 canonical reference key；命中环路时必须在 Provider 调用前返回 `MIX-SOURCE-POLICY`，不得退化为 `MIX-SOURCE-DUPLICATE-ID`。

## 4. 声明起始位置

声明解析器必须基于原始 UTF-8 文本定位当前 start tag 的 `<`：

- StAX 继续负责安全 XML tokenization 和节点路径；
- 以 `Location.characterOffset` 为上界向后定位当前 start tag；
- 失败时按 Location 行范围回退定位；
- 将 `<` 的 1-based 行、列写入 `SourceRef`；
- 无法精确定位时阻断解析，禁止使用错误的标签末尾位置静默降级。

## 5. 验收 Oracle

- canonical 与 `/./` 引用生成相同图；
- Provider 只登记 canonical key 时 `/./` 声明仍成功；
- 等价文本声明被识别为 `source.edge.duplicate`；
- `sourceId != uri` 的环路在递归 Provider 调用前阻断，access count 不增加，返回 `MIX-SOURCE-POLICY`；
- 7 条固定声明边的 line、column、nodePath 与原始文本 `<` 精确一致；
- 原 10 Source / 7 Edge、安全策略、资源预算和不可变性合同保持通过。

## 6. 范围边界

- 不修改 `dec-core-context` 生产代码；
- 不实现 real-path、符号链接解析或网络访问；
- 不启动 T04 Canonical Frontend；
- Java release 8；
- 所有 `@Override` 独占一行，方法和关键逻辑使用中文注释。
