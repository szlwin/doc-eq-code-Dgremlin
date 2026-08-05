# TASK-P1-T12 I002 Development Evidence

- Architecture：`DEVSKEL-P1-T12-R02@a7f8d99b1afe`
- First GREEN：`DEV-P1-T12-R02@4499bd90849d`
- Clean-code Head：`5d5a7d72119b5a36a38b19cda44186de70911912`
- Evidence：`EVD-000823`～`EVD-000827`

## Delivered behavior

- 前九个普通 Pass 只能获得不含发布能力的 `PassContext`；
- 第十阶段必须实现 `PublicationCompilerPass`，只接收 `PublicationPassContext`；
- `PublicationRequest` 只在 `CompilerPipeline.execute` 栈上局部持有，Session 不保存；
- 每个 Pass 创建独立 Context，并在 `finally` 中关闭；
- Session 在 PUBLISHED/FAILED 后拒绝 Diagnostic、artifact、执行记录和状态写入；
- Result 构造时复制 state、Diagnostic、artifact、timing、transition 和 executedPasses，不保存 Session；
- List/Set/Map/Optional artifact 递归快照，未知对象、数组语义外对象和循环图稳定拒绝；
- start-clock 成功后才登记 executedPass；
- clock/token 基础设施异常使用 `MIX-OBSERVER-FAILURE` 独立分类；
- publish 前最后一刻重新检查 ERROR、token 和 Deadline；
- publisher 调用数在成功路径精确为 1，全部发布前失败路径为 0；
- PublicationResult/status 非空、CONFLICT、publisher 异常和一次调用门禁均已实现；
- PUBLISHED 返回后立即进入不可逆终态，end-clock、Observer、token 变化或 Pass 后置异常不能降级；
- PublicationStatus 只读取一次，避免不稳定实现形成分裂终态。

## Scope and style

生产修改仅限 `dec.core.compiler.pass`，未修改 T01～T11 公共合同、Compiler API、Context 或 P2～P7 runtime。所有 `@Override` 独占一行；公开方法、构造器和重要原子提交、生命周期、快照及失败逻辑均使用中文注释。
