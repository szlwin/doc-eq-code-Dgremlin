# FEATURE-DESC-3361AD2E54FC P2 简化配置与规则执行模型

> 文档 revision：REQAN-P2-R04。本文正式取代 REQAN-P2-R01 中以 Runtime Scope、Session、Handle、Capability、owner identity 和 mutation stamp 为中心的重型运行时访问模型。旧文档与旧实现仅作为历史记录，不再作为 P2 验收依据。

## 1. 需求信息

| 项目 | 内容 |
|---|---|
| 需求编号 | P2-SYSTEM-RULEVIEW |
| 需求名称 | P2 简化配置与规则执行模型 |
| 版本目标编号 | FEATURE-DESC-3361AD2E54FC |
| 需求类型 | 架构简化、配置发布与业务执行兼容 |
| 主责模块 | dec-core-context、dec-core-compiler、dec-core-model |
| 协作模块 | dec-core-starter、dec-context-config-parse-xml、dec-context-config-parse-yaml、dec-demo |
| 受影响角色 | 配置作者、规则开发者、业务开发者、测试人员、维护人员 |
| 优先级 | P2 |
| 当前状态 | 已确认，进入文档归档 |
| 对应变更需求编号 | FEATURE-DESC-3361AD2E54FC |
| 需求输入 revision | 用户确认的“易用优先”运行模型与 P2 后续 1～3；实现基线 DEV-P2-SIMPLE-R43@a5ecf75d5169 |

### 1.1 阅读摘要

P2 采用易用优先的简化运行模型：业务统一调用 `ConfigUtil.parseConfigInfo(path)`，不创建、传递或接收 ConfigInfo。现代 XML 配置先解析到候选 ConfigInfo，编译成功后与同一个 EngineContext 整体安装；旧 XML/YAML 配置继续使用候选解析和整体 ConfigInfo 替换。业务执行仍使用 DataUtil、ModelLoader 和 ModelContainer，不接触 Runtime Scope、Session、Handle 或 Capability。

## 2. 背景与问题

### 2.1 当前行为与证据

P2 曾引入 RuntimeModelAccessScope、Session、Handle、Frame、ProtectedAccessInvocation、RuntimeBindingProof、OneShotWriteCapability、owner identity 和 mutation stamp 等对象，用于严格限制模型读取和修改。该方案对当前 DEC 配置与规则执行场景过度复杂，业务调用者需要理解大量基础设施概念，且偏离 `RuleTests` 已验证的直接调用方式。

当前代码已经形成更简单的可用链路：

```java
ModelData order = DataUtil.createViewData("OrderInfo");
order.setValue("totalPrice", 350);

ModelContainer container = new ModelContainer();
container.load(new ModelLoader().load("save-Order", order, "con1"));
container.execute();
```

配置侧已具备候选 `ConfigInfo`、编译后的 `EngineContext` 以及成功后整体安装的基础能力；XML 与 YAML 解析器均可将内容写入指定候选配置。

### 2.2 需要解决的问题

1. 正式取消重型 runtime 安全模型作为 P2 需求，避免已退役代码继续约束后续维护。
2. 明确 `ConfigInfo` 与 `EngineContext` 的关系，使配置解析、编译和发布形成一条可理解的链路。
3. 保留现有业务代码的简单调用方式，不要求显式传递 `EngineContext` 或运行时凭证。
4. 对配置、View、Rule、Connection 缺失以及执行失败给出明确错误，并保留事务关闭语义。
5. 保持 XML/YAML 路径识别、Java 8 和现有规则名称兼容，同时不向业务调用者暴露 ConfigInfo。

## 3. 需求目标

1. XML/YAML 配置必须由统一门面写入独立候选 `ConfigInfo`，不得要求调用方创建候选，也不得在解析未完成时污染当前已安装配置。
2. 现代 XML 编译成功时，候选 `ConfigInfo` 与其 `EngineContext` 必须整体生效；编译失败时，旧配置必须保持可用。
3. 业务代码继续通过 `DataUtil`、`ModelData`、`ModelLoader`、`ModelContainer.execute()` 完成模型创建与规则执行。
4. 普通业务调用不需要 Runtime Scope、Session、Handle、Capability、owner identity 或一次性写权限证明。
5. 配置、View、Rule、Connection 不存在时必须在真正产生业务副作用前明确失败。
6. `ModelContainer` 必须保留连接获取、提交、回滚和关闭职责。
7. 所有生产代码保持 Java 8 可编译，规则名继续支持 `save-Order` 形式。

