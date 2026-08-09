# COMPILER 业务模型

> Revision：`BM-R14`。Base：`BM-R13`。  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R02` + ACTIVE `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` + PROPOSED `DEC-P2-AC007-STAGE-BOUNDARY-001`。  
> Business Flow：`FLOW-R04@p2-system-ruleview-protected-access`。  
> Status：`NEEDS_EXACT_REVIEW / MACHINE_BLOCKED / AC007_PENDING_USER_DECISION`。

BM-R14 保留 BM-R13 的 System ownership、RuleView→View、shared ModelPath、operation independence、PolicyIndex、Guard、atomic publication 与 deterministic denial 语义，并补齐 version contract、ownership truth source、P1→P2 migration 与 AC-007 决策合法性。

## 1. P2 业务目标

P2 在同一 immutable `CompiledModelSet` / `EngineContext` 中形成：

1. first-class System identity + version/source identity + derived ownership snapshot；
2. `RuleViewKey=(SystemKey,name)` + resolved View + resolved Rule closure；
3. rule/change/query-contract/model-access 共用 canonical `ModelPath`；
4. READ/WRITE/EXECUTE 独立的 exact policy facts；
5. compiler-published immutable `ModelAccessPolicyIndex`；
6. production protected-access runtime flow `Bridge -> capability -> Gateway -> Guard -> operation`；
7. System/RuleView/ownership/policy/digest 同一 atomic publication closure。

AC-007 的最终 acceptance scope 当前未获用户授权，不在 BM-R14 中把 Option A 或 B 冻结为 ACTIVE。

## 2. 统一语言

| ID | 术语 | 定义 |
|---|---|---|
| TERM-SYSTEM-COMPILED-IDENTITY | System compiled identity | `SystemKey + SystemVersionIdentity + SourceRef + derived immutable ownership snapshot`。 |
| TERM-SYSTEM-VERSION-IDENTITY | SystemVersionIdentity | optional declared version + mandatory source semantic digest + exact `schemaVersion` + exact `compilerVersion`。`optionsDigest/optionsVersion` 属 enclosing compiled-set compile identity，不在 System 层重复为业务版本。 |
| TERM-SYSTEM-OWNERSHIP-SNAPSHOT | System ownership snapshot | 从 final authoritative compiled facts 派生的一次性 immutable read index；不是第二 authority。 |
| TERM-RULEVIEW-COMPOSITE-IDENTITY | RuleView composite identity | `(SystemKey,name)`；保留 existing source-compatible constructor/accessors。 |
| TERM-MODEL-PATH | ModelPath | P2 唯一 runtime/canonical exact path identity。P1 `SharedModelPath` 仅作为 source/compat input。 |
| TERM-ACCESS-OPERATION | AccessOperation | P2 唯一 runtime authorization operation：READ/WRITE/EXECUTE；P1 `AccessMode` 仅为 source/compat input。 |
| TERM-PROTECTED-ACCESS-FLOW | Protected access flow | `FLOW-PROTECTED-ACCESS-EXECUTE`；与 compile/publication flow 分离。 |

<a id="ENT-COMPILED-SYSTEM"></a>
## 3. CompiledSystem / version identity

`CompiledSystem` required facts：`SystemKey`、`SourceRef`、`SystemVersionIdentity`、owned Data/View/RuleView/Rule/Information/ModelAccessRule key sets。

`SystemVersionIdentity` 必须满足：

- `declaredVersion` 仅输入显式声明时有值；否则 empty；
- `sourceSemanticDigest` mandatory、deterministic；
- `schemaVersion` 等于 enclosing published `CompiledModelSet.schemaVersion`；
- `compilerVersion` 等于 enclosing published `CompiledModelSet.compilerVersion`；
- source-order-only 变化不得制造业务 version；semantic/source/schema/compiler compatibility 变化必须反映在 version/digest identity；
- `optionsDigest/optionsVersion` 继续由 enclosing CompiledModelSet/digest closure 承担，不伪造成 System declared version。

<a id="AGG-SYSTEM-COMPILED-CONFIG"></a>
## 4. Ownership authoritative sources

`CompiledSystem` ownership snapshot **不是 authority**，truth source 固定为：

| Snapshot field | authoritative source |
|---|---|
| `ownedDataKeys` | final owner-qualified typed Data registry |
| `ownedViewKeys` | final owner-qualified typed View registry |
| `ownedRuleViewKeys` | final owner-qualified typed RuleView registry |
| `ownedInformationKeys` | final owner-qualified typed Information registry |
| `ownedRuleKeys` | final `CompiledRuleView` compiled/nested rule closure for that System；不为 snapshot 额外创建 duplicate global Rule registry |
| `ownedModelAccessRuleKeys` | final `ModelAccessPolicyIndex.keys()` / compiled policy rules filtered by System |

构造顺序：

```text
freeze typed registries
 -> freeze CompiledRuleView resolved/nested rule closure
 -> freeze exact CompiledModelAccessRules + ModelAccessPolicyIndex
 -> derive one CompiledSystem ownership snapshot
 -> validate every authority fact appears exactly once and snapshot has no foreign/missing key
 -> include same snapshot in SemanticDigestInput
 -> atomic publication
