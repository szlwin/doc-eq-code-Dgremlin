# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R15`。
> Base：`TESTDESIGN-P2-R14`。
> Inputs：Requirement `REQAN-P2-R01`、Decision `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`、Business Model `BM-R12`、Design `DESIGN-P2-R14`。
> Status：`NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`。本 Revision 重新 materialize AC-001～AC-010 全量 Case，并保留 R14 runtime remediation；不创建 skeleton、不执行 TDD。

## 1. Test Design principles

1. 当前 Test Design 的所有 traceability `test_case_ids` 必须在本文件真实存在。
2. 所有 Design refs 必须指向 `DESIGN-P2-R14` consolidated artifact，而不是过期 R01/R12 章节。
3. System / RuleView / ModelAccess / publication / diagnostics / migration 与 runtime remediation 同时覆盖，不能只保留近期 FND Case。
4. Direct bridge 采用用户确认的 direct-argument authority decision：caller 选择另一个有效 compiler-published ruleKey/op 不作为 forged-authority failure。
5. AC-007 当前只能证明 P2 protected runtime seam 无旁路；P3-P7 实际 executor 尚未实现，因此最终状态为 `CONTRACT_ONLY`。
6. 每个 target RED 第二条 Maven command 禁止 `-am`。
7. Design exact Review / machine gate 前不进入 TDD。

## 2. AC-001 System deterministic compile

### CASE-P2-TD-SYSTEM-DETERMINISM-001 — BLOCKING

Module/TestClass：`dec-core-compiler / dec.core.compiler.p2.SystemCompilationContractTest`

Fixture：相同 System definitions，以至少两种 source discovery order 输入。

Oracle：

- exact same ordered SystemKey set；
- same semantic digest；
- same ordered diagnostics；
- no filename/path/order-derived identity。

### CASE-P2-TD-SYSTEM-DUPLICATE-001 — BLOCKING

Two sources declare same exact SystemKey。

Expected：`MIX-SYSTEM-DUPLICATE`；candidate publication=0；old EngineContext unchanged。

### CASE-P2-TD-SYSTEM-FORWARD-REF-001 — BLOCKING

A System-owned definition references a System/member discovered later。

Expected：all System symbols register before reference resolution；valid forward ref succeeds independent of source order；unknown ref emits stable ERROR。

## 3. AC-002 RuleView ownership / duplicate / isolation

### CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001 — BLOCKING

New mix RuleView omits System。

Expected：`MIX-RULEVIEW-SYSTEM-REQUIRED`；no bare-name fallback；no candidate publication。

### CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001 — BLOCKING

Two RuleViews with same `(SystemKey,name)`。

Expected：duplicate ERROR + SourceRefs for both definitions；no publication。

### CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001 — BLOCKING

System A and B both define RuleView `summary`。

Expected：`RuleViewKey(A,summary) != RuleViewKey(B,summary)`；both coexist；lookup/call in A never returns B and vice versa；parallel Contexts remain isolated。

## 4. AC-003 composite RuleView lookup/call

### CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001 — BLOCKING

Input call supplies `system-ref + rule-ref`。

Expected：exact SystemKey resolution -> exact RuleViewKey -> exact RuleView；wrong system or wrong local name fails deterministically。

### CASE-P2-TD-RULEVIEW-BARE-NAME-REJECT-001 — BLOCKING

Production canonical resolver inspection + runtime fixture。

Expected：no canonical public `find(String)`/`require(String)` new path；bare `summary` cannot select one of multiple Systems。

### CASE-P2-TD-LEGACY-NO-NEW-BARE-FALLBACK-001 — BLOCKING

Legacy read-only adapter may read only when legacy compatibility rule permits；it cannot register new canonical RuleView or silently resolve ambiguous cross-System local names。

## 5. AC-004 READ / WRITE / EXECUTE authorization matrix

### CASE-P2-TD-ACCESS-READ-MATRIX-001 — BLOCKING

Cover declared exact READ、undeclared READ、read wildcard finite expansion、unknown exact path。

Expected：only compiler-published exact READ keys can ALLOW；unknown/missing key fail closed。

### CASE-P2-TD-ACCESS-WRITE-MATRIX-001 — BLOCKING

Cover explicit WRITE allow and undeclared shared WRITE。

Expected：undeclared shared WRITE compile/runtime DENY；no default allow。

### CASE-P2-TD-ACCESS-EXECUTE-MATRIX-001 — BLOCKING

Cover explicit EXECUTE allow and undeclared EXECUTE。

Expected：missing exact EXECUTE rule DENY。

### CASE-P2-TD-STATIC-DENY-001 — BLOCKING

Static invalid/undeclared access must fail before candidate publication or protected operation。

Decision oracle：caller choosing another **valid** exact PolicyIndex ruleKey/op is allowed by current P2 decision and is not a negative fixture。

## 6. AC-005 canonical ModelPath

### CASE-P2-TD-MODEL-PATH-UNKNOWN-001 — BLOCKING

Unknown/non-canonical/fuzzy runtime path -> compile ERROR / exact runtime miss；no parent/bare fallback。

### CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001 — BLOCKING

