# TASK-P1-T11 I002 TDD Evidence

- TDD：`TDD-P1-T11-R02@1297a9dd947f`
- Review：`REV-000478`
- Evidence：`EVD-000769`～`EVD-000770`
- Head：`1297a9dd947fedc3683d2eff1d61d6484e73a351`
- P0 Run：`30919478960`
- Artifact：`8896452496`
- SHA-256：`c9e14d66f0150a2eb5ca2b2658adf3c4835c8a88f0271595ece97dadbf6c944b`
- Result：`FAILURE / VALID RED`

## Independent Surefire parse

- Surefire XML：`64`
- 全部测试记录：`344`
- Failures：`5`
- Errors：`0`
- Skipped：`0`
- I002：`7 tests / 5 expected failures / 0 errors`
- 既有 T11：`26/26 PASSED`

## Expected failing Oracle

- `rejectsNullResolvedReferencesContainer`
- `nullContainerOverridesPreviouslyProvidedReferences`
- `nullContainerFailsWholeBatchAtomically`
- `snapshotsBatchBeforeClassificationTraversal`
- `doesNotExposeCallerIteratorFailure`

显式空列表与非 null 容器中的 null 元素边界保持通过。RED 只来自 Review 指定业务缺陷，不是编译、依赖、选择器或环境错误。
