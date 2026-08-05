# CODEREVIEW-P1-T12-R12 — R11/R06 重新审查失效记录

- Revision：`PR27@a59a39fde202366742963658bf07797c9537de57`
- Result：`NEEDS_CHANGES`
- Invalidated Review：`CODEREVIEW-P1-T12-R11@ce8c92523256`
- Invalidated Completion：`COMPLETION-P1-T12-R06@ce8c92523256`
- Preservation：`INVALIDATED / PRESERVED`
- Open P0/P1/P2：`0 / 0 / 1`

## Finding

### FND-P1-T12-I007-001 `[P2][SPEC][CORRECTNESS][ORACLE]`

`ArtifactComparisonOperation.FinishPairsTask` 在 Map canonical pair 排序后直接构造 parts 并 intern node，没有检查相邻 pair 是否具有相同 canonical key ID。`FinishSequenceTask` 同样没有复核 identity-backed Set 的 duplicate canonical element。

因此两个各自包含 equality-equal、identity-distinct key 的 `IdentityHashMap` 可能得到相同 MAP canonical node 并被判为相等，违反 R06 Architecture 中“相同 canonical key 的 Map 判定为非法碰撞”以及 I001～I005 collision/fail-closed 合同。

## Gate decision

- R11 原 Review 与 R06 Completion 文件、CI、Artifact、Revision Lock 均保持原样，不删除、不覆盖；
- R11/R06 不再作为当前 PASSED 事实；
- TASK-P1-T12 进入 I007；
- PR #27 转为 Draft，暂不合并；
- TASK-P1-T13 继续 `BLOCKED_UNTIL_PR_27_MERGE`；
- 需重新完成 Design、Plan、有效 RED、Architecture、Development、独立 Review、P0、Artifact 与 Completion。

## Review profiles

| Review Profile | Result |
|---|---|
| SpecComplianceReviewAgent | NEEDS_CHANGES |
| EngineeringStandardsReviewAgent | PASSED |
| PerformanceReviewAgent | PASSED |
| TestEvidenceReviewAgent | NEEDS_CHANGES |
| ArchitectureReviewAgent | NEEDS_CHANGES |
| MaintainabilityReviewAgent | PASSED_WITH_CLEANUP |
| SecurityReviewAgent | NOT_APPLICABLE |

非阻断清理项：删除无调用点的 private `ConditionalCompareTask`。
