# P3 Information Engine 技术设计

> Design revision：`DESIGN-P3-R02`
> Base revision：`BM-P3-R01`
> Design topic：`DESIGN-P3-INFORMATION-ENGINE`
> Requirement：`REQAN-P3-R01@5b4727fc5db4`
> Module：`P3-INFORMATION-ENGINE`
> Implementation strategy contract：`1`
> 状态：READY

## 第一部分：设计正文

### 1. 一页设计摘要

<!-- DESIGN-NARRATIVE-SUMMARY -->

本设计把 P3 Information Engine 分为配置编译、实时识别和受保护物化三条边界。编译阶段将 `mix` 的三类 Information 转为不可变 `CompiledInformationSet`，建立限定 `InformationKey`、模型 read-set 和无环依赖图；识别阶段每次按 read-set 从当前模型/事务上下文重新读取，不维护 `MutationSet` 或 reverse-DAG 缓存；物化阶段只允许模型表达式原子写入声明的 `change-data` 值，写入后再次读取并识别，失败抛异常并交给现有框架回滚。

#### 1.1 改造前后对照

| 关注点 | 当前情况 | 调整后 | 带来的价值 |
|---|---|---|---|
| Information 事实 | 仅存在 XML 声明，缺少统一运行契约 | 编译为不可变 Information 定义和 Key | P4/P5 可稳定消费 |
| 求值 | 规则、模型字段和复合引用没有统一入口 | 原子先求值，复合按 DAG 拓扑短路 | 结果语义一致 |
| 数据读取 | 可能依赖旧值或调用方缓存 | 每次按配置重新读取当前值 | 与事务内真实数据一致 |
| 物化 | Change 语义未形成 P3 契约 | 仅 `change-data` 写入，失败异常回滚 | 禁止错误伪装和部分成功 |

#### 1.2 六个关键结论

| 读者最关心的问题 | 当前结论 |
|---|---|
| 本次改变什么 | 增加 Information 编译、识别、依赖和物化契约 |
| 保持什么不变 | P2 归属/权限、现有 ModelContainer 事务、XML fixture 不变 |
| 主流程如何工作 | parse → compile → publish → evaluate → optional materialize |
| 失败时留下什么事实 | 稳定 `ERROR`、诊断和读路径；物化失败无提交并抛异常 |
| 最关键的技术决策 | `InformationEngine` 无状态实时读取，不维护 MutationSet |
| 如何证明设计完成 | 设计 Review 覆盖 BR/AC/TR、接口、失败路径和测试接缝 |

### 2. 背景、现状与设计目标

<!-- DESIGN-NARRATIVE-CURRENT -->

当前 `mix` 已声明 16 个 Information，但 P3 尚无统一的编译产物和识别入口。P2 已提供 System、View、RuleView 和 model-access Binding；现有 `ModelContainer`/`TransactionContainer` 已负责执行、提交、回滚和资源清理。本设计只定义 P3 接入和调用边界，不把 P2 解析器、事务承载或测试实现提前纳入。

#### 2.1 当前实现证据

| 能力/入口 | 当前锚点 | 本次处置 |
|---|---|---|
| 规则执行与回滚 | `dec-core-model/src/main/java/dec/core/model/container/ModelContainer.java`、`.../execute/tran/TransactionContainer.java` | REUSE，物化失败直接抛异常 |
| model-access Binding | `dec-core-compiler/.../ModelAccessCompiler.java`、`.../DefaultModelAccessSelectorResolver.java` | REUSE，消费显式 ref 和 target-main 优先规则 |
| mix fixture | `dec-demo/src/main/resources/mix/system/systems.xml` | REUSE，作为 16 Information 基线 |
| 旧 RuleTests 调用链 | `dec-demo/src/test/java/dec/demo/model/RuleTests.java` | 仅作为执行链参考，不作为 P3 测试证据 |

#### 2.2 设计目标与非目标

- 目标：形成不可变编译事实、实时 read-set、DAG 求值、路径解析、结果证据和 change-data 物化契约。
- 非目标：不实现 Action/Produce/Directory/Query，不维护 MutationSet/reverse-DAG，不新增事务、幂等或并发机制，不修订 P2 文档或 XML。
- 必须保持：普通 null 和非法路径为 `ERROR`；显式 `InformationKey = null` 比较为例外；`every(emptyCollection)=TRUE` 但订单明细必须非空。

### 3. 影响范围与明确边界