## 4. 范围

### 4.1 范围内

- `ConfigInfo` 使用自身配置集合，并持有编译成功的 `EngineContext`。
- `ConfigManager` 管理当前安装配置和线程内候选配置，并整体安装 `ConfigInfo + EngineContext`。
- `ConfigUtil.parseConfigInfo(path)` 根据扩展名选择 XML/YAML Parser，并在内部管理候选 `ConfigInfo`。
- XML/YAML Parser 支持解析到框架传入的候选 `ConfigInfo`，该能力不作为业务 API 暴露。
- Compiler/Starter 编译候选配置，只有成功发布后才安装。
- `DataUtil.createViewData` 创建完整可用的 `ViewData`。
- `ModelLoader`、`RuleContainer`、`ModelContainer` 对规则、连接和执行失败提供明确异常。
- 退役原 P2 runtime/access 生产代码及仅为其服务的测试代码。

### 4.2 范围外

- 不建立模型字段级权限系统、租户级权限系统或脚本沙箱。
- 不要求运行时对象 owner identity、generation identity、一次性 capability 或 mutation stamp。
- 不改变现有 XML/YAML 的既有业务表达能力和规则实现语法。
- YAML 的 System/Business Source Graph 声明发现与完整静态编译属于 P8；P2 遇到该输入必须明确拒绝，不能部分安装。
- 不新增数据库表、字段、迁移脚本或外部 HTTP API。
- 不在本次需求中重构既有 SQL 生成器和数据库驱动实现。

### 4.3 约束与依赖

- Java 源码与产物兼容级别保持 Java 8。
- `EngineContext` 仍是编译结果的不可变运行视图；业务调用者不直接管理它。
- XML 与 YAML 对 P2 既有 Data/View/Rule/Connection 配置必须产生等价的 `ConfigInfo` 配置事实。
- 现代 System/Business 配置在 P2 以 XML 为编译入口；YAML 格式对等由 P8 完成。
- 简化不等于静默容错；缺少定义和执行错误必须显式报告。
- DEC-P2-R34-001、DEC-P2-R34-002、DEC-P2-R34-003 已由用户确认无需考虑，不构成本需求门禁。

### 4.4 失败语义

- 候选配置解析失败：终止本次加载，不安装候选配置。
- 候选配置编译失败：返回编译诊断，当前已安装配置与 `EngineContext` 不变。
- YAML 声明 System/Business：返回包含 P8 迁移边界的明确异常，不安装候选配置。
- View 不存在：`DataUtil.createViewData` 抛出可定位的定义缺失异常。
- Rule 不存在或不适用于目标模型：装载/执行前明确失败，不执行数据库操作。
- Connection 不存在：获取连接时抛出包含连接名的明确异常。
- 数据库执行失败：回滚已打开的相关连接，并在 finally 路径关闭连接。

### 4.5 恢复与兼容边界

- 配置修正后可重新执行“解析候选 -> 编译 -> 安装”，不要求重启 JVM。
- 旧业务调用代码无需增加 Context、Scope、Session 或凭证参数。
- 原重型 runtime/access 类型不再提供兼容适配层；未迁移的内部调用应在编译期暴露并删除。
- 现有规则名称、View 名称和 Connection 名称保持字符串调用兼容。

### 4.6 完成标准

- 需求与设计文档不再把重型 runtime/access 模型列为 P2 必需能力。
- 三个退役生产目录中不存在 Java 源文件。
- 代码 revision 可完成 Java 8 `test-compile`。
- 需求、设计与业务模型文档通过对应结构校验和 `wk -wd` 归档检查。
- 轻量模式未显式要求测试时，编译通过只登记为编译证据，不表述为测试通过。

## 5. 功能列表

| 功能编号 | 功能名称 | 说明 | 优先级 |
|---|---|---|---|
| P2-SYSTEM-RULEVIEW-F01 | 候选配置加载 | 统一门面把 XML/YAML 写入内部候选 ConfigInfo | P0 |
| P2-SYSTEM-RULEVIEW-F02 | 编译与原子安装 | 编译成功后整体安装 ConfigInfo + EngineContext | P0 |
| P2-SYSTEM-RULEVIEW-F03 | 简单模型执行 | 保留 DataUtil -> ModelLoader -> ModelContainer 调用 | P0 |
| P2-SYSTEM-RULEVIEW-F04 | 明确错误与事务收尾 | 缺少定义和数据库失败可定位，连接正确收尾 | P0 |
| P2-SYSTEM-RULEVIEW-F05 | 重型 runtime 退役 | 删除 P2 runtime/access 生产入口和专用测试 | P1 |

