# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R23`
> Base：`TESTDESIGN-P2-R22`
> Inputs：`REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R20` + `FLOW-R10@p2-system-ruleview-protected-access` + `P2-IMPACT-R22` + `DESIGN-P2-R22`
> Decisions：AC-007 Option B ACTIVE；AccessOperation READ/WRITE-only ACTIVE
> Status：`NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`

R23 preserves the exact 19-TestClass RED registry from R22, adds four blockers exposed by the independent Review, and makes every current blocking Case self-contained. Each case states fixture/preconditions, action, expected observable result, forbidden side effects and current flow/failure reference. No superseded TestDesign is needed as a behavioral oracle; no TDD execution is claimed.

## 1. Formal RED rules

- Bootstrap MAY use `-am` and `-Dmaven.test.skip=true install`.
- Target RED MUST NOT use `-am`.
- Target command MUST use `-Dsurefire.failIfNoSpecifiedTests=true`.
- Missing TestClass/symbol/setup or compilation failure before the intended assertion is `INVALID_RED`.
- A registry entry is planning metadata only until the exact target RED has actually been executed under TDD gate.

## 2. Exact TestClass registry

### `DAG` — `P2RevisionDependencyDagContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/contract/P2RevisionDependencyDagContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=P2RevisionDependencyDagContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `SYSTEM` — `SystemCompilationContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/system/SystemCompilationContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=SystemCompilationContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `RULEVIEW` — `RuleViewCompilationContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/ruleview/RuleViewCompilationContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=RuleViewCompilationContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `TARGET` — `TargetKeyModelPathContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/model/access/TargetKeyModelPathContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=TargetKeyModelPathContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `POLICY` — `ModelAccessPolicyContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/model/access/ModelAccessPolicyContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=ModelAccessPolicyContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `API` — `ProtectedAccessCurrentApiContractTest`
- Module: `dec-core-context`
- Planned source: `dec-core-context/src/test/java/dec/core/context/runtime/ProtectedAccessCurrentApiContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-context -Dtest=ProtectedAccessCurrentApiContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `VALUE` — `RuntimeFactValueContractTest`
- Module: `dec-core-context`
- Planned source: `dec-core-context/src/test/java/dec/core/context/runtime/RuntimeFactValueContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-context -Dtest=RuntimeFactValueContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `ID` — `OpaqueRuntimeIdContractTest`
- Module: `dec-core-context`
- Planned source: `dec-core-context/src/test/java/dec/core/context/runtime/OpaqueRuntimeIdContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-context -Dtest=OpaqueRuntimeIdContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `INTENT` — `ProtectedWriteIntentResolutionTest`
- Module: `dec-core-starter`
- Planned source: `dec-core-starter/src/test/java/dec/core/starter/access/ProtectedWriteIntentResolutionTest.java`
- Bootstrap: `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-starter -Dtest=ProtectedWriteIntentResolutionTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `ADAPTER` — `ProtectedRuntimeModelAdapterIntegrationTest`
- Module: `dec-core-starter`
- Planned source: `dec-core-starter/src/test/java/dec/core/starter/access/ProtectedRuntimeModelAdapterIntegrationTest.java`
- Bootstrap: `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-starter -Dtest=ProtectedRuntimeModelAdapterIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `LOCATOR` — `RuntimeObjectLocatorIntegrationTest`
- Module: `dec-core-model`
- Planned source: `dec-core-model/src/test/java/dec/core/model/runtime/RuntimeObjectLocatorIntegrationTest.java`
- Bootstrap: `./mvnw -pl dec-core-model -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-model -Dtest=RuntimeObjectLocatorIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `TXN` — `ProtectedWriteTransactionIntegrationTest`
- Module: `dec-core-model`
- Planned source: `dec-core-model/src/test/java/dec/core/model/runtime/ProtectedWriteTransactionIntegrationTest.java`
- Bootstrap: `./mvnw -pl dec-core-model -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-model -Dtest=ProtectedWriteTransactionIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `COMPOSE` — `ProtectedAccessProductionCompositionTest`
- Module: `dec-core-starter`
- Planned source: `dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessProductionCompositionTest.java`
- Bootstrap: `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-starter -Dtest=ProtectedAccessProductionCompositionTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `CONC` — `ProtectedAccessConcurrencyTest`
- Module: `dec-core-starter`
- Planned source: `dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessConcurrencyTest.java`
- Bootstrap: `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-starter -Dtest=ProtectedAccessConcurrencyTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `DEP` — `ProtectedAccessDependencyDirectionTest`
- Module: `dec-core-starter`
- Planned source: `dec-core-starter/src/test/java/dec/core/starter/architecture/ProtectedAccessDependencyDirectionTest.java`
- Bootstrap: `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-starter -Dtest=ProtectedAccessDependencyDirectionTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `PUB` — `AtomicPublicationContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/publication/AtomicPublicationContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=AtomicPublicationContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `DIAG` — `P2DiagnosticDeterminismTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/diagnostic/P2DiagnosticDeterminismTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=P2DiagnosticDeterminismTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `FIXTURE` — `P2RealFixtureIntegrationTest`
- Module: `dec-demo`
- Planned source: `dec-demo/src/test/java/dec/demo/p2/P2RealFixtureIntegrationTest.java`
- Bootstrap: `./mvnw -pl dec-demo -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-demo -Dtest=P2RealFixtureIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `COMPAT` — `P2DeclarationCompatibilityContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/compat/P2DeclarationCompatibilityContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=P2DeclarationCompatibilityContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

