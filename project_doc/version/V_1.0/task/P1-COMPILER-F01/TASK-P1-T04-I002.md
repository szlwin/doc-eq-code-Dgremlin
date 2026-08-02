# TASK-P1-T04 / I002 — XML 资源预算 Rework

- 状态：`IN_PROGRESS`
- Rework base：`7edf31fca334cdd7e6342ed31b80e40d5bacb68d`
- Branch：`feature/p1-t04-xml-canonical-20260802-1744`
- PR：`#19`（Draft）
- Dependency：`COMPLETION-P1-T03-R05@91271c9a1c20`
- Independent Review：`REV-000207`，`NEEDS_CHANGES / REWORK`
- 历史 Completion：`COMPLETION-P1-T04-R01@ba472906c719`（被推翻，保留）
- Design：`DESIGN-R19@P1-T04-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R15@P1-T04-REWORK-I002`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 开放 Findings

1. `[P1][BLOCKER]` 深层 XML 缺少深度、节点数和累计 nodePath 预算，可产生二次方路径内存放大；
2. `[P2]` R14 与 R18 的 schemaLocation 规则冲突；
3. `[P2]` CDATA 拼接、后代 schemaVersion 和 null 输入缺少直接 Oracle。

## 完成门禁

- 冻结并实现 R19 全部生产预算；
- 所有预算在危险分配前检查；
- 超限统一返回 FAILED、`MIX_FRONTEND_XML_UNSAFE`、空 root、外部 I/O 0；
- nodePath 不再遍历祖先栈，行定位不再线性重复扫描；
- R15 明确 schemaLocation 立即失败；
- 补齐 CDATA 顺序、全树 schemaVersion、null 输入 Oracle；
- 有效 RED、Skeleton、GREEN、五类独立 Review、Testing、Completion R02 全部完成；
- Context、Compiler、XML、12 模块 Reactor、Java 8 和失败门禁通过；
- 开放 P0/P1 为 0；
- 所有 `@Override` 独占一行，方法、构造器和关键逻辑使用中文注释；
- T05 保持未启动；未经明确授权不得合并 PR #19。
