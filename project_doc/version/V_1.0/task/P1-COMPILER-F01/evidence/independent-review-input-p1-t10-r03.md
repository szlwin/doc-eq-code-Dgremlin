# TASK-P1-T10 I003 Independent Review Input

- Review：`NEEDS_CHANGES / REWORK`
- Reviewed Head：`7e466e7cf0f28aa4062294923c27b5f59cbd355d`
- Invalidated Completion：`COMPLETION-P1-T10-R02@6f4c7b6f3ec3`
- Open P0/P1/P2：`0 / 1 / 1`
- Next Iteration：`TASK-P1-T10 / I003`

## 新 Finding

1. `FND-P1-T10-I003-001 [P1][BLOCKER]`：I002 结构门禁把 TypedKey reference 与精确 path lexical 共用同一“必须已 trim”规则，错误拒绝 nonblank padded `model-ref` 与 `ref@view`；这破坏了 T06 Raw lexical 保留、T07 TypedKey 独立规范化和 T08 reference trim 合同。
2. `FND-P1-T10-I003-002 [P2]`：现有 Oracle 只覆盖 blank、missing 与 padded path，未覆盖跨 T06/T07/T10 的 padded TypedKey reference 和 lexical-preservation。

## 必须保持的合同

- `model-ref`、`ref@view`：Raw lexical 只要求 non-null 且 trim 后非空；Raw 值不得改写，后续由 `ViewKey` 规范化。
- `read/write@path`、`ref@property`：继续要求 nonblank、已经 trim、无空 segment，并执行既有 wildcard/selector grammar。
- `definition.name` 与 `model-ref` 必须按原始 lexical 完全一致。
- Binding 的 `sourceModel/targetView` 必须发布 canonical `ViewKey`。
- R01/R02 全部 Design、Plan、RED、Architecture、Review、Completion、CI 与 Artifact 历史不可变保留。
- PR #25 在 I003 完成前不得合并，TASK-P1-T11 保持阻断。
