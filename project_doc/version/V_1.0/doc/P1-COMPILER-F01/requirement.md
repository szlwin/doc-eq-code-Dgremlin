# P1-COMPILER-F01 统一 AST、Registry 与 Compiler 骨架

> 文档职责：本文件固定 P1 必须表现出的可观察能力与边界；包、类和接口的具体组织进入设计文档。P1 不实现 P2～P8 的业务运行语义。

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
| 当前状态 | 需求分析完成 |
| 对应变更需求编号 | - |

## 2. 背景与问题

### 2.1 当前行为与证据

1. `ConfigFactory` 通过固定整数槽位保存全局单例 Config，新增配置类型需要修改共享数组和常量。
2. `ConfigManager` 持有进程级可变 `ConfigInfo`，同一 JVM 中多个项目或多个版本会共享并覆盖状态。
3. XML、YAML 解析器直接构造并写入运行时 Config，格式解析、引用解析、校验、注册和发布没有清晰边界。
4. XML 与 YAML 各自维护解析和字段兼容逻辑，无法证明同义配置得到相同语义结果。
5. 错误主要以异常或控制台输出暴露，没有统一错误码、源位置、实体标识和稳定顺序。
6. `RuleConfig` 等注册表按裸字符串名称保存对象，无法为后续强类型标识和多上下文隔离提供基础。

证据来源：`doc/mix-framework-technical-remediation-plan.md`、`doc/mix-framework-p0-p8-detailed-task-plan.md`，以及当前 `dec-core-context`、XML/YAML 解析和 starter 代码。

### 2.2 需要解决的问题

1. 配置格式、配置模型、编译校验和运行时上下文相互耦合，错误晚发现且扩展成本高。
2. 全局可变配置使多项目、多版本、并发初始化和后续热替换无法安全演进。
3. 缺少稳定引用、诊断、不可变注册表和编译摘要，后续阶段没有可信基础。
4. 旧 API 仍被当前运行时使用，必须有明确、只读、可删除的迁移边界。

## 3. 需求目标

1. 建立 XML/YAML 共用的格式中立文档表示和 Raw AST，使同义输入可比较、可追踪。
2. 建立确定性 Compiler Pipeline，集中完成结构校验、符号注册、引用解析、图准备、语义校验和发布。
3. 建立强类型 Key、不可变 Registry、Compiled AST、稳定 Diagnostic 和可重复 digest。
4. 建立实例级不可变 EngineContext，保证两个上下文在同一 JVM 中并存且无状态污染。
5. 通过只读 Legacy Config Adapter 支持旧代码渐进迁移，禁止新代码继续向旧全局 Config 注册。
6. 为 P2～P8 提供可测试、可扩展的编译基础，但不提前实现后续业务语义。

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

- 以 P0 Maven Wrapper、CI、测试失败阻断和旧行为快照为构建基线；实施前必须补齐 P0 动态构建回执。
- 保持 Java 8 源码兼容，强类型 Key 和不可变对象使用普通 final 类。
- `dec-core-compiler` 不依赖 SQL、MySQL、demo 或具体运行时执行模块。
- parser 只负责前端转换，不直接写全局 Config；runtime 不依赖 DOM4J、SnakeYAML 或前端节点。
- 编译包含 ERROR 时不得发布 EngineContext；WARN/INFO 稳定排序并可追踪。
- 同义 XML/YAML 的语义 digest 忽略格式和源位置，但源位置保留在原始声明和诊断中。

## 5. 功能列表

| 功能编号 | 功能名称 | 主责模块 | 协作模块 | 关联流程 | 功能说明 | 状态 |
|---|---|---|---|---|---|---|
| P1-COMPILER-F01 | 统一编译上下文骨架 | dec-core-compiler | dec-core-context、XML/YAML parser、starter、demo | FLOW-CONFIG-COMPILE | 建立统一文档前端、AST、Registry、Compiler Pipeline 与 EngineContext | 已分析 |

## 6. 功能详细需求

### 6.1 P1-COMPILER-F01 统一编译上下文骨架

#### 6.1.1 功能目标

把“按格式直接修改全局 Config”的加载方式重构为“文档前端 → 统一 Raw AST → 编译 → 不可变 EngineContext”的确定性过程，并保留旧行为兼容入口。

#### 6.1.2 角色与权限

