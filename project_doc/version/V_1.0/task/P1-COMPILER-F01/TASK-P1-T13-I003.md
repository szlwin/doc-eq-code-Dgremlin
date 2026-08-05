# TASK-P1-T13 / I003 — FAILED Observer Oracle Hardening

- Status：`IN_PROGRESS / ORACLE_HARDENING`
- Base：`PR28@f80656c19dd695c92e75a4d8eceb8b54d8e37940`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / OPEN / DO_NOT_MERGE`
- Previous Review：`CODEREVIEW-P1-T13-R03@7d39c3bc0ab4` — `INVALIDATED / PRESERVED`
- Previous Completion：`COMPLETION-P1-T13-R02@7d39c3bc0ab4` — `INVALIDATED / PRESERVED`
- Design：`DESIGN-R47@P1-T13-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R43@P1-T13-REWORK-I003`
- Open P0/P1/P2：`0 / 0 / 1`

## Finding

- `FND-P1-T13-I003-001`：P2，FAILED Observer 测试未完整冻结原 ERROR identity、Warning subject 和执行顺序合同。

## Goal

补强现有独立 Review 测试，完整比较正常 Observer 控制组与 FAILED transition 抛异常的观察组，确保唯一差异为额外 Observer Warning。

## TDD classification

- Mode：`ORACLE_HARDENING`
- Production RED：`NOT_APPLICABLE`
- Reason：生产行为静态复核正确，需求仅补充已有行为的回归断言；完整断言预期直接通过。
- Negative proof：旧 Oracle 无法阻断 code/subject/order 变异，新 Oracle 必须能够阻断。

## Allowed changes

- `CompilationObserverIndependentReviewTest.java`
- I003 相关 `project_doc`

## Excluded

- 所有 production 文件；
- ContextPublisher、PublicationRequest、EngineContext CAS；
- T14/T15；
- P2～P7 runtime。

## Required assertions

- 原 ERROR：code/severity/messageKey/pass；
- Observer Warning：code/severity/messageKey/pass；
- Control/Observed：state、executedPasses、transitions、timings 完全一致；
- publisher=0；
- artifacts empty；
- Observed 仅比 Control 多一个 Observer Warning。
