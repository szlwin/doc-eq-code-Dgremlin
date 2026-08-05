# TP-P1-COMPILER-F01-R39 — TASK-P1-T12 I006 comparison operation 资源返工计划

- Revision：`TP-P1-COMPILER-F01-R39@P1-T12-REWORK-I006`
- Design：`DESIGN-R43@P1-T12-REWORK-I006`
- Status：`PASSED`
- Base：`PR27@956e51b998068b726eefc4ccfbafe12f868ca72b`
- Invalidated Completion：`COMPLETION-P1-T12-R05@304a2156ff5e`

## Sequential workflow

1. 修复并验证 `common-develop-v2.44-rc8` 基线标签，保留已有 Skill worktree drift，不 reset/clean。
2. 将 PR #27 转为 Draft，将 R05 标记为失效历史，保持 T13 阻断。
3. 保留 R38～R42、R34～R38、I001～I005 的全部事实，不删除、不覆盖。
4. 冻结 R43/R39，记录 first commit/blob，且必须早于 I006 有效 RED。
5. 新增 I006 Task、R09/R05 invalidation 与 Review 输入，Open P0/P1/P2=`0/3/1`。
6. 新增可在 I005 Head 编译的资源 Oracle；使用有限计数 iterator 和快速失败保护，避免 RED 自身 OOM 或长时间运行。
7. Architecture Review 冻结 operation-level pair result cache、iterator continuation tasks、增量 canonicalization 与预算前置顺序。
8. 重构 `ArtifactComparisonSupport`：每次公开操作只创建一个 `ComparisonOperation`，候选之间共享 pair/canonical cache 与预算。
9. List equality 改为 Iterator 驱动，不调用外部 `size/get(index)`；canonical List/Set/Map 改为增量 iterator task，禁止整体复制和外部 size 预分配。
10. pair cache 使用 `VISITING/EQUAL/NOT_EQUAL`，根结论独立、已完成子 pair 跨候选复用；循环稳定拒绝。
11. Set/Map 继续使用同一 Operation 的 canonical IDs，临时索引在预算通过后逐项增长。
12. 首轮 GREEN 后执行独立 Review，补充异常 size、LinkedList、自定义非 RandomAccess List、无限 iterator、宽 Set/Map、多候选 EQUAL/NOT_EQUAL cache 与预算组合 Oracle。
13. 运行 I006 定向、T12 全量、Compiler 全量、12 模块 `clean verify`、Java 8 与故意失败门禁。
14. 下载 Artifact，独立计算 SHA-256 并解析全部 Surefire XML。
15. 形成 clean-code Head；其后只写 I006 Evidence、Review、Revision Lock、Completion、handoff/resume。
16. 对 final documented Head 再运行 P0 和 Artifact 校验。
17. 更新同一 PR #27 并标记 Ready for Review；不创建替代 PR、不自动合并，T13 保持阻断。

## Acceptance gates

- 外部 Set/Map 不使用整体复制构造器；
- 外部 List 的异常或超大 size 不被调用或用于预分配；
- Iterator 在下一元素读取前先通过 edge budget；
- LinkedList 与非 RandomAccess List 的实际访问量线性；
- 单次 contains/indexOf/get/containsKey/containsValue/entrySet.contains 的全部候选共享一个 Operation；
- 相同 EQUAL/NOT_EQUAL 子 pair 跨候选只执行一次；
- 无限 iterator 在预算边界稳定抛 `ComparisonLimitException`；
- 临时 canonical IDs/pairs 数量受 edge/node budget 约束；
- I001～I005 与 T01～T11 全部不回归；
- Open P0/P1/P2=`0/0/0`；
- 所有 `@Override` 独占一行，方法与重要逻辑使用中文注释；
- Java 8、全 Reactor、故意失败门禁和 Artifact Evidence 通过。

## Validation

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineReworkI006Test test

./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineReworkI005Test,CompilerPipelineReworkI005IndependentReviewTest,CompilerPipelineReworkI006Test test

./mvnw -pl dec-core-compiler -am test
./mvnw clean verify
```