- 配置维护者只能提交 XML/YAML 文档，不可通过文档触发任意类实例化或外部资源访问。
- 编译调用者可读取 CompilationResult、Diagnostics 和成功产物，不得修改已发布 Registry/EngineContext。
- Legacy 使用者只可读取投影视图；写入、注册和清空操作必须明确拒绝。

#### 6.1.3 前置条件

- 文档源集合具有确定的资源标识和读取顺序。
- 对应格式的 DocumentFrontend 已注册。
- 编译选项、schema 版本和兼容策略在本次 CompilationSession 内固定。

#### 6.1.4 正常流程

1. 发现并按稳定顺序读取文档源。
2. 格式前端生成 CanonicalDocumentNode，并保留 SourceLocation、格式和 schema 版本。
3. Raw AST Builder 生成格式中立声明，保留声明顺序和未解析引用。
4. 编译器执行结构校验、符号注册、引用解析、图准备和语义校验。
5. 无 ERROR 时发布不可变 Registry、CompiledBusiness 和 EngineContext，并计算稳定语义 digest。
6. 调用者可创建两个独立 Context，且任一 Context 的读取和旧适配不改变另一个 Context。

#### 6.1.5 业务规则

- BR-P1-COMPILER-001：XML 与 YAML 前端必须输出同一 CanonicalDocumentNode 契约，不得各自复制业务校验。
- BR-P1-COMPILER-002：Canonical 节点必须保留节点类型、有序属性、标量、有序子节点、格式、schema 版本和 SourceLocation。
- BR-P1-COMPILER-003：Raw AST 必须覆盖 Data、View、RuleView、System、Information、Directory、Action、Produce 和 SourceRef；P1 只完整编译 Data/View/Rule。
- BR-P1-COMPILER-004：Compiler Pipeline 顺序固定为 parse、structural validation、symbol registration、reference resolution、graph preparation、semantic validation、publish。
- BR-P1-COMPILER-005：普通可聚合错误必须被收集并按源路径、位置、错误码、实体 Key 和 pass 稳定排序。
- BR-P1-COMPILER-006：存在 ERROR 时 CompilationResult 不得包含可发布 EngineContext；不得返回部分成功或空成功。
- BR-P1-COMPILER-007：强类型 Key 必须区分 Data、View、RuleView、System、Information、Directory、Business、Action 命名空间；RuleViewKey 预留 `(system,name)`。
- BR-P1-COMPILER-008：符号注册必须检测重复定义并允许跨文件前向引用；最终 Registry 只读且不可变。
- BR-P1-COMPILER-009：Compiled AST 不得保留未解析字符串引用、DOM、SnakeYAML Node 或其他 parser 节点。
- BR-P1-COMPILER-010：语义 digest 对同义输入稳定，忽略格式和 SourceLocation；源文档 digest 单独保留。
- BR-P1-COMPILER-011：EngineContext 必须实例级、不可变，不提供全局 current Context。
- BR-P1-COMPILER-012：旧 Config Adapter 只读、deprecated，不能注册、删除或修改新旧配置事实。
- BR-P1-COMPILER-013：P2+ 未支持语义必须保留 Raw 声明并产生明确 deferred/unsupported 诊断，不得静默丢弃。

#### 6.1.6 输入与输出约束

- 输入：零到多个 DocumentSource；每个源必须有稳定 sourceId、format 和内容，重复 sourceId 为错误。
- 输出：CompilationResult，包含稳定排序 Diagnostics、源摘要、语义摘要，以及仅在无 ERROR 时存在的 EngineContext。

#### 6.1.7 状态、幂等、并发与事务语义

- 状态：CompilationSession 从 CREATED 依次进入 PARSED、STRUCTURALLY_VALIDATED、SYMBOLS_REGISTERED、REFERENCES_RESOLVED、GRAPH_PREPARED、SEMANTICALLY_VALIDATED，最终 PUBLISHED 或 FAILED。
- 幂等：相同文档集合、schema、编译选项和插件版本重复编译得到相同语义 digest 与诊断顺序。
- 并发：不同 CompilationSession 不共享可变 SymbolTable、DiagnosticCollector 或 RegistryBuilder；成功 Context 可并发读取。
- 事务/部分失败：编译发布是单次构建结果的原子边界；任一 ERROR 导致本次产物不发布，不回写旧全局 Config。

#### 6.1.8 异常与禁止副作用

- 格式错误、未知元素/属性、重复定义、未知引用、非法引用类型和图错误形成 Diagnostic，不修改已存在的 EngineContext。
- Legacy Adapter 写操作抛出明确不支持错误，不偷偷写回旧 Config。
- P1 不执行 System 权限、Information 求值、Directory 路径或事务业务逻辑。

