# TASK-P1-T05 / I003 — Scalar Budget / Resolver Rework

- 状态：`COMPLETED`
- PR：`#20`
- Branch：`feature/p1-t05-yaml-canonical-20260802-2106`
- Rework Base：`499b977a773da3e25b776d4debf7abb1391b5192`
- Dependency：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- Superseded Completions：`COMPLETION-P1-T05-R01@040f09b80463`、`COMPLETION-P1-T05-R02@27d566714f5c`
- Design：`DESIGN-R22@P1-T05-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R18@P1-T05-REWORK-I003`
- TDD：`TDD-P1-T05-R03@3deacf0aa036`
- Architecture Skeleton：`DEVSKEL-P1-T05-R03@05873e286c2d`
- Development：`DEV-P1-T05-R03@30529276cd8f`
- Code Review：`CODEREVIEW-P1-T05-R03@30529276cd8f`
- Testing：`TESTING-P1-T05-R03@30529276cd8f`
- Completion：`COMPLETION-P1-T05-R03@30529276cd8f`
- Review：`REV-000244`～`REV-000256`
- Evidence：`EVD-000487`～`EVD-000498`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 独立 Review

- 推翻 Review：`REV-000244`
- 推翻 Evidence：`EVD-000487`
- 结论：I002 Completion R02 当前有效性失效，但全部历史不可变保留

## Finding Closure

- `FND-P1-T05-I003-001` `[P1][BLOCKER]`：`CLOSED` — 四位置原始长度前置门禁和诊断优先级 Oracle；
- `FND-P1-T05-I003-002` `[P1][BLOCKER]`：`CLOSED` — SnakeYAML 2.2 Resolver Pattern、四位置正负 Oracle 和结构 Oracle；
- `FND-P1-T05-I003-003` `[P1][PROCESS BLOCKER]`：`CLOSED` — 新建并锁定 R22/R18，clean-code blob 复核一致。

## 完成事实

1. `ScalarNode.getValue()` 在统一入口只读取一次；
2. 未经 trim 的原始长度先于 regex、timestamp 解析或其他词法处理；
3. 单值预算与最终 Canonical 累计预算分离；
4. 普通 scalar、`#text`、属性 value 和 Sequence item共享同一入口；
5. bool/int/float/null/timestamp 直接使用 SnakeYAML 2.2 `Resolver` 公开 Pattern；
6. 删除复制的数字接受正则和 `BigDecimal` / `BigInteger` construction；
7. timestamp 在官方 Pattern 后追加真实日期、时间和时区范围检查；
8. `1e3`、`1.2e3`、显式 `!!float 1e3` 四位置通过；
9. 显式 `!!int 0b_`、`0x_`、`0_` 四位置失败；
10. 超限合法及非法 typed scalar 四位置均优先返回 `yaml.frontend.limit.scalar-per-node`；
11. 严格 UTF-8、portable name、对象/tag、anchor/alias、图、映射和资源合同未回退；
12. YAML 59/59、XML 30/30、Compiler 83/83、Context 正常测试 26/26、Demo 4/4、legacy declaration 1/1 通过；
13. 故意失败门禁 1 项按预期失败并被门禁识别；
14. 12 模块 Reactor、Java release 8 通过；
15. MySQL 为 `SKIPPED_NOT_APPLICABLE`；
16. Specification、Architecture、Security、Code、TDD 五类独立 Review 全部 PASSED；
17. 开放 P0/P1/P2 为 0；
18. `@Override` 独占一行，方法、构造器和重要逻辑使用中文注释；
19. 未修改 Context 生产代码、compiler canonical 公共 API或 XML Frontend；
20. 未启动 T06；
21. PR #20 未经明确授权不得合并。

## Revision Lock

- Design first commit：`ab9ca21cf668aba03f030129022458bbd46304fc`；
- Design blob：`b8ffb41226866b0854def9d4ce12a6c68c150b3b`；
- Plan first commit：`a2283a8661210e0ebda26a67fad05a60d770a89b`；
- Plan blob：`26adb13c7192e5f7419c59acf445bf8b56b6ceb7`；
- clean-code Head `30529276cd8f...` 复核完全一致；
- R22/R18 创建后未修改；
- R21/R17/R02 未被 I003 修改。

## Evidence 入口

- Revision Lock：`../evidence/revision-lock-p1-t05-r03.json`；
- Design/Plan：`../evidence/design-plan-p1-t05-r03.md`；
- RED：`../evidence/tdd-red-p1-t05-r03.md`；
- Skeleton：`../evidence/architecture-skeleton-p1-t05-r03.md`；
- Development：`../evidence/development-p1-t05-r03.md`；
- Reviews：`../review/review-p1-t05-r03.md`；
- Testing：`../evidence/testing-p1-t05-r03.md`；
- Completion：`../evidence/commands/completion-p1-t05-r03/completion-report.json`；
- 机器恢复：`../../../tdd_p1_t05_r03_completion.json`。

## 历史保护

I001 与 I002 的 Design、Plan、TDD、Skeleton、Development、Review、Testing、Completion、Evidence 和机器 checkpoint 均未覆盖或删除。R21/R17 保持其既有历史内容，R22/R18 是 I003 唯一有效 Design / Plan 输入。
