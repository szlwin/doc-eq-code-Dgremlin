# TASK-P1-T04 / I002 — XML 资源预算 Rework

- 状态：`COMPLETED`
- Rework base：`7edf31fca334cdd7e6342ed31b80e40d5bacb68d`
- Branch：`feature/p1-t04-xml-canonical-20260802-1744`
- PR：`#19`（完成后恢复 Ready for review）
- Dependency：`COMPLETION-P1-T03-R05@91271c9a1c20`
- Independent Review：`REV-000207`，`NEEDS_CHANGES / REWORK`
- 历史 Completion：`COMPLETION-P1-T04-R01@ba472906c719`（被推翻并保留）
- Design：`DESIGN-R19@P1-T04-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R15@P1-T04-REWORK-I002`
- TDD：`TDD-P1-T04-R02@e2033f2b249e`
- Architecture Skeleton：`DEVSKEL-P1-T04-R02@710d114248d0`
- Development：`DEV-P1-T04-R02@0699c6bc2ed4`
- Code Review：`CODEREVIEW-P1-T04-R02@0699c6bc2ed4`
- Testing：`TESTING-P1-T04-R02@0699c6bc2ed4`
- Completion：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- Review：`REV-000207`～`REV-000219`
- Evidence：`EVD-000451`～`EVD-000463`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Finding 关闭结果

1. `[P1]` 深层 XML 资源耗尽：已关闭。生产 Frontend 冻结文档字节、元素深度、节点数、累计 nodePath、属性数、单节点直接文本和累计直接文本预算；
2. `[P2]` Design/Plan schemaLocation 冲突：已关闭。R15 明确 `xsi:schemaLocation` 与 `xsi:noNamespaceSchemaLocation` 必须立即失败；R14 保留为历史；
3. `[P2]` Canonical Oracle 缺口：已关闭。文本—CDATA—文本顺序、后代 schemaVersion、null source 和 null options 均有直接测试。

## 完成事实

- 文档字节在 reader 创建前检查；
- 深度、节点数、属性数和累计路径在 SourceRef、属性 Map、NodeBuilder 创建前检查；
- nodePath 只基于父路径单次拼接，不再遍历全部祖先；
- 文本预算在 StringBuilder 追加前检查；
- long 计数溢出按预算失败处理；
- start tag 每个元素只定位一次，行索引使用二分查找；
- 任一预算失败均返回 `FAILED`、`MIX_FRONTEND_XML_UNSAFE`、空 Canonical root、外部访问 0；
- 未捕获 `OutOfMemoryError`，未构造真实 OOM 测试；
- 生产预算精确值和非正配置均有直接 Oracle；
- Clean-code Head：`0699c6bc2ed41100c3a4538b76a691b7757f683b`；
- P0 Run：`30748395446`；
- Artifact：`8833627854`；
- Artifact SHA-256：`a7a7703c706e8bb3cadafb74366e13131ea63a37dd3bbf7f9446b3608ed7c97a`；
- Context 26/26、Compiler 83/83、XML T04 30/30、Demo 4/4 通过；
- XML 资源预算专项 12/12 通过；
- 12 模块 Reactor、Java release 8 和故意失败门禁通过；
- MySQL 为 `SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1 为 0；
- 未修改 `dec-core-context` 生产代码或 compiler canonical 公共 API；
- 未启动 YAML Frontend、RawDefinitionSet、Symbol、Pipeline 或 TASK-P1-T05；
- 所有 `@Override` 独占一行，公共方法、构造器和关键安全逻辑使用中文注释；
- 未经明确授权不得合并 PR #19。
