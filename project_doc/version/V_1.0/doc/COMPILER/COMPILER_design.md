# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R12`。Base：`DESIGN-P2-R11`，输入 Business Model candidate：`BM-R12`。
> 状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。本 Revision 只收敛 FND-P2-REV-004 / FND-P2-REV-015 / FND-P2-REV-016 的剩余 implementation-readiness：①为上层可信 execution module 提供 production-reachable、但不能签任意 caller facts 的 composition-issued execution bridge；②冻结 `ModelAccessPolicyIndex` validated construction、policy-aware `CompiledModelSet` publication path、legacy constructor fail-closed 兼容语义，以及 policy index 在 digest-bound input 之前进入同一发布闭包。不新增 FND-020，不改变 BM-R12 业务语义。
> 当前 canonical Business Model 仍是历史 BM-R07；正式 RC9 reopen/publish、current-revision risk Evidence 与 exact independent Review 完成前，本 Design 不得 PASSED。

## 1. 设计目标与不可绕过约束

1. System 是显式一等身份；RuleView 唯一身份为 `(SystemKey,name)`。
2. READ/WRITE/EXECUTE 独立授权，未声明即拒绝；共享 WRITE 默认拒绝。
3. 所有 protected READ/WRITE/EXECUTE 必须通过同一个 starter-owned protected runtime；`STATIC_ALLOW` 只能是 Guard exact lookup 后的内部 fast path。
4. Runtime ModelPath lookup exact-only；wildcard 只允许 compile-time finite canonical expansion。
5. Java 生产 API 保持 release 8；`EngineContext` 现有 final class、单参 constructor、`compiledModelSet()/modelSet()/projection()` 保持兼容。
6. `RuntimeBindingPlan` 只属于 `RUNTIME_GUARD_REQUIRED`；STATIC_ALLOW 不得伪造或要求 runtime plan。
7. 被 Guard 验证的 actual target 与最终 operation target 必须是同一个 framework binding；capability/proof A 不得授权 B。
8. public `ProtectedAccessResolutionContext` / `ProtectedOperationIntent` getter 不是 authority；caller 自行实现接口不能获得授权。
9. **上层可信 execution module 不直接调用 package-private `issueInvocation(...)`，也不直接持有/构造 issued pair。生产可达入口是 composition-issued `ProtectedExecutionBridge`；bridge 已固定 consumer/rule/operation，per-call 仅接受同一 trusted adapter 能识别的 opaque execution token。**
10. **唯一 policy authority 是 compiler-published、`CompiledModelSet`-owned immutable `ModelAccessPolicyIndex`。Index 必须通过 context-owned validated factory 构造，并在 semantic digest 绑定之前进入 compiler publication closure。**
11. legacy 八参数 `CompiledModelSet` constructor 保持兼容，但确定性绑定 `ModelAccessPolicyIndex.empty()`；它绝不从 definitions/typed registries 重建 policy。P2 protected access 在 legacy context 上因此 fail closed。
12. DENY 必须发生在相应失败点后的 target resolution、capability issuance、policy lookup、模型访问、状态推进和外部副作用之前。
13. P2 只交付访问控制执行边界所需 runtime plumbing，不实现 P3～P7 的 Information/Rule/Change/Action/QueryPlan 完整业务执行语义。

## 2. Repository-valid Maven ownership

```text
dec-core-context
  dec.core.context.model.access.*
  -> neutral immutable rules/plan/requirement
  -> ModelAccessPolicyIndex + validated factory
  -> protected-access read contracts
  -> CompiledModelSet / EngineContext additive policy accessor

       ^ existing dependency
       |
dec-core-compiler
  dec.core.compiler.modelaccess / compiler publication pipeline
  -> production classifier
  -> exact CompiledModelAccessRule / RuntimeBindingPlan
  -> ModelAccessPolicyIndex.of(compiledRules)
  -> updated SemanticDigestInput / DigestBoundCompiledInput
  -> policy-aware CompiledModelSet publication

       ^ existing composition dependency
       |
dec-core-starter
  dec.core.starter.access.*
  -> ProtectedAccessRuntime / Factory
  -> ProtectedExecutionBridge
  -> resolver / gateway / guard / verifier / context-local registry
  -> package-private issued input/capability records

  dec.core.starter.access.spi.*
  -> trusted composition-time execution state/target/operation ports
  -> bridge receiver/capability delivery

       ^ application/composition dependency
dec-demo and future P3-P7 execution modules
  -> register trusted adapters at composition
  -> receive exact bridge capability
  -> invoke bridge with adapter-owned opaque execution token
```

