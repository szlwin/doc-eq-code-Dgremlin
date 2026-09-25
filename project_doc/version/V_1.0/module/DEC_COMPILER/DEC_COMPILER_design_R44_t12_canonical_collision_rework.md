# DESIGN-R44 — TASK-P1-T12 canonical collision fail-closed 返工

- Revision：`DESIGN-R44@P1-T12-REWORK-I007`
- Status：`PASSED`
- Supersedes：`DESIGN-R43@P1-T12-REWORK-I006`
- Base：`PR27@a59a39fde202366742963658bf07797c9537de57`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated Completion：`COMPLETION-P1-T12-R06@ce8c92523256`
- Review Gate：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 0 / 1`

## 1. 历史与范围

R38～R43、R34～R39、I001～I006 的 Design、Plan、RED、Architecture、Development、Review、Testing、Completion、CI 与 Artifact 必须作为不可变历史保留。I007 只关闭：

- `FND-P1-T12-I007-001` `[P2][SPEC][CORRECTNESS][ORACLE]`：外部 identity-backed Map/Set 中 equality-equal、identity-distinct 的 key/element 被归一为重复 canonical ID 后，当前完成阶段仍形成合法 canonical node，两个非法 collision 容器可能被判断为相等。

## 2. Canonical collision 合同

comparison canonicalization 只允许为满足 Java Set/Map equality 唯一性约束的容器形成合法节点：

- Set 排序后的 canonical element ID 不得重复；
- Map 按 canonical key/value 排序后，相邻 pair 的 canonical key ID 不得重复；
- Map.Entry 不应用容器级 duplicate-key 门禁；
- 普通 hash collision 但 equals 不同的标量必须获得不同 canonical ID，并继续精确区分；
- 正常 LinkedHashMap、HashMap、LinkedHashSet 的 equality 语义保持。

## 3. Fail-closed 语义

重复 canonical key/element 代表输入容器违反 comparison canonical model，不得生成可 intern 的合法 MAP/SET node。I007 选择稳定异常语义：

```text
ArtifactSnapshots.CanonicalCollisionException
artifact comparison canonical collision: map-key
artifact comparison canonical collision: set-element
```

该异常为 package-private 内部类型，不扩展 Compiler 公共 API。直接 `controlledEquals()`、Frozen receiver 对非法外部 Map/Set 的 equality/query 均允许稳定传播该异常；不得返回 `true`，不得依赖 OOM、超时或递归错误。

## 4. 完成阶段门禁

- `FinishSequenceTask` 仅在 `type == SET && unordered` 时检查排序后的相邻 ID；
- `FinishPairsTask` 仅在 `type == MAP && unordered` 时检查排序后的相邻 key ID；
- duplicate 检查必须发生在 `session.nodeId(type, parts)` 与 `session.complete(...)` 之前；
- collision 输入不得写入 `nodeIds`、`idsByIdentity` 或 FROZEN 状态；
- 检查只扫描已受 edge/node budget 约束的 metadata，不新增无界临时结构。

## 5. 清理与兼容

- 删除无调用点的 private `ConditionalCompareTask`；
- 所有 `@Override` 独占一行；
- 新增异常、collision 检查和关键分支使用中文注释；
- Java release 8；
- I001～I006 的 iterator traversal、operation cache、comparison budgets、snapshot budgets、Publication、Diagnostic、Clock、Deadline、Context/Result 与 commit-wins 合同保持；
- 不实现 T13/T14/T15 或 P2～P7 runtime。

## 6. Blocking Oracle

有效 RED 至少覆盖：

1. 两个结构相同、各自包含 equality-equal identity-distinct key 的 IdentityHashMap 均不得比较为 true；
2. 单侧 Map duplicate canonical key 也必须稳定拒绝；
3. identity-backed Set 的 duplicate canonical element 稳定拒绝；
4. 正常 LinkedHashMap 保持相等；
5. hashCode 相同但 equals 不同的 key 保持精确不等；
6. FrozenMap/FrozenSet 正常 equality/query、I001～I006 全部回归绿色。
