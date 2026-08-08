# COMPILER 业务模型

> Revision：`BM-R07@7d7bf504ca9d`。Base Revision：`BM-R06@6a0bce4fa0ae`。历史业务模型 `DEC_COMPILER/BM-R05@4ecb1f8c09f4` 与当前 `COMPILER` 为同一逻辑模块谱系；结构化事实源为同目录 `COMPILER_business_model.yaml`，本文提供完整、等价、面向人的可读视图。

## 1. 模块使命、边界与文档谱系

COMPILER 将调用方提供的配置根 Source 编译为不可变、可追踪、可复现的 `CompiledModelSet` 与实例级 `EngineContext`。P1 建立 Source/Canonical/Raw/TypedKey/Reference/Deferred/Diagnostic/digest/原子发布基线；P2 在同一编译模型上消费 System、RuleView 与 model-access 的 Deferred 边界，使 System 成为一等编译实体、RuleView 使用 `(SystemKey,name)` 复合身份，并把 model-access 收敛为静态 fail-closed 与运行时 Guard 的统一权限边界。P3～P8 仍按 DeferredDefinition 分阶段拥有后续语义。

- `DEC_COMPILER`：BM-R05 及更早的历史文档模块代码，继续作为 P1 设计、Evidence 和历史 Revision 的可追溯路径。
- `COMPILER`：BM-R06 起的规范化当前文档模块代码，是同一个 compiler 业务模型的后续 Revision，不代表新模块、第二 Registry、第二 Context 或第二运行时。
- 下游 Design/TestDesign/Plan/TDD/Development 必须消费当前 `COMPILER` Revision，同时允许通过 lineage 回查 `DEC_COMPILER` 历史证据；禁止把两条路径解释成并行架构。

## 2. 统一语言

| 术语 ID | 标准术语 | 定义 | 禁止混用词 | 来源 |
|---|---|---|---|---|
| TERM-COMPILATION-SESSION | CompilationSession | 一次根 Source、编译选项、构建期 Registry、Diagnostic 与结果的隔离编译会话；只在构建期间可变。 | global current compiler<br>shared mutable compiler | REQAN-R05@7de35e8dc15b |
| TERM-RAW-DEFINITION | RawDefinition | 从 CanonicalDocumentNode 构建、保留 SourceRef 且尚未执行后续阶段运行语义的格式中立定义。 | DOM definition<br>runtime object | BR-P1-004<br>BR-P1-007 |
| TERM-TYPED-KEY | TypedKey | 由定义类型及其命名空间组成的强类型身份；不同定义类型不得共用字符串命名空间。 | string id<br>global name | BR-P1-005<br>BR-P1-006 |
| TERM-INFORMATION-OWNER | System-owned Information | InformationKey 由 SystemKey 与局部名称组成；BusinessScope 只消费限定引用，不拥有 Information。 | BusinessScope information<br>scope-owned information | BR-P1-006<br>BR-P1-016 |
| TERM-COMMON-SYSTEM | common System | 只拥有跨 System expression Information 的特殊 System；不拥有 Data、View、RuleView、ModelAccess 或运行时编排。 | global system<br>shared runtime | BR-P1-016<br>BR-P1-019 |
| TERM-MODEL-ACCESS-BINDING | ModelAccessBinding | 共享模型源路径与当前 System 本地 View 目标选择器之间的显式一对多绑定。 | name inference<br>implicit view mapping | BR-P1-018<br>BR-P1-020 |
| TERM-TARGET-MAIN | target-main | View 根目标选择器；ref@property 必须先对其做区分大小写的完整匹配。 | root-property<br>fuzzy root alias | BR-P1-020 |
| TERM-DEFERRED-DEFINITION | DeferredDefinition | 当前阶段尚未拥有的后续语义登记。P2 已消费 System、RuleView 与 model-access 所有权/授权语义；P3～P8 仍必须以 requiredStage、reason、SourceRef 与已解析 Key 显式 Deferred。 | ignored definition<br>partially compiled runtime | BR-P1-007<br>BR-P1-008<br>BR-P2-SYSTEM-RULEVIEW-020 |
| TERM-COMPILED-MODEL-SET | CompiledModelSet | 无 ERROR 后一次性构造并冻结的全部编译事实集合。 | mutable config registry | BR-P1-009<br>BR-P1-011 |
| TERM-ENGINE-CONTEXT | EngineContext | 实例级不可变运行时读取入口；不同 Context 不共享可变 Registry，且不存在全局 current。 | singleton context<br>global current context | BR-P1-011 |
| TERM-DIAGNOSTIC | Diagnostic | 包含稳定 code、severity、SourceRef、definitionKey、relatedRefs、pass 与 recoveryHint 的可排序失败事实。 | generic exception<br>operation failed | EX-P1-COMPILER-001<br>NFR-AUDIT |
| TERM-SOURCE-REF | SourceRef | 规范化 sourceId、行、列和节点路径组成的不可变来源位置。 | raw file path only | BR-P1-008 |
| TERM-SEMANTIC-DIGEST | semanticDigest | 由规范化语义、稳定选项和编译器版本计算的确定性摘要，与原始 sourceDigest 分离。 | file hash only | BR-P1-010 |
| TERM-CORE-CONFIG-PROJECTION | CoreConfigProjection | 从同一 CompiledModelSet 派生的 deprecated 只读 Data/View/Rule 投影，不拥有第二 Registry。 | legacy mutable config<br>compatibility runtime | BR-P1-012 |
| TERM-SYSTEM-COMPILED-IDENTITY | System compiled identity | System 以显式 SystemKey 作为一等编译身份；身份不得由文件名、路径、RuleView 名称或加载顺序推断。 | implicit system<br>file-derived system | BR-P2-SYSTEM-RULEVIEW-001<br>BR-P2-SYSTEM-RULEVIEW-002 |
| TERM-RULEVIEW-COMPOSITE-IDENTITY | RuleView composite identity | RuleView 的唯一身份是 (SystemKey,name)；跨 System 同名合法，同一 System 重复定义非法，调用不得回退为裸 name。 | global RuleView name<br>bare RuleView lookup | BR-P2-SYSTEM-RULEVIEW-003<br>BR-P2-SYSTEM-RULEVIEW-004<br>BR-P2-SYSTEM-RULEVIEW-005 |
| TERM-MODEL-ACCESS-RULE | ModelAccessRule | 由 System、目标模型或 View、规范化 ModelPath、READ/WRITE/EXECUTE 操作与 SourceRef 共同限定的访问授权事实；未声明权限不产生隐式允许。 | default allow<br>shared write by default | BR-P2-SYSTEM-RULEVIEW-007<br>BR-P2-SYSTEM-RULEVIEW-008<br>BR-P2-SYSTEM-RULEVIEW-009 |
| TERM-MODEL-PATH | ModelPath | 对模型属性路径做精确、确定性、可复用编译后的路径身份；expression、change、query 与权限校验共享同一语义，不允许模糊或跨模型猜测。 | fuzzy model path<br>root-property fallback | BR-P2-SYSTEM-RULEVIEW-010<br>BR-P2-SYSTEM-RULEVIEW-011 |
| TERM-COMPILER-DOCUMENT-LINEAGE | Compiler document module lineage | DEC_COMPILER（BM-R05 及更早）与 COMPILER（BM-R06 起）是同一个逻辑 compiler 业务模型的连续文档谱系；模块代码规范化只改变当前事实路径，不创建第二套 compiler、Registry、Context 或运行时权威。下游必须消费当前 COMPILER Revision，并可沿 lineage 回查 DEC_COMPILER 历史 Evidence。 | parallel compiler module<br>second compiler runtime<br>independent DEC_COMPILER current model | BM-R05@4ecb1f8c09f4<br>BM-R06@6a0bce4fa0ae<br>FEATURE-DESC-3361AD2E54FC |

## 3. 场景模型

