# 基于 `mix` 的 P0—P8 详细任务实施手册

> 正式事实源：`project_doc/docs/_plans/mix-framework-p0-p8-detailed-task-plan.md`。其他同名路径仅保留迁移跳转，不再维护正文。

## 1. 文档定位

本文是 `project_doc/docs/_plans/mix-framework-technical-remediation-plan.md` 的执行级配套文档，用于把 P0—P8 从架构阶段拆成可持续执行、可验收、可交接的具体任务，避免后续因会话、人员或 Agent 记忆变化而偏离目标。

本文固定以下内容：

- 每个阶段的当前情况、整改目标、前置条件和退出门禁；
- 每项任务的编号、措施、影响模块、交付物和验收证据；
- 明确禁止提前实施的事项；
- 阶段间依赖和必须维护的交接记录；
- 任何目标语义变化必须经过 ADR，不得依赖临时对话解释。

---

## 2. 不可变目标语义

### 2.1 总体执行模式

```text
XML / YAML
    -> 统一 Raw AST
    -> 编译、引用解析、图构建和静态校验
    -> 不可变 CompiledBusiness / EngineContext
    -> 统一解释执行引擎
```

不得把 `mix` 转换成大量业务 Java 类作为主要执行方式。

### 2.2 RuleView 标识

RuleView 使用二元标识：

```text
(system, rule-view-info/@name)
```

不得退回只按 `name` 全局注册。

### 2.3 Information 边界

- `rule-data`、`change-data` 只能访问 `model-ref` 对应模型数据；
- `expression` 只能引用 Information；
- 识别与物化分离；
- 复合 Information 不允许直接物化；
- Information 依赖必须无环；
- 模型变化只失效并重算受影响节点。

### 2.4 Directory 与支付流程

普通 `subdirectory` 默认表示子目录执行完成后向父目录执行，不使用 `role="predecessor"`。

`role="case"` 只表示结果目录的一种业务情况。

```text
ordered -> paying -> PayResult -> success / error
```

- `startPay` 只在 `paying`；
- `paying` 是中间执行过程，不是支付结果；
- `PayResult` 只接收、保存并分类支付结果；
- `success`、`error` 才是 PayResult case。

### 2.5 查询语义

```text
find("PayResult")               -> success OR error
find("PayResult").eq("success") -> success only
find("PayResult").eq("error")   -> error only
```

查询必须由 Information、case 图和 View 映射编译为 QueryPlan，不得硬编码目录名、状态值或连接名。

---

## 3. 执行治理

### 3.1 阶段顺序

```text
P0 -> P1 -> P2 -> P3 -> P4 -> P5 -> P6 -> P7 -> P8
```

阶段、阶段内任务和 Review 当前全部串行执行。只有当前阶段退出门禁全部通过，才允许进入下一阶段；同一时刻只允许一个任务或一个 Review 处于 `IN_PROGRESS`/`REVIEWING`。多个 Review Agent 按任务计划登记顺序逐个执行，不得并行调度，也不得跨阶段提前实现依赖尚未稳定的能力。

### 3.2 状态与证据

正式开发时建立：

```text
docs/remediation/
├── status.md
├── decisions/
├── evidence/
├── P0/
├── P1/
├── P2/
├── P3/
├── P4/
├── P5/
├── P6/
├── P7/
└── P8/
```

每阶段维护：

- `task-status.md`：任务状态、负责人、开始和完成时间；
- `decisions.md`：阶段设计决定和 ADR；
- `evidence.md`：测试命令、日志、快照和报告；
- `known-issues.md`：已知问题及后续任务；
- `handoff.md`：稳定接口和下一阶段启动条件。

任务状态只允许：`TODO`、`IN_PROGRESS`、`BLOCKED`、`REVIEWING`、`PASSED`、`FAILED`、`NA`。

代码完成不等于任务通过；必须有测试、构建、诊断、Trace、QueryPlan、性能报告或 ADR 等证据。

### 3.3 提交规则

- 提交消息包含阶段与任务编号，例如 `P2-T05 add system-scoped RuleView registry`；
- 不混入无关修改；
- 兼容代码必须标注删除阶段；
- 改变固定目标语义前必须提交 ADR，并同步更新两份整改文档和 `mix`。

---

# P0：构建与保护基线

## P0 当前情况

- 根 POM 及多个模块配置 `testFailureIgnore=true`；
- `dec-demo` 未进入默认 Reactor；
- 多个测试位于 `src/main/java`，通过 `main` 手工执行；
- 部分测试依赖 MySQL、固定连接和硬编码配置；
- 没有 Maven Wrapper；
- XML/YAML、Directory、declaration 的旧行为没有统一快照；
- `mix` 还不是自动化 contract fixture。

## P0 整改目标

建立可重复构建、失败可阻断、无外部 MySQL可运行的工程基线；固定当前 public API 和旧行为，为 P1—P8 提供可信回归门禁。P0 不实现新业务语义。

