# COMPILER 业务模型

> Revision：`BM-R16`。Base：`BM-R15`。  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R04` + ACTIVE `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` + ACTIVE `DEC-P2-AC007-STAGE-BOUNDARY-001:OPTION_B` + ACTIVE `DEC-P2-ACCESS-OPERATIONS-001:READ_WRITE_ONLY`。  
> Business Flow：`FLOW-R06@p2-system-ruleview-protected-access`。  
> Status：`NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`。

## 0. Canonical artifact authority

`COMPILER_business_model.md` 与 `COMPILER_business_model.yaml` 构成同一 **BM-R16 canonical pair**：

- `.yaml`：machine-readable canonical facts / IDs / invariants / relations；
- `.md`：同一事实的 normative human-readable contract 与解释；
- 两者 revision、inputs、term/invariant semantics 必须一致；semantic mismatch 使整个 BM-R16 candidate 无效并阻断 Review；
- 不允许 Reviewer/DevelopAgent 在二者冲突时自行选择更宽语义。

## 1. P2 业务目标

P2 形成：

1. first-class System identity/version/source + derived immutable ownership snapshot；
2. `RuleViewKey=(SystemKey,name)` + exact resolved View + exact compiled Rule closure；
3. rule/change/query-contract/model-access 共用 canonical exact `ModelPath`；
4. **READ / WRITE 两种且仅两种**独立授权 operation；
5. compiler-published immutable `ModelAccessPolicyIndex` 作为唯一 runtime authorization authority；
6. P2 production representative Rule/change/custom-action consumers 通过同一 production composition 与 Bridge/Gateway/Guard seam；
7. System/RuleView/policy/ownership/version/digest 同一 atomic publication closure。

## 2. System / version / ownership

`CompiledSystem` required facts：

```text
SystemKey
SourceRef
SystemVersionIdentity
ownedDataKeys
ownedViewKeys
ownedRuleViewKeys
ownedRuleKeys
ownedInformationKeys
ownedModelAccessRuleKeys
```

`SystemVersionIdentity`：optional declaredVersion + mandatory sourceSemanticDigest + exact schemaVersion + exact compilerVersion。schema/compiler 必须等于 enclosing `CompiledModelSet`；options identity 仍由 compiled-set/digest closure 承担。

Ownership snapshot 只是 derived immutable read index，不是 authority。truth source：

| snapshot | authoritative source |
|---|---|
| Data | final owner-qualified typed Data registry |
| View | final owner-qualified typed View registry |
| RuleView | final owner-qualified typed RuleView registry |
| Information | final owner-qualified typed Information registry |
| Rule | final `CompiledRuleView` compiled rule closure |
| ModelAccessRule | final `ModelAccessPolicyIndex.keys()` / compiled policy rules filtered by System |

Snapshot 与 authority 必须双向 exact-coherent，参与 semantic digest，runtime 不重建，也不能反向覆盖 authority。

## 3. RuleView / Rule canonical identity

`RuleViewKey=(SystemKey, localName)`；existing constructor/accessors 保持 source-compatible。

`CompiledRuleView`：

```text
RuleViewKey
resolvedViewKey
ordered immutable resolvedRuleKeys
SourceRef
```

### RuleKey

P2 冻结 Rule 的 canonical identity：

```text
RuleKey = (ownerRuleViewKey, localRuleName)
```

业务不变量：

- `ownerRuleViewKey` non-null；`localRuleName` nonblank、使用 source 中 exact canonical local name，不做大小写/fuzzy fallback；
- value equality/hash/order identity 同时包含 owner RuleView + local name；
- 每个 `CompiledRuleView.resolvedRuleKeys` 内 RuleKey unique；
- `key.ownerRuleViewKey == compiledRuleView.key`；
- authoritative store 是该 `CompiledRuleView` 的 immutable compiled/nested rule closure；**P2 不为了 ownership 新建 duplicate global Rule registry**；
- System `ownedRuleKeys` 由所有 owner-compatible `CompiledRuleView` closure 派生。

Unknown/duplicate/wrong-owner Rule 或 View 在 publication 前产生 stable source-aware ERROR。

## 4. ModelPath / P1 migration

P2 canonical/runtime path identity 只有 exact `ModelPath`。P1 `SharedModelPath` 仅 source/compatibility input。

```text
SharedModelPath exact
 -> shared ModelPathCompiler
 -> exact ModelPath

SharedModelPath("*") where source allows
 -> exact target schema
 -> deterministic finite sorted expansion
 -> each child through same ModelPathCompiler
 -> exact ModelPath facts