不新增 `dec-core-runtime`。禁止 context -> compiler/starter、compiler -> starter、starter 为 P2 新增对 `dec-core-model` 的业务耦合、split package、global mutable current Context、第二套 policy registry。

## 3. Compile-time access rule invariant（R11 保持）

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }
```

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；真实 fixture：`systems.xml / order.ordered / status = 1`。
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；真实 fixture：`every(orderDetailList,status = 1)` element `status` READ。
- 其它未冻结 dynamic selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

```text
STATIC_ALLOW
 -> runtimeRequirement = empty
 -> runtimeBindingPlan = empty

RUNTIME_GUARD_REQUIRED
 -> EXACT_RUNTIME_BINDING requirement present
 -> exactly one compiler-published RuntimeBindingPlan present
```

## 4. `ModelAccessPolicyIndex` validated construction contract

### 4.1 Context-owned public API

`dec-core-context / dec.core.context.model.access` 冻结：

```java
public final class ModelAccessPolicyIndex {
    private ModelAccessPolicyIndex(...);

    public static ModelAccessPolicyIndex empty();

    public static ModelAccessPolicyIndex of(
        Iterable<CompiledModelAccessRule> rules);

    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

选择 `Iterable<CompiledModelAccessRule>` 而不是 caller-supplied `Map`，使 factory 能在 snapshot 时显式检测 duplicate key，而不是让 Map 提前吞掉 duplicate。

`of(...)` 必须一次性执行：

1. null collection/rule/key -> fail；
2. `index key == rule.key()`，key 从 rule 本身派生，caller 无 separate map-key 注入点；
3. duplicate `ModelAccessRuleKey` -> fail；
4. `STATIC_ALLOW` 必须 plan/requirement empty；
5. `RUNTIME_GUARD_REQUIRED` 必须 exact plan + EXACT_RUNTIME_BINDING requirement；
6. wildcard/runtime fuzzy key 不得进入 index；
7. deterministic key ordering + defensive immutable snapshot；
8. factory 返回后无 public mutator/builder continuation。

`empty()` 是确定性 immutable empty authority，用于 legacy compatibility；它不扫描 definitions，不自动派生任何 permission。

## 5. `CompiledModelSet` policy-aware publication + legacy compatibility

真实当前仓库的八参数 constructor 保持原 signature，不删除、不改参数顺序：

```java
public CompiledModelSet(
    PublishedSourceManifest sourceManifest,
    Registry<DefinitionKey, CompiledDefinition> definitions,
    DeferredRegistry deferred,
    List<Diagnostic> diagnostics,
    DigestPair digestPair,
    String compilerVersion,
    String schemaVersion,
    String optionsVersion);
```

R12 冻结其兼容语义：

```text
legacy 8-arg constructor
 -> existing facts snapshot semantics remain
 -> modelAccessPolicyIndex = ModelAccessPolicyIndex.empty()
 -> DO NOT derive/rebuild policy from definitions()/typedRegistries()
 -> protected access exact lookup => POLICY_NOT_FOUND unless a policy-aware published set was used
```

新增显式 policy-aware publication factory，而不是让 Development 自选 overload/builder/reconstruction：

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
    String optionsVersion);

