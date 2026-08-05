# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T14 / I001` 已完成
- 当前有效 Completion：`COMPLETION-P1-T14-R01@252024603bfc`
- Dependency：`COMPLETION-P1-T13-R03@5075793d06cc`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@3e4da420d2ef5ada8398aefbbeabb37964e384ce`
- Branch：`feature/p1-t14-candidate-context-20260805-2324`
- PR：`#29 / OPEN / FINAL_P0_PENDING / NOT_MERGED`
- Design：`DESIGN-R48@P1-T14-I001`
- Plan：`TP-P1-COMPILER-F01-R44@P1-T14-I001`
- TDD：`TDD-P1-T14-R01@f0f76facdd76`
- Architecture：`DEVSKEL-P1-T14-R01@94fcc64aa6da`
- Development：`DEV-P1-T14-R01@1a930d775e3e`
- Code Review：`CODEREVIEW-P1-T14-R01@252024603bfc`
- Testing：`TESTING-P1-T14-R01@252024603bfc`
- Reviews：`REV-000706`～`REV-000724`
- Evidence：`EVD-001046`～`EVD-001067`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- Builder 固定四阶段且只能使用一次；
- Registry/Deferred 在阶段入口完成完整快照；
- size、keys、copy 和 final size 必须一致；
- key/value identity、duplicate 和 missing value fail-closed；
- FrozenInput 为不可变 Pipeline artifact；
- candidate 包含完整模型、摘要、版本和 Diagnostic；
- ERROR 拒绝，Warning 保留；
- final Pass 只准备 candidate，不持有 Publisher/CAS；
- missing input publisher=0；normal input publisher=1；
- T12/T13 Deadline、Observer、Digest 和 commit-wins 保持；
- T15 与 P2～P7 runtime 未实现。

## Validation

- Valid RED：`f0f76facdd76d626cd82859ef8413964ae1b6fdf`
- Valid RED Run/Artifact/SHA：`31021944964` / `8936970743` / `f9e5259bb29a11f7ebf23637f3541df0f82485af10a2dc6953b7e89c939ccc5e`
- Review RED：`a494fa37574f7ae37362421d15e4f6a175ff6091`
- Review RED Run/Artifact/SHA：`31023013154` / `8937412168` / `28448029b7f95dee776129bbf8c6fd521856d5dc489bd37f25d0a59c37c9ed99`
- Production Revision：`1a930d775e3e226da55ec83697a2942d3dd1950d`
- Code/Test Revision：`252024603bfcdcee4ac42310b54b2af143aca002`
- Clean P0：`31023363308` — SUCCESS
- Clean Artifact/SHA：`8937562356` / `a8027e3479e0800086e9d97ef640ef1189b6a7dfde2324d712c0647e305250a6`
- Surefire XML：108；T14：12/12；T13：34/34；T12：133/133；Compiler：498/498；Normal：618/618
- All records：619；intentional failure：1；Errors/Skipped：0/0
- Java 8、12 modules、intentional failure gate：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Revision Integrity

- R48 first commit/blob：`ceb032670a96715a61ff3db6edd7032fc58b409f` / `6fdd71a8ddeae2afa2935233aee3a2d24441a98b`
- R44 first commit/blob：`1581481e3c8acb46d6120aa28b63476aa2e9890c` / `006311b43f1304aaa439b19b5d9b4eea3d808af5`
- R48/R44 均早于有效 RED，blob 未变化；
- Code/Test Revision 后只允许 `project_doc` 更新。

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t14-r01/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t14-r01.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t14-r01.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t14_r01_completion.json`
- 所有 `@Override` 独占一行，方法和重要逻辑使用中文注释；
- 仅在用户明确授权后合并 PR #29；
- TASK-P1-T15：`BLOCKED_UNTIL_PR_29_MERGE`。
