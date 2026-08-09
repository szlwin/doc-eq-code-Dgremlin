# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R19`  
> Base：`TESTDESIGN-P2-R18`  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R04` + `BM-R16` + `DESIGN-P2-R18` + `FLOW-R06@p2-system-ruleview-protected-access`  
> Decisions：AC-007 Option B ACTIVE；AccessOperation READ/WRITE-only ACTIVE  
> Status：`NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`

R19 保留 R18 已恢复的 System/RuleView/ModelPath/PolicyIndex/runtime cases，移除 current P2 EXECUTE acceptance，并补 RuleKey/API closure、production composition acquisition、atomic capability concurrency 与 BM canonical-pair consistency cases。本文仍只做 Test Design，不创建 TDD skeleton、不执行测试。

## 1. Formal RED

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

第二条禁止 `-am`。Missing TestClass/symbol/setup/compile failure before intended assertion = `INVALID_RED`。

## 2. Planned TestClass map

### dec-core-context
- `SystemOwnershipSnapshotContractTest`
- `RuleViewCompiledRelationContractTest`
- `RuleKeyContractTest`
- `P2KeySourceCompatibilityTest`
- `ModelAccessPolicyIndexContractTest`
- `ModelAccessPolicyPublicationCompatibilityTest`
- `ReadWriteAccessOperationContractTest`

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
- `ProtectedAccessProductionCompositionTest`
- `RuleProtectedAccessConsumerIntegrationTest`
- `ChangeProtectedAccessConsumerIntegrationTest`
- `CustomActionProtectedAccessConsumerIntegrationTest`
- `ProtectedAccessConsumerParityTest`
- `ProtectedAccessRepresentativeConsumerStructureTest`
- `ProtectedAccessCapabilityConcurrencyTest`
- `RuntimeBindingProofIntegrationTest`
- `ProtectedAccessOperationBindingTest`
- `RuntimeDenialDiagnosticDeterminismTest`

### dec-demo
- `P2SystemOwnershipRealFixtureTest`
- `P2RuleViewCompositeRealFixtureTest`
- `P2DynamicClassifierRealFixtureTest`
- `P2Ac007RepresentativeConsumersRealFixtureTest`
- `P2SourceToReadWriteOperationTest`

## 3. AC-001 System

### CASE-P2-TD-SYSTEM-DETERMINISM-001 — BLOCKING
Same semantic sources in different discovery order -> same System order/ownership/version/digest/diagnostic。

### CASE-P2-TD-SYSTEM-DUPLICATE-001 — BLOCKING
Duplicate SystemKey -> stable ERROR, publication=0, old Context unchanged。

### CASE-P2-TD-SYSTEM-FORWARD-REF-001 — BLOCKING
All System symbols registered before owner-qualified reference resolution。

### CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001 — BLOCKING
Compare snapshot with exact typed registries + CompiledRuleView RuleKey closure + PolicyIndex keys。Orphan/missing/foreign projection invalidates candidate。

### CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001 — BLOCKING
Declared version exact/empty；source semantic digest deterministic；schemaVersion/compilerVersion equal enclosing set；no time/random identity。

### CASE-P2-TD-BM-CANONICAL-PAIR-001 — BLOCKING REVIEW/CONTRACT
BM YAML and MD declare same BM-R16 inputs and same semantic invariants for operation set, RuleKey identity/store, AC-007 production composition, capability concurrency。Semantic mismatch invalid candidate。

## 4. AC-002 RuleView / RuleKey

### CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001 — BLOCKING
Missing System stable ERROR。

### CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001 — BLOCKING
Same `(System,name)` duplicate stable ERROR。

### CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001 — BLOCKING
Same local name under different Systems coexists/is isolated。

### CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001 — BLOCKING
Resolved View/Rule refs exact and owner compatible；unknown/wrong owner stable ERROR。

### CASE-P2-TD-RULEKEY-CONTRACT-001 — BLOCKING
Given two RuleViews containing same local Rule name：RuleKeys differ by owner。Within one RuleView duplicate `(ownerRuleViewKey,localRuleName)` rejects。Every `resolvedRuleKeys` owner equals that RuleView key。No global Rule registry is required/consulted for ownership projection。

## 5. AC-003 composite lookup / compatibility

### CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001 — BLOCKING
Exact owner-qualified lookup only；wrong System no global fallback。

### CASE-P2-TD-KEY-SOURCE-COMPAT-001 — BLOCKING
Existing `SystemKey(String)/name()` and `RuleViewKey(SystemKey,String)/owner()/name()` external source compiles unchanged。

### CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001 — BLOCKING
P2 canonical resolver exposes no new bare-name adapter/fallback。If a historical read path remains, ambiguous same-name rejects and it cannot mutate Registry/Policy or perform protected WRITE。

## 6. AC-004 READ / WRITE only

### CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001 — BLOCKING
For identical System/target/path：

```text
READ-only policy:  READ eligible, WRITE DENY
WRITE-only policy: WRITE eligible, READ DENY
no policy:         READ DENY, WRITE DENY
```

No `hasAnyPermission(path)` shortcut。

### CASE-P2-TD-NO-EXECUTE-CONTRACT-001 — BLOCKING CONTRACT
Assert current `AccessOperation` public enum/contract contains exactly READ and WRITE；there is no P2 source/raw/policy/Bridge/Guard EXECUTE path。Historical R01 EXECUTE wording is not current candidate behavior because `DEC-P2-ACCESS-OPERATIONS-001` is ACTIVE。

### CASE-P2-TD-STATIC-DENY-001 — BLOCKING
Static undeclared READ/WRITE never defaults allow。

## 7. AC-005 ModelPath / P1 migration

### CASE-P2-TD-MODEL-PATH-UNKNOWN-001 — BLOCKING
Unknown/case mismatch/parent/fuzzy fallback stable ERROR across consumers。

### CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001 — BLOCKING
Legal source `*` expands deterministically before policy publication；runtime wildcard count=0。

### CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001 — BLOCKING
Equivalent rule-data/change-data/query-contract/model-access path -> value-equal ModelPath and same invalid-path diagnostic family。

### CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001 — BLOCKING
SharedModelPath exact/wildcard converts once；AccessMode.READ/WRITE maps exactly to AccessOperation.READ/WRITE；after conversion PolicyIndex/Bridge/Guard never query P1 types as authority；no dual-authority fallback。

## 8. AC-006 dynamic runtime

### CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001 — BLOCKING
Real fixture static exact vs collection element dynamic classification remains deterministic；unsupported selector ERROR。

### CASE-P2-TD-RUNTIME-BINDING-PROOF-001 — BLOCKING
Valid membership may proceed；foreign/stale/wrong membership DENY before effects。

### CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001 — BLOCKING
Exact rule but wrong/stale plan DENY；no rule reselection。

### CASE-P2-SOURCE-TO-READ-WRITE-OPERATION-001-R19 — BLOCKING
Real source -> parser/raw -> compiler -> exact READ/WRITE rule -> PolicyIndex -> immutable Context -> production Bridge -> Guard -> READ or WRITE operation。No hand-built policy/manual capability。

## 9. AC-007 Option B — blocking set

### CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001 — BLOCKING
No public/protected issued-pair/capability mint、secondary permission map or compatibility authority bypass。

### CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001 — BLOCKING
Use normal starter `ProtectedAccessRuntimeFactory.bind(realEngineContext)` to obtain `ProtectedAccessComposition`。

Oracle：
- non-null `bridge/ruleEntry/changeEntry/customActionEntry`；
- all three entries use exact same Bridge instance and same EngineContext authority snapshot；
- production E2E obtains entries from composition, not `new Entry(testBridge)`；
- caller cannot acquire Gateway/Guard/resolver/raw operation/mutable PolicyIndex/capability mint from composition。

### CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001 — BLOCKING
Acquire Rule entry from production composition。Authorized READ/WRITE path -> Bridge/Gateway/Guard then exactly one expected effect；unauthorized -> stable DENY, operation/effect=0。

### CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001 — BLOCKING
Same via production composition Change entry；unauthorized mutation count=0。

### CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001 — BLOCKING
Same via production composition CustomAction entry；unauthorized effect count=0。

### CASE-P2-TD-AC007-CONSUMER-PARITY-001 — BLOCKING
Same Context + exact invocation + runtime target facts across three entries -> same authorization classification/code；consumer category cannot alter READ/WRITE/key/target selection。

### CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001 — BLOCKING
Main-source classes exist；composition-controlled Entry construction；no protected authority dependency except Bridge。

### CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001 — BLOCKING
`dec-demo` real fixture -> compiler -> EngineContext -> normal starter factory/composition -> all three main-source entries -> allow/deny。Forbidden: hand-built PolicyIndex, manual issued pair/capability, test-local consumer substitute, reflection/package-private internal invocation。

## 10. AC-008 atomic publication

### CASE-P2-TD-ATOMIC-PUBLICATION-001 — BLOCKING
Any ownership/ref/path/read-write policy ERROR -> publication=0, old Context unchanged。

### CASE-P2-TD-CONTEXT-ISOLATION-001 — BLOCKING
No shared mutable snapshots/current Context。

### CASE-P2-TD-POLICY-INDEX-PUBLICATION-001 — BLOCKING
Index before digest；same immutable authority through published set/context。

## 11. AC-009 deterministic diagnostic/runtime denial

### CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001 — BLOCKING
Repeat compile error -> same ordered code/identity/SourceRef。

### CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001 — BLOCKING
Repeat POLICY_NOT_FOUND/RUNTIME_BINDING_STALE/RUNTIME_PLAN_MISMATCH/TARGET_SUBSTITUTION/GUARD_UNAVAILABLE/CAPABILITY_ALREADY_CONSUMED -> same code/System/optional RuleView/READ-or-WRITE/ModelPath/policy SourceRef；effect=0；no sensitive values。

## 12. One-shot concurrency — BLOCKING

### CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001
Create one real starter-internal capability through controlled production seam, then use latch/barrier to release two consumers simultaneously。

Oracle：
- `ISSUED -> CONSUMED` transition success <=1；
- operation/effect count exactly <=1 and for authorized case exactly 1 overall；
- losing invocation stable `CAPABILITY_ALREADY_CONSUMED`；loser operation/effect=0；
- later reuse same denial；
- no `Thread.sleep` correctness oracle。

### CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001
Different invocation/capability objects execute concurrently without frame/owner/cursor/target/operation cross-wire。

## 13. AC-010 declaration boundary

### CASE-P2-TD-DECLARATION-BOUNDARY-001 — BLOCKING
Retired module not restored；surviving compatibility read-only；P7 deletion traceable。

## 14. Review Gate

- Requirement overlay R04 exact Review required；
- BM-R16/FLOW-R06/DESIGN-P2-R18 exact Reviews required before TestDesign pass；
- risk/lifecycle required；
- no Case is execution Evidence yet；
- Implementation Plan/TDD/Development remain BLOCKED。
