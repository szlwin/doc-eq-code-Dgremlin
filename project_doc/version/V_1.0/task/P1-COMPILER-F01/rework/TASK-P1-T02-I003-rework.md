# TASK-P1-T02 REWORK I003

- 任务：`TASK-P1-T02`
- Iteration：`I003`
- 状态：`COMPLETED`
- 分支：`feature/p1-t02-rework-i002-20260802-1116`
- PR：`#17`
- 基线：`dev_all@f88f45731e16868bfacb489b63e3086aae49d018`
- 被推翻 Completion：`COMPLETION-P1-T02-R02@8847b3c7dfac`
- 设计：`DESIGN-R10@P1-T02-REWORK-I003`
- 实施计划：`TP-P1-COMPILER-F01-R06@P1-T02-REWORK-I003`
- TDD：`TDD-P1-T02-R03@925b53f4d709`
- Architecture Skeleton：`DEVSKEL-P1-T02-R03@35d1d76f007d`
- Development：`DEV-P1-T02-R03@122ffc28165f`
- Code Review：`CODEREVIEW-P1-T02-R03@122ffc28165f`
- Testing：`TESTING-P1-T02-R03@122ffc28165f`
- Completion：`COMPLETION-P1-T02-R03@122ffc28165f`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

完整规格 Review 发现 I002 只验证了最终 T01 发布聚合适配，未验证 `DESIGN-R05` 与 `DEC_COMPILER_api_contract.md` 的完整公共 API。I002 Completion、Review、Evidence 和 PR 历史均保留，但“任务完成”和“开放 P0/P1 为 0”结论已由 I003 取代。

## 关闭 Finding

- `FND-P1-T02-I003-001` — CLOSED：CompilationRequest 已恢复 Source、Frontend、Deadline、Clock 与 Observer 完整 Session 边界；
- `FND-P1-T02-I003-002` — CLOSED：条件发布已恢复 Optional、PublicationResult interface 与独立 PublicationStatus；
- `FND-P1-T02-I003-003` — CLOSED：CompilationResult 已恢复 interface，Published 完整事实 accessor 已冻结；
- `FND-P1-T02-I003-004` — CLOSED：Test Oracle 与 Completion 已直接纳入 DESIGN-R05 和 API Contract；
- `FND-P2-T02-I003-005` — CLOSED：核心值对象 toString 已包含全部语义字段。

开放 P0/P1 Finding：无。

## TDD 与验证

- 有效 RED Head：`925b53f4d709a5e7f34d7c5a177548e2691c7c25`；
- RED P0 Run：`30732063081`；Context 26 项和既有 Compiler 12 项全绿，新增 4 项完整 API 合同测试按预期失败；
- Skeleton Head：`35d1d76f007dc7cb87132015765f1662ba2f19da`；
- Skeleton P0 Run：`30732307826`；仅 Published 工厂显式骨架行为保持 1 项受控 RED；
- 干净代码 Head：`122ffc28165ff33c5e75955bfbece9a23c6803d7`；
- GREEN P0 Run：`30732488810`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：20 run / 0 failures / 0 errors / 0 skipped；
- 完整 12 模块 Reactor：PASSED；
- Java release 8：PASSED；
- 故意失败测试阻断：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Review 与 Evidence

- Review：`REV-000121`～`REV-000128`，全部 PASSED；
- Evidence：`EVD-000367`～`EVD-000372`，全部 ACTIVE；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t02_r03_completion.json`。

## 最终合同

1. CompilationRequest 完整接收 8 项调用方依赖；
2. Deadline 从 CompilationOptions 分离，并与 Timing 使用同一注入 MonotonicClock；
3. SourceProvider、FrontendRegistry、Clock、Observer 不依赖全局状态；
4. 条件发布使用 `Optional<EngineContext>`；
5. PublicationResult 与 PublicationStatus 分离；
6. CompilationResult 为 interface，仅暴露 status 与 diagnostics；
7. Published 结果暴露完整事实并拒绝任意重新拼接；
8. Failed 结果不暴露候选模型、Context、Digest 或版本；
9. 本任务未实现 T03 SourceGraph、真实 Frontend 或 Compiler Pipeline。

## 后续门禁

- PR #17 通过最终文档化 Head P0 后才能恢复 Ready for review；
- 未获得明确授权不得合并 PR #17；
- PR #17 合并到 `dev_all` 前，`TASK-P1-T03` 保持阻断。
