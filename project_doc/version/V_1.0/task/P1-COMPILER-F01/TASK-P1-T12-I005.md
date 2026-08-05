# TASK-P1-T12 / I005 — 冻结结果 Equality/Query 资源边界返工

- Status：`IN_PROGRESS / DESIGN_PASSED`
- Base：`PR27@2e113984973232d2d9a1d35bb886f73488f539c8`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated History：`COMPLETION-P1-T12-R01@c6a515820972`、`COMPLETION-P1-T12-R02@5d5a7d72119b`、`COMPLETION-P1-T12-R03@4d4cd5c4c049`、`COMPLETION-P1-T12-R04@923129b1f20d`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R42@P1-T12-REWORK-I005`
- Plan：`TP-P1-COMPILER-F01-R38@P1-T12-REWORK-I005`
- Review：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 1 / 1`

## Findings

- `FND-P1-T12-I005-001` `[P1][BLOCKER][RESOURCE]`：Frozen List/Set/Map/Entry 的 equality/query 对共享 DAG 仍可能指数展开；
- `FND-P1-T12-I005-002` `[P2][ORACLE]`：缺少 equality/query 的 leaf 调用计数、pair/edge/depth 预算、跨 freeze Session、普通外部容器和 Collection 合同测试。

## Goal

交付非递归 equality/query、identity-pair memoization、共享 canonicalization、comparison depth/pair/edge/node 预算及稳定超限异常；冻结结果与普通 Java 容器的查询和比较必须精确、受控，并保持 Collection equals/hashCode 合同。

## History preservation

R38～R41、R34～R37、I001～I004 的 RED、Architecture、Development、Review、Testing、Completion、CI、Artifact 和 documented Head 均保持原文件及原 SHA，不回写为通过。I005 使用新的 Revision、Evidence、Review 和 Completion。

## Stop conditions

- R42/R38 未早于 I005 RED；
- equality/query 仍依赖 AbstractList/AbstractSet/AbstractMap 的递归默认实现；
- 没有 identity-pair memoization；
- 缺少 comparison depth、pair、edge 或 canonical-node 任一预算；
- 24 层共享 DAG 的 equals/get/contains 仍指数展开；
- 超限依赖 JVM Error、超时或线程中断；
- hash collision 可误判相等；
- 普通外部容器 query 会触发其容器 equals/hashCode；
- I001～I004、prepare/commit、Diagnostic、Clock、Deadline、Context/Result 合同回归；
- Open P0/P1/P2 未清零；
- 未完成最终 P0、Artifact、Revision Integrity 和独立 Review；
- 用户未授权时合并 PR #27 或启动 T13。
