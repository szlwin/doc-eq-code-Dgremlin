# TASK-P1-T09 / I001 — System-owned Information 与 common 表达式绑定

- 状态：`COMPLETED / PASSED`
- Base：`dev_all@e47551e0c79984d8f3fafc0ce379da76ad0d5593`
- Dependency：`COMPLETION-P1-T08-R02@bab0993ecfd8`
- Branch：`feature/p1-t09-engine-context-20260804-1040`
- PR：`#24`
- Design：`DESIGN-R31@P1-T09-I001`
- Plan：`TP-P1-COMPILER-F01-R27@P1-T09-I001`
- TDD：`TDD-P1-T09-R01@404105e89485`
- Architecture：`DEVSKEL-P1-T09-R01@8ae3f86316fa`
- Development：`DEV-P1-T09-R01@ecfe3f53bde7`
- Code Review：`CODEREVIEW-P1-T09-R01@ecfe3f53bde7`
- Testing：`TESTING-P1-T09-R01@ecfe3f53bde7`
- Completion：`COMPLETION-P1-T09-R01@ecfe3f53bde7`
- Reviews：`REV-000374`～`REV-000390`
- Evidence：`EVD-000623`～`EVD-000645`
- Open P0/P1/P2：`0 / 0 / 0`

## Delivered Contract

- expression 编译为不可变 REFERENCE/AND/OR AST；
- `and` 高于 `or`，operator 严格小写；
- qualified Information 必须恰好为 `system.name` 两个非空 segment；
- 普通 System expression 只能引用同 System Information；
- common 可引用任意已存在的 fully-qualified Information，包括 `common.*`；
- common Information 只允许 `name + expression`，data/view/rule/model 成员 fail-closed；
- 每个成功 expression 生成 `RequiredStage.P3`、`DeferredKind.INFORMATION` 的 Deferred；
- dependency 使用精确 `InformationKey`，稳定排序并去重；
- common 间接循环在 T09 只发布 Deferred，不提前执行循环检测；
- Diagnostic 完整聚合、去重、稳定排序；任一 ERROR 不发布部分 Compilation。

## Validation

- Clean-code Head：`ecfe3f53bde72e055c97886aef20712f6a42fea3`
- P0 Run：`30874981158` — SUCCESS
- Artifact：`8879210068`
- SHA-256：`faeb4b46c1325fe50edbe90dc2d89098ded105fd683d994160da025bda244fb3`
- T09：`24/24`
- Compiler：`219/219`
- Normal tests：`339/339`
- Intentional failure gate：`recognized`
- Reactor：`12 modules / PASSED`
- Java release 8：`PASSED`
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Scope and Coding

- 生产修改仅位于 `dec.core.compiler.information`；
- 未修改 Context、T06 Raw、T07 Symbol、T08、Compiler API 或 `systems.xml`；
- 不求值、不建 DAG、不检测循环、不缓存、不引入 I/O、网络或全局状态；
- 临时源码快照 workflow 已删除；
- `@Override` 独占一行；
- 方法、构造器及重要 parser、owner、common、Diagnostic、资源与失败逻辑使用中文注释。

## Next Gate

- PR #24：待 Independent Review，未经用户明确授权不得合并；
- PR #24 合并前 `TASK-P1-T10` 保持 `BLOCKED_UNTIL_PR_MERGE`。
