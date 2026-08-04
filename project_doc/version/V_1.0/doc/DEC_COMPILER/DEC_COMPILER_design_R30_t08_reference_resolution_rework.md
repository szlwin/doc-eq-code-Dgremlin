# DESIGN-R30 — TASK-P1-T08 I002 引用解析返工

- Revision：`DESIGN-R30@P1-T08-REWORK-I002`
- Status：`PASSED`
- Supersedes：`DESIGN-R29@P1-T08-I001` 的当前有效性；R29 保留为历史
- Base：`PR-23@9ece664412ee947f536e2de73f20b5c7b9790bf1`
- Dependency：`COMPLETION-P1-T07-R02@ffe544e3060d`
- Invalidated Completion：`COMPLETION-P1-T08-R01@ab432a3189f4`
- Branch：`feature/p1-t08-reference-resolution-20260803-2254`
- Target PR：`#23`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Owner module：`dec-core-compiler`
- Owner component：`dec.core.compiler.symbol.ReferenceResolver`
- Findings：`FND-P1-T08-I002-001`～`004`

## 1. 目标

在不删除 T08/I001 历史的前提下，修复独立 Review 发现的 lexical fail-closed、资源复杂度、输入快照绑定和真实 T06 body 覆盖缺口。

I002 继续保持 T08 原有正常引用矩阵与成功路径：成功只构造精确 TypedKey 并调用 `SymbolTable.find`；lexical 数据只用于失败分类，不参与成功目标替代。

## 2. 完整输入快照绑定

### 2.1 SymbolTable 内部快照身份

`SymbolTableBuilder.build(RawDefinitionSet)` 在成功发布 SymbolTable 时同时保存不可变的完整 `RawDefinitionSet` 输入快照。该快照只作为 compiler-symbol 包内的绑定事实，不新增或改变 Context、T06 Raw、T07 Symbol 的公共 API。

`SymbolTable` 的现有公共查询、keys、definitions、size、equals/hashCode 行为保持兼容；新增快照访问或匹配能力仅为 package-private。

### 2.2 Resolver 前置校验

`ReferenceResolver.resolve(definitions, symbolTable)` 在创建 sourceKey、System View、Data property 或 lexical 索引前执行完整快照比较：

- 定义数量；
- sourceOrdinal；
- kind；
- sourceRef；
- ownerToken；
- name；
- attributes；
- references；
- body；
- format；
- schemaVersion。

由于 `RawDefinition`、`RawNodeBody`、`RawReference` 与 `RawDefinitionSet` 已实现完整值语义，绑定校验以 `RawDefinitionSet.equals` 为最终判定，并定位第一个差异生成稳定 SourceRef/relatedRefs。

不一致时立即返回：

```text
code:       MIX_REF_UNKNOWN
severity:   ERROR
messageKey: reference.input.snapshot-mismatch
pass:       reference-resolution
```

不得开始引用解析，不得发布部分 `ResolvedReferenceSet`，不得仅按 ordinal 猜测 sourceKey。

## 3. Lexical fail-closed 合同

### 3.1 简单目标

所有会进入 TypedKey 构造器的简单 lexical 先经过统一 `parseSimpleTarget`：

- null、缺失或 `trim()` 后为空 → `reference.owner.invalid`；
- 保存原始 targetToken 用于 ResolvedReference/Diagnostic 事实；
- TypedKey 使用 trim 后的非空 token 构造；
- 任何输入相关 `IllegalArgumentException` 转换为 `reference.owner.invalid`，不得越过 `ReferenceResolutionResult`。

适用范围至少包括：

- Connection data-source；
- View target-main、nested data；
- System data-ref/view-ref 的 ref/name；
- RuleView owner/view；
- Action system/rule；
- Directory rel；
- property lexical。

### 3.2 qualified Information

`parseQualifiedInformationTarget` 必须：

1. 以整体 trim 后文本解析；
2. 恰好包含一个 `.`；
3. system 与 information 两段 trim 后均非空；
4. 多余段、首尾分隔符、空白段全部拒绝；
5. 仅解析成功后构造 `SystemKey + InformationKey`。

以下均返回 `FAILED + reference.owner.invalid`：

```text
user.active.extra
 .active
user. 
active
```

### 3.3 缺失声明节点

System body 中出现 `data-ref` 或 `view-ref` 节点但既无 `ref` 也无 `name` 时，必须在该节点 SourceRef 产生 `reference.owner.invalid`；不得静默忽略或回退到其他来源。

### 3.4 最终异常边界

