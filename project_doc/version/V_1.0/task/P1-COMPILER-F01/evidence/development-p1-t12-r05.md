# TASK-P1-T12 I005 Development Evidence

- Development：`DEV-P1-T12-R05@304a2156ff5e`
- Clean-code Head：`304a2156ff5e86c2a45213d4e917f17b9a172831`
- Evidence：`EVD-000936`～`EVD-000945`

## Production changes

### ArtifactComparisonSupport

新增 package-private 比较引擎：

- 显式 `ArrayDeque<PairTask>`，不使用 JVM 方法递归；
- identity pair memo，同一共享 pair 只比较一次；
- depth、visited-pairs、traversed-edges、canonical-nodes 四类预算；
- List/Optional 顺序 pair traversal；
- Set/Map 双根共享 canonical intern table；
- scalar identity/cache 与 canonical container identity/cache；
- query helpers：List index、Set contains、Map key/value、EntrySet contains；
- comparison 超限抛出稳定 `ComparisonLimitException`。

### ArtifactSnapshots

- 增加生产默认 comparison limits 与同包 `controlledEquals`；
- `FrozenList` 覆盖 equals、contains、indexOf、lastIndexOf；
- `FrozenSet` 覆盖 equals、contains；
- `FrozenMap` 覆盖 equals、get、containsKey、containsValue；
- `FrozenEntry` 使用受控 key/value equality，并保持不可变 Entry/hash 合同；
- `FrozenEntrySet` 覆盖 equals、contains；
- Frozen List/Set/Map/Entry 继续复用 I004 缓存 hash；
- hash 不作为相等充分条件，碰撞后仍进行精确结构比较。

## Scope / style

- 生产变更仅位于 `dec.core.compiler.pass`；
- 未实现 T13/T14/T15 或 P2～P7 runtime；
- Java release 8 编译通过；
- 所有 `@Override` 独占一行；
- 公开方法、构造器以及 pair traversal、memoization、budget、canonicalization、query matching 均有中文注释。

Development Gate：`PASSED`。
