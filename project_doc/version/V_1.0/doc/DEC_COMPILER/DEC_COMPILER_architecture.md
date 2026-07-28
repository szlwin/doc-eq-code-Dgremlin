# DEC_COMPILER 架构设计

> 候选 Revision：`DESIGN-R05@0b37a9b4dd48`。`DESIGN-R04@1c14c8e89779` 已被 `REV-000038` 退回；当前为 DESIGN I007 返修候选，不复用旧 Review/Evidence。
> 输入：`REQAN-R05@7de35e8dc15b`、`BM-R05@4ecb1f8c09f4`。本文只设计 P1 Compiler，不提前实现 P2～P7 运行时语义。

## 1. 目标与约束

- 将 XML/YAML 配置统一编译为不可变 `CompiledModelSet` 与显式 `EngineContext`；
- 编译过程局限于 `CompilationSession`，不得写入全局 `Config`、静态 Registry 或静态 current Context；
- Information 由 System 拥有，跨 System expression 只能由 `common` System 拥有；
- ModelAccess 以 source path 与 target selector 两部分建模，`target-main` 精确优先、property path 精确回退；
- 任一 ERROR 阻止发布，失败不得暴露部分 Registry，调用方已有 Context 保持有效；
- `dec-expand-declaration`、兼容 Adapter、第二 Runtime、第二 Registry 均禁止重新引入。

## 2. 总体组件图 {#arc-overview}

```text
DocumentSourceProvider
        |
        v
SourceDiscoveryService --> MixSourceGraph + SourceManifest
        |
        v
XML/YAML Frontend ------> CanonicalDocumentNode
        |
        v
RawDefinitionBuilder ---> RawDefinitionSet
        |
        v
ModelCompiler.compileAndPublish
        |
        v
CompilationSession
  1 StructuralValidationPass
  2 SymbolRegistrationPass
  3 ReferenceResolutionPass
  4 InformationOwnershipPass
  5 ModelAccessBindingPass
  6 DeferredClassificationPass
  7 P1SemanticValidationPass
  8 DigestPass
  9 PublicationPass
        |
        +-- ERROR --> CompilationFailed + Diagnostic[]
        |
        +-- PUBLISHED -> CompiledModelSet -> EngineContext
                                      \-> CoreConfigProjection(read-only)
```

主路径、失败路径、重试和补偿都由同一个 `CompilationResult` 表达，不允许异常路径旁路发布。

## 3. Maven 与包依赖边界 {#arc-module-boundary}

```text
dec-core-context <------ dec-core-compiler
       ^                        ^
       |                        |
dec-context-config-parse-xml ---+
dec-context-config-parse-yaml --+
       ^
       |
dec-core-starter
       ^
       |
dec-demo (fixture/tests only)
```

| 模块 | 责任 | 禁止依赖/行为 |
|---|---|---|
| `dec-core-compiler` | 编译 API、source graph、canonical contract、raw/symbol/diagnostic builder、pass、SemanticDigestInput，以及同一 Session 的编译与原子发布协调 | 具体 frontend 实现、DOM4J、SnakeYAML、SQL/MySQL、demo、旧 declaration runtime |
| `dec-core-context` | SourceRef、Diagnostic/Code、DefinitionKey、immutable Registry、DeferredDefinition、CompiledDefinition、DigestPair、CompiledModelSet、EngineContext、只读投影等发布态中立模型 | compiler/frontend 反向依赖、静态 current Context |
| XML/YAML frontend | 安全解析并产出 CanonicalDocumentNode | 修改 Config、执行业务语义、任意 Java 类型构造 |
| `dec-core-starter` | 组装 SourceProvider、Frontend、显式 ContextPublisher/expectedCurrent，并调用 compileAndPublish | 在 compile 返回后再次发布、持有全局 current Context、失败时替换旧 Context |
| `dec-demo` | 实际 mix fixture 与合同测试 | 被生产模块反向依赖 |

## 4. 编译主路径与失败隔离 {#arc-compile-flow}

1. `ModelCompiler.compileAndPublish` 接收显式 `PublicationRequest(expectedCurrent,publisher)` 并创建新的 `CompilationSession`；
2. SourceDiscovery 根据白名单 scheme、最大深度和最大源数量构建 `MixSourceGraph`；
3. Frontend 安全解析，每个节点保留 `SourceRef`；
4. Raw builder 产生不持有 DOM/YAML Node 的 `RawDefinitionSet`；
5. Symbol registration 先登记全部 TypedKey，再解析前向引用；
6. Ownership、common expression、ModelAccess selector 和 Deferred 边界依次校验；
7. Diagnostic 按稳定 sort key 汇总；
8. 有 ERROR 时返回失败结果，`modelSet/context` 为空；
9. 无 ERROR 时先冻结不含摘要字段的 `SemanticDigestInput`，计算 digest，再构建不可变 `CompiledModelSet` 与候选 `EngineContext`；
10. 同一协调调用通过注入的 ContextPublisher 执行一次 compare-and-set；成功后 Session 进入 `PUBLISHED` 并返回 PublishedCompilationResult，冲突或发布失败则进入 `FAILED` 并返回 FailedCompilationResult。Starter 不在返回后执行第二次发布。