## 7. 跨功能规则

- CR-P1-COMPILER-001：文档格式只影响前端解析和 SourceLocation，不影响后续 AST、Registry、语义校验与 digest 规则。
- CR-P1-COMPILER-002：所有编译状态仅属于一次 CompilationSession；禁止静态可变注册表、当前 Context 和隐式跨次缓存。
- CR-P1-COMPILER-003：所有发布对象以防御性复制和不可变集合建立，构造完成后不可观察到部分状态。
- CR-P1-COMPILER-004：错误码、排序键和实体 Key 是测试和诊断契约；修改必须显式版本化。
- CR-P1-COMPILER-005：兼容适配器不得成为新事实源；任何新能力只进入 Raw/Compiled/EngineContext 路径。
- CR-P1-COMPILER-006：P1 对后续实体只做结构承载和引用占位，不将未确认语义固化为运行行为。

## 8. 异常与边界场景

| 场景编号 | 关联功能 | 场景 | 预期结果 | 禁止副作用 |
|---|---|---|---|---|
| EX-P1-COMPILER-001 | P1-COMPILER-F01 | 空文档集合 | 返回明确空输入诊断或空编译策略结果，不产生隐式全局 Context | 不读取默认目录、不复用上次结果 |
| EX-P1-COMPILER-002 | P1-COMPILER-F01 | XML/YAML 语法错误 | ERROR 含 sourceId 与位置，停止该源构建但聚合其他独立错误 | 不发布部分 Context |
| EX-P1-COMPILER-003 | P1-COMPILER-F01 | 未知元素或属性 | 严格模式产生稳定错误码 | 不忽略未知字段 |
| EX-P1-COMPILER-004 | P1-COMPILER-F01 | 重复 Key | Diagnostic 指向首次和重复定义，Registry 不发布 | 不以最后一次覆盖前值 |
| EX-P1-COMPILER-005 | P1-COMPILER-F01 | 跨文件前向引用 | 全部符号注册后成功解析 | 不要求文件人为排序来规避错误 |
| EX-P1-COMPILER-006 | P1-COMPILER-F01 | 未知或类型不匹配引用 | ERROR 指向引用位置和目标 Key | 不保留未解析字符串到 Compiled AST |
| EX-P1-COMPILER-007 | P1-COMPILER-F01 | 两个并发 CompilationSession | 各自结果和诊断隔离 | 不共享可变 Builder/Collector |
| EX-P1-COMPILER-008 | P1-COMPILER-F01 | Legacy Adapter 尝试写入 | 明确拒绝且状态不变 | 不回写旧 Config 或新 Context |
| EX-P1-COMPILER-009 | P1-COMPILER-F01 | 后续阶段语义出现在 Raw AST | 保留结构并标记 deferred/unsupported | 不执行 P2+ 业务语义，不静默丢弃 |

## 9. 验收标准

### AC-P1-COMPILER-001 同义前端结果

Given 语义等价的最小 XML 与 YAML Data/View/Rule fixture 以及固定 schema 和选项  
When 分别通过对应 DocumentFrontend 和 Raw AST Builder 处理  
Then 两者得到等价的 Canonical/Raw 语义结构与相同语义 digest  
And 格式和 SourceLocation 差异不污染语义比较。

### AC-P1-COMPILER-002 诊断与失败发布

Given 包含结构错误、重复定义、未知引用和非法引用类型的多文件输入  
When 执行完整 Compiler Pipeline  
Then 返回带稳定错误码、sourceId、位置、实体 Key 和 pass 的稳定排序 Diagnostics  
And 任一 ERROR 时不返回可发布 EngineContext、不修改已存在 Context。

### AC-P1-COMPILER-003 强类型注册与前向引用

Given Data、View、Rule 跨文件定义且存在合法前向引用和不同命名空间同名实体  
When 执行符号注册和引用解析  
Then 合法引用解析为强类型对象、不同命名空间互不覆盖、重复同 Key 被拒绝  
And 发布 Registry 不允许后续修改。

### AC-P1-COMPILER-004 不可变 Context 与稳定摘要

Given 两组不同配置和同一组配置的重复编译  
When 在同一 JVM 中创建多个 EngineContext 并并发读取  
Then Context 互不污染且相同输入产生相同语义 digest、不同输入产生不同 digest  
And Compiled AST 不含未解析引用或 parser 节点。

