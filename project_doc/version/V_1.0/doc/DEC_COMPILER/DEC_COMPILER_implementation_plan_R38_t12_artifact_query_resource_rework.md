# TP-P1-COMPILER-F01-R38 — TASK-P1-T12 I005 Equality/Query 资源返工计划

- Revision：`TP-P1-COMPILER-F01-R38@P1-T12-REWORK-I005`
- Design：`DESIGN-R42@P1-T12-REWORK-I005`
- Status：`PASSED`
- Base：`PR27@2e113984973232d2d9a1d35bb886f73488f539c8`
- Invalidated Completion：`COMPLETION-P1-T12-R04@923129b1f20d`

## Sequential workflow

1. 将 PR #27 转为 Draft，将 R04 标记为失效历史，并保持 T13 阻断。
2. 保留 R38～R41、R34～R37、I001～I004 的全部事实，不删除、不覆盖。
3. 冻结 R42/R38，记录 first commit/blob，且必须早于 I005 有效 RED。
4. 新增 I005 Task、R04 invalidation 与 Review 输入，Open P0/P1/P2=`0/1/1`。
5. 新增可在 I004 Head 编译的 equality/query 资源 Oracle，使用 guarded leaf 和反射边界验证，避免 RED 自身耗尽 CPU。
6. Architecture Review 冻结显式 pair stack、identity-pair memo、共享 canonicalization、四类 comparison budgets 与稳定异常。
7. 在 `ArtifactSnapshots` 内增加 package-private `ComparisonLimits`、`ComparisonLimitException` 和受控比较入口。
8. `FrozenList` 覆盖 equals/contains/indexOf/lastIndexOf；`FrozenSet` 覆盖 equals/contains；`FrozenMap` 覆盖 equals/get/containsKey/containsValue；新增受控 FrozenEntry 与 EntrySet 查询。
9. List/Optional 使用显式 pair traversal；Set/Map 使用同一比较 Session 的 canonical child IDs 完成无序精确匹配。
10. 内部 Frozen 容器仅将缓存 hash 用作快速拒绝，hash 相同仍执行精确比较。
11. 对普通外部 List/Set/Map/Optional 查询值执行非递归读取，不调用其容器 equals/hashCode。
12. 首轮 GREEN 后执行独立 Review，补充 hash collision、普通容器对称性、传递性、Entry、containsValue、预算组合与循环 query Oracle。
13. 运行 I005 定向、T12 全量、Compiler 全量、12 模块 `clean verify`、Java 8 与故意失败门禁。
14. 下载 Artifact，独立计算 SHA-256 并解析全部 Surefire XML。
15. 形成 clean-code Head；其后只写 I005 Evidence、Review、Revision Lock、Completion、handoff/resume。
16. 对 final documented Head 再运行 P0 和 Artifact 校验。
17. 更新同一 PR #27 并标记 Ready for Review；不创建替代 PR、不自动合并，T13 保持阻断。

## Acceptance gates

- 独立 24 层共享 DAG 的 List equals、Set contains/equals、Map get/containsKey/equals 不指数展开；
- leaf equals 调用与唯一图规模近线性；
- 同一 identity pair 只比较一次；
- comparison depth/pair/edge/canonical-node 边界精确；
- 超限稳定抛出 `ComparisonLimitException`，不出现 JVM Error；
- hash 相同但结构不同返回 false；
- Frozen 与普通 Java List/Set/Map 的对称性和 hash 合同通过；
- Entry 与 EntrySet 查询受控；
- I001～I004 与 T01～T11 全部不回归；
- Open P0/P1/P2=`0/0/0`；
- 所有 `@Override` 独占一行，方法与重要逻辑使用中文注释；
- Java 8、全 Reactor、故意失败门禁和 Artifact Evidence 通过。

## Validation

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineReworkI005Test test

./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineReworkI004Test,CompilerPipelineReworkI004IndependentReviewTest,CompilerPipelineReworkI005Test test

./mvnw -pl dec-core-compiler -am test
./mvnw clean verify
```
