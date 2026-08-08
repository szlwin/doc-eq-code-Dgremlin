# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R14`。Base：`DESIGN-P2-R13`。Inputs：Requirement `REQAN-P2-R01` + formal Decision `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` + canonical Business Model candidate `BM-R12`。
> 状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。本 Revision 是 consolidated Design：恢复 System / RuleView / composite-call / atomic-publication 主设计，并保留 R13 direct-argument bridge、PolicyIndex、runtime binding、one-shot capability 设计。不新增 FND-020。

<a id="p2-system"></a>
## 1. P2 System 一等编译设计

### 1.1 模块归属

- `dec-core-context`：拥有 neutral `SystemKey`、不可变 `CompiledSystem` read contract；
- `dec-core-compiler`：拥有 System discovery/registration/reference resolution/deterministic publication；
- XML/YAML frontend：只保留显式 System 与 SourceRef，不自行推断身份；
- `dec-core-starter`：不维护第二份 System registry。

### 1.2 核心值与 Registry

```java
public final class SystemKey implements Comparable<SystemKey> {
    public static SystemKey of(String value);
    public String value();
}

public final class CompiledSystem {
    public SystemKey key();
    public SourceRef sourceRef();
}
```

Compiler 构建期 registry exact-keyed by `SystemKey`。System identity 只来自配置显式 System name/key，不来自文件名、路径、加载顺序或 RuleView 名称。

### 1.3 pipeline

```text
source discovery
 -> canonical parse
 -> collect explicit System declarations
 -> register all SystemKey symbols
 -> reject exact duplicates
 -> resolve forward System refs / owned definitions
 -> deterministic sort
 -> semantic digest input
 -> candidate publication
```

规则：

- duplicate exact SystemKey -> `MIX-SYSTEM-DUPLICATE`；
- unknown System ref -> `MIX-SYSTEM-UNKNOWN`；
- source order changes cannot change final key set/digest；
- any ERROR prevents candidate publication and keeps old Context unchanged。

<a id="p2-ruleview"></a>
## 2. RuleView `(SystemKey,name)` 完整设计

### 2.1 RuleViewKey

```java
public final class RuleViewKey implements Comparable<RuleViewKey> {
    public static RuleViewKey of(SystemKey systemKey, String localName);
    public SystemKey systemKey();
    public String localName();
}
```

`equals/hashCode/compareTo` include both fields。Different Systems may own the same local name; same-System duplicate is invalid。

### 2.2 CompiledRuleView

```java
public final class CompiledRuleView {
    public RuleViewKey key();
    public SourceRef sourceRef();
    public List<RuleKey> resolvedRuleRefs();
}
```

New mix RuleView must explicitly supply System. Missing -> `MIX-RULEVIEW-SYSTEM-REQUIRED`。Unknown System / Rule -> stable ERROR。No implicit fallback from file location or caller context。

<a id="p2-ruleview-resolver"></a>
### 2.3 RuleViewResolver / composite call

```java
public interface RuleViewResolver {
    Optional<CompiledRuleView> find(RuleViewKey key);
    CompiledRuleView require(SystemKey systemKey, String localName);
}
```

Composite invocation source fact:

```text
system-ref + rule-ref
 -> SystemKey exact resolve
 -> RuleViewKey(SystemKey, rule-ref)
 -> RuleViewResolver exact lookup
```

Prohibited new-code shapes:

```text
find("ruleViewName")
require("ruleViewName")
global bare-name registration
fallback to first matching RuleView across Systems
```

A legacy read-only bare-name adapter may survive only at the explicitly documented compatibility boundary; it may not register new facts, become canonical resolver, or resolve ambiguous cross-System names。

<a id="p2-model-access"></a>
## 3. ModelAccessRule / ModelPath compile-time design

```java
public enum AccessOperation { READ, WRITE, EXECUTE }
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }
```

`ModelAccessRuleKey` remains exact over the compiled authorization identity defined by BM-R12: System + target + canonical ModelPath + operation。

`DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`。

`EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`。

