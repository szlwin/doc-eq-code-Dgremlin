# TASK-P1-T12 R04 Completion 失效记录

- Review：`NEEDS_CHANGES / REWORK`
- Reviewed Head：`2e113984973232d2d9a1d35bb886f73488f539c8`
- Invalidated Completion：`COMPLETION-P1-T12-R04@923129b1f20d`
- New Iteration：`TASK-P1-T12 / I005`
- Open P0/P1/P2：`0 / 1 / 1`
- Next Design：`DESIGN-R42@P1-T12-REWORK-I005`
- Next Plan：`TP-P1-COMPILER-F01-R38@P1-T12-REWORK-I005`

## Independent findings

### FND-P1-T12-I005-001 — P1 BLOCKER RESOURCE

I004 已关闭 freeze traversal、共享 DAG 复制、snapshot budgets 与构建期 hash 放大，但冻结结果公开读取仍沿用递归 Collection equality：

- `FrozenList` 继承 `AbstractList.equals()`；
- `FrozenSet.contains()` 调用 `values.contains()`；
- `FrozenMap.get/containsKey()` 调用 `Objects.equals()`；
- `FrozenEntrySet.contains()` 调用 `entries.contains()`。

两个独立 freeze Session 的结构相同共享 DAG 在 equals/get/contains 中会按路径指数展开，而非按唯一图规模处理。该问题发生于 Pipeline 已成功返回 Result 后，不受 freeze 四类预算约束。

### FND-P1-T12-I005-002 — P2 ORACLE

I004 测试没有覆盖 FrozenList.equals、FrozenSet.equals/contains、FrozenMap.equals/get/containsKey、FrozenEntrySet.contains，也没有 leaf equals 调用计数、identity-pair memo、comparison budgets、跨 freeze Session 或普通外部容器合同 Oracle。

## Gate

- R04 的所有 Design、Plan、RED、Architecture、Development、Review、Completion、CI 与 Artifact 保留为不可变历史；
- R04 不得继续作为当前有效 Completion；
- PR #27 不允许合并；
- TASK-P1-T13 保持 `BLOCKED_UNTIL_PR_27_MERGE`；
- 只有 I005 Open P0/P1/P2=`0/0/0` 且最终 P0/Artifact/Revision Integrity 通过后才能形成新 Completion。
