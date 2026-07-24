# P1-COMPILER-F01 统一 AST、Registry 与 Compiler 骨架

> 文档职责：RequirementConfirmationAgent 在需求确认阶段确认目标、范围、约束和决策；RequirementAnalysisAgent 在后续阶段补全功能、规则、异常、验收与追踪。本需求描述框架必须表现出的可观察能力，具体包、类和接口进入设计文档。

## 1. 需求信息

| 项目 | 内容 |
|---|---|
| 需求编号 | P1-COMPILER |
| 需求名称 | 统一 AST、Registry 与 Compiler 骨架 |
| 版本目标编号 | P1-COMPILER-F01 |
| 需求类型 | 架构整改 |
| 主责模块 | dec-core-compiler |
| 协作模块 | dec-core-context、dec-context-config-parse-xml、dec-context-config-parse-yaml、dec-core-starter、dec-demo |
| 受影响角色 | 框架开发者、配置维护者、运行时集成者、测试人员 |
| 当前状态 | 需求确认完成 |
| 对应变更需求编号 | - |

## 2. 背景与问题

### 2.1 当前行为与证据

1. `ConfigFactory` 通过固定整数槽位保存全局单例 Config，扩展新配置类型需要修改共享数组和常量。
2. `ConfigManager` 持有进程级可变 `ConfigInfo`，同一 JVM 中多个项目或多个版本会共享并覆盖状态。
3. XML、YAML 解析器直接构造并写入现有运行时 Config；格式解析、引用解析、校验、注册和运行时发布没有清晰边界。
4. XML 与 YAML 各自维护解析和字段兼容逻辑，缺少统一格式中立表示，无法证明同义配置得到相同结果。
5. 当前错误主要以异常或控制台输出暴露，没有统一错误码、源文件位置、实体标识和稳定排序。
6. `RuleConfig` 等注册表按裸字符串名称保存对象，无法为 P2 的 `(system,name)` RuleView 标识和多上下文隔离提供基础。

证据来源：`doc/mix-framework-technical-remediation-plan.md`、`doc/mix-framework-p0-p8-detailed-task-plan.md`，以及当前 `dec-core-context`、XML/YAML 解析和 starter 代码。

### 2.2 需要解决的问题

1. 配置格式、配置模型、编译校验和运行时上下文相互耦合，导致错误晚发现、扩展成本高和行为漂移。
2. 全局可变配置使多项目、多版本、并发初始化和热替换无法安全演进。
3. 缺少稳定的引用、诊断、不可变注册表和编译摘要，后续 System、Information、Directory、Query 等阶段没有可信基础。
4. 旧 API 仍被当前运行时使用，必须提供明确、只读、可删除的迁移边界，而不是一次性破坏兼容。

## 3. 需求目标

1. 建立 XML/YAML 共用的格式中立文档表示和 Raw AST，使同义输入可比较、可追踪。
2. 建立确定性 Compiler Pipeline，集中完成结构校验、符号注册、引用解析、图准备、语义校验和发布。
3. 建立强类型 Key、不可变 Registry、Compiled AST、稳定 Diagnostic 和可重复 digest。
4. 建立实例级不可变 EngineContext，保证两个上下文可在同一 JVM 中并存且无状态污染。
5. 通过只读 Legacy Config Adapter 支持旧代码渐进迁移，并明确禁止新代码继续向旧全局 Config 注册。
6. 为 P2～P8 提供可测试、可扩展、边界清晰的编译基础，但不提前实现后续业务语义。

## 4. 范围

### 4.1 范围内

- 新增独立 `dec-core-compiler` 模块并纳入默认 Reactor。
- CanonicalDocumentNode、SourceLocation、DocumentFormat 与格式无关的文档前端契约。
- 覆盖 `mix` 声明形态的 Raw AST；P1 只完整编译 Data、View、Rule，后续实体保留结构信息。
- Diagnostic、稳定错误码、强类型 Key、SymbolTable、RegistryBuilder、Compiled AST 与稳定 digest。
- 固定顺序的 Compiler Pipeline 及每个 pass 的独立验证接缝。
- 实例级不可变 EngineContext 与两个 Context 的隔离能力。
- 旧 Config 的只读投影视图与弃用边界。
- XML 新前端和最小 YAML 同接口路径；资源确定性排序、未知元素/属性和引用错误诊断。
- 重复定义、未知引用、前向引用、多文件、源位置、诊断顺序、digest 和 Context 隔离测试要求。

### 4.2 范围外

- P2 的完整 System、ModelAccess 和 RuleView 复合 Key 运行语义。
- P3 的 Information 求值、DAG、物化和增量失效。
- P4～P7 的 Action、Produce、Directory 状态机、QueryPlan、事务和 declaration 收敛。
- P8 的完整 XML/YAML 对等、全量 legacy 删除、性能发布和热替换发布流程。
- 生成大量业务 Java 类作为主要执行方式。
- 修改既有业务语义以“顺便修复”旧行为差异。