| 场景 ID | Given | When | Then | 追踪 |
|---|---|---|---|---|
| SCN-COMPILER-SUCCESS | 根 DocumentSource 位于允许根路径<br>XML/YAML frontend 与 ContextPublisher 已注入<br>所有引用最终可解析 | CompilationSession 执行发现、解析、Raw 构建、符号注册、引用解析、Deferred 分类，并以 expectedCurrent 条件发布 | 产生不可变 CompiledModelSet 与 EngineContext<br>ContextPublisher 在同一次 compile 调用内原子替换 expectedCurrent<br>sourceDigest 与 semanticDigest 均记录<br>Diagnostic 无 ERROR | TR-P1-COMPILER-001<br>TR-P1-COMPILER-002<br>TR-P1-COMPILER-003<br>TR-P1-COMPILER-004<br>TR-P1-COMPILER-005 |
| SCN-COMPILER-INVALID-REFERENCE | 输入包含未知引用、重复 TypedKey 或归属冲突<br>调用方持有旧 EngineContext | ReferenceResolutionService 或验证策略产生 ERROR | CompilationSession 进入 FAILED<br>不构造或发布新 CompiledModelSet<br>调用方旧 EngineContext 保持原值 | TR-P1-COMPILER-003<br>TR-P1-COMPILER-005 |
| SCN-COMMON-EXPRESSION | common.payError expression 引用 payment.error 与 order.payErrorStatus<br>两个引用均为已注册 InformationKey | InformationOwnershipPolicy 编译 common expression | 建立 common.payError 到两个 system-qualified InformationKey 的依赖<br>登记 P3 expression evaluation Deferred<br>common 不获得 Data/View/RuleView/ModelAccess | TR-P1-COMPILER-004<br>TR-P1-COMPILER-008 |
| SCN-MODEL-ACCESS-TARGET-MAIN | read@path=user<br>ref@view=UserInfo<br>ref@property=user<br>UserInfo.target-main=user | ModelAccessSelectorPolicy 解析目标 | 源路径保持 SharedModelPath(user)<br>selector 精确命中 target-main<br>绑定到 UserInfo 根目标且不查找其它 View | TR-P1-COMPILER-008<br>TR-P1-COMPILER-009 |
| SCN-MODEL-ACCESS-PROPERTY-FALLBACK | selector 未命中 target-main<br>目标 View 属性树存在唯一精确 property path | ModelAccessSelectorPolicy 逐段解析 property path | 建立唯一属性级绑定<br>路径段区分大小写<br>任一非复合中间段或多候选均产生 ERROR | TR-P1-COMPILER-009 |
| SCN-MULTI-CONTEXT-ISOLATION | 两个 CompilationSession 使用不同输入<br>两个结果同时存在于同一 JVM | 调用方分别读取两个 EngineContext | Registry、Diagnostic 与 digest 互不污染<br>任一 Context 不提供写入口或全局 current | TR-P1-COMPILER-005<br>TR-P1-COMPILER-006 |
| SCN-RETIRE-DECLARATION | dec-expand-declaration 曾是临时模块 | 执行仓库、Reactor、依赖、服务、反射字符串和 artifact 残留扫描 | 不存在模块或 Adapter 残留<br>必要场景只基于 mix fixture 重写<br>恢复仅使用 Git revert | TR-P1-COMPILER-007 |
| SCN-SECURE-FRONTEND | 输入包含 XML 外部实体、网络 URI、路径逃逸或 YAML 任意类型标签 | SourceDiscoveryPolicy 或 CanonicalizationService 读取输入 | 在 I/O 或 frontend 边界产生稳定 ERROR<br>不访问网络或允许根目录外资源<br>不进入 RawDefinitionSet | TR-P1-COMPILER-001<br>TR-P1-COMPILER-002 |
| SCN-P2-SYSTEM-MULTISOURCE | 多个 system-file-info 来源声明不同 System<br>加载顺序可能变化 | 编译器注册 System 定义 | 相同语义输入得到相同 SystemKey 集合与 semanticDigest<br>同一 SystemKey 重复定义产生稳定 ERROR 且不发布候选 Context | TR-P2-SYSTEM-RULEVIEW-001<br>TR-P2-SYSTEM-RULEVIEW-008<br>TR-P2-SYSTEM-RULEVIEW-009 |
| SCN-P2-RULEVIEW-ISOLATION | 两个不同 System 均声明同名 RuleView | 调用方以 system-ref + rule-ref 解析 RuleView | 分别命中各自 (SystemKey,name) 定义<br>裸 name 查找与同 System 重复定义被拒绝 | TR-P2-SYSTEM-RULEVIEW-002<br>TR-P2-SYSTEM-RULEVIEW-003 |
| SCN-P2-MODEL-ACCESS-STATIC | ModelAccessRule 的 System、目标、ModelPath 与 operation 在编译期可确定 | 编译器执行访问规则验证 | 明确声明的合法操作编译为 Allow 事实<br>未声明共享 WRITE、非法路径或越界引用产生 ERROR 并阻断发布 | TR-P2-SYSTEM-RULEVIEW-004<br>TR-P2-SYSTEM-RULEVIEW-005<br>TR-P2-SYSTEM-RULEVIEW-008 |
| SCN-P2-MODEL-ACCESS-RUNTIME | 访问在编译期合法但最终资源/路径取值只能在运行时确定 | Rule、change 或 custom action 尝试 mutation | 统一 Guard 在 mutation 与外部副作用之前判定<br>DENY 时不改变业务状态且无旁路入口 | TR-P2-SYSTEM-RULEVIEW-006<br>TR-P2-SYSTEM-RULEVIEW-007 |

## 4. 聚合与一致性边界

| 聚合 ID | 名称 | 根 | 成员 | 事务/原子边界 | 一致性 | 不变量 | 追踪 |
|---|---|---|---|---|---|---|---|
| AGG-COMPILATION-SESSION | Compilation Session Aggregate | ENT-COMPILATION-SESSION | ENT-MIX-SOURCE-GRAPH<br>ENT-RAW-DEFINITION-SET<br>ENT-SYMBOL-TABLE<br>ENT-DEFERRED-REGISTRY<br>VO-DOCUMENT-SOURCE<br>VO-CANONICAL-DOCUMENT-NODE<br>VO-SOURCE-REF<br>VO-TYPED-KEY<br>VO-INFORMATION-KEY<br>VO-MODEL-ACCESS-BINDING<br>VO-DEFERRED-DEFINITION<br>VO-DIAGNOSTIC<br>VO-DIGEST-PAIR<br>VO-COMPILATION-RESULT<br>ENT-COMPILED-SYSTEM<br>VO-RULEVIEW-KEY<br>VO-MODEL-PATH<br>VO-MODEL-ACCESS-RULE | 一次 compile 调用；构建期变化只存在于该 Session，成功时由 compiler 调用注入的 ContextPublisher 条件发布后才进入 PUBLISHED | 强一致、全有或全无；不同 Session 不共享可变对象 | INV-COMPILER-001<br>INV-COMPILER-002<br>INV-COMPILER-003<br>INV-COMPILER-004<br>INV-COMPILER-005<br>INV-COMPILER-006<br>INV-COMPILER-007<br>INV-COMPILER-008<br>INV-COMPILER-009<br>INV-COMPILER-010<br>INV-COMPILER-011<br>INV-COMPILER-012<br>INV-COMPILER-013<br>INV-COMPILER-016<br>INV-COMPILER-017<br>INV-COMPILER-018<br>INV-COMPILER-019<br>INV-COMPILER-020<br>INV-COMPILER-021<br>INV-COMPILER-022 | TR-P1-COMPILER-001<br>TR-P1-COMPILER-002<br>TR-P1-COMPILER-003<br>TR-P1-COMPILER-004<br>TR-P1-COMPILER-005<br>TR-P1-COMPILER-007<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009<br>TR-P2-SYSTEM-RULEVIEW-001<br>TR-P2-SYSTEM-RULEVIEW-002<br>TR-P2-SYSTEM-RULEVIEW-003<br>TR-P2-SYSTEM-RULEVIEW-004<br>TR-P2-SYSTEM-RULEVIEW-005<br>TR-P2-SYSTEM-RULEVIEW-006<br>TR-P2-SYSTEM-RULEVIEW-007<br>TR-P2-SYSTEM-RULEVIEW-008<br>TR-P2-SYSTEM-RULEVIEW-009<br>TR-P2-SYSTEM-RULEVIEW-010 |
| AGG-PUBLISHED-CONTEXT | Published Context Aggregate | ENT-ENGINE-CONTEXT | ENT-COMPILED-MODEL-SET<br>ENT-CORE-CONFIG-PROJECTION<br>VO-DIGEST-PAIR | 只有 PUBLISHED 终态才能一次性创建并交给调用方；失败不替换旧 Context | 发布后永久不可变；Projection 与 CompiledModelSet 同源 | INV-COMPILER-009<br>INV-COMPILER-010<br>INV-COMPILER-011<br>INV-COMPILER-014 | TR-P1-COMPILER-005<br>TR-P1-COMPILER-006 |
| AGG-SYSTEM-COMPILED-CONFIG | System Compiled Configuration Aggregate | ENT-COMPILED-SYSTEM | VO-RULEVIEW-KEY<br>VO-MODEL-PATH<br>VO-MODEL-ACCESS-RULE<br>VO-MODEL-ACCESS-BINDING | 单个 CompilationSession 内完成 System 成员注册、引用解析、路径与访问规则校验；只有整个编译候选无 ERROR 时才随完整 Context 原子发布。 | 强一致；同一 System 内身份和权限不变量不得以部分成功形式暴露。 | INV-COMPILER-016<br>INV-COMPILER-017<br>INV-COMPILER-018<br>INV-COMPILER-019<br>INV-COMPILER-020<br>INV-COMPILER-021<br>INV-COMPILER-022 | TR-P2-SYSTEM-RULEVIEW-001<br>TR-P2-SYSTEM-RULEVIEW-002<br>TR-P2-SYSTEM-RULEVIEW-003<br>TR-P2-SYSTEM-RULEVIEW-004<br>TR-P2-SYSTEM-RULEVIEW-005<br>TR-P2-SYSTEM-RULEVIEW-006<br>TR-P2-SYSTEM-RULEVIEW-007<br>TR-P2-SYSTEM-RULEVIEW-008<br>TR-P2-SYSTEM-RULEVIEW-009<br>TR-P2-SYSTEM-RULEVIEW-010 |

### 4.1 Compilation Session Aggregate

