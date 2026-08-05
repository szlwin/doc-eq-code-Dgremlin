# TASK-P1-T12 / I006 — comparison operation 资源边界返工

- Status：`IN_PROGRESS / DESIGN_PASSED`
- Base：`PR27@956e51b998068b726eefc4ccfbafe12f868ca72b`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated History：`COMPLETION-P1-T12-R01@c6a515820972`、`R02@5d5a7d72119b`、`R03@4d4cd5c4c049`、`R04@923129b1f20d`、`R05@304a2156ff5e`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R43@P1-T12-REWORK-I006`
- Plan：`TP-P1-COMPILER-F01-R39@P1-T12-REWORK-I006`
- Review：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 3 / 1`

## Findings

- `FND-P1-T12-I006-001` `[P1][BLOCKER][RESOURCE]`：comparison budget 前整体复制外部 Set/Map，并信任外部 List.size() 预分配；
- `FND-P1-T12-I006-002` `[P1][BLOCKER][MEMO]`：公开查询每个候选新建 ComparisonSession，operation-level pair/canonical cache 丢失；
- `FND-P1-T12-I006-003` `[P1][BLOCKER][RESOURCE]`：任意 List 使用 size()+get(index)，LinkedList 可 O(n²)；
- `FND-P1-T12-I006-004` `[P2][ORACLE]`：缺少超宽/无限 iterator、异常 size、非 RandomAccess、多候选共享子图等阻断 Oracle。

## Goal

交付单次公开操作共享的 `ComparisonOperation`、EQUAL/NOT_EQUAL/VISITING pair cache、Iterator-driven List equality 和增量 canonicalization。所有外部容器读取、临时保存与任务压栈必须先通过预算；资源拒绝稳定使用 `ComparisonLimitException`。

## History preservation

R38～R42、R34～R38、I001～I005 的 RED、Architecture、Development、Review、Testing、Completion、CI、Artifact 和 documented Head 均保持原文件及原 SHA，不回写为通过。I006 使用新的 Revision、Evidence、Review 和 Completion。

## Stop conditions

- R43/R39 未早于 I006 RED；
- 仍存在 `new ArrayList<>(external Set/Map)`；
- 仍用外部 List.size() 预分配或 get(index) 遍历；
- 一个公开查询内为每个候选新建独立 Session；
- EQUAL/NOT_EQUAL pair 不能跨候选复用；
- 无限 iterator 不能在 edge 上限前稳定停止；
- 临时索引在预算前物化；
- I001～I005、prepare/commit、Diagnostic、Clock、Deadline、Context/Result 合同回归；
- Open P0/P1/P2 未清零；
- 未完成最终 P0、Artifact、Revision Integrity 和独立 Review；
- 用户未授权时合并 PR #27 或启动 T13。
