# DEC_COMPILER 详细设计

> Revision：DESIGN-R02-DRAFT。该设计以用户提供的实际 `mix` 文件为输入事实，仍需 DesignReviewAgent、ArchitectureReviewAgent、TestDesignAgent、ImpactAnalysisReviewAgent 和 CrossModuleIntegrationReviewAgent 串行 Review。

## 1. 设计目标

建立一个不依赖旧全局 Config 写入、不会形成第二套 Business runtime、能够完整发现实际 `mix` 源图的编译骨架。

## 2. 包建议

```text
dec.core.compiler.api
  DocumentSource, DocumentFrontend, Compiler, CompilationResult

dec.core.compiler.source
  MixSourceResolver, MixSourceGraph, SourceEdge, SourceManifest

dec.core.compiler.canonical
  CanonicalDocumentNode, CanonicalScalar, SourceRef

dec.core.compiler.raw
  RawDefinitionSet, Raw*Definition

dec.core.compiler.symbol
  DefinitionKey, SymbolTableBuilder, RegistryBuilder

dec.core.compiler.pass
  CompilerPass, PassContext, PassResult

dec.core.compiler.compiled
  CompiledModelSet, Compiled*Definition, DeferredDefinition

dec.core.compiler.diagnostic
  Diagnostic, DiagnosticCode, DiagnosticCollector

dec.core.context
  EngineContext, Registry, CoreConfigProjection
```

## 3. Source API

```java
public interface DocumentSource {
    String sourceId();
    DocumentFormat format();
    byte[] content();
    SourceOrigin origin();
}

public interface DocumentSourceProvider {
    DocumentSource resolve(SourceReference reference, SourceResolutionContext context);
    List<DocumentSource> resolveFileSet(SourceReference reference, SourceResolutionContext context);
}
```

生产代码不得读取固定的 `dec-demo/src/main/resources/mix`。测试通过 classpath Provider 指向该 fixture。

## 4. MixSourceResolver

### 4.1 输入

- root SourceReference；
- Provider；
- allowed schemes；
- max source count/depth；
- strictness options。

### 4.2 算法

1. 解析 root，并读取 datasource、connection、data/view file set、system-file、business-file；
2. 标准化所有 SourceReference；
3. 展开 data/view file set，按 sourceId 排序；
4. 解析 System 文件的最低限度结构，提取每个 System 的 rule-file；
5. 解析并加入 Rule 文件；
6. 去重并检测同一 sourceId 内容冲突；
7. 形成 `MixSourceGraph` 与稳定 `SourceManifest`；
8. 所有文档再统一进入 frontend/Raw build。

### 4.3 约束

- 文件发现图允许未来扩展，但 P1 只允许已注册边类型；
- 未知 file-info 节点在严格模式报错；
- 路径逃逸、循环引用、超过深度/数量限制报错；
- 发现阶段只提取文件引用，不注册业务定义。

## 5. CanonicalDocumentNode

字段：

```text
nodeName
orderedAttributes
optionalScalar
orderedChildren
sourceRef
format
schemaVersion
```

XML/YAML 差异只保留在 format/sourceRef；业务构建规则不写在 frontend 中。

## 6. RawDefinitionSet

```text
RawRootConfigDefinition
RawDataSourceDefinition
RawConnectionDefinition
RawDataDefinition
RawViewDefinition
RawSystemDefinition
RawRuleViewDefinition
RawRuleDefinition
RawBusinessScopeDefinition
RawInformationDefinition
RawDirectoryDefinition
RawActionDefinition
RawProduceDefinition
```

所有 RawDefinition 保存 SourceRef、owner、声明顺序和规范化属性。不得引用 DOM Element 或 SnakeYAML Node。

## 7. Key 与符号注册

```java
final class RuleViewKey {
    private final SystemKey system;
    private final String name;
}

final class InformationKey {
    private final BusinessScopeKey scope;
    private final String name;
}
```

注册阶段先注册所有顶层 Key，再注册 owner-scoped 子定义。ActionKey 为 `(DirectoryKey, actionName)`。无名称 Produce 使用 `(ActionKey, sourceOrdinal)`。

## 8. 引用解析

### 8.1 P1 必须解析

- connection→datasource；
- view→data/子属性结构；
- system→data、view；
- system source edge→rule file；
- ruleView→system、view；
- business action→system、ruleView；
- information→system、view/ruleView；
- directory→information；
- subdirectory→directory；
- produce→information（存在时）。

### 8.2 P1 不执行

- ModelAccess 路径权限；
- Information expression/rule-data/change-data；
- Rule grammar；
- Directory 执行、case 查询、back；
- Action/Produce 调用和事务。

这些内容进入 DeferredDefinitionRegistry。

## 9. DeferredDefinition

```java
public final class DeferredDefinition {
    DefinitionKey ownerKey;
    DeferredKind kind;
    RequiredStage requiredStage;
    String reasonCode;
    SourceRef sourceRef;
    NormalizedBody body;
    List<DefinitionKey> resolvedReferences;
}
```

requiredStage 使用 P2_SYSTEM、P3_INFORMATION、P4_ACTION_PRODUCE、P5_DIRECTORY、P6_QUERY、P7_RUNTIME。P1 完成时不得出现 `UNKNOWN_STAGE`。

## 10. Compiler Pass

