# TASK-P1-T07 R02 Design / Plan Evidence

- Task：`TASK-P1-T07 / I002`
- Design：`DESIGN-R28@P1-T07-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R24@P1-T07-REWORK-I002`
- Rework Base：`43846e2d2e2c8b174fb87cdeb15e16c37392f505`
- Clean-code Head：`ffe544e3060dd15b82a73677b30147aaa4b360af`

## Revision Lock

- R28 first commit：`b717288297a5c78a79584412909f7e74550f7beb`
- R28 blob：`142ec612eb5658f41108330a4ca5b545521fd85c`
- R24 first commit：`577c68cb5b79993909660485110f11f4f8495f7a`
- R24 blob：`7a041c5c3811c1725482ee0b5ad288428c745a4e`
- 两个 Revision 均在有效 RED Head `619714e24fd5e37fc186897485aef1f9039c6209` 前创建；clean-code Head 复核 blob 未变化。

## 冻结决策

- 结构 owner 使用原始 Raw lexical parent name 精确比较；
- TypedKey 独立执行 Context canonical trim；
- RuleView 使用自身 ownerToken 在完整 System 集合中查找，不依赖最近 System；
- missing System 固定产生 `symbol.owner.system.missing`；
- Diagnostic 通过 LinkedHashSet 单次 add 去重，最终由 SymbolBuildResult 稳定排序；
- R01 Completion 作为被独立 Review 推翻的不可变历史保留；
- 不修改 Context/T06 合同，不启动 T08。

## Review

- `REV-000324`～`REV-000327`：Review Confirmation、Design、Architecture、Plan 均 PASSED；
- Evidence：`EVD-000567`～`EVD-000570`。
