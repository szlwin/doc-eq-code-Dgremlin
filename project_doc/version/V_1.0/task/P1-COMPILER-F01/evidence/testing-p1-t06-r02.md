# TASK-P1-T06 / I002 — Testing Evidence

- Testing：`TESTING-P1-T06-R02@aec3cd105b15`
- Review：`REV-000281`
- Evidence：`EVD-000523`
- Clean-code Head：`aec3cd105b15a302d8c1c91014c6c16529ef8c6a`
- P0 Run：`30793559695`
- Artifact：`8847970363`
- Artifact SHA-256：`922f8b7dc26245d6f0001ea1b6da86be05aed68ec21c1504634ec9f28ad64ae9`
- Result：`PASSED`

## Surefire 矩阵

| 范围 | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| T06 I001 Existing Raw | 16 | 0 | 0 | 0 |
| T06 I002 Rework | 8 | 0 | 0 | 0 |
| T06 I002 Independent Review | 4 | 0 | 0 | 0 |
| T06 I002 Additional Review | 3 | 0 | 0 | 0 |
| T06 Raw Total | 31 | 0 | 0 | 0 |
| Compiler Total | 114 | 0 | 0 | 0 |
| XML T04 | 30 | 0 | 0 | 0 |
| YAML T05 | 59 | 0 | 0 | 0 |
| Context 正常测试 | 26 | 0 | 0 | 0 |
| Demo | 4 | 0 | 0 | 0 |
| Legacy declaration | 1 | 0 | 0 | 0 |
| 故意失败门禁 | 1 | 1（预期） | 0 | 0 |

## 构建门禁

- Maven Wrapper：PASSED；
- repository-bundled legacy dependencies：PASSED；
- 12 模块 Reactor：PASSED；
- 生产和测试源码 Java release 8：PASSED；
- 故意失败返回非零并被门禁脚本识别：PASSED；
- Surefire/JaCoCo 报告上传：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`，不表述为测试通过。

## Artifact 独立校验

Artifact ZIP 已下载到隔离环境：

- 实际 SHA-256：`922f8b7dc26245d6f0001ea1b6da86be05aed68ec21c1504634ec9f28ad64ae9`；
- 与 GitHub digest 完全一致；
- 独立解析 55 个 Surefire XML；
- 唯一失败为 `P0IntentionalFailureTest` 的预期门禁失败；
- 无非预期 failure、error 或 skipped。

## 结论

五个独立 Review Finding、既有 T06 Grammar/顺序/引用边界、上游 T04/T05 和全仓回归均通过，开放 P0/P1/P2 为 0。