对所有满足 T06 对象构造合同的输入，`resolve()` 只能返回 `RESOLVED` 或 `FAILED`。统一 lexical/TypedKey helper 是主要边界；入口保留只捕获输入相关 `IllegalArgumentException` 的最后 fail-closed 防线，使用稳定 Diagnostic，不吞掉其他编程错误。

## 4. O(1) lexical 失败分类

原 `Map<String, List<DefinitionKey>>` 替换为：

```text
Map<String, LexicalCandidateSummary>
```

每个摘要在索引阶段一次性保存：

- 是否存在任意类型；
- `Class<? extends DefinitionKey> → 稳定首个代表 Key`；
- 稳定首个任意类型代表 Key；
- 是否存在 RuleView（由类型摘要 O(1) 判断）。

以下查询必须为平均 O(1) 或 O(log n)：

- 同 lexical 是否存在任意类型；
- 是否存在期望类型；
- 是否存在 RuleView；
- 获取期望类型或任意类型的稳定 related SourceRef。

生产逻辑不得遍历同 lexical 候选 List。新增 package-private、不可变的 lookup observer seam，仅用于小预算测试计数；默认实现无副作用、无 static mutable state。

## 5. 真实 T06 body 集成合同

新增 Canonical 集成 Oracle，主路径必须经过：

```text
CanonicalDocumentNode
→ RawDefinitionBuilder
→ RawDefinitionSet
→ SymbolTableBuilder
→ SymbolTable
→ ReferenceResolver
```

至少覆盖：

- View `target-main`；
- nested property `data`；
- nested property `ref-property`；
- System `data-ref@ref`；
- System `data-ref@name`；
- System `view-ref@ref`；
- System `view-ref@name`；
- RuleView 文档位于 System 文档之前；
- Directory/Produce qualified Information；
- `user.active.extra`、` .active`、`user. `；
- blank target-main；
- blank data-ref@name；
- blank view-ref@name；
- data-ref/view-ref 缺失 ref/name；
- 不同 RawDefinitionSet/SymbolTable 快照。

现有手工 Raw fixture 继续用于精确单元测试，但不能单独作为 Completion 证据。

## 6. 结果与 Diagnostic

保留 R29 既有：

- `reference.unknown`；
- `reference.type.mismatch`；
- `reference.property.unknown`；
- `reference.owner.invalid`；
- `reference.rule-system.mismatch`。

新增：

- `reference.input.snapshot-mismatch`。

所有失败使用 `MIX_REF_UNKNOWN`，Rule/System 归属冲突继续使用 `MIX_REF_RULE_SYSTEM_MISMATCH`。Diagnostic 继续以 LinkedHashSet 去重、完整扫描后按 `Diagnostic.compareTo` 稳定排序；快照不匹配属于入口阻断，允许在解析扫描前立即失败。

## 7. 架构骨架

Architecture Skeleton 必须先落地以下真实边界，具体解析仍使用显式未实现失败：

- SymbolTable 完整输入快照绑定；
- `parseSimpleTarget`；
- `parseQualifiedInformationTarget`；
- `safeTypedKey`/统一构造失败转换；
- `LexicalCandidateSummary`；
- lookup observer seam；
- snapshot mismatch Diagnostic；
- Canonical 集成测试入口。

Skeleton Review 未通过前不得实现完整 Role Policy。

## 8. 资源、安全与兼容性

- 快照比较 O(D)，仅一次；
- Symbol 与 lexical 摘要索引 O(D)；
- 每个引用成功查询与失败分类平均 O(1) 或 O(log n)；
- 总体目标 O(D + R)，不允许按同 lexical 候选数放大；
- 无 I/O、网络、反射加载、表达式执行或运行时规则执行；
- 无 static mutable registry；
- 不改变 Raw lexical；
- 不启动 T09/T10/P2～P7；
- PR #23 合并前 T09 继续阻断。

## 9. 编码规范

- Java release 8；
- 所有 `@Override` 注解独占一行；
- 公共方法、构造器和重要快照、lexical、复杂度、owner、Diagnostic、失败发布逻辑使用中文注释；
- 注释解释约束与原因，不逐行复述实现。

## 10. Review

- `REV-000353` — IndependentReviewAgent — `NEEDS_CHANGES`；
- `REV-000354` — DependencyGateReview — `PASSED`；
- `REV-000355` — DesignReviewAgent — `PASSED`；
- `REV-000356` — ArchitectureReviewAgent — `PASSED`；
- Evidence：`EVD-000600`～`EVD-000603`；
- Open P0/P1/P2：`0 / 3 / 1`，等待 I002 TDD 与实现关闭。