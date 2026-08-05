# TASK-P1-T13 / R01 Testing Evidence

- Evidence：`EVD-001011`～`EVD-001014`
- Testing：`TESTING-P1-T13-R01@74672ee1367b`
- Code Review：`CODEREVIEW-P1-T13-R01@74672ee1367b`

## First GREEN

- Head：`44aaa97678407865a34d06a9d4e61c21538ba273`
- Production：`65f96c71ae0560f375d402b586125ad4879dde4b`
- P0 Run：`31007497348` — SUCCESS
- Artifact：`8930962119`
- SHA-256：`e42468dc2480a7e103aa511c41518fcb692b996f3547cc7374e143226f1c6e88`

## Clean-code / Independent Review validation

- Code/Test Revision：`74672ee1367bab9de75b4028cd4578b6118f96f0`
- Validation Head：`eadeeffba4a947b1f400890fffbeafc30803ef1a`
- P0 Run：`31008161016` — SUCCESS
- Artifact：`8931238649`
- 独立 ZIP SHA-256：`57c6b57716f52e0c86ace7daf221fb51b8c88a5c7af5e2396a8d690c9f4dfed4`
- GitHub digest：`sha256:57c6b57716f52e0c86ace7daf221fb51b8c88a5c7af5e2396a8d690c9f4dfed4`

## Parsed result

- Surefire XML：`105`
- T13：`25/25`
  - `SemanticDigestDeterminismTest`：`6/6`
  - `CompilationObserverTest`：`4/4`
  - `CompilationDeadlineTest`：`3/3`
  - `SemanticDigestIndependentReviewTest`：`8/8`
  - `CompilationObserverIndependentReviewTest`：`4/4`
- T12：`133/133`
- Compiler module：`477/477`
- 正常测试：`597/597`
- 全部测试记录：`598`
- 故意失败门禁：`P0IntentionalFailureTest` 1 项按预期失败并被识别
- Errors / Skipped：`0 / 0`
- Maven Wrapper、legacy dependency bootstrap、Java release 8、12 模块 Reactor：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Review oracles

- Unicode supplementary code point object-key 顺序；
- JSON quote/backslash/control escaping、canonical decimal；
- NaN/Infinity/unknown/cycle/duplicate-key fail-closed；
- empty input、版本域与不可变 Registry 视图；
- Source length-prefix 与 Unicode sourceId 顺序；
- 13 个 Timing Observer failure Warning；
- 全部 Transition failure Warning；
- supplemental elapsed 与 20 次 Clock 读取；
- observation diagnostic code/severity/seal 边界。

结论：`PASSED`。