Real `read path="*"` expands at compile-time to finite exact child rules。Published PolicyIndex contains no wildcard key；equivalent source ordering yields same exact expansion order/digest。

## 7. AC-006 dynamic access

### CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R15 — BLOCKING

Module/TestClass：`dec-demo / dec.demo.p2.P2DynamicClassifierRealFixtureTest`

Real source：existing `dec-demo/src/main/resources/mix/system/systems.xml`。

Required：

- direct `status = 1` -> `STATIC_BOUND / STATIC_ALLOW`；
- `every(orderDetailList,status=1)` -> `RUNTIME_OBJECT_BOUND / RUNTIME_GUARD_REQUIRED`；
- unsupported dynamic selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`；
- no classifier stub can satisfy。

### CASE-P2-RUNTIME-BINDING-PROOF-001-R15 — BLOCKING

Module/TestClass：`dec-core-starter / dec.core.starter.access.RuntimeBindingProofIntegrationTest`

Actual element A belonging to exact frame/cursor/collection -> proof ALLOW；foreign B、stale frame/cursor/context、wrong plan/provenance -> DENY/effects=0。Verifier cannot select another policy rule。

## 8. AC-007 unified Guard / no bypass

### CASE-P2-TD-GUARD-NO-BYPASS-001 — BLOCKING CONTRACT

Repository/API inspection + starter harness：protected operation exposed by P2 runtime cannot execute by direct target/port call bypassing Gateway/Guard。Direct Guard/Gateway test helper does not count as E2E。

### CASE-P2-STATIC-ALLOW-GUARD-PATH-001-R15 — BLOCKING

`DIRECT_EXACT -> STATIC_ALLOW` via direct bridge：Gateway=1、Guard=1、PolicyIndex lookup=1、RuntimeBindingVerifier=0、operation=1。Caller-side direct STATIC shortcut must not exist。

### CASE-P2-DIRECT-BRIDGE-REACHABILITY-001-R15 — BLOCKING

Module/TestClass：`dec-demo / dec.demo.p2.P2DirectBridgeReachabilityTest`

Different module/package uses only public production bridge：

```text
EngineContext
 -> runtime composition
 -> direct bridge
 -> execute(ruleKey,op,frame,owner,cursor)
 -> internal issuance
 -> resolver/capability
 -> Gateway/Guard
 -> operation
