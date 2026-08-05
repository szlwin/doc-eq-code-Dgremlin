# TASK-P1-T12 I005 Code / Independent Review

- Code Review：`CODEREVIEW-P1-T12-R09@304a2156ff5e`
- Reviewed Head：`304a2156ff5e86c2a45213d4e917f17b9a172831`
- Review result：`PASSED`
- Open P0/P1/P2：`0 / 0 / 0`
- Reviews：`REV-000591`～`REV-000610`
- Evidence：`EVD-000956`～`EVD-000965`

## Findings closed

### FND-P1-T12-I005-001 — CLOSED

Frozen List/Set/Map/Entry 的公开 equality/query 已全部进入非递归、受预算的图比较：

- 24 层共享 DAG 的 List equals、Set contains/equals、Map get/containsKey/containsValue/equals 与 EntrySet contains 不再按路径指数展开；
- identity pair memo 保证同一 `(left,right)` pair 只展开一次；
- Set/Map 使用同一比较 Session 的 canonical IDs 完成无序精确匹配；
- hash 仅作为两个内部 Frozen 对象的快速拒绝，hash 相同后仍执行精确比较；
- 普通外部容器作为 query 时由 Frozen receiver 显式读取，不调用其容器 equals/hashCode；
- comparison depth/pair/edge/canonical-node 超限稳定抛出 `ComparisonLimitException`，不依赖 JVM Error、耗时阈值或线程中断。

### FND-P1-T12-I005-002 — CLOSED

新增 16 项 I005 Oracle，覆盖：

- 两个独立 freeze Session 的共享 DAG；
- leaf equals 操作计数；
- List/Set/Map/Entry/EntrySet 全部公开读取路径；
- equal/non-equal 与 hash collision；
- identity-pair memo；
- 四类 comparison budget；
- Java Collection 自反、对称、传递和 hash 一致；
- 普通外部容器 query；
- I001～I004 全量回归。

## Independent Review notes

- `ArtifactComparisonSupport` 不形成公共 API；
- `ComparisonLimits` 和 `controlledEquals` 保持 package-private，仅用于精确边界测试；
- scalar equality 仍遵循 Java equals/hash 语义；
- cycle external query 在 canonicalization 时稳定拒绝；
- Frozen Entry 不允许 `setValue`；
- 没有新增 publisher capability 或绕过 prepare/commit；
- 没有实现 T13/T14/T15 或 P2～P7 runtime；
- 所有 `@Override` 独占一行，重要逻辑有中文注释。

## Validation

- Valid RED：`c3a78498e595d0006334c8ec382c72c830142d19` / Run `30983520984` / 6 failures / 0 errors；
- First GREEN：`6e3cb1dca3c55ad32aac335c51c552be37457f5d` / Run `30984182632` — SUCCESS；
- Clean-code Head：`304a2156ff5e86c2a45213d4e917f17b9a172831`；
- Clean-code P0：`30984394393` — SUCCESS；
- Artifact：`8921466813`；SHA-256：`3a2002648c03c082f649991317e5ef3abbb167df6d99327dfa23c9e787d2fe6d`；
- I005 16/16；T12 99/99；Compiler 418/418；正常测试 538/538；Errors/Skipped 0/0。

Review Gate：`PASSED`。
