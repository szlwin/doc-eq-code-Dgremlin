# TASK-P1-T10 Development Evidence R01

- Revision：`DEV-P1-T10-R01@9e94bc68d9a8`
- Base：`dev_all@4fe0f6def8581e5c7234d86dfa0aafae794db15f`
- Dependency：`COMPLETION-P1-T09-R02@95b08223083f`
- Design：`DESIGN-R33@P1-T10-I001`
- Plan：`TP-P1-COMPILER-F01-R29@P1-T10-I001`
- TDD：`TDD-P1-T10-R01@f1ff4c03ece8`
- Architecture：`DEVSKEL-P1-T10-R01@6db11965ec79`
- Concrete implementation：`60370e9e9e289db8b223cd0d9f38d322ff9a1052`
- Independent review fixes：`6a4c6aaead711c199181115b3c6bb8e615cbdbaa`
- Clean-code Head：`9e94bc68d9a8c25351213bb46a6cafa5702105d9`
- Status：`PASSED`

## Implemented Contract

- 完整 RawDefinitionSet 与 SymbolTable 快照门禁先于全部 selector 工作；
- shared source path 与当前 System View selector 严格分离；
- target-main 区分大小写完整匹配优先；
- 未命中后只在当前 System 已声明的目标 View property 树中逐段精确解析；
- 未声明 View、未知 source View、缺失、歧义、非复合中间段、非法 lexical 均 fail-closed；
- 完全重复 Binding 与不同 SourceRef 的语义重复均拒绝；
- WRITE 相同、祖先、后代和通配 `*` 重叠均阻断；
- 成功结果生成不可变 Binding 与 `RequiredStage.P2`、`DeferredKind.MODEL_ACCESS` Deferred；
- 任一 ERROR 不发布部分 Compilation。

## Independent Review Closure

- `FND-P1-T10-I001-001`：真实 Canonical `view-ref@ref` 未被首轮实现识别；已改为优先读取 `ref`，兼容 focused fixture 的 `name`，真实 Canonical → T06 → T07 → T10 回归通过。
- `FND-P1-T10-I001-002`：`ModelAccessBinding.compareTo/toString` 未包含 SourceRef，与完整 equals/hashCode 值语义不一致；已补齐 SourceRef 排序与字符串表示。
- 通配 WRITE、跨 View 禁止回退、不同来源语义重复、非法 path 和无运行时状态测试全部通过。

## Validation

- Review-fix P0：Run `30888668771` / Artifact `8884115300` / SUCCESS；
- Clean-code P0：Run `30888758375` / Artifact `8884155225` / SUCCESS；
- Clean-code Artifact SHA-256：`f7dbad60dd352535113f7a8fa74f85a475e7cc3bf40dc9aa29acdc074f11fb24`；
- T10：`24/24`；正常测试：`375/375`；Java release 8 与 12 模块 Reactor：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Scope and Coding

- 生产代码仅位于 `dec.core.compiler.modelaccess`；
- fixture 只删除冗余后代 WRITE `payInfo.payDetailList`，保留父路径 `payInfo`；
- 未修改 Context、T06/T07/T08/T09 公共合同或 Compiler API；
- 无权限执行、SQL、I/O、网络、查询、缓存、DAG 或全局状态；
- 临时 source workflow 已删除；
- 所有 `@Override` 独占一行；方法、构造器及重要 selector、快照、Diagnostic、重叠和失败逻辑均使用中文注释。