重试会创建新的 `CompilationSession`，不复用可变 SymbolTable。请求级 timeout/cancel 只中止当前 Session，不改变已发布 Context。

模块依赖保持单向：`dec-core-compiler -> dec-core-context`；具体 frontend 实现依赖 compiler SPI 与 context 值对象，Starter 依赖 compiler 和所选 frontend 实现并完成注入。Context 模块不引用 compiler 的 Session、Pass、Raw 或 API；compiler 不引用具体 frontend 模块，只通过注入的 `DocumentFrontend` SPI 调用。

## 5. System Information 与 common 边界 {#arc-information-ownership}

- `InformationKey = (SystemKey owner, informationName)`；
- 普通 System expression 的每个依赖必须与 owner System 相同；
- 跨 System expression 仅允许 owner=`common`，且引用必须是完整限定 `system.information`；
- `common` 不得声明 Data、View、RuleView、ModelAccess，也不得声明非 expression Information；
- P1 解析 expression AST 与 `InformationKey` 依赖，但求值、DAG、循环和缓存属于 P3 Deferred。

## 6. ModelAccess 架构边界 {#arc-model-access}

`ModelAccessBinding` 由 `SharedModelPath sourcePath`、`ViewKey targetView`、`SystemViewSelector selector` 和 `TargetPropertyPath resolvedTarget` 组成。解析只在当前 System 声明的 View 集合内进行：

1. 精确解析 `ref@view`；
2. `ref@property` 精确匹配目标 View 的 `target-main`；
3. 未匹配时逐段、区分大小写解析 property path；
4. 缺失、非复合中间段、多候选、重复和重叠写均产生稳定 Diagnostic；
5. 禁止 root-property、模糊匹配、跨 View/跨 System 搜索和静默降级。

## 7. 发布与并发模型 {#arc-publication}

- `CompilationSession` 是请求级可变聚合，线程不共享；
- `CompiledModelSet`、Registry、DeferredRegistry、Diagnostic list 发布后不可变；
- `EngineContext` 通过构造函数接收完整模型和版本元数据；
- 同一进程可并存多个 Context；
- 发布操作以单一原子步骤替换调用方显式持有的引用；
- ContextPublisher 只操作调用方显式传入的 holder，不读取或注册全局 current Context；
- timeout、cancel、解析失败、验证失败均不产生部分成功；
- 相同规范化语义应产生相同 semanticDigest，与线程调度、文件枚举顺序无关。

## 8. 安全与资源限制 {#arc-security}

- XML：禁用 DTD、外部实体、外部 schema 下载；
- YAML：safe node loader，禁止任意 Java class/tag；
- Source：scheme 白名单、根目录约束、路径规范化后再做逃逸检查；
- 限额：最大源数量、深度、单文件大小、总字节、节点数；
- Diagnostic 不回显敏感凭据和完整文件内容；
- 所有 IO 通过 Provider 注入，生产代码不硬编码 demo 路径。

## 9. 兼容与退役 {#arc-retirement}

`CoreConfigProjection` 是由新 Registry 单向计算的只读视图，不保存第二份事实，不支持 put/remove/clear。`dec-expand-declaration` 从 Maven、ServiceLoader、反射类名、测试和文档中删除；回滚只允许 Git revert，不建立运行时双轨。

## 10. 跨模块失败策略 {#arc-cross-module-failure}

| 边界 | timeout | retry | 幂等 | 部分失败/补偿 |
|---|---|---|---|---|
| Provider→Frontend | 请求预算内 | 仅显式新 Session | sourceId+content digest | 无外部写，失败返回 Diagnostic |
| Frontend→Compiler | 同步请求预算 | 不在同 Session 自动重试 | Canonical digest | RawSet 不发布 |
| Compiler→ContextPublisher | 不允许阻塞外部 IO | 重建新 Session | semanticDigest + expectedCurrent | ERROR 不调用 Publisher；CAS 失败保留旧 Context并返回 FAILED |
| Starter→Compiler | 同步协调调用 | caller 可重新提交新 request | 显式 holder/version | 只注入依赖，不在 compile 返回后再次发布 |

## 11. 架构验证 {#arc-verification}

- ArchUnit/Maven dependency tree 验证模块依赖；
- 并发测试验证 Session 隔离和 deterministic digest；
- 负向安全 fixture 验证 XML/YAML/path 限制；
- publication 测试验证 ERROR/timeout/cancel 均不替换旧 Context；
- 仓库扫描验证无 `dec-expand-declaration`、Adapter、静态 current Context 和第二 Registry。