- 聚合根：`ENT-COMPILATION-SESSION`。
- 原子边界：一次 compile 调用；构建期变化只存在于该 Session，成功时由 compiler 调用注入的 ContextPublisher 条件发布后才进入 PUBLISHED。
- 一致性：强一致、全有或全无；不同 Session 不共享可变对象。
- 成员：`ENT-MIX-SOURCE-GRAPH`, `ENT-RAW-DEFINITION-SET`, `ENT-SYMBOL-TABLE`, `ENT-DEFERRED-REGISTRY`, `VO-DOCUMENT-SOURCE`, `VO-CANONICAL-DOCUMENT-NODE`, `VO-SOURCE-REF`, `VO-TYPED-KEY`, `VO-INFORMATION-KEY`, `VO-MODEL-ACCESS-BINDING`, `VO-DEFERRED-DEFINITION`, `VO-DIAGNOSTIC`, `VO-DIGEST-PAIR`, `VO-COMPILATION-RESULT`, `ENT-COMPILED-SYSTEM`, `VO-RULEVIEW-KEY`, `VO-MODEL-PATH`, `VO-MODEL-ACCESS-RULE`。
- 必须共同保护的不变量：`INV-COMPILER-001`, `INV-COMPILER-002`, `INV-COMPILER-003`, `INV-COMPILER-004`, `INV-COMPILER-005`, `INV-COMPILER-006`, `INV-COMPILER-007`, `INV-COMPILER-008`, `INV-COMPILER-009`, `INV-COMPILER-010`, `INV-COMPILER-011`, `INV-COMPILER-012`, `INV-COMPILER-013`, `INV-COMPILER-016`, `INV-COMPILER-017`, `INV-COMPILER-018`, `INV-COMPILER-019`, `INV-COMPILER-020`, `INV-COMPILER-021`, `INV-COMPILER-022`。

### 4.2 Published Context Aggregate

- 聚合根：`ENT-ENGINE-CONTEXT`。
- 原子边界：只有 PUBLISHED 终态才能一次性创建并交给调用方；失败不替换旧 Context。
- 一致性：发布后永久不可变；Projection 与 CompiledModelSet 同源。
- 成员：`ENT-COMPILED-MODEL-SET`, `ENT-CORE-CONFIG-PROJECTION`, `VO-DIGEST-PAIR`。
- 必须共同保护的不变量：`INV-COMPILER-009`, `INV-COMPILER-010`, `INV-COMPILER-011`, `INV-COMPILER-014`。

### 4.3 System Compiled Configuration Aggregate

- 聚合根：`ENT-COMPILED-SYSTEM`。
- 原子边界：单个 CompilationSession 内完成 System 成员注册、引用解析、路径与访问规则校验；只有整个编译候选无 ERROR 时才随完整 Context 原子发布。。
- 一致性：强一致；同一 System 内身份和权限不变量不得以部分成功形式暴露。。
- 成员：`VO-RULEVIEW-KEY`, `VO-MODEL-PATH`, `VO-MODEL-ACCESS-RULE`, `VO-MODEL-ACCESS-BINDING`。
- 必须共同保护的不变量：`INV-COMPILER-016`, `INV-COMPILER-017`, `INV-COMPILER-018`, `INV-COMPILER-019`, `INV-COMPILER-020`, `INV-COMPILER-021`, `INV-COMPILER-022`。

## 5. 实体和值对象

### 5.1 实体

| ID | 对象 | 身份 | 关键属性 | 关键行为 | 生命周期/Owner | 追踪 |
|---|---|---|---|---|---|---|
| ENT-COMPILATION-SESSION | CompilationSession | sessionId | sessionId:CompilationSessionId(必填)<br>rootSource:DocumentSource(必填)<br>options:CompilationOptions(必填)<br>state:CompilationState(必填)<br>diagnostics:List<Diagnostic>(必填) | discoverSources<br>parseCanonicalDocuments<br>buildRawDefinitions<br>registerSymbols<br>resolveReferences<br>classifyDeferredDefinitions<br>publishOrFail | CREATED 到 PUBLISHED 或 FAILED；终态后不得继续写入 / dec-core-compiler | TR-P1-COMPILER-001<br>TR-P1-COMPILER-002<br>TR-P1-COMPILER-003<br>TR-P1-COMPILER-004<br>TR-P1-COMPILER-005<br>TR-P1-COMPILER-006<br>TR-P1-COMPILER-007<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009 |
| ENT-MIX-SOURCE-GRAPH | MixSourceGraph | rootSourceId | rootSourceId:SourceId(必填)<br>sources:SortedMap<SourceId,DocumentSource>(必填)<br>edges:List<TypedSourceEdge>(必填) | addUniqueSource<br>addTypedEdge<br>iterateInCanonicalOrder | dec-core-compiler | TR-P1-COMPILER-001 |
| ENT-RAW-DEFINITION-SET | RawDefinitionSet | compilationSessionId | definitions:Map<TypedKey,RawDefinition>(必填)<br>sourceRefs:Map<TypedKey,SourceRef>(必填) | addWithoutOverwrite<br>readByTypedKey<br>freezeForPasses |  | TR-P1-COMPILER-002<br>TR-P1-COMPILER-003<br>TR-P1-COMPILER-008 |
| ENT-SYMBOL-TABLE | SymbolTable | compilationSessionId | symbols:Map<TypedKey,Symbol>(必填)<br>registrationComplete:boolean(必填) | registerUnique<br>sealRegistration<br>resolveForwardReference |  | TR-P1-COMPILER-003<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009 |
| ENT-DEFERRED-REGISTRY | DeferredDefinitionRegistry | compilationSessionId | entries:Map<TypedKey,List<DeferredDefinition>>(必填)<br>sealed:boolean(必填) | registerCompleteDeferred<br>rejectMissingBoundary<br>freeze |  | TR-P1-COMPILER-004<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009 |
| ENT-COMPILED-MODEL-SET | CompiledModelSet | semanticDigest | registries:ImmutableTypedRegistries(必填)<br>deferredRegistry:ImmutableDeferredRegistry(必填)<br>diagnostics:ImmutableList<Diagnostic>(必填)<br>semanticDigest:Digest(必填) | readTypedRegistry<br>readDeferredDefinitions<br>readDiagnostics | 无 ERROR 时一次构造并永久不可变 | TR-P1-COMPILER-002<br>TR-P1-COMPILER-003<br>TR-P1-COMPILER-004<br>TR-P1-COMPILER-005<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009 |
| ENT-ENGINE-CONTEXT | EngineContext | contextId | contextId:EngineContextId(必填)<br>compiledModelSet:CompiledModelSet(必填)<br>compilerVersion:String(必填) | readCompiledModels<br>createCoreConfigProjection | 由成功 CompilationResult 创建；无全局 current；不提供写入 API | TR-P1-COMPILER-005<br>TR-P1-COMPILER-006 |
| ENT-CORE-CONFIG-PROJECTION | CoreConfigProjection | contextId | contextId:EngineContextId(必填)<br>dataView:ReadOnlyMap(必填)<br>viewView:ReadOnlyMap(必填)<br>ruleView:ReadOnlyMap(必填) | readData<br>readView<br>readRule<br>rejectMutation | 与来源 EngineContext 同生命周期，不单独注册事实 | TR-P1-COMPILER-006 |
| ENT-COMPILED-SYSTEM | CompiledSystem | systemKey | systemKey:SystemKey(必填)<br>sourceRefs:List<SourceRef>(必填)<br>dataRefs:Set<DataKey>(必填)<br>viewRefs:Set<ViewKey>(必填)<br>ruleViewKeys:Set<RuleViewKey>(必填)<br>modelAccessRules:Set<ModelAccessRule>(必填) | publishImmutable<br>rejectDuplicateSystemKey<br>exposeOwnerQualifiedMembers |  | TR-P2-SYSTEM-RULEVIEW-001<br>TR-P2-SYSTEM-RULEVIEW-002<br>TR-P2-SYSTEM-RULEVIEW-004<br>TR-P2-SYSTEM-RULEVIEW-008 |

### 5.2 值对象