## P0 任务

### P0-T01 冻结整改前基线

- 措施：记录起始 commit、模块、源码/测试/资源数量、Java/Maven 版本、当前构建问题、硬编码连接、main 测试和已知缺陷。
- 影响：根 POM、各模块 POM、`dec-demo`、`dec-expand-declaration`。
- 交付：`docs/remediation/P0/baseline.md` 和可重复生成清单的脚本。
- 验收：基线可从同一 commit 重复生成，未把目标行为误写为当前行为。

### P0-T02 引入 Maven Wrapper 与统一 JDK

- 措施：增加 `mvnw`、`.mvn/wrapper`，父 POM 固定 JDK 与 Maven 版本，加入 Enforcer。
- 验收：`./mvnw -version` 与 `./mvnw -DskipTests package` 可执行。

### P0-T03 集中构建插件

- 措施：compiler、surefire、failsafe、jacoco、enforcer 统一到父 POM；删除所有 `testFailureIgnore=true`。
- 验收：任意失败测试使 Reactor 返回非零；插件版本不在子模块漂移。

### P0-T04 恢复完整 Reactor

- 措施：将 `dec-demo` 纳入默认构建，或建立有明确恢复条件的 profile；输出模块依赖图。
- 验收：正式模块不再通过注释静默排除。

### P0-T05 迁移 main 测试

- 措施：将 `DirectoryTest`、`RuleTests`、`OrderTest`、declaration 业务场景迁入 `src/test/java`，使用 JUnit 断言。
- 验收：核心行为无需人工观察控制台；演示入口与测试职责分离。

### P0-T06 隔离数据库依赖

- 措施：parser/compiler/runtime 单测使用内存适配器或测试双；MySQL 放入 `mysql-it` profile；账号密码由环境提供。
- 验收：无 MySQL、无 Docker时 `./mvnw clean verify` 通过核心测试。

### P0-T07 建立旧 XML/YAML 契约快照

- 措施：为 Data、Relation、View、Rule、Directory 建最小双格式 fixture，记录解析结果和已知差异。
- 验收：快照稳定，差异有明确后续任务，不在 P0 随意修复语义。

### P0-T08 建立 `mix` contract 骨架

- 措施：在测试资源建立 `mix` fixture；P0 只验证文件完整、XML well-formed、引用文件存在，并为后续实体数量和编译断言留位。
- 验收：资源损坏测试失败；未支持项明确 pending，不能注释整个测试。

### P0-T09 建立 CI

- 措施：PR 与 `dev_all` push 执行 Wrapper 构建，上传测试报告；MySQL IT 独立 job。
- 验收：故意失败测试时 CI 阻断。

### P0-T10 建立异常和日志基线

- 措施：盘点 parser、runtime、query、transaction 中 swallowed exception、printStackTrace、null 成功等位置，映射到 P1/P7。
- 验收：所有高风险点有后续任务编号。

## P0 禁止事项

不得实现 Information、Directory 新状态机、RuleView 复合 Key、declaration 删除；不得通过忽略测试或吞异常让构建变绿。

## P0 退出门禁

- [ ] Wrapper 和 CI 可重复构建；
- [ ] 测试失败阻断；
- [ ] 无 MySQL核心测试通过；
- [ ] `dec-demo` 进入正式构建；
- [ ] 旧行为快照和 `mix` 骨架存在；
- [ ] P1 可依赖稳定基线。

---

# P1：统一 AST、Registry 与 Compiler 骨架

## P1 当前情况

- `ConfigFactory` 和多个全局 Config 使用固定类型槽位；
- XML/YAML parser 直接写运行时 Config；
- 无统一 Raw AST、source location 和集中 Diagnostic；
- 同 JVM难以安全加载多个项目或版本；
- parser、配置存储和 runtime 边界不清。

## P1 整改目标

建立格式中立 AST、Compiler Pipeline、强类型 Key、不可变 Registry 和实例级 EngineContext；旧 Config 仅通过只读 adapter 迁移。

## P1 任务

### P1-T01 新增 `dec-core-compiler`

- 建立独立模块，不依赖 SQL/MySQL/demo；定义 parser、compiler、runtime 边界和架构测试。

### P1-T02 定义 CanonicalDocumentNode

- 字段：类型、属性、标量、顺序子节点、文件、行列/路径、格式、schema 版本。
- 验收：同语义 XML/YAML 可规范化为等价节点。

### P1-T03 定义 Raw AST

- 建立 RawConfig、Data、View、RuleView、System、Information、Directory、Action、Produce 和 SourceRef；字段覆盖 `mix`，即使部分运行语义后续才实现。

### P1-T04 定义 SourceLocation 与 Diagnostic

- 建立 severity、稳定错误码、文件/行列/实体 Key；Compiler 聚合普通错误并稳定排序。

### P1-T05 建立强类型 Key

