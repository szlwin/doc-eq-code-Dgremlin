# TASK-P1-T09 / I002 — canonical common、输入快照与 depth 返工

- 状态：`COMPLETED / PASSED`
- Base：`dev_all@e47551e0c79984d8f3fafc0ce379da76ad0d5593`
- Rework Base：`19b14487646c66ab1d7a386e96fc4876581b214c`
- Dependency：`COMPLETION-P1-T08-R02@bab0993ecfd8`
- Branch：`feature/p1-t09-engine-context-20260804-1040`
- PR：`#24`
- Design：`DESIGN-R32@P1-T09-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R28@P1-T09-REWORK-I002`
- TDD：`TDD-P1-T09-R02@002594d2cba2`
- Architecture：`DEVSKEL-P1-T09-R02@3efb2d1f0c97`
- Development：`DEV-P1-T09-R02@95b08223083f`
- Testing：`TESTING-P1-T09-R02@95b08223083f`
- Completion：`COMPLETION-P1-T09-R02@95b08223083f`
- Invalidated Completion：`COMPLETION-P1-T09-R01@ecfe3f53bde7`（不可变历史保留）
- Reviews：`REV-000391`～`REV-000407`
- Evidence：`EVD-000646`～`EVD-000668`
- Open P0/P1/P2：`0 / 0 / 0`

## Finding closure

- `FND-P1-T09-I002-001` CLOSED：common 权限与限制统一使用 canonical SystemKey，padded raw lexical 保留；
- `FND-P1-T09-I002-002` CLOSED：Raw/Symbol 完整快照入口门禁，失败只返回 `information.input.snapshot-mismatch`；
- `FND-P1-T09-I002-003` CLOSED：R27 正确 first commit 为 `e7713c449927...`，原无效 SHA 保留为历史；
- `FND-P1-T09-I002-004` CLOSED：128 层括号通过，129 层稳定失败。

## Validation

- Clean-code Head：`95b08223083f9d6b8573e96cdd12364334c0f234`
- P0 Run：`30882162374` — SUCCESS
- Artifact：`8881702632`
- SHA-256：`2f09baf88333eeff96e34ac7ab6be840c0aba4bfffd20309d9afe6bfad64ce4f`
- I002：`12/12`
- T09：`36/36`
- Symbol：`66/66`
- Compiler：`231/231`
- 正常测试：`351/351`
- Surefire XML：`70`
- 故意失败门禁：`recognized`
- Reactor：`12 modules / PASSED`
- Java release 8：`PASSED`
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Scope and Coding

- 生产修改仅位于 information 包和 SymbolTable additive read-only predicate；
- raw lexical、Registry、SymbolTable equals/hashCode、Context、T08、Compiler API 与 systems.xml 未改变；
- 无求值、DAG、循环检测、缓存、I/O、网络、模糊查询或全局状态；
- 临时 workflow 已删除；
- `@Override` 独占一行；
- 方法和重要 identity、snapshot、parser、Diagnostic、资源与失败逻辑使用中文注释。

## Next Gate

- PR #24：Ready for Independent Review；未经用户明确授权不得合并；
- PR #24 合并前 `TASK-P1-T10` 保持 `BLOCKED_UNTIL_PR_MERGE`。
