# DEV-P1-T15-R01 — Starter 实例化与 Declaration Runtime 退役

- Evidence ID：`EVD-001104`
- Development：`DEV-P1-T15-R01@f36b03e6243`
- Code/Test Revision：`f36b03e6243f6e3c9d2f5b2ffce7cf4b1fd63eb3`
- Status：`PASSED`

## Production changes

- 新增 `dec.core.starter.CompilerStarter`，构造器只接收实例级 `ModelCompiler`；
- `compileAndPublish` 在参数非空校验后精确委托同一个 Compiler，并原样返回 `CompilationResult`；
- `projection` 只接受 `PublishedCompilationResult`，直接返回其 `engineContext().projection()`；
- 删除 Starter 的 `ConfigUtil`、`DataSourceManager` 与 XML/YAML Parser 直接依赖；
- Starter 不保存 static current Context，不创建额外 Publisher/CAS，不使用反射或 ServiceLoader。

## Retirement changes

- 从根 Reactor 和 dependencyManagement 删除 `dec-expand-declaration`；
- 从 Git 树整体删除 `dec-expand-declaration/`，未迁移旧 package，未新增 Adapter；
- 删除 `dec-demo` 中依赖旧 declaration/config 的源码、资源与测试；
- Demo 仍保留的独立模型示例改为显式依赖 `dec-core-model`，不通过 Starter 传递引入；
- 新增仓库、源码、ServiceLoader、反射字符串、依赖树和 Artifact 残留扫描；
- 新增 mutation proof，临时注入旧 module 与 `LegacyDeclarationAdapter` 时必须 fail-closed，清理后恢复 GREEN。

## Independent Review repair

Review 发现原有 3 项 T15 测试只覆盖结构和类路径，没有落实 Architecture 中声明的委托与 Projection 行为合同。修复仅新增独立测试，不修改生产实现：

- 精确一次委托；
- request/publication/result 对象同一性；
- 空参数在委托前拒绝；
- Projection 与已发布 EngineContext 同源且对象同一；
- null 与非发布结果稳定拒绝。

## Style and scope

- 所有新增 `@Override` 注解独占一行；
- 类、方法、测试替身、委托、Projection 与 mutation 重要逻辑均使用中文注释；
- Java release 8；
- 无 sleep、wall-clock、全局 mutable current 或新增运行时依赖；
- 未实现 P2～P7 runtime。

Development 完成，允许进入独立 Code Review 与 Testing。