- 至少包括 DataKey、ViewKey、RuleViewKey、SystemKey、InformationKey、DirectoryKey、BusinessKey、ActionKey。
- `RuleViewKey` 预留 `(system,name)`。

### P1-T06 建立 SymbolTable 与 RegistryBuilder

- 支持重复检测、跨文件前向引用、按类型遍历；编译结束发布不可变 Registry。

### P1-T07 定义 Compiled AST

- Compiled 对象不保留未解析字符串引用，不依赖 DOM/YAML Node，可生成稳定 digest。

### P1-T08 实现 Compiler Pipeline

```text
parse -> structural validation -> symbol registration -> reference resolution -> graph preparation -> semantic validation -> publish
```

每个 pass 独立测试。

### P1-T09 建立 EngineContext

- 持有 compiled registries、datasource metadata、plugins、schema/version/digest；实例不可变，不使用全局当前 Context。

### P1-T10 建立旧 Config Adapter

- 新 Context 可投影为旧 Config 只读视图；adapter deprecated，新代码禁止通过 adapter 注册。

### P1-T11 改造 XML 前端

- 新增 `XmlDocumentFrontend`；未知属性严格校验；资源错误进入 Diagnostic；新能力不直接写全局 Config。

### P1-T12 建立 YAML 同一前端接口

- P1 实现最小等价路径，完整对等放 P8。

### P1-T13 Compiler 测试

- 覆盖重复定义、未知引用、前向引用、多文件、source location、Context 隔离、digest 和 Diagnostic 顺序。

## P1 禁止事项

不得在 P1 实现完整 System 权限、Information 求值、Directory 状态机；不得让 Compiled AST 持有解析器节点；EngineContext 不得成为新全局单例。

## P1 退出门禁

- [ ] `dec-core-compiler` 进入 Reactor；
- [ ] XML 与最小 YAML生成统一 Raw AST；
- [ ] Data/View/Rule 可编译进 EngineContext；
- [ ] 两个 Context 无污染；
- [ ] Diagnostic 有错误码和位置；
- [ ] 旧 Config adapter 只读。

---

# P2：System 与 RuleView 归属

## P2 当前情况

- `mix/system/systems.xml` 未进入核心配置；
- `rule-view-info/@system` 已写入 XML，但现有模型、Parser、Registry 未完整执行；
- RuleView 可能只按名称注册；
- 共享模型缺少读写路径权限；
- declaration 有另一套 System，不能继续作为第二套核心实现。

## P2 整改目标

System 成为一等编译实体；RuleView 以 `(system,name)` 注册和调用；model-access 编译为静态与运行时权限屏障。

## P2 任务

### P2-T01 定义 System Raw/Compiled 模型

- 包含 key、版本、data/view 引用、model access、RuleView 集合和 source location。

### P2-T02 实现 `system-file-info` 加载

- 加入 Loader Pipeline，支持多文件、重复检测和顺序无关；`mix/system/systems.xml` 成为 fixture。

### P2-T03 定义 ModelAccessRule

- 字段：system、model/view、path、READ/WRITE/EXECUTE、source；默认最小权限、写入默认拒绝。

### P2-T04 实现 ModelPathCompiler

- 强类型编译 `OrderInfo.payInfo.resultCode`，校验属性、集合导航并生成标准路径；表达式、change、query 共用。

### P2-T05 修改 RuleView 模型

- `RuleViewInfo` 增加 system；Registry 使用 RuleViewKey；新 `mix` 中 system 必填，旧配置只经 adapter。

### P2-T06 改造 RuleParser

- 读取 system；校验 system/view/rule 引用；路径访问进入 compiler 校验；错误定位到 `rule-view-info`。

### P2-T07 静态访问校验

- 检查 order 写 payInfo、payment 写订单状态、user 越界访问；动态 grammar 无法静态确认时标记 runtime check。

### P2-T08 实现 ModelAccessGuard

```text
ModelMutationService -> ModelAccessGuard -> mutation
```

Rule、change、custom action 禁止绕过统一写入服务。

### P2-T09 改造 RuleView 查找调用

- Action 的 `system-ref + rule-ref` 编译时直接解析 CompiledRuleView；禁止裸名称查找。

### P2-T10 同名 RuleView 隔离测试

- 建立 `order.validate` 与 `payment.validate`，验证注册、查找、执行不覆盖。

### P2-T11 越权测试矩阵

- 合法读写、未声明读写、只读路径写入、父路径、集合、自定义 Action、动态 grammar。

### P2-T12 declaration System 迁移边界

- 记录旧 SystemDesc 到新 CompiledSystem 的映射和不能映射能力，P7 收敛入口。

## P2 禁止事项

不得让 system 只是文档字段；不得按包名推断 System；不得默认允许共享模型写入；不得在 P2 删除旧 declaration 入口。

## P2 退出门禁