| 范围 | 是否变化 | 说明 |
|---|---|---|
| dec-core-compiler | 是 | 增加 Information 定义编译和 DAG 校验边界 |
| dec-core-model/context | 是 | 增加只读识别和物化编排接口 |
| dec-context-config-parse-xml | 否 | 继续提供 Raw 声明，不修改解析语法 |
| 数据库表/字段 | 否 | 只读取/写入既有模型路径 |
| API | 新增内部契约 | 不承诺外部 HTTP/API；面向 P4/P5 的稳定 Java 只读边界 |
| 测试 | 后续阶段 | 本阶段只定义测试接缝，不创建测试 Case |

范围外还包括 P2 model-access 兼容修订、现代 YAML、Consumer runtime、独立 Session/Transaction、幂等和并发控制。

### 4. 目标方案与职责边界

<!-- DESIGN-NARRATIVE-TARGET -->

```text
Raw XML declarations + P2 published bindings
        -> InformationCompiler
        -> CompiledInformationSet (immutable)
        -> InformationEngine.evaluate(key, ModelContext)
             -> ConfiguredModelReader.read(path)
             -> AtomicEvaluator / CompositeEvaluator(DAG)
             -> IdentificationResult(TRUE|FALSE|ERROR)
        -> InformationMaterializer (only model atomic change-data)
             -> existing ModelContainer transaction
             -> reread + re-evaluate target/downstream
```

| 组件 | 调整后职责 | 不应承担的职责 |
|---|---|---|
| `InformationCompiler` | 类型互斥、Key、路径、DAG、权限校验 | 运行时读写模型 |
| `CompiledInformationSet` | 保存不可变 Information、read-set、依赖拓扑 | 缓存运行结果 |
| `InformationEngine` | 按 Key 实时读取并返回结果 | 修改模型、维护 MutationSet |
| `ModelPathResolver` | target-main 基础字段优先，relation Data 独立解析 | 跨 View 模糊搜索 |
| `InformationMaterializer` | 校验 change-data、写声明值、触发重读 | 自建事务、吞异常 |
| `ModelContainer` | 既有提交/回滚/关闭 | Information DAG 编译 |

### 5. 核心流程、状态与失败路径

<!-- DESIGN-NARRATIVE-FLOW -->

主流程严格区分编译、识别和物化：编译失败不替换当前集合，识别失败只产生 ERROR，物化失败由现有事务回滚且不继续下游。

#### 5.1 主流程

```text
discover raw definitions
  -> validate kind/ref/path/permission
  -> build InformationKey + read-set + dependency DAG
  -> publish immutable CompiledInformationSet
  -> evaluate(key): read configured paths -> evaluate atomics -> evaluate composite
  -> optional materialize(model-atomic): write change-data -> reread -> evaluate
```

#### 5.2 关键失败路径

```text
compile error (missing ref/cycle/path/permission)
  -> reject candidate; current compiled set unchanged
evaluate error (invalid path/null/expression)
  -> IdentificationResult.ERROR + diagnostic; no model write
materialize exception
  -> throw; existing ModelContainer rollback; do not evaluate downstream
```

#### 5.3 状态与步骤

| 状态/步骤 | 前置条件 | 动作 | 成功结果 | 失败/恢复 |
|---|---|---|---|---|
| `RAW` | XML/P2 facts 可发现 | 读取声明 | Raw definitions | 解析错误，拒绝发布 |
| `COMPILED` | Key/ref/path/DAG 合法 | 生成不可变集合 | 可识别 | 任一校验失败，保留旧集合 |
| `EVALUATING` | Key 已发布 | 按 read-set 重读并求值 | TRUE/FALSE | 普通 null/非法路径为 ERROR |
| `MATERIALIZING` | 模型原子 + write 权限 | 写 change-data | 写后重读 | 抛异常、回滚、不继续下游 |

### 6. 数据与持久化方案

<!-- DESIGN-NARRATIVE-DATA -->

P3 不新增表或持久化结构。`CompiledInformationSet`、read-set、DAG 和结果证据为内存事实；模型值由当前 `ModelContext` 的路径读取，必要时通过既有数据连接重新读取。物化写入仍由 `ModelContainer` 的现有事务边界承载。

