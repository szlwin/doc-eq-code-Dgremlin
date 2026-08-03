# TASK-P1-T06 / I002 — TDD RED Evidence

- TDD：`TDD-P1-T06-R02@895d907b1980`
- Review：`REV-000273`
- Evidence：`EVD-000515`
- Head：`895d907b1980fcaadd8fba76a90f478bb63cce3e`
- P0 Run：`30792214070`
- Artifact：`8847477189`
- Artifact SHA-256：`88fba017119149e6a3d73863c9b0061f9d85973e9450bc41c47705511163d563`
- Result：`PASSED RED`

## 有效 RED

- 生产及测试源码 Java release 8 编译成功；
- Context：26/26 PASSED；
- I001 T06 Raw：16/16 PASSED；
- I002 Rework：8 run / 8 expected failures / 0 errors；
- Compiler：107 run，其中仅 I002 8 项预期失败；
- 下游 Reactor 因受控 RED 在 compiler 模块停止，不声明通过。

## 八个预期失败

1. 14 Kind owner/name lexical token 被 trim；
2. name、attributes、body、definition/child reference 来源事实不一致；
3. public RawDefinition 未强制 Kind matrix；
4. public RawBuildResult.failed 未强制 code/severity/pass；
5. 空白 reference 未在第一阶段精确定位；
6. package-private depth limits seam 不存在；
7. package-private node-count limits seam 不存在；
8. RawDefinition.toString 未覆盖全部语义字段。

## RED 可信性

- 14 Kind 均通过合法 Grammar Canonical fixture 触发，不绕过 Builder；
- public constructor/factory 边界使用直接调用；
- depth/node 使用小型注入预算，不制造 StackOverflowError；
- toString 同时使用 equals 不同的对照对象；
- 失败不是编译错误、fixture 错误或无关模块回归。

## 透明中间记录

首次 Head `4c62700d3dde100aceb48665df4cf8556094cbc8` 的 Run `30792026502` 因 Oracle 调用了不存在的 `RawDefinitionSet.byKind` 而测试编译失败。该运行不作为 TDD RED Evidence；修正为公开 `definitions(kind)` 后重新形成上述有效 RED。
