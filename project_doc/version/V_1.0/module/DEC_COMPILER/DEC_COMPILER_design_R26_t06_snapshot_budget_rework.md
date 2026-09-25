# DESIGN-R26 — TASK-P1-T06 I004 Snapshot 前置预算返工

- Revision：`DESIGN-R26@P1-T06-REWORK-I004`
- Status：`PASSED`
- Base：`feature/p1-t06-raw-definition-20260803-1334@36b223e0f50fe090031b499366eb6ff5844b05d3`
- Superseded current validity：`COMPLETION-P1-T06-R03@432ccdc1103f`
- Historical revisions：R23～R25、R19～R21、R01～R03 全部不可变保留
- Scope：仅 `RawDefinitionBuilder.snapshotDocuments` 的分配前节点硬上限、确定性 Oracle 与 T06 I004 事实文档

## 问题

I003 已保证调用方 List 只迭代一次，并让 validate、extract、ordinal 和异常定位共享同一不可变 snapshot。但当前实现先完整复制全部文档引用，再由 `validateDocuments` 创建 `ValidationBudget`。由于每个 Canonical 文档至少包含一个根节点，文档数量本身必须受 `maxCanonicalNodeCount` 约束；否则超大或不终止 iterator 可在资源门禁前持续扩容。

## 冻结设计

1. `snapshotDocuments` 改为实例方法，直接读取当前 Builder 的 `RawBuilderLimits`；
2. 调用方 `documents` 仍只允许一次 iterator；
3. 每次取得非 null 文档后、加入 ArrayList 前，检查 `snapshot.size() >= limits.maxCanonicalNodeCount()`；
4. 达到上限后的下一个文档立即返回 `raw.limit.node-count`；
5. Diagnostic SourceRef 使用触发上限的当前文档 SourceRef；
6. 触发上限后不得继续请求 iterator 的下一项；
7. snapshot 失败不得发布部分 RawDefinitionSet；
8. snapshot 返回后仍冻结为不可变 List；
9. `ValidationBudget` 必须继续遍历完整树，检查所有根和后代的累计节点数；前置文档数限制不能替代树节点预算；
10. 不捕获 `OutOfMemoryError`，不使用真实 OOM 或无限循环作为测试 Oracle；
11. 不调用原始 List 的 `size/isEmpty/get/toArray/stream/parallelStream/spliterator`；
12. 不修改 Grammar、14 Kind、lexical、reference、depth budget、公开 API 或 T07 范围。

## 边界语义

假设 `maxCanonicalNodeCount = N`：

- 0 个文档：`raw.input.required`；
- 1..N 个单根文档：允许进入完整树验证；
- 第 N+1 个文档：在 snapshot.add 前失败为 `raw.limit.node-count`；
- 一个文档包含超过 N 个根与后代节点：snapshot 文档数检查通过，但完整树验证在第 N+1 个节点失败；
- null 文档优先报告 `raw.document.required`，不被节点上限掩盖；
- iterator 自身 RuntimeException 保持 `raw.build.failed`。

## 确定性 Oracle

1. 小预算 2 下两个单节点文档通过；
2. 第三个文档在 snapshot 阶段立即失败为 `raw.limit.node-count`；
3. SourceRef 精确指向第三个文档；
4. 第三个文档后若 iterator 被继续读取则测试主动抛错，证明 Builder 已停止；
5. 单个文档含多个后代时仍由 `ValidationBudget` 执行完整树总节点限制；
6. snapshot 资源失败不发布部分集合；
7. 原始 List 的 iterator 只调用一次；
8. 不调用原始 List 的随机访问、批量转换或 Stream/Spliterator 入口。

## Review 结论

- `REV-000296`：IndependentReviewAgent — `NEEDS_CHANGES`，接受 `FND-P1-T06-I004-001`；
- `REV-000297`：DesignReviewAgent — `PASSED`；
- `EVD-000538`～`EVD-000539`；
- I003 Finding 保持 CLOSED；
- 下一阶段：冻结 R22 实施计划并建立有效 RED。
