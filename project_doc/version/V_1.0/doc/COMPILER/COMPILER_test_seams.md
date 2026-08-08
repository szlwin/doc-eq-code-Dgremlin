# COMPILER P2 设计测试接缝

> Revision：`DESIGN-P2-R01@8875f042898c`。该文件属于 Design Revision 的可测试性说明；正式 Test Design 另见本需求的 `test_case.md`。

## 1. 编译 seam
- deterministic System source provider：同语义多文件不同顺序；
- System duplicate fixture：同 key 两个 SourceRef；
- composite RuleView fixture：跨 System 同名、同 System 重名、缺 system；
- model shape fixture：合法、unknown、non-composite path；
- access matrix fixture：READ/WRITE/EXECUTE 各自 declared/undeclared。

## 2. Runtime seam
- immutable Context A/B；
- `RuntimeFactEvaluatorStub(ALLOW|DENY|THROW|UNKNOWN)`；
- `ModelAccessGuardSpy` 记录 authorize 次数与 request；
- `MutationProbe` 记录 state version、write count、external effect count；
- `CompositeRuleViewResolverSpy` 记录 RuleViewKey，禁止 bare-name probe 被调用。

## 3. Oracle 原则
- expected identity/path/operation 由 requirement/BM/Design 固定，不从 implementation output 反推；
- static invalid 必须在 compile 阶段失败；
- dynamic DENY 的核心 oracle 是 protected state 与 external effect 均未变化；
- 同语义顺序变化必须得到同 key set、Diagnostic order 和 semantic digest；
- 不以 module-not-found、compile error、dependency download failure 作为未来有效 TDD RED。
