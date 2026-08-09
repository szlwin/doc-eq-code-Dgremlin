# COMPILER P2 Architecture

> Revision：`DESIGN-P2-R16`  
> Inputs：`BM-R14 / FLOW-R04@p2-system-ruleview-protected-access / REQAN-P2-R01+DEC-OVERLAY-20260809-R02`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED / AC007_PENDING_USER_DECISION`

## 1. 模块权威边界

- `dec-core-context`：neutral immutable values/read contracts：SystemKey、RuleViewKey、CompiledSystem、CompiledRuleView、ModelPath、AccessOperation、ModelAccessPolicyIndex、CompiledModelSet/EngineContext reads。
- `dec-core-compiler`：source/compat conversion、System/RuleView resolution、shared ModelPath compilation、exact access rule construction、PolicyIndex construction、ownership derivation、digest-bound candidate freeze。
- `dec-core-starter`：production Bridge、internal issuance/target resolution/capability mint、Gateway/Guard、operation adapter composition。
- XML/YAML frontends：preserve source facts/SourceRef；不自行决定 runtime authority。
- P3/P4/P6：是否必须在 P2 交付 concrete consumer 取决于 AC-007 pending user decision；不得反向创建第二 authority。

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

Ownership snapshot is a read index, not an authority. No digest-time or runtime reconstruction from it.

## 3. Ownership source topology

```text
Typed Data/View/RuleView/Information registries ----\
CompiledRuleView nested/resolved rule closure -------+--> CompiledSystem ownership snapshot
ModelAccessPolicyIndex keys -------------------------/
```

No duplicate Rule registry is introduced only for ownership. No duplicate model-access map is introduced only for ownership.

## 4. P1→P2 migration topology

```text
P1 SharedModelPath exact ----> shared ModelPath compiler ----> P2 ModelPath
P1 SharedModelPath "*" ------> finite expansion -------------> exact P2 ModelPaths
P1 AccessMode.READ ----------> AccessOperation.READ
P1 AccessMode.WRITE ---------> AccessOperation.WRITE
EXECUTE ---------------------> explicit P2 source only
```

After conversion, PolicyIndex/Bridge/Guard never consult P1 path/operation types as authority.

## 5. Runtime protected-access flow

`FLOW-PROTECTED-ACCESS-EXECUTE`：

```text
ProtectedExecutionBridge
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

## 6. AC-007 architecture status

Common architecture foundation is frozen: no public issued-pair/capability mint, no secondary permission authority, no compatibility write bypass, no public raw operation adapter handed to business caller。

但 final P2 acceptance scope 尚未冻结：
- Option A：seam/no-bypass 即 P2 final acceptance；
- Option B：P2 还要交付 representative production consumers。

`DEC-P2-AC007-STAGE-BOUNDARY-001 = PROPOSED / PENDING_USER_DECISION`；Architecture 不把 Option A 当成 ACTIVE。

## 7. Source compatibility

Existing Java public surfaces remain additive-compatible：
- `SystemKey(String)` / `name()`；
- `RuleViewKey(SystemKey,String)` / `owner()` / `name()`；
- `EngineContext(CompiledModelSet)`；
- legacy eight-argument `CompiledModelSet` constructor。

## 8. Failure boundaries

Compile ERROR -> no candidate publication / old Context retained。Runtime policy/proof/target/Guard failure -> DENY before operation/effects。No fallback from P2 canonical facts to broader P1 compatibility authority。

## 9. Gate

Architecture exact Review, Impact/CrossModule/Concurrency Review, risk detection and AC-007 user decision remain blocking. Implementation Plan/TDD/Development remain BLOCKED.
