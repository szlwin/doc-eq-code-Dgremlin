# DEC_COMPILER P1 详细设计

- Revision：`DESIGN-R01@a7a6820a381e`
- 输入：`REQAN-R02@d38b7f83f222`、`BM-R01@52a58f20cb32`
- 范围：P1 AST、Registry、Compiler、EngineContext 与只读 Legacy Adapter 骨架
- 不包含：P2+ System 权限、Information、Directory/Action/Produce、Query、事务运行语义

## 1. 设计目标

把现有“XML/YAML 直接写全局 Config”改为唯一事实链：

```text
DocumentSource[]
  -> DocumentFrontend(XML/YAML)
  -> CanonicalDocumentNode
  -> RawDocumentSet
  -> Compiler Passes
  -> CompiledBusiness + immutable Registries
  -> EngineContext
  -> optional LegacyConfigView(read-only)
```

设计必须满足：确定性、错误聚合、ERROR 不发布、Context 实例隔离、Java 8 兼容、旧读取渐进迁移。

## 2. 模块职责

| 模块 | 设计职责 | 禁止依赖/行为 |
|---|---|---|
| `dec-core-context` | 中立不可变值对象、Key、Diagnostic、Compiled contracts、EngineContext、LegacyConfigView 接口 | 不依赖 DOM4J/SnakeYAML/compiler/sql/mysql/demo；不保存全局 current Context |
| `dec-core-compiler` | Frontend SPI、Raw AST、Pass、SymbolTableBuilder、RegistryBuilder、digest、CompilationResult | 不依赖 XML/YAML 实现、starter、runtime、SQL/MySQL、demo |
| `dec-context-config-parse-xml` | 安全 XML frontend、SourceLocation 捕获、Canonical 节点生成 | 不执行业务校验、不写 ConfigFactory/ConfigManager |
| `dec-context-config-parse-yaml` | 受控 YAML Node frontend、Mark 位置捕获、最小等价路径 | 不允许任意 Java 类型构造，不复制业务校验 |
| `dec-core-starter` | 发现 source、组合 frontend/compiler/plugin、显式返回 CompilationResult | 不持有静态 Context，不吞编译错误 |
| `dec-demo` | fixtures、contract tests、legacy regression | 不成为生产模块依赖 |

依赖方向：`context <- compiler <- frontends/starter/demo`；frontends 依赖 `compiler + context`，compiler 不反向依赖 frontends。

## 3. 核心数据结构

### 3.1 DocumentSource 与 CanonicalDocumentNode

- `DocumentSource`：`sourceId`、`DocumentFormat`、只读内容、`sourceDigest`。
- `CanonicalDocumentNode`：`nodeType`、有序属性、可选 scalar、有序 children、`SourceRef`、`schemaVersion`。
- 属性与子节点顺序用于重现原始声明；语义规范化阶段决定哪些顺序参与 digest。
- XML/YAML 的格式与位置保留在 SourceRef，不参与 semanticDigest。

### 3.2 Raw AST

P1 定义 `RawData`、`RawView`、`RawRuleView`、`RawSystem`、`RawInformation`、`RawDirectory`、`RawAction`、`RawProduce` 和 `RawReference`。Data/View/Rule 完成基础编译；其他声明保存结构和 SourceRef，并由 `DeferredSemanticPolicy` 标记，不进入运行执行。

### 3.3 Key 与 SymbolTable

- `DataKey(name)`、`ViewKey(name)`、`RuleViewKey(system,name)`、`SystemKey(name)`、`InformationKey(business,name)`、`DirectoryKey(business,name)`、`BusinessKey(name)`、`ActionKey(business,directory,name)`。
- Java 8 使用 `final` 类、构造校验、值相等、稳定 `compareTo`/canonical string。
- `SymbolTableBuilder` 分类型登记全部声明；重复同 Key 记录首次与重复 SourceRef。
- 前向引用在符号注册完成后统一解析，不依赖文件人为排序。

## 4. Compiler Pipeline

| 顺序 | Pass | 输入 | 输出 | 可聚合错误 | 终止边界 |
|---:|---|---|---|---|---|
| 1 | Parse | DocumentSource | Canonical/Raw candidates | 格式、资源、安全 | 单源不可继续但继续读取独立源 |
| 2 | StructuralValidation | Raw declarations | validated raw | 未知元素/属性、必填结构 | 严重结构错误阻断该声明后续 |
| 3 | SymbolRegistration | validated raw | mutable SymbolTableBuilder | 重复 Key | 完成所有可登记符号 |
| 4 | ReferenceResolution | raw + symbols | resolved refs | unknown/type mismatch | 不把未解析引用带入 compiled |
| 5 | GraphPreparation | resolved declarations | graph placeholders/indexes | 环、非法边、deferred | P1 不执行 P2+ 图语义 |
| 6 | SemanticValidation | resolved structures | validated compiled candidates | 不变量、范围越界 | 聚合普通错误 |
| 7 | Publish | candidates + diagnostics | immutable registries/context | 不可变性与完整性 | 任一 ERROR -> FAILED，无 Context |

