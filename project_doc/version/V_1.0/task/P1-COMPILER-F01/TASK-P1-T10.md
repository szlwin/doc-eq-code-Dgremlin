# TASK-P1-T10 — ModelAccess 精确 Selector

- 当前 Iteration：`I003`
- 状态：`COMPLETED / PASSED`
- 当前有效 Completion：`COMPLETION-P1-T10-R03@336d309f3748`
- Base：`dev_all@4fe0f6def8581e5c7234d86dfa0aafae794db15f`
- Dependency：`COMPLETION-P1-T09-R02@95b08223083f`
- Branch：`feature/p1-t10-rule-dag-20260804-1428`
- PR：`#25`
- Design：`DESIGN-R35@P1-T10-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R31@P1-T10-REWORK-I003`
- TDD：`TDD-P1-T10-R03@b16d5ee9f9f1`
- Architecture：`DEVSKEL-P1-T10-R03@d3f7225b4ee9`
- Development：`DEV-P1-T10-R03@bc056b7ed1da`
- Code Review：`CODEREVIEW-P1-T10-R03@336d309f3748`
- Testing：`TESTING-P1-T10-R03@336d309f3748`
- Reviews：`REV-000445`～`REV-000458`
- Evidence：`EVD-000719`～`EVD-000739`
- Open P0/P1/P2：`0 / 0 / 0`

## Completion History

- R01 / I001：`COMPLETION-P1-T10-R01@9e94bc68d9a8`，已失效，不可变历史保留；
- R02 / I002：`COMPLETION-P1-T10-R02@6f4c7b6f3ec3`，已失效，不可变历史保留；
- R03 / I003：`COMPLETION-P1-T10-R03@336d309f3748`，当前有效。

## Delivered Contract

- `ModelAccessCompiler` 在 owner、View、selector 或 body 解析前校验完整 Raw/Symbol 快照；
- 共享模型 source path 与当前 System target selector 严格分离；
- target-main 完整、区分大小写匹配优先；未命中后只在当前 System 已声明 View 的全部 `property-info` 中逐段精确解析；
- 禁止大小写折叠、前后缀、root-property、模糊、跨 View 或跨 System 回退；
- `SharedModelPath` 只允许完整 `*`，嵌入式 wildcard 一律 fail-closed；
- WRITE 重复、祖先、后代和完整 wildcard 重叠由 compilation-local segment trie 检测；
- ModelAccess root/body/attributes/scalar/children 在 resolver 前严格验证；
- `model-ref/ref@view` 是 TypedKey reference：允许 nonblank padded Raw lexical，Raw 值不改写，后续由 `ViewKey` canonicalize；
- `read/write@path` 与 `ref@property` 是精确 lexical：继续要求已经 trim，并执行既有 grammar；
- Raw `definition.name` 与 `model-ref` 按原始 lexical 完全一致；Binding 只发布 canonical `SystemKey/ViewKey`；
- 完全重复和不同 SourceRef 的语义重复 Binding 均拒绝；
- 成功发布不可变 `ModelAccessBinding` 与 `DeferredKind.MODEL_ACCESS`、`RequiredStage.P2` Deferred；
- Diagnostic 聚合、去重、稳定排序；任一 ERROR 不发布部分 Compilation；
- P1 不执行权限、访问、查询、SQL、I/O、缓存、DAG 或运行时逻辑。

## I003 Validation

- Valid RED：`b16d5ee9f9f1...` / Run `30905938187` / `3 failures, 0 errors`；
- Architecture：`d3f7225b4ee9...` / Run `30906147605` / `3 controlled failures, 0 errors`；
- Rejected fixture attempt：`bc056b7ed1da...` / Run `30906241652` / `2 failures, 0 errors`；
- First GREEN：`33a536d5a574...` / Run `30906506619`；
- Clean-code Head：`336d309f3748328ba4dea18be9944a95751ccc29`；
- P0 Run：`30906761804` — SUCCESS；
- Artifact：`8891365180`；
- SHA-256：`62aea0ce1ed32917e7c6dcdd8ae5c60fc0f627db90335cbbddb0c84c1f3e1915`；
- I003：`12/12`；T10：`54/54`；Compiler：`285/285`；正常测试：`405/405`；
- 故意失败门禁按预期识别；12 模块 Reactor、Java release 8：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Scope and Coding

- I003 生产修改仅为 `ModelAccessStructureValidator.java`；
- 未修改 Context、T06/T07/T08/T09 公共合同或 Compiler API；
- 无临时 workflow/publish trigger；
- `@Override` 独占一行规则未破坏；
- 方法和重要 lexical、canonicalization、结构门禁逻辑使用中文注释。

## Next Gate

- PR #25 未经用户明确授权不得合并；
- PR #25 合并前 `TASK-P1-T11` 保持 `BLOCKED_UNTIL_PR_MERGE`。
