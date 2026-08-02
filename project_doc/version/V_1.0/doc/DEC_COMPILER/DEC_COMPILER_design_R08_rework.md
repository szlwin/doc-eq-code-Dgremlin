# DEC_COMPILER 详细设计 R08：Projection 派生视图写入拒绝返工

> Revision：`DESIGN-R08@P1-T01-REWORK-I010`  
> 前置：`BM-R05@4ecb1f8c09f4`、`DESIGN-R05@0b37a9b4dd48`、`DESIGN-R06@P1-T01-REWORK-I008`、`DESIGN-R07@P1-T01-REWORK-I009`  
> 适用任务：`TASK-P1-T01` REWORK iteration I010

## 1. 返工目标

本 Revision 关闭 PR #16 最新独立 Review 发现的一个 P1 Blocker：`CoreConfigProjection` 根 List 已统一拒绝写入，但 `subList()` 返回的 `AbstractList.SubList` 仍允许空子列表上的部分写方法无操作成功。

I008、I009 已关闭的 Finding 必须保持回归通过。本轮不实现 T02/T03，不改变 `CompiledModelSet`、Typed Registry、EngineContext 或 SourceManifest 的既有合同。

## 2. 派生 List 视图合同

`data()`、`views()`、`rules()` 返回的 List 以及由这些 List 继续派生的所有公共视图，都属于 Projection 写入合同的一部分。任何调用方可见的写入尝试都必须明确失败，不能因为集合为空、迭代器状态无效或默认实现短路而正常返回。

内部 `ProjectionReadOnlyList` 必须覆盖：

```java
@Override
public List<E> subList(int fromIndex, int toIndex)
```

实现要求：

1. 先通过底层稳定快照的 `subList(fromIndex, toIndex)` 保留 Java List 的索引范围校验；
2. 再创建新的 `ProjectionReadOnlyList` 防御性快照，不能直接暴露 `AbstractList.SubList`；
3. 子列表及嵌套子列表继续对 `add/set/remove/clear/addAll/removeAll/retainAll/removeIf/replaceAll/sort` 抛出 `ProjectionWriteRejectedException`；
4. 异常继续携带 `DiagnosticCode.MIX_PROJECTION_WRITE`；
5. 子列表拒绝后，根 Projection、来源 `CompiledModelSet` 与所有快照的对象和值均保持不变。

## 3. Iterator 与 ListIterator 防御性闭包

为避免同一不变量继续通过派生遍历视图绕过，`ProjectionReadOnlyList` 还必须提供受控 Iterator/ListIterator：

- `iterator().remove()` 始终抛出 `ProjectionWriteRejectedException`；
- `listIterator().remove()`、`set()`、`add()` 始终抛出同一专用异常；
- 读取、前后移动和索引查询委托给不可变快照；
- 即使列表为空或尚未调用 `next()`，写方法也不能先抛出普通 `IllegalStateException`。

该要求不改变标准读取语义，只冻结所有公开派生写入口的稳定失败语义。

## 4. 测试 Oracle

新增 R04 合同测试，至少覆盖：

- 空 `subList(0, 0)` 的 `clear/removeAll/retainAll/removeIf/replaceAll/sort` 全部拒绝；
- 非空子列表的 `clear/remove/set/add` 全部拒绝；
- 嵌套空子列表仍拒绝写入；
- 根 List 与子列表的 Iterator/ListIterator 写入口均拒绝；
- 所有异常均为 `ProjectionWriteRejectedException`，错误码为 `MIX_PROJECTION_WRITE`；
- 写入失败前后模型、根列表与子列表内容不变；
- I008、I009 的现有 21 项 Context 测试继续通过。

## 5. 编码约束

- 所有新增或修改的 `@Override` 注解必须独占一行；
- 新增方法和关键逻辑必须使用中文注释；
- 保持 Java 8 兼容；
- 不引入可成功修改 Projection 的路径；
- 原 R03 Completion、Review 和 Evidence 保留为被本次 Review 推翻的历史记录。