public ModelAccessPolicyIndex modelAccessPolicyIndex();
```

`published(...)` 必须验证 non-null index，并把 sourceManifest + definitions + typedRegistries + deferred + **exact same immutable policy index** + diagnostics + versions + digestPair 作为一次发布闭包冻结。`equals/hashCode/toString` 的语义必须把 policy index 作为发布模型事实考虑；不得让两个 policy 不同的 CompiledModelSet 在 equals/hashCode 上被视为相同。

`EngineContext` 继续保留现有 constructor，并新增只读转交：

```java
public final class EngineContext {
    public ModelAccessPolicyIndex modelAccessPolicyIndex();
}
```

它直接转交 `compiledModelSet().modelAccessPolicyIndex()`；不得复制或建立第二套 authority。

## 6. Compiler publication / semantic digest 顺序

真实当前 compiler 已通过 `DigestBoundCompiledInput` 把模型事实与摘要原子绑定，再由 `CompiledModelSetBuilder.FrozenInput.candidate(...)` 调用八参数 constructor。R12 冻结 P2 后的唯一 production 顺序：

```text
model access compilation complete
 -> exact CompiledModelAccessRule iterable
 -> ModelAccessPolicyIndex.of(rules)
 -> immutable policy index
 -> SemanticDigestInput includes same policy index canonical entries
 -> CompilerDigestService computes digest
 -> DigestBoundCompiledInput stores the same immutable policy index + digest
 -> CompiledModelSetBuilder.FrozenInput.candidate(...)
 -> CompiledModelSet.published(..., same policy index, same digest, ...)
 -> EngineContext
```

因此必须同步修改的 production contract：

- `SemanticDigestInput` 增加 immutable policy-index semantic input；
- `DigestBoundCompiledInput.bind(...)` 接收/冻结 policy index，并保证摘要使用的 index 与最终发布的 index 是同一个不可变 snapshot；
- `DigestBoundCompiledInput` 暴露只读 `modelAccessPolicyIndex()` 给 candidate publication；
- `CompiledModelSetBuilder.FrozenInput.candidate(...)` 的 P2 production path 必须调用 `CompiledModelSet.published(...)`，不得继续走 legacy 八参数 constructor；
- policy index 的 canonical digest entry 至少覆盖 exact rule key、status、runtime requirement identity、runtime binding plan identity 与影响授权语义的稳定字段；
- policy rule/status/plan/requirement 语义变化必须改变 semantic digest；runtime capability/issued-token/registry/one-shot state 不进入 digest。

Legacy direct caller 使用八参数 constructor 时保留其 supplied `DigestPair`；该 compatibility path 不伪称是 P2 compiler-published policy-aware closure。

## 7. Production-reachable trusted execution bridge

### 7.1 设计原则

R11 的 package-private `issueInvocation(...)` 继续保持 internal，**不会**改成 public。上层 module 也不再要求先拿 issued context/intent pair 才能调用 runtime。

公开 production execution capability 改为：

```java
public interface ProtectedExecutionToken {
    // marker only; no consumer/rule/operation/frame/owner/cursor authority getters
}

public final class ProtectedExecutionBridge {
    // no public/protected constructor or public rebind API
    public ProtectedAccessResult execute(ProtectedExecutionToken token);
}
```

一个 bridge 在 application/runtime composition 时一次绑定：

```text
one context-bound ProtectedAccessRuntime
one trusted adapter registration
one AccessConsumerIrKey
one exact ModelAccessRuleKey
one AccessOperation
one trusted execution-state port
one target-resolution port
one operation-execution port
```

上述 consumer/rule/operation 是 **composition-time frozen binding**，不是 per-call 参数。Bridge 不提供 setter/rebind/`execute(ruleKey,operation,...)`。

### 7.2 Trusted adapter SPI

`dec-core-starter.access.spi` 增加/冻结：

```java
public interface ProtectedExecutionStatePort {
    boolean recognizes(ProtectedExecutionToken token);
    RuntimeExecutionFrameId frameId(ProtectedExecutionToken token);
    RuntimeResolutionOwnerId ownerResolutionId(ProtectedExecutionToken token);
    Optional<RuntimeCollectionCursorId> collectionCursorId(
        ProtectedExecutionToken token);
}

public interface ProtectedExecutionBridgeReceiver {
    void bind(ProtectedExecutionBridge bridge);
}
```

并保留：

```text
ProtectedTargetResolutionPort
ProtectedOperationExecutionPort
ProtectedAccessAdapterRegistry
```

Normative：

- adapter/ports/receiver 只在 `ProtectedAccessRuntimeFactory` composition 时注册并 freeze；
- factory 为每个 exact registration 创建 bridge，随后**一次性**交付给该 registration 的 receiver；
- bridge possession 是 composition-issued capability；business caller 不通过 key 查询任意 bridge；
- per-call 只传 `ProtectedExecutionToken`；token 可以是上层 framework 的私有实现，但必须被**该 bridge 已绑定的 state port** 通过 object/type/registry semantics 识别；caller 自行实现 marker interface 不构成 authority；
- `ProtectedExecutionStatePort` 只提供当前 execution occurrence 的 frame/owner/cursor，不能改变 bridge 已绑定的 consumer/rule/operation；
- target/operation ports 与 bridge registration 一次绑定；Guard ALLOW 后不可换 adapter/target。

### 7.3 `ProtectedAccessRuntime` public surface

R12 不再把以下形态作为 public production entry：

```java
// NOT public production API
execute(ProtectedAccessResolutionContext context,
        ProtectedOperationIntent intent)
