# FEATURE-DESC-4AB41AC241A1 P3 Information Engine 需求确认

> 文档职责：RequirementConfirmationAgent 在需求确认阶段创建并维护第 1～4、12、14 节；RequirementAnalysisAgent 在需求分析阶段补全第 5～13 节。需求文档描述“必须表现为什么”，不得把表名、字段名、API 路径、类名或框架方案作为需求结论；必要的现状证据可引用具体实现，但技术方案进入设计文档。

> 文档导航：[项目文档首页](../../../../docs/README.md) · [版本摘要](../../version_summary.md) · [本版本需求列表](../../requirement_list.md)

## 1. 需求信息

| 项目 | 内容 |
|---|---|
| 需求编号 | P3-INFORMATION-ENGINE |
| 需求名称 | P3 Information Engine 需求确认 |
| 版本目标编号 | FEATURE-DESC-4AB41AC241A1 |
| 需求类型 | 架构能力、信息识别与物化 |
| 主责模块 | dec-core-model、dec-core-context、dec-core-compiler |
| 协作模块 | dec-context-config-parse-xml、dec-demo、P2 System/RuleView 能力 |
| 受影响角色 | 配置作者、规则开发者、业务编排者、测试人员、维护人员 |
| 当前状态 | 需求确认完成，待重新分析 |
| 对应变更需求编号 | FEATURE-DESC-4AB41AC241A1 |

### 1.1 阅读摘要

| 读者最关心的问题 | 当前结论 |
|---|---|
| 为什么要做 | 当前框架没有 Information 一等事实、依赖图、可区分的识别结果或物化语义；`mix` 只能作为目标示例，无法被统一编译和消费。 |
| 做到什么算有价值 | `mix` 的 16 个 Information 能按归属、表达式类型和依赖关系一致识别；每次判断都按 Information 配置读取当前模型值，物化结果可验证且错误不会伪装成 FALSE。 |
| 本次明确不做 | 不处理 P2 文档/治理事实不一致；不实现 Action/Produce、Directory、Query、Transaction/Session 或现代 YAML，也不复活 `dec-expand-declaration`/Consumer runtime；`materialize` 成功后不自动或作为流程步骤调用只读 `evaluate`。 |
| 如何判断完成 | 以第 9 节 `AC-P3-INFORMATION-ENGINE-*` 的可观察结果为准 |
| 仍需谁做决定 | 业务语义已确认；Information 专项测试在功能实现后由测试设计/开发阶段补充，本阶段不要求已有测试证据。 |

## 2. 背景与问题

### 2.1 当前行为与证据

当前 `dec-demo/src/main/resources/mix` 已提供可读的目标契约，但入口注释明确提示现有解析器未必支持 `system-file-info`、`business-file-info` 等元素。`systems.xml` 定义四个 System（user、order、payment、common）及 16 个 Information；`orm-view.xml` 提供 UserInfo/OrderInfo，后三个 System 通过显式 model/view 归属使用共享模型。RuleView 位于三个业务 System 的规则文件中，`business/order-business.xml` 仅引用限定的 Information，不拥有 Information。

以 `mix` 为事实样例，Information 数量与分类如下：

- user：`activated`、`certified` 为 RuleView 原子，`effective` 为 `user.activated AND user.certified` 复合（3）。
- order：`waitPay` 为 RuleView 原子；`ordered`、`paying`、`paySuccessStatus`、`payErrorStatus` 为只访问 OrderInfo 模型的 `rule-data` 原子，并分别声明状态 1/2/3/4 的 `change-data`；`payable` 为 `ordered OR waitPay` 复合（6）。
- payment：`paymentInfo`、`hasResult`、`success`、`error` 为 RuleView 原子；`completed` 为 `success OR error` 复合（5）。
- common：`paySuccess` 为 `payment.success AND order.paySuccessStatus`，`payError` 为 `payment.error AND order.payErrorStatus`，均为跨 System 复合（2）。

样例同时给出关键引用事实：`rule-ref` 必须解析到所属 System 的 RuleView；`rule-data/change-data` 只读写其 `model-ref` 指向的模型路径；`expression` 只引用 Information Key；`common` 不直接拥有模型、View 或 RuleView。