| ID | 对象 | 相等性/身份 | 关键属性 | 行为 | 追踪 |
|---|---|---|---|---|---|
| VO-DOCUMENT-SOURCE | DocumentSource | sourceId | sourceId:String(必填)<br>uri:URI(必填)<br>format:DocumentFormat(必填)<br>allowedRoot:URI(必填) | normalizeId<br>openOnce | TR-P1-COMPILER-001<br>TR-P1-COMPILER-002 |
| VO-CANONICAL-DOCUMENT-NODE | CanonicalDocumentNode | structuralValue | nodeType:NodeType(必填)<br>name:String(必填)<br>attributes:SortedMap<String,String>(必填)<br>children:List<CanonicalDocumentNode>(必填)<br>sourceRef:SourceRef(必填) | iterateDeterministically | TR-P1-COMPILER-002 |
| VO-SOURCE-REF | SourceRef | sourceId+line+column+nodePath | sourceId:String(必填)<br>line:int(必填)<br>column:int(必填)<br>nodePath:String(必填) | compareForStableOrder | TR-P1-COMPILER-001<br>TR-P1-COMPILER-002<br>TR-P1-COMPILER-004 |
| VO-TYPED-KEY | TypedKey | keyType+namespace+localName | keyType:DefinitionType(必填)<br>namespace:String(必填)<br>localName:String(必填) | compare<br>renderQualifiedName | TR-P1-COMPILER-003 |
| VO-INFORMATION-KEY | InformationKey | systemKey+localInformationName | systemKey:SystemKey(必填)<br>localName:String(必填) | renderSystemQualified<br>rejectBusinessScopeNamespace | TR-P1-COMPILER-003<br>TR-P1-COMPILER-008 |
| VO-MODEL-ACCESS-BINDING | ModelAccessBinding | systemKey+sourcePath+operation+targetView+selector | systemKey:SystemKey(必填)<br>operation:READ\\|WRITE\\|EXECUTE(必填)<br>sourcePath:SharedModelPath(必填)<br>targetView:ViewKey(必填)<br>selector:TargetSelector(必填)<br>resolvedTarget:ViewRoot\\|PropertyPath(必填) | resolveTargetMainFirst<br>resolveExactPropertyPathFallback<br>rejectDuplicateOrAmbiguous | TR-P1-COMPILER-008<br>TR-P1-COMPILER-009<br>TR-P2-SYSTEM-RULEVIEW-004<br>TR-P2-SYSTEM-RULEVIEW-005 |
| VO-DEFERRED-DEFINITION | DeferredDefinition | definitionKey+requiredStage+reason | definitionKey:TypedKey(必填)<br>requiredStage:P3\\|P4\\|P5\\|P6\\|P7\\|P8(必填)<br>reason:String(必填)<br>sourceRef:SourceRef(必填)<br>resolvedKeys:Set<TypedKey>(必填) | validateCompleteness | TR-P1-COMPILER-004<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009<br>TR-P2-SYSTEM-RULEVIEW-010 |
| VO-DIAGNOSTIC | Diagnostic | sourceRef+code+definitionKey+pass | code:DiagnosticCode(必填)<br>severity:INFO\\|WARNING\\|ERROR(必填)<br>sourceRef:SourceRef(必填)<br>definitionKey:TypedKey(可选)<br>relatedRefs:List<SourceRef>(可选)<br>pass:CompilerPass(必填)<br>recoveryHint:String(必填) | sortDeterministically | TR-P1-COMPILER-001<br>TR-P1-COMPILER-002<br>TR-P1-COMPILER-003<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009 |
| VO-DIGEST-PAIR | DigestPair | sourceDigest+semanticDigest | sourceDigest:SHA-256(必填)<br>semanticDigest:SHA-256(必填) | compareSemanticEquality | TR-P1-COMPILER-005 |
| VO-COMPILATION-RESULT | CompilationResult | sessionId+terminalState | state:PUBLISHED\\|FAILED(必填)<br>compiledModelSet:CompiledModelSet(可选)<br>engineContext:EngineContext(可选)<br>diagnostics:List<Diagnostic>(必填)<br>digests:DigestPair(可选) | isSuccess<br>requireNoPartialPublication | TR-P1-COMPILER-005 |
| VO-RULEVIEW-KEY | RuleViewKey | systemKey+name | systemKey:SystemKey(必填)<br>name:String(必填) | canonical<br>rejectBareNameFallback | TR-P2-SYSTEM-RULEVIEW-002<br>TR-P2-SYSTEM-RULEVIEW-003 |
| VO-MODEL-PATH | ModelPath | modelKey+segments | modelKey:TypedKey(必填)<br>segments:List<String>(必填) | compileExact<br>rejectUnknownSegment<br>rejectNonCompositeIntermediate<br>canonical | TR-P2-SYSTEM-RULEVIEW-005 |
| VO-MODEL-ACCESS-RULE | ModelAccessRule | systemKey+targetKey+modelPath+operation | systemKey:SystemKey(必填)<br>targetKey:DataKey\\|ViewKey\\|RuleViewKey(必填)<br>modelPath:ModelPath(必填)<br>operation:READ\\|WRITE\\|EXECUTE(必填)<br>sourceRef:SourceRef(必填) | compileStaticDecision<br>requireRuntimeGuardWhenDeferred<br>denyWhenUndeclared | TR-P2-SYSTEM-RULEVIEW-004<br>TR-P2-SYSTEM-RULEVIEW-005<br>TR-P2-SYSTEM-RULEVIEW-006<br>TR-P2-SYSTEM-RULEVIEW-007 |

## 6. 强类型 Key 与定义映射

| 配置结构 | Raw/身份 | 编译结果/绑定 | P2 当前语义 | 后续边界 |
|---|---|---|---|---|
| orm-datasource | RawDataSourceDefinition / DataSourceKey | CompiledDataSourceDefinition | 沿用 P1 | P7 datasource/session |
| orm-connection | RawConnectionDefinition / ConnectionKey | CompiledConnectionDefinition | 沿用 P1 | P7 connection/transaction |
| data | RawDataDefinition / DataKey | CompiledDataDefinition | System 所属关系进入 CompiledSystem | P6 query/SQL |
| view | RawViewDefinition / ViewKey | CompiledViewDefinition | System 所属与 ModelPath/ModelAccess 目标参与 P2 校验 | P6 query |
| system | RawSystemDefinition / SystemKey | CompiledSystem | 显式一等身份；多源确定性；重复 SystemKey ERROR | P2 当前拥有 |
| rule-view-info | RawRuleViewDefinition / RuleViewKey(SystemKey,name) | Resolved/Compiled RuleView | 注册、解析、调用均禁止裸 name fallback | P4 execution |
| business-config | RawBusinessScopeDefinition / BusinessScopeKey | CompiledBusinessScopeDefinition | 不拥有 Information | P4/P5 编排 |
| information | RawInformationDefinition / InformationKey(SystemKey,localName) | LinkedInformationDefinition | 保持 System owner/common 规则 | P3 DAG/evaluation |
| model-access | RawModelAccessDefinition / System+operation+sourcePath | ModelAccessBinding + ModelAccessRule + ModelPath | 静态可判定 fail-closed；真正动态的合法访问标记 RuntimeGuardRequired | P6 consumers must reuse same path/access semantics |
| directory/action/produce | BusinessScope-qualified TypedKey | Linked definition | 仅校验未来运行入口不得绕过权限边界 | P4/P5 |

## 7. Information 所有权与 common System

- 普通 System 的 `InformationKey=(SystemKey,localName)`；BusinessScope 只消费限定引用，不拥有 Information。
- 普通 System expression 只组合本 System Information；跨 System expression 仅允许由 `common` 拥有。
- `common` 只拥有 expression Information，不拥有 Data、View、RuleView、ModelAccess 或运行时编排。
- P2 不改变 P3 expression DAG/evaluation 的阶段归属，只保证 System/RuleView/model-access 的 owner-qualified 编译事实完整。

## 8. P2：System、RuleView、ModelPath 与 ModelAccess 权限边界

### 8.1 System compiled identity

- `SystemKey` 必须来自显式 System 声明，不得由文件名、目录、包名、RuleView 名称或加载顺序推断。
- 多个 `system-file-info` 来源可参与同一 CompilationSession；相同语义输入必须产生确定性结果，重复 SystemKey 必须稳定失败且不发布候选 Context。

### 8.2 RuleView composite identity

- `RuleViewKey=(SystemKey,name)` 是唯一规范身份；不同 System 可安全拥有同名 RuleView。
- 同一 System 内重复 name、未知 System、裸 name lookup 或跨 System fallback 都必须产生明确失败。

### 8.3 ModelPath

- ModelPath 以模型 TypedKey + 精确路径段组成；unknown segment、非复合中间段、模糊搜索、跨模型猜测均非法。
- expression/change/query/permission 等后续消费者必须复用同一条路径编译语义，不能各自解释。

### 8.4 ModelAccessRule 与静态/运行时屏障

- 授权事实至少由 System、目标、ModelPath、READ/WRITE/EXECUTE operation 与 SourceRef 限定；未声明权限不产生隐式 allow，共享 WRITE 默认拒绝。
- 编译期能确定的非法访问必须产生 ERROR 并阻断发布；只有确实依赖运行时资源/值的合法动态边界可进入 `RuntimeGuardRequired`。
- Runtime Guard 必须位于 mutation 和外部副作用之前；DENY 时业务状态保持不变，Rule/change/custom action 不得存在旁路。

## 9. DeferredDefinition 阶段边界

| requiredStage | 当前已完成 | 后续阶段拥有 | 禁止提前执行 |
|---|---|---|---|
| P2 | System / RuleView / model-access ownership、identity、path 与 authorization 语义已由当前 Revision 消费 | 本阶段已完成业务建模，后续由 Design/TDD/Development 实现 | 不得再把 P2 核心语义当作 ignored/deferred |
| P3 | InformationKey、expression 引用 Key、SourceRef | DAG、循环检测、求值、物化与失效 | P2 不求值 expression |
| P4 | Action/Produce 结构与 TypedKey；未来 mutation 入口必须服从 Guard | Action/Produce 执行 | P2 不触发行为 |
| P5 | Directory 结构与 Information 引用 | 状态机、分类、back | P2 不进行目录流转 |
| P6 | Data/View/ModelPath/ModelAccess 结构和权限契约 | QueryPlan、SQL、方言 | P2 不生成完整 SQL/QueryPlan |
| P7 | DataSource/Connection 结构；declaration 兼容边界只记录 | Session、事务、资源生命周期、旧 declaration 最终收敛 | P2 不提前删除 declaration 边界 |
| P8 | Canonical/Raw、digest 与前端契约 | XML/YAML 完整对等、性能、安全和发布验收 | P2 不声明最终迁移完成 |

## 10. 不变量

