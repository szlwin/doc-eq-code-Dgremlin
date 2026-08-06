# DEV-P1-T14-R03 — I003 Evidence Repair Development

- Evidence ID：`EVD-001094`
- Development：`DEV-P1-T14-R03@37fb814b39c5`
- Code/Test Revision：`37fb814b39c54e6260fd65d13cb31e817bc0fe92`
- Status：`PASSED`

## Changes

I003 不修改 T14 生产语义。开发范围仅包含：

- 新增 `scripts/remediation/prove_t14_provenance_mutation_gate.sh`；
- P0 Workflow 在完整 `clean verify` 后运行 mutation proof；
- Artifact 增加 `dec-core-compiler/target/t14-mutation-proof/**`；
- mutation harness 精确验证测试实际执行、assertion failure、0 error；
- mutation 完成后恢复生产源码并重跑目标测试为 GREEN；
- 定向重跑前保存完整 Surefire XML，结束后恢复完整 5 项/11 项报告。

## Production scope

- T14 生产代码变更：0；
- T14 JUnit 测试源码变更：0；
- ContextPublisher、PublicationRequest、EngineContext CAS：未修改；
- T15、Starter 接线、P2～P7 runtime：未实现；
- mutation 只作用于 CI 临时工作树，不会提交被破坏源码。

## Style

- 脚本方法、mutation、恢复和证据校验均使用中文注释；
- 本轮没有新增 Java `@Override`；既有 `@Override` 仍独占一行；
- Java release 8、无新依赖、无 sleep、无 wall-clock、无共享可变测试状态。

## Result

I003 Development 完成，Architecture delta 为 `CI-ONLY / NO_PRODUCTION_CHANGE`。允许进入独立 Code Review。
