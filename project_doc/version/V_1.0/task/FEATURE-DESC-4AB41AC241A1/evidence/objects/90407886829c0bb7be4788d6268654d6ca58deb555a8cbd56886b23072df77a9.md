# P3 Information Engine 技术设计

> Design revision：`DESIGN-P3-R08`
> Base revision：`BM-P3-R04`
> Design topic：`DESIGN-P3-INFORMATION-ENGINE`
> Requirement：`REQAN-P3-R05`
> Business Flow：`FLOW-R08@p3-information-evaluation` / `FLOW-P3-INFORMATION-EVALUATION`
> Module：`P3-INFORMATION-ENGINE`
> Implementation strategy contract：`1`
> 状态：DRAFT

## 第一部分：设计正文

### 1. 一页设计摘要

<!-- DESIGN-NARRATIVE-SUMMARY -->

本设计把 P3 Information Engine 分为配置编译、实时识别和受保护物化三条边界。编译阶段将 `mix` 的三类 Information 与同一 Compilation Session 中已冻结的 P2 exact binding 合成为不可变 `CompiledInformationSet`；解析 Directory 的每个 `<change-info information-ref="...">` 时，创建并保存一个 `ChangeInfo` 到 RuleViewInfo 的映射，在编译完成时生成一次带 `##` 前缀的两规则 RuleViewInfo，通过 `ConfigInfo.addRuleViewInfo()` 注册，再与 `CompiledModelSet`、`EngineContext` 一起发布。识别阶段只消费该集合内的 exact binding，不调用 change-info 物化，也不维护 `MutationSet`、reverse-DAG 缓存或 runtime 路径解析；物化阶段只允许模型表达式原子写入声明的 `change-data` 值，并将已生成 RuleView 内按序执行的 `grammer` Rule 与 `update` Rule 交给现有 `ModelContainer.execute()` 承载。

本 revision 按用户确认收敛失败语义：普通非法模型路径、普通 `null`、非法表达式、缺失引用、权限拒绝和只读 RuleView 执行失败均产生可定位的 `ERROR` 并抛出异常；只有正常条件不满足才返回 `FALSE`，显式 `InformationKey = null` 比较仍是例外。`evaluate` 只读；`materialize` 才允许写入。物化成功以 commit 和 `MaterializationResult` 为终点，不调用 `evaluate`，不重新识别目标或下游 Information，也不返回识别结果。

#### 1.1 改造前后对照

| 关注点 | 当前情况 | 调整后 | 带来的价值 |
|---|---|---|---|
| Information 事实 | XML 已有声明，编译器已有部分表达式/Deferred 基础但缺少完整闭环 | 编译为不可变 Information 定义和 Key | P4/P5 可稳定消费 |
| 求值 | 规则、模型字段和复合引用没有统一入口 | 原子先求值，复合按 DAG 拓扑短路 | 结果语义一致 |
| 数据读取 | 可能依赖旧值或调用方缓存 | 每次按配置重新读取当前值 | 与事务内真实数据一致 |
| 物化 | Change 语义未形成 P3 契约 | 编译期为每个 `change-info` 生成并注册一次可复用 RuleView，保存到 ChangeInfo 映射；P5 在最后一个 Action 成功后直接调用，按 `grammer → update` 写入 | 禁止运行期临时生成、错误伪装和部分成功 |

#### 1.2 六个关键结论

| 读者最关心的问题 | 当前结论 |
|---|---|
| 本次改变什么 | 增加 Information 编译、识别、依赖和物化契约 |
| 保持什么不变 | P2 归属/权限、现有 ModelContainer 事务、XML fixture 不变 |
| 主流程如何工作 | parse → compile（含 ChangeInfo RuleView 生成/ConfigInfo 注册）→ publish → evaluate；P5 在最后一个 Action 成功后调用映射对象完成 materialize，成功 commit + MaterializationResult 后结束 |
| 失败时留下什么事实 | 普通求值错误为 `ERROR` 诊断并抛异常；正常不满足才是 `FALSE`；物化写入失败无提交并抛异常 |
| 最关键的技术决策 | `InformationEngine` 无状态实时读取，不维护 MutationSet |
| 如何证明设计完成 | 设计 Review 覆盖 BR/AC/TR、接口、失败路径和测试接缝 |

### 2. 背景、现状与设计目标

<!-- DESIGN-NARRATIVE-CURRENT -->

当前 `mix` 已声明 16 个 Information；`dec-core-compiler/src/main/java/dec/core/compiler/information/InformationCompiler.java` 已提供部分表达式/Deferred 编译骨架，但尚未形成 16 项 Information 的模型表达式、DAG、识别和物化闭环。`dec-demo/src/test/resources/mix/business/order-business.xml` 已明确 `paying` 目录包含 Action `startPay`，其后配置 `<change-info information-ref="order.paying"/>`；对应 Information 在 `systems.xml` 中声明了 `change-data`。P2 已提供 System、View、RuleView 和 model-access Binding；现有 `ModelContainer`/`RuleContainer`/`TransactionContainer` 已负责执行、提交、回滚和资源清理。本 revision 明确 ChangeInfo 的编译期一次生成、ConfigInfo 注册、DirectoryInfo.ChangeInfo 映射和 P5 最后 Action 成功后的调用接缝，不修改 XML 语法或既有事务承载。

#### 2.1 当前实现证据

| 能力/入口 | 当前锚点 | 本次处置 |
|---|---|---|
| 规则执行与回滚 | `dec-core-model/src/main/java/dec/core/model/container/ModelContainer.java:76-137,172-247`、`dec-core-model/src/main/java/dec/core/model/execute/rule/RuleContainer.java:45-61,103-127`、`.../execute/tran/TransactionContainer.java:212-249` | REUSE，物化复用 `ModelContainer.load(...).execute()`，异常由其回滚 |
| model-access Binding | `dec-core-compiler/src/main/java/dec/core/compiler/modelaccess/ModelAccessPolicyCompiler.java`、`.../DefaultModelAccessSelectorResolver.java` | REUSE，编译期消费显式 ref 和 target-main 优先规则；runtime 不重复解析 |
| mix fixture | `dec-demo/src/main/resources/mix/system/systems.xml` | REUSE，作为 16 Information 基线 |
| 旧 RuleTests 调用链 | `dec-demo/src/test/java/dec/demo/model/RuleTests.java` | 仅作为执行链参考，不作为 P3 测试证据 |

#### 2.2 设计目标与非目标

- 目标：形成不可变编译事实、同 Session 发布闭包、实时 read-set、DAG 求值、编译期 exact path/binding、结果诊断和 change-data 物化契约。
- 非目标：不重新定义 Action/Produce/Directory/Query 的业务语义；P3 不实现 P4 Action/Produce 或 P5 Directory 执行器，只提供 ChangeInfo 编译事实和调用契约。不维护 MutationSet/reverse-DAG，不新增事务、幂等或并发机制，不修订 P2 文档或 XML；多目录、多 ChangeInfo、并发隔离和连接选择不在本阶段要求内。
- 必须保持：普通 null 和非法路径为 `ERROR` 并抛异常；显式 `InformationKey = null` 比较为例外；`every(emptyCollection)=TRUE` 但订单明细必须非空。

