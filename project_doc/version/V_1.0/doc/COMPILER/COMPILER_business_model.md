# COMPILER 业务模型

> Revision：`BM-R17`。Base：`BM-R16`。  
> Authoritative Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R04` + ACTIVE `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` + ACTIVE `DEC-P2-AC007-STAGE-BOUNDARY-001:OPTION_B` + ACTIVE `DEC-P2-ACCESS-OPERATIONS-001:READ_WRITE_ONLY`。  
> Status：`NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`。

## 0. Canonical artifact authority / revision direction

`COMPILER_business_model.yaml` 是 BM machine-readable canonical authority；本 `.md` 是同 revision 的 normative human-readable mirror。两者 revision、inputs、identity、invariant、truth-table 语义不一致时，整个 BM-R17 candidate 无效。

BM 的 authoritative inputs **不包含 Business Flow / Design / Test Design**。下游 revision 只能按如下方向形成：

```text
REQAN-P2-R01 + Overlay R04 + active Decisions
  -> BM-R17
  -> FLOW-R07
  -> DESIGN-P2-R19
  -> TESTDESIGN-P2-R20
```

Flow/Design/TestDesign 可以回指 BM 作为 trace，但不能反向成为 BM input。

## 1. P2 业务目标

P2 形成：first-class System；System-scoped RuleView/Rule；统一 exact ModelPath；**READ/WRITE 且仅 READ/WRITE**；compiler-published immutable `ModelAccessPolicyIndex`；AC-007 Option B 三类 production representative consumer；capability-bound real READ/WRITE operation；System/RuleView/policy/ownership/version/digest atomic publication。

## 2. System / RuleView / Rule

`CompiledSystem` required facts：SystemKey、SourceRef、SystemVersionIdentity、owned Data/View/RuleView/Rule/Information/ModelAccessRule keys。Ownership 只是由 authoritative stores 派生的 immutable projection，不是另一份 registry/permission authority。

`SystemVersionIdentity = optional declaredVersion + sourceSemanticDigest + schemaVersion + compilerVersion`；schema/compiler 与 enclosing `CompiledModelSet` 相等。

`RuleViewKey=(SystemKey,localName)`；`CompiledRuleView = key + resolvedViewKey + ordered immutable resolvedRuleKeys + SourceRef`。

`RuleKey=(ownerRuleViewKey,localRuleName)`；authoritative store 是 owning `CompiledRuleView` 的 immutable compiled rule closure；不新建 duplicate global Rule registry。

## 3. TargetKey — P1 sourceModel 到 P2 target 的唯一映射

P2 冻结 target identity：

```text
TargetKey = (SystemKey ownerSystemKey, canonicalSourceModelName)
```

业务规则：

- P1/model-access `sourceModel` 必须先在 owner System 的 canonical compiled target namespace 中 exact resolve；
- unknown、ambiguous、cross-System resolution 均为 stable compile ERROR；
- resolve 成功后只生成一个 value-equal `TargetKey`；禁止大小写/fuzzy/parent fallback；
- `sourcePath` **不参与 TargetKey identity**，它只进入共享 `ModelPathCompiler`；
- 因此 source binding 必须按两个正交维度转换：`sourceModel -> TargetKey`，`sourcePath -> ModelPath`；
- TargetKey/ModelPath 转换完成后，P1 source strings 不再作为 runtime authority。

Dynamic binding 中，`targetView + selector + resolved TargetKey + ModelPath` 编译为唯一 `RuntimeBindingPlan`；runtime plan 不能重新选择另一个 TargetKey/ModelPath/rule/op。

## 4. ModelPath / READ-WRITE migration

`SharedModelPath exact -> shared ModelPathCompiler -> exact ModelPath`；合法 source `*` 在 target schema 已解析后 finite/sorted expansion，wildcard 不进入 compiled rule/PolicyIndex/Bridge/Guard。

RULE / CHANGE / QUERY_CONTRACT / MODEL_ACCESS 对 equal System/TargetKey/raw path 产出 value-equal ModelPath。

`AccessOperation = READ | WRITE`，没有 EXECUTE。`AccessMode.READ/WRITE` 仅单向映射到 P2 READ/WRITE；转换后 PolicyIndex/Bridge/Guard 不读取 P1 AccessMode 作为 authority。

READ 与 WRITE 相互不蕴含；未声明默认 DENY。

## 5. ModelAccess identity / policy classification truth table

```text
ModelAccessRuleKey = SystemKey + TargetKey + ModelPath + AccessOperation(READ|WRITE)
```

唯一 runtime authorization authority 是 immutable `ModelAccessPolicyIndex`。

P2 只允许以下 policy classification：

| PolicyStatus | RuntimeAccessRequirement | RuntimeBindingPlan | valid |
|---|---|---|---|
| `STATIC_ALLOW` | `NONE` | absent | YES |
| `STATIC_ALLOW` | `EXACT_RUNTIME_BINDING` | present/absent | NO |
| `RUNTIME_GUARD_REQUIRED` | `EXACT_RUNTIME_BINDING` | present | YES |
| `RUNTIME_GUARD_REQUIRED` | `NONE` | present/absent | NO |

null/unknown status、requirement、plan mismatch 均为 compile/publication ERROR；PolicyIndex construction 必须在 collapse/publish 前验证，runtime 不容错猜测或重分类。

`RuntimeBindingPlan` 的 exact facts 至少绑定：resolved TargetKey、canonical ModelPath、targetView、selector plan identity、SourceRef；它只能证明当前 runtime object/member 与已选 rule 的绑定，不能扩大授权。

## 6. Real protected READ / WRITE semantics

P2 的 protected operation 不是 `effectCount` 抽象，也不是 caller 提供 callback。

```text
entry/direct caller
 -> neutral ProtectedAccessPort
 -> starter Bridge
 -> target/operation resolution
 -> one-shot capability(actual TargetKey/object + ModelPath + READ|WRITE + invocation)
 -> atomic consume
 -> Gateway
 -> Guard
 -> internal ProtectedOperationExecutionPort
