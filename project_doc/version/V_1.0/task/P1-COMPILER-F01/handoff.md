# P1-COMPILER-F01 阶段交接

> T01～T04 已合并到 `dev_all`。T04 当前有效 Completion 为 `COMPLETION-P1-T04-R02@0699c6bc2ed4`，merge / T05 base 为 `09edf814bdf0800e7e9633545ca743200169b377`。T05 I001、I002 Completion 已被后续独立 Review 推翻并作为不可变历史保留；当前有效任务为 `TASK-P1-T05 / I003`。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`，merge `f88f45731e16868bfacb489b63e3086aae49d018`；
- T02：`COMPLETION-P1-T02-R05@35376308b013`，merge `370b72f4bf4ec9b3620586f26d13d95f611f3cc9`；
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`，merge `df5e8c057d9aa8e3e477c54325bc476e7fdc5bee`；
- T04：`COMPLETION-P1-T04-R02@0699c6bc2ed4`，merge `09edf814bdf0800e7e9633545ca743200169b377`。

## T05 历史 Revision

- I001 Completion：`COMPLETION-P1-T05-R01@040f09b80463`，被 `REV-000231` 推翻；
- I002 Completion：`COMPLETION-P1-T05-R02@27d566714f5c`，被 `REV-000244` 推翻；
- R20/R21、R16/R17、R01/R02 及全部 Review/Evidence 均保留，不能作为当前 Completion 或 T06 前置输入。

## T05 I003（当前有效）

- Design：`DESIGN-R22@P1-T05-REWORK-I003`；
- Plan：`TP-P1-COMPILER-F01-R18@P1-T05-REWORK-I003`；
- TDD：`TDD-P1-T05-R03@3deacf0aa036`；
- Architecture Skeleton：`DEVSKEL-P1-T05-R03@05873e286c2d`；
- Development：`DEV-P1-T05-R03@30529276cd8f`；
- Code Review：`CODEREVIEW-P1-T05-R03@30529276cd8f`；
- Testing：`TESTING-P1-T05-R03@30529276cd8f`；
- Completion：`COMPLETION-P1-T05-R03@30529276cd8f`；
- Review：`REV-000244`～`REV-000256`；
- Evidence：`EVD-000487`～`EVD-000498`；
- Clean-code Head：`30529276cd8fa35e0eeeafb1256b85cb99820afb`；
- P0 Run：`30756293074`；
- Artifact：`8836020099`；
- Artifact SHA-256：`3362ee5de19129f0a819bb1587e42552077618f7bf43b3011e15540ec0bcd688`；
- YAML 59/59；XML 30/30；Compiler 83/83；Context 正常测试 26/26；Demo 4/4；legacy declaration 1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1/P2：无。

## Revision Integrity

- R22 first commit：`ab9ca21cf668aba03f030129022458bbd46304fc`；
- R22 blob：`b8ffb41226866b0854def9d4ce12a6c68c150b3b`；
- R18 first commit：`a2283a8661210e0ebda26a67fad05a60d770a89b`；
- R18 blob：`26adb13c7192e5f7419c59acf445bf8b56b6ceb7`；
- R22/R18 在 I003 RED 前创建，clean-code Head复核 blob 不变；
- I003 未修改 R21/R17/R02 历史。

## I003 Scalar 合同

- 原始 scalar 值只读取一次；
- 未经 trim 的单值长度门禁先于任何 Resolver regex、timestamp 语义或数值派生；
- 单值预算与最终 Canonical 累计预算分离；
- 普通 scalar、`#text`、属性 value 和 Sequence item共享同一入口；
- bool/int/float/null/timestamp 直接使用 SnakeYAML 2.2 `Resolver` 公开 Pattern；
- 不复制数字接受正则，不构造 `BigDecimal` / `BigInteger`；
- timestamp 在官方 Pattern 后校验真实日期、时间和时区范围；
- `1e3`、`1.2e3`、显式 `!!float 1e3` 四位置通过；
- 显式 `!!int 0b_`、`0x_`、`0_` 四位置失败；
- 超限合法和非法 typed scalar均优先返回 `yaml.frontend.limit.scalar-per-node`；
- 失败保持 `FAILED / MIX_FRONTEND_YAML_UNSAFE / empty root`。

## 保持的安全与 Canonical 合同

- 原始 byte[] 严格 UTF-8 decoder，malformed/unmappable 均 REPORT；
- 根、子节点和属性名使用 `[A-Za-z_][A-Za-z0-9._-]*`；
- nodePath 无路径分隔符或换行歧义；
- `SafeConstructor + composeAll`，不调用 `load` / `loadAs`；
- Java/object/local/custom、binary/set/omap/pairs tag拒绝；
- anchor、alias、共享/递归图、merge、duplicate/complex key拒绝；
- 单 Mapping root、`@attributes`、`#text`、Sequence 重复子节点映射不变；
- 属性排序、子节点顺序、schemaVersion 和一基 SourceRef不变；
- R20 生产预算值不变，累计计数继续溢出安全；
- 不捕获 `OutOfMemoryError`，不使用真实 OOM 或耗时阈值测试。

## 架构、PR 与下一步

- YAML 模块单向依赖 compiler canonical API；
- 未修改 `dec-core-context` 生产代码、compiler canonical 公共 API或 XML Frontend；
- 当前 PR：`#20`，分支 `feature/p1-t05-yaml-canonical-20260802-2106`，目标 `dev_all`；
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t05-r03/completion-report.json`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t05_r03_completion.json`；
- `@Override` 独占一行，方法、构造器及关键逻辑使用中文注释；
- 未经明确授权不得合并 PR #20；
- PR #20 合并前 `TASK-P1-T06` 保持未启动和阻断。
