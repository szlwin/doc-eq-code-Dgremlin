# 基于 `mix` 目标文档的框架技术整改方案

> 2026-07-25 架构决策更新：`dec-expand-declaration` 整体退役；P1—P8 直接以 `mix` 为唯一目标契约，不建立 Adapter。

## 1. 文档目的

本文以 `dec-demo/src/main/resources/mix` 下的目标配置为唯一目标语义，对当前 `doc-eq-code-Dgremlin` 框架进行全量代码审计，并给出可分阶段实施、可测试、可验收的技术整改方案。

整改目标不是把 XML 生成大量 Java 代码，而是建立如下运行模式：

```text
XML / YAML 文档
    -> 解析为统一业务 AST
    -> 编译、引用解析和静态校验
    -> 生成不可变运行时元数据
    -> 由统一引擎解释执行、查询和追踪
```

`mix` 目录中的数据、视图、System、RuleView、Information、Directory、Action、Produce、Back 等定义应成为框架的正式契约，而不是仅供讨论、当前解析器未必支持的示例。

---

## 2. 审计范围与方法

### 2.1 审计范围

本次静态审计排除了 `.git`、`target`、IDE 元数据和二进制依赖，读取了仓库中的全部文本源码、配置、测试与设计文档：

| 类型 | 数量 |
|---|---:|
| 文本文件总数 | 503 |
| Java 文件 | 420 |
| XML 文件 | 43 |
| Markdown 文件 | 15 |
| 文本总行数 | 39,179 |

重点审计模块：

- `dec-core-context`
- `dec-context-config-parse-xml`
- `dec-context-config-parse-yaml`
- `dec-core-model`
- `dec-core-datasource`
- `dec-datasource-orm-sql`
- `dec-datasource-orm-mysql`
- `dec-core-starter`
- `dec-expand-declaration`（仅作为历史临时模块审计，目标架构中整体删除）
- `dec-demo`
- `docs`

### 2.2 动态验证边界

当前执行环境没有 Maven 可执行文件，因此本次不能完成 Maven 编译和测试。该限制不影响静态结构审计，但意味着正式整改的第一步必须先建立可重复构建和 CI 基线，不能在没有构建门禁的情况下直接大规模重构。

---

## 3. `mix` 定义的目标业务语义

### 3.1 统一业务模型

`OrderInfo` 是跨 `user`、`order`、`payment` System 使用的业务模型。System 不拥有整个业务模型，只拥有：

- 可引用的数据；
- 可调用的视图；
- 所属 RuleView；
- 对共享模型的可读、可写路径。

### 3.2 RuleView 归属

RuleView 使用二元标识：

```text
(system, rule-view-info/@name)
```

例如：

```xml
<rule-view-info system="order" name="save-Order" view-ref="OrderInfo">
```

不能继续只以 `name` 放入全局 Map，否则不同 System 中的同名 RuleView 会互相覆盖，也无法执行模型写权限校验。

### 3.3 Information Tree

Information 分为三类：

1. RuleView 识别型原子 Information；
2. `rule-data` 识别且可通过 `change-data` 物化的原子 Information；
3. 只组合其他 Information 的复合 Information。

关键边界：

- `rule-data`、`change-data` 只能访问 `model-ref` 指向的业务模型数据；
- `expression` 只能引用 Information；
- 识别与物化必须分离；
- Information 依赖图必须无环；
- 模型变化后只重算受影响的 Information。

### 3.4 Directory

普通 `subdirectory` 的默认语义是：

```text
子目录执行完成 -> 向父目录执行
```

不需要 `role="predecessor"`。

`role="case"` 仅表示父结果目录中的一种业务情况，不代表普通执行前置关系。

支付链路为：

```text
ordered -> paying -> PayResult -> success / error
```

其中：

- `startPay` 只属于 `paying`；
- `PayResult` 只接收、保存、识别支付结果；
- `paying` 不是一种支付结果；
- `success`、`error` 才是 `PayResult` 的结果情况。

### 3.5 查询语义

```text
find("PayResult")
```

查询 `PayResult` 的全部 `role="case"` 情况，即 `success OR error`，不包含普通执行子目录 `paying`。

```text
find("PayResult").eq("success")
```

只查询 `success` 情况。

查询条件必须从 Information 和视图映射编译而来，不能把目录名称硬编码为某个状态值。

---

## 4. 当前框架架构概览

### 4.1 配置与元模型

`dec-core-context` 当前提供 Data、Relation、View、Rule、Service、Directory、Datasource、Connection 等配置对象，并通过 `ConfigFactory` 的固定数组和多个全局单例 Config 保存。

当前 `Config` 类型槽位只有：

```text
DATASOURCE, CONNECTION, DATA, RELATION, VIEWDATA,
RULE, SERVICE, DIRECTORY_CONFIG, DATASOURCE_CONFIG,
CONNECTION_CONFIG
```

没有：

- System；
- ModelAccess；
- Business；
- Information；
- Dependency；
- Produce；
- Back；
- 编译后的目录图和信息图。

### 4.2 XML / YAML 解析

XML 通过 `ConfigFileParser + CommonParser` 按固定顺序加载 Data、Relation、View、Rule、Service、Directory。

YAML 维护了一套独立解析实现，字段别名、初始化行为和异常处理与 XML 不完全一致。

当前两种格式不是“同一 AST 的两个前端”，而是两套逐步漂移的加载逻辑。

