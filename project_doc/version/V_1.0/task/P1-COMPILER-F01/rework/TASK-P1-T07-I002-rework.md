# TASK-P1-T07 / I002 — owner identity 与 Diagnostic 聚合 Rework

- 状态：`IN_PROGRESS`
- PR：`#22`（Draft）
- Branch：`feature/p1-t07-symbol-table-20260803-1958`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Rework Base：`43846e2d2e2c8b174fb87cdeb15e16c37392f505`
- Superseded Completion：`COMPLETION-P1-T07-R01@7f4ee8a0ee5a`
- Dependency：`COMPLETION-P1-T06-R04@242db638c61d`
- Design：`DESIGN-R28@P1-T07-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R24@P1-T07-REWORK-I002`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Review Finding

- `FND-P1-T07-I002-001` `[P1][OPEN]`：结构 owner 错误地使用 trim 后 TypedKey name 与 Raw lexical token 比较；
- `FND-P1-T07-I002-002` `[P1][OPEN]`：RuleView 错误依赖最近扫描 System，不能支持独立文档顺序；
- `FND-P1-T07-I002-003` `[P2][OPEN]`：Diagnostic 去重使用 `ArrayList.contains`，最坏 O(n²)。

## 冻结决策

- 结构 owner 使用原始 lexical parent name 做精确 equals；
- TypedKey 构造独立执行 Context canonical trim；
- RuleView 根据自身 ownerToken 构造 SystemKey，并在完整 System 集合中做 canonical 存在性校验；
- missing System 使用 `symbol.owner.system.missing` fail closed；
- Diagnostic 使用哈希集合线性聚合，最终由 SymbolBuildResult 稳定排序；
- R01、R27、R23、既有 Review/Evidence/Completion 全部不可变保留。

## 编码规则

- Java release 8；
- `@Override` 注解独占一行；
- 方法、构造器和重要 owner、RuleView、Diagnostic、资源、失败逻辑添加中文注释；
- 不修改 Context、Raw、Frontend 或 Compiler API 生产合同；
- 不实现 T08，不合并 PR #22。

## 当前 Gate

- Design R28：PASSED；
- Plan R24：PASSED；
- Open P0/P1/P2：`0 / 2 / 1`；
- 下一阶段：建立可编译 TDD seam 与有效 RED。
