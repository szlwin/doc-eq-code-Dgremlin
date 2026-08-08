# COMPILER P2 架构增量

> Revision：`DESIGN-P2-R14`。Base：`DESIGN-P2-R13`。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本 Revision 恢复 System / RuleView 主架构，并与 direct bridge + PolicyIndex publication 组成一份 consolidated architecture。

## 1. Repository dependency direction

```text
dec-core-context
  SystemKey / RuleViewKey / immutable compiled facts
  ModelAccessPolicyIndex / CompiledModelSet / EngineContext
       ^
       | existing compiler dependency
dec-core-compiler
  System registration / RuleView composite resolution
  ModelPath + access classification
  PolicyIndex construction + digest-bound publication
       ^
       | existing starter composition dependency
dec-core-starter
  ProtectedExecutionBridge / resolver / Gateway / Guard / verifier
  target/operation SPI
       ^
       | application dependency
dec-demo / future P3-P7 execution modules
  fixture / consumer integration
```

No `dec-core-runtime`。No context -> compiler/starter reverse dependency。Compiler does not depend on starter。

<a id="2-发布闭包"></a>
## 2. System / RuleView / policy 发布闭包

```text
canonical source set
 -> register all explicit SystemKey
 -> register RuleViewKey(SystemKey,name)
 -> resolve System/RuleView/rule/view references
 -> canonical ModelPath
 -> compiled access rules
 -> ModelAccessPolicyIndex.of(...)
 -> SemanticDigestInput(System + RuleView + same PolicyIndex)
 -> digest
 -> DigestBoundCompiledInput(same immutable facts + digest)
 -> CompiledModelSet.published(...)
 -> EngineContext
```

All-or-nothing：任一 duplicate/unknown/missing-System/path/access ERROR 都阻断候选发布。Old EngineContext remains visible。

## 3. System ownership architecture

System registry belongs to compiler build state and publishes immutable context facts。System identity is exact `SystemKey` only。All System symbols are registered before forward references resolve。

No file/path/order inference。No starter/runtime second System registry。

## 4. RuleView composite architecture

Canonical RuleView registry exact-keyed by `(SystemKey,name)`。

```text
system-ref + rule-ref
 -> SystemKey
 -> RuleViewKey(SystemKey,rule-ref)
 -> exact immutable RuleViewResolver
```

Different Systems may hold the same local RuleView name。Same-System duplicate is ERROR。New bare-name lookup/register is architecturally forbidden。

Legacy bare-name compatibility, if present, is an isolated read-only adapter and cannot feed canonical registry。

<a id="3-动态权限边界"></a>
## 5. Model access / dynamic authorization architecture

Compile-time：

```text
DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW
EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED
unsupported selector -> compile ERROR
```

READ wildcard is finite-expanded before policy publication。Runtime keys are exact only。

Unique policy authority：

```text
compiler exact rules
 -> ModelAccessPolicyIndex
 -> CompiledModelSet
 -> EngineContext
 -> Guard exact lookup once
```

No definitions scan / typed-registry rebuild / starter secondary policy map。

## 6. Direct bridge architecture

Decision `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` makes direct arguments the current P2 production entry：

```text
future executor/business caller
 -> bridge.execute(ruleKey,op,frame,owner,cursor)
 -> starter validates invocation shape/current context
 -> internal issued invocation record
 -> target resolver
 -> one-shot capability
 -> Gateway
 -> Guard exact PolicyIndex lookup
 -> STATIC fast path or runtime proof
 -> same bound operation target
```

Bridge composition binds context/runtime + `AccessConsumerIrKey` provenance + target/operation ports。Caller chooses per-call exact rule/op in current P2。Consumer identity is not an authorization-key dimension in this Revision。

## 7. Requirement delta boundary

The original Requirement states future consumers must not expand compiler-declared authorization。Current decision interprets this as：

- callers may select among exact rules already compiler-published in current PolicyIndex；
- callers cannot create or modify policy rules；
- absent exact key/op remains DENY；
- no per-consumer binding is required now。

This is an explicit Decision delta, not an implicit claim that REQAN-P2-R01 already specified this API trust model。

## 8. CompiledModelSet compatibility architecture

```text
LEGACY
existing 8-arg constructor
 -> existing immutable model facts
 -> ModelAccessPolicyIndex.empty()
 -> no policy reconstruction

P2 PRODUCTION
System/RuleView/access compile
 -> policy index before digest
 -> CompiledModelSet.published(...same index...)
 -> EngineContext
```

Legacy Context protected access therefore exact-misses policy and fails closed。

## 9. Atomic publication / Context isolation

Compiler candidate holds System registry, RuleView registry, PolicyIndex, diagnostics, versions and digest as one publication closure。Publisher is invoked only after complete validation。Publication conflict/failure keeps old Context。

Parallel Contexts do not share mutable System/RuleView/policy registries and there is no global current Context。

## 10. Diagnostic architecture

Stable code + SourceRef + definition identity + related refs. Deterministic sort independent of source input order。System duplicate, missing RuleView System, same-system duplicate, unknown composite reference, path/access errors all remain compile-time publication blockers。

## 11. Concurrency / TOCTOU

- System/RuleView/PolicyIndex publication objects immutable；
- bridge itself stateless for independent direct calls；
- identical scalar calls are independent, not replay；
- issued invocation/capability state context-local；
- same capability reserve/consume atomic and terminal success <= 1；
- runtime branch revalidates Context/frame/cursor/rule/plan/membership immediately before operation；
- actual target and operation remain capability-bound。

## 12. AC-007 no-bypass scope

Architecture enforces a single starter protected-access seam. P2 can prove that the seam itself has no direct Guard/Gateway bypass. Concrete Rule/change/custom-action/query executors belong to P3-P7 and therefore AC-007 remains **CONTRACT_ONLY** until those execution modules integrate and are verified。

P2 must not claim those future business execution paths are already implementation-verified。

<a id="4-迁移架构"></a>
## 13. Migration / compatibility

- P2 does not restore retired `dec-expand-declaration`；
- surviving declaration/System compatibility remains read-only until P7；
- old bare RuleView adapter cannot register new composite facts；
- no hidden second runtime / registry / Context；
- Java 8 and existing EngineContext/CompiledModelSet constructor compatibility retained。

## 14. Cross-module implementation mapping

`CMI-P2-SYSTEM-RULEVIEW-001`:

1. frontend preserves explicit System/RuleView/model-access SourceRef facts；
2. compiler owns System registration/composite resolution/path/access compilation；
3. context owns immutable published keys/registries/PolicyIndex；
4. starter owns protected runtime/Guard；
5. demo/future executors consume public bridge only；
6. declaration compatibility remains a separate read-only migration boundary。

## 15. Review gate

Exact `DESIGN-P2-R14` Architecture + ApiContract + Develop + Impact + CrossModule + Concurrency Review required。`risk_detection.json` remains NOT_SCANNED until real machine lifecycle is available。Implementation Plan/TDD/Development remain BLOCKED。
