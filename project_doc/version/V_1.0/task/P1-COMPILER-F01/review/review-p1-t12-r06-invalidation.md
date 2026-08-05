# TASK-P1-T12 R03 Completion Invalidation Review

- Review：`REV-000566`～`REV-000569`
- Result：`NEEDS_CHANGES / REWORK`
- Invalidated Completion：`COMPLETION-P1-T12-R03@4d4cd5c4c049`
- Reviewed Head：`cf6e7dbe18d2f172dc4c68c793f45d9ecfbabe9d`
- Open P0/P1/P2：`0 / 1 / 1`

## Confirmed findings

### FND-P1-T12-I004-001 `[P1][BLOCKER][RESOURCE]`

`ArtifactSnapshots.freeze()` 使用 JVM 递归和 active-path identity 集合，只能检测循环；完成容器后删除 identity，没有 FROZEN memoization，也没有 depth、unique container、edge 或 map-entry 预算。

独立复现：

- `-Xss1m` 深层无环单元素 List 链可触发 `StackOverflowError`；
- `-Xmx512m` 24 层共享 DAG 可触发 `OutOfMemoryError`；
- 两者均属于 `Error`，可越过只捕获 `RuntimeException` 的 Pass 边界；
- 结果无法稳定形成 `PipelineExecutionResult`，具有确定性资源拒绝风险。

### FND-P1-T12-I004-002 `[P2]`

现有 I003 Oracle 未覆盖深层无环图、共享 DAG、资源预算、宽容器、操作次数和资源失败后的 publisher=0。

## Decision

- R03 Completion 失效但全部历史原样保留；
- 开启 `TASK-P1-T12 / I004`；
- 使用 `DESIGN-R41@P1-T12-REWORK-I004` 与 `TP-P1-COMPILER-F01-R37@P1-T12-REWORK-I004`；
- PR #27 保持 Draft 且禁止合并；
- TASK-P1-T13 保持阻断。
