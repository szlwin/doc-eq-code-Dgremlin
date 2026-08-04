# EVD-000648 — DEV-P1-T09-R02 Development Evidence

- Revision: `DEV-P1-T09-R02@95b08223083f`
- Clean-code Head: `95b08223083f9d6b8573e96cdd12364334c0f234`
- Findings closed: `FND-P1-T09-I002-001/002/003/004`
- Status: `PASSED`

## Changes

- `InformationIdentity` 统一 canonical `SystemKey("common")` 判定；raw name/owner lexical 不改写；
- `InformationCommonValidator` 的 System、Information、ModelAccess 限制共享 canonical 身份；
- `DefaultInformationReferenceResolver` 共享同一 common predicate；
- `SymbolTable.isBuiltFrom` 以完整不可变 RawDefinitionSet 值语义返回只读一致性结果；
- `InformationCompiler` 在 common validation、parser、owner lookup、resolver、Deferred 之前执行独占快照门禁；
- 快照失败只返回 `information.input.snapshot-mismatch`，无 parser/resolver 调用和部分输出；
- parser 根深度从 0 开始，128 层通过、129 层失败；
- R27 正确 first commit 恢复为 `e7713c449927...`，原无效 Evidence 保留为历史；
- 临时源码快照 workflow 已删除。

## Scope

生产修改仅位于 `dec.core.compiler.information`，以及 `SymbolTable` 的一个 additive read-only predicate。未改变 Raw lexical、Registry、SymbolTable equals/hashCode、Context、T08 公共行为、Compiler API 或 systems.xml；未启动 T10/T11/P2～P7。

所有 `@Override` 独占一行，新增及重要 identity、snapshot、parser、Diagnostic、资源和失败逻辑均使用中文注释。
