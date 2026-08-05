# DEVSKEL-P1-T12-R07 — canonical collision fail-closed Architecture

- Architecture：`DEVSKEL-P1-T12-R07@cb3f08f28807`
- Task：`TASK-P1-T12 / I007`
- Design：`DESIGN-R44@P1-T12-REWORK-I007`
- Plan：`TP-P1-COMPILER-F01-R40@P1-T12-REWORK-I007`
- Valid RED：`TDD-P1-T12-R07@cb3f08f28807`
- Status：`PASSED`

## 1. Collision 类型

在 `ArtifactSnapshots` 内新增 package-private、稳定内部异常：

```java
static final class CanonicalCollisionException
        extends IllegalArgumentException
```

消息严格为：

```text
artifact comparison canonical collision: map-key
artifact comparison canonical collision: set-element
```

异常不形成 Compiler 公共 API，不映射为资源超限，不捕获 JVM Error。

## 2. Set 完成门禁

`FinishSequenceTask` 仅对 `CanonicalType.SET` 且 `unordered=true` 执行：

1. 将已受预算约束的 child IDs 写入 `int[] parts`；
2. 排序；
3. 线性扫描相邻 ID；
4. 发现重复时立即抛 `CanonicalCollisionException("set-element")`；
5. 只有无重复时调用 `session.nodeId()` 与 `session.complete()`。

List/Optional 不应用该门禁。

## 3. Map 完成门禁

`FinishPairsTask` 仅对 `CanonicalType.MAP` 且 `unordered=true` 执行：

1. 校验 key/value assignment；
2. 按 canonical key ID、value ID 排序；
3. 线性扫描相邻 pair 的 key ID；
4. 发现重复时立即抛 `CanonicalCollisionException("map-key")`；
5. 只有无重复时构造 parts、intern MAP node 并完成 source。

`CanonicalType.ENTRY` 不应用 duplicate-key 门禁。

## 4. Cache 完整性

collision 检查发生于 `session.nodeId()` 和 `session.complete()` 之前，因此非法 source：

- 不写入 `nodeIds` 合法 MAP/SET key；
- 不写入 `idsByIdentity`；
- 不转为 `FROZEN`；
- 不向父 assignment 写入 child ID。

异常终止当前 comparison operation；operation 不再复用 partial metadata。

## 5. 复杂度与预算

检查只对已经受 comparison edge/canonical-node 预算限制的 `parts` 或 `ordered` 执行一次 O(n) 扫描；Map 排序仍为 O(n log n)，不新增外部容器复制或无界索引。

## 6. 清理

删除无任何构造或调用点的 private `ConditionalCompareTask`。不得改动公开 facade、operation-level pair cache、iterator continuation 或预算模型。

## 7. Compatibility

- I001～I006 的 comparison/snapshot/Publication 合同保持；
- 普通 hash collision 但 equals 不同继续获得不同 scalar canonical ID；
- 正常 LinkedHashMap/Set、FrozenMap/FrozenSet equality/query 保持；
- Java release 8；
- 所有 `@Override` 独占一行；
- 异常、duplicate scan、cache 完整性和清理逻辑使用中文注释；
- 不实现 T13/T14/T15 或 P2～P7 runtime。
