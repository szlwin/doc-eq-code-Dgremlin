# TASK-P1-T03 I001

- 任务：`TASK-P1-T03`
- Iteration：`I001`
- 状态：`COMPLETED`
- 分支：`feature/p1-t03-source-graph-20260802-1430`
- PR：`#18`，Completion 后恢复 Ready for review
- 基线：`dev_all@370b72f4bf4ec9b3620586f26d13d95f611f3cc9`
- 前置 Completion：`COMPLETION-P1-T02-R05@35376308b013`
- Design：`DESIGN-R13@P1-T03-I001`
- Plan：`TP-P1-COMPILER-F01-R09@P1-T03-I001`
- TDD：`TDD-P1-T03-R01@deaed1e7ea1a`
- Architecture Skeleton：`DEVSKEL-P1-T03-R01@f8437837e2a8`
- Development：`DEV-P1-T03-R01@713848bfa65e`
- Code Review：`CODEREVIEW-P1-T03-R01@713848bfa65e`
- Testing：`TESTING-P1-T03-R01@713848bfa65e`
- Completion：`COMPLETION-P1-T03-R01@713848bfa65e`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 完成结果

固定入口 `classpath:mix/orm-config.xml` 已构建精确 SourceGraph：

- SourceManifest：10 个唯一 Source；
- 声明边：7 条；
- Provider 调用：8 次；
- data 文件集：3 个 Source，只登记 Manifest，不伪造边；
- view 文件集：1 个 Source，只登记 Manifest，不伪造边；
- root 声明 4 条边；
- systems 声明 3 条 rule 边；
- 正序、逆序和随机文件集枚举得到完全相同的不可变图；
- `dec-demo` 主资源与测试镜像的固定 10 个 Source 字节和解析图一致。

Provider 调用 Oracle 从初始 10 修正为 8：一个文件集调用可以返回多个 Source。该修正只纠正 Provider 合同计数，没有放宽 10 Source / 7 Edge 验收。

## 安全和失败合同

- 字面量 `../`、编码 traversal、未知 scheme、opaque query 均在 Provider 调用前拒绝；
- 所有引用和 Provider 返回 Source 均验证 AllowedRoot；
- StAX 声明解析关闭 DTD、外部实体和外部资源解析；
- missing explicit Source 保留 `MIX-SOURCE-NOT-FOUND`；
- duplicate sourceId 使用 `MIX-SOURCE-DUPLICATE-ID`，相同或不同内容均拒绝；
- null/throwing Provider、错误基数、空文件集和 null result 使用 `MIX-SOURCE-POLICY`；
- maxDepth、maxSources、maxTotalBytes 均执行阻断；
- 重复 root 声明和祖先环路均阻断；
- 任何失败均不暴露部分 SourceGraph。

## TDD 与测试

### 有效 RED

- Head：`deaed1e7ea1adcf2580f01e75fe6ec39a140aba1`；
- P0 Run：`30736453340`；
- Context：26/26 通过；
- 既有 Compiler：47/47 通过；
- 新 T03：10 failures / 0 errors / 0 skipped；
- RED 唯一原因：`MixSourceResolver.resolve` 的 `Architecture skeleton only`；
- Java release 8 生产与测试源码编译成功。

### Architecture Skeleton

- Head：`f8437837e2a868360a6d31703b494f4cc72e229d`；
- P0 Run：`30736568295`；
- Context：26/26 通过；
- Compiler：57 项中 9 项受控 RED / 0 errors；
- 字面量 traversal 和未知 scheme 的 Provider 前置门禁已通过。

### 最终代码 GREEN

- Clean-code Head：`713848bfa65e19c8c802e4777944a3e22efec83e`；
- P0 Run：`30736808017`；
- Artifact：`8829855289`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：62 run / 0 failures / 0 errors / 0 skipped；
- T03：15 run / 0 failures / 0 errors / 0 skipped；
- 完整 12 模块 Reactor：PASSED；
- Java release 8：PASSED；
- 故意失败测试阻断：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Review 与 Evidence

- Review：`REV-000145`～`REV-000151` 全部 PASSED；
- Evidence：`EVD-000386`～`EVD-000392` ACTIVE；
- 开放 P0/P1 Finding：无；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t03_r01_completion.json`。

## 编码和范围

- 所有新增和修改的 `@Override` 均独占一行；
- 方法、构造器和安全、排序、候选隔离逻辑均使用中文注释；
- 未修改 `dec-core-context` 生产代码；
- Compiler 生产依赖仍仅为 Context；
- `dec-demo` 只作为测试 fixture 来源，不是生产依赖；
- 未实现 T04 Canonical Frontend、RawDefinitionSet、Symbol 或 Compiler Pipeline；
- PR #18 未获得明确授权不得合并；
- PR #18 合并到 `dev_all` 前，`TASK-P1-T04` 保持未启动和阻断。
