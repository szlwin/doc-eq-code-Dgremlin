# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R09`。Base：`DESIGN-P2-R08`，继承 P1 已通过的 Compiler Pipeline/Context 基线。
> 输入 Business Model 候选：`BM-R12`（标准 changeset `CHG-V_1.0-COMPILER-P2-BM-R12`；在正式 RC9 reopen/publish 前仍为 MACHINE_BLOCKED）。
> 状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本文件是 P2 当前 canonical Design source；历史 R01～R08 保留在 Git/changes 历史中，与本文件冲突时以 R09 为当前候选语义。

## 1. 设计目标与不可绕过约束

1. System 是显式一等身份；RuleView 唯一身份为 `(SystemKey,name)`。
2. READ/WRITE/EXECUTE 独立授权，未声明即拒绝；共享 WRITE 默认拒绝。
3. **所有 protected READ/WRITE/EXECUTE 都必须通过同一个 `ProtectedAccessGateway -> ModelAccessGuard` 执行路径。** `STATIC_ALLOW` 只能是 Guard 在 exact rule lookup 之后的内部 fast path，caller 不得存在静态直通入口。
4. runtime exact lookup 禁止 wildcard、prefix/suffix、parent/child、bare-name 与跨 target/System fallback。
5. Java 生产 API 以 release 8 为约束；现有 `public final class EngineContext` 与 P1 API 保持 additive compatibility。
6. P2 不新增 source-authored 权限 Predicate DSL；AC-006 只处理已静态授权 surface 下的 runtime object binding。
7. 被 Guard 验证的实际 target 与最终 protected operation target 必须是同一个不可替换 framework binding；proof/capability A 不得授权 B。
8. DENY 必须发生在 read/write/execute、状态推进和外部副作用之前。
9. `RuntimeBindingPlan` **只属于 `RUNTIME_GUARD_REQUIRED`**；`STATIC_ALLOW` 不得伪造、借用或要求 RuntimeBindingPlan。

## 2. 模块与 ownership

```text
dec-core-context
  dec.core.context.model.*
  dec.core.context.model.access.*
      neutral immutable rule / plan / requirement contracts
      ResolvedProtectedAccess / Guard / decision contracts

        ^ depends-on
        |
dec-core-compiler
  dec.core.compiler.*
      resolved access-consumer IR
      production DynamicBindingClassifier
      exact rule / RuntimeBindingPlan publication

        ^
        |
framework execution runtime
      ProtectedAccessResolutionContext ownership
      actual target resolution
      one-shot ResolvedProtectedAccess issuance
      ProtectedAccessGateway -> Guard -> same-target execution
      runtime membership verifier only when selected rule requires it
```

禁止 context -> compiler、split package、compiler -> concrete parser、global mutable current，以及 business caller 构造 replacement rule/plan/requirement/capability 或 protected-target bypass。

## 3. Production classifier

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }
```

分类在 exact static authorization 后消费 production `ResolvedAccessConsumerIr`：

- `DIRECT_EXACT -> STATIC_BOUND`；真实 fixture：`systems.xml / order.ordered / status = 1`。
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND`；真实 fixture：`every(orderDetailList, status = 1)` 中 element `status` READ。
- 其它 runtime index/key/filter/find/selector 或无法确定 element shape -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

Classifier stub 仅用于下游 unit isolation，不能证明 classifier correctness/AC-006。

## 4. Compiled rule / RuntimeBindingPlan

```java
public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
    public SourceRef sourceRef();
}
```

规范状态：

```text
STATIC_BOUND
 -> STATIC_ALLOW
 -> runtimeRequirement = empty
 -> runtimeBindingPlan = empty

RUNTIME_OBJECT_BOUND
 -> RUNTIME_GUARD_REQUIRED
 -> runtimeRequirement = EXACT_RUNTIME_BINDING
 -> runtimeBindingPlan = exactly one compiler-published plan
```

`RuntimeBindingPlan(COLLECTION_ELEMENT_MEMBERSHIP)`、requirement key、model-shape digest 与 exact rule 同属 immutable `CompiledModelSet` closure。只有 runtime-required rule 可以引用 plan；STATIC_ALLOW rule 携带 plan 属非法 compiled state。

## 5. ProtectedAccessResolutionContext：所有 protected access 的 framework execution frame

R08 的 `RuntimeResolutionContext` 名称和职责过窄，只能解释 runtime-plan path。R09 将当前候选 contract 统一为 framework-owned `ProtectedAccessResolutionContext`：

```java
public interface ProtectedAccessResolutionContext {
    String engineContextId();
    AccessConsumerIrKey accessConsumerIrKey();
    RuntimeExecutionFrameId frameId();
    RuntimeResolutionOwnerId ownerResolutionId();
    Optional<RuntimeCollectionCursorId> collectionCursorId();
}
```

Normative：