| ID | 可判定陈述 | 触发点 | 失败语义 | 追踪 |
|---|---|---|---|---|
| INV-COMPILER-001 | 编译器不得硬编码 dec-demo 或 mix fixture 路径；根 SourceProvider 必须由调用方注入。 | 开始源发现 | ERR-MIX-SOURCE-POLICY | TR-P1-COMPILER-001 |
| INV-COMPILER-002 | 目录发现按规范化 sourceId 稳定排序，文件系统枚举顺序不得改变源图或 semanticDigest。 | 构建 MixSourceGraph | ERR-MIX-SOURCE-DUPLICATE-ID 或确定性验证失败 | TR-P1-COMPILER-001<br>TR-P1-COMPILER-005 |
| INV-COMPILER-003 | 每个 TypedKey 在同一 CompilationSession 中唯一；注册完成后才解析跨文件前向引用，重复定义不得覆盖。 | 符号注册与引用解析 | ERR-MIX-SYMBOL-DUPLICATE 或 ERR-MIX-REF-UNKNOWN | TR-P1-COMPILER-003 |
| INV-COMPILER-004 | InformationKey 必须由所属 SystemKey 与局部名称组成；BusinessScope 不得拥有 Information。 | 构建 RawInformationDefinition | ERR-MIX-INFORMATION-OWNER | TR-P1-COMPILER-003<br>TR-P1-COMPILER-008 |
| INV-COMPILER-005 | 普通 System expression 只能引用本 System InformationKey；跨 System expression 只能由 common 拥有，且 common 只能声明 expression Information。 | 解析 Information expression | ERR-MIX-INFORMATION-CROSS-SYSTEM 或 ERR-MIX-COMMON-MEMBER | TR-P1-COMPILER-004<br>TR-P1-COMPILER-008 |
| INV-COMPILER-006 | Information view-ref、rule-ref 与 ModelAccess ref@view 只能指向当前 System 已声明的 View 或 RuleView。 | 解析 System 内引用 | ERR-MIX-REF-VIEW-NOT-DECLARED 或 ERR-MIX-REF-RULE-SYSTEM-MISMATCH | TR-P1-COMPILER-003<br>TR-P1-COMPILER-008 |
| INV-COMPILER-007 | ModelAccess sourcePath 与 target selector 是不同字段；selector 先精确匹配 target-main，未匹配才逐段精确解析同一 View property path；禁止模糊、跨 View 或静默降级。 | 解析 ModelAccessBinding | ERR-MIX-MODEL-ACCESS-NOT-FOUND、AMBIGUOUS 或 NON-COMPOSITE | TR-P1-COMPILER-008<br>TR-P1-COMPILER-009 |
| INV-COMPILER-008 | P2 已消费 System、RuleView 与 model-access 所有权/授权语义；其余 P3～P8 语义仍必须登记包含 requiredStage、reason、SourceRef 和已解析 TypedKey 的 DeferredDefinition，P2 不提前执行后续运行语义。 | Deferred 分类 | ERR-MIX-DEFERRED-INCOMPLETE | TR-P1-COMPILER-004<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009<br>TR-P2-SYSTEM-RULEVIEW-010 |
| INV-COMPILER-009 | 任一 ERROR、ContextPublisher 拒绝 expectedCurrent 条件或发布异常都使 CompilationSession 进入 FAILED；compiler 不得返回未发布的成功结果，CompiledModelSet 与 EngineContext 不得部分暴露，调用方旧 Context 不变。 | 任一 Compiler Pass 结束或 compiler 原子发布时 | ERR-MIX-PUBLICATION-BLOCKED | TR-P1-COMPILER-003<br>TR-P1-COMPILER-005<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009 |
| INV-COMPILER-010 | 同义输入、稳定选项和同一编译器版本必须产生相同 semanticDigest；sourceDigest 单独反映原文。 | 摘要计算 | ERR-MIX-DIGEST-NONDETERMINISTIC | TR-P1-COMPILER-005 |
| INV-COMPILER-011 | 已发布 Registry、CompiledModelSet 与 EngineContext 永久不可变，不存在全局 current Context，不同 Context 不共享可变状态。 | 发布和读取 | ERR-MIX-CONTEXT-MUTATION | TR-P1-COMPILER-005<br>TR-P1-COMPILER-006 |
| INV-COMPILER-012 | XML 禁止外部实体和网络解析，YAML 禁止任意 Java 类型，Source 不得逃逸允许根路径或使用未授权 scheme。 | Source 读取和 frontend 解析 | ERR-MIX-SOURCE-PATH-ESCAPE、ERR-MIX-XML-UNSAFE 或 ERR-MIX-YAML-UNSAFE | TR-P1-COMPILER-001<br>TR-P1-COMPILER-002 |
| INV-COMPILER-013 | dec-expand-declaration、LegacyDeclarationAdapter、复制实现或第二运行时不得存在于仓库、Reactor、依赖、服务、反射字符串或发布 artifact。 | 退役扫描和构建 | ERR-MIX-RETIREMENT-RESIDUE | TR-P1-COMPILER-007 |
| INV-COMPILER-014 | CoreConfigProjection 只能从当前 CompiledModelSet 派生 Data/View/Rule 只读视图，写入必须拒绝，不能拥有第二 Registry。 | 旧核心读取或写入尝试 | ERR-MIX-PROJECTION-WRITE | TR-P1-COMPILER-006 |
| INV-COMPILER-015 | RuleViewKey 预留 SystemKey 命名空间，RuleView.system、来源 System 与 view-ref 所属 System 必须一致。 | 注册和解析 RuleView | ERR-MIX-REF-RULE-SYSTEM-MISMATCH | TR-P1-COMPILER-003<br>TR-P1-COMPILER-008 |
| INV-COMPILER-016 | SystemKey 必须来自显式 System 声明且与来源顺序无关；同一 SystemKey 的重复定义必须产生 ERROR，禁止路径或文件名推断身份。 | 注册 System | ERR-MIX-SYSTEM-DUPLICATE | TR-P2-SYSTEM-RULEVIEW-001<br>TR-P2-SYSTEM-RULEVIEW-009 |
| INV-COMPILER-017 | RuleView 的身份和所有查找必须使用 (SystemKey,name)；跨 System 同名合法，同 System 重复定义与裸 name 回退均非法。 | 注册或解析 RuleView | ERR-MIX-RULEVIEW-DUPLICATE 或 ERR-MIX-RULEVIEW-UNKNOWN | TR-P2-SYSTEM-RULEVIEW-002<br>TR-P2-SYSTEM-RULEVIEW-003 |
| INV-COMPILER-018 | READ、WRITE、EXECUTE 三种权限独立判定；未声明权限不产生允许，尤其共享模型 WRITE 默认拒绝。 | 编译 ModelAccessRule | ERR-MIX-MODEL-ACCESS-DENIED | TR-P2-SYSTEM-RULEVIEW-004 |
| INV-COMPILER-019 | 能由 System、目标、ModelPath 和 operation 在编译期确定的非法访问必须阻断发布；只有静态合法但资源事实真正依赖运行时的访问才可登记 RuntimeGuardRequired。 | 访问规则编译 | ERR-MIX-MODEL-ACCESS-DENIED | TR-P2-SYSTEM-RULEVIEW-004<br>TR-P2-SYSTEM-RULEVIEW-006<br>TR-P2-SYSTEM-RULEVIEW-008 |
| INV-COMPILER-020 | 所有 Rule、change 与 custom action 的 mutation 路径在需要动态判定时必须先经过同一访问 Guard；DENY 必须发生在状态变更或外部副作用之前。 | 运行时 mutation 授权 | ERR-MIX-MODEL-ACCESS-RUNTIME-DENIED | TR-P2-SYSTEM-RULEVIEW-006<br>TR-P2-SYSTEM-RULEVIEW-007 |
| INV-COMPILER-021 | ModelPath 使用同一精确编译语义供 expression、change、query 和 access 校验复用；未知段、非复合中间段、模糊匹配或跨目标搜索均非法。 | 编译模型路径 | ERR-MIX-MODEL-PATH-INVALID | TR-P2-SYSTEM-RULEVIEW-005 |
| INV-COMPILER-022 | P2 只收敛 System、RuleView 与 model-access 所有权/授权语义；declaration 兼容事实在 P2 保留且不得形成第二运行时权威，最终退役边界仍属于 P7。 | 处理 declaration 兼容边界 | ERR-MIX-P2-DECLARATION-BOUNDARY | TR-P2-SYSTEM-RULEVIEW-010 |

## 11. 状态机

### SM-COMPILATION-SESSION CompilationSession 生命周期

- 初始状态：`CREATED`；状态：`CREATED`, `SOURCES_DISCOVERED`, `PARSED`, `RAW_BUILT`, `STRUCTURALLY_VALIDATED`, `SYMBOLS_REGISTERED`, `REFERENCES_RESOLVED`, `GRAPH_PREPARED`, `SEMANTICALLY_VALIDATED`, `PUBLISHED`, `FAILED`；终态：`PUBLISHED`, `FAILED`。

