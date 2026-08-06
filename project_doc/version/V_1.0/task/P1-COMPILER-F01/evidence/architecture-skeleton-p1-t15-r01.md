# DEVSKEL-P1-T15-R01 — Starter 与退役架构门禁

- Architecture ID：`DEVSKEL-P1-T15-R01@bff67b86fb55`
- Input：`TDD-P1-T15-R01@bff67b86fb55`
- Design：`DESIGN-R51@P1-T15-I001`
- Plan：`TP-P1-COMPILER-F01-R47@P1-T15-I001`
- Status：`PASSED`

## 1. Projection boundary

现有 `CoreConfigProjection` 已满足单一事实源：

- 只能由 `CompiledModelSet` 派生；
- `EngineContext` 构造时从同一模型创建 Projection；
- Data/View/Rule 列表不可变；
- 写入口和集合写方法稳定拒绝；
- 无 static mutable registry。

T15 不重写 Projection 生产代码，只补充最终集成与独立 Review。

## 2. Starter boundary

新增 `dec.core.starter.CompilerStarter`：

- `public final`；
- 唯一字段为 `private final ModelCompiler compiler`；
- 唯一公共构造器接收 `ModelCompiler`；
- `compileAndPublish` 对 request/publicationRequest 做 null 检查后原样委托一次；
- 返回同一个 `CompilationResult` 实例；
- `projection(CompilationResult)` 只接受 `PublishedCompilationResult`，返回其 `engineContext().projection()`；
- 非发布结果稳定抛 `IllegalStateException("projection requires a published compilation result")`；
- 不保存 current Context，不拥有额外 Publisher/CAS，不调用反射或 ServiceLoader。

## 3. Starter dependency

Starter POM 只保留：

- `dec-core-compiler`；
- 既有 provided 日志依赖。

删除 Context/Model/Datasource/XML/YAML 的直接旧依赖；Compiler 已通过公共 API 提供 Context 结果。

## 4. Retirement

- 根 POM 删除 declaration module 和 dependencyManagement；
- Git 树删除整个 `dec-expand-declaration/`；
- 不迁移旧 package，不创建 Adapter；
- 退役 Gate 在完整 build 后扫描 POM、源代码、服务、反射字符串、依赖树和 Artifact。

## 5. Test architecture

- JUnit 直接验证委托次数、参数 identity、结果 identity、Projection identity、失败拒绝和无 static mutable；
- 独立 Review 使用仓库/Artifact 扫描验证无旧 package；
- retirement Gate 后续增加 mutation proof，确保 fake module/source residue 会被阻断；
- Java release 8，禁止 sleep、wall-clock、全局可变状态。

## 6. Style

- 所有 `@Override` 独占一行；
- Starter 构造、委托、Projection 读取和拒绝逻辑使用中文注释；
- 脚本扫描与恢复逻辑使用中文注释。
