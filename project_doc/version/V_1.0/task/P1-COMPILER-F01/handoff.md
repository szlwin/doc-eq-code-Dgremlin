# P1-COMPILER-F01 阶段交接

> T01、T02、T03 已合并到 `dev_all`。T03 的 R01～R04 Completion 均被后续独立 Review 推翻并作为不可变历史保留；T03 当前有效 Completion 为 `COMPLETION-P1-T03-R05@91271c9a1c20`，merge / T04 base 为 `df5e8c057d9aa8e3e477c54325bc476e7fdc5bee`。T04 当前有效 iteration 为 `TASK-P1-T04 / I001`。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`，merge `f88f45731e16868bfacb489b63e3086aae49d018`；
- T02：`COMPLETION-P1-T02-R05@35376308b013`，merge `370b72f4bf4ec9b3620586f26d13d95f611f3cc9`；
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`，merge `df5e8c057d9aa8e3e477c54325bc476e7fdc5bee`。

## T04 I001（当前有效）

- Design：`DESIGN-R18@P1-T04-I001`；
- Plan：`TP-P1-COMPILER-F01-R14@P1-T04-I001`；
- TDD：`TDD-P1-T04-R01@1b39f27b972e`；
- Architecture Skeleton：`DEVSKEL-P1-T04-R01@70df083e1b8a`；
- Development：`DEV-P1-T04-R01@ba472906c719`；
- Code Review：`CODEREVIEW-P1-T04-R01@ba472906c719`；
- Testing：`TESTING-P1-T04-R01@ba472906c719`；
- Completion：`COMPLETION-P1-T04-R01@ba472906c719`；
- Review：`REV-000196`～`REV-000206`；
- Evidence：`EVD-000439`～`EVD-000450`；
- Clean-code Head：`ba472906c719985b21cb6cbed70df5360a59fadc`；
- P0 Run：`30743067868`；
- Artifact：`8831948275`；
- Artifact SHA-256：`0824424e712ff64af63736d0b6be0bddf5f2e372ca26bf8633ebb6f822d6eee3`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：83 run / 0 failures / 0 errors / 0 skipped；
- XML T04：15 run / 0 failures / 0 errors / 0 skipped；
- 12 模块 Reactor、Java release 8、故意失败阻断：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1：无。

## T04 冻结合同

### Canonical Frontend

- 实现类型：`dec.core.compiler.canonical.xml.SecureXmlDocumentFrontend`；
- 通过 compiler-owned `DocumentFrontend`、`FrontendOptions`、`FrontendResult` 和 `CanonicalDocumentNode` API 工作；
- 元素和属性使用 local-name，命名空间前缀不进入 Canonical 名称；
- 属性按 key 稳定排序，子节点保持文档顺序；
- 直接文本与 CDATA 形成可选标量，纯空白不发布标量；
- schemaVersion 来自显式 FrontendOptions 并传递到所有节点；
- 每个节点 SourceRef 指向 start tag 的 `<`，nodePath 为完整 local-name 路径；
- LF、CRLF、CR 均已验证；
- 完整根生成前不发布任何部分 Canonical。

### XML 安全边界

- DOCTYPE、内部实体、外部通用实体和外部参数实体均被拒绝；
- 网络、根外文件和外部 schema 访问次数均为 0；
- `xsi:schemaLocation` 与 `xsi:noNamespaceSchemaLocation` 直接失败，不创建 SchemaFactory；
- XInclude 仅作为普通 Canonical 数据保留，不执行包含；
- 关键 StAX 安全属性不受支持时 fail closed；
- Malformed XML、错误 DocumentFormat 和重复 Canonical 属性 local-name 返回 `MIX_FRONTEND_XML_UNSAFE`，不携带部分根。

### 架构和范围

- XML 模块单向依赖 compiler canonical API；compiler 不反向依赖 XML 模块；
- Frontend 不持有 DOM、DOM4J、ConfigFactory、ConfigInfo、Registry 或 EngineContext；
- 旧 XML API 和下游回归保持；
- 未修改 `dec-core-context` 生产代码；
- 未实现 T05 YAML Frontend、RawDefinitionSet、Symbol 或 Compiler Pipeline。

## 编码、PR 与下一步

- `@Override` 独占一行；公共方法、构造器和关键安全、定位、状态转换逻辑使用中文注释；
- 当前 PR：`#19`，分支 `feature/p1-t04-xml-canonical-20260802-1744`，目标 `dev_all`；
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t04-r01/completion-report.json`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t04_r01_completion.json`；
- 未经明确授权不得合并 PR #19；
- PR #19 合并前 `TASK-P1-T05` 保持未启动和阻断。
