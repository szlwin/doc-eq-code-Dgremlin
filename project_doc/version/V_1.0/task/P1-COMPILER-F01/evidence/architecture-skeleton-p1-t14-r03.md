# DEVSKEL-P1-T14-R03 — I003 Architecture Review

- Evidence ID：`EVD-001092`
- Revision：`DEVSKEL-P1-T14-R03@dc4f0f5cc566`
- Design：`DESIGN-R50@P1-T14-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R46@P1-T14-REWORK-I003`
- Result：`PASSED`

## Review scope

I003 只修复 TDD 与证据生命周期，不改变 T14 生产语义。独立复核以下边界：

- `CompilerDigestService.bind()` 是唯一 atomic provenance 入口；
- `DigestBoundCompiledInput` 私有构造并绑定同一模型快照与 Digest；
- raw/published Source identity 闭包一致性门禁存在；
- `CompiledModelSetBuilder` 只接受 bound input；
- Publication Pass 在 prepare 前校验 request schema/options；
- Pipeline 仍是唯一 Publisher/CAS capability 持有者；
- Registry snapshot 完整性门禁保持；
- 当前生产代码无需修改。

## Mutation harness architecture

Mutation harness 仅属于 `scripts/remediation` 和 P0 Workflow：

1. 在 CI 临时工作树备份两个生产文件；
2. 使用精确文本替换分别破坏 request binding 和 Source closure 门禁；
3. 运行单个已有 JUnit 行为测试；
4. 解析 Surefire XML，要求测试实际执行且只有 assertion failure；
5. 复制 mutation XML/日志到独立 Artifact 目录；
6. 恢复源码并重跑目标测试为 GREEN；
7. mutation 版本不得提交到 Git。

该设计不向生产包增加 API、依赖或 capability，也不改变 Java 8 合同。

## Architecture findings

- P0：0；
- P1：0；
- P2：0；
- Architecture delta：`CI-ONLY / NO_PRODUCTION_CHANGE`。

## Style gate

- 新增脚本关键步骤使用中文注释；
- 如后续出现 Java 修改，`@Override` 必须独占一行；
- 禁止 sleep、wall-clock、共享可变状态和网络依赖。
