# DEC_COMPILER 业务模型

> Revision：`BM-R05@4ecb1f8c09f4`。输入：`REQAN-R05@7de35e8dc15b`；上一正式版本：`BM-R04@1b19a0ba26b6`。结构化事实源为同目录 YAML，本文提供等价的人类可读视图。

## 1. 模块使命与边界

DEC_COMPILER 将调用方提供的配置根 Source 编译为不可变、可追踪、可复现的 `CompiledModelSet` 与实例级 `EngineContext`。它负责 P1 的源发现、格式中立结构、强类型符号、P1 引用解析、Deferred 登记、Diagnostic、摘要，并在同一次 compile 调用内通过注入的 `ContextPublisher` 完成条件原子发布；不执行 P2～P8 的运行语义，不拥有数据库连接，不生成大批业务 Java 类，也不恢复 `dec-expand-declaration` 或第二运行时。

## 2. 统一语言

| 术语 ID | 标准术语 | 定义 | 禁止混用词 |
|---|---|---|---|
| TERM-COMPILATION-SESSION | `CompilationSession` | 一次根 Source、编译选项、构建期 Registry、Diagnostic 与结果的隔离编译会话；只在构建期间可变。 | global current compiler, shared mutable compiler |
| TERM-RAW-DEFINITION | `RawDefinition` | 从 CanonicalDocumentNode 构建、保留 SourceRef 且尚未执行后续阶段运行语义的格式中立定义。 | DOM definition, runtime object |
| TERM-TYPED-KEY | `TypedKey` | 由定义类型及其命名空间组成的强类型身份；不同定义类型不得共用字符串命名空间。 | string id, global name |
| TERM-INFORMATION-OWNER | `System-owned Information` | InformationKey 由 SystemKey 与局部名称组成；BusinessScope 只消费限定引用，不拥有 Information。 | BusinessScope information, scope-owned information |
| TERM-COMMON-SYSTEM | `common System` | 只拥有跨 System expression Information 的特殊 System；不拥有 Data、View、RuleView、ModelAccess 或运行时编排。 | global system, shared runtime |
| TERM-MODEL-ACCESS-BINDING | `ModelAccessBinding` | 共享模型源路径与当前 System 本地 View 目标选择器之间的显式一对多绑定。 | name inference, implicit view mapping |
| TERM-TARGET-MAIN | `target-main` | View 根目标选择器；ref@property 必须先对其做区分大小写的完整匹配。 | root-property, fuzzy root alias |
| TERM-DEFERRED-DEFINITION | `DeferredDefinition` | P1 已完成结构和 Key 解析、但明确交由 P2～P8 执行的语义登记。 | ignored definition, partially compiled runtime |
| TERM-COMPILED-MODEL-SET | `CompiledModelSet` | 无 ERROR 后一次性构造并冻结的全部编译事实集合。 | mutable config registry |
| TERM-ENGINE-CONTEXT | `EngineContext` | 实例级不可变运行时读取入口；不同 Context 不共享可变 Registry，且不存在全局 current。 | singleton context, global current context |
| TERM-DIAGNOSTIC | `Diagnostic` | 包含稳定 code、severity、SourceRef、definitionKey、relatedRefs、pass 与 recoveryHint 的可排序失败事实。 | generic exception, operation failed |
| TERM-SOURCE-REF | `SourceRef` | 规范化 sourceId、行、列和节点路径组成的不可变来源位置。 | raw file path only |
| TERM-SEMANTIC-DIGEST | `semanticDigest` | 由规范化语义、稳定选项和编译器版本计算的确定性摘要，与原始 sourceDigest 分离。 | file hash only |
| TERM-CORE-CONFIG-PROJECTION | `CoreConfigProjection` | 从同一 CompiledModelSet 派生的 deprecated 只读 Data/View/Rule 投影，不拥有第二 Registry。 | legacy mutable config, compatibility runtime |

## 3. 场景模型

| 场景 ID | Given | When | Then | 追踪 |
|---|---|---|---|---|
| SCN-COMPILER-SUCCESS | 根 DocumentSource 位于允许根路径；XML/YAML frontend 与 ContextPublisher 已注入；所有引用最终可解析 | CompilationSession 执行发现、解析、Raw 构建、符号注册、引用解析、Deferred 分类，并以 expectedCurrent 条件发布 | 产生不可变 CompiledModelSet 与 EngineContext；ContextPublisher 在同一次 compile 调用内原子替换 expectedCurrent；sourceDigest 与 semanticDigest 均记录；Diagnostic 无 ERROR | TR-P1-COMPILER-001, TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-004, TR-P1-COMPILER-005 |
| SCN-COMPILER-INVALID-REFERENCE | 输入包含未知引用、重复 TypedKey 或归属冲突；调用方持有旧 EngineContext | ReferenceResolutionService 或验证策略产生 ERROR | CompilationSession 进入 FAILED；不构造或发布新 CompiledModelSet；调用方旧 EngineContext 保持原值 | TR-P1-COMPILER-003, TR-P1-COMPILER-005 |
| SCN-COMMON-EXPRESSION | common.payError expression 引用 payment.error 与 order.payErrorStatus；两个引用均为已注册 InformationKey | InformationOwnershipPolicy 编译 common expression | 建立 common.payError 到两个 system-qualified InformationKey 的依赖；登记 P3 expression evaluation Deferred；common 不获得 Data/View/RuleView/ModelAccess | TR-P1-COMPILER-004, TR-P1-COMPILER-008 |
| SCN-MODEL-ACCESS-TARGET-MAIN | read@path=user；ref@view=UserInfo；ref@property=user；UserInfo.target-main=user | ModelAccessSelectorPolicy 解析目标 | 源路径保持 SharedModelPath(user)；selector 精确命中 target-main；绑定到 UserInfo 根目标且不查找其它 View | TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| SCN-MODEL-ACCESS-PROPERTY-FALLBACK | selector 未命中 target-main；目标 View 属性树存在唯一精确 property path | ModelAccessSelectorPolicy 逐段解析 property path | 建立唯一属性级绑定；路径段区分大小写；任一非复合中间段或多候选均产生 ERROR | TR-P1-COMPILER-009 |
| SCN-MULTI-CONTEXT-ISOLATION | 两个 CompilationSession 使用不同输入；两个结果同时存在于同一 JVM | 调用方分别读取两个 EngineContext | Registry、Diagnostic 与 digest 互不污染；任一 Context 不提供写入口或全局 current | TR-P1-COMPILER-005, TR-P1-COMPILER-006 |
| SCN-RETIRE-DECLARATION | dec-expand-declaration 曾是临时模块 | 执行仓库、Reactor、依赖、服务、反射字符串和 artifact 残留扫描 | 不存在模块或 Adapter 残留；必要场景只基于 mix fixture 重写；恢复仅使用 Git revert | TR-P1-COMPILER-007 |
| SCN-SECURE-FRONTEND | 输入包含 XML 外部实体、网络 URI、路径逃逸或 YAML 任意类型标签 | SourceDiscoveryPolicy 或 CanonicalizationService 读取输入 | 在 I/O 或 frontend 边界产生稳定 ERROR；不访问网络或允许根目录外资源；不进入 RawDefinitionSet | TR-P1-COMPILER-001, TR-P1-COMPILER-002 |

