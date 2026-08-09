# COMPILER P2 Architecture

> Revision：`DESIGN-P2-R17`  
> Inputs：`BM-R15 / FLOW-R05@p2-system-ruleview-protected-access / REQAN-P2-R01+DEC-OVERLAY-20260809-R03`  
> Decisions：`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001:ACTIVE`；`DEC-P2-AC007-STAGE-BOUNDARY-001:ACTIVE_OPTION_B`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## 1. 模块权威边界

- `dec-core-context`：neutral immutable values/read contracts：SystemKey、RuleViewKey、CompiledSystem、CompiledRuleView、ModelPath、AccessOperation、ModelAccessPolicyIndex、CompiledModelSet/EngineContext reads。
- `dec-core-compiler`：source/compat conversion、System/RuleView resolution、shared ModelPath compilation、exact access rule construction、PolicyIndex construction、ownership derivation、digest-bound candidate freeze。
- `dec-core-starter`：production Bridge、**P2 Rule/change/custom-action representative production entry adapters**、internal issuance/target resolution/capability mint、Gateway/Guard、operation adapter composition。
- XML/YAML frontends：preserve source facts/SourceRef；不自行决定 runtime authority。
- P3：full Rule/Information semantics remain downstream；必须复用 P2 protected-access seam。
- P4：full change/custom-action/Action/Produce state machine remains downstream；必须复用 P2 protected-access seam。
- P6：QueryPlan full compile/execute remains downstream；Option B 不要求 P2 concrete query consumer。

Representative consumers are upstream caller adapters, not a second authorization layer。

## 2. 发布闭包

<a id="2-发布闭包"></a>

```text
Source / P1 compatibility facts
 -> final typed registries
 -> CompiledRuleView + rule closure
 -> SharedModelPath/AccessMode one-way conversion
 -> exact P2 ModelPath/AccessOperation
 -> exact CompiledModelAccessRule
 -> ModelAccessPolicyIndex
 -> derived CompiledSystem ownership snapshot
 -> SystemVersionIdentity(sourceDigest+schemaVersion+compilerVersion)
 -> SemanticDigestInput(same immutable facts)
 -> DigestBoundCompiledInput
 -> CompiledModelSet.published
 -> EngineContext
```

Ownership snapshot is a read index, not an authority。Runtime representative consumer/Bridge/capability instances do not enter semantic digest。

## 3. Ownership source topology

```text
Typed Data/View/RuleView/Information registries ----\
CompiledRuleView nested/resolved rule closure -------+--> CompiledSystem ownership snapshot
ModelAccessPolicyIndex keys -------------------------/
```

No duplicate Rule registry or duplicate model-access map is introduced for ownership。

## 4. P1→P2 migration topology

```text
P1 SharedModelPath exact ----> shared ModelPath compiler ----> P2 ModelPath
P1 SharedModelPath "*" ------> finite expansion -------------> exact P2 ModelPaths
P1 AccessMode.READ ----------> AccessOperation.READ
P1 AccessMode.WRITE ---------> AccessOperation.WRITE
EXECUTE ---------------------> explicit P2 source only
```

After conversion, PolicyIndex/Bridge/Guard never consult P1 path/operation types as authority。

<a id="p2-ac007-architecture"></a>
## 5. AC-007 Option B production entry topology

User-selected Option B is ACTIVE。P2 production topology must include three concrete main-source entry adapters：

```text
RuleProtectedAccessEntry --------\
ChangeProtectedAccessEntry -------+--> same immutable ProtectedExecutionBridge
CustomActionProtectedAccessEntry -/
```

Each entry may keep immutable provenance metadata but its only protected-access authority dependency is `ProtectedExecutionBridge`。It must not have a constructor/field/service locator path to Gateway、Guard、resolver、raw operation port、mutable PolicyIndex、issued-pair/capability mint。

This is intentionally a **single authority funnel**：three real entry categories, one bridge authority path。

## 6. Runtime protected-access flow

`FLOW-PROTECTED-ACCESS-EXECUTE`：

```text
representative production entry (RULE / CHANGE / CUSTOM_ACTION)
 -> ProtectedExecutionBridge
 -> starter-internal issued invocation
 -> exact target resolver
 -> internal one-shot capability(target+operation)
 -> ProtectedAccessGateway
 -> ModelAccessGuard
 -> exact current-context PolicyIndex lookup
 -> optional runtime proof
 -> bound operation OR deterministic DENY
```

STATIC_ALLOW 也进入 Guard；runtime branch 只追加 proof。Same capability concurrent terminal success <=1。

### 6.1 Consumer parity architecture invariant

Consumer entry category is not part of `ModelAccessRuleKey` or Guard authorization semantics。For same Context/invocation/runtime facts，RULE/CHANGE/CUSTOM_ACTION entries must produce the same authorization classification。They may contribute different provenance labels only。

### 6.2 Real production acceptance boundary

AC-007 evidence must instantiate/call main-source entry classes using normal public production API/composition。Test-local consumers、reflection into internals、manual issued pair/capability or secondary test permission maps do not satisfy architecture reachability。

## 7. Downstream stage boundary after Option B

Option B does **not** move full downstream engines into P2：

```text
P2: representative Rule/change/custom-action protected-access entry adapters + common authority seam
P3: full Rule/Information evaluation semantics
P4: full Action/Produce/change/custom-action execution state machine
P6: full QueryPlan compile/execute
```

P3/P4 real executors may call the same Bridge directly or through a compatible adapter, but they cannot bypass Gateway/Guard or introduce a second authorization authority。P6 remains future and must reuse P2 ModelPath/authorization semantics。

## 8. Source compatibility

Existing public surfaces remain additive-compatible：
- `SystemKey(String)` / `name()`；
- `RuleViewKey(SystemKey,String)` / `owner()` / `name()`；
- `EngineContext(CompiledModelSet)`；
- legacy eight-argument `CompiledModelSet` constructor。

Representative consumer APIs are additive Java-8-compatible main-source types。

## 9. Failure/concurrency boundaries

Compile ERROR -> no candidate publication / old Context retained。Runtime policy/proof/target/Guard failure -> DENY before operation/effects。No fallback from P2 canonical facts to broader P1 compatibility authority。

Representative entry objects and Bridge may be reused concurrently；they hold no mutable per-invocation authority state。Different invocations cannot cross-wire rule/op/frame/owner/cursor/target/capability。Same capability concurrent terminal success <=1。

## 10. Gate

AC-007 user decision is satisfied by Option B。Architecture/API/Impact/CrossModule/Concurrency exact Reviews、Requirement/BM/BusinessFlow exact Reviews and machine risk detection remain blocking。Implementation Plan/TDD/Development remain BLOCKED。
