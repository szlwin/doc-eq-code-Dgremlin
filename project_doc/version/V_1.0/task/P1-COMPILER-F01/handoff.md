# P1-COMPILER-F01 阶段交接

> T01、T02、T03 已合并到 `dev_all`。T03 当前有效 Completion 为 `COMPLETION-P1-T03-R05@91271c9a1c20`，T04 基线为 `dev_all@df5e8c057d9aa8e3e477c54325bc476e7fdc5bee`。T04 I001 的 Completion R01 已被 `REV-000207` 推翻并作为不可变历史保留；当前有效 iteration 为 `TASK-P1-T04 / I002`。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`，merge `f88f45731e16868bfacb489b63e3086aae49d018`；
- T02：`COMPLETION-P1-T02-R05@35376308b013`，merge `370b72f4bf4ec9b3620586f26d13d95f611f3cc9`；
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`，merge `df5e8c057d9aa8e3e477c54325bc476e7fdc5bee`。

## T04 历史 Revision

- I001：`COMPLETION-P1-T04-R01@ba472906c719`；
- 推翻 Review：`REV-000207`；
- 状态：不可变历史，不能作为当前 Completion 或 T05 前置输入。

## T04 I002（当前有效）

- Design：`DESIGN-R19@P1-T04-REWORK-I002`；
- Plan：`TP-P1-COMPILER-F01-R15@P1-T04-REWORK-I002`；
- TDD：`TDD-P1-T04-R02@e2033f2b249e`；
- Architecture Skeleton：`DEVSKEL-P1-T04-R02@710d114248d0`；
- Development：`DEV-P1-T04-R02@0699c6bc2ed4`；
- Code Review：`CODEREVIEW-P1-T04-R02@0699c6bc2ed4`；
- Testing：`TESTING-P1-T04-R02@0699c6bc2ed4`；
- Completion：`COMPLETION-P1-T04-R02@0699c6bc2ed4`；
- Review：`REV-000207`～`REV-000219`；
- Evidence：`EVD-000451`～`EVD-000463`；
- Clean-code Head：`0699c6bc2ed41100c3a4538b76a691b7757f683b`；
- P0 Run：`30748395446`；
- Artifact：`8833627854`；
- Artifact SHA-256：`a7a7703c706e8bb3cadafb74366e13131ea63a37dd3bbf7f9446b3608ed7c97a`；
- Context：26/26；Compiler：83/83；XML T04：30/30；Demo：4/4；
- XML 资源预算专项：12/12；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1：无。

## 当前 XML 资源安全合同

生产 `XmlFrontendLimits` 冻结：

- `maxDocumentBytes = 1,048,576`；
- `maxElementDepth = 256`；
- `maxNodeCount = 65,536`；
- `maxCumulativeNodePathChars = 4,194,304`；
- `maxAttributesPerElement = 256`；
- `maxDirectTextCharsPerElement = 262,144`；
- `maxCumulativeDirectTextChars = 1,048,576`。

执行边界：

- 文档字节在 reader 创建前检查；
- START_ELEMENT 的深度、节点、属性和路径预算在 SourceRef、Map、NodeBuilder 分配前检查；
- nodePath 只基于父路径进行一次拼接，不再遍历祖先栈；
- 文本预算在 StringBuilder 追加前检查；
- 计数溢出按预算失败；最大深度同时约束后续递归值操作；
- 超限统一返回 `FAILED`、`MIX_FRONTEND_XML_UNSAFE`、空 root、外部访问 0；
- 不捕获 `OutOfMemoryError`，不使用真实 OOM 测试。

## Canonical 与 XML 安全合同

- 元素和属性使用 local-name，属性稳定排序，子节点保持文档顺序；
- 普通文本与 CDATA 按文档顺序拼接并 trim；
- schemaVersion 传递给根、子节点和孙节点；
- SourceRef 指向 start tag `<`，nodePath 为完整 local-name 路径，LF/CRLF/CR 已验证；
- null source、null options、错误格式和 malformed XML 稳定失败且无部分 root；
- DOCTYPE、通用实体、参数实体和外部 schema 均拒绝；
- `xsi:schemaLocation` 与 `xsi:noNamespaceSchemaLocation` 立即失败；
- 网络、文件和 schema 外部访问为 0，XInclude 只作为数据保留。

## 架构、PR 与下一步

- XML 模块单向依赖 compiler canonical API；未修改 compiler canonical 公共 API；
- 未修改 `dec-core-context` 生产代码；
- 未实现 YAML Frontend、RawDefinitionSet、Symbol 或 Pipeline；
- 当前 PR：`#19`，分支 `feature/p1-t04-xml-canonical-20260802-1744`，目标 `dev_all`；
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t04-r02/completion-report.json`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t04_r02_completion.json`；
- 所有 `@Override` 独占一行，方法、构造器及关键逻辑使用中文注释；
- 未经明确授权不得合并 PR #19；
- PR #19 合并前 `TASK-P1-T05` 保持未启动和阻断。
