# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R18`。Base：`DESIGN-P2-R17`。  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R04` + `BM-R16` + `FLOW-R06@p2-system-ruleview-protected-access`。  
> Decisions：ACTIVE `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`；ACTIVE `DEC-P2-AC007-STAGE-BOUNDARY-001:OPTION_B`；ACTIVE `DEC-P2-ACCESS-OPERATIONS-001:READ_WRITE_ONLY`。  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

R18 保留 R17 已收敛的 System/RuleView/PolicyIndex/Guard/atomic publication/P1 migration，并关闭本轮 implementation-readiness 缺口：READ/WRITE-only contract、RuleKey/API closure、AC-007 production composition、bare-name compatibility boundary、one-shot capability atomic concurrency。

<a id="p2-system"></a>
## 1. System first-class snapshot

Existing public API MUST remain：

```java
public SystemKey(String name);
public String name();
```

Optional aliases只能 additive。`SystemVersionIdentity`：

```java
public final class SystemVersionIdentity {
    public Optional<String> declaredVersion();
    public String sourceSemanticDigest();
    public String schemaVersion();
    public String compilerVersion();
}
```

`CompiledSystem` immutable read model：SystemKey、SourceRef、versionIdentity、owned Data/View/RuleView/Rule/Information/ModelAccessRule keys。Ownership 是 derived projection，不是 authority；authoritative sources 与 BM-R16 一致。

<a id="p2-ruleview"></a>
## 2. RuleView / RuleKey

Existing `RuleViewKey` surface MUST remain：

```java
public RuleViewKey(SystemKey owner, String name);
public SystemKey owner();
public String name();
```

Additive `of/systemKey/localName` 可以存在但不能替代 existing API。

`CompiledRuleView`：

```java
public final class CompiledRuleView {
    public RuleViewKey key();
    public ViewKey resolvedViewKey();
    public List<RuleKey> resolvedRuleKeys();
    public SourceRef sourceRef();
}
```

<a id="p2-rule-key"></a>
### 2.1 RuleKey implementation contract

`RuleKey` 新增于 `dec-core-context`，public immutable value type：

```java
public final class RuleKey {
    public RuleKey(RuleViewKey ownerRuleViewKey, String localRuleName);
    public static RuleKey of(RuleViewKey ownerRuleViewKey, String localRuleName);
    public RuleViewKey ownerRuleViewKey();
    public String localRuleName();
}
```

Contract：

- constructor arguments non-null；name after existing lexical validation must be nonblank；
- no case folding/fuzzy normalization beyond the shared source-name lexical rule；
- `equals/hashCode` exactly `(ownerRuleViewKey, localRuleName)`；
- deterministic ordering, when needed, is owner RuleView canonical order then local name；
- every key in `CompiledRuleView.resolvedRuleKeys()` must have `ownerRuleViewKey().equals(compiledRuleView.key())`；
- duplicate local RuleKey within one RuleView is compile ERROR；
- **no global RuleKey Registry is introduced** solely for P2 ownership；the authoritative RuleKey store/closure is the immutable owning `CompiledRuleView` compiled/nested rule closure。

<a id="p2-ruleview-resolver"></a>
## 3. RuleViewResolver / bare-name boundary

```java
public interface RuleViewResolver {
    Optional<CompiledRuleView> find(RuleViewKey key);
    CompiledRuleView require(SystemKey systemKey, String localName);
}
```

New P2 canonical API contains **no bare-name adapter** and no global fallback. If pre-P2 historical code still exposes a bare-name read compatibility path, it stays outside canonical P2 resolver, read-only, cannot register facts or mutate PolicyIndex, cannot perform protected WRITE, and ambiguous same-name across Systems deterministically rejects.

<a id="p2-model-path"></a>
## 4. ModelPath / TargetKey API closure

`TargetKey` is a neutral immutable `dec-core-context` exact target identity. It is owner-qualified by the compiled model/view target contract and has value equality/hash semantics; no fuzzy/parent lookup is part of key equality.

`ModelPath` is immutable exact canonical segments in `dec-core-context`：

- no wildcard segment after compiler normalization；
- equality/hash based on canonical segments；
- `toString`/diagnostic rendering deterministic；
- no consumer-specific semantics。

Shared compiler：

```java
public enum ModelPathConsumerKind { RULE, CHANGE, QUERY_CONTRACT, MODEL_ACCESS }
public interface ModelPathCompiler { ModelPath compile(ModelPathInput input); }
```

Consumer kind is provenance only。

<a id="p2-p1-migration"></a>
## 5. P1 SharedModelPath / AccessMode migration

