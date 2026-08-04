# TASK-P1-T08 / I002 Development Evidence

- Revision：`DEV-P1-T08-R02@bab0993ecfd8`
- Architecture：`DEVSKEL-P1-T08-R02@3e85814d5cf5`
- Clean-code Head：`bab0993ecfd8c344beead62712ba8dc02621038d`
- Status：`PASSED`
- Reviews：`REV-000362`～`REV-000368`
- Evidence：`EVD-000611`～`EVD-000617`

## FND-P1-T08-I002-001 — CLOSED

- 新增 `ReferenceTargetParser`；
- simple target 在 TypedKey 构造前执行 trim/nonblank 校验；
- qualified Information 必须恰好一个点且两段均非空；
- `user.active.extra`、` .active`、`user. ` 均返回 `reference.owner.invalid`；
- View target-main、nested data/property、System data-ref/view-ref、RuleView、Action、Directory rel 全部进入统一安全构造边界；
- System 声明节点缺失 ref/name 产生稳定 owner Diagnostic；
- `resolve()` 的最后输入防线将输入相关 `IllegalArgumentException` 转换为失败结果，不允许异常越过 `ReferenceResolutionResult`。

## FND-P1-T08-I002-002 — CLOSED

- 删除 `Map<String, List<DefinitionKey>>` 候选列表；
- 新增 `ReferenceLexicalIndex.CandidateSummary`；
- 每个 lexical 仅保存任意类型稳定代表与每种 Key 类型稳定首个代表；
- 任意类型、期望类型、RuleView 与 related SourceRef 查询均为平均 O(1)；
- package-private `LookupObserver` 只用于确定性小预算计数，无 static mutable state；
- 12 个不同 owner 的同名候选与 9 个 owner mismatch 引用只触发 9 次摘要查询。

## FND-P1-T08-I002-003 — CLOSED

- `SymbolTable` package-private 保存生成它的完整 `RawDefinitionSet`；
- `SymbolTableBuilder` 在成功发布时传递同一快照；
- `ReferenceResolver` 在任何 sourceKey、lexical、System View 或 Data property 索引前比较完整 RawDefinitionSet 值语义；
- name、kind、sourceRef/body、增加、删除、重编号和上一 revision 快照不一致均返回单一 `reference.input.snapshot-mismatch`；
- 快照失败不开始解析、不发布部分引用集合。

## FND-P1-T08-I002-004 — CLOSED

新增 `ReferenceResolverCanonicalIntegrationTest`，主夹具经过：

```text
CanonicalDocumentNode
→ RawDefinitionBuilder
→ RawDefinitionSet
→ SymbolTableBuilder
→ SymbolTable
→ ReferenceResolver
```

覆盖 View target-main、nested data/ref-property、System ref/name、RuleView 前向文档顺序、Directory/Produce qualified Information、全部指定 malformed lexical 与不同快照。

## 范围与编码

- 生产修改仅位于 `dec-core-compiler.symbol`；
- `SymbolTableBuilder` 仅改变一个构造参数传递，不改变注册算法；
- 未修改 Context、T06 Raw 或 T07 Symbol 公共合同；
- 所有 `@Override` 独占一行；
- 方法、构造器和关键快照、lexical、复杂度、owner、Diagnostic 与失败逻辑均有中文注释；
- 无模糊匹配、跨类型降级、I/O、网络、反射执行或 static mutable registry；
- 未启动 T09/T10/P2～P7。