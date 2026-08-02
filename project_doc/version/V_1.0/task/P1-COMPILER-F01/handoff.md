# P1-COMPILER-F01 阶段交接

> `TASK-P1-T01` 最终 iteration `I010` 已通过 PR #16 合并到 `dev_all`。`TASK-P1-T02` 的 R01 因 T01 合同变化被重开，R02 因完整 API 规格 Review 被推翻，R03 又因 Source/Frontend 数据闭包 Review 被推翻；最新 REWORK iteration `I004` 已以 `COMPLETION-P1-T02-R04@8b3e716a9730` 完成。所有旧 Revision、Review、Evidence 和 PR 记录作为历史保留。

## T01 REWORK I010 已完成并合并

- Completion：`COMPLETION-P1-T01-R04@ee99223a243f`；
- Merge commit：`f88f45731e16868bfacb489b63e3086aae49d018`；
- Context 测试：26 run / 0 failures / 0 errors / 0 skipped；
- 最终发布聚合、Projection 写入拒绝和派生 List 闭包均已冻结。

## T02 REWORK I004 已完成

- 基线：`dev_all@f88f45731e16868bfacb489b63e3086aae49d018`；
- Design：`DESIGN-R11@P1-T02-REWORK-I004`；
- Implementation Plan：`TP-P1-COMPILER-F01-R07@P1-T02-REWORK-I004`；
- TDD：`TDD-P1-T02-R04@b0502ee13dba`，P0 Run `30733257810` 形成 4 failures / 0 errors 的有效 RED；
- Architecture Skeleton：`DEVSKEL-P1-T02-R04@21d28d33eac9`，P0 Run `30733441104` 保持单一受控 RED；
- Development：`DEV-P1-T02-R04@8b3e716a9730`；
- Code Review：`CODEREVIEW-P1-T02-R04@8b3e716a9730`；
- Testing：`TESTING-P1-T02-R04@8b3e716a9730`；
- Completion：`COMPLETION-P1-T02-R04@8b3e716a9730`；
- Review：`REV-000129`～`REV-000137` 全部 PASSED；
- Evidence：`EVD-000373`～`EVD-000378` ACTIVE；
- 干净代码 Head：`8b3e716a9730d12fe84c6efd3fb8481998e335e0`；
- P0 Run：`30733616822`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：35 run / 0 failures / 0 errors / 0 skipped；
- 完整 12 模块 Reactor：PASSED；
- Java release 8：PASSED；
- 故意失败测试阻断门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1 Finding：无。

## T02 最终公共合同

### Session、发布与结果边界

- `CompilationRequest` 显式持有 root、Source Provider、Frontend Registry、options、Optional Deadline、CancellationToken、MonotonicClock 和 Observer；
- `CompilationOptions` 只保存 schema version 与 options digest，Deadline 不参与语义选项；
- `PublicationRequest` 与 `ContextPublisher` 使用 `Optional<EngineContext>`；
- `PublicationResult` interface 与 `PublicationStatus` enum 分离；
- `CompilationResult` 为 interface，只暴露 status 和 diagnostics；
- Published 结果暴露 modelSet、engineContext、digests、compiler/schema/options/digest-algorithm 事实并绑定单一发布事实；
- Failed 结果只暴露失败状态和至少一个 ERROR Diagnostic。

### Source 与 Frontend 数据闭包

- `DocumentSource` 是 final immutable value object，完整冻结 `sourceId`、规范化绝对 `URI`、显式 `DocumentFormat`、`AllowedRoot`、防御性内容字节和 content digest；
- `AllowedRoot` 验证 scheme、authority 与规范化路径段边界，并拒绝兄弟前缀、编码穿越、query、fragment 和不同 authority；
- `CanonicalDocumentNode` 冻结名称、稳定排序属性、Optional 标量、顺序子节点、SourceRef、格式和 Schema 版本；
- `FrontendResult` 暴露 `FrontendStatus`、`Optional<CanonicalDocumentNode>` 和不可变 diagnostics；
- PARSED 恰有一个 Canonical 根且不含 ERROR；FAILED 不携带 Canonical 候选且至少包含一个 ERROR；
- `SourceResolutionResults` 对 RESOLVED/FAILED 执行相同的候选隔离和 Diagnostic 不变量；
- Provider → `DocumentSource.format()` → `FrontendRegistry.require()` → `DocumentFrontend.parse()` → `FrontendResult.canonicalRoot()` 已由可执行 Oracle 冻结；
- 公共 API 不暴露 DOM、YAML Node 或第三方 Parser 类型；
- T03 无需修改 T02 已冻结的公共构造器或方法签名。

## 编码和范围

- Compiler 模块只依赖 Context，不存在 Context → Compiler 反向依赖；
- 未修改 `dec-core-context` 生产代码；
- 未实现 SourceGraph、真实 XML/YAML Frontend 或 Compiler Pipeline；
- 所有新增和修改的 `@Override` 独占一行；
- 方法、构造器和重要逻辑使用中文注释。

## PR 状态与下一步

- 当前 PR：`#17`，目标分支 `dev_all`；
- 被推翻的 T02 Completion：R01、R02、R03 均只作为历史保留；
- 被替代 PR：`#15`，已关闭且未合并；
- `TASK-P1-T03` 尚未启动；
- 必须先完成 PR #17 Review 与合并，再从新的 `dev_all` 启动 T03；
- 未获得明确授权不得合并 PR #17。