### 3. 影响范围与明确边界

| 范围 | 是否变化 | 说明 |
|---|---|---|
| dec-core-compiler | 是 | 增加 Information/ChangeInfo 编译、RuleViewInfo 生成和注册边界 |
| dec-core-context | 是 | 承载中立的 `CompiledInformationSet`、ChangeInfo 映射、`CompiledModelSet` 和 `EngineContext` 发布聚合，不依赖 model |
| dec-core-model | 是 | 承载 `InformationEngine`/`InformationMaterializer` runtime 实现，消费已注册对象并复用现有执行链 |
| dec-context-config-parse-xml | 兼容扩展 | 读取并保存 `change-info@information-ref`；不改变 XML 语法 |
| P4 Action/Produce | 下游输入 | 消费 P3 已发布 Information/ChangeInfo 事实并产出有序 Action 结果，不在 P3 实现 |
| P5 Directory | 下游输入 | 按 `DirectoryInfo.actions` 顺序执行，最后一个 Action 成功后追加 ChangeInfo loader，不在 P3 实现 |
| 数据库表/字段 | 否 | 只读取/写入既有模型路径 |
| API | 新增内部契约 | 不承诺外部 HTTP/API；面向 P4/P5 的稳定 Java 只读边界 |
| 测试 | 后续阶段 | 本阶段只定义测试接缝，不创建测试 Case |

范围外还包括 P2 model-access 兼容修订、现代 YAML、Consumer runtime、独立 Session/Transaction、独立 RuleView 隔离器、幂等和并发控制；P3 不修改 ModelContainer 的事务生命周期。

### 4. 目标方案与职责边界

<!-- DESIGN-NARRATIVE-TARGET -->

```text
Raw XML declarations
        -> ReferencePass
        -> ModelAccessPass (P2 exact CompiledTargetBinding + policy)
        -> InformationPass (InformationCompiler MODIFY)
        -> ChangeInfoMaterializationPass (每个 change-info 生成/注册一次 RuleViewInfo，并保存 ChangeInfo 映射)
        -> Deferred/Semantic/Digest passes
        -> CompiledModelSetBuilder
             [CompiledInformationSet + exact ModelAccessPolicyIndex + all P1/P2 facts]
        -> CandidateContextPublicationPass
             -> one EngineContext / one atomic published CompiledModelSet
        -> InformationEngine.evaluate(key, ModelContext)
             -> compiler-frozen exact binding/read-set
             -> read-only RuleView binding
             -> boolean evaluator
        -> P5 Directory after-last-action hook
             -> ChangeInfo 的已注册 RuleViewInfo 映射
        -> InformationMaterializer (only model-atomic change-data)
             -> prebuilt RuleView: grammer Rule -> update Rule
             -> existing ModelContainer.load(...).execute()
             -> existing commit/rollback/close/clear
             -> success: MaterializationResult -> end
```

| 组件 | 调整后职责 | 不应承担的职责 |
|---|---|---|
| `InformationCompiler` | MODIFY 既有编译入口；三类输入分流、类型互斥、Key、编译期 exact binding/read-set、DAG、权限校验，并为每个 `change-info` 生成一次 RuleViewInfo、调用 ConfigInfo 注册并回填 ChangeInfo 映射 | 运行时读写模型；重复解析 selector；运行时生成 RuleView |
| `CompiledInformationSet` | 保存不可变 Information、编译期 exact binding/read-set、依赖拓扑、canonical semantic form 和按 ChangeInfoKey 索引的已编译 RuleView 映射 | 缓存运行结果；重新解析路径；运行时临时注册/清理 RuleView |
| `InformationEngine` | 按 Key 消费已发布集合，实时读取并返回 TRUE/FALSE；只执行编译期通过的只读 RuleView | 修改模型、维护 MutationSet、解析 selector、拥有 P2 binding |
| `InformationMaterializer` | 消费已发布的 `ChangeInfoMaterializationPlan`/RuleView，按序执行 `grammer` 与 `update` 两条内部规则，并调用既有 ModelContainer | 运行时生成或全局注册 RuleView；自建连接/事务，直接 commit/rollback/close，吞异常 |
| `DirectoryChangeInfoHook` | 作为 P5 的消费契约，在既有 Directory/Action 执行器确认最后一个 Action 成功后，通过 DirectoryInfo.ChangeInfo 映射找到已注册 RuleViewInfo 并调用 `InformationMaterializer` | 重新编译 Information；在 Action 未成功完成时触发 change-info；调用 evaluate |
| `ModelContainer` | 继续独占连接、提交、回滚、关闭、clear；既有 `load(...).execute()` 是唯一物化承载 | Information DAG 编译或 P2 路径解析 |
| `dec-core-context` | 中立 immutable published aggregates 与 `EngineContext` | 依赖 `dec-core-model` 或执行 RuleView |
| `dec-core-model` | runtime engine/materializer，消费 context 的发布结果 | 重建 compiler/policy/path facts |

### 5. 核心流程、状态与失败路径

<!-- DESIGN-NARRATIVE-FLOW -->

主流程严格区分编译、识别和物化：编译失败不替换当前集合；编译成功时为每个合法 `change-info` 生成一次 RuleViewInfo、注册到 ConfigInfo 并保存到 ChangeInfo 映射；识别只读，正常条件不满足返回 `FALSE`，普通非法路径/null、权限、表达式或 RuleView 错误产生 `ERROR` 并抛异常；P5 在最后一个 Action 成功后通过该映射调用 RuleView，物化只执行其中的写入规则，成功提交并返回 `MaterializationResult` 后结束，失败由既有 ModelContainer 回滚并抛异常。两条入口相互独立，物化流程不调用识别入口。

#### 5.1 主流程

```text
discover raw definitions
  -> ReferencePass resolves symbols
  -> ModelAccessPass freezes P2 exact binding/policy
  -> InformationPass (same CompilationSession) builds InformationKey + read-set + DAG
  -> ChangeInfoMaterializationPass resolves each change-info InformationKey, builds one immutable RuleViewInfo,
     registers it with ConfigInfo and stores the ChangeInfo mapping
  -> Deferred/Semantic/Digest binds all facts and Information semantic form
  -> CompiledModelSetBuilder freezes CompiledInformationSet + policy + digest
  -> CandidateContextPublicationPass atomically publishes one EngineContext
  -> evaluate(key): consume exact binding -> read -> read-only RuleView/check -> TRUE/FALSE
  -> publish ChangeInfo mapping with EngineContext
  -> P5 after last Action success: DirectoryChangeInfoHook invokes the mapped RuleViewInfo
  -> materialize(model-atomic): prebuilt grammer Rule -> update Rule -> ModelContainer.execute()
  -> commit -> MaterializationResult -> end
```

#### 5.1.1 三类 Information 编译契约

