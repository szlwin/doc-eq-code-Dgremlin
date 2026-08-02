# TASK-P1-T02 REWORK I005

- 任务：`TASK-P1-T02`
- Iteration：`I005`
- 状态：`COMPLETED`
- 分支：`feature/p1-t02-rework-i002-20260802-1116`
- PR：`#17`，Completion 后恢复 Ready for review
- 基线：`dev_all@f88f45731e16868bfacb489b63e3086aae49d018`
- 重开输入 Head：`eeb29ac6e5380c92b753ce1b16efcfdc17b98aee`
- 被推翻 Completion：`COMPLETION-P1-T02-R04@8b3e716a9730`
- 设计：`DESIGN-R12@P1-T02-REWORK-I005`
- 实施计划：`TP-P1-COMPILER-F01-R08@P1-T02-REWORK-I005`
- TDD：`TDD-P1-T02-R05@0e2924d4f125`
- Architecture Skeleton：`DEVSKEL-P1-T02-R05@2bda34e6eed1`
- Development：`DEV-P1-T02-R05@35376308b013`
- Code Review：`CODEREVIEW-P1-T02-R05@35376308b013`
- Testing：`TESTING-P1-T02-R05@35376308b013`
- Completion：`COMPLETION-P1-T02-R05@35376308b013`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

独立负向规格 Review 证明 I004 已建立 Source/Frontend 主数据闭包，但 `AllowedRoot` 在规范化前没有拒绝原始父目录穿越，且 opaque URI 的 query 标记不属于 `URI.getQuery()`，从而可以绕过安全根检查；同时 `SourceResolutionResults.resolved(...)` 未区分单 Source 和文件集，也未拒绝重复 `sourceId`。I004 的 Design、Review、Testing、Completion 和 Evidence 全部保留，但“开放 P0/P1 为 0”和完整安全来源合同结论由 I005 替代。

## Finding 关闭结果

- `FND-P1-T02-I005-001` — CLOSED：原始 raw/decoded URI 在 normalize 前后均验证，opaque query/fragment 明确拒绝；
- `FND-P1-T02-I005-002` — CLOSED：成功工厂已拆分，单源与文件集基数、sourceId 唯一性和第三方结果验证已冻结；
- `FND-P1-T02-I003-004` — CLOSED_AFTER_REOPEN：安全根和解析基数负向 Oracle 子项已补齐。

开放 P0/P1 Finding：无。

## 最终实现事实

- `AllowedRoot` 在规范化前后检查 raw 与解码 location；
- 层次 URI 通过 query/fragment 字段验证，opaque URI 通过 scheme-specific part 验证 `?`；
- `DocumentSource` 将原始 URI 交给 `AllowedRoot.contains(...)`，通过后才保存规范化 URI；
- 通用 `resolved(List,List)` 已移除；
- `resolvedSingle(...)` 恰好携带一个 Source；
- `resolvedFileSet(...)` 至少携带一个 Source；
- 两种成功结果均拒绝 ERROR、null Source 和重复 sourceId，并稳定排序；
- `validateSingle(...)` 与 `validateFileSet(...)` 防御性复制合法第三方结果；
- null、错误基数、重复身份、成功含 ERROR、失败含部分 Source 或无 ERROR 均转换为无候选 `MIX-SOURCE-POLICY` FAILED。

## TDD 与测试

- 有效 RED Head：`0e2924d4f125c971d1189ac24399a7b975b2e1d0`；
- RED P0 Run：`30734576119`；
- RED：Context 26/26、既有 Compiler 35/35 通过；新增 8 项为 8 failures / 0 errors；
- Skeleton Head：`2bda34e6eed1a394833781db9719d4fc923a9957`；
- Skeleton P0 Run：`30734683602`；
- Skeleton：Compiler 43 项中仅 4 项受控 failure，0 errors；
- Clean-code Head：`35376308b0133344ebddadc1bf45e07c11f7959c`；
- GREEN P0 Run：`30734789072`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：47 run / 0 failures / 0 errors / 0 skipped；
- 完整 12 模块 Reactor：PASSED；
- Java release 8：PASSED；
- 故意失败测试阻断：PASSED；
- Artifact：`8829179331`；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Review 与 Evidence

- Review：`REV-000138`～`REV-000144` 全部 PASSED；
- Evidence：`EVD-000379`～`EVD-000385` ACTIVE；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t02_r05_completion.json`。

## 范围和后续门禁

- 未修改 `dec-core-context` 生产代码；
- 未实现文件系统 real-path、符号链接解析、SourceGraph、真实 Frontend、RawDefinitionSet 或 Compiler Pipeline；
- 所有新增和修改的 `@Override` 均独占一行；
- 方法、构造器和重要逻辑均使用中文注释；
- PR #17 未获得明确授权不得合并；
- PR #17 合并到 `dev_all` 前，`TASK-P1-T03` 继续保持未启动和阻断。
