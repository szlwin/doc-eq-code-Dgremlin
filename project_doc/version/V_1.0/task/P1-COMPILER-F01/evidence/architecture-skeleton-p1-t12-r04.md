# TASK-P1-T12 I004 Architecture Skeleton Evidence

- Architecture：`DEVSKEL-P1-T12-R04@c82e0a3023da`
- Design：`DESIGN-R41@P1-T12-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R37@P1-T12-REWORK-I004`
- First Skeleton Commit：`c82e0a3023da9a74c4a7f3ccc4c34bb83655941d`
- Evidence：`EVD-000879`～`EVD-000884`

## Frozen skeleton

- `ArtifactSnapshots` 使用显式 work stack，不再通过 Java 方法递归遍历容器；
- 每次 freeze Session 同时维护 VISITING identity 与 FROZEN identity→snapshot；
- FROZEN 共享子图直接复用同一 immutable snapshot，VISITING 再遇到仍判定循环；
- 默认 budget 固定为 depth=256、unique containers=4096、traversed edges=65536、map entries=16384；
- package-private limits 仅用于小预算边界测试，不形成公共 API；
- 资源超限使用内部专用异常，由 Pipeline 映射为 `MIX-PUBLICATION-BLOCKED / pipeline.artifact.resource-exceeded`；
- List/Set/Map/Optional 顺序、不可变输出、null 拒绝、Map/Set collision 和循环合同继续保留；
- 目标 Set/Map 不得通过递归 `hashCode()` 展开共享 DAG，collision 使用底向上的 canonical structural ID；
- 尚未修改生产行为，且该 Skeleton commit 早于 I004 有效 RED。

Architecture checkpoint：`PASSED_FOR_RED`。
