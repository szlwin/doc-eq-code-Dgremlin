# TASK-P1-T12 I005 Testing Evidence

- Testing：`TESTING-P1-T12-R05@304a2156ff5e`
- Clean-code Head：`304a2156ff5e86c2a45213d4e917f17b9a172831`
- P0 Run：`30984394393` — `SUCCESS`
- Artifact：`8921466813`
- SHA-256：`3a2002648c03c082f649991317e5ef3abbb167df6d99327dfa23c9e787d2fe6d`
- Evidence：`EVD-000946`～`EVD-000955`

## Independent Artifact parse

- Surefire XML：`96`
- 全部测试记录：`539`
- 正常测试：`538 / 538 PASSED`
- Intentional failure：`1`，仅 `P0IntentionalFailureTest`；
- Errors / Skipped：`0 / 0`
- Compiler module：`418 / 418`
- T12 total：`99 / 99`
- I005：`16 / 16`
- I005 primary RED Oracle：`8 / 8`
- I005 independent Review Oracle：`8 / 8`
- MySQL：`SKIPPED_NOT_APPLICABLE`

独立计算 ZIP SHA-256 与 GitHub Artifact digest 完全一致。

## Covered gates

- 24 层共享 DAG List equals 操作计数；
- Set contains/equals 跨独立 freeze Session；
- Map get/containsKey/containsValue/equals；
- Entry/EntrySet contains 与对称 hash；
- identity-pair memo；
- comparison depth、pair、edge、canonical-node 预算；
- hash 相同但结构不同；
- Frozen 与普通 Java List/Set/Map 对称性、传递性与 hash 一致；
- 外部普通容器的 equals/hashCode 不被 Frozen receiver 调用；
- I001～I004、prepare/commit、Diagnostic、Clock、Deadline、Context/Result 与 commit-wins 全部不回归。

Testing Gate：`PASSED`。
