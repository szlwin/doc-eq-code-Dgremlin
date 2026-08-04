# TASK-P1-T10 / I001 — ModelAccess 精确 Selector

- 状态：`COMPLETED / PASSED`
- Base：`dev_all@4fe0f6def8581e5c7234d86dfa0aafae794db15f`
- Dependency：`COMPLETION-P1-T09-R02@95b08223083f`
- Branch：`feature/p1-t10-rule-dag-20260804-1428`
- PR：`#25`
- Design：`DESIGN-R33@P1-T10-I001`
- Plan：`TP-P1-COMPILER-F01-R29@P1-T10-I001`
- TDD：`TDD-P1-T10-R01@f1ff4c03ece8`
- Architecture：`DEVSKEL-P1-T10-R01@6db11965ec79`
- Development：`DEV-P1-T10-R01@9e94bc68d9a8`
- Code Review：`CODEREVIEW-P1-T10-R01@9e94bc68d9a8`
- Testing：`TESTING-P1-T10-R01@9e94bc68d9a8`
- Completion：`COMPLETION-P1-T10-R01@9e94bc68d9a8`
- Reviews：`REV-000408`～`REV-000424`
- Evidence：`EVD-000669`～`EVD-000691`
- Findings：`FND-P1-T10-I001-001/002` CLOSED
- Open P0/P1/P2：`0 / 0 / 0`

## Delivered Contract

- `ModelAccessCompiler` 在任何 owner、View、selector 或 body 解析前校验完整 Raw/Symbol 快照；
- 共享模型 source path 与当前 System target selector 严格分离；
- target-main 完整、区分大小写匹配优先；
- 未命中后只在当前 System 已声明的目标 View property 树中逐段精确解析；
- 禁止大小写折叠、前后缀、root-property、模糊、跨 View 或跨 System 回退；
- 未声明 View、未知 source View、缺失、歧义、非复合中间段和非法 lexical fail-closed；
- 完全重复和不同 SourceRef 的语义重复 Binding 均拒绝；
- WRITE 相同、祖先、后代及通配 `*` 重叠均拒绝；
- 成功发布不可变 `ModelAccessBinding` 与 `DeferredKind.MODEL_ACCESS`、`RequiredStage.P2` Deferred；
- Deferred reason 为 `model-access-selector-binding`，body format 为 `model-access-binding/v1`；
- Diagnostic 聚合、去重、稳定排序；任一 ERROR 不发布部分 Compilation；
- P1 不执行权限、访问、查询、SQL、I/O、缓存、DAG 或运行时逻辑。

## Independent Review

- 真实 Canonical `view-ref` 使用 `ref` 属性，首轮实现仅读取 `name`；已修复并增加真实 Canonical → T06 → T07 → T10 集成测试；
- `ModelAccessBinding.compareTo/toString` 首轮未包含 SourceRef；已修复为与 equals/hashCode 完整值语义一致；
- 新增通配 WRITE、跨 View 禁止回退、不同来源语义重复、非法 path 和无运行时状态测试。

## Validation

- Valid RED：`f1ff4c03ece8...` / Run `30885614810` / `17 failures, 0 errors`；
- Architecture：`6db11965ec79...` / Run `30886407036` / `14 controlled failures, 0 errors`；
- Clean-code Head：`9e94bc68d9a8c25351213bb46a6cafa5702105d9`；
- P0 Run：`30888758375` — SUCCESS；
- Artifact：`8884155225`；
- SHA-256：`f7dbad60dd352535113f7a8fa74f85a475e7cc3bf40dc9aa29acdc074f11fb24`；
- T10：`24/24`；Compiler：`255/255`；正常测试：`375/375`；
- XML：`30/30`；YAML：`59/59`；Context：`26/26`；Demo：`4/4`；Legacy：`1/1`；
- 故意失败门禁按预期识别；12 模块 Reactor、Java release 8：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Scope and Coding

- 生产修改仅位于 `dec.core.compiler.modelaccess`；
- 两份 systems fixture 只移除冗余后代 WRITE `payInfo.payDetailList`；
- 未修改 Context、T06/T07/T08/T09 公共合同或 Compiler API；
- 临时 source workflow 已删除；
- 所有 `@Override` 独占一行；
- 方法、构造器及重要 selector、快照、Diagnostic、重叠和失败逻辑均使用中文注释。

## Next Gate

- PR #25 未经用户明确授权不得合并；
- PR #25 合并前 `TASK-P1-T11` 保持 `BLOCKED_UNTIL_PR_MERGE`。