### 4.3 规则执行

`RuleParser` 将 `rule-view-info` 加载为 `RuleViewInfo`，`RuleConfig` 只使用 RuleView 名称作为 Key。`RuleContainer` 顺序执行内部规则，`RuleUtil` 通过静态 Map 反射创建具体执行器。

当前规则执行具备部分 CRUD、检查和 grammar 能力，但缺少：

- RuleView System 作用域；
- RuleView 调用契约；
- Information 识别结果契约；
- 模型写路径授权；
- Action / Produce 的统一返回值；
- 可插拔、类型安全的执行器注册。

### 4.4 Directory

当前 `DirectoryInfo` 只有旧字段：

- `viewRef`；
- `isRoot`；
- Action；
- Change；
- SubDirectory；
- parentDirectory。

当前 `DirectoryParser` 仍读取：

- `view-ref`；
- `ref-rule`；
- `any-one`；
- `mutual-exclusion`；
- Change 的 `property + 文本表达式`。

与 `mix` 的 `model-ref`、`information-ref`、`system-ref`、`rule-ref`、Dependency、Produce、Back、`role="case"` 不兼容。

### 4.5 查询与会话

`DirectoryContainer.find()` 当前：

- 强制被查询目录必须是根目录；
- 依赖 `start/end`，未设置时存在空指针风险；
- 从旧 Change grammar 拼 SQL；
- 硬编码 `con1`；
- 直接拼接 SQL；
- 不支持参数绑定；
- 不支持 `role="case"`；
- 不支持 `eq`；
- 不支持关联模型查询计划；
- 不支持 Information 的可下推/不可下推区分。

`DirectoryContainer.execute()` 目前只设置操作类型，没有真正的目录执行实现。

### 4.6 历史临时模块 `dec-expand-declaration`

`dec-expand-declaration` 曾用于验证 System、Business、Produce/Consume、事务策略和流程执行想法，但它形成了与 `dec-core-context / dec-core-model` 平行的 Context、parser 和 runtime。

该模块不是 System 或核心业务能力的依赖，目标架构决定整体删除：

- 不抽取其代码；
- 不建立 LegacyAdapter；
- 不迁移旧 declaration XML/YAML；
- 不保留独立 artifact；
- 有价值的业务场景只允许依据 `mix` 重新编写为 `dec-demo` fixture 和端到端测试。

`Business` 在新架构中如继续存在，只是统一 AST/EngineContext 的逻辑作用域，不是独立模块或第二套运行时。

---

## 5. 关键差距矩阵

| `mix` 能力 | 当前状态 | 主要问题 | 目标整改 |
|---|---|---|---|
| `system-file-info` | 不支持 | Config 入口无 System 文件类型 | 新增 System parser 与编译模型 |
| `business-file-info` | 不支持 | 无统一 BusinessScope AST | 在 `dec-core-compiler` 中新增逻辑 BusinessScope parser/compiler，不建立独立项目 |
| `rule-view-info/@system` | XML 已写、Java 忽略 | Rule 全局同名覆盖 | 使用 `RuleViewKey(system, name)` |
| `model-access-info` | 不支持 | 无读写路径授权 | 编译期校验 + 运行时写屏障 |
| Information | 不支持 | 无识别、物化、依赖图 | 新建 InformationEngine |
| `rule-data` | 不支持 | 无模型表达式编译 | 独立 ModelExpressionCompiler |
| `change-data` | 不支持 | 旧 Change 语义不同 | Materializer + 受影响信息重算 |
| `expression` | 不支持 | 无 Information 组合计算 | InformationExpressionCompiler |
| Directory `model-ref` | 不支持 | 仍使用 `view-ref` | 统一改为 modelRef |
| Directory `information-ref` | 不支持 | 无目录后置条件 | 执行后强制验证 |
| Dependency | 不支持 | 无进入门禁 | 执行前 Information 门禁 |
| Action `system-ref` | 不支持 | 无能力归属 | SystemScopedActionInvoker |
| Action `rule-ref` | 仍读 `ref-rule` | 新旧契约冲突 | 拒绝旧属性，引用 RuleView |
| 自定义 Action | 现有接口只是前后钩子 | SPI 语义不匹配 | 新建注册表与执行上下文 |
| Produce | 不支持 | 无数据产出验证 | ProduceResult + verifier |
| Back | 不支持 | 无返回路径和返回 Action | BackPlan + compensation/action |
| `role="case"` | 不支持 | 仍用 any-one/mutual-exclusion | Case edge 与执行 edge 分离 |
| `find("PayResult")` | 不支持 | 强制根目录、旧状态拼接 | Case union QueryPlan |
| `.eq("success")` | 不支持 | 仅有 `equals()` 且语义不同 | 新 Query API 与编译器 |
| 多模型关联 | 部分 View 可映射 | Directory 查询不使用 View 图 | JoinPlan / FetchPlan |
| 编译期校验 | 基本没有 | 未知属性被忽略、错误晚发现 | Diagnostic compiler |
| 热加载/多项目 | 不支持 | 全局静态单例污染 | 不可变 EngineContext |

---

## 6. 目标总体架构

### 6.1 分层

```text
Document Source Providers
          |
          v
MixSourceResolver -> MixSourceGraph
          |
          v
XML/YAML Frontends -> CanonicalDocumentSet
          |
          v
RawDefinitionBuilder -> RawDefinitionSet
          |
          v
Model Compiler
  structural validation / symbol registration
  reference resolution / ownership validation
  graph preparation / deferred classification
          |
          v
CompiledModelSet -> EngineContext
          |
          +-> P2～P7 Runtime Engines
          +-> CoreConfigProjection (read-only, deprecated)
```

