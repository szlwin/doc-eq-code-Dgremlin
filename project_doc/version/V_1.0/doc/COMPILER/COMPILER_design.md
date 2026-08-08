# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R06`。Base：`DESIGN-P2-R05`，并继承 P1 已通过的 Compiler Pipeline/Context 基线。
> 输入 Business Model 候选：`BM-R09`（标准 changeset `CHG-V_1.0-COMPILER-P2-BM-R09`；在 RC9 正式 reopen/publish 前仍为 MACHINE_BLOCKED）。
> 状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本文件是 P2 当前 canonical Design source；历史 R01～R05 继续保留在 Git/changes 历史中，但与本文件冲突时以 R06 为当前候选语义。

## 1. 设计目标与不可绕过约束

1. System 是显式的一等编译身份；RuleView 唯一身份为 `(SystemKey,name)`。
2. READ/WRITE/EXECUTE 独立授权，未声明即拒绝；共享 WRITE 默认拒绝。
3. 所有 protected READ/WRITE/EXECUTE 都进入同一 `ModelAccessGuard`。`STATIC_ALLOW` 只是 Guard 内 fast path，caller 不得绕过 Guard。
4. runtime exact lookup 永不支持 wildcard、prefix/suffix、parent/child、bare-name 或跨 target/System fallback。
5. Java 生产 API 以根 `pom.xml` 的 `maven.compiler.release=8` 为约束；不得使用 record、`Map.of/copyOf` 等 Java 9+ API。
6. 现有 `public final class EngineContext`、单参构造器、`compiledModelSet()`、`modelSet()`、`projection()` 保持兼容；P2 只增加兼容能力。
7. P2 不新增 source-authored 权限 Predicate DSL。AC-006 的 runtime-check-required 来源于**最终对象绑定依赖运行时值**，不是新增业务权限表达式。
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

禁止：

- context -> compiler 反向依赖；
- 把 compiler builder 塞进 `dec.core.context.*` split package；
- compiler -> concrete XML parser；
- starter/global singleton 持有全局 current Context；
- runtime caller 构造/提交一个 requirement 来替换已发布 rule 自带 requirement。

### 2.1 跨模块构造边界

`RuntimeAccessRequirement` 属于 `dec-core-context` 的 immutable fact，但由 `dec-core-compiler` 产生。构造 seam 必须可被 compiler 合法调用，因此使用 **context-owned public validated factory**，而不是 package-private factory：

```java
public final class RuntimeAccessRequirement {
    public enum Kind { EXACT_RUNTIME_BINDING }

    private RuntimeAccessRequirement(...);

    public static RuntimeAccessRequirement derived(
        ModelAccessRuleKey authorizedRuleKey,
        Kind kind,
        SourceRef sourceRef);

    public RuntimeRequirementKey key();
    public ModelAccessRuleKey authorizedRuleKey();
    public Kind kind();
    public SourceRef sourceRef();
    public String canonicalForm();
}
```

安全边界不是“谁能 new 一个值对象”，而是**只有同一 `CompiledModelSet` 中由 compiler 发布并由 exact PolicyIndex 选中的 `CompiledModelAccessRule` 才具有授权权威**。runtime request 中不接受 caller-supplied requirement，因此外部代码即使能调用 validated factory 也不能扩大权限。

`RuntimeRequirementKey` 由 `RuntimeAccessRequirement.derived(...)` 内部确定性生成；不提供 caller-chosen public key factory。

## 3. System / RuleView / ModelPath

### 3.1 System

- 显式 `SystemKey` 注册；多 source 输入按 canonical source order 处理；重复 key -> `MIX-SYSTEM-DUPLICATE`。
- `CompiledSystem` 与 registry、RuleView、access rules 同属于一个 `CompiledModelSet` 发布闭包。

### 3.2 RuleView

- 新 RuleView 缺 owner System -> `MIX-RULEVIEW-SYSTEM-REQUIRED`；
- 同 System 同名 -> `MIX-RULEVIEW-DUPLICATE`；
- 跨 System 同名合法；
- runtime 只允许 `RuleViewKey` 或 `(SystemKey,name)` 精确 lookup；禁止新 bare-name API。

### 3.3 ModelPath 与真实 `read path="*"`

运行时 `ModelPath` 始终 exact。真实 `systems.xml` 中 `order`、`payment` 的 `<read path="*"/>` 只是一种 **source/compile-time selector**：

1. 先解析唯一 target；
2. 从该 target 的 immutable `CompiledTargetPathCatalog` 枚举有限 canonical readable paths；
3. canonical sort + deduplicate；
4. 每个 path 形成普通 exact READ `ModelAccessRuleKey`；
5. wildcard 永不进入 runtime PolicyIndex；
6. wildcard WRITE/EXECUTE -> compile ERROR；
7. empty expansion -> compile ERROR；
8. expanded exact key set + target model-shape digest 进入 semantic digest；model shape 改变必须重新 compile。

