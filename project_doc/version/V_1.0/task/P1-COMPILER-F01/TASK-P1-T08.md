# TASK-P1-T08 / I002 — P1 强类型引用解析返工

- 状态：`REWORK / IN_PROGRESS`
- Base：`PR-23@9ece664412ee947f536e2de73f20b5c7b9790bf1`
- Dependency：`COMPLETION-P1-T07-R02@ffe544e3060d`
- Branch：`feature/p1-t08-reference-resolution-20260803-2254`
- PR：`#23`（Draft）
- Design：`DESIGN-R30@P1-T08-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R26@P1-T08-REWORK-I002`
- Invalidated Completion：`COMPLETION-P1-T08-R01@ab432a3189f4`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Review Input：`REV-000353 / NEEDS_CHANGES`

## I001 历史

`TASK-P1-T08 / I001`、R29/R25、R01 Completion、Review、Evidence、P0、Artifact 和失败 attempt 全部作为不可变历史保留。独立 Review 发现 3 个 P1 与 1 个 P2 后，R01 不再是当前有效 Completion，但不得覆盖或删除。

## 当前 Findings

- `FND-P1-T08-I002-001`：非法或不完整 lexical 未 fail-closed；
- `FND-P1-T08-I002-002`：lexical 失败分类存在 O(N×M) 候选扫描；
- `FND-P1-T08-I002-003`：RawDefinitionSet 与 SymbolTable 未绑定同一完整快照；
- `FND-P1-T08-I002-004`：缺少 Canonical → T06 → T07 → T08 真实 body 集成 Oracle。

Open P0/P1/P2：`0 / 3 / 1`。

## I002 冻结目标

- qualified Information 严格为两段 `system.name`；
- 所有 TypedKey 输入先经过统一 lexical fail-closed 边界；
- System data-ref/view-ref 缺失 ref/name 必须返回 `reference.owner.invalid`；
- SymbolTable package-private 保存完整 RawDefinitionSet 输入快照；
- Resolver 在任何索引前验证快照，不一致返回 `reference.input.snapshot-mismatch`；
- lexical 失败分类使用预聚合摘要，查询平均 O(1) 或 O(log n)；
- 使用可计数小预算 Oracle 验证查询次数不随同名候选数放大；
- 增加真实 CanonicalDocumentNode → RawDefinitionBuilder → SymbolTableBuilder → ReferenceResolver 集成测试；
- 保持 R29 正常引用矩阵、精确 TypedKey 成功查询和失败不发布部分结果。

## 当前 Gate

- PR #23：Draft、未合并；
- R30：PASSED；
- R26：PASSED；
- 下一阶段：新增 I002 Oracle 并取得 Java 8 编译成功、errors=0 的有效 RED；
- Architecture Skeleton 双 Review 未通过前不得进入具体实现；
- PR #23 合并前 TASK-P1-T09 继续阻断。

## 编码规范

- Java release 8；
- 所有 `@Override` 注解独占一行；
- 方法、构造器和重要快照、lexical、复杂度、owner、Diagnostic、资源与失败逻辑使用中文注释；
- 不修改 Context、T06 Raw 或 T07 Symbol 公共合同；
- 不引入模糊搜索、跨类型降级、I/O、运行时执行或 static mutable registry；
- 不启动 T09/T10/P2～P7。