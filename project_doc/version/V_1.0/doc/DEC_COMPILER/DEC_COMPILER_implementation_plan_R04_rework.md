# DEC_COMPILER 实施计划 R04：TASK-P1-T01 I010

> Revision：`TP-P1-COMPILER-F01-R04@P1-T01-REWORK-I010`  
> 输入设计：`DESIGN-R08@P1-T01-REWORK-I010`  
> 目标 PR：`#16`

## 1. 执行顺序

1. 将 `TASK-P1-T01` 重开为 REWORK iteration I010，并保留 R03 历史；
2. 新增 R04 TDD，形成只由派生 List 视图合同缺失导致的有效 RED；
3. 冻结 `subList` 与受控 Iterator/ListIterator 的架构骨架；
4. 实现派生视图稳定写入拒绝；
5. 执行 Spec、Architecture、Engineering、TDD 与 Evidence 独立 Review；
6. 执行完整 P0 Build Gate、Testing 和 Completion Verification；
7. 写入 Evidence、handoff、resume context 和机器恢复入口；
8. 最终文档化 Head 再次通过 P0 后，将 PR #16 恢复为 Ready for review。

## 2. TDD RED 要求

测试源和生产源必须以 Java 8 正常编译。RED 只能来自：

- 空 `subList` 写操作未抛出专用异常；
- 派生 Iterator/ListIterator 写操作未抛出专用异常。

既有 21 项 Context 测试必须继续全绿。不得接受编译错误、依赖错误、测试未选中或环境错误作为 RED。

## 3. 架构骨架

骨架阶段允许新增以下结构，但未完成行为必须显式失败：

- `ProjectionReadOnlyList.subList(...)`；
- 内部受控 `ProjectionReadOnlyListIterator`；
- 统一生成派生写入异常的辅助逻辑。

不得返回可变视图、普通 `Collections.unmodifiableList` 或原始 `AbstractList.SubList`。

## 4. Development

实现应满足：

- `subList` 返回防御性快照包装；
- 嵌套子列表继续使用同一包装；
- Iterator/ListIterator 所有写方法统一抛出 `ProjectionWriteRejectedException`；
- 异常 Diagnostic code 固定为 `MIX_PROJECTION_WRITE`；
- 所有 `@Override` 独占一行；
- 新增和修改的方法、重要逻辑使用中文注释。

## 5. 验证范围

- R04 新增合同测试；
- R03 Projection 与 dependency 测试；
- R02 发布模型与身份测试；
- 全部 `dec-core-context` 测试；
- 完整 11 模块 Maven Reactor；
- Java release 8；
- 故意失败测试阻断构建；
- MySQL 按 T01 无数据库变更判定为不适用。

## 6. 完成门禁

只有在以下条件全部满足时，才能将 I010 标记为完成：

- 新 Finding CLOSED；
- I008、I009 Findings 回归保持 CLOSED；
- 开放 P0/P1 为 0；
- 独立 Review 全部 PASSED；
- 干净代码 Head 与最终文档化 Head 均通过 P0；
- PR #16 无未解决 Review 线程；
- T02/T03 未被本轮提前启动或声明完成。
