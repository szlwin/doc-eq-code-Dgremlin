# DESIGN-R25 — TASK-P1-T06 I003 输入批次快照返工

- Revision：`DESIGN-R25@P1-T06-REWORK-I003`
- Status：`PASSED`
- Base：`feature/p1-t06-raw-definition-20260803-1334@3884f331dd066da1ff556f9b0544716d7ca3502c`
- Superseded current validity：`COMPLETION-P1-T06-R02@aec3cd105b15`
- Historical revisions：R23、R24、R19、R20、R01、R02 全部不可变保留
- Scope：仅 `RawDefinitionBuilder.build(List)` 输入边界、确定性 Oracle 与 T06 事实文档

## 问题

public `build(List<CanonicalDocumentNode>)` 当前对调用方原始 List 执行两次遍历：第一遍验证，第二遍提取。自定义 List 或并发修改可使第二遍返回未验证文档，破坏 Grammar 白名单、fail-closed、构建确定性以及“第二阶段只消费已验证输入”的 Completion 声明。

## 冻结设计

1. public 入口只读取调用方 `documents` 一次；
2. `snapshotDocuments` 在同一次迭代中复制输入顺序并逐项拒绝 null；
3. 空批次只根据复制结果判断，不调用原始 List 的 `isEmpty()` 或 `get()`；
4. 验证与提取只能消费同一个不可变快照；
5. 快照完成后不得再次访问原始 `documents`，包括 RuntimeException 失败路径；
6. 快照读取期间出现 RuntimeException 时进入稳定 `raw.build.failed` 受控失败，不暴露部分集合；
7. 快照中的文档顺序是 `sourceOrdinal` 的唯一批次顺序；
8. CanonicalDocumentNode 已是不可变值对象，因此只冻结容器，不重复深拷贝节点；
9. 不修改 Grammar、Raw Kind、lexical、reference、资源预算或公开 API；
10. TypedKey、SymbolTable、引用解析、Pipeline 与 TASK-P1-T07 继续阻断。

## 失败边界

- `documents == null`：`FAILED / raw.input.required / empty set`；
- snapshot 为空：`FAILED / raw.input.required / empty set`；
- snapshot 含 null：`FAILED / raw.document.required / empty set`；
- snapshot 读取 RuntimeException：`FAILED / raw.build.failed / empty set`；
- snapshot 内 unsupported root：`FAILED / raw.document.root.unsupported / empty set`；
- snapshot 内 unknown child：`FAILED / raw.structure.unknown / empty set`。

## 确定性 Oracle

1. 第一次迭代合法、第二次迭代 unsupported root：提取仍只能消费第一次冻结的合法批次；
2. 第一次迭代合法、第二次迭代带 unknown child：非法节点不得进入 Raw body；
3. 原始 List 在快照完成后被修改：结果保持第一次快照；
4. 快照文档顺序精确决定 ordinal；
5. unsupported root 位于真实快照时 fail closed；
6. unknown child 位于真实快照时 fail closed；
7. 测试使用可控 side-effecting List，不依赖线程调度、时间阈值、OOM 或 StackOverflowError。

## Review 结论

- `REV-000283`：IndependentReviewAgent — `NEEDS_CHANGES`，接受新 P1；
- `REV-000284`：DesignReviewAgent — `PASSED`；
- `EVD-000525`～`EVD-000526`；
- 开放 Finding：`FND-P1-T06-I003-001`；
- 下一阶段：冻结 R21 实施计划并建立有效 RED。
