# TASK-P1-T08 独立 Review — I002 返工输入

- Review：`REV-000353`
- Reviewer：`IndependentReviewAgent`
- Reviewed Head：`9ece664412ee947f536e2de73f20b5c7b9790bf1`
- Reviewed Completion：`COMPLETION-P1-T08-R01@ab432a3189f4`
- Result：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 3 / 1`
- Evidence：`EVD-000600`
- Source：用户提供的《TASK-P1-T08 独立 Review 报告》

## 历史有效性

`COMPLETION-P1-T08-R01@ab432a3189f4` 被本次独立 Review 推翻，不再是当前有效 Completion；R01 的 Completion、Review、Evidence、P0、Artifact 与失败 attempt 全部作为不可变历史保留，不覆盖、不删除。

PR #23 在 I002 完成前保持 Draft 且不得合并；TASK-P1-T09 继续阻断。

## Findings

### FND-P1-T08-I002-001 — P1 / BLOCKER

非法或不完整 lexical 未 fail-closed：

- qualified Information 必须严格为两段 `system.name`，多段、空 owner、空 name 均返回 `reference.owner.invalid`；
- View `target-main`、System `data-ref/view-ref@name`、缺失 ref/name 节点等 T06 合法输入不得让 TypedKey 构造异常越过结果边界；
- `ReferenceResolver.resolve()` 对全部 T06 合法输入只能返回 `RESOLVED` 或 `FAILED`。

### FND-P1-T08-I002-002 — P1 / BLOCKER

lexical 失败分类索引使用 `Map<String, List<DefinitionKey>>`，owner-scoped 同名 Key 与多引用组合会形成 O(N×M) 扫描。必须改为预聚合类型摘要，使任意类型、期望类型、RuleView 存在性及稳定 related SourceRef 查询为平均 O(1) 或 O(log n)，并提供可计数小预算 Oracle。

### FND-P1-T08-I002-003 — P1 / BLOCKER

`RawDefinitionSet` 与 `SymbolTable` 未绑定同一完整输入快照，仅按 sourceOrdinal 恢复 sourceKey 可静默错绑。必须在建立任何解析索引前验证完整 RawDefinitionSet 快照一致性；不一致返回：

```text
MIX_REF_UNKNOWN
reference.input.snapshot-mismatch
```

至少覆盖 kind、sourceOrdinal、sourceRef、ownerToken、name、attributes、references、body、format、schemaVersion，以及新增、删除、重新编号/排序和上一 revision SymbolTable。

### FND-P1-T08-I002-004 — P2

现有手工 Raw fixture 未充分覆盖真实 T06 body 路径。必须增加：

```text
CanonicalDocumentNode
→ RawDefinitionBuilder
→ SymbolTableBuilder
→ ReferenceResolver
```

覆盖 View target-main、nested property data/ref-property、System ref/name、RuleView 前向、Directory/Produce qualified Information、全部 malformed lexical 与不同快照输入。

## 返工目标

建立 `TASK-P1-T08 / I002`、`DESIGN-R30@P1-T08-REWORK-I002` 与 `TP-P1-COMPILER-F01-R26@P1-T08-REWORK-I002`，重新执行 TDD RED、Architecture Skeleton 双 Review、具体实现、最终 Code Review、全量 P0、Artifact 独立校验、Revision Integrity 与 Completion R02。