现有执行链证据（仅用于界定 P3 接入边界）：`dec-demo/src/test/java/dec/demo/model/RuleTests.java` 展示先加载 `save-Order`/`back-Order` 规则，再调用 `ModelContainer.execute()` 并断言结果；`dec-core-model/src/main/java/dec/core/model/container/ModelContainer.java` 与 `dec-core-model/src/main/java/dec/core/model/execute/tran/TransactionContainer.java` 已负责规则执行、成功提交、异常回滚、结果回写和资源清理。因此 P3 物化只规定 `change-data` 写入值及失败时抛出的 `ERROR`，直接复用现有执行链。

`model-access` 解析证据：`dec-core-compiler/src/main/java/dec/core/compiler/raw/RawDefinitionBuilder.java` 的 systems grammar 接受 `read`/`write` 下的 `ref`；`dec-core-compiler/src/main/java/dec/core/compiler/modelaccess/ModelAccessCompiler.java` 仅遍历显式 `ref` 生成 Binding，旧式 `<read path="*"/>` 或 `<write path="status"/>` 没有 ref 时不会生成可用 Binding；`DefaultModelAccessSelectorResolver` 已实现 selector 先匹配 View `target-main`、再精确回退 property path。该差异属于 P2 model-access 兼容事实，P3 只消费已发布 Binding，不在本需求中改写 XML 解析器。

### 2.2 需要解决的问题

1. 缺少统一的 Information 解析、编译和识别契约，RuleView 判断、模型表达式判断和跨 System 组合无法被同一调用方可靠消费。
2. 旧 Change 语义与目标 `change-data` 不等价；Information 判断还需要明确从配置路径读取模型值的规则，避免把旧缓存或推测值当成当前结果。

## 3. 需求目标

1. 建立互斥的 RuleView 原子、模型表达式原子和复合 Information 三类业务事实；严格分离只访问模型的 Model Expression 与只访问 Information Key 的 Information Expression。
2. 为每次识别提供 TRUE/FALSE/ERROR 结果及可审计证据、依赖结果、读路径和模型版本；本框架按数据已准备好的正常前提执行，不引入数据未加载、依赖未解析或动态暂不可判定的 UNRESOLVED 运行态；识别不得隐式修改模型，ERROR 不得降级为 FALSE。
3. 建立无环 Information 依赖图和稳定的模型路径/依赖描述；每次判断按依赖拓扑读取并计算，不维护 MutationSet 或 reverse-DAG 运行时失效缓存。
4. 仅允许声明 `change-data` 的模型表达式原子执行受保护物化；成功提交并返回物化结果后流程结束，不自动重识别目标或下游；复合 Information 不可直接物化。

## 4. 范围

### 4.1 范围内

- P3 Information 的 Raw/Compiled 事实、归属与限定 InformationKey；以 `mix/system/systems.xml` 的 16 个定义为完整 fixture 基线。
- `system`/`model`/`rule`/`rule-data`/`change-data`/`expression` 的解析与互斥校验；RuleView 原子识别、模型表达式识别和复合短路。
- 无环 DAG、拓扑顺序、模型路径/依赖描述和稳定图摘要；结果证据、trace、诊断和模型版本。
- `change-data` 的权限检查、原子修改、提交/回滚结果；每次判断实时按 Information 配置读取模型值，不维护 MutationSet 或增量失效缓存；对 P4/P5 的调用只保留稳定 InformationEngine 事实边界。

### 4.2 范围外

- P2 System/RuleView/model-access 的事实修订、治理投影修复或旧任务重写；P3 只消费已发布的 P2 归属和路径权限事实。
- Action、Produce、Directory 状态机/Back、Query/SQL、事务与 Session、外部服务执行、现代 YAML 对等、数据库迁移以及任何生产代码实现。
- 独立 Consumer runtime、Producer/Consumer SPI、旧 Directory Change 作为 Information 替代物，以及 `dec-expand-declaration` 的 Adapter 或代码复用。
- `materialize` 成功提交后的自动重评估、目标重识别或下游 Information 重识别；后续识别只能由调用方另行发起普通只读 `evaluate`，不属于物化流程及其返回结果。

### 4.3 约束与依赖

