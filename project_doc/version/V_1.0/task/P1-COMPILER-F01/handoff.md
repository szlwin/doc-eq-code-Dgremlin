# P1-COMPILER-F01 阶段交接

> `TASK-P1-T01` 的 R01、R02、R03 Completion 均因后续完整规格 Review 被重开；最新 REWORK iteration `I010` 已以 `COMPLETION-P1-T01-R04@ee99223a243f` 完成全部流程。所有旧 Revision、Review 和 Evidence 作为历史保留。

## T01 REWORK I010 已完成

- Design Revision：`DESIGN-R08@P1-T01-REWORK-I010`；
- Implementation Plan：`TP-P1-COMPILER-F01-R04@P1-T01-REWORK-I010`；
- TDD：`TDD-P1-T01-R04@f87a3f96fcbb`，P0 Run `30729765475` 形成有效 RED，`REV-000103` PASSED；
- Architecture Skeleton：`DEVSKEL-P1-T01-R04@1865378a29e3`，`REV-000104`、`REV-000105` PASSED；
- Development：`DEV-P1-T01-R04@ee99223a243f`；
- Code Review：`CODEREVIEW-P1-T01-R04@ee99223a243f`，`REV-000106`～`REV-000109` PASSED；
- Testing：`TESTING-P1-T01-R04@ee99223a243f`，`REV-000110` PASSED；
- Completion：`COMPLETION-P1-T01-R04@ee99223a243f`，`REV-000111` PASSED；
- Evidence：`EVD-000355`～`EVD-000360` ACTIVE；
- 干净代码 Head：`ee99223a243fd5f470e37e3e81b50c9980524ae4`；
- P0 Run：`30729866803`，完整 Reactor 与失败门禁均 PASSED；
- Context 测试：26 run / 0 failures / 0 errors / 0 skipped；
- MySQL Job：T01 无数据库变更，`SKIPPED_NOT_APPLICABLE`；
- I008、I009 已关闭的 Finding 保持 CLOSED；
- I010 的 `FND-P1-T01-I010-001` CLOSED；
- 开放 P0/P1 Finding：无。

## 最终 Projection 写入拒绝闭包

- `ProjectionWriteRejectedException` 保持 `UnsupportedOperationException` 兼容性并携带 `MIX-PROJECTION-WRITE` Diagnostic；
- 四个 deprecated 兼容写入口只能拒绝，不能修改发布事实；
- Data/View/Rule 根 List 的 Java 8 写入口统一拒绝；
- 空、非空、嵌套 `subList` 均由防御性 `ProjectionReadOnlyList` 快照承载；
- 根 List 和所有派生子列表的 Iterator/ListIterator 写方法均统一拒绝；
- 即使列表为空或尚未执行 `next()`，也不会先返回普通集合异常或无操作成功；
- 所有拒绝前后 `CompiledModelSet`、根 Projection 和派生快照保持不变。

## 下一步

1. 先将 PR #16 合并到 `dev_all`；
2. PR #15（TASK-P1-T02）必须基于新的 `dev_all` rebase，并适配 T01 的最终公共合同；
3. 对 T02 重新执行受影响 TDD/测试、独立 Review、Testing 和 Completion Verification；
4. T02 重验证完成前不得启动 TASK-P1-T03。

当前没有在 T01 分支启动 T02 或 T03 实现。