- 只能由 framework execution pipeline 创建；business code 无 public constructor/factory。
- 绑定 current EngineContext、当前 resolved access-consumer IR、execution frame/root owner；`every(...)` 等 runtime element 场景额外绑定 collection cursor。
- 生命周期只覆盖当前 protected access evaluation；不得跨 Context、rule evaluation、frame/cursor 缓存复用。
- 不暴露 raw domain object getter；actual target identity/provenance 只在 framework context-local registry/执行帧内部存在。
- DIRECT_EXACT 与 EVERY_COLLECTION_ELEMENT 都通过该 execution context 解析实际 target；区别在于 Guard 是否需要额外 runtime membership verification，而不是 capability 是否存在。

## 6. 通用 one-shot `ResolvedProtectedAccess`

`ResolvedProtectedAccess` 是**所有 protected access** 的 execution capability，不等价于 runtime-binding capability，也不要求 RuntimeBindingPlan：

```java
public final class ResolvedProtectedAccess {
    // no public/protected constructor; no public mint/factory
    public String capabilityId();
    public String engineContextId();
    public ModelAccessRuleKey requestedRuleKey();
    public AccessOperation operation();
    public RuntimeExecutionFrameId executionFrameId();
}
```

Capability 的 hidden framework binding 同时包含：

- exact actual target identity；
- operation payload/action identity；
- resolution owner identity；
- optional collection cursor identity；
- optional runtime membership/provenance；
- one-shot lifecycle state。

`requestedRuleKey` 由已解析 access-consumer IR 的 exact System/target/ModelPath/operation 确定性形成，不是 caller 自报 selected policy。Capability 创建阶段**不需要 PolicyIndex lookup，也不需要 RuntimeBindingPlan**。

## 7. `ProtectedAccessResolver`：先绑定 target，不预判 policy status

```java
public interface ProtectedAccessResolver {
    ResolvedProtectedAccess resolve(
        ProtectedAccessResolutionContext executionContext,
        ProtectedOperationIntent operationIntent);
}
```

`ProtectedOperationIntent` 是 framework-owned immutable intent，包含当前 access-consumer IR 已确定的 exact `ModelAccessRuleKey`、READ/WRITE/EXECUTE intent 与必要 payload/action identity，但没有 caller 可替换的 raw target 参数。

Resolver 负责：

1. 在当前 execution frame 中解析实际 target A；
2. 将 A + exact operation intent + frame/owner/cursor 原子登记为 one-shot capability；
3. 不查询/选择 policy status；
4. 不要求 runtime plan；
5. 不返回 detached runtime handle 供业务 caller 重组。

因此同一个 resolver 能合法创建 STATIC_ALLOW 与 RUNTIME_GUARD_REQUIRED 两类访问所需 capability。

## 8. `ProtectedAccessGateway` + `ModelAccessGuard`：统一执行路径

```java
public interface ProtectedAccessGateway {
    ProtectedAccessResult execute(ResolvedProtectedAccess access);
}

public interface ModelAccessGuard {
    ModelAccessDecision authorize(ResolvedProtectedAccess access);
}
```

Gateway 是所有 protected READ/WRITE/EXECUTE 的唯一支持执行入口。固定顺序：

1. 原子 reserve one-shot capability；若已 consumed -> DENY。
2. 调用 Guard **恰好一次**；Gateway 自身不得额外 PolicyIndex lookup。
3. Guard 使用 `requestedRuleKey` 对 current Context PolicyIndex 做 **恰好一次 exact lookup** 得到 selected rule。
4. Guard 验证 Context/key/operation/capability frame 基本一致性。
5. 根据 selected rule status 进入唯一分支：
   - `STATIC_ALLOW`：确认 selected rule 的 plan/requirement 都为空；runtime verifier/evaluator 调用数 = 0；返回内部 fast-path ALLOW。
   - `RUNTIME_GUARD_REQUIRED`：require exact plan + requirement，并调用 RuntimeBindingVerifier 校验 capability hidden membership/provenance 对 current plan/rule/Context/frame/cursor 仍有效；通过才 ALLOW。
6. ALLOW 后 Gateway 在同一个 framework execution boundary 内对 capability hidden-bound **同一个 actual target + 同一个 operation** 执行。
7. 成功或 terminal DENY 都 consume capability；caller 不获得可复用 detached ALLOW token。

### 8.1 STATIC_ALLOW 的完整可达路径（FND-001）

```text
DIRECT_EXACT source/IR
 -> STATIC_BOUND
 -> compiled rule STATIC_ALLOW, no RuntimeBindingPlan
 -> framework resolves actual target A
 -> generic ResolvedProtectedAccess A
 -> ProtectedAccessGateway.execute(A)
 -> ModelAccessGuard.authorize(A)
      -> exact PolicyIndex lookup = 1
      -> selected STATIC_ALLOW
      -> runtime verifier = 0
      -> evaluator = 0
      -> ALLOW fast path inside Guard
 -> gateway executes same hidden A target once
```

任何 caller-side：

```text
if (rule.status == STATIC_ALLOW) directReadOrWrite(target)
```

都属于 `MODEL_ACCESS_GUARD_BYPASS`，不属于支持架构。