- [ ] systems.xml 编译通过；
- [ ] `mix` RuleView 均有有效 System；
- [ ] RuleViewKey 全链路生效；
- [ ] 同名隔离通过；
- [ ] model-access 静态和运行时校验通过。

---

# P3：Information Engine

## P3 当前情况

核心框架没有 Information 一等模型、DAG、识别结果、物化和增量失效；旧 Change 与目标 Information 语义不同；两套表达式语言尚未隔离。

## P3 整改目标

建立三类 Information、两套表达式编译器、DAG、RecognitionResult、Materializer 和增量失效，使 RuleView 与 rule-data 识别进入统一 API。

## P3 任务

### P3-T01 定义 Information 类型

- `RULE_VIEW_ATOMIC`、`MODEL_EXPRESSION_ATOMIC`、`COMPOSITE`；CompiledInformation 包含 recognizer、materializer、依赖、读写路径和缓存策略。

### P3-T02 实现 InformationParser

- 解析 system/model/rule/rule-data/change-data/expression；校验互斥组合；复合 Information 禁止 materializer。

### P3-T03 实现 ModelExpressionCompiler

- 仅允许模型路径、常量和受控函数；产出可执行 AST与读写路径；禁止引用 Information。

### P3-T04 实现 InformationExpressionCompiler

- 仅允许 InformationKey、AND/OR/NOT；禁止模型字段；提取依赖边。

### P3-T05 构建 Information DAG

- 循环检测、拓扑顺序、reverse dependency index、稳定图摘要。

### P3-T06 定义 RecognitionResult

- 状态 `TRUE/FALSE/UNRESOLVED/ERROR`，包含证据、依赖结果、错误和模型版本；UNRESOLVED 不等于 FALSE。

### P3-T07 RuleViewRecognizer

- 调用 system-scoped RuleView；识别过程不得隐式修改模型；输出 evidence 与 trace。

### P3-T08 ModelExpressionRecognizer

- 明确 null、空集合、缺失路径语义；错误为 ERROR，不静默 false。

### P3-T09 CompositeRecognizer

- 短路计算，定义 UNRESOLVED 传播，只访问依赖 Information。

### P3-T10 InformationMaterializer

- 只支持声明 `change-data` 的原子 Information；写入经 Guard；物化后重新识别；返回 MutationSet。

### P3-T11 增量失效

- 建立 model path 到 Information 索引；MutationSet 精确失效并沿 reverse DAG 传播。

### P3-T12 InformationEngine API

```text
evaluate(key, modelContext)
materialize(key, modelContext)
invalidate(mutationSet)
explain(key)
```

### P3-T13 `mix` Information 测试

- 覆盖 user、order、payment 共 16 个 Information，组合、物化和重算。

### P3-T14 错误配置测试

- expression 引用模型、rule-data 引用 Information、越权 change、复合 materializer、循环、缺失引用。

## P3 禁止事项

不得用旧 Directory Change 代替 Information；不得把 `payment.success` 放入 rule-data；不得把 ERROR 当 FALSE；不得在复合 Information 中写模型。

## P3 退出门禁

- [ ] 全部 `mix` Information 编译；
- [ ] 两套表达式严格隔离；
- [ ] DAG、结果状态、materialize、增量失效有测试；
- [ ] P4/P5 可稳定调用 InformationEngine。

---

# P4：Action、RuleView 与 Produce

## P4 当前情况

旧 Action 缺少统一执行上下文；`ref-rule` 与 `rule-ref` 混杂；自定义 Action SPI 不匹配业务 Action；核心 runtime 无 Produce 契约；Rule 执行结果不能表达产物、mutation 和 evidence。

## P4 整改目标

建立统一 Action Runtime、RuleView Invoker、CustomActionRegistry、ActionResult、ProduceVerifier 和失败策略。

## P4 任务

### P4-T01 定义 Raw/Compiled Action

- 包含 owner directory、system、rule/custom type、payload、produce、failure policy 和 source。

### P4-T02 新旧属性策略

- 新契约只用 `rule-ref`；`ref-rule` 仅 legacy adapter；同时出现必须报错。

### P4-T03 定义 ActionExecutionContext

- EngineContext、Directory、System、model、payload、InformationEngine、transaction、trace、deadline。

### P4-T04 定义 ActionResult

- status、produced data、MutationSet、payload、diagnostics、error、evidence；禁止 null 表示结果。

### P4-T05 RuleViewActionInvoker

- 使用 RuleViewKey；写入经 Guard；Rule fail-fast；汇总 MutationSet 并触发 Information 失效。

### P4-T06 CustomAction SPI

- `validate`、`execute`、`supports`；实例级注册表，重复检测、readiness validation、测试替身。

### P4-T07 `smsNotify` 测试实现

- 验证未注册时编译失败、执行 trace；核心模块不硬编码短信服务。

### P4-T08 定义 Produce

- ref/type、information-ref、payload source、required、multiplicity、scope、source。

