# DEC_COMPILER Design R09 — TASK-P1-T02 REWORK I002

## 1. 修订原因

`TASK-P1-T01` 的最终合同已通过 PR #16 合并到 `dev_all@f88f45731e16868bfacb489b63e3086aae49d018`。旧 T02 Completion `COMPLETION-P1-T02-R01@643b44a8b72a` 基于 `dev_all@de96e2e521f9f3ac0dc6919d0bd3f9b14bf5b836`，其测试夹具仍使用旧 `CompiledModelSet` 构造合同，且 `PublishedCompilationResult` 允许把值相等但并非同一发布聚合的 `CompiledModelSet` 与 `EngineContext` 组合。

本修订创建新 iteration `TASK-P1-T02/I002`，旧 Completion、Review 与 Evidence 仅作为历史保留，不再作为当前通过结论。

## 2. 设计目标

1. 在最新 `dev_all` 上恢复并验证 T02 公共 Compiler API；
2. 保持 Compiler 模块只依赖 `dec-core-context`，不得形成 Context → Compiler 反向依赖；
3. 保持 `ModelCompiler.compileAndPublish(...)` 为唯一公共编译入口；
4. 使成功结果成为 T01 完整发布事实的只读包装，不允许重新拼接聚合；
5. 保持失败结果不暴露候选模型、Context、Digest 或版本事实；
6. 保持 Java release 8 兼容。

## 3. 发布结果不变量

### INV-T02-R09-001 精确模型身份

`PublishedCompilationResult` 同时接收 `CompiledModelSet` 与 `EngineContext` 时，必须满足：

```text
compiledModelSet == context.compiledModelSet()
```

不得以 `equals(...)` 代替对象身份。原因是 Publisher 实际暴露的是一个确定的 `EngineContext` 候选；允许使用另一个值相等模型会重新引入“调用方自由组合发布聚合”的入口。

### INV-T02-R09-002 Diagnostics 单一事实源

成功结果的 diagnostics 必须与 `compiledModelSet.diagnostics()` 完全一致。构造参数保留用于兼容既有 T02 API，但只能验证并复用模型中的不可变 diagnostics，不能形成第二份独立发布事实。

失败结果仍要求 diagnostics 非空且至少包含一个 `ERROR`，并进行稳定排序与防御性复制。

### INV-T02-R09-003 Context 派生闭包

`EngineContext` 继续由 `new EngineContext(compiledModelSet)` 构造，其 Projection 只能由同一模型确定性派生。T02 不提供接收独立 Projection 的入口。

## 4. API 兼容策略

- 保留旧 T02 已公开的类型名、方法名和构造参数；
- 仅收紧非法聚合的构造条件；
- `PublishedCompilationResult` 的合法调用方无需迁移；
- 旧调用方若传入值相等但不同实例的模型，或传入与模型不一致的 diagnostics，将获得 `IllegalArgumentException`；
- 不在本任务实现 SourceGraph、Frontend、Compiler Pipeline 或具体发布器。

## 5. 架构边界

```text
调用方
  → ModelCompiler.compileAndPublish
      → 内部 Compiler Pipeline（后续任务）
      → EngineContext(CompiledModelSet)
      → ContextPublisher.publish(expectedCurrent, candidate)
      → PublishedCompilationResult / FailedCompilationResult
```

`dec-core-compiler` 只依赖 `dec-core-context`。Context、Datasource、Frontend 和 Runtime 模块不得依赖 Compiler API。

## 6. 验证要求

- 有效 RED 必须在 Java 8 编译成功后，仅因两项新发布聚合不变量未满足而失败；
- Architecture Skeleton 必须显式建立精确发布事实验证边界，并保持至少一项受控 RED；
- GREEN 必须覆盖值相等但不同模型实例、diagnostics 不一致、合法成功结果、失败结果不可观察候选等场景；
- 完整 Maven Reactor、P0 故意失败阻断门禁和模块依赖审计必须通过；
- 所有新增或修改的 `@Override` 单独一行；公共方法和重要逻辑使用中文注释。