- P1 编译产物提供可解析的 System/View/Rule/Information Deferred 事实；P2 提供 `(system, RuleView)`、model-ref 与读写路径权限，P3 不改变其治理事实。
- `rule-data`/`change-data` 的模型路径必须可由模型表达式编译器解析；`expression` 必须由 Information 表达式编译器解析，二者语法和 AST 不得互用。
- Information DAG 必须在发布前完成循环、缺失引用、跨 System 归属和非法组合校验；任一错误阻止该配置的 P3 Information 发布。
- 模型声明路径不存在必须为 `ERROR`；运行时读取到 `null` 默认也为 `ERROR`，唯一例外是显式 Information 空值比较（如 `payment.success = null`），该比较按布尔条件求值。
- `every(emptyCollection, ...)` 固定为 `TRUE`；订单相关 Information 另加“订单明细集合非空”前置条件。明细为空时订单 Information 返回 `FALSE`（可附 `ORDER_DETAIL_REQUIRED` 诊断），避免空明细订单被识别为已下单/支付中/成功/失败。
- `model-access` 的当前解析事实：`read`/`write` 只有携带显式 `<ref view="..." property="..."/>` 才会生成可用 Binding；旧式无 `<ref>` 简写不会形成有效授权，因此 P3 不将其作为兼容输入。`read path="*"` 仍可使用，但必须配合显式 `<ref>`。
- `ref@property` 解析先区分大小写精确匹配目标 View 的 `target-main`，未命中时才在同一 View 的 property 树中逐段精确查找；`OrderInfo.target-main="order"` 因此优先绑定 `order` 根目标。
- View 属性取值先按 `target-main` 对应的基础 Data 解析基础字段，例如 `OrderInfo.status` 对应 `order` Data 的 `orderStatus`；关系属性按其自身 `data` 归属解析。`OrderInfo.orderDetailList` 的 `data="orderDetail"`，不视为 `order` 基础 Data 的字段或其从属属性。
- 物化沿用当前框架已有的原子化与异常回滚机制；P3 只定义应写入的值和失败结果，不新增事务承载、幂等或并发控制要求。
- Java 8 与现有 `mix` XML fixture 是工程约束；需求确认阶段不得修改生产代码、测试代码或 P2 事实文件。

## 5. 功能列表

> 一个需求至少包含一个功能。单功能需求只保留一行；多功能需求按稳定功能编号逐行登记。功能可以跨模块，但必须明确主责模块和协作模块。

| 功能编号 | 功能名称 | 主责模块 | 协作模块 | 关联流程 | 功能说明 | 状态 |
|---|---|---|---|---|---|---|
| P3-INFORMATION-ENGINE-F01 | Information Engine 需求基线 | dec-core-model、dec-core-context、dec-core-compiler | dec-context-config-parse-xml、dec-demo | FLOW-P3-INFORMATION-EVALUATION | 确认 mix 16 个 Information 的分类、表达式隔离、识别、物化、DAG 与实时读取边界 | 已确认 |

## 6. 功能详细需求

> 每个功能必须有独立小节。复制下面的小节即可扩展为多功能需求；不得把多个可独立验收的功能混写在一个小节。

### 6.1 P3-INFORMATION-ENGINE-F01 Information Engine 需求基线

#### 6.1.1 功能目标

使 `mix` 中的 Information 成为可编译、可识别、可组合、可物化且按当前模型值实时求值的统一业务事实，并为 P4/P5 提供稳定的只读消费边界。

#### 6.1.2 角色与权限

- 配置作者声明 Information 归属、模型引用、规则引用和表达式；System 只可使用自身授权的 View/RuleView 与模型读写路径；复合 Information 不获得模型写权限。

#### 6.1.3 前置条件

- P1/P2 已发布可解析的 System、View、RuleView、model-ref 和路径权限事实；`mix` 的 10 个 XML 文件可被发现并解析。

#### 6.1.4 正常流程

