# TASK-P1-T02 REWORK I005

- 任务：`TASK-P1-T02`
- Iteration：`I005`
- 状态：`REWORK`
- 分支：`feature/p1-t02-rework-i002-20260802-1116`
- PR：`#17`，当前 Draft
- 基线：`dev_all@f88f45731e16868bfacb489b63e3086aae49d018`
- 重开输入 Head：`eeb29ac6e5380c92b753ce1b16efcfdc17b98aee`
- 被推翻 Completion：`COMPLETION-P1-T02-R04@8b3e716a9730`
- 设计：`DESIGN-R12@P1-T02-REWORK-I005`
- 实施计划：`TP-P1-COMPILER-F01-R08@P1-T02-REWORK-I005`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

独立负向规格 Review 证明 I004 已建立 Source/Frontend 主数据闭包，但 `AllowedRoot` 在规范化前没有拒绝原始父目录穿越，且 opaque URI 的 query 标记不属于 `URI.getQuery()`，从而可以绕过安全根检查；同时 `SourceResolutionResults.resolved(...)` 未区分单 Source 和文件集，也未拒绝重复 `sourceId`。I004 的 Design、Review、Testing、Completion 和 Evidence 全部保留，但“开放 P0/P1 为 0”和完整安全来源合同结论失效。

## 开放 Finding

- `FND-P1-T02-I005-001` — P1：`AllowedRoot` 可接受原始父目录穿越根，并可接受 opaque URI query 根或候选；
- `FND-P1-T02-I005-002` — P1：SourceResolution 成功结果未冻结单源基数、文件集非空、`sourceId` 唯一性和第三方结果防御性验证；
- `FND-P1-T02-I003-004` — REOPENED_SUBSET：完整 API Oracle 的安全根和解析基数负向子项重新打开。

## 保持关闭的合同

- CompilationRequest 8 参数 Session 输入边界；
- Deadline 与 CompilationOptions 分离；
- Optional 条件发布与 PublicationResult/Status 分离；
- CompilationResult interface、Published 完整事实与 Failed 候选隔离；
- DocumentSource 六项来源事实及内容防御性复制；
- CanonicalDocumentNode 不可变树与 Frontend PARSED/FAILED 候选隔离；
- Provider → Frontend → Canonical 主数据链路；
- Parser 实现类型隔离和 Compiler → Context 单向依赖。

## 门禁

1. 先建立 Java 8 可编译的负向 Test Oracle，并形成有效 RED，再修改生产实现；
2. `AllowedRoot` 必须在 `URI.normalize()` 前检查原始 raw 与解码 location；
3. 层次 URI 与 opaque URI 的 query/fragment 必须分别按真实 URI 语义拒绝；
4. 成功工厂必须区分 `resolvedSingle` 与 `resolvedFileSet`，不得保留可误用的通用成功工厂；
5. 单源结果固定一个 Source，文件集至少一个 Source，二者均拒绝重复 `sourceId`；
6. 第三方 `SourceResolutionResult` 合同违规必须可由 T03 使用冻结 API 转换为 `MIX-SOURCE-POLICY` FAILED 结果，且不泄漏部分 Source；
7. 不修改 `dec-core-context` 生产代码；
8. 不实现 T03 SourceGraph、真实 Frontend 或 Compiler Pipeline；
9. 开放 P0/P1 阻断 Completion；
10. 所有 `@Override` 单独一行；
11. 方法、构造器和重要逻辑使用中文注释；
12. PR #17 在 Completion 前保持 Draft，合并前 TASK-P1-T03 保持阻断。
