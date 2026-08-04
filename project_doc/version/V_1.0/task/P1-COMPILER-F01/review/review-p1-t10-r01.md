# TASK-P1-T10 Review R01

- Revision：`CODEREVIEW-P1-T10-R01@9e94bc68d9a8`
- Task：`TASK-P1-T10 / I001`
- Result：`PASSED`
- Reviews：`REV-000408`～`REV-000424`
- Evidence：`EVD-000669`～`EVD-000691`
- Open P0/P1/P2：`0 / 0 / 0`
- Clean-code Head：`9e94bc68d9a8c25351213bb46a6cafa5702105d9`

## Review matrix

- Architecture：exact source path 与 target selector 边界明确；P1 不执行权限或数据访问；
- Input identity：RawDefinitionSet/SymbolTable 完整快照门禁位于全部语义工作之前；
- Exactness：target-main 完整匹配优先，property path 逐段、区分大小写、限于同一已声明 View；
- Failure：未知/未声明/缺失/歧义/非复合/非法 lexical 均 Diagnostic fail-closed；
- Batch atomicity：任一 ERROR 不发布部分 Binding 或 Deferred；
- Duplicate：完全重复与不同 SourceRef 的语义重复均阻断；
- WRITE overlap：相同、祖先、后代与 `*` 通配均阻断；
- Value semantics：Binding、Compilation、Result、Resolution 基于全部语义字段保持不可变与一致 equals/hashCode/compareTo/toString；
- Resource/security：无 I/O、网络、SQL、权限执行、查询、缓存、反射执行、模糊匹配或全局状态；
- Scope：未修改 Context、T06/T07/T08/T09 公共合同或 Compiler API；
- Coding：`@Override` 独占一行，方法、构造器及重要 selector、快照、Diagnostic、重叠与失败逻辑使用中文注释。

## Findings

### FND-P1-T10-I001-001 — CLOSED

首轮实现仅读取 focused fixture 的 `view-ref@name`，真实 Canonical 使用 `view-ref@ref`。修复后优先读取 `ref`，兼容测试 seam 的 `name`，并新增真实 `CanonicalDocumentNode → RawDefinitionBuilder → SymbolTableBuilder → ModelAccessCompiler` 集成测试。

### FND-P1-T10-I001-002 — CLOSED

`ModelAccessBinding.equals/hashCode` 包含 SourceRef，但首轮 `compareTo/toString` 未包含，存在值语义与稳定排序不一致。修复后 SourceRef 参与排序和字符串表示，并新增不同 SourceRef 的值语义测试。

## Negative review coverage

- Canonical `view-ref@ref`；
- `*` WRITE 与具体路径重叠；
- 其它 View 存在同名 selector 时禁止回退；
- 不同 SourceRef 的语义重复；
- malformed path 不泄漏输入异常；
- 公共类型 final、字段 final、无 execute/query/sql/cache/current API；
- 快照失配 resolver 调用 0 次；
- Diagnostic 稳定排序且失败无部分发布。

## Evidence conclusion

- Valid RED：17 failures / 0 errors；
- Architecture：14 controlled failures / 0 errors；
- T10 final：24/24；
- Normal tests：375/375；
- Clean-code P0、Java 8、12 模块 Reactor 和故意失败门禁均通过；
- MySQL：`SKIPPED_NOT_APPLICABLE`。
