# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R11`。Base：`DESIGN-P2-R10`，输入 Business Model candidate：`BM-R12`。
> 状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。本 Revision 只收敛 FND-P2-REV-004 剩余的两个 implementation-readiness contract：framework-owned context/intent 的不可伪造 issuance authority，以及 Compiler -> CompiledModelSet -> EngineContext -> Guard 的唯一 immutable PolicyIndex authority；不新增 FND-020，不改变 BM-R12 业务语义。
> 当前 canonical Business Model 仍是历史 BM-R07；正式 RC9 reopen/publish、current-revision risk Evidence 与 exact independent Review 完成前，本 Design 不得 PASSED。

## 1. 设计目标与不可绕过约束

1. System 是显式一等身份；RuleView 唯一身份为 `(SystemKey,name)`。
2. READ/WRITE/EXECUTE 独立授权，未声明即拒绝；共享 WRITE 默认拒绝。
3. 所有 protected READ/WRITE/EXECUTE 必须通过同一个 starter-owned protected runtime；`STATIC_ALLOW` 只能是 Guard exact lookup 后的内部 fast path。
4. Runtime ModelPath lookup exact-only；wildcard 只允许 compile-time finite canonical expansion。
5. Java 生产 API 保持 release 8；`EngineContext` 现有 final class、单参 constructor、`compiledModelSet()/modelSet()/projection()` 保持兼容。
6. `RuntimeBindingPlan` 只属于 `RUNTIME_GUARD_REQUIRED`；STATIC_ALLOW 不得伪造或要求 runtime plan。
7. 被 Guard 验证的 actual target 与最终 operation target 必须是同一个 framework binding；capability/proof A 不得授权 B。
8. **`ProtectedAccessResolutionContext` 与 `ProtectedOperationIntent` 的公开 getter 不是 authority。生产 runtime 只接受 starter context-local registry 已签发并按对象身份登记的 exact issued pair。**
9. **唯一 policy authority 是 compiler-published、CompiledModelSet-owned 的 immutable `ModelAccessPolicyIndex`。Guard 不得扫描 definitions、复制第二个 Map、或从 resolver/gateway/verifier/adapter 重新选择 policy。**
10. DENY 必须先于 target resolution、capability issuance、policy lookup、模型访问、状态推进和外部副作用中与该失败点对应的任何后续动作。
11. P2 只交付访问控制执行边界所需 runtime plumbing，不实现 P3～P7 的 Information/Rule/Change/Action/QueryPlan 完整执行语义。

## 2. Repository-valid Maven ownership（R10 保持）

```text
dec-core-context
  dec.core.context.model.access.*
  -> neutral immutable contracts/facts
  -> ModelAccessPolicyIndex + issued-input read interfaces

       ^ existing dependency
       |
dec-core-compiler
  dec.core.compiler.access.*
  -> resolved access-consumer IR
  -> production DynamicBindingClassifier
  -> exact CompiledModelAccessRule / RuntimeBindingPlan publication
  -> ModelAccessPolicyIndex assembly + semantic-digest contribution

       ^ existing composition dependency
       |
dec-core-starter
  dec.core.starter.access.*
  -> concrete resolver / gateway / guard / verifier / registry / runtime
  -> package-private issued context/intent implementations
  dec.core.starter.access.spi.*
  -> trusted composition-time execution adapters

       ^ application/composition dependency
dec-demo and future P3-P7 execution modules
  -> consume starter runtime / provide trusted adapters
```

R11 不新增 `dec-core-runtime` 或其他 Maven module。禁止 context -> compiler/starter、compiler -> starter、starter 为 P2 access control 新增对 `dec-core-model` 的业务耦合、split package、global mutable current Context。

## 3. Compile-time access rule invariant

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }
```

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；真实 fixture：`systems.xml / order.ordered / status = 1`。
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；真实 fixture：`every(orderDetailList,status = 1)` element `status` READ。
- 其它未冻结 runtime index/key/filter/find/selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

```text
STATIC_ALLOW
 -> runtimeRequirement = empty
 -> runtimeBindingPlan = empty

RUNTIME_GUARD_REQUIRED
 -> EXACT_RUNTIME_BINDING requirement present
 -> exactly one compiler-published RuntimeBindingPlan present
