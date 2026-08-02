# TASK-P1-T03 REWORK I003

- Task：`TASK-P1-T03`
- Iteration：`I003`
- 状态：`COMPLETED`
- 分支：`feature/p1-t03-source-graph-20260802-1430`
- Rework base：`4f218f5dbf329949b8f7b3d7396668919482d198`
- 原 Completion：`COMPLETION-P1-T03-R02@6af43b47f044`（被独立 Review 推翻，历史保留）
- Design：`DESIGN-R15@P1-T03-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R11@P1-T03-REWORK-I003`
- TDD：`TDD-P1-T03-R03@d5b42e9eb166`
- Architecture Skeleton：`DEVSKEL-P1-T03-R03@3c9f64c4ac11`
- Development：`DEV-P1-T03-R03@cedf22bb14ff`
- Code Review：`CODEREVIEW-P1-T03-R03@cedf22bb14ff`
- Testing：`TESTING-P1-T03-R03@cedf22bb14ff`
- Completion：`COMPLETION-P1-T03-R03@cedf22bb14ff`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Findings 关闭结果

- `FND-P1-T03-I003-001` / P1：已关闭。`.`、`./`、`./.` 均冻结为非空 canonical key `.`；Resolver 返回 `FAILED / MIX-SOURCE-PATH-ESCAPE / graph empty`，Provider access 为 0，无未分类异常泄漏。
- `FND-P1-T03-I003-002` / P1：已关闭。opaque、hierarchical 和相对路径中的 `%2e/%2E` 独立点段统一删除；Provider、Edge、duplicate key、graph equality 和 cycle stack 使用统一 key。
- `FND-P1-T03-I003-003` / P2：已关闭。同一固定 XML 已分别以 CRLF、CR 运行，7 条边的 line、column、nodePath 均直接验证。

## 安全边界

- `%2e%2e`、`.%2e`、`%2e.` 等解码后为父目录的 segment 保留原始文本，由 SourcePolicy 拒绝；
- `%2F` 等编码分隔符不解码，不改变路径 segment 边界；
- 不对完整 URI 进行 URL decode；
- query、fragment、AllowedRoot、DTD、外部实体和外部资源门禁保持不变。

## 验证结果

- Clean-code Head：`cedf22bb14ffbcd45e0eff2f680c3505dc9f7ed0`
- P0 Run：`30739517365`
- Artifact：`8830794341`
- Artifact SHA-256：`b833525e84af935c99c05ed3ab95424d59577f98bd2fa54905473c5cc08b7973`
- Context：26 run / 0 failures / 0 errors / 0 skipped
- Compiler：74 run / 0 failures / 0 errors / 0 skipped
- I003：6 run / 0 failures / 0 errors / 0 skipped
- 完整 12 模块 Reactor：PASSED
- Java release 8：PASSED
- 故意失败阻断：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`
- 开放 P0/P1：无

## 范围与后续门禁

- 精确 10 Source / 7 Edge 合同保持不变；
- 未修改 `dec-core-context` 生产代码；
- 未启动 T04；
- 所有新增和修改的 `@Override` 独占一行；
- 方法、构造器和关键逻辑使用中文注释；
- PR #18 未经明确授权不得合并；
- PR #18 合并前 T04 保持阻断。
