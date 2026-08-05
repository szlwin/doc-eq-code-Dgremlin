# TASK-P1-T13 I002 重新 Review 与失效记录

- Review ID：`CODEREVIEW-P1-T13-R04-INVALIDATION@P1-T13-I003`
- Reviewed Head：`f80656c19dd695c92e75a4d8eceb8b54d8e37940`
- Reviewed Code/Test Revision：`7d39c3bc0ab45b6cd3c8ab637c10ae40a15e07b8`
- Previous Review：`CODEREVIEW-P1-T13-R03@7d39c3bc0ab4`
- Previous Completion：`COMPLETION-P1-T13-R02@7d39c3bc0ab4`
- Result：`NEEDS_CHANGES`
- Open P0/P1/P2：`0 / 0 / 1`

## Gate decision

以下历史事实完整保留，但立即失效，不再作为当前通过门禁：

- `CODEREVIEW-P1-T13-R03@7d39c3bc0ab4` — `INVALIDATED / PRESERVED`；
- `COMPLETION-P1-T13-R02@7d39c3bc0ab4` — `INVALIDATED / PRESERVED`。

TASK-P1-T13 进入轻量返工迭代 `I003`。PR #28 保持 Open、未合并；TASK-P1-T14 继续 `BLOCKED_UNTIL_PR_28_MERGE`。

## FND-P1-T13-I003-001

- Severity：`P2`
- Area：`SPEC / TEST-EVIDENCE / ORACLE`
- Status：`OPEN`

现有 `failedTransitionObserverFailurePreservesOriginalFailure()` 已验证 FAILED、原 ERROR 存在、Observer Warning、publisher=0 和 empty artifacts，但没有完整冻结：

1. 原 ERROR 的 `code/severity/messageKey`；
2. Warning 的 `code/severity/messageKey/pass(subject)`；
3. 与正常 Observer 控制组相比，`executedPasses/transitions/timings` 完全一致；
4. Observed 相比 Control 唯一允许差异为多一个 Observer Warning。

## Scope

该 Finding 理论上只需修改测试。除非补强 Oracle 实际失败并证明生产行为不符合 R46，否则不得修改 production。

## TDD classification

本迭代属于已有正确行为的 Oracle hardening：新增完整断言预期直接通过，传统 production RED 不适用。使用负向变异证明替代：验证旧断言无法阻断 code/subject/order 变异，而新断言能够阻断。