1. 解析并校验 16 个 Information，按三类互斥形式建立限定 Key 和模型路径/依赖描述。
2. 分别编译 Model Expression 与 Information Expression，构建无环依赖图。
3. 在每次判断时按 Information 配置实时读取当前模型值，先识别原子 Information，再按依赖拓扑短路计算复合 Information，返回 TRUE/FALSE/ERROR 结果及证据。
4. 对声明 `change-data` 的模型表达式原子执行受保护物化；提交成功后返回变更路径与提交结果并结束，不自动调用 `evaluate` 或识别目标及下游节点。

#### 6.1.5 业务规则

- BR-P3-INFORMATION-ENGINE-001：Information 只能是 RuleView 原子、模型表达式原子或只组合 Information 的复合类型之一；混合配置、缺失引用、循环和越权写入均为配置/编译错误。
- BR-P3-INFORMATION-ENGINE-002：模型声明路径不存在或运行时普通求值遇到 `null` 返回 `ERROR`；显式 `InformationKey = null` 比较是唯一例外；`every(emptyCollection, ...)` 为 TRUE，但订单相关 Information 必须同时满足明细非空，明细为空时返回 FALSE 并可附 `ORDER_DETAIL_REQUIRED` 诊断。
- BR-P3-INFORMATION-ENGINE-003：Information 每次判断都按其配置的模型路径和依赖重新读取当前值；读取必须使用当前数据/事务上下文可见的最新值，必要时允许从数据库重新读取，不维护或消费 MutationSet。基础字段先解析到 `target-main` 对应 Data，关系字段再按自身 Data 解析。

#### 6.1.6 输入与输出约束

- 输入：限定的 InformationKey、已发布模型上下文和模型版本；表达式不得跨语言引用。每次判断根据 Information 配置重新读取所需模型路径，不接收或维护 MutationSet。
- 输出：`TRUE/FALSE/ERROR` 之一，附证据、依赖结果、读路径、诊断和模型版本；本框架正常数据前提下不产生 `UNRESOLVED`；物化额外返回变更路径集合及提交结果，不返回目标或下游重识别结果。

#### 6.1.7 状态与物化语义

- 状态：识别结果按本次读取所处的模型/配置版本绑定；不维护跨请求的 MutationSet 或增量失效状态，每次判断重新获取配置声明的值并按 DAG 拓扑计算。
- 识别：同一模型/配置上下文中只读获取值并返回结果，不产生隐式模型写入。
- 物化：仅写入声明的 `change-data` 值；写入失败直接返回 `ERROR` 并抛出异常，由现有框架原子化与回滚机制撤销本次写入，不继续下游流程。提交成功后返回物化结果并结束，不自动调用 `evaluate`；P3 不新增独立事务、幂等或并发语义。

#### 6.1.8 异常与禁止副作用

- 非法路径、非法表达式、缺失引用、循环、权限拒绝和普通 null 均为可定位 `ERROR`；识别阶段禁止隐式模型修改，失败不得静默转 FALSE。

## 7. 跨功能规则

> 仅单功能且不存在共享约束时可写“无”；多功能需求必须明确共享不变量、先后顺序、依赖、触发、取消、补偿和禁止副作用。

- CR-P3-INFORMATION-ENGINE-001：所有 Information 统一通过限定 Key 和依赖/read-set 被消费；P3 不创建 Consumer runtime，不把 Directory Change、Produce 或 Action 语义提前纳入 Information。

## 8. 异常与边界场景

| 场景编号 | 关联功能 | 场景 | 预期结果 | 禁止副作用 |
|---|---|---|---|---|
| EX-P3-INFORMATION-ENGINE-001 | P3-INFORMATION-ENGINE-F01 | 声明路径不存在、普通求值遇到 null、复合引用缺失或 Information 形成循环 | 编译或识别返回 `ERROR`，包含路径/Key/来源位置；循环和缺失引用在发布前拒绝 | 不得写模型、更新缓存为 FALSE、继续物化或推进下游流程 |
| EX-P3-INFORMATION-ENGINE-002 | P3-INFORMATION-ENGINE-F01 | `orderDetailList` 为空但订单状态满足 `status = 1/2/3/4` | `every(emptyCollection, ...)` 为 TRUE，但订单相关 Information 因明细非空前置条件不满足而返回 FALSE，可附 `ORDER_DETAIL_REQUIRED` 诊断 | 不得物化订单状态，不得把空明细订单分类为 ordered/paying/success/error |
| EX-P3-INFORMATION-ENGINE-003 | P3-INFORMATION-ENGINE-F01 | 一次判断涉及基础字段与关系字段（如 `OrderInfo.status` 与 `OrderInfo.orderDetailList.status`） | 基础字段先按 `target-main` 对应 Data 解析；关系字段按自身 `data`（`orderDetail`）解析，二者均按本次 Information 配置实时读取 | 不得把关系字段误读为 `order` 基础字段，也不得使用未重新读取的旧值 |

