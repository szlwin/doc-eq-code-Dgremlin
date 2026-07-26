# P1-COMPILER-F01 需求分析：基于实际 `mix` 的统一编译骨架

> Revision：REQAN-R03-DRAFT。本文替代旧分析作为新的评审草案，旧 Revision 保留为历史 Evidence。

## 1. 分析输入

- `P1-COMPILER-CR01`；
- `DEC_COMPILER_mix_contract_inventory.md`；
- 实际 `mix/orm-config.xml`、`system/systems.xml`、`business/order-business.xml`、Data/View/Rule 文件；
- P0 已通过的构建和测试基线。

## 2. 关键事实

1. 根配置直接发现 Data/View/System/Business，但不直接发现 Rule；
2. Rule 文件由 System 的 `rule-file-info` 间接发现；
3. RuleView 自身又声明 `system`，因此存在双向一致性约束；
4. Business 文件同时承载 Information、Directory、Action、Produce；
5. `mix` 中某些元素是目标语义，旧解析器未必支持；P1 不能复用旧解析器“能解析什么就算什么”的边界；
6. `dec-expand-declaration` 不参与任何依赖或迁移。

## 3. 源发现图

### 3.1 边类型

| 边类型 | 来源 | 目标 | 说明 |
|---|---|---|---|
| ROOT_DATA_FILESET | `orm-config.xml` | `data/` | 目录集合，稳定排序 |
| ROOT_VIEW_FILESET | `orm-config.xml` | `view/` | 目录集合，稳定排序 |
| ROOT_SYSTEM_FILE | `orm-config.xml` | `systems.xml` | 显式文件 |
| ROOT_BUSINESS_FILE | `orm-config.xml` | `order-business.xml` | 显式文件 |
| SYSTEM_RULE_FILE | `systems.xml#system` | 对应 rule XML | 间接发现，带 SystemKey |

### 3.2 发现与语义顺序

- 发现必须先解析能产生新 SourceRef 的文件，这是源图遍历要求；
- 符号注册和引用解析不依赖发现顺序，所有 RawDefinition 建立后统一处理；
- 多个等价入口、重复 sourceId 和循环文件引用必须诊断；
- semanticDigest 对稳定源集合的排列不敏感。

## 4. 定义分类

| 分类 | P1 处理级别 | 后续阶段 |
|---|---|---|
| DataSource/Connection | 结构编译、引用解析 | P7 资源生命周期 |
| Data/View | 完整结构编译 | P6 Query、P7 runtime |
| System | Key、Data/View/Rule 文件引用、结构编译；ModelAccess 延迟 | P2 |
| RuleView/Rule | System 作用域符号、结构编译；执行语义延迟 | P2/P4 |
| BusinessScope | 逻辑命名空间完整编译 | 无独立 runtime |
| Information | Key、直接引用解析；表达式/物化语义延迟 | P3 |
| Directory | Key、information-ref、子目录关系结构解析；状态机延迟 | P5 |
| Action/Produce | owner、system-ref、rule-ref、information-ref 等类型引用；执行延迟 | P4 |

## 5. 核心设计取舍

### 5.1 `CompiledModelSet` 而非 `CompiledBusiness`

输出代表整个配置模型集合，BusinessScope 只是其中一个 Registry，避免形成独立业务运行时含义。

### 5.2 `RawDefinition` 而非 `RawDeclaration`

该术语覆盖所有配置定义，与已废弃的 declaration 模块彻底解耦。

### 5.3 显式 Deferred Registry

P1 不支持的语义不能丢失或假装已完成。每个 DeferredDefinition 至少包含：

- ownerKey；
- kind；
- requiredStage；
- reasonCode；
- SourceRef；
- normalizedBody；
- 已成功解析的类型引用；
- unresolved diagnostics。

### 5.4 旧核心投影而非 Adapter

`CoreConfigProjection` 只是从新 Registry 计算旧核心读取形态；无写 API、无缓存事实、无 declaration 类型。

## 6. 影响分析

| 模块 | 变更 | 禁止依赖 |
|---|---|---|
| `dec-core-compiler` | 新增源图、前端契约、AST、Pass、结果 | SQL、MySQL、demo、废弃模块 |
| `dec-core-context` | Key、不可变定义、Registry、EngineContext | XML/YAML parser |
| XML frontend | 将 XML 转为 CanonicalNode | 直接写 ConfigFactory |
| YAML frontend | 同契约最小路径 | 任意 Java 类型构造 |
| `dec-core-starter` | 调用 compiler 并原子切换 Context | 全局可变 current Context |
| `dec-demo` | 保存 `mix` fixture 和 contract tests | 生产编译器反向依赖 demo |
| `dec-expand-declaration` | 整体删除 | 任何保留/抽取/Adapter |

## 7. 风险

- R1：旧 parser 不支持 `system-file-info`/`business-file-info`，可能诱导绕过统一前端；措施：以新前端和 fixture contract 为准；
- R2：Rule 文件间接发现造成加载顺序耦合；措施：分离 source discovery 与 symbol resolution；
- R3：BusinessScope 被误建为独立 runtime；措施：架构测试禁止第二 Engine/Context；
- R4：Deferred 变成永久垃圾桶；措施：每项必须带 requiredStage，并在 P2～P7 逐阶段消减；
- R5：只读投影演变为双写；措施：API 不暴露 mutator，写调用测试必须失败；
- R6：模块删除后测试缺口；措施：必要场景只以 `mix` fixture 重写，不复制旧代码。

## 8. 分析结论

P1 可在不实现业务运行语义的前提下，为实际 `mix` 建立完整源图、统一定义模型、强类型引用和不可变发布边界。P1 设计不得依赖临时模块，也不得将 BusinessScope 解释为独立项目。
