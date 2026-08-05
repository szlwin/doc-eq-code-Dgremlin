# TASK-P1-T12 / I006 Rework Input

- Evidence：`EVD-000966`
- Source Revision：`PR27@956e51b998068b726eefc4ccfbafe12f868ca72b`
- Review：`CODEREVIEW-P1-T12-R10`
- Design：`DESIGN-R43@P1-T12-REWORK-I006`
- Plan：`TP-P1-COMPILER-F01-R39@P1-T12-REWORK-I006`
- Skill Baseline：`common-develop-v2.44-rc8@4787876e135d347e9f37580910e2d28b09ea2ba4`
- Skill Guard：`DIRTY / HEAD_MATCHES_BASELINE / CRITICAL_FILE_DRIFT=0`

## Confirmed source facts

1. `indexOf/findEntryByKey/containsValue/containsEntry` 为每个候选创建新的 ComparisonSession；
2. List equality 使用 `size()+get(index)`；
3. canonical List 使用外部 size 预分配；
4. canonical Set/Map 使用整体复制构造器；
5. edge budget 在上述外部物化之后才执行；
6. 当前 CI 成功但没有覆盖资源反例。

## Required closure

- Operation-level EQUAL/NOT_EQUAL/VISITING pair cache；
- 单次 query canonicalization 与跨候选共享 metadata；
- Iterator-driven List pair traversal；
- Iterator-driven List/Set/Map canonical traversal；
- element read/save/push 前预算检查；
- 宽/无限 iterator、异常 size、LinkedList、多候选共享 DAG Oracle；
- I001～I005 全部回归验证。

## Boundaries

只允许修改 `dec.core.compiler.pass`、对应测试与 `project_doc` 事实文件；不实现 T13/T14/T15，不合并 PR #27。