| 转换 | 当前状态 | 命令 | 下一状态 | 前置条件 | 副作用 | 失败 |
|---|---|---|---|---|---|---|
| TRANS-COMP-001 | CREATED | discoverSources | SOURCES_DISCOVERED | root DocumentSource 与允许根存在<br>SourceProvider 已注入 | 构建有类型 MixSourceGraph | 任何 Source ERROR 转 FAILED |
| TRANS-COMP-002 | SOURCES_DISCOVERED | parseCanonicalDocuments | PARSED | 所有 Source format 有已注册 frontend | 构建 CanonicalDocumentNode | 格式或安全 ERROR 转 FAILED |
| TRANS-COMP-003 | PARSED | buildRawDefinitions | RAW_BUILT | Canonical 节点有效 | 构建 RawDefinitionSet 并保留 SourceRef | 结构 ERROR 转 FAILED |
| TRANS-COMP-004 | RAW_BUILT | validateStructure | STRUCTURALLY_VALIDATED | 所有必需字段可判定 | 产生稳定 Diagnostic | ERROR 转 FAILED |
| TRANS-COMP-005 | STRUCTURALLY_VALIDATED | registerSymbols | SYMBOLS_REGISTERED | TypedKey 可构造 | 注册全部符号后封存 | 重复 Key 转 FAILED |
| TRANS-COMP-006 | SYMBOLS_REGISTERED | resolveReferences | REFERENCES_RESOLVED | 注册阶段已封存 | 解析前向引用、Information owner、ModelAccess selector | 未知、归属或歧义 ERROR 转 FAILED |
| TRANS-COMP-007 | REFERENCES_RESOLVED | classifyDeferred | GRAPH_PREPARED | P1 可解析 Key 完整 | 登记 P2～P8 DeferredDefinition | Deferred 不完整转 FAILED |
| TRANS-COMP-008 | GRAPH_PREPARED | validateSemantics | SEMANTICALLY_VALIDATED | 无先前 ERROR | 计算稳定 Diagnostic 顺序与 digest 输入 | ERROR 转 FAILED |
| TRANS-COMP-009 | SEMANTICALLY_VALIDATED | publishAtomically | PUBLISHED | Diagnostic 中不存在 ERROR<br>所有 Registry 可防御性冻结<br>ContextPublisher 与 expectedCurrent 已注入 | compiler 一次性创建 CompiledModelSet 和 EngineContext，并在本次 compile 调用内按 expectedCurrent 条件原子发布 | 条件冲突、空返回或异常均转 FAILED且不替换旧 Context |

## 12. 领域服务、策略与事件

### 12.1 服务

| ID | 服务 | 引入理由 | 输入 | 输出 | 追踪 |
|---|---|---|---|---|---|
| SVC-SOURCE-DISCOVERY | SourceDiscoveryService | 源图跨多个文档类型和间接 Rule 文件，不自然属于单一定义对象。 | DocumentSource<br>SourceProvider<br>SourcePolicy | MixSourceGraph<br>Diagnostic | TR-P1-COMPILER-001 |
| SVC-CANONICALIZATION | CanonicalizationService | XML/YAML frontend 必须共享格式中立契约且隔离具体解析库节点。 | DocumentSource<br>DocumentFrontend | CanonicalDocumentNode<br>Diagnostic | TR-P1-COMPILER-002 |
| SVC-RAW-BUILDER | RawDefinitionBuilder | 跨定义类型构建 RawDefinitionSet，需要统一 SourceRef 和未知节点策略。 | CanonicalDocumentNode | RawDefinitionSet<br>Diagnostic | TR-P1-COMPILER-002<br>TR-P1-COMPILER-003 |
| SVC-SYMBOL-REGISTRATION | SymbolRegistrationService | 跨文件前向引用要求先完成所有 TypedKey 注册再解析。 | RawDefinitionSet | SymbolTable<br>Diagnostic | TR-P1-COMPILER-003 |
| SVC-REFERENCE-RESOLUTION | ReferenceResolutionService | Information 所有权、RuleView 归属、ModelAccess selector 和跨文件引用需要统一确定性策略。 | RawDefinitionSet<br>SymbolTable<br>InformationOwnershipPolicy<br>ModelAccessSelectorPolicy | ResolvedDefinitions<br>ModelAccessBinding<br>Diagnostic | TR-P1-COMPILER-003<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009<br>TR-P2-SYSTEM-RULEVIEW-002<br>TR-P2-SYSTEM-RULEVIEW-003<br>TR-P2-SYSTEM-RULEVIEW-005 |
| SVC-DEFERRED-CLASSIFICATION | DeferredClassificationService | P1 必须显式区分已解析结构与后续阶段运行语义。 | ResolvedDefinitions | DeferredDefinitionRegistry<br>Diagnostic | TR-P1-COMPILER-004<br>TR-P1-COMPILER-008 |
| SVC-MODEL-PUBLICATION | ModelPublicationService | 只有 compiler 内的单一服务能保护 ERROR 不发布、防御性冻结、expectedCurrent 条件和旧 Context 不替换不变量。 | ValidatedCompilationSession<br>PublicationRequest<br>ContextPublisher | CompilationResult<br>CompiledModelSet<br>EngineContext | TR-P1-COMPILER-005<br>TR-P1-COMPILER-006 |
| SVC-SYSTEM-COMPILATION | SystemCompilationService | System 多源加载、显式身份和成员所有权需要确定性编译边界。 | RawSystemDefinition<br>SystemKey<br>SymbolTable | CompiledSystem<br>Diagnostic | TR-P2-SYSTEM-RULEVIEW-001<br>TR-P2-SYSTEM-RULEVIEW-008<br>TR-P2-SYSTEM-RULEVIEW-009 |
| SVC-RULEVIEW-RESOLUTION | RuleViewResolutionService | RuleView 必须以 owner-qualified composite identity 注册与解析，禁止裸名称回退。 | SystemKey<br>RuleViewName<br>SymbolTable | RuleViewKey<br>ResolvedRuleView<br>Diagnostic | TR-P2-SYSTEM-RULEVIEW-002<br>TR-P2-SYSTEM-RULEVIEW-003 |
| SVC-MODEL-PATH-COMPILATION | ModelPathCompilationService | expression、change、query 与访问控制必须共享同一精确路径语义。 | TypedKey<br>RawPath<br>CompiledModelShape | ModelPath<br>Diagnostic | TR-P2-SYSTEM-RULEVIEW-005 |
| SVC-MODEL-ACCESS-AUTHORIZATION | ModelAccessAuthorizationService | 静态可判定权限必须在发布前 fail-closed，真正动态的合法访问必须统一运行时 Guard。 | CompiledSystem<br>ModelAccessRule<br>ModelPath<br>AccessOperation | StaticAllow\\|RuntimeGuardRequired\\|Diagnostic | TR-P2-SYSTEM-RULEVIEW-004<br>TR-P2-SYSTEM-RULEVIEW-006<br>TR-P2-SYSTEM-RULEVIEW-007<br>TR-P2-SYSTEM-RULEVIEW-008 |

### 12.2 策略

| ID | 策略 | 引入理由 | 输入 | 输出 | 追踪 |
|---|---|---|---|---|---|
| POL-INFORMATION-OWNERSHIP | InformationOwnershipPolicy | Information owner 和 common 跨 System 规则横跨 System、Information 与 BusinessScope 定义。 | RawSystemDefinition<br>RawInformationDefinition<br>SymbolTable | InformationKey<br>ResolvedInformationDependencies<br>Diagnostic | TR-P1-COMPILER-003<br>TR-P1-COMPILER-004<br>TR-P1-COMPILER-008 |
| POL-MODEL-ACCESS-SELECTOR | ModelAccessSelectorPolicy | target-main 优先和 property path 回退是可变但必须确定性的业务规则。 | SharedModelPath<br>ViewKey<br>TargetSelector<br>CompiledViewStructure | ModelAccessBinding<br>Diagnostic | TR-P1-COMPILER-008<br>TR-P1-COMPILER-009<br>TR-P2-SYSTEM-RULEVIEW-005 |
| POL-DEFERRED-BOUNDARY | DeferredBoundaryPolicy | 防止 P2 在收敛 System、RuleView、model-access 后继续提前执行 P3～P8 语义或静默忽略未完成定义。 | ResolvedDefinition<br>StageOwnershipMatrix | DeferredDefinition | TR-P1-COMPILER-004<br>TR-P2-SYSTEM-RULEVIEW-010 |
| POL-PUBLICATION | AtomicPublicationPolicy | 保护无 ERROR、全量冻结、expectedCurrent 条件发布和旧 Context 保留的一致性边界。 | CompilationSession<br>DiagnosticSet<br>PublicationRequest<br>ContextPublisher | CompilationResult | TR-P1-COMPILER-005 |
| POL-SOURCE-SECURITY | SourceSecurityPolicy | 路径、URI、XML 和 YAML 安全需要统一前置拒绝。 | DocumentSource<br>FrontendSecurityOptions | AllowedSource\\|Diagnostic | TR-P1-COMPILER-001<br>TR-P1-COMPILER-002 |
| POL-RETIREMENT | DeclarationRetirementPolicy | 不可逆替代必须保证临时模块及 Adapter 无残留。 | RepositoryTree<br>Reactor<br>DependencyTree<br>ArtifactList | RetirementGateResult | TR-P1-COMPILER-007 |
| POL-RULEVIEW-COMPOSITE-IDENTITY | RuleViewCompositeIdentityPolicy | 跨 System 同名隔离和无歧义调用要求 identity 始终绑定 SystemKey。 | SystemKey<br>RuleViewName | RuleViewKey<br>Diagnostic | TR-P2-SYSTEM-RULEVIEW-002<br>TR-P2-SYSTEM-RULEVIEW-003 |
| POL-MODEL-PATH | ModelPathPolicy | 所有模型路径消费者必须共享精确、确定性的路径编译规则。 | TypedKey<br>RawPath<br>CompiledModelShape | ModelPath<br>Diagnostic | TR-P2-SYSTEM-RULEVIEW-005 |
| POL-MODEL-ACCESS-AUTHORIZATION | ModelAccessAuthorizationPolicy | 访问控制采用最小权限和 fail-closed；READ/WRITE/EXECUTE 独立，未声明共享 WRITE 默认拒绝。 | SystemKey<br>TargetKey<br>ModelPath<br>AccessOperation<br>DeclaredModelAccess | StaticAllow\\|RuntimeGuardRequired\\|Diagnostic | TR-P2-SYSTEM-RULEVIEW-004<br>TR-P2-SYSTEM-RULEVIEW-006<br>TR-P2-SYSTEM-RULEVIEW-007 |

