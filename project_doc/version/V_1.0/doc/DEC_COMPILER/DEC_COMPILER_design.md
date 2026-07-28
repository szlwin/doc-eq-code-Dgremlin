# DEC_COMPILER 详细设计

> 候选 Revision：`DESIGN-R05@0b37a9b4dd48`。`DESIGN-R04@1c14c8e89779` 已被 `REV-000038` 退回；当前为 DESIGN I007 返修候选，不复用旧 Review/Evidence。
> 输入：`REQAN-R05@7de35e8dc15b`、`BM-R05@4ecb1f8c09f4`。稳定模型 ID、错误码和 9 条 TR 在本文中保持可追踪。

## 1. 设计目标 {#design-goals}

P1 建立统一、确定性、安全、可诊断的配置编译骨架。P1 负责源发现、Canonical、Raw、强类型符号、引用绑定、Deferred 分类、CompiledModelSet 与原子发布；P2～P7 的权限、Information 求值、Action/Produce、Directory、Query、Transaction 均只登记 Deferred，不在本阶段执行。

## 2. 包与类型布局 {#design-packages}

```text
dec.core.compiler.api
  ModelCompiler, CompilationRequest, CompilationResult, CompilationOptions

dec.core.compiler.source
  DocumentSource, DocumentSourceProvider, MixSourceResolver,
  MixSourceGraph, SourceEdge, SourceManifest, SourcePolicy

dec.core.compiler.canonical
  DocumentFrontend, FrontendRegistry, CanonicalDocumentNode

dec.core.compiler.raw
  RawDefinitionSet, Raw*Definition, RawDefinitionBuilder

dec.core.compiler.symbol
  SymbolTableBuilder, ReferenceResolver

dec.core.compiler.information
  InformationExpressionAst, InformationReferenceResolver,
  InformationOwnershipValidator, CommonSystemValidator

dec.core.compiler.modelaccess
  SharedModelPath, SystemViewSelector, ModelAccessBinding,
  ModelAccessSelectorResolver

dec.core.compiler.deferred
  DeferredDefinitionBuilder, DeferredClassificationPolicy

dec.core.compiler.pass
  CompilerPass, PassContext, PassResult, CompilerPipeline

dec.core.compiler.diagnostic
  DiagnosticCollector, DiagnosticOrder

dec.core.compiler.compiled
  SemanticDigestInput, CompiledModelSetBuilder

dec.core.context.model
  SourceRef, Diagnostic, DiagnosticCode, DefinitionKey, *Key, Registry,
  DeferredKind, RequiredStage, DeferredDefinition, DeferredRegistry,
  CompiledDefinition, DigestPair, CompiledModelSet

dec.core.context
  EngineContext, CoreConfigProjection
```

`dec-core-context` 只拥有发布边界两侧共享的中立不可变值对象，不依赖 compiler。`CompiledModelSet` 可达的类型闭包，包括 Diagnostic/Code、SourceRef、Key、Deferred、CompiledDefinition 和 Digest，全部在 context；compiler 只保留 collector、order 与各类 builder。`dec-core-compiler` 依赖 context 并拥有所有可变 builder、Session、Pass 和发布协调；因此不存在 `compiler -> context -> compiler` 循环。

## 3. 编译入口与 Session {#design-compilation-session}

`ModelCompiler.compileAndPublish(request, publicationRequest)` 每次创建一个 `CompilationSession`。Session 持有 request、publication request、source graph、canonical set、raw set、symbol table builder、diagnostic collector、deferred builder 和 timing collector；不得放入 static/thread-local 全局容器。compile-only 能力仅作为包内 PassHarness 接缝存在，不得作为可绕过发布状态机的公共成功入口。

状态机：

```text
CREATED -> SOURCES_DISCOVERED -> PARSED -> RAW_BUILT
        -> STRUCTURALLY_VALIDATED -> SYMBOLS_REGISTERED
        -> REFERENCES_RESOLVED -> GRAPH_PREPARED
        -> SEMANTICALLY_VALIDATED -> PUBLISHED
任一阶段 -> FAILED
```