`RawDefinitionSet` 是解析层输出的不可变候选快照；P3 在 InformationCompiler 内将其中的 Information 定义投影为编译候选。每个定义包含
`InformationKey(system, name)`、`sourceLocation`、`viewRef`、`kind`、表达式文本、
`ruleRef`、模型 `readPaths`、`changeData` 和依赖 Key 列表，并带有
`configurationRevision`。对于 `change-info`，解析器将 `information-ref` 保存到 ChangeInfo，并建立 `ChangeInfoKey(businessScopeKey, directoryKey, informationKey)`。编译器以目标模型原子的 `changeData` 构造一个固定两步的 RuleViewInfo：规则一为 `grammer`，规则二为 `update`；名称固定为 `##` + `business-config.name` + `.` + `directory.name` + `.` + `change-info.information-ref`。该对象在本次 XML/配置编译中只生成一次，通过 `ConfigInfo.addRuleViewInfo()` 注册，并将映射回填到 ChangeInfo，随后随已编译配置发布；运行时按 DirectoryInfo.ChangeInfo 取得对象，不重新生成或临时注册。编译器逐项校验，任何失败都拒绝发布候选集合并保留当前已发布集合。

| 类型 | 输入识别 | 编译与绑定 | 运行时语义 | 失败码 |
|---|---|---|---|---|
| `rule-ref`（RuleView 原子） | `view-ref` + `rule-ref`，无 model expression | 在 InformationPass 消费同一 Session 的 P2 exact binding；只读校验通过后记录 RuleView 身份、来源位置和绑定；含 `insert/update/delete/grammer` 的 RuleView 拒绝作为 evaluate binding | 运行时仅执行 `check/checkData/checkDataPattern/checkPattern/get/query` 白名单；检查不满足为 `FALSE`，非法路径/null/执行异常为 `ERROR` 并抛异常 | `INFORMATION_RULE_REF_INVALID`、`INFORMATION_READ_ONLY_VIOLATION` |
| `rule-data`（模型表达式原子） | `view-ref` + `rule-data`，可选 `change-data` | 在 InformationPass 消费 P2 exact `CompiledTargetBinding`，冻结 canonical read/write path、Data Owner 和 AST；`change-data` 只登记声明写入路径和值；被 `change-info` 引用时在同一编译批次生成一次两规则 plan | 每次按已冻结 read-set 读取当前上下文；仅声明 `change-data` 且有写权限时可物化；运行时复用已发布 plan | `INFORMATION_MODEL_EXPRESSION_INVALID`、`INFORMATION_PATH_INVALID`、`INFORMATION_WRITE_FORBIDDEN` |
| `expression`（复合 Information） | 仅 `expression` 文本 | 使用 Information Expression 编译器解析限定 InformationKey，禁止模型路径和 `change-data`；建立依赖边并校验缺失引用、跨 System 归属和循环 | 按固定 DAG 顺序求值；任何被访问节点的 `ERROR` 直接向调用方抛出，不得变成 `FALSE`；复合节点不可直接物化 | `INFORMATION_EXPRESSION_INVALID`、`INFORMATION_DEPENDENCY_INVALID`、`INFORMATION_MATERIALIZE_FORBIDDEN` |

`RawDefinitionSet` 的发布边界是全量候选原子替换：候选中任一类型、引用、路径、权限、DAG、ChangeInfo RuleView 生成/注册或映射校验失败，InformationCompiler 返回包含 `sourceLocation`、Key、路径和失败码的诊断，不产生新的 `CompiledInformationSet`。只有 ModelAccessPass、InformationPass 和 ChangeInfoMaterializationPass 均成功，且同一 Session 的所有必需事实通过 Semantic/Digest 后，才允许 `CompiledModelSetBuilder` 构造候选。

#### 5.1.2 同 Session 编译与发布闭包

| 顺序 | Pass/边界 | 本阶段冻结事实 | 失败时的原子行为 |
|---:|---|---|---|
| 1 | `ReferencePass` | symbols 与强类型 references | 不进入后续 Pass，不替换已发布 Context |
| 2 | `ModelAccessPass` | `ModelAccessCompilation`、`CompiledTargetBinding`、`ModelAccessPolicyIndex` | P2 binding/policy 任一失败，Information 不编译、不发布 |
| 3 | `InformationPass` | `CompiledInformationSet`：所有 Key、RuleView binding、model read/write path、Data Owner、DAG | 任一 Information 错误，候选集合丢弃 |
| 4 | `ChangeInfoMaterializationPass` | 每个 `ChangeInfoKey` 对应的 immutable `RuleViewInfo(grammer → update)`；按唯一名称调用 ConfigInfo.addRuleViewInfo 并回填 ChangeInfo 映射 | 任一 change-info 无法解析目标、生成规则、注册或绑定权限，候选集合丢弃 |
| 5 | `DeferredPass`/`SemanticPass` | 现有 Deferred、Definition Registry、SourceManifest | 任一失败，候选集合丢弃 |
| 6 | `DigestPass` + `CompiledModelSetBuilder` | 同一 Session 的 source/semantic digest、Information canonical form、change-info plan、P2 policy 与全部 aggregates | digest/provenance 不匹配，禁止构造 candidate |
| 7 | `CandidateContextPublicationPass` | 一个完整 `CompiledModelSet` 和由其唯一构造的 `EngineContext` | publisher/CAS 失败，旧 Context 保持不变 |

目标 Pass 顺序为 `SourceGraph → Structural → Symbols → References → ModelAccess → Information → ChangeInfoMaterialization → Deferred → Semantic → Digest → CandidateContextPublication`。`InformationPass` 不再早于 ModelAccessPass；`CompiledModelSetBuilder` 的生产构造必须同时接收 `CompiledInformationSet`、`ChangeInfoPlanIndex`、`ModelAccessPolicyIndex` 和 digest-bound input，`CompiledModelSet` 必须持有前两者，`EngineContext` 只能从该完整集合构造。最终 semantic digest 在现有 P2 policy digest 基础上纳入 `CompiledInformationSet.canonicalForm()` 与 change-info plan canonical form；runtime 仅用 `EngineContext.compiledModelSet()` 读取这些事实。

#### 5.2 关键失败路径

```text
compile error (missing ref/cycle/path/permission)
  -> reject candidate; current compiled set unchanged
evaluate error (invalid path/null/expression)
  -> ERROR diagnostic + InformationEvaluationException; no model write
materialize exception
  -> throw InformationMaterializationException; existing ModelContainer rollback; Directory execution blocks upward/downstream continuation
```

#### 5.3 状态与步骤

