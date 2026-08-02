# TASK-P1-T05 / I003 — Testing Evidence

- Testing：`TESTING-P1-T05-R03@30529276cd8f`
- Review：`REV-000255`
- Evidence：`EVD-000497`
- Clean-code Head：`30529276cd8fa35e0eeeafb1256b85cb99820afb`
- P0 Run：`30756293074`
- Artifact：`8836020099`
- Artifact SHA-256：`3362ee5de19129f0a819bb1587e42552077618f7bf43b3011e15540ec0bcd688`
- Result：`PASSED`

## Artifact 独立复核

Artifact ZIP 已独立下载、计算 SHA-256 并解析全部 Surefire XML。实际 ZIP SHA-256 与 GitHub digest 完全一致。

| 范围 | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| Context 正常测试 | 26 | 0 | 0 | 0 |
| Compiler | 83 | 0 | 0 | 0 |
| XML T04 | 30 | 0 | 0 | 0 |
| YAML T05 Total | 59 | 0 | 0 | 0 |
| YAML I001/I002 Existing | 45 | 0 | 0 | 0 |
| YAML I003 Budget / Resolver | 12 | 0 | 0 | 0 |
| YAML I003 Architecture Review | 2 | 0 | 0 | 0 |
| Demo | 4 | 0 | 0 | 0 |
| Legacy declaration | 1 | 0 | 0 | 0 |
| 故意失败门禁 | 1 | 1（预期） | 0 | 0 |

## 构建门禁

- Maven Wrapper：PASSED；
- repository-bundled legacy dependencies：PASSED；
- 12 模块 Reactor：PASSED；
- 生产和测试源码 Java release 8：PASSED；
- 故意失败返回非零且被门禁识别：PASSED；
- 报告上传：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`，不表述为测试通过。

## 专项结论

- 超限 scalar 四位置均优先命中 `yaml.frontend.limit.scalar-per-node`；
- Resolver float正向四位置通过；
- Resolver invalid int负向四位置失败；
- Policy 架构与代表性 Resolver 词法一致性通过；
- 严格 UTF-8、portable name、对象/tag、anchor/alias、资源预算与 Canonical 映射回归通过；
- 失败不发布部分 root；
- 开放 P0/P1/P2 为 0。