### 4.3 约束与依赖

- 以 P0 已建立的 Maven Wrapper、CI、测试失败阻断和旧行为快照为构建基线；动态构建证据若仍未回执，实施阶段必须先补齐，不得忽略。
- 代码保持 Java 8 兼容，因此强类型 Key 和不可变对象使用普通 final 类，不使用 Java record。
- `dec-core-compiler` 不依赖 SQL、MySQL、demo 或具体运行时执行模块。
- parser 只负责前端转换，不直接写全局 Config；runtime 不依赖 DOM4J、SnakeYAML 或前端节点。
- 编译包含 ERROR 时不得发布 EngineContext；WARN/INFO 必须稳定排序并可追踪。
- 同义 XML/YAML 的语义 digest 忽略格式和源位置，但源位置仍保留在诊断和原始声明中。

## 5. 功能列表

> 由 RequirementAnalysisAgent 在 R02 补全。

| 功能编号 | 功能名称 | 主责模块 | 协作模块 | 关联流程 | 功能说明 | 状态 |
|---|---|---|---|---|---|---|
| P1-COMPILER-F01 | 统一编译上下文骨架 | dec-core-compiler | dec-core-context、XML/YAML parser、starter、demo | FLOW-CONFIG-COMPILE | 建立统一文档前端、AST、Registry、Compiler Pipeline 与 EngineContext | 待分析 |

## 6. 功能详细需求

待需求分析补全。

## 7. 跨功能规则

待需求分析补全。

## 8. 异常与边界场景

待需求分析补全。

## 9. 验收标准

需求确认阶段先冻结以下可观察完成维度，详细 Given/When/Then 在 R02 补全：

1. XML 与 YAML 同义 fixture 形成等价 Canonical/Raw 语义结果。
2. 重复定义、未知引用和非法结构形成带稳定错误码与位置的确定性诊断。
3. Data/View/Rule 可编译并发布到不可变 EngineContext；错误输入不发布部分 Context。
4. 同一 JVM 中两个 EngineContext 互不污染，重复编译产生稳定 digest。
5. Legacy Adapter 只能读取，不能注册或修改新 Context。
6. P2+ 语义保持未实现且不得以空成功或全局单例占位。

## 10. 非功能要求

待需求分析补全。

## 11. 模块职责边界

待需求分析补全。

## 12. 待确认事项与已确认决策

### 12.1 待确认事项

| 决策编号 | 问题 | 可选方案 | 推荐方案 | 是否阻塞 | 责任人/Agent | 状态 |
|---|---|---|---|---|---|---|
| - | 当前不存在阻塞需求确认的待决事项 | - | - | 否 | ProjectManagerAgent | 已闭合 |

### 12.2 已确认决策

| 决策编号 | 结论 | 原因 | 证据/确认来源 | supersedes |
|---|---|---|---|---|
| DEC-P1-COMPILER-001 | P1 只建立 AST、Registry、Compiler、EngineContext 和前端骨架，不实现 P2～P8 运行语义 | 保持阶段依赖和回归边界 | 用户当前指令及两份整改文档 | - |
| DEC-P1-COMPILER-002 | 新编译对象和 Key 保持 Java 8 兼容，不使用 record | 当前父 POM 仍以 Java 8 为源码兼容目标 | P0 构建基线 | - |
| DEC-P1-COMPILER-003 | EngineContext 必须实例级、不可变，禁止成为新的全局单例 | 解决多项目、多版本和并发初始化污染 | 技术整改方案第 6、15 节 | - |
| DEC-P1-COMPILER-004 | 旧 Config 只通过只读 adapter 迁移；新代码不得经 adapter 或旧 Config 注册 | 避免双写和第二事实源 | P1-T10 | - |
| DEC-P1-COMPILER-005 | XML/YAML 共享 compiler 与 Raw AST；P1 只实现最小 YAML 等价，完整对等留到 P8 | 控制 P1 范围并保证演进方向 | P1-T11、P1-T12 | - |
| DEC-P1-COMPILER-006 | 编译失败不发布部分 EngineContext；诊断集合稳定排序 | 保证运行时只消费完整、可复现产物 | P1-T04、P1-T08、P1-T09 | - |

## 13. 追踪关系

待需求分析补全并通过 `requirement_doc.py sync-traceability` 同步。

## 14. 变更记录

| 文档 revision | 日期 | 阶段 | 变更内容 | 责任 Agent |
|---|---|---|---|---|
| R01 | 2026-07-24 | 需求确认 | 冻结 P1 目标、范围、约束、完成维度和六项关键决策 | RequirementConfirmationAgent |
| R02 | 待生成 | 需求分析 | 功能、规则、异常、验收与追踪 | RequirementAnalysisAgent |