| 状态/步骤 | 前置条件 | 动作 | 成功结果 | 失败/恢复 |
|---|---|---|---|---|
| `RAW` | XML/P2 facts 可发现 | 读取声明 | Raw definitions | 解析错误，拒绝发布 |
| `COMPILED` | Key/ref/path/DAG/change-info 合法 | 生成不可变集合及每个 change-info 的 RuleView plan | 可识别且可按 ChangeInfoKey 物化 | 任一校验失败，保留旧集合 |
| `EVALUATING` | Key 已发布 | 按 frozen read-set 重读并求值 | `TRUE`/`FALSE` | 正常条件不满足为 `FALSE`；普通 null/非法路径/权限/表达式或 RuleView 执行异常为 `ERROR` 并抛 `InformationEvaluationException` |
| `MATERIALIZING` | 最后一个 Action 已成功，且 `ChangeInfoKey` 对应已发布 plan | 直接取得已发布 RuleView，按序执行 `grammer`、`update`，调用既有 ModelContainer.execute | 成功 commit，返回仅含 Key、变更路径和提交结果的 `MaterializationResult` 后结束 | 规则返回失败或抛异常均升级为 `InformationMaterializationException`，由 ModelContainer 回滚；不调用 evaluate 或继续目录上行/下游；复合或无权限不写入 |

### 6. 数据与持久化方案

<!-- DESIGN-NARRATIVE-DATA -->

P3 不新增表或持久化结构。`CompiledInformationSet`、exact binding、read-set、DAG、`ChangeInfoMaterializationPlan` 和结果诊断为同一发布 Context 的内存事实；模型值由当前 `ModelContext` 按已冻结路径读取，必要时通过当前 ModelContainer 持有的数据连接重新读取。物化写入仍由 `ModelContainer` 的现有事务边界承载。

1. 读取：只读取当前 Key 及依赖声明的模型路径。
2. 校验：编译 revision、P2 exact Binding、Data Owner 和路径来源必须与 `CompiledInformationSet` 匹配；runtime 不再调用 selector/resolver。
3. 编译期变更计划：对每个 `change-info` 只生成一次包含 `grammer → update` 的 RuleViewInfo，名称保留 `##` 前缀，通过 `ConfigInfo.addRuleViewInfo()` 注册并以 ChangeInfo 映射发布；不在运行期生成、注册或清理。
4. 内存变更：运行时只复用已发布 plan，生成待写入的 `change-data` intent，不维护 MutationSet。
5. 保存：最后一个 Action 成功后调用已发布 RuleView，使用既有模型写入能力；成功 commit 后返回 `MaterializationResult` 并结束，不重读或重新识别。
6. 副作用：P3 识别入口只接受编译期只读 RuleView binding，不调用 materialize；含写规则的 RuleView 在编译期拒绝。change-info 物化使用已发布的两规则 RuleView，失败不得继续目录上行或下游。
7. 提交：沿用当前框架 commit/rollback/close。
8. 失败：普通非法路径/null、权限、RuleView/表达式校验或求值异常记录 `ERROR` 诊断并抛异常；物化规则返回失败或抛异常时由既有 ModelContainer 回滚并由 Materializer 抛出，不重试、不静默吞错。

### 7. 接口、交互与兼容策略

<!-- DESIGN-NARRATIVE-COMPATIBILITY -->

调用方只依赖 `InformationEngine.evaluate` 的稳定内部契约、ChangeInfo 的已注册 RuleViewInfo 映射和独立的 `InformationMaterializer.materialize` 入口；P2 Binding、ModelContainer 和 mix XML 保持兼容，新增能力不会改变既有 RuleTests 调用方式。两个入口不合并：evaluate 只识别，materialize 只接收编译期生成的 RuleViewInfo。对于 `order-business.xml`，`paying` 的最后一个 Action `startPay` 成功后由 P5 DirectoryChangeInfoHook 直接调用 `order.paying` 对应映射。

内部接口建议如下，具体类名可在开发阶段按现有包结构落位：

```java
interface InformationEngine {
  IdentificationResult evaluate(InformationKey key, ModelContext context);
}
interface ConfiguredModelReader {
  ModelValue read(CompiledModelPath path, ModelContext context);
}
interface InformationCompiler {
  // 保留既有兼容入口；P3 production path 使用带 exact binding 的 MODIFY 入口
  InformationCompilationResult compile(RawDefinitionSet raw, SymbolTable symbols);
  InformationCompilationResult compile(
      RawDefinitionSet raw, SymbolTable symbols,
      ModelAccessCompilation bindings);
}
interface InformationMaterializer {
  MaterializationResult materialize(ChangeInfoMaterializationPlan plan, ModelContext context);
}
interface DirectoryChangeInfoHook {
  MaterializationResult afterLastAction(
      ChangeInfoKey changeInfoKey, ActionExecutionResult actionResult, ModelContext context);
}
interface ModelLoaderRuleViewExtension {
  // 兼容扩展：旧的按名称 load(...) 保持不变；P3 直接携带已发布 RuleViewInfo
  ModelLoader load(RuleViewInfo ruleView, ModelData data, String connection);
}
```

`InformationCompiler` 的现有两参数入口不删除；它只负责兼容调用，P3 production path 必须走带 `ModelAccessCompilation` 的 MODIFY 入口，并以该对象提供的 exact binding 生成 `CompiledInformationSet`。对于每个合法 `<change-info information-ref="...">`，解析阶段保存 ChangeInfo，编译器在本次 XML/配置编译中只生成一次 RuleViewInfo；名称为 `##` + `business-config.name` + `.` + `directory.name` + `.` + `change-info.information-ref`，规则顺序固定为 `grammer` 后 `update`，两条规则接收同一 `ModelLoader.value`（`GrammerExecute` 改写的 map 是 `UpdateExecute` 的输入）。编译器调用 `ConfigInfo.addRuleViewInfo()` 注册，并把同一对象保存到 ChangeInfo 映射；映射随 `EngineContext` 发布，运行时直接取得对象，不运行时生成、注册、清理或每次调用重新构造。P5 的 `DirectoryChangeInfoHook` 只在最后一个 Action 成功后调用 materialize；成功 commit + `MaterializationResult` 后结束，不调用 evaluate 或继续目录上行/下游；失败由现有 ModelContainer 回滚并抛 `InformationMaterializationException`。`evaluate` 对正常求值返回 `IdentificationResult(TRUE|FALSE)`；普通非法路径/null、表达式异常或只读 RuleView 执行异常返回 `ERROR` 诊断并抛 `InformationEvaluationException`，不允许转为 `FALSE`。复合节点按固定 DAG 顺序求值，子节点 `ERROR` 直接传播为异常；只有条件不满足才是 `FALSE`。复合 Information 不可物化并抛 `InformationMaterializationException`。P2 的显式 `<ref>` Binding、target-main 优先和 `orderDetail` 独立归属只作为 InformationCompiler 的输入事实，不在 P3 runtime 重新解析。

现有 `orm-rule.xml` 的执行证据是：`RuleParser` 接受 `grammer` 与 `update` 类型，`RuleContainer.executeAllRule()` 按 `RuleViewInfo.rules` 的声明顺序执行，`GrammerExecute` 使用 `ModelLoader.value` 作为表达式参数 map，`UpdateExecute` 使用同一 value 调用数据更新链。因此 P3 在配置编译阶段构造并发布上述 RuleViewInfo/ChangeInfoMaterializationPlan；运行时只通过兼容的直接 RuleView 接缝把已发布对象交给既有 `ModelContainer.load(loader).execute()`，不新增事务入口。

