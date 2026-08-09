# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R15`。Base：`DESIGN-P2-R14`。  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809` + `BM-R13` + persistent decisions。  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

R15 保留 R14 已恢复的 System/RuleView/PolicyIndex/direct bridge/Guard/atomic publication 主设计，并补齐 System ownership/version、RuleView resolved View、cross-consumer ModelPath、AC-007 production seam、cross-operation independence 和 runtime DENY deterministic contract。

<a id="p2-system"></a>
## 1. System first-class compiled snapshot

### 1.1 Context-owned public values

```java
public final class SystemVersionIdentity {
    public Optional<String> declaredVersion();
    public String sourceSemanticDigest();
    public String schemaVersion();
}

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

所有集合必须 defensive-copy + immutable + deterministic order。若 source 没有 declared version，`declaredVersion()` 返回 empty；禁止生成时间戳/随机/文件顺序版本。

### 1.2 Ownership construction

Compiler 先注册所有 System symbols，再收集 owner-qualified facts。candidate freeze 前执行：

```text
SystemKey registry
 -> resolve Data/View/RuleView/Rule/Information/model-access ownership
 -> build CompiledSystem ownership snapshot
 -> validate snapshot == final typed registries for that owner
 -> digest-bound freeze
```

任何 orphan/missing/mismatched owner 都是 compile ERROR，candidate publication=0。

### 1.3 CompiledModelSet read surface

```java
public Optional<CompiledSystem> system(SystemKey key);
public Set<SystemKey> systemKeys();
```

这里不是第二 registry；它读取同一个 immutable compiled snapshot。

<a id="p2-ruleview"></a>
## 2. RuleView composite identity and resolved View relation

```java
public final class RuleViewKey {
    public static RuleViewKey of(SystemKey systemKey, String localName);
    public SystemKey systemKey();
    public String localName();
}

public final class CompiledRuleView {
    public RuleViewKey key();
    public ViewKey resolvedViewKey();
    public List<RuleKey> resolvedRuleKeys();
    public SourceRef sourceRef();
}
```

Rules：

1. new mix RuleView 缺 System -> `MIX-RULEVIEW-SYSTEM-REQUIRED`；
2. same System + same localName duplicate -> stable ERROR；
3. `view-ref` 必须 exact resolve，unknown View -> stable source-aware ERROR；
4. every rule-ref exact resolve，unknown Rule -> stable source-aware ERROR；
5. resolved View ownership 默认必须与 RuleView System 一致；任何 cross-System View 必须来自显式 future contract，P2 不猜测；
6. key/resolvedView/ordered rules/source identity 进入 semantic digest。

<a id="p2-ruleview-resolver"></a>
## 3. RuleViewResolver / composite call

```java
public interface RuleViewResolver {
    Optional<CompiledRuleView> find(RuleViewKey key);

    CompiledRuleView require(SystemKey systemKey, String localName);
}
```

Production new path 只接受 `system-ref + rule-ref` / full `RuleViewKey`。不提供 new bare-name resolver；legacy bare-name read adapter 不能写新 registry。

<a id="p2-model-path"></a>
## 4. One canonical ModelPath compiler for all consumers

```java
public enum ModelPathConsumerKind {
    RULE,
    CHANGE,
    QUERY_CONTRACT,
    MODEL_ACCESS
}

public final class ModelPathInput {
    public ModelPathConsumerKind consumerKind();
    public SystemKey systemKey();
    public TargetKey targetKey();
    public String rawPath();
    public SourceRef sourceRef();
}

public interface ModelPathCompiler {
    ModelPath compile(ModelPathInput input);
}
```

Invariant：`consumerKind` 只用于 provenance/Diagnostic，**不得改变 canonicalization semantics**。相同 System/target/raw segments 经 RULE/CHANGE/QUERY_CONTRACT/MODEL_ACCESS 编译必须得到 value-equal `ModelPath`。

QueryPlan execution 属 P6；P2 只冻结其 future compile/IR boundary 必须消费这个 shared ModelPath contract。

`read path="*"` 先由 compiler 在已知 target schema 上有限展开成 exact child ModelPath，runtime 不保留 wildcard authority。

<a id="p2-model-access"></a>
## 5. ModelAccess rule / operation independence

`ModelAccessRuleKey` exact identity 包含 System、target、ModelPath、AccessOperation。READ/WRITE/EXECUTE 是 mutually independent dimensions。

必须满足：

```text
READ-only rule:
  READ    -> can evaluate
  WRITE   -> POLICY_NOT_FOUND / operation mismatch DENY
  EXECUTE -> DENY

WRITE-only rule:
  READ    -> DENY
  WRITE   -> can evaluate
  EXECUTE -> DENY
```

不得把“path 存在任意 permission”实现成 allow。

<a id="p2-policy-index"></a>
## 6. Validated immutable ModelAccessPolicyIndex

```java
public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

`of` 在 collapse 前检查 duplicate/null/invalid key/status/plan/wildcard，并构造 canonical immutable order。Guard 每次 protected access 对 current Context exact lookup once；无 secondary permission map。

<a id="p2-context"></a>
## 7. CompiledModelSet / EngineContext publication

Legacy existing eight-argument constructor 保留 source compatibility，并 deterministically attach `ModelAccessPolicyIndex.empty()`；它不从 definitions/typedRegistries 重建 P2 policy。

P2 production path：