状态名与顺序严格复用需求 `6.1.7` 和 BM-R05 `TRANS-COMP-001`～`TRANS-COMP-009`，不增加 `COMPILED` 等平行状态。`PARSED` 表示所有 Frontend 已产生 Canonical 节点；`STRUCTURALLY_VALIDATED` 表示 Canonical/Raw 结构校验完成；`GRAPH_PREPARED` 表示 P1 引用已解析且 P2～P7 Deferred 已完整分类。只有 `SEMANTICALLY_VALIDATED` 且无 ERROR 才可执行发布转换；该转换先构造完整候选，再通过显式 ContextPublisher 原子暴露，成功后进入 `PUBLISHED`。任何 ERROR、timeout、cancel、候选构造失败或 CAS conflict 都进入 `FAILED`，具体原因分别由 `MIX-PUBLICATION-BLOCKED`、`MIX-COMPILATION-TIMED-OUT`、`MIX-COMPILATION-CANCELLED`、`MIX-CONTEXT-CONSTRUCTION-FAILED`、`MIX-PUBLICATION-CONFLICT` 表达；Publisher 返回 null 或抛异常使用 `MIX-PUBLICATION-FAILURE`。FAILED 为终态，新重试创建新 Session。

## 4. MixSourceResolver {#design-mix-source-resolver}

输入：root SourceReference、Provider、scheme 白名单、根路径、maxDepth、maxSources、maxTotalBytes。算法：

1. 解析 root 并提取 `ROOT_DATA_FILESET`、`ROOT_VIEW_FILESET`、`ROOT_SYSTEM_FILE`、`ROOT_BUSINESS_FILE` 声明边；System 文件提取 `SYSTEM_RULE_FILE`；
2. 规范化 URI 和相对路径，检查根目录逃逸；
3. 展开 file set 后按 sourceId 排序；
4. 最小解析 System 文件，提取 rule-file edge；
5. 每条边构造 `SourceGraphEdge(edgeType,fromSourceId,targetReference,declarationSourceRef)`；`declarationSourceRef` 指向父 Source 中声明该引用的位置，根边使用 synthetic root SourceRef；
6. 使用 `(sourceId, contentDigest)` 去重；相同 ID 不同内容报错；
7. 检测 edge cycle、深度和数量限制；
8. 输出稳定 `MixSourceGraph` 与 `SourceManifest`，source 和 edge 均按规范化稳定键排序；
9. 发现阶段不登记业务定义。

## 5. CanonicalDocumentNode {#design-canonical-document-node}

```text
nodeName: String
orderedAttributes: List<CanonicalAttribute>
scalar: Optional<CanonicalScalar>
orderedChildren: List<CanonicalDocumentNode>
sourceRef: SourceRef
format: XML|YAML
schemaVersion: String
```

XML/YAML Frontend 只负责安全解析与语法规范化；业务默认值、owner、Key 和引用规则属于 Raw builder/Compiler Pass。Canonical 不持有 DOM、SAX handler、JAXB 或 YAML Node，并统一引用 `dec.core.context.model.SourceRef`。

## 6. RawDefinitionSet {#design-raw-definition-set}

覆盖 RootConfig、DataSource、Connection、Data、View、System、RuleView、Rule、BusinessScope、Information、ModelAccess、Directory、Action、Produce。每个 RawDefinition 保存 stable source ordinal、SourceRef、owner token、name、ordered attributes、raw references 和 normalized body。未知元素在 P1 一律产生 `MIX-STRUCTURE-UNKNOWN` ERROR，不提供可静默发布的 lenient 模式。未来扩展必须先由版本化 Frontend/Raw builder 显式声明节点语义。

## 7. TypedKey 与 SymbolTable {#design-typed-key-symbol-table}

TypedKey 类型至少包括 `DataSourceKey/ConnectionKey/DataKey/ViewKey/SystemKey/RuleViewKey/BusinessScopeKey/InformationKey/DirectoryKey/ActionKey/ProduceKey`。

- `InformationKey(SystemKey owner, String name)`，不再绑定 BusinessScope；
- `RuleViewKey(SystemKey owner, String name)`；
- `ActionKey(DirectoryKey owner, String actionName)`；
- 无名 Produce 以 `(ActionKey, sourceOrdinal)` 形成稳定 Key；
- 注册分两遍：先顶层/owner Key，再解析子定义和前向引用；
- 同类型重复 Key 报 `ERR-MIX-SYMBOL-DUPLICATE`，不同类型同名互不覆盖；
- Registry 使用有序不可变 map，禁止最后写入覆盖。

## 8. System-owned Information 与 common expression {#design-information-expression}

`RawInformationDefinition` 包含 owner System、kind、expression/rule-data/change-data body 与 SourceRef。P1：

