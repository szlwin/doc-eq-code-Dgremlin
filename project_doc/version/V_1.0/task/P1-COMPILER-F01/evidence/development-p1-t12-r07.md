# EVD-000994 — DEV-P1-T12-R07

- Development：`DEV-P1-T12-R07@74f402287bc4`
- Architecture：`DEVSKEL-P1-T12-R07@cb3f08f28807`
- Status：`PASSED`

## Production changes

### ArtifactSnapshots.java

- 新增 package-private `CanonicalCollisionException extends IllegalArgumentException`；
- 稳定消息：
  - `artifact comparison canonical collision: map-key`
  - `artifact comparison canonical collision: set-element`。

### ArtifactComparisonOperation.java

- `FinishSequenceTask`：SET 排序后线性检查相邻 canonical ID，重复时在 `nodeId/complete` 前拒绝；
- `FinishPairsTask`：MAP 排序后线性检查相邻 canonical key ID，重复时在 parts/node intern 前拒绝；
- `CanonicalType.ENTRY` 不应用 duplicate-key 门禁；
- 删除无调用点的 private `ConditionalCompareTask`。

## Revision chain

- Production commit：`5c765bfbb47d35d0bfe48debe68dffc1476bd096`
- Temporary workflow removed / First GREEN Head：`2da699060a4bb596c612a7b26fa022fcb6474a4d`
- Independent Review fixture commit：`f8125c2abf80c8edd77641128e5c8df0362ccb0a`
- Fixture correction / Clean-code Head：`74f402287bc4968dae3221848a91d968ecad0698`

## Style and scope

- 生产修改仅限 `dec.core.compiler.pass`；
- 所有 `@Override` 独占一行；
- 异常、duplicate scan 与关键边界使用中文注释；
- Java release 8；
- 临时 materialization workflow 已删除，不存在于 clean-code Head；
- 未实现 T13/T14/T15 或 P2～P7 runtime。
