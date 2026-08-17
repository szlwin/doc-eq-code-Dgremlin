# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R01@8875f042898c`。Base Design：`DESIGN-R05@0b37a9b4dd48`。输入：`REQAN-P2-R01@d08612768131`、`BM-R07@7d7bf504ca9d`。
> 本 Revision 是同一 COMPILER 设计谱系上的 P2 增量：P1 的 Source/Canonical/Raw/Symbol/Deferred/Diagnostic/digest/原子发布保持有效；P2 只消费 System、RuleView 与 model-access 的 Deferred 所有权/授权边界，不建立第二 Compiler、Registry、Context 或 runtime authority。

## 1. 设计目标与冻结决策 {#p2-design-goals}

P2 把已经存在于 P1 类型系统中的 `SystemKey`、`RuleViewKey(SystemKey,name)` 从“可表达”推进为端到端强制语义，并补齐 `CompiledSystem`、统一 `ModelPath`、`ModelAccessRule` 与 fail-closed Guard。冻结决策如下：

1. System 身份只来自显式 System 定义；路径、文件名、包名和调用上下文都不是身份来源。
2. RuleView 唯一身份始终是 `(SystemKey,name)`；新编译/发布/调用不提供裸名称 fallback。
3. P1 `SharedModelPath + selector` 继续承担配置 selector 解析；P2 新增的 `ModelPath` 是面向运行语义消费者的已编译、强类型路径身份，两者职责不合并。
4. `READ/WRITE/EXECUTE` 独立授权；未声明即 DENY，尤其共享模型 WRITE 默认拒绝。
5. 静态可判定的非法访问在候选发布前失败；只有结构合法但资源事实确实依赖运行时的访问可形成 `RuntimeGuardRequired`。
6. Guard 必须位于任何受保护模型读取/写入/执行的共同前置边界；DENY 发生在 mutation、状态推进或外部副作用之前。
7. 旧 `ConfigInfo/RuleViewInfo` 裸名称能力仅作为 P7 前的兼容读取边界；P2 不允许其向新 Registry 注册事实，也不复制 declaration runtime。

## 2. P1 基线复用与 P2 增量 {#p2-baseline-delta}

| P1 已有事实 | P2 处理 | 禁止 |
|---|---|---|
| `SystemKey` | 直接复用并强制来源于显式 System | 新建第二 SystemId 或按路径推断 |
| `RuleViewKey(SystemKey,name)` | 直接作为注册、发布、lookup 唯一 Key | `Map<String,RuleView>` 新注册、跨 System 搜索 |
| `TypedDefinitionRegistries.systems()/ruleViews()` | 继续作为低层 typed registry；增加领域化只读查询 facade | 第二份可变 Registry |
| `ModelAccessBinding` / `SharedModelPath` | 继续完成 P1 selector 精确绑定 | 把 selector 当成 P2 运行路径重新解释 |
| `CompiledModelSet` / `EngineContext` | 扩展不可变发布事实闭包 | 全局 current Context |
| P1 Deferred | P2 消费 System/RuleView/model-access 权限语义；P3～P8 仍 Deferred | 提前实现 P3～P7 |

## 3. 模块与包边界 {#p2-modules}

### 3.1 `dec-core-context`

新增/收敛中立不可变类型：

- `CompiledSystem`：SystemKey、SourceRef、成员 Key 集合、RuleViewKey 集合、access rule key 集合；
- `ModelPath`：`targetKey + immutable segments`；
- `AccessOperation`：闭集 `READ|WRITE|EXECUTE`；
- `ModelAccessRule`：`systemKey + targetKey + modelPath + operation + sourceRef + decisionRequirement`；
- `AccessDecisionRequirement`：`STATIC_ALLOW|RUNTIME_GUARD_REQUIRED`，不存在 STATIC_DENY 发布值；静态 DENY 必须转 Diagnostic 并阻断发布；
- `ModelAccessDecision`：运行时 `ALLOW|DENY`；
- `ModelAccessRequest`：调用方明确提供 Context、System、Target、ModelPath、Operation 和必要 runtime facts；
- `ModelAccessGuard`：只读判定接口，不执行 mutation。

Context 不依赖 compiler、parser 或执行模块；所有集合防御性复制并不可变。

