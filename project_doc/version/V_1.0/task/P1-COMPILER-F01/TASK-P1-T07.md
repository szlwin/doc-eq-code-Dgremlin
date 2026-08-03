# TASK-P1-T07 — TypedKey 与两遍 Symbol 注册

- 当前迭代：`I002`
- 状态：`COMPLETED`
- Result：`PASSED`
- Branch：`feature/p1-t07-symbol-table-20260803-1958`
- PR：`#22`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Dependency：`COMPLETION-P1-T06-R04@242db638c61d`
- Current Completion：`COMPLETION-P1-T07-R02@ffe544e3060d`
- Superseded Completion：`COMPLETION-P1-T07-R01@7f4ee8a0ee5a`
- Design：`DESIGN-R28@P1-T07-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R24@P1-T07-REWORK-I002`
- TDD：`TDD-P1-T07-R02@619714e24fd5`
- Architecture：`DEVSKEL-P1-T07-R02@ffe544e3060d`
- Development：`DEV-P1-T07-R02@ffe544e3060d`
- Testing：`TESTING-P1-T07-R02@ffe544e3060d`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Completion 历史

### R01 / I001

`COMPLETION-P1-T07-R01@7f4ee8a0ee5a` 曾通过 P0，但后续独立 Review 发现两个 P1 和一个 P2，因此不再是有效下游输入。R01、R27、R23、TDD、Review、Evidence 和 Artifact 全部不可变保留。

### R02 / I002

R02 关闭：

- `FND-P1-T07-I002-001`：Raw lexical owner 与 TypedKey canonical 混用；
- `FND-P1-T07-I002-002`：RuleView 错误依赖最近 System；
- `FND-P1-T07-I002-003`：Diagnostic 去重最坏 O(n²)。

## 当前发布合同

- 复用 Context 已发布的 11 类 TypedKey，不创建平行字符串 Key；
- 结构 owner 使用 T06 保存的原始 lexical parent name 精确比较；
- TypedKey 独立执行 Context canonical trim，RawDefinition lexical 不改写；
- RuleView 第一遍暂存，在全部 System 登记后按自身 ownerToken 查找实际 SystemKey；
- 支持规则文档位于 System 前后、非最近 System、多 System、多 RuleView 和同名 owner 隔离；
- RuleView owner 不存在时产生 `symbol.owner.system.missing`；
- Information 第二遍绑定 SystemKey，Produce 第二遍绑定 ActionKey 并使用 sourceOrdinal；
- 同 TypedKey 重复拒绝，首定义不覆盖，Diagnostic 关联双方 SourceRef；
- Diagnostic 使用 LinkedHashSet 单次 add 聚合，最终稳定排序；
- 两遍完整扫描后统一失败，任一 ERROR 都不发布部分 SymbolTable；
- SymbolTable 包装 Context ImmutableRegistry，keys/definitions 稳定有序且不可变；
- RawReference 不解析、不执行 I/O；T08 未启动。

## 流程证据

- R02 RED Head：`619714e24fd5e37fc186897485aef1f9039c6209`
- RED Run：`30818564155`，I002 9 failures / 0 errors，R01 Symbol 23/23 PASSED
- Rejected Skeleton：`15f6e0e8ef9b`，Run `30818790734`
- Rejected GREEN：`a74fa3962641`，Run `30819131805`
- Clean-code Head：`ffe544e3060dd15b82a73677b30147aaa4b360af`
- Final clean-code P0：`30819541292` — SUCCESS
- Artifact：`8858227740`
- SHA-256：`e976842a19ff208a951e143e0e66e90a2c2fb75d4782c1c26850f133cde15356`，独立比对一致
- Surefire XML：62 个

## 最终测试

- Symbol：32/32
- Compiler：161/161
- XML：30/30
- YAML：59/59
- Context 正常：26/26
- Demo：4/4
- Legacy declaration：1/1
- 故意失败门禁：1 项按预期失败并被识别
- 12 模块 Reactor、Java release 8：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Review 与 Revision Integrity

- Review：`REV-000324`～`REV-000338`
- Evidence：`EVD-000567`～`EVD-000585`
- Open P0/P1/P2：`0 / 0 / 0`
- R28 first commit：`b717288297a5c78a79584412909f7e74550f7beb`
- R28 blob：`142ec612eb5658f41108330a4ca5b545521fd85c`
- R24 first commit：`577c68cb5b79993909660485110f11f4f8495f7a`
- R24 blob：`7a041c5c3811c1725482ee0b5ad288428c745a4e`
- R28/R24 在 RED 前创建，clean-code Head 复核未变化

## 范围与编码规范

- 生产修改仅在 `dec-core-compiler.symbol`；
- 测试修改仅在 `dec-core-compiler.symbol` 测试包；
- 未修改 Context、Raw、Frontend、SourceGraph 或 Compiler API 生产合同；
- Java release 8；
- 所有新增或修改的 `@Override` 独占一行；
- 方法、构造器和重要 owner、RuleView、Diagnostic、资源、失败逻辑使用中文注释；
- 无 static mutable Registry 或全局 Session 状态。

## 下一步

PR #22 完成最终文档化 Head 验证后可转为 Ready for Review。未经用户明确授权不得合并；PR 合并前 `TASK-P1-T08` 保持阻断。
