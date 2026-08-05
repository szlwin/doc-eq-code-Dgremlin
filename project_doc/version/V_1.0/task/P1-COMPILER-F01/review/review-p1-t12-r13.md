# CODEREVIEW-P1-T12-R13 — I007 canonical collision 独立 Review

- Revision：`CODEREVIEW-P1-T12-R13@74f402287bc4`
- Task：`TASK-P1-T12 / I007`
- Design：`DESIGN-R44@P1-T12-REWORK-I007`
- Plan：`TP-P1-COMPILER-F01-R40@P1-T12-REWORK-I007`
- Result：`PASSED`
- Open P0/P1/P2：`0 / 0 / 0`
- Reviews：`REV-000634`～`REV-000652`
- Evidence：`EVD-000991`～`EVD-001002`

## Finding closure

### FND-P1-T12-I007-001 `[P2][SPEC][CORRECTNESS][ORACLE]` — CLOSED

- MAP canonical pair 排序后检查相邻 key ID；
- SET canonical IDs 排序后检查相邻 element ID；
- collision 在 `nodeId()`、`complete()` 和父 assignment 之前稳定拒绝；
- 两个都包含相同非法 collision 结构的外部 Map/Set 不再可能返回 true；
- Map.Entry 不误用容器 duplicate-key 门禁；
- 正常 LinkedHashMap/Set 和普通 hash collision 保持精确语义。

## Independent Review Oracle

1. Map collision 的异常类型和消息稳定；
2. Set collision 的异常类型和消息稳定；
3. FrozenMap 对同 size/hash 非法外部 Map fail-closed；
4. FrozenSet 对同 size/hash 非法外部 Set fail-closed；
5. Map.Entry 相等比较不触发 duplicate 门禁；
6. 嵌套 Map collision 保留 `map-key` 原因；
7. 普通 hash collision 不被误判为 canonical collision；
8. 标准 Map equality-equal key 折叠后的正常结构保持；
9. 空与单元素 Set/Map 保持；
10. collision operation 不污染后续独立 operation。

## Review profiles

| Review Profile | Result |
|---|---|
| SpecComplianceReviewAgent | PASSED |
| EngineeringStandardsReviewAgent | PASSED |
| PerformanceReviewAgent | PASSED |
| TestEvidenceReviewAgent | PASSED |
| ArchitectureReviewAgent | PASSED |
| MaintainabilityReviewAgent | PASSED |
| SecurityReviewAgent | NOT_APPLICABLE |

## Engineering conclusions

- duplicate scan 对已受预算约束 metadata 执行 O(n) 相邻扫描；
- Map 原 O(n log n) 排序复杂度不变；
- 不新增外部容器整体复制、递归或公共 API；
- `ConditionalCompareTask` 死代码已删除；
- 所有 `@Override` 独占一行；
- 新增方法、异常与关键逻辑均有中文注释；
- I001～I006、Publication、snapshot、iterator、operation cache、budget、Diagnostic、Clock、Deadline、Context/Result 与 commit-wins 合同保持；
- 未实现 T13/T14/T15 或 P2～P7 runtime。

## Validation

- Valid RED：`cb3f08f28807...` / Run `31000174741` / 4 expected failures / 0 errors；
- First GREEN：`2da699060a4b...` / Run `31000726214` — SUCCESS；
- Clean-code Head：`74f402287bc4968dae3221848a91d968ecad0698`；
- Clean P0：`31000986498` — SUCCESS；
- Artifact：`8928238806`；
- SHA-256：`7d0a8c38c9d93df547ced820b3bf5ebdc964307bfc1032aeb48cf10cc12f19b5`；
- I007 `16/16`；T12 `133/133`；Compiler `452/452`；正常测试 `572/572`；
- Surefire XML `100`；Errors/Skipped `0/0`；
- Java 8、12 modules、intentional failure gate：PASSED；MySQL：SKIPPED_NOT_APPLICABLE。

PR #27 未经用户明确授权不得合并；合并前 TASK-P1-T13 保持 `BLOCKED_UNTIL_PR_27_MERGE`。