### 3.2 `dec-core-compiler`

在现有 Pipeline 中增加 P2 owner-qualified pass/service：

- `SystemCompilationService` / `SystemCompilationPass`；
- `RuleViewResolutionService` / `RuleViewOwnershipPass`；
- `ModelPathCompiler` / `ModelPathCompilationPass`；
- `ModelAccessAuthorizationService` / `StaticAccessValidationPass`；
- `P2CompiledFactsAssembler`，把 System、RuleView、ModelPath 与 access rules 放入候选 `CompiledModelSet`。

Compiler 仍是唯一候选构建与原子发布协调者，不依赖具体 XML parser 实现。

### 3.3 frontend / legacy XML parser

安全 Canonical frontend 继续只提供节点、属性与 SourceRef。`dec-context-config-parse-xml` 中旧 `RuleParser -> ConfigContextUtil -> ConfigInfo` 属于兼容历史路径：P2 设计要求显式标记为 legacy read boundary；它不得成为新 `RuleViewKey` Registry 的来源、不得通过裸 name 为新调用提供 fallback。新 `mix` 的 RuleView `system` 属性必须在 Canonical/Raw 输入中保留并进入 Compiler。

### 3.4 `dec-core-starter` 与后续执行入口

Starter 只组装 Compiler/Context 与统一 Guard；不得保存全局权限表。后续 Rule/change/custom action/query 消费者只接收已发布 Context 中的 owner-qualified facts，并通过共同 Guard seam 请求动态授权。

## 4. System 编译设计（P2-T01/T02） {#p2-system}

### 4.1 输入

`RawSystemDefinition` 必须携带：显式 name、SourceRef、稳定 source ordinal，以及其声明的 Data/View/rule-file/Information/model-access 引用。`system-file-info` 可以产生多个 System source；Source discovery 的输入顺序只影响读取过程，不影响最终 identity 集合。

### 4.2 两阶段算法

1. **Register phase**：按 `(SystemKey.canonical, SourceRef stable order)` 排序，先注册全部显式 SystemKey；重复 Key 收集双方/多方 SourceRef 并产生 `MIX-SYSTEM-DUPLICATE`。
2. **Link phase**：在完整 System symbol set 上解析成员与前向引用，构造 `CompiledSystem`。任何 unknown/mismatch 只产生 Diagnostic，不创建“半 System”。

### 4.3 发布不变量

`CompiledSystem` 只出现在无 ERROR 候选；`TypedDefinitionRegistries.systems()` 与 `CompiledSystemRegistry` 必须由同一 `CompiledModelSet` 派生并保持 Key 集合一致。语义摘要纳入排序后的 System identity、成员 identity 与 access rule canonical form。

## 5. RuleView 复合身份与解析（P2-T05/T06/T09/T10） {#p2-ruleview}

### 5.1 注册

- Raw RuleView 必须包含 owner System；缺失 owner -> `MIX-RULEVIEW-SYSTEM-REQUIRED`；
- Key 固定使用现有 `RuleViewKey(SystemKey owner,String name)`；
- 同 System 同名 -> `MIX-RULEVIEW-DUPLICATE`；
- 跨 System 同名合法，Registry 中形成两个不同 Key；
- RuleView 引用的 View/Rule 必须在同 owner System 允许范围内，否则使用 source-aware mismatch Diagnostic。

### 5.2 Runtime lookup

Context 暴露 `RuleViewResolver.resolve(SystemKey,String)` 或等价 facade，内部只构造 `RuleViewKey` 做精确查找。新 API 不提供 `resolve(String bareName)`。兼容读取若仍存在，必须在命名上明确 `LegacyRuleViewReadAdapter`，只读旧 `ConfigInfo`，不得写新 Registry，且不得被新业务调用链依赖。

## 6. 统一 ModelPath（P2-T04） {#p2-model-path}

`ModelPath` 与 P1 selector 分层：

```text
配置 selector: SharedModelPath + SystemViewSelector -> ModelAccessBinding
运行语义路径: target DefinitionKey + exact segments -> ModelPath
```

`ModelPathCompiler` 只接受明确 target shape；逐段大小写敏感精确解析：

