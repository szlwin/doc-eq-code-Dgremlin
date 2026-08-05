# TASK-P1-T12 I005 独立 Review 输入

- Evidence：`EVD-000910`～`EVD-000914`
- Repository：`szlwin/doc-eq-code-Dgremlin`
- PR：`#27`
- Reviewed Head：`2e113984973232d2d9a1d35bb886f73488f539c8`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Branch ahead/behind：`123 / 0`
- Review result：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 1 / 1`

## Confirmed evidence

1. `FrozenList` 只覆盖 get/size/hashCode，继承递归 `AbstractList.equals()`；
2. `FrozenSet.contains()` 直接调用 `values.contains()`；
3. `FrozenMap.get/containsKey()` 对 key 调用 `Objects.equals()`；
4. `FrozenEntrySet.contains()` 直接调用 `entries.contains()`；
5. 独立探针在 24 层共享 DAG 上超过 2,000,000 次 leaf equals；
6. I004 最终 CI 与 Artifact 有效，但测试集未覆盖 equality/query 资源复杂度。

## Required next action

建立 `TASK-P1-T12 / I005`、`DESIGN-R42@P1-T12-REWORK-I005`、`TP-P1-COMPILER-F01-R38@P1-T12-REWORK-I005`，完成有效 RED、非递归比较实现、独立 Review、全量 CI/Artifact 和新 Completion；R01～R04 保留为失效历史。