1. 创建 `InformationKey`；
2. expression 解析为 `InformationExpressionAst`；
3. 完整限定引用解析为强类型 `InformationKey`；
4. 普通 System 的依赖 owner 必须等于当前 owner；
5. 跨 System 依赖仅允许 owner=`common`；
6. common 不得包含 Data/View/RuleView/ModelAccess，且 Information 必须是 expression；
7. 未限定引用、未知引用、非法 owner 产生 Diagnostic；
8. 将 AST、resolved keys 和 SourceRef 登记为 P3 Deferred；P1 不求值、不建运行时缓存。

当前 fixture 的 `common.paySuccess` 精确依赖 `payment.success`、`order.paySuccessStatus`；`common.payError` 精确依赖 `payment.error`、`order.payErrorStatus`。

## 9. ModelAccess selector {#design-model-access-selector}

```text
ModelAccessBinding
  ownerSystem: SystemKey
  sourcePath: SharedModelPath
  targetView: ViewKey
  selector: SystemViewSelector
  resolvedTarget: TargetPropertyPath
  accessMode: READ|WRITE
  sourceRef: SourceRef
```

解析算法：

1. `ref@view` 必须精确命中当前 System `view-info` 已声明 View；
2. 将 `ref@property` 与目标 View 的 `target-main` 做区分大小写的完整精确匹配；
3. 未命中时按 `.` 分段遍历 property path；
4. 每段必须唯一，中间段必须是复合属性；
5. 同一 sourcePath 的完全重复 ref、重叠 WRITE、多候选均拒绝；
6. 不跨 View/System 搜索，不做前缀/后缀/模糊匹配，不回退 root-property。

输出为 P2 Deferred 的已解析 `ModelAccessBinding`；P1 不判断运行时读写权限。

## 10. 引用解析 {#design-reference-resolution}

P1 必须类型化解析：connection→datasource、view→data/property、system→data/view、rule file→system、ruleView→system/view、information expression→InformationKey、business action→system/ruleView、directory→information/subdirectory、produce→information（存在时）、ModelAccess→View/property。未知、类型不匹配、owner 不一致均聚合 Diagnostic，绝不猜测。

## 11. DeferredDefinition {#design-deferred-definition}

```java
public final class DeferredDefinition {
    private final DefinitionKey ownerKey;
    private final DeferredKind kind;
    private final RequiredStage requiredStage;
    private final String reasonCode;
    private final SourceRef sourceRef;
    private final NormalizedBody body;
    private final List<DefinitionKey> resolvedReferences;

    public DeferredDefinition(
            DefinitionKey ownerKey,
            DeferredKind kind,
            RequiredStage requiredStage,
            String reasonCode,
            SourceRef sourceRef,
            NormalizedBody body,
            List<DefinitionKey> resolvedReferences) {
        this.ownerKey = Objects.requireNonNull(ownerKey, "ownerKey");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.requiredStage = Objects.requireNonNull(requiredStage, "requiredStage");
        this.reasonCode = Objects.requireNonNull(reasonCode, "reasonCode");
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
        this.body = Objects.requireNonNull(body, "body");
        this.resolvedReferences = Collections.unmodifiableList(
            new ArrayList<DefinitionKey>(
                Objects.requireNonNull(resolvedReferences, "resolvedReferences")));
    }

    public DefinitionKey ownerKey() { return ownerKey; }
    public DeferredKind kind() { return kind; }
    public RequiredStage requiredStage() { return requiredStage; }
    public String reasonCode() { return reasonCode; }
    public SourceRef sourceRef() { return sourceRef; }
    public NormalizedBody body() { return body; }
    public List<DefinitionKey> resolvedReferences() { return resolvedReferences; }
}
```

所有 Key、Definition、Request、Result 等值对象采用相同 Java 8 不可变实现约束，并基于全部语义字段实现 `equals/hashCode/toString`。映射：P2 System/ModelAccess；P3 Information DAG/循环/求值；P4 Action/Produce；P5 Directory；P6 Query/SQL；P7 Session/Transaction。缺 requiredStage、reason、SourceRef 或可解析但未类型化的引用时，报 `ERR-MIX-DEFERRED-INCOMPLETE`。

## 12. Compiler Pipeline {#design-compiler-pipeline}

| # | Pass | 产出 | 失败行为 |
|---:|---|---|---|
| 1 | SourceGraphValidationPass | validated graph | 缺失/冲突/逃逸阻断 |
| 2 | StructuralValidationPass | validated canonical/raw | 未知/缺字段聚合 |
| 3 | SymbolRegistrationPass | typed symbol table | 重复 Key 聚合 |
| 4 | ReferenceResolutionPass | linked raw definitions | 未知/类型不匹配聚合 |
| 5 | InformationOwnershipPass | expression AST + keys | owner/common 违规阻断 |
| 6 | ModelAccessBindingPass | exact bindings | not-found/ambiguous 阻断 |
| 7 | DeferredClassificationPass | complete deferred registry | 不完整阻断 |
| 8 | P1SemanticValidationPass | invariant result | 违反 P1 不变量阻断 |
| 9 | DigestPass | source/semantic digest | 非确定性阻断 |
| 10 | PublicationPass | model/context | 仅无 ERROR 执行 |