```text
SharedModelPath exact -> shared ModelPathCompiler -> exact ModelPath
SharedModelPath("*") -> exact schema -> finite sorted expansion -> exact ModelPaths
AccessMode.READ       -> AccessOperation.READ
AccessMode.WRITE      -> AccessOperation.WRITE
```

Current P2 has no EXECUTE operation and no EXECUTE migration/source contract。After conversion PolicyIndex/Bridge/Guard never consult `SharedModelPath` or `AccessMode` as authority；no dual-read/broader-result fallback。

<a id="p2-model-access"></a>
## 6. READ/WRITE-only authorization types

```java
public enum AccessOperation {
    READ,
    WRITE
}
```

There is **no EXECUTE enum member** in current P2。

`ModelAccessRuleKey` (`dec-core-context`, public immutable value)：

```text
identity = SystemKey + TargetKey + ModelPath + AccessOperation
```

- all fields non-null；
- exact value equality/hash includes all four；
- consumer category is not an identity/authorization field；
- operation only READ/WRITE。

`CompiledModelAccessRule` (`dec-core-context`, public immutable read fact)：

```text
ModelAccessRuleKey key
SourceRef sourceRef
PolicyStatus status
RuntimeAccessRequirement runtimeRequirement
Optional<RuntimeBindingPlan> runtimePlan
```

Construction is compiler-owned; public callers do not mutate it。READ-only policy never authorizes WRITE and vice versa。

<a id="p2-policy-index"></a>
## 7. ModelAccessPolicyIndex

```java
public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

`of` validates duplicate/null/wildcard/operation/status-plan before collapse；only READ/WRITE keys allowed；result immutable canonical order。Index is sole runtime permission authority。

<a id="p2-context"></a>
## 8. CompiledModelSet / EngineContext publication

Existing public constructors remain source-compatible。Legacy 8-arg `CompiledModelSet` attaches deterministic empty PolicyIndex and never reconstructs policy。P2 `published(...)` carries same immutable PolicyIndex/System/RuleView/ownership/version/digest closure。

```text
registries
 -> CompiledRuleViews + RuleKey closure
 -> exact ModelPaths + READ/WRITE operations
 -> CompiledModelAccessRules
 -> PolicyIndex
 -> ownership + SystemVersionIdentity
 -> SemanticDigestInput
 -> digest
 -> published CompiledModelSet
 -> EngineContext
```

<a id="p2-direct-bridge"></a>
## 9. Direct public Bridge / invocation API

```java
public final class ProtectedAccessInvocation {
    public static ProtectedAccessInvocation of(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId);

    public ModelAccessRuleKey requestedRuleKey();
    public AccessOperation operation();
    public RuntimeExecutionFrameId frameId();
    public RuntimeResolutionOwnerId ownerResolutionId();
    public Optional<RuntimeCollectionCursorId> cursorId();
}

