# TASK-P1-STAGE-CLOSURE — P1 产品集成与阶段证据收口

## 1. 当前迭代

```text
Logical Task: TASK-P1-STAGE-CLOSURE
Iteration: I001
Status: REWORK_VALIDATED / CI_GREEN / FINAL_REVIEW_PENDING
Base: dev_all@81aa3b40129d10a08b3f1a20ba6312b4015b9079
Rework Branch: rework/p1-stage-closure-20260807
PR: #31 / DRAFT / OPEN / NOT_MERGED
Depends on: TASK-P1-T01 ~ TASK-P1-T15
```

PR #30 已在本任务开始前由外部操作合并到 `dev_all`；本轮 Stage Closure 返修从该合并后的精确基线单独建分支，不改写 PR #30 历史。

## 2. Findings

| Finding | Severity | 状态 | 关闭/同步依据 |
| --- | --- | --- | --- |
| `FND-P1-STAGE-001 / REQUIREMENT_COMPLIANCE / STARTER_BOUNDARY / ACCEPTANCE_ORACLE` | P1 | `CLOSED` | `CompilerBootstrapStageClosureTest` 3/3；真实 XML+YAML、十阶段、Digest、CAS publish、失败不污染已有 Context。 |
| `FND-P1-STAGE-002 / EVIDENCE_INTEGRITY / TRACEABILITY / STAGE_STATE` | P2 | `PARTIALLY_RESOLVED / MACHINE_SYNC_BLOCKED` | `TASK-P1-STAGE-CLOSURE.md`、handoff、resume 与显式 overlay 已统一绑定本轮 Revision/CI；但 common-develop baseline guard 无效，禁止手工改写 `task_state.md` / `stage_outcomes.md` 的机器 JSON，正式迁移仍阻断 Completion。 |
| `FND-P1-STAGE-003 / SECURITY / CLASSPATH_ROOT / SYMLINK_ESCAPE` | P1 | `CLOSED` | Provider 对 `file:` 资源执行物理路径/真实路径边界和 symlink fail-closed；新增 escape/cycle 负向测试。 |
| `FND-P1-STAGE-004 / RESOURCE_BUDGET / SOURCE_READ / PRE_ALLOCATION` | P1 | `CLOSED` | Provider 在流式读取写入增长缓冲区前执行字节上限，并对文件集累计预算；新增 single/aggregate 负向测试。 |

当前实现/代码候选上 Open P0/P1：`0 / 0`；Open P2：`1`（`FND-P1-STAGE-002` 的 machine record 迁移）。最终独立 Review 仍是 Stage Completion 的必经门禁，且 P2 未清零前不得写 Completion。

## 3. 冻结目标

1. `CompilerBootstrap` 必须组装生产 `DocumentSourceProvider`、XML/YAML `FrontendRegistry`、固定十阶段 `CompilerPipeline`、单调时钟、Observer 与调用方提供的 `ContextPublisher`。
2. 从根 `SourceReference`、`CompilationOptions` 和显式 CAS 预期一次完成 compile-and-publish。
3. 真实 mix fixture 必须同时经过 XML 与 YAML Frontend，并依次形成 SourceGraph、Canonical、Raw、Symbol、Reference、Information、ModelAccess、Deferred、Digest、Candidate Context 和 PUBLISHED 结果。
4. 第二次失败编译不得调用 Publisher，不得覆盖此前成功 Context。
5. T15 Runtime Retirement Completion 保持有效，但不得继续等同于 P1 Stage Completion。
6. 根级 traceability、task_state、stage_outcomes、handoff 和 resume_context 必须绑定本轮有效 Revision、测试与 supersede 链。
7. `AllowedRoot` 必须同时约束逻辑 classpath URI 与 exploded-directory 的物理真实路径；目录扫描不得跟随符号链接。
8. Provider 必须在读取时执行与 `SourcePolicy.maxTotalBytes` 同源的硬字节预算，禁止先完整读入再校验。

## 4. TDD RED → GREEN

### RED

