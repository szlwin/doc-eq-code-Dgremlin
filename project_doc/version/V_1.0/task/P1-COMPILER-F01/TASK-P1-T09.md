# TASK-P1-T09 / I002 — canonical common、输入快照与 depth 返工

- 状态：`DESIGN / PASSED`
- Base：`dev_all@e47551e0c79984d8f3fafc0ce379da76ad0d5593`
- Rework Base：`19b14487646c66ab1d7a386e96fc4876581b214c`
- Dependency：`COMPLETION-P1-T08-R02@bab0993ecfd8`
- Branch：`feature/p1-t09-engine-context-20260804-1040`
- PR：`#24`（Draft）
- Design：`DESIGN-R32@P1-T09-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R28@P1-T09-REWORK-I002`
- Invalidated Completion：`COMPLETION-P1-T09-R01@ecfe3f53bde7`（不可变历史保留）
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Review Input：Independent Review `NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 3 / 1`

## Findings

- `FND-P1-T09-I002-001`：common 权限与限制必须统一使用 canonical `SystemKey("common")`；raw lexical 只保留来源事实。
- `FND-P1-T09-I002-002`：在任何 semantic work 前校验 RawDefinitionSet 与 SymbolTable 完整快照一致性。
- `FND-P1-T09-I002-003`：R27 原 first-commit SHA 不存在，必须新增可验证更正 Evidence，不覆盖 R01 历史。
- `FND-P1-T09-I002-004`：128 层括号必须通过，129 层必须稳定返回 limit Diagnostic。

## Acceptance

- `AC-P1-T09-I002-001`：padded common 获得 common 跨 System 权限，同时执行所有 common Information/System/ModelAccess 限制，RawDefinition lexical 不被改写。
- `AC-P1-T09-I002-002`：任何 Raw/Symbol 快照失配只返回 `information.input.snapshot-mismatch`，parser/resolver 不执行且无部分输出。
- `AC-P1-T09-I002-003`：R27 更正 Evidence 使用 GitHub 可读取的真实 first commit；若无法恢复，则使用可验证 pre-RED checkpoint 并明确降级，不再声明无效 SHA 有效。
- `AC-P1-T09-I002-004`：parser 128/129 相邻边界与 R32 完全一致。

## Scope

- Production：`dec-core-compiler/src/main/java/dec/core/compiler/information/**`
- Additive read-only predicate：`dec-core-compiler/src/main/java/dec/core/compiler/symbol/SymbolTable.java`
- Test：`dec-core-compiler/src/test/java/dec/core/compiler/information/**`
- Documents：T09 I002 Design/Plan/Evidence/Review/Completion/resume/handoff/checkpoint

## Gate

- R32/R28 已在新测试前冻结；
- 下一阶段：TDD RED；
- 有效 RED 必须 Java 8 编译、errors=0、I001/T08/T07 与既有 Compiler 回归通过；
- Architecture Skeleton 双 Review 通过前不得进入具体实现；
- R01 Completion、Review、Evidence 和失败 attempt 不得覆盖或删除；
- 未经用户明确授权不得合并 PR #24；
- PR #24 合并前 `TASK-P1-T10` 保持 `BLOCKED_UNTIL_PR_MERGE`。

## Coding

- `@Override` 注解独占一行；
- 方法、构造器及重要 identity、snapshot、parser、Diagnostic、资源和失败逻辑使用中文注释；
- 不求值、不建 DAG、不检测循环、不缓存、不引入 I/O、网络、模糊查询或全局状态。
