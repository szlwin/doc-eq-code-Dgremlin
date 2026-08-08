# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R07`。Base：`DESIGN-P2-R06`，并继承 P1 已通过的 Compiler Pipeline/Context 基线。
> 输入 Business Model 候选：`BM-R10`（标准 changeset `CHG-V_1.0-COMPILER-P2-BM-R10`；在 RC9 正式 reopen/publish 前仍为 MACHINE_BLOCKED）。
> 状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本文件是 P2 当前 canonical Design source；历史 R01～R06 继续保留在 Git/changes 历史中，但与本文件冲突时以 R07 为当前候选语义。

## 1. 设计目标与不可绕过约束

1. System 是显式的一等编译身份；RuleView 唯一身份为 `(SystemKey,name)`。
2. READ/WRITE/EXECUTE 独立授权，未声明即拒绝；共享 WRITE 默认拒绝。
3. 所有 protected READ/WRITE/EXECUTE 都进入同一 `ModelAccessGuard`。`STATIC_ALLOW` 只是 Guard 内 fast path，caller 不得绕过 Guard。
4. runtime exact lookup 永不支持 wildcard、prefix/suffix、parent/child、bare-name 或跨 target/System fallback。
5. Java 生产 API 以根 `pom.xml` 的 `maven.compiler.release=8` 为约束；不得使用 record、`Map.of/copyOf` 等 Java 9+ API。
6. 现有 `public final class EngineContext`、单参构造器、`compiledModelSet()`、`modelSet()`、`projection()` 保持兼容；P2 只增加兼容能力。
7. P2 不新增 source-authored 权限 Predicate DSL。AC-006 的 runtime-check-required 来源于最终对象绑定依赖运行时值，不是新增业务权限表达式。
8. Guard DENY 必须在实际 read/write/execute、状态推进或外部副作用前完成。

## 2. 模块和 package 边界

```text
dec-core-context
  dec.core.context.model.*
  dec.core.context.model.access.*
      neutral immutable P2 facts
      Guard/request/decision contracts
      validated public construction factories for immutable compiled facts

        ^ depends-on
        |
dec-core-compiler
  dec.core.compiler.system.*
  dec.core.compiler.ruleview.*
  dec.core.compiler.modelpath.*
  dec.core.compiler.access.*
      compiler passes/builders
      runtime-binding classification
      canonical fact assembly

        ^
        |
frontends / starter / execution consumers
```

禁止 context -> compiler 反向依赖、split package、compiler -> concrete parser、global current Context，以及 runtime caller 构造/提交 replacement requirement/rule/plan。

### 2.1 跨模块构造边界

`RuntimeAccessRequirement` 属于 `dec-core-context` immutable fact，由 `dec-core-compiler` 产生，使用 context-owned public validated factory：

```java
public final class RuntimeAccessRequirement {
    public enum Kind { EXACT_RUNTIME_BINDING }

    private RuntimeAccessRequirement(...);

    public static RuntimeAccessRequirement derived(
        ModelAccessRuleKey authorizedRuleKey,
        RuntimeBindingPlanKey planKey,
        Kind kind,
        SourceRef sourceRef);

    public RuntimeRequirementKey key();
    public ModelAccessRuleKey authorizedRuleKey();
    public RuntimeBindingPlanKey planKey();
    public Kind kind();
    public SourceRef sourceRef();
    public String canonicalForm();
}
```

授权权威来自 current `CompiledModelSet` 中 exact PolicyIndex 选中的 rule，不来自 factory visibility。`RuntimeRequirementKey` 由 authorized rule + plan + kind + canonical Source identity 确定性生成；caller 不可指定 key。

## 3. System / RuleView / ModelPath

### 3.1 System / RuleView

- 显式 `SystemKey`；重复 -> `MIX-SYSTEM-DUPLICATE`。
- RuleView 新定义缺 owner System -> `MIX-RULEVIEW-SYSTEM-REQUIRED`；同 System 同名 -> duplicate；跨 System 同名合法。
- runtime 只允许 owner-qualified exact lookup，禁止 bare-name fallback。

### 3.2 ModelPath 与真实 `read path="*"`

Runtime `ModelPath` 始终 exact。真实 `systems.xml` 的 READ `*` 只在 compile-time：唯一 target -> immutable target path catalog -> finite canonical sort/dedup -> exact READ rule。wildcard 永不进入 runtime PolicyIndex；wildcard WRITE/EXECUTE 和 empty expansion compile ERROR；expanded exact key set + model-shape digest 进入 semantic digest。

