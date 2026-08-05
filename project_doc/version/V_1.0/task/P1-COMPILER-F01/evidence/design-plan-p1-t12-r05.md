# TASK-P1-T12 I005 Design / Plan Evidence

- Design：`DESIGN-R42@P1-T12-REWORK-I005`
- Plan：`TP-P1-COMPILER-F01-R38@P1-T12-REWORK-I005`
- Evidence：`EVD-000931`～`EVD-000935`

## Revision facts

- R42 first commit：`6d109ab58793d080ba8f86d593040b3b5353b79d`
- R42 first/final blob：`ef4797a32aa30aac1cdd67e0d211705e1c6fb62e`
- R38 first commit：`2063c164d06246fc9f03c010e6443b45f44f6480`
- R38 first/final blob：`370678742f0a6e6cd0228a0c08b1400a36528031`
- Valid RED：`c3a78498e595d0006334c8ec382c72c830142d19`

R42/R38 均在有效 RED 前冻结，最终 blob 与首次提交完全一致。

## Frozen contract

- equality/query 使用显式 pair stack；
- 同一 `(left identity,right identity)` pair 只展开一次；
- comparison budgets：depth=256、pairs=16384、edges=131072、canonical nodes=16384；
- List/Optional 有序比较，Set/Map 使用跨双根 canonical IDs 无序比较；
- Frozen List/Set/Map/Entry 覆盖公开 equals/contains/get/containsKey/containsValue/entrySet.contains；
- hash 仅用于快速拒绝，hash 相同仍精确比较；
- 普通外部容器由 Frozen receiver 显式读取，不调用其容器 equals/hashCode；
- R01～R04 Completion 作为失效历史保留；PR #27 不自动合并，T13 继续阻断。

Design/Plan Gate：`PASSED`。