### 6.2 核心原则

1. `mix` 是目标配置契约，`dec-demo/src/main/resources/mix` 是 fixture，不是生产硬编码路径；
2. 根配置直接发现 Data/View/System/Business，System 再间接发现 Rule；
3. 发现顺序与语义解析顺序分离；
4. XML/YAML 只负责 Canonical 转换，不写全局 Config；
5. 只有无 ERROR 的 `CompiledModelSet` 才能发布；
6. BusinessScope 只是命名空间，不是独立模块或 Engine；
7. P2～P7 语义使用显式 DeferredDefinitionRegistry；
8. 所有引用使用强类型 Key；
9. `dec-expand-declaration` 整体删除，不建立 Adapter。

---

## 7. 统一定义模型与编译产物

### 7.1 新模块

新增 `dec-core-compiler`，负责 SourceGraph、RawDefinitionSet、Compiler Pass、Diagnostic、digest 和 Publication；不得依赖 SQL、MySQL、demo、frontend 实现或废弃模块。

### 7.2 RawDefinitionSet

```text
RawRootConfigDefinition
RawDataSourceDefinition
RawConnectionDefinition
RawDataDefinition
RawViewDefinition
RawSystemDefinition
RawRuleViewDefinition
RawRuleDefinition
RawBusinessScopeDefinition
RawInformationDefinition
RawDirectoryDefinition
RawActionDefinition
RawProduceDefinition
```

RawDefinition 保存 SourceRef 和规范化声明，不保存 DOM/SnakeYAML Node。

### 7.3 强类型 Key

```text
DataSourceKey(name)
ConnectionKey(name)
DataKey(name)
ViewKey(name)
SystemKey(name)
RuleViewKey(systemKey,name)
BusinessScopeKey(name)
InformationKey(scopeKey,name)
DirectoryKey(scopeKey,name)
ActionKey(directoryKey,name)
```

### 7.4 CompiledModelSet

P1 发布：SourceManifest、Data/View 等 Registry、结构化 System/RuleView/BusinessScope、已类型化的 Information/Directory/Action/Produce 外壳、DeferredDefinitionRegistry 和 semanticDigest。

P1 不把 Information 表达式、ModelAccess 权限、Rule grammar、Directory 状态机、Action/Produce 执行解释为已完成语义。

---

## 8. `mix` 配置加载与编译整改

### 8.1 实际源发现图

```text
mix/orm-config.xml
  -> mix/data/*.xml
  -> mix/view/*.xml
  -> mix/system/systems.xml
       -> mix/rule/user-rule.xml
       -> mix/rule/order-rule.xml
       -> mix/rule/payment-rule.xml
  -> mix/business/order-business.xml
```

### 8.2 Source Discovery

1. 解析 root 的最低限度文件引用；
2. 标准化并展开 Data/View 文件集合；
3. 解析 System 文件的最低限度结构，发现 Rule 文件；
4. 建立有类型 SourceEdge、去重和稳定排序；
5. 发现完成后统一进入 frontend 和 RawDefinition build。

### 8.3 Compiler Pass

```text
source graph validation
-> structural validation
-> symbol registration
-> reference resolution
-> ownership validation
-> graph preparation
-> deferred classification
-> P1 semantic validation
-> publish
```

### 8.4 Frontend 修改点

- XML：新增 root/System/Business/Rule 的 Canonical builder，禁用 DTD/外部实体；
- YAML：安全 Node 模式，同一 Canonical/Raw 契约；
- Parser 不直接调用 ConfigFactory、ConfigManager 或运行时注册表；
- 未知元素/属性在严格模式形成 Diagnostic。

### 8.5 Diagnostic

P1 使用 `MIX-SRC-*`、`MIX-STRUCT-*`、`MIX-SYMBOL-*`、`MIX-REF-*`、`MIX-DEFER-*`、`MIX-PUBLISH-*` 稳定错误码。错误聚合后稳定排序，任何 ERROR 阻止发布。

---

## 9. System 与模型访问控制整改

### 9.1 System 注册

建立不可变 `SystemRegistry`：

```text
system name -> SystemDefinition
```

SystemDefinition 包含：

- data refs；
- view refs；
- rule files；
- model access policies；
- 可选默认连接/数据源策略。

### 9.2 RuleView 作用域

将当前：

```text
RuleConfig: Map<String, RuleViewInfo>
```

改为：

```text
RuleViewRegistry: Map<RuleViewKey, RuleViewDefinition>
```

所有调用必须传：

```text
system-ref + rule-ref
```

Information 和 Action 的 `system-ref` 必须与目标 RuleView 的 system 相同。

### 9.3 模型路径编译

启动时把：

```text
user
payInfo.payDetailList
*
```

编译为标准 `ModelPath`，并校验路径确实存在于 `view-ref` 指向的模型中。

权限规则：

- 写父路径代表是否允许写全部子路径，需要明确配置策略；
- 本方案建议父路径写权限包含子路径，但显式 deny 优先；
- `read path="*"` 只授予读取，不授予写入；
- RuleView 的所有写规则、`change-data`、自定义 Action 输出映射均需做静态写集分析；
- 静态分析不能确定时，运行时 `ModelMutationGuard` 再拦截。

