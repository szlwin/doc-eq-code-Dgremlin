# DESIGN-R28 — TASK-P1-T07 owner identity 与 Diagnostic 聚合 Rework

- Revision：`DESIGN-R28@P1-T07-REWORK-I002`
- Status：`PASSED`
- PR：`#22`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Rework Base：`43846e2d2e2c8b174fb87cdeb15e16c37392f505`
- Superseded Completion：`COMPLETION-P1-T07-R01@7f4ee8a0ee5a`
- Dependency：`COMPLETION-P1-T06-R04@242db638c61d`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Owner module：`dec-core-compiler`
- Owner component：`dec.core.compiler.symbol`

## 1. Review 结论与 Finding

独立 Review 将 R01 推翻为 `NEEDS_CHANGES / REWORK`。R01、R27、R23 及其证据作为不可变历史保留，不覆盖、不删除。

本轮必须关闭：

- `FND-P1-T07-I002-001` `[P1]`：Raw lexical owner 与 TypedKey canonical 混用；
- `FND-P1-T07-I002-002` `[P1]`：RuleView 身份错误依赖最近扫描到的 System；
- `FND-P1-T07-I002-003` `[P2]`：Diagnostic 去重使用 ArrayList.contains，最坏二次复杂度。

## 2. Raw lexical 与 TypedKey canonical 分层

T06 保存验证后的原始 token，不 trim、不改大小写。Context TypedKey 构造器会按既有 `requireText()` 规则 trim。T07 必须同时维护两类事实：

```text
Raw lexical context：用于结构 owner 的精确来源比较
TypedKey context：用于 Symbol 身份、排序和查找
```

结构嵌套定义的 owner 校验固定为：

```text
RawDefinition.ownerToken 原始值 == 原始父定义 lexical name
```

TypedKey 在上述校验通过后独立构造并执行 Context 已冻结的 canonical 规则。

上下文至少保存：

- `SystemKey systemKey` 与 `String systemLexicalName`；
- `BusinessScopeKey scopeKey` 与 `String scopeLexicalName`；
- `DirectoryKey directoryKey` 与 `String directoryLexicalName`；
- `ActionKey actionKey` 与 `String actionLexicalName`。

适用关系：

- System → Information；
- BusinessScope → Directory；
- Directory → Action；
- Action → Produce；
- Produce owner 使用原始 `directoryLexicalName + "/" + actionLexicalName`；
- ROOT_CONFIG → DataSource/Connection 继续使用原始 ROOT_CONFIG lexical name。

成功后 TypedKey 中的 name 可以被 trim，但已登记 `RawDefinition` 必须继续保留原始 lexical。

## 3. RuleView 显式 owner 身份

RuleView 的 `ownerToken` 来自其自身属性，是显式 System 身份，不是嵌套扫描上下文。RuleView 不得读取“最近 System”。

第一阶段拆为同一 Pass 内的两个确定步骤：

1. 扫描全部 RawDefinition，登记所有 System 及其他顶层/结构 owner Key；RuleView 暂存，不登记；
2. System 集合完整后，逐个处理 RuleView：
   - 要求 ownerToken 存在；
   - 使用原始 ownerToken 构造 `SystemKey`，由 Context 规则 canonicalize；
   - 在已登记 System Symbol 中按 `SystemKey` 查找实际 owner；
   - owner 不存在时 fail closed，Diagnostic 为：
     - code：`MIX_STRUCTURE_UNKNOWN`；
     - severity：`ERROR`；
     - messageKey：`symbol.owner.system.missing`；
     - definitionKey：目标 `SystemKey`；
     - sourceRef：RuleView SourceRef；
     - pass：`symbol-registration`；
   - owner 存在时创建 `RuleViewKey(actualSystemKey, ruleViewName)`。

因此 RuleView 支持：

- 规则文档在 Systems 文档之前或之后；
- 指向多个 System 中任意一个；
- 多个 RuleView 分别指向不同 System；
- 相同 RuleView name 在不同 System 下共存；
- 文档排列变化不改变最终 SymbolTable。