### P4-T09 ProduceVerifier

- 验证产物存在、类型、数量、Information 成立；复合 Information 不被错误物化。

### P4-T10 Action Pipeline

```text
precondition -> invoke -> apply mutation -> invalidate -> verify produce -> verify directory information -> trace
```

### P4-T11 迁移 `mix` Action

- saveOrder、startPay、receivePayResult、confirmPaySuccess、recordPyaError、smsNotify、confirmPayError、resetPayResult。

### P4-T12 Rule 内部执行一致性

- 顺序、fail-fast、识别/写入区分、统一返回、反射写入纳入 MutationSet。

### P4-T13 Action/Produce 测试

- 成功、失败、未注册、Produce 缺失、Information 不成立、越权 mutation、trace。

## P4 禁止事项

不得建立全局静态 CustomActionRegistry；不得忽略 Produce；不得让 Action 自管独立事务；不得在 P4 实现 Directory 路径规划。

## P4 退出门禁

- [ ] `rule-ref` 全链路生效；
- [ ] RuleView与Custom Action 共用 ActionResult；
- [ ] `mix` 关键 Action 可独立执行；
- [ ] Produce 和权限校验可阻断；
- [ ] mutation 触发 Information 失效。

---

# P5：Directory 图、执行、分类与 Back

## P5 当前情况

旧 Directory 以 `view-ref`、Change、any-one、mutual-exclusion 为中心；`DirectoryContainer.execute()` 无完整实现；普通执行边与 case 边未区分；Dependency、Produce、Back、分类无运行时。

## P5 整改目标

将 Directory 编译为有类型图；实现 PathPlanner、状态机、DependencyGate、Action/Change/Produce/Postcondition、PayResult 分类、BackPlan 和 Trace。

## P5 任务

### P5-T01 重定义 Directory Raw/Compiled

- name/type/model/information/root/dependencies/actions/change/execution children/case children/back/source。

### P5-T02 定义有类型边

- `ExecutionEdge(child -> parent)`、`CaseEdge(parent -> case)`、`BackEdge`；编译后分开存储。

### P5-T03 实现新 DirectoryParser

- 读取 model-ref、information-ref、Dependency、Action、Produce、case、Back；无 role 为 execution edge；拒绝 `role="predecessor"`。

### P5-T04 DirectoryGraphCompiler

- 唯一性、循环、case 目标、root、可达性、多父策略、Back 路径；验证 paying 不在 PayResult cases。

### P5-T05 PathPlanner

- root/当前目录到目标、禁止跳过、歧义检测、case 不作普通前置、输出可解释 PathPlan。

### P5-T06 DirectoryExecutionContext

- EngineContext、路径、模型、payload、transaction、InformationEngine、ActionRuntime、trace、options。

### P5-T07 Directory 状态机

```text
PLANNED -> CHECKING_DEPENDENCIES -> EXECUTING_ACTIONS -> APPLYING_CHANGE -> VERIFYING_PRODUCE -> VERIFYING_INFORMATION -> CLASSIFYING -> COMPLETED/FAILED/BACK
```

### P5-T08 DependencyGate

- 调用 InformationEngine；FALSE/UNRESOLVED/ERROR有确定处理；不隐式物化。

### P5-T09 接入 ActionPipeline

- XML 顺序、fail-fast、失败后不执行 change/produce/postcondition。

### P5-T10 Change 阶段

- `change-info` 调用 InformationMaterializer，不解释旧 Change SQL 文本；物化后验证成立。

### P5-T11 Directory 后置验证

- 自身 `information-ref` 必须 TRUE 才完成。

### P5-T12 Result Classification

- 父目录 Action 后重算 case；success/error 必须唯一；paying 不参与；选择 case 进入后续执行。

### P5-T13 BackPlan

- 起点、目标、逐级路径、每级 back action、mutation、事务策略和返回后当前目录。

### P5-T14 ExecutionTrace

- 记录 Path、Dependency、Action、RuleView、Produce、Change、Information、Classification、Back、Transaction、Error。

### P5-T15 成功流程测试

- `ordered -> paying -> PayResult -> success`；startPay 一次；PayResult 只接收结果；success 唯一。

### P5-T16 失败流程测试

- `ordered -> paying -> PayResult -> error`；验证 recordPyaError、smsNotify、confirmPayError 顺序。

### P5-T17 Back 测试

- error 返回 paying，执行 resetPayResult，恢复可再次支付状态。

### P5-T18 路径和分类错误测试

- 直接 execute success、路径歧义、循环、case 多/零命中、paying 错标、Back 目标缺失、Dependency失败。

## P5 禁止事项

不得把 paying 当 case；不得把 case 当执行前置；不得恢复 predecessor；不得直接设置目录状态跳过路径；不得在 P5 写最终 SQL 查询。

## P5 退出门禁