#### 7.2.1 ModelContainer 物化接入边界

现有 `ModelContainer` 的真实边界是：`load(ModelLoader)` 登记执行链；`execute()` 在 `begain()` 中建立连接，逐个调用既有 `RuleContainer`，然后由 `end(isSuccess)` 统一 `commit` 或 `roolback`，最后 `close` 并 `clear`（`ModelContainer.java:76-137,172-247`）。P3 不增加 `executeAtomically`、`ModelContainerTransaction` 或其它事务句柄。配置编译阶段为每个 `change-info` 只构造一次 `RuleViewInfo`：规则一为 `grammer`，执行 `change-data` 并改写该 loader 的 `value`；规则二为 `update`，读取同一 `value` 并写入声明的目标路径。运行时 `InformationMaterializer` 取得已发布 plan，创建本次调用专属的 `ModelLoader`（不共享可变 value），通过兼容的 `ModelLoader`/`RuleContainer` 直接 RuleView 接缝交给 `ModelContainer.load(loader).execute()`；写入失败或容器结果为失败时抛 `InformationMaterializationException`，由既有生命周期完成回滚。

物化提交成功后立即返回仅包含 InformationKey、变更路径和提交结果的 `MaterializationResult` 并结束。`InformationMaterializer` 不调用 `evaluate`，不重新识别目标或下游 Information，返回对象也不包含目标/下游 `IdentificationResult`；本 revision 不调度、不编排也不实现任何物化后重评估。物化失败回滚并抛异常，同样不调用 evaluate 或继续任何下游步骤。

#### 7.1 页面业务行为

不适用：本需求没有页面或 UI 行为。

#### 7.2 事务、并发、幂等与一致性

| 关注点 | 设计选择 | 生效边界 |
|---|---|---|
| 事务 | 复用现有 ModelContainer.load(...).execute() | 连接、提交、回滚、关闭和 clear 全由 ModelContainer 承担；P3 不新增 executeAtomically |
| 并发 | 已发布 plan 只读共享；每次调用创建自己的 ModelLoader/value | 不共享可变 loader/value；不使用全局临时 RuleView 名称，避免跨请求碰撞 |
| 幂等 | 不新增 P3 语义 | 不在 Information Engine 内维护 |
| 一致性 | 物化成功以 commit + MaterializationResult 结束；evaluate 始终是调用方显式发起的独立只读入口 | 不维护 MutationSet；change-info 不触发重读、目标/下游重识别或识别结果返回 |

### 8. 开发者交接摘要

<!-- DESIGN-NARRATIVE-HANDOFF -->

先在同一 CompilationSession 调整 ModelAccess → Information → ChangeInfoMaterialization 的 Pass 顺序，修改既有 `InformationCompiler`，把 `CompiledInformationSet` 与按 ChangeInfo 映射的 immutable RuleViewInfo 纳入 `CompiledModelSetBuilder`/`CompiledModelSet`/`EngineContext` 的原子发布闭包；随后实现只读 binding 的 `InformationEngine`、消费已注册对象的 `InformationMaterializer`、`ModelLoader`/`RuleContainer` 直接 RuleView 接缝，P4 提供 Action 结果、P5 接入 after-last-action hook，最后复用 ModelContainer 的既有 `load/execute`。普通非法路径/null、权限、表达式和 RuleView 执行异常必须保留来源位置、Key、路径和诊断并以 `ERROR` 抛出；不得添加缓存、runtime resolver、MutationSet、运行时动态 RuleView 注册或 executeAtomically。每个 change-info 的 RuleViewInfo 只能在编译期生成一次，规则固定为 `grammer → update`；`order-business.xml` 的 `startPay` 成功后由 P5 直接调用 `order.paying` 映射，成功 commit + `MaterializationResult` 后结束，严禁调用 evaluate、重新识别目标/下游或扩展返回识别字段。测试设计阶段应覆盖 16 个 Information、ChangeInfo 单次生成/注册/映射、`order.paying` 的 startPay after-last-action 触发、TRUE/FALSE/ERROR、空集合、非法路径、基础/关系 Data 归属、只读 RuleView 拒绝写规则、共享 value、成功终点和物化回滚。

#### 8.1 实施策略决策摘要

本节只保留实施策略索引；`IMPL-DEC-P3-001`～`IMPL-DEC-P3-005` 的完整定义、代码证据、兼容性和验证要求以第 9 节 `DESIGN-IMPLEMENTATION-DECISIONS` 为唯一事实源，避免同一稳定 ID 在两处出现内容差异。

| 决策 ID | 对象 | 策略 | 完整定义 |
|---|---|---|---|
| IMPL-DEC-P3-001 | ModelContainer 事务 | REUSE | 见第 9 节 |
| IMPL-DEC-P3-002 | model-access Binding | REUSE | 见第 9 节 |
| IMPL-DEC-P3-003 | Information 编译事实 | MODIFY | 见第 9 节 |
| IMPL-DEC-P3-004 | 实时识别入口 | CREATE | 见第 9 节 |
| IMPL-DEC-P3-005 | ChangeInfo RuleView 编译/注册/映射 | MODIFY | 见第 9 节 |

#### 8.2 开发开始前仍需确认

- 无新增业务决策；类名和包路径在设计 Review 通过后由开发阶段按现有模块结构确定。

## 第二部分：开发实施明细

### 9. 需求映射与总变更清单
<!-- DESIGN-CHANGE-INVENTORY -->
| 变更对象 | 变更类型 | 当前事实与证据 | 目标变化 | 作用 | Owner | 兼容要求 |
|---|---|---|---|---|---|---|
| CompiledInformationSet | ADD | 当前无统一 Information 编译集合 | 保存不可变 Key/read-set/DAG | 统一配置事实 | dec-core-compiler | 不改变 P2 Binding |
| InformationEngine | ADD | 当前无统一识别入口 | 提供 boolean 识别与诊断 | 统一求值 | dec-core-model | 不维护缓存、不解析路径 |
| InformationMaterializer | ADD | 当前无 P3 物化契约 | commit 后返回 `MaterializationResult` 并结束 | 受保护物化 | dec-core-model | 复用现有事务；不调用 evaluate 或返回识别结果 |
| CompiledModelSet / EngineContext | MODIFY | 当前发布闭包只含 P2 runtime aggregates | 同 Session 携带 `CompiledInformationSet`、`ChangeInfoPlanIndex` 与最终 digest | 原子消费边界 | dec-core-context | 不改变旧 modelSet 别名 |
| ChangeInfoMaterializationPlan / ChangeInfoPlanIndex | ADD | 当前 change-info 无已注册执行计划 | 编译期为每个 change-info 生成一次可复用 RuleViewInfo，通过 ConfigInfo.addRuleViewInfo() 注册并按 ChangeInfoKey 映射 | change-info 物化计划 | dec-core-context | immutable；不做运行时动态注册 |
| DirectoryChangeInfoHook | ADD | 当前没有 change-info after-last-action 接缝 | 最后一个 Action 成功后取得已发布 plan 并调用 materialize | 目录执行接入 | dec-core-model | 只消费已发布 plan；失败阻断后续 |
| BM-P3-R04 | REUSE | 当前业务模型 revision | 作为设计输入 | 需求承接 | P3 | 保持语义一致 |

