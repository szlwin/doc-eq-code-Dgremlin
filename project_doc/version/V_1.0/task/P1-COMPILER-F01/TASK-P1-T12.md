# TASK-P1-T12 / I001 — 十阶段 Compiler Pipeline 与 Session 状态机

- Status：`IN_PROGRESS / TDD_RED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- Design：`DESIGN-R38@P1-T12-I001`
- Plan：`TP-P1-COMPILER-F01-R34@P1-T12-I001`
- Open P0/P1/P2：`0 / 0 / 0`

## Goal

交付固定十阶段 `CompilerPipeline` 和 Session 状态机，使合法输入沿唯一状态路径进入 `PUBLISHED`；任一 ERROR、RuntimeException、cancel 或 timeout 稳定进入 `FAILED`，并停止后续 Pass，尤其不得执行 `PublicationPass`。

## Allowed scope

- `dec-core-compiler/src/main/java/dec/core/compiler/pass/**`
- `dec-core-compiler/src/test/java/dec/core/compiler/pass/**`
- T12 对应的 Design、Plan、Task、Review、Evidence、Completion 与恢复文件

## Frozen pass order

1. SourceGraphValidationPass
2. StructuralValidationPass
3. SymbolRegistrationPass
4. ReferenceResolutionPass
5. InformationOwnershipPass
6. ModelAccessBindingPass
7. DeferredClassificationPass
8. P1SemanticValidationPass
9. DigestPass
10. PublicationPass

## RED Oracle

- `CompilerPipelineOrderTest`
- `CompilationSessionStateTest`
- `SessionIsolationTest`

当前架构 checkpoint 已提供可编译类型和稳定失败结果，但尚未执行任何 Pass。有效 RED 必须仅由目标 Pipeline 行为未实现导致，errors=0，T01～T11 回归保持绿色。

## Stop conditions

- PR #26 或 T11 Completion 未进入最新 dev_all；
- R38/R34 未早于 RED 冻结；
- 修改 T01～T11 公共合同；
- 实现 T13/T14/T15 范围；
- 未关闭 Open P0/P1；
- 未完成最终 P0、Artifact、Revision Integrity 和独立 Review；
- 用户未授权时合并后续 PR 或启动 T13。
