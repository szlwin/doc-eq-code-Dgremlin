# TASK-P1-T05 / I002 — YAML 来源事实 Rework

- 状态：`COMPLETED`
- PR：`#20`
- Branch：`feature/p1-t05-yaml-canonical-20260802-2106`
- Rework Base：`52fe48d46dd2c4ac9c822d5be141d47c03ae955f`
- Dependency：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- Superseded Completion：`COMPLETION-P1-T05-R01@040f09b80463`
- Design：`DESIGN-R21@P1-T05-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R17@P1-T05-REWORK-I002`
- TDD：`TDD-P1-T05-R02@c362011eac56`
- Architecture Skeleton：`DEVSKEL-P1-T05-R02@122f8ddc37df`
- Development：`DEV-P1-T05-R02@27d566714f5c`
- Code Review：`CODEREVIEW-P1-T05-R02@27d566714f5c`
- Testing：`TESTING-P1-T05-R02@27d566714f5c`
- Completion：`COMPLETION-P1-T05-R02@27d566714f5c`
- Review：`REV-000231`～`REV-000243`
- Evidence：`EVD-000475`～`EVD-000486`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 独立 Review

- 推翻 Review：`REV-000231`
- 推翻 Evidence：`EVD-000475`
- 结论：I001 Completion R01 失效但作为不可变历史保留

## Finding Closure

- `FND-P1-T05-I002-001` `[P1][BLOCKER]`：`CLOSED` — 严格 UTF-8 REPORT + 4 类原始 byte[] Oracle；
- `FND-P1-T05-I002-002` `[P1][BLOCKER]`：`CLOSED` — 标准 tag 全量词法策略 + 四位置正负 Oracle；
- `FND-P1-T05-I002-003` `[P2]`：`CLOSED` — portable name policy + nodePath 正负 Oracle。

## 完成事实

1. 非法 UTF-8 不再替换为 U+FFFD 后继续解析；
2. malformed/unmappable 输入在 parser 创建前稳定失败；
3. bool/int/float/null/timestamp 同时通过 tag 白名单和冻结词法校验；
4. 合法显式及隐式 typed scalar 保留原始词法，不执行 Java 业务对象构造；
5. `!!null attacker-data`、`!!int not-an-int`、`09`、`.`、非法日期/时间/时区均失败；
6. 普通 scalar、`#text`、属性 value 和 Sequence item 使用同一词法门禁；
7. 根、子节点和属性名使用 `[A-Za-z_][A-Za-z0-9._-]*`；
8. nodePath 不再接受 `/`、换行、冒号或非法首字符 segment；
9. 原有 tag/object/anchor/alias/递归/merge/资源/Canonical 合同未回退；
10. Context 26/26、Compiler 83/83、XML 30/30、YAML 45/45、Demo 4/4、legacy declaration 1/1 通过；
11. 12 模块 Reactor、Java release 8、故意失败门禁通过；
12. MySQL 为 `SKIPPED_NOT_APPLICABLE`；
13. Specification、Architecture、Security、Code、TDD 五类独立 Review 全部 PASSED；
14. 开放 P0/P1/P2 为 0；
15. `@Override` 独占一行，公共方法、构造器和重要逻辑使用中文注释；
16. 未修改 Context 生产代码、compiler canonical 公共 API或 XML 生产语义；
17. 未启动 T06；
18. PR #20 未经明确授权不得合并。

## Evidence 入口

- Design/Plan：`../evidence/design-plan-p1-t05-r02.md`；
- RED：`../evidence/tdd-red-p1-t05-r02.md`；
- Skeleton：`../evidence/architecture-skeleton-p1-t05-r02.md`；
- Development：`../evidence/development-p1-t05-r02.md`；
- Reviews：`../review/review-p1-t05-r02.md`；
- Testing：`../evidence/testing-p1-t05-r02.md`；
- Completion：`../evidence/commands/completion-p1-t05-r02/completion-report.json`；
- 机器恢复：`../../../tdd_p1_t05_r02_completion.json`。

## 历史保护

I001 的 Design R20、Plan R16、TDD R01、Skeleton R01、Development R01、Testing R01、Completion R01、Review `REV-000220`～`REV-000230`、Evidence `EVD-000464`～`EVD-000474` 均未覆盖或删除。