READ `path="*"` is finite-expanded during compilation to exact child keys. Runtime `PolicyIndex` never stores wildcard/fuzzy keys。Unsupported selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`。

## 4. RuntimeAccessRequirement / RuntimeBindingPlan

STATIC_ALLOW invariant:

```text
runtimeRequirement = empty
runtimeBindingPlan = empty
```

RUNTIME_GUARD_REQUIRED invariant:

```text
EXACT_RUNTIME_BINDING requirement present
exact compiler-published RuntimeBindingPlan present
```

Runtime proof may prove actual element membership/provenance for the selected exact policy rule; it cannot create a new policy rule or change requested rule/op。

<a id="p2-policy-index"></a>
## 5. 唯一 immutable ModelAccessPolicyIndex

Context-owned API：

```java
public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

`of(...)` rejects duplicate exact keys, nulls, invalid STATIC/RUNTIME state and non-canonical runtime keys; output is immutable/deterministic。

Policy authority chain：

```text
compiled rules
 -> ModelAccessPolicyIndex.of(...)
 -> SemanticDigestInput(same immutable index)
 -> CompilerDigestService
 -> DigestBoundCompiledInput(same index + digest)
 -> CompiledModelSetBuilder.FrozenInput
 -> CompiledModelSet.published(...same index...)
 -> EngineContext.modelAccessPolicyIndex()
 -> DefaultModelAccessGuard.find(exact key) exactly once
```

Forbidden：Guard scan `definitions()`；rebuild from `TypedDefinitionRegistries`；starter second permission `Map`；resolver/gateway/verifier/adapter policy reselection。

<a id="p2-context"></a>
## 6. CompiledModelSet / EngineContext publication

Existing 8-arg `CompiledModelSet` constructor remains source-compatible and deterministically attaches `ModelAccessPolicyIndex.empty()`; it never reconstructs policy。

P2 production publication uses：

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

`EngineContext(CompiledModelSet)` remains unchanged and adds direct read-through `modelAccessPolicyIndex()`。

System, RuleView, PolicyIndex and digest are part of the same immutable publication closure。Any System/RuleView/model-access ERROR -> no new Context; caller-held old Context remains visible。Parallel Context instances never share mutable registries。

<a id="p2-direct-bridge"></a>
## 7. Direct-argument ProtectedExecutionBridge

Formal decision：`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`。

```java
public final class ProtectedExecutionBridge {
    public ProtectedAccessResult execute(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> collectionCursorId);
}
```

Bridge composition binds current `EngineContext/runtime`, `AccessConsumerIrKey`, target-resolution port and operation-execution port。Per-call caller supplies rule/op/frame/owner/cursor。

Current P2 accepted semantics：

- caller may choose any exact ruleKey/op that is actually present and valid in current compiler-published PolicyIndex；
- `AccessConsumerIrKey` is provenance/diagnostic only, not an authorization-key dimension；
- P2 does not implement consumer -> rule/op binding；
- requested key/op absent/mismatched in PolicyIndex -> DENY；
- future per-consumer hardening requires new Requirement/Decision Review。

No `ProtectedExecutionToken` / `recognizes()` / claim/replay contract exists。

<a id="p2-runtime-guard"></a>
## 8. Runtime execution path

```text
bridge.execute(ruleKey,op,frame,owner,cursor)
 -> validate required argument shape/current Context
 -> starter internal issueInvocation(exact invocation facts)
 -> internal issued context+intent pair
 -> resolver resolves actual target using bound target port
 -> one-shot ResolvedProtectedAccess capability binds target + op + invocation provenance
 -> Gateway atomically reserves capability
 -> Guard exact current-context PolicyIndex lookup = 1
 -> STATIC_ALLOW fast path OR RuntimeBindingVerifier exact proof
 -> same capability-bound operation port + target
 -> terminal consume
```

STATIC_ALLOW still enters Gateway/Guard。Runtime verifier calls = 0 for static; runtime-required uses exact selected rule/plan。

## 9. Direct invocation validation

Stable fail-closed cases before protected operation：

- null/invalid ruleKey/op/frame/owner/cursor shape；
- key missing from current PolicyIndex -> `POLICY_NOT_FOUND`；
- requested `operation` inconsistent with `requestedRuleKey` -> invalid invocation / no operation；
- Context mismatch/stale frame/cursor；
- adapter unavailable；
- Guard unavailable；
- runtime proof missing/invalid/stale/plan mismatch；
- capability/target substitution。