<!-- DESIGN-IMPLEMENTATION-DECISIONS -->
| 决策 ID | 对象 | 策略 | 现有候选与代码证据 | REUSE 不适用理由 | COMPATIBLE_EXTEND 不适用理由 | MODIFY 不适用理由 | 公共逻辑处理 | 公共逻辑 Owner | 兼容、调用方与验证 | 关联蓝图 |
|---|---|---|---|---|---|---|---|---|---|---|
| IMPL-DEC-P3-001 | ModelContainer 事务 | REUSE | `dec-core-model/.../ModelContainer.java`、`dec-core-model/.../execute/rule/RuleContainer.java`、`dec-demo/src/main/resources/model/test-rule/orm-rule.xml` | 已有 `load(...).execute()` 按 RuleView 顺序执行并统一提交/回滚；P3 不需要新增事务入口 | 不需要扩展入口 | 不需要修改现有事务 | REUSE_SHARED | dec-core-model | 旧 RuleTests 不变；生成 `grammer → update` 失败回滚测试 | BP-P3-004 |
| IMPL-DEC-P3-002 | model-access Binding | REUSE | `ModelAccessCompiler.java` | P2 已发布完整 Binding | P3 不增加授权入口 | 不修改 P2 解析 | REUSE_SHARED | dec-core-compiler | 显式 ref 兼容；路径测试 | BP-P3-002 |
| IMPL-DEC-P3-003 | Information 编译集合 | MODIFY | `dec-core-compiler/src/main/java/dec/core/compiler/information/InformationCompiler.java`（已有骨架） | 现有骨架未覆盖三类输入、P2 exact binding 和发布集合 | 保留现有 Deferred 编译调用方并兼容扩展 | 不删除现有编译入口；回归既有 Deferred 调用方与编译失败路径 | REUSE_SHARED | dec-core-compiler | 保留既有调用方；新增绑定/DAG/发布闭包回归测试 | BP-P3-001 |
| IMPL-DEC-P3-004 | 实时识别入口 | CREATE | proposed `dec-core-model/.../InformationEngine.java` | 当前没有统一识别入口 | 没有兼容接口可扩展 | 不存在可修改的 P3 组件 | EXTRACT_NEW | dec-core-model | 新增 TRUE/FALSE/ERROR 契约；只读 RuleView 复用测试 | BP-P3-003 |
| IMPL-DEC-P3-005 | change-info RuleView 计划 | MODIFY | `ChangeInfo`、`RuleViewInfo`、`ModelLoader`、`RuleContainer` 现有模型与 proposed `ChangeInfoMaterializationPlan`/`ChangeInfoPlanIndex`/`DirectoryChangeInfoHook` | 现有 change-info 只保存表达式，未形成可复用 RuleView 计划 | 保留按名称 `ModelLoader.load(...)`；增加携带已发布 RuleViewInfo 的兼容入口，RuleContainer 优先直接对象 | 不修改既有 ModelContainer 事务生命周期 | REUSE_SHARED | dec-core-compiler + dec-core-context + dec-core-model | 编译期单次生成、ChangeInfoKey 隔离、直接复用、旧调用方回归和并发调用测试 | BP-P3-005 |

### 10. 表、字段与数据读写明细
<!-- DESIGN-TABLE-CHANGES -->
| 表/存储对象 | 变更类型 | 作用与 Owner | 新增/修改内容 | 迁移/回填 | DB 增量与实现锚点 |
|---|---|---|---|---|---|
| 既有 ModelData | UNCHANGED | 模型值承载；dec-core-model | 不新增字段 | 不适用（无 DB 变化） | DB-N/A；`ModelContainer.java` |
<!-- DESIGN-FIELD-CHANGES -->
| 表 | 字段 | 变更类型 | 业务语义与作用 | 值来源 | 创建时写入 | 更新条件 | 是否可变 |
|---|---|---|---|---|---|---|---|
| 既有 ModelData | change-data 目标路径 | UNCHANGED | 配置声明的模型值 | InformationMaterializer | 既有模型写入 | 仅声明 change-data 时 | 是 |
<!-- DESIGN-DATA-ACCESS -->
| 业务动作 | 触发入口 | 表/聚合 | 操作 | 字段 | 查询/更新条件 | 读取一致性/锁 | 写入时机与保存方式 | 事务 | 失败后的事实 | Trace |
|---|---|---|---|---|---|---|---|---|---|---|
| 识别 Information | `evaluate` | Information evaluation | READ | compiler-frozen read-set | 当前 Key 和依赖路径 | 当前上下文值 | 不写入 P3 materialize | 不适用：识别只读 | 正常不满足为 `FALSE`；非法路径/null/执行异常为 `ERROR` + exception | TR-P3-INFORMATION-ENGINE-001 |
| 物化模型原子 | `materialize` | ModelData | WRITE | change-data 目标 | 声明值且有写权限 | ModelContainer 既有事务 | `grammer` 后 `update`，成功提交 | ModelContainer.load(...).execute() | 规则失败/异常升级为 exception 并回滚 | TR-P3-INFORMATION-ENGINE-001 |

### 11. 接口与字段映射明细
<!-- DESIGN-API-CHANGES -->
| 接口/方法 | 类型 | 调用方 | 请求变化 | 响应变化 | 校验与权限 | 幂等 | 错误码 | 事务边界 | 兼容策略 | API 增量与实现锚点 |
|---|---|---|---|---|---|---|---|---|---|---|
| `InformationCompiler.compile` | 内部新增 | 配置编排 | RawInformationSet + P2 bindings | CompiledInformationSet/诊断 | 编译校验 | 不适用：无副作用 | `INFORMATION_DEFINITION_INVALID` | 不适用：内存编译 | 不影响旧入口 | API-N/A；`InformationCompiler` |
| `InformationEngine.evaluate` | 内部新增 | P4/P5 | InformationKey + ModelContext | TRUE/FALSE + diagnostics/read evidence；路径/null/权限/表达式/RuleView 错误记录 ERROR 并抛异常 | compiler-frozen read-only binding/read-set | 不适用：无 P3 写入 | `INFORMATION_PATH_INVALID` 等 `ERROR` 诊断 | 不适用：只读识别入口 | 新增识别边界；编译期拒绝写规则 | API-N/A；`InformationEngine` |
| `InformationMaterializer.materialize` | 内部新增 | P5 编排 | ChangeInfo 映射 + 模型原子 context | 仅含 Key、变更路径、提交结果的 MaterializationResult/异常 | change-data 写权限 | 不新增 | `INFORMATION_MATERIALIZE_FAILED` | ModelContainer.load(...).execute() | 消费编译期注册的 `grammer → update` RuleView；成功后结束且不调用 evaluate | API-N/A；`InformationMaterializer` |

