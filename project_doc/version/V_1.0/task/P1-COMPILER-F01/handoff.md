# P1-COMPILER-F01 阶段交接

> `TASK-P1-T01` 最终 iteration `I010` 已通过 PR #16 合并到 `dev_all`。`TASK-P1-T02` 的 R01～R04 均因后续独立 Review 发现新的合同缺口而作为历史保留；最新有效 iteration 为 `I005`，Completion 为 `COMPLETION-P1-T02-R05@35376308b013`。

## T01 REWORK I010 已完成并合并

- Completion：`COMPLETION-P1-T01-R04@ee99223a243f`；
- Merge commit：`f88f45731e16868bfacb489b63e3086aae49d018`；
- Context 测试：26 run / 0 failures / 0 errors / 0 skipped。

## T02 REWORK I005 已完成

- 基线：`dev_all@f88f45731e16868bfacb489b63e3086aae49d018`；
- Design：`DESIGN-R12@P1-T02-REWORK-I005`；
- Implementation Plan：`TP-P1-COMPILER-F01-R08@P1-T02-REWORK-I005`；
- TDD：`TDD-P1-T02-R05@0e2924d4f125`，P0 Run `30734576119` 形成 8 failures / 0 errors 的有效 RED；
- Architecture Skeleton：`DEVSKEL-P1-T02-R05@2bda34e6eed1`，P0 Run `30734683602` 保持 4 项受控 RED；
- Development：`DEV-P1-T02-R05@35376308b013`；
- Code Review：`CODEREVIEW-P1-T02-R05@35376308b013`；
- Testing：`TESTING-P1-T02-R05@35376308b013`；
- Completion：`COMPLETION-P1-T02-R05@35376308b013`；
- Review：`REV-000138`～`REV-000144` 全部 PASSED；
- Evidence：`EVD-000379`～`EVD-000385` ACTIVE；
- Clean-code Head：`35376308b0133344ebddadc1bf45e07c11f7959c`；
- P0 Run：`30734789072`；
- Artifact：`8829179331`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：47 run / 0 failures / 0 errors / 0 skipped；
- 完整 12 模块 Reactor、Java release 8、故意失败阻断门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1 Finding：无。

## T02 最终公共合同

### Session、发布与结果边界

- `CompilationRequest` 显式持有 root、Source Provider、Frontend Registry、options、Optional Deadline、CancellationToken、MonotonicClock 和 Observer；
- Deadline 与语义 Options 分离；
- 条件发布使用 `Optional<EngineContext>`；
- `PublicationResult/PublicationStatus` 与 `CompilationResult` interface 已冻结；
- Published 结果绑定模型、Context、Digest 和版本事实；Failed 结果不暴露候选事实。

### Source、Frontend 与安全策略闭包

- `DocumentSource` 冻结 `sourceId`、URI、格式、AllowedRoot、内容和摘要，并在规范化前验证原始 URI；
- `AllowedRoot` 在 normalize 前后检查 raw 与解码 location，拒绝字面量/编码 traversal、query、fragment、不同 scheme/authority 和兄弟前缀；
- opaque URI 的 query 通过 scheme-specific part 识别，不依赖 `URI.getQuery()`；
- `CanonicalDocumentNode` 与 `FrontendResult` 冻结格式中立的成功/失败候选隔离；
- `resolvedSingle` 恰好一个 Source，`resolvedFileSet` 至少一个 Source；
- 所有成功 Source 按 sourceId 排序并拒绝重复身份；
- `validateSingle/validateFileSet` 对第三方 Provider 结果执行防御性复制和完整合同验证；
- null、错误基数、重复 sourceId、成功含 ERROR、失败含部分 Source 或无 ERROR 均转换为无候选 `MIX-SOURCE-POLICY` FAILED；
- Provider → Source → Frontend → Canonical 主数据流可直接供 T03 使用，无需修改 T02 公共签名；
- 公共 API 不暴露 DOM、YAML Node 或第三方 Parser 类型。

## 编码和范围

- Compiler 模块只依赖 Context；未修改 `dec-core-context` 生产代码；
- 未实现 real-path、符号链接解析、SourceGraph、真实 Frontend、RawDefinitionSet 或 Compiler Pipeline；
- 所有新增和修改的 `@Override` 独占一行；
- 方法、构造器和重要逻辑使用中文注释。

## PR 状态与下一步

- 当前 PR：`#17`，目标分支 `dev_all`；
- 被推翻的 T02 Completion R01～R04 均作为历史保留；
- 被替代 PR：`#15`，已关闭且未合并；
- `TASK-P1-T03` 尚未启动；
- 必须先完成 PR #17 Review，并取得明确合并授权后才能合并；
- PR #17 合并后，才能从新的 `dev_all` 启动 T03。