- unknown segment -> `MIX-MODEL-PATH-INVALID`；
- non-composite intermediate -> 同 code + 精确 failing segment；
- 禁止 wildcard、前/后缀、跨 target 搜索、root-property fallback；
- canonical form 为 `targetKey.canonical + '/' + escapedSegments`，供 rule/change/query/access 共用；
- 编译结果不可保存 parser-specific DOM/YAML node。

## 7. ModelAccessRule 与静态授权（P2-T03/T07/T11） {#p2-model-access}

### 7.1 规则结构

每条规则必须完整绑定：`SystemKey`、`targetKey`、`ModelPath`、`AccessOperation`、`SourceRef`。READ/WRITE/EXECUTE 作为闭集独立值，不做层级继承。

### 7.2 静态判定

`ModelAccessAuthorizationService.compile(...)` 的候选结果只有：

- `STATIC_ALLOW`：System、target、path、operation 均可静态确定且显式授权；
- `RUNTIME_GUARD_REQUIRED`：身份/结构/声明均合法，但资源实例或动态边界确实只能运行时确定；
- `Diagnostic ERROR`：未声明、错误 System/target/path、operation 不匹配、共享 WRITE 未授权等。

没有 `UNKNOWN_ALLOW`。未声明权限、Guard 不可用、Context 不匹配均 fail-closed。

### 7.3 权限索引

发布时派生不可变 `ModelAccessPolicyIndex`：主键 `(SystemKey,targetKey,ModelPath,AccessOperation)`；只由 `ModelAccessRule` 构造。运行时不得重新解析 XML/YAML，也不得遍历全局名称空间。

## 8. Runtime Guard（P2-T08/T11） {#p2-runtime-guard}

`ModelAccessGuard.authorize(request)` 是唯一动态授权 seam：

1. 验证 request 的 Context identity 与 policy index 同源；
2. 使用精确四元组定位 rule；缺失即 DENY；
3. `STATIC_ALLOW` 可直接 ALLOW；
4. `RUNTIME_GUARD_REQUIRED` 调用注入的 runtime fact evaluator，只允许产生 ALLOW/DENY；异常、null、超时或无法确定均 DENY；
5. 返回决定与稳定 reason code，不执行模型写入或外部操作。

调用顺序必须是：`build request -> authorize -> if ALLOW then execute mutation/read/execute`。Guard 不能被 Rule/change/custom action 自己替换；测试 seam 必须能断言 DENY 后 mutation counter、state version、external-effect spy 均未变化。

## 9. Pipeline 与状态转换 {#p2-pipeline}

在 P1 `REFERENCES_RESOLVED -> GRAPH_PREPARED -> SEMANTICALLY_VALIDATED -> PUBLISHED` 语义内插入 P2 pass，不增加第二生命周期：

| 顺序 | Pass | 输出 | ERROR |
|---:|---|---|---|
| 1 | existing Source/Structural/Symbol passes | Raw + symbols | P1 codes |
| 2 | `SystemCompilationPass` | `CompiledSystem` candidates | duplicate/unknown System |
| 3 | `RuleViewOwnershipPass` | owner-qualified RuleView facts | missing owner/duplicate/mismatch |
| 4 | `ModelPathCompilationPass` | canonical ModelPath set | invalid/non-composite path |
| 5 | `StaticAccessValidationPass` | access rules + decision requirement | denied/undeclared operation |
| 6 | existing Deferred classification | only P3～P8 remaining Deferred | incomplete boundary |
| 7 | digest/candidate/publication | immutable Context | P1 publication codes |

任何 P2 ERROR 沿用 P1 原子发布规则：本轮 FAILED，Publisher 不接收候选，旧 Context 不变。

## 10. CompiledModelSet / EngineContext 扩展 {#p2-context}

`CompiledModelSet` 增加不可变 P2 视图，不复制 `definitions`：

- `systems(): Registry<SystemKey,CompiledSystem>`；
- `ruleViews(): Registry<RuleViewKey,CompiledDefinition|CompiledRuleView>`；
- `modelAccessRules(): ModelAccessPolicyIndex`；
- `resolveRuleView(SystemKey,String)` facade 可位于 EngineContext/专用 resolver，但底层只查同一 model set。

