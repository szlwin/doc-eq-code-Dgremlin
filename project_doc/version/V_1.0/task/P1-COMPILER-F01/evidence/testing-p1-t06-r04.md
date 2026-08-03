# TASK-P1-T06 I004 Testing Evidence

- Revision：`TESTING-P1-T06-R04@242db638c61d`
- Clean-code Head：`242db638c61d58eb70e452c1ac08668b6d738b0a`
- P0 Build Gate：Run `30810370900` — `SUCCESS`
- Artifact：`8854512655`
- GitHub digest：`sha256:4472e3cd084eadc18e6b47af19738f9f227834d0a5217caac29b0529ee1aeb33`
- 独立 ZIP SHA-256：`4472e3cd084eadc18e6b47af19738f9f227834d0a5217caac29b0529ee1aeb33`
- Artifact 校验：完全一致
- Review：`REV-000307` — TestEvidenceReviewAgent — `PASSED`
- Evidence：`EVD-000549`

## Surefire 独立解析

| 范围 | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| I004 snapshot budget | 8 | 0 | 0 | 0 |
| I003 snapshot | 7 | 0 | 0 | 0 |
| T06 Raw total | 46 | 0 | 0 | 0 |
| Compiler total | 129 | 0 | 0 | 0 |
| XML | 30 | 0 | 0 | 0 |
| YAML | 59 | 0 | 0 | 0 |
| Context 正常测试 | 26 | 0 | 0 | 0 |
| Demo | 4 | 0 | 0 | 0 |
| Legacy declaration | 1 | 0 | 0 | 0 |
| 故意失败门禁 | 1 | 1（预期） | 0 | 0 |

## Gate

- Maven Wrapper：PASSED；
- repository-bundled legacy dependencies bootstrap：PASSED；
- Core build and tests：PASSED；
- 12 模块 Reactor：PASSED；
- Java release 8 编译：PASSED；
- 故意失败阻断证明：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`，不得表述为测试通过。

I004 的 8 项 Oracle 证明：两个单根文档等于预算时允许；第三个文档在 add 前失败；SourceRef 指向第三文档；不读取第四项；完整树后代预算仍执行；失败不发布集合；原始 List 只创建一次 iterator；不调用随机访问、批量转换、Stream 或 Spliterator 入口。
