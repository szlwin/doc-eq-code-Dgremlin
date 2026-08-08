# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R08`。Base：`DESIGN-P2-R07`，并继承 P1 已通过的 Compiler Pipeline/Context 基线。
> 输入 Business Model 候选：`BM-R11`（标准 changeset `CHG-V_1.0-COMPILER-P2-BM-R11`；在 RC9 正式 reopen/publish 前仍为 MACHINE_BLOCKED）。
> 状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本文件是 P2 当前 canonical Design source；历史 R01～R07 保留为 Git/changes 历史，与本文件冲突时以 R08 为当前候选语义。

## 1. 设计目标与不可绕过约束

1. System 是显式一等身份；RuleView 唯一身份为 `(SystemKey,name)`。
2. READ/WRITE/EXECUTE 独立授权，未声明即拒绝；共享 WRITE 默认拒绝。
3. 所有 protected READ/WRITE/EXECUTE 都进入同一 Guard；`STATIC_ALLOW` 仅为 Guard 内 fast path。
4. runtime exact lookup 禁止 wildcard、prefix/suffix、parent/child、bare-name 和跨 target/System fallback。
5. Java 生产 API 以 release 8 为约束；EngineContext 现有 final/additive compatibility 保持。
6. P2 不新增 source-authored 权限 Predicate DSL；AC-006 只处理已静态授权 surface 下的 runtime object binding。
7. **Guard 验证的实际 runtime object 与最终 protected operation 的实际 target 必须是同一个不可替换 framework binding。** Detached ALLOW、proof A + target B、authorize 后重新选择 target 均禁止。
8. DENY 必须发生在 read/write/execute、状态推进和外部副作用之前。

## 2. 模块与 ownership

```text
dec-core-context
  dec.core.context.model.*
  dec.core.context.model.access.*
      neutral immutable rule / plan / requirement contracts
      Guard decision contracts
      opaque capability contracts

        ^ depends-on
        |
dec-core-compiler
  dec.core.compiler.*
      resolved access-consumer IR
      production DynamicBindingClassifier
      RuntimeBindingPlan publication

        ^
        |
framework execution consumers
      RuntimeResolutionContext creation
      actual object resolution
      ResolvedProtectedAccess issuance
      ProtectedAccessGateway verify+execute
```

禁止 context -> compiler、split package、compiler -> concrete parser、global mutable current，以及 business caller 构造 replacement rule/plan/requirement/capability。

## 3. Production classifier（FND-018 保持冻结）

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }
```

分类在 exact static authorization 后消费 production `ResolvedAccessConsumerIr`：

- `DIRECT_EXACT -> STATIC_BOUND`；真实 fixture：`systems.xml / order.ordered / status = 1`。
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND`；真实 fixture：`every(orderDetailList, status = 1)` 中 element `status` READ。
- 其它 runtime index/key/filter/find/selector 或无法确定 element shape -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

Classifier stub 仅用于下游 unit isolation，不能证明 classifier correctness/AC-006。

## 4. RuntimeBindingPlan / RuntimeAccessRequirement

`RUNTIME_OBJECT_BOUND` 产生 compiler-published immutable：

```java
public final class RuntimeBindingPlan {
    public enum Kind { COLLECTION_ELEMENT_MEMBERSHIP }
    public RuntimeBindingPlanKey key();
    public ModelAccessRuleKey authorizedRuleKey();
    public CanonicalModelPath collectionPath();
    public CanonicalModelPath elementRelativePath();
    public SourceRef sourceRef();
}

public final class RuntimeAccessRequirement {
    public enum Kind { EXACT_RUNTIME_BINDING }
    public RuntimeRequirementKey key();
    public ModelAccessRuleKey authorizedRuleKey();
    public RuntimeBindingPlanKey planKey();
    public Kind kind();
}
```

Rule/plan/requirement 同属一个 immutable `CompiledModelSet` closure；plan key/model-shape digest/requirement identity 进入 semantic digest。Authority 只来自 current Context exact PolicyIndex 选中的 rule。

## 5. RuntimeResolutionContext 完整契约（FND-004 收敛）

`RuntimeResolutionContext` 不是 caller 任意 DTO，而是 framework-owned、短生命周期 execution frame：

