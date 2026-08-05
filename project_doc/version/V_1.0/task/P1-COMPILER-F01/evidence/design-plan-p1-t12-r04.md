# TASK-P1-T12 I004 Design / Plan Evidence

- Evidence：`EVD-000885`～`EVD-000888`
- Review：`REV-000570`～`REV-000572`
- Design：`DESIGN-R41@P1-T12-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R37@P1-T12-REWORK-I004`
- Base：`PR27@cf6e7dbe18d2f172dc4c68c793f45d9ecfbabe9d`

## Revision facts

- R41 first commit：`37dc26b5297dc73d5ac8f167df04ef62c9d3d97e`
- R41 first/final blob：`058f60f38649f3d7557eaf821bff3df37a3ea37c`
- R37 first commit：`c06f715d414af37474076efe5bce1cd933248177`
- R37 first/final blob：`3d1200fd5a4339c91577aaa78873ac05beb68914`
- R41/R37 均早于 I004 有效 RED，且未在实现阶段回写。

## Frozen decisions

- 使用显式 traversal stack，禁止依赖 JVM 方法递归栈；
- 区分 VISITING 循环与 FROZEN 共享 DAG，并复用完成态 immutable snapshot；
- 默认预算：depth=256、unique containers=4096、edges=65536、map entries=16384；
- 资源超限映射为 `MIX-PUBLICATION-BLOCKED / pipeline.artifact.resource-exceeded`；
- Set/Map collision 使用 canonical structural identity，不通过递归 hash 展开共享图；
- R01～R03 Completion 全部保留为失效历史，PR #27 不自动合并。

Design / Plan Review：`PASSED_FOR_I004_RED`。