## 9. 验收标准

> 每条验收标准必须有稳定 ID，并描述可建立的前置状态、动作、可观察结果和禁止副作用。不得以“调用某接口”“写入某表”作为业务验收结论。

### AC-P3-INFORMATION-ENGINE-001 Information Engine 需求基线

Given `mix` 的 4 个 System 和 16 个 Information 已被完整发现，且模型、View、RuleView 引用可解析  
When 编译并识别全部 Information，再分别执行合法 `change-data` 物化、模型路径变更和错误配置场景  
Then 7 个 RuleView 原子、4 个模型表达式原子、5 个复合 Information 的 Key、模型路径/依赖描述、DAG 和 TRUE/FALSE/ERROR 结果均可观察；空集合与订单明细非空规则、null/非法路径 ERROR 规则一致生效  
And 复合 Information 不可直接物化，识别不产生隐式模型写入，错误不得转为 FALSE；每次判断按配置重新读取当前值，基础字段与 `orderDetailList` 等关系字段分别解析；物化失败抛出异常并由现有回滚机制撤销写入。

## 10. 非功能要求

### 10.1 性能

- 每次 Information 判断只读取该 Information 配置声明的模型路径及其依赖路径；不要求维护缓存、MutationSet 或增量失效索引，具体时延阈值由测试设计阶段基于基线补充。

### 10.2 安全与权限

- 模型写入、`change-data` 和 RuleView 访问必须遵守已发布的 System/model-access 权限；权限拒绝为 ERROR 且无写副作用。

### 10.3 可靠性、一致性与恢复

- 结果必须绑定本次读取的模型/配置版本；物化任一步失败不得部分成功、不得继续流程；成功提交后物化流程结束，不自动重新读取或继续识别。

### 10.4 审计与可观测性

- 结果保留证据、依赖结果、读写路径、诊断、来源位置和模型版本，使 TRUE/FALSE/ERROR 可追溯；正常数据前提下不记录 UNRESOLVED 运行结果。Information 专项测试证据在后续测试设计/开发阶段补充。

### 10.5 兼容与历史数据

- 以当前 `mix` XML 为 P3 唯一 fixture；不要求 P3 提供现代 YAML 对等，不兼容旧 Directory Change、Consumer runtime 或 `dec-expand-declaration` Adapter。

## 11. 模块职责边界

### 11.1 dec-core-model、dec-core-context、dec-core-compiler 模块

负责：

- 负责 Information 定义/编译模型、两套表达式语言、识别结果、DAG、物化边界、实时读取和对下游的只读事实契约。

不负责：

- 不负责修订 P2 System/model-access 事实，不负责 Action/Produce/Directory/Query/Transaction/Session 的完整运行语义，不负责生产代码实现在本需求阶段落地。

### 11.2 协作模块

| 模块 | 负责内容 | 输入/输出业务事实 | 失败责任 |
|---|---|---|---|
| dec-context-config-parse-xml | 发现并提供 `mix` XML 的 Raw 定义与来源位置 | 10 个 XML 文件、System/View/Rule/Information 原始声明 | 解析/来源错误由解析模块报告，Information 互斥与依赖错误由 P3 编译边界报告 |
| dec-demo | 提供 `mix` fixture 和可复现的业务样例输入 | 16 个 Information、模型字段、RuleView 引用与样例组合 | fixture 不一致需阻止 P3 验收，不修改 P2 事实 |
| P2 System/RuleView 能力 | 提供已发布归属、View/RuleView Key 和模型读写权限 | system、model-ref、rule-ref、read/write path | P3 只消费其事实；缺失或拒绝按 ERROR 处理 |

### 11.3 协作边界

