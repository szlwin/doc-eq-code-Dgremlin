# COMPILER P2 API 契约

> Revision：`DESIGN-P2-R01@8875f042898c`。Base：`DESIGN-R05@0b37a9b4dd48`。输入业务模型：`BM-R07@7d7bf504ca9d`。
> 本文冻结 P2 所需的公共/跨模块契约形状，不包含具体实现代码；现有 P1 API 未在本文声明变更的部分保持兼容。

## 1. 复用的现有身份

- `SystemKey(String name)`：继续作为唯一 System identity；canonical 规则保持 P1 不变。
- `RuleViewKey(SystemKey owner,String name)`：继续作为唯一 RuleView identity；不新增 bare-name key。
- `TypedDefinitionRegistries.systems()/ruleViews()`：继续存在且只读。

## 2. CompiledSystem

| 字段 | 类型 | 约束 |
|---|---|---|
| key | `SystemKey` | 必填、显式声明来源 |
| sourceRef | `SourceRef` | 必填 |
| dataKeys | immutable `List<DataKey>` | canonical sort |
| viewKeys | immutable `List<ViewKey>` | canonical sort |
| ruleViewKeys | immutable `List<RuleViewKey>` | owner 必须等于 key |
| informationKeys | immutable `List<InformationKey>` | owner 必须等于 key/common 规则另行保持 |
| accessRuleKeys | immutable list | 只引用同一 Context 发布闭包 |

所有集合构造时防御性复制；对象按全部语义字段实现 value semantics。

## 3. ModelPath

`ModelPath(targetKey, segments)`：targetKey 必填，segments 非空或明确 root 表示；segment 已完成 exact compile，不允许运行时 fuzzy lookup。`canonical()` 必须稳定、可比较并纳入 semantic digest。

## 4. AccessOperation

闭集：`READ`、`WRITE`、`EXECUTE`。序列化值稳定且大小写固定；增加新值属于显式兼容变更，不得将 READ/WRITE/EXECUTE 解释为权限等级。

## 5. ModelAccessRule

`ModelAccessRule(systemKey,targetKey,modelPath,operation,sourceRef,decisionRequirement)`。

`decisionRequirement` 只允许：

- `STATIC_ALLOW`
- `RUNTIME_GUARD_REQUIRED`

静态 DENY 不得构造发布对象；必须返回 compile Diagnostic 并阻断候选。

## 6. Policy index

`ModelAccessPolicyIndex.find(SystemKey, DefinitionKey, ModelPath, AccessOperation)` 返回 `Optional<ModelAccessRule>` 或等价 typed lookup。未命中代表未授权，不触发 wildcard、parent path、同名 target 或跨 System fallback。

## 7. RuleViewResolver

`resolve(SystemKey systemKey, String ruleViewName)` 或 `resolve(RuleViewKey key)` 返回 typed result/Optional。禁止新增 `resolve(String bareName)` 到 P2 新 API。unknown 必须能产生/关联稳定 `MIX-RULEVIEW-UNKNOWN` 事实。

## 8. Runtime Guard

| 契约 | 输入 | 输出 | 约束 |
|---|---|---|---|
| `ModelAccessGuard.authorize` | `ModelAccessRequest` | `ModelAccessDecision` | 无副作用，fail-closed |
| `ModelAccessRequest` | Context identity、SystemKey、targetKey、ModelPath、AccessOperation、runtime facts | immutable | 不从 global current 补值 |
| `ModelAccessDecision` | `ALLOW|DENY`、reason code、policy/source ref（适用） | immutable | DENY 在 mutation 前返回 |
| `RuntimeFactEvaluator` | 已声明 runtime requirement + request facts | ALLOW/DENY | exception/null/timeout -> DENY |

## 9. EngineContext P2 读取面

EngineContext/CompiledModelSet 必须能在不复制事实的前提下提供：System registry、RuleView registry、policy index 和 composite resolver。所有结果来自同一个 `CompiledModelSet`，不同 Context 不共享可变 registry/cache。

## 10. 兼容 API

现有 `ConfigInfo.getRuleViewInfo(String)`、`DataUtil.getRuleViewInfo(String)` 等裸名称方法视为 legacy read-only surface；P2 不将其删除（P7 边界），但禁止新 Compiler/Starter/执行路径依赖这些方法完成 composite lookup，也禁止它们写入新 P2 registry。

## 11. 失败契约

Compile failure 使用稳定 Diagnostic；Runtime Guard 使用稳定 DENY result。二者都必须携带适用的 System/RuleView/operation/path/SourceRef 定位信息，不能返回 null-success、吞异常或只打印日志。