```

`ProtectedAccessRuntime` 可以继续是 public composition holder，但 `executeIssuedPair(...)` / `issueInvocation(...)` 为 starter package-private internal seam。外部合法 consumer 使用其 composition-issued `ProtectedExecutionBridge`。

这样消除 R11 的循环：

```text
future executor
  --X--> cannot call package-private issueInvocation directly
  --X--> does not need issued pair before public call

composition
  -> trusted adapter registration
  -> factory creates exact bridge
  -> receiver obtains bridge capability

runtime invocation
  -> adapter/framework creates its own recognized opaque token for current execution occurrence
  -> bridge.execute(token)
  -> starter validates token via bound state port
  -> starter derives frame/owner/cursor from trusted port
  -> starter uses bridge-bound consumer/rule/operation
  -> internal issueInvocation
  -> internal exact issued pair
  -> authenticity gate
  -> resolver/capability/gateway/Guard/operation
```

`dec-demo` 和未来 P3～P7 executor 因此可使用正式 production SPI 完成 E2E，不需要 reflection、test-only backdoor、同 package helper 或伪造 issued implementation。

## 8. Bridge invocation fail-closed contract

执行顺序冻结：

```text
STEP B0 bridge identity/context binding valid
STEP B1 bound ProtectedExecutionStatePort.recognizes(token)
        false -> DENY PROTECTED_EXECUTION_TOKEN_UNTRUSTED
STEP B2 starter derives frame/owner/cursor from bound trusted state port
        bridge-bound consumer/rule/operation remain immutable
STEP B3 internal registry.issueInvocation(derived trusted facts)
        -> internal issued context+intent exact pair
STEP B4 registry.requireIssuedPair(pair)
STEP B5 resolver -> one-shot ResolvedProtectedAccess
STEP B6 gateway -> Guard -> exact ModelAccessPolicyIndex lookup once
STEP B7 STATIC fast path OR runtime proof verification
STEP B8 same bridge/capability-bound target operation
```

对 B0/B1 失败：

```text
internal issued pair = 0
target resolution = 0
capability issuance = 0
Gateway = 0
Guard = 0
PolicyIndex lookup = 0
protected operation = 0
state change = 0
external effects = 0
```

不允许 `bridge.execute(token, ruleKey)`、`bridge.execute(token, operation)`、`bridge.execute(token, frame/owner/cursor)`。

## 9. Internal issued-pair authenticity（R11 防御保留）

`ProtectedAccessResolutionContext` / `ProtectedOperationIntent` 仍是 neutral read contracts；生产 implementations 仍为 starter package-private issued objects。`ContextLocalProtectedAccessRegistry.requireIssuedPair(...)` 继续按 exact object identity + exact pair relationship 验证 internal pair。

这层是 bridge 后的 defense-in-depth，不是外部 consumer 的 mint API。任何低层 forged implementation、A-context+B-intent、READ->WRITE/EXECUTE replacement 仍在 target resolution/policy lookup 前 fail closed：

- `PROTECTED_ACCESS_INPUT_UNTRUSTED`
- `PROTECTED_ACCESS_INPUT_PAIR_MISMATCH`

## 10. Guard / Gateway / policy authority

`DefaultModelAccessGuard` 只允许：

```text
engineContext.modelAccessPolicyIndex().find(access.requestedRuleKey())
```

恰好一次 exact lookup。禁止扫描 `definitions()`、从 `TypedDefinitionRegistries` 重建、starter secondary Map、resolver/gateway/verifier/adapter lookup。

STATIC_ALLOW：plan/requirement empty，verifier=0，evaluator=0，Guard-internal ALLOW。

RUNTIME_GUARD_REQUIRED：exact plan+requirement，`DefaultRuntimeBindingVerifier` 验证 hidden membership/provenance；verifier 不重查 policy。

Gateway 只执行 capability issuance 时绑定的 target+execution port+operation；A proof/capability 不得操作 B；terminal ALLOW/DENY consume one-shot capability。

## 11. TOCTOU / concurrency

- bridge registration immutable；运行期不能 rebind consumer/rule/operation/ports；
- execution token 只在 bound state port 的当前 execution occurrence 内有效；foreign/stale/replayed token fail closed；
- internal issued pair 与 capability 均 context-local；无 global current/singleton；
- capability reserve/consume atomic；concurrent execute 最多一个 terminal success；
- runtime path operation 前 revalidate Context/frame/cursor/rule/plan/membership；static path revalidate Context/frame/target binding；
- immutable policy index 不允许 lookup A 后切换 copied index B。

## 12. STATIC/RUNTIME unified path

```text
composition-issued bridge
 -> recognized execution token
 -> internal issued pair
 -> resolver capability
 -> gateway
 -> Guard ModelAccessPolicyIndex exact lookup = 1
      STATIC_ALLOW(no plan)
        -> verifier 0 / evaluator 0
      RUNTIME_GUARD_REQUIRED(plan+requirement)
        -> verifier 1
 -> same target operation once on ALLOW