P3 只接收 P1/P2 已发布的 System、View、RuleView、模型路径和权限事实；P3 输出 InformationKey、识别结果、模型路径/依赖描述和本次读取证据供 P4/P5 使用。P3 不反向修改 P2，不拥有 BusinessScope、Action、Produce、Directory 或 Query 状态，也不维护 MutationSet。

## 12. 待确认事项与已确认决策

### 12.1 待确认事项

| 决策编号 | 问题 | 可选方案 | 推荐方案 | 是否阻塞 | 责任人/Agent | 状态 |
|---|---|---|---|---|---|---|

### 12.2 已确认决策

| 决策编号 | 结论 | 原因 | 证据/确认来源 | supersedes |
|---|---|---|---|---|
| DEC-P3-INFORMATION-ENGINE-001 | 模型声明路径不存在统一返回 `ERROR`，并携带路径和来源位置。 | 非法路径继续执行并返回 FALSE 会掩盖配置错误。 | 用户本轮确认（2026-08-31） | - |
| DEC-P3-INFORMATION-ENGINE-002 | 普通求值遇到 null 直接返回 `ERROR`；唯一例外是显式 `InformationKey = null`，如 `payment.success = null`。 | 防止空值被误判为业务 FALSE；显式空值比较保留业务判断能力。 | 用户本轮确认（2026-08-31） | - |
| DEC-P3-INFORMATION-ENGINE-003 | `every(emptyCollection, ...) = TRUE`；订单相关 Information 另要求订单明细非空。 | 保留全称量词语义，同时避免空明细订单被误分类。 | 用户本轮确认（2026-08-31） | - |
| DEC-P3-INFORMATION-ENGINE-004 | 本框架按数据已准备好的正常前提执行；不考虑数据未加载、依赖未识别、动态规则暂不可判定或超时等 `UNRESOLVED` 触发场景，识别结果只需覆盖 TRUE/FALSE/ERROR。 | 这些状态不会在当前框架出现，纳入需求会制造无实现依据的额外分支。 | 用户本轮确认（2026-08-31） | DEC-P3-INFORMATION-ENGINE-004（待确认项） |
| DEC-P3-INFORMATION-ENGINE-005 | 物化失败直接返回 `ERROR` 并抛出异常；仅定义 `change-data` 写入值，沿用现有原子化和异常回滚机制，不新增幂等、并发或独立事务要求。 | 当前框架已提供原子化和回滚，P3 不重复定义执行基础设施。 | 用户本轮确认（2026-08-31） | DEC-P3-INFORMATION-ENGINE-005（待确认项） |
| DEC-P3-INFORMATION-ENGINE-006 | P3 不维护 MutationSet 或 reverse-DAG 运行时失效缓存；每次判断按 Information 配置在当前数据/事务上下文中重新读取模型值，必要时从数据库再读。 | 避免缓存值与事务内真实数据不一致，且实现更简单。 | 用户本轮确认（2026-08-31） | - |
| DEC-P3-INFORMATION-ENGINE-007 | 当前 `mix` 采用带显式 `<ref>` 的 `model-access` 写法；旧式无 `<ref>` 简写不在 P3 兼容范围内。`ref@property` 先匹配 `target-main`；`OrderInfo.status` 先归属 `order` 基础 Data，`orderDetailList` 归属独立的 `orderDetail` Data。 | 现有编译器只有显式 ref 才能生成 Binding；关系字段不能误当作主对象基础字段。 | `systems.xml`、`RawDefinitionBuilder`、`ModelAccessCompiler`、`DefaultModelAccessSelectorResolver` 现状核对（2026-08-31） | - |
| DEC-P3-INFORMATION-ENGINE-008 | Information 专项测试用例和实现后测试证据延后到测试设计/开发阶段；需求确认阶段只冻结业务语义和可观察结果。 | 当前 Information 功能尚未实现，现阶段不存在可验证的 Information 测试功能。 | 用户本轮确认（2026-08-31） | - |
| DEC-P3-INFORMATION-ENGINE-009 | `materialize` 成功提交后直接返回物化结果并结束；不自动或作为 `FLOW-P3-INFORMATION-EVALUATION` 的步骤执行独立只读重评估，不返回目标或下游重识别结果。 | 用户明确取消原计划第 7 项，避免把提交后的额外读取和下游识别纳入本次实现。 | 用户本轮确认（2026-09-02） | DEC-P3-INFORMATION-ENGINE-005、DEC-P3-INFORMATION-ENGINE-006 中关于物化后重识别的部分 |

