# P3 Information Engine 业务模型

- Revision：`BM-P3-R06`
- Base Revision：`REQAN-P3-R08`
- Flow Revision：`FLOW-R09@p3-information-evaluation`
- 状态：跨产物一致性修复候选，尚未发布；旧 `BM-P3-R05` Review 不证明本候选。

## 核心术语与所有权

`Information` 是可识别、可组合或作为模型表达式原子的业务事实；`InformationKey` 是 System 归属与本地名称组成的稳定身份。模型表达式只访问模型路径，Information 表达式只引用 InformationKey。

`MaterializationTargetFact` 是 P3 在配置编译期输出、供 Directory 消费的不可变目标事实，只描述目标 Information、`change-data`、路径、权限和物化规则约束。它不是 `ChangeInfo`，也不包含 `RuleViewInfo` 实例或映射。

`directory@information-ref` 是 Directory 的识别条件；`change-info@information-ref` 是 Directory 持有的物化目标声明。`Directory`、`ChangeInfo`、ChangeInfo 持有的 `RuleViewInfo` 映射，以及这些对象的生成、注册、不变量和生命周期均属于 Directory，不属于 Information model。后续 Directory 解析阶段消费 `MaterializationTargetFact`，完成映射校验和对象构建。

## 对象与聚合

- `Information`：按 RuleView 原子、模型表达式原子、复合表达式三类互斥建模。
- `ModelPath`：记录规范路径和 Data 归属；基础字段先解析 `target-main`，关系字段按自身 Data 解析，`orderDetailList` 属于独立 `orderDetail` Data。
- `InformationDependency`：记录 InformationKey 依赖边，发布前禁止缺失引用和循环。
- `MaterializationTargetFact`：记录可物化目标 Information、声明的 `change-data`、规范写路径、精确写权限和物化规则约束，供 Directory 编译使用。
- `IdentificationResult`：绑定本次模型/配置版本，状态只能为 `TRUE`、`FALSE`、`ERROR`。

`Information model` 聚合只保护 Information 定义、Key、路径、DAG 和 MaterializationTargetFact 的编译完整性。`Information evaluation` 聚合只保护只读识别结果和证据。Directory 聚合及其事务状态留给后续 Directory 阶段建模；P3 不为 ChangeInfo/RuleViewInfo 建立平行聚合或状态机。

## P3 业务不变量

1. Information 类型互斥，复合表达式不得引用模型路径。
2. 每次判断按 Information 配置重新读取当前值，不维护或消费 MutationSet。
3. 模型声明路径不存在、普通求值遇到 `null`、权限、表达式或只读 RuleView 执行失败均记录 `ERROR` 并抛出 `InformationEvaluationException`，不得降级为 `FALSE`、进入物化或继续下游；显式 `InformationKey = null` 比较是例外。
4. `every(emptyCollection, ...)` 为 `TRUE`，但订单相关 Information 必须同时满足订单明细非空。
5. 只有声明 `change-data` 的模型表达式原子可以产生 MaterializationTargetFact；目标 Information、`change-data`、路径和精确写权限必须在 P3 编译期完整且合法，否则候选配置不发布。

ChangeInfo/RuleViewInfo 的唯一映射、命名、生成、注册与生命周期不作为 P3 不变量；这些不变量由后续 Directory 解析阶段拥有并校验。

## 状态与下游执行契约

P3 识别依次经历 `REQUESTED → EVALUATING → TRUE/FALSE/ERROR`。复合 Information 按无环依赖拓扑求值并短路。P3 没有物化状态机，因为 P3 只发布目标事实；Directory 只负责解析映射并提供有序 Action，ModelContainer 承载后续执行和事务状态。

Directory 的下游契约为：

1. 配置编译期检查 `change-info` 目标引用、目标 Information 类型与 `change-data` 声明、路径/权限、RuleViewInfo 规则及 ChangeInfo 映射；任一非法配置均拒绝发布。
2. Directory 解析阶段为每个合法 ChangeInfo 生成、注册并映射一次名称为 `##` + `business-config.name` + `.` + `directory.name` + `.` + `change-info.information-ref` 的 RuleViewInfo；运行阶段只复用已编译映射。
3. Directory 按声明顺序提供 Action；每个 Action 携带 `refRule`。执行调用方按顺序把 `refRule` 传给 `ModelLoader.load(refRule, modelData, connection)`，将多个 Loader 装入同一个 ModelContainer；RuleViewInfo 由既有 `DataUtil.getRuleViewInfo(refRule)` → `ConfigInfo.getRuleViewInfo(refRule)` 链获取。
4. ModelContainer 在一次 `execute()` 中按 Loader 顺序执行；只有全部 Loader 成功后才完成 `grammer → update → commit` 并正常返回。任一 Action/规则/写入/持久化/commit 失败都停止后续 Loader，整体回滚、关闭和清理，并通过既有异常边界中断；不新增 `MaterializationResult` 或其它结果对象，`evaluate` 调用次数为 0，不识别或返回目标及下游结果。

当前范围无外部不可回滚副作用，不新增补偿、幂等、并发或独立事务语义；未来引入此类能力时必须另行确认并在 Directory 模型中补充。

## 业务错误与恢复

- P3 定义、依赖、目标类型、`change-data`、路径或权限非法：编译失败并拒绝发布候选配置，保留已发布事实。
- Directory 的 RuleViewInfo 规则或 ChangeInfo 映射非法：Directory 编译失败并拒绝发布候选配置，不生成可执行映射。
- P3 识别遇到非法路径、普通 `null`、权限、表达式或 RuleView 错误：记录 `ERROR`、抛出异常，禁止物化和下游。
- 配置编译遇到非法 ChangeInfo/RuleViewInfo 映射、缺失 Action.refRule 或不确定顺序：候选配置不发布，不创建 Loader 或事务。
- Action.refRule 装载、规则执行、change-data 写入、持久化或提交任一步失败：ModelContainer 记录首个失败 Loader/refRule，停止后续 Loader；对已建立连接回滚、关闭和清理，通过既有异常边界中断；本范围不定义外部补偿。

失败观察契约：失败必须能保留首个失败 Loader 的 `refRule`、Rule 名、`errorCode`、`errorName` 和 `errorMsg`；根因作为主异常，rollback/close 等收尾异常作为 suppressed 信息保留，不得被 `clear()` 覆盖。多连接只要求每个已建立连接按既有规则收到对应的 commit 或 rollback 及 close，不宣称跨物理连接的分布式原子性。验证事件至少包括 `LOAD(refRule)`、`EXECUTE_START`、`RULE_RESULT`、首失败后的 `EXECUTE_STOP`、`COMMIT` 或 `ROLLBACK`、`CLOSE`、`CLEAR` 和异常传播。

## 追踪

模型事实、`FLOW-P3-INFORMATION-EVALUATION` 和验收统一追踪到 `TR-P3-INFORMATION-ENGINE-001` / `AC-P3-INFORMATION-ENGINE-001`。该 revision 不修改 P2，不引入 MutationSet、reverse-DAG 缓存、独立事务或物化后自动重评估，也不提前拥有 P4 Action/Produce 或 P5/Directory 的对象和运行状态；Directory 只提供 Action/refRule，具体执行和事务由 ModelContainer 负责。