public final class ProtectedExecutionBridge {
    public ProtectedAccessResult execute(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId);

    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
```

`ProtectedAccessInvocation` immutable/non-null except optional cursor；operation must be READ/WRITE and equal requestedRuleKey operation。Direct scalar overload and invocation overload are semantically identical。No token/recognizes/claim。

### Runtime coordinate types

`RuntimeExecutionFrameId`、`RuntimeResolutionOwnerId`、`RuntimeCollectionCursorId`、resolved `RuntimeObjectId`、runtime plan identity are immutable opaque IDs in neutral/runtime contract modules. They identify runtime coordinates but **do not grant permission**。Equality/hash is exact opaque identity；no mutable object/value payload。

Starter creates an internal unique `ProtectedInvocationId` for each bridge call；caller cannot choose/reuse it as authority。

`ProtectedAccessResult` is immutable terminal ALLOW/DENY result；it never exposes internal capability。DENY exposes only stable non-sensitive provenance fields defined in runtime denial contract。

<a id="p2-production-composition"></a>
## 10. Production composition/acquisition — AC-007 Option B

R17 only defined three Entry classes；R18 freezes the normal production acquisition path so tests cannot close AC-007 by hand-constructing wrappers。

Owner module：`dec-core-starter`。

```java
public final class ProtectedAccessRuntimeFactory {
    // factory itself is assembled by the normal starter application/runtime composition root;
    // its authority-bearing collaborators are not handed to business consumers.
    public ProtectedAccessComposition bind(EngineContext engineContext);
}

public final class ProtectedAccessComposition {
    public EngineContext engineContext();
    public ProtectedExecutionBridge bridge();
    public RuleProtectedAccessEntry ruleEntry();
    public ChangeProtectedAccessEntry changeEntry();
    public CustomActionProtectedAccessEntry customActionEntry();
}
```

Contract：

- `bind` requires one non-null immutable EngineContext and creates one context-bound Bridge；
- `bridge()` and all three entries are immutable composition members；
- all entries hold/reference **the exact same Bridge instance** returned by `bridge()` and therefore the same EngineContext/PolicyIndex authority snapshot；
- normal application/demo integration obtains entries from `ProtectedAccessRuntimeFactory -> ProtectedAccessComposition`；manual `new Entry(testBridge)` may unit-test an entry but is **not valid AC-007 production reachability Evidence**；
- factory internal construction may bind starter-internal target resolver/proof/operation ports/Gateway/Guard, but those collaborators are not returned to business callers；
- no global mutable current composition/context。

<a id="p2-ac007-consumers"></a>
## 11. Representative production entries

```java
public final class RuleProtectedAccessEntry {
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
public final class ChangeProtectedAccessEntry {
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
public final class CustomActionProtectedAccessEntry {
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
```

Entry constructors are starter/composition-controlled; business code obtains instances from composition。Each entry's only protected-access authority dependency is the bound Bridge。No Gateway/Guard/resolver/raw operation/PolicyIndex mutation/issued-pair/capability mint dependency。

Same Context + exact invocation + runtime target facts -> same authorization classification across all entry categories。Authorized independent calls may each effect once；unauthorized calls effect=0。

P3/P4 full engines remain downstream；P6 full QueryPlan remains downstream。

<a id="p2-operation-binding"></a>
## 12. Internal issued invocation / one-shot capability

Starter-internal capability binds exactly：

```text
ProtectedInvocationId
current EngineContext identity
ModelAccessRuleKey
AccessOperation READ|WRITE
resolved RuntimeObjectId / actual target
runtime binding-plan/proof identity as applicable
```

Capability has no public constructor/getter that lets business caller retarget/mint authority。A capability for target A/READ cannot operate target B or WRITE。

<a id="p2-concurrency"></a>
## 13. Atomic one-shot concurrency

R18 chooses **concurrent-reachable + atomic consume**, not thread confinement。

Semantic state machine：

```text
ISSUED --atomic CAS--> CONSUMED
```

- Gateway must acquire/consume the same capability with one atomic state transition before protected operation；
- two threads racing the same capability: successful transitions <= 1；
- only winner may reach Guard/operation path for that capability；
- loser returns stable `CAPABILITY_ALREADY_CONSUMED` DENY, operation/effect=0；
- sequential reuse after terminal consume has same denial；
- implementation may use `AtomicBoolean/AtomicReference` or equivalent Java-8 atomic primitive, but check-then-set non-atomic logic is forbidden；
- tests use latch/barrier/controlled seam, never `Thread.sleep` as correctness oracle。

Different invocation/capability objects may run concurrently and must not cross-wire frame/owner/cursor/target/op。

<a id="p2-runtime-guard"></a>
## 14. Protected runtime authority flow

```text
composition entry OR direct Bridge
 -> Bridge
 -> starter internal invocation identity
 -> exact target resolver
 -> one-shot capability
 -> atomic consume
 -> Gateway
 -> Guard exact current PolicyIndex lookup
 -> optional runtime proof
 -> bound READ/WRITE operation OR deterministic DENY
```

STATIC_ALLOW still goes through Guard exact lookup；runtime-required branch adds proof only。

<a id="p2-runtime-denial"></a>
## 15. Deterministic denial

Stable non-sensitive fields：code、SystemKey、optional RuleViewKey provenance、AccessOperation(READ|WRITE)、canonical ModelPath、policy SourceRef。Minimum denial families include POLICY_NOT_FOUND、RUNTIME_BINDING_STALE、RUNTIME_PLAN_MISMATCH、TARGET_SUBSTITUTION、GUARD_UNAVAILABLE、CAPABILITY_ALREADY_CONSUMED。

<a id="p2-pipeline"></a>
## 16. Business Flow

Current `FLOW-R06` splits compile/publication and protected runtime execution。AC-007 Option B includes production composition acquisition plus three representative entries before Bridge。

<a id="p2-compatibility"></a>
## 17. Java 8 / compatibility

- existing SystemKey/RuleViewKey/EngineContext/legacy CompiledModelSet surfaces remain；
- P2 does not add a new bare-name RuleView adapter；
- historical read-only compatibility, if still present, cannot become canonical fallback or permission authority；
- `dec-expand-declaration` remains retired；declaration boundary read-only until P7；
- no record/sealed/module-system dependency。

## 18. Gate

DESIGN-P2-R18 = `NEEDS_REVIEW / MACHINE_BLOCKED`。Requirement R04、BM-R16、FLOW-R06、ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency exact Review 与 risk scan 未完成；Implementation Plan/TDD/Development remain BLOCKED。