### 9.4 运行时写屏障

禁止 Rule 执行器直接取得无保护的 `Map<String,Object>` 后任意修改。改为：

```text
MutableModelView(system, model, accessPolicy)
```

所有 set/add/remove 操作经过权限检查并记录变更路径，用于 Information 增量重算和执行追踪。

---

## 10. Information Engine 整改

### 10.1 Information 类型

```text
ATOMIC_RULE       system-ref + model-ref + rule-ref
ATOMIC_MODEL_EXPR system-ref + model-ref + rule-data [+ change-data]
COMPOSITE         expression
```

三种定义必须互斥，编译器拒绝混合配置。

### 10.2 两套表达式语言必须分离

#### Model Expression

用于：

- `rule-data`；
- `change-data`；
- 可下推到数据源的原子判断。

可访问：业务模型字段、集合操作，如 `every`。

#### Information Expression

用于：

- `expression`。

只能访问 Information Key 和逻辑运算，不允许直接访问模型字段。

不能继续用一个无类型 grammar 字符串同时承担两种语义。

### 10.3 识别结果契约

新增：

```java
InformationResult {
    boolean resolved;
    boolean matched;
    Evidence evidence;
    Set<ModelPath> readPaths;
    List<Diagnostic> diagnostics;
}
```

RuleView 用于 Information 识别时：

- 不能只返回普通“执行成功”；
- 必须返回 matched true/false；
- 默认禁止产生副作用；
- 如需查询数据源，必须声明只读；
- 必须记录证据或至少记录规则与输入版本。

### 10.4 Information 依赖图

编译期建立：

```text
Information -> upstream Information
ModelPath -> affected Information
```

运行时模型路径变化后：

1. 失效受影响原子 Information；
2. 重新识别原子 Information；
3. 按拓扑顺序重算复合 Information；
4. 更新当前 Directory 分类状态。

### 10.5 物化

`change-data` 只属于 `ATOMIC_MODEL_EXPR`。执行步骤：

```text
校验 System 写权限
-> 执行受保护模型修改
-> 收集变更路径
-> 重算 Information
-> 验证目标 Information 成立
```

物化失败必须回滚当前执行单元，不能仅返回 false 后继续流程。

---

## 11. Action、RuleView 与 Produce 整改

### 11.1 统一 Action SPI

现有 `DirectoryAction.beforeRule/afterRule` 是钩子，不是 `mix` 定义的自定义 Action。新增：

```java
interface BusinessAction {
    ActionResult execute(ActionContext context) throws ActionException;
}
```

`ActionContext` 至少包含：

- business / directory / action 标识；
- 当前 System；
- 受保护业务模型；
- 输入数据；
- ExecutionTrace；
- TransactionContext；
- InformationSnapshot；
- Produce collector。

### 11.2 两类 Action

#### RuleView Action

有 `rule-ref`：

```text
RuleViewRegistry.resolve(system-ref, rule-ref)
-> 校验 model-ref 兼容
-> 按顺序执行内部 rule
-> 返回 ActionResult
```

#### Custom Action

无 `rule-ref`：

```text
CustomActionRegistry.resolve(action name)
-> 执行注册实现
-> 返回 ActionResult
```

自定义 Action 名称全局唯一，注册完成前引擎不可进入 READY。

### 11.3 RuleView 内部执行

保留当前顺序规则执行思想，但重构：

- `RuleUtil` 的 Class 反射 Map 改为 `RuleExecutorRegistry`；
- 使用显式构造器，不使用 `Class.newInstance()`；
- RuleExecutor 输入输出类型化；
- 失败策略默认 fail-fast；
- CRUD 规则、grammar 规则和 check 规则统一返回 `RuleExecutionResult`；
- 禁止通过 `return null` 表示不支持；
- 连接由 `ExecutionContext` 注入，不在规则内部临时查全局默认连接。

### 11.4 Produce 契约

```java
ProduceResult {
    String ref;
    Object value;
    Optional<InformationKey> informationRef;
    Set<ModelPath> changedPaths;
}
```

每个 Action 完成后：

1. 校验所有 `produce/@ref` 均实际产生；
2. 写入执行上下文；
3. 如有 `information-ref`，验证目标为原子 Information；
4. 重新识别目标 Information；
5. 重算下游复合 Information；
6. 目标 Information 不成立则 Action 失败。

`PaymentInfo`、`PayResult`、`PyaError` 当前不是 Data/View 定义，必须明确为以下一种：

- 外部输入类型；
- Action 临时产出类型；
- 业务模型片段；
- 持久化 Data。

建议新增 `data-contract` 或 `payload-definition`，避免编译器只能把这些 ref 当作不透明字符串。

---

## 12. Directory Engine 整改

### 12.1 图模型

Directory 图需要区分两种边：

```text
EXECUTION：普通 subdirectory，子 -> 父执行
CASE：role="case"，父结果目录包含的分类
```

`paying -> PayResult` 是 EXECUTION。

`PayResult -> success/error` 是 CASE 分类关系；它们可同时参与后续执行路径，但不能被当作 PayResult 之前的支付执行步骤。

### 12.2 编译期图校验

必须校验：

- 根目录数量和业务入口；
- 所有 `rel` 存在；
- 所有执行目录从根可达；
- 执行图无非法环；
- CASE 目标存在；
- 一个 CASE 是否允许多父结果目录；
- 多父目录路径是否歧义；
- Back 路径有效；
- case Information 可用于唯一分类。

