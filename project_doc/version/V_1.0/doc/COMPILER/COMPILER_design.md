# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R10`。Base：`DESIGN-P2-R09`，输入 Business Model candidate：`BM-R12`。
> 状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。本 Revision 专门收敛 FND-P2-REV-004 的 repository-valid implementation ownership；不新增 FND-020，不改变 BM-R12 业务语义。
> 当前 canonical Business Model 仍是历史 BM-R07；正式 RC9 reopen/publish、current-revision risk Evidence 与 exact independent Review 完成前，本 Design 不得 PASSED。

## 1. 设计目标与不可绕过约束

1. System 是显式一等身份；RuleView 唯一身份为 `(SystemKey,name)`。
2. READ/WRITE/EXECUTE 独立授权，未声明即拒绝；共享 WRITE 默认拒绝。
3. 所有 protected READ/WRITE/EXECUTE 必须通过同一个 protected-access runtime；`STATIC_ALLOW` 只能是 Guard exact lookup 后的内部 fast path。
4. Runtime ModelPath lookup exact-only；wildcard 只允许 compile-time finite expansion。
5. Java 生产 API 保持 release 8；`EngineContext` 现有 final class、单参 constructor、`compiledModelSet()/modelSet()/projection()` 保持兼容。
6. `RuntimeBindingPlan` 只属于 `RUNTIME_GUARD_REQUIRED`；STATIC_ALLOW 不得伪造或要求 runtime plan。
7. 被 Guard 验证的 actual target 与最终 operation target 必须是同一个 framework binding；capability/proof A 不得授权 B。
8. DENY 必须先于模型访问、状态推进和外部副作用。
9. P2 只交付访问控制执行边界所需 runtime plumbing，不实现 P3～P7 的 Information/Rule/Change/Action/QueryPlan 完整执行语义。

## 2. R10 repository-valid Maven ownership

真实 reactor 已存在 `dec-core-context`、`dec-core-compiler`、`dec-core-frontends`、`dec-core-starter`、`dec-demo` 等模块；R10 **不新增 `dec-core-runtime` 或任何新 Maven module**。

冻结依赖/ownership：

```text
dec-core-context
  package: dec.core.context.model.access.*
  owns: neutral immutable contracts/facts only
        CompiledModelAccessRule / RuntimeBindingPlan / RuntimeAccessRequirement
        ProtectedAccessResolutionContext / ProtectedOperationIntent
        ResolvedProtectedAccess / ModelAccessDecision / reasons
        public neutral interfaces required across modules

       ^ existing dependency
       |
dec-core-compiler
  package: dec.core.compiler.access.*
  owns: resolved access-consumer IR
        production DynamicBindingClassifier
        exact access-rule assembly
        RuntimeBindingPlan/Requirement publication

       ^ existing composition dependency
       |
dec-core-starter
  package: dec.core.starter.access.*
  owns: concrete protected runtime composition and implementations
        DefaultProtectedAccessResolver
        DefaultProtectedAccessGateway
        DefaultModelAccessGuard
        DefaultRuntimeBindingVerifier
        ContextLocalProtectedAccessRegistry
        ProtectedAccessRuntime
        ProtectedAccessRuntimeFactory

  package: dec.core.starter.access.spi.*
  owns: bootstrap-time trusted framework adapter SPI
        ProtectedTargetResolutionPort
        ProtectedOperationExecutionPort
        ProtectedAccessAdapterRegistry

       ^ application/composition consumer
dec-demo and future P3-P7 execution modules
  use starter runtime / provide trusted adapters at composition time
  MUST NOT perform independent PolicyIndex lookup or protected-operation bypass
```

禁止：context -> compiler/starter、compiler -> starter、starter -> `dec-core-model` 仅为 P2 access control 而新增的反向/业务耦合、split package、global mutable current Context。

### 2.1 为什么 concrete runtime 归属 `dec-core-starter`

