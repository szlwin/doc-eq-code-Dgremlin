# DEC_COMPILER 业务模型

- Revision：`BM-R01@52a58f20cb32`
- 范围：P1 统一 AST、Registry、Compiler 与 EngineContext 骨架
- 明确不做：P2+ System 权限、Information、Directory/Action/Produce、查询和事务运行语义

## 1. 统一语言

| ID | 术语 | 定义 |
|---|---|---|
| `TERM-DOCUMENT-SOURCE` | DocumentSource | 具有稳定 sourceId、受控格式、内容和源摘要的不可变输入值；不得隐式读取默认目录。 |
| `TERM-CANONICAL-NODE` | CanonicalDocumentNode | XML/YAML 共享的格式中立有序节点，保留节点类型、属性、标量、子节点、格式、schemaVersion 和 SourceLocation。 |
| `TERM-RAW-AST` | RawDocumentSet | 保留声明顺序、SourceRef 与未解析强类型引用的格式中立声明集合；P2+ 声明在 P1 只被承载。 |
| `TERM-COMPILATION-SESSION` | CompilationSession | 一次编译的唯一构建一致性边界，拥有独立的选项、Builder、SymbolTable 与 DiagnosticCollector。 |
| `TERM-DIAGNOSTIC` | Diagnostic | 包含 severity、code、message、source、location、entityKey 与 pass 的结构化问题事实，并按稳定键排序。 |
| `TERM-COMPILED-BUSINESS` | CompiledBusiness | 已解析引用、无 parser 节点、不可变且可计算稳定语义摘要的发布前编译产物。 |
| `TERM-ENGINE-CONTEXT` | EngineContext | 仅在无 ERROR 时发布的实例级不可变运行时上下文；不同实例不共享可变状态。 |
| `TERM-LEGACY-VIEW` | LegacyConfigView | 由 EngineContext 投影的 deprecated 只读兼容视图，不拥有、注册或修改事实。 |

## 2. 核心场景

### SCN-COMPILE-SUCCESS
- Given：稳定排序的 DocumentSource 集合；已注册受控 DocumentFrontend；固定 schema/compiler/options/plugin 版本
- When：执行完整七步 Compiler Pipeline
- Then：生成不可变 CompiledRegistry 与 EngineContext；输出稳定 Diagnostics、sourceDigest 与 semanticDigest；不写入旧全局 Config
- Trace：TR-P1-COMPILER-001, TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-004, TR-P1-COMPILER-005, TR-P1-COMPILER-006

### SCN-COMPILE-FAILURE
- Given：输入包含格式、结构、重复、未知引用或语义错误；已有 EngineContext 正在被调用方使用
- When：执行编译并出现 ERROR
- Then：聚合并稳定排序 Diagnostics；本次 CompilationResult 不包含 EngineContext；既有 EngineContext 保持不变
- Trace：TR-P1-COMPILER-002, TR-P1-COMPILER-004

### SCN-CONTEXT-ISOLATION
- Given：两组不同输入或相同输入重复编译
- When：并发创建并读取多个 CompilationSession 与 EngineContext
- Then：构建状态、诊断和注册表互不污染；相同输入语义 digest 一致；不同输入 digest 不同
- Trace：TR-P1-COMPILER-004

### SCN-LEGACY-READONLY
- Given：已发布 EngineContext；旧代码通过兼容入口读取配置
- When：读取并尝试写入、注册、删除或清空
- Then：读取结果与 EngineContext 一致；所有写操作明确拒绝；不建立第二份可变事实源
- Trace：TR-P1-COMPILER-005

### SCN-DEFERRED-SEMANTICS
- Given：Raw AST 含 System、Information、Directory、Action 或 Produce 声明
- When：P1 编译骨架处理输入
- Then：结构与 SourceRef 被保留；产生明确 deferred/unsupported 诊断；不执行 P2+ 业务语义
- Trace：TR-P1-COMPILER-006

## 3. 聚合与一致性边界