## 3. Complete blocking Case map

Every blocking Case maps to the registry entry after `=>`. R23 has **68 blocking Case IDs -> 19 exact TestClasses**.

### `DAG` => `P2RevisionDependencyDagContractTest` (1 case)
- `CASE-P2-TD-REVISION-DAG-001`

### `SYSTEM` => `SystemCompilationContractTest` (6 cases)
- `CASE-P2-TD-SYSTEM-DETERMINISM-001`
- `CASE-P2-TD-SYSTEM-DUPLICATE-001`
- `CASE-P2-TD-SYSTEM-FORWARD-REF-001`
- `CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001`
- `CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001`
- `CASE-P2-TD-BM-CANONICAL-PAIR-001`

### `RULEVIEW` => `RuleViewCompilationContractTest` (8 cases)
- `CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001`
- `CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001`
- `CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001`
- `CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001`
- `CASE-P2-TD-RULEKEY-CONTRACT-001`
- `CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001`
- `CASE-P2-TD-KEY-SOURCE-COMPAT-001`
- `CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001`

### `TARGET` => `TargetKeyModelPathContractTest` (6 cases)
- `CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001`
- `CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001`
- `CASE-P2-TD-MODEL-PATH-UNKNOWN-001`
- `CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001`
- `CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001`
- `CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001`

### `POLICY` => `ModelAccessPolicyContractTest` (7 cases)
- `CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001`
- `CASE-P2-TD-NO-EXECUTE-CONTRACT-001`
- `CASE-P2-TD-STATIC-DENY-001`
- `CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001`
- `CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001`
- `CASE-P2-TD-RUNTIME-BINDING-PROOF-001`
- `CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001`

### `API` => `ProtectedAccessCurrentApiContractTest` (1 case)
- `CASE-P2-TD-CURRENT-API-SELF-CONTAINED-001`

### `VALUE` => `RuntimeFactValueContractTest` (2 cases)
- `CASE-P2-TD-RUNTIME-FACT-VALUE-DOMAIN-001`
- `CASE-P2-TD-RUNTIME-FACT-VALUE-DEEP-IMMUTABILITY-001`

### `ID` => `OpaqueRuntimeIdContractTest` (1 case)
- `CASE-P2-TD-OPAQUE-RUNTIME-ID-VALUE-CONTRACT-001`

### `INTENT` => `ProtectedWriteIntentResolutionTest` (7 cases)
- `CASE-P2-TD-WRITE-INTENT-NOT-FOUND-001`
- `CASE-P2-TD-WRITE-INTENT-AMBIGUOUS-001`
- `CASE-P2-TD-WRITE-INTENT-FREEZE-STABILITY-001`
- `CASE-P2-TD-WRITE-AUTHORITY-MODEL-ACCESS-RULEKEY-001`
- `CASE-P2-TD-WRITE-SINGLE-PATH-AUTHORITY-001`
- `CASE-P2-TD-TYPED-RUNTIME-CONTEXT-001`
- `CASE-P2-TD-MUTATION-STAMP-OBJECT-BINDING-001`

### `ADAPTER` => `ProtectedRuntimeModelAdapterIntegrationTest` (4 cases)
- `CASE-P2-TD-REAL-READ-OPERATION-001`
- `CASE-P2-TD-REAL-WRITE-OPERATION-001`
- `CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001`
- `CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001`

### `LOCATOR` => `RuntimeObjectLocatorIntegrationTest` (3 cases)
- `CASE-P2-TD-RUNTIME-OBJECT-LOCATOR-SCOPE-001`
- `CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001`
- `CASE-P2-TD-RUNTIME-TARGET-SELECTION-001`

### `TXN` => `ProtectedWriteTransactionIntegrationTest` (1 case)
- `CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001`

### `COMPOSE` => `ProtectedAccessProductionCompositionTest` (9 cases)
- `CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001`
- `CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001`
- `CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CONSUMER-PARITY-001`
- `CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001`
- `CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001`
- `CASE-P2-TD-COMPOSITION-RUNTIME-CONTEXT-MATCH-001`

