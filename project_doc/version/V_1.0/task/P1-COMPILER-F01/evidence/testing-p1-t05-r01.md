# TASK-P1-T05 Testing Evidence R01

- Revision：`TESTING-P1-T05-R01@040f09b80463`
- Review：`REV-000229`
- Evidence：`EVD-000473`
- Head：`040f09b80463911c092e7693f47814f3904758fd`
- P0 Run：`30750632160`
- Artifact：`8834325522`
- Artifact SHA-256：`dc5bb0b3c4d1505f7f418c34042eb0071e1c770fc5cda489476cc76e91eb576c`
- Result：`PASSED`

## Surefire 结果

| 范围 | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| Context | 26 | 0 | 0 | 0 |
| Compiler | 83 | 0 | 0 | 0 |
| XML T04 | 30 | 0 | 0 | 0 |
| YAML T05 | 35 | 0 | 0 | 0 |
| Demo | 4 | 0 | 0 | 0 |
| Declaration legacy | 1 | 0 | 0 | 0 |

YAML T05 明细：

- `CanonicalParityTest`：3/3；
- `YamlFrontendResourceLimitTest`：11/11；
- `YamlFrontendSecurityTest`：10/10；
- `YamlFrontendArchitectureTest`：3/3；
- `YamlFrontendReviewTest`：8/8。

## Build Gate

- 12 模块 Reactor：`PASSED`；
- YAML 生产源码：14 files / Java release 8 编译；
- YAML 测试源码：6 files / Java release 8 编译；
- 故意失败门禁：1 个预期失败，脚本确认非零状态可阻断 Build；
- 报告上传：`PASSED`；
- MySQL：`SKIPPED_NOT_APPLICABLE`，不得表述为数据库测试通过。

## 合同结果

安全对象构造、tag、anchor/alias/递归/merge、文档形状、资源预算、SourceRef、schemaVersion、Canonical parity、参数失败、模块隔离和范围门禁全部通过。失败结果不携带部分 Canonical root。