| 聚合 | 根 | 成员 | 一致性边界 |
|---|---|---|---|
| `AGG-COMPILATION-SESSION` | `ENT-COMPILATION-SESSION` | ENT-DOCUMENT-SOURCE, ENT-RAW-DECLARATION, ENT-DIAGNOSTIC, VO-SOURCE-REF, VO-CANONICAL-NODE, VO-TYPED-KEY, VO-COMPILATION-RESULT | 一次 compile 调用；构建状态仅在 session 内可变，发布结果一次性生成。 强一致；有 ERROR 不发布，无部分成功。 |
| `AGG-ENGINE-CONTEXT` | `ENT-ENGINE-CONTEXT` | ENT-COMPILED-BUSINESS, VO-LEGACY-CONFIG-VIEW | 构造完成后的不可变快照；不支持原地更新。 并发只读一致；新编译产物与旧 Context 并存。 |

## 4. 核心不变量

| ID | 不变量 | 触发 | 失败 |
|---|---|---|---|
| `INV-COMPILER-001` | Compiler pass 顺序固定且不可跳跃或逆序。 | 每次 session 状态迁移 | `ERR-COMPILER-ILLEGAL-STATE` |
| `INV-COMPILER-002` | 存在任一 ERROR 时不得生成或发布 EngineContext。 | semantic validation 与 publish | `ERR-COMPILER-PUBLISH-BLOCKED` |
| `INV-COMPILER-003` | Compiled AST 与 Registry 不得包含 parser 节点、未解析字符串引用或可变集合。 | publish 前验证 | `ERR-COMPILER-UNRESOLVED-OR-MUTABLE` |
| `INV-COMPILER-004` | 同一 session 中 TypedKey 唯一；不同命名空间同名实体互不覆盖，跨文件前向引用在注册完成后解析。 | symbol registration/reference resolution | `ERR-COMPILER-SYMBOL` |
| `INV-COMPILER-005` | 相同规范输入、schema、compiler/options/plugin 版本得到相同 semanticDigest 与诊断顺序。 | digest 与 result 生成 | `ERR-COMPILER-NONDETERMINISTIC` |
| `INV-COMPILER-006` | 不同 CompilationSession 不共享可变 Builder、Collector、SymbolTable 或 current Context。 | session 创建与并发编译 | `ERR-COMPILER-SESSION-LEAK` |
| `INV-COMPILER-007` | LegacyConfigView 只读且不拥有独立事实；P2+ 声明在 P1 只承载不执行。 | legacy 写操作或 deferred 声明处理 | `ERR-COMPILER-SCOPE-VIOLATION` |

## 5. CompilationSession 状态机

```text
CREATED -> PARSED -> STRUCTURALLY_VALIDATED -> SYMBOLS_REGISTERED
-> REFERENCES_RESOLVED -> GRAPH_PREPARED -> SEMANTICALLY_VALIDATED
-> PUBLISHED
任一步 ERROR -> FAILED
```

| 迁移 | From | Command | To | 失败 |
|---|---|---|---|---|
| `TRN-PARSE` | CREATED | `parseDocuments` | PARSED | FAILED |
| `TRN-STRUCTURE` | PARSED | `validateStructure` | STRUCTURALLY_VALIDATED | FAILED |
| `TRN-SYMBOLS` | STRUCTURALLY_VALIDATED | `registerSymbols` | SYMBOLS_REGISTERED | FAILED |
| `TRN-REFERENCES` | SYMBOLS_REGISTERED | `resolveReferences` | REFERENCES_RESOLVED | FAILED |
| `TRN-GRAPH` | REFERENCES_RESOLVED | `prepareGraphs` | GRAPH_PREPARED | FAILED |
| `TRN-SEMANTIC` | GRAPH_PREPARED | `validateSemantics` | SEMANTICALLY_VALIDATED | FAILED |
| `TRN-PUBLISH` | SEMANTICALLY_VALIDATED | `publish` | PUBLISHED | FAILED |

## 6. 服务与策略

