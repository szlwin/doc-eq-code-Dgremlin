# DESIGN-PLAN-P1-STAGE-CLOSURE-R01

## 一、需求一致性决定

本迭代不变更已确认 P1 需求，也不取消 Starter 生产组装职责。T15 I001/I002 的实例级 `CompilerStarter` 和 Runtime Retirement 作为低层委托与退役成果保留；新增 `CompilerBootstrap` 完成原计划尚未交付的产品入口。

## 二、职责边界

### dec-core-compiler

- 提供生产 `ClasspathDocumentSourceProvider`；
- 提供固定十阶段标准 Pass 组装与 `ModelCompiler` 适配；
- Pipeline 继续独占最终 Publisher 调用；
- 成功结果必须返回 Publisher 实际接收的同一个 `EngineContext` 实例。

### dec-core-starter

- 组装 classpath Provider、安全 SourcePolicy、XML/YAML FrontendRegistry、标准 ModelCompiler、Clock、Observer 和 Publisher；
- 对外提供 `root + CompilationOptions + expectedCurrent` 的一键 compile-and-publish；
- Projection 只读取 `PublishedCompilationResult.engineContext().projection()`。

## 三、固定 Pass 数据流

```text
SourceGraphValidationPass
  -> MixSourceGraph
StructuralValidationPass
  -> XML/YAML Canonical roots + RawDefinitionSet
SymbolRegistrationPass
  -> SymbolTable
ReferenceResolutionPass
  -> ResolvedReferenceSet
InformationOwnershipPass
  -> InformationCompilation
ModelAccessBindingPass
  -> ModelAccessCompilation
DeferredClassificationPass
  -> DeferredRegistry
P1SemanticValidationPass
  -> Immutable CompiledDefinition Registry
DigestPass
  -> DigestBoundCompiledInput
PublicationPass
  -> candidate EngineContext -> CAS publish
```

## 四、安全与失败合同

- Source 路径必须位于显式 `AllowedRoot`，只接受 `classpath` scheme；
- classpath 文件集枚举在目录和 jar 两种部署形态下稳定排序；
- 未知协议、不可读资源、空文件集和重复 sourceId 均 fail-closed；
- 任一阶段 ERROR 或异常终止后续 Pass；
- 发布前所有候选事实绑定同一 Source/Schema/Options/Digest 闭包；
- 失败不得调用 Publisher；CAS 冲突返回 FAILED；
- 不引入 static current Context、ServiceLoader 或反射组装。

## 五、TDD 计划

1. RED：新增真实生产 Bootstrap 测试，因 `CompilerBootstrap` 缺失在 testCompile 阶段失败；
2. GREEN：实现 Provider、标准 Compiler、Bootstrap 和真实 XML/YAML mix fixture；
3. Regression：完整 P0、T14 mutation、failure gate、T15 retirement gate；
4. MySQL：通过 workflow_dispatch 或用户本地 Java 17 + MySQL 8.0 独立验证；
5. Review：检查依赖方向、发布 identity、失败不污染、中文注释及 `@Override` 独占一行。
