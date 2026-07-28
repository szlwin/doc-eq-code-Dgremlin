# DEC_COMPILER 详细设计

> Revision：`DESIGN-R04@1c14c8e89779`  
> 输入：`REQAN-R04@7421b050ed44`、`BM-R04@1b19a0ba26b6`。稳定模型 ID、错误码和 9 条 TR 在本文中保持可追踪。

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
  DocumentFrontend, FrontendRegistry, CanonicalDocumentNode, SourceRef

dec.core.compiler.raw
  RawDefinitionSet, Raw*Definition, RawDefinitionBuilder

dec.core.compiler.symbol
  TypedKey, *Key, SymbolTable, SymbolTableBuilder, ReferenceResolver

dec.core.compiler.information
  InformationExpressionAst, InformationReferenceResolver,
  InformationOwnershipValidator, CommonSystemValidator

dec.core.compiler.modelaccess
  SharedModelPath, SystemViewSelector, ModelAccessBinding,
  ModelAccessSelectorResolver

dec.core.compiler.deferred
  DeferredDefinition, DeferredKind, RequiredStage, DeferredDefinitionRegistry

dec.core.compiler.pass
  CompilerPass, PassContext, PassResult, CompilerPipeline

dec.core.compiler.diagnostic
  Diagnostic, DiagnosticCode, DiagnosticCollector, DiagnosticOrder

dec.core.compiler.compiled
  CompiledModelSet, Compiled*Definition, DigestPair

dec.core.context
  EngineContext, Registry, CoreConfigProjection
```

## 3. 编译入口与 Session {#design-compilation-session}

`ModelCompiler.compile(request)` 每次创建一个 `CompilationSession`。Session 持有 request、source graph、canonical set、raw set、symbol table builder、diagnostic collector、deferred builder 和 metrics；不得放入 static/thread-local 全局容器。

状态机：

```text
CREATED -> DISCOVERED -> CANONICALIZED -> RAW_BUILT
        -> SYMBOLS_REGISTERED -> REFERENCES_RESOLVED
        -> VALIDATED -> COMPILED -> PUBLISHED
任一阶段 -> FAILED
```

只有 `VALIDATED` 且无 ERROR 才可进入 `COMPILED`；只有完整 EngineContext 构造成功才可进入 `PUBLISHED`。FAILED 为终态，新重试创建新 Session。

## 4. MixSourceResolver {#design-mix-source-resolver}

输入：root SourceReference、Provider、scheme 白名单、根路径、maxDepth、maxSources、maxTotalBytes。算法：

1. 解析 root 并提取 datasource/connection/data-view/system/business file edge；
2. 规范化 URI 和相对路径，检查根目录逃逸；
3. 展开 file set 后按 sourceId 排序；
4. 最小解析 System 文件，提取 rule-file edge；
5. 使用 `(sourceId, contentDigest)` 去重；相同 ID 不同内容报错；
6. 检测 edge cycle、深度和数量限制；
7. 输出稳定 `MixSourceGraph` 与 `SourceManifest`；
8. 发现阶段不登记业务定义。

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

XML/YAML Frontend 只负责安全解析与语法规范化；业务默认值、owner、Key 和引用规则属于 Raw builder/Compiler Pass。Canonical 不持有 DOM、SAX handler、JAXB 或 YAML Node。

## 6. RawDefinitionSet {#design-raw-definition-set}

覆盖 RootConfig、DataSource、Connection、Data、View、System、RuleView、Rule、BusinessScope、Information、ModelAccess、Directory、Action、Produce。每个 RawDefinition 保存 stable source ordinal、SourceRef、owner token、name、ordered attributes、raw references 和 normalized body。未知元素在 strict 模式产生 `ERR-MIX-STRUCT-*`，不得静默丢弃。

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
public record DeferredDefinition(
    DefinitionKey ownerKey,
    DeferredKind kind,
    RequiredStage requiredStage,
    String reasonCode,
    SourceRef sourceRef,
    NormalizedBody body,
    List<DefinitionKey> resolvedReferences) {}
```

映射：P2 System/ModelAccess；P3 Information DAG/循环/求值；P4 Action/Produce；P5 Directory；P6 Query/SQL；P7 Session/Transaction。缺 requiredStage、reason、SourceRef 或可解析但未类型化的引用时，报 `ERR-MIX-DEFERRED-INCOMPLETE`。

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

`CompiledModelSet` 包含 SourceManifest、各 TypedKey Registry、DeferredRegistry、DigestPair、compiler/schema/options version。构造完成后所有集合不可变。`EngineContext` 只通过构造函数接收完整模型；无 public mutator、无 static current、无隐式 register。

