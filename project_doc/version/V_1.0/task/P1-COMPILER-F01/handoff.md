# P1-COMPILER-F01 阶段交接

> `TASK-P1-T01` 已通过 PR #16 合并，`TASK-P1-T02` 已通过 PR #17 合并。最新完成任务为 `TASK-P1-T03 / I001`，有效 Completion 为 `COMPLETION-P1-T03-R01@713848bfa65e`。T01/T02 的全部历史 Revision、Review、Evidence 和被后续 Review 推翻的 Completion 均继续保留。

## 已合并前置任务

### T01

- Completion：`COMPLETION-P1-T01-R04@ee99223a243f`；
- Merge commit：`f88f45731e16868bfacb489b63e3086aae49d018`；
- Context Projection 和发布聚合合同已冻结。

### T02

- Completion：`COMPLETION-P1-T02-R05@35376308b013`；
- Merge commit / T03 基线：`370b72f4bf4ec9b3620586f26d13d95f611f3cc9`；
- Source、AllowedRoot、Provider typed-result、Frontend 和 Canonical 公共合同已冻结；
- `validateSingle/validateFileSet` 可供 T03 防御性验证 Provider 结果。

## T03 I001 已完成

- Base：`dev_all@370b72f4bf4ec9b3620586f26d13d95f611f3cc9`；
- Design：`DESIGN-R13@P1-T03-I001`；
- Plan：`TP-P1-COMPILER-F01-R09@P1-T03-I001`；
- TDD：`TDD-P1-T03-R01@deaed1e7ea1a`；
- Architecture Skeleton：`DEVSKEL-P1-T03-R01@f8437837e2a8`；
- Development：`DEV-P1-T03-R01@713848bfa65e`；
- Code Review：`CODEREVIEW-P1-T03-R01@713848bfa65e`；
- Testing：`TESTING-P1-T03-R01@713848bfa65e`；
- Completion：`COMPLETION-P1-T03-R01@713848bfa65e`；
- Review：`REV-000145`～`REV-000151` 全部 PASSED；
- Evidence：`EVD-000386`～`EVD-000392` ACTIVE；
- Clean-code Head：`713848bfa65e19c8c802e4777944a3e22efec83e`；
- P0 Run：`30736808017`；
- Artifact：`8829855289`；
- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：62 run / 0 failures / 0 errors / 0 skipped；
- T03 Oracle：15 run / 0 failures / 0 errors / 0 skipped；
- 完整 12 模块 Reactor、Java release 8 和故意失败测试阻断：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1 Finding：无。

## T03 已冻结合同

### 精确 SourceGraph

- 根入口：`classpath:mix/orm-config.xml`；
- SourceManifest：精确 10 个唯一 Source，按 sourceId 稳定排序；
- 声明边：精确 7 条；
- root → data/view/system/business 共 4 条；
- systems → 3 个 rule 共 3 条；
- data/view 文件集展开只登记 Source，不产生伪边；
- Provider 合同调用为 8 次：root、2 个文件集、system、3 个 rule、business；
- 文件集正序、逆序和随机顺序产生相同图；
- 主资源与测试镜像的固定 10 个 Source 字节和图一致。

### 安全和失败语义

- SourcePolicy 在 Provider 前验证绝对 URI、scheme、AllowedRoot、字面量/编码 traversal、opaque query 和 depth；
- 每个 Provider 返回 Source 再次验证 AllowedRoot；
- 最小声明解析器只提取 T03 Source 声明，不生成 Canonical；
- StAX 关闭 DTD、外部实体和外部资源解析；
- missing Source 使用 `MIX-SOURCE-NOT-FOUND`；
- duplicate sourceId 使用 `MIX-SOURCE-DUPLICATE-ID`；
- Provider 合同、资源预算、重复声明和环路失败使用 `MIX-SOURCE-POLICY`；
- 任何失败不暴露部分 SourceGraph；
- 所有公开集合均不可变。

## 编码和范围

- 所有新增和修改的 `@Override` 独占一行；
- 方法、构造器和重要安全、排序、候选隔离逻辑使用中文注释；
- 未修改 `dec-core-context` 生产代码；
- Compiler 生产依赖仍仅为 Context；
- `dec-demo` 只作为 Maven testResources fixture，不是生产依赖；
- 未实现 T04 Canonical Frontend、RawDefinitionSet、Symbol 或 Compiler Pipeline。

## PR 状态与下一步

- 当前 PR：`#18`，目标分支 `dev_all`；
- 当前分支：`feature/p1-t03-source-graph-20260802-1430`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t03_r01_completion.json`；
- 未获得明确授权不得合并 PR #18；
- PR #18 合并前 `TASK-P1-T04` 保持未启动和阻断；
- PR #18 合并后，才能从新的 `dev_all` 启动 T04。
