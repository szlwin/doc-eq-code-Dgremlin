# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R17`。Base：`DESIGN-P2-R16`。  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R03` + `BM-R15` + `FLOW-R05@p2-system-ruleview-protected-access`。  
> Decisions：ACTIVE `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`；ACTIVE user-selected `DEC-P2-AC007-STAGE-BOUNDARY-001:OPTION_B`。  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

R17 保留 R16 的 System/RuleView/PolicyIndex/direct bridge/Guard/atomic publication、SystemVersion、ownership truth source、existing key source compatibility 与 P1→P2 migration，并把 AC-007 Option B 冻结成 production main-source Rule/change/custom-action representative consumer API 与 executable acceptance topology。

<a id="p2-system"></a>
## 1. System first-class compiled snapshot

### 1.1 Existing SystemKey API MUST remain

```java
public SystemKey(String name);
public String name();

// optional additive aliases only
public static SystemKey of(String name);
public String value();
```

Development 不得删除 constructor/name() 或要求调用方先迁移。

### 1.2 SystemVersionIdentity

```java
public final class SystemVersionIdentity {
    public Optional<String> declaredVersion();
    public String sourceSemanticDigest();
    public String schemaVersion();
    public String compilerVersion();
}
```

- no declared source version -> `Optional.empty()`；
- `schemaVersion()` == enclosing published `CompiledModelSet.schemaVersion`；
- `compilerVersion()` == enclosing published `CompiledModelSet.compilerVersion`；
- no timestamp/random/load-order identity；
- options digest/version remains enclosing compiled-set/digest input fact。

### 1.3 CompiledSystem read model

```java
public final class CompiledSystem {
    public SystemKey key();
    public SourceRef sourceRef();
    public SystemVersionIdentity versionIdentity();
    public Set<DataKey> ownedDataKeys();
    public Set<ViewKey> ownedViewKeys();
    public Set<RuleViewKey> ownedRuleViewKeys();
    public Set<RuleKey> ownedRuleKeys();
    public Set<InformationKey> ownedInformationKeys();
    public Set<ModelAccessRuleKey> ownedModelAccessRuleKeys();
}
```

所有集合 immutable + deterministic。它是派生 read snapshot，不是 registry/authorization authority。

### 1.4 Ownership authoritative source and construction

```text
final typed Data registry -----------\
final typed View registry ------------\
final typed RuleView registry ---------+-> derive CompiledSystem ownership snapshot
final typed Information registry ------/
CompiledRuleView rule closure ---------/
ModelAccessPolicyIndex.keys() ---------/
```

Rule keys 来自 final `CompiledRuleView` compiled/nested rule closure，不为 ownership 新增 duplicate global Rule registry。ModelAccessRule keys 来自 final policy rules / `ModelAccessPolicyIndex.keys()` filtered by System。

Candidate freeze：

```text
freeze typed registries
 -> freeze CompiledRuleViews/rule closure
 -> ModelAccessPolicyIndex.of(final rules)
 -> derive ownership snapshot exactly once
 -> validate both directions
 -> include same snapshot in SemanticDigestInput
 -> publish same snapshot
```

Runtime 不重建 ownership；snapshot 不能反向写 authoritative facts。

<a id="p2-ruleview"></a>
## 2. RuleView composite identity / resolved View / source compatibility

```java
public final class RuleViewKey {
    // existing; MUST remain
    public RuleViewKey(SystemKey owner, String name);
    public SystemKey owner();
    public String name();

    // additive aliases allowed
    public static RuleViewKey of(SystemKey systemKey, String localName);
    public SystemKey systemKey();
    public String localName();
}
```

`owner()==systemKey()`；`name().equals(localName())`。

```java
public final class CompiledRuleView {
    public RuleViewKey key();
    public ViewKey resolvedViewKey();
    public List<RuleKey> resolvedRuleKeys();
    public SourceRef sourceRef();
}
```

View/rule refs exact-resolve before publication；unknown/wrong-owner View/Rule -> stable source-aware ERROR。Rule closure 是 ownedRuleKeys authority source。

<a id="p2-ruleview-resolver"></a>
## 3. RuleViewResolver

```java
public interface RuleViewResolver {
    Optional<CompiledRuleView> find(RuleViewKey key);
    CompiledRuleView require(SystemKey systemKey, String localName);
}
```

New production path 无 bare-name fallback；legacy compatibility read-only。

<a id="p2-model-path"></a>
## 4. One canonical P2 ModelPath compiler

```java
public enum ModelPathConsumerKind { RULE, CHANGE, QUERY_CONTRACT, MODEL_ACCESS }

public interface ModelPathCompiler {
    ModelPath compile(ModelPathInput input);
}
```

