# TASK-P1-T13 / I001 — 确定性 Digest、Deadline 与 Observer

- Status：`COMPLETED / PASSED`
- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / OPEN / READY_FOR_REVIEW`
- Design：`DESIGN-R45@P1-T13-I001`
- Plan：`TP-P1-COMPILER-F01-R41@P1-T13-I001`
- TDD：`TDD-P1-T13-R01@4f3d444f779f`
- Architecture：`DEVSKEL-P1-T13-R01@4f3d444f779f`
- Development：`DEV-P1-T13-R01@74672ee1367b`
- Code Review：`CODEREVIEW-P1-T13-R01@74672ee1367b`
- Testing：`TESTING-P1-T13-R01@74672ee1367b`
- Completion：`COMPLETION-P1-T13-R01@74672ee1367b`
- Reviews：`REV-000653`～`REV-000671`
- Evidence：`EVD-001003`～`EVD-001018`
- Open P0/P1/P2：`0 / 0 / 0`

## Delivered contract

- `SemanticDigestInput` 在构造时冻结 PublishedSourceManifest、Definition、Deferred 与 compiler/schema/options 版本域；
- `CanonicalJsonWriter` 使用 Unicode code point object-key 顺序、标准 JSON escaping 与 canonical decimal；
- NaN、Infinity、未知对象、循环结构和重复 object key 稳定拒绝；
- `CompilerDigestService` 生成长度前缀 Source SHA-256 与 canonical semantic SHA-256；
- sourceDigest 对 Source 输入顺序稳定，原始内容变化必然改变；
- semanticDigest 排除 SourceRef line/column、Source format/content digest、Timing、Observer、DigestPair 和 Publication 状态；
- 完整成功 Pipeline 记录 PASS=10、DISCOVERY=1、PARSE=1、DIGEST=1，共 13 个 Timing；
- supplemental timing 复用同一 elapsed，完整成功路径仍只读取 Clock 20 次；
- Observer RuntimeException 逐次转换为 `MIX-OBSERVER-FAILURE / WARNING`；
- Observer Warning 不改变 PUBLISHED/FAILED、artifact、Context、publisher 次数或 digest；
- Deadline、Cancel、Clock overflow、十 Pass 固定顺序、prepare/commit 与 commit-wins 合同保持；
- T14 候选 Context/CAS 扩展、T15 Starter/旧模块退役和 P2～P7 runtime 未实现。

## TDD RED

- Head：`4f3d444f779f5c1f69a5b61751cbd00b4a9a528b`
- P0 Run：`31005889102` — `FAILURE / EXPECTED_RED`
- Artifact：`8930284340`
- SHA-256：`fe03a8fea61ff6ecbcd2a45f8ddba3f91ac37629cf8c9ff1a583777dc5fa5946`
- Result：`13 tests / 11 expected failures / 2 passing controls / 0 errors`

## First GREEN

- Head：`44aaa97678407865a34d06a9d4e61c21538ba273`
- Production：`65f96c71ae0560f375d402b586125ad4879dde4b`
- P0 Run：`31007497348` — SUCCESS
- Artifact：`8930962119`
- SHA-256：`e42468dc2480a7e103aa511c41518fcb692b996f3547cc7374e143226f1c6e88`

## Clean-code validation

- Code/Test Revision：`74672ee1367bab9de75b4028cd4578b6118f96f0`
- Validation Head：`eadeeffba4a947b1f400890fffbeafc30803ef1a`
- P0 Run：`31008161016` — SUCCESS
- Artifact：`8931238649`
- SHA-256：`57c6b57716f52e0c86ace7daf221fb51b8c88a5c7af5e2396a8d690c9f4dfed4`
- Surefire XML：`105`
- T13：`25/25`
- T12：`133/133`
- Compiler：`477/477`
- 正常测试：`597/597`
- 全部测试记录：`598`
- 故意失败门禁：1 项按预期失败并被识别
- Errors / Skipped：`0 / 0`
- Java release 8、12 模块 Reactor：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Revision integrity and style

- R45 first commit/blob：`ef33ccf6f8fb7c4b2c76a4b137344cd5cb479858` / `ef0afc35234292a9c8e21a862af62eb91a100056`
- R41 first commit/blob：`392a8a40d3a390b6b8faae4e6e7d3af19df70091` / `6a5216718681d6f14ffe9ae9cfa56eb0a4d57cfa`
- R45/R41 均早于有效 RED，最终 blob 未变化；
- Code/Test Revision 后只允许 `project_doc` 更新；
- 所有传输 payload 与临时 Workflow 已删除；
- 所有 `@Override` 独占一行；
- 方法与排序、encoding、digest、Clock、Observer failure boundary 均使用中文注释。

PR #28 未执行合并；未经用户明确授权不得合并。PR #28 合并前 `TASK-P1-T14` 保持 `BLOCKED_UNTIL_PR_28_MERGE`。
