# DEC_COMPILER Design R18 — T04 安全 XML Canonical Frontend

- Revision：`DESIGN-R18@P1-T04-I001`
- 任务：`TASK-P1-T04 / I001`
- 基线：`dev_all@df5e8c057d9aa8e3e477c54325bc476e7fdc5bee`
- 前置 Completion：`COMPLETION-P1-T03-R05@91271c9a1c20`
- 状态：`PASSED`

## 1. 目标

将 `DocumentFormat.XML` 的 `DocumentSource` 安全转换为 compiler-owned、格式中立且不可变的 `CanonicalDocumentNode`。Frontend 只发布名称、稳定排序属性、可选标量、文档顺序子节点、精确 `SourceRef`、格式和 schemaVersion；不得登记 TypedKey、业务默认值、Registry、EngineContext 或运行时对象。

## 2. 模块与依赖

- `dec-core-compiler` 继续拥有 `DocumentFrontend`、`FrontendOptions`、`FrontendResult`、`FrontendResults`、`CanonicalDocumentNode` 与 `DocumentFormat`；
- `dec-context-config-parse-xml` 新增对 `dec-core-compiler` 的单向生产依赖，并实现 `dec.core.compiler.canonical.xml.SecureXmlDocumentFrontend`；
- compiler 不得依赖 XML 模块、DOM4J、JAXP Schema、网络客户端或旧 Config 类型；
- 旧 XML 解析代码保持可编译，T04 新 Frontend 不调用 ConfigFactory、ConfigInfo、Registry 或全局配置。

## 3. Canonical 规则

1. 元素 local-name 作为节点 `name`，命名空间前缀不进入规范名称；
2. 属性使用 local-name 作为 key，按 `CanonicalDocumentNode` 的 key 顺序稳定发布；命名空间声明不作为业务属性；
3. 子元素保持文档顺序；
4. 元素直接文本与 CDATA 按出现顺序拼接，纯空白标量视为空；存在非空文本时保留文本内容但统一去除首尾空白；
5. 每个节点 `SourceRef` 指向 start tag 的 `<`，行列为 1-based，`nodePath` 使用 local-name 的完整绝对路径；同名兄弟不增加实现私有序号；
6. 根节点 schemaVersion 精确来自显式 `FrontendOptions`，所有后代继承同一值；
7. 成功结果为 `PARSED`、唯一 canonicalRoot、无 ERROR；失败结果为 `FAILED`、canonicalRoot 为空、至少一个 `MIX-FRONTEND-XML-UNSAFE` ERROR。

## 4. 安全边界

- 显式拒绝 `DOCTYPE`；
- 关闭 DTD、外部通用实体、外部参数实体和实体替换；
- 安装拒绝全部外部资源的 `XMLResolver`；
- 不创建 SchemaFactory；检测到 `xsi:schemaLocation` 或 `xsi:noNamespaceSchemaLocation` 时立即失败，不能把外部 schema 位置降级成普通业务属性；
- 不解析 XInclude；
- 只从 `DocumentSource.content()` 读取字节，不根据 URI、systemId、schemaLocation 或实体声明打开网络与文件；
- 解析器不支持关键安全属性时立即返回受控失败，不静默降级；
- XML 格式不匹配、null 参数、Malformed XML、重复 Canonical 属性 key、外部 schema、外部实体和 DTD 均返回无部分根的失败结果。

## 5. SourceRef 定位

StAX 的列号可能位于 start tag 末尾，不能直接发布。Frontend 使用原始 UTF-8 文本和 StAX character offset 反向定位当前 start tag 的 `<`，兼容 LF、CRLF、CR，并校验 qualified-name 的 local-name 与当前事件一致。

## 6. 测试 Oracle

- 合法 fixture 验证根、属性稳定顺序、标量、子节点顺序及每个节点精确 `SourceRef`；
- 命名空间前缀变化不改变 Canonical local-name；
- LF、CRLF、CR 均验证 start tag `<` 的位置；
- DOCTYPE、内部/外部实体、外部 schema、网络 systemId、file systemId、Malformed XML、错误格式全部失败；
- 网络和文件访问探针保持 0；
- FAILED 不暴露部分 Canonical；
- Canonical 对输入集合防御性复制且不持有 Parser/DOM 类型；
- 现有 Context、Compiler、legacy XML、完整 12 模块 Reactor 和 Java release 8 不回归。

## 7. 非目标

- 不实现 YAML Frontend（T05）；
- 不生成 RawDefinitionSet（T06）；
- 不登记 TypedKey、Symbol 或引用；
- 不执行 Compiler Pipeline、Digest 或发布；
- 不删除或迁移旧 XML 模块的全部 legacy API；
- 不修改 `dec-core-context` 生产代码。

## 8. 编码门禁

- 所有新增或修改的 `@Override` 独占一行；
- 公共方法、构造器和关键安全、定位、状态转换逻辑使用中文注释；
- Java release 8，不使用 Java 9+ API；
- 开放 P0/P1 阻断 Completion；
- 未经明确授权不得合并最终 PR。
