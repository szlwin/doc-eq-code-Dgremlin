# DESIGN-R36 — TASK-P1-T11 P2-P7 Deferred 分类

- Revision：`DESIGN-R36@P1-T11-I001`
- Status：`PASSED`
- Base：`dev_all@f97b7e47ac0fb40209c4dc512aa15d67c19be44b`
- Dependency：`COMPLETION-P1-T10-R03@336d309f3748`
- Owner：`dec-core-compiler / dec.core.compiler.deferred`

## 1. 目标

将 P1 已解析但由后续阶段执行的 System permission、ModelAccess、Information、Action、Produce、Directory、Query 和 Transaction 统一登记为完整、不可变、稳定排序的 `DeferredDefinition`。P1 只分类和冻结强类型引用，不执行 P2-P7 runtime。

## 2. 稳定映射

| DeferredKind | RequiredStage | reasonCode |
|---|---|---|
| `SYSTEM_PERMISSION` | `P2` | `system-permission-evaluation` |
| `MODEL_ACCESS` | `P2` | `model-access-selector-binding` |
| `INFORMATION` | `P3` | `information-expression-evaluation` |
| `ACTION` | `P4` | `action-execution` |
| `PRODUCE` | `P4` | `produce-execution` |
| `DIRECTORY` | `P5` | `directory-evaluation` |
| `QUERY` | `P6` | `query-planning` |
| `TRANSACTION` | `P7` | `transaction-execution` |

`DeferredClassificationPolicy` 是无状态、封闭的映射来源；不得由调用方覆盖 requiredStage，也不得接受与 kind 不一致的 reasonCode。

## 3. 分类输入

`DeferredClassificationInput` 是分类前请求，不是可发布模型。它使用 Builder 收集：

- `ownerKey`：强类型 `DefinitionKey`；
- `kind`；
- `ordinal >= 0`；
- `reasonCode`；
- `SourceRef`；
- `NormalizedBody`；
- `resolvedReferences`：强类型 `DefinitionKey` 列表，允许显式空列表；
- `unresolvedReferences`：尚未类型化的 lexical 列表，成功输入必须为空。

Builder 可以形成不完整请求，以便分类器把缺字段统一转换为 Diagnostic；不完整请求绝不能进入 Context Registry。

## 4. 完整性门禁

`DeferredDefinitionBuilder` 对整个输入批次执行：

1. 校验输入列表与元素非空；
2. 校验 owner、kind、ordinal、reason、SourceRef、body、resolvedReferences 均存在；
3. reasonCode 必须与 Policy 的稳定值一致；
4. resolvedReferences 不得包含 null；
5. unresolvedReferences 必须为空，禁止把原始字符串伪装成已解析引用；
6. 构造 `DeferredKey(owner, kind, ordinal)` 与 `DeferredDefinition`；
7. 拒绝重复 `DeferredKey`；
8. 任一错误返回 `FAILED`、稳定排序的 `MIX-DEFERRED-INCOMPLETE`，且 Registry 缺席；
9. 全部成功才一次性发布 `ImmutableDeferredRegistry`。

## 5. Diagnostic

- code：`MIX-DEFERRED-INCOMPLETE`
- severity：`ERROR`
- pass：`DeferredClassificationPass`
- messageKey：`deferred.incomplete.<field>` 或 `deferred.incomplete.duplicate-key`
- 缺 SourceRef 时使用稳定内部位置 `<deferred>:0:0#/classification`，不得抛出 null 异常替代业务 Diagnostic。

## 6. 不可变与确定性

- 输入列表和引用列表均防御性复制；
- Diagnostic 与 Registry key 稳定排序；
- 输入顺序变化不改变 Registry；
- 成功结果与失败结果互斥；
- 无 static/thread-local 可变状态；
- 不新增 Context 公共字段，不修改 T06-T10 公共合同。

## 7. 禁止范围

不得实现或调用权限判断、Information evaluator、Action/Produce executor、Directory engine、Query planner、SQL、Transaction manager、DAG、缓存、I/O、网络、线程或发布逻辑。不得新增原始字符串引用到 `DeferredDefinition`。

## 8. TDD Oracle

1. 八种 kind 全覆盖且 P2-P7 分组准确；
2. owner、kind、stage、reason、SourceRef、body、typed references 完整；
3. 输入乱序仍生成相同 Registry；
4. 缺 owner/kind/ordinal/reason/SourceRef/body/resolvedReferences 分别失败；
5. reason 不匹配、null typed ref、unresolved lexical、重复 key 分别失败；
6. 任一失败不发布部分 Registry；
7. 生产类不存在 runtime 执行入口或静态可变状态；
8. Java release 8、全 Reactor 和既有 T01-T10 回归通过。