## 6. 功能详细需求

### 6.1 P2-SYSTEM-RULEVIEW-F01 候选配置加载

#### 6.1.1 功能目标

业务调用者只提供 XML 或 YAML 配置路径；框架在不改变当前已安装配置的前提下完成候选解析。

#### 6.1.2 角色与权限

配置加载器可以创建并写入候选 `ConfigInfo`；业务调用者只能通过已安装配置使用 View、Rule 和 Connection，不参与候选状态管理。

#### 6.1.3 前置条件

- 配置文件可读取且格式合法。
- 统一加载入口负责创建候选 `ConfigInfo`。
- 配置中引用的基础类型可由对应解析器识别。

#### 6.1.4 正常流程

1. 调用方执行 `ConfigUtil.parseConfigInfo(path)`。
2. ConfigUtil 创建候选 `ConfigInfo`，并根据 `.xml`、`.yaml` 或 `.yml` 选择 Parser。
3. Parser 将 Connection、Data、View、Rule 等既有配置写入候选对象。
4. 现代 XML 继续由 Compiler 构建 System/Business 编译模型和 `EngineContext`。
5. 现代 XML 只有编译、发布成功后才整体安装；旧 XML/YAML 在完整解析成功后替换当前 ConfigInfo。
6. YAML 若声明 System/Business，P2 明确拒绝并保持旧配置，完整格式对等在 P8 完成。

#### 6.1.5 业务规则

- BR-P2-SYSTEM-RULEVIEW-001：候选解析不得修改当前已安装 `ConfigInfo`。
- BR-P2-SYSTEM-RULEVIEW-002：一个候选对象中的 View、Rule、Connection 和 EngineContext 必须属于同一次加载结果。
- BR-P2-SYSTEM-RULEVIEW-003：编译或发布失败时不得安装候选对象。
- BR-P2-SYSTEM-RULEVIEW-004：安装通过单一整体引用生效，读取方不得观察到新配置配旧 Context 或旧配置配新 Context。
- BR-P2-SYSTEM-RULEVIEW-005：XML 与 YAML 使用同一个不暴露 ConfigInfo 的业务门面，并遵守候选解析失败不安装语义。
- BR-P2-SYSTEM-RULEVIEW-011：P2 不得把 YAML System/Business 当作旧配置静默加载；必须在安装前明确提示该能力属于 P8。

#### 6.1.6 输入与输出约束

业务输入只有配置文件路径，公开方法不返回 ConfigInfo。框架内部输出是已填充候选对象、编译诊断或已安装配置；Parser 与 Compiler 不得向业务代码暴露候选 ConfigInfo 或半成品 `EngineContext`。

#### 6.1.7 状态、幂等、并发与事务语义

同一配置重复加载可产生新的候选对象；`ConfigUtil.parseConfigInfo` 将加载和安装串行化，因此 P2 不引入 expected Context、generation 或 CAS 冲突协议。解析或编译失败时保留旧对象，不进行半量回滚。配置安装不涉及数据库事务。

#### 6.1.8 异常与禁止副作用

解析、编译或安装准备任一步失败，不得更改当前已安装配置；不得吞掉异常后继续使用半成品候选对象。

### 6.2 P2-SYSTEM-RULEVIEW-F03 简单模型执行

#### 6.2.1 正常流程

1. 业务代码按 View 名称创建 `ModelData`。
2. 业务代码设置或读取模型属性。
3. `ModelLoader` 按简单规则名和连接名创建执行任务。
4. `ModelContainer.load` 收集任务并准备连接。
5. `ModelContainer.execute` 执行规则，成功提交，失败回滚，最终关闭连接。

#### 6.2.2 业务规则