### AC-P1-COMPILER-005 Legacy 只读兼容

Given 已成功发布的 EngineContext 和 Legacy Config 读取调用  
When 通过 adapter 读取并尝试注册、删除或修改  
Then读取结果与 Context 一致，所有写操作明确拒绝  
And adapter 不创建第二份可变事实源。

### AC-P1-COMPILER-006 P1 范围与测试覆盖

Given 含 System、Information、Directory、Action、Produce 等后续声明的输入  
When P1 编译骨架处理该输入并运行 compiler contract tests  
Then Raw AST 保留声明，Data/View/Rule 基础编译可用，未支持语义明确标记 deferred/unsupported  
And 不执行权限、Information 求值、Directory 状态机或 SQL/事务逻辑。

## 10. 非功能要求

### 10.1 性能

- P1 不设绝对性能发布阈值；必须提供可测量的 parse/compile 接缝，避免明显的按引用全量重复扫描。
- Diagnostic 排序和 digest 计算必须确定性，不依赖 HashMap 遍历顺序。

### 10.2 安全与权限

- XML 前端禁止外部实体、DTD 和非预期网络/文件解析。
- YAML 前端禁止任意 Java 类型构造，只读取受控 Node 树。
- SourceLocation 与 Diagnostic 不泄露文档内容中的敏感值。

### 10.3 可靠性、一致性与恢复

- 编译失败不改变调用方当前已用 Context；调用方可继续使用旧 Context。
- 每次编译独立，失败后可用修正后的同一输入重新执行，无残留 Builder 状态。

### 10.4 审计与可观测性

- 每个 Diagnostic 包含 severity、code、message、source、location、entityKey 和 pass。
- CompilationResult 包含 schemaVersion、compilerVersion、sourceDigest、semanticDigest 和耗时统计接缝。

### 10.5 兼容与历史数据

- 旧 Config 读取路径在 P1 保留并标记 deprecated；不删除 declaration，不改变既有 XML/YAML 业务含义。
- Adapter 删除阶段必须在后续迁移文档明确；P1 不做历史数据迁移。

## 11. 模块职责边界

### 11.1 dec-core-compiler 模块

负责：

- DocumentFrontend SPI、Raw AST 构建编排、Compiler Pipeline、pass、SymbolTableBuilder、RegistryBuilder、DiagnosticCollector、digest 和 CompilationResult。

不负责：

- SQL/MySQL、业务运行时执行、demo 编排、P2+ System/Information/Directory 语义。

### 11.2 协作模块

| 模块 | 负责内容 | 输入/输出业务事实 | 失败责任 |
|---|---|---|---|
| dec-core-context | 中立不可变模型、Key、Diagnostic、Registry、EngineContext 和 Legacy 只读视图契约 | 提供不依赖具体格式/运行时的编译契约 | 契约不完整或可变性漏洞 |
| dec-context-config-parse-xml | XML DocumentFrontend、安全解析、位置捕获 | XML → CanonicalDocumentNode | XML 格式和资源诊断 |
| dec-context-config-parse-yaml | 最小 YAML DocumentFrontend、安全 Node 解析、位置捕获 | YAML → CanonicalDocumentNode | YAML 格式和资源诊断 |
| dec-core-starter | 发现文档、组合 frontends/compiler、返回 CompilationResult；不保存全局 current Context | DocumentSource 集合 → 编译调用 | 启动组合和插件缺失诊断 |
| dec-demo | fixture、契约测试和旧行为回归 | 可重复输入与可观察断言 | 测试资源和验收覆盖 |

### 11.3 协作边界

前端只能依赖中立契约并产生 Canonical 节点；Compiler 不反向依赖 XML/YAML 实现；starter 只负责组合；运行时在后续阶段只消费不可变 EngineContext。跨模块顺序、失败与恢复见 `FLOW-CONFIG-COMPILE` 和 `CMI-P1-COMPILER-001`。

## 12. 待确认事项与已确认决策

### 12.1 待确认事项

| 决策编号 | 问题 | 可选方案 | 推荐方案 | 是否阻塞 | 责任人/Agent | 状态 |
|---|---|---|---|---|---|---|
| - | 当前不存在阻塞 P1 设计的待决事项 | - | - | 否 | ProjectManagerAgent | 已闭合 |

### 12.2 已确认决策