- [ ] 三类边分离；
- [ ] 图编译校验通过；
- [ ] 成功、失败、Back 通过；
- [ ] startPay 只在 paying；
- [ ] PayResult 只接收分类；
- [ ] success/error 唯一；
- [ ] Trace 可还原完整路径。

---

# P6：QueryPlan 与 SQL 适配

## P6 当前情况

旧 `find()` 强制根目录、依赖 start/end、拼旧 Change SQL、硬编码 `con1`，缺少参数化、case union、eq、关联模型和 runtime-only Information 策略。

## P6 整改目标

DirectoryQuery 表达业务查询，QueryCompiler 生成中立 QueryPlan，SQL/MySQL adapter 负责翻译；支持 case union/filter、关联、分页、参数化和解释能力。

## P6 任务

### P6-T01 DirectoryQuery API

- `find`、`eq`、`from/to`、`with`、`where`、`page`；`.eq()` 明确为 case filter。

### P6-T02 定义 QueryPlan

- root model、projection、join、predicate、union、typed parameters、order、page、assembly、connection route、post-filter。

### P6-T03 InformationPredicateCompiler

- 将 Information 分类为完全下推、部分下推、runtime-only、不可查询；RuleView Information 不默认可转 SQL。

### P6-T04 case 查询编译

- `find(PayResult)` 从 Compiled CaseEdge 生成 OR；eq 只选一个 case；不得遍历普通 subdirectory。

### P6-T05 View/JoinPlan

- 使用 `orm-view.xml`；支持 OrderInfo/UserInfo、one-to-many、join 类型、分页去重和结果装配。

### P6-T06 ConnectionRoute

- 从 model/view/datasource metadata 解析，不硬编码；缺失连接在编译或启动失败。

### P6-T07 typed parameter

- 名称、类型、值、敏感标记；占位符绑定；null、日期、枚举、集合有确定策略。

### P6-T08 重构 `dec-core-datasource`

- QueryExecutor、DataCommandExecutor、ConnectionRouter、DatasourceCapabilities、ResultAssembler。

### P6-T09 通用 SQL Translator

- QueryPlan 到 SQL，支持 join、projection、predicate、union、paging abstraction 和 snapshot test。

### P6-T10 MySQL 方言

- quoting、paging、type conversion、函数、integration test、grammar 生成来源统一。

### P6-T11 runtime-only 策略

- 先下推再后过滤；限制候选集；禁止无界全表；可配置拒绝；Trace 显示边界。

### P6-T12 替换旧 Directory find

- 新 API 默认 QueryCompiler；旧 API deprecated；删除 `con1` 和 Change SQL 拼接。

### P6-T13 查询测试矩阵

- PayResult union、eq success/error、paying 排除、path、with UserInfo、参数化、注入、分页、runtime-only、route。

### P6-T14 Query explain

- 输出 selected cases、Information predicates、joins、pushdown、post-filter、route 和 SQL。

## P6 禁止事项

不得在 DirectoryEngine 直接写 SQL；不得硬编码状态或连接；不得强行把所有 Information 转 SQL；不得拼用户参数；core 不依赖 MySQL。

## P6 退出门禁

- [ ] DirectoryQuery/QueryPlan 稳定；
- [ ] PayResult union 与 eq 正确；
- [ ] paying 排除；
- [ ] 全参数化；
- [ ] 无 `con1`；
- [ ] Join/分页/runtime-only 有测试；
- [ ] SQL 与 MySQL 分层。

---

# P7：事务、会话与运行时收敛

## P7 当前情况

`dec-core-model` 存在多套 Session/Transaction；`dec-expand-declaration` 有独立 Context、System、Business、Service、事务和 parser；新 Directory/Action/Produce 缺少统一会话；错误、回滚、Back 和补偿边界未统一。

## P7 整改目标

建立一个 ExecutionSession 和 TransactionCoordinator；所有 runtime 共享会话；统一错误与回滚；吸收 declaration 可复用 SPI，停止第二套正式引擎。

## P7 任务

### P7-T01 盘点事务路径

- 列出 SimpleSession、SessionExecuter、transaction groups、declaration policy、connection lifecycle，标记保留/适配/删除。

### P7-T02 定义 ExecutionSession

- Context version、transaction scope、connections、models、Information cache、trace、errors、resource lifecycle；禁止隐式全局 Session。

### P7-T03 定义 TransactionPolicy

- REQUIRED、REQUIRES_NEW（若保留）、NONE、多数据源策略、回滚条件、外部副作用、Back 关系。

### P7-T04 TransactionCoordinator

- 开启/加入、route、commit/rollback、nested scope、outcome、resource close；不包含业务路径规划。

### P7-T05 统一错误模型

- Compilation、Validation、Dependency、Action、Produce、Classification、Query、Transaction、AccessDenied；稳定 code、实体 Key、source/trace 和 cause。

### P7-T06 失败回滚语义