- BR-P2-SYSTEM-RULEVIEW-006：`createViewData` 返回完整 `ViewData`，不得因内部 runtime 模式退化为只读或受保护占位对象。
- BR-P2-SYSTEM-RULEVIEW-007：规则可继续使用 `save-Order` 形式的名称调用。
- BR-P2-SYSTEM-RULEVIEW-008：业务调用者不显式传递 `EngineContext`，框架从当前已安装 `ConfigInfo` 获取所需定义。
- BR-P2-SYSTEM-RULEVIEW-009：Rule、View、Connection 缺失必须在对应副作用发生前失败。
- BR-P2-SYSTEM-RULEVIEW-010：`ModelContainer` 对已打开连接执行提交、回滚和关闭，不因简化运行模型取消事务收尾。

## 7. 跨功能规则

| 规则编号 | 规则 |
|---|---|
| CFR-P2-001 | 配置对象内部集合按实例隔离，不通过全局 ConfigFactory 保存业务配置。 |
| CFR-P2-002 | `ConfigInfo.getEngineContext()` 在尚未编译安装时明确失败。 |
| CFR-P2-003 | 兼容投影必须来自同一个已安装 EngineContext。 |
| CFR-P2-004 | 错误信息至少包含缺失对象类型和调用名称。 |
| CFR-P2-005 | 注释使用中文并说明业务意图、失败结果和来源稳定 ID，便于非原作者阅读。 |

## 8. 异常与边界场景

| 场景 | 预期结果 | 状态影响 |
|---|---|---|
| XML/YAML 文件不存在或格式错误 | 返回解析异常 | 当前配置不变 |
| 候选配置缺少必需定义 | 编译失败并返回诊断 | 当前配置不变 |
| 两个线程同时调用统一加载入口 | 按调用顺序串行完成候选加载与安装 | 不出现交错写入或半成品配置 |
| `createViewData` 使用未知 View | 明确的 DataNotDefineException | 不创建模型 |
| 装载未知 Rule | 明确的规则异常 | 不打开或不执行数据库操作 |
| 指定未知 Connection | 包含连接名的连接异常 | 不执行规则 |
| 多连接执行中出现异常 | 回滚已参与连接并关闭资源 | 不提交失败批次 |
| 已安装配置被重新加载 | 新配置和新 Context 整体替换 | 后续调用使用新配置 |

## 9. 验收标准

### AC-P2-SYSTEM-RULEVIEW-001 ConfigInfo 实例隔离

- 给定两个独立 `ConfigInfo`，向其中一个添加配置。
- 另一个对象不得出现该配置，当前已安装对象也不得被候选解析提前修改。

### AC-P2-SYSTEM-RULEVIEW-002 XML/YAML 候选解析

- XML 与 YAML 都通过 `ConfigUtil.parseConfigInfo(path)` 加载，公开方法不接收或返回 `ConfigInfo`。
- 合法旧配置加载后 Data/View/Rule/Connection 可直接使用；解析失败时当前安装配置不变。

### AC-P2-SYSTEM-RULEVIEW-003 编译成功整体安装

- 给定合法候选配置，编译与发布成功。
- `ConfigManager` 返回该 `ConfigInfo`，且该对象持有本次发布的同一个 `EngineContext`。

### AC-P2-SYSTEM-RULEVIEW-004 编译失败保留旧配置

- 给定已安装配置和一个不可编译候选配置。
- 编译失败后，已安装 `ConfigInfo` 与 `EngineContext` 的对象身份均不变。

### AC-P2-SYSTEM-RULEVIEW-005 简单业务调用兼容

- 使用 `DataUtil.createViewData("OrderInfo")`、`ModelLoader.load("save-Order", data, "con1")` 和 `ModelContainer.execute()`。
- 调用方无需创建或传递 runtime Scope、Session、Handle、Frame、Capability 或 owner identity。

### AC-P2-SYSTEM-RULEVIEW-006 定义缺失明确失败

- 分别使用不存在的 View、Rule 和 Connection 名称。
- 每种情况在数据库副作用前失败，异常可识别缺失类型和名称。

### AC-P2-SYSTEM-RULEVIEW-007 事务与资源收尾

- 成功执行时提交相关连接；任一执行失败时回滚相关连接。
- 成功和失败路径均关闭已打开连接，不因异常覆盖原始业务失败。

### AC-P2-SYSTEM-RULEVIEW-008 Java 8 与退役边界

- 项目 `test-compile` 在 Java 8 release 约束下通过。
- `dec-core-context/.../runtime`、`dec-core-model/.../runtime`、`dec-core-starter/.../access` 不再包含生产 Java 文件。

