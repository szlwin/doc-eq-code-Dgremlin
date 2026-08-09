# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R25`
 > Base：`TESTDESIGN-P2-R24`
> Inputs：`REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R20` + `FLOW-R10` + `P2-IMPACT-R23` + `DESIGN-P2-R24`
> Status：`NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`

R25 preserves R24 and adds production registration provenance: **69 blocking Case IDs -> 19 exact TestClasses**. No TDD/Evidence is claimed.

## Formal RED
For every registry row `(M,C)`: bootstrap=`./mvnw -pl M -am -Dmaven.test.skip=true install`; target RED=`./mvnw -pl M -Dtest=C -Dsurefire.failIfNoSpecifiedTests=true test`. Target RED never uses `-am`; missing class/symbol/setup or pre-assert compile failure=`INVALID_RED`.

## Exact registry

|Key|Module|TestClass|Planned source|
|---|---|---|---|
|`DAG`|`dec-core-compiler`|`P2RevisionDependencyDagContractTest`|`dec-core-compiler/src/test/java/dec/core/compiler/contract/P2RevisionDependencyDagContractTest.java`|
|`SYSTEM`|`dec-core-compiler`|`SystemCompilationContractTest`|`dec-core-compiler/src/test/java/dec/core/compiler/system/SystemCompilationContractTest.java`|
|`RULEVIEW`|`dec-core-compiler`|`RuleViewCompilationContractTest`|`dec-core-compiler/src/test/java/dec/core/compiler/ruleview/RuleViewCompilationContractTest.java`|
|`TARGET`|`dec-core-compiler`|`TargetKeyModelPathContractTest`|`dec-core-compiler/src/test/java/dec/core/compiler/model/access/TargetKeyModelPathContractTest.java`|
|`POLICY`|`dec-core-compiler`|`ModelAccessPolicyContractTest`|`dec-core-compiler/src/test/java/dec/core/compiler/model/access/ModelAccessPolicyContractTest.java`|
|`API`|`dec-core-context`|`ProtectedAccessCurrentApiContractTest`|`dec-core-context/src/test/java/dec/core/context/runtime/ProtectedAccessCurrentApiContractTest.java`|
|`VALUE`|`dec-core-context`|`RuntimeFactValueContractTest`|`dec-core-context/src/test/java/dec/core/context/runtime/RuntimeFactValueContractTest.java`|
|`ID`|`dec-core-context`|`OpaqueRuntimeIdContractTest`|`dec-core-context/src/test/java/dec/core/context/runtime/OpaqueRuntimeIdContractTest.java`|
|`INTENT`|`dec-core-starter`|`ProtectedWriteIntentResolutionTest`|`dec-core-starter/src/test/java/dec/core/starter/access/ProtectedWriteIntentResolutionTest.java`|
|`ADAPTER`|`dec-core-starter`|`ProtectedRuntimeModelAdapterIntegrationTest`|`dec-core-starter/src/test/java/dec/core/starter/access/ProtectedRuntimeModelAdapterIntegrationTest.java`|
|`LOCATOR`|`dec-core-model`|`RuntimeObjectLocatorIntegrationTest`|`dec-core-model/src/test/java/dec/core/model/runtime/RuntimeObjectLocatorIntegrationTest.java`|
|`TXN`|`dec-core-model`|`ProtectedWriteTransactionIntegrationTest`|`dec-core-model/src/test/java/dec/core/model/runtime/ProtectedWriteTransactionIntegrationTest.java`|
|`COMPOSE`|`dec-core-starter`|`ProtectedAccessProductionCompositionTest`|`dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessProductionCompositionTest.java`|
|`CONC`|`dec-core-starter`|`ProtectedAccessConcurrencyTest`|`dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessConcurrencyTest.java`|
|`DEP`|`dec-core-starter`|`ProtectedAccessDependencyDirectionTest`|`dec-core-starter/src/test/java/dec/core/starter/architecture/ProtectedAccessDependencyDirectionTest.java`|
|`PUB`|`dec-core-compiler`|`AtomicPublicationContractTest`|`dec-core-compiler/src/test/java/dec/core/compiler/publication/AtomicPublicationContractTest.java`|
|`DIAG`|`dec-core-compiler`|`P2DiagnosticDeterminismTest`|`dec-core-compiler/src/test/java/dec/core/compiler/diagnostic/P2DiagnosticDeterminismTest.java`|
|`FIXTURE`|`dec-demo`|`P2RealFixtureIntegrationTest`|`dec-demo/src/test/java/dec/demo/p2/P2RealFixtureIntegrationTest.java`|
|`COMPAT`|`dec-core-compiler`|`P2DeclarationCompatibilityContractTest`|`dec-core-compiler/src/test/java/dec/core/compiler/compat/P2DeclarationCompatibilityContractTest.java`|

