# TASK-P1-T08 / I001 — P1 强类型引用解析

- 状态：`IN_PROGRESS`
- Base：`dev_all@c6cd8ec156563480ec30989cdd358d4979a8599b`
- Dependency：`COMPLETION-P1-T07-R02@ffe544e3060d`
- Branch：`feature/p1-t08-reference-resolution-20260803-2254`
- PR：`#23`（Draft）
- Design：`DESIGN-R29@P1-T08-I001`
- Plan：`TP-P1-COMPILER-F01-R25@P1-T08-I001`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 当前 Gate

- PR #22：已合并；
- dev_all 最新基线：已确认；
- Design R29：PASSED；
- Plan R25：PASSED；
- Open P0/P1/P2：`0 / 0 / 0`；
- 下一阶段：建立可编译 TDD seam 与有效 RED。

## 冻结边界

- 精确 TypedKey 查询；
- 完整 SymbolTable 后解析，支持前向引用；
- unknown/type mismatch/owner mismatch 聚合；
- 失败不发布部分结果；
- 不实现 T09/T10/P2～P7；
- 不合并 PR #23。

## 编码规范

- Java release 8；
- `@Override` 独占一行；
- 方法、构造器与重要解析、owner、Diagnostic、资源和失败逻辑使用中文注释；
- 不引入 static mutable registry、I/O 或运行时执行。
