# TASK-P1-T12 I005 Architecture Skeleton Evidence

- Architecture：`DEVSKEL-P1-T12-R05@c3a78498e595`
- Design：`DESIGN-R42@P1-T12-REWORK-I005`
- Plan：`TP-P1-COMPILER-F01-R38@P1-T12-REWORK-I005`
- Valid RED：`c3a78498e595d0006334c8ec382c72c830142d19`
- Evidence：`EVD-000915`～`EVD-000922`

## Frozen skeleton

- 新增 package-private `ArtifactComparisonSupport`，所有 equality/query 使用显式 pair work stack，不依赖 Java 容器递归 equals；
- 单次比较维护 identity `(left,right)` pair memo，同一共享 pair 只展开一次；
- 比较默认预算固定为 depth=256、pairs=16384、edges=131072、canonical nodes=16384；
- package-private `ComparisonLimits` 和 `controlledEquals` 仅用于精确边界 Oracle，不形成 Compiler 公共 API；
- List/Optional 按顺序比较；Set/Map 在同一比较 Session 中构建跨 freeze Session canonical metadata，完成无序匹配；
- 内部 Frozen 容器的缓存 hash 仅作为快速拒绝，hash 相同后仍进行有预算的精确比较；
- 普通外部 List/Set/Map/Optional 查询值由显式遍历读取，不调用其容器 equals/hashCode；
- `FrozenList` 覆盖 equals/contains/indexOf/lastIndexOf；`FrozenSet` 覆盖 equals/contains；`FrozenMap` 覆盖 equals/get/containsKey/containsValue；Entry 与 EntrySet 同样使用受控比较；
- comparison 超限使用内部 `ComparisonLimitException`，不捕获或依赖 JVM Error；
- I004 的 freeze stack、VISITING/FROZEN memoization、四类 snapshot budget、collision、cached hash 与 Pipeline failure mapping 均保持不变。

Architecture checkpoint：`PASSED_FOR_GREEN`。