1. 读取：只读取当前 Key 及依赖声明的模型路径。
2. 校验：编译 revision、P2 Binding 和路径来源必须匹配。
3. 内存变更：只生成待写入的 `change-data` intent，不维护 MutationSet。
4. 保存：调用既有模型写入能力；成功后立即按配置重新读取。
5. 副作用：识别阶段无副作用；物化失败不得继续下游。
6. 提交：沿用当前框架 commit/rollback/close。
7. 失败：抛出异常并返回 ERROR 诊断，不重试、不静默降级。

### 7. 接口、交互与兼容策略

<!-- DESIGN-NARRATIVE-COMPATIBILITY -->

调用方只依赖 InformationEngine 的稳定内部契约；P2 Binding、ModelContainer 和 mix XML 保持兼容，新增能力不会改变既有 RuleTests 调用方式。

内部接口建议如下，具体类名可在开发阶段按现有包结构落位：

```java
interface InformationEngine {
  IdentificationResult evaluate(InformationKey key, ModelContext context);
  MaterializationResult materialize(InformationKey key, ModelContext context);
}
interface ConfiguredModelReader {
  ModelValue read(ModelPath path, ModelContext context);
}
interface InformationCompiler {
  CompiledInformationSet compile(RawInformationSet raw, PublishedBindings bindings);
}
```

`evaluate` 只读；`materialize` 只接受声明 `change-data` 的模型表达式原子。复合 Information 直接返回稳定错误。P2 的显式 `<ref>` Binding、target-main 优先和 `orderDetail` 独立归属作为输入事实，不在 P3 重新解析。

#### 7.1 页面业务行为

不适用：本需求没有页面或 UI 行为。

#### 7.2 事务、并发、幂等与一致性

| 关注点 | 设计选择 | 生效边界 |
|---|---|---|
| 事务 | 复用现有 ModelContainer | 仅 change-data 物化 |
| 并发 | 不新增 P3 语义 | 按项目既有框架处理 |
| 幂等 | 不新增 P3 语义 | 不在 Information Engine 内维护 |
| 一致性 | 写后重新读取当前值 | 物化成功后再识别 |

### 8. 开发者交接摘要

<!-- DESIGN-NARRATIVE-HANDOFF -->

先实现不可变 Key/定义/依赖图，再实现 `ModelPathResolver` 和只读求值器，最后接入现有 ModelContainer 的 change-data 写入。所有错误必须保留来源位置、Key、路径和诊断；不得添加缓存或吞异常。测试设计阶段应覆盖 16 个 Information、null/空集合、非法路径、基础/关系 Data 归属和物化回滚。

#### 8.1 实施策略决策

| 决策 ID | 对象 | 策略 | 代码证据 | 选择理由 | 公共逻辑 Owner |
|---|---|---|---|---|---|
| IMPL-DEC-P3-001 | ModelContainer 事务 | REUSE | `dec-core-model/.../ModelContainer.java` | 已提供提交/回滚/关闭，P3 不重复实现 | dec-core-model |
| IMPL-DEC-P3-002 | model-access Binding | REUSE | `ModelAccessCompiler.java`、`DefaultModelAccessSelectorResolver.java` | P2 已发布 target-main/path 事实 | dec-core-compiler |
| IMPL-DEC-P3-003 | Information 编译事实 | CREATE | 当前无 Information 编译产物 | 需建立唯一 P3 Owner 和不可变 DAG | dec-core-compiler |
| IMPL-DEC-P3-004 | 实时识别入口 | CREATE | 当前无统一 InformationEngine | 需统一 TRUE/FALSE/ERROR 和证据输出 | dec-core-model/context |

#### 8.2 开发开始前仍需确认

- 无新增业务决策；类名和包路径在设计 Review 通过后由开发阶段按现有模块结构确定。

## 第二部分：开发实施明细

### 9. 需求映射与总变更清单
<!-- DESIGN-CHANGE-INVENTORY -->
| 变更对象 | 变更类型 | 当前事实与证据 | 目标变化 | 作用 | Owner | 兼容要求 |
|---|---|---|---|---|---|---|
| CompiledInformationSet | ADD | 当前无统一 Information 编译集合 | 保存不可变 Key/read-set/DAG | 统一配置事实 | dec-core-compiler | 不改变 P2 Binding |
| InformationEngine | ADD | 当前无统一识别入口 | 提供 TRUE/FALSE/ERROR 识别 | 统一求值 | dec-core-model/context | 不维护缓存 |
| InformationMaterializer | ADD | 当前无 P3 物化契约 | 写入后重读并识别 | 受保护物化 | dec-core-model | 复用现有事务 |
| BM-P3-R01 | REUSE | 当前业务模型 revision | 作为设计输入 | 需求承接 | P3 | 保持语义一致 |