```java
public interface RuntimeResolutionContext {
    String engineContextId();
    AccessConsumerIrKey accessConsumerIrKey();
    RuntimeExecutionFrameId frameId();
    RuntimeResolutionOwnerId ownerResolutionId();
    Optional<RuntimeCollectionCursorId> collectionCursorId();
}
```

Normative：

- 只能由当前 framework execution pipeline 创建；business code 无 public constructor/factory。
- 绑定 current EngineContext、当前 resolved access-consumer IR、当前 root/owner resolution 和（若为 every）当前 collection cursor/element selection frame。
- 生命周期只覆盖当前 protected access evaluation；不得跨 Context、rule evaluation、collection cursor 或 request 缓存复用。
- 不暴露 raw domain object getter；实际对象 identity/provenance 仅由 resolver/gateway 内部 registry 持有。
- execution frame 失效、owner/cursor 改变或 Context replacement 会使后续 capability fail closed。

## 6. R08：proof 与 actual operation 原子绑定

R07 的 `RuntimeBindingHandle` 可以证明 element A 的 membership，但 detached handle + `Guard.authorize(...)` 后仍可能由 caller 改为操作 B。R08 因此把 handle 降为 resolver 内部 proof material；**caller-facing execution authority 是 one-shot `ResolvedProtectedAccess` capability**。

```java
public final class ResolvedProtectedAccess {
    // no public/protected constructor; no public mint/factory
    public String capabilityId();
    public String engineContextId();
    public ModelAccessRuleKey selectedRuleKey();
    public RuntimeBindingPlanKey planKey();
    public AccessOperation operation();
}
```

Capability 内部（不通过 public API 暴露）必须同时绑定：

- exact actual object identity；
- collection owner/membership provenance；
- current resolution/frame/cursor identity；
- exact selected rule + plan + Context；
- exact protected operation intent；
- one-shot lifecycle state。

### 6.1 Resolver

```java
public interface RuntimeBindingResolver {
    ResolvedProtectedAccess resolve(
        RuntimeBindingPlan plan,
        RuntimeResolutionContext executionContext,
        ProtectedOperationIntent operationIntent);
}
```

`ProtectedOperationIntent` 也是 framework-owned immutable intent：它描述当前 access-consumer IR 已确定的 READ/WRITE/EXECUTE intent 和必要 payload/action identity，**不含一个可由 caller 替换的 target object 参数**。当前 AC-006 真实 fixture 使用 READ；未来 WRITE/EXECUTE 若为 runtime-bound，必须复用同一 capability invariant。

Resolver 在解析实际 object A 的同一 framework step 中创建 capability A，并将 hidden target identity + proof + operation intent 原子登记；不返回 `handle A` 让业务代码自行重新组合 target。

### 6.2 ProtectedAccessGateway

```java
public interface ProtectedAccessGateway {
    ProtectedAccessResult execute(ResolvedProtectedAccess access);
}
```

Gateway 是 runtime-bound protected operation 的唯一支持执行入口：

1. 读取 capability 当前 Context/rule/plan/operation；
2. exact PolicyIndex lookup **一次**获得 selected rule；
3. 调用 `ModelAccessGuard` 验证 capability 内部 proof/membership/provenance；
4. 在同一次 gateway execution 中再次确认 capability 未 consumed、frame/cursor/membership 未失效；
5. 由 framework executor 对 **capability 内部登记的 actual target** 执行 **capability 内部登记的 operation**；
6. API 不接受第二个 object/target 参数，不接受 caller callback/closure 选择另一个 protected object；
7. ALLOW 成功执行或 terminal DENY 后 capability 均 consumed；replay fail closed。

因此正常 API 上不存在：

```text
valid proof/capability A + arbitrary protected object B
```

若底层 invariant-test seam 人为注入 capability target identity 与 executor actual target identity 不一致，必须 `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`，B operation count = 0。

### 6.3 TOCTOU / membership change

若从 resolve 到 gateway execute 之间 owner collection membership、frame/cursor、Context 或 plan/rule identity 发生变化，capability 失效并在操作前 DENY。实现可以使用 context-local resolution registry/version/critical section，但 Development 不得放宽成“验证旧 proof 后照常执行当前任意 object”。

## 7. Guard contract

