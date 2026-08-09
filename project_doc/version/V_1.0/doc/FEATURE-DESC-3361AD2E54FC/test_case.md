# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R16`  
> Base：`TESTDESIGN-P2-R15`  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809` + `BM-R13` + `DESIGN-P2-R15`  
> Status：`NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`

R16 保留 R15 已重新 materialize 的 AC-001～AC-010 基础 Case，并新增 System ownership/RuleView→View、cross-consumer ModelPath、cross-operation non-implication、P2 production seam、runtime denial diagnostic determinism。本文只做 Test Design，不创建 TDD skeleton、不执行测试。

## 1. Formal RED contract

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

第二条禁止 `-am`。missing TestClass/symbol/setup/compile failure 使 intended assertion 未实际运行时为 `INVALID_RED`。

## 2. Planned TestClass map

### dec-core-context
- `dec.core.context.p2.SystemOwnershipSnapshotContractTest`
- `dec.core.context.p2.RuleViewCompiledRelationContractTest`
- `dec.core.context.model.access.ModelAccessPolicyIndexContractTest`
- `dec.core.context.model.ModelAccessPolicyPublicationCompatibilityTest`

### dec-core-compiler
- `dec.core.compiler.p2.SystemCompilationContractTest`
- `dec.core.compiler.p2.RuleViewCompilationContractTest`
- `dec.core.compiler.p2.ModelPathCrossConsumerContractTest`
- `dec.core.compiler.access.ModelAccessRuleCompilationContractTest`
- `dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest`
- `dec.core.compiler.p2.P2DiagnosticDeterminismTest`

### dec-core-starter
- `dec.core.starter.access.ProtectedExecutionBridgeContractTest`
- `dec.core.starter.access.ProtectedExecutionBridgeConcurrencyTest`
- `dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest`
- `dec.core.starter.access.ProtectedAccessStaticAllowPathTest`
- `dec.core.starter.access.RuntimeBindingProofIntegrationTest`
- `dec.core.starter.access.ProtectedAccessOperationBindingTest`
- `dec.core.starter.access.ProtectedAccessOperationIndependenceTest`
- `dec.core.starter.access.ProtectedAccessProductionSeamTest`
- `dec.core.starter.access.RuntimeDenialDiagnosticDeterminismTest`

### dec-demo
- `dec.demo.p2.P2SystemOwnershipRealFixtureTest`
- `dec.demo.p2.P2RuleViewCompositeRealFixtureTest`
- `dec.demo.p2.P2DynamicClassifierRealFixtureTest`
- `dec.demo.p2.P2DirectBridgeReachabilityTest`
- `dec.demo.p2.P2DynamicSourceToOperationTest`

## 3. AC-001 System deterministic compile + first-class ownership

### CASE-P2-TD-SYSTEM-DETERMINISM-001 — BLOCKING
Module/Class：`dec-core-compiler / SystemCompilationContractTest`

Same semantic System sources in at least two discovery orders.

Oracle：same ordered SystemKey set、same ownership snapshots、same semantic digest、same ordered diagnostics；no filename/path/load-order identity。

### CASE-P2-TD-SYSTEM-DUPLICATE-001 — BLOCKING
Same exact SystemKey in two sources -> `MIX-SYSTEM-DUPLICATE`; publication=0; old Context unchanged。

### CASE-P2-TD-SYSTEM-FORWARD-REF-001 — BLOCKING
All System symbols register before owner-qualified references resolve; legal forward refs order-independent; unknown owner stable ERROR。

### CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001 — BLOCKING
Module/Class：`dec-core-context / SystemOwnershipSnapshotContractTest`

For one compiled System assert public snapshot contains exact owned Data/View/RuleView/Rule/Information/ModelAccessRule keys and every key resolves in same `CompiledModelSet`.

Negative：
- orphan owned fact；
- ownership snapshot missing an existing System-owned fact；
- snapshot contains foreign/missing key；
- mutable set exposure。

All invalid candidates must be rejected before publication.

### CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001 — BLOCKING
Declared version present -> exact value retained；declared version absent -> Optional.empty；source semantic content change -> sourceSemanticDigest changes；source order only -> digest unchanged；no timestamp/random fabricated version。

### CASE-P2-TD-SYSTEM-OWNERSHIP-REAL-FIXTURE-001 — BLOCKING
Module/Class：`dec-demo / P2SystemOwnershipRealFixtureTest`

Compile real `mix/system/systems.xml`. For `order` System prove owned relationships include applicable Data/View/RuleView/Information/model-access keys from source facts, not only key+SourceRef shell.

## 4. AC-002 RuleView ownership / duplicate / View relation

### CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001 — BLOCKING
Missing `system` -> `MIX-RULEVIEW-SYSTEM-REQUIRED`; no bare fallback。

### CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001 — BLOCKING
Same `(SystemKey,name)` twice -> stable ERROR, no publication。

### CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001 — BLOCKING
Two Systems define same local RuleView name -> both coexist and resolve only through own composite keys。

### CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001 — BLOCKING
Module/Class：`dec-core-compiler / RuleViewCompilationContractTest`

Real/fixture RuleView has `view-ref`.

Oracle：`CompiledRuleView.resolvedViewKey()` exact matches intended View; returned View resolves and ownership is compatible with RuleView System; ordered `resolvedRuleKeys` exact resolve.

Negative：unknown View、wrong owner View、unknown Rule -> source-aware stable ERROR；publication=0。

### CASE-P2-TD-RULEVIEW-VIEW-REAL-FIXTURE-001 — BLOCKING
Module/Class：`dec-demo / P2RuleViewCompositeRealFixtureTest`

For real `save-Order` (or current equivalent) prove `system=order`, composite key, `view-ref=OrderInfo` exact resolution and rule refs from production compiler output.

## 5. AC-003 composite lookup / no bare fallback

### CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001 — BLOCKING
`RuleViewResolver.require(system,name)` resolves exact owner-qualified result; wrong System fails without global search。

### CASE-P2-TD-RULEVIEW-BARE-NAME-REJECT-001 — BLOCKING
No new production `find(String)`/bare-name lookup surface or equivalent fallback。

### CASE-P2-TD-LEGACY-NO-NEW-BARE-FALLBACK-001 — BLOCKING
Legacy compatibility can read historical surface only; cannot register new RuleView, alter ownership, write PolicyIndex or execute protected mutation bypass。

## 6. AC-004 READ / WRITE / EXECUTE independence

### CASE-P2-TD-ACCESS-READ-MATRIX-001 — BLOCKING
Declared READ exact rule -> READ path can proceed to Guard decision; undeclared READ -> DENY。

### CASE-P2-TD-ACCESS-WRITE-MATRIX-001 — BLOCKING
Explicit WRITE -> can proceed; undeclared shared WRITE -> DENY before mutation。

### CASE-P2-TD-ACCESS-EXECUTE-MATRIX-001 — BLOCKING
Explicit EXECUTE -> can proceed; undeclared EXECUTE -> DENY before effect。

### CASE-P2-TD-ACCESS-NON-IMPLICATION-001 — BLOCKING
Module/Class：`dec-core-starter / ProtectedAccessOperationIndependenceTest`

For same System+target+ModelPath run exact cross-operation matrix:

```text
READ-only policy:    READ ALLOW-eligible, WRITE DENY, EXECUTE DENY
WRITE-only policy:   READ DENY, WRITE ALLOW-eligible, EXECUTE DENY
EXECUTE-only policy: READ DENY, WRITE DENY, EXECUTE ALLOW-eligible
```

Assert Guard exact lookup uses operation-qualified key and no `hasAnyPermission(path)` shortcut exists。

### CASE-P2-TD-STATIC-DENY-001 — BLOCKING
Static illegal/undeclared exact access -> compile ERROR or exact fail-closed result per classification; never default allow。

## 7. AC-005 one canonical ModelPath across consumers

### CASE-P2-TD-MODEL-PATH-UNKNOWN-001 — BLOCKING
Unknown segment/case mismatch/parent fallback/fuzzy search -> stable ERROR; no consumer-specific recovery。

### CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001 — BLOCKING
Real `read path="*"` expands at compile time to finite exact child ModelPaths; published index contains no wildcard runtime key。

### CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001 — BLOCKING
Module/Class：`dec-core-compiler / ModelPathCrossConsumerContractTest`

Given equivalent path facts from:
- rule-data `status = 1`；
- change-data `status : 1`；
- QUERY_CONTRACT path `status`；
- model-access path `status`。

When all enter production shared `ModelPathCompiler`.

Then：
- value-equal canonical `ModelPath`；
- exact same segments/case/root semantics；
- same invalid path yields same path classification family/source-aware Diagnostic；
- no consumer-specific parent/fuzzy fallback；
- QUERY_CONTRACT test stops at P2 compile/IR boundary, does not implement QueryPlan execution。

Also repeat for collection-related `every(orderDetailList,status=1)` path components where applicable。

## 8. AC-006 dynamic access

### CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001 — BLOCKING
Module/Class：`dec-demo / P2DynamicClassifierRealFixtureTest`

Real fixture:
- `status = 1` -> `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；
- `every(orderDetailList,status=1)` -> `RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；
- unsupported dynamic selector -> compile ERROR。

### CASE-P2-TD-RUNTIME-BINDING-PROOF-001 — BLOCKING
Runtime element/frame/owner/cursor belongs to compiler plan -> may proceed; foreign/stale/wrong membership -> DENY before operation/effects。

### CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001 — BLOCKING
Exact rule exists but wrong/stale plan/proof -> stable DENY; no rule reselection。

## 9. AC-007 P2 production seam / no legal bypass

Effective acceptance follows `DEC-P2-AC007-STAGE-BOUNDARY-001`.

### CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001 — BLOCKING
Module/Class：`dec-core-starter / ProtectedAccessProductionSeamTest`

Inspect real compiled API/visibility and execute production seam.

Must prove：
- public business runtime entry is `ProtectedExecutionBridge.execute(...)`；
- no public/protected issued-pair mint；
- no public/protected capability mint；
- no public post-Guard operation API that accepts caller-selected target；
- no EngineContext secondary permission map/authority；
- compatibility adapter cannot write/mint/execute protected mutation；
- allow and deny both traverse Bridge→Gateway→Guard；
- reflection/package-private/test backdoor is not used as production evidence。

### CASE-P2-TD-GUARD-NO-BYPASS-001 — BLOCKING
Attempt all supported P2 production paths; protected operation/effect counter remains 0 unless same invocation passes Guard。

### CASE-P2-TD-STATIC-ALLOW-GUARD-PATH-001 — BLOCKING
STATIC_ALLOW still does exact Guard lookup once; runtime verifier/evaluator count=0。

### CASE-P2-TD-DIRECT-BRIDGE-REACHABILITY-001 — BLOCKING
Module/Class：`dec-demo / P2DirectBridgeReachabilityTest`

Different Maven module/package uses only public production composition/Bridge; no package-private issueInvocation/manual issued pair/reflection；real Context policy reaches operation。

### Downstream obligations — NOT P2 TDD
P3 Rule/Information, P4 change/custom-action/produce and P6 QueryPlan concrete consumer integration must later prove they plug into the P2 seam. R16 must not pretend those executors already exist。

## 10. AC-008 atomic publication / Context isolation

### CASE-P2-TD-ATOMIC-PUBLICATION-001 — BLOCKING
Ownership/RuleView/View/ref/path/policy ERROR -> candidate publication=0 and old Context unchanged；valid candidate publishes all snapshots/index/digest together。

### CASE-P2-TD-CONTEXT-ISOLATION-001 — BLOCKING
Two EngineContexts have separate immutable System ownership/RuleView/PolicyIndex snapshots；no shared mutable registry/current。

### CASE-P2-TD-POLICY-INDEX-PUBLICATION-001 — BLOCKING
Module/Class：`dec-core-compiler / ModelAccessPolicyIndexPublicationTest`

Index built before semantic digest；same immutable index retained by DigestBoundCompiledInput and final `CompiledModelSet.published`；P2 path does not use legacy constructor；authorization semantic change changes digest；equivalent ordering does not。

### CASE-P2-TD-POLICY-PUBLICATION-COMPATIBILITY-001 — BLOCKING
Legacy eight-arg CompiledModelSet constructor still exists -> empty policy; no reconstruction; new published factory retains supplied authority; equality/hash semantics distinguish policy-aware content as required。

## 11. AC-009 compile + runtime diagnostic determinism

### CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001 — BLOCKING
Repeat duplicate System, ownership mismatch, missing RuleView System, unknown View/Rule, invalid path/static access failures -> same ordered codes/definition identities/SourceRefs/relatedRefs。

### CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001 — BLOCKING
Module/Class：`dec-core-starter / RuntimeDenialDiagnosticDeterminismTest`

For each class below, repeat the same denial against same immutable Context at least twice:
- `POLICY_NOT_FOUND`；
- `RUNTIME_BINDING_STALE`；
- `RUNTIME_PLAN_MISMATCH`；
- `TARGET_SUBSTITUTION`；
- `GUARD_UNAVAILABLE`。

Oracle：same denial code、SystemKey、optional RuleView provenance、AccessOperation、canonical ModelPath、policy SourceRef；operation/effects=0；no sensitive runtime actual value/object dump/credential/config payload。

## 12. AC-010 declaration migration boundary

### CASE-P2-TD-DECLARATION-BOUNDARY-001 — BLOCKING
`dec-expand-declaration` remains retired；surviving compatibility only read；no write/second registry/second runtime authority；P7 deletion conditions remain traceable。

## 13. Runtime remediation retained

### CASE-P2-POLICY-INDEX-CONSTRUCTION-001-R16 — BLOCKING
`ModelAccessPolicyIndex.empty/of(Iterable)` duplicate/null/key/status/plan/wildcard validation, deterministic order, immutable exact find。

### CASE-P2-DIRECT-BRIDGE-CONTRACT-001-R16 — BLOCKING
Direct bridge exact argument shape; no token API; operation mismatch/policy miss fail closed；caller valid alternate rule/op selection is not treated as forged authority under persistent Decision。

### CASE-P2-DIRECT-BRIDGE-CONCURRENCY-001-R16 — BLOCKING
Different concurrent invocations do not cross-wire frame/owner/cursor/rule/op/target/capability. Identical arguments are independent invocations; no token replay assertion。

### CASE-P2-OPERATION-BINDING-001-R16 — BLOCKING
Capability A cannot execute target B or substitute operation after resolution；same capability concurrent terminal success <= 1。

### CASE-P2-SOURCE-TO-OPERATION-001-R16 — BLOCKING
Module/Class：`dec-demo / P2DynamicSourceToOperationTest`

Real source -> compiler -> ownership/RuleView/PolicyIndex -> EngineContext -> public direct Bridge -> resolver -> capability -> Gateway -> Guard -> protected operation. No hand-built policy index/manual issued pair shortcuts。

## 14. Review Gate

- all current traceability case IDs must resolve to this R16 file；
- current Design R15 must pass exact Review before TestDesign can pass；
- risk scan/lifecycle still required；
- no Case is execution Evidence yet；
- no TDD/Development until open P0/P1 and machine gates permit。