### 12.3 执行状态机

新增：

```text
DirectoryExecutionState
    currentDirectory
    targetDirectory
    resolvedPath
    completedSteps
    informationSnapshot
    producedData
    transactionState
    trace
```

进入单个目录的固定模板：

```text
1. Dependency 校验
2. Action 顺序执行
3. Produce 校验与信息重算
4. Change 物化
5. 验证 directory/@information-ref
6. 结果目录自动分类
7. 写执行记录
```

### 12.4 路径规划

```text
execute("success")
```

必须先根据当前状态和图规划：

```text
ordered -> paying -> PayResult -> success
```

不能直接执行 success 的 Action。

路径规划器需返回：

- 主执行路径；
- 每步依赖；
- CASE 判定点；
- 事务边界；
- Back 可用路径。

### 12.5 PayResult 分类

进入 PayResult 后：

1. 执行 `receivePayResult`；
2. 校验 `payment.hasResult`；
3. 重算 `payment.success`、`payment.error`；
4. 在 `role="case"` 集合中匹配；
5. 本业务必须且只能命中一个；
6. 命中 success 或 error 后再执行对应目录。

不需要为 `paying` 添加 `predecessor` 角色，也不能把 `startPay` 放到 PayResult。

### 12.6 Back

Back 不应简单反向调用目录 Action。新增 `BackPlan`：

- 从当前目录定位目标父/子关系；
- 执行该关系声明的 back Action；
- 清理或恢复 Produce；
- 重新物化目标目录 Information；
- 重算 Information；
- 验证返回后状态；
- 记录补偿结果。

多级返回逐边执行，不能一次跳过中间 Back。

---

## 13. Query Engine 整改

### 13.1 Query API

建议替换当前含糊 API：

```java
find("PayResult")
    .eq("success")
    .with("UserInfo")
    .start("ordered")
    .end("success")
    .execute();
```

其中：

- `eq` 是 CASE 过滤；
- `start/end` 是路径范围；
- `with` 是关联模型 FetchPlan；
- `include/exclude` 明确为目录集合过滤；
- 不再使用与 Java Object `equals` 混淆的方法名。

### 13.2 QueryPlan

Directory 查询先编译为中立计划：

```text
DirectoryQuery
-> InformationPredicateTree
-> ModelPredicateTree
-> JoinPlan
-> ProjectionPlan
-> ParameterBindings
-> Datasource QueryPlan
```

SQL 模块只负责把 QueryPlan 转换为参数化 SQL，不理解 Directory 业务语义。

### 13.3 `find("PayResult")`

编译为：

```text
CASE(payment.success) OR CASE(payment.error)
```

`paying` 是 EXECUTION edge，因此不进入查询条件。

### 13.4 可下推与不可下推

Information 应标记查询能力：

- `PUSHDOWN`：可转换为字段谓词；
- `JOIN_PUSHDOWN`：需要关联模型；
- `RUNTIME_ONLY`：只能加载候选数据后由 RuleView 判断；
- `UNSUPPORTED`：不能用于列表查询，编译期报错。

例如 `rule-data` 通常可下推；带外部服务调用的 RuleView Information 通常只能运行时判断。

### 13.5 SQL 安全

必须移除 DirectoryContainer 中字符串拼接 SQL 的方式：

- 所有常量使用参数绑定；
- 字段和表来自已编译模型映射；
- 不允许配置文本直接成为 SQL；
- 连接从 QueryContext 获取，不得硬编码 `con1`；
- SQL 转换器返回 SQL + typed parameters。

### 13.6 关联模型

`with("UserInfo")` 应根据 View 的 Relation 图产生 JoinPlan 或批量 FetchPlan，不能仅把名称保存在空方法中。

需要支持：

- one-to-one；
- one-to-many；
- 嵌套 `payInfo.payDetailList`；
- 关联条件与查询条件的别名管理；
- 避免 one-to-many 导致主对象重复；
- 分页时先主表分页再批量加载集合。

---

## 14. 事务、会话和多数据源整改

### 14.1 连接解析

禁止运行时通过全局默认连接或硬编码名称决定连接。连接选择顺序应明确：

```text
Rule override
-> System default
-> Data mapping datasource
-> Business default
-> Engine default
```

解析结果在编译期固化为 `ConnectionRoute`，运行时只按路由取连接。

### 14.2 事务协调

当前 `ModelContainer`、`MultipleTranContainer` 等核心事务路径存在重叠。`dec-expand-declaration` 不作为抽取来源，建议在核心模块内重新定义统一契约：

```text
TransactionCoordinator
TransactionScope
TransactionPolicy
RollbackPolicy
```

先支持：

- 单数据源本地事务；
- 多连接同成功提交、失败回滚的协调；
- 明确“不提供真正分布式原子性”的边界。

以后再通过 SPI 接入 Saga、TCC 或外部事务管理器。

### 14.3 执行失败语义

Action、Produce、Change、Information、Directory 任一步失败时：

- 停止后续步骤；
- 标记失败节点；
- 按事务策略回滚；
- 执行明确配置的 Back/补偿；
- 保留完整 trace；
- 不允许 `printStackTrace` 后继续返回空结果。

---

## 15. EngineContext 与生命周期整改

### 15.1 去全局可变单例

当前 ConfigManager、各 Config 单例、静态 Rule Map 会造成：