## 4. 聚合与一致性边界

| 聚合 ID | 根 | 成员 | 事务/原子边界 | 一致性 | 不变量 |
|---|---|---|---|---|---|
| AGG-COMPILATION-SESSION | `ENT-COMPILATION-SESSION` | ENT-MIX-SOURCE-GRAPH, ENT-RAW-DEFINITION-SET, ENT-SYMBOL-TABLE, ENT-DEFERRED-REGISTRY, VO-DOCUMENT-SOURCE, VO-CANONICAL-DOCUMENT-NODE, VO-SOURCE-REF, VO-TYPED-KEY, VO-INFORMATION-KEY, VO-MODEL-ACCESS-BINDING, VO-DEFERRED-DEFINITION, VO-DIAGNOSTIC, VO-DIGEST-PAIR, VO-COMPILATION-RESULT | 一次 compile 调用；构建期变化只存在于该 Session，成功时由 compiler 调用注入的 ContextPublisher 条件发布后才进入 PUBLISHED | 强一致、全有或全无；不同 Session 不共享可变对象 | INV-COMPILER-001, INV-COMPILER-002, INV-COMPILER-003, INV-COMPILER-004, INV-COMPILER-005, INV-COMPILER-006, INV-COMPILER-007, INV-COMPILER-008, INV-COMPILER-009, INV-COMPILER-010, INV-COMPILER-011, INV-COMPILER-012, INV-COMPILER-013 |
| AGG-PUBLISHED-CONTEXT | `ENT-ENGINE-CONTEXT` | ENT-COMPILED-MODEL-SET, ENT-CORE-CONFIG-PROJECTION, VO-DIGEST-PAIR | 只有 PUBLISHED 终态才能一次性创建并交给调用方；失败不替换旧 Context | 发布后永久不可变；Projection 与 CompiledModelSet 同源 | INV-COMPILER-009, INV-COMPILER-010, INV-COMPILER-011, INV-COMPILER-014 |

### 4.1 Compilation Session Aggregate

- `CompilationSession` 是唯一允许构建期变化的聚合根；SourceGraph、RawDefinitionSet、SymbolTable、Deferred Registry 和 Diagnostic 只属于该 Session。
- 所有 TypedKey 注册完成后才允许解析前向引用；任何 ERROR 使整个 Session 失败。
- compiler 是原子发布用例的唯一编排者；Starter 只注入 `ContextPublisher`、`expectedCurrent` 和其它依赖，不得在 compile 返回后执行第二次发布。
- Session 终态只有 `PUBLISHED` 或 `FAILED`，终态后不得继续写入。

### 4.2 Published Context Aggregate

- `CompiledModelSet` 与 `EngineContext` 只在无 ERROR 时一次性构造并冻结。
- 发布失败不替换调用方已经持有的旧 Context。
- `CoreConfigProjection` 只从同一 CompiledModelSet 派生只读 Data/View/Rule 视图，不建立第二 Registry。

## 5. 实体和值对象

### 5.1 实体

| ID | 对象 | 身份 | 关键行为 | 生命周期/边界 | 追踪 |
|---|---|---|---|---|---|
| ENT-COMPILATION-SESSION | `CompilationSession` | `sessionId` | discoverSources, parseCanonicalDocuments, buildRawDefinitions, registerSymbols, resolveReferences, classifyDeferredDefinitions, publishOrFail | CREATED 到 PUBLISHED 或 FAILED；终态后不得继续写入 | TR-P1-COMPILER-001, TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-004, TR-P1-COMPILER-005, TR-P1-COMPILER-006, TR-P1-COMPILER-007, TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| ENT-MIX-SOURCE-GRAPH | `MixSourceGraph` | `rootSourceId` | addUniqueSource, addTypedEdge, iterateInCanonicalOrder | dec-core-compiler | TR-P1-COMPILER-001 |
| ENT-RAW-DEFINITION-SET | `RawDefinitionSet` | `compilationSessionId` | addWithoutOverwrite, readByTypedKey, freezeForPasses | - | TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-008 |
| ENT-SYMBOL-TABLE | `SymbolTable` | `compilationSessionId` | registerUnique, sealRegistration, resolveForwardReference | - | TR-P1-COMPILER-003, TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| ENT-DEFERRED-REGISTRY | `DeferredDefinitionRegistry` | `compilationSessionId` | registerCompleteDeferred, rejectMissingBoundary, freeze | - | TR-P1-COMPILER-004, TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| ENT-COMPILED-MODEL-SET | `CompiledModelSet` | `semanticDigest` | readTypedRegistry, readDeferredDefinitions, readDiagnostics | 无 ERROR 时一次构造并永久不可变 | TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-004, TR-P1-COMPILER-005, TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| ENT-ENGINE-CONTEXT | `EngineContext` | `contextId` | readCompiledModels, createCoreConfigProjection | 由成功 CompilationResult 创建；无全局 current；不提供写入 API | TR-P1-COMPILER-005, TR-P1-COMPILER-006 |
| ENT-CORE-CONFIG-PROJECTION | `CoreConfigProjection` | `contextId` | readData, readView, readRule, rejectMutation | 与来源 EngineContext 同生命周期，不单独注册事实 | TR-P1-COMPILER-006 |