构造时验证：所有 P2 map key 与内部 key 一致；RuleView owner 必须存在；rule target/path 必须属于同一发布闭包；不能包含静态 DENY；Diagnostics 不能含 ERROR。Context 间不共享可变 cache。

## 11. Diagnostic 与拒绝契约 {#p2-diagnostics}

设计冻结以下 P2 稳定 code（与 BM-R07 error IDs 一一对应）：

| code | 触发 | 阶段 |
|---|---|---|
| `MIX-SYSTEM-DUPLICATE` | SystemKey 重复/冲突 | compile |
| `MIX-RULEVIEW-SYSTEM-REQUIRED` | 新 RuleView 缺显式 System | compile |
| `MIX-RULEVIEW-DUPLICATE` | 同 System 同名 | compile |
| `MIX-RULEVIEW-UNKNOWN` | 复合 Key 不存在/错误 owner | compile/runtime lookup |
| `MIX-MODEL-PATH-INVALID` | 路径未知、非复合、越目标 | compile |
| `MIX-MODEL-ACCESS-DENIED` | 静态未授权/operation 不匹配 | compile |
| `MIX-MODEL-ACCESS-RUNTIME-DENIED` | Guard 拒绝/不可判定 | runtime |
| `MIX-P2-DECLARATION-BOUNDARY` | P2 删除/复制旧 runtime 或形成第二 authority | verification |

所有 Diagnostic/denial 只携带定位所需的 SystemKey、RuleViewKey（适用时）、operation、canonical path、SourceRef/reason code；不回显凭据或完整 runtime value。

## 12. 并发、幂等与原子性 {#p2-concurrency}

- System/RuleView/access 编译器均无静态可变状态；状态只在当前 CompilationSession；
- 所有 registry/index 在发布前冻结；同 Context 多线程只读；
- 相同语义输入的 SystemKey/RuleViewKey/ModelPath/rule 排序与 digest 稳定；
- Guard 不写 policy index；runtime evaluator 若需要业务状态，由调用方通过 request snapshot 显式提供；
- 并行 Context A/B 使用各自 model set，禁止按“最新全局 Context”判定；
- 编译错误和 runtime DENY 均无需补偿，因为副作用边界之前即失败。

## 13. 安全设计 {#p2-security}

- 权限默认 DENY；WRITE 无显式声明必须失败；
- Operation 不提升：READ 不蕴含 WRITE/EXECUTE；
- Guard evaluator 异常/null/timeout/unknown -> DENY；
- 路径 canonicalization 在授权前完成，防止不同字符串指向同一资源却绕过 policy key；
- Diagnostic 对 runtime value 做最小披露；
- legacy adapter 不得写新 registry 或获得 wildcard 权限。

## 14. 兼容与 P7 declaration 边界 {#p2-compatibility}

P2 不删除旧 `ConfigInfo/RuleConfig/RuleViewInfo`、不恢复已退役 `dec-expand-declaration`，也不复制它们形成新 runtime。兼容边界只有：

1. 允许旧调用在明确 legacy adapter 内读取旧裸名称事实；
2. 新 Compiler/Context facts 不反向写旧 Config；
3. 新调用链不得依赖裸名称 adapter；
4. 无法无损映射的 declaration 差异记录为 P7 migration fact；
5. P7 删除条件至少包括：所有新调用完成 composite lookup、所有动态 mutation 接入 Guard、无新注册流入 legacy registry、回归 fixture 全绿。

## 15. 跨模块时序 {#p2-cross-module}

```text
Frontend/Source
  -> Compiler: explicit System + RuleView owner + model-access + SourceRef
Compiler
  -> Context candidate: CompiledSystem + RuleViewKey + ModelPath + policy index
Compiler
  -> ContextPublisher: one atomic publish (only no ERROR)
Caller/Starter
  -> EngineContext: resolve RuleView(SystemKey,name)
Caller/Executor
  -> ModelAccessGuard: authorize(Context,System,target,path,operation,runtimeFacts)
ModelAccessGuard
  -> Caller: ALLOW | DENY
Caller
  -> protected operation: only after ALLOW
```

该时序落实 `CMI-P2-SYSTEM-RULEVIEW-001` 四个 CMSTEP；任何 compile ERROR 停在发布前，runtime DENY 停在 protected operation 前。

## 16. API/实现契约边界 {#p2-api-contract}