### AC-P2-SYSTEM-RULEVIEW-009 YAML 现代声明边界

- YAML 不包含 System/Business 声明时通过统一入口兼容加载。
- YAML 包含 System/Business 声明时在安装前明确失败，错误说明完整 YAML Source Graph 编译属于 P8。
- 失败后当前已安装 ConfigInfo 与 EngineContext 不变，不允许部分安装。

## 10. 非功能要求

### 10.1 性能

简化链路不得为每次模型字段读写创建 Scope、Session、Handle 或权限证明对象。配置加载成本允许集中在解析和编译阶段。

### 10.2 安全与权限

本需求不提供字段级授权。安全底线是配置发布失败不污染当前配置、缺失定义不静默降级、数据库失败正确回滚和关闭。

### 10.3 可靠性、一致性与恢复

配置与 `EngineContext` 以整体对象发布；失败保留旧对象。数据库操作沿用 `ModelContainer` 的事务边界。

### 10.4 审计与可观测性

解析、编译、规则装载和连接获取失败应保留异常原因与对象名称。不得在错误消息中输出连接密码等敏感配置。

### 10.5 兼容与历史数据

无数据库结构和历史数据迁移。保持 Java 8、现有 View/Rule/Connection 名称以及 `RuleTests` 风格的业务调用兼容。

## 11. 模块职责边界

### 11.1 dec-core-context 与 dec-core-compiler

- context：保存每实例配置、当前安装配置和不可变 `EngineContext`。
- compiler：校验候选配置并生成可发布 `EngineContext`，不直接执行数据库规则。

### 11.2 配置解析与 starter

- XML/YAML parser：只负责将外部配置解析到候选 `ConfigInfo`。
- starter：编排 compile/publish/install，并将同一发布结果交给 `ConfigManager`。

### 11.3 dec-core-model

- 创建业务 `ModelData`，装载并执行规则。
- `ModelContainer` 管理连接、提交、回滚和关闭。
- 不再依赖已退役 runtime/access 模型。

### 11.4 协作边界

配置链路为 `ConfigUtil -> XML/YAML Parser -> candidate ConfigInfo`；现代 XML 再进入 `Compiler -> EngineContext -> install`，旧 XML/YAML 在完整解析后替换 ConfigInfo。业务链路为 `DataUtil -> ModelData -> ModelLoader -> ModelContainer.execute()`。两条链路通过当前已安装 `ConfigInfo` 内部关联，不要求业务参数显式传递 `EngineContext`。

## 12. 待确认事项与已确认决策

### 12.1 待确认事项

无阻断性待确认事项。后续如需要租户权限、字段权限或脚本隔离，应建立独立需求，不恢复本 P2 已退役类型作为默认方案。

### 12.2 已确认决策

| 决策编号 | 结论 |
|---|---|
| DEC-P2-SIMPLE-001 | 易用优先，保留 RuleTests 风格的直接业务调用。 |
| DEC-P2-SIMPLE-002 | ConfigInfo 与编译后的 EngineContext 在一次成功发布中整体安装。 |
| DEC-P2-SIMPLE-003 | 退役 context/runtime、model/runtime、starter/access 及其专用测试。 |
| DEC-P2-SIMPLE-004 | 只保留定义检查、编译失败保护和数据库事务收尾等基础保护。 |
| DEC-P2-SIMPLE-006 | P2 隐藏 XML/YAML 的 ConfigInfo 候选；YAML System/Business Source Graph 编译留到 P8，并在 P2 明确拒绝。 |
| DEC-P2-SIMPLE-005 | DEC-P2-R34-001、002、003 无需考虑，不作为 P2 验收条件。 |

## 13. 追踪关系

> 配置链复用 `FLOW-CONFIG-COMPILE`；业务执行使用 `FLOW-SIMPLE-MODEL-EXECUTE`，不再沿用未归档的 Guard/Capability 流程候选。

