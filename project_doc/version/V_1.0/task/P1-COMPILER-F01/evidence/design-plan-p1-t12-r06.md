# TASK-P1-T12 / R06 Design and Plan Evidence

- Evidence：`EVD-000967`～`EVD-000970`
- Iteration：`P1-T12-I006`
- Design：`DESIGN-R43@P1-T12-REWORK-I006`
- Plan：`TP-P1-COMPILER-F01-R39@P1-T12-REWORK-I006`
- Base：`PR27@956e51b998068b726eefc4ccfbafe12f868ca72b`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`

## Revision integrity

- R43 first commit：`32c905ed33a43e23db88f4704485d51f346530a1`
- R43 frozen blob：`03f2b05814bdb145ef77c570001c43aa3d23d300`
- R39 first commit：`d3b5718435a379c96019b0283a4de7127e7e28f4`
- R39 frozen blob：`6de1787c65bd286e5b95ef080db09e32cd93b869`
- Valid RED Head：`788f475d60e4864fc6c11bfffee3ff925aa757ac`

R43/R39 均在 I006 测试文件和有效 RED Head 之前提交；截至 clean-code Head 内容 blob 未变化。

## Frozen decisions

- 单次公开查询只创建一个 `ComparisonOperation`；
- pair 状态为 `VISITING/EQUAL/NOT_EQUAL`，完成状态跨候选复用；
- List equality 与 List/Set/Map canonicalization 使用 iterator continuation；
- 不读取外部 List/entrySet size，不使用外部 Collection 整体复制构造器；
- 每次外部元素读取、metadata 保存和子任务调度前先执行 edge/node 预算；
- iterator 业务异常原样传播；资源超限稳定抛 `ComparisonLimitException`；
- Java 8、`@Override` 独占一行、方法和重要逻辑使用中文注释。
