# DESIGN-R43 — TASK-P1-T12 comparison operation 资源边界返工

- Revision：`DESIGN-R43@P1-T12-REWORK-I006`
- Status：`PASSED`
- Supersedes：`DESIGN-R42@P1-T12-REWORK-I005`
- Base：`PR27@956e51b998068b726eefc4ccfbafe12f868ca72b`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated Completion：`COMPLETION-P1-T12-R05@304a2156ff5e`
- Review Gate：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 3 / 1`

## 1. 历史与范围

R38～R42、R34～R38、I001～I005 的 Design、Plan、RED、Architecture、Development、Review、Testing、Completion、CI 与 Artifact 必须作为不可变历史保留。I006 只关闭：

- `FND-P1-T12-I006-001` `[P1][BLOCKER][RESOURCE]`：comparison budget 生效前物化外部 Set/Map，且信任外部 List.size() 预分配；
- `FND-P1-T12-I006-002` `[P1][BLOCKER][MEMO]`：单次公开查询为每个候选重新创建 ComparisonSession，丢失 operation-level pair/canonical cache；
- `FND-P1-T12-I006-003` `[P1][BLOCKER][RESOURCE]`：对任意 List 使用 size()+get(index)，非 RandomAccess List 可退化为 O(n²)；
- `FND-P1-T12-I006-004` `[P2][ORACLE]`：缺少超宽/无限 iterator、异常 size、LinkedList、多候选共享子图与 operation cache 的阻断测试。

不得实现 T13 Digest/Observer 完整策略、T14 Context CAS、T15 Starter 或 P2～P7 runtime。

## 2. Operation-level 比较模型

每次公开 equality/query 创建一个 `ComparisonOperation`，其生命周期覆盖该公开操作的全部候选。Operation 必须共享：

```text
ComparisonBudget
(left identity, right identity) → VISITING / EQUAL / NOT_EQUAL
CanonicalSession
scalar intern table
container identity → canonical ID
```

规则：

- `equalsValues` 使用一个 Operation；
- `indexOf/lastIndexOf/contains` 的全部候选共享同一 Operation；
- `get/containsKey/containsValue/entrySet.contains` 的全部 entry 共享同一 Operation；
- 已完成 pair 的 `EQUAL/NOT_EQUAL` 结果跨候选复用；
- `VISITING` 只表示当前路径循环，必须稳定拒绝，不得把未完成 pair 当作相等；
- 每个候选保留独立根结论，但共享已完成子 pair、canonical metadata 和总预算；
- query canonicalization 在同一 Operation 中最多完成一次；
- 同一共享子图在多个候选中不得重复展开。

## 3. Iterator-driven traversal

### 3.1 List equality

- 任意普通 `List` 均通过 `Iterator` 顺序读取；
- 不调用外部 List 的 `size()` 或 `get(index)`；
- 使用 iterator continuation task，在读取一个 pair 前先扣 edge budget，再调用 `next()`；
- 不为了逆序压栈整体复制外部 List；
- FrozenList 的 `lastIndexOf` 可使用内部受信任 ArrayList 的 `ListIterator`，但候选值比较仍共享同一 Operation。

### 3.2 Canonicalization

- 外部 List/Set/Map/Entry 全部通过显式 iterator task 增量遍历；
- 禁止 `new ArrayList<>(externalCollection)`；
- 禁止使用外部 `size()` 作为临时集合初始容量；
- 每次调用 `iterator.next()`、保存 child metadata 或压入 child task 前必须先通过对应 edge/node 预算；
- Map entry 的 key/value 分别受 edge budget 约束；
- 临时 child ID、pair 和索引数量天然受 edge/canonical-node 上限约束；
- Set/Map 的无序 canonical key 在遍历完成后排序，排序输入不得超过已验证预算。

### 3.3 异常或无限 iterator

- 只要 iterator 持续产出元素，Operation 必须在达到 edge 上限时、调用下一次 `next()` 前抛出 `ComparisonLimitException`；
- 不捕获或依赖 `OutOfMemoryError`、`StackOverflowError`、超时或线程中断；
- iterator 自身主动抛出的业务异常不得被伪装成资源超限。

## 4. 精确比较与 pair cache

- List/Optional/Entry 使用显式 pair traversal；
- scalar pair 只在首次 pair 上调用一次 `equals`；
- 同一 identity pair 再次出现时直接复用 `EQUAL/NOT_EQUAL`；
- List child 失败后停止当前根剩余比较，但已完成缓存保留供后续候选复用；
- Set/Map 使用同一 Operation 的 CanonicalSession 完成无序精确比较；
- cached hash 仅用于两个内部 Frozen 值的快速拒绝；
- hash 相同仍执行精确比较；
- 不调用外部 Collection 自身的 equals/hashCode 作为容器比较边界。

## 5. 资源预算

继续冻结 I005 默认值：

```text
max comparison depth: 256
max visited pairs:    16384
max traversed edges:  131072
max canonical nodes:  16384
```

计数与拒绝顺序：

- root pair 深度为 1；
- 新 identity pair 登记前检查 pair budget；
- iterator 读取下一元素前检查 edge budget；
- 首次进入容器 identity、建立 canonical 临时节点前检查 node budget；
- pair/canonical memo 命中不重复消耗内部 edge；
- 任一预算超限必须在外部元素读取、临时保存或任务压栈之前拒绝。

## 6. 稳定异常合同

资源超限继续抛出 package-private：

```text
ComparisonLimitException
artifact comparison resource limit exceeded: <dimension>
```

不得返回部分结果，不得转为 false，不得依赖 JVM Error。

## 7. 阻断 Oracle

I006 至少新增：

1. 外部 Set/Map 的 iterator 在 edge 上限后一元素读取前停止，禁止整体复制；
2. 外部 List 的 `size()` 报告 `Integer.MAX_VALUE` 或直接抛错时，equality/canonicalization 不调用 size；
3. LinkedList 与非 RandomAccess 计数 List 的节点访问保持 O(n)，不调用 get(index)；
4. Frozen List 多位置引用同一 DAG，未命中 query 的 leaf pair 只比较一次；
5. Frozen Map 多个 value 共享同一 DAG，containsValue 未命中时共享 pair cache；
6. 多个 Set element / Map key 共享子图时，查询不会因重复展开提前耗尽预算；
7. 无限 iterator 在读取上限处稳定抛 `ComparisonLimitException`，无 OOM/timeout；
8. 临时 canonical child IDs/pairs 在预算前不预分配外部 size；
9. pair `NOT_EQUAL` 与 `EQUAL` 均能跨候选复用；
10. I001～I005、prepare/commit、Diagnostic、Clock、Deadline、Context/Result 和 commit-wins Oracle 全部保持。

## 8. 编码与 Gate

- Java release 8；
- 所有 `@Override` 注解独占一行；
- 公开方法、构造器以及 operation cache、pair state、iterator task、budget、canonicalization 和 failure boundary 使用中文注释；
- Open P0/P1/P2 必须为 `0/0/0`；
- Completion 前必须完成有效 RED、Architecture、GREEN、独立 Review、全量 P0、Artifact 独立解析和 Revision Integrity；
- PR #27 不自动合并；合并前 TASK-P1-T13 保持阻断。