| 追踪编号 | 功能编号 | 业务规则/跨功能规则 | 验收标准 | 关联流程 | 影响分析 | 后续业务模型 | 后续设计 | 测试 Case | 状态 |
|---|---|---|---|---|---|---|---|---|---|
| TR-P2-001 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-001、BR-P2-SYSTEM-RULEVIEW-002 | AC-P2-SYSTEM-RULEVIEW-001 | FLOW-CONFIG-COMPILE | ConfigInfo 实例隔离 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |
| TR-P2-002 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-003、BR-P2-SYSTEM-RULEVIEW-005 | AC-P2-SYSTEM-RULEVIEW-002 | FLOW-CONFIG-COMPILE | XML/YAML 候选解析 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |
| TR-P2-003 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-002、BR-P2-SYSTEM-RULEVIEW-003 | AC-P2-SYSTEM-RULEVIEW-003 | FLOW-CONFIG-COMPILE | 编译成功整体安装 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |
| TR-P2-004 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-003、BR-P2-SYSTEM-RULEVIEW-004 | AC-P2-SYSTEM-RULEVIEW-004 | FLOW-CONFIG-COMPILE | 编译失败保留旧配置 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |
| TR-P2-005 | P2-SYSTEM-RULEVIEW-F03 | BR-P2-SYSTEM-RULEVIEW-006、BR-P2-SYSTEM-RULEVIEW-007、BR-P2-SYSTEM-RULEVIEW-008 | AC-P2-SYSTEM-RULEVIEW-005 | FLOW-SIMPLE-MODEL-EXECUTE | 简单业务调用 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |
| TR-P2-006 | P2-SYSTEM-RULEVIEW-F03 | BR-P2-SYSTEM-RULEVIEW-009 | AC-P2-SYSTEM-RULEVIEW-006 | FLOW-SIMPLE-MODEL-EXECUTE | 缺失定义错误 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |
| TR-P2-007 | P2-SYSTEM-RULEVIEW-F03 | BR-P2-SYSTEM-RULEVIEW-010 | AC-P2-SYSTEM-RULEVIEW-007 | FLOW-SIMPLE-MODEL-EXECUTE | 事务与资源收尾 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |
| TR-P2-008 | P2-SYSTEM-RULEVIEW-F01、P2-SYSTEM-RULEVIEW-F03 | BR-P2-SYSTEM-RULEVIEW-001、BR-P2-SYSTEM-RULEVIEW-006 | AC-P2-SYSTEM-RULEVIEW-008 | FLOW-CONFIG-COMPILE、FLOW-SIMPLE-MODEL-EXECUTE | Java 8 与退役边界 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |
| TR-P2-009 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-002、BR-P2-SYSTEM-RULEVIEW-004 | AC-P2-SYSTEM-RULEVIEW-003 | FLOW-CONFIG-COMPILE | ConfigInfo 与 EngineContext 整体安装 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |
| TR-P2-010 | P2-SYSTEM-RULEVIEW-F01、P2-SYSTEM-RULEVIEW-F03 | BR-P2-SYSTEM-RULEVIEW-001、BR-P2-SYSTEM-RULEVIEW-006 | AC-P2-SYSTEM-RULEVIEW-008 | FLOW-CONFIG-COMPILE、FLOW-SIMPLE-MODEL-EXECUTE | 简化模型与代码 revision 对齐 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |
| TR-P2-011 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-005、BR-P2-SYSTEM-RULEVIEW-011 | AC-P2-SYSTEM-RULEVIEW-009 | FLOW-CONFIG-COMPILE | YAML 统一门面与 P8 边界 | BM-R06 | DESIGN-P2-R40 | TESTDESIGN-P2-R41 | COVERED |

## 14. 变更记录

| Revision | 日期 | 变更 | 授权/证据 |
|---|---|---|---|
| REQAN-P2-R01 | 2026-08-08 | 定义 System/RuleView 与严格 runtime model-access 方案 | 历史 P2 文档 |
| REQAN-P2-R02 | 2026-08-24 | 正式切换到 ConfigInfo + EngineContext 与直接 ModelContainer 执行的简化运行模型；重型 runtime/access 需求失效 | 用户明确确认；代码 8b362d5dc5b324104585ac41db77f4c42544c5ee |
| REQAN-P2-R04 | 2026-08-24 | ConfigUtil 成为 XML/YAML 统一且不暴露 ConfigInfo 的入口；现代 XML 在 P2 编译，YAML System/Business 格式对等明确归属 P8 | 用户确认继续完成 P2 后续 1～3；现有 SourceDeclarationParser 为 XML 专用实现 |
| REQAN-P2-R03 | 2026-08-24 | 统一 BR/AC 命名空间并补齐整体安装、代码 revision 的稳定追踪项 | wk-wd 结构化归档预演校验 |