完整签名见 `COMPILER_api_contract.md`。设计要求：

- 复用现有 `SystemKey` / `RuleViewKey`，不改 canonical equality；
- 新值对象均 Java 8 immutable；
- 所有 public collection 返回 immutable snapshot/view；
- lookup 返回 `Optional` 或显式 typed result，不以 null-success 表达 unknown；
- Guard 只判定，不执行业务副作用；
- 新 API 不提供 bare-name RuleView lookup；
- API contract 的 runtime-denial 与 compile Diagnostic 区分，但共享 canonical System/path/operation identity。

## 17. 测试接缝 {#p2-test-seams}

Design 必须为 Test Design 提供以下稳定 seam：

- `SystemDefinitionFixture`：多 source/顺序重排/重复 System；
- `RuleViewCompositeFixture`：order/payment 同名 RuleView、缺 System、同 System 重名；
- `ModelShapeFixture + ModelPathCompiler`：合法/未知/非复合 path；
- `ModelAccessPolicyFixture`：READ/WRITE/EXECUTE allow/deny 矩阵；
- `ModelAccessGuardSpy/RuntimeFactEvaluatorStub`：ALLOW/DENY/exception/unknown；
- `MutationProbe`：记录 stateVersion、writeCount、externalEffectCount，证明 DENY 无副作用；
- `ContextPairFixture`：两个 Context 同名 RuleView 与不同 policy 隔离；
- `LegacyBoundaryScan`：证明新 production path 不调用 `ConfigInfo.getRuleViewInfo(String)`/`DataUtil.getRuleViewInfo(String)` 作为 fallback。

## 18. P2-T01～T12 设计映射 {#p2-task-map}

| P2 task | 设计落点 | 主要 TR |
|---|---|---|
| T01 System Raw/Compiled | §4、§10 | TR-001/008/009 |
| T02 System loader | §4、§9 | TR-001/008 |
| T03 ModelAccessRule | §7 | TR-004/006/007 |
| T04 ModelPathCompiler | §6 | TR-005 |
| T05 RuleViewKey/registry | §5 | TR-002/003 |
| T06 Parser/Diagnostic owner | §3.3、§5、§11 | TR-002/003/009 |
| T07 static permission | §7、§9 | TR-004/005/008 |
| T08 runtime Guard | §8、§13 | TR-006/007 |
| T09 composite call | §5.2、§15 | TR-003 |
| T10 same-name isolation | §5、§12 | TR-002/003 |
| T11 unauthorized matrix | §7/8/13 | TR-004/006/007/009 |
| T12 declaration boundary | §14 | TR-010 |

## 19. 需求追踪 {#p2-traceability}

| TR | 设计引用 |
|---|---|
| TR-P2-SYSTEM-RULEVIEW-001 | §4 System 编译、§9 Pipeline |
| TR-P2-SYSTEM-RULEVIEW-002 | §5 RuleView 注册、§10 Context |
| TR-P2-SYSTEM-RULEVIEW-003 | §5.2 Runtime lookup、§15 时序 |
| TR-P2-SYSTEM-RULEVIEW-004 | §7 权限模型、§13 fail-closed |
| TR-P2-SYSTEM-RULEVIEW-005 | §6 ModelPath、§7 静态校验 |
| TR-P2-SYSTEM-RULEVIEW-006 | §7 RuntimeGuardRequired、§8 Guard |
| TR-P2-SYSTEM-RULEVIEW-007 | §8 无旁路、§15 时序 |
| TR-P2-SYSTEM-RULEVIEW-008 | §9/§10 原子发布、§12 Context 隔离 |
| TR-P2-SYSTEM-RULEVIEW-009 | §11 Diagnostic、§12 deterministic |
| TR-P2-SYSTEM-RULEVIEW-010 | §14 declaration/P7 边界 |

## 20. 停止条件 {#p2-stop}

出现以下任一情况必须回到 Requirement/Business Model，而不是在实现阶段自行决定：默认允许未声明 WRITE、允许 bare RuleView fallback 进入新调用、需要第二全局 Registry/Context、需要删除 declaration runtime、需要提前实现 P3～P7 完整语义，或现有配置无法用显式 System/RuleView/ModelPath 无损表达且会改变已冻结业务语义。
