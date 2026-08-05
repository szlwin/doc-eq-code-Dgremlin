# TASK-P1-T12 / I006 — comparison operation 资源边界返工

- Status：`COMPLETED / PASSED`
- Base：`PR27@956e51b998068b726eefc4ccfbafe12f868ca72b`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated History：`COMPLETION-P1-T12-R01@c6a515820972`、`R02@5d5a7d72119b`、`R03@4d4cd5c4c049`、`R04@923129b1f20d`、`R05@304a2156ff5e`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R43@P1-T12-REWORK-I006`
- Plan：`TP-P1-COMPILER-F01-R39@P1-T12-REWORK-I006`
- TDD：`TDD-P1-T12-R06@788f475d60e4`
- Architecture：`DEVSKEL-P1-T12-R06@788f475d60e4`
- Development：`DEV-P1-T12-R06@ce8c92523256`
- Code Review：`CODEREVIEW-P1-T12-R11@ce8c92523256`
- Testing：`TESTING-P1-T12-R06@ce8c92523256`
- Completion：`COMPLETION-P1-T12-R06@ce8c92523256`
- Reviews：`REV-000611`～`REV-000633`
- Evidence：`EVD-000966`～`EVD-000990`
- Open P0/P1/P2：`0 / 0 / 0`

## Findings closed

- `FND-P1-T12-I006-001` `[P1][RESOURCE]` CLOSED：外部 Set/Map/List 使用 iterator 增量遍历，预算在 `next()`、key/value 读取和临时 metadata 创建前生效；
- `FND-P1-T12-I006-002` `[P1][MEMO]` CLOSED：单次公开查询共享一个 `ComparisonOperation`，EQUAL/NOT_EQUAL pair 和 canonical metadata 跨候选复用；
- `FND-P1-T12-I006-003` `[P1][RESOURCE]` CLOSED：任意普通 List 使用 Iterator continuation，不调用 `size()+get(index)`；
- `FND-P1-T12-I006-004` `[P2][ORACLE]` CLOSED：新增 18 项超宽/无限 iterator、异常 size、非 RandomAccess、多候选 cache 与精确预算测试。

## Delivered contract

- 一个公开 equality/query 操作只创建一个 Operation；
- pair 状态为 `VISITING/EQUAL/NOT_EQUAL`，完成状态跨候选复用；
- List equality 不依赖 RandomAccess；
- canonical List/Set/Map/Entry 不整体复制外部容器、不信任外部 size；
- 无限 iterator 在第 maxEdges+1 次读取前稳定拒绝；
- iterator 业务异常原样传播；
- comparison limits 保持 depth=256、pairs=16384、edges=131072、canonical nodes=16384；
- I001～I005、Publication、Diagnostic、Clock、Deadline、Context/Result 合同保持；
- Java 8、所有 `@Override` 独占一行、方法和重要逻辑使用中文注释；
- 未实现 T13/T14/T15 或 P2～P7 runtime。

## Validation

- Valid RED：`788f475d60e4864fc6c11bfffee3ff925aa757ac` / Run `30991106416` / 7 failures / 0 errors；
- First GREEN：`91fe23a388d6fc62376222f36a291e8d00544f6a` / Run `30992157198` — SUCCESS；
- Clean-code Head：`ce8c9252325642cf45e89f71aaa1f807d4916aca`；
- P0 Run：`30992489987` — SUCCESS；
- Artifact：`8924724966`；
- SHA-256：`f0d5b9ce6c44a922b9bdd534c82f0e235912588f97ced16c117d9b57774a54a4`；
- Surefire XML 98；I006 18/18；T12 117/117；Compiler 436/436；正常测试 556/556；
- Errors/Skipped 0/0；12 modules、Java 8、intentional failure gate：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

PR #27 未经用户明确授权不得合并；TASK-P1-T13 保持 `BLOCKED_UNTIL_PR_27_MERGE`。
