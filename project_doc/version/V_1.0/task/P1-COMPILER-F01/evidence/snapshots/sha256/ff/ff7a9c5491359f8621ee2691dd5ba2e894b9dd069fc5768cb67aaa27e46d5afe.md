# P1-COMPILER-F01 概念模型

> Revision：BM-R02-DRAFT。基于实际 `mix` fixture 重建。

## 1. 统一语言

| 术语 | 定义 |
|---|---|
| Mix Root | 一次配置装配的根文档，当前 fixture 为 `orm-config.xml` |
| DocumentSource | 具有稳定 sourceId、format、content 和来源关系的输入 |
| MixSourceGraph | 根、目录集合、显式文件和 System Rule 文件组成的有类型源图 |
| CanonicalDocumentNode | 与 XML/YAML 无关的有序节点树 |
| RawDefinition | 从 Canonical 节点构建、保留 SourceRef 和未解析语义体的定义 |
| RawDefinitionSet | 一次 Session 的全部 RawDefinition |
| DefinitionKey | 区分实体命名空间的强类型 Key |
| BusinessScope | Business 配置的逻辑命名空间，不是独立模块或 runtime |
| DeferredDefinition | P1 已保存结构和可解析引用、但由后续阶段实现语义的定义 |
| CompiledModelSet | P1 成功发布的不可变模型集合 |
| EngineContext | 持有 CompiledModelSet、Registries、Diagnostics 摘要和版本信息的实例级上下文 |
| CoreConfigProjection | 从 EngineContext 计算的旧核心只读读取视图 |

## 2. 聚合

### CompilationSession

聚合根。拥有 SourceGraph、RawDefinitionSet、SymbolTableBuilder、DiagnosticCollector、PassState，不与其他 Session 共享可变状态。

### CompiledModelSet

成功发布聚合。包含：

- SourceManifest；
- DataSourceRegistry；
- ConnectionRegistry；
- DataRegistry；
- ViewRegistry；
- SystemRegistry（结构层）；
- RuleViewRegistry（结构层）；
- BusinessScopeRegistry；
- InformationRegistry（结构层）；
- DirectoryRegistry（结构层）；
- DeferredDefinitionRegistry；
- semanticDigest。

## 3. 强类型 Key

```text
DataSourceKey(name)
ConnectionKey(name)
DataKey(name)
ViewKey(name)
SystemKey(name)
RuleViewKey(systemKey, name)
BusinessScopeKey(name)
InformationKey(businessScopeKey, name)
DirectoryKey(businessScopeKey, name)
ActionKey(directoryKey, name)
```

Produce 没有业务名称时使用 `ProduceId(actionKey, sourceOrdinal)`，只在同一已标准化文档内稳定，不伪造全局名称。

## 4. 关系

- MixRoot 引用 Data/View 文件集合、System 文件和 Business 文件；
- System 引用 Data、View 和 Rule 文件，并声明 ModelAccess；
- RuleView 归属于一个 System 并引用一个 View；
- BusinessScope 包含 Information 和 Directory；
- Directory 通过 information-ref 关联 Information；
- Action 可引用 System + RuleView；
- Produce 可引用模型或上下文数据，并可关联 Information。

## 5. 不变量

1. sourceId 在一个 Session 内唯一；
2. DefinitionKey 在对应 Registry 内唯一；
3. RuleViewKey 的 System 与源发现边上的 System 一致；
4. BusinessScope 不拥有 EngineContext；
5. ERROR 存在时不得产生 CompiledModelSet；
6. 已发布 Registry 不可修改；
7. DeferredDefinition 必须有 requiredStage，不能静默丢失；
8. CompiledModelSet 不持有 parser 节点；
9. CoreConfigProjection 不成为事实源；
10. `dec-expand-declaration` 不出现在模型、API 或依赖中。

## 6. 状态与事件

### CompilationSession 状态

`CREATED → SOURCES_DISCOVERED → PARSED → RAW_BUILT → STRUCTURALLY_VALIDATED → SYMBOLS_REGISTERED → REFERENCES_RESOLVED → GRAPH_PREPARED → SEMANTICALLY_VALIDATED → PUBLISHED|FAILED`

### 领域事件

- SourceDiscovered；
- DefinitionRegistered；
- ReferenceResolved；
- DefinitionDeferred；
- DiagnosticAdded；
- ModelSetPublished；
- CompilationFailed。

这些事件是可观测测试接缝，不要求在 P1 建立事件总线。