| 顺序 | Pass | 失败行为 |
|---|---|---|
| 1 | SourceGraphValidationPass | 源缺失/冲突阻断 |
| 2 | StructuralValidationPass | 聚合结构错误 |
| 3 | SymbolRegistrationPass | 重复 Key 聚合 |
| 4 | ReferenceResolutionPass | 未知/类型不匹配聚合 |
| 5 | OwnershipValidationPass | RuleView System、Business owner 校验 |
| 6 | GraphPreparationPass | 构建依赖图，不执行语义 |
| 7 | DeferredClassificationPass | requiredStage 完整性校验 |
| 8 | P1SemanticValidationPass | P1 支持实体不变量 |
| 9 | PublicationPass | 仅无 ERROR 时发布 |

## 11. CompiledModelSet

```java
public final class CompiledModelSet {
    SourceManifest sources;
    Registry<DataSourceKey, CompiledDataSourceDefinition> dataSources;
    Registry<ConnectionKey, CompiledConnectionDefinition> connections;
    Registry<DataKey, CompiledDataDefinition> data;
    Registry<ViewKey, CompiledViewDefinition> views;
    Registry<SystemKey, LinkedSystemDefinition> systems;
    Registry<RuleViewKey, LinkedRuleViewDefinition> ruleViews;
    Registry<BusinessScopeKey, CompiledBusinessScopeDefinition> businessScopes;
    Registry<InformationKey, LinkedInformationDefinition> information;
    Registry<DirectoryKey, LinkedDirectoryDefinition> directories;
    DeferredDefinitionRegistry deferred;
    String semanticDigest;
}
```

名称中的 Linked 表示引用已类型化，但后续业务语义尚未执行。

## 12. EngineContext

EngineContext 只通过构造函数接收完整 CompiledModelSet 和版本信息；无 public mutator、无静态 current、无隐式注册。

新 Context 构建失败时，starter 不替换当前调用方显式持有的旧 Context。

## 13. CoreConfigProjection

只覆盖仍被旧核心读取的 Data/View/Rule 基础视图。设计规则：

- 由 EngineContext 即时/不可变计算；
- 不实现旧 register/remove/clear；
- 写调用抛 `UnsupportedOperationException` 或专用错误；
- 不包含 SystemDesc、BusinessDesc、Producer、Consumer 或 declaration 类型；
- 后续调用迁移完成后删除。

## 14. Diagnostic

建议错误码：

```text
MIX-SRC-001 UNKNOWN_SOURCE_SCHEME
MIX-SRC-002 SOURCE_NOT_FOUND
MIX-SRC-003 DUPLICATE_SOURCE_ID
MIX-SRC-004 PATH_ESCAPE
MIX-SRC-005 SOURCE_CYCLE
MIX-STRUCT-001 UNKNOWN_ELEMENT
MIX-STRUCT-002 MISSING_REQUIRED_ATTRIBUTE
MIX-SYMBOL-001 DUPLICATE_KEY
MIX-REF-001 UNKNOWN_REFERENCE
MIX-REF-002 REFERENCE_TYPE_MISMATCH
MIX-REF-003 RULE_SYSTEM_MISMATCH
MIX-DEFER-001 MISSING_REQUIRED_STAGE
MIX-PUBLISH-001 ERROR_PREVENTS_PUBLICATION
```

排序键：sourceId、line、column、code、entityKey、pass。

## 15. Digest

- sourceDigest：标准化 sourceId + 原始字节摘要；
- semanticDigest：规范化 CompiledModelSet + Deferred 元数据，不含 SourceRef 的物理位置；
- compilerVersion、schemaVersion、optionsDigest 进入 CompilationResult；
- 使用有序集合和稳定序列化，禁止依赖 HashMap 遍历顺序。

## 16. `dec-expand-declaration` 退役

P1 第一实施任务：

1. 从根 POM 删除 module；
2. 删除 dependency/dependencyManagement；
3. 删除 `dec-expand-declaration/`；
4. 删除 `dec-demo` 对 artifact 的依赖；
5. 必要场景仅使用 `mix` fixture 和新 compiler contract 重写；
6. 扫描 package/import/ServiceLoader/反射字符串/文档/artifact；
7. 禁止创建任何 Adapter 或复制类。

## 17. 实施顺序

1. P1-T01 退役临时模块；
2. P1-T02 建立残留扫描和架构测试；
3. P1-T03 DocumentSource/SourceRef；
4. P1-T04 MixSourceGraph；
5. P1-T05 Canonical frontend contract；
6. P1-T06 RawDefinitionSet；
7. P1-T07 Key/SymbolTable；
8. P1-T08 Diagnostic；
9. P1-T09 Reference resolution；
10. P1-T10 Deferred Registry；
11. P1-T11 CompiledModelSet/EngineContext；
12. P1-T12 CoreConfigProjection；
13. P1-T13 XML frontend；
14. P1-T14 YAML minimal parity；
15. P1-T15 实际 `mix` contract 与完整门禁。

## 18. 设计禁止事项

- 不得使用 `CompiledBusiness`、`RawDeclaration`、LegacyDeclarationAdapter 作为新设计类型；
- 不得把 BusinessScope 变为 Maven 模块；
- 不得硬编码 demo fixture；
- 不得让 DeferredDefinition 无期限或无 requiredStage；
- 不得在 parser 中执行注册或业务规则；
- 不得以复制临时模块代码作为实现捷径。