```

## 4. 唯一 immutable `ModelAccessPolicyIndex` authority

### 4.1 Context-owned neutral API

`dec-core-context / dec.core.context.model.access` 增加唯一 runtime access-policy index：

```java
public final class ModelAccessPolicyIndex {
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

Normative：

- immutable snapshot；`find` 只接受 exact `ModelAccessRuleKey`，禁止 wildcard/prefix/suffix/parent/child/bare-name fallback；
- index key 必须与 `CompiledModelAccessRule.key()` 完全一致；duplicate/mismatch 在 publish 前失败；
- runtime index 中不得保存 wildcard key；READ `*` 已在 compiler 阶段 finite expansion；
- `keys()` deterministic/read-only，仅用于 diagnostics/tests，不允许 caller 重建 runtime authority。

### 4.2 `CompiledModelSet` publication contract

`CompiledModelSet` 必须把 policy index 作为同一个 immutable publication closure 的一部分：

```java
public final class CompiledModelSet {
    // existing constructor/API remain compatible via additive implementation strategy
    public ModelAccessPolicyIndex modelAccessPolicyIndex();
}
```

生产构造/assembly 必须使 `sourceManifest + definitions + typedRegistries + deferred + modelAccessPolicyIndex + diagnostics + versions + digestPair` 一次冻结。Compiler 是 index 的唯一生产发布者。

Semantic digest 必须覆盖 policy index 的 canonical ordered entries，至少包括 exact rule key、status、runtime requirement identity、runtime binding plan identity 与影响授权语义的稳定 rule fields。要求：

- 等价 source/order -> same index canonical form + same semantic digest；
- policy rule/status/plan/requirement 的语义变化 -> semantic digest 必须变化；
- runtime capability/registry/one-shot state 不进入 semantic digest。

### 4.3 `EngineContext` read authority

`EngineContext` 保持 existing constructor，不复制 policy：

```java
public final class EngineContext {
    public ModelAccessPolicyIndex modelAccessPolicyIndex();
}
```

该 accessor 必须直接返回 `compiledModelSet().modelAccessPolicyIndex()` 的同一个 immutable authority（允许等价实现，但不得创建第二套可漂移 registry）。

### 4.4 Guard 的唯一 lookup

`DefaultModelAccessGuard` 只允许：

```text
engineContext.modelAccessPolicyIndex().find(access.requestedRuleKey())
```

恰好一次 exact lookup。禁止：

- 扫描 `definitions()` 推断权限；
- 在 starter 创建/缓存第二个 `Map<ModelAccessRuleKey,...>` 作为 authority；
- resolver/gateway/verifier/adapter 做 policy lookup；
- 从 `TypedDefinitionRegistries`、business caller 或 capability getter 重建 policy。

`POLICY_NOT_FOUND` 只能由上述唯一 index 的 exact miss 产生。

## 5. Framework-issued input authority

### 5.1 Public read contracts are not mint authority

为了保持 context-neutral cross-module contract，以下类型仍可为 public read interfaces：

```java
public interface ProtectedAccessResolutionContext {
    String engineContextId();
    AccessConsumerIrKey accessConsumerIrKey();
    RuntimeExecutionFrameId frameId();
    RuntimeResolutionOwnerId ownerResolutionId();
    Optional<RuntimeCollectionCursorId> collectionCursorId();
}

public interface ProtectedOperationIntent {
    ModelAccessRuleKey requestedRuleKey();
    AccessOperation operation();
}
```

这些 getter 只用于 diagnostics/read-only inspection。**任何 caller 自行实现接口并填写 getter 值都不获得 authority。** Resolver/Guard 不得把 getter 值本身当作“framework-owned”的证明。

### 5.2 Production implementation ownership

生产实现只允许 starter package-private classes：

```text
dec.core.starter.access.IssuedProtectedAccessResolutionContext
dec.core.starter.access.IssuedProtectedOperationIntent
```

无 public/protected constructor/factory；不从 `ProtectedAccessRuntime` 暴露 public mint API。

### 5.3 `ContextLocalProtectedAccessRegistry` issuance record

同一个 context-local registry 除 capability state 外，还保存 package-private `IssuedInvocationRecord`。每条 record 绑定：

```text
exact context object identity
exact intent object identity
EngineContext identity + engineContextId
AccessConsumerIrKey
RuntimeExecutionFrameId
RuntimeResolutionOwnerId
optional RuntimeCollectionCursorId
exact requested ModelAccessRuleKey
AccessOperation
hidden operation payload/action identity
trusted adapter binding / consumer kind
issuance lifecycle state
```

Authority 来自 registry record，不来自接口 getter。生产必须按 **reference identity + exact pair relationship** 验证。

### 5.4 Trusted issuance path

Issued pair 只能由 starter 内部 issuance path 根据 composition-time trusted framework adapter/execution state 创建：

```text
trusted execution adapter registered/frozen at runtime composition
 -> starter internal issuance path
 -> ContextLocalProtectedAccessRegistry.issueInvocation(...)
 -> exact IssuedProtectedAccessResolutionContext A
 -> exact IssuedProtectedOperationIntent A
 -> registry stores authoritative pair A
```

`issueInvocation(...)` 为 starter-internal/package-private implementation detail；business caller、Rule source、change/custom action code、future executor business logic均无 public mint/factory。未来 execution adapter 只能通过 composition-time trusted integration 得到/转交 issued pair，不能每次调用提交任意 raw facts 让 starter“签名”。

Host/application composition 对 adapter registration 是 framework trust boundary；per-business-operation caller 不是该 trust boundary。

## 6. `ProtectedAccessRuntime.execute` authenticity gate

Public facade 继续保持：

```java
public final class ProtectedAccessRuntime {
    public ProtectedAccessResult execute(
        ProtectedAccessResolutionContext context,
        ProtectedOperationIntent intent);
}
```

但执行顺序现在冻结为：

```text
STEP 0  registry.requireIssuedPair(context,intent)
        - exact object identity known
        - exact context+intent pair known
        - EngineContext/frame/owner/cursor/consumer/rule/op facts match authoritative record
        - issuance still active

        unknown caller implementation
          -> DENY PROTECTED_ACCESS_INPUT_UNTRUSTED

        issued context A + issued intent B / pair mismatch
          -> DENY PROTECTED_ACCESS_INPUT_PAIR_MISMATCH

        both failures happen BEFORE:
          target resolution = 0
          capability issuance = 0
          PolicyIndex lookup = 0
          protected operation = 0
          external effects = 0

STEP 1  resolver uses authoritative IssuedInvocationRecord, not caller getter values,
        to resolve target and create one-shot ResolvedProtectedAccess
STEP 2  gateway -> Guard
STEP 3  Guard exact ModelAccessPolicyIndex lookup once
STEP 4  static fast path OR runtime proof verification
STEP 5  same capability-bound target operation
```

Operation substitution READ -> WRITE/EXECUTE cannot be achieved by supplying a new `ProtectedOperationIntent` implementation；it fails STEP 0。A legitimately issued intent's operation is immutable and registry-bound。

## 7. `ResolvedProtectedAccess` authority

```java
public final class ResolvedProtectedAccess {
    public String capabilityId();
    public String engineContextId();
    public ModelAccessRuleKey requestedRuleKey();
    public AccessOperation operation();
    public RuntimeExecutionFrameId executionFrameId();
    // no public/protected constructor, mint API, raw target getter or selected-policy setter
}
```

Capability is issued only after STEP 0 succeeds。Its public getters are views；hidden registry binding remains authoritative for actual target, operation payload/action identity, frame/owner/cursor/provenance and one-shot lifecycle。

## 8. Starter concrete responsibilities

### 8.1 `DefaultProtectedAccessResolver`

- first consumes validated `IssuedInvocationRecord` only；
- target resolution occurs after input-authenticity gate；
- binds actual target + exact operation + adapter + frame/owner/cursor/provenance；
- creates capability；
- PolicyIndex lookup = 0。

### 8.2 `DefaultModelAccessGuard`

- owns current immutable EngineContext；
- calls **only** `engineContext.modelAccessPolicyIndex().find(requestedRuleKey)` exactly once；
- STATIC_ALLOW: plan/requirement empty, verifier=0/evaluator=0；
- RUNTIME_GUARD_REQUIRED: require exact compiler-published plan/requirement and call `DefaultRuntimeBindingVerifier`；
- no copied/secondary policy map。

### 8.3 `DefaultProtectedAccessGateway`

- atomically reserves capability；
- Guard exactly once；Gateway policy lookup=0；
- ALLOW 后只能执行 capability issuance 时登记的 same target + execution port + operation；
- consume on terminal ALLOW/DENY；no second target/callback。

### 8.4 `DefaultRuntimeBindingVerifier`

- runtime-required branch only；
- verifies hidden membership/provenance against selected exact rule/plan/current Context/frame/cursor；
- policy lookup=0；STATIC_ALLOW calls=0。

## 9. Trusted adapter SPI / consumer integration

R10 ownership保持：

```text
dec.core.starter.access.spi.ProtectedTargetResolutionPort
dec.core.starter.access.spi.ProtectedOperationExecutionPort
dec.core.starter.access.spi.ProtectedAccessAdapterRegistry
```

新增明确 invariant：composition-time adapter registration 是 trusted framework boundary；per-call business code cannot register adapters or ask runtime to issue context/intent from caller-selected facts。

Production consumer chain：

```text
framework execution adapter/current execution state
 -> starter-issued exact context+intent pair
 -> ProtectedAccessRuntime.execute(pair)
 -> authenticity gate
 -> resolver -> capability
 -> gateway -> Guard -> one immutable policy index lookup
 -> static/runtime branch
 -> same-bound operation
```

禁止 consumer：自己 new/implement context/intent 作为 authority、改 consumerIrKey/frame/owner/cursor/rule/op、自己查询 policy、自己 mint capability、Guard 后换 target/adapter、建立第二权限 registry。

## 10. Unified STATIC/RUNTIME behavior

```text
issued exact pair
 -> authenticity gate PASS
 -> resolver capability
 -> gateway
 -> Guard ModelAccessPolicyIndex exact lookup = 1
      STATIC_ALLOW(no plan)
        -> verifier 0 / evaluator 0
      RUNTIME_GUARD_REQUIRED(plan+requirement)
        -> verifier 1
 -> same target operation once on ALLOW
```

`STATIC_ALLOW` caller-side direct path仍是 `MODEL_ACCESS_GUARD_BYPASS`。Capability/proof A + target B仍是 `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`。

## 11. TOCTOU / concurrency

- issued invocation record 与 capability reserve/consume均 context-local；不得 global mutable。
- issued pair replay/expired frame -> fail closed；capability concurrent execute最多一个 terminal success。
- runtime path operation 前 revalidate Context/frame/cursor/rule/plan/membership；static path revalidate Context/frame/target binding。
- policy index immutable，运行期不存在“先查 index A、后切到 copied index B”的 TOCTOU。

## 12. ModelPath / wildcard / digest

Runtime `ModelAccessPolicyIndex` 只含 exact keys。READ `path="*"` compile-time finite expansion；wildcard WRITE/EXECUTE、empty expansion、parent/fuzzy fallback compile ERROR。

Semantic digest必须覆盖最终 exact policy index；等价 source ordering deterministic，任何授权语义变化改变 semantic digest。

## 13. EngineContext / Java 8 compatibility

- `EngineContext` 保持 `public final`、现有单参 constructor/core accessors；只 additive `modelAccessPolicyIndex()`。
- Context 不依赖 starter concrete types。
- Public read interfaces + starter package-private issued implementations兼容 Java 8；禁止 sealed class/record/Java9 collection factories。

## 14. Stable reasons

Compile 至少：`MIX-SYSTEM-DUPLICATE`、`MIX-RULEVIEW-SYSTEM-REQUIRED`、`MIX-MODEL-PATH-INVALID`、wildcard unsupported/empty、`MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`、`MIX-MODEL-ACCESS-DENIED`。

Runtime 至少：

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

## 15. P2 / P3-P7 scope boundary

P2 IN SCOPE：immutable exact policy index publication/read authority、trusted input issuance/authenticity gate、starter resolver/guard/verifier/gateway/registry/factory、one-shot target binding、TOCTOU/replay fail closed、adapter SPI/no-bypass rule。

P2 OUT OF SCOPE：完整 Information/Rule/change/action/query evaluator/executor、QueryPlan、datasource transaction orchestration、source-authored per-object permission predicate DSL、P3-P7 business side effects。

## 16. Review / lifecycle gate

`DESIGN-P2-R11` **not PASSED**。FND-004 remains `PARTIAL_FIX_PROPOSED / OPEN` until exact Architecture + ApiContract + Develop + Impact + CrossModule + Concurrency Review confirms trusted issuance + single PolicyIndex authority and RC9 machine lifecycle/risk Evidence is valid。FND-001/FND-019 candidate semantics remain substantively fixed but formally OPEN。Implementation Plan / TDD / Development remain BLOCKED。