```java
public static CompiledModelSet published(
    PublishedSourceManifest sourceManifest,
    Registry<DefinitionKey, CompiledDefinition> definitions,
    DeferredRegistry deferred,
    ModelAccessPolicyIndex modelAccessPolicyIndex,
    List<Diagnostic> diagnostics,
    DigestPair digestPair,
    String compilerVersion,
    String schemaVersion,
    String optionsDigest);

public ModelAccessPolicyIndex modelAccessPolicyIndex();
public Optional<CompiledSystem> system(SystemKey key);
public Optional<CompiledRuleView> ruleView(RuleViewKey key);
```

`EngineContext` additive read-through 返回同一 immutable authority/snapshots。

### 7.1 Digest order

```text
resolve Systems + ownership snapshots
 -> resolve RuleViews + view/rule refs
 -> compile canonical ModelPaths
 -> compile exact access rules
 -> ModelAccessPolicyIndex.of(rules)
 -> SemanticDigestInput(all same immutable facts)
 -> digest
 -> DigestBoundCompiledInput(same facts + digest)
 -> CompiledModelSet.published(...)
 -> EngineContext
```

Ownership/version/RuleView resolved View/Rule refs/access policy semantics 改变必须改变 semantic digest。runtime Bridge/capability state 不进入 digest。

<a id="p2-direct-bridge"></a>
## 8. Direct public protected-access bridge

Persistent decision `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` 生效：

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

Caller 当前可以选择 exact compiler-published ruleKey/op。Bridge 必须在 internal issuance 前检查 non-null/shape/context binding；requested `operation` 与 exact rule key/rule 不一致即 DENY，不做 operation upgrade/fallback。

相同参数的两个 `bridge.execute(...)` 是两个独立 invocation；P2 不定义业务幂等 token/replay 语义。

<a id="p2-runtime-guard"></a>
## 9. Production protected-access seam / AC-007

根据 `DEC-P2-AC007-STAGE-BOUNDARY-001`，P2 的可完成验收是**唯一 production seam + 无合法旁路**，而不是提前实现 P3/P4/P6 concrete executors。

唯一 supported path：

```text
ProtectedExecutionBridge.execute(...)
 -> starter internal issueInvocation
 -> exact target resolver
 -> one-shot ResolvedProtectedAccess capability
 -> ProtectedAccessGateway
 -> ModelAccessGuard
 -> protected operation
```

冻结 visibility/dependency：

- `issueInvocation` / issued pair implementations package-private/internal；
- capability constructor/mint internal；
- Guard 不暴露“先 allow 再让 caller 自己选择 target”的 API；
- operation execution port 只由 starter composition 绑定，不通过 EngineContext/Bridge getter 暴露给 business consumer；
- compatibility adapter read-only，不能执行 protected write/mint capability；
- Context 只有一个 PolicyIndex authority。

P3 Rule/Information、P4 change/custom-action/produce、P6 QueryPlan concrete integration 必须创建 downstream acceptance，且只能接入此 seam。

<a id="p2-operation-binding"></a>
## 10. Actual-target / operation one-shot capability

Resolver 基于 current invocation + compiler plan 得到 actual target，随后产生 immutable one-shot capability，绑定：

- current EngineContext identity；
- exact rule key；
- exact AccessOperation；
- exact actual target identity；
- runtime proof/plan identity（适用时）。

Gateway/Guard 只能执行 capability-bound target/op。A capability 不能替换为 B target。same capability concurrent terminal success <= 1。

<a id="p2-runtime-denial"></a>
## 11. Deterministic runtime denial contract

```java
public interface ProtectedAccessDenial {
    ProtectedAccessDenialCode code();
    SystemKey systemKey();
    Optional<RuleViewKey> ruleViewKey();
    AccessOperation operation();
    ModelPath modelPath();
    SourceRef policySourceRef();
}
```

相同 current facts 重复 DENY 时必须稳定：same code、System、optional RuleView provenance、operation、canonical ModelPath、policy SourceRef。

至少冻结：

- `POLICY_NOT_FOUND`；
- `RUNTIME_BINDING_STALE`；
- `RUNTIME_PLAN_MISMATCH`；
- `TARGET_SUBSTITUTION`；
- `GUARD_UNAVAILABLE`。

Denial 不携带敏感 actual value、对象 dump、secret/config payload。

<a id="p2-pipeline"></a>
## 12. Compiler pipeline / atomic publication

任何 System ownership、RuleView View/rule reference、ModelPath、access rule ERROR 都使 candidate 全量失败；old EngineContext 保持。并行 Context ownership/RuleView/PolicyIndex snapshots 不共享 mutable state。

<a id="p2-diagnostics"></a>
## 13. Compile Diagnostic determinism

重复 System、ownership mismatch、missing RuleView System、unknown View/Rule、invalid path、static permission error 必须 stable code + definition identity + SourceRef/relatedRefs，排序可重复。

<a id="p2-compatibility"></a>
## 14. Compatibility / migration

P2 不恢复 `dec-expand-declaration`。surviving legacy bare RuleView read compatibility 只能 read，不得注册新 composite facts、不写 policy、不成为 protected-operation bypass。最终删除/transaction/resource convergence 属 P7。

<a id="p2-concurrency"></a>
## 15. Concurrency

- different bridge invocations 可并发，bridge 本身 immutable/stateless；
- ownership/RuleView/PolicyIndex snapshots immutable；
- same capability concurrent execution <= 1 terminal success；
- stale Context/frame/cursor/membership 在 operation 前 fail closed；
- 不使用 token/recognizes/claim 模型。

## 16. Gate

DESIGN-P2-R15 仍是 candidate：Requirement overlay、BM-R13、Architecture/API/Develop/Impact/CrossModule/Concurrency exact Reviews 与 risk scan 尚未完成；不得进入 Implementation Plan/TDD/Development。
