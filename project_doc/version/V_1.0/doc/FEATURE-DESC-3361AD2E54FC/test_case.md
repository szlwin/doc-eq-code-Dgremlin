# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R17`  
> Base：`TESTDESIGN-P2-R16`  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R02` + `BM-R14` + `DESIGN-P2-R16` + `FLOW-R04@p2-system-ruleview-protected-access`  
> Status：`NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED / AC007_BLOCKED_BY_USER_DECISION`

R17 保留 R16 已完整恢复的 AC-001～AC-010 Case。新增/强化内容仅针对本轮 Review：SystemVersion compiler compatibility、ownership authoritative source、existing key source compatibility、P1→P2 path/operation migration；AC-007 不再把 seam-only 当作 final acceptance。

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
- `RuntimeDenialDiagnosticDeterminismTest`

### dec-demo
- `P2SystemOwnershipRealFixtureTest`
- `P2RuleViewCompositeRealFixtureTest`
- `P2DynamicClassifierRealFixtureTest`
- `P2DirectBridgeReachabilityTest`
- `P2DynamicSourceToOperationTest`

## 3. AC-001 System deterministic compile / ownership / version

### CASE-P2-TD-SYSTEM-DETERMINISM-001 — BLOCKING
Same semantic inputs different discovery order -> same ordered SystemKey, ownership snapshots, digest, diagnostics。

### CASE-P2-TD-SYSTEM-DUPLICATE-001 — BLOCKING
Duplicate exact SystemKey -> stable ERROR, publication=0, old Context retained。

### CASE-P2-TD-SYSTEM-FORWARD-REF-001 — BLOCKING
Register all System symbols before owner-qualified resolution；legal forward refs order-independent。

### CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001 — BLOCKING
Assert snapshot against its authoritative sources, **not against a hypothetical all-purpose typed registry**：
- Data/View/RuleView/Information -> corresponding final typed registries；
- Rule -> final CompiledRuleView rule closure；
- ModelAccess -> final PolicyIndex keys/compiled policy rules。

Negative：orphan/missing/foreign key、snapshot mutation、snapshot used to rebuild authority -> candidate invalid/no publication。

### CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001 — BLOCKING
Oracle：
- declared version exact when present, empty when absent；
- source semantic content change changes sourceSemanticDigest；source order only does not；
- `schemaVersion == CompiledModelSet.schemaVersion`；
- `compilerVersion == CompiledModelSet.compilerVersion`；
- compilerVersion change participates in semantic identity/digest；
- no timestamp/random fabricated version；options digest remains enclosing compile identity。

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
Real `save-Order`/current equivalent proves `system=order`, `view-ref=OrderInfo`, rule refs from production compiler output。

## 5. AC-003 composite lookup / source compatibility

### CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001 — BLOCKING
Exact owner-qualified lookup only；wrong System no global fallback。

### CASE-P2-TD-RULEVIEW-BARE-NAME-REJECT-001 — BLOCKING
No new production bare-name lookup。

### CASE-P2-TD-LEGACY-NO-NEW-BARE-FALLBACK-001 — BLOCKING
Legacy adapter read-only/no registry-policy write/no protected mutation bypass。

### CASE-P2-TD-KEY-SOURCE-COMPAT-001 — BLOCKING
Compile an external test source using existing public surfaces:
```java
new SystemKey("order").name();
new RuleViewKey(new SystemKey("order"), "save-Order").owner();
new RuleViewKey(new SystemKey("order"), "save-Order").name();
```
Must compile/run unchanged. If additive `of/systemKey/localName/value` exist, aliases must equal existing values. Removal/rename is failure。

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
Equivalent rule-data/change-data/QUERY_CONTRACT/model-access `status` -> value-equal P2 ModelPath and same invalid-path family。Query stops at P2 compile/IR boundary。

### CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001 — BLOCKING
Production migration contract:
- exact P1 `SharedModelPath` -> exact P2 `ModelPath`；
- wildcard P1 path -> finite exact P2 paths；
- `AccessMode.READ -> AccessOperation.READ`；
- `AccessMode.WRITE -> AccessOperation.WRITE`；
- no P1 input may infer EXECUTE；
- after conversion PolicyIndex/Bridge/Guard never query `SharedModelPath` or `AccessMode` as authority；
- no dual-authority fallback chooses broader result。

## 8. AC-006 dynamic access / runtime flow

### CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001 — BLOCKING
Real fixture `status=1 -> STATIC_BOUND/STATIC_ALLOW`; `every(orderDetailList,status=1) -> RUNTIME_OBJECT_BOUND/RUNTIME_GUARD_REQUIRED`; unsupported selector ERROR。

### CASE-P2-TD-RUNTIME-BINDING-PROOF-001 — BLOCKING
Valid membership can proceed；foreign/stale/wrong membership DENY before effects。

### CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001 — BLOCKING
Exact rule but wrong/stale plan -> DENY, no rule reselection。

### CASE-P2-SOURCE-TO-OPERATION-001-R17 — BLOCKING
Real source -> compile flow -> Context -> public Bridge -> runtime protected flow -> Guard -> operation；no hand-built policy/manual issued pair。

## 9. AC-007 — BLOCKED_BY_USER_DECISION

`DEC-P2-AC007-STAGE-BOUNDARY-001 = PROPOSED / PENDING_USER_DECISION`。因此本节不能成为 final AC closure。

### CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001 — BLOCKING FOUNDATION
Prove common foundation: no public/protected issued-pair/capability mint、no secondary authority、compatibility cannot write/mint、existing supported P2 protected access traverses Bridge→Gateway→Guard。

### CASE-P2-TD-GUARD-NO-BYPASS-001 — BLOCKING FOUNDATION
P2 existing supported protected operation/effect counter remains 0 unless same invocation passes Guard。

### CASE-P2-TD-STATIC-ALLOW-GUARD-PATH-001 — BLOCKING FOUNDATION
STATIC_ALLOW still exact Guard lookup once；runtime verifier=0。

### CASE-P2-TD-DIRECT-BRIDGE-REACHABILITY-001 — BLOCKING FOUNDATION
Cross-module public production Bridge reachability；no reflection/package-private/manual issued pair。

### Pending branches
- If user chooses **A**：上述 foundation + BusinessFlow/API/dependency no-bypass oracles form P2 AC-007 test set；P3/P4/P6 concrete integration becomes downstream acceptance。
- If user chooses **B**：R17 must be revised again with exact representative production Rule/change/custom-action consumer TestClasses/fixtures/oracles before TestDesign can pass。

Current status：`PENDING_USER_DECISION`，not COVERED/PASSED。

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
Repeat POLICY_NOT_FOUND/RUNTIME_BINDING_STALE/RUNTIME_PLAN_MISMATCH/TARGET_SUBSTITUTION/GUARD_UNAVAILABLE -> same code/System/optional RuleView/op/P2 ModelPath/policy SourceRef；effects=0；no sensitive values。

## 12. AC-010 declaration boundary

### CASE-P2-TD-DECLARATION-BOUNDARY-001 — BLOCKING
`dec-expand-declaration` retired；surviving compatibility read-only/no second runtime authority；P7 deletion conditions traceable。

## 13. Runtime remediation retained

### CASE-P2-POLICY-INDEX-CONSTRUCTION-001-R17 — BLOCKING
Index duplicate/null/key/status/plan/wildcard validation, deterministic immutable exact find。

### CASE-P2-DIRECT-BRIDGE-CONTRACT-001-R17 — BLOCKING
Direct bridge/no token/op mismatch/policy miss fail closed；valid alternate rule/op selection allowed by ACTIVE user decision。

### CASE-P2-DIRECT-BRIDGE-CONCURRENCY-001-R17 — BLOCKING
Independent invocations do not cross-wire；identical args are independent；same capability <=1 terminal success。

### CASE-P2-OPERATION-BINDING-001-R17 — BLOCKING
Capability A cannot target/op substitute B。

## 14. Review Gate

- Design R16 exact Review before TestDesign pass；
- AC-007 explicit user decision before AC-007 case set can be frozen；
- risk/lifecycle required；
- no Case is execution Evidence yet；
- Implementation Plan/TDD/Development remain BLOCKED。