### 5.2 值对象

| ID | 对象 | 相等性/身份 | 关键属性 | 追踪 |
|---|---|---|---|---|
| VO-DOCUMENT-SOURCE | `DocumentSource` | `sourceId` | sourceId, uri, format, allowedRoot | TR-P1-COMPILER-001, TR-P1-COMPILER-002 |
| VO-CANONICAL-DOCUMENT-NODE | `CanonicalDocumentNode` | `structuralValue` | nodeType, name, attributes, children, sourceRef | TR-P1-COMPILER-002 |
| VO-SOURCE-REF | `SourceRef` | `sourceId+line+column+nodePath` | sourceId, line, column, nodePath | TR-P1-COMPILER-001, TR-P1-COMPILER-002, TR-P1-COMPILER-004 |
| VO-TYPED-KEY | `TypedKey` | `keyType+namespace+localName` | keyType, namespace, localName | TR-P1-COMPILER-003 |
| VO-INFORMATION-KEY | `InformationKey` | `systemKey+localInformationName` | systemKey, localName | TR-P1-COMPILER-003, TR-P1-COMPILER-008 |
| VO-MODEL-ACCESS-BINDING | `ModelAccessBinding` | `systemKey+sourcePath+operation+targetView+selector` | systemKey, operation, sourcePath, targetView, selector, resolvedTarget | TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| VO-DEFERRED-DEFINITION | `DeferredDefinition` | `definitionKey+requiredStage+reason` | definitionKey, requiredStage, reason, sourceRef, resolvedKeys | TR-P1-COMPILER-004, TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| VO-DIAGNOSTIC | `Diagnostic` | `sourceRef+code+definitionKey+pass` | code, severity, sourceRef, definitionKey, relatedRefs, pass, recoveryHint | TR-P1-COMPILER-001, TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| VO-DIGEST-PAIR | `DigestPair` | `sourceDigest+semanticDigest` | sourceDigest, semanticDigest | TR-P1-COMPILER-005 |
| VO-COMPILATION-RESULT | `CompilationResult` | `sessionId+terminalState` | state, compiledModelSet, engineContext, diagnostics, digests | TR-P1-COMPILER-005 |

## 6. 强类型 Key 与定义映射

| 配置结构 | RawDefinition | TypedKey | P1 编译结果 | 后续边界 |
|---|---|---|---|---|
| `orm-datasource` | RawDataSourceDefinition | DataSourceKey | CompiledDataSourceDefinition | P7 datasource/session |
| `orm-connection` | RawConnectionDefinition | ConnectionKey | CompiledConnectionDefinition | P7 connection/transaction |
| `data` | RawDataDefinition | DataKey | CompiledDataDefinition | P6 query/SQL |
| `view` | RawViewDefinition | ViewKey | CompiledViewDefinition | P2 ownership、P6 query |
| `system` | RawSystemDefinition | SystemKey | LinkedSystemDefinition | P2 access/permission |
| `rule-view-info` | RawRuleViewDefinition | RuleViewKey(SystemKey,name) | LinkedRuleViewDefinition | P2 ownership、P4 execution |
| `business-config` | RawBusinessScopeDefinition | BusinessScopeKey | CompiledBusinessScopeDefinition | P4/P5 编排；不拥有 Information |
| `information` | RawInformationDefinition | InformationKey(SystemKey,localName) | LinkedInformationDefinition | P3 DAG/evaluation |
| `model-access` | RawModelAccessDefinition | SystemKey + operation + sourcePath | ModelAccessBinding | P2 permission、P6 access |
| `directory` | RawDirectoryDefinition | DirectoryKey(BusinessScopeKey,name) | LinkedDirectoryDefinition | P5 state machine |
| `action` | RawActionDefinition | ActionKey(BusinessScopeKey,name) | LinkedActionDefinition | P4 execution |
| `produce` | RawProduceDefinition | ProduceKey(BusinessScopeKey,name) | LinkedProduceDefinition | P4 execution |

## 7. Information 所有权与 common System

### 7.1 普通 System

- Information 必须位于所属 `<system>/<information-info>`；`InformationKey=(SystemKey, localName)`。
- `view-ref` 和 `rule-ref` 只能引用该 System 已声明的 View/RuleView。
- 普通 System 的 expression 只能组合本 System 的 InformationKey。

### 7.2 common System

- `common` 只允许 expression Information；不得拥有 Data、View、RuleView、ModelAccess、rule-ref、view-ref、rule-data 或 change-data。
- `common.paySuccess` 依赖 `payment.success` 与 `order.paySuccessStatus`。
- `common.payError` 依赖 `payment.error` 与 `order.payErrorStatus`。
- P1 只解析并登记 system-qualified `InformationKey` 依赖；表达式 DAG、循环检测与求值属于 P3 Deferred。

### 7.3 BusinessScope

- BusinessScope 只为 Directory/Action/Produce 提供跨 System 编排命名空间。
- BusinessScope 消费 `{system}.{information}`，但不定义或拥有 Information。

## 8. ModelAccessBinding