`consumerKind` 仅 provenance/Diagnostic；equal System/target/path across all consumer kinds -> value-equal ModelPath。Query execution 仍属 P6。

<a id="p2-p1-migration"></a>
## 5. P1 SharedModelPath / AccessMode -> P2 canonical facts

### 5.1 SharedModelPath

```text
SharedModelPath exact
 -> compatibility adapter/compiler normalization
 -> ModelPathCompiler
 -> one exact ModelPath
```

`SharedModelPath("*")`：

```text
"*"
 -> resolve exact target schema
 -> stable sorted finite child paths
 -> compile each child through same ModelPath canonicalization
 -> exact ModelPath set
```

Post-condition：`CompiledModelAccessRule`、`ModelAccessPolicyIndex`、Bridge、Guard wildcard count == 0，且不再查询 `SharedModelPath`。

### 5.2 AccessMode

```text
AccessMode.READ  -> AccessOperation.READ
AccessMode.WRITE -> AccessOperation.WRITE
```

`AccessMode` 无 EXECUTE；EXECUTE 只来自 explicit P2/new-source operation declaration。PolicyIndex/Bridge/Guard lookup 只使用 `AccessOperation`。

### 5.3 Single-authority migration rule

Conversion 后：
- `SharedModelPath/AccessMode` = provenance/compat source fact；
- `ModelPath/AccessOperation` = canonical compiled fact；
- `ModelAccessPolicyIndex` = runtime authorization authority。

禁止“双读后取更宽权限”。

<a id="p2-model-access"></a>
## 6. Operation-qualified authorization

`ModelAccessRuleKey` exact identity 包含 System + target + ModelPath + AccessOperation。READ/WRITE/EXECUTE independent；same path 其它 operation policy irrelevant。

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

`of` 在 collapse 前拒绝 duplicate/null/wildcard/invalid status-plan，输出 immutable canonical order。ModelAccess ownership snapshot 从这里读取 keys，不建立第二 policy map。

<a id="p2-context"></a>
## 8. CompiledModelSet / EngineContext publication

Existing eight-argument public constructor MUST remain source-compatible，确定性 attach `ModelAccessPolicyIndex.empty()`，不得从 definitions/registries 重建 policy。

P2 `published(...)` 路径携带同一 policy-aware digest closure。Additive reads：System/RuleView/PolicyIndex。

```text
final typed registries
 -> final CompiledRuleViews/rule closure
 -> P1 compatibility conversion -> P2 exact ModelPaths/AccessOperations
 -> exact access rules -> PolicyIndex
 -> derived ownership snapshots + SystemVersionIdentity(schema+compiler)
 -> SemanticDigestInput(same facts + compile identity)
 -> digest -> DigestBoundCompiledInput -> CompiledModelSet.published -> EngineContext
```

<a id="p2-direct-bridge"></a>
## 9. Direct public bridge

ACTIVE user decision：

```java
public final class ProtectedExecutionBridge {
    public ProtectedAccessResult execute(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId);
}
```

No token/recognizes/claim。Same arguments = independent invocations。Operation mismatch/policy miss fail closed。

<a id="p2-ac007-consumers"></a>
## 10. AC-007 Option B representative production consumers

Option B 是用户已授权的 current P2 acceptance。`dec-core-starter` 必须在 **main production source** 提供以下 additive public entry API（包名可在实现计划中按现有 starter namespace 落位，但类型/职责/依赖形状属于 frozen contract）：

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

public final class RuleProtectedAccessEntry {
    public RuleProtectedAccessEntry(ProtectedExecutionBridge bridge);
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}

public final class ChangeProtectedAccessEntry {
    public ChangeProtectedAccessEntry(ProtectedExecutionBridge bridge);
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}