### 12.3 事件

| ID | 事件 | 语义/理由 | 输入 | 输出 | 追踪 |
|---|---|---|---|---|---|
| EVT-SOURCES-DISCOVERED | SourcesDiscovered | 记录源图事实供后续 Pass 使用，不表示业务运行事件。 | MixSourceGraph | source count<br>typed edge count | TR-P1-COMPILER-001 |
| EVT-COMPILATION-FAILED | CompilationFailed | 对外表达未发布且旧 Context 不变的已发生编译事实。 | CompilationSessionId<br>DiagnosticSet | FAILED CompilationResult | TR-P1-COMPILER-003<br>TR-P1-COMPILER-005<br>TR-P1-COMPILER-008<br>TR-P1-COMPILER-009 |
| EVT-MODEL-PUBLISHED | ModelPublished | 对外表达 compiler 已在同一次 compile 调用内使新的不可变 Context 原子可见。 | CompiledModelSet<br>DigestPair<br>expectedCurrent | EngineContextId | TR-P1-COMPILER-005<br>TR-P1-COMPILER-006 |

## 13. 业务错误与 Diagnostic

Diagnostic 继续按 SourceRef/code/definition/pass 稳定排序；所有静态 ERROR 阻断候选 Context 发布。运行时权限拒绝必须发生在 mutation/外部副作用之前。

| 错误 ID | Diagnostic/触发条件 | 对外语义 | 可重试 | 状态改变 | 追踪 |
|---|---|---|---|---|---|
| ERR-MIX-SOURCE-POLICY | 编译器依赖硬编码 demo/fixture 路径或未注入根 SourceProvider | 源发现策略无效 | 否 | 否 | TR-P1-COMPILER-001 |
| ERR-MIX-SOURCE-NOT-FOUND | 显式引用的配置 Source 不存在 | 配置源缺失 | 是 | 否 | TR-P1-COMPILER-001 |
| ERR-MIX-SOURCE-PATH-ESCAPE | Source 逃逸允许根路径或使用未授权 URI scheme | 配置源访问被安全策略拒绝 | 否 | 否 | TR-P1-COMPILER-001 |
| ERR-MIX-SOURCE-DUPLICATE-ID | 两个来源规范化后得到相同 sourceId | 源身份冲突 | 否 | 否 | TR-P1-COMPILER-001 |
| ERR-MIX-XML-UNSAFE | XML 包含外部实体、DOCTYPE 网络解析或不安全特性 | XML 输入被安全策略拒绝 | 否 | 否 | TR-P1-COMPILER-002 |
| ERR-MIX-YAML-UNSAFE | YAML 请求任意 Java 类型构造或不安全 tag | YAML 输入被安全策略拒绝 | 否 | 否 | TR-P1-COMPILER-002 |
| ERR-MIX-SYMBOL-DUPLICATE | 同一 TypedKey 被重复注册 | 定义身份重复且不得覆盖 | 否 | 否 | TR-P1-COMPILER-003 |
| ERR-MIX-REF-UNKNOWN | 注册完成后引用仍无法解析到目标 TypedKey | 引用目标不存在 | 是 | 否 | TR-P1-COMPILER-003 |
| ERR-MIX-REF-RULE-SYSTEM-MISMATCH | RuleView 来源 System、system 属性或 view-ref 所属 System 不一致 | RuleView 归属冲突 | 否 | 否 | TR-P1-COMPILER-003<br>TR-P1-COMPILER-008 |
| ERR-MIX-INFORMATION-OWNER | Information 位于 BusinessScope、System 外部或使用非 System 命名空间 | Information 所有权非法 | 否 | 否 | TR-P1-COMPILER-003<br>TR-P1-COMPILER-008 |
| ERR-MIX-INFORMATION-CROSS-SYSTEM | 普通 System expression 引用其它 System InformationKey | 跨 System expression 必须迁入 common | 否 | 否 | TR-P1-COMPILER-008 |
| ERR-MIX-COMMON-MEMBER | common 声明非 expression Information 或拥有 Data/View/RuleView/ModelAccess | common 成员超出允许边界 | 否 | 否 | TR-P1-COMPILER-008 |
| ERR-MIX-COMMON-UNQUALIFIED | common 或 BusinessScope 使用未限定、未知 System 或未知局部名称的 Information 引用 | 跨 System Information 引用必须完整限定 | 是 | 否 | TR-P1-COMPILER-008 |
| ERR-MIX-REF-VIEW-NOT-DECLARED | Information 或 ModelAccess 指向当前 System 未声明的 View | 目标 View 不在当前 System 边界 | 是 | 否 | TR-P1-COMPILER-008<br>TR-P1-COMPILER-009 |
| ERR-MIX-MODEL-ACCESS-NOT-FOUND | selector 未命中 target-main 且同一 View property path 不存在 | ModelAccess 目标选择器无匹配 | 是 | 否 | TR-P1-COMPILER-009 |
| ERR-MIX-MODEL-ACCESS-AMBIGUOUS | selector、重复 ref 或写映射产生多个候选或重叠冲突 | ModelAccess 映射不唯一 | 否 | 否 | TR-P1-COMPILER-009 |
| ERR-MIX-MODEL-ACCESS-NON-COMPOSITE | property path 中间段不是复合属性 | ModelAccess property path 无法继续遍历 | 是 | 否 | TR-P1-COMPILER-009 |
| ERR-MIX-DEFERRED-INCOMPLETE | 后续语义缺少 requiredStage、reason、SourceRef 或已解析 Key | Deferred 边界不完整 | 否 | 否 | TR-P1-COMPILER-004 |
| ERR-MIX-PUBLICATION-BLOCKED | Diagnostic 含 ERROR 或 Registry 未能完整冻结 | 新模型禁止发布，旧 Context 保持 | 是 | 否 | TR-P1-COMPILER-005 |
| ERR-MIX-DIGEST-NONDETERMINISTIC | 同义输入与稳定选项产生不同 semanticDigest | 编译结果不可复现 | 否 | 否 | TR-P1-COMPILER-005 |
| ERR-MIX-CONTEXT-MUTATION | 调用方尝试修改已发布 Registry 或使用全局 current Context | 不可变和实例隔离契约被破坏 | 否 | 否 | TR-P1-COMPILER-005<br>TR-P1-COMPILER-006 |
| ERR-MIX-PROJECTION-WRITE | 旧核心调用方通过 CoreConfigProjection 注册、修改或删除事实 | 兼容投影只读 | 否 | 否 | TR-P1-COMPILER-006 |
| ERR-MIX-RETIREMENT-RESIDUE | 仓库、Reactor、依赖、服务、反射字符串或 artifact 仍包含 dec-expand-declaration 或 Adapter | 临时模块退役未完成 | 否 | 否 | TR-P1-COMPILER-007 |
| ERR-MIX-SYSTEM-DUPLICATE | 同一显式 SystemKey 被多个来源重复声明 | System 身份冲突；编译失败且不发布 | 否 | 否 | TR-P2-SYSTEM-RULEVIEW-001<br>TR-P2-SYSTEM-RULEVIEW-009 |
| ERR-MIX-RULEVIEW-DUPLICATE | 同一 System 内重复声明相同 RuleView name | RuleView 复合身份冲突；编译失败且不发布 | 否 | 否 | TR-P2-SYSTEM-RULEVIEW-002 |
| ERR-MIX-RULEVIEW-UNKNOWN | 使用裸 RuleView 名称、未知 System 或未知 (system,name) 调用目标 | RuleView 必须通过完整 composite identity 解析 | 否 | 否 | TR-P2-SYSTEM-RULEVIEW-003 |
| ERR-MIX-MODEL-PATH-INVALID | ModelPath 包含未知段、非复合中间段、模糊/跨目标搜索或其他非精确语义 | 模型路径非法；静态阶段阻断 | 否 | 否 | TR-P2-SYSTEM-RULEVIEW-005 |
| ERR-MIX-MODEL-ACCESS-DENIED | 静态可判定的 READ/WRITE/EXECUTE 未被显式授权或越过 System/目标/path 边界 | 访问被拒绝；编译失败且候选 Context 不发布 | 否 | 否 | TR-P2-SYSTEM-RULEVIEW-004<br>TR-P2-SYSTEM-RULEVIEW-008 |
| ERR-MIX-MODEL-ACCESS-RUNTIME-DENIED | 编译期合法但运行时 Guard 判定当前资源访问无权 | 访问在 mutation/副作用之前失败，状态保持不变 | 是 | 否 | TR-P2-SYSTEM-RULEVIEW-006<br>TR-P2-SYSTEM-RULEVIEW-007 |
| ERR-MIX-P2-DECLARATION-BOUNDARY | P2 尝试删除 declaration 兼容边界或建立与统一 compiler/context 并行的第二运行时权威 | 阶段越界；必须保留到 P7 收敛 | 否 | 否 | TR-P2-SYSTEM-RULEVIEW-010 |

## 14. 跨模块实现与生命周期