`ModelAccessBinding` 明确分离：

1. `read|write@path`：共享模型的源路径 `SharedModelPath`；
2. `ref@view`：当前 System 已声明的目标 `ViewKey`；
3. `ref@property`：目标 View 选择器；
4. 解析顺序：先对 `target-main` 做区分大小写完整匹配；未命中才在同一 View 属性树中逐段精确查找 property path；
5. 多个 ref 独立解析；完全重复、重叠写、多候选、路径中间段非复合或两步均不命中均产生 ERROR；
6. 禁止 `root-property`、模糊匹配、全局/跨 View 搜索和静默降级。

示例：`OrderInfo.user` 的 `read@path="user"` 保持源路径；`ref view="UserInfo" property="user"` 首先命中 `UserInfo.target-main="user"`，绑定到 View 根目标。

## 9. DeferredDefinition 阶段边界

| requiredStage | P1 已完成 | 后续阶段拥有 | 禁止提前执行 |
|---|---|---|---|
| P2 | System/View/RuleView/ModelAccess Key 与结构引用 | 访问权限、完整 System 语义、RuleView 归属执行约束 | P1 不判定运行权限 |
| P3 | InformationKey、expression 引用 Key、SourceRef | DAG、循环检测、求值、物化与失效 | P1 不求值 expression |
| P4 | Action/Produce 结构与 TypedKey | Action/Produce 执行 | P1 不触发行为 |
| P5 | Directory 结构与 Information 引用 | 状态机、分类、back | P1 不进行目录流转 |
| P6 | Data/View/ModelAccess 结构 | QueryPlan、SQL、方言 | P1 不生成 SQL |
| P7 | DataSource/Connection 结构 | Session、事务、资源生命周期 | P1 不打开连接 |
| P8 | Canonical/Raw、digest 与前端契约 | XML/YAML 完整对等、性能、安全和发布验收 | P1 不声明最终发布完成 |

每个 DeferredDefinition 必须包含 `definitionKey`、`requiredStage`、`reason`、`SourceRef` 和已解析 `resolvedKeys`；缺一项即 `ERR-MIX-DEFERRED-INCOMPLETE`。

## 10. 不变量

| ID | 可判定陈述 | 触发点 | 失败语义 | 追踪 |
|---|---|---|---|---|
| INV-COMPILER-001 | 编译器不得硬编码 dec-demo 或 mix fixture 路径；根 SourceProvider 必须由调用方注入。 | 开始源发现 | `ERR-MIX-SOURCE-POLICY` | TR-P1-COMPILER-001 |
| INV-COMPILER-002 | 目录发现按规范化 sourceId 稳定排序，文件系统枚举顺序不得改变源图或 semanticDigest。 | 构建 MixSourceGraph | `ERR-MIX-SOURCE-DUPLICATE-ID 或确定性验证失败` | TR-P1-COMPILER-001, TR-P1-COMPILER-005 |
| INV-COMPILER-003 | 每个 TypedKey 在同一 CompilationSession 中唯一；注册完成后才解析跨文件前向引用，重复定义不得覆盖。 | 符号注册与引用解析 | `ERR-MIX-SYMBOL-DUPLICATE 或 ERR-MIX-REF-UNKNOWN` | TR-P1-COMPILER-003 |
| INV-COMPILER-004 | InformationKey 必须由所属 SystemKey 与局部名称组成；BusinessScope 不得拥有 Information。 | 构建 RawInformationDefinition | `ERR-MIX-INFORMATION-OWNER` | TR-P1-COMPILER-003, TR-P1-COMPILER-008 |
| INV-COMPILER-005 | 普通 System expression 只能引用本 System InformationKey；跨 System expression 只能由 common 拥有，且 common 只能声明 expression Information。 | 解析 Information expression | `ERR-MIX-INFORMATION-CROSS-SYSTEM 或 ERR-MIX-COMMON-MEMBER` | TR-P1-COMPILER-004, TR-P1-COMPILER-008 |
| INV-COMPILER-006 | Information view-ref、rule-ref 与 ModelAccess ref@view 只能指向当前 System 已声明的 View 或 RuleView。 | 解析 System 内引用 | `ERR-MIX-REF-VIEW-NOT-DECLARED 或 ERR-MIX-REF-RULE-SYSTEM-MISMATCH` | TR-P1-COMPILER-003, TR-P1-COMPILER-008 |
| INV-COMPILER-007 | ModelAccess sourcePath 与 target selector 是不同字段；selector 先精确匹配 target-main，未匹配才逐段精确解析同一 View property path；禁止模糊、跨 View 或静默降级。 | 解析 ModelAccessBinding | `ERR-MIX-MODEL-ACCESS-NOT-FOUND、AMBIGUOUS 或 NON-COMPOSITE` | TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| INV-COMPILER-008 | 每个后续语义必须登记包含 requiredStage、reason、SourceRef 和已解析 TypedKey 的 DeferredDefinition；P1 不执行 P2～P8 运行语义。 | Deferred 分类 | `ERR-MIX-DEFERRED-INCOMPLETE` | TR-P1-COMPILER-004, TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| INV-COMPILER-009 | 任一 ERROR、ContextPublisher 拒绝 expectedCurrent 条件或发布异常都使 CompilationSession 进入 FAILED；compiler 不得返回未发布的成功结果，CompiledModelSet 与 EngineContext 不得部分暴露，调用方旧 Context 不变。 | 任一 Compiler Pass 结束或 compiler 原子发布时 | `ERR-MIX-PUBLICATION-BLOCKED` | TR-P1-COMPILER-003, TR-P1-COMPILER-005, TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| INV-COMPILER-010 | 同义输入、稳定选项和同一编译器版本必须产生相同 semanticDigest；sourceDigest 单独反映原文。 | 摘要计算 | `ERR-MIX-DIGEST-NONDETERMINISTIC` | TR-P1-COMPILER-005 |
| INV-COMPILER-011 | 已发布 Registry、CompiledModelSet 与 EngineContext 永久不可变，不存在全局 current Context，不同 Context 不共享可变状态。 | 发布和读取 | `ERR-MIX-CONTEXT-MUTATION` | TR-P1-COMPILER-005, TR-P1-COMPILER-006 |
| INV-COMPILER-012 | XML 禁止外部实体和网络解析，YAML 禁止任意 Java 类型，Source 不得逃逸允许根路径或使用未授权 scheme。 | Source 读取和 frontend 解析 | `ERR-MIX-SOURCE-PATH-ESCAPE、ERR-MIX-XML-UNSAFE 或 ERR-MIX-YAML-UNSAFE` | TR-P1-COMPILER-001, TR-P1-COMPILER-002 |
| INV-COMPILER-013 | dec-expand-declaration、LegacyDeclarationAdapter、复制实现或第二运行时不得存在于仓库、Reactor、依赖、服务、反射字符串或发布 artifact。 | 退役扫描和构建 | `ERR-MIX-RETIREMENT-RESIDUE` | TR-P1-COMPILER-007 |
| INV-COMPILER-014 | CoreConfigProjection 只能从当前 CompiledModelSet 派生 Data/View/Rule 只读视图，写入必须拒绝，不能拥有第二 Registry。 | 旧核心读取或写入尝试 | `ERR-MIX-PROJECTION-WRITE` | TR-P1-COMPILER-006 |
| INV-COMPILER-015 | RuleViewKey 预留 SystemKey 命名空间，RuleView.system、来源 System 与 view-ref 所属 System 必须一致。 | 注册和解析 RuleView | `ERR-MIX-REF-RULE-SYSTEM-MISMATCH` | TR-P1-COMPILER-003, TR-P1-COMPILER-008 |

