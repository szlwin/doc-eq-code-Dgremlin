# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R16`。Base：`DESIGN-P2-R15`。  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R02` + `BM-R14` + `FLOW-R04@p2-system-ruleview-protected-access`。  
> Decisions：ACTIVE `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`；PROPOSED/PENDING_USER_DECISION `DEC-P2-AC007-STAGE-BOUNDARY-001`。  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED / AC007_PENDING_USER_DECISION`。

R16 保留 System/RuleView/PolicyIndex/direct bridge/Guard/atomic publication 主设计，补齐 SystemVersion compiler compatibility、ownership authoritative source、existing key source compatibility、P1→P2 path/operation migration，并把 compile/runtime Business Flow 分开。

<a id="p2-system"></a>
## 1. System first-class compiled snapshot

### 1.1 Existing SystemKey API MUST remain

```java
// existing public surface; MUST remain source-compatible
public SystemKey(String name);
public String name();

// optional additive aliases only
public static SystemKey of(String name);
public String value();
```

Development 不得为了 R16 删除 constructor/name() 或要求调用方迁移后才能编译。

### 1.2 SystemVersionIdentity

```java
public final class SystemVersionIdentity {
    public Optional<String> declaredVersion();
    public String sourceSemanticDigest();
    public String schemaVersion();
    public String compilerVersion();
}
```

Contract：

- no declared source version -> `Optional.empty()`；
- `schemaVersion()` == enclosing published `CompiledModelSet.schemaVersion`；
- `compilerVersion()` == enclosing published `CompiledModelSet.compilerVersion`；
- no timestamp/random/load-order identity；
- options digest/version remains enclosing compiled-set/digest input fact, not a fabricated System business version。

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

所有集合 immutable + deterministic。这个对象是**派生 read snapshot**，不是新的 registry/authorization authority。

### 1.4 Ownership authoritative source and construction

```text
A. final typed Data registry ---------\
B. final typed View registry ----------\
C. final typed RuleView registry -------+-> derive CompiledSystem ownership snapshot
D. final typed Information registry ----/
E. CompiledRuleView rule closure -------/
F. ModelAccessPolicyIndex.keys() -------/
```

Exact mapping：

- Data/View/RuleView/Information keys：来自对应 owner-qualified final typed registry；
- Rule keys：来自 final `CompiledRuleView` compiled/nested rule closure，**不为 snapshot 新增 duplicate global Rule registry**；
- ModelAccessRule keys：来自 final compiled policy rules / `ModelAccessPolicyIndex.keys()` filtered by System。

Candidate freeze order：

```text
freeze typed registries
 -> freeze CompiledRuleViews/rule closure
 -> ModelAccessPolicyIndex.of(final rules)
 -> derive ownership snapshot exactly once
 -> validate both directions (authority -> snapshot and snapshot -> authority)
 -> include same snapshot in SemanticDigestInput
 -> publish same snapshot
```

Runtime 不重建 ownership；snapshot 也不能反向写 authoritative facts。

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

Alias pairs must be value-identical: `owner()==systemKey()` and `name().equals(localName())`。

```java
public final class CompiledRuleView {
    public RuleViewKey key();
    public ViewKey resolvedViewKey();
    public List<RuleKey> resolvedRuleKeys();
    public SourceRef sourceRef();
}
```

View/rule refs exact-resolve before publication；unknown/wrong-owner View/Rule -> stable source-aware ERROR。Rule closure 是 ownedRuleKeys 的 authority source。

<a id="p2-ruleview-resolver"></a>
## 3. RuleViewResolver

```java
public interface RuleViewResolver {
    Optional<CompiledRuleView> find(RuleViewKey key);
    CompiledRuleView require(SystemKey systemKey, String localName);
}
```

New production path 无 bare-name fallback；legacy compatibility read-only，不可注册新 composite facts。

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

P1 `SharedModelPath` 保留 source/compatibility input，不再是 P2 runtime policy type。

```text
SharedModelPath exact
 -> SharedModelPathToModelPathAdapter/compiler normalization
 -> ModelPathCompiler
 -> one exact ModelPath
```

`SharedModelPath("*")` 只在现有 source contract 合法的位置存在：

```text
"*"
 -> resolve exact target schema
 -> stable sorted finite child paths
 -> compile each child through same ModelPath canonicalization
 -> exact ModelPath set