| ID | 名称 | 输入 | 输出 |
|---|---|---|---|
| `SVC-DOCUMENT-FRONTEND` | DocumentFrontend | DocumentSource, FrontendOptions | CanonicalDocumentNode, Diagnostic[] |
| `SVC-BUSINESS-COMPILER` | BusinessCompiler | DocumentSource[], CompilationOptions, FrontendRegistry | CompilationResult |
| `SVC-LEGACY-PROJECTION` | LegacyConfigProjectionService | EngineContext | LegacyConfigView |
| `POL-DIAGNOSTIC-ORDER` | DiagnosticStableOrderPolicy | Diagnostic[] | 按 sourceId/location/code/entityKey/pass/ordinal 排序的 Diagnostic[] |
| `POL-DIGEST` | SemanticDigestPolicy | Canonical Raw/Compiled structure, schema/compiler/options/plugin version | sourceDigest, semanticDigest |
| `POL-DEFERRED` | DeferredSemanticPolicy | P2+ RawDeclaration | 保留声明, deferred/unsupported Diagnostic |

## 7. 业务错误

| ID | 条件 | 含义 | 可重试 | 状态改变 |
|---|---|---|---|---|
| `ERR-COMPILER-SOURCE` | sourceId 重复、空输入策略不满足或资源不可读 | 输入源集合无效 | 是 | 否 |
| `ERR-COMPILER-FORMAT` | XML/YAML 语法错误、XXE/DTD 或任意类型构造尝试 | 前端拒绝不安全或无效格式 | 是 | 否 |
| `ERR-COMPILER-STRUCTURE` | 未知元素/属性或缺失必填结构 | Raw 声明结构无效 | 是 | 否 |
| `ERR-COMPILER-SYMBOL` | TypedKey 重复、命名空间错误或引用目标类型不匹配 | 符号注册或引用解析失败 | 是 | 否 |
| `ERR-COMPILER-PUBLISH-BLOCKED` | 存在 ERROR 或不可变性/完整性检查失败 | 本次结果不得发布 EngineContext | 是 | 否 |
| `ERR-COMPILER-SESSION-LEAK` | 检测到跨 Session 可变状态、静态 Registry 或全局 current Context | 编译隔离契约被破坏 | 否 | 否 |
| `ERR-COMPILER-LEGACY-WRITE` | 通过 LegacyConfigView 注册、修改、删除或清空 | 只读迁移边界被违反 | 否 | 否 |
| `ERR-COMPILER-SCOPE-VIOLATION` | P1 尝试执行 System 权限、Information、Directory、Action、Produce 或 SQL/事务语义 | 阶段范围越界 | 否 | 否 |

## 8. 追踪

| Trace | Model Refs |
|---|---|
| `TR-P1-COMPILER-001` | `TERM-CANONICAL-NODE`, `ENT-DOCUMENT-SOURCE`, `VO-CANONICAL-NODE`, `SVC-DOCUMENT-FRONTEND`, `POL-DIGEST` |
| `TR-P1-COMPILER-002` | `ENT-DIAGNOSTIC`, `VO-COMPILATION-RESULT`, `INV-COMPILER-001`, `INV-COMPILER-002`, `POL-DIAGNOSTIC-ORDER`, `ERR-COMPILER-PUBLISH-BLOCKED` |
| `TR-P1-COMPILER-003` | `VO-TYPED-KEY`, `ENT-RAW-DECLARATION`, `INV-COMPILER-004`, `AGG-COMPILATION-SESSION` |
| `TR-P1-COMPILER-004` | `ENT-COMPILED-BUSINESS`, `ENT-ENGINE-CONTEXT`, `INV-COMPILER-003`, `INV-COMPILER-005`, `INV-COMPILER-006`, `SM-COMPILATION-SESSION` |
| `TR-P1-COMPILER-005` | `VO-LEGACY-CONFIG-VIEW`, `SVC-LEGACY-PROJECTION`, `INV-COMPILER-007`, `ERR-COMPILER-LEGACY-WRITE` |
| `TR-P1-COMPILER-006` | `POL-DEFERRED`, `INV-COMPILER-007`, `ERR-COMPILER-SCOPE-VIOLATION`, `SCN-DEFERRED-SEMANTICS` |

## 9. 关键模型结论

1. `CompilationSession` 是唯一可变构建聚合；`EngineContext` 是不可变发布聚合。
2. ERROR 与发布互斥，不存在部分成功或空成功。
3. Raw AST 可保留 P2+ 声明，但 P1 不执行其业务语义。
4. `LegacyConfigView` 只读，不形成第二事实源。
5. XML/YAML 只在 DocumentFrontend 层有差异，后续模型和 digest 统一。
