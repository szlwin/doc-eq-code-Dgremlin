# COMPILER P2 设计测试接缝

> Revision：`DESIGN-P2-R12`。正式 Test Design candidate：`TESTDESIGN-P2-R13`。
> 本 Revision 新增 production bridge reachability 与 policy publication/legacy compatibility seams；不创建 production skeleton，不执行 TDD。

## 1. Module seams

| Seam | Exact module | Production owner |
|---|---|---|
| Policy-index validated factory / CompiledModelSet compatibility | `dec-core-context` | `dec.core.context.model.access.*` + existing model classes |
| Classifier/rule/plan/index publication/digest | `dec-core-compiler` | existing modelaccess/compiled/pass packages |
| Bridge/runtime/Guard/Gateway/registry | `dec-core-starter` | `dec.core.starter.access.*` |
| Trusted state/target/operation ports | `dec-core-starter` | `dec.core.starter.access.spi.*` |
| Real bridge reachability | `dec-demo` | existing `systems.xml` + starter dependency |

No case may use nonexistent `framework execution runtime` / `dec-core-runtime` module。

## 2. Policy-index construction seam — FND-015

Context contract tests must prove：

```text
ModelAccessPolicyIndex.empty()
ModelAccessPolicyIndex.of(Iterable<CompiledModelAccessRule>)
find(exact ModelAccessRuleKey)
keys()
```

Required negative seams：

- duplicate exact rule keys are rejected even if caller would otherwise place them in a Map；
- null/mismatched/non-canonical keys rejected；
- STATIC_ALLOW with plan/requirement rejected；
- runtime-required without exact plan/requirement rejected；
- returned index immutable/deterministic；
- no public mutator or unvalidated raw-map authority constructor。

## 3. CompiledModelSet publication compatibility seam — FND-015

Reflection/source/API test must freeze the existing eight-argument constructor signature and the new explicit factory：

```text
existing 8-arg constructor -> policy index empty
CompiledModelSet.published(..., policyIndex, ...)
CompiledModelSet.modelAccessPolicyIndex()
EngineContext.modelAccessPolicyIndex()
```

Assertions：

- legacy constructor remains callable and existing P1 facts/projection behavior unchanged；
- legacy constructor returns immutable empty policy index；
- legacy constructor does not scan definitions/typedRegistries to reconstruct permission；
- protected access against legacy context exact-misses policy and fails closed；
- policy-aware factory retains exact immutable index；
- EngineContext returns same immutable authority；
- equals/hashCode distinguish different policy indexes。

## 4. Compiler digest/publication seam — FND-015

Compiler harness observes the P2 production sequence：

```text
compiled access rules
 -> ModelAccessPolicyIndex.of
 -> SemanticDigestInput(same index)
 -> DigestBoundCompiledInput(same index + digest)
 -> CompiledModelSetBuilder.FrozenInput
 -> CompiledModelSet.published(same index + digest)
```

Must prove：

- index built before digest compute；
- digest input and final published model use the same immutable index snapshot；
- semantic rule/status/plan/requirement change alters semantic digest；
- equivalent ordering keeps same digest/index order；
- production P2 candidate path does not call legacy 8-arg constructor；
- runtime capability/token/registry state does not enter semantic digest。

## 5. Production execution bridge API seam — FND-004/FND-016

Starter API inspection must require：

```text
ProtectedExecutionBridge
ProtectedExecutionToken
ProtectedExecutionStatePort
ProtectedExecutionBridgeReceiver
```

and forbid external production reliance on：

```text
public issueInvocation(...)
public execute(ProtectedAccessResolutionContext, ProtectedOperationIntent)
public bridgeFor(arbitrary ruleKey)
execute(token, ruleKey/op/frame/owner/cursor)
```

Bridge properties：

- no public/protected constructor/rebind；
- created only by runtime factory from frozen composition registration；
- receiver gets its exact bridge once；
- bridge binds consumer/rule/operation/state/target/operation ports immutably；
- per-call input is only opaque token。

## 6. Token authenticity seam

Use deterministic trusted state port with an adapter-private token implementation。

Positive：recognized current token -> internal issuance allowed。

Negative：

- caller anonymous marker token；
- token owned by another bridge/adapter；
- stale/replayed execution token；
- token copied into different EngineContext runtime。

Expected before internal issuance：

```text
PROTECTED_EXECUTION_TOKEN_UNTRUSTED
issued pair = 0
target resolver = 0
capability = 0
Gateway = 0
Guard = 0
Policy lookup = 0
operation/effects = 0
```

Token exposes no consumer/rule/op/frame/owner/cursor authority getters。

## 7. Trusted bridge reachability seam — FND-016

`dec-demo` test must prove a different module/package can use only production-supported API：

