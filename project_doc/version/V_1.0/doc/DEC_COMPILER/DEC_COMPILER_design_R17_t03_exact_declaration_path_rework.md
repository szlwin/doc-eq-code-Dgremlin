# DEC_COMPILER Design R17 — T03 完整声明路径边界

## Revision

- Revision：`DESIGN-R17@P1-T03-REWORK-I005`
- Task：`TASK-P1-T03 / I005`
- Supersedes：`DESIGN-R16@P1-T03-REWORK-I004` 的声明路径未闭包部分
- Rework base：`d41b4553189b4b9e80a7ca92c4acd34e4fc97e42`

## 问题事实

`SourceDeclarationParser` 当前使用最后两级元素名识别声明，无法证明声明位于 R13 冻结的完整 XML 路径。错误 root、忽略子树和错误 systems 路径中的同名元素可能被登记为真实 `SourceGraphEdge`。

## 冻结合同

### Root 文档

只允许以下完整 local-name 路径产生边：

- `/orm-config/orm-data-file-info/orm-file`
- `/orm-config/orm-view-file-info/orm-file`
- `/orm-config/system-file-info/system-file`
- `/orm-config/business-file-info/business-file`

文档根必须是 `orm-config`。其它路径中的同名元素全部忽略。忽略后缺失或重复四类 root 声明，由既有 `RootDeclarations` 映射为 `MIX_SOURCE_POLICY`。

### Systems 文档

只允许以下完整 local-name 路径产生边：

- `/systems/system/rule-file-info/rule-file`

文档根必须是 `systems`。其它路径中的 `rule-file` 全部忽略；没有任何合法 rule 声明属于不完整结构，必须映射为 `MIX_SOURCE_POLICY`。

## 安全边界

1. 不根据 XML 路径后缀识别声明；
2. 不在忽略节点中创建 Source 边；
3. 错误 root 在根文档解析后不得访问 data、view、systems 或 business Provider；
4. 错误 systems 结构不得访问 rule 或 business Provider；
5. 任何失败不发布部分 `SourceGraph`；
6. DTD、外部实体、外部资源关闭策略保持不变；
7. namespace prefix 不进入身份，继续使用 StAX local name；
8. 不修改 `dec-core-context` 生产代码，不实现 T04。

## 实现决策

- 使用精确元素栈比较函数替代 `endsWith()`；
- 在首个 `START_ELEMENT` 冻结并验证文档根 local name；
- root 完整性继续由 `RootDeclarations` 统一验证，避免双重规则；
- systems 至少存在一条合法完整路径 rule 声明，否则抛出内部受控 `SourceDeclarationException`；
- Resolver 继续把解析异常映射为稳定 `MIX_SOURCE_POLICY` 失败结果。

## 验收

- wrong-root、wrong-nesting、ignored-subtree、wrong-systems-root、wrong-system-path Oracle 全部通过；
- 合法 fixture 仍为 10 Source、7 Edge、8 次 Provider 调用；
- Context 与既有 Compiler 测试零回归；
- Java release 8、12 模块 Reactor 和失败阻断通过；
- 开放 P0/P1 为 0 后方可形成 Completion R05。
