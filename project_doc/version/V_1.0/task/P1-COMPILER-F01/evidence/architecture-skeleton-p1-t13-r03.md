# DEVSKEL-P1-T13-R03 — I003 No-Production Architecture Gate

- Architecture ID：`DEVSKEL-P1-T13-R03@5075793d06cc`
- Iteration：`TASK-P1-T13 / I003`
- Input：`TDD-P1-T13-R03@5075793d06cc`
- Status：`PASSED`

## Decision

完整 Oracle 在当前实现上直接通过，没有暴露生产缺陷，因此本迭代禁止修改 production。

## Allowed test architecture

- Control 与 Observed 使用相同 `failureIndex=2`；
- 两组使用独立但从 0 开始、每次递增 1ns 的确定性 Clock；
- 两组使用独立 execution list 和 publisher counter；
- Control 使用正常 RecordingObserver；
- Observed 仅在 `to=FAILED` 时抛异常；
- 使用完整 identity helper 查找唯一 Diagnostic；
- 比较 state、executedPasses、fixture executions、transitions、timings；
- 精确验证 Warning subject=`STRUCTURALLY_VALIDATED->FAILED`。

## Production scope

```text
Production files changed: 0
```

禁止修改 CompilerPipeline、CompilationSession、PipelineDiagnostics、Digest、Publisher、CAS、T14/T15 或 P2～P7 runtime。

## Style gate

- `@Override` 独占一行；
- 测试方法、fixture、Diagnostic 选择和顺序比较均有中文注释；
- 不使用 wall clock、sleep 或执行顺序依赖。