## 13. 追踪关系

> 每个功能、每条业务规则、每条跨功能规则和每条验收标准都必须被至少一个追踪项引用；涉及三步以上、状态流转、补偿或跨模块协作时还应填写稳定 `FLOW-*`。需求分析通过后使用 `requirement_doc.py sync-traceability` 同步到长任务 `traceability.json`。

| 追踪编号 | 功能编号 | 业务规则/跨功能规则 | 验收标准 | 关联流程 | 影响分析 | 后续业务模型 | 后续设计 | 测试 Case | 状态 |
|---|---|---|---|---|---|---|---|---|---|
| TR-P3-INFORMATION-ENGINE-001 | P3-INFORMATION-ENGINE-F01 | BR-P3-INFORMATION-ENGINE-001、BR-P3-INFORMATION-ENGINE-002、BR-P3-INFORMATION-ENGINE-003、CR-P3-INFORMATION-ENGINE-001 | AC-P3-INFORMATION-ENGINE-001 | FLOW-P3-INFORMATION-EVALUATION | P4/P5 消费 InformationEngine；P2 权限事实为前置 | P3 Information 事实模型 | P3 Information 编译/识别/物化设计 | 测试设计阶段补充 `CASE-P3-MIX-001`、`CASE-P3-ERROR-001` | COVERED |

## 14. 变更记录

| 文档 revision | 日期 | 阶段 | 变更内容 | 责任 Agent |
|---|---|---|---|---|
| REQCONF-P3-R01 | 2026-08-31 | 需求确认 | 基于 P1—P8 调整报告、P3 详细计划及 `mix` 全部 10 个 XML 文件，确认 16 个 Information 的分类、依赖、识别/物化/增量失效目标与 P2/P4-P8 边界。 | RequirementConfirmationAgent |
| REQCONF-P3-R02 | 2026-08-31 | 需求确认 | 根据用户确认冻结非法路径 ERROR、普通 null ERROR（显式 Information 空值比较除外）及 `every(emptyCollection)=TRUE + 订单明细非空`；补齐功能、异常、验收和追踪内容，提交同 revision 独立 Review。 | RequirementConfirmationAgent |
| REQCONF-P3-R03 | 2026-08-31 | 需求确认 | 根据用户进一步确认，关闭本框架不会出现的数据未加载/依赖未识别/动态暂不可判定等 UNRESOLVED 运行场景；明确结果为 TRUE/FALSE/ERROR，物化失败抛异常并沿用现有原子回滚，仅定义 change-data 写入值，不新增幂等、并发或独立事务要求；补充 RuleTests.java 与 ModelContainer 现状依据。 | RequirementConfirmationAgent |
| REQCONF-P3-R04 | 2026-08-31 | 需求确认 | 根据用户确认取消 MutationSet/reverse-DAG 运行时失效，改为每次按 Information 配置实时读取；确认当前无 Information 专项测试，测试用例延后至测试设计/开发阶段；补充 model-access 显式 ref、target-main 优先及 `orderDetailList` 独立 `orderDetail` Data 归属事实，并将其纳入 P3 需求边界。 | RequirementConfirmationAgent |
| REQCONF-P3-R05 | 2026-09-02 | 需求确认 | 根据用户最新授权取消物化成功后的独立只读重评估：`materialize` 以提交成功并返回变更路径/提交结果为终点，不自动调用 `evaluate`，不返回目标或下游重识别结果；失败回滚和禁止继续下游保持不变。 | RequirementConfirmationAgent |
| REQAN-P3-R01 | 2026-08-31 | 需求分析 | 在 R04 已确认语义基础上完成第 5～13 节的功能、规则、异常、验收、流程和追踪分析；保留 `TR-P3-INFORMATION-ENGINE-001` 作为需求到规则/验收/流程的唯一追踪项，明确测试 Case/test_ref 延后至 test_design/development。 | RequirementAnalysisAgent |