- 定义 Action、Produce、postcondition、classification、Custom Action、Back 的失败和回滚规则；Trace 在回滚后保留。

### P7-T07 整合 DirectoryEngine

- 每次执行绑定同一 Session；Action/Mutation/Information/Trace/Transaction 共用上下文。

### P7-T08 抽取 declaration 能力

- 保留 Produce/Consumer SPI、workflow callback、transaction policy 等思想，改造成核心 SPI。

### P7-T09 LegacyDeclarationAdapter

- 旧配置转新 Raw AST/运行计划；输出不兼容诊断；明确删除版本。

### P7-T10 冻结旧入口

- 旧 ContextStorage、SystemBuilder、ServiceDeclare deprecated；文档不推荐；禁止新增 `mix` 能力。

### P7-T11 删除重复运行路径

- adapter 覆盖后删除重复 parser、Context、空工厂和工具类；更新依赖和迁移说明。

### P7-T12 多 System 单事务测试

- 同数据库共享事务；任一步失败全部回滚；权限仍按 System 隔离。

### P7-T13 多数据源策略测试

- 不宣称不存在的 XA；不支持原子时明确失败或补偿；Trace 记录每个资源 outcome。

### P7-T14 并发 Session 隔离

- cache、transaction、trace、model 不串。

## P7 禁止事项

不得保留两套正式 Engine；不得把 Back 当数据库 rollback；不得默认宣称多数据源原子事务；不得在旧 runtime 继续新增功能；删除前必须有 adapter。

## P7 退出门禁

- [ ] ExecutionSession/Coordinator 生效；
- [ ] Directory/Action/Rule/Produce 共用事务；
- [ ] 失败回滚有测试；
- [ ] 错误模型统一；
- [ ] declaration 能力已抽取；
- [ ] 第二套 runtime 删除或完全冻结；
- [ ] 并发隔离通过。

---

# P8：XML/YAML 对等、清理、性能与发布

## P8 当前情况

XML/YAML 独立演进；legacy 与新契约并存；存在重复异常、工具、空 Factory、生成代码来源不清；文档/demo/API 未统一；尚无发布级性能、并发、热加载与安全验收。

## P8 整改目标

XML/YAML 只作为同一 AST 的不同前端；同语义 digest 一致；删除 legacy/dead code；`mix` 成为正式契约；完成安全、性能、并发、热加载、迁移与发布。

## P8 任务

### P8-T01 完成 YAML Canonical Frontend

- 转 CanonicalDocumentNode；业务校验共用 Compiler；source location 和错误码与 XML一致。

### P8-T02 XML/YAML 等价 fixture

- Data、View、System、RuleView、Information、Directory、Action/Produce、Back 和完整 `mix`。

### P8-T03 Compiled digest 对等

- 比较 key、引用、图、表达式、权限、ActionPlan、Query metadata；忽略 source location。

### P8-T04 删除 legacy 属性

- `ref-rule`、`role="predecessor"`、新 schema 下 any-one/mutual-exclusion、旧 Directory view-ref、旧 Change SQL；新 schema 明确拒绝。

### P8-T05 清理重复和死代码

- 重复 exception、拼写错误兼容、空 Factory、未使用 Config 槽位、重复 parser 工具、注释旧实现、第二 runtime 残留。

### P8-T06 统一生成代码来源

- grammar 与生成 Java 对应；生成目录明确；禁止手改；CI 检查漂移。

### P8-T07 将 `mix` 升级为正式契约

- 移除“当前未必支持”说明；补充关键语义；XML/YAML 同步；端到端测试；README 指向 `mix`。

### P8-T08 更新文档体系

- architecture、design、parser/schema、compiler、runtime、System、Information、Directory、Query、transaction、migration、examples。

### P8-T09 性能基线

- parse/compile、Context 内存、Information 初次/增量、Directory、Query compile、并发读取；建立阈值和趋势。

### P8-T10 并发安全

- Context/Registry 不可变；Session/cache/plugin scope 正确；无部分可见状态。

### P8-T11 原子热替换

```text
load -> isolated compile -> readiness validation -> atomic publish
```

旧 Session 使用旧 Context，新 Session 使用新 Context；失败编译不影响当前版本。

### P8-T12 安全检查

- XML XXE、YAML 类型注入、SQL 注入、expression 沙箱、reflection path、custom action 注册、日志脱敏、路径穿越。

### P8-T13 最终迁移报告

- 保留/deprecated/删除 API，配置迁移，declaration 迁移，XML/YAML 差异和升级步骤。

### P8-T14 全仓最终验收

```bash
./mvnw clean verify
./mvnw -Pmysql-it verify
```

并验证成功/失败/Back、三类查询、权限、digest、并发、热替换、性能、安全。

### P8-T15 发布准备

- 版本、changelog、release notes、migration guide、artifact 清单、source/javadoc、tag、rollback。

## P8 禁止事项