```

- `INV-COMPILER-016A`：每个 owner-qualified fact belongs to exactly one System；
- `INV-COMPILER-016B`：snapshot 与上述 authoritative sources exact-coherent；
- `INV-COMPILER-016C`：snapshot/version identity 进入 semantic digest；
- `INV-COMPILER-016D`：snapshot immutable/context-local；
- `INV-COMPILER-016E`：禁止以 snapshot 反向重建/覆盖 typed registry、RuleView rule closure 或 PolicyIndex；runtime 不重新计算 ownership。

<a id="ENT-COMPILED-RULEVIEW"></a>
## 5. RuleView compiled relation 与 compatibility

`CompiledRuleView = RuleViewKey + resolvedViewKey + ordered resolvedRuleKeys + SourceRef`。View/Rule exact resolve；unknown/wrong-owner reference 为 stable ERROR；Rule authority 来自 RuleView compiled closure，不要求新增独立 global Rule registry。

Existing key source compatibility 是 P2 contract：

```java
new SystemKey(name);  key.name();
new RuleViewKey(owner, name);  key.owner();  key.name();
```

新 `of()/systemKey()/localName()/value()` 等只能是 additive alias，不得删除/改名 existing public constructor/accessors。

<a id="VO-MODEL-PATH"></a>
## 6. Shared ModelPath 与 P1→P2 conversion

P2 canonical/runtime authority 只有 `ModelPath`。P1 `SharedModelPath` 定位为 source/compatibility representation。

`INV-COMPILER-018`：RULE / CHANGE / QUERY_CONTRACT / MODEL_ACCESS 对 equal System/target/path 产出 value-equal `ModelPath`。

`INV-COMPILER-023`：

```text
P1 SharedModelPath exact non-wildcard
 -> shared P2 ModelPathCompiler
 -> exact P2 ModelPath

P1 SharedModelPath("*") where allowed by source contract
 -> compile-time finite deterministic expansion against exact target schema
 -> exact P2 ModelPath facts
 -> wildcard never reaches CompiledModelAccessRule / PolicyIndex / Bridge / Guard
```

禁止让 `SharedModelPath` 与 `ModelPath` 同时成为 runtime authority。

<a id="VO-MODEL-ACCESS-RULE"></a>
## 7. AccessMode→AccessOperation / permission independence

```text
AccessMode.READ  -> AccessOperation.READ
AccessMode.WRITE -> AccessOperation.WRITE
```

P1 `AccessMode` 没有 EXECUTE，因此绝不推断 EXECUTE；EXECUTE 必须来自 explicit P2/new-source declaration。转换后 runtime PolicyIndex/Bridge/Guard 只消费 `AccessOperation`。

`INV-COMPILER-018A`：READ/WRITE/EXECUTE mutually independent，一种 permission 不隐含另一种。

<a id="POL-MODEL-ACCESS-AUTHORIZATION"></a>
## 8. PolicyIndex / direct bridge authority

`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` 为用户确认 ACTIVE：caller 可选择 exact compiler-published ruleKey/op；missing/invalid/mismatch fail closed。`AccessConsumerIrKey` 为 provenance/diagnostic，不是 authorization-key 维度。

所有 protected access（STATIC_ALLOW 亦然）必须经过 Gateway→Guard exact current-context PolicyIndex lookup；runtime-required branch 再执行 exact plan/proof。

<a id="INV-COMPILER-020"></a>
## 9. Runtime protected-access invariant 与 AC-007 待决

独立于 AC-007 A/B，P2 共用 invariant 是：no public issued-pair/capability mint、no secondary permission authority、任何已存在的 P2 supported protected operation 都经 `FLOW-PROTECTED-ACCESS-EXECUTE`、DENY before effects、actual target/op one-shot capability binding。

`DEC-P2-AC007-STAGE-BOUNDARY-001` 当前 `PROPOSED / PENDING_USER_DECISION`：Option A = P2 seam acceptance；Option B = P2 representative concrete consumers。用户选择前不 supersede 原 R01 AC-007，也不宣称任一方案 ACTIVE。

<a id="INV-COMPILER-019"></a>
## 10. Digest / atomic publication

```text
typed registries
 -> CompiledRuleViews + rule closure
 -> canonical P2 ModelPaths / AccessOperations
 -> exact CompiledModelAccessRules
 -> ModelAccessPolicyIndex
 -> derived CompiledSystem snapshots + SystemVersionIdentity
 -> SemanticDigestInput(same facts + compiler/schema/options identity)
 -> digest -> DigestBoundCompiledInput -> CompiledModelSet.published -> EngineContext
```

Digest 后不得重建 ownership/policy；任何 ERROR 保留 old Context。

<a id="INV-COMPILER-021"></a>
## 11. Diagnostic / runtime denial

Compile ERROR 与 runtime DENY deterministic/source-aware。重复相同 denial 保留 same code/System/optional RuleView/op/canonical ModelPath/policy SourceRef，禁止暴露 sensitive actual values。

<a id="INV-COMPILER-022"></a>
## 12. Compatibility / declaration migration

P2 保留 surviving read-only declaration/System compatibility 到 P7；不恢复 `dec-expand-declaration`，不允许 legacy adapter 写新 registry/policy。`SharedModelPath`/`AccessMode` compatibility 也只能单向转换到 P2 canonical facts，不形成第二 runtime authority。

## 13. Gate

BM-R14 = `NEEDS_EXACT_REVIEW / MACHINE_BLOCKED / AC007_PENDING_USER_DECISION`。risk scan、Requirement/BM/BusinessFlow/Impact/CrossModule exact Review 均未闭环；不得 PASSED，不得进入 Implementation Plan/TDD/Development。