`ModelAccessGuard` 仍是 authorization decision authority，但 **detached `ALLOW` 不是 runtime-bound object execution authority**。

```text
ResolvedProtectedAccess
 -> ProtectedAccessGateway
 -> exact selected rule lookup ONCE
 -> Guard verifies same capability proof
 -> ALLOW
 -> gateway executes same internally-bound target+operation
```

Request/capability 不携带 replacement rule/requirement/plan/target。Verifier 不重新选 policy。Business caller 不能在 Guard ALLOW 后调用一个接受 arbitrary target 的 protected accessor。

`STATIC_ALLOW` 仍走 Guard；FND-019 的 capability/gateway强约束特别适用于 `RUNTIME_GUARD_REQUIRED`。

## 8. RuntimeBindingHandle / proof material

`RuntimeBindingHandle` 可作为 resolver 内部 registry/proof value 保留，用于 current Context + rule + plan + membership/provenance 验证，但不得作为“授权后可搭配任意 object”的 caller execution token。Business caller 无 mint API，也无受支持的 `execute(handle, object)` API。

Stable runtime reasons 至少包括：

- `RUNTIME_BINDING_REQUIRED`
- `RUNTIME_BINDING_PROOF_INVALID`
- `RUNTIME_BINDING_STALE`
- `RUNTIME_BINDING_PLAN_MISMATCH`
- `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`
- `RUNTIME_BINDING_CAPABILITY_CONSUMED`
- `CONTEXT_IDENTITY_MISMATCH`
- `POLICY_NOT_FOUND`
- `GUARD_UNAVAILABLE`

所有 DENY 均在 protected operation 前完成。

## 9. Wildcard / RuntimeFactValue / timeout / EngineContext

继续继承已冻结契约：

- real source READ `path="*"` 仅 compile-time finite exact expansion；runtime PolicyIndex zero wildcard。
- `RuntimeFactValue` public final/private constructor/six typed factory/deep immutable/canonical/typed visitor。
- R04 bounded evaluator timeout/cancel/rejection/exception/null/unknown fail-closed 仅供未来 Requirement-authorized predicate extension；当前 AC-006 不依赖 predicate evaluator。
- EngineContext 保持 existing final class + current constructors/core accessors；P2 只 additive owner-qualified policy/Guard/runtime gateway read surfaces。

## 10. Source -> Compiler -> operation-bound AC-006 chain

```text
real systems.xml + order.ordered rule-data
 -> exact static read authorization
 -> production classifier
      direct status -> STATIC_BOUND -> STATIC_ALLOW
      every(orderDetailList,status) -> RUNTIME_OBJECT_BOUND
 -> RuntimeBindingPlan + EXACT_RUNTIME_BINDING
 -> exact RUNTIME_GUARD_REQUIRED rule
 -> immutable Context publication
 -> framework RuntimeResolutionContext for current every element
 -> resolver resolves actual element A + operation intent
 -> one-shot ResolvedProtectedAccess A
 -> ProtectedAccessGateway
      exact rule lookup + Guard verify A
      execute the same internally-bound A target
 -> ALLOW: A operation exactly once
 -> substitution attempt proof/capability A + target B: impossible by supported API or invariant-test DENY
 -> stale/replayed/changed-membership capability: DENY before operation/effects
```

## 11. Concurrency / immutability

Compiled facts immutable；PolicyIndex/resolution registry context-local；capability one-shot state transition 必须原子；concurrent replay 只能一个 terminal consumer；membership/frame invalidation 在 gateway execute 前 revalidate；无 global mutable current/cache。

## 12. Declaration compatibility boundary

`DEC-EXPAND-DECLARATION` 仅历史 retired fact；P2 只保留 read-only legacy compatibility 到 P7，不恢复 retired module、不 dual-write、不创建第二 runtime authority。

## 13. Review / lifecycle gate

`DESIGN-P2-R08` **not PASSED**。正式 RC9 lifecycle 仍需从 business_model reopen，materialize/publish BM-R11，再绑定 exact Design revision、生成 current-revision risk Evidence，并完成 ApiContract/Concurrency/Architecture/BusinessModel/Develop/Requirement/TestDesign/Impact/CrossModule 等触发 Review 与 DataMigration review/valid waiver。当前 Implementation Plan / TDD / Development 继续 BLOCKED。
