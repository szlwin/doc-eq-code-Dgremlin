# P1-COMPILER-F01 阶段交接

> T01～T04 已合并到 `dev_all`。T04 I001 的 Completion R01 被 `REV-000207` 推翻并作为不可变历史保留；T04 当前有效 Completion 为 `COMPLETION-P1-T04-R02@0699c6bc2ed4`，merge / T05 base 为 `09edf814bdf0800e7e9633545ca743200169b377`。当前有效任务为 `TASK-P1-T05 / I001`。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`，merge `f88f45731e16868bfacb489b63e3086aae49d018`；
- T02：`COMPLETION-P1-T02-R05@35376308b013`，merge `370b72f4bf4ec9b3620586f26d13d95f611f3cc9`；
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`，merge `df5e8c057d9aa8e3e477c54325bc476e7fdc5bee`；
- T04：`COMPLETION-P1-T04-R02@0699c6bc2ed4`，merge `09edf814bdf0800e7e9633545ca743200169b377`。

## T05 I001（当前有效）

- Design：`DESIGN-R20@P1-T05-I001`；
- Plan：`TP-P1-COMPILER-F01-R16@P1-T05-I001`；
- TDD：`TDD-P1-T05-R01@859c7aacae91`；
- Architecture Skeleton：`DEVSKEL-P1-T05-R01@b597d5fa0e33`；
- Development：`DEV-P1-T05-R01@040f09b80463`；
- Code Review：`CODEREVIEW-P1-T05-R01@040f09b80463`；
- Testing：`TESTING-P1-T05-R01@040f09b80463`；
- Completion：`COMPLETION-P1-T05-R01@040f09b80463`；
- Review：`REV-000220`～`REV-000230`；
- Evidence：`EVD-000464`～`EVD-000474`；
- Clean-code Head：`040f09b80463911c092e7693f47814f3904758fd`；
- P0 Run：`30750632160`；
- Artifact：`8834325522`；
- Artifact SHA-256：`dc5bb0b3c4d1505f7f418c34042eb0071e1c770fc5cda489476cc76e91eb576c`；
- Context 26/26；Compiler 83/83；XML 30/30；YAML 35/35；Demo 4/4；legacy declaration 1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1：无。

## YAML Canonical 合同

- 实现：`dec.core.compiler.canonical.yaml.SafeYamlDocumentFrontend`；
- 只使用安全 compose 表示树，不执行任意 Java/object 构造；
- document root 为单 key Mapping；`@attributes` 表示属性，`#text` 表示直接 scalar，Sequence 表示同名重复子节点；
- 属性稳定排序，子节点保持文档顺序；标准 scalar 保留词法值，null 不发布 scalar；
- schemaVersion 传播到所有节点；
- SourceRef 使用 YAML key/item 的一基 Mark 和完整 nodePath；
- 同语义 XML/YAML 语义树已直接比较，格式和物理来源各自保留。

## YAML 安全与资源合同

- Java/object/local/custom、binary/set/omap/pairs tag 均拒绝；
- anchor、alias、共享/递归图、merge、duplicate/complex key均拒绝；
- null source/options、错误格式、空/多 document、错误 root 和 malformed YAML 稳定失败；
- 失败为 `FAILED / MIX_FRONTEND_YAML_UNSAFE / empty root`；
- 生产预算：文档 1,048,576 bytes；code point 1,048,576；深度 128；节点 65,536；累计路径 4,194,304；Mapping 256；Sequence 4,096；单 scalar 262,144；累计 scalar 1,048,576；alias 0；
- 文档预算在 parser 前检查；parser 限制在 compose 阶段启用；Canonical 节点/路径/集合/scalar 预算在发布前检查；
- 累计计数使用溢出安全 long；不捕获 `OutOfMemoryError`，不使用真实 OOM 测试。

## 架构、PR 与下一步

- YAML 模块单向依赖 compiler canonical API；compiler 无 YAML 反向依赖；XML 仅为 parity test dependency；
- 未修改 `dec-core-context` 生产代码、compiler canonical 公共 API和 XML Frontend 生产语义；
- 当前 PR：`#20`，分支 `feature/p1-t05-yaml-canonical-20260802-2106`，目标 `dev_all`；
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t05-r01/completion-report.json`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t05_r01_completion.json`；
- `@Override` 独占一行，方法、构造器及关键逻辑使用中文注释；
- 未经明确授权不得合并 PR #20；
- PR #20 合并前 `TASK-P1-T06` 保持未启动和阻断。