| 模块 | 业务模型责任 | P2 增量责任 | 失败责任 |
|---|---|---|---|
| XML/YAML frontend | 安全解析并产生 CanonicalDocumentNode | 保留显式 System/RuleView/model-access SourceRef 事实，不创建全局状态 | 格式、安全和来源错误在进入 RawDefinitionSet 前失败 |
| dec-core-compiler | Session、SourceGraph、Raw、TypedKey、Reference、Deferred、Diagnostic、digest、原子发布 | SystemCompilation、RuleView composite resolution、ModelPath compilation、static access decision | 任一静态 ERROR 不发布候选 Context |
| dec-core-context | 不可变 EngineContext、CoreConfigProjection、ContextPublisher | 持有 owner-qualified System/RuleView/access facts 与 Runtime Guard 所需不可变事实 | 保持 Context isolation；不得全局可变查找 |
| dec-core-starter/调用边界 | 注入 SourceProvider/frontend/compiler/publisher | 调用 RuleView 使用 system-ref + rule-ref；真正动态访问进入统一 Guard | 未知 composite key 或 runtime DENY 明确失败 |
| dec-demo | 真实 mix fixture 与契约证据 | 提供 systems.xml、同名 RuleView、授权/拒绝矩阵 | fixture 只做验证，不成为生产依赖 |
| declaration legacy boundary | P1 已退役临时 dec-expand-declaration 实现 | P2 只保留现存 declaration System 兼容/迁移边界说明；最终收敛属于 P7 | P2 若提前删除边界或建立第二 runtime authority 必须阻断 |

## 15. 追踪映射

| TR | 业务模型稳定 ID |
|---|---|
| TR-P1-COMPILER-001 | SCN-COMPILER-SUCCESS<br>SCN-SECURE-FRONTEND<br>ENT-MIX-SOURCE-GRAPH<br>INV-COMPILER-001<br>INV-COMPILER-002<br>INV-COMPILER-012<br>SVC-SOURCE-DISCOVERY |
| TR-P1-COMPILER-002 | SCN-COMPILER-SUCCESS<br>SCN-SECURE-FRONTEND<br>ENT-RAW-DEFINITION-SET<br>VO-CANONICAL-DOCUMENT-NODE<br>INV-COMPILER-012<br>SVC-CANONICALIZATION<br>SVC-RAW-BUILDER |
| TR-P1-COMPILER-003 | SCN-COMPILER-INVALID-REFERENCE<br>ENT-SYMBOL-TABLE<br>VO-TYPED-KEY<br>VO-INFORMATION-KEY<br>INV-COMPILER-003<br>INV-COMPILER-004<br>INV-COMPILER-006<br>INV-COMPILER-015<br>SVC-SYMBOL-REGISTRATION<br>SVC-REFERENCE-RESOLUTION |
| TR-P1-COMPILER-004 | SCN-COMMON-EXPRESSION<br>ENT-DEFERRED-REGISTRY<br>VO-DEFERRED-DEFINITION<br>INV-COMPILER-005<br>INV-COMPILER-008<br>SVC-DEFERRED-CLASSIFICATION<br>POL-DEFERRED-BOUNDARY |
| TR-P1-COMPILER-005 | SCN-COMPILER-SUCCESS<br>SCN-COMPILER-INVALID-REFERENCE<br>SCN-MULTI-CONTEXT-ISOLATION<br>AGG-PUBLISHED-CONTEXT<br>ENT-COMPILED-MODEL-SET<br>ENT-ENGINE-CONTEXT<br>VO-DIGEST-PAIR<br>INV-COMPILER-009<br>INV-COMPILER-010<br>INV-COMPILER-011<br>SVC-MODEL-PUBLICATION<br>POL-PUBLICATION |
| TR-P1-COMPILER-006 | SCN-MULTI-CONTEXT-ISOLATION<br>ENT-CORE-CONFIG-PROJECTION<br>AGG-PUBLISHED-CONTEXT<br>INV-COMPILER-014 |
| TR-P1-COMPILER-007 | SCN-RETIRE-DECLARATION<br>INV-COMPILER-013<br>POL-RETIREMENT<br>ERR-MIX-RETIREMENT-RESIDUE |
| TR-P1-COMPILER-008 | SCN-COMMON-EXPRESSION<br>SCN-MODEL-ACCESS-TARGET-MAIN<br>VO-INFORMATION-KEY<br>VO-MODEL-ACCESS-BINDING<br>INV-COMPILER-004<br>INV-COMPILER-005<br>INV-COMPILER-006<br>POL-INFORMATION-OWNERSHIP<br>SVC-REFERENCE-RESOLUTION |
| TR-P1-COMPILER-009 | SCN-MODEL-ACCESS-TARGET-MAIN<br>SCN-MODEL-ACCESS-PROPERTY-FALLBACK<br>VO-MODEL-ACCESS-BINDING<br>INV-COMPILER-007<br>POL-MODEL-ACCESS-SELECTOR<br>ERR-MIX-MODEL-ACCESS-NOT-FOUND<br>ERR-MIX-MODEL-ACCESS-AMBIGUOUS<br>ERR-MIX-MODEL-ACCESS-NON-COMPOSITE |
| TR-P2-SYSTEM-RULEVIEW-001 | ENT-COMPILED-SYSTEM<br>INV-COMPILER-016<br>SVC-SYSTEM-COMPILATION |
| TR-P2-SYSTEM-RULEVIEW-002 | VO-RULEVIEW-KEY<br>INV-COMPILER-017<br>POL-RULEVIEW-COMPOSITE-IDENTITY |
| TR-P2-SYSTEM-RULEVIEW-003 | VO-RULEVIEW-KEY<br>SVC-RULEVIEW-RESOLUTION<br>INV-COMPILER-017 |
| TR-P2-SYSTEM-RULEVIEW-004 | VO-MODEL-ACCESS-RULE<br>INV-COMPILER-018<br>POL-MODEL-ACCESS-AUTHORIZATION |
| TR-P2-SYSTEM-RULEVIEW-005 | VO-MODEL-PATH<br>INV-COMPILER-021<br>SVC-MODEL-PATH-COMPILATION |
| TR-P2-SYSTEM-RULEVIEW-006 | VO-MODEL-ACCESS-RULE<br>INV-COMPILER-019<br>SVC-MODEL-ACCESS-AUTHORIZATION |
| TR-P2-SYSTEM-RULEVIEW-007 | INV-COMPILER-020<br>SVC-MODEL-ACCESS-AUTHORIZATION<br>POL-MODEL-ACCESS-AUTHORIZATION |
| TR-P2-SYSTEM-RULEVIEW-008 | AGG-SYSTEM-COMPILED-CONFIG<br>AGG-COMPILATION-SESSION<br>INV-COMPILER-019 |
| TR-P2-SYSTEM-RULEVIEW-009 | INV-COMPILER-016<br>ENT-COMPILED-SYSTEM<br>VO-DIAGNOSTIC |
| TR-P2-SYSTEM-RULEVIEW-010 | INV-COMPILER-022<br>POL-DEFERRED-BOUNDARY<br>VO-DEFERRED-DEFINITION |

## 16. Revision 变更集与模块 Lineage

| 项目 | 内容 |
|---|---|
| Change Set | CHG-V_1.0-COMPILER-P2-BM-R07 |
| Base Revision | BM-R06@6a0bce4fa0ae |
| Result Revision | BM-R07@7d7bf504ca9d |
| Historical Lineage | DEC_COMPILER/BM-R05@4ecb1f8c09f4 → COMPILER/BM-R06@6a0bce4fa0ae → COMPILER/BM-R07@7d7bf504ca9d |
| 语义变化 | 新增 TERM-COMPILER-DOCUMENT-LINEAGE，明确路径/模块代码规范化不代表第二逻辑模块；P2 System/RuleView/model-access 业务语义保持 BM-R06，不静默改写已通过规则。 |
| 可读性变化 | 将 YAML 中完整集合重新投影为类似 BM-R05 的 17 节人类可读文档；Markdown 不再用单行 JSON 表格隐藏聚合、Key、Deferred、状态机、错误和跨模块边界。 |
| 下游处置 | Business Model rework 使 Design/TestDesign/Plan/TDD/Development 继续保持 STALE；只有当前 Revision 六项独立 Review 全部 PASSED 后才允许恢复 Design。 |

### 16.1 Stable ID 继承规则

- BM-R05 中所有仍成立的 terms/scenarios/entities/valueObjects/aggregates/invariants/stateMachines/services/policies/events/businessErrors/traceability stable IDs 必须继续存在。
- BM-R06 已声明的 P2 更新只允许通过 changeset 的 `update/add` 操作演进；本次 R07 不删除、不重命名既有 stable ID。
- `DEC_COMPILER` 历史文档只作为 lineage/history，不与 `COMPILER` 并行成为第二份当前事实源。

## 17. 未决问题、风险与停止条件

- 未决 P0/P1：本 Revision 完成 Review 后应为无。
- 若任何 Reviewer 发现 BM-R05 stable ID 遗失、P2 业务规则被静默改义、DEC_COMPILER/COMPILER 被解释为并行模块、裸 RuleView name fallback、默认允许共享 WRITE、Guard 可被旁路、P2 提前执行 P3～P7 或提前删除 declaration P7 边界，必须 `NEEDS_CHANGES` 并重新形成 Revision。
- 具体 Java 类、API 方法签名、包结构和实现 seam 继续属于 Design；Business Model 只冻结业务/架构语义输入。
