# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R18`  
> Base：`TESTDESIGN-P2-R17`  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R03` + `BM-R15` + `DESIGN-P2-R17` + `FLOW-R05@p2-system-ruleview-protected-access`  
> Decision：`DEC-P2-AC007-STAGE-BOUNDARY-001 = ACTIVE / OPTION_B / user-decided`  
> Status：`NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`

R18 保留 R17 的 AC-001～AC-010、SystemVersion、ownership truth source、existing API source compatibility、P1→P2 migration 与 runtime remediation Case，并把 AC-007 Option B materialize 为真实 Rule/change/custom-action production representative consumer blocking cases。本文仍只做 Test Design，不创建 TDD skeleton、不执行测试。

## 1. Formal RED contract

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

第二条禁止 `-am`。Missing TestClass/symbol/setup/compile failure 导致 intended assertion 未运行时为 `INVALID_RED`。

## 2. Planned TestClass map

### dec-core-context
- `SystemOwnershipSnapshotContractTest`
- `RuleViewCompiledRelationContractTest`
- `P2KeySourceCompatibilityTest`
- `ModelAccessPolicyIndexContractTest`
- `ModelAccessPolicyPublicationCompatibilityTest`

### dec-core-compiler
- `SystemCompilationContractTest`
- `RuleViewCompilationContractTest`
- `ModelPathCrossConsumerContractTest`
- `P1ToP2ModelAccessMigrationContractTest`
- `ModelAccessRuleCompilationContractTest`
- `ModelAccessPolicyIndexPublicationTest`
- `P2DiagnosticDeterminismTest`

### dec-core-starter
- `ProtectedExecutionBridgeContractTest`
- `ProtectedExecutionBridgeConcurrencyTest`
- `ModelAccessPolicyAuthorityIntegrationTest`
- `ProtectedAccessStaticAllowPathTest`
- `RuntimeBindingProofIntegrationTest`
- `ProtectedAccessOperationBindingTest`
- `ProtectedAccessOperationIndependenceTest`
- `ProtectedAccessProductionSeamTest`
- `RuleProtectedAccessConsumerIntegrationTest`
- `ChangeProtectedAccessConsumerIntegrationTest`
- `CustomActionProtectedAccessConsumerIntegrationTest`
- `ProtectedAccessConsumerParityTest`
- `ProtectedAccessRepresentativeConsumerStructureTest`
- `RuntimeDenialDiagnosticDeterminismTest`

### dec-demo
- `P2SystemOwnershipRealFixtureTest`
- `P2RuleViewCompositeRealFixtureTest`
- `P2DynamicClassifierRealFixtureTest`
- `P2DirectBridgeReachabilityTest`
- `P2DynamicSourceToOperationTest`
- `P2Ac007RepresentativeConsumersRealFixtureTest`

## 3. AC-001 System deterministic compile / ownership / version

### CASE-P2-TD-SYSTEM-DETERMINISM-001 — BLOCKING
Same semantic inputs different discovery order -> same ordered SystemKey、ownership snapshots、digest、diagnostics。

### CASE-P2-TD-SYSTEM-DUPLICATE-001 — BLOCKING
Duplicate exact SystemKey -> stable ERROR、publication=0、old Context retained。

### CASE-P2-TD-SYSTEM-FORWARD-REF-001 — BLOCKING
Register all System symbols before owner-qualified resolution；legal forward refs order-independent。

### CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001 — BLOCKING
Assert snapshot against authoritative sources：Data/View/RuleView/Information typed registries；Rule -> final CompiledRuleView rule closure；ModelAccess -> final PolicyIndex keys/compiled policy rules。Negative：orphan/missing/foreign key、snapshot mutation/rebuild authority -> invalid candidate/no publication。

### CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001 — BLOCKING
Declared version exact when present / empty when absent；source semantic change changes sourceSemanticDigest；source order only does not；schemaVersion/compilerVersion exact equal enclosing CompiledModelSet；no fabricated timestamp/random version。

### CASE-P2-TD-SYSTEM-OWNERSHIP-REAL-FIXTURE-001 — BLOCKING
Real `systems.xml` order System proves owned Data/View/RuleView/Information/ModelAccess relationships from production output。

## 4. AC-002 RuleView ownership / View resolution

### CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001 — BLOCKING
Missing System -> `MIX-RULEVIEW-SYSTEM-REQUIRED`。

### CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001 — BLOCKING
Same `(System,name)` duplicate -> stable ERROR。

### CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001 — BLOCKING
Cross-System same local name coexists/is isolated。

### CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001 — BLOCKING
`resolvedViewKey` exact and owner-compatible；resolved Rule closure exact；unknown/wrong-owner View/Rule stable source-aware ERROR。

### CASE-P2-TD-RULEVIEW-VIEW-REAL-FIXTURE-001 — BLOCKING
Real `save-Order`/current equivalent proves system/order View/rule resolution from production compiler output。

## 5. AC-003 composite lookup / source compatibility

### CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001 — BLOCKING
Exact owner-qualified lookup only；wrong System no global fallback。

### CASE-P2-TD-RULEVIEW-BARE-NAME-REJECT-001 — BLOCKING
No new production bare-name lookup。

### CASE-P2-TD-LEGACY-NO-NEW-BARE-FALLBACK-001 — BLOCKING
Legacy adapter read-only/no registry-policy write/no protected mutation bypass。

### CASE-P2-TD-KEY-SOURCE-COMPAT-001 — BLOCKING
External test source using existing public `SystemKey(String)/name()` and `RuleViewKey(SystemKey,String)/owner()/name()` compiles/runs unchanged；additive aliases, if present, equal existing values。

## 6. AC-004 operation independence

### CASE-P2-TD-ACCESS-READ-MATRIX-001 — BLOCKING
Declared exact READ eligible；undeclared READ DENY。

### CASE-P2-TD-ACCESS-WRITE-MATRIX-001 — BLOCKING
Declared exact WRITE eligible；undeclared WRITE DENY before mutation。

### CASE-P2-TD-ACCESS-EXECUTE-MATRIX-001 — BLOCKING
Declared exact EXECUTE eligible；undeclared EXECUTE DENY before effect。

### CASE-P2-TD-ACCESS-NON-IMPLICATION-001 — BLOCKING
READ-only -> READ eligible, WRITE/EXECUTE DENY；WRITE-only -> WRITE eligible, READ/EXECUTE DENY；EXECUTE-only -> EXECUTE eligible, READ/WRITE DENY。No `hasAnyPermission(path)` shortcut。

### CASE-P2-TD-STATIC-DENY-001 — BLOCKING
Static illegal/undeclared exact access never defaults allow。

## 7. AC-005 shared ModelPath / P1 migration

### CASE-P2-TD-MODEL-PATH-UNKNOWN-001 — BLOCKING
Unknown segment/case mismatch/parent/fuzzy fallback -> stable ERROR across consumers。

### CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001 — BLOCKING
Source `SharedModelPath("*")`/read wildcard expands deterministically to finite exact P2 ModelPaths before policy publication；runtime wildcard count=0。

### CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001 — BLOCKING
Equivalent rule-data/change-data/QUERY_CONTRACT/model-access path -> value-equal P2 ModelPath and same invalid-path family。Query stops at P2 compile/IR boundary。

### CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001 — BLOCKING
Exact P1 path -> exact P2 path；wildcard -> finite exact paths；AccessMode READ/WRITE -> exact AccessOperation；no P1 input infers EXECUTE；after conversion PolicyIndex/Bridge/Guard never query P1 path/mode as authority；no dual-authority fallback。

## 8. AC-006 dynamic access / runtime flow

### CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001 — BLOCKING
Real fixture `status=1 -> STATIC_BOUND/STATIC_ALLOW`; `every(orderDetailList,status=1) -> RUNTIME_OBJECT_BOUND/RUNTIME_GUARD_REQUIRED`; unsupported selector ERROR。

### CASE-P2-TD-RUNTIME-BINDING-PROOF-001 — BLOCKING
Valid membership can proceed；foreign/stale/wrong membership DENY before effects。

### CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001 — BLOCKING
Exact rule but wrong/stale plan -> DENY, no rule reselection。

### CASE-P2-SOURCE-TO-OPERATION-001-R18 — BLOCKING
Real source -> compile flow -> Context -> public Bridge -> runtime protected flow -> Guard -> operation；no hand-built policy/manual issued pair。

## 9. AC-007 — OPTION B ACTIVE / BLOCKING

User decision `DEC-P2-AC007-STAGE-BOUNDARY-001` is ACTIVE Option B。AC-007 cannot close on seam-only evidence；all cases below are part of the current blocking acceptance set。

### CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001 — BLOCKING
Module/Class：`dec-core-starter / ProtectedAccessProductionSeamTest`

Prove no public/protected issued-pair/capability mint、no secondary permission authority、compatibility cannot write/mint、all supported P2 protected access after entry reaches Bridge→Gateway→Guard before operation。

### CASE-P2-TD-GUARD-NO-BYPASS-001 — BLOCKING
Protected operation/effect counter remains 0 unless the same invocation passes Guard。

### CASE-P2-TD-STATIC-ALLOW-GUARD-PATH-001 — BLOCKING
STATIC_ALLOW still performs exact Guard lookup once；runtime verifier/evaluator count=0。

### CASE-P2-TD-DIRECT-BRIDGE-REACHABILITY-001 — BLOCKING
Cross-module public production Bridge reachability；no reflection/package-private/manual issued pair。

### CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001 — BLOCKING
Module/Class：`dec-core-starter / RuleProtectedAccessConsumerIntegrationTest`

Use real main-source `RuleProtectedAccessEntry` + immutable `ProtectedAccessInvocation` + production Bridge composition。

Authorized oracle：
- entry=1；Bridge=1；Gateway=1；Guard exact lookup=1；
- capability-bound operation=1；expected effect=1；
- no direct Gateway/Guard/operation call from the test or consumer。

Unauthorized oracle：
- entry=1；Bridge=1；
- stable DENY for exact missing/unauthorized rule/op；
- operation=0；effect=0；
- no fallback to alternate rule/op/consumer permission。

### CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001 — BLOCKING
Module/Class：`dec-core-starter / ChangeProtectedAccessConsumerIntegrationTest`

Same production rules/oracles using real main-source `ChangeProtectedAccessEntry`。Authorized change effect occurs only after Guard；unauthorized path mutates nothing。

### CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001 — BLOCKING
Module/Class：`dec-core-starter / CustomActionProtectedAccessConsumerIntegrationTest`

Same production rules/oracles using real main-source `CustomActionProtectedAccessEntry`。Authorized action effect occurs only after Guard；unauthorized path effects=0。

### CASE-P2-TD-AC007-CONSUMER-PARITY-001 — BLOCKING
Module/Class：`dec-core-starter / ProtectedAccessConsumerParityTest`

Given the same immutable Context + same exact `ProtectedAccessInvocation` + same runtime target facts, invoke Rule/change/custom-action entries independently。

For an authorized policy：all three return the same authorization classification and each independent invocation has one Guard lookup/one bound effect。

For an unauthorized policy：all three return the same stable authorization denial class/code and effects=0。

Assert consumer category is absent from authorization key/equality semantics and cannot upgrade/downgrade READ/WRITE/EXECUTE or reselect rule/target。

### CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001 — BLOCKING
Module/Class：`dec-core-starter / ProtectedAccessRepresentativeConsumerStructureTest`

Inspect production API/dependency shape：
- real classes exist under main source, not only tests；
- constructors accept/bind `ProtectedExecutionBridge` as the protected authority dependency；
- no constructor/field/service-locator dependency on Gateway、Guard、target resolver、raw operation port、mutable/secondary PolicyIndex、issued-pair/capability mint；
- executable path tests do not use reflection to invoke internals。

### CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001 — BLOCKING
Module/Class：`dec-demo / P2Ac007RepresentativeConsumersRealFixtureTest`

Use real P2 source/fixtures -> production compiler -> immutable Context/PolicyIndex -> normal public production construction of all three representative entry classes -> each executes one authorized and one unauthorized case。

Forbidden shortcuts：hand-built PolicyIndex、manual issued pair/capability、test-local consumer substitute、reflection/package-private internal call。

Oracle：all three entries reach the same protected authority seam；authorized effects occur after Guard；unauthorized effects=0；same authorization facts produce parity across entry categories。

### AC-007 downstream non-scope

This P2 acceptance does not require P3 full Information/Rule engine, P4 full Action/Produce state machine, or P6 QueryPlan execution。Those stages remain downstream but must reuse P2 seam and may not establish bypass authority。

## 10. AC-008 atomic publication / Context isolation

### CASE-P2-TD-ATOMIC-PUBLICATION-001 — BLOCKING
Any ownership/ref/path/conversion/policy ERROR -> publication=0, old Context unchanged；valid candidate publishes all same closure。

### CASE-P2-TD-CONTEXT-ISOLATION-001 — BLOCKING
No shared mutable snapshots/registry/current Context。

### CASE-P2-TD-POLICY-INDEX-PUBLICATION-001 — BLOCKING
Index before digest；same immutable index/snapshot through DigestBoundCompiledInput and published set。

### CASE-P2-TD-POLICY-PUBLICATION-COMPATIBILITY-001 — BLOCKING
Legacy 8-arg constructor remains -> empty PolicyIndex/no reconstruction；new published path retains supplied authority。

## 11. AC-009 deterministic compile/runtime denial

### CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001 — BLOCKING
Repeat duplicate System/ownership mismatch/RuleView missing/unknown View-Rule/path/conversion/static access -> same ordered code/identity/SourceRef。

### CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001 — BLOCKING
Repeat POLICY_NOT_FOUND/RUNTIME_BINDING_STALE/RUNTIME_PLAN_MISMATCH/TARGET_SUBSTITUTION/GUARD_UNAVAILABLE -> same code/System/optional RuleView/op/P2 ModelPath/policy SourceRef；effects=0；no sensitive values。For same authorization facts across Rule/change/custom-action entries, authorization denial classification/code remains equal。

## 12. AC-010 declaration boundary

### CASE-P2-TD-DECLARATION-BOUNDARY-001 — BLOCKING
`dec-expand-declaration` retired；surviving compatibility read-only/no second runtime authority；P7 deletion conditions traceable。

## 13. Runtime remediation retained

### CASE-P2-POLICY-INDEX-CONSTRUCTION-001-R18 — BLOCKING
Index duplicate/null/key/status/plan/wildcard validation, deterministic immutable exact find。

### CASE-P2-DIRECT-BRIDGE-CONTRACT-001-R18 — BLOCKING
Direct bridge/no token/op mismatch/policy miss fail closed；valid alternate rule/op selection allowed by ACTIVE user decision。

### CASE-P2-DIRECT-BRIDGE-CONCURRENCY-001-R18 — BLOCKING
Independent invocations do not cross-wire；identical args are independent；representative entry objects/Bridge hold no mutable per-invocation authority state；same capability <=1 terminal success。

### CASE-P2-OPERATION-BINDING-001-R18 — BLOCKING
Capability A cannot target/op substitute B。

## 14. Review Gate

- Requirement overlay R03 exact Review required；
- BM-R15 / FLOW-R05 / Design R17 exact Reviews required before TestDesign can pass；
- AC-007 Option B decision itself is no longer blocking, but all new AC-007 cases are only Design artifacts until TDD/execution is legal；
- risk/lifecycle required；
- no Case is execution Evidence yet；
- Implementation Plan/TDD/Development remain BLOCKED。
