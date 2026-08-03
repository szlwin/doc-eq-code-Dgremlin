# TASK-P1-T07 / I001 — TypedKey 与两遍 Symbol 注册

- 状态：`COMPLETED`
- Branch：`feature/p1-t07-symbol-table-20260803-1958`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Dependency：`COMPLETION-P1-T06-R04@242db638c61d`
- Design：`DESIGN-R27@P1-T07-I001`
- Plan：`TP-P1-COMPILER-F01-R23@P1-T07-I001`
- TDD：`TDD-P1-T07-R01@9e7dbc1bb451`
- Architecture Skeleton：`DEVSKEL-P1-T07-R01@c4d33f9ec8e9`
- Development：`DEV-P1-T07-R01@7f4ee8a0ee5a`
- Code Review：`CODEREVIEW-P1-T07-R01@7f4ee8a0ee5a`
- Testing：`TESTING-P1-T07-R01@7f4ee8a0ee5a`
- Completion：`COMPLETION-P1-T07-R01@7f4ee8a0ee5a`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Target PR：`#22`，目标 `dev_all`

## 完成结果

复用 Context 已发布的 11 类 TypedKey，将 T06 `RawDefinitionSet` 通过两个完整 sourceOrdinal 扫描转换为稳定有序、只读、无覆盖的 `SymbolTable`。

第一遍登记顶层与 owner Key，第二遍登记 Information 与 Produce。重复 TypedKey 产生 `MIX_SYMBOL_DUPLICATE`，保留首定义并关联双方 SourceRef；owner 上下文错误 fail closed；任一错误均不发布部分表。

`ROOT_CONFIG`、`RULE`、`MODEL_ACCESS` 没有 Context TypedKey，保持 Raw 事实。RawReference 原样保留，T08 未启动。

## 流程证据

- 有效 RED：`9e7dbc1bb451...`，Run `30813248674`，SymbolRegistration 11 failures / 0 errors；
- Skeleton：`c4d33f9ec8e9...`，Run `30813513010`，10 controlled failures / 0 errors；
- 首个 GREEN：`7612ebc81c6b...`，Run `30813780829`，后被独立 Review 推翻；
- Finding：`FND-P1-T07-I001-001`，第一遍错误短路第二遍 Finding 收集；
- 独立复现：`0aefe724a1b1...`，Run `30814139674`，1 expected failure / 0 errors；
- 最终 clean-code：`7f4ee8a0ee5a...`，Run `30814383829` — SUCCESS；
- Artifact：`8856098502`；
- SHA-256：`1f71fb0f3f2615dfc599792e5760993048f832a085bdfed965b44b0f13acfdf8`；
- Artifact 独立校验：实际 ZIP SHA-256 与 GitHub digest 一致；
- Surefire XML：60 个。

## 最终测试

- Symbol：23/23
- Compiler：152/152
- XML：30/30
- YAML：59/59
- Context 正常：26/26
- Demo：4/4
- Legacy declaration：1/1
- 故意失败门禁：1 项按预期失败并被识别
- 12 模块 Reactor、Java release 8：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Review 与 Gate

- Review：`REV-000309`～`REV-000323`
- Evidence：`EVD-000551`～`EVD-000566`
- `FND-P1-T07-I001-001`：CLOSED
- 开放 P0/P1/P2：`0 / 0 / 0`
- R27 blob：`613edfdc133fa68aa12ae3adc31eb8ae23058d9c`
- R23 blob：`840989a6119e7e5f99981957614806c2152ea56d`
- Revision Integrity：PASSED

## 范围与编码规范

生产和测试代码只新增 `dec-core-compiler.symbol` 包；未修改 Context、Raw、Frontend、SourceGraph 或 Compiler API 生产合同。

- Java release 8
- 所有新增 `@Override` 注解独占一行
- 方法、构造器和重要注册、owner、重复、资源、失败逻辑均使用中文注释
- 无 static mutable Registry 或全局 Session 状态

## 下一步

PR #22 可转为 Ready for Review，但未经用户明确授权不得合并。PR 合并前 `TASK-P1-T08` 保持未启动和阻断。