### `CONC` => `ProtectedAccessConcurrencyTest` (3 cases)
- `CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001`
- `CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001`
- `CASE-P2-TD-CROSS-SESSION-MODELDATA-OWNERSHIP-001`

### `DEP` => `ProtectedAccessDependencyDirectionTest` (1 case)
- `CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001`

### `PUB` => `AtomicPublicationContractTest` (3 cases)
- `CASE-P2-TD-ATOMIC-PUBLICATION-001`
- `CASE-P2-TD-CONTEXT-ISOLATION-001`
- `CASE-P2-TD-POLICY-INDEX-PUBLICATION-001`

### `DIAG` => `P2DiagnosticDeterminismTest` (2 cases)
- `CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001`
- `CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001`

### `FIXTURE` => `P2RealFixtureIntegrationTest` (2 cases)
- `CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001`
- `CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001`

### `COMPAT` => `P2DeclarationCompatibilityContractTest` (1 case)
- `CASE-P2-TD-DECLARATION-BOUNDARY-001`

## 4. Current-revision self-contained behavioral oracles

The following table is normative TestDesign. `Fixture / preconditions` and `Action` define the setup and stimulus; `Expected observable result` and `Forbidden side effects` define the pass/fail oracle.

| Case | Fixture / preconditions | Action | Expected observable result | Forbidden side effects | Current flow / failure refs |
|---|---|---|---|---|---|
| `CASE-P2-TD-REVISION-DAG-001` | Read only current Requirement/Overlay, BM-R20, FLOW-R10, P2-IMPACT-R22, DESIGN-P2-R22 and TESTDESIGN-P2-R23 revision headers/refs. | Resolve every current authoritative input/ref edge. | Exactly REQAN-P2-R01 + Overlay R04 -> BM-R20 -> FLOW-R10 -> P2-IMPACT-R22/DESIGN-P2-R22 -> TESTDESIGN-P2-R23; no downstream-as-upstream cycle. | No R19/R09/R21/R22 current authority remains except explicit base/supersedes/history refs. | revision DAG / no runtime flow |
| `CASE-P2-TD-SYSTEM-DETERMINISM-001` | Compile a minimal current P2 fixture with two Systems and deterministic declaration order permutations. | Compile equivalent System declarations in multiple source orders. | SystemKey set, ownership projection and diagnostics are identical. | No order-dependent System identity or policy facts. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-01 |
| `CASE-P2-TD-SYSTEM-DUPLICATE-001` | Compile a minimal current P2 fixture with two Systems and deterministic declaration order permutations. | Compile duplicate System identity declarations. | Stable duplicate-System compile error and publication=0. | No first/last declaration win. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-01 |
| `CASE-P2-TD-SYSTEM-FORWARD-REF-001` | Compile a minimal current P2 fixture with two Systems and deterministic declaration order permutations. | Compile valid forward references between current System-related declarations. | Reference resolution is deterministic after symbol registration. | No source-order fallback. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-01 |
| `CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001` | Compile a minimal current P2 fixture with two Systems and deterministic declaration order permutations. | Publish a valid candidate then mutate source parse objects. | Published CompiledSystem ownership remains immutable. | No live mutable source collection leaks. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-01 |
| `CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001` | Compile a minimal current P2 fixture with two Systems and deterministic declaration order permutations. | Compile equal semantic inputs and then a semantic change. | Equal inputs preserve deterministic version/digest identity; semantic change changes it. | No nondeterministic timestamp/object-identity contribution. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-01 |
| `CASE-P2-TD-BM-CANONICAL-PAIR-001` | Compile a minimal current P2 fixture with two Systems and deterministic declaration order permutations. | Validate BM-R20 YAML and compare human BM claims to canonical IDs/invariants/errors. | BM-R20 is a complete current snapshot and key human/canonical statements agree. | No omitted BM-R18 fact is assumed inherited only from baseRevision. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-01 |
| `CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001` | Compile Systems plus RuleViews with explicit System ownership and shared View symbols. | Compile a RuleView with missing System owner. | Stable required-owner compile error; publication=0. | No implicit/default System. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-02 |
| `CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001` | Compile Systems plus RuleViews with explicit System ownership and shared View symbols. | Compile duplicate RuleView local identity in the same System. | Stable same-System duplicate RuleView error; publication=0. | No first/last declaration win. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-02 |
| `CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001` | Compile Systems plus RuleViews with explicit System ownership and shared View symbols. | Compile same RuleView local name under two Systems. | Distinct RuleViewKey identities coexist without collision. | No bare local-name authority lookup. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-02 |
| `CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001` | Compile Systems plus RuleViews with explicit System ownership and shared View symbols. | Compile RuleView referencing an existing then missing shared View. | Existing resolves exactly; missing yields stable compile error. | No owner-System-qualified invention for shared source View. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-02 |
| `CASE-P2-TD-RULEKEY-CONTRACT-001` | Compile Systems plus RuleViews with explicit System ownership and shared View symbols. | Construct RuleKey.of(ownerRuleViewKey, localRuleName) equality/hash cases. | Exact case-sensitive value identity is stable. | No runtime permission inference from RuleKey. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-02 |
| `CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001` | Compile Systems plus RuleViews with explicit System ownership and shared View symbols. | Lookup RuleViews by exact composite key across Systems. | Only exact owner+local identity resolves. | No bare-name fallback. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-02 |
| `CASE-P2-TD-KEY-SOURCE-COMPAT-001` | Compile Systems plus RuleViews with explicit System ownership and shared View symbols. | Compile existing P1 key/source callers against current APIs. | Existing stable key/source surfaces remain source compatible. | No forced migration to a new source namespace. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-02 |
| `CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001` | Compile Systems plus RuleViews with explicit System ownership and shared View symbols. | Attempt P2 permission lookup using only a local/bare name. | Lookup is rejected/not found unless exact composite/current key is provided. | No compatibility fallback that widens authority. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-02 |
| `CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001` | Compile a real P1-style model-access sourceModel/sourcePath fixture with shared Views and owner Systems. | Compile two owner Systems authorizing the same shared sourceModel. | TargetKey values are equal shared ViewKey wrappers while ModelAccessRuleKey differs by owner System. | No owner System embedded in TargetKey. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-03 |
| `CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001` | Compile a real P1-style model-access sourceModel/sourcePath fixture with shared Views and owner Systems. | Change sourcePath while holding sourceModel constant, then change sourceModel. | Path changes only ModelPath; sourceModel changes TargetKey. | No path-derived target identity. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-03 |
| `CASE-P2-TD-MODEL-PATH-UNKNOWN-001` | Compile a real P1-style model-access sourceModel/sourcePath fixture with shared Views and owner Systems. | Compile an unknown sourcePath segment. | Stable source-aware path compile error; publication=0. | No runtime wildcard/name search fallback. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-03 |
| `CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001` | Compile a real P1-style model-access sourceModel/sourcePath fixture with shared Views and owner Systems. | Compile a finite wildcard source path. | Compiler expands to a finite exact set of ModelPath values before publication. | No wildcard reaches runtime. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-03 |
| `CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001` | Compile a real P1-style model-access sourceModel/sourcePath fixture with shared Views and owner Systems. | Compare exact ModelPath observed by compiler, Guard and operation adapter. | All consumers observe value-equal canonical path. | No second path representation or renormalization. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-03 |
| `CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001` | Compile a real P1-style model-access sourceModel/sourcePath fixture with shared Views and owner Systems. | Compile existing P1 path declarations into P2 READ/WRITE rules. | Migration produces exact ModelPath and only READ/WRITE operations. | No EXECUTE or path=* runtime branch. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-03 |
| `CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001` | Compile legal and illegal READ/WRITE model-access rules over exact TargetKey/ModelPath facts. | Compile/authorize READ and WRITE independently for the same target/path. | READ and WRITE permissions are independent exact ModelAccessRuleKey entries. | No READ-implies-WRITE or WRITE-implies-READ. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-04 |
| `CASE-P2-TD-NO-EXECUTE-CONTRACT-001` | Compile legal and illegal READ/WRITE model-access rules over exact TargetKey/ModelPath facts. | Search current enum/source/schema/design/test contracts and try to construct EXECUTE. | No P2 EXECUTE source/API/policy/runtime value exists. | No hidden enum/default string EXECUTE. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-04 |
| `CASE-P2-TD-STATIC-DENY-001` | Compile legal and illegal READ/WRITE model-access rules over exact TargetKey/ModelPath facts. | Invoke an access with no exact allowed policy entry. | Deterministic DENY before protected operation. | No allow-by-absence/fallback. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-04 |
| `CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001` | Compile legal and illegal READ/WRITE model-access rules over exact TargetKey/ModelPath facts. | Construct all status/requirement/plan presence combinations. | Only the two frozen legal rows are constructible/publishable. | Runtime never repairs malformed classification. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-04 |
| `CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001` | Compile legal and illegal READ/WRITE model-access rules over exact TargetKey/ModelPath facts. | Compile a dynamic rule and inspect RuntimeBindingPlan. | Plan preserves exact TargetKey/targetView/selector facts required for selection. | No missing selector resolved by runtime guess. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-04 |
| `CASE-P2-TD-RUNTIME-BINDING-PROOF-001` | Compile legal and illegal READ/WRITE model-access rules over exact TargetKey/ModelPath facts. | Resolve one dynamic target then alter frame/owner/cursor/target proof before Guard. | Guard accepts only the exact frozen proof matching plan and target. | No proof recomputation that widens authority. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-04 |
| `CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001` | Compile legal and illegal READ/WRITE model-access rules over exact TargetKey/ModelPath facts. | Use a resolved target/proof that does not satisfy the compiled plan. | RUNTIME_PLAN_MISMATCH/DENY before model effect. | No runtime reclassification. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-04 |
| `CASE-P2-TD-CURRENT-API-SELF-CONTAINED-001` | Compile/reflect only current source against the signatures written in DESIGN-P2-R22; superseded design text is unavailable. | Compile/reflect every P2-added type/factory/result/resolver in DESIGN-P2-R22. | Every cross-module P2 immutable type has an explicit construction surface and all referenced P2 types are defined/current. | Test may not read R19/R20/R21 to discover missing constructors or semantics. | DESIGN-P2-R22 current API contract |
| `CASE-P2-TD-RUNTIME-FACT-VALUE-DOMAIN-001` | Create nested mutable input values, snapshot them to RuntimeFactValue, then mutate original input. | Construct all NULL/BOOLEAN/INTEGER/DECIMAL/STRING/LIST/OBJECT values and unsupported raw objects. | Closed domain, canonical numbers, deterministic equality/hash/json; unsupported arbitrary object has no construction surface. | No raw Object accessor/reference leak. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-06 |
| `CASE-P2-TD-RUNTIME-FACT-VALUE-DEEP-IMMUTABILITY-001` | Create nested mutable input values, snapshot them to RuntimeFactValue, then mutate original input. | Mutate nested source lists/maps after snapshot and attempt to mutate returned collections. | Snapshot is unchanged and returned collections are immutable. | No live nested reference leak. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-06 |
| `CASE-P2-TD-OPAQUE-RUNTIME-ID-VALUE-CONTRACT-001` | Construct every current opaque ID wrapper using null/blank/mixed-case/exact strings. | Construct all runtime ID wrappers with null/blank/case variants. | Null/blank rejected; exact case-sensitive equals/hash/value preserved, including explicit RuntimeModelSessionId. | No permission/scope inference from RuntimeObjectId text. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-02 |
| `CASE-P2-TD-WRITE-INTENT-NOT-FOUND-001` | Create an exact ModelAccessRuleKey, frozen ResolvedRuntimeTarget and controlled write-intent candidates before Guard. | Provide zero WRITE intent candidates for the frozen authority/target. | WRITE_INTENT_NOT_FOUND; capability/Guard/operation=0. | No synthesized default mutation. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-03 |
| `CASE-P2-TD-WRITE-INTENT-AMBIGUOUS-001` | Create an exact ModelAccessRuleKey, frozen ResolvedRuntimeTarget and controlled write-intent candidates before Guard. | Provide two WRITE intent candidates for the same frozen authority/target. | WRITE_INTENT_AMBIGUOUS; capability/Guard/operation=0. | No first/last candidate choice. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-03 |
| `CASE-P2-TD-WRITE-INTENT-FREEZE-STABILITY-001` | Create an exact ModelAccessRuleKey, frozen ResolvedRuntimeTarget and controlled write-intent candidates before Guard. | Freeze one intent then mutate candidate provider/frame/cursor state. | Frozen target/stamp/intent remain unchanged or stale-deny; no reselection. | No post-Guard intent replacement. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-03 |
| `CASE-P2-TD-WRITE-AUTHORITY-MODEL-ACCESS-RULEKEY-001` | Create an exact ModelAccessRuleKey, frozen ResolvedRuntimeTarget and controlled write-intent candidates before Guard. | Invoke Rule/Change/CustomAction paths with same ModelAccessRuleKey and optional RuleKey provenance. | Authorization depends only on exact ModelAccessRuleKey; RuleKey may be absent. | No RuleKey-based permission authority. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-03 |
| `CASE-P2-TD-WRITE-SINGLE-PATH-AUTHORITY-001` | Create an exact ModelAccessRuleKey, frozen ResolvedRuntimeTarget and controlled write-intent candidates before Guard. | Reflect ResolvedWriteIntent/ResolvedProtectedWriteAccess/operation port signatures. | Only key.modelPath/stamp.modelPath (required equal) represent WRITE path; port has no extra path argument. | No independently supplied second ModelPath. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-03 |
| `CASE-P2-TD-TYPED-RUNTIME-CONTEXT-001` | Create an exact ModelAccessRuleKey, frozen ResolvedRuntimeTarget and controlled write-intent candidates before Guard. | Carry frame/owner/optional cursor from invocation through target/intent. | Typed wrappers remain exact end-to-end. | No raw String/null/N-A cursor sentinel. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-03 |
| `CASE-P2-TD-MUTATION-STAMP-OBJECT-BINDING-001` | Create an exact ModelAccessRuleKey, frozen ResolvedRuntimeTarget and controlled write-intent candidates before Guard. | Attempt to build intent from target object A and stamp session/object/path/version from B or a different path. | Construction rejects mismatch; valid stamp exactly equals target session/object and key path. | No version proof borrowed from another runtime object/path. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-03 |
| `CASE-P2-TD-REAL-READ-OPERATION-001` | Acquire normal production starter composition over explicit EngineContext + RuntimeExecutionFrameSnapshot + real ModelData. | ALLOW READ through normal production composition and mutate source after return. | Value equals actual ModelData/path snapshot; write/version count=0; snapshot remains immutable. | No fake-only reachability or mutation. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-06 |
| `CASE-P2-TD-REAL-WRITE-OPERATION-001` | Acquire normal production starter composition over explicit EngineContext + RuntimeExecutionFrameSnapshot + real ModelData. | ALLOW WRITE through normal production composition with one valid frozen target/stamp. | Actual ModelData/path mutates exactly once, version increments once, receipt binds same intent. | No effect before Guard or second target/path. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-06 |
| `CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001` | Acquire normal production starter composition over explicit EngineContext + RuntimeExecutionFrameSnapshot + real ModelData. | Acquire operation path only from ProtectedAccessRuntimeFactory.production(engineContext).create(frameSnapshot). | The normal composition reaches dec-core-model production adapter/session and actual ModelData. | Fake adapter/effect counter alone cannot satisfy case. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-06 |
| `CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001` | Acquire normal production starter composition over explicit EngineContext + RuntimeExecutionFrameSnapshot + real ModelData. | Inspect public business/consumer constructors and try to inject RuntimeModelOperationPort/callback. | No caller injection/replacement surface exists. | No operation callback substitution after Guard. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-06 |
| `CASE-P2-TD-RUNTIME-OBJECT-LOCATOR-SCOPE-001` | Create sealed model sessions and controlled RuntimeBindingPlan/frame/owner/cursor fixtures over explicit ModelData handles. | Register a ModelData pre-seal, seal, resolve in owner session, then close it. | Owner session resolves exact object; post-seal registration rejected; closed binding becomes stale. | No static/global locator. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-02 |
| `CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001` | Create sealed model sessions and controlled RuntimeBindingPlan/frame/owner/cursor fixtures over explicit ModelData handles. | Exercise explicit session mismatch, active-session missing object and closed/expired binding separately. | Mismatch -> RUNTIME_SESSION_SCOPE_MISMATCH; active missing -> RUNTIME_OBJECT_NOT_FOUND; closed/expired -> RUNTIME_OBJECT_STALE. | No attempt to infer cross-session state from RuntimeObjectId text. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-02 |
| `CASE-P2-TD-RUNTIME-TARGET-SELECTION-001` | Create sealed model sessions and controlled RuntimeBindingPlan/frame/owner/cursor fixtures over explicit ModelData handles. | Give RuntimeTargetResolver controlled 0,1,2 candidates under exact RuntimeBindingPlan/frame/owner/cursor. | 0 -> RUNTIME_TARGET_NOT_FOUND; 1 -> exact immutable ResolvedRuntimeTarget; 2 -> RUNTIME_TARGET_AMBIGUOUS. | No first/name/frame-only/owner-only/cursor-only fallback. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-02 |
| `CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001` | Acquire real ModelData through normal composition, capture pre-write state, then inject mutation or commit failures after Guard ALLOW. | Trigger mutation failure and commit failure separately after Guard ALLOW. | Observable ModelData/origin equals pre-write state, receipt absent, capability CONSUMED, RUNTIME_WRITE_FAILED. | No partial publication, automatic retry or reselection. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-06 / FAIL-P2-ACCESS-OPERATION-001 |
| `CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001` | Build factory with explicit EngineContext and frame snapshot; obtain Rule/Change/CustomAction entries only from normal composition. | Attempt protected operation through normal public consumer surfaces without Bridge/Guard. | No legal public bypass reaches model operation. | No direct Gateway/Guard/model-port access by business callers. | FLOW-PROTECTED-ACCESS-EXECUTE / composition preconditions |
| `CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001` | Build factory with explicit EngineContext and frame snapshot; obtain Rule/Change/CustomAction entries only from normal composition. | Create one production composition from explicit Context/frame snapshot. | Composition exposes one shared bridge/context/frame/session to representative entries. | No entry-specific authority instance or global Context. | FLOW-PROTECTED-ACCESS-EXECUTE / composition preconditions |
| `CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001` | Build factory with explicit EngineContext and frame snapshot; obtain Rule/Change/CustomAction entries only from normal composition. | Invoke representative Rule entry through composition. | Rule path reaches same Bridge/resolver/Guard/model seam. | No Rule-only bypass. | FLOW-PROTECTED-ACCESS-EXECUTE / composition preconditions |
| `CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001` | Build factory with explicit EngineContext and frame snapshot; obtain Rule/Change/CustomAction entries only from normal composition. | Invoke representative Change entry without RuleKey provenance. | Change path authorizes via ModelAccessRuleKey and same Bridge. | No mandatory RuleKey authority. | FLOW-PROTECTED-ACCESS-EXECUTE / composition preconditions |
| `CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001` | Build factory with explicit EngineContext and frame snapshot; obtain Rule/Change/CustomAction entries only from normal composition. | Invoke representative CustomAction entry without RuleKey provenance. | CustomAction path authorizes via ModelAccessRuleKey and same Bridge. | No custom-action bypass or RuleKey requirement. | FLOW-PROTECTED-ACCESS-EXECUTE / composition preconditions |
| `CASE-P2-TD-AC007-CONSUMER-PARITY-001` | Build factory with explicit EngineContext and frame snapshot; obtain Rule/Change/CustomAction entries only from normal composition. | Run equivalent access through Rule, Change and CustomAction entries. | Equivalent authority/context yields equivalent allow/deny and operation semantics. | No consumer-specific permission widening. | FLOW-PROTECTED-ACCESS-EXECUTE / composition preconditions |
| `CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001` | Build factory with explicit EngineContext and frame snapshot; obtain Rule/Change/CustomAction entries only from normal composition. | Reflect production entry structure and dependency injection. | Entries receive neutral/shared Bridge only, not mutable authority internals. | No per-entry Guard/PolicyIndex/model port injection. | FLOW-PROTECTED-ACCESS-EXECUTE / composition preconditions |
| `CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001` | Build factory with explicit EngineContext and frame snapshot; obtain Rule/Change/CustomAction entries only from normal composition. | Use real fixture and normal factory/composition for representative entries. | At least one representative path reaches actual model state under production assembly. | Manual new Entry(testBridge) alone is insufficient. | FLOW-PROTECTED-ACCESS-EXECUTE / composition preconditions |
| `CASE-P2-TD-COMPOSITION-RUNTIME-CONTEXT-MATCH-001` | Build factory with explicit EngineContext and frame snapshot; obtain Rule/Change/CustomAction entries only from normal composition. | Create composition with frame/owner A, invoke with A then with mismatching frame or owner B. | A may proceed to resolver; mismatch -> RUNTIME_CONTEXT_MISMATCH with resolver/capability/Guard/effect=0. | No runtime target selection under mismatched composition context. | FLOW-PROTECTED-ACCESS-EXECUTE / composition preconditions |
| `CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001` | Use barriers/latches, never sleeps; operate on the same actual ModelData/path when testing contention. | Release multiple threads against one capability. | Exactly one consumes/proceeds; all others receive capability-consumed denial. | No double Guard or effect. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-04,06 |
| `CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001` | Use barriers/latches, never sleeps; operate on the same actual ModelData/path when testing contention. | Freeze two capabilities with same actual ModelData/path/stamp version and race them. | Exactly one commit/receipt/version increment; one stale loser with mutation=0. | No lost update, partial state or session-local split lock domain. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-04,06 |
| `CASE-P2-TD-CROSS-SESSION-MODELDATA-OWNERSHIP-001` | Use barriers/latches, never sleeps; operate on the same actual ModelData/path when testing contention. | Register the exact same ModelData instance twice in one session, then in two active sessions; close owner then retry. | Same-session duplicate -> ALREADY_REGISTERED; cross-session active -> OWNERSHIP_CONFLICT; after close lease may transfer without version reset. | No two active coordination/version domains for one actual ModelData. | FLOW-PROTECTED-ACCESS-EXECUTE / STEP-P2-ACCESS-04,06 |
| `CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001` | Inspect Maven/module dependencies and current P2 type ownership using source/build metadata. | Inspect P3/P4/P6 core dependencies and starter/model/context edges. | P3/P4/P6 -> context allowed; -> starter forbidden; starter -> model allowed for production assembly. | No core dependency on starter internals. | P2-IMPACT-R22 dependency rules |
| `CASE-P2-TD-ATOMIC-PUBLICATION-001` | Build one valid and one invalid immutable candidate Context while retaining a previously published Context. | Construct valid candidate then fail candidate construction/publication around swap. | Only whole new candidate or whole previous Context is observable; COMPILER coordinates swap. | No partial Context or Context-initiated publication. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-05,06 |
| `CASE-P2-TD-CONTEXT-ISOLATION-001` | Build one valid and one invalid immutable candidate Context while retaining a previously published Context. | Keep old EngineContext reference while compiling/publishing another candidate. | Old snapshot remains immutable/usable; new snapshot is separate. | No mutation of existing Context or global-current dependency. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-05,06 |
| `CASE-P2-TD-POLICY-INDEX-PUBLICATION-001` | Build one valid and one invalid immutable candidate Context while retaining a previously published Context. | Publish valid exact policy set and attempt invalid classification publication. | Valid immutable PolicyIndex publishes with closure; invalid input publishes nothing. | No runtime repair/partial index. | FLOW-CONFIG-COMPILE / STEP-P2-COMPILE-05,06 |
| `CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001` | Run equivalent compile/runtime failures repeatedly with ordering perturbations and non-sensitive fixtures. | Repeat equivalent compile failures with declaration-order changes. | Stable error code/source-aware non-sensitive diagnostics ordering. | No object identity/hash iteration leakage. | compile/runtime failurePaths in FLOW-R10 |
| `CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001` | Run equivalent compile/runtime failures repeatedly with ordering perturbations and non-sensitive fixtures. | Repeat equivalent runtime denials with candidate/order perturbations. | Stable denial code and non-sensitive diagnostic representation. | No object identity/hash iteration leakage. | compile/runtime failurePaths in FLOW-R10 |
| `CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001` | Use repository real P1/P2 XML/demo fixture and normal production composition, not a fake callback-only seam. | Compile real dynamic fixture requiring EXACT_RUNTIME_BINDING and run target resolution. | Compiler emits legal runtime plan and production resolver/Guard path is reachable. | No fake-only dynamic classifier or static allow substitution. | FLOW-CONFIG-COMPILE -> FLOW-PROTECTED-ACCESS-EXECUTE |
| `CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001` | Use repository real P1/P2 XML/demo fixture and normal production composition, not a fake callback-only seam. | From real source model-access declarations, compile then execute representative READ and WRITE. | Source identity/path/op map to exact policy, target selection and real operation end-to-end. | No EXECUTE or different runtime path semantics. | FLOW-CONFIG-COMPILE -> FLOW-PROTECTED-ACCESS-EXECUTE |
| `CASE-P2-TD-DECLARATION-BOUNDARY-001` | Compile current P1-compatible declarations and verify P2 adds no retired-module or EXECUTE dependency. | Compile current declarations and inspect module/source references. | P2 declaration boundary points only at active/current P1-compatible modules and current P2 contracts. | No retired P1 module dependency or P7 scope creep. | P2 declaration compatibility boundary |

