# TASK-P1-T09 / I001 — System-owned Information 与 common 表达式绑定

- 状态：`DESIGN / PASSED`
- Base：`dev_all@e47551e0c79984d8f3fafc0ce379da76ad0d5593`
- Dependency：`COMPLETION-P1-T08-R02@bab0993ecfd8`
- Branch：`feature/p1-t09-engine-context-20260804-1040`
- PR：`#24`（Draft）
- Design：`DESIGN-R31@P1-T09-I001`
- Plan：`TP-P1-COMPILER-F01-R27@P1-T09-I001`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Owner：`DevelopAgent`
- Reviewers：`PlanReviewAgent`、`ArchitectureReviewAgent`、`TestDesignAgent`、`DevelopAgent`

## Goal

将 Information 归属、expression AST、限定引用和 common 跨 System 规则编译为强类型 P3 Deferred；不执行表达式求值。

## Acceptance

- `AC-P1-T09-001`：`common.paySuccess` 与 `common.payError` 精确绑定四个 system-qualified `InformationKey` 依赖事实并生成 P3 Deferred；
- `AC-P1-T09-002`：未限定引用、非法 owner、普通 System 跨 System 引用和 common 非 expression/非法成员失败且不发布部分结果。

## Scope

- Production：`dec-core-compiler/src/main/java/dec/core/compiler/information/**`
- Test：`dec-core-compiler/src/test/java/dec/core/compiler/information/**`
- Fixture：当前固定 `systems.xml` 只读验证；无要求差异不修改。

## Gate

- R31/R27 已冻结；
- 下一阶段：TDD RED；
- 有效 RED 必须 Java 8 编译、errors=0、既有回归通过；
- Architecture Skeleton 双 Review 通过前不得进入具体实现；
- 未经用户明确授权不得合并 PR #24；
- PR #24 合并前 TASK-P1-T10 保持阻断。

## Coding

- `@Override` 注解独占一行；
- 方法、构造器及重要 parser、owner、common、Diagnostic、资源与失败逻辑使用中文注释；
- 不求值、不建 DAG、不检测循环、不缓存、不引入全局状态。
