# TASK-P1-T02 REWORK I004

- 任务：`TASK-P1-T02`
- Iteration：`I004`
- 状态：`COMPLETED`
- 分支：`feature/p1-t02-rework-i002-20260802-1116`
- PR：`#17`
- 基线：`dev_all@f88f45731e16868bfacb489b63e3086aae49d018`
- 重开输入 Head：`be06b8d1c128ae3a86b3740d3f3c8a0f80aed246`
- 被推翻 Completion：`COMPLETION-P1-T02-R03@122ffc28165f`
- 设计：`DESIGN-R11@P1-T02-REWORK-I004`
- 实施计划：`TP-P1-COMPILER-F01-R07@P1-T02-REWORK-I004`
- TDD：`TDD-P1-T02-R04@b0502ee13dba`
- Architecture Skeleton：`DEVSKEL-P1-T02-R04@21d28d33eac9`
- Development：`DEV-P1-T02-R04@8b3e716a9730`
- Code Review：`CODEREVIEW-P1-T02-R04@8b3e716a9730`
- Testing：`TESTING-P1-T02-R04@8b3e716a9730`
- Completion：`COMPLETION-P1-T02-R04@8b3e716a9730`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

独立 Review 证明 I003 已关闭完整 Session 输入、Optional 发布和 CompilationResult 形状问题，但 Source/Frontend 仅冻结了方法名，没有冻结从安全 Source 到 Canonical 解析产物的实际数据闭包。I003 Completion、Review、Evidence 和 P0 记录均保留，但“开放 P0/P1 为 0”和“完整 API 已冻结”结论已由 I004 替代。

## Finding 关闭结果

- `FND-P1-T02-I004-001` — CLOSED：`FrontendResult` 通过 `Optional<CanonicalDocumentNode>` 返回成功解析产物；
- `FND-P1-T02-I004-002` — CLOSED：`DocumentSource` 冻结 `sourceId/uri/format/allowedRoot/content/contentDigest`；
- `FND-P1-T02-I004-003` — CLOSED：Test Oracle 已覆盖 Provider → Frontend → Canonical 的最小可执行数据闭包；
- `FND-P1-T02-I003-004` — CLOSED_AFTER_REOPEN：完整 DESIGN-R05/API Contract Oracle 已补齐 Source/Frontend 数据闭包；
- `FND-P2-T02-I004-004` — CLOSED：AllowedRoot 已统一等价尾斜杠根边界，并验证编码穿越、query、fragment、authority 和兄弟前缀。

## 已保持关闭的 I003 Finding

- `FND-P1-T02-I003-001` — 完整 8 参数 CompilationRequest Session 边界；
- `FND-P1-T02-I003-002` — Optional 条件发布和 PublicationResult/Status 分离；
- `FND-P1-T02-I003-003` — CompilationResult interface 与 Published 完整 API；
- `FND-P2-T02-I003-005` — 值对象字符串表示完整性。

## 验证结果

- 有效 RED Head：`b0502ee13dbae7d5065e78494c75ee679927d01e`，P0 Run `30733257810`；
- Skeleton Head：`21d28d33eac9b5395397124317bf62b4fcd08eee`，P0 Run `30733441104`，仅一项受控 RED；
- 干净代码 Head：`8b3e716a9730d12fe84c6efd3fb8481998e335e0`；
- 最终代码 P0 Run：`30733616822`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：35 run / 0 failures / 0 errors / 0 skipped；
- 完整 12 模块 Reactor：PASSED；
- Java release 8：PASSED；
- 故意失败阻断门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- Review：`REV-000129`～`REV-000137` PASSED；
- Evidence：`EVD-000373`～`EVD-000378` ACTIVE；
- 开放 P0/P1：无。

## 最终边界

1. DocumentSource 允许 T03 直接使用显式 format 选择 Frontend，无需后缀猜测、downcast 或隐藏映射；
2. PARSED 恰有一个 Canonical 根且无 ERROR，FAILED 无 Canonical 且至少一个 ERROR；
3. Canonical 节点不暴露 DOM、YAML Node 或第三方 Parser 类型；
4. SourceResolution 成功与失败结果均通过统一工厂冻结候选隔离；
5. 未修改 `dec-core-context` 生产代码；
6. 未实现 T03 SourceGraph、真实 Frontend 或 Compiler Pipeline；
7. 所有新增和修改的 `@Override` 独占一行；
8. 方法、构造器和重要逻辑均使用中文注释；
9. 未经明确授权不得合并 PR #17；
10. PR #17 合并前 TASK-P1-T03 保持阻断。