- `dec-core-starter` 已是当前真实 production composition root：它依赖 compiler/frontends 并拥有 `CompilerBootstrap` / `CompilerStarter`。
- `dec-demo` 已显式依赖 `dec-core-starter`，因此上层应用已有稳定的 composition 方向。
- 将 concrete Guard/Gateway/registry 放 context 会污染 neutral context；放 compiler 会把 execution runtime 反向塞入 compile-time module；新增 runtime module 会无必要扩大 P2 reactor scope。
- Starter concrete runtime 只依赖 context/compiler contracts，不直接理解 OrderInfo/POJO、Rule/Change/Action/QueryPlan 业务语义。

## 3. Production classifier / compiled rule invariant

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }
```

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；真实 fixture：`systems.xml / order.ordered / status = 1`。
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；真实 fixture：`every(orderDetailList,status = 1)` element `status` READ。
- 其它未冻结 runtime index/key/filter/find/selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

Compiled state：

```text
STATIC_ALLOW
 -> runtimeRequirement = empty
 -> runtimeBindingPlan = empty

RUNTIME_GUARD_REQUIRED
 -> EXACT_RUNTIME_BINDING requirement present
 -> exactly one compiler-published RuntimeBindingPlan present
```

Classifier correctness 不得由 Test stub 自证。

## 4. Context-owned neutral contracts

`dec-core-context` 只冻结跨模块稳定 contract；不包含 starter concrete implementation。

```java
public interface ProtectedAccessResolutionContext {
    String engineContextId();
    AccessConsumerIrKey accessConsumerIrKey();
    RuntimeExecutionFrameId frameId();
    RuntimeResolutionOwnerId ownerResolutionId();
    Optional<RuntimeCollectionCursorId> collectionCursorId();
}

public final class ResolvedProtectedAccess {
    public String capabilityId();
    public String engineContextId();
    public ModelAccessRuleKey requestedRuleKey();
    public AccessOperation operation();
    public RuntimeExecutionFrameId executionFrameId();
    // no public/protected constructor, mint API or raw-target getter
}

public interface ModelAccessGuard {
    ModelAccessDecision authorize(ResolvedProtectedAccess access);
}

public interface RuntimeBindingVerifier {
    RuntimeBindingVerification verify(
        ResolvedProtectedAccess access,
        CompiledModelAccessRule selectedRule,
        RuntimeBindingPlan plan,
        String engineContextId);
}
```

`EngineContext` 继续只持 compiled immutable context。R10 **不要求 EngineContext 依赖 starter concrete types**；protected runtime 由 starter composition 针对某个 immutable EngineContext 创建并注入到上层 execution consumer。

## 5. Starter concrete package ownership

### 5.1 `dec.core.starter.access.DefaultProtectedAccessResolver`

职责：
- 接受 framework-owned `ProtectedAccessResolutionContext + ProtectedOperationIntent`；
- 通过 bootstrap-time adapter registry 解析 actual target；
- 在 context-local registry 内原子绑定 target identity + operation + owner/frame/cursor/provenance；
- 产生 one-shot generic `ResolvedProtectedAccess`；
- 不查询 PolicyIndex、不决定 STATIC/RUNTIME、不接受 RuntimeBindingPlan 作为 universal input。

### 5.2 `dec.core.starter.access.DefaultModelAccessGuard`

职责：
- 对 capability `requestedRuleKey` 在 current EngineContext PolicyIndex 做 **唯一一次 exact lookup**；
- STATIC_ALLOW：确认 plan/requirement empty，verifier=0/evaluator=0，返回 Guard-internal fast-path ALLOW；
- RUNTIME_GUARD_REQUIRED：require exact plan/requirement，调用 `DefaultRuntimeBindingVerifier`；
- policy missing、Context mismatch、invalid/stale capability、invalid compiled state 全部 fail closed。

### 5.3 `dec.core.starter.access.DefaultRuntimeBindingVerifier`

职责：只在 selected runtime-required rule 下验证 hidden membership/provenance 与 current Context/rule/plan/frame/cursor；STATIC_ALLOW 调用数必须为 0。

### 5.4 `dec.core.starter.access.DefaultProtectedAccessGateway`

职责：
1. atomically reserve capability；
2. 调用 Guard 恰好一次，自己不做第二次 PolicyIndex lookup；
3. ALLOW 后调用 context-local registry 中与 capability 同时绑定的 trusted operation adapter；
4. executor actual target 必须等于 capability hidden target；
5. terminal ALLOW execution 或 DENY 后 consume capability；
6. 禁止 `execute(capability,target)`、`execute(handle,rawObject)`、caller callback 选第二 target。

### 5.5 `ContextLocalProtectedAccessRegistry`

- starter-owned、per-runtime/per-EngineContext；无 global current/singleton。
- 保存 opaque capability -> hidden target/adapter/operation/frame/provenance/one-shot state。
- 不进入 CompiledModelSet，不改变 semantic digest。
- reserve/consume 与并发 replay 判定原子化；Context replacement/frame expiry/member change使 capability fail closed。

### 5.6 `ProtectedAccessRuntime` / `ProtectedAccessRuntimeFactory`

这是上层 consumer 唯一需要看到的 starter facade/composition surface。概念 contract：

```java
public final class ProtectedAccessRuntime {
    public ProtectedAccessResult execute(
        ProtectedAccessResolutionContext context,
        ProtectedOperationIntent intent);
}