## 5. Four new Review blockers added by R23

### CASE-P2-TD-COMPOSITION-RUNTIME-CONTEXT-MATCH-001 — BLOCKING
Production composition binds exact frame/owner. An invocation with a different frame or owner must return `RUNTIME_CONTEXT_MISMATCH` before RuntimeTargetResolver, capability, Guard or model effect.

### CASE-P2-TD-RUNTIME-TARGET-SELECTION-001 — BLOCKING
Exercise the sole RuntimeTargetResolver with controlled 0/1/N candidates under exact RuntimeBindingPlan + frame/owner/cursor + sealed session. Zero=`RUNTIME_TARGET_NOT_FOUND`; one freezes exact `ResolvedRuntimeTarget`; N>1=`RUNTIME_TARGET_AMBIGUOUS`; no alternate selector/fallback is legal.

### CASE-P2-TD-MUTATION-STAMP-OBJECT-BINDING-001 — BLOCKING
Attempt to combine target object A with session/object/path/version from B. `ResolvedWriteIntent.of(...)` must reject any stamp whose session/object differs from `ResolvedRuntimeTarget` or whose path differs from `ModelAccessRuleKey.modelPath`.

### CASE-P2-TD-CROSS-SESSION-MODELDATA-OWNERSHIP-001 — BLOCKING
Register the exact same ModelData instance twice in one active session and in two active sessions. Same-session duplicate=`RUNTIME_OBJECT_ALREADY_REGISTERED`; cross-session active alias=`RUNTIME_OBJECT_OWNERSHIP_CONFLICT`. After owner close, lease transfer may occur but per-path version must not reset.

## 6. Preserved PASS directions

- P1 sourceModel remains shared `ViewKey -> TargetKey`; authorization owner System is separate.
- READ/WRITE-only and two-row policy classification remain exhaustive.
- Direct Bridge and WRITE intent use `ModelAccessRuleKey`; RuleKey is optional provenance only.
- WRITE has one path authority; no second operation-port ModelPath.
- WRITE intent remains exact 0/1/N and frozen before Guard.
- RuntimeFactValue remains closed/deep immutable/deterministic.
- Existing 19 TestClass module/path/RED commands remain exact.
- AC-007 production Rule/Change/CustomAction entries share one Bridge/Context/session authority snapshot.
- Same-capability atomic consume remains at most one Guard/operation attempt.

## 7. P2/P7 Test boundary

R23 tests P2 RuntimeModelSession only as a composition/frame locator and protected-operation atomicity seam. Tests must not treat it as a generic user session, cross-request transaction manager or P7 resource lifecycle abstraction.

## 8. Review / Evidence gate

`risk_detection.json` remains NOT_SCANNED and current execution Evidence IDs remain none. Exact RED commands above are planned commands, not executed Evidence. `TESTDESIGN-P2-R23` remains `NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`; Implementation Plan/TDD/Development remain BLOCKED.