- Test-only Revision：`e565163c746e5b7e1fb09a7fa47912065d6ea627`；
- P0 Run：`31147472707`；
- `core-verify`：`FAILED`，失败点为新增测试要求三参数 `ClasspathDocumentSourceProvider(ClassLoader, AllowedRoot, long)`，而生产代码尚无该 API；
- 该失败发生在 `dec-core-compiler` testCompile，属于预期 RED，不是 runner/network 基础设施失败；
- RED 覆盖：symlink escape、symlink cycle、oversized single source、aggregate byte budget。

### GREEN

- Code/Test Revision：`b603579d75770ca07760522e2df218047f6708ac`；
- Provider Revision：`60f1b22b903a214455981704419130471819059a`；
- P0 Run：`31147778389` — `SUCCESS`；
- `core-verify` Job：`92770789003` — `SUCCESS`；
- `mysql-it` Job：`92770789019` — `SUCCESS`；
- Core Artifact：`8982191285`，SHA-256 `2c7103f36ed4aa12e891408a50a855a003b3dee45f87e808754cea9a2078d328`；
- MySQL Artifact：`8982163220`，SHA-256 `5af08b353a68af719700ec14c940a14aebccc5f0534c6d7b64db6978374c17b9`。

验证结果：

- `ClasspathDocumentSourceProviderTest`：7/7 passed，其中 4 个新增安全/预算负向测试全部通过；
- `dec-core-compiler`：511 tests，0 failures/errors/skipped；
- `dec-core-starter`：13 tests，0 failures/errors/skipped；
- `CompilerBootstrapStageClosureTest`：3/3 passed；
- T14 provenance gate：PASSED；
- T15 retirement gate：PASSED；
- intentional-failure gate：PASSED（正确证明 Maven 测试失败会阻断构建）；
- MySQL 业务 suite：`RuleTests`、`DirectoryTest`、`OrderTest` 共 3/3 passed，0 failures/errors/skipped；
- 数据库执行标记：3/3；最终表计数 order/order-detail/pay/pay-detail/product/user=`1/1/1/1/1/3`。

完整验证证据见：`evidence/stage-closure-i001-rework-validation.md`。

## 5. 返修实现边界

- `ClasspathDocumentSourceProvider` 新增显式读取预算构造参数，并保留 64 MiB 兼容默认值；
- `CompilerBootstrap` 将 `builder.maxTotalBytes` 同时传给 `SourcePolicy` 与 Provider，避免策略与读取层预算漂移；
- exploded-directory 扫描遇到 symlink 直接 fail-closed，单文件 `file:` 资源在读取前校验 physical AllowedRoot 与 real path；
- jar entry 不引入文件系统 symlink 跳转，继续沿用逻辑 classpath AllowedRoot；
- `readAll` 在超过预算的 chunk 写入 `ByteArrayOutputStream` 前失败；文件集使用同一次解析的 `remainingBytes` 累计预算；
- `@Override` 保持独占一行，新增关键方法/逻辑均补充中文注释；
- 未恢复 Declaration Runtime，未扩大 T15 已完成的生产边界。

## 6. 当前门禁

```text
Design: FROZEN
Plan: FROZEN
TDD RED: PASSED / Run 31147472707
Development Rework: IMPLEMENTED / b603579d75770ca07760522e2df218047f6708ac
Pre-Review Full Validation: PASSED / Run 31147778389
Traceability Human/Repository Sync: IMPLEMENTED / THIS PR DOC COMMIT
Code Review: PENDING_FINAL_INDEPENDENT_REVIEW
Formal Testing: PENDING_REVIEW_GATE; Run 31147778389 is reusable only if final reviewed Head is unchanged
Machine State Migration: BLOCKED_BY_INVALID_COMMON_DEVELOP_BASELINE
P1 Stage Completion: BLOCKED_ON_FINAL_REVIEW_TESTING_AND_FND_P1_STAGE_002
```

本记录只声明返修实现与前置验证已收敛；不得在独立 Review 前写入最终 `PASSED`/Completion，也不得未经用户明确授权合并 PR #31。
