# TASK-P1-T06 I003 Testing Evidence

- Revision：`TESTING-P1-T06-R03@432ccdc1103f`
- Clean-code Head：`432ccdc1103f0119230858e7ae2343529af6c294`
- P0 Build Gate：Run `30801214669` — `SUCCESS`
- Artifact：`8850875201`
- Artifact size：`1,757,285 bytes`
- GitHub digest：`sha256:eadc28a2db03ff23405869712aefa84398cf1b9b37f9408b20d348af67d783b7`
- 独立下载实际 ZIP SHA-256：`eadc28a2db03ff23405869712aefa84398cf1b9b37f9408b20d348af67d783b7`
- Review：`REV-000294` TestEvidenceReviewAgent — `PASSED`
- Evidence：`EVD-000536`

## Surefire 独立解析

| 范围 | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| I003 snapshot | 7 | 0 | 0 | 0 |
| T06 Raw total | 38 | 0 | 0 | 0 |
| Compiler total | 121 | 0 | 0 | 0 |
| XML | 30 | 0 | 0 | 0 |
| YAML | 59 | 0 | 0 | 0 |
| Context 正常测试 | 26 | 0 | 0 | 0 |
| Demo | 4 | 0 | 0 | 0 |
| Legacy declaration | 1 | 0 | 0 | 0 |
| 故意失败门禁 | 1 | 1（预期） | 0 | 0 |

T06 Raw 组成：

- `RawDefinitionBuilderContractTest`：11；
- `RawGrammarAndFormatReviewTest`：3；
- `RawValueObjectReviewTest`：2；
- `RawInvariantReworkTest`：8；
- `RawInvariantIndependentReviewTest`：4；
- `RawInvariantAdditionalReviewTest`：3；
- `RawInputSnapshotReworkTest`：7。

## I003 Oracle

- 第二次 iterator 的 unsupported root 不被消费；
- 第二次 iterator 的 unknown child 不进入 Raw body；
- snapshot 后原 List 修改不影响结果；
- snapshot 顺序决定 ordinal；
- snapshot 内真实 unsupported root/unknown child 继续 fail closed；
- snapshot 读取异常稳定返回 `raw.build.failed`，且原始 List 只迭代一次。

## Gate

- Maven Wrapper：PASSED；
- 依赖引导：PASSED；
- Java release 8 production/test compile：PASSED；
- 12 模块 Reactor：PASSED；
- 故意失败可阻断构建证明：PASSED；
- 报告上传：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`，不得表述为通过。