## Blocking cases by exact TestClass

### `DAG` — Resolve only current chain/projections; exact DAG, no stale current authority/cycle.
`CASE-P2-TD-REVISION-DAG-001`

### `SYSTEM` — Compile deterministic System fixtures; duplicates/missing/ordering fail stably; no order fallback/mutation leak.
`CASE-P2-TD-SYSTEM-DETERMINISM-001` `CASE-P2-TD-SYSTEM-DUPLICATE-001` `CASE-P2-TD-SYSTEM-FORWARD-REF-001` `CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001` `CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001` `CASE-P2-TD-BM-CANONICAL-PAIR-001`

### `RULEVIEW` — Compile RuleView ownership/key/lookup fixtures; exact composite identity; no bare-name/cross-System fallback.
`CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001` `CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001` `CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001` `CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001` `CASE-P2-TD-RULEKEY-CONTRACT-001` `CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001` `CASE-P2-TD-KEY-SOURCE-COMPAT-001` `CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001`

### `TARGET` — Compile shared ViewKey TargetKey + exact ModelPath; orthogonal axes/wildcard finite expansion; stable errors.
`CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001` `CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001` `CASE-P2-TD-MODEL-PATH-UNKNOWN-001` `CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001` `CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001` `CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001`

### `POLICY` — Compile READ/WRITE two-row policy + compiler-resolved binding; invalid tuples/plans deny/publish=0; no runtime repair/reparse.
`CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001` `CASE-P2-TD-NO-EXECUTE-CONTRACT-001` `CASE-P2-TD-STATIC-DENY-001` `CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001` `CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001` `CASE-P2-TD-RUNTIME-BINDING-PROOF-001` `CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001`

### `API` — Compile/reflect current R24 API only; Java8 surfaces complete, including RuntimeModelSession extends AutoCloseable and typed registration input.
`CASE-P2-TD-CURRENT-API-SELF-CONTAINED-001`

### `VALUE` — Exercise closed deep-immutable RuntimeFactValue canonical domain/equality/serialization; no live/raw Object leak.
`CASE-P2-TD-RUNTIME-FACT-VALUE-DOMAIN-001` `CASE-P2-TD-RUNTIME-FACT-VALUE-DEEP-IMMUTABILITY-001`

### `ID` — Opaque runtime IDs exact/case-sensitive/nonblank; no permission or scope inference from text.
`CASE-P2-TD-OPAQUE-RUNTIME-ID-VALUE-CONTRACT-001`

### `INTENT` — Resolve WRITE authority/path/typed context/stamp 0/1/N; one immutable intent pre-Guard; no post-Guard reselection/splice.
`CASE-P2-TD-WRITE-INTENT-NOT-FOUND-001` `CASE-P2-TD-WRITE-INTENT-AMBIGUOUS-001` `CASE-P2-TD-WRITE-INTENT-FREEZE-STABILITY-001` `CASE-P2-TD-WRITE-AUTHORITY-MODEL-ACCESS-RULEKEY-001` `CASE-P2-TD-WRITE-SINGLE-PATH-AUTHORITY-001` `CASE-P2-TD-TYPED-RUNTIME-CONTEXT-001` `CASE-P2-TD-MUTATION-STAMP-OBJECT-BINDING-001`

### `ADAPTER` — Normal production adapter performs real READ/WRITE; DENY invokes model effect zero times; no caller operation injection.
`CASE-P2-TD-REAL-READ-OPERATION-001` `CASE-P2-TD-REAL-WRITE-OPERATION-001` `CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001` `CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001`