## 11. CompilationSession 状态机

| 转换 | 当前状态 | 命令 | 下一状态 | 前置条件 | 副作用 | 失败 |
|---|---|---|---|---|---|---|
| TRANS-COMP-001 | CREATED | `discoverSources` | SOURCES_DISCOVERED | root DocumentSource 与允许根存在；SourceProvider 已注入 | 构建有类型 MixSourceGraph | 任何 Source ERROR 转 FAILED |
| TRANS-COMP-002 | SOURCES_DISCOVERED | `parseCanonicalDocuments` | PARSED | 所有 Source format 有已注册 frontend | 构建 CanonicalDocumentNode | 格式或安全 ERROR 转 FAILED |
| TRANS-COMP-003 | PARSED | `buildRawDefinitions` | RAW_BUILT | Canonical 节点有效 | 构建 RawDefinitionSet 并保留 SourceRef | 结构 ERROR 转 FAILED |
| TRANS-COMP-004 | RAW_BUILT | `validateStructure` | STRUCTURALLY_VALIDATED | 所有必需字段可判定 | 产生稳定 Diagnostic | ERROR 转 FAILED |
| TRANS-COMP-005 | STRUCTURALLY_VALIDATED | `registerSymbols` | SYMBOLS_REGISTERED | TypedKey 可构造 | 注册全部符号后封存 | 重复 Key 转 FAILED |
| TRANS-COMP-006 | SYMBOLS_REGISTERED | `resolveReferences` | REFERENCES_RESOLVED | 注册阶段已封存 | 解析前向引用、Information owner、ModelAccess selector | 未知、归属或歧义 ERROR 转 FAILED |
| TRANS-COMP-007 | REFERENCES_RESOLVED | `classifyDeferred` | GRAPH_PREPARED | P1 可解析 Key 完整 | 登记 P2～P8 DeferredDefinition | Deferred 不完整转 FAILED |
| TRANS-COMP-008 | GRAPH_PREPARED | `validateSemantics` | SEMANTICALLY_VALIDATED | 无先前 ERROR | 计算稳定 Diagnostic 顺序与 digest 输入 | ERROR 转 FAILED |
| TRANS-COMP-009 | SEMANTICALLY_VALIDATED | `publishAtomically` | PUBLISHED | Diagnostic 中不存在 ERROR；所有 Registry 可防御性冻结；ContextPublisher 与 expectedCurrent 已注入 | compiler 一次性创建 CompiledModelSet 和 EngineContext，并在本次 compile 调用内按 expectedCurrent 条件原子发布 | 条件冲突、空返回或异常均转 FAILED且不替换旧 Context |

任何阶段产生 ERROR 都直接转入 `FAILED`；不存在从 `FAILED` 恢复同一 Session 的转换。修复配置后必须创建新的 CompilationSession。

## 12. 领域服务、策略与事件

### 12.1 服务

| ID | 服务 | 引入理由 | 输入 | 输出 |
|---|---|---|---|---|
| SVC-SOURCE-DISCOVERY | `SourceDiscoveryService` | 源图跨多个文档类型和间接 Rule 文件，不自然属于单一定义对象。 | DocumentSource, SourceProvider, SourcePolicy | MixSourceGraph, Diagnostic |
| SVC-CANONICALIZATION | `CanonicalizationService` | XML/YAML frontend 必须共享格式中立契约且隔离具体解析库节点。 | DocumentSource, DocumentFrontend | CanonicalDocumentNode, Diagnostic |
| SVC-RAW-BUILDER | `RawDefinitionBuilder` | 跨定义类型构建 RawDefinitionSet，需要统一 SourceRef 和未知节点策略。 | CanonicalDocumentNode | RawDefinitionSet, Diagnostic |
| SVC-SYMBOL-REGISTRATION | `SymbolRegistrationService` | 跨文件前向引用要求先完成所有 TypedKey 注册再解析。 | RawDefinitionSet | SymbolTable, Diagnostic |
| SVC-REFERENCE-RESOLUTION | `ReferenceResolutionService` | Information 所有权、RuleView 归属、ModelAccess selector 和跨文件引用需要统一确定性策略。 | RawDefinitionSet, SymbolTable, InformationOwnershipPolicy, ModelAccessSelectorPolicy | ResolvedDefinitions, ModelAccessBinding, Diagnostic |
| SVC-DEFERRED-CLASSIFICATION | `DeferredClassificationService` | P1 必须显式区分已解析结构与后续阶段运行语义。 | ResolvedDefinitions | DeferredDefinitionRegistry, Diagnostic |
| SVC-MODEL-PUBLICATION | `ModelPublicationService` | 只有 compiler 内的单一服务能保护 ERROR 不发布、防御性冻结、expectedCurrent 条件和旧 Context 不替换不变量。 | ValidatedCompilationSession, PublicationRequest, ContextPublisher | PUBLISHED 或 FAILED CompilationResult, CompiledModelSet, EngineContext |