- 测试污染；
- 多项目配置互相覆盖；
- 热加载时读到半成品；
- 并发初始化风险；
- 无法并存多个业务版本。

目标：

```java
EngineContext context = compiler.compile(documentSet);
BusinessEngine engine = new BusinessEngine(context, runtimePlugins);
```

### 15.2 发布模型

使用构建后一次发布：

```text
LOADING -> COMPILING -> VALIDATED -> READY
```

只有 READY Context 对外可见。热加载使用新 Context 原子替换，旧执行继续使用旧快照。

### 15.3 版本与缓存

CompiledModelSet 包含：

- business version；
- source digest；
- compile time；
- schema version；
- dependency digests。

Information 结果缓存、QueryPlan 缓存均绑定该版本。

---

## 16. `dec-expand-declaration` 整体退役方案

### 16.1 决策边界

该模块是临时项目，不属于目标架构。System、Business、Information、Action、Produce、Directory、Query、Session 和事务均不得依赖其代码。

不执行以下工作：

- 不抽取 Produce/Consume SPI；
- 不迁移 SystemDesc/BusinessDesc；
- 不提供 LegacyDeclarationAdapter；
- 不兼容旧 declaration XML/YAML；
- 不保留独立 parser、ContextStorage、workflow engine 或 artifact。

### 16.2 替代能力归属

| 旧模块中曾验证的概念 | 新实现位置 | 新语义来源 |
|---|---|---|
| System | P2 / System compiler | `mix/system` |
| Business | P1/P2 / BusinessScope | `mix` 文件聚合关系 |
| Produce/输入依赖 | P3/P4 | Information、ActionInput、Produce |
| 流程与回调 | P5 | DirectoryEngine、ActionPipeline、Trace |
| Query | P6 | QueryPlan |
| Session/事务/回滚 | P7 | ExecutionSession、TransactionCoordinator、CompensationPolicy |

以上均为重新设计，不复制旧模块实现。

### 16.3 删除路径

```text
P1：删除 Maven module、依赖、源码与正式配置入口；把必要场景按 mix 重写到 dec-demo
P2—P6：所有新能力只基于统一 Compiled AST 实现，并持续执行依赖隔离测试
P7：验证统一 Session/Transaction/Runtime，不存在旧模块 Adapter 或复制 runtime
P8：执行仓库、Reactor、依赖树、artifact 和文档残留扫描，发布清单中不得出现旧模块
```

P0 的历史构建证据可以保留，但不构成继续保留该模块的理由。

---

## 17. 基线质量整改

### 17.1 构建门禁

根 POM 修改：

- 明确 Java 版本；
- 固定插件版本；
- `maven-surefire-plugin/testFailureIgnore` 改为 false；
- `dec-demo` 加入可验证的 integration profile；
- 添加 Maven Wrapper；
- CI 执行 clean verify；
- 禁止依赖 `target` 中历史产物。

### 17.2 测试结构

当前绝大多数模块没有标准单元测试，多个 “Tests” 是 `src/main/java` 中的 main 方法。需要迁移到：

```text
src/test/java
src/test/resources
```

测试必须无本地固定 MySQL 密码和端口依赖。

### 17.3 错误处理

全仓静态扫描发现大量：

- `return null`；
- TODO；
- `System.out`；
- `printStackTrace`；
- raw type；
- 反射 `newInstance()`；
- 空 Factory；
- 重复工具类和拼写错误类名。

整改要求：

- 用 Optional、空集合或明确异常代替语义不明的 null；
- 所有运行时失败使用结构化异常与 Diagnostic；
- 全部改为 SLF4J；
- 清理 dead code 和大段注释旧实现；
- 为核心接口加泛型和不可变约束。

### 17.4 已发现的基线缺陷示例

- XML `ConfigFileParser` 不负责创建 ConfigInfo，依赖外部调用顺序；
- `AbstractQuery(String connectionName)` 在 `con` 初始化前访问 `con`；
- `DirectoryContainer.find()` 强制 root，与目标查询语义冲突；
- `DirectoryContainer.getCondition()` 在 start 未设置时可能空指针；
- `DirectoryContainer` 硬编码 `con1`；
- `DirectoryFileParser` 对不存在的 rel 未给出清晰诊断；
- `RuleConfig` 仅以名称为 Key；
- XML/YAML 加载初始化行为不同；
- `AbstractConfig` 仍是空实现；
- 父 POM 忽略测试失败。

这些问题应在 P0/P1 阶段先修复或由新架构替换。

---

## 18. 测试方案

### 18.1 Parser Contract Test

对 `mix` 每种 XML 元素测试：

- 正常解析；
- 缺失必填属性；
- 未知属性；
- 未知元素；
- 重复定义；
- 错误引用；
- XML/YAML 等价 AST。

### 18.2 Compiler Test

覆盖：

- System / RuleView 二元 Key；
- 模型路径存在性；
- 写权限；
- Information 三种合法形式；
- Information 循环；
- Produce 映射；
- Directory 可达性与环；
- CASE 唯一分类；
- Back 路径；
- Query 可下推能力。

### 18.3 Information Engine Test

覆盖：

- `user.effective = activated AND certified`；
- `order.payable = ordered OR waitPay`；
- 模型字段变化后的增量失效；
- `change-data` 物化；
- RuleView Information true / false / unresolved；
- 复合 Information 不能被直接 Produce 映射。

