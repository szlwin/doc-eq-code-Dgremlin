# TASK-P1-T12 I004 TDD Evidence

- TDD：`TDD-P1-T12-R04@1270d6f2b829`
- Evidence：`EVD-000889`～`EVD-000894`
- Review：`REV-000573`～`REV-000575`

## Valid RED

- Head：`1270d6f2b829a568f7edda4a23e21ba2748d7a50`
- Run：`30974123330`
- Artifact：`8917617823`
- SHA-256：`3a9e7a4d87d97d4f7b667d32a85026f160c49be0d14152dc7c5ea03a025e9164`
- I004：8 tests / 6 expected failures / 0 errors / 2 passing boundary controls

六项失败精确命中：

- 默认深度超限未阻断；
- 24 层共享 DAG 重复展开；
- depth limits API 缺失；
- unique-container budget 缺失；
- List/Set edge budget 缺失；
- Map entry/edge budget 缺失。

通过项确认深度 256 原合同可运行、循环图仍被拒绝。I001～I003 和 T01～T11 均保持绿色。

## Independent Review RED

首轮 GREEN 后新增共享 DAG 作为 Set 元素的 hash-amplification Oracle：

- Head：`cbeed46dbf053184f247184ad9976c706d42f500`
- Run：`30974844132`
- Artifact：`8917868255`
- SHA-256：`aa5d191c64ac4e49875cef0272a164e4d73afcd3e79039da56b3a54b403a53be`
- Result：仅 `sharedDagSetElementDoesNotAmplifyHashComputation` 失败，0 errors；其余 I004 与历史回归绿色。

该失败确认 `LinkedHashSet.add()` 会递归调用共享冻结 List 的 `hashCode()`，因此纳入 I004 实现，不作为后续遗留项。