```

Forbidden：reflection、package-private access、manual issued pair、manual Guard shortcut。

AC-007 final status remains `CONTRACT_ONLY` until concrete P3-P7 Rule/change/custom-action/query executors exist and integrate this seam。

## 9. AC-008 atomic publication / Context isolation

### CASE-P2-TD-ATOMIC-PUBLICATION-001 — BLOCKING

Inject System duplicate、missing RuleView System、invalid model path、invalid policy construction before publication。

Expected：publisher call=0 or publication failure leaves prior Context unchanged；no partial System/RuleView/PolicyIndex visible。

### CASE-P2-TD-CONTEXT-ISOLATION-001 — BLOCKING

Compile two valid independent contexts with different System/RuleView/policy facts。

Expected：no mutable registry sharing；Context A lookup/Guard never sees B facts；no global current。

## 10. AC-009 Diagnostic deterministic / source aware

### CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001 — BLOCKING

Run same invalid semantic set under different source discovery order。

Cover at least：System duplicate、missing RuleView System、same-System duplicate、unknown composite RuleView ref、invalid path、denied access。

Expected identical ordered tuples：stable code + definition identity + SourceRef/relatedRefs。

## 11. AC-010 declaration migration boundary

### CASE-P2-TD-DECLARATION-BOUNDARY-001 — BLOCKING

Repository/source inspection + compatibility harness。

Expected：retired `dec-expand-declaration` not restored；surviving declaration/System compatibility read-only；no new canonical System/RuleView registration through legacy surface；P7 retirement marker remains explicit。

## 12. FND-015 PolicyIndex construction / publication

### CASE-P2-POLICY-INDEX-CONSTRUCTION-001-R15 — BLOCKING

Module/TestClass：`dec-core-context / dec.core.context.model.access.ModelAccessPolicyIndexContractTest`

Required API `empty()/of(Iterable)/find/keys`。Reject duplicate exact key、null、STATIC with runtime plan、runtime-required missing exact plan/requirement、runtime wildcard/fuzzy key。Snapshot immutable/deterministic。

### CASE-P2-POLICY-PUBLICATION-COMPATIBILITY-001-R15 — BLOCKING

Module/TestClass：`dec-core-context / dec.core.context.model.ModelAccessPolicyPublicationCompatibilityTest`

Assert existing 8-arg constructor remains；its policy index is immutable empty and no reconstruction occurs。`CompiledModelSet.published(...policyIndex...)` retains exact authority。`EngineContext.modelAccessPolicyIndex()` is direct read-through。Different policy index changes equality/hash semantics。

### CASE-P2-POLICY-INDEX-PUBLICATION-001-R15 — BLOCKING

Module/TestClass：`dec-core-compiler / dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest`

Required production order：System/RuleView/access semantic facts -> exact PolicyIndex -> SemanticDigestInput -> digest -> DigestBoundCompiledInput -> `CompiledModelSet.published`。Same immutable index snapshot used for digest and final model。Authorization/System/RuleView semantic change changes semantic digest。

### CASE-P2-POLICY-INDEX-AUTHORITY-001-R15 — BLOCKING

Module/TestClass：`dec-core-starter / dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest`

Guard current-context index lookup=1；resolver/gateway/verifier/target port/operation port policy lookups=0；no starter second permission map；no definitions/typedRegistries reconstruction。

## 13. Direct bridge argument / concurrency contract

### CASE-P2-DIRECT-BRIDGE-CONTRACT-001-R15 — BLOCKING

Module/TestClass：`dec-core-starter / dec.core.starter.access.ProtectedExecutionBridgeContractTest`

Required public shape accepts `(ModelAccessRuleKey, AccessOperation, frameId, ownerId, Optional<cursorId>)`。No token/recognizes/claim API。Bridge composition retains context + consumer provenance + target/operation ports。

### CASE-P2-DIRECT-BRIDGE-ARGUMENT-VALIDATION-001-R15 — BLOCKING

Null/blank/malformed frame/owner、invalid optional cursor、ruleKey missing PolicyIndex、operation inconsistent with key -> deterministic DENY before operation/effects。

Caller selecting another valid exact PolicyIndex key/op is explicitly **not** a forged-authority failure under Decision `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`。

### CASE-P2-DIRECT-BRIDGE-CONCURRENCY-001-R15 — BLOCKING

Module/TestClass：`dec-core-starter / dec.core.starter.access.ProtectedExecutionBridgeConcurrencyTest`

Use barriers/latches, never `Thread.sleep`。

- two different concurrent calls -> no cross-wiring of rule/op/frame/owner/cursor/target/capability；
- two identical scalar calls are independent invocations; Test does not assert replay suppression；
- each produced capability remains separately one-shot。

## 14. FND-019 capability actual-target/operation binding

### CASE-P2-OPERATION-BINDING-001-R15 — BLOCKING

Module/TestClass：`dec-core-starter / dec.core.starter.access.ProtectedAccessOperationBindingTest`

- capability A + forced target B -> `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH` / op=0；
- operation substitution after capability issuance -> DENY；
- same capability concurrent terminal execution success <= 1；
- stale Context/frame/cursor/plan/membership immediately before operation -> DENY/effects=0。

## 15. Full real source -> operation

### CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R15 — BLOCKING

Module/TestClass：`dec-demo / dec.demo.p2.P2DynamicSourceToOperationTest`

```text
real systems.xml
 -> production source/frontend/compiler
 -> deterministic System registry
 -> RuleView composite registry
 -> ModelPath/access classification
 -> ModelAccessPolicyIndex
 -> semantic digest-bound CompiledModelSet.published
 -> EngineContext
 -> direct bridge
 -> Gateway/Guard
 -> static or runtime-proof branch
 -> capability-bound protected operation
```

Positive + negative assertions include System/RuleView lookup isolation、policy miss、runtime proof stale、target substitution and no bypass。

## 16. Exact Maven / planned TestClass map

| Purpose | Exact module | Planned TestClass |
|---|---|---|
| System compile | `dec-core-compiler` | `dec.core.compiler.p2.SystemCompilationContractTest` |
| RuleView composite | `dec-core-compiler` | `dec.core.compiler.p2.RuleViewCompositeContractTest` |
| access/path classifier | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessRuleCompilationContractTest` |
| policy factory | `dec-core-context` | `dec.core.context.model.access.ModelAccessPolicyIndexContractTest` |
| publication compatibility | `dec-core-context` | `dec.core.context.model.ModelAccessPolicyPublicationCompatibilityTest` |
| policy publication/digest | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest` |
| direct bridge | `dec-core-starter` | `dec.core.starter.access.ProtectedExecutionBridgeContractTest` |
| bridge concurrency | `dec-core-starter` | `dec.core.starter.access.ProtectedExecutionBridgeConcurrencyTest` |
| policy authority | `dec-core-starter` | `dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest` |
| static guard path | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessStaticAllowPathTest` |
| runtime proof | `dec-core-starter` | `dec.core.starter.access.RuntimeBindingProofIntegrationTest` |
| target binding | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessOperationBindingTest` |
| publication/diagnostics | `dec-core-compiler` | `dec.core.compiler.p2.P2PublicationDiagnosticContractTest` |
| real classifier | `dec-demo` | `dec.demo.p2.P2DynamicClassifierRealFixtureTest` |
| direct reachability | `dec-demo` | `dec.demo.p2.P2DirectBridgeReachabilityTest` |
| source E2E | `dec-demo` | `dec.demo.p2.P2DynamicSourceToOperationTest` |

Formal command pattern：

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

Second command MUST NOT use `-am`。Missing module/test/symbol/setup/compile failure = INVALID_RED。

## 17. Gate

`TESTDESIGN-P2-R15 = NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`。

No Test Case above is execution Evidence yet。No skeleton exists。Design R14 exact Review must pass first；then Test Design R15 receives its own independent Requirement/Design/TDD/TestEvidence Review before TDD can begin。