RuleView 的显式 owner 采用 canonical System identity 做存在性校验；它不执行结构父子 lexical equals，因为 RuleView 与 System 可以来自独立文档。

## 4. 两遍流程

### 第一遍

- 登记 DataSource、Connection、Data、View、System、BusinessScope、Directory、Action；
- 结构 owner 使用 Raw lexical context 精确比较；
- 建立 sourceOrdinal → TypedKey 映射；
- 收集 RuleView，待所有 System 登记后按显式 ownerToken 登记。

### 第二遍

重新扫描同一不可变 RawDefinitionSet：

- System 恢复 `SystemKey + systemLexicalName`；
- Information 使用原始 system lexical 校验，并创建 canonical `InformationKey`；
- BusinessScope、Directory、Action 恢复对应 TypedKey 与原始 lexical；
- Produce 使用原始 composite owner 校验，并创建 `ProduceKey(ActionKey, sourceOrdinal)`。

两遍全部完成后统一决定 BUILT/FAILED；任一 ERROR 都不发布部分 SymbolTable。

## 5. Diagnostic 线性聚合

禁止 `ArrayList.contains` 去重。单次 build 使用 `LinkedHashSet<Diagnostic>` 或等价哈希集合：

- 每次报告只执行一次集合 add；
- 平均插入/去重复杂度 O(1)；
- 完整收集上限内所有不同 Diagnostic；
- 相同 Diagnostic 只保留一次；
- 创建 `SymbolBuildResult` 时复制并复用 `Diagnostic.compareTo` 稳定排序。

新增包内 `DiagnosticAccumulator`，提供只读快照和测试可见的 add-attempt 计数。小预算 Oracle 必须证明 N 次报告对应 N 次集合插入尝试，不采用真实大数据耗时阈值。

## 6. 测试 Oracle

1. padded System / Information：原始 owner 精确匹配后成功；
2. padded BusinessScope / Directory / Action：完整链成功；
3. padded Produce composite owner 成功；
4. padded RuleView owner canonicalize 后匹配实际 System；
5. TypedKey canonical name 已规范化，RawDefinition lexical 保持原值；
6. RuleView 在 System 前和后均成功；
7. RuleView 指向非最近 System；
8. 多个 RuleView 分别指向不同 System；
9. 相同 RuleView name 在不同 System 下共存；
10. RuleView owner System 不存在时稳定失败且不发布部分表；
11. 文档顺序变化不改变最终 SymbolTable；
12. DiagnosticAccumulator N 次 add 只有 N 次哈希插入尝试；
13. 大量不同重复 Finding 在小注入预算内完整收集且稳定排序；
14. R01 的 23 项 Symbol Oracle 全部继续通过。

## 7. Scope

允许：

- `dec-core-compiler/src/main/java/dec/core/compiler/symbol/**`
- `dec-core-compiler/src/test/java/dec/core/compiler/symbol/**`
- `project_doc/version/V_1.0/**` 中 T07 I002 的 Design、Plan、Review、Evidence、Completion 与恢复事实

禁止：

- 修改 Context TypedKey canonical 规则；
- 修改 T06 Raw 生产合同；
- 实现 T08 ReferenceResolver 或后续 Pass；
- 合并 PR #22。

## 8. 编码与 Review 门禁

- Java release 8；
- 所有新增或修改的 `@Override` 独占一行；
- 方法、构造器及重要 owner、RuleView、Diagnostic、资源与失败逻辑使用中文注释；
- 不引入 static mutable 状态或 I/O；
- R28/R24 必须在 RED 前冻结；
- 只有三个 Finding 全部 CLOSED、开放 P0/P1/P2 为 0 且最终 Artifact 独立校验通过，才可生成 R02 Completion 并将 PR #22 转为 Ready for Review；
- 未经用户明确授权不得合并，T08 继续阻断。

## 9. Review

- `REV-000324` — IndependentReviewConfirmation — `PASSED`；
- `REV-000325` — DesignReviewAgent — `PASSED`；
- `REV-000326` — ArchitectureReviewAgent — `PASSED`；
- Evidence：`EVD-000567`～`EVD-000569`；
- 下一阶段：冻结 R24 实施计划并建立 I002 有效 RED。
