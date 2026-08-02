# P1-COMPILER-F01 阶段交接

> T01～T04 已合并到 `dev_all`。T04 当前有效 Completion 为 `COMPLETION-P1-T04-R02@0699c6bc2ed4`，merge / T05 base 为 `09edf814bdf0800e7e9633545ca743200169b377`。T05 I001 Completion R01 已被 `REV-000231` 推翻并作为不可变历史保留；当前有效任务为 `TASK-P1-T05 / I002`。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`，merge `f88f45731e16868bfacb489b63e3086aae49d018`；
- T02：`COMPLETION-P1-T02-R05@35376308b013`，merge `370b72f4bf4ec9b3620586f26d13d95f611f3cc9`；
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`，merge `df5e8c057d9aa8e3e477c54325bc476e7fdc5bee`；
- T04：`COMPLETION-P1-T04-R02@0699c6bc2ed4`，merge `09edf814bdf0800e7e9633545ca743200169b377`。

## T05 历史 Revision

- I001 Completion：`COMPLETION-P1-T05-R01@040f09b80463`；
- 推翻 Review：`REV-000231`；
- 状态：不可变历史，不能作为当前 Completion 或 T06 前置输入。

## T05 I002（当前有效）

- Design：`DESIGN-R21@P1-T05-REWORK-I002`；
- Plan：`TP-P1-COMPILER-F01-R17@P1-T05-REWORK-I002`；
- TDD：`TDD-P1-T05-R02@c362011eac56`；
- Architecture Skeleton：`DEVSKEL-P1-T05-R02@122f8ddc37df`；
- Development：`DEV-P1-T05-R02@27d566714f5c`；
- Code Review：`CODEREVIEW-P1-T05-R02@27d566714f5c`；
- Testing：`TESTING-P1-T05-R02@27d566714f5c`；
- Completion：`COMPLETION-P1-T05-R02@27d566714f5c`；
- Review：`REV-000231`～`REV-000243`；
- Evidence：`EVD-000475`～`EVD-000486`；
- Clean-code Head：`27d566714f5c4e521a969b92d4642111971bb96e`；
- Development P0 Run：`30752686888`；
- Artifact：`8834954051`；
- Artifact SHA-256：`44ca69b67e75e46278f8b622fe864293e7251154456f1809d75d97a44e7f0090`；
- Context 26/26；Compiler 83/83；XML 30/30；YAML 45/45；Demo 4/4；legacy declaration 1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1/P2：无。

## I002 来源事实合同

- 原始 byte[] 使用严格 UTF-8 `CharsetDecoder`；malformed/unmappable 均 REPORT；
- 非法 continuation、截断、overlong 和 UTF-8 surrogate 均在 parser 前失败；
- 标准 str/bool/int/float/null/timestamp 同时执行 tag 白名单与冻结词法校验；
- 合法显式和隐式 typed scalar 保留原始词法，不执行 Java 业务对象 construction；
- `!!null attacker-data`、非法 int/bool/float/timestamp、前导零十进制、孤立小数点、非法日期/时间/时区均失败；
- 普通 scalar、`#text`、属性 value 和 Sequence item 使用相同门禁；
- 根、子节点和属性名使用 `[A-Za-z_][A-Za-z0-9._-]*`；
- nodePath 不接受 `/`、换行、冒号、空白或非法首字符 segment；
- 失败保持 `FAILED / MIX_FRONTEND_YAML_UNSAFE / empty root`。

## 保持的 R20 合同

- `SafeConstructor + composeAll`，不调用 `load` / `loadAs`；
- Java/object/local/custom、binary/set/omap/pairs tag 拒绝；
- anchor、alias、共享/递归图、merge、duplicate/complex key拒绝；
- 单 Mapping root、`@attributes`、`#text`、Sequence 重复子节点映射不变；
- 属性稳定排序、子节点文档顺序、schemaVersion 全树传播和一基 SourceRef 不变；
- 生产预算值及检查顺序不变；累计计数使用溢出安全 long；
- 不捕获 `OutOfMemoryError`，不使用真实 OOM 测试。

## 架构、PR 与下一步

- YAML 模块单向依赖 compiler canonical API；compiler 无 YAML 反向依赖；
- 未修改 `dec-core-context` 生产代码、compiler canonical 公共 API和 XML Frontend 生产语义；
- 当前 PR：`#20`，分支 `feature/p1-t05-yaml-canonical-20260802-2106`，目标 `dev_all`；
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t05-r02/completion-report.json`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t05_r02_completion.json`；
- `@Override` 独占一行，方法、构造器及关键逻辑使用中文注释；
- 未经明确授权不得合并 PR #20；
- PR #20 合并前 `TASK-P1-T06` 保持未启动和阻断。