所有 Pass 只通过 `PassContext` 读写 Session 内 builder；Diagnostic 排序后才形成结果。

## 13. CompiledModelSet 与 EngineContext {#design-compiled-model-set}

摘要构造分两步：先冻结 `SemanticDigestInput(SourceManifest semantic view, typed registries, resolved keys, Deferred metadata, compiler/schema/options version)`，该输入明确不包含 DigestPair、metrics 和 SourceRef 物理行列；计算 DigestPair 后再构造 `CompiledModelSet(content,digests,versions)`。因此不存在“CompiledModelSet 包含 digest、digest 又依赖 CompiledModelSet”的循环。

`CompiledModelSet` 位于 `dec-core-context`，最终包含 SourceManifest 的中立发布视图、各 TypedKey Registry、DeferredRegistry、DigestPair、compiler/schema/options version。它不引用 compiler 的 Raw、Pass、Session 或 builder。构造完成后所有集合不可变。`EngineContext` 只通过构造函数接收完整模型；无 public mutator、无 static current、无隐式 register。

Publication 先完整构造候选 Context，再执行一次原子暴露。构造、验证、timeout、cancel 或 CAS 失败时旧 Context 保持有效，候选对象不可见并可回收。

## 14. CoreConfigProjection {#design-core-config-projection}

只读投影覆盖旧调用仍需读取的 Data/View/Rule 基础结构；每次由新 Registry 计算或在 Context 构造时不可变缓存。写 API 产生 `MIX-PROJECTION-WRITE` 并抛专用 UnsupportedOperationException。不得包含 SystemDesc、BusinessDesc、Producer、Consumer、declaration 类型或第二 Registry。

## 15. Diagnostic 与错误映射 {#design-diagnostic-catalog}

`Diagnostic(code,severity,messageKey,definitionKey,sourceRef,relatedRefs,recoveryHint,pass)`；其中需求排序字段 `entityKey` 定义为 `definitionKey.map(DefinitionKey::canonical).orElse("")`。稳定排序键严格为 `sourceId,line,column,code,entityKey,pass`，不再维护 `passId` 或其它平行别名。以下 BM-R05 错误均有设计映射：

| 业务错误 ID | 设计 Diagnostic |
|---|---|
| ERR-MIX-SOURCE-POLICY | MIX-SOURCE-POLICY |
| ERR-MIX-SOURCE-NOT-FOUND | MIX-SOURCE-NOT-FOUND |
| ERR-MIX-SOURCE-PATH-ESCAPE | MIX-SOURCE-PATH-ESCAPE |
| ERR-MIX-SOURCE-DUPLICATE-ID | MIX-SOURCE-DUPLICATE-ID |
| ERR-MIX-XML-UNSAFE | MIX-FRONTEND-XML-UNSAFE |
| ERR-MIX-YAML-UNSAFE | MIX-FRONTEND-YAML-UNSAFE |
| ERR-MIX-SYMBOL-DUPLICATE | MIX-SYMBOL-DUPLICATE |
| ERR-MIX-REF-UNKNOWN | MIX-REF-UNKNOWN |
| ERR-MIX-REF-RULE-SYSTEM-MISMATCH | MIX-REF-RULE-SYSTEM-MISMATCH |
| ERR-MIX-INFORMATION-OWNER | MIX-INFORMATION-OWNER |
| ERR-MIX-INFORMATION-CROSS-SYSTEM | MIX-INFORMATION-CROSS-SYSTEM |
| ERR-MIX-COMMON-MEMBER | MIX-COMMON-MEMBER |
| ERR-MIX-COMMON-UNQUALIFIED | MIX-COMMON-UNQUALIFIED |
| ERR-MIX-REF-VIEW-NOT-DECLARED | MIX-REF-VIEW-NOT-DECLARED |
| ERR-MIX-MODEL-ACCESS-NOT-FOUND | MIX-MODEL-ACCESS-NOT-FOUND |
| ERR-MIX-MODEL-ACCESS-AMBIGUOUS | MIX-MODEL-ACCESS-AMBIGUOUS |
| ERR-MIX-MODEL-ACCESS-NON-COMPOSITE | MIX-MODEL-ACCESS-NON-COMPOSITE |
| ERR-MIX-DEFERRED-INCOMPLETE | MIX-DEFERRED-INCOMPLETE |
| ERR-MIX-PUBLICATION-BLOCKED | MIX-PUBLICATION-BLOCKED |
| ERR-MIX-DIGEST-NONDETERMINISTIC | MIX-DIGEST-NONDETERMINISTIC |
| ERR-MIX-CONTEXT-MUTATION | MIX-CONTEXT-MUTATION |
| ERR-MIX-PROJECTION-WRITE | MIX-PROJECTION-WRITE |
| ERR-MIX-RETIREMENT-RESIDUE | MIX-RETIREMENT-RESIDUE |

