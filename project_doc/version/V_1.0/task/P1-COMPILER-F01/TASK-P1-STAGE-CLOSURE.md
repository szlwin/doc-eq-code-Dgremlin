# TASK-P1-STAGE-CLOSURE — P1 产品集成与阶段证据收口

## 1. 当前状态

```text
Logical Task: TASK-P1-STAGE-CLOSURE
External Closure Record: I001
Status: STAGE_COMPLETED / MACHINE_SYNCED
Base: dev_all@81aa3b40129d10a08b3f1a20ba6312b4015b9079
Rework Branch: rework/p1-stage-closure-20260807
PR: #31 / OPEN / NOT_MERGED
Reviewed Code Head: 75559ecc2e4791eddee166cf3010128130e27078
Canonical Code Review: CODEREVIEW-P1-STAGE-CLOSURE-R01@75559ecc2e47 / I008
Canonical Testing: TESTING-P1-STAGE-CLOSURE-R01@75559ecc2e47 / I009
Canonical Completion: COMPLETION-P1-STAGE-CLOSURE-R01@75559ecc2e47 / I009
Depends on: TASK-P1-T01 ~ TASK-P1-T15
```

PR #30 已在本任务开始前由外部操作合并到 `dev_all`；本轮 Stage Closure 返修从该合并后的精确基线单独建分支，不改写 PR #30 历史。`TASK-P1-STAGE-CLOSURE / I001` 继续作为人工恢复记录；正式 common-develop machine history 已通过 canonical `P1-COMPILER-F01` 的 Code Review I008、Testing I009、Completion I009 收口。

## 2. Findings

| Finding | Severity | 状态 | 关闭/同步依据 |
| --- | --- | --- | --- |
| `FND-P1-STAGE-001 / REQUIREMENT_COMPLIANCE / STARTER_BOUNDARY / ACCEPTANCE_ORACLE` | P1 | `CLOSED` | `CompilerBootstrapStageClosureTest` 3/3；真实 XML+YAML、十阶段、Digest、CAS publish、失败不污染已有 Context。 |
| `FND-P1-STAGE-002 / EVIDENCE_INTEGRITY / TRACEABILITY / STAGE_STATE` | P2 | `CLOSED / MACHINE_SYNCED` | 已由正式状态机从 `code_review` 重开，保留旧 machine history/stale chain；Code Review I008、Testing I009、Completion I009 全部 PASSED，`task_state.md` / `stage_outcomes.md` 已绑定本轮 Stage Closure revision。 |
| `FND-P1-STAGE-003 / SECURITY / CLASSPATH_ROOT / SYMLINK_ESCAPE` | P1 | `CLOSED` | Provider 对 `file:` 资源执行物理路径/真实路径边界和 symlink fail-closed；escape/cycle 真实临时文件系统负向测试通过。 |
| `FND-P1-STAGE-004 / RESOURCE_BUDGET / SOURCE_READ / PRE_ALLOCATION` | P1 | `CLOSED` | Provider 在流式读取写入增长缓冲区前执行字节上限，并对文件集累计预算；single/aggregate budget 负向测试通过。 |

当前 Open P0/P1/P2：`0 / 0 / 0`。Stage Completion 不再被 Finding 阻断。

## 3. 冻结目标

1. `CompilerBootstrap` 必须组装生产 `DocumentSourceProvider`、XML/YAML `FrontendRegistry`、固定十阶段 `CompilerPipeline`、单调时钟、Observer 与调用方提供的 `ContextPublisher`。
2. 从根 `SourceReference`、`CompilationOptions` 和显式 CAS 预期一次完成 compile-and-publish。
3. 真实 mix fixture 必须同时经过 XML 与 YAML Frontend，并依次形成 SourceGraph、Canonical、Raw、Symbol、Reference、Information、ModelAccess、Deferred、Digest、Candidate Context 和 PUBLISHED 结果。
4. 第二次失败编译不得调用 Publisher，不得覆盖此前成功 Context。
5. T15 Runtime Retirement Completion 保持有效，但不得继续等同于 P1 Stage Completion。
6. 根级 traceability、task_state、stage_outcomes、handoff 和 resume_context 必须绑定本轮有效 Revision、测试与 supersede 链。
7. `AllowedRoot` 必须同时约束逻辑 classpath URI 与 exploded-directory 的物理真实路径；目录扫描不得跟随符号链接。
8. Provider 必须在读取时执行与 `SourcePolicy.maxTotalBytes` 同源的硬字节预算，禁止先完整读入再校验。

## 4. TDD RED → GREEN 与最终复核

### RED

- Test-only Revision：`e565163c746e5b7e1fb09a7fa47912065d6ea627`；
- P0 Run：`31147472707`；
- `core-verify`：预期失败，新增测试在实现前三参数 Provider API 不存在；
- RED 覆盖：symlink escape、symlink cycle、oversized single source、aggregate byte budget。

