# DESIGN-R33 — TASK-P1-T10 ModelAccess 精确 Selector

- Revision: `DESIGN-R33@P1-T10-I001`
- Status: `PASSED`
- Base: `dev_all@4fe0f6def8581e5c7234d86dfa0aafae794db15f`
- Dependency: `COMPLETION-P1-T09-R02@95b08223083f`
- Branch/PR: `feature/p1-t10-rule-dag-20260804-1428` / `#25`
- Owner: `dec-core-compiler / dec.core.compiler.modelaccess`
- Mode: `SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 1. Goal

T10 将 T06 `RawDefinitionKind.MODEL_ACCESS` 与 T07 `SymbolTable` 编译为不可变 `ModelAccessBinding`，严格分离共享模型源路径与当前 System 本地 View 目标 selector。P1 只解析结构与强类型引用，生成 `RequiredStage.P2` 的完整 Deferred；不判断运行时权限、不执行访问、不生成 SQL。

## 2. Input identity

`ModelAccessCompiler.compile(RawDefinitionSet, SymbolTable)` 必须在任何 owner、View、selector 或 body 解析前调用 `SymbolTable.isBuiltFrom`。输入失配只返回一个 `modelaccess.input.snapshot-mismatch` ERROR，不调用 selector resolver，不发布部分 Binding 或 Deferred。

## 3. Value objects

```text
SharedModelPath      := exact source path segments | "*"
SystemViewSelector   := exact case-sensitive selector segments
TargetPropertyPath   := TARGET_MAIN(selector) | PROPERTY_PATH(segments)
ModelAccessBinding   := ownerSystem + sourceModel + sourcePath + accessMode
                        + targetView + selector + resolvedTarget + sourceRef
```

所有对象 Java 8 不可变，基于全部语义字段实现 equals/hashCode/toString；集合防御复制并稳定排序。`@Override` 独占一行，方法和重要逻辑使用中文注释。

## 4. Exact selection algorithm

1. owner lexical 必须可构造 `SystemKey`，`model-ref` 必须精确命中 `ViewKey`。
2. `ref@view` 必须精确命中当前 System `view-info` 中声明的 View；不跨 System 或其它 View 搜索。
3. `ref@property` 先与目标 View `target-main` 做区分大小写完整匹配。
4. 命中 target-main 时直接发布 `TargetPropertyPath.TARGET_MAIN`，即使同名 property path 存在也不得降级。
5. 未命中 target-main 时按 `.` 分段，在同一 View `property-info` 树中逐段按 property `name` 精确匹配。
6. 每段 0 个候选为 `MIX-MODEL-ACCESS-NOT-FOUND`；多个候选为 `MIX-MODEL-ACCESS-AMBIGUOUS`；非末段候选没有 property 子节点为 `MIX-MODEL-ACCESS-NON-COMPOSITE`。
7. 禁止大小写折叠、前后缀、模糊、root-property、跨 View/System 回退。

## 5. Batch rules

- 一个 read/write 可包含多个 ref，每个 ref 独立解析。
- 完全重复 binding 拒绝。
- 同一 ModelAccess 内 WRITE source path 相同、祖先或后代关系均视为重叠，返回 `MIX-MODEL-ACCESS-AMBIGUOUS`。
- 失败完整聚合、去重、稳定排序；任一 ERROR 不发布部分 Compilation。
- 没有 ref 的 read/write 保留在 normalized Deferred body，但不制造伪目标 Binding。

## 6. Deferred

每个成功的 Raw ModelAccess 生成一个：

```text
DeferredKind.MODEL_ACCESS
RequiredStage.P2
reasonCode = model-access-selector-binding
body.format = model-access-binding/v1
resolvedReferences = source ViewKey + 全部 target ViewKey（去重、稳定排序）
```

Binding 按 owner、source model、mode、source path、target view、selector 稳定排序。Deferred ordinal 使用同一 owner 下 Raw ModelAccess 的稳定出现顺序。

## 7. Diagnostics

- `modelaccess.input.snapshot-mismatch` → `MIX-MODEL-ACCESS-AMBIGUOUS`
- `modelaccess.owner.invalid` → `MIX-MODEL-ACCESS-NOT-FOUND`
- `modelaccess.source-view.not-found` → `MIX-MODEL-ACCESS-NOT-FOUND`
- `modelaccess.view.not-declared` → `MIX-REF-VIEW-NOT-DECLARED`
- `modelaccess.selector.not-found` → `MIX-MODEL-ACCESS-NOT-FOUND`
- `modelaccess.selector.ambiguous` / `modelaccess.binding.duplicate` / `modelaccess.write.overlap` → `MIX-MODEL-ACCESS-AMBIGUOUS`
- `modelaccess.selector.non-composite` → `MIX-MODEL-ACCESS-NON-COMPOSITE`
- malformed path/ref/body → stable fail-closed Diagnostic，不抛出调用方输入异常。

## 8. Fixture closure

当前 payment fixture 同时声明 WRITE `payInfo` 与 `payInfo.payDetailList`，属于祖先/后代重叠。T10 将删除冗余后代 WRITE，保留 `payInfo`，并由负向 Oracle 单独证明重叠会阻断。

## 9. TDD gate

有效 RED 必须 Java 8 编译、errors=0，既有 351 个正常测试保持通过，新失败只来自 T10 Oracle。Architecture Skeleton 只建立 API/value-object/result seam，不接入真实 resolver/compiler，保持受控 RED。GREEN 后运行 12 模块 `clean verify`、故意失败门禁和 Artifact 独立 SHA/Surefire 解析。

## 10. Scope

允许：

- `dec-core-compiler/src/main/java/dec/core/compiler/modelaccess/**`
- `dec-core-compiler/src/test/java/dec/core/compiler/modelaccess/**`
- 两份 `mix/system/systems.xml` 的重叠 WRITE 修复
- T10 文档、Review、Evidence、Completion 与恢复入口

禁止修改 Context、T06/T07/T08/T09 公共合同、Compiler API、其它 fixture 语义；禁止实现 T11/P2 执行、权限、SQL、缓存、I/O、网络、全局状态或模糊选择。