以下为技术状态机补充 code，不改写 BM-R05 的 23 个业务错误：`MIX-STRUCTURE-UNKNOWN`、`MIX-COMPILATION-CANCELLED`、`MIX-COMPILATION-TIMED-OUT`、`MIX-CONTEXT-CONSTRUCTION-FAILED`、`MIX-PUBLICATION-CONFLICT`、`MIX-PUBLICATION-FAILURE`、`MIX-OBSERVER-FAILURE`。它们同样是稳定公共契约。

## 16. Digest {#design-digest}

`sourceDigest` 基于规范化 sourceId 与原始字节 SHA-256；`semanticDigest` 使用 `DEC-SEMANTIC-DIGEST-V1`：将 SemanticDigestInput 编码为 UTF-8 canonical JSON，object key 按 Unicode code point 升序，数组按各领域 canonical key 升序，数字使用十进制规范形式，字符串使用 JSON 标准转义，缺失可选值编码为 JSON null，再计算 SHA-256。输入不包含 DigestPair、metrics 或 SourceRef 物理行列。compilerVersion、schemaVersion、optionsDigest 与 digestAlgorithmVersion 进入版本域，并由 PublishedCompilationResult 单独暴露。禁止依赖 HashMap、文件系统枚举或线程调度顺序。

### 16.1 Timing 与 Observer {#design-timing-observer}

编译器通过注入的 `MonotonicClock.nanoTime()` 采集 discovery、parse、每个 pass 和 digest 的纳秒耗时，并向 `CompilationObserver` 发送不可变 `CompilationTiming` 和 `SessionStateTransition`。Deadline 使用同一 MonotonicClock 的绝对纳秒域，因此测试可确定性推进时间。Observer 不参与 semanticDigest，也不得改变 Session 状态；Observer 抛出的异常转为非 ERROR 的 `MIX-OBSERVER-FAILURE` Diagnostic 后继续原流程。该 Diagnostic 可增加，但原 status、context 与 digest 不变。

## 17. 跨模块调用与恢复 {#design-cross-module-contract}

- Frontend 同步返回 Canonical 或 Diagnostic；不自动重试不安全输入；
- Compiler 请求支持 deadline/cancel token；取消只影响当前 Session；
- Starter 注入显式 PublicationRequest 并只接收 PUBLISHED/FAILED result；不得在 compileAndPublish 返回后再次发布；
- 无远程副作用，不存在“部分模型成功”；任何错误都回到旧 Context；
- 配置修复后通过新 CompilationSession 重编译；仓库恢复通过 Git revert。

## 18. dec-expand-declaration 退役 {#design-declaration-retirement}

删除模块、POM、依赖、服务注册、反射字符串、文档和测试引用。不提供 LegacyDeclarationAdapter、不保留双写。仓库扫描与 dependency tree 是阻断验收。

## 19. 追踪映射 {#design-traceability}

| TR | 设计锚点 |
|---|---|
| TR-P1-COMPILER-001 | `#design-mix-source-resolver`, `#design-compiler-pipeline` |
| TR-P1-COMPILER-002 | `#design-canonical-document-node`, `#design-raw-definition-set` |
| TR-P1-COMPILER-003 | `#design-typed-key-symbol-table`, `#design-information-expression` |
| TR-P1-COMPILER-004 | `#design-deferred-definition` |
| TR-P1-COMPILER-005 | `#design-compiled-model-set`, `#design-digest` |
| TR-P1-COMPILER-006 | `#design-core-config-projection` |
| TR-P1-COMPILER-007 | `#design-declaration-retirement` |
| TR-P1-COMPILER-008 | `#design-information-expression` |
| TR-P1-COMPILER-009 | `#design-model-access-selector` |