### 初始 GREEN

- Code/Test Revision：`b603579d75770ca07760522e2df218047f6708ac`；
- P0 Run：`31147778389` — SUCCESS；
- Provider：7/7；Compiler：511/511；Starter：13/13；Stage Closure：3/3；MySQL：SUCCESS；T14/T15/intentional-failure gates：PASSED。

### 独立返修复核基线

- Reviewed Head：`75559ecc2e4791eddee166cf3010128130e27078`；
- P0 Run：`31148550742` — SUCCESS；
- Merge candidate：`d3319c5b9224b57fa4b174fc6741fa30d6c7d427`；
- Artifact：`8982454725`；SHA-256 `a1d04b81b259bd83a42a75ee180556748d135de82ae984dd8dd6c4db6a4431ac`；
- `core-verify` / `mysql-it`：SUCCESS / SUCCESS；
- `ClasspathDocumentSourceProviderTest`：7/7；`dec-core-compiler`：511/511；`dec-core-starter`：13/13；`CompilerBootstrapStageClosureTest`：3/3；
- T14 provenance mutation、intentional failing-test blocking、T15 retirement：PASSED。

## 5. 正式 machine-state 收口

- Reopen：从 `code_review` 合法重开，source revision=`75559ecc2e4791eddee166cf3010128130e27078`；旧 Code Review / Testing / Completion 结果进入 immutable history/stale chain；
- Code Review I008：`CODEREVIEW-P1-STAGE-CLOSURE-R01@75559ecc2e47` — PASSED；
- Code Review 独立 Review：`REV-000077` Architecture、`REV-000078` Spec、`REV-000079` Engineering、`REV-000080` CrossModule、`REV-000081` Impact、`REV-000082` Performance、`REV-000083` Security，全部 PASSED；
- Testing I009：`TESTING-P1-STAGE-CLOSURE-R01@75559ecc2e47` — PASSED；Test Evidence Review=`REV-000084`；
- Completion I009：`COMPLETION-P1-STAGE-CLOSURE-R01@75559ecc2e47` — PASSED；
- StageOutcome：`SO-P1-COMPILER-F01-CODE_REVIEW-I008`、`SO-P1-COMPILER-F01-TESTING-I009`、`SO-P1-COMPILER-F01-COMPLETION_VERIFICATION-I009`；
- `long_task validate`、`risk_detect validate`、`evidence validate`、`acceptance validate` 全部 PASSED；
- Open P0/P1=0；结构化 expected-results assertions 已补齐并 VERIFIED；
- 临时 GitHub 导出 workflow 已删除；相对 reviewed Head `75559ecc...`，`project_doc` 之外没有新增差异，也没有新增生产 Java 修改。

## 6. 返修实现边界

- `ClasspathDocumentSourceProvider` 已实现 physical/real path symlink fail-closed 与流式 byte budget；
- `CompilerBootstrap` 已将同一 `maxTotalBytes` 同时交给 Provider 与 `SourcePolicy`；
- 不再继续修改 P1-003/P1-004 的生产代码；本次 machine-state 收口只新增/更新 `project_doc` 机器状态、Evidence、Review 与恢复文档；
- `@Override` 在 reviewed production code 中保持独占一行；关键实现已有中文注释；
- 未恢复 Declaration Runtime，T15 retirement gate 保持通过。

## 7. 最终门禁

```text
Design: FROZEN
Plan: FROZEN
TDD: PASSED
Development: PASSED
Code Review I008: PASSED
Testing I009: PASSED
Completion I009: PASSED
Machine State Migration: PASSED
Open P0/P1/P2: 0/0/0
P1 Stage Completion: PASSED
PR #31: OPEN / NOT_MERGED
```

本记录已完成 Stage Completion。最终 machine-state 提交到 PR #31 后，GitHub P0 仅作为发布后确认；若该 CI 仍为 GREEN，可将 PR 从 Draft 标记为 Ready for Review。未经用户明确授权不得合并 PR #31，也不得把本次收口自动解释为后续 P2/catalog 开发授权。


## 8. 发布后确认

- Stage Closure Published Head：`06e70cbb9fd81f9e7e96c840f29ffc7e67ce53b6`；
- Final P0 Build Gate：`31161560840` — SUCCESS；
- `core-verify` / `mysql-it`：SUCCESS / SUCCESS；
- PR #31：Ready for Review / Open / Not merged；
- 发布后 Fact Sync 只同步版本级/人工投影视图，不重开已 PASSED 的 Code Review I008、Testing I009、Completion I009；
- 下一动作仅为人工 Review / Merge 决策；未经用户明确授权不得 merge，也不得进入 P2/catalog 开发。