### 8.2 RUNTIME_GUARD_REQUIRED 路径

```text
EVERY_COLLECTION_ELEMENT source/IR
 -> RUNTIME_OBJECT_BOUND
 -> runtime rule + RuntimeBindingPlan + EXACT_RUNTIME_BINDING
 -> framework resolves current actual element A into generic capability A
 -> Gateway -> Guard exact lookup once
 -> selected runtime rule
 -> RuntimeBindingVerifier verifies A membership/provenance against exact plan
 -> ALLOW
 -> Gateway executes same hidden A target once
```

STATIC 与 runtime path 共享 capability/gateway/Guard；差异仅发生在 Guard 选中 rule 后是否需要 runtime proof。

## 9. Proof-to-operation binding / FND-019

支持 API 不存在：

- `execute(capability, target)`；
- `execute(handle, rawObject)`；
- caller callback/closure 在 ALLOW 后选择第二 protected target；
- caller-side direct static executor。

若低层 invariant seam 人为制造 `executorActualTarget != capabilityHiddenTarget`，必须在 protected operation 前 DENY `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`，A/B protected operation count 均为 0。

## 10. TOCTOU / concurrency

Gateway 在 one-shot reserve 与 operation 之间维持 context-local execution invariant。对 runtime-required capability，Guard 在实际 operation 前重新确认 Context、frame/cursor、selected rule/plan 与 membership/provenance；对 static capability，至少重新确认 Context/frame/capability target binding 未失效。Capability consumption 状态转换必须原子化；并发 `execute(A)` 最多一个 terminal successful consumer。

## 11. ModelPath / wildcard

Runtime `ModelPath` 永远 exact。真实 `systems.xml` 中 READ `path="*"` 只允许 compile-time 对 exact target path catalog 做 finite canonical expansion；runtime PolicyIndex 不含 wildcard。wildcard WRITE/EXECUTE、empty expansion、parent/fuzzy fallback 均禁止。

## 12. RuntimeFactValue / evaluator / timeout

继续保持 closed Java-8 `RuntimeFactValue`：public final、private constructor、六种 typed factory、LIST/OBJECT deep immutable、typed visitor、deterministic canonical form。

当前 AC-006 不使用 business predicate evaluator。R04 bounded evaluator executor/fake monotonic time/timed Future/cancel/fail-closed 仅保留给未来 Requirement-authorized predicate extension；STATIC_ALLOW 和当前 EXACT_RUNTIME_BINDING 均不得无条件触发 evaluator。

## 13. EngineContext compatibility

保留 final class、现有单参 constructor、`compiledModelSet()/modelSet()/projection()`；P2 只增加 owner-qualified lookup、contextId、fail-closed Guard、`ProtectedAccessResolver` 与 `ProtectedAccessGateway` 等 additive read surfaces。禁止新增 bare-name `findRuleView(String)`。

## 14. Stable reasons

Compile 至少：`MIX-SYSTEM-DUPLICATE`、`MIX-RULEVIEW-SYSTEM-REQUIRED`、`MIX-MODEL-PATH-INVALID`、wildcard unsupported/empty、`MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`、`MIX-MODEL-ACCESS-DENIED`。

Runtime 至少：`POLICY_NOT_FOUND`、`CONTEXT_IDENTITY_MISMATCH`、`MODEL_ACCESS_GUARD_BYPASS`、`RUNTIME_BINDING_REQUIRED`、`RUNTIME_BINDING_PROOF_INVALID`、`RUNTIME_BINDING_STALE`、`RUNTIME_BINDING_PLAN_MISMATCH`、`RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`、`RUNTIME_BINDING_CAPABILITY_CONSUMED`、`GUARD_UNAVAILABLE`、`STATIC_ALLOW`、`RUNTIME_ALLOW`、`RUNTIME_DENY`。

## 15. Full source -> protected operation chain

```text
real source
 -> resolved access-consumer IR
 -> exact static authorization
 -> production DynamicBindingClassifier
      DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW(no plan)
      EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> runtime rule + plan
 -> immutable CompiledModelSet publication
 -> framework resolves actual target into generic ResolvedProtectedAccess
 -> ProtectedAccessGateway
 -> ModelAccessGuard exact lookup once
      STATIC_ALLOW -> fast path, runtime verifier/evaluator 0
      RUNTIME_GUARD_REQUIRED -> exact runtime plan/proof verification
 -> Gateway executes same internally-bound target+operation once
 -> no caller-side static or runtime bypass
```

## 16. Review / lifecycle gate

`DESIGN-P2-R09` **not PASSED**。FND-001、FND-004、FND-019 仍保持 OPEN，候选内容必须由正式 RC9 lifecycle 绑定 exact revision 后再完成 ApiContract/Architecture/BusinessModel/Requirement/TestDesign/Develop/Impact/CrossModule/Concurrency 等独立 Review 与 current-risk Evidence。当前不制造 repository Evidence，不推进 Implementation Plan/TDD/Development。
