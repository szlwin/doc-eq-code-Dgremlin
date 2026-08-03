# TASK-P1-T07 / I001 — TypedKey 与两遍 Symbol 注册

- 状态：`IN_PROGRESS`
- Branch：`feature/p1-t07-symbol-table-20260803-1958`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Dependency：`COMPLETION-P1-T06-R04@242db638c61d`
- Design：`DESIGN-R27@P1-T07-I001`
- Plan：`TP-P1-COMPILER-F01-R23@P1-T07-I001`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Target PR：待创建，目标 `dev_all`

## 目标

复用 Context 已发布的 11 类 TypedKey，将 T06 `RawDefinitionSet` 通过两遍扫描转换为稳定有序、只读、无覆盖的 `SymbolTable`，为 T08 前向引用解析准备完整符号集合。

## 范围

允许修改：

- `dec-core-compiler/src/main/java/dec/core/compiler/symbol/**`
- `dec-core-compiler/src/test/java/dec/core/compiler/symbol/**`
- T07 对应的 Design、Plan、Review、Evidence、Completion 和恢复事实

禁止修改 Context 与 Raw 生产合同；禁止解析 RawReference；禁止启动 ReferenceResolver、Information、ModelAccess、Deferred、Pipeline、Digest、Publication 或 `TASK-P1-T08`。

## 当前 Gate

- 最新 `dev_all`：`3e0492b0319173c87abff6952d4dad0f5507c31c`
- PR #21 / T06：已合并
- T06 Completion：`COMPLETION-P1-T06-R04@242db638c61d`
- Design R27：PASSED
- Plan R23：PASSED
- 开放 P0/P1/P2：`0 / 0 / 0`
- 下一阶段：TDD RED

## 编码规则

- Java release 8
- 所有新增和修改的 `@Override` 注解独占一行
- 方法、构造器及重要注册、owner、重复、资源和失败逻辑使用中文注释
- 不使用 static mutable Registry 或全局 Session 状态

## 后续门禁

只有有效 RED、Architecture Skeleton 独立 Review、GREEN、全量测试、Artifact 校验、独立 Review 和 R01 Completion 全部通过后，才允许创建最终 PR。PR 创建后保持未合并；`TASK-P1-T08` 继续阻断。