```

Post-condition：wildcard 不进入 `CompiledModelAccessRule` / PolicyIndex / Bridge / Guard。

RULE / CHANGE / QUERY_CONTRACT / MODEL_ACCESS 对 equal System/target/raw path 产出 value-equal ModelPath；consumer kind 只能影响 provenance/diagnostic。

## 5. AccessOperation — READ/WRITE only

用户 Decision `DEC-P2-ACCESS-OPERATIONS-001` 冻结：

```text
AccessOperation = READ | WRITE
```

没有 EXECUTE。Current P2 不定义 EXECUTE source syntax、raw fact、enum value、policy rule、runtime operation 或 test acceptance。

P1 conversion：

```text
AccessMode.READ  -> AccessOperation.READ
AccessMode.WRITE -> AccessOperation.WRITE
```

转换后 runtime PolicyIndex/Bridge/Guard 不再读取 P1 `AccessMode` 作为 authority。

READ 与 WRITE 相互独立：READ-only 不允许 WRITE；WRITE-only 不允许 READ；未声明默认 DENY。

## 6. ModelAccess rule / authority

```text
ModelAccessRuleKey = SystemKey + TargetKey + ModelPath + AccessOperation(READ|WRITE)
```

`CompiledModelAccessRule` immutable facts 至少包括 exact key、SourceRef、policy status、runtime requirement/plan（如果需要动态 proof）。

唯一授权 authority：compiler-published immutable `ModelAccessPolicyIndex`。`CompiledSystem.ownedModelAccessRuleKeys` 只是它的 projection，不能成为第二 permission map。

Direct Bridge user Decision 继续 ACTIVE：caller 可选择 current PolicyIndex 中 exact rule key/op；consumer provenance 不进入 authorization key。Policy miss/op mismatch/proof failure/target mismatch 都 fail closed。

## 7. AC-007 Option B production consumers

用户已确认 Option B。P2 必须有真实 main-source representative consumers：

```text
RULE          RuleProtectedAccessEntry
CHANGE        ChangeProtectedAccessEntry
CUSTOM_ACTION CustomActionProtectedAccessEntry
```

三者不能只在 Test 中手工拼装，必须由 `dec-core-starter` normal production composition 获取：

```text
starter production composition root
 -> ProtectedAccessRuntimeFactory (starter-owned lifecycle)
 -> bind current immutable EngineContext
 -> ProtectedAccessComposition
      ├─ bridge()
      ├─ ruleEntry()
      ├─ changeEntry()
      └─ customActionEntry()
          all bound to the SAME Bridge + SAME EngineContext authority snapshot
```

Business caller 可以使用 composition 暴露的 Bridge/entries，但不能获得 Gateway、Guard、target resolver、raw operation port、PolicyIndex mutation、issued-pair/capability mint。

三类 entry 的 consumer category 只作 provenance。相同 Context + exact invocation + runtime target facts 必须得到相同 authorization classification。

P3/P4/P6 完整 engine 仍 downstream；未来 executor 必须复用该 authority seam。

## 8. Runtime invocation / one-shot capability

```text
entry or direct Bridge
 -> internal issued invocation
 -> exact target resolver
 -> starter-internal one-shot capability(target + READ/WRITE + invocation identity)
 -> Gateway
 -> Guard exact current PolicyIndex lookup/proof
 -> bound operation OR deterministic DENY
```

Capability 不是 public API authority；caller 不能创建或 retarget。

### Atomic one-shot concurrency

Capability 采用并发可达、**atomic consume** 语义，而不是依赖 thread confinement：

```text
ISSUED --atomic compare-and-set--> CONSUMED
```

同一个 capability 即使被两个线程同时送达 Gateway，也最多一个 consume 成功；loser 稳定 `CAPABILITY_ALREADY_CONSUMED` DENY，operation/effect=0。成功方 operation/effect 最多一次。顺序 reuse 同样 DENY。

## 9. Bare-name compatibility boundary

P2 **不新增** bare-name RuleView adapter/API。Canonical production lookup 只有 `(SystemKey,name)`。

如果历史代码仍存在 pre-P2 bare-name read compatibility，它只能是既有 read-only boundary：

- 不向新 Registry 注册；
- 不修改 PolicyIndex；
- 不进行 protected WRITE；
- 同名跨 System ambiguous 时 deterministic reject；
- 不能被新 P2 production code 当作 fallback。

## 10. Digest / atomic publication

```text
typed registries
 -> CompiledRuleViews + RuleKey closure
 -> P1 compatibility conversion
 -> exact ModelPaths + READ/WRITE operations
 -> exact CompiledModelAccessRules
 -> PolicyIndex
 -> derived System ownership + SystemVersionIdentity
 -> SemanticDigestInput(same facts)
 -> digest
 -> CompiledModelSet.published
 -> EngineContext
```

任何 ERROR：candidate publication=0，旧 Context 不变。

## 11. Diagnostic / denial

Compile ERROR 与 runtime DENY deterministic/source-aware。Runtime denial 至少稳定携带 code/System/optional RuleView/READ-or-WRITE/canonical ModelPath/policy SourceRef；禁止敏感 actual value/object dump。

## 12. Gate

BM-R16 = `NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`。Requirement R04、FLOW-R06、Impact/CrossModule、risk detection 与 independent exact Reviews 未闭环；不得进入 Implementation Plan/TDD/Development。