<!-- DESIGN-IMPLEMENTATION-DECISIONS -->
| 决策 ID | 对象 | 策略 | 现有候选与代码证据 | REUSE 不适用理由 | COMPATIBLE_EXTEND 不适用理由 | MODIFY 不适用理由 | 公共逻辑处理 | 公共逻辑 Owner | 兼容、调用方与验证 | 关联蓝图 |
|---|---|---|---|---|---|---|---|---|---|---|
| IMPL-DEC-P3-001 | ModelContainer 事务 | REUSE | `dec-core-model/.../ModelContainer.java` | 已有事务能力满足 P3 | 不需要扩展入口 | 不需要修改现有事务 | REUSE_SHARED | dec-core-model | 旧 RuleTests 不变；回滚测试 | BP-P3-004 |
| IMPL-DEC-P3-002 | model-access Binding | REUSE | `ModelAccessCompiler.java` | P2 已发布完整 Binding | P3 不增加授权入口 | 不修改 P2 解析 | REUSE_SHARED | dec-core-compiler | 显式 ref 兼容；路径测试 | BP-P3-002 |
| IMPL-DEC-P3-003 | Information 编译集合 | CREATE | proposed `dec-core-compiler/.../InformationCompiler.java` | 当前没有 Information 集合 | 没有兼容接口可扩展 | 不存在可修改的 P3 组件 | EXTRACT_NEW | dec-core-compiler | 新增内部契约；编译测试 | BP-P3-001 |
| IMPL-DEC-P3-004 | 实时识别入口 | CREATE | proposed `dec-core-context/.../InformationEngine.java` | 当前没有统一识别入口 | 没有兼容接口可扩展 | 不存在可修改的 P3 组件 | EXTRACT_NEW | dec-core-context | 新增内部契约；三态测试 | BP-P3-003 |

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
| 识别 Information | `evaluate` | Information evaluation | READ | read-set | 当前 Key 和依赖路径 | 当前上下文值 | 不写入 | 不适用：识别只读 | ERROR + diagnostic | TR-P3-INFORMATION-ENGINE-001 |
| 物化模型原子 | `materialize` | ModelData | WRITE | change-data 目标 | 声明值且有写权限 | 事务内一致读取 | 写后重读 | ModelContainer | 异常并回滚 | TR-P3-INFORMATION-ENGINE-001 |

### 11. 接口与字段映射明细
<!-- DESIGN-API-CHANGES -->
| 接口/方法 | 类型 | 调用方 | 请求变化 | 响应变化 | 校验与权限 | 幂等 | 错误码 | 事务边界 | 兼容策略 | API 增量与实现锚点 |
|---|---|---|---|---|---|---|---|---|---|---|
| `InformationCompiler.compile` | 内部新增 | 配置编排 | RawInformationSet + P2 bindings | CompiledInformationSet/诊断 | 编译校验 | 不适用：无副作用 | `INFORMATION_DEFINITION_INVALID` | 不适用：内存编译 | 不影响旧入口 | API-N/A；`InformationCompiler` |
| `InformationEngine.evaluate` | 内部新增 | P4/P5 | InformationKey + ModelContext | IdentificationResult | read-set 权限 | 不适用：无副作用 | `INFORMATION_PATH_INVALID` | 不适用：只读 | 新增只读边界 | API-N/A；`InformationEngine` |
| `InformationMaterializer.materialize` | 内部新增 | P3 编排 | 模型原子 Key + context | MaterializationResult/异常 | change-data 写权限 | 不新增 | `INFORMATION_MATERIALIZE_FAILED` | ModelContainer | 复用既有事务 | API-N/A；`ModelContainer` |