```

READ：Guard ALLOW 后读取 capability-bound actual target/path 的当前 runtime value，返回 immutable read value snapshot；**READ 不产生 mutation**。

WRITE：Guard ALLOW 后执行 capability-bound、由 current frame/owner runtime execution state 解析出的 exact write intent；成功只产生一次 mutation，并返回 immutable write receipt。Business caller 不能向 Bridge 注入 raw write callback/operation port，也不能在 Guard 后替换 target/path/write intent。

所有 DENY（policy miss、proof stale、target mismatch、operation mismatch、Guard unavailable、capability consumed）发生在 operation port/effect 前；READ result absent、WRITE receipt absent、mutation=0。

## 7. Neutral downstream seam / module direction

为避免 P3/P4/P6 core module 反向依赖 `dec-core-starter`：

- `dec-core-context` 拥有 neutral immutable keys/IDs、`ProtectedAccessInvocation`、`ProtectedAccessResult`、`ProtectedAccessPort`；
- `dec-core-starter` 的 `ProtectedExecutionBridge` 实现 `ProtectedAccessPort`，并独占 target resolver、capability、Gateway、Guard、operation execution adapter、production assembly；
- P2 Option B 的 Rule/Change/CustomAction representative Entry 仍由 starter `ProtectedAccessComposition` 获取；
- future P3/P4/P6 完整 engine 只依赖 neutral `ProtectedAccessPort` contract，由 application/starter composition 注入实现；**禁止 core -> starter dependency**。

Neutral port 不是第二 authority：它只把 invocation 送入 starter implementation，permission truth 仍唯一来自当前 EngineContext 的 PolicyIndex。

## 8. AC-007 Option B production composition

`ProtectedAccessRuntimeFactory.bind(current EngineContext) -> ProtectedAccessComposition`；composition 暴露 same Bridge + Rule/Change/CustomAction entries，三者共享同一 Bridge/Context authority snapshot。Manual `new Entry(testBridge)` 可做 unit test，但不是 AC-007 production reachability Evidence。

Business caller 不获得 Gateway、Guard、resolver、raw operation port、mutable PolicyIndex、issued-pair/capability mint。

## 9. One-shot concurrency

Capability 并发可达，采用 atomic `ISSUED -> CONSUMED`。同一个 capability 竞争消费时成功数 <=1，实际 protected operation/effect <=1；loser stable `CAPABILITY_ALREADY_CONSUMED`，operation=0/effect=0。禁止非原子 check-then-set。

## 10. Digest / publication / diagnostics

```text
typed registries + RuleView rule closure
 -> sourceModel->TargetKey + SharedModelPath->ModelPath + READ/WRITE conversion
 -> exact policy classification truth-table validation
 -> CompiledModelAccessRules + PolicyIndex
 -> System ownership/version
 -> SemanticDigestInput
 -> digest
 -> CompiledModelSet.published
 -> EngineContext
```

任何 ERROR：candidate publication=0，old Context unchanged。Compile ERROR/runtime DENY deterministic、source-aware、non-sensitive。

## 11. Gate

BM-R17 = `NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`。FLOW-R07、Design R19、TestDesign R20 为下游 candidates；risk/lifecycle/exact independent Reviews 尚未闭环。Implementation Plan/TDD/Development remain BLOCKED。