### 12.2 策略

| ID | 策略 | 引入理由 | 输入 | 输出 |
|---|---|---|---|---|
| POL-INFORMATION-OWNERSHIP | `InformationOwnershipPolicy` | Information owner 和 common 跨 System 规则横跨 System、Information 与 BusinessScope 定义。 | RawSystemDefinition, RawInformationDefinition, SymbolTable | InformationKey, ResolvedInformationDependencies, Diagnostic |
| POL-MODEL-ACCESS-SELECTOR | `ModelAccessSelectorPolicy` | target-main 优先和 property path 回退是可变但必须确定性的业务规则。 | SharedModelPath, ViewKey, TargetSelector, CompiledViewStructure | ModelAccessBinding, Diagnostic |
| POL-DEFERRED-BOUNDARY | `DeferredBoundaryPolicy` | 防止 P1 提前执行 P2～P8 语义或静默忽略未完成定义。 | ResolvedDefinition, StageOwnershipMatrix | DeferredDefinition |
| POL-PUBLICATION | `AtomicPublicationPolicy` | 保护无 ERROR、全量冻结、expectedCurrent 条件发布和旧 Context 保留的一致性边界。 | CompilationSession, DiagnosticSet, PublicationRequest, ContextPublisher | PUBLISHED 或 FAILED CompilationResult |
| POL-SOURCE-SECURITY | `SourceSecurityPolicy` | 路径、URI、XML 和 YAML 安全需要统一前置拒绝。 | DocumentSource, FrontendSecurityOptions | AllowedSource|Diagnostic |
| POL-RETIREMENT | `DeclarationRetirementPolicy` | 不可逆替代必须保证临时模块及 Adapter 无残留。 | RepositoryTree, Reactor, DependencyTree, ArtifactList | RetirementGateResult |

### 12.3 事件

| ID | 事件 | 语义 | 输入 | 输出 |
|---|---|---|---|---|
| EVT-SOURCES-DISCOVERED | `SourcesDiscovered` | 记录源图事实供后续 Pass 使用，不表示业务运行事件。 | MixSourceGraph | source count, typed edge count |
| EVT-COMPILATION-FAILED | `CompilationFailed` | 对外表达未发布且旧 Context 不变的已发生编译事实。 | CompilationSessionId, DiagnosticSet | FAILED CompilationResult |
| EVT-MODEL-PUBLISHED | `ModelPublished` | 对外表达 compiler 已在同一次 compile 调用内使新的不可变 Context 原子可见。 | CompiledModelSet, DigestPair, expectedCurrent | EngineContextId |

## 13. 业务错误与 Diagnostic

Diagnostic 稳定排序键为 `sourceId → line → column → code → definitionKey → pass`。所有 ERROR 都阻断发布；`recoveryHint` 只说明修复方向，不执行静默修复。