### 12. 行为调整与代码改动
<!-- DESIGN-BEHAVIOR-ADJUSTMENTS -->
| 功能/场景 | 当前行为与证据 | 调整后行为 | 受影响入口/调用方 | 数据策略 | API/UI 策略 | 回归范围 |
|---|---|---|---|---|---|---|
| Information 识别 | 无统一入口 | evaluate 消费 exact read-only binding、实时读取并返回 TRUE/FALSE；错误抛 ERROR 异常 | 新增 P3 调用方 | 不缓存/不重解析 | 内部 API；无 UI | 编译/识别测试 |
| change-data 物化 | 无 P3 契约 | 只写声明值；成功 commit + MaterializationResult 后结束，失败回滚并抛异常 | InformationMaterializer | 既有 ModelContainer | 内部 API；无 UI；不返回识别结果 | 成功终点/回滚测试 |
<!-- DESIGN-CODE-BLUEPRINT -->
| 蓝图 ID | 顺序 | 仓库/模块 | 文件、类或配置 | 变更类型 | 方法/符号 | 决策 ID | 具体改动 | 作用 | 公共逻辑 Owner | 来源文档 | 对应测试 | 依赖 |
|---|---:|---|---|---|---|---|---|---|---|---|---|---|
| BP-P3-001 | 1 | dec-core-compiler | existing `InformationCompiler.java` | MODIFY | `compile` | IMPL-DEC-P3-003 | 消费 ModelAccess exact binding，生成 Key/read-set/DAG/CompiledInformationSet；保留既有 Deferred 调用方 | 编译事实 | dec-core-compiler | `..._business_model.md#核心术语` | 编译/发布闭包测试 | P2 ModelAccessPass |
| BP-P3-002 | 2 | dec-core-compiler/context | existing `StandardCompilerPasses.java`, `CompiledModelSetBuilder.java`, `CompiledModelSet.java`, `EngineContext.java` | MODIFY | pass order / builder / aggregate | IMPL-DEC-P3-003 | ModelAccess → Information；同 Session 携带 Information、policy、digest；一次构造/发布 | 原子 Context 闭包 | dec-core-compiler + dec-core-context | `..._business_model.md#对象与聚合` | 顺序/digest/identity 测试 | BP-P3-001 |
| BP-P3-003 | 3 | dec-core-model | proposed `InformationEngine.java` | ADD | `evaluate` | IMPL-DEC-P3-004 | 消费 `CompiledInformationSet` exact read-only binding；仅执行 check/checkData/checkDataPattern/checkPattern/get/query；正常不满足为 FALSE，非法路径/null/权限/表达式/RuleView 执行异常抛 ERROR | 识别入口 | dec-core-model | `..._business_model.md#业务不变量` | TRUE/FALSE/ERROR 与只读 RuleView 测试 | BP-P3-001/BP-P3-002 |
| BP-P3-004 | 4 | dec-core-model | proposed `InformationMaterializer.java` | ADD | `materialize` | IMPL-DEC-P3-001 | 消费已注册 ChangeInfo RuleViewInfo，按 `grammer` → `update` 共享 ModelLoader.value 执行既有 ModelContainer.load(...).execute()；成功返回 MaterializationResult 后结束，失败抛异常并依赖既有回滚；不修改 ModelContainer.java | 物化边界 | dec-core-model | `..._business_model.md#识别与物化状态` | 共享 value/顺序/成功终点/回滚测试 | BP-P3-002/BP-P3-005 |
| BP-P3-005 | 5 | dec-core-compiler/context/model | proposed `ChangeInfoMaterializationPlan`/`ChangeInfoPlanIndex`、compatible `ModelLoader`/`RuleContainer` seam、`DirectoryChangeInfoHook` | ADD + COMPATIBLE_EXTEND | compile/publish/afterLastAction | IMPL-DEC-P3-005 | 配置编译完成时为每个 change-info 生成一次 immutable RuleViewInfo，按 ChangeInfoKey 隔离并随 EngineContext 发布；最后一个 Action 成功后直接复用已发布对象，不全局动态注册、不重复生成、不调用 evaluate | change-info 调用接入 | dec-core-compiler + dec-core-context + dec-core-model | `..._business_model.md#识别与物化状态`、`order-business.xml` | `order-business.xml` 的 `order.paying/startPay`；单次生成、复用、Action 失败不触发、并发隔离与旧 load 兼容测试 | BP-P3-002 |

### 13. 异常、安全、观测与验证明细
<!-- DESIGN-VERIFICATION -->
| 验证项 | 对应需求/风险 | 测试层级 | 前置数据 | 操作 | 失败注入/边界 | 可观察结果 | 自动化命令或证据入口 |
|---|---|---|---|---|---|---|---|
| 编译/DAG/发布闭包 | BR-P3-INFORMATION-ENGINE-001/TR-P3-INFORMATION-ENGINE-001 | 单元/集成 | 16 Information + P2 exact bindings | compile/publish | 缺失/循环/混合/越权、pass 顺序、旧 Context 保持 | 同 Session 的 `CompiledInformationSet`、policy、digest 共同进入一个 `CompiledModelSet`/`EngineContext`，失败不发布 | test_design 阶段 |
| 实时读取 | BR-P3-INFORMATION-ENGINE-003/TR-P3-INFORMATION-ENGINE-001 | 集成 | 同一 Key 两次改变模型值 | evaluate | 验证第二次读新值、runtime 不解析 path | 第二次结果反映新值且带实际 readPaths/evidence | test_design 阶段 |
| null/空集合/路径/组合 | BR-P3-INFORMATION-ENGINE-002/TR-P3-INFORMATION-ENGINE-001 | 单元 | null、empty、非法路径、组合节点 | evaluate | 只读 RuleView 写规则、非法路径/null/权限、订单明细前置 | `every(emptyCollection)=TRUE`；订单明细前置不满足为 FALSE；非法路径/null/权限/表达式/RuleView 异常为 ERROR + exception | test_design 阶段 |
| 物化规则与成功终点 | AC-P3-INFORMATION-ENGINE-001/TR-P3-INFORMATION-ENGINE-001 | 集成 | 编译期已发布的 `order.paying` change-info plan 与可提交 change-data | materialize | 监测编译期只生成一次 RuleView、最后一个 Action 成功后复用、`grammer` 先于 `update`、共享 value、evaluate 调用次数和返回字段 | commit 一次；返回 MaterializationResult；evaluate 调用次数为 0；无目标/下游识别字段 | test_design 阶段 |
| 物化失败与回滚 | AC-P3-INFORMATION-ENGINE-001/TR-P3-INFORMATION-ENGINE-001 | 集成 | change-data 写入异常 | materialize | grammer/update/persistence/commit 任一点失败 | 更新失败抛异常，ModelContainer 回滚，不调用 evaluate 或继续任何下游步骤 | test_design 阶段 |

### 14. 测试接缝

