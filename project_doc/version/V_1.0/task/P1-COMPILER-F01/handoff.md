# P1-COMPILER-F01 阶段交接

> `TASK-P1-T01` 已通过 PR #16 合并，`TASK-P1-T02` 已通过 PR #17 合并。`TASK-P1-T03 / I001` 的 Completion R01 被独立 Review `REV-000152` 推翻并作为历史保留；最新有效 iteration 为 `I002`，Completion 为 `COMPLETION-P1-T03-R02@6af43b47f044`。

## 已合并前置任务

- T01 Completion：`COMPLETION-P1-T01-R04@ee99223a243f`，merge `f88f45731e16868bfacb489b63e3086aae49d018`。
- T02 Completion：`COMPLETION-P1-T02-R05@35376308b013`，merge / T03 base `370b72f4bf4ec9b3620586f26d13d95f611f3cc9`。

## T03 历史与当前 Revision

### I001（历史，已失效）

- Completion：`COMPLETION-P1-T03-R01@713848bfa65e`；
- 最终文档 Head：`335cc7ae2843145ae891a22892a169e74ac5d6fc`；
- 独立 Review 发现 canonical reference、cycle identity 和声明起始列缺口；
- R01 不修改、不删除，状态为 `STALE_BY_REVIEW`。

### I002（当前有效）

- Design：`DESIGN-R14@P1-T03-REWORK-I002`；
- Plan：`TP-P1-COMPILER-F01-R10@P1-T03-REWORK-I002`；
- TDD：`TDD-P1-T03-R02@15e7144d489a`；
- Architecture Skeleton：`DEVSKEL-P1-T03-R02@a9f2ceaa4d19`；
- Development：`DEV-P1-T03-R02@6af43b47f044`；
- Code Review：`CODEREVIEW-P1-T03-R02@6af43b47f044`；
- Testing：`TESTING-P1-T03-R02@6af43b47f044`；
- Completion：`COMPLETION-P1-T03-R02@6af43b47f044`；
- Review：`REV-000152`～`REV-000162`；
- Evidence：`EVD-000393`～`EVD-000403`；
- Clean-code Head：`6af43b47f0446f3dc4980f5877a58275aaf17448`；
- P0 Run：`30738516967`；
- Artifact：`8830460790`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：68 run / 0 failures / 0 errors / 0 skipped；
- I002 专项：6 run / 0 failures / 0 errors / 0 skipped；
- 12 模块 Reactor、Java 8、故意失败阻断：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1：无。

## T03 当前冻结合同

### SourceGraph

- 固定根入口 `classpath:mix/orm-config.xml`；
- 精确 10 个唯一 Source、7 条真实声明边；
- 目录展开项不伪造边；
- 文件集正序、逆序、随机顺序产生相同图；
- 主资源和测试镜像的固定字节及图一致。

### Canonical reference 与身份域

- `SourceReference.value()` 是 Provider、边目标、重复键、排序、图相等性和 cycle stack 共用的 canonical key；
- opaque `classpath:` 和 hierarchical/relative 路径只消除独立 `.` 段并统一 scheme 大小写；
- `..`、编码 traversal、query、fragment 和非法 URI 文本不被隐藏，继续交给 SourcePolicy 拒绝；
- sourceId 独立用于 Manifest 唯一性和 Diagnostic 身份，不要求与 URI 相等；
- `sourceId != URI` 的环路在递归 Provider 调用前阻断，返回 `MIX-SOURCE-POLICY`。

### 声明位置与安全

- StAX 继续负责安全 tokenization，DTD、外部实体和外部资源关闭；
- 基于原始 UTF-8 文本定位声明 start tag `<`；
- 真实 root/systems fixture 的 7 条边均精确验证 line、column、nodePath；
- 失败不暴露部分 SourceGraph。

## 编码和范围

- `@Override` 独占一行；
- 方法、构造器和关键逻辑使用中文注释；
- Java release 8；
- 未修改 `dec-core-context` 生产代码；
- 未实现 T04 Canonical Frontend、RawDefinitionSet、Symbol 或 Compiler Pipeline。

## PR 状态与下一步

- 当前 PR：`#18`，目标 `dev_all`；
- 当前分支：`feature/p1-t03-source-graph-20260802-1430`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t03_r02_completion.json`；
- 未经明确授权不得合并 PR #18；
- PR #18 合并前 `TASK-P1-T04` 保持未启动和阻断。
