# COMPILER 业务模型

> Revision：`BM-R12`。Base：`BM-R07@7d7bf504ca9d` + R09/R10/R11/R12 remediation changesets。
> 状态：`MATERIALIZED_CANONICAL_CANDIDATE / NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`。
> 本文把此前仅存在于 changeset/candidate 中的 P2 System、RuleView、model-access 业务事实合并回 canonical `COMPILER_business_model.md/.yaml`。P1 已通过的 Source/Canonical/Raw/TypedKey/Reference/Deferred/Diagnostic/digest/原子发布事实保持继承且不改变；machine `task_state` 仍是历史 BM-R07，RC9 reopen/publish 前不得称 BM-R12 PASSED。

## 1. 模块使命与阶段边界

COMPILER 将配置 Source 编译为不可变、确定性、可追踪的 `CompiledModelSet` 与实例级 `EngineContext`。P2 在 P1 编译基线上完成三项业务闭包：

1. System 成为显式一等编译身份；
2. RuleView 唯一身份为 `(SystemKey,name)`，调用使用 `system-ref + rule-ref`；
3. model-access 把 READ/WRITE/EXECUTE 收敛为 compiler-published policy + 统一 runtime Guard，静态或运行时都 fail closed。

P3～P7 仍拥有 Information/Rule/Change/Action/QueryPlan/事务等完整业务执行语义；P2 只冻结这些未来执行入口必须复用的身份、路径和访问控制边界。

## 2. 统一语言

| ID | 术语 | 业务定义 |
|---|---|---|
| TERM-SYSTEM-COMPILED-IDENTITY | System compiled identity | System 以显式 `SystemKey` 作为一等身份；不得由文件名、目录、加载顺序或 RuleView 名称推断。 |
| TERM-RULEVIEW-COMPOSITE-IDENTITY | RuleView composite identity | RuleView 唯一身份是 `(SystemKey,name)`；跨 System 同名合法，同一 System 重复非法，new mix 不允许裸 name 注册/解析。 |
| TERM-MODEL-PATH | ModelPath | 经 compiler 规范化的 exact path；rule/change/query/permission 共享同一语义。runtime 不允许 fuzzy/parent/bare-name fallback。 |
| TERM-MODEL-ACCESS-RULE | ModelAccessRule | 由 System、target、exact ModelPath、AccessOperation、SourceRef 构成的 compiler 授权事实；未声明不产生隐式允许。 |
| TERM-MODEL-ACCESS-POLICY-INDEX | ModelAccessPolicyIndex | compiler 从最终 exact `CompiledModelAccessRule` 集构造并随 `CompiledModelSet` 发布的唯一 immutable runtime policy authority。 |
| TERM-RUNTIME-BINDING | Runtime binding | 编译期确认访问类型合法，但 actual collection element/target 必须在运行时证明属于编译时允许的 exact binding。 |
| TERM-PROTECTED-ACCESS | Protected access | 任何受 P2 model-access 约束的 READ/WRITE/EXECUTE；STATIC_ALLOW 也必须进入 Gateway/Guard。 |

## 3. P2 场景模型

### SCN-P2-SYSTEM-MULTISOURCE

Given 多个 `system-file-info` 输入、输入顺序可变化；When compiler 注册 System；Then：

- 相同语义输入得到相同 `SystemKey` 集和 semantic digest；
- 同一 `SystemKey` 重复定义 -> `MIX-SYSTEM-DUPLICATE`；
- forward reference 在完整 symbol registration 后解析；
- 任一 ERROR 都不得发布部分 Context。

追踪：`TR-P2-SYSTEM-RULEVIEW-001 / 008 / 009`。

### SCN-P2-RULEVIEW-COMPOSITE

Given 两个 System 可以声明同名 RuleView；When 注册、解析或调用 RuleView；Then：

- key = `(SystemKey,name)`；
- new RuleView 缺 System -> `MIX-RULEVIEW-SYSTEM-REQUIRED`；
- 同 System 同名 -> duplicate ERROR；
- 不同 System 同名相互隔离；
- `system-ref + rule-ref` exact resolve；
- bare-name fallback 对新路径禁止。

追踪：`TR-P2-SYSTEM-RULEVIEW-002 / 003 / 009 / 010`。

### SCN-P2-STATIC-ACCESS

Given exact System/target/path/operation 可在编译期确定；When model-access compilation 完成；Then：

- 合法 exact access -> `STATIC_ALLOW`；
- STATIC_ALLOW 没有 runtime requirement/plan；
- 未声明访问、未知 path、共享 WRITE 未授权 -> compile ERROR；
- READ `path="*"` 只允许 compiler 做 finite exact expansion，runtime index 不保存 wildcard key。

追踪：`TR-P2-SYSTEM-RULEVIEW-004 / 005`。

### SCN-P2-RUNTIME-ACCESS

