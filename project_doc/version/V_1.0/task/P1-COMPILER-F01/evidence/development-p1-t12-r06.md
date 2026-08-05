# DEV-P1-T12-R06 — comparison operation 资源返工 Development Evidence

- Development：`DEV-P1-T12-R06@ce8c92523256`
- Architecture：`DEVSKEL-P1-T12-R06@788f475d60e4`
- Evidence：`EVD-000975`～`EVD-000982`
- Production first GREEN Head：`91fe23a388d6fc62376222f36a291e8d00544f6a`
- Clean-code Head：`ce8c9252325642cf45e89f71aaa1f807d4916aca`

## Production changes

- 新增 `ArtifactComparisonOperation`，持有单次公开操作的预算、pair cache 与 canonical cache；
- `ArtifactComparisonSupport` 保留既有 package-private facade，调用点和 Compiler 公共 API 不变；
- 一个 `indexOf/lastIndexOf/contains/get/containsKey/containsValue/entrySet.contains` 操作内全部候选共享同一 Operation；
- identity pair 完成状态 `EQUAL/NOT_EQUAL` 跨候选复用，`VISITING` 循环稳定拒绝；
- List equality 使用双方 Iterator continuation，不调用外部 `size/get(index)`；
- List/Set/Map/Entry canonicalization 使用显式 iterator task，不整体复制外部容器；
- edge budget 在 `iterator.next()`、key/value 读取和 child 调度之前执行；
- canonical-node budget 在首次建立容器临时节点前执行；
- iterator 业务异常原样传播，不伪装为资源超限；
- comparison depth/pair/edge/canonical-node 默认值保持 I005 合同。

## Tests

- `CompilerPipelineReworkI006Test`：8 项；
- `CompilerPipelineReworkI006IndependentReviewTest`：10 项；
- 覆盖超宽/无限 Set/Map、异常 List size、非 RandomAccess、正反向 List 查询、Map key/value、Set element、EntrySet、多候选 EQUAL/NOT_EQUAL cache、业务异常和循环。

## Scope and style

- 生产修改仅限 `dec.core.compiler.pass`；
- 不扩展 Compiler 公共 API；
- 未实现 T13/T14/T15 或 P2～P7 runtime；
- 所有新增 `@Override` 独占一行；
- operation cache、pair state、iterator task、canonicalization、budget 和 failure boundary 均有中文注释；
- Java release 8 编译和全 Reactor 验证通过。