```text
real application composition
 -> register trusted state/target/operation ports + bridge receiver
 -> ProtectedAccessRuntimeFactory creates runtime
 -> receiver gets bridge
 -> framework adapter creates recognized opaque execution token
 -> bridge.execute(token)
 -> starter internal issuance
 -> Guard
 -> operation
```

Test fails if it needs：

- reflection；
- package-private method access from `dec-demo`；
- test-only public mint helper；
- manual issued context/intent construction；
- moving test class into `dec.core.starter.access` solely for access；
- direct Guard/Gateway invocation instead of production bridge。

## 8. Internal issued-pair defense seam

Starter same-package tests still validate R11 defenses：

- caller/fake read-interface implementations cannot pass registry identity check；
- A-context+B-intent rejected；
- READ -> WRITE/EXECUTE intent replacement rejected；
- failures before resolver/policy lookup；
- internal issue record not externally exposed。

This seam is defense-in-depth；it does not replace the external production reachability case。

## 9. STATIC_ALLOW Guard path — FND-001 regression

Through the bridge, not a package-private shortcut：

```text
DIRECT_EXACT -> STATIC_ALLOW(no plan)
recognized token
 -> bridge
 -> internal issuance
 -> resolver
 -> Gateway=1
 -> Guard=1
 -> PolicyIndex exact lookup=1
 -> RuntimeBindingVerifier=0
 -> evaluator=0
 -> same target operation=1
```

Caller-side direct static executor remains unavailable / `MODEL_ACCESS_GUARD_BYPASS` before operation。

## 10. Runtime binding / substitution — FND-017/FND-019

Bridge-bound runtime fixture：

- actual element A -> runtime-required rule -> verifier match -> A op=1；
- foreign B under same static tuple -> DENY；
- capability A + forced executor target B -> `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`，A/B op=0；
- membership/frame/cursor/Context invalidation -> stale DENY；
- capability replay -> consumed；concurrent replay -> at most one success。

Bridge operation/rule cannot be changed by token or caller after composition。

## 11. Single policy authority seam

Spy counts：

```text
Guard current EngineContext policy lookup = 1
Resolver = 0
Gateway = 0
RuntimeBindingVerifier = 0
Execution state port = 0
Target port = 0
Operation port = 0
```

Repository/source inspection fails if starter builds a second authorization `Map<ModelAccessRuleKey,...>` or scans definitions/typed registries for policy selection。

## 12. Real source integration

Existing source：`dec-demo/src/main/resources/mix/system/systems.xml`。

Oracle：

```text
real source
 -> production parser/compiler
 -> policy index construction
 -> semantic digest binding
 -> policy-aware CompiledModelSet
 -> EngineContext
 -> production starter composition
 -> bridge delivered to trusted test adapter
 -> recognized execution token
 -> static status branch: Guard=1/verifier=0/op=1
 -> every(orderDetailList,status=1) branch: Guard=1/verifier=1/op=1 on valid A
 -> forged/foreign/stale token or runtime proof: op=0/effects=0
```

Manual compiled rule、manual issued pair、classifier stub or direct Gateway cannot satisfy E2E。

## 13. P2/P3 boundary seam

Architecture/source inspection fails P2 if it introduces：

- full Rule/change/action/query business executor；
- QueryPlan semantics；
- datasource transaction orchestration；
- source-authored per-object permission DSL；
- starter business dependency merely to access domain POJO。

Bridge/state port is access-control plumbing only。

## 14. Exact planned test classes

`TESTDESIGN-P2-R13` freezes at least：

- `dec-core-context`: `dec.core.context.model.access.ModelAccessPolicyIndexContractTest`
- `dec-core-context`: `dec.core.context.model.ModelAccessPolicyPublicationCompatibilityTest`
- `dec-core-compiler`: `dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest`
- `dec-core-starter`: `dec.core.starter.access.ProtectedExecutionBridgeContractTest`
- `dec-core-starter`: `dec.core.starter.access.ProtectedAccessInputAuthorityTest`
- `dec-core-starter`: `dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest`
- `dec-core-starter`: existing static/runtime/proof/substitution tests
- `dec-demo`: `dec.demo.p2.P2TrustedIssuanceReachabilityTest`
- `dec-demo`: `dec.demo.p2.P2DynamicSourceToOperationTest`

Names are planned TDD targets only；current files are not claimed to exist。

## 15. RED validity

Every target：

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

Second command MUST NOT use `-am`。Missing module/test/symbol/setup/compile failure = INVALID_RED；pre-skeleton RED must use compilable reflection/source/API-shape tests when necessary。

## 16. Review gate

These seams are candidate design only。FND-004/FND-015/FND-016 remain PARTIAL_FIX_PROPOSED / OPEN until exact R12 specialist Review accepts them；TDD execution remains illegal while Design/machine gates are blocked。