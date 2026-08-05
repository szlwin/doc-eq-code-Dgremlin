# DEVSKEL-P1-T12-R06 — comparison operation 资源边界 Architecture

- Architecture：`DEVSKEL-P1-T12-R06@788f475d60e4`
- Task：`TASK-P1-T12 / I006`
- Design：`DESIGN-R43@P1-T12-REWORK-I006`
- Plan：`TP-P1-COMPILER-F01-R39@P1-T12-REWORK-I006`
- Valid RED：`TDD-P1-T12-R06@788f475d60e4`
- Status：`PASSED`

## 1. Operation 边界

每个公开 equality/query 仅创建一个 `ArtifactComparisonOperation`。该对象在整个公开操作期间共享：

- `ComparisonBudget`；
- identity pair 的 `VISITING / EQUAL / NOT_EQUAL` 状态；
- `CanonicalSession`；
- scalar 与 canonical node intern table；
- query 侧已完成 canonical metadata。

候选根结论相互独立，但已完成的子 pair 和 canonical identity 可跨候选复用。不得在候选循环中创建新的 Session。

## 2. Pair 状态机

```text
ABSENT -> VISITING -> EQUAL
                  -> NOT_EQUAL
```

- 首次登记 pair 前扣减 pair budget；
- `EQUAL/NOT_EQUAL` 命中直接复用，不重复展开内部边；
- `VISITING` 代表当前比较路径形成循环，稳定拒绝；
- 完成状态同时登记反向 identity pair，保持小规模普通容器对称比较；
- 根比较失败只结束当前候选，不清除 operation cache。

## 3. Iterator-driven List traversal

List equality 使用显式 continuation task：

1. 获取双方 Iterator；
2. 每轮先比较 `hasNext()`；
3. 两侧均有下一项时，先扣减一条逻辑 pair edge；
4. 再调用双方 `next()`；
5. continuation 与 child pair 依次压入显式栈。

不得调用外部 List 的 `size()` 或 `get(index)`。`indexOf/contains/lastIndexOf` 对内部受信任候选列表迭代，但候选比较复用同一 Operation。

## 4. 增量 canonical traversal

List、Set、Map 和 Entry 使用显式 iterator task：

- 不使用 `new ArrayList<>(externalCollection)`；
- 不使用外部 `size()` 预分配；
- 每次读取下一元素前先扣减 edge budget；
- 首次进入容器 identity、创建临时 canonical node 前先扣减 canonical-node budget；
- 读取 Map entry 后，key/value 分别在读取和调度前受 edge budget 约束；
- child ID、entry pair 和排序输入逐项增长，其规模被 edge/node 上限约束；
- 无限 iterator 在下一次 `next()` 前由 edge budget 稳定终止。

## 5. 无序容器

Set/Map 继续使用同一 Operation 的 canonical IDs 完成无序精确比较：

- Set 收集 child canonical ID 后排序；
- Map 收集 key/value canonical pair 后按 key/value 排序；
- 相同 canonical key 的 Map 判定为非法碰撞；
- cached hash 仅用于两个内部 Frozen 值的快速拒绝；
- hash 相同仍执行受控精确比较。

## 6. 失败边界

资源超限继续抛出：

```text
ComparisonLimitException
artifact comparison resource limit exceeded: <dimension>
```

Iterator 主动抛出的业务异常原样传播，不伪装成资源超限。实现不得捕获或依赖 `OutOfMemoryError`、`StackOverflowError`、超时或线程中断。

## 7. Compatibility

- 保留 `ArtifactComparisonSupport` 为既有 package-private facade；
- `ArtifactSnapshots` 的调用点和 Compiler 公共 API 不扩展；
- I001～I005 的 comparison limits、freeze stack、snapshot budget、Publication、Diagnostic、Clock、Deadline、Context/Result 合同保持；
- Java release 8；
- 所有 `@Override` 独占一行；
- operation cache、pair state、iterator task、canonicalization、budget 和 failure boundary 使用中文注释；
- 不实现 T13/T14/T15 或 P2～P7 runtime。