| 错误 ID | Diagnostic code | 触发条件 | 对外语义 | 可重试 | 状态改变 | 追踪 |
|---|---|---|---|---|---|---|
| ERR-MIX-SOURCE-POLICY | `MIX-SOURCE-POLICY` | 编译器依赖硬编码 demo/fixture 路径或未注入根 SourceProvider | 源发现策略无效 | 否 | 否 | TR-P1-COMPILER-001 |
| ERR-MIX-SOURCE-NOT-FOUND | `MIX-SOURCE-NOT-FOUND` | 显式引用的配置 Source 不存在 | 配置源缺失 | 是 | 否 | TR-P1-COMPILER-001 |
| ERR-MIX-SOURCE-PATH-ESCAPE | `MIX-SOURCE-PATH-ESCAPE` | Source 逃逸允许根路径或使用未授权 URI scheme | 配置源访问被安全策略拒绝 | 否 | 否 | TR-P1-COMPILER-001 |
| ERR-MIX-SOURCE-DUPLICATE-ID | `MIX-SOURCE-DUPLICATE-ID` | 两个来源规范化后得到相同 sourceId | 源身份冲突 | 否 | 否 | TR-P1-COMPILER-001 |
| ERR-MIX-XML-UNSAFE | `MIX-FRONTEND-XML-UNSAFE` | XML 包含外部实体、DOCTYPE 网络解析或不安全特性 | XML 输入被安全策略拒绝 | 否 | 否 | TR-P1-COMPILER-002 |
| ERR-MIX-YAML-UNSAFE | `MIX-FRONTEND-YAML-UNSAFE` | YAML 请求任意 Java 类型构造或不安全 tag | YAML 输入被安全策略拒绝 | 否 | 否 | TR-P1-COMPILER-002 |
| ERR-MIX-SYMBOL-DUPLICATE | `MIX-SYMBOL-DUPLICATE` | 同一 TypedKey 被重复注册 | 定义身份重复且不得覆盖 | 否 | 否 | TR-P1-COMPILER-003 |
| ERR-MIX-REF-UNKNOWN | `MIX-REF-UNKNOWN` | 注册完成后引用仍无法解析到目标 TypedKey | 引用目标不存在 | 是 | 否 | TR-P1-COMPILER-003 |
| ERR-MIX-REF-RULE-SYSTEM-MISMATCH | `MIX-REF-RULE-SYSTEM-MISMATCH` | RuleView 来源 System、system 属性或 view-ref 所属 System 不一致 | RuleView 归属冲突 | 否 | 否 | TR-P1-COMPILER-003, TR-P1-COMPILER-008 |
| ERR-MIX-INFORMATION-OWNER | `MIX-INFORMATION-OWNER` | Information 位于 BusinessScope、System 外部或使用非 System 命名空间 | Information 所有权非法 | 否 | 否 | TR-P1-COMPILER-003, TR-P1-COMPILER-008 |
| ERR-MIX-INFORMATION-CROSS-SYSTEM | `MIX-INFORMATION-CROSS-SYSTEM` | 普通 System expression 引用其它 System InformationKey | 跨 System expression 必须迁入 common | 否 | 否 | TR-P1-COMPILER-008 |
| ERR-MIX-COMMON-MEMBER | `MIX-COMMON-MEMBER` | common 声明非 expression Information 或拥有 Data/View/RuleView/ModelAccess | common 成员超出允许边界 | 否 | 否 | TR-P1-COMPILER-008 |
| ERR-MIX-COMMON-UNQUALIFIED | `MIX-COMMON-UNQUALIFIED` | common 或 BusinessScope 使用未限定、未知 System 或未知局部名称的 Information 引用 | 跨 System Information 引用必须完整限定 | 是 | 否 | TR-P1-COMPILER-008 |
| ERR-MIX-REF-VIEW-NOT-DECLARED | `MIX-REF-VIEW-NOT-DECLARED` | Information 或 ModelAccess 指向当前 System 未声明的 View | 目标 View 不在当前 System 边界 | 是 | 否 | TR-P1-COMPILER-008, TR-P1-COMPILER-009 |
| ERR-MIX-MODEL-ACCESS-NOT-FOUND | `MIX-MODEL-ACCESS-NOT-FOUND` | selector 未命中 target-main 且同一 View property path 不存在 | ModelAccess 目标选择器无匹配 | 是 | 否 | TR-P1-COMPILER-009 |
| ERR-MIX-MODEL-ACCESS-AMBIGUOUS | `MIX-MODEL-ACCESS-AMBIGUOUS` | selector、重复 ref 或写映射产生多个候选或重叠冲突 | ModelAccess 映射不唯一 | 否 | 否 | TR-P1-COMPILER-009 |
| ERR-MIX-MODEL-ACCESS-NON-COMPOSITE | `MIX-MODEL-ACCESS-NON-COMPOSITE` | property path 中间段不是复合属性 | ModelAccess property path 无法继续遍历 | 是 | 否 | TR-P1-COMPILER-009 |
| ERR-MIX-DEFERRED-INCOMPLETE | `MIX-DEFERRED-INCOMPLETE` | 后续语义缺少 requiredStage、reason、SourceRef 或已解析 Key | Deferred 边界不完整 | 否 | 否 | TR-P1-COMPILER-004 |
| ERR-MIX-PUBLICATION-BLOCKED | `MIX-PUBLICATION-BLOCKED` | Diagnostic 含 ERROR 或 Registry 未能完整冻结 | 新模型禁止发布，旧 Context 保持 | 是 | 否 | TR-P1-COMPILER-005 |
| ERR-MIX-DIGEST-NONDETERMINISTIC | `MIX-DIGEST-NONDETERMINISTIC` | 同义输入与稳定选项产生不同 semanticDigest | 编译结果不可复现 | 否 | 否 | TR-P1-COMPILER-005 |
| ERR-MIX-CONTEXT-MUTATION | `MIX-CONTEXT-MUTATION` | 调用方尝试修改已发布 Registry 或使用全局 current Context | 不可变和实例隔离契约被破坏 | 否 | 否 | TR-P1-COMPILER-005, TR-P1-COMPILER-006 |
| ERR-MIX-PROJECTION-WRITE | `MIX-PROJECTION-WRITE` | 旧核心调用方通过 CoreConfigProjection 注册、修改或删除事实 | 兼容投影只读 | 否 | 否 | TR-P1-COMPILER-006 |
| ERR-MIX-RETIREMENT-RESIDUE | `MIX-RETIREMENT-RESIDUE` | 仓库、Reactor、依赖、服务、反射字符串或 artifact 仍包含 dec-expand-declaration 或 Adapter | 临时模块退役未完成 | 否 | 否 | TR-P1-COMPILER-007 |

## 14. 跨模块实现与生命周期

| 模块 | 业务模型责任 | 输入 | 输出 | 失败责任 |
|---|---|---|---|---|
| XML/YAML frontend | 安全解析并产生 CanonicalDocumentNode | DocumentSource | Canonical node / format Diagnostic | 格式与安全错误 |
| `dec-core-compiler` | Session、SourceGraph、Raw、TypedKey、Reference、Deferred、Diagnostic、digest，以及同一次 compile 调用内的原子发布编排 | Canonical nodes, PublicationRequest, ContextPublisher | PUBLISHED / FAILED CompilationResult | 任一 ERROR、发布冲突或异常均不暴露新 Context |
| `dec-core-context` | 不可变 EngineContext、CoreConfigProjection 与 ContextPublisher 条件发布契约/实现 | CompiledModelSet, expectedCurrent | Context / read-only projection / publication result | 不可变性、实例隔离与条件替换 |
| `dec-core-starter` | 注入 SourceProvider、frontend、compiler 与 ContextPublisher，并返回 compiler 结果 | root Source + options + expectedCurrent | CompilationResult | 组装失败且旧 Context 不变；禁止二次发布 |
| `dec-demo` | 提供真实 mix fixture 和契约证据 | fixture | tests/evidence | fixture 漂移；不得成为核心依赖 |