### 18.4 Directory Engine Test

使用内存数据源和假 Action：

```text
ordered -> paying -> PayResult -> success
ordered -> paying -> PayResult -> error
error -> paying back
```

验证：

- startPay 只执行一次且只在 paying；
- PayResult 不执行支付发起；
- success/error 唯一分类；
- 失败后不执行后续 Action；
- 直接 execute("success") 不跳过路径；
- Back 逐级执行。

### 18.5 Query Test

必须验证：

```text
find("PayResult")               -> success OR error
find("PayResult").eq("success") -> success only
find("PayResult").eq("error")   -> error only
```

另验证：

- paying 不进入 PayResult case 查询；
- path range；
- with UserInfo；
- 参数绑定；
- one-to-many 分页；
- runtime-only Information 的回退策略。

### 18.6 Datasource Integration Test

建议：

- H2 或内存适配器用于快速测试；
- MySQL 使用 Testcontainers profile；
- 无 Docker 环境时仍能跑绝大多数 compiler/runtime 测试；
- SQL 快照测试验证 QueryPlan 翻译。

### 18.7 `mix` 验收测试

`mix` 应成为正式 contract fixture：

```text
dec-demo/src/test/resources/mix
```

测试启动必须完成：

1. 全文件加载；
2. 编译零错误；
3. System/Rule/Information/Directory 数量断言；
4. 三条主流程执行；
5. 三类查询执行；
6. 权限拒绝用例；
7. 错误配置诊断用例。

---

## 19. 分阶段实施计划

### P0：构建与保护基线

范围：

- Maven Wrapper；
- CI；
- 禁止忽略测试失败；
- 当前 public API 冒烟测试；
- 将 main 测试迁入 test；
- 建立 `mix` contract test 空壳；
- 记录旧行为快照。

验收：

```text
./mvnw clean verify
```

可在无外部 MySQL 时通过核心测试。

### P1：统一 AST、Registry 与 Compiler 骨架

范围：

- 新增 `dec-core-compiler`；
- Raw / Compiled 定义与逻辑 BusinessScope；
- 强类型 Key、Diagnostic、EngineContext；
- 删除固定 Config 数组扩展方式；
- 旧核心 Config 仅保留临时只读 adapter；
- 整体删除 `dec-expand-declaration` 模块和依赖；必要场景按 `mix` 重写到 `dec-demo`。

验收：

- 可把现有 Data/View/Rule 编译到新 Context；
- 同一 JVM 可同时存在两个 Context；
- 错误集合包含文件与位置；
- 仓库、Reactor 和依赖树不再包含废弃模块。

### P2：System、BusinessScope 与 RuleView 归属

范围：

- `system-file-info`；
- System parser；
- 逻辑 BusinessScope；
- model access；
- RuleView system 属性；
- RuleViewKey；
- 写路径静态校验和运行时屏障。

BusinessScope 只存在于统一 AST/EngineContext，不是独立项目，也不映射旧 declaration 模型。

验收：

- `mix/system/systems.xml` 编译通过；
- 越权写 `payInfo` 的 order RuleView 被拒绝；
- 同名 RuleView 可存在于不同 System。

### P3：Information Engine

范围：

- Information parser；
- 两套表达式编译器；
- Information DAG；
- 识别结果；
- materializer；
- 增量失效；
- 将消费语义统一为 Information 依赖、模型/View read-set，不建立独立 Consumer runtime。

验收：

- `mix` 16 个 Information 全部编译；
- 组合结果正确；
- 修改模型后只重算受影响节点；
- 循环和非法引用有稳定诊断。

### P4：Action、输入契约、Produce 与自定义 SPI

范围：

- 新 Action runtime；
- RuleView invoker；
- CustomActionRegistry；
- ActionInput / ActionResult；
- ProduceVerifier；
- payload contract；
- 失败策略。

所有接口直接依据 `mix` 定义，不迁移旧 Producer/Consumer SPI。

验收：

- startPay、receivePayResult、recordPyaError 可通过统一接口执行；
- smsNotify 可注册并校验；
- Produce 缺失时流程失败；
- `information-ref` 验证生效。

### P5：Directory 图、执行与 Back

范围：

- Execution / Case edge；
- path planner；
- dependency gate；
- directory state machine；
- classification；
- BackPlan；
- trace。

流程执行只由统一 DirectoryEngine 根据 `mix` Compiled AST 完成，不调用旧 workflow runtime。

验收：

- 成功、失败、返回三条流程通过；
- paying 不作为 PayResult case；
- 直接目标执行不跳过路径；
- Action 顺序和 fail-fast 正确。

### P6：QueryPlan 与 SQL 适配

范围：

- 新 DirectoryQuery API；
- Information predicate compiler；
- JoinPlan；
- SQL parameter binding；
- SQL/MySQL translator；
- runtime-only 策略。

QueryCompiler 只依赖统一 EngineContext/Compiled AST，不读取旧 declaration Context 或 Business runtime。

验收：

- PayResult union 查询；
- eq success/error；
- path query；
- with UserInfo；
- 无硬编码连接和 SQL 注入路径。

### P7：事务、会话与核心运行时收敛

范围：

- TransactionCoordinator；
- ExecutionSession/ConnectionRoute；
- RuntimeComponentRegistry；
- 统一错误模型；
- execution trace；
- 外部副作用与 CompensationPolicy；
- 资源生命周期和并发隔离；
- 废弃模块残留门禁。