```

Caller-side direct STATIC path仍是 `MODEL_ACCESS_GUARD_BYPASS`；proof/capability A + target B仍是 `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`。

## 13. P2 / P3～P7 scope boundary

P2 IN SCOPE：policy index/publication API、compiler publication/digest plumbing、starter bridge/runtime/access-control enforcement、trusted adapter SPI、fail-closed token/issued-pair/capability lifecycle。

P2 OUT OF SCOPE：完整 Rule/change/action/query evaluator、Information execution、QueryPlan、datasource transaction orchestration、业务 side effects、source-authored per-object ACL DSL。

Future executor 只需实现/注册自己的 trusted state/target/operation ports，并在 composition 获得 bridge；不得重新设计 permission channel。

## 14. Stable reasons

Compile 至少保持：`MIX-SYSTEM-DUPLICATE`、`MIX-RULEVIEW-SYSTEM-REQUIRED`、`MIX-MODEL-PATH-INVALID`、wildcard unsupported/empty、`MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`、`MIX-MODEL-ACCESS-DENIED`。

Runtime 至少：

- `PROTECTED_EXECUTION_TOKEN_UNTRUSTED`
- `PROTECTED_ACCESS_INPUT_UNTRUSTED`
- `PROTECTED_ACCESS_INPUT_PAIR_MISMATCH`
- `POLICY_NOT_FOUND`
- `CONTEXT_IDENTITY_MISMATCH`
- `MODEL_ACCESS_GUARD_BYPASS`
- `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`
- `RUNTIME_BINDING_REQUIRED`
- `RUNTIME_BINDING_PROOF_INVALID`
- `RUNTIME_BINDING_STALE`
- `RUNTIME_BINDING_PLAN_MISMATCH`
- `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`
- `RUNTIME_BINDING_CAPABILITY_CONSUMED`
- `GUARD_UNAVAILABLE`
- `STATIC_ALLOW`
- `RUNTIME_ALLOW`
- `RUNTIME_DENY`

## 15. Review / lifecycle gate

`DESIGN-P2-R12` **not PASSED**。本 Revision 是对 frozen `DESIGN-P2-R11` Review finding 的 candidate remediation：

- FND-004：仍 `PARTIAL_FIX_PROPOSED / OPEN`，production-reachable bridge + trusted issuance candidate 已冻结；
- FND-015：`PARTIAL_FIX_PROPOSED / OPEN`，validated index construction + explicit publication/legacy compatibility candidate 已冻结；
- FND-016：`PARTIAL_FIX_PROPOSED / OPEN`，真实 consumer source-to-runtime reachability candidate 已冻结；
- 不新增 FND-020；effective OPEN P1 仍为 19。

必须由 exact `DESIGN-P2-R12` Architecture + ApiContract + Develop + Impact + CrossModule + Concurrency Review 独立确认后，才能调整上述 candidate 状态；RC9 machine lifecycle/risk Evidence 仍缺失。Implementation Plan / TDD / Development 保持 BLOCKED。