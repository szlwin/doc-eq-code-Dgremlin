# TASK-P1-T12 / I005 — 冻结结果 Equality/Query 资源边界返工

- Status：`COMPLETED / PASSED`
- Base：`PR27@2e113984973232d2d9a1d35bb886f73488f539c8`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated History：`COMPLETION-P1-T12-R01@c6a515820972`、`COMPLETION-P1-T12-R02@5d5a7d72119b`、`COMPLETION-P1-T12-R03@4d4cd5c4c049`、`COMPLETION-P1-T12-R04@923129b1f20d`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R42@P1-T12-REWORK-I005`
- Plan：`TP-P1-COMPILER-F01-R38@P1-T12-REWORK-I005`
- TDD：`TDD-P1-T12-R05@c3a78498e595`
- Architecture：`DEVSKEL-P1-T12-R05@c3a78498e595`
- Development：`DEV-P1-T12-R05@304a2156ff5e`
- Code Review：`CODEREVIEW-P1-T12-R09@304a2156ff5e`
- Testing：`TESTING-P1-T12-R05@304a2156ff5e`
- Completion：`COMPLETION-P1-T12-R05@304a2156ff5e`
- Reviews：`REV-000591`～`REV-000610`
- Evidence：`EVD-000910`～`EVD-000965`
- Open P0/P1/P2：`0 / 0 / 0`

## Findings closed

- `FND-P1-T12-I005-001` `[P1][BLOCKER][RESOURCE]` — CLOSED：Frozen List/Set/Map/Entry equality/query 使用显式 pair stack、identity-pair memo 和 comparison budgets，24 层共享 DAG 不再指数展开；
- `FND-P1-T12-I005-002` `[P2][ORACLE]` — CLOSED：新增 leaf 调用计数、跨 freeze Session、List/Set/Map/Entry、四类预算、hash collision、普通外部容器与 Collection 合同 Oracle。

## Delivered contract

- equality/query 不依赖 AbstractList/AbstractSet/AbstractMap 的递归默认实现；
- 同一 `(left identity,right identity)` pair 只展开一次；
- 默认 comparison limits：depth=256、pairs=16384、edges=131072、canonical nodes=16384；
- List/Optional 有序 pair traversal；Set/Map 使用双根共享 canonical IDs；
- Frozen List 覆盖 equals/contains/indexOf/lastIndexOf；
- Frozen Set 覆盖 equals/contains；
- Frozen Map 覆盖 equals/get/containsKey/containsValue；
- Frozen Entry/EntrySet 使用受控 key/value equality；
- hash 只作快速拒绝，hash 相同仍精确比较；
- 普通外部容器 query 不调用其容器 equals/hashCode；
- 超限稳定抛出 `ComparisonLimitException`，不依赖 JVM Error、超时或线程中断；
- I001～I004、prepare/commit、Diagnostic、Clock、Deadline、Context/Result 与 commit-wins 合同保持。

## Validation

- Valid RED：`c3a78498e595d0006334c8ec382c72c830142d19` / Run `30983520984` / 6 failures / 0 errors；
- First GREEN：`6e3cb1dca3c55ad32aac335c51c552be37457f5d` / Run `30984182632` — SUCCESS；
- Clean-code Head：`304a2156ff5e86c2a45213d4e917f17b9a172831`；
- P0 Run：`30984394393` — SUCCESS；
- Artifact：`8921466813`；
- SHA-256：`3a2002648c03c082f649991317e5ef3abbb167df6d99327dfa23c9e787d2fe6d`；
- I005 16/16；T12 99/99；Compiler 418/418；正常测试 538/538；
- Surefire XML 96；Errors/Skipped 0/0；
- Java 8、12 模块 Reactor、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

PR #27 未经用户明确授权不得合并；PR #27 合并前 `TASK-P1-T13` 保持 `BLOCKED_UNTIL_PR_27_MERGE`。
