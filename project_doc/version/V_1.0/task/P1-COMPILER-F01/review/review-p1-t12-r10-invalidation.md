# CODEREVIEW-P1-T12-R10 — I005 independent re-review invalidation

- Revision：`PR27@956e51b998068b726eefc4ccfbafe12f868ca72b`
- Result：`NEEDS_CHANGES / REWORK`
- Invalidates Review：`CODEREVIEW-P1-T12-R09@304a2156ff5e`
- Invalidates Completion：`COMPLETION-P1-T12-R05@304a2156ff5e`
- Preservation：R09/R05 及其 CI、Artifact、Evidence 不删除、不覆盖，仅标记失效。
- Open P0/P1/P2：`0 / 3 / 1`

## Reopened findings

### FND-P1-T12-I006-001 `[P1][BLOCKER][RESOURCE]`

`CanonicalizeTask` 在 comparison budget 生效前使用外部 `List.size()` 预分配，并整体复制 Set/Map。预算无法在外部元素读取和临时物化之前稳定拒绝。

### FND-P1-T12-I006-002 `[P1][BLOCKER][MEMO]`

List/Set/Map/Entry 的公开查询循环对每个候选新建 ComparisonSession。虽然共享总预算，但 pair result、canonical cache 和 scalar intern table 被丢弃，同一共享子图可按候选数重复展开。

### FND-P1-T12-I006-003 `[P1][BLOCKER][RESOURCE]`

List equality 与 canonicalization 对任意 List 使用 `size()+get(index)`。普通 LinkedList 或非 RandomAccess List 可在逻辑 edge 未超限时产生 O(n²) 实际节点访问。

### FND-P1-T12-I006-004 `[P2][ORACLE]`

I005 Oracle 未覆盖外部超宽/无限 iterator、异常 size、LinkedList、多候选共享子图及 operation-level EQUAL/NOT_EQUAL cache。

## Review profile

| Review Profile | Result |
|---|---|
| SpecComplianceReviewAgent | FAILED |
| EngineeringStandardsReviewAgent | FAILED |
| PerformanceReviewAgent | FAILED |
| TestEvidenceReviewAgent | FAILED |
| ArchitectureReviewAgent | PASSED / RECHECK_REQUIRED |
| SecurityReviewAgent | NOT_APPLICABLE |

## Gate

- PR #27 转为 Draft；
- TASK-P1-T12 进入 I006；
- 必须重新执行 Design、Plan、有效 RED、Architecture、Development、独立 Review、P0、Artifact 与 Completion；
- PR #27 不得合并；
- TASK-P1-T13 保持 `BLOCKED_UNTIL_PR_27_MERGE`。
