# TASK-P1-T06 / I003 — Input Snapshot Rework

- 状态：`COMPLETED`
- PR：`#21`
- Branch：`feature/p1-t06-raw-definition-20260803-1334`
- Rework Base：`3884f331dd066da1ff556f9b0544716d7ca3502c`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Historical Completion：`COMPLETION-P1-T06-R01@90d483290cf3`、`COMPLETION-P1-T06-R02@aec3cd105b15`
- Superseding Review：`REV-000283`
- Finding：`FND-P1-T06-I003-001` — `CLOSED`
- Design：`DESIGN-R25@P1-T06-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R21@P1-T06-REWORK-I003`
- TDD：`TDD-P1-T06-R03@ea1701deb923`
- Architecture Skeleton：`DEVSKEL-P1-T06-R03@35357c213fdc`
- Development：`DEV-P1-T06-R03@432ccdc1103f`
- Code Review：`CODEREVIEW-P1-T06-R03@432ccdc1103f`
- Testing：`TESTING-P1-T06-R03@432ccdc1103f`
- Completion：`COMPLETION-P1-T06-R03@432ccdc1103f`
- Reviews：`REV-000283`～`REV-000295`
- Evidence：`EVD-000525`～`EVD-000537`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 完成结果

public `build(List)` 现在只迭代调用方容器一次，冻结不可变 snapshot；validate、extract、ordinal 和 RuntimeException 失败定位只消费该 snapshot。

7 项确定性 side-effecting List Oracle 全部通过：

- 后续 iterator 的 unsupported root 不被消费；
- 后续 iterator 的 unknown child 不进入 Raw body；
- snapshot 后原 List 修改不影响结果；
- snapshot 顺序决定 ordinal；
- snapshot 内真实 unsupported root/unknown child fail closed；
- snapshot 读取异常稳定失败且不重访原 List。

上一轮 2 个 P1、3 个 P2 继续保持 `CLOSED`；六类根 Grammar、14 Kind、lexical、reference、depth/node budget、toString 与不可变集合合同均未回退。

## 测试

- P0 Run：`30801214669` — `SUCCESS`；
- Artifact：`8850875201`；
- SHA-256：`eadc28a2db03ff23405869712aefa84398cf1b9b37f9408b20d348af67d783b7`；
- I003：7/7；T06 Raw：38/38；Compiler：121/121；
- XML：30/30；YAML：59/59；Context 正常：26/26；Demo：4/4；Legacy：1/1；
- 故意失败门禁：1 项按预期失败并被识别；
- 12 模块 Reactor、Java release 8：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 边界

R01/R02 以及 I001/I002 的 Design、Plan、TDD、Skeleton、Development、Testing、Review、Evidence、Completion 和机器 checkpoint 均作为不可变历史保留。

PR #21 未合并；未经用户明确授权不得合并。TASK-P1-T07 未启动并继续阻断。