Given `every(orderDetailList,status=1)` 等访问在编译期可确认规则、但 actual element 只能运行时确定；When compiler classification；Then：

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；
- runtime-required 必须携带 exact `RuntimeAccessRequirement + RuntimeBindingPlan`；
- unsupported dynamic selector -> compile ERROR，不允许 fail-open。

运行时所有 protected access：

`Bridge/Runtime -> internal issuance -> resolver -> one-shot capability -> Gateway -> Guard -> exact PolicyIndex lookup -> static fast path OR runtime proof -> same bound target operation`。

追踪：`TR-P2-SYSTEM-RULEVIEW-006 / 007`。

## 4. 聚合与发布边界

### AGG-COMPILATION-SESSION

P1 原子发布不变量继续有效：一个 compilation session 内构建完整候选；任一 ERROR、取消、deadline、publication conflict 都不得替换 caller 已持有的旧 `EngineContext`。

P2 追加成员：

- `ENT-COMPILED-SYSTEM`
- `VO-RULEVIEW-KEY`
- `ENT-COMPILED-RULEVIEW`
- `VO-MODEL-PATH`
- `VO-MODEL-ACCESS-RULE`
- `ENT-MODEL-ACCESS-POLICY-INDEX`

### AGG-SYSTEM-COMPILED-CONFIG

根：`ENT-COMPILED-SYSTEM`。

成员：System-owned RuleViews、model-access rules、相关 SourceRef/Diagnostic。

一致性：同一 compiled publication closure 中，System/RuleView/PolicyIndex/digest 必须来自同一冻结输入；禁止 System registry 来自一次编译、PolicyIndex 来自另一次编译。

## 5. 实体与值对象

### ENT-COMPILED-SYSTEM

- identity：`SystemKey`；
- required：explicit system name/key、SourceRef；
- deterministic equality/order；
- duplicate exact key 在候选发布前失败。

### VO-RULEVIEW-KEY

`RuleViewKey = (SystemKey systemKey, String localName)`。

不变量：

- 两字段都 non-null/non-blank；
- equality/hashCode 同时包含 SystemKey 与 name；
- 不提供 canonical new-code bare-name key。

### ENT-COMPILED-RULEVIEW

必须持有 exact `RuleViewKey`、SourceRef、resolved rule refs；new mix System 缺失或未知时不得构造。

### VO-MODEL-PATH

exact、case-sensitive、canonical；runtime policy key 不支持 wildcard/fuzzy/parent inference。

### VO-MODEL-ACCESS-RULE

业务 identity 至少包含：`SystemKey + target identity + ModelPath + AccessOperation`。状态为：

- `STATIC_ALLOW`；或
- `RUNTIME_GUARD_REQUIRED`。

STATIC_ALLOW 不得携带 runtime plan；runtime-required 必须拥有 exact requirement/plan。

### ENT-MODEL-ACCESS-POLICY-INDEX

唯一 runtime authorization authority。compiler 从最终 exact rule 集构造；`CompiledModelSet` 和 `EngineContext` 只读持有；Guard exact lookup 一次；starter 不得维护第二份 permission map。

## 6. 业务服务

### SVC-SYSTEM-COMPILATION

职责：收集 System 定义、exact key 注册、duplicate/unknown 检查、deterministic ordering、SourceRef Diagnostic。

### SVC-RULEVIEW-RESOLUTION

职责：

- 注册 `(SystemKey,name)`；
- resolve `system-ref + rule-ref`；
- 拒绝 missing System、same-system duplicate、unknown System/Rule、new-code bare-name lookup。

### SVC-MODEL-PATH-COMPILATION

职责：统一 exact path；READ wildcard 只在 compile-time finite expansion；非法/未知/unsupported dynamic path compile ERROR。

### SVC-MODEL-ACCESS-AUTHORIZATION

职责分为 compile-time publication 与 runtime enforcement：

- compiler 构造 exact `CompiledModelAccessRule`、`ModelAccessPolicyIndex`；
- semantic digest 覆盖 policy authorization semantics；
- runtime Guard 对 current EngineContext PolicyIndex exact lookup；
- STATIC_ALLOW 也经过 Guard；
- runtime-required 追加 binding proof；
- DENY 发生在 protected operation/外部副作用之前。

## 7. P2 核心不变量

