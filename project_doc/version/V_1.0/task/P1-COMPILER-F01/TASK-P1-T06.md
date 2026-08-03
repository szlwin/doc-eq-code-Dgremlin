# TASK-P1-T06 / I001 — RawDefinitionSet

- 状态：`COMPLETED`
- PR：`#21`
- Branch：`feature/p1-t06-raw-definition-20260803-1334`
- Base：`dev_all@17ce0834b947a75ff3ccbd24c7b1332fb93e8941`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Design：`DESIGN-R23@P1-T06-I001`
- Plan：`TP-P1-COMPILER-F01-R19@P1-T06-I001`
- TDD：`TDD-P1-T06-R01@8c5efd3dcbee`
- Architecture Skeleton：`DEVSKEL-P1-T06-R01@6033e59728e7`
- Development：`DEV-P1-T06-R01@90d483290cf3`
- Code Review：`CODEREVIEW-P1-T06-R01@90d483290cf3`
- Testing：`TESTING-P1-T06-R01@90d483290cf3`
- Completion：`COMPLETION-P1-T06-R01@90d483290cf3`
- Review：`REV-000257`～`REV-000269`
- Evidence：`EVD-000499`～`EVD-000511`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 完成事实

1. 新增格式中立 `dec.core.compiler.raw` 包及 8 个生产类型；
2. 严格验证 `orm-config`、`orm-data-mapping`、`orm-view-mapping`、`systems`、`orm-rule-mapping`、`business-config` 六类根；
3. 生成 14 类 RawDefinition；
4. `sourceOrdinal` 按输入文档顺序和定义先序从 0 连续递增；
5. 保留 SourceRef、format、schemaVersion、owner/name、稳定 attributes、有序 references 和递归 normalized body；
6. reference 只保存 lexical target，不解析、不触发 I/O；
7. 父定义引用遍历在嵌套定义边界停止；
8. Builder 先验证整批输入，再提取和发布完整集合；
9. unknown root/child、null、缺 name/owner 均 `FAILED / MIX_STRUCTURE_UNKNOWN / empty set`；
10. 所有公开集合 defensive copy 且不可变；
11. `RawDefinitionSet` 强制 ordinal 为 `0..size-1`；
12. `RawBuildResult` Diagnostic 逐项 non-null、稳定排序并冻结；
13. 等价 XML/YAML Canonical 输入除 format 来源事实外产生相同 Raw 事实；
14. 无 parser 类型、无跨调用可变 registry、无 public mutator；
15. 未修改 Context、Source Graph、Canonical API、XML/YAML Frontend 生产代码；
16. 未启动 TypedKey、SymbolTable、引用解析、Pipeline 或 T07。

## 独立 Review Finding

- `FND-P1-T06-I001-001`：公开 Set 未强制 ordinal 连续，`CLOSED`；
- `FND-P1-T06-I001-002`：FAILED diagnostics 未逐项 non-null/排序，`CLOSED`；
- `FND-P2-T06-I001-003`：JaCoCo synthetic 字段造成测试假阳性，`CLOSED`；
- 开放 P0/P1/P2：`0 / 0 / 0`。

## Revision Lock

- Design first commit：`8a6cadfbb35f82820dd077a44033c6ba179ad77c`；
- Design blob：`f7eb09ff6291c3f84bb93ace88a6d301d9ef73ad`；
- Plan first commit：`5bab7c508e27762d306d672fd925f2c743fbd245`；
- Plan blob：`e5e65b495ffe01c8265061d975f2264d31b761e7`；
- R23/R19 在 RED 前创建，clean-code Head 复核未变化。

## TDD / Skeleton / GREEN

### RED

- Head：`8c5efd3dcbeea49b9a4e7a68e07aba5825e15618`；
- Run：`30788394890`；
- Artifact：`8846107158`；
- SHA-256：`0dd3625a723587d957640b518af5f6408ae0a695276e55e7f59f72268035f691`；
- T06：11 run / 11 expected failures / 0 errors；
- Java release 8 编译通过。

### Architecture Skeleton

- Head：`6033e59728e75011f3baf89d68c2919bd5ffd947`；
- Run：`30788597060`；
- Artifact：`8846182393`；
- SHA-256：`d1c866350ab28c76335830e0b03e12e2c79959a4e64edb905aa2c94383a0d451`；
- 值对象与结果接缝存在，业务行为继续受控 RED。

### Clean-code GREEN

- Head：`90d483290cf3943003624f21f19981535ca1408c`；
- Run：`30789608249`；
- Artifact：`8846541706`；
- SHA-256：`b0cf248154f392bf85a95c8903949efc16bf1a3bb264a2cbef72210df808b51f`；
- Artifact 已独立下载、校验 SHA-256 并解析 Surefire XML；
- T06：16/16；Compiler total：99/99；XML：30/30；YAML：59/59；Context 正常：26/26；Demo：4/4；Legacy：1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Evidence 入口

- Dependency/Base：`evidence/dependency-base-p1-t06-r01.md`；
- Revision Lock：`evidence/revision-lock-p1-t06-r01.json`；
- Design/Plan：`evidence/design-plan-p1-t06-r01.md`；
- RED：`evidence/tdd-red-p1-t06-r01.md`；
- Skeleton：`evidence/architecture-skeleton-p1-t06-r01.md`；
- Development：`evidence/development-p1-t06-r01.md`；
- Reviews：`review/review-p1-t06-r01.md`；
- Testing：`evidence/testing-p1-t06-r01.md`；
- Completion：`evidence/commands/completion-p1-t06-r01/completion-report.json`；
- 机器恢复：`../../tdd_p1_t06_r01_completion.json`。

## 历史保护与下一步

T01～T05 的 Design、Plan、TDD、Development、Review、Testing、Completion、Evidence 和 checkpoint 均未覆盖或删除。PR #21 未经明确授权不得合并；PR #21 合并前 `TASK-P1-T07` 保持未启动和阻断。
