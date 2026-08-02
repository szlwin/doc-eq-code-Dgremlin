# P1-COMPILER-F01 阶段交接

> `TASK-P1-T01` 已通过 PR #16 合并，`TASK-P1-T02` 已通过 PR #17 合并。T03 的 Completion R01、R02、R03 均被后续独立 Review 推翻并作为不可变历史保留；最新有效 iteration 为 `TASK-P1-T03 / I004`，Completion 为 `COMPLETION-P1-T03-R04@04bfb86c9bf1`。

## 已合并前置任务

- T01 Completion：`COMPLETION-P1-T01-R04@ee99223a243f`，merge `f88f45731e16868bfacb489b63e3086aae49d018`。
- T02 Completion：`COMPLETION-P1-T02-R05@35376308b013`，merge / T03 base `370b72f4bf4ec9b3620586f26d13d95f611f3cc9`。

## T03 历史 Revision

### I001（历史，STALE_BY_REVIEW）

- Completion：`COMPLETION-P1-T03-R01@713848bfa65e`；
- 独立 Review `REV-000152` 发现 literal dot canonical、reference/sourceId identity 和声明起始位置缺口。

### I002（历史，STALE_BY_REVIEW）

- Completion：`COMPLETION-P1-T03-R02@6af43b47f044`；
- 独立 Review `REV-000163` 发现仅点段空 key、编码 `%2e` canonical 和 CRLF/CR Evidence 缺口。

### I003（历史，STALE_BY_REVIEW）

- Completion：`COMPLETION-P1-T03-R03@cedf22bb14ff`；
- 独立 Review `REV-000174` 发现前导点段可把相对引用提升为绝对 URI，以及 Resolver 根受控边界未完整实现。

## T03 I004（当前有效）

- Design：`DESIGN-R16@P1-T03-REWORK-I004`；
- Plan：`TP-P1-COMPILER-F01-R12@P1-T03-REWORK-I004`；
- TDD：`TDD-P1-T03-R04@282951053978`；
- Architecture Skeleton：`DEVSKEL-P1-T03-R04@01e8b7aa0e61`；
- Development：`DEV-P1-T03-R04@04bfb86c9bf1`；
- Code Review：`CODEREVIEW-P1-T03-R04@04bfb86c9bf1`；
- Testing：`TESTING-P1-T03-R04@04bfb86c9bf1`；
- Completion：`COMPLETION-P1-T03-R04@04bfb86c9bf1`；
- Review：`REV-000174`～`REV-000184`；
- Evidence：`EVD-000415`～`EVD-000427`；
- Clean-code Head：`04bfb86c9bf1accf879a729b3ceb04e1eee46f86`；
- P0 Run：`30740667853`；
- Artifact：`8831175314`；
- Artifact SHA-256：`af2820e792436d885d24ca91fbae7318445d0d22d64a84fda317e7442fd2ce6f`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：78 run / 0 failures / 0 errors / 0 skipped；
- I004 专项：4 run / 0 failures / 0 errors / 0 skipped；
- 12 模块 Reactor、Java 8、故意失败阻断：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1：无。

## T03 当前冻结合同

### SourceGraph 与身份域

- 固定根入口 `classpath:mix/orm-config.xml`；
- 精确 10 个唯一 Source、7 条真实声明边；
- data/view 文件集展开不伪造边；
- Provider 合同调用为 8 次；
- 文件集顺序与主/测试资源镜像确定性保持通过；
- sourceId 独立用于 Manifest 和 Diagnostic；canonical reference 用于 Provider、Edge、duplicate key、sorting、graph equality 和 cycle stack。

### Canonical 点段与 URI 类别

- `.`、`./`、`./.` 统一为非空 key `.`；
- 绝对 opaque/hierarchical URI 的字面量和编码 `%2e/%2E` 点段统一消除；
- 相对引用删除前导点段后仍保持相对 URI 类别；
- `./classpath:mix/orm-config.xml` 与 `%2e/classpath:mix/orm-config.xml` 统一为 `./classpath:mix/orm-config.xml`；
- `./file:/mix/root.xml` 与 `%2E/file:/mix/root.xml` 统一为 `./file:/mix/root.xml`；
- 上述四种相对根均返回 `MIX_SOURCE_PATH_ESCAPE`、空 graph、Provider access 0；
- `%2e%2e`、`.%2e`、`%2e.` 等父目录证据保留；
- `%2F` 等编码分隔符不解码，不改变 segment 结构；
- 不对完整 URI 执行 URL decode。

### Resolver 受控边界

- `root == null`、`policy == null` 保持显式参数异常；
- 根 SourceRef 创建、策略验证、Provider 判空、Discovery 创建和执行均位于统一受控边界；
- 内部 RuntimeException 映射为稳定失败结果，不泄漏异常或部分图。

### 声明位置与安全

- LF、CRLF、CR 均直接验证 7 条边的 line、column、nodePath；
- column 指向 start tag 的 `<`；
- DTD、外部实体和外部资源解析关闭；
- query、fragment、AllowedRoot 和 traversal 门禁保持；
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
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t03_r04_completion.json`；
- 未经明确授权不得合并 PR #18；
- PR #18 合并前 `TASK-P1-T04` 保持未启动和阻断。