### 12. 行为调整与代码改动
<!-- DESIGN-BEHAVIOR-ADJUSTMENTS -->
| 功能/场景 | 当前行为与证据 | 调整后行为 | 受影响入口/调用方 | 数据策略 | API/UI 策略 | 回归范围 |
|---|---|---|---|---|---|---|
| Information 识别 | 无统一入口 | evaluate 实时读取并返回三态 | 新增 P3 调用方 | 不缓存 | 内部 API；无 UI | 编译/识别测试 |
| change-data 物化 | 无 P3 契约 | 只写声明值，失败抛异常 | InformationMaterializer | 既有 ModelContainer | 内部 API；无 UI | 回滚测试 |
<!-- DESIGN-CODE-BLUEPRINT -->
| 蓝图 ID | 顺序 | 仓库/模块 | 文件、类或配置 | 变更类型 | 方法/符号 | 决策 ID | 具体改动 | 作用 | 公共逻辑 Owner | 来源文档 | 对应测试 | 依赖 |
|---|---:|---|---|---|---|---|---|---|---|---|---|---|
| BP-P3-001 | 1 | dec-core-compiler | proposed `InformationCompiler.java` | ADD | `compile` | IMPL-DEC-P3-003 | 生成 Key/read-set/DAG | 编译事实 | dec-core-compiler | `..._business_model.md#核心术语` | 编译测试 | P2 PublishedBindings |
| BP-P3-002 | 2 | dec-core-model | proposed `ModelPathResolver.java` | ADD | `resolve` | IMPL-DEC-P3-002 | target-main 优先、relation Data 回退 | 路径解析 | dec-core-model | `..._business_model.md#对象与聚合` | 路径测试 | ModelContext |
| BP-P3-003 | 3 | dec-core-context | proposed `InformationEngine.java` | ADD | `evaluate` | IMPL-DEC-P3-004 | 原子/复合实时求值 | 识别入口 | dec-core-context | `..._business_model.md#业务不变量` | 三态测试 | BP-P3-001 |
| BP-P3-004 | 4 | dec-core-model | proposed `InformationMaterializer.java` | ADD | `materialize` | IMPL-DEC-P3-001 | 写入、重读、异常传播 | 物化边界 | dec-core-model | `..._business_model.md#识别与物化状态` | 回滚测试 | ModelContainer |

### 13. 异常、安全、观测与验证明细
<!-- DESIGN-VERIFICATION -->
| 验证项 | 对应需求/风险 | 测试层级 | 前置数据 | 操作 | 失败注入/边界 | 可观察结果 | 自动化命令或证据入口 |
|---|---|---|---|---|---|---|---|
| 编译/DAG | BR-001/TR-001 | 单元 | 16 Information + P2 bindings | compile | 缺失/循环 | 集合发布或稳定诊断 | test_design 阶段 |
| 实时读取 | BR-003/TR-001 | 集成 | 同一 Key 两次改变模型值 | evaluate | 验证第二次读新值 | 第二次结果反映新值 | test_design 阶段 |
| null/空集合/路径 | BR-002/TR-001 | 单元 | null、empty、非法路径 | evaluate | 例外规则 | TRUE/FALSE/ERROR 符合规则 | test_design 阶段 |
| 物化失败 | AC-001/TR-001 | 集成 | change-data 写入异常 | materialize | 抛异常、回滚、不下游 | 模型恢复且无下游调用 | test_design 阶段 |

### 14. 测试接缝

- `InformationCompiler`：输入 Raw definitions 和 PublishedBindings，输出不可变集合，可独立断言 Key、DAG 和路径诊断。
- `ConfiguredModelReader`：以真实 ModelContext 或受控 reader 提供当前值，验证每次调用重新读取。
- `InformationEngine`：断言 TRUE/FALSE/ERROR、短路和证据 readPaths。
- `InformationMaterializer`：注入既有写入端，验证写入值、异常传播、回滚后不继续下游。

### 15. 兼容、迁移与回滚

不涉及数据库迁移、外部 API 或 XML 迁移。编译失败保留上一份 `CompiledInformationSet`；设计或实现回滚只需移除新增 P3 组件，不改变 P2 Binding 和 ModelContainer。旧 `RuleTests.java` 继续保持原有调用语义。

### 16. 追踪与验证

当前设计 revision `DESIGN-P3-R01` 绑定输入 `BM-P3-R01`，所有设计决策、接口和测试接缝均追踪至 `TR-P3-INFORMATION-ENGINE-001` 与 `AC-P3-INFORMATION-ENGINE-001`。本阶段不宣称已执行 Information 测试；实现后必须在 test_design/development/testing 阶段补充真实测试证据。

## 附录：追踪、决策与未决项

- 追踪：`TR-P3-INFORMATION-ENGINE-001` → `AC-P3-INFORMATION-ENGINE-001`。
- 决策：`IMPL-DEC-P3-001` 至 `IMPL-DEC-P3-004`。
- 未决项：无业务语义未决项；实现阶段仅需确定实际 Java 包路径。