- `InformationCompiler`：输入 Raw definitions 和同 Session 的 ModelAccessCompilation，输出不可变集合，可独立断言 Key、exact binding、DAG、change-info plan 单次生成和路径诊断；不新增同名 compiler 文件。
- `ConfiguredModelReader`：以真实 ModelContext 或受控 reader 提供当前值，验证每次调用重新读取。
- `InformationEngine`：通过编译期只读 RuleView binding 和受控模型 reader，断言 TRUE/FALSE、ERROR exception 和 readPaths；含 insert/update/delete/grammer 的 RuleView 必须在编译期拒绝。
- `InformationMaterializer`：断言复用已发布 RuleView 的 `grammer` → `update` 顺序及每次调用独立的 `ModelLoader.value`，调用真实 ModelContainer.load(...).execute()，验证成功 commit + MaterializationResult 后结束、evaluate 调用次数为 0、返回对象无目标/下游识别字段，以及异常传播、rollback/close/clear 和失败后不继续下游。
- `DirectoryChangeInfoHook`：使用 `order-business.xml` 的 `order.paying`/`startPay` fixture，断言最后一个 Action 成功后调用一次已发布 plan；Action 失败、plan 缺失或过期时不执行写入。

### 15. 兼容、迁移与回滚

不涉及数据库迁移、外部 API 或 XML 迁移。编译失败保留上一份完整 `EngineContext`（其中的 `CompiledInformationSet`、P2 policy 和 digest 不拆分替换）；设计或实现回滚只需移除新增 P3 组件，不改变 P2 Binding、ModelContainer 的既有事务语义。旧 `RuleTests.java` 继续保持原有调用语义。

### 16. 追踪与验证

当前设计 revision `DESIGN-P3-R08` 绑定输入 `REQAN-P3-R05`、`BM-P3-R04` 与 `FLOW-R08@p3-information-evaluation`，并以 `FLOW-P3-INFORMATION-EVALUATION` 的主路径、变体和失败路径作为 `flow_ref`/`diagram_ref` 设计证据。R08 明确 `change-info` RuleViewInfo 在 XML/配置编译完成时只生成一次，通过 `ConfigInfo.addRuleViewInfo()` 注册并保存到 ChangeInfo 映射；最后一个 Action 成功后由 P5 直接复用该对象，不运行时生成。所有设计决策、接口和测试接缝均追踪至 `TR-P3-INFORMATION-ENGINE-001` 与 `AC-P3-INFORMATION-ENGINE-001`。本阶段只完成设计结构验证，不宣称已执行 Information 测试或 Design Review；实现后必须在 test_design/development/testing 阶段补充真实测试证据，ArchitectureReviewAgent 另行审查本 revision。

## 附录：追踪、决策与未决项

- 追踪：`TR-P3-INFORMATION-ENGINE-001` → `AC-P3-INFORMATION-ENGINE-001`。
- 决策：`IMPL-DEC-P3-001` 至 `IMPL-DEC-P3-005`；其中 `IMPL-DEC-P3-001` 固定为复用现有 ModelContainer.load(...).execute()，`IMPL-DEC-P3-003` 固定为 MODIFY，`IMPL-DEC-P3-002` 固定为 compiler-owned exact binding，`IMPL-DEC-P3-004` 固定为 dec-core-model runtime read-only engine + materializer，`IMPL-DEC-P3-005` 固定为编译期一次生成 ChangeInfoMaterializationPlan 并通过兼容直接 RuleView 接缝复用。
- Review remediation：`ISSUE-MR-0017`～`ISSUE-MR-0019` 和 `ISSUE-MR-0025` 由本 revision 绑定当前 Requirement、Business Model、Flow、Design 和代码 Evidence 后提交独立 Architecture Review；本阶段 Owner 仅登记 remediated，不自行验证或关闭。
- 未决项：无业务语义未决项；实现阶段只需按本设计落位既有 compiler/model/context 包路径，不得恢复 runtime `ModelPathResolver` 或三态错误传播，不得把 change-info RuleView 改回运行时临时生成。

### 附录 A：R03 finding 收敛对照

| Issue | 本 revision 的闭合设计事实 |
|---|---|
| `ISSUE-MR-0009` | ModelAccess → Information 的固定 Pass 顺序；`CompiledInformationSet` 纳入 `CompiledModelSet`/`EngineContext`；同 Session 的 P2 exact binding、Information、policy、digest 一起原子发布；context 只承载中立聚合，model 只消费。 |
| `ISSUE-MR-0010` | 历史 R03 方案：通过 `executeAtomically` seam 接入（已被 R05 用户决策替换，保留作历史记录）。 |
| `ISSUE-MR-0011` | 历史 R03 方案：对外仅 boolean 且错误转 false（已被 R05 用户决策替换，普通非法路径/null 改为 ERROR + exception）。 |
| `ISSUE-MR-0012` | `InformationCompiler` 在所有清单中固定为 MODIFY；`materialize` 只属于独立 `InformationMaterializer`，`InformationEngine` 只负责 evaluate；既有 Deferred 调用方保留。 |
| `ISSUE-MR-0013` | 删除 runtime `ModelPathResolver`；target-main/关系 Data 归属和 `CompiledTargetBinding` 只在 compiler 冻结并进入 `CompiledInformationSet`，runtime 只消费 exact 结果。 |

### 附录 B：R04/R06 finding 收敛对照（R08）

| Issue | R08 处理事实 |
|---|---|
| `ISSUE-MR-0017` | `FLOW-P3-INFORMATION-EVALUATION` 和本设计均固定：不新增 `executeAtomically` 或事务句柄；`materialize` 生成同一 RuleView 的 `grammer → update` 规则链，共享一个 `ModelLoader.value`，由现有 `ModelContainer.load(...).execute()` 统一 commit/rollback/close/clear；成功返回 `MaterializationResult` 后结束，失败回滚、抛异常且阻断下游。 |
| `ISSUE-MR-0018` | `evaluate` 只消费编译期通过的 read-only RuleView binding；`insert/update/delete/grammer` 规则在编译期拒绝，运行时只允许 `check/checkData/checkDataPattern/checkPattern/get/query`；普通条件不满足为 FALSE，非法路径/null/权限/表达式或 RuleView 执行异常为 ERROR + `InformationEvaluationException`，并阻断 materialize/下游。 |
| `ISSUE-MR-0019` | `ModelContainer` 策略固定为 REUSE，`BP-P3-004` 明确不修改 `ModelContainer.java`；`BP-P3-005` 固定 change-info RuleView 在编译期生成一次、按 `ChangeInfoKey` 随 EngineContext 发布，运行时通过兼容直接 RuleView 接缝复用；每次调用只创建独立 ModelLoader/value。成功路径不增加重评估状态、目标/下游识别或返回字段。 |
| `ISSUE-MR-0025` | R08 采用用户确认的编译期方案：解析 ChangeInfo 时保存 information-ref 和映射；编译完成时一次生成名称为 `##` + business-config.name + `.` + directory.name + `.` + information-ref 的 RuleViewInfo，通过 ConfigInfo.addRuleViewInfo() 注册；P5 在最后一个 Action 成功后直接复用，失败不触发 change-info。