- `INV-COMPILER-016`：System 必须以显式 `SystemKey` 一等注册；同 key duplicate ERROR；顺序变化不得改变 semantic result。
- `INV-COMPILER-017`：RuleView key 必须是 `(SystemKey,name)`；new RuleView System required；bare-name new lookup/register 禁止。
- `INV-COMPILER-018`：ModelAccessRule 按 System/target/path/operation exact 表达；未声明权限默认 DENY，共享 WRITE 默认 DENY。
- `INV-COMPILER-019`：System/RuleView/model-access 任一 ERROR 都不得发布部分 `CompiledModelSet/EngineContext`；旧 Context 保持。
- `INV-COMPILER-020`：所有 protected READ/WRITE/EXECUTE 都必须经过统一 Gateway/Guard；STATIC_ALLOW 只能是 Guard 内 fast path。
- `INV-COMPILER-021`：动态访问只能收窄 compiler-published policy；runtime proof 不得创建新的 rule/path/operation allowance。
- `INV-COMPILER-022`：P2 不恢复 retired `dec-expand-declaration`；只保留明确 read-only compatibility boundary，最终删除留给 P7。
- `INV-COMPILER-023`：PolicyIndex 是唯一 runtime permission authority；Guard lookup exact once；definitions/typedRegistries/starter secondary map 不得成为权限来源。
- `INV-COMPILER-024`：被 Guard 验证的 actual target + operation 与最终执行必须由同一个 one-shot capability 绑定；A capability 不得授权 B target。

## 8. 失败与稳定结果

Compile 至少：

- `MIX-SYSTEM-DUPLICATE`
- `MIX-SYSTEM-UNKNOWN`
- `MIX-RULEVIEW-SYSTEM-REQUIRED`
- `MIX-RULEVIEW-DUPLICATE`
- `MIX-RULEVIEW-UNKNOWN-SYSTEM`
- `MIX-RULEVIEW-UNKNOWN-RULE`
- `MIX-MODEL-PATH-INVALID`
- `MIX-MODEL-ACCESS-DENIED`
- `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`

Runtime 至少：

- `POLICY_NOT_FOUND`
- `CONTEXT_IDENTITY_MISMATCH`
- `MODEL_ACCESS_GUARD_BYPASS`
- `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`
- `RUNTIME_BINDING_REQUIRED`
- `RUNTIME_BINDING_PROOF_INVALID`
- `RUNTIME_BINDING_STALE`
- `RUNTIME_BINDING_PLAN_MISMATCH`
- `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`
- `RUNTIME_BINDING_CAPABILITY_CONSUMED`
- `GUARD_UNAVAILABLE`
- `STATIC_ALLOW / RUNTIME_ALLOW / RUNTIME_DENY`

## 9. 原子发布、Context 与 Diagnostic

- System/RuleView/PolicyIndex 必须进入同一 immutable `CompiledModelSet` publication closure；
- semantic digest 包含 System/RuleView identity 与 PolicyIndex authorization-significant fields；
- equivalent input order -> same semantic digest；
- authorization/System/RuleView semantic change -> digest changes；
- failed candidate 保留 old Context；并行 Context 无全局 mutable registry；
- Diagnostic 按 stable code + definition identity + SourceRef deterministic 排序，包含相关 System/RuleView/model-access 来源。

## 10. P2 direct bridge Requirement/Decision delta

BM-R12 的权限业务事实仍是“只有 compiler-published PolicyIndex 中存在的 exact rule/op 才可能 ALLOW”。

当前 P2 另有用户明确确认的设计决策 `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`：production caller 可以在 `ProtectedExecutionBridge.execute(...)` 逐次提交 exact `ModelAccessRuleKey + AccessOperation + frame/owner/cursor`。因此在当前 P2：

- `AccessConsumerIrKey` 是 provenance/diagnostic 维度，不是 rule authorization key 的组成部分；
- P2 不要求 consumer -> rule/op binding；
- caller 仍不能使 PolicyIndex 中不存在的 rule/op 获得 ALLOW；
- 后续若要恢复 per-consumer authority binding，必须作为新的 Requirement/Decision Review，不在本 Revision 隐式加入。

该 decision 是对 Requirement 的显式解释偏差，不能被描述为 REQAN-P2-R01 原文已经包含的语义。

## 11. Traceability / downstream contract

- AC-001 System：`ENT-COMPILED-SYSTEM / INV-COMPILER-016 / SVC-SYSTEM-COMPILATION`。
- AC-002/003 RuleView：`VO-RULEVIEW-KEY / INV-COMPILER-017 / SVC-RULEVIEW-RESOLUTION`。
- AC-004～007 model-access：`VO-MODEL-ACCESS-RULE / ENT-MODEL-ACCESS-POLICY-INDEX / INV-COMPILER-018/020/021/023/024 / SVC-MODEL-ACCESS-AUTHORIZATION`。
- AC-008 publication/context：`AGG-COMPILATION-SESSION / INV-COMPILER-019`。
- AC-009 diagnostics：System/RuleView/model-access stable diagnostics。
- AC-010 migration：`INV-COMPILER-022`。

下游 Design/TestDesign 必须使用当前 BM-R12 IDs，不得继续把 R09～R12 changeset 当作唯一事实源。

## 12. Gate

BM-R12 已在 canonical 文件中 materialize，但**尚未 machine-published，也尚未完成 BM-R12 exact independent Review**。因此当前状态只能是：

`MATERIALIZED_CANONICAL_CANDIDATE / NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`

不得据此进入 Implementation Plan/TDD/Development。