public final class ProtectedAccessRuntimeFactory {
    public static ProtectedAccessRuntime create(
        EngineContext engineContext,
        ProtectedAccessAdapterRegistry trustedAdapters);
}
```

Factory 创建 resolver/registry/guard/verifier/gateway 的同一 immutable composition，不使用全局 current Context。

## 6. Trusted adapter SPI 与 actual operation ownership

P2 需要冻结 access-control execution port，但不能提前实现 P3～P7 业务执行器。因此 adapter SPI 归属 `dec-core-starter`：

```text
dec.core.starter.access.spi.ProtectedTargetResolutionPort
dec.core.starter.access.spi.ProtectedOperationExecutionPort
dec.core.starter.access.spi.ProtectedAccessAdapterRegistry
```

规则：
- adapters **只在 bootstrap/runtime-factory composition 时注册并冻结**；不是每次 execute 的 caller callback。
- resolver 根据 `accessConsumerIrKey/consumerKind` 选择可信 target-resolution port，并把选择结果与 capability 一起登记。
- gateway 只能调用 capability issuance 时登记的 execution port；caller 无法在 Guard ALLOW 后换 adapter/target。
- 未配置支持某 consumer kind 的 production adapter -> `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE` DENY；不得 fallback direct operation。
- `dec-core-starter` 不新增对 `dec-core-model` 的 P2 直接依赖；未来 P3～P7 module 若实现 Rule/change/action/query execution adapter，应位于 starter 之上并依赖 starter SPI，而不是让 starter 依赖业务 executor。

## 7. Production consumer integration

冻结唯一接入规则：

```text
Rule / change / custom action / protected query / future execution consumer
 -> obtain one context-bound ProtectedAccessRuntime from application composition
 -> construct framework-owned resolution context + operation intent through its execution adapter
 -> ProtectedAccessRuntime.execute(context,intent)
 -> starter resolver -> capability -> gateway -> guard -> same-bound adapter operation
```

禁止 consumer：
- 自己读取 `CompiledModelAccessRule.status()` 后直接操作；
- 自己查询 PolicyIndex；
- 自己 new/mint capability；
- 在 Guard 之后提交第二 raw target/callback；
- 建立第二套权限 registry/runtime authority。

当前仓库尚未实现完整 Rule/change/custom action/query execution engine 时，P2 只提供上述 runtime boundary + fail-closed adapter SPI；后续 phase 的真实 consumer 必须接入该边界，不能重新设计权限通道。

## 8. P2 / P3～P7 scope boundary

P2 **IN SCOPE**：
- exact access-policy facts/classification/plan；
- context-neutral contracts；
- starter concrete resolver/guard/verifier/gateway/registry/factory；
- one-shot target binding、TOCTOU/replay fail closed；
- trusted adapter SPI 与 no-bypass integration rule。

P2 **OUT OF SCOPE**：
- Information/Rule/change/action/query 的完整业务 evaluator/executor；
- datasource transaction orchestration；
- QueryPlan；
- source-authored per-object permission predicate DSL；
- 为 P3～P7 提前实现业务 side effects。

## 9. Unified STATIC/RUNTIME path

```text
all protected access
 -> starter ProtectedAccessRuntime
 -> DefaultProtectedAccessResolver binds actual target + operation
 -> generic ResolvedProtectedAccess
 -> DefaultProtectedAccessGateway
 -> DefaultModelAccessGuard exact lookup once
      STATIC_ALLOW
        -> no runtime plan
        -> verifier 0 / evaluator 0
        -> internal ALLOW
      RUNTIME_GUARD_REQUIRED
        -> exact plan+requirement
        -> DefaultRuntimeBindingVerifier
 -> gateway executes the same capability-bound adapter+target once
 -> consume capability