Publication 先完整构造候选 Context，再执行一次原子暴露。构造、验证、timeout、cancel 或 CAS 失败时旧 Context 保持有效，候选对象不可见并可回收。

## 14. CoreConfigProjection {#design-core-config-projection}

只读投影覆盖旧调用仍需读取的 Data/View/Rule 基础结构；每次由新 Registry 计算或在 Context 构造时不可变缓存。写 API 返回 `ERR-MIX-PROJECTION-WRITE`/抛专用 UnsupportedOperationException。不得包含 SystemDesc、BusinessDesc、Producer、Consumer、declaration 类型或第二 Registry。

## 15. Diagnostic 与错误映射 {#design-diagnostic-catalog}

`Diagnostic(code,severity,messageKey,definitionKey,sourceRef,relatedRefs,recoveryHint,passId)`；排序键为 `sourceId,line,column,code,definitionKey,passId`。以下 BM-R04 错误均有设计映射：

| 业务错误 ID | 设计 Diagnostic |
|---|---|
| ERR-MIX-SOURCE-POLICY | MIX-SRC-001 UNKNOWN_SOURCE_SCHEME |
| ERR-MIX-SOURCE-NOT-FOUND | MIX-SRC-002 SOURCE_NOT_FOUND |
| ERR-MIX-SOURCE-PATH-ESCAPE | MIX-SRC-003 PATH_ESCAPE |
| ERR-MIX-SOURCE-DUPLICATE-ID | MIX-SRC-004 DUPLICATE_SOURCE_ID |
| ERR-MIX-XML-UNSAFE | MIX-SEC-001 XML_UNSAFE |
| ERR-MIX-YAML-UNSAFE | MIX-SEC-002 YAML_UNSAFE |
| ERR-MIX-SYMBOL-DUPLICATE | MIX-SYMBOL-001 DUPLICATE_KEY |
| ERR-MIX-REF-UNKNOWN | MIX-REF-001 UNKNOWN_REFERENCE |
| ERR-MIX-REF-RULE-SYSTEM-MISMATCH | MIX-REF-002 RULE_SYSTEM_MISMATCH |
| ERR-MIX-INFORMATION-OWNER | MIX-INFO-001 INVALID_OWNER |
| ERR-MIX-INFORMATION-CROSS-SYSTEM | MIX-INFO-002 CROSS_SYSTEM_OWNER |
| ERR-MIX-COMMON-MEMBER | MIX-INFO-003 INVALID_COMMON_MEMBER |
| ERR-MIX-COMMON-UNQUALIFIED | MIX-INFO-004 UNQUALIFIED_REFERENCE |
| ERR-MIX-REF-VIEW-NOT-DECLARED | MIX-ACCESS-001 VIEW_NOT_DECLARED |
| ERR-MIX-MODEL-ACCESS-NOT-FOUND | MIX-ACCESS-002 SELECTOR_NOT_FOUND |
| ERR-MIX-MODEL-ACCESS-AMBIGUOUS | MIX-ACCESS-003 SELECTOR_AMBIGUOUS |
| ERR-MIX-MODEL-ACCESS-NON-COMPOSITE | MIX-ACCESS-004 NON_COMPOSITE_PATH |
| ERR-MIX-DEFERRED-INCOMPLETE | MIX-DEFER-001 INCOMPLETE_DEFERRED |
| ERR-MIX-PUBLICATION-BLOCKED | MIX-PUBLISH-001 ERROR_PREVENTS_PUBLICATION |
| ERR-MIX-DIGEST-NONDETERMINISTIC | MIX-DIGEST-001 NONDETERMINISTIC_RESULT |
| ERR-MIX-CONTEXT-MUTATION | MIX-CONTEXT-001 IMMUTABLE_CONTEXT |
| ERR-MIX-PROJECTION-WRITE | MIX-PROJECTION-001 WRITE_REJECTED |
| ERR-MIX-RETIREMENT-RESIDUE | MIX-RETIRE-001 LEGACY_RESIDUE |

## 16. Digest {#design-digest}

`sourceDigest` 基于规范化 sourceId 与原始字节摘要；`semanticDigest` 基于稳定序列化的 CompiledModelSet、resolved key 和 Deferred 元数据，不包含 SourceRef 物理行列。compilerVersion、schemaVersion、optionsDigest 单独进入 CompilationResult。禁止依赖 HashMap/文件系统枚举/线程调度顺序。

## 17. 跨模块调用与恢复 {#design-cross-module-contract}

- Frontend 同步返回 Canonical 或 Diagnostic；不自动重试不安全输入；
- Compiler 请求支持 deadline/cancel token；取消只影响当前 Session；
- Starter 只接收 SUCCESS result；context publication 按 semanticDigest 幂等；
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
