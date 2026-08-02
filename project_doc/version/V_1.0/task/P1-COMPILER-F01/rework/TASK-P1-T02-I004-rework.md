# TASK-P1-T02 REWORK I004

- 任务：`TASK-P1-T02`
- Iteration：`I004`
- 状态：`REWORK`
- 分支：`feature/p1-t02-rework-i002-20260802-1116`
- PR：`#17`，当前 Draft
- 基线：`dev_all@f88f45731e16868bfacb489b63e3086aae49d018`
- 重开输入 Head：`be06b8d1c128ae3a86b3740d3f3c8a0f80aed246`
- 被推翻 Completion：`COMPLETION-P1-T02-R03@122ffc28165f`
- 设计：`DESIGN-R11@P1-T02-REWORK-I004`
- 实施计划：`TP-P1-COMPILER-F01-R07@P1-T02-REWORK-I004`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

独立 Review 证明 I003 已关闭完整 Session 输入、Optional 发布和 CompilationResult 形状问题，但 Source/Frontend 仅冻结了方法名，没有冻结从安全 Source 到 Canonical 解析产物的实际数据闭包。I003 Completion、Review、Evidence 和 P0 记录均保留，但“开放 P0/P1 为 0”和“完整 API 已冻结”结论失效。

## 开放 Finding

- `FND-P1-T02-I004-001` — P1：`FrontendResult` 无法返回 Canonical 解析产物，仓库缺少 `CanonicalDocumentNode`；
- `FND-P1-T02-I004-002` — P1：`DocumentSource` 缺少 `uri/format/allowedRoot` 安全来源事实；
- `FND-P1-T02-I004-003` — P1：完整 API Test Oracle 未覆盖 Provider → Frontend → Canonical 数据闭包；
- `FND-P1-T02-I003-004` — REOPENED：此前“完整 DESIGN-R05/API Contract Test Oracle”关闭结论失效，必须由 I004 Oracle 重新关闭。

## 已保持关闭的 I003 Finding

- `FND-P1-T02-I003-001` — 完整 8 参数 CompilationRequest Session 边界；
- `FND-P1-T02-I003-002` — Optional 条件发布和 PublicationResult/Status 分离；
- `FND-P1-T02-I003-003` — CompilationResult interface 与 Published 完整 API；
- `FND-P2-T02-I003-005` — 值对象字符串表示完整性。

## 门禁

1. 先更新 Test Oracle 形成有效 RED，再修改生产代码；
2. RED 必须在 Java 8 测试源码编译成功后形成；
3. DocumentSource 必须让 T03 无需猜测格式、downcast 或隐藏映射；
4. PARSED 必须恰有 Canonical 根，FAILED 必须无 Canonical 且至少一个 ERROR；
5. 不暴露 DOM、YAML Node 或第三方 Parser 类型；
6. 不修改 `dec-core-context` 生产代码；
7. 不实现 T03 SourceGraph、真实 Frontend 或 Pipeline；
8. 开放 P0/P1 阻断 Completion；
9. 所有 `@Override` 单独一行；
10. 方法、构造器和重要逻辑使用中文注释；
11. PR #17 在 Completion 前保持 Draft；
12. PR #17 合并前 TASK-P1-T03 保持阻断。