## 4. Production classifier 与 AC-006 runtime-check-required

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }
```

`DynamicBindingClassification` 是 production compiler fact，不是 Test stub 输入。分类发生在 exact target/path/operation 静态授权完成之后，只消费 resolved access-consumer IR。

### 4.1 R07 确定性分类规则

当前 P2 只冻结两类：

1. `DIRECT_EXACT -> STATIC_BOUND`
   - access IR 直接引用 canonical exact path；
   - 不含 collection iteration、runtime index/key、filter/find/selector 或其它 element-selection operator；
   - 真实 fixture：`systems.xml` 中 `order.ordered` 的 `rule-data` 子表达式 `status = 1`，其 `status` READ 必须 `STATIC_BOUND`。
2. `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND`
   - IR 为当前语法已有 `every(<collectionPath>, <elementExpression>)`；
   - collectionPath 必须 exact resolve 为 collection；element protected member 必须 exact resolve 到 element type 的 relative path；
   - 真实 fixture：`every(orderDetailList, status = 1)` 中 element `status` READ 必须 `RUNTIME_OBJECT_BOUND`；
   - source READ `*` 必须先展开出该 element member 的 exact READ rule；不存在时 compile ERROR，禁止 parent-path fallback。
3. 其它 dynamic IR -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

Classifier 输入至少含 `consumerKind + exactTargetKey + canonicalBasePath + canonicalRelativeElementPath(if any) + resolvedModelShape + SourceRef`；输出含 classification、稳定 reason 和（仅 runtime 时）deterministic `RuntimeBindingPlan`。同义 IR 必须得到相同 classification/plan key/digest。

### 4.2 Compiler-derived RuntimeBindingPlan

```java
public final class RuntimeBindingPlan {
    public enum Kind { COLLECTION_ELEMENT_MEMBERSHIP }

    public static RuntimeBindingPlan collectionElementMembership(
        ModelAccessRuleKey authorizedRuleKey,
        CanonicalModelPath collectionPath,
        CanonicalModelPath elementRelativePath,
        SourceRef sourceRef,
        Digest modelShapeDigest);

    public RuntimeBindingPlanKey key();
    public ModelAccessRuleKey authorizedRuleKey();
    public Kind kind();
    public CanonicalModelPath collectionPath();
    public CanonicalModelPath elementRelativePath();
    public SourceRef sourceRef();
}
```

`RuntimeAccessRequirement(EXACT_RUNTIME_BINDING)` 必须引用该 plan key。Plan 是 compiler-published immutable fact，描述“实际 element 必须由当前 Context 下该 exact collection path 的 framework resolver 解析”，不是 caller predicate。

### 4.3 Opaque runtime-object binding proof

R06 四字段 `RuntimeAccessBinding(context,target,path,operation)` 废止，因为无法区分同一 static tuple 下 element A/B。R07 使用 framework-owned opaque handle：

```java
public final class RuntimeBindingHandle {
    // no public/protected constructor; no public mint/factory API
    public String engineContextId();
    public RuntimeBindingPlanKey planKey();
    public ModelAccessRuleKey selectedRuleKey();
    public String resolutionId();
}

public interface RuntimeBindingResolver {
    RuntimeBindingHandle resolve(
        RuntimeBindingPlan plan,
        RuntimeResolutionContext executionContext);

    RuntimeBindingVerification verify(
        RuntimeBindingHandle handle,
        RuntimeBindingPlan plan,
        ModelAccessRuleKey selectedRuleKey,
        String engineContextId);
}
```

Contract：

- handle 只能由 framework resolver 在实际模型解析发生时签发；业务 caller/Rule/change/custom action 无 mint API；
- resolver/verifier 内部可持有实际 object identity、collection-owner identity、provenance，但不向 Guard/业务代码暴露 raw POJO；
- verify 必须证明 handle 绑定 current EngineContext + exact selected rule + exact plan，且实际对象是 plan 指定 collection 的真实成员；
- handle A 来自当前 `OrderInfo.orderDetailList` member -> 可匹配；handle B 来自另一 OrderInfo/collection/context/plan/rule，即使 System/target/path/operation 相同也必须失败；
- stale、replay、unknown/forged resolution id 全部 fail closed；Guard 不接受 caller 自报 boolean 或 raw object。

因此 AC-006 ALLOW/DENY 来自真实 runtime membership/provenance，而不是重复比较静态四元组，也不是新增 per-element business ACL。若未来需要按 element 业务属性授权，必须先新 Requirement。

### 4.4 Compiled rule

```java
public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
    public SourceRef sourceRef();
}
```

静态非法/未授权 -> compile ERROR；`STATIC_BOUND -> STATIC_ALLOW`；`RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + RuntimeBindingPlan`。Runtime rule 缺 plan/requirement 是非法 compiled state。

## 5. Guard：selected rule + plan + opaque handle

```text
ModelAccessRequest
 -> validate Context/key/operation
 -> exact PolicyIndex lookup ONCE
 -> selected CompiledModelAccessRule
 -> STATIC_ALLOW: Guard internal ALLOW
 -> RUNTIME_GUARD_REQUIRED:
      require requirement + plan + RuntimeBindingHandle
      RuntimeBindingResolver.verify(handle, exact plan, exact rule, current Context)
 -> MATCH ? ALLOW : DENY
 -> only ALLOW may execute protected operation
```

Request 不携带 replacement rule/requirement/plan；verifier 不重新选 policy；proof mismatch/unknown/stale/context mismatch 均在 protected operation 前 DENY。当前 AC-006 不依赖 optional RuntimeFactEvaluator。

## 6. RuntimeFactValue

继续 R05 closed Java-8 value：public final、private constructor、六种 typed factory、LIST/OBJECT deep immutable、无 generic Object getter、typed visitor、deterministic canonical form、不可 subclass。

## 7. Timeout / cancellation / unavailable

继承 R04 bounded evaluator executor/fake monotonic time/timed Future/cancel(true)/interrupt restore/rejection/exception/null/unknown fail-closed。`EXACT_RUNTIME_BINDING` 为同步纯验证，不应无条件触发 evaluator executor。

## 8. EngineContext 兼容

保留现有 final class、单参 constructor、`compiledModelSet()/modelSet()/projection()`。P2 仅 additive owner-qualified lookup、contextId、policy status、non-null Guard/runtime binding resolver read surface；禁止新的 `findRuleView(String bareName)`。

## 9. Diagnostic / denial reasons

Compile 至少：`MIX-SYSTEM-DUPLICATE`、`MIX-RULEVIEW-SYSTEM-REQUIRED`、`MIX-MODEL-PATH-INVALID`、wildcard unsupported/empty、`MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`、`MIX-MODEL-ACCESS-DENIED`。

Runtime 至少：`POLICY_NOT_FOUND`、`CONTEXT_IDENTITY_MISMATCH`、`RUNTIME_BINDING_REQUIRED`、`RUNTIME_BINDING_PROOF_INVALID`、`RUNTIME_BINDING_STALE`、`RUNTIME_BINDING_PLAN_MISMATCH`、`GUARD_UNAVAILABLE`、future evaluator reasons、`STATIC_ALLOW`、`RUNTIME_ALLOW`、`RUNTIME_DENY`。

## 10. Source -> Compiler -> Runtime AC-006 chain

```text
real existing source + declared model-access
 -> resolved access-consumer IR
 -> exact static authorization
 -> production DynamicBindingClassifier
      DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW
      EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND
                                -> RuntimeBindingPlan(COLLECTION_ELEMENT_MEMBERSHIP)
                                -> RuntimeAccessRequirement(EXACT_RUNTIME_BINDING)
                                -> RUNTIME_GUARD_REQUIRED
      unsupported dynamic IR -> compile ERROR
 -> immutable CompiledModelSet publication
 -> framework resolver resolves actual collection element and issues opaque handle
 -> Guard verifies exact selected rule + plan + current Context + actual membership
 -> matching member handle ALLOW; foreign/stale/forged/replayed handle DENY before side effects
```

Production compiler 必须由真实 fixture 达到 `RUNTIME_OBJECT_BOUND`；仅 Guard unit test 或 classifier stub 不能满足 AC-006。

## 11. Concurrency / immutability

CompiledModelSet/Rule/Requirement/RuntimeBindingPlan/RuntimeFactValue/RuntimeBindingHandle immutable；PolicyIndex context-local；无 global mutable current/cache；concurrent authorization 不修改 policy；future evaluator timeout task 无 protected operation authority。

## 12. Declaration compatibility boundary

`DEC-EXPAND-DECLARATION` 仅历史 retired fact；P2 只保留 read-only legacy compatibility 到 P7，不恢复 retired module、不 dual-write、不创建第二 runtime authority。

## 13. Review / lifecycle gate

`DESIGN-P2-R07` **not PASSED**。必须由正式 RC9 lifecycle 绑定 exact revision，再完成 current-risk 对应的 ApiContract/Concurrency/Architecture/BusinessModel/Develop/Requirement/TestDesign/Impact/CrossModule 等独立 Review 与 DataMigration review/valid waiver。当前不制造 repository Evidence，不推进 Implementation Plan/TDD/Development。