### `LOCATOR` — Sealed session + exact sourceTargetKey/compiledBinding/context selects 0/1/N; deterministic scope/not-found/stale; no fallback inference.
`CASE-P2-TD-RUNTIME-OBJECT-LOCATOR-SCOPE-001` `CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001` `CASE-P2-TD-RUNTIME-TARGET-SELECTION-001`

### `TXN` — Guard-approved WRITE commits once or rollback/restores; stale/failure no receipt/model change; capability consumed.
`CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001`

### `COMPOSE` — Normal production composition uses one Context/bridge, explicit typed registrations, representative Rule/Change/CustomAction parity, no legal bypass.
`CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001` `CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001` `CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001` `CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001` `CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001` `CASE-P2-TD-AC007-CONSUMER-PARITY-001` `CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001` `CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001` `CASE-P2-TD-COMPOSITION-RUNTIME-CONTEXT-MATCH-001` `CASE-P2-TD-PRODUCTION-RUNTIME-REGISTRATION-BINDING-001`

### `CONC` — Latch/barrier concurrency: one capability consumes once; same actual ModelData/path/version has one commit; cross-session ownership fail closed.
`CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001` `CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001` `CASE-P2-TD-CROSS-SESSION-MODELDATA-OWNERSHIP-001`

### `DEP` — Dependency direction: core->context allowed; core->starter forbidden; starter->model production assembly allowed.
`CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001`

### `PUB` — Compiler coordinates atomic whole-Context publication; old snapshot immutable; PolicyIndex no partial/runtime repair.
`CASE-P2-TD-ATOMIC-PUBLICATION-001` `CASE-P2-TD-CONTEXT-ISOLATION-001` `CASE-P2-TD-POLICY-INDEX-PUBLICATION-001`

### `DIAG` — Compile/runtime diagnostics stable and non-sensitive; no object/hash/iteration-order leakage.
`CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001` `CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001`

### `FIXTURE` — Real P1 fixture reaches compiler plan -> production resolver/Guard -> READ/WRITE; no fake/static-allow substitution.
`CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001` `CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001`

### `COMPAT` — Current P2 declaration boundary preserves active P1 compatibility; no retired module/P7 scope creep.
`CASE-P2-TD-DECLARATION-BOUNDARY-001`

## Normative R25 blocker oracles

- `CASE-P2-TD-REVISION-DAG-001`: exact authority is `REQAN+Overlay -> BM-R20 -> FLOW-R10 -> DESIGN-P2-R24 -> TESTDESIGN-P2-R25`; `P2-IMPACT-R23` is parallel/non-authoritative; BM/Flow use stable downstream projections; stale R22/R23/R24 current authority or Impact<->Design cycle forbidden.
- `CASE-P2-TD-CURRENT-API-SELF-CONTAINED-001`: R24 alone compiles/reflects all P2-added APIs; MUST contain `public interface RuntimeModelSession extends AutoCloseable`; `interface ... implements AutoCloseable` makes RED `INVALID_RED`; typed registration/frame constructors compile.
- `CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001`: P1 `targetView + TargetPropertyPath(kind,value)` -> neutral `CompiledTargetBinding` losslessly; raw selector/parser/property-tree/definition runtime access count=0.
- `CASE-P2-TD-PRODUCTION-RUNTIME-REGISTRATION-BINDING-001`: normal factory + exact EngineContext + >=2 explicit `RuntimeModelRegistrationInput(TargetKey,CompiledTargetBinding,ModelData)` pairs. Valid pairs register/resolve exact ModelData; pair absent from Context fails composition before resolver/capability/Guard/effect. `ModelData.name`, ViewData/property-tree, list-order/first-match, raw-definition, selector-reparse access counts=0. Registration cannot grant permission.
- `CASE-P2-TD-RUNTIME-TARGET-SELECTION-001`: exact sourceTargetKey + compiler binding + typed composition/session yields 0=>NOT_FOUND, 1=>one immutable target, N=>AMBIGUOUS; no metadata/order/raw-selector fallback.

## Gate
`risk_detection.json` remains `NOT_SCANNED`; current execution Evidence IDs are none. Same-revision Design/Impact/TestDesign specialist Review and risk Evidence are required before lifecycle/TDD closure. Implementation Plan, TDD and Development remain BLOCKED.
