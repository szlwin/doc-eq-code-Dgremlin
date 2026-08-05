# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T13 / I001` 已完成
- 当前有效 Completion：`COMPLETION-P1-T13-R01@74672ee1367b`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / OPEN / READY_FOR_REVIEW`
- Design：`DESIGN-R45@P1-T13-I001`
- Plan：`TP-P1-COMPILER-F01-R41@P1-T13-I001`
- TDD：`TDD-P1-T13-R01@4f3d444f779f`
- Architecture：`DEVSKEL-P1-T13-R01@4f3d444f779f`
- Development：`DEV-P1-T13-R01@74672ee1367b`
- Code Review：`CODEREVIEW-P1-T13-R01@74672ee1367b`
- Testing：`TESTING-P1-T13-R01@74672ee1367b`
- Reviews：`REV-000653`～`REV-000671`
- Evidence：`EVD-001003`～`EVD-001018`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- `SemanticDigestInput` 构造时冻结全部语义事实与版本域；
- canonical JSON 使用 Unicode code point key order、标准 escaping、canonical decimal；
- 未知值、NaN/Infinity、循环和重复 object key fail-closed；
- sourceDigest 使用 domain、Source 数量、sourceId/content 长度前缀和 SHA-256；
- semanticDigest 排除 line/column、Source format/content digest、Timing、Observer、DigestPair 与 Publication；
- 成功 Pipeline Timing 为 PASS=10、DISCOVERY=1、PARSE=1、DIGEST=1；
- supplemental timing 不增加 Clock 读取；
- timing/transition Observer failure 转为稳定 Warning，不能改变终态或 artifact；
- T12 Deadline/Cancel/Clock/Publication 原子性保持；
- T14/T15 与 P2～P7 runtime 未实现。

## Validation

- Valid RED：`4f3d444f779f5c1f69a5b61751cbd00b4a9a528b` / Run `31005889102` / `11 expected failures, 2 controls, 0 errors`
- First GREEN：`44aaa97678407865a34d06a9d4e61c21538ba273` / Run `31007497348` — SUCCESS
- Production：`65f96c71ae0560f375d402b586125ad4879dde4b`
- Code/Test：`74672ee1367bab9de75b4028cd4578b6118f96f0`
- Clean validation：`eadeeffba4a947b1f400890fffbeafc30803ef1a` / Run `31008161016` — SUCCESS
- Artifact：`8931238649`
- SHA-256：`57c6b57716f52e0c86ace7daf221fb51b8c88a5c7af5e2396a8d690c9f4dfed4`
- T13：`25/25`；T12：`133/133`；Compiler：`477/477`；Normal：`597/597`
- Surefire XML：`105`；Errors/Skipped：`0/0`
- 12 modules / Java release 8 / intentional failure gate：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t13-r01/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t13-r01.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t13-r01.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t13_r01_completion.json`
- Skill baseline：`common-develop-v2.44-rc8@4787876e135d347e9f37580910e2d28b09ea2ba4`；guard=`DIRTY / HEAD_MATCHES / CRITICAL_DRIFT_0`；
- 所有 `@Override` 独占一行，方法和重要逻辑使用中文注释；
- 仅在用户明确授权后合并 PR #28；
- TASK-P1-T14：`BLOCKED_UNTIL_PR_28_MERGE`。
