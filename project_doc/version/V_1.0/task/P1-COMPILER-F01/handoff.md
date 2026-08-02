# P1-COMPILER-F01 阶段交接

> `TASK-P1-T01` 已通过 PR #16 合并，`TASK-P1-T02` 已通过 PR #17 合并。T03 Completion R01～R04 均被后续独立 Review 推翻并作为不可变历史保留；最新有效 iteration 为 `TASK-P1-T03 / I005`，Completion 为 `COMPLETION-P1-T03-R05@91271c9a1c20`。

## 已合并前置任务

- T01 Completion：`COMPLETION-P1-T01-R04@ee99223a243f`，merge `f88f45731e16868bfacb489b63e3086aae49d018`。
- T02 Completion：`COMPLETION-P1-T02-R05@35376308b013`，merge / T03 base `370b72f4bf4ec9b3620586f26d13d95f611f3cc9`。

## T03 历史 Revision

- I001 / R01：被 `REV-000152` 推翻，历史保留；
- I002 / R02：被 `REV-000163` 推翻，历史保留；
- I003 / R03：被 `REV-000174` 推翻，历史保留；
- I004 / R04：被 `REV-000185` 的完整 XML 声明路径 P1 Finding 推翻，历史保留。

## T03 I005（当前有效）

- Design：`DESIGN-R17@P1-T03-REWORK-I005`；
- Plan：`TP-P1-COMPILER-F01-R13@P1-T03-REWORK-I005`；
- TDD：`TDD-P1-T03-R05@06bc2a0c0ebd`；
- Architecture Skeleton：`DEVSKEL-P1-T03-R05@1d49bb2f1fa3`；
- Development：`DEV-P1-T03-R05@91271c9a1c20`；
- Code Review：`CODEREVIEW-P1-T03-R05@91271c9a1c20`；
- Testing：`TESTING-P1-T03-R05@91271c9a1c20`；
- Completion：`COMPLETION-P1-T03-R05@91271c9a1c20`；
- Review：`REV-000185`～`REV-000195`；
- Evidence：`EVD-000428`～`EVD-000438`；
- Clean-code Head：`91271c9a1c2083c2843b7c2e69bb3570f9155d55`；
- P0 Run：`30741699603`；
- Artifact：`8831504648`；
- Artifact SHA-256：`de9e07229c82374a5eb36cc4a5ca4b2c5df0f18e53de582a13b456cd1bc206f7`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：83 run / 0 failures / 0 errors / 0 skipped；
- I005 专项：5 run / 0 failures / 0 errors / 0 skipped；
- 12 模块 Reactor、Java 8、故意失败阻断：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1：无。

## T03 当前冻结合同

### SourceGraph

- 固定根入口 `classpath:mix/orm-config.xml`；
- 精确 10 个唯一 Source、7 条真实声明边、8 次 Provider 调用；
- data/view 文件集展开不伪造边；
- 文件集顺序与主/测试资源镜像确定性保持；
- sourceId 与 canonical reference 身份域分离；
- 任何失败不发布部分 SourceGraph。

### 完整 XML 声明路径

root 文档只允许：

- `/orm-config/orm-data-file-info/orm-file`
- `/orm-config/orm-view-file-info/orm-file`
- `/orm-config/system-file-info/system-file`
- `/orm-config/business-file-info/business-file`

systems 文档只允许：

- `/systems/system/rule-file-info/rule-file`

文档根必须分别为 `orm-config` 与 `systems`。其它路径中的同名元素全部忽略；错误根、错误嵌套与不完整结构返回 `MIX_SOURCE_POLICY`，且不访问下游目标 Provider。发布 Edge 的 `nodePath` 只能来自上述五条路径。

### Canonical、安全与位置

- 相对/绝对 URI 类别保持；
- 字面量和编码 `%2e` 当前目录段规范化保持；
- 编码父目录、编码分隔符、query、fragment 安全证据不被隐藏；
- Resolver 根操作处于统一受控失败边界；
- LF、CRLF、CR 均验证七条边的 line、column、nodePath；
- DTD、外部实体和外部资源解析关闭。

## 编码、范围与下一步

- `@Override` 独占一行，方法、构造器和关键逻辑使用中文注释；
- Java release 8；
- 未修改 `dec-core-context` 生产代码；
- 未实现 T04 Canonical Frontend、RawDefinitionSet、Symbol 或 Compiler Pipeline；
- 当前 PR：`#18`，分支 `feature/p1-t03-source-graph-20260802-1430`，目标 `dev_all`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t03_r05_completion.json`；
- 未经明确授权不得合并 PR #18；
- PR #18 合并前 `TASK-P1-T04` 保持未启动和阻断。
