# TP-P1-COMPILER-F01-R34 — TASK-P1-T12 implementation plan

- Revision：`TP-P1-COMPILER-F01-R34@P1-T12-I001`
- Design：`DESIGN-R38@P1-T12-I001`
- Status：`PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`

## Sequential workflow

1. 验证 PR #26 已合并，`dev_all` 精确包含 `COMPLETION-P1-T11-R02@86b55b45d1cd`。
2. 从最新 `dev_all` 创建 T12 独立分支，不复用 T11 分支。
3. 冻结 R38/R34，并记录首次提交和 blob；Revision 必须早于有效 RED。
4. 建立 `CompilerPass`、`PassContext`、`PassResult`、`CompilationSession`、`CompilerPipeline` 和结果类型的可编译架构骨架。
5. 新增 `CompilerPipelineOrderTest`、`CompilationSessionStateTest`、`SessionIsolationTest`，运行有效 RED；errors 必须为 0，失败仅来自未实现 Pipeline 行为。
6. Architecture Review 验证固定十 Pass、唯一状态路径、Session-local 数据、终态、取消/超时和 Publication 阻断接缝。
7. 实现固定顺序验证、状态推进、Diagnostic 聚合、artifact/timing/transition 快照、cancel/timeout 和 fail-closed RuntimeException 边界。
8. 首轮 GREEN 后执行独立 Review，补充 reordered/missing/duplicate/null Pass、调用方 List 修改、终态重入、异常边界和无全局状态 Oracle。
9. 运行 T12 定向测试、T11/T10/T09/T08/T07 回归、Compiler 模块全量、12 模块 `clean verify`、Java 8 与故意失败门禁。
10. 下载 Artifact，独立计算 ZIP SHA-256 并解析全部 Surefire XML。
11. 形成 clean-code Head；其后只写 `project_doc` Evidence、Review、Revision Lock、Completion、resume/handoff。
12. 对 final documented Head 再运行 P0 和 Artifact 校验。
13. 创建新的 PR 指向 `dev_all`，更新完整说明，不自动合并；PR 合并前 T13 保持阻断。

## Acceptance gates

- 固定十 Pass 精确按 R38 顺序运行；
- 成功状态路径精确且无平行状态；
- 任一 Pass ERROR、RuntimeException、cancel 或 timeout 进入 FAILED；
- 失败位置之后的 Pass 调用数全部为 0，PublicationPass 尤其必须为 0；
- PUBLISHED/FAILED 终态不能继续转换；
- 两次 execute 创建不同 Session，所有 artifact、Diagnostic、timing、transition 隔离；
- 所有输入/输出集合防御性复制且不可变；
- Open P0/P1/P2=`0/0/0`；
- `@Override` 独占一行，公开方法和重要逻辑使用中文注释；
- Java 8、全 Reactor、故意失败门禁与 Artifact Evidence 通过。

## Validation

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=CompilerPipelineOrderTest,CompilationSessionStateTest,SessionIsolationTest test

./mvnw -pl dec-core-compiler -am \
  -Dtest=DeferredI002ReworkTest,ModelAccessSelectorTest,InformationOwnershipTest,ReferenceResolverContractTest,SymbolRegistrationTest test

./mvnw -pl dec-core-compiler -am test
./mvnw clean verify
```
