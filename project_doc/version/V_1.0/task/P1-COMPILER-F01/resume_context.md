# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T09 / I001` 已完成
- 当前有效 Completion：`COMPLETION-P1-T09-R01@ecfe3f53bde7`
- Dependency：`COMPLETION-P1-T08-R02@bab0993ecfd8`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@e47551e0c79984d8f3fafc0ce379da76ad0d5593`
- Branch：`feature/p1-t09-engine-context-20260804-1040`
- PR：`#24`
- Design：`DESIGN-R31@P1-T09-I001`
- Plan：`TP-P1-COMPILER-F01-R27@P1-T09-I001`
- TDD：`TDD-P1-T09-R01@404105e89485`
- Architecture：`DEVSKEL-P1-T09-R01@8ae3f86316fa`
- Development：`DEV-P1-T09-R01@ecfe3f53bde7`
- Code Review：`CODEREVIEW-P1-T09-R01@ecfe3f53bde7`
- Testing：`TESTING-P1-T09-R01@ecfe3f53bde7`
- Reviews：`REV-000374`～`REV-000390`
- Evidence：`EVD-000623`～`EVD-000645`
- Open P0/P1/P2：`0 / 0 / 0`

## Revision Integrity

- R31 first commit：`f2ab328f67b03f710abafc85a9e1616ebe23f298`
- R31 blob：`539b8603efba73b45547a4602c9b14e2b523c2e4`
- R27 first commit：`4483ce64c6ecffc989e3adcbd3a8178d301cace9`
- R27 blob：`20a16d1e7b199088086f496fe94aeb8b8684d8ca`
- R31/R27 在有效 RED 前创建，clean-code Head 复核未变化。

## TDD / Architecture

- Rejected RED：`cf8c8b6f6b4b...` / Run `30873531942` / `17 failures + 1 error`；测试缺类异常未接管；
- Valid RED：`404105e89485...` / Run `30873857907` / Artifact `8878801137` / `18 failures + 0 errors`；
- Architecture：`8ae3f86316fa...` / Run `30874099740` / Artifact `8878893760` / `17 controlled failures + 0 errors`；
- 两个 JaCoCo GREEN attempt 误报 synthetic `$jacocoData`，均保留为已关闭测试发现。

## Current Contract

- 不可变 REFERENCE/AND/OR AST；`and` 高于 `or`，operator 小写；
- expression 长度 8192、token 1024、嵌套 128 硬预算；
- qualified target 严格为两个非空 segment；
- 普通 System 只允许同 owner Information；
- common 允许 fully-qualified 跨 System 与 `common.*` 引用；
- common 只允许 name+expression，禁止 data/view/rule/model 成员；
- 成功生成 P3 Information Deferred，dependency 精确、排序、去重；
- common 间接循环保留为 Deferred，T09 不求值、不建 DAG、不检测循环、不缓存；
- Diagnostic 完整聚合、去重、稳定排序；任一 ERROR 不发布部分结果；
- parser 失败时 resolver 不执行。

## Validation

- Clean-code Head：`ecfe3f53bde72e055c97886aef20712f6a42fea3`
- P0 Run：`30874981158` — SUCCESS
- Artifact：`8879210068`
- SHA-256：`faeb4b46c1325fe50edbe90dc2d89098ded105fd683d994160da025bda244fb3`
- Independent ZIP SHA match：`true`
- Surefire XML：`69`
- T09：`24/24`
- Symbol：`66/66`
- Compiler：`219/219`
- XML：`30/30`
- YAML：`59/59`
- Context normal：`26/26`
- Demo：`4/4`
- Legacy：`1/1`
- Normal tests：`339/339`
- Intentional failure gate：`recognized`
- Reactor：`12 modules / PASSED`
- Java release 8：`PASSED`
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Scope / Recovery

- 生产范围仅 `dec.core.compiler.information`；未修改 Context、T06、T07、T08、Compiler API 或 systems fixture；
- 临时 workflow 已删除；
- `@Override` 独占一行，方法和重要逻辑使用中文注释；
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t09-r01/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t09-r01.md`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t09_r01_completion.json`
- 下一 Agent：`IndependentReviewAgent`
- 下一动作：复核 PR #24 最终文档化 Head；仅在用户明确授权后合并；
- TASK-P1-T10：`BLOCKED_UNTIL_PR_MERGE`。
