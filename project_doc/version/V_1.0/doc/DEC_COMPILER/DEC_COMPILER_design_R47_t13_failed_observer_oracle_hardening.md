# DESIGN-R47 — TASK-P1-T13 FAILED Observer Oracle Hardening

- Revision：`DESIGN-R47@P1-T13-REWORK-I003`
- Status：`PASSED`
- Base：`PR28@f80656c19dd695c92e75a4d8eceb8b54d8e37940`
- Dependency：`CODEREVIEW-P1-T13-R04-INVALIDATION@P1-T13-I003`
- Previous Design：`DESIGN-R46@P1-T13-REWORK-I002` — `INVALIDATED / PRESERVED`
- Scope：`TASK-P1-T13 / I003`

## 1. Goal

只补齐 R46 已规定但 I002 未完整实现的 FAILED Observer 独立 Review Oracle，不改变生产行为。

## 2. Control and observed executions

使用相同失败位置、相同确定性 Clock 和相同 Publisher 构造两次执行：

- Control：失败 Pipeline + 正常 Observer；
- Observed：失败 Pipeline + 仅在 `to=FAILED` 时抛异常的 Observer。

两次执行必须使用独立但行为一致的 fixture，避免共享可变状态污染比较。

## 3. Exact diagnostic contract

Control 与 Observed 的原 ERROR 必须精确保持：

- code=`MIX_PUBLICATION_BLOCKED`；
- severity=`ERROR`；
- messageKey=`test.pass.error`；
- pass=`PipelineTestPass`。

Observed 的新增 Warning 必须精确为：

- code=`MIX_OBSERVER_FAILURE`；
- severity=`WARNING`；
- messageKey=`pipeline.observer.transition.failure`；
- pass/subject=`STRUCTURALLY_VALIDATED->FAILED`。

Observed 相比 Control 唯一允许增加一个上述 Warning，不允许替换、删除或重排原诊断。

## 4. Sequence invariants

Control 与 Observed 必须完全一致：

- `state()`；
- `executedPasses()`；
- `transitions()`；
- `timings()`；
- publisher calls；
- artifacts。

其中两组 publisher calls 均为 0，artifacts 均为空。

## 5. TDD mode

本任务是测试证据补强，生产行为静态上已满足合同。新增完整 Oracle 预期直接通过，因此 production RED 不适用。以负向变异证明替代：

- 旧 Oracle 对 ERROR code 变异不敏感；
- 旧 Oracle 对 Warning subject 变异不敏感；
- 旧 Oracle 对 transition/timing/executedPasses 顺序变异不敏感；
- 新 Oracle 对上述任一变异均会失败。

## 6. Allowed scope

只允许修改：

- `CompilationObserverIndependentReviewTest.java`；
- TASK-P1-T13 I003 的 `project_doc` 记录。

禁止修改生产代码、T14/T15 或 P2～P7 runtime。

## 7. Style

- 所有 `@Override` 独占一行；
- 测试方法、fixture、诊断选择和控制组对照逻辑使用中文注释；
- 不依赖测试执行顺序或 wall clock。
