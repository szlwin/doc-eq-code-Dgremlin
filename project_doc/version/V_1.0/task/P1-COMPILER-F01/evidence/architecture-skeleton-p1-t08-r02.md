# TASK-P1-T08 / I002 Architecture Skeleton R02

- Revision：`DEVSKEL-P1-T08-R02@cad63d3d19af`
- Design：`DESIGN-R30@P1-T08-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R26@P1-T08-REWORK-I002`
- TDD：`TDD-P1-T08-R02@bfc8e4df822a`
- Status：`PASSED / CONTROLLED_RED`
- Reviews：`REV-000358`～`REV-000359`
- Evidence：`EVD-000605`～`EVD-000608`

## 已落地骨架

1. `ReferenceTargetParser`
   - simple lexical trim/nonblank 边界；
   - qualified Information 恰好一个点、两段均非空；
   - parser 无副作用，Diagnostic 保持由 Resolver 统一发布。
2. `ReferenceLexicalIndex`
   - lexical → 类型摘要；
   - 每种 TypedKey 类型仅保存稳定首个代表；
   - 任意类型、期望类型、RuleView 与 related SourceRef 代表均可平均 O(1) 查询；
   - 不保存或扫描同 lexical 候选 List。
3. `ReferenceSnapshotBinding`
   - 使用 RawDefinitionSet 完整值语义；
   - 首差异 SourceRef/relatedRefs；
   - 冻结 `reference.input.snapshot-mismatch` Diagnostic。
4. 真实 Canonical 集成入口与 I002 小预算复杂度 Oracle 已在 RED 前建立。

## 受控 RED

当前骨架只冻结边界类型，尚未改写 `SymbolTable` 输入快照、`ReferenceResolver` Role Policy 和 LookupObserver seam，因此 I002 新 Oracle 应继续受控失败；I001 正常引用矩阵及全部既有回归必须保持通过。

## Review

- `REV-000358` — ArchitectureReviewAgent — `PASSED`
  - 边界拆分无循环依赖；
  - Parser、Index、Snapshot 均为 package-private 无状态/单次调用结构；
  - 没有修改 Context、T06 Raw 或 T07 Symbol 公共合同；
  - O(D+R) 目标可由骨架实现。
- `REV-000359` — SpecComplianceReviewAgent — `PASSED`
  - 覆盖四个 Finding 的对应架构接缝；
  - 不侵入 T09/T10/P2～P7；
  - Skeleton 未伪造 GREEN。

下一阶段：将完整 RawDefinitionSet 快照绑定到 SymbolTable，接入 LookupObserver、lexical summary 与 parser，并关闭全部 Finding。