# TASK-P1-T07 / I001 独立 Review — R01

- Result：`PASSED`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Clean-code Head：`7f4ee8a0ee5a8be84e8edfe715a85189858ac425`
- Design：`DESIGN-R27@P1-T07-I001`
- Plan：`TP-P1-COMPILER-F01-R23@P1-T07-I001`
- TDD：`TDD-P1-T07-R01@9e7dbc1bb451`
- Skeleton：`DEVSKEL-P1-T07-R01@c4d33f9ec8e9`

## Review 结论

```text
P0: 0
P1: 0
P2: 0
```

T07 已复用 Context 中冻结的 11 类 TypedKey，通过两个完整 sourceOrdinal 扫描构建稳定、有序、不可变的 SymbolTable。实现不新增平行字符串 Key，不修改 Context、Raw、Frontend 或 Compiler API 公共合同，也不解析 RawReference。

## 两遍注册

第一遍登记：

- DataSourceKey
- ConnectionKey
- DataKey
- ViewKey
- SystemKey
- RuleViewKey
- BusinessScopeKey
- DirectoryKey
- ActionKey

第二遍登记：

- InformationKey，精确绑定当前 SystemKey
- ProduceKey，精确绑定当前 ActionKey 并使用 Raw sourceOrdinal

`ROOT_CONFIG`、`RULE`、`MODEL_ACCESS` 没有已发布 Context TypedKey，保持 Raw 事实，不制造伪 Key。

## 重复与失败

- 每次 TreeMap 写入前显式检查重复；
- 首定义永不被后写覆盖；
- 后续重复产生 `MIX_SYMBOL_DUPLICATE / ERROR / symbol.duplicate`；
-主 SourceRef 为重复定义，relatedRefs 保存首定义 SourceRef；
- 两遍始终完整扫描后再决定 FAILED；
- 完全相同 Diagnostic 去重；
- 任一错误都不发布部分 SymbolTable。

独立 Review 在首个 GREEN `7612ebc81c6b...` 上发现 `FND-P1-T07-I001-001`：第一遍错误会短路第二遍，导致同批次子定义 Finding 遗漏。负向 Head `0aefe724a1b1...` 的 P0 Run `30814139674` 精确得到 1 个失败（预期 2 个重复 Diagnostic，实际 1 个）。最终修复 Head `7f4ee8a0ee5a...` 已关闭该 Finding。

## Owner 与隔离

- Information 不跨 System 猜测 owner；
- Directory、Action、Produce 不跨 BusinessScope/System 文档泄漏旧上下文；
- ownerToken 与当前结构 owner 不一致时返回 `symbol.owner.context.invalid`；
- 不同类型同名可以共存；
- 不同 System 下同名 Information 可以共存；
- 不同 BusinessScope 下同名 Directory/Action 可以共存。

## 资源与不可变性

- 在 Symbol Map 分配前拒绝超过 65,536 个 RawDefinition；
- 测试通过注入小预算验证边界，不执行真实 OOM；
- SymbolTable 使用 Context `ImmutableRegistry`；
- keys 与 definitions 按 DefinitionKey 自然顺序冻结并一一对应；
- SymbolBuildResult 强制 BUILT/FAILED 互斥合同；
- FAILED 只接受 ERROR 且 pass 为 `symbol-registration` 的 Diagnostic。

## 测试证据

最终 clean-code P0：

```text
Run:       30814383829
Result:    SUCCESS
Artifact:  8856098502
SHA-256:   1f71fb0f3f2615dfc599792e5760993048f832a085bdfed965b44b0f13acfdf8
```

Artifact 已独立下载，实际 ZIP SHA-256 与 GitHub digest 一致；60 个 Surefire XML 已解析：

- Symbol：23/23
- Compiler：152/152
- XML：30/30
- YAML：59/59
- Context 正常：26/26
- Demo：4/4
- Legacy declaration：1/1
- 故意失败门禁：1 项按预期失败并被识别
- 12 模块 Reactor、Java release 8：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Revision 与范围

- R27 blob：`613edfdc133fa68aa12ae3adc31eb8ae23058d9c`
- R23 blob：`840989a6119e7e5f99981957614806c2152ea56d`
- 两者在 RED 前建立，最终 clean-code Head 复核未变化。
- 生产代码只新增 `dec-core-compiler.symbol` 包；
- 测试只新增同包 T07 Oracle；
- 所有新增 `@Override` 独占一行；
- 方法、构造器和关键注册、owner、重复、资源、失败逻辑均有中文注释；
- 未启动 ReferenceResolver、Information、ModelAccess、Deferred、Pipeline、Digest、Publication 或 T08。

## Review 记录

- `REV-000312` — TDDReviewAgent — PASSED
- `REV-000313` — ArchitectureReviewAgent — PASSED
- `REV-000314` — SpecComplianceReviewAgent — PASSED
- `REV-000315` — IndependentReviewAgent — NEEDS_CHANGES，发现 `FND-P1-T07-I001-001`
- `REV-000316` — CodeReviewAgent — PASSED，Finding CLOSED
- `REV-000317` — SpecificationReviewAgent — PASSED
- `REV-000318` — EngineeringStandardsReviewAgent — PASSED
- `REV-000319` — ArchitectureReviewAgent — PASSED
- `REV-000320` — SecurityReviewAgent — PASSED
- `REV-000321` — TDDReviewAgent — PASSED
- `REV-000322` — TestEvidenceReviewAgent — PASSED
- `REV-000323` — CompletionReviewAgent — PASSED
- Evidence：`EVD-000554`～`EVD-000566`

## 最终 Gate

`TASK-P1-T07 / I001` 可形成 R01 Completion。PR 可以转为 Ready for Review，但未经用户明确授权不得合并；`TASK-P1-T08` 在 PR 合并前保持未启动和阻断。
