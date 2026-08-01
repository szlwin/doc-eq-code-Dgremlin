# P1-COMPILER-F01 阶段交接

> `TASK-P1-T01` 的 R01 与 R02 Completion 均因后续完整规格 Review 被重开；REWORK iteration `I009` 已以 `COMPLETION-P1-T01-R03@175b86e1e3ea` 完成全部流程。所有旧 Revision、Review 和 Evidence 作为历史保留。

## T01 REWORK I009 已完成

- Design Revision：`DESIGN-R07@P1-T01-REWORK-I009`；
- Implementation Plan：`TP-P1-COMPILER-F01-R03@P1-T01-REWORK-I009`；
- TDD：`TDD-P1-T01-R03@81b071739b19`，P0 Run `30707008948` 形成有效 RED，`REV-000094` PASSED；
- Architecture Skeleton：`DEVSKEL-P1-T01-R03@7f41cb0d06dd`，`REV-000095`、`REV-000096` PASSED；
- Development：`DEV-P1-T01-R03@6c8a2d1a7cd5`；
- Code Review：`CODEREVIEW-P1-T01-R03@175b86e1e3ea`，`REV-000097`～`REV-000100` PASSED；
- Testing：`TESTING-P1-T01-R03@175b86e1e3ea`，`REV-000101` PASSED；
- Completion：`COMPLETION-P1-T01-R03@175b86e1e3ea`，`REV-000102` PASSED；
- Evidence：`EVD-000349`～`EVD-000354` ACTIVE；
- 干净代码 Head：`6c8a2d1a7cd5a6b760a19598737b569bfe8de8b9`；
- 代码与设计验证 Head：`175b86e1e3eabb718c7f3782ca6a794d6c381bfe`；
- P0 Run：`30707306280`，完整 Reactor 与失败门禁均 PASSED；
- Context 测试：21 run / 0 failures / 0 errors / 0 skipped；
- MySQL Job：T01 无数据库变更，`SKIPPED_NOT_APPLICABLE`；
- I008 已关闭的五个 P1 Finding 保持 CLOSED；
- I009 的三个 P1 Finding 全部 CLOSED；
- 开放 P0/P1 Finding：无。

## 本轮冻结的补充合同

- `ProjectionWriteRejectedException` 是 Projection 写入的稳定专用异常，并保持 `UnsupportedOperationException` 兼容性；
- 异常携带 `MIX-PROJECTION-WRITE`、ERROR severity、稳定 SourceRef、messageKey、pass 与中文恢复建议；
- `register`、`replace`、`remove`、`clear` 四个 deprecated 兼容入口只能拒绝，不能修改发布事实；
- `data()`、`views()`、`rules()` 返回的 List 对 Java 8 变更方法统一产生相同专用拒绝语义；
- 所有拒绝前后 `CompiledModelSet`、Projection 列表和值保持不变；
- `PublishedSourceDependency` 要求 `declarationSourceRef.sourceId()` 等于 `fromSourceId`；
- 普通依赖边与 synthetic root edge 均遵守相同来源身份；
- `PublishedSourceManifest` 保留防御性二次校验。

## 下一步

1. 先将 PR #16 合并到 `dev_all`；
2. PR #15（TASK-P1-T02）必须基于新的 `dev_all` rebase，并适配 T01 的最终公共合同；
3. 对 T02 重新执行受影响 TDD/测试、独立 Review、Testing 和 Completion Verification；
4. T02 重验证完成前不得启动 TASK-P1-T03。

当前没有在 T01 分支启动 T02 或 T03 实现。
