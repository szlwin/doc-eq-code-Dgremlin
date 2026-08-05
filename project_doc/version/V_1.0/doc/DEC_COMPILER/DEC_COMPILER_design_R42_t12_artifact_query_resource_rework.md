# DESIGN-R42 — TASK-P1-T12 冻结结果 Equality/Query 资源边界返工

- Revision：`DESIGN-R42@P1-T12-REWORK-I005`
- Status：`PASSED`
- Supersedes：`DESIGN-R41@P1-T12-REWORK-I004`
- Base：`PR27@2e113984973232d2d9a1d35bb886f73488f539c8`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated Completion：`COMPLETION-P1-T12-R04@923129b1f20d`
- Independent Review：`NEEDS_CHANGES / REWORK`，Open P0/P1/P2=`0/1/1`

## 1. 历史与范围

R38～R41、R34～R37、I001～I004 的 Design、Plan、RED、Architecture、Development、Review、Completion、CI 与 Artifact 必须作为不可变历史保留。I005 只关闭：

- `FND-P1-T12-I005-001` `[P1][BLOCKER][RESOURCE]`：冻结结果跨 `PipelineExecutionResult` 边界后的 equals/contains/get/containsKey/entrySet.contains 仍可能对共享 DAG 指数展开；
- `FND-P1-T12-I005-002` `[P2][ORACLE]`：缺少 equality/query 的共享 DAG、操作计数、预算、跨 freeze Session、普通外部容器与 Java Collection 合同 Oracle。

不得实现 T13 Digest/Observer 完整策略、T14 Context CAS 业务、T15 Starter 或 P2～P7 runtime。

## 2. 比较模型

### 2.1 显式 pair traversal

冻结容器的比较必须使用显式工作栈，不调用容器自身的递归 equals。每个比较工作项至少包含：

- left value；
- right value；
- 当前逻辑深度；
- 当前操作预算。

比较 Session 维护：

```text
(left identity, right identity) visited pairs
left/right identity → canonical node metadata
shared scalar/node intern table
```

规则：

- 同一 pair 再次出现：直接复用已验证结果，不重复展开；
- 两边同一 identity：立即相等；
- List 与 Optional 按顺序显式展开；
- Set 与 Map 使用同一比较 Session 的 canonical child IDs 完成无序匹配；
- 不得直接比较 freeze Session 内的旧 structuralId 数值；
- 普通外部 List/Set/Map/Optional 查询值同样由该非递归比较器读取。

### 2.2 精确性与快速拒绝

- 两个内部 Frozen 容器可先使用缓存 Java-compatible hash 快速拒绝；
- hash 相同不能直接判定相等，仍必须执行受控精确比较；
- scalar 与 `ImmutablePipelineArtifact` 只在 pair 首次出现时调用一次 equals；
- hash 相同但结构不同必须返回 false；
- Set/Map 匹配必须保持 Java Collection 的无序 equality 语义；
- List、Optional 保持顺序语义；
- Map key 匹配与 value 比较必须使用同一受控比较合同。

## 3. 比较资源预算

生产默认比较预算冻结为：

```text
max comparison depth: 256
max visited pairs:    16384
max traversed edges:  131072
max canonical nodes:  16384
```

计数规则：

- root pair 深度为 1；
- 首次登记 identity pair 时增加 visited pairs；
- List item pair、Optional value pair、Set candidate、Map key/value pair各计相应 edge；
- canonicalization 按每侧 unique container identity 计 canonical nodes；
- pair memo 命中不重复增加 pair 或内部 edge；
- 任一计数超过预算时，在继续入栈或物化临时索引前稳定拒绝。

package-private `ComparisonLimits` 与受控入口仅用于同包边界测试，不形成 Compiler 公共 API。

## 4. 稳定拒绝合同

比较或查询超限使用内部专用 `ComparisonLimitException`：

```text
artifact comparison resource limit exceeded: <dimension>
```

不得通过捕获 `StackOverflowError`、超时、线程中断或 `OutOfMemoryError` 实现拒绝。公开 Frozen 容器操作在超限时抛出该稳定异常，不返回部分匹配结果。

## 5. Frozen 容器公开合同

### FrozenList

必须覆盖：

- `equals(Object)`；
- `contains(Object)`；
- `indexOf(Object)`；
- `lastIndexOf(Object)`。

上述操作不得调用外部 query 对象的递归 equals。

### FrozenSet

必须覆盖：

- `equals(Object)`；
- `contains(Object)`。

Set 匹配不得通过递归 List.contains；共享 DAG element 的 query 复杂度应与唯一图规模近线性相关。

### FrozenMap

必须覆盖：

- `equals(Object)`；
- `get(Object)`；
- `containsKey(Object)`；
- `containsValue(Object)`。

不得调用普通外部 Map 的 `get()` 或 key.hashCode 作为精确匹配边界。

### FrozenEntry / FrozenEntrySet

- entry equality 使用受控 key/value 比较；
- `FrozenEntrySet.contains()` 使用受控 entry 比较；
- entry hash 继续符合 `keyHash ^ valueHash`；
- entry、entrySet 与 Map 的 equals/hashCode 合同一致。

## 6. Java Collection 合同

- Frozen List/Set/Map 与结构相等的普通 Java List/Set/Map 比较返回 true；
- 小规模普通容器反向比较保持对称；
- 相等对象的 hashCode 必须一致；
- equals 保持自反、对称、传递和 null/type 安全；
- 不同结构即使缓存 hash 相同也不得误判；
- Map key、Set element 和 Entry 查询的返回语义与标准 Collection 一致。

## 7. 阻断 Oracle

I005 至少新增：

1. 两个独立 freeze Session 的 24 层共享 DAG 执行 `FrozenList.equals()`，leaf equals 调用次数与唯一节点近线性；
2. `FrozenSet.contains/equals()` 查询结构相等与不等的独立共享 DAG；
3. `FrozenMap.get/containsKey/equals()` 查询结构相等与不等的独立共享 DAG key；
4. `FrozenEntrySet.contains()` 使用结构相等的普通 `Map.Entry`；
5. 同一共享 pair 只比较一次；
6. comparison depth、pair、edge、canonical-node 在边界成功、超限稳定拒绝；
7. hash 相同但结构不同返回 false；
8. Frozen 与普通 Java List/Set/Map 的对称性和 hash 一致性；
9. 普通外部容器作为 query 值时不触发其递归 equals/hashCode；
10. I001～I004 的 freeze、memoization、预算、collision、prepare/commit、Diagnostic、Clock、Deadline、Context/Result 和 commit-wins Oracle 全部继续通过。

## 8. 编码与 Gate

- Java release 8；
- 所有 `@Override` 注解独占一行；
- 公开方法、构造器以及 pair traversal、memoization、canonicalization、budget、query matching、failure boundary 使用中文注释；
- Open P0/P1/P2 必须为 `0/0/0`；
- Completion 前必须完成有效 RED、Architecture、GREEN、独立 Review、全量 P0、Artifact 独立解析和 Revision Integrity；
- PR #27 不自动合并；合并前 TASK-P1-T13 保持阻断。