`dec-expand-declaration` 的处置为整体删除，无迁移、无 Adapter、无在途运行时双轨；历史只保留在 Git 与 Evidence，恢复方式只有 Git revert。

## 15. 追踪映射

| TR | 业务模型稳定 ID |
|---|---|
| TR-P1-COMPILER-001 | `SCN-COMPILER-SUCCESS`, `SCN-SECURE-FRONTEND`, `ENT-MIX-SOURCE-GRAPH`, `INV-COMPILER-001`, `INV-COMPILER-002`, `INV-COMPILER-012`, `SVC-SOURCE-DISCOVERY` |
| TR-P1-COMPILER-002 | `SCN-COMPILER-SUCCESS`, `SCN-SECURE-FRONTEND`, `ENT-RAW-DEFINITION-SET`, `VO-CANONICAL-DOCUMENT-NODE`, `INV-COMPILER-012`, `SVC-CANONICALIZATION`, `SVC-RAW-BUILDER` |
| TR-P1-COMPILER-003 | `SCN-COMPILER-INVALID-REFERENCE`, `ENT-SYMBOL-TABLE`, `VO-TYPED-KEY`, `VO-INFORMATION-KEY`, `INV-COMPILER-003`, `INV-COMPILER-004`, `INV-COMPILER-006`, `INV-COMPILER-015`, `SVC-SYMBOL-REGISTRATION`, `SVC-REFERENCE-RESOLUTION` |
| TR-P1-COMPILER-004 | `SCN-COMMON-EXPRESSION`, `ENT-DEFERRED-REGISTRY`, `VO-DEFERRED-DEFINITION`, `INV-COMPILER-005`, `INV-COMPILER-008`, `SVC-DEFERRED-CLASSIFICATION`, `POL-DEFERRED-BOUNDARY` |
| TR-P1-COMPILER-005 | `SCN-COMPILER-SUCCESS`, `SCN-COMPILER-INVALID-REFERENCE`, `SCN-MULTI-CONTEXT-ISOLATION`, `AGG-PUBLISHED-CONTEXT`, `ENT-COMPILED-MODEL-SET`, `ENT-ENGINE-CONTEXT`, `VO-DIGEST-PAIR`, `INV-COMPILER-009`, `INV-COMPILER-010`, `INV-COMPILER-011`, `SVC-MODEL-PUBLICATION`, `POL-PUBLICATION` |
| TR-P1-COMPILER-006 | `SCN-MULTI-CONTEXT-ISOLATION`, `ENT-CORE-CONFIG-PROJECTION`, `AGG-PUBLISHED-CONTEXT`, `INV-COMPILER-014` |
| TR-P1-COMPILER-007 | `SCN-RETIRE-DECLARATION`, `INV-COMPILER-013`, `POL-RETIREMENT`, `ERR-MIX-RETIREMENT-RESIDUE` |
| TR-P1-COMPILER-008 | `SCN-COMMON-EXPRESSION`, `SCN-MODEL-ACCESS-TARGET-MAIN`, `VO-INFORMATION-KEY`, `VO-MODEL-ACCESS-BINDING`, `INV-COMPILER-004`, `INV-COMPILER-005`, `INV-COMPILER-006`, `POL-INFORMATION-OWNERSHIP`, `SVC-REFERENCE-RESOLUTION` |
| TR-P1-COMPILER-009 | `SCN-MODEL-ACCESS-TARGET-MAIN`, `SCN-MODEL-ACCESS-PROPERTY-FALLBACK`, `VO-MODEL-ACCESS-BINDING`, `INV-COMPILER-007`, `POL-MODEL-ACCESS-SELECTOR`, `ERR-MIX-MODEL-ACCESS-NOT-FOUND`, `ERR-MIX-MODEL-ACCESS-AMBIGUOUS`, `ERR-MIX-MODEL-ACCESS-NON-COMPOSITE` |

## 16. Revision 变更集

| 项目 | 内容 |
|---|---|
| Change Set | `CHG-V_1.0-DEC_COMPILER-BM-005` |
| Base Revision | `BM-R04@1b19a0ba26b6` |
| Result Revision | `BM-R05@4ecb1f8c09f4` |
| Input Revision | `REQAN-R05@7de35e8dc15b` |
| 变更原因 | REQAN-R05 将原子发布责任统一归属 dec-core-compiler，并把 ContextPublisher 定义为注入契约；业务模型需消除 Starter 发布歧义 |
| 受影响追踪 | `TR-P1-COMPILER-001`～`TR-P1-COMPILER-009` |
| 下游处置 | design/test_design/implementation_plan 继续保持 STALE，必须消费本 Revision |

### 16.1 变更操作

- 重建术语、场景、实体、值对象、两个聚合和 15 条不变量；
- 新增 `InformationKey(SystemKey,localName)`、`ModelAccessBinding`、`DeferredDefinition` 与稳定 Diagnostic 模型；
- 明确 common System 与 BusinessScope 的边界；
- 明确 CompilationSession 状态机及 ERROR 全有或全无发布；
- 明确 compiler 在同一次 compile 调用内完成 expectedCurrent 条件发布，Starter 只注入依赖并返回结果；
- 将 9 条 TR 映射到稳定模型 ID；
- 保留 BM-R01/BM-R02 草案及其 Review/Evidence 作为不可变历史，不覆盖历史记录。

## 17. 未决问题、风险与停止条件

- 未决 P0/P1：无。
- P1 只冻结结构和契约；具体 Java 类、API 方法签名与包结构由 design 阶段决定。
- 若实现要求 BusinessScope 拥有 Information、普通 System 组合跨 System expression、模糊 selector、全局 Context、运行时双轨或提前执行 P3～P7，必须停止并回到需求/业务模型重新确认。
