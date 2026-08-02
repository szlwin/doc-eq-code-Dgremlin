# DEC Compiler Design R15 — T03 Canonical Edge Rework

- Revision：`DESIGN-R15@P1-T03-REWORK-I003`
- 前置有效基线：`DESIGN-R14@P1-T03-REWORK-I002`
- 被推翻 Completion：`COMPLETION-P1-T03-R02@6af43b47f044`
- 触发 Review：`REV-000163`

## 1. 重开原因

独立 Review 确认三个边界缺口：

1. 仅由当前目录段组成的相对引用 `.`、`./`、`./.` 被规范化为空字符串，破坏 `SourceReference` 非空值对象合同，并可能在 `SourcePolicy` 前泄漏未分类异常。
2. 独立百分号编码当前目录段 `%2e` 未进入 canonical key，导致等价引用仍可能形成不同 Provider 行为、边身份、重复键、图和环路身份。
3. 声明定位实现声明支持 LF、CRLF、CR，但缺少 CRLF/CR 的直接可执行 Oracle。

## 2. SourceReference 非空不变量

`SourceReference` 构造前后均必须保持非空：

- 原始输入 trim 后为空仍抛出既有 `IllegalArgumentException`；
- canonicalization 后不得产生空字符串；
- 对仅由当前目录段和分隔符组成的相对引用，统一冻结为 `.`；
- `.`、`./`、`./.` 必须产生相同 canonical key `.`；
- 该相对 key 由 `SourcePolicy` 按“非绝对 URI”映射为 `MIX-SOURCE-PATH-ESCAPE`；
- Resolver 必须返回稳定 `FAILED`、空 graph，且 Provider 访问次数为 0。

## 3. 编码当前目录段

canonicalization 必须逐个 raw path segment 判断，不得先解码整个 URI：

- raw segment 解码一次后恰好为 `.`：删除；
- raw segment 解码一次后为 `..`：保留原始 segment，交由 `SourcePolicy` 拒绝；
- `%2e` 与 `%2E` 均视为当前目录段；
- `.`, `%2e` 的混合若解码后为 `..`，必须保留，不能被删除；
- `%2F` 等编码分隔符不得改变 segment 边界；
- 其他编码内容保持原始 raw 文本。

该规则同时适用于：

- opaque URI，例如 `classpath:mix/system/%2e/systems.xml`；
- hierarchical URI，例如 `file:///mix/system/%2e/systems.xml`；
- 相对路径。

统一 canonical key 必须继续用于 Provider、Edge、duplicate key、sorting、graph equality、Digest 输入和 cycle stack。

## 4. Resolver 受控失败边界

- 根 `SourceRef` 创建、策略验证、Provider 判空和 Discovery 必须置于 Resolver 的受控失败边界内；
- 合法构造的 `SourceReference` 不得因 canonical key 为空导致异常泄漏；
- 非绝对根引用必须稳定返回 `MIX-SOURCE-PATH-ESCAPE`；
- 编程错误参数 `root == null`、`policy == null` 仍保持显式参数异常合同。

## 5. 换行位置 Oracle

使用同一固定 root/systems XML，分别转换为：

- LF；
- CRLF；
- CR。

每种换行形式均验证：

- 精确 10 Source / 7 Edge；
- 7 条边的 line、column、nodePath；
- column 指向 start tag 的 `<`；
- 换行形式不改变图拓扑和引用身份。

## 6. 验收 Oracle

- `SourceReference(".")`、`SourceReference("./")`、`SourceReference("./.")` 均为 `.`；
- 三种根引用均返回 `FAILED / MIX-SOURCE-PATH-ESCAPE / graph empty / provider access 0`，且不抛未分类异常；
- opaque `%2e` 与无点段、字面量 `.` 形成同一 key 和同一图；
- hierarchical `file:` `%2e` 与无点段形成同一 key；
- Provider 只登记 canonical key 时编码引用成功；
- 编码等价声明被识别为 `source.edge.duplicate`；
- 编码等价引用不能绕过 cycle stack；
- CRLF、CR 的全部 7 条声明位置直接通过。

## 7. 范围边界

- 不修改 `dec-core-context` 生产代码；
- 不解码完整 URI 或编码分隔符；
- 不实现 real-path、符号链接或网络访问；
- 不启动 T04；
- Java release 8；
- 所有 `@Override` 独占一行，方法、构造器和重要逻辑使用中文注释。