每个 pass 实现独立接口，输入输出只使用中立模型；`CompilerPipeline` 固定顺序，不允许插件重排核心 pass。

## 5. Diagnostic 设计

字段：`severity`、稳定 `code`、脱敏 `message`、`SourceRef`、`entityKey`、`passId`、`ordinal`、可选 related locations。

稳定排序键：

```text
severity rank -> sourceId -> line -> column -> code -> entityKey canonical -> pass order -> ordinal
```

普通结构/引用错误聚合；不可恢复基础错误仍生成 Diagnostic。禁止 `printStackTrace`、`return null` 或空成功替代错误。

建议 P1 错误码域：

- `DEC-SRC-*` 输入源；
- `DEC-FMT-*` XML/YAML 格式与安全；
- `DEC-STR-*` 结构；
- `DEC-SYM-*` 重复符号；
- `DEC-REF-*` 引用；
- `DEC-SEM-*` 语义/范围；
- `DEC-PUB-*` 发布；
- `DEC-LEG-*` Legacy 写入。

## 6. Digest 与确定性

- `sourceDigest`：按 sourceId 稳定排序后，包含原始内容摘要、格式和 schemaVersion。
- `semanticDigest`：只对规范化 CompiledBusiness 编码；忽略格式、SourceLocation、Map/线程遍历顺序。
- digest 输入包含 schemaVersion、compilerVersion、CompilationOptions digest、影响语义的 plugin descriptors。
- 编码采用显式字段顺序、UTF-8、长度前缀，不使用默认 Java serialization。

## 7. 发布与不可变性

1. Builder、collector、临时 graph 只存在于一个 CompilationSession。
2. Publish 前执行 unresolved/parser-node/mutability 检查。
3. Registry 由防御性复制和不可变集合构造。
4. `CompilationResult.engineContext` 仅在 diagnostics 无 ERROR 时存在。
5. 新 Context 不自动替换旧 Context；调用方显式选择使用哪个实例。
6. P1 不实现 P8 原子热替换，只保证可安全构造并存快照。

## 8. Legacy Config Adapter

- `LegacyConfigView` 由一个 EngineContext 投影；读取语义保持旧 API 所需形状。
- 所有 `add/register/remove/clear/set` 明确抛 `LegacyWriteUnsupportedException`。
- 标记 deprecated，并记录删除阶段；新代码禁止从 adapter 注册。
- 不双写 ConfigFactory/ConfigManager，不把 adapter 作为缓存或事实源。

## 9. 安全设计

- XML 使用 StAX/SAX 安全配置：禁用 DTD、外部实体、外部 schema 和网络解析；限制递归深度/属性/文本长度。
- YAML 使用 safe/compose Node 路径，拒绝任意类型标签；限制 alias、深度、节点数和 scalar 长度。
- SourceLocation/Diagnostic 对敏感 scalar 只显示路径或摘要，不回显值。
- Frontend 不允许配置触发反射实例化、文件包含、URL 获取或类加载。

## 10. 并发、幂等与恢复

- 每次 `compile` 新建 Session；所有 Builder/Collector 为实例字段，不使用 static mutable。
- EngineContext/Registry 可并发读取；用户插件在 P1 只允许不可变 descriptor。
- 同输入和版本重复编译产生相同 digest/diagnostic order。
- 编译失败无补偿写操作；调用方继续使用旧 Context，修正输入后创建新 Session 重试。

## 11. P1-T01～T13 落地顺序

1. T01 模块与架构测试；
2. T02 Canonical node；
3. T03 Raw AST；
4. T04 SourceLocation/Diagnostic；
5. T05 强类型 Key；
6. T06 SymbolTable/RegistryBuilder；
7. T07 Compiled AST/digest；
8. T08 Pipeline；
9. T09 EngineContext；
10. T10 Legacy read-only adapter；
11. T11 XML frontend；
12. T12 最小 YAML frontend；
13. T13 contract/compiler tests。

任何子任务失败均不得以空实现、忽略测试或静默 deferred 代替。

## 12. 需求追踪

| Trace | 设计实现点 |
|---|---|
| TR-P1-COMPILER-001 | DocumentFrontend、Canonical node、Raw AST、digest normalization |
| TR-P1-COMPILER-002 | Pass、DiagnosticCollector、ERROR publish gate |
| TR-P1-COMPILER-003 | TypedKey、SymbolTableBuilder、ReferenceResolver、immutable Registry |
| TR-P1-COMPILER-004 | Compiled AST、digest、CompilationSession、EngineContext |
| TR-P1-COMPILER-005 | LegacyConfigView 与写入拒绝 |
| TR-P1-COMPILER-006 | DeferredSemanticPolicy 与 P1 scope guard |
