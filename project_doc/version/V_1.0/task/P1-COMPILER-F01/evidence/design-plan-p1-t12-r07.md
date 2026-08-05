# EVD-000992 — TASK-P1-T12 / R07 Design 与 Plan 证据

- Task：`TASK-P1-T12 / I007`
- Design：`DESIGN-R44@P1-T12-REWORK-I007`
- Plan：`TP-P1-COMPILER-F01-R40@P1-T12-REWORK-I007`
- Status：`PASSED / FROZEN`

## Revision order

| Artifact | First commit | Blob |
|---|---|---|
| R44 Design | `f5adb11de55364150973fb048396841341fc29a9` | `e8417832ed971c230b9159f5bcec8d577d15a268` |
| R40 Plan | `0f09627b1f6664a084c7f1a9ac18b68b7027bb9b` | `223ed0fc0ae8bdef22de1cca8f28916752d8d97b` |
| Valid RED | `cb3f08f28807ad40e2a4b40519baf4a2fc83ba61` | — |

R44/R40 的 first commit 均严格早于有效 RED，最终 blob 与首次冻结 blob 相同。

## Frozen decisions

- duplicate canonical Map key 与 Set element 均以稳定 `CanonicalCollisionException` fail-closed；
- collision 检查发生在 `nodeId()`、`complete()` 与父 assignment 之前；
- Map.Entry 不应用 duplicate-key 门禁；
- 普通 hash collision 继续精确比较；
- 删除无调用点 `ConditionalCompareTask`；
- 保持 I001～I006、Java 8、预算和 Publication 合同。