public final class CustomActionProtectedAccessEntry {
    public CustomActionProtectedAccessEntry(ProtectedExecutionBridge bridge);
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
```

### 10.1 Frozen dependency rule

Each representative entry has exactly one protected-access authority dependency: `ProtectedExecutionBridge`。它可以持有 immutable provenance metadata，但不得依赖/持有/lookup：

- `ProtectedAccessGateway`；
- `ModelAccessGuard`；
- target resolver；
- raw operation execution port；
- mutable/current `ModelAccessPolicyIndex`；
- issued-pair/capability factory/mint。

Entry implementation is therefore structurally：

```java
return bridge.execute(
    invocation.requestedRuleKey(),
    invocation.operation(),
    invocation.frameId(),
    invocation.ownerResolutionId(),
    invocation.cursorId());
```

Consumer kind is represented by the concrete entry type/provenance only，**never** by an authorization key field。

### 10.2 Production reachability rule

AC-007 Evidence 必须使用这三个 main-source entry types through normal public production construction/composition。以下都无效：

- test-local fake consumer 代替 main-source class；
- reflection 直接调用 internal issuance/Guard/operation；
- package-private backdoor；
- manual issued pair/capability；
- hand-built secondary permission map。

### 10.3 Authorization parity rule

对 same immutable Context 与 same exact `ProtectedAccessInvocation`：

```text
RuleProtectedAccessEntry.execute(inv)
ChangeProtectedAccessEntry.execute(inv)
CustomActionProtectedAccessEntry.execute(inv)
```

必须得到相同 authorization classification；若 DENY，stable denial code/authorization facts 一致。若 ALLOW，每次独立 invocation 都只能经 Bridge→Gateway→Guard 到达自己的一次 capability-bound operation。Consumer type 不得升级/降级 READ/WRITE/EXECUTE，也不得重新选择 rule/target。

### 10.4 Stage boundary

这些类型是 P2 representative protected-access acceptance adapters：
- 不实现 P3 Information evaluation/DAG/materialization/invalidation；
- 不实现 P4 Action/Produce full state machine；
- 不实现 P6 QueryPlan execution；
- 后续 P3/P4/P6 real executors 仍必须复用同一 Bridge/Gateway/Guard authority seam。

<a id="p2-runtime-guard"></a>
## 11. FLOW-PROTECTED-ACCESS-EXECUTE

```text
Rule/Change/CustomAction representative production entry
 -> ProtectedExecutionBridge.execute
 -> internal issueInvocation
 -> exact target resolver
 -> one-shot capability(target + operation)
 -> ProtectedAccessGateway
 -> ModelAccessGuard exact current PolicyIndex lookup + proof
 -> bound operation OR deterministic DENY
```

No public issued-pair/capability mint，no raw operation authority exposed to consumer，no secondary permission map。STATIC_ALLOW 也进入 Guard；runtime branch 只追加 proof。

<a id="p2-operation-binding"></a>
## 12. One-shot actual target/operation binding

Capability exact binds context/rule/op/actual target/runtime proof-plan identity。A capability 不能操作 B target；same capability concurrent terminal success <=1。

<a id="p2-runtime-denial"></a>
## 13. Deterministic runtime denial

Stable fields：code、SystemKey、optional RuleView provenance、AccessOperation、canonical ModelPath、policy SourceRef；不返回 sensitive actual value/object dump/credential。至少覆盖 POLICY_NOT_FOUND、RUNTIME_BINDING_STALE、RUNTIME_PLAN_MISMATCH、TARGET_SUBSTITUTION、GUARD_UNAVAILABLE。

Same authorization facts 通过三类 representative consumer 时，consumer provenance 可不同，但 authorization classification / denial code 不得因 consumer kind 改变。

<a id="p2-pipeline"></a>
## 14. Business Flow split

Current `FLOW-R05@p2-system-ruleview-protected-access`：
- `FLOW-CONFIG-COMPILE`：AC-001～005、008、compile diagnostic、010；
- `FLOW-PROTECTED-ACCESS-EXECUTE`：runtime AC-004、006、**007 Option B**、runtime diagnostic；
- AC-007 runtime flow 起点显式包含 RULE/CHANGE/CUSTOM_ACTION representative production entries，不再只从 bare Bridge 开始。

<a id="p2-diagnostics"></a>
## 15. Compile Diagnostic

Duplicate System、ownership mismatch、unknown View/Rule、invalid path、static permission/conversion error -> stable source-aware ERROR + publication=0。

<a id="p2-compatibility"></a>
## 16. Compatibility / migration

Java 8。SystemKey/RuleViewKey existing constructors/accessors、EngineContext existing constructor、legacy CompiledModelSet constructor保留。`dec-expand-declaration` retired；surviving legacy adapters read-only。

<a id="p2-concurrency"></a>
## 17. Concurrency

Immutable snapshots/index；three entry objects may be shared/reused and call immutable Bridge concurrently；different bridge invocations may run concurrently；same capability <=1 terminal success；stale context/frame/cursor/membership fail closed。No token model。Representative entry must not hold mutable per-invocation state that can cross-wire concurrent calls。

## 18. Gate

DESIGN-P2-R17 = `NEEDS_REVIEW / MACHINE_BLOCKED`。AC-007 user decision 已满足，但 Requirement/BM/BusinessFlow、Architecture/API/Develop/Impact/CrossModule/Concurrency exact Review 与 risk scan 未完成；Implementation Plan/TDD/Development remain BLOCKED。
