# DESIGN-P2-R01 Testability Matrix

Revision: `DESIGN-P2-R01@8875f042898c`.

本矩阵是 Design Review 的测试可行性证据，不是正式 Test Design；正式 `test_case.md` 由下一阶段生成。每个 AC 在当前 Design 已有稳定 seam、oracle 与预计 Case 族，避免 Test Design 再决定产品语义。

| AC | Design seam / oracle | 预计 Test Design Case 族 |
|---|---|---|
| AC-P2-SYSTEM-RULEVIEW-001 | deterministic System source provider；SystemKey 集合与 semanticDigest 顺序无关 | TD-P2-SYSTEM-DETERMINISM, TD-P2-SYSTEM-DUPLICATE |
| AC-P2-SYSTEM-RULEVIEW-002 | composite RuleView fixture；跨 System 同名分别命中，同 System 重复拒绝 | TD-P2-RULEVIEW-ISOLATION, TD-P2-RULEVIEW-DUPLICATE |
| AC-P2-SYSTEM-RULEVIEW-003 | CompositeRuleViewResolverSpy；只允许 RuleViewKey(system,name)，bare-name probe 不得调用 | TD-P2-RULEVIEW-LOOKUP, TD-P2-BARE-NAME-NO-FALLBACK |
| AC-P2-SYSTEM-RULEVIEW-004 | access matrix fixture；READ/WRITE/EXECUTE 独立 declared/undeclared | TD-P2-ACCESS-MATRIX |
| AC-P2-SYSTEM-RULEVIEW-005 | ModelShapeFixture + ModelPathCompiler；exact path、unknown、non-composite | TD-P2-MODEL-PATH-EXACT, TD-P2-MODEL-PATH-INVALID |
| AC-P2-SYSTEM-RULEVIEW-006 | compile seam；静态非法授权产生 Diagnostic 且 candidate 不发布 | TD-P2-STATIC-DENY |
| AC-P2-SYSTEM-RULEVIEW-007 | RuntimeFactEvaluatorStub + GuardSpy + MutationProbe；DENY/异常/unknown 均零副作用 | TD-P2-RUNTIME-GUARD, TD-P2-GUARD-FAIL-CLOSED |
| AC-P2-SYSTEM-RULEVIEW-008 | candidate publication + ContextPairFixture；单一原子发布闭包 | TD-P2-ATOMIC-PUBLICATION, TD-P2-CONTEXT-ISOLATION |
| AC-P2-SYSTEM-RULEVIEW-009 | duplicate/invalid fixtures + deterministic Diagnostic order | TD-P2-DIAGNOSTIC-DETERMINISM |
| AC-P2-SYSTEM-RULEVIEW-010 | LegacyBoundaryScan；新路径不得写旧 Config/Rule registry，P7 前仅保留读取兼容 | TD-P2-DECLARATION-BOUNDARY, TD-P2-LEGACY-NO-NEW-FALLBACK |

## RED 契约前置约束

- 当前阶段只定义 seam/oracle，不执行 TDD RED。
- 下一 Test Design 必须将上述 Case 族细化为可执行 Case，并绑定 TR/AC。
- 将来 TDD 的有效 RED 必须是业务断言失败；module-not-found、依赖下载失败、编译环境错误不算有效 RED。