不得保留两套业务校验；不得无限期接受旧属性；不得原地修改正在使用的 Context；不得用性能猜测代替测量；测试失败不得发布。

## P8 退出门禁

- [ ] XML/YAML digest 一致；
- [ ] legacy 明确拒绝或仅在限期 adapter；
- [ ] `mix` 正式可执行；
- [ ] 全仓测试、安全、并发、热替换、性能通过；
- [ ] 迁移和发布文档完成；
- [ ] 不存在两套正式业务 runtime。

---

## 4. 跨阶段追踪矩阵

| 目标能力 | 主阶段 | 前置 | 最终验证 |
|---|---|---|---|
| 构建与测试阻断 | P0 | 无 | P8 全仓 |
| 统一 AST/Compiler | P1 | P0 | XML/YAML digest |
| EngineContext 隔离 | P1 | P0 | P7 并发、P8 热替换 |
| RuleView System | P2 | P1 | P4/P5 E2E |
| 模型权限 | P2 | P1 | P3/P4/P8 安全 |
| Information DAG | P3 | P1/P2 | P5 分类、P6 查询 |
| Action/Produce | P4 | P2/P3 | P5 E2E |
| Directory 执行/Back | P5 | P1—P4 | 成功/失败/Back |
| case 查询/QueryPlan | P6 | P3/P5 | SQL/MySQL contract |
| 统一事务/Session | P7 | P4—P6 | rollback/concurrency |
| declaration 收敛 | P7 | P1—P6 | 单 runtime |
| XML/YAML/清理/发布 | P8 | 全部 | release gate |

---

## 5. 防偏移规则

以下变化必须提交 ADR，并同步更新本文件、总体整改方案和 `mix`：

- 改变 P0—P8 顺序；
- 改变 RuleView 复合 Key；
- 改变普通 subdirectory 默认方向；
- 改变 paying/PayResult/case 关系；
- 合并两套 Information 表达式语言；
- 允许 System 默认越权访问；
- 改为业务代码生成主模式；
- 保留两套正式 runtime；
- 改变 find/eq 语义；
- 改变多数据源事务承诺。

每阶段开始必须记录：上一阶段门禁、稳定接口、明确不做项、ADR、测试命令、fixture 和阻断问题。

兼容适配器必须标记删除阶段；占位实现必须明确抛 unsupported，不能返回空成功；feature flag 不能用于隐藏失败测试。

---

## 6. 阶段交接模板

```markdown
# Pn 阶段交接

## 基线
- 起始提交：
- 完成提交：
- 构建命令：

## 已稳定接口
- 接口：
- 语义：
- 线程安全：
- 兼容性：

## 已完成任务
- Pn-Txx：PASSED，证据：...

## 未完成或延期
- 任务：
- 原因：
- 后续阶段：

## 已知问题
- 级别：
- 影响：
- 后续任务：

## 禁止下一阶段假设
- ...

## 下一阶段启动条件
- ...
```

---

## 7. 最终 Definition of Done

P0—P8 全部完成必须同时满足：

1. `mix/orm-config.xml` 可直接加载；
2. XML/YAML 进入同一 AST 和 Compiler；
3. System、RuleView、Information、Directory、Action、Produce、Back 是正式编译模型；
4. RuleView System 参与 Key、校验、执行和权限；
5. Information 两套语言严格隔离；
6. DAG、识别、物化、增量失效完整；
7. 普通 subdirectory 默认子到父，不存在 predecessor；
8. paying 只负责支付执行；
9. PayResult 只接收和分类；
10. success/error 唯一；
11. Action、Produce、Dependency、Change、Back 有真实 runtime；
12. `find(PayResult)` 与 eq success/error 正确；
13. 查询使用 QueryPlan、typed parameter、connection route；
14. System 越权写被拒绝；
15. Directory/Action/Rule/Produce 共用 Session 和事务；
16. 不存在两套正式业务 runtime；
17. 无外部数据库可运行核心测试；
18. XML/YAML digest 一致；
19. EngineContext 可并发读取和原子热替换；
20. CI 不忽略测试失败；
21. Trace 可还原完整业务路径；
22. 文档、schema、示例、代码、测试一致；
23. 每阶段任务和证据可追溯。

---

## 8. 顺序重申

```text
P0 构建基线
-> P1 AST / Compiler / EngineContext
-> P2 System / RuleView / 权限
-> P3 Information
-> P4 Action / Produce
-> P5 Directory / Classification / Back
-> P6 QueryPlan / SQL
-> P7 Transaction / Session / Runtime 收敛
-> P8 XML/YAML / 清理 / 发布
```

不得因为局部功能看起来容易，就跳过依赖阶段直接修改旧 `DirectoryContainer`。Directory 的执行与查询依赖 P1—P4 的编译模型、System 权限、Information 和 Action/Produce 契约。本文作为后续整改长期执行基线，任何偏离必须显式通过 ADR 和文档更新。