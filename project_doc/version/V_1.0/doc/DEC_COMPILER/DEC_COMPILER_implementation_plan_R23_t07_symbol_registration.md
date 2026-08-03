# TP-P1-COMPILER-F01-R23 — TASK-P1-T07 实施计划

- Revision：`TP-P1-COMPILER-F01-R23@P1-T07-I001`
- Status：`PASSED`
- Design：`DESIGN-R27@P1-T07-I001`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Dependency：`COMPLETION-P1-T06-R04@242db638c61d`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Branch：`feature/p1-t07-symbol-table-20260803-1958`

## 顺序流程

1. 建立可编译 TDD seam，并新增 `TypedKeyContractTest` 与 `SymbolRegistrationTest`。
2. 运行标准 P0；RED 必须来自目标行为尚未实现，不能来自缺类、编译或依赖错误。
3. 提交 Architecture Skeleton，冻结结果对象、只读 Registry、两遍扫描和重复诊断接缝，保留受控 RED。
4. 完成 TypedKey 映射、owner 上下文、重复聚合、资源上限和不可变发布。
5. 运行 T07 定向测试、Compiler 全量和 12 模块 Reactor。
6. 串行完成 Specification、Engineering、Architecture、Security、TDD、Test Evidence 与 Completion Review。
7. 建立 R01 Completion、机器恢复入口和 Git checkpoint。
8. 最终文档化 Head 再运行 P0、独立校验 Artifact，并创建面向 `dev_all` 的 PR。
9. 不合并 PR，不启动 `TASK-P1-T08`。

## TDD seam

TDD commit 同时提供最小可编译类型：

- `SymbolBuildStatus`
- `SymbolBuildResult`
- `SymbolTable`
- `SymbolTableBuilder`

该 seam 只保证 Java 8 编译和稳定的“未实现”失败结果，不实现真实注册。测试因此以行为断言形成 RED。

## TDD Oracle

- 11 类 Context TypedKey 的类型隔离和 owner 链。
- InformationKey 精确绑定 SystemKey。
- 无名 Produce 使用 sourceOrdinal。
- 11 类 RawDefinition 到 TypedKey 的映射。
- 第一遍登记顶层与 owner Key，第二遍登记 Information 与 Produce。
- 不同类型同名、不同 System 同名 Information、不同 Scope 同名 Directory/Action 可共存。
- 同 TypedKey 重复产生 `MIX_SYMBOL_DUPLICATE`，首定义不被覆盖。
- 重复 Diagnostic 同时保留首定义和重复定义 SourceRef。
- owner 上下文不完整或 token 不一致时 fail closed。
- keys、definitions、diagnostics 稳定有序且不可变。
- 重复运行结果完全一致。
- `ROOT_CONFIG`、`RULE`、`MODEL_ACCESS` 不产生伪 TypedKey。
- 不解析 RawReference，失败不暴露部分 SymbolTable。

## Architecture Skeleton

Skeleton 包含：

- `SymbolTable` 只读 facade，内部使用 Context `ImmutableRegistry`。
- `SymbolBuildResult` 的 BUILT/FAILED 不变量。
- `SymbolTableBuilder` 的 `firstPass`、`secondPass`、`register`、owner context 和 Diagnostic factory 接缝。
- 所有状态仅存在于单次调用中，不使用 static mutable Registry。

Skeleton Review 必须确认不新增平行 TypedKey、不修改 Context/T06 合同、不实现 T08，并检查中文注释和独占一行的 `@Override`。

## GREEN

第一遍按 sourceOrdinal 登记 DataSource、Connection、Data、View、System、BusinessScope、RuleView、Directory、Action，并建立 ordinal 到 TypedKey 的映射。

第二遍重新扫描同一 RawDefinitionSet：Information 绑定当前 System；Produce 绑定当前 Action并使用 sourceOrdinal。ownerToken 必须与结构上下文精确一致。

每次登记前检查重复，首定义保留，后续重复只追加稳定 Diagnostic。全部扫描后若存在错误，返回 FAILED 且不发布 table。

在创建 Symbol Map 前拒绝超过 65,536 个 RawDefinition；Produce ordinal 必须能安全转换为 int。使用 TreeMap 和 DefinitionKey 自然顺序，所有输出 defensive copy 且不可变。

## 允许范围

- `dec-core-compiler/src/main/java/dec/core/compiler/symbol/**`
- `dec-core-compiler/src/test/java/dec/core/compiler/symbol/**`
- T07 对应 `project_doc/version/V_1.0/**` 事实

禁止修改 Context、Raw、Frontend、SourceGraph 和 Compiler API 生产代码；禁止实现 T08 及后续 Pass。

## 验证

- T07：`TypedKeyContractTest,SymbolRegistrationTest`
- Compiler 模块全量测试
- Maven `clean verify`
- 故意失败测试阻断证明

## 停止条件

需要修改 Context/Raw 公共合同、RED 不真实、Skeleton Review 失败、范围越界、出现 last-write-wins、失败发布部分表、Java 8/P0/Artifact/Revision 门禁失败，或开放 P0/P1 不为 0时立即阻断。

## Review

- `REV-000311` — PlanReviewAgent — `PASSED`
- `EVD-000553`
- R27/R23 在 RED 前冻结，后续只引用不覆盖。