Caller selecting another **valid** compiler-published key/op is not treated as forged authority in current P2, per formal decision。

<a id="p2-concurrency"></a>
## 10. Concurrency / TOCTOU

Bridge is immutable/stateless and may receive concurrent independent calls。Two calls with identical scalar arguments are two independent invocations and may each create a distinct capability；P2 does not implement duplicate suppression/business idempotency。

Mandatory atomicity is per capability：

- same `ResolvedProtectedAccess` capability concurrent Gateway execution -> terminal success `<= 1`；
- capability A cannot be used for target B or operation B；
- runtime branch revalidates Context/frame/cursor/rule/plan/membership immediately before operation；
- static branch revalidates Context/frame/target binding immediately before operation；
- PolicyIndex is immutable, so no authority swap between lookup and execution。

<a id="p2-pipeline"></a>
## 11. Consolidated P2 compiler pipeline

```text
1 source discovery + secure canonical parse
2 register all System symbols
3 register RuleViewKey(SystemKey,name) and other symbols
4 resolve System/RuleView/rule/view references
5 compile canonical ModelPath
6 classify direct/dynamic access
7 build exact CompiledModelAccessRule set
8 ModelAccessPolicyIndex.of(...)
9 include System/RuleView/PolicyIndex semantics in SemanticDigestInput
10 DigestBoundCompiledInput freezes same facts + digest
11 CompiledModelSet.published(...same facts...)
12 caller-scoped atomic Context publication
```

No partial Context on any ERROR。

<a id="p2-diagnostics"></a>
## 12. Diagnostics

At least：

- `MIX-SYSTEM-DUPLICATE`
- `MIX-SYSTEM-UNKNOWN`
- `MIX-RULEVIEW-SYSTEM-REQUIRED`
- `MIX-RULEVIEW-DUPLICATE`
- `MIX-RULEVIEW-UNKNOWN-SYSTEM`
- `MIX-RULEVIEW-UNKNOWN-RULE`
- `MIX-MODEL-PATH-INVALID`
- `MIX-MODEL-ACCESS-DENIED`
- `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`

Diagnostic ordering must be deterministic and SourceRef-aware。Equivalent source ordering yields identical ordered diagnostics。

<a id="p2-compatibility"></a>
## 13. Compatibility / migration

- Java release 8；
- no new `dec-core-runtime` module；
- context never depends on compiler/starter；compiler never depends on starter；
- existing `EngineContext(CompiledModelSet)` and existing 8-arg `CompiledModelSet` constructor remain；
- legacy RuleView bare-name surface is read-only compatibility only and may not register into new composite registry；
- P2 does not restore retired `dec-expand-declaration`；surviving declaration/System compatibility is retained unchanged until P7；
- failure preserves old Context；no hidden migration or second runtime authority。

## 14. Requirement coverage map

| AC | Design coverage |
|---|---|
| AC-001 System deterministic compile | §1 + §11 + §12 |
| AC-002 RuleView System ownership/isolation | §2 |
| AC-003 composite lookup/call | §2.3 |
| AC-004 READ/WRITE/EXECUTE matrix | §3 + §5 + Decision |
| AC-005 ModelPath | §3 |
| AC-006 dynamic access | §4 + §8 |
| AC-007 unified Guard/no bypass | §8 + §13; actual future P3-P7 executor implementation remains contract-only |
| AC-008 atomic publication/context isolation | §6 + §11 |
| AC-009 deterministic/source-aware diagnostics | §12 |
| AC-010 declaration migration boundary | §13 |

AC-007 is design-contract coverage only until later concrete execution modules exist; Test Design must not claim implementation proof。

## 15. Gate

`DESIGN-P2-R14 = NEEDS_REVIEW / MACHINE_BLOCKED`。

Required exact-revision Review：Architecture、ApiContract、Develop、Impact、CrossModule、Concurrency；DataMigration remains conditional on migration-impact detection。Until exact Review + machine lifecycle close, Implementation Plan/TDD/Development remain BLOCKED。