```

Post-condition：`CompiledModelAccessRule`、`ModelAccessPolicyIndex`、Bridge、Guard 中 wildcard count == 0，且不再查询 `SharedModelPath`。

### 5.2 AccessMode

```text
AccessMode.READ  -> AccessOperation.READ
AccessMode.WRITE -> AccessOperation.WRITE
```

`AccessMode` 无 EXECUTE；禁止默认/推断 EXECUTE。EXECUTE 只来自 explicit P2/new-source operation declaration。

Post-condition：PolicyIndex/Bridge/Guard lookup 只使用 `AccessOperation`，不同时维护 `AccessMode` authority。

### 5.3 Single-authority migration rule

Conversion 完成前可以保留 old source objects；conversion 完成后：

- `SharedModelPath/AccessMode` = provenance/compat source fact；
- `ModelPath/AccessOperation` = canonical compiled fact；
- `ModelAccessPolicyIndex` = runtime authorization authority。

禁止“双读后取更宽权限”。

<a id="p2-model-access"></a>
## 6. Operation-qualified authorization

`ModelAccessRuleKey` exact identity 包含 System + target + ModelPath + AccessOperation。READ/WRITE/EXECUTE independent；same path 上其它 operation policy irrelevant。

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

Digest/publication order：

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

<a id="p2-runtime-guard"></a>
## 10. FLOW-PROTECTED-ACCESS-EXECUTE and AC-007 pending decision

Common runtime foundation：

```text
Bridge.execute
 -> internal issueInvocation
 -> exact target resolver
 -> one-shot capability(target + operation)
 -> Gateway
 -> Guard exact current PolicyIndex lookup + proof
 -> bound operation OR deterministic DENY
```

No public issued-pair/capability mint，no raw operation authority exposed to business caller，no secondary permission map。

但是 `DEC-P2-AC007-STAGE-BOUNDARY-001` **不是 ACTIVE**。R16 只记录：

- Option A：以上 seam/no-bypass 成为 P2 final AC，concrete consumers 下沉；
- Option B：在 P2 增加 representative production consumers 执行原 literal AC。

用户明确选择前，Design 不把任何 option 作为完成标准；TestDesign 对 AC-007 必须 `BLOCKED_BY_USER_DECISION`。

<a id="p2-operation-binding"></a>
## 11. One-shot actual target/operation binding

Capability exact binds context/rule/op/actual target/runtime proof-plan identity。A capability 不能操作 B target；same capability concurrent terminal success <=1。

<a id="p2-runtime-denial"></a>
## 12. Deterministic runtime denial

Stable fields：code、SystemKey、optional RuleView provenance、AccessOperation、canonical ModelPath、policy SourceRef；不返回 sensitive actual value/object dump/credential。至少覆盖 POLICY_NOT_FOUND、RUNTIME_BINDING_STALE、RUNTIME_PLAN_MISMATCH、TARGET_SUBSTITUTION、GUARD_UNAVAILABLE。

<a id="p2-pipeline"></a>
## 13. Business Flow split

Current flow revision `FLOW-R04@p2-system-ruleview-protected-access`：

- `FLOW-CONFIG-COMPILE`：AC-001～005、008、compile diagnostic、010；
- `FLOW-PROTECTED-ACCESS-EXECUTE`：runtime AC-004、006、runtime diagnostic；AC-007 仅挂 `PENDING_USER_DECISION`。

不再用纯 compile flow 表示 Bridge/Gateway/Guard execution。

<a id="p2-diagnostics"></a>
## 14. Compile Diagnostic

Duplicate System、ownership mismatch、unknown View/Rule、invalid path、static permission/conversion error -> stable source-aware ERROR + publication=0。

<a id="p2-compatibility"></a>
## 15. Compatibility / migration

Java 8。SystemKey/RuleViewKey existing constructors/accessors、EngineContext existing constructor、legacy CompiledModelSet constructor保留。`dec-expand-declaration` 保持 retired；surviving legacy adapters read-only。

<a id="p2-concurrency"></a>
## 16. Concurrency

Immutable snapshots/index；different bridge invocation 可并发；same capability <=1 terminal success；stale context/frame/cursor/membership fail closed。无 token model。

## 17. Gate

DESIGN-P2-R16 = `NEEDS_REVIEW / MACHINE_BLOCKED / AC007_PENDING_USER_DECISION`。Requirement decision、BusinessFlow、Architecture/API/Develop/Impact/CrossModule/Concurrency exact Review 与 risk scan 未完成；Implementation Plan/TDD/Development BLOCKED。
