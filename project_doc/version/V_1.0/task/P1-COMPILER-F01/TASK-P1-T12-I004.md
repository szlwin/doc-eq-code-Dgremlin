# TASK-P1-T12 / I004 — Artifact Snapshot 资源边界返工

- Status：`IN_PROGRESS / DESIGN_PASSED`
- Base：`PR27@cf6e7dbe18d2f172dc4c68c793f45d9ecfbabe9d`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated History：`COMPLETION-P1-T12-R01@c6a515820972`、`COMPLETION-P1-T12-R02@5d5a7d72119b`、`COMPLETION-P1-T12-R03@4d4cd5c4c049`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R41@P1-T12-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R37@P1-T12-REWORK-I004`
- Review：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 1 / 1`

## Findings

- `FND-P1-T12-I004-001` `[P1][BLOCKER][RESOURCE]`：递归 snapshot 对深层无环图、共享 DAG 和宽容器没有资源边界，且已完成共享子图没有 identity memoization；
- `FND-P1-T12-I004-002` `[P2]`：缺少深度、共享 DAG、唯一节点、边、Map entry、宽容器、操作次数和 publisher=0 Oracle。

## Goal

交付非递归 artifact traversal、VISITING/FROZEN identity memoization、四类显式资源预算及稳定资源超限 Diagnostic；共享 DAG 必须线性遍历并复用冻结 identity，资源失败必须 `FAILED + publisher=0`。

## History preservation

R38～R40、R34～R36、I001～I003 的 RED、Architecture、Development、Review、Testing、Completion、CI、Artifact 和 documented Head 均保持原文件及原 SHA，不回写为通过。I004 使用新的 Revision、Evidence、Review 和 Completion。

## Stop conditions

- R41/R37 未早于 I004 RED；
- 仍使用 JVM 方法递归遍历容器；
- 没有 FROZEN identity memoization；
- 资源预算缺少 depth、unique containers、edges 或 map entries 任一项；
- 24 层共享 DAG 仍指数复制；
- 资源失败可越过 Pipeline 或 publisher 非 0；
- 循环、collision、prepare/commit、Diagnostic、Clock、Deadline、Context/Result Oracle 回归；
- Open P0/P1/P2 未清零；
- 未完成最终 P0、Artifact、Revision Integrity 和独立 Review；
- 用户未授权时合并 PR #27 或启动 T13。
