# TASK-P1-T13 — 确定性 Digest、Deadline 与 Observer

- Current Iteration：`I003`
- Status：`COMPLETED / PASSED`
- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Rework Base：`PR28@f80656c19dd695c92e75a4d8eceb8b54d8e37940`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Current Design：`DESIGN-R47@P1-T13-REWORK-I003`
- Current Plan：`TP-P1-COMPILER-F01-R43@P1-T13-REWORK-I003`
- Current TDD：`TDD-P1-T13-R03@5075793d06cc`
- Current Architecture：`DEVSKEL-P1-T13-R03@5075793d06cc`
- Current Development：`DEV-P1-T13-R03@5075793d06cc`
- Current Code Review：`CODEREVIEW-P1-T13-R05@5075793d06cc`
- Current Testing：`TESTING-P1-T13-R03@5075793d06cc`
- Current Completion：`COMPLETION-P1-T13-R03@5075793d06cc`
- Previous Code Review：`CODEREVIEW-P1-T13-R03@7d39c3bc0ab4` — `INVALIDATED / PRESERVED`
- Previous Completion：`COMPLETION-P1-T13-R02@7d39c3bc0ab4` — `INVALIDATED / PRESERVED`
- Reviews：`REV-000693`～`REV-000705`
- Evidence：`EVD-001034`～`EVD-001045`
- Open P0/P1/P2：`0 / 0 / 0`

## I003 closure

`FND-P1-T13-I003-001` 已关闭。FAILED Observer 独立 Review 现完整冻结：

- 原 ERROR code/severity/messageKey/pass；
- Observer Warning code/severity/messageKey/pass(subject)；
- Control/Observed state、executedPasses、fixture executions、transitions、timings 一致；
- publisher=0；
- artifacts empty；
- Observed 唯一新增一个 Warning。

该返工只修改测试，Production files changed=`0`。传统 production RED 不适用于已有正确行为的 Oracle hardening，已记录负向变异证明。

## Current contract

- Strict Unicode Source identity P1 修复继续 CLOSED；
- malformed high/low surrogate 继续 strict UTF-8 fail-closed；
- 合法 ASCII/BMP/supplementary digest vectors 保持；
- FAILED Observer Warning 不改变原 ERROR、状态、执行顺序、Timing、publisher 或 artifacts；
- T12 Deadline/Cancel/Clock/Publication 原子性保持；
- 未实现 T14/T15 或 P2～P7 runtime。

## Validation

- Code/Test Revision：`5075793d06cc028038d9689f0ca733ecc446e7b0`；
- P0 Run：`31016766448` — SUCCESS；
- Artifact：`8934826368`；
- SHA-256：`13ad7a816de48d7aca33a18996934d41d330e3f41df9742a4c33c9a167926ef6`；
- Surefire XML：106；T13：34/34；T12：133/133；Compiler：486/486；
- 正常测试：606/606；intentional failure：1；Errors/Skipped：0/0；
- Java release 8、12 modules：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

## Preserved history

- I001 Completion：`COMPLETION-P1-T13-R01@74672ee1367b` — INVALIDATED / PRESERVED；
- I002 Completion：`COMPLETION-P1-T13-R02@7d39c3bc0ab4` — INVALIDATED / PRESERVED；
- I003 Completion：`COMPLETION-P1-T13-R03@5075793d06cc` — CURRENT / PASSED。

## Style and scope

- 所有 `@Override` 独占一行；
- 方法、fixture、Diagnostic 选择和重要顺序逻辑使用中文注释；
- Production files changed：0；
- Code/Test Revision 后只允许 `project_doc` 更新。

PR #28 未执行合并；未经用户明确授权不得合并。TASK-P1-T14 保持 `BLOCKED_UNTIL_PR_28_MERGE`。
