# DESIGN-R41 — TASK-P1-T12 Artifact Snapshot 资源边界返工

- Revision：`DESIGN-R41@P1-T12-REWORK-I004`
- Status：`PASSED`
- Supersedes：`DESIGN-R40@P1-T12-REWORK-I003`
- Base：`PR27@cf6e7dbe18d2f172dc4c68c793f45d9ecfbabe9d`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated Completion：`COMPLETION-P1-T12-R03@4d4cd5c4c049`
- Independent Review：`NEEDS_CHANGES / REWORK`，Open P0/P1/P2=`0/1/1`

## 1. 历史与范围

R38～R40、R34～R36、I001～I003 的 RED、Architecture、Development、Review、Completion、CI 与 Artifact 必须作为不可变历史保留。I004 只关闭以下 Findings：

- `FND-P1-T12-I004-001` `[P1][BLOCKER][RESOURCE]`：递归 artifact snapshot 对深层无环图、共享 DAG、宽容器没有资源边界，且没有完成态 identity memoization；
- `FND-P1-T12-I004-002` `[P2]`：缺少深度、共享 DAG、宽容器、操作次数及 publisher=0 的阻断 Oracle。

不得实现 T13 Digest/Observer 完整策略、T14 Context CAS 业务、T15 Starter 或 P2～P7 runtime。

## 2. Snapshot 图语义

### 2.1 Identity 状态

每次 `freeze()` 使用独立 Session，并同时维护：

```text
VISITING source identity
FROZEN source identity → immutable snapshot
```

规则：

- source identity 首次进入容器：标记 `VISITING`；
- 遍历完成：保存 `FROZEN → immutable snapshot`；
- 再遇到 `VISITING`：判定为循环，稳定失败；
- 再遇到 `FROZEN`：直接复用同一个冻结对象，不重复遍历或复制；
- 同一共享子图在输出中的多次引用必须保持同一冻结对象 identity。

循环检测与共享 DAG 必须严格区分：循环失败，共享 DAG 复用。

### 2.2 非递归遍历

容器图必须通过显式 traversal stack 执行深度优先遍历，不依赖 JVM 方法递归栈。遍历工作项至少包含：

- source value；
- 当前容器深度；
- 父容器写入目标；
- 容器完成动作。

`StackOverflowError` 不得成为正常资源拒绝机制，也不得依赖捕获 `Error`。

## 3. 默认资源预算

生产默认预算冻结为：

```text
max nesting depth:      256
max unique containers:  4096
max traversed edges:    65536
max map entries:        16384
```

计数规则：

- `nesting depth`：根容器深度为 1；标量不增加容器深度；
- `unique containers`：按 source identity 计数，仅首次进入 Optional/List/Set/Map 时增加；
- `traversed edges`：Optional present item、List item、Set item 各计 1；Map key 和 value 各计 1；
- `map entries`：每个 Map entry 计 1；
- FROZEN identity 再次引用不增加 unique container，也不重新遍历其内部 edge；父到共享节点的引用 edge 仍计入当前父容器的 edge；
- 任一计数在物化或入栈前超过上限，立即受控失败。

内部测试可使用 package-private limits 构造较小预算验证边界，但不得形成公共 API。

## 4. 资源超限失败合同

资源超限使用专用内部异常边界，并由 `CompilerPipeline` 收敛为：

```text
state      = FAILED
diagnostic = MIX-PUBLICATION-BLOCKED
messageKey = pipeline.artifact.resource-exceeded
publisher  = 0
```

Diagnostic 必须携带当前 Pass 名称。不得捕获 `OutOfMemoryError` 或 `StackOverflowError` 作为功能实现；应在分配大量输出、递归栈增长或指数展开之前通过预算和 memoization 阻断。

循环、未知可变对象和冻结后 Map/Set equality collision 继续使用现有稳定 pass failure 合同，不得回归。

## 5. 容器构建与事实保真

- List：保留迭代顺序，输出不可变；
- Set：保留迭代顺序，冻结后 equality collision 失败；
- Map：保留 entry 顺序，key/value 均冻结，冻结后 key collision 失败；
- Optional：present value 冻结，empty 直接返回；
- Immutable scalar 与 `ImmutablePipelineArtifact` 直接复用；
- null item/key/value 继续拒绝；
- 已完成共享子图必须复用同一 immutable snapshot，不得指数复制。

## 6. 阻断 Oracle

I004 至少新增：

1. 深度恰好等于 limits：成功；
2. 深度超过 limits：稳定 `pipeline.artifact.resource-exceeded`；
3. 24 层共享 DAG：不指数展开且成功；
4. 同一共享子图多次引用：输出使用 `assertSame` 复用；
5. 通过 counting container 断言遍历操作数与唯一图规模线性相关，不使用耗时阈值；
6. 循环图继续稳定失败；
7. List/Set 宽度超过 edge 预算稳定失败；
8. Map 超过 map-entry 或 edge 预算稳定失败；
9. unique-container 预算边界成功/超限失败；
10. 任一资源失败路径 `state=FAILED`、publisher=0，且不出现 `StackOverflowError` 或 `OutOfMemoryError`；
11. I001～I003 的 capability、prepare/commit、Diagnostic、Clock、Deadline、collision、Context/Result 与 commit-wins Oracle 全部继续通过。

## 7. 编码与 Gate

- Java release 8；
- 所有 `@Override` 注解独占一行；
- 公开方法、构造器和重要 traversal、memoization、budget、collision、failure mapping 逻辑使用中文注释；
- Open P0/P1/P2 必须为 `0/0/0`；
- Completion 前必须完成有效 RED、Architecture、GREEN、独立 Review、全量 P0、Artifact 独立解析和 Revision Integrity；
- PR #27 不自动合并；合并前 TASK-P1-T13 保持阻断。