验收：

- 多 System 流程共享统一执行上下文；
- 失败回滚与外部副作用符合策略；
- 不存在 `dec-expand-declaration`、Adapter 或复制的第二套 runtime。

### P8：YAML 对等、清理与正式发布

范围：

- XML/YAML 同 AST；
- legacy 属性删除；
- dead code、空 Factory、重复工具类清理；
- 废弃模块、artifact、包、文档和复制实现残留扫描；
- 文档和示例更新；
- 性能、并发和热加载测试。

验收：

- XML/YAML 编译产物 digest 一致；
- `ref-rule` 被明确拒绝；
- 全仓测试通过；
- `mix` 不再含“当前解析器未必支持”的说明；
- EngineContext 可安全并发读取和原子热替换。

---

## 20. 建议的代码改动清单

### `dec-core-context`

- 新增 System、Business、Information、Action、Produce 编译模型；
- 重写 Directory 模型；
- RuleView 增加 System 归属并改复合 Key；
- 引入 EngineContext / Registry；
- 逐步移除全局 Config 单例。

### `dec-core-compiler`（新增）

- symbol table；
- reference resolver；
- graph compiler；
- expression compiler；
- model path compiler；
- access validator；
- diagnostic collector；
- compiled model publisher。

### `dec-context-config-parse-xml`

- System / Business / Information / Produce / Back parser；
- Loader Pipeline；
- strict attribute/element validation；
- source location；
- 资源加载健壮性。

### `dec-context-config-parse-yaml`

- 改为统一 CanonicalDocumentNode；
- 删除重复业务校验；
- 与 XML 共用 compiler tests。

### `dec-core-model`

- InformationEngine；
- DirectoryEngine；
- PathPlanner；
- ActionRuntime；
- ProduceVerifier；
- CustomActionRegistry；
- QueryCompiler；
- TransactionCoordinator；
- ExecutionTrace。

### `dec-core-datasource`

- QueryPlan / DataCommand 中立接口；
- typed parameter；
- connection route；
- capability declaration。

### `dec-datasource-orm-sql`

- QueryPlan -> SQL；
- join/projection/predicate 编译；
- 全参数化；
- 结果装配计划。

### `dec-datasource-orm-mysql`

- MySQL 方言实现；
- 类型转换修正；
- MySQL integration test；
- grammar 文件与生成代码来源统一。

### `dec-core-starter`

- EngineBuilder；
- compile-and-publish；
- plugin registration；
- readiness validation；
- 生命周期管理。

### `dec-expand-declaration`（删除）

- 从根 Reactor 和 dependencyManagement 删除；
- 删除模块源码、资源、测试和 artifact；
- 不提供 Adapter，不抽取 runtime SPI；
- 必要业务场景基于 `mix` 在 `dec-demo` 重新实现测试；
- 增加依赖树和文本残留检查，防止重新引入。


### `dec-demo`

- 将 `mix` 转成正式测试 fixture；
- 内存测试数据源；
- 成功/失败/back/query/权限端到端测试；
- 移除硬编码数据库账号密码。

---

## 21. 最终验收标准

框架整改完成必须同时满足：

0. 仓库、Reactor、依赖树和发布 artifact 中不存在 `dec-expand-declaration`，且没有 LegacyDeclarationAdapter；
1. `mix/orm-config.xml` 可直接加载；
2. System、RuleView、Information、Directory 全部进入统一编译模型；
3. `rule-view-info/@system` 在解析、注册、执行和权限检查中真实生效；
4. Information 识别、物化、组合、增量重算完整；
5. 普通 subdirectory 默认按子到父执行；
6. paying 只执行支付操作，不属于 PayResult case；
7. PayResult 只接收和分类支付结果；
8. success/error 自动且唯一分类；
9. Action、Produce、Dependency、Change、Back 均有运行时实现；
10. `find("PayResult")` 返回 success + error；
11. `.eq("success")` 和 `.eq("error")` 正确过滤；
12. 查询使用 QueryPlan 和参数化 SQL；
13. System 越权写入在编译期或运行时被拒绝；
14. XML/YAML 生成同一编译产物；
15. 不生成大量业务 Java 类，由引擎解释执行；
16. 不再存在两套并行业务运行时；
17. 无外部数据库时核心 compiler/runtime 测试可运行；
18. CI 不忽略任何测试失败；
19. 执行日志可还原路径、Action、RuleView、Produce、Information、Change、分类、Back 和事务结果；
20. 所有新目标语义均以自动化测试锁定。

---

## 22. 实施顺序结论

不能从修改 `DirectoryParser` 开始局部打补丁。正确顺序是：

```text
构建门禁
-> 统一 AST / Compiler / EngineContext
-> System + RuleView 归属
-> Information Engine
-> Action + Produce
-> Directory 执行
-> QueryPlan
-> Transaction、Session 与核心 runtime 收敛
-> YAML 对等与清理
```

原因是 Directory 的执行和查询都依赖 System、RuleView、Information、模型权限和 Produce 契约。若先扩展旧 DirectoryContainer，会把新语义继续耦合到字符串 SQL、全局单例和旧 Change 模型中，后续仍需二次重写。

本方案建议先完成 P0-P3，得到可编译的 `mix` 元模型和 Information Engine，再进入真正的业务执行实现。这样每个阶段都有独立产物和测试门禁，可以持续交付，而不是一次性替换整个框架。
