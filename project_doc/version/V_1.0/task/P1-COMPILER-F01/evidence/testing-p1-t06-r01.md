# TASK-P1-T06 Testing Evidence

- Review：`REV-000268`
- Evidence：`EVD-000510`
- Revision：`TESTING-P1-T06-R01@90d483290cf3`
- Clean-code Head：`90d483290cf3943003624f21f19981535ca1408c`
- P0 Run：`30789608249`
- Artifact：`8846541706`
- Artifact SHA-256：`b0cf248154f392bf85a95c8903949efc16bf1a3bb264a2cbef72210df808b51f`
- 结果：`PASSED`

## CI Gate

- Maven Wrapper：PASSED；
- repository-bundled legacy dependency bootstrap：PASSED；
- 12 模块 Core build and tests：PASSED；
- Java release 8：PASSED；
- 故意失败阻断：PASSED；
- report upload：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 独立 Artifact 验证

Artifact 已独立下载，实际 ZIP SHA-256 与 GitHub digest 完全一致。解析 Surefire XML 后：

| 范围 | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| T06 Raw | 16 | 0 | 0 | 0 |
| Compiler total | 99 | 0 | 0 | 0 |
| XML T04 | 30 | 0 | 0 | 0 |
| YAML T05 | 59 | 0 | 0 | 0 |
| Context 正常测试 | 26 | 0 | 0 | 0 |
| Demo | 4 | 0 | 0 | 0 |
| Legacy declaration | 1 | 0 | 0 | 0 |
| 故意失败门禁 | 1 | 1（预期） | 0 | 0 |

T06 16 项组成：

- `RawDefinitionBuilderContractTest`：11；
- `RawValueObjectReviewTest`：2；
- `RawGrammarAndFormatReviewTest`：3。

故意失败用例未计入正常通过数量，其失败被 workflow 正确识别为门禁证据。
