# DEC_COMPILER 业务模型

> Revision：BM-R02-DRAFT。本文已基于实际 `mix` fixture 重写，待串行 Review。

## 1. 模块使命

DEC_COMPILER 把一组配置文档编译为不可变、可追踪、可复现的 `CompiledModelSet`。它不执行业务流程，不拥有数据库连接，不构造第二套 Business runtime。

## 2. 领域对象

| 对象 | 职责 |
|---|---|
| MixSourceGraph | 保存配置源及发现边 |
| CanonicalDocument | 保存格式中立节点和 SourceRef |
| RawDefinitionSet | 保存所有配置定义 |
| SymbolTable | 保存构建期强类型符号 |
| DeferredDefinitionRegistry | 保存后续阶段语义及 requiredStage |
| CompiledModelSet | 不可变发布产物 |
| EngineContext | 实例级运行时读取入口 |
| CoreConfigProjection | 旧核心只读投影 |

## 3. 实际 `mix` 映射

| XML 结构 | RawDefinition | P1 结果 |
|---|---|---|
| `orm-datasource` | RawDataSourceDefinition | CompiledDataSourceDefinition |
| `orm-connection` | RawConnectionDefinition | CompiledConnectionDefinition |
| `data` | RawDataDefinition | CompiledDataDefinition |
| `view` | RawViewDefinition | CompiledViewDefinition |
| `system` | RawSystemDefinition | LinkedSystemDefinition + deferred ModelAccess |
| `rule-view-info` | RawRuleViewDefinition | LinkedRuleViewDefinition + deferred execution |
| `business-config` | RawBusinessScopeDefinition | CompiledBusinessScopeDefinition |
| `information` | RawInformationDefinition | LinkedInformationDefinition + deferred evaluation |
| `directory` | RawDirectoryDefinition | LinkedDirectoryDefinition + deferred state machine |
| `action` | RawActionDefinition | LinkedActionDefinition + deferred execution |
| `produce` | RawProduceDefinition | LinkedProduceDefinition + deferred execution |

## 4. 服务

- SourceDiscoveryService；
- CanonicalizationService；
- RawDefinitionBuilder；
- SymbolRegistrationService；
- ReferenceResolutionService；
- DeferredClassificationService；
- ModelPublicationService。

## 5. 不变量

- 编译失败不发布；
- 同一 Key 不覆盖；
- 前向引用在注册完成后解析；
- BusinessScope 仅为命名空间；
- Deferred 必须可追踪；
- EngineContext 不全局共享可变状态；
- 废弃模块无任何模型或依赖入口。
