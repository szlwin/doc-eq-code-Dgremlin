# TASK-P1-T08 / I002 Design 与 Plan Evidence

- Design：`DESIGN-R30@P1-T08-REWORK-I002`
- Design first commit：`04c590caba096b999d2320e364b464143f24f3e0`
- Design blob：`5f392e855b5f5e3a3dc93e19f02c03db57cebe11`
- Plan：`TP-P1-COMPILER-F01-R26@P1-T08-REWORK-I002`
- Plan first commit：`dbea77b8698648acc35cbdb947687c58597d6612`
- Plan blob：`6ab25d67c788933d12e76206636590880c0c3598`
- Effective RED Head：`bfc8e4df822a54e072e2c3c79c011adee204a6ab`
- Clean-code Head：`bab0993ecfd8c344beead62712ba8dc02621038d`
- Status：`PASSED`
- Reviews：`REV-000354`～`REV-000357`、`REV-000371`
- Evidence：`EVD-000601`～`EVD-000604`、`EVD-000620`

## 冻结时序

R30 与 R26 均在 I002 任何新测试和生产实现前创建。有效 RED、Architecture Skeleton、GREEN 和 clean-code Head 复核时，两份文档 blob 均与首次冻结内容一致。

## 冻结合同

1. RawDefinitionSet 与 SymbolTable 必须绑定同一完整值语义快照；
2. 不一致返回 `reference.input.snapshot-mismatch`，不得建立解析索引；
3. qualified Information 必须严格为两个非空 segment；
4. 所有 TypedKey 构造失败转换为稳定 Diagnostic；
5. lexical 失败分类使用预聚合摘要，查询平均 O(1) 或 O(log n)；
6. System 声明节点缺失 ref/name 必须 fail-closed；
7. 真实 Canonical → T06 → T07 → T08 集成路径必须进入验收；
8. 不改变 Context、T06 Raw、T07 Symbol 公共合同；
9. 不启动 T09/T10/P2～P7；
10. `@Override` 独占一行，方法和重要逻辑使用中文注释。

## 历史保留

R29/R25、TASK-P1-T08/I001、COMPLETION-P1-T08-R01 及其 Review、Evidence、P0、Artifact 和失败 attempt 均未覆盖或删除。R01 仅失去当前有效性，继续作为不可变历史存在。