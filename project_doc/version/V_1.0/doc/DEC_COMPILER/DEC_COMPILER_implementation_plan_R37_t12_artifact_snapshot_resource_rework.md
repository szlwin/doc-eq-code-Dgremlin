# TP-P1-COMPILER-F01-R37 — TASK-P1-T12 I004 Artifact Snapshot 资源返工计划

- Revision：`TP-P1-COMPILER-F01-R37@P1-T12-REWORK-I004`
- Design：`DESIGN-R41@P1-T12-REWORK-I004`
- Status：`PASSED`
- Base：`PR27@cf6e7dbe18d2f172dc4c68c793f45d9ecfbabe9d`
- Invalidated Completion：`COMPLETION-P1-T12-R03@4d4cd5c4c049`

## Sequential workflow

1. 将 PR #27 转为 Draft，将 R03 标记为失效历史，并保持 T13 阻断。
2. 保留 R38～R40、R34～R36、I001～I003 的 RED、Architecture、Review、Completion、CI 与 Artifact，不删除、不覆盖。
3. 冻结 R41/R37，记录 first commit/blob，且必须早于 I004 有效 RED。
4. 新增 I004 Task、Review invalidation 与 Review 输入，Open P0/P1/P2=`0/1/1`。
5. 新增可在 I003 Head 编译的资源 Oracle，确认仅命中递归栈、共享 DAG、预算和复杂度缺口。
6. Architecture Review 冻结 explicit traversal stack、VISITING/FROZEN identity memoization、默认 budgets 与稳定 Diagnostic。
7. 将 `ArtifactSnapshots` 改为非递归 DFS，已冻结共享子图直接复用同一 immutable snapshot。
8. 增加 max depth、unique containers、traversed edges、map entries 四类预算，并在物化前计数。
9. 增加内部专用 resource-limit exception，由 `CompilerPipeline` 映射为 `pipeline.artifact.resource-exceeded`，publisher=0。
10. 保留循环、未知可变对象、null、Map/Set collision 与不可变输出合同。
11. 使用 package-private small limits 和 counting containers 验证边界与线性操作数，不形成公共 API。
12. 首轮 GREEN 后执行独立 Review，补充 Optional、Map key/value shared identity、预算组合、异常源容器和 Result identity Oracle。
13. 运行 I004 定向、T12 全量、Compiler 全量、12 模块 `clean verify`、Java 8 与故意失败门禁。
14. 下载 Artifact，独立计算 SHA-256 并解析全部 Surefire XML。
15. 形成 clean-code Head；其后只写 I004 Evidence、Review、Revision Lock、Completion、handoff/resume。
16. 对 final documented Head 再运行 P0 和 Artifact 校验。
17. 更新同一 PR #27 并标记 Ready for Review；不创建替代 PR、不自动合并，T13 保持阻断。

## Acceptance gates

- 深度等于预算成功，超过预算稳定失败；
- 24 层共享 DAG 不指数展开；
- 同一共享子图输出 identity 复用；
- counting container 操作数与唯一图规模线性相关；
- List/Set/Map/unique-container 预算超限均稳定失败；
- 资源超限结果为 FAILED、`pipeline.artifact.resource-exceeded`、publisher=0；
- 无 `StackOverflowError` 或 `OutOfMemoryError` 越过 Pipeline；
- 循环、collision、未知可变对象与 I001～I003 原 Oracle 不回归；
- Open P0/P1/P2=`0/0/0`；
- 所有 `@Override` 独占一行，方法与重要逻辑使用中文注释；
- Java 8、全 Reactor、故意失败门禁和 Artifact Evidence 通过。

## Validation

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineReworkI004Test test

./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineOrderTest,CompilationSessionStateTest,SessionIsolationTest,CompilerPipelineIndependentReviewTest,CompilerPipelineReworkI002Test,CompilerPipelineReworkI002IndependentReviewTest,CompilerPipelineReworkI002HardeningTest,CompilerPipelineReworkI003Test,CompilerPipelineReworkI003IndependentReviewTest,CompilerPipelineReworkI004Test test

./mvnw -pl dec-core-compiler -am test
./mvnw clean verify
```