```

任何 caller-side STATIC direct path为 `MODEL_ACCESS_GUARD_BYPASS`。任何 proof/capability A + target B 为 `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`。

## 10. TOCTOU / concurrency

- capability reserve/consume atomic；concurrent execute 最多一个 terminal successful consumer。
- runtime path operation 前 revalidate Context/frame/cursor/rule/plan/membership；static path revalidate Context/frame/capability binding。
- stale/replay/unknown capability/adapter mismatch均 operation=0/effects=0。
- registry context-local；禁止 global mutable proof registry。

## 11. ModelPath / wildcard / selected-rule authority

Runtime ModelPath exact-only。READ `path="*"` compile-time finite canonical expansion；runtime wildcard key=0；wildcard WRITE/EXECUTE、empty expansion、parent/fuzzy fallback compile ERROR。
Guard 对 requested rule key 只做一次 exact lookup；Gateway/resolver/verifier/adapter不得重新选 policy。

## 12. EngineContext / Java 8 compatibility

- `EngineContext` 继续 `public final`；现有单参 constructor/core accessors保持。
- Starter runtime composition 使用该 immutable EngineContext；不引入 context -> starter dependency。
- 新 API 使用 Java 8 类型；禁止 record、`Map.of/copyOf` 等 Java 9+ API。

## 13. Stable reasons

Compile 至少：`MIX-SYSTEM-DUPLICATE`、`MIX-RULEVIEW-SYSTEM-REQUIRED`、`MIX-MODEL-PATH-INVALID`、wildcard unsupported/empty、`MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`、`MIX-MODEL-ACCESS-DENIED`。

Runtime 至少：`POLICY_NOT_FOUND`、`CONTEXT_IDENTITY_MISMATCH`、`MODEL_ACCESS_GUARD_BYPASS`、`PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`、`RUNTIME_BINDING_REQUIRED`、`RUNTIME_BINDING_PROOF_INVALID`、`RUNTIME_BINDING_STALE`、`RUNTIME_BINDING_PLAN_MISMATCH`、`RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`、`RUNTIME_BINDING_CAPABILITY_CONSUMED`、`GUARD_UNAVAILABLE`、`STATIC_ALLOW`、`RUNTIME_ALLOW`、`RUNTIME_DENY`。

## 14. Repository-valid test ownership

- `dec-core-context`: neutral contract/API shape/immutability tests。
- `dec-core-compiler`: classifier/rule/plan publication unit tests。
- `dec-core-starter`: resolver/gateway/guard/verifier/registry/concurrency/no-bypass behavior tests。
- `dec-demo`: existing real `systems.xml` source-to-operation integration tests because that resource and starter dependency already live there。

No Test Design case may use abstract `<target-module>` after R10；TESTDESIGN-P2-R11 freezes exact module/class/commands。

## 15. Review / lifecycle gate

`DESIGN-P2-R10` **not PASSED**。FND-004 remains `PARTIAL_FIX_PROPOSED / OPEN` until exact Architecture + ApiContract + Develop + Impact + CrossModule + Concurrency Review confirms this ownership against the repository and RC9 machine lifecycle/risk Evidence is valid。FND-001/FND-019 candidate semantics remain substantively fixed but formally OPEN。Implementation Plan / TDD / Development remain BLOCKED。