## 4. ModelAccessRule 与 AC-006 runtime-check-required

### 4.1 编译结果

```java
public enum AccessCompilationStatus {
    STATIC_ALLOW,
    RUNTIME_GUARD_REQUIRED
}

public final class CompiledModelAccessRule {
    private final ModelAccessRuleKey key;
    private final AccessCompilationStatus status;
    private final RuntimeAccessRequirement runtimeRequirement;
    private final SourceRef sourceRef;

    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public SourceRef sourceRef();
}
```

规则：

- 静态 System/target/path/operation 不合法或未授权 -> compile ERROR，不发布 rule。
- 权限和实际对象绑定都能静态证明 -> `STATIC_ALLOW`。
- **静态授权结构合法，但最终对象实例/容器元素的绑定依赖运行时值** -> compiler 确定性生成 `RuntimeAccessRequirement(EXACT_RUNTIME_BINDING)` 并发布 `RUNTIME_GUARD_REQUIRED`。
- `RUNTIME_GUARD_REQUIRED` 不需要新的 XML/YAML predicate declaration；它从现有 access/path IR 的动态绑定分类派生。
- requirement 只能验证实际 runtime binding 没有逃出已授权 System/target/exact path/operation，不能增加额外权限。

这使 Requirement AC-006 可达：合法动态访问可以编译成功并发布，而不是因为没有新 predicate grammar 被误判为 compile ERROR。

### 4.2 Runtime binding facts

execution consumer 在真正访问对象前构造只读 binding proof：

```java
public final class RuntimeAccessBinding {
    private final String engineContextId;
    private final DefinitionKey targetKey;
    private final CanonicalModelPath resolvedPath;
    private final AccessOperation operation;

    public static RuntimeAccessBinding resolved(
        String engineContextId,
        DefinitionKey targetKey,
        CanonicalModelPath resolvedPath,
        AccessOperation operation);

    public String engineContextId();
    public DefinitionKey targetKey();
    public CanonicalModelPath resolvedPath();
    public AccessOperation operation();
}
```

该对象不携带业务模型实例、不暴露任意 POJO，也不拥有 policy identity。Guard 只把它与已选中的 rule 做约束验证。

## 5. Guard 流程：selected rule 唯一权威

所有 protected request：

```text
build ModelAccessRequest
  -> validate Context identity / owner-qualified key / operation
  -> exact PolicyIndex lookup ONCE
  -> selected CompiledModelAccessRule
  -> STATIC_ALLOW: Guard 内直接 ALLOW
  -> RUNTIME_GUARD_REQUIRED:
       validate selectedRule.runtimeRequirement
       validate RuntimeAccessBinding against selected rule
       optional bounded evaluator seam only for future requirement-authorized extensions
  -> ALLOW ? execute : execute nothing
```

强约束：

- evaluator/validator 接收 exact `selectedRule`，不得再次 PolicyIndex lookup；
- request 不携带可替换 policy/requirement；
- key mismatch、Context mismatch、missing binding、binding target/path/operation mismatch、Guard unavailable、timeout、exception、null/unknown 都 fail closed；
- `STATIC_ALLOW` 仍经过 Guard，evaluator 调用 0 次；
- Guard 从不执行业务 mutation/read side effect，只返回 decision。

当前 P2 没有 source-authored predicate DSL，因此 AC-006 的动态分支由 Guard 自身的 `EXACT_RUNTIME_BINDING` validator 完成。若未来 Requirement 正式引入业务 predicate，再以新 Requirement/Design revision 扩展 evaluator，不得在 Development 偷加。

## 6. RuntimeFactValue

R05 的 closed Java-8 value 方案继续有效：

- `public final class RuntimeFactValue`；
- private constructor；
- STRING/BOOLEAN/DECIMAL/INSTANT/LIST/OBJECT 六个 typed factories；
- LIST/OBJECT 递归防御性复制并不可变；
- 无 `Object value()` generic payload getter；
- typed visitor；
- deterministic canonical form；
- 外部无法 subclass。

RuntimeFacts 可用于未来经过 Requirement 授权的 evaluator 扩展，但不是当前 AC-006 reachability 的前置条件。

## 7. Timeout / cancellation / unavailable

继承 R04：

- Guard owns bounded evaluation executor and timeout budget；
- Java 8 `Duration timeoutBudget` + injected monotonic `GuardTimeSource.nanoTime()`；
- timed Future/get、timeout cancel(true)、interrupt restore、rejection/exception/null/unknown fail closed；
- no `Thread.sleep` oracle；
- unavailable Guard 使用非 null fail-closed sentinel，返回 `GUARD_UNAVAILABLE`；
- evaluator unavailable 与 Guard unavailable reason 分离。

`EXACT_RUNTIME_BINDING` 本身为同步、纯验证，不需要异步 evaluator；bounded executor 不得被无条件触发。

## 8. EngineContext 兼容

保留现有：

```java
public final class EngineContext {
    public EngineContext(CompiledModelSet compiledModelSet);
    public CompiledModelSet compiledModelSet();
    public ModelSet modelSet();
    public CoreConfigProjection projection();
}
```

P2 只增加兼容 overload/read surfaces，包括 contextId、owner-qualified System/RuleView lookup、policy status 和 non-null Guard。现有 equals/hashCode/toString 语义不得因新增字段被静默改变；禁止 `findRuleView(String bareName)` 新入口。

## 9. Diagnostic / denial reasons

Compile 至少稳定区分：

- `MIX-SYSTEM-DUPLICATE`
- `MIX-RULEVIEW-SYSTEM-REQUIRED`
- `MIX-RULEVIEW-DUPLICATE`
- `MIX-RULEVIEW-UNKNOWN`
- `MIX-MODEL-PATH-INVALID`
- wildcard unsupported/empty expansion
- `MIX-MODEL-ACCESS-DENIED`

Runtime 至少稳定区分：

- `POLICY_NOT_FOUND`
- `CONTEXT_IDENTITY_MISMATCH`
- `RUNTIME_BINDING_REQUIRED`
- `RUNTIME_BINDING_MISMATCH`
- `GUARD_UNAVAILABLE`
- `RUNTIME_EVALUATOR_UNAVAILABLE`
- `RUNTIME_EVALUATOR_EXCEPTION`
- `RUNTIME_EVALUATOR_NULL`
- `RUNTIME_EVALUATOR_TIMEOUT`
- `RUNTIME_EVALUATOR_UNKNOWN`
- `STATIC_ALLOW`
- `RUNTIME_ALLOW`
- `RUNTIME_DENY`

## 10. Source -> Compiler -> Runtime AC-006 chain

Canonical required flow：

```text
existing source syntax + declared model-access
  -> Canonical/Raw access IR
  -> exact static authorization
  -> DynamicBindingClassification
       STATIC_BOUND              -> STATIC_ALLOW
       RUNTIME_OBJECT_BOUND      -> derived RuntimeAccessRequirement(EXACT_RUNTIME_BINDING)
                                    + RUNTIME_GUARD_REQUIRED
  -> immutable CompiledModelSet publication
  -> runtime resolves actual object/path
  -> RuntimeAccessBinding
  -> Guard exact selected rule + requirement validation
  -> ALLOW or DENY before side effects
```

Production compiler must have at least one test fixture that reaches `RUNTIME_OBJECT_BOUND`; a design where no production source can ever emit `RUNTIME_GUARD_REQUIRED` is non-conforming even if Guard unit tests pass。

## 11. Concurrency / immutability

- CompiledModelSet/Rule/Requirement/RuntimeFactValue/Binding are immutable；
- PolicyIndex immutable and context-local；
- no global mutable cache/current context；
- concurrent authorization cannot mutate shared policy；
- bounded executor queue/thread ownership and replacement/degraded behavior remain R04 contracts；
- timed-out evaluator has no authority to execute protected operation or mutate protected state。

## 12. Declaration compatibility boundary

`DEC-EXPAND-DECLARATION` remains retired historical fact。P2 surviving boundary is read-only legacy compatibility (`ConfigInfo.getRuleViewInfo(String)` / `DataUtil.getRuleViewInfo(String)` or equivalent existing surface) until P7。P2 must not restore retired module, dual-write registries, or create a second runtime authority。

## 13. Review / lifecycle gate

`DESIGN-P2-R06` is **not PASSED**。Before Design can pass it requires the current RC9 lifecycle to bind the exact revision and independent reviews appropriate to current detected risks, including at least：

- ApiContractReviewAgent
- ConcurrencyReviewAgent
- ArchitectureReviewAgent
- BusinessModelReviewAgent
- DevelopAgent
- RequirementReviewAgent
- TestDesignAgent
- ImpactAnalysisReviewAgent
- CrossModuleIntegrationReviewAgent
- DataMigrationReviewAgent or a contract-valid waiver

The installed common-develop baseline currently reports `INVALID_BASELINE` because `common-develop-v2.44-rc9` is missing。This document does not repair that Skill baseline, fabricate repository Evidence, or claim machine closure。
