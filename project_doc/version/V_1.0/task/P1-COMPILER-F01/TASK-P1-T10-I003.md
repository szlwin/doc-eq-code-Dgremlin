# TASK-P1-T10 / I003 — TypedKey Reference Lexical Rework

- Status：`IN_PROGRESS`
- Trigger：独立 Review `NEEDS_CHANGES / REWORK`
- Reviewed Head：`7e466e7cf0f28aa4062294923c27b5f59cbd355d`
- Invalidated：`COMPLETION-P1-T10-R02@6f4c7b6f3ec3`
- Design：`DESIGN-R35@P1-T10-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R31@P1-T10-REWORK-I003`
- PR：`#25`（Draft）
- Open P0/P1/P2 at start：`0 / 1 / 1`

R01/R02 全部历史不可变保留。I003 只修复 T10 结构门禁对 padded TypedKey reference 的 false-negative，并补齐真实 Canonical 跨阶段 Oracle；不启动 T11/P2～P7。
