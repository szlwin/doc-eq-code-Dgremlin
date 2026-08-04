# TASK-P1-T08 / I002 — P1 强类型引用解析返工

- 状态：`COMPLETED / PASSED`
- Base：`dev_all@c6cd8ec156563480ec30989cdd358d4979a8599b`
- Rework Base：`PR-23@9ece664412ee947f536e2de73f20b5c7b9790bf1`
- Dependency：`COMPLETION-P1-T07-R02@ffe544e3060d`
- Branch：`feature/p1-t08-reference-resolution-20260803-2254`
- PR：`#23`
- Design：`DESIGN-R30@P1-T08-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R26@P1-T08-REWORK-I002`
- TDD：`TDD-P1-T08-R02@bfc8e4df822a`
- Architecture：`DEVSKEL-P1-T08-R02@3e85814d5cf5`
- Development：`DEV-P1-T08-R02@bab0993ecfd8`
- Code Review：`CODEREVIEW-P1-T08-R02@bab0993ecfd8`
- Testing：`TESTING-P1-T08-R02@bab0993ecfd8`
- Completion：`COMPLETION-P1-T08-R02@bab0993ecfd8`
- Invalidated Completion：`COMPLETION-P1-T08-R01@ab432a3189f4`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## I001 历史

`TASK-P1-T08 / I001`、R29/R25、R01 Completion、Review、Evidence、P0、Artifact 和失败 attempt 全部作为不可变历史保留。独立 Review 推翻 R01 的当前有效性，但没有覆盖或删除任何历史文件。

## Finding Closure

- `FND-P1-T08-I002-001`：CLOSED — 严格 lexical grammar、安全 TypedKey 构造、缺失 ref/name 与最终输入异常边界；
- `FND-P1-T08-I002-002`：CLOSED — lexical 类型摘要替代候选 List，9 个失败引用仅 9 次 O(1) 查询；
- `FND-P1-T08-I002-003`：CLOSED — SymbolTable 绑定完整 RawDefinitionSet，索引前验证快照并返回 `reference.input.snapshot-mismatch`；
- `FND-P1-T08-I002-004`：CLOSED — Canonical → T06 → T07 → T08 真实 body 集成 Oracle。

Open P0/P1/P2：`0 / 0 / 0`。

## 最终合同

- qualified Information 必须严格为两个非空 segment：`system.name`；
- 所有 TypedKey 输入在构造前执行 lexical fail-closed；
- System data-ref/view-ref 缺失 ref/name 返回 `reference.owner.invalid`；
- Resolver 对全部 T06 合法对象只能返回 `RESOLVED` 或 `FAILED`，输入相关异常不得逃逸；
- SymbolTable package-private 保存完整 RawDefinitionSet 构建快照；
- Resolver 在任何索引前验证快照；
- lexical 失败分类使用预聚合摘要，查询平均 O(1)；
- 成功路径继续只构造期望 TypedKey 并调用 `SymbolTable.find`；
- 失败不发布部分 `ResolvedReferenceSet`；
- 不使用模糊匹配、跨类型降级或 first-match；
- 不启动 T09/T10/P2～P7。

## TDD 与验证

- 被拒绝 I002 RED：`668c48bccf50` / Run `30869955354` / 22 failures / 0 errors，测试合法引用数期望偏高；
- 有效 RED：`bfc8e4df822a` / Run `30870089960` / 21 failures / 0 errors；
- Architecture Skeleton：`3e85814d5cf5` / Run `30870305310` / 21 controlled failures / 0 errors；
- Clean-code Head：`bab0993ecfd8c344beead62712ba8dc02621038d`；
- P0 Run：`30871077040` / SUCCESS；
- Artifact：`8877900378`；
- Artifact SHA-256：`a6eed26d25e9962a28d79abc4108fc61992d5d43eae7c70261c38403a8a3d68c`，独立校验一致；
- I002 22/22；I001 12/12；T08 34/34；Symbol 66/66；Compiler 195/195；
- XML 30/30；YAML 59/59；Context 正常 26/26；Demo 4/4；Legacy 1/1；
- 正常测试 315 项全部通过；故意失败门禁 1 项按预期失败并被识别；
- 12 模块 Reactor、Java release 8：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Revision Integrity

- R30 first commit：`04c590caba096b999d2320e364b464143f24f3e0`；blob：`5f392e855b5f5e3a3dc93e19f02c03db57cebe11`；
- R26 first commit：`dbea77b8698648acc35cbdb947687c58597d6612`；blob：`6ab25d67c788933d12e76206636590880c0c3598`；
- R30/R26 均在有效 RED 前创建，clean-code Head 复核未变化。

## 编码与范围

- 生产修改仅位于 `dec-core-compiler.symbol`；
- 未修改 Context、T06 Raw、T07 Symbol 公共合同或 Compiler API；
- 临时源码快照 workflow 已删除，不存在最终 PR 文件列表；
- 所有 `@Override` 独占一行；
- 方法、构造器和重要快照、lexical、复杂度、owner、Diagnostic、资源与失败逻辑使用中文注释；
- 无 static mutable registry、I/O、网络、反射执行或运行时规则执行。

## 后续 Gate

- PR #23 进入最终文档化 P0 与 Independent Review；
- 未经用户明确授权不得合并 PR #23；
- PR #23 合并前 TASK-P1-T09 继续阻断。