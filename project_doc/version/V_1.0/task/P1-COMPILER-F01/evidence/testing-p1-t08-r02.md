# TASK-P1-T08 / I002 Testing Evidence

- Revision：`TESTING-P1-T08-R02@bab0993ecfd8`
- Clean-code Head：`bab0993ecfd8c344beead62712ba8dc02621038d`
- P0 Run：`30871077040`
- Result：`SUCCESS`
- Artifact：`8877900378`
- Artifact size：`1871523`
- GitHub SHA-256：`a6eed26d25e9962a28d79abc4108fc61992d5d43eae7c70261c38403a8a3d68c`
- Independent ZIP SHA-256：`a6eed26d25e9962a28d79abc4108fc61992d5d43eae7c70261c38403a8a3d68c`
- Surefire XML：`66`
- Status：`PASSED`
- Reviews：`REV-000369`～`REV-000370`
- Evidence：`EVD-000618`～`EVD-000619`

## TDD 历史

### 被拒绝 I002 RED attempt

- Head：`668c48bccf5095889c0d5ab845ea6d9523d57353`
- P0 Run：`30869955354`
- Artifact：`8877523987`
- Result：`22 failures / 0 errors`
- Rejected reason：合法 Canonical 引用矩阵实际为 15，测试错误要求至少 16；这是测试期望偏高，不是生产缺陷。

### 有效 I002 RED

- Revision：`TDD-P1-T08-R02@bfc8e4df822a`
- Head：`bfc8e4df822a54e072e2c3c79c011adee204a6ab`
- P0 Run：`30870089960`
- Artifact：`8877573048`
- Artifact SHA-256：`353f779d7a230b43e27119c7cbb8b71dc9291389e467c7de2ed5efb17f2cda4a`
- Result：`21 failures / 0 errors`
- Java release 8：编译成功；
- 合法 Canonical 主路径：PASSED；
- I001 T08 12/12、T07 Symbol 44/44 与既有 Compiler 回归：PASSED。

### Architecture Skeleton 受控 RED

- Revision：`DEVSKEL-P1-T08-R02@3e85814d5cf5`
- Head：`3e85814d5cf5c64be04646ed6dfb23c0e182ebf8`
- P0 Run：`30870305310`
- Artifact：`8877647016`
- Artifact SHA-256：`3547a8a0ac1dda3d521ca0a71eeb98e1344084e1c662ee23df3c6acb5cb89e67`
- Result：`21 controlled failures / 0 errors`。

## Clean-code 独立解析

| 范围 | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| I002 Boundary Rework | 13 | 0 | 0 | 0 |
| I002 Canonical Integration | 9 | 0 | 0 | 0 |
| I002 total | 22 | 0 | 0 | 0 |
| I001 T08 | 12 | 0 | 0 | 0 |
| T08 total | 34 | 0 | 0 | 0 |
| Symbol package | 66 | 0 | 0 | 0 |
| Compiler module | 195 | 0 | 0 | 0 |
| XML | 30 | 0 | 0 | 0 |
| YAML | 59 | 0 | 0 | 0 |
| Context normal | 26 | 0 | 0 | 0 |
| Demo | 4 | 0 | 0 | 0 |
| Legacy declaration | 1 | 0 | 0 | 0 |

所有正常测试共 `315` 项通过。另有 `P0IntentionalFailureTest` 1 项按预期失败，并被 P0 的故意失败阻断证明正确识别；总 XML 内测试数为 316，失败仅该预期门禁 1 项，errors=0、skipped=0。

## 构建门禁

- Maven Wrapper：PASSED；
- 仓库内 legacy dependencies bootstrap：PASSED；
- 12 模块 Reactor：PASSED；
- Java release 8：PASSED；
- Core build and tests：PASSED；
- Prove failing tests block the build：PASSED；
- Artifact upload：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 结论

四个 I002 Finding 均有直接负向或集成 Oracle；Artifact 已独立下载并验证摘要，测试证据可用于 Completion R02。