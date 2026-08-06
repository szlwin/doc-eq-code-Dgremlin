# CODEREVIEW-P1-T15-R01 — Starter 与退役边界独立 Review

- Review ID：`REV-000760`
- Code Review：`CODEREVIEW-P1-T15-R01@f36b03e6243`
- Input：`DEV-P1-T15-R01@f36b03e6243`
- Design：`DESIGN-R51@P1-T15-I001`
- Plan：`TP-P1-COMPILER-F01-R47@P1-T15-I001`
- Status：`PASSED`
- Open P0/P1/P2：`0 / 0 / 0`

## Review scope

- Starter 公共入口、对象状态与依赖边界；
- `EngineContext → CoreConfigProjection` 单一事实源；
- 根 POM、Starter POM、Demo POM 与模块删除；
- declaration package、ServiceLoader、反射字符串和发布 Artifact 残留；
- retirement gate 与 mutation proof；
- Java 8、`@Override` 格式、中文注释和测试隔离。

## Finding

### FND-P1-T15-I001-001 — P2 / SPEC / CORRECTNESS / ORACLE

Architecture 明确要求验证委托次数、参数 identity、结果 identity、Projection identity 和失败拒绝，但最初 T15 测试只有 3 项结构/类路径检查。已有 CI GREEN 不能独立证明 Starter 的核心行为合同。

Disposition：`CLOSED`。

修复：新增 `CompilerStarterBehaviorT15IndependentReviewTest`，共 4 项测试，覆盖：

1. `compileAndPublish` 精确调用一次，并保留 request、publicationRequest、result 对象同一性；
2. null compiler/request/publicationRequest 在委托前拒绝，Compiler 调用次数保持 0；
3. `projection` 返回已发布结果所持同一个 EngineContext 的同一个 Projection；
4. null 和普通非发布结果稳定拒绝，异常消息固定。

该修复只增加测试，不调整生产实现，避免用实现变化掩盖 Oracle 缺口。

## Review conclusions

- `CompilerStarter` 唯一业务字段为 `private final ModelCompiler`，无 static mutable 状态；
- 委托、返回和 Projection 均不复制、不缓存、不重组发布事实；
- 旧 Starter 全局配置入口和旧 Parser 运行时依赖已删除；
- `dec-expand-declaration` 已从源码、POM、Reactor 和 Artifact 中整体退役；
- Demo 未通过 Starter 获取模型传递依赖；
- mutation 能检测旧 module 与旧 package 回流，恢复后再次通过；
- 无 Adapter、双轨 runtime、反射生产逻辑或额外 Publisher/CAS；
- 新增 `@Override` 均独占一行，方法与重要逻辑均有中文注释。

Review 结论：`PASSED / NO_OPEN_P0_P1_P2`，允许进入最终 Testing 与 Completion。
