# COMPILER P2 Architecture

> Revision：`DESIGN-P2-R18`  
> Inputs：`BM-R16 / FLOW-R06@p2-system-ruleview-protected-access / REQAN-P2-R01+DEC-OVERLAY-20260809-R04`  
> Decisions：Direct Bridge ACTIVE；AC-007 Option B ACTIVE；AccessOperation READ/WRITE-only ACTIVE  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## 1. 模块权威边界

- `dec-core-context`：neutral immutable System/RuleView/RuleKey/TargetKey/ModelPath/READ-WRITE AccessOperation/ModelAccessPolicyIndex/CompiledModelSet/EngineContext contracts。
- `dec-core-compiler`：source conversion、System/RuleView/Rule resolution、shared ModelPath compilation、READ/WRITE rule construction、PolicyIndex construction、ownership derivation、digest-bound candidate freeze。
- `dec-core-starter`：production composition、three representative AC-007 entries、Bridge、internal invocation/target resolution/capability、Gateway/Guard、operation adapters。
- `dec-demo`：real cross-module fixture/reachability only；不定义 permission authority。
- XML/YAML frontends：source/raw facts + SourceRef；P1 AccessMode only READ/WRITE and converts one-way。

## 2. Compile/publication topology

```text
Source/P1 compatibility
 -> typed registries
 -> CompiledRuleView + RuleKey owner-qualified closure
 -> SharedModelPath/AccessMode conversion
 -> exact ModelPath + AccessOperation(READ|WRITE)
 -> exact CompiledModelAccessRule
 -> immutable ModelAccessPolicyIndex
 -> derived CompiledSystem ownership
 -> SystemVersionIdentity(sourceDigest+schema+compiler)
 -> SemanticDigestInput
 -> CompiledModelSet.published
 -> EngineContext
```

No EXECUTE source/raw/runtime branch exists in current P2。No wildcard runtime authority。

## 3. RuleKey/ownership topology

```text
Typed Data/View/RuleView/Information registries ----\
CompiledRuleView(owner RuleViewKey) -----------------+-> System ownership projection
  -> immutable RuleKey(ownerRuleViewKey, localName) -/
ModelAccessPolicyIndex.keys() -----------------------/
```

No duplicate global Rule registry or second policy map is introduced。

## 4. Production composition topology — AC-007 Option B

```text
normal starter application/runtime composition root
 -> starter-internal authority collaborators
 -> ProtectedAccessRuntimeFactory
 -> bind(current immutable EngineContext)
 -> ProtectedAccessComposition
      ├─ SAME ProtectedExecutionBridge
      ├─ RuleProtectedAccessEntry ---------┐
      ├─ ChangeProtectedAccessEntry -------+-> SAME Bridge
      └─ CustomActionProtectedAccessEntry -┘
```

Business caller obtains entries from composition。Gateway/Guard/resolver/raw operation/PolicyIndex mutation/capability mint remain hidden inside starter authority boundary。

Direct Bridge remains public because of user Decision, but AC-007 representative consumer production Evidence must use composition acquisition rather than test-only hand construction。

## 5. Runtime authority path

```text
entry or direct Bridge
 -> internal ProtectedInvocationId
 -> exact target resolution
 -> internal one-shot capability(invocation+target+READ|WRITE)
 -> atomic ISSUED->CONSUMED
 -> Gateway
 -> Guard exact current PolicyIndex lookup
 -> optional runtime proof
 -> bound READ/WRITE operation OR deterministic DENY
```

STATIC_ALLOW also enters Guard。Runtime proof cannot reselect rule or operation。

## 6. Capability concurrency boundary

Capability is concurrency-reachable and therefore uses atomic consume, not implicit thread confinement。

```text
Thread A ----\
              -> atomic consume same capability -> exactly one winner
Thread B ----/                                -> loser CAPABILITY_ALREADY_CONSUMED
```

Winner effect <=1；loser effect=0。Different capabilities may execute concurrently without cross-wiring。

## 7. Compatibility boundaries

- Existing SystemKey/RuleViewKey/EngineContext/legacy CompiledModelSet APIs remain additive-compatible。
- P2 canonical RuleView lookup has no bare-name adapter/fallback。
- Historical bare-name read compatibility, if physically present, remains read-only outside canonical P2 path and ambiguous-name rejects。
- `dec-expand-declaration` remains retired；surviving declaration compatibility read-only until P7。

## 8. Failure boundaries

Compile ERROR -> no candidate publication / old Context retained。Runtime policy/proof/target/Guard/capability-consume failure -> DENY before effects。No fallback from P2 canonical facts to broader P1 source facts。

## 9. Gate

Architecture/API/Develop/Impact/CrossModule/Concurrency exact Reviews and risk detection remain blocking。Implementation Plan/TDD/Development remain BLOCKED。