| 决策编号 | 结论 | 原因 | 证据/确认来源 | supersedes |
|---|---|---|---|---|
| DEC-P1-COMPILER-001 | P1 只建立 AST、Registry、Compiler、EngineContext 和前端骨架 | 保持阶段依赖和回归边界 | 两份整改文档及用户指令 | - |
| DEC-P1-COMPILER-002 | 新编译对象和 Key 保持 Java 8 兼容 | 与 P0 编译基线一致 | P0 构建基线 | - |
| DEC-P1-COMPILER-003 | EngineContext 实例级且不可变 | 解决多项目、多版本和并发污染 | 技术整改方案 | - |
| DEC-P1-COMPILER-004 | 旧 Config 只读适配，新代码不得注册 | 避免双写和第二事实源 | P1-T10 | - |
| DEC-P1-COMPILER-005 | XML/YAML 共享 Raw AST；完整对等留 P8 | 控制 P1 范围 | P1-T11、P1-T12 | - |
| DEC-P1-COMPILER-006 | 编译失败不发布部分 Context；诊断稳定排序 | 保证产物完整、可复现 | P1-T04、T08、T09 | - |

## 13. 追踪关系

| 追踪编号 | 功能编号 | 业务规则/跨功能规则 | 验收标准 | 关联流程 | 影响分析 | 后续业务模型 | 后续设计 | 测试 Case | 状态 |
|---|---|---|---|---|---|---|---|---|---|
| TR-P1-COMPILER-001 | P1-COMPILER-F01 | BR-P1-COMPILER-001、BR-P1-COMPILER-002、BR-P1-COMPILER-003、CR-P1-COMPILER-001 | AC-P1-COMPILER-001 | FLOW-CONFIG-COMPILE | IMP-P1-COMPILER-001、CMI-P1-COMPILER-001 | 待业务模型 | 待设计 | CASE-P1-CANONICAL-001 | PENDING |
| TR-P1-COMPILER-002 | P1-COMPILER-F01 | BR-P1-COMPILER-004、BR-P1-COMPILER-005、BR-P1-COMPILER-006、CR-P1-COMPILER-004 | AC-P1-COMPILER-002 | FLOW-CONFIG-COMPILE | IMP-P1-COMPILER-002、CMI-P1-COMPILER-001 | 待业务模型 | 待设计 | CASE-P1-DIAGNOSTIC-001 | PENDING |
| TR-P1-COMPILER-003 | P1-COMPILER-F01 | BR-P1-COMPILER-007、BR-P1-COMPILER-008 | AC-P1-COMPILER-003 | FLOW-CONFIG-COMPILE | IMP-P1-COMPILER-003、CMI-P1-COMPILER-001 | 待业务模型 | 待设计 | CASE-P1-SYMBOL-001 | PENDING |
| TR-P1-COMPILER-004 | P1-COMPILER-F01 | BR-P1-COMPILER-009、BR-P1-COMPILER-010、BR-P1-COMPILER-011、CR-P1-COMPILER-002、CR-P1-COMPILER-003 | AC-P1-COMPILER-004 | FLOW-CONFIG-COMPILE | IMP-P1-COMPILER-004、CMI-P1-COMPILER-001 | 待业务模型 | 待设计 | CASE-P1-CONTEXT-001 | PENDING |
| TR-P1-COMPILER-005 | P1-COMPILER-F01 | BR-P1-COMPILER-012、CR-P1-COMPILER-005 | AC-P1-COMPILER-005 | FLOW-CONFIG-COMPILE | IMP-P1-COMPILER-005、CMI-P1-COMPILER-001 | 待业务模型 | 待设计 | CASE-P1-LEGACY-001 | PENDING |
| TR-P1-COMPILER-006 | P1-COMPILER-F01 | BR-P1-COMPILER-013、CR-P1-COMPILER-006 | AC-P1-COMPILER-006 | FLOW-CONFIG-COMPILE | IMP-P1-COMPILER-006、CMI-P1-COMPILER-001 | 待业务模型 | 待设计 | CASE-P1-SCOPE-001 | PENDING |

## 14. 变更记录

| 文档 revision | 日期 | 阶段 | 变更内容 | 责任 Agent |
|---|---|---|---|---|
| R01 | 2026-07-24 | 需求确认 | 冻结目标、范围、约束和关键决策 | RequirementConfirmationAgent |
| R02 | 2026-07-24 | 需求分析 | 补全功能、13 条规则、6 条跨规则、9 个异常、6 条 AC、NFR、模块边界与追踪 | RequirementAnalysisAgent |
