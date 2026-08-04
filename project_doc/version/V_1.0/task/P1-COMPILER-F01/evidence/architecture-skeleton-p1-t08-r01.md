# DEVSKEL-P1-T08-R01 — ReferenceResolver Architecture Skeleton

- Revision：`DEVSKEL-P1-T08-R01@d7155c4f0bb1`
- Design：`DESIGN-R29@P1-T08-I001`
- Plan：`TP-P1-COMPILER-F01-R25@P1-T08-I001`
- Valid RED：`TDD-P1-T08-R01@d7155c4f0bb1`
- P0 Run：`30827276340`
- Artifact：`8861386414`
- Artifact SHA-256：`2765996eac357969a12f4ee72d69d0f8be4ead5180950c9ba16fbbee0509136b`
- Result：`PASSED`

## 结构冻结

Architecture Review 冻结以下单次调用结构：

1. `ReferenceResolver.resolve(RawDefinitionSet, SymbolTable)`；
2. `ResolutionState` 仅在一次 resolve 内存在，不跨线程共享；
3. `sourceOrdinal -> DefinitionKey`：恢复引用来源的精确 TypedKey；
4. `DefinitionKey -> RawDefinition`：关联 SourceRef 与 Data 属性；
5. `lexical -> existing DefinitionKey`：只用于 unknown/type mismatch/owner mismatch 失败分类，禁止参与成功匹配；
6. `SystemKey -> declared ViewKey Set`：校验 RuleView 的 System 声明边界；
7. `DataKey -> exact property Set`：校验 View ref-property，不创建平行 PropertyKey；
8. `LinkedHashSet<ResolvedReference>` 与 `LinkedHashSet<Diagnostic>`：完整聚合、确定性去重；
9. 成功查询始终构造期望 TypedKey并调用 `SymbolTable.find(expectedKey)`；
10. 全部定义扫描完成后统一发布 `RESOLVED` 或 `FAILED`，失败不发布部分集合。

## Role Policy 接缝

按 RawDefinitionKind 独立派发：Connection、View、System、RuleView、Action、Directory、Produce。Information、ModelAccess、Rule property、Produce 模型输出以及 P2～P7 明确不进入本次实现。

## Review 结论

- `REV-000343` — TddRedReviewAgent — `PASSED`；
- `REV-000344` — ArchitectureSkeletonReviewAgent — `PASSED`；
- `REV-000345` — SecurityBoundaryReviewAgent — `PASSED`；
- Evidence：`EVD-000590`～`EVD-000592`；
- 有效 RED 为 9 failures / 0 errors；首个 9 failures / 3 errors attempt 保留为测试设计失败历史；
- 不修改 Context、T06、T07 公共合同；
- 不引入 I/O、反射、模糊搜索、static mutable registry；
- `@Override` 独占一行，方法和关键逻辑使用中文注释；
- 下一阶段：提交真实索引 Skeleton，确认受控 RED 后完成 GREEN。
