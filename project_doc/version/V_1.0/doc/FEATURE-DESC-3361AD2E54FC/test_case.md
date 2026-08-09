# P2 TestDesign R26
`TESTDESIGN-P2-R26`; base R25; inputs REQAN-P2-R01@d08612768131 + Overlay R04 + BM-R20 + FLOW-R11 + P2-IMPACT-R24 + DESIGN-P2-R25.
Status `NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`. **72 blocking Cases -> 21 exact TestClasses**. Every row below is a current-revision Fixture/Action/Expected/Forbidden/Ref oracle; no historical TestDesign lookup.

## RED
Bootstrap `(M,C)`: `./mvnw -pl M -am -Dmaven.test.skip=true install`.
Target `(M,C)`: `./mvnw -pl M -Dtest=C -Dsurefire.failIfNoSpecifiedTests=true test` (never `-am`). Pre-assert compile/setup/missing class=`INVALID_RED`.

## Registry
`key | module | class | planned source`
`DAG | dec-core-compiler | P2RevisionDependencyDagContractTest | dec-core-compiler/src/test/java/dec/core/compiler/contract/P2RevisionDependencyDagContractTest.java`
`SYSTEM | dec-core-compiler | SystemCompilationContractTest | dec-core-compiler/src/test/java/dec/core/compiler/system/SystemCompilationContractTest.java`
`RULEVIEW | dec-core-compiler | RuleViewCompilationContractTest | dec-core-compiler/src/test/java/dec/core/compiler/ruleview/RuleViewCompilationContractTest.java`
`TARGET | dec-core-compiler | TargetKeyModelPathContractTest | dec-core-compiler/src/test/java/dec/core/compiler/model/access/TargetKeyModelPathContractTest.java`
`POLICY | dec-core-compiler | ModelAccessPolicyContractTest | dec-core-compiler/src/test/java/dec/core/compiler/model/access/ModelAccessPolicyContractTest.java`
`API_CTX | dec-core-context | ProtectedAccessContextApiContractTest | dec-core-context/src/test/java/dec/core/context/runtime/ProtectedAccessContextApiContractTest.java`
`API_MODEL | dec-core-model | ProtectedAccessModelApiContractTest | dec-core-model/src/test/java/dec/core/model/runtime/ProtectedAccessModelApiContractTest.java`
`API_STARTER | dec-core-starter | ProtectedAccessStarterApiContractTest | dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessStarterApiContractTest.java`
`VALUE | dec-core-context | RuntimeFactValueContractTest | dec-core-context/src/test/java/dec/core/context/runtime/RuntimeFactValueContractTest.java`
`ID | dec-core-context | OpaqueRuntimeIdContractTest | dec-core-context/src/test/java/dec/core/context/runtime/OpaqueRuntimeIdContractTest.java`
`INTENT | dec-core-starter | ProtectedWriteIntentResolutionTest | dec-core-starter/src/test/java/dec/core/starter/access/ProtectedWriteIntentResolutionTest.java`
`ADAPTER | dec-core-starter | ProtectedRuntimeModelAdapterIntegrationTest | dec-core-starter/src/test/java/dec/core/starter/access/ProtectedRuntimeModelAdapterIntegrationTest.java`
`LOCATOR | dec-core-model | RuntimeObjectLocatorIntegrationTest | dec-core-model/src/test/java/dec/core/model/runtime/RuntimeObjectLocatorIntegrationTest.java`
`TXN | dec-core-model | ProtectedWriteTransactionIntegrationTest | dec-core-model/src/test/java/dec/core/model/runtime/ProtectedWriteTransactionIntegrationTest.java`
`COMPOSE | dec-core-starter | ProtectedAccessProductionCompositionTest | dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessProductionCompositionTest.java`
`CONC | dec-core-starter | ProtectedAccessConcurrencyTest | dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessConcurrencyTest.java`
`DEP | dec-core-starter | ProtectedAccessDependencyDirectionTest | dec-core-starter/src/test/java/dec/core/starter/architecture/ProtectedAccessDependencyDirectionTest.java`
`PUB | dec-core-compiler | AtomicPublicationContractTest | dec-core-compiler/src/test/java/dec/core/compiler/publication/AtomicPublicationContractTest.java`
`DIAG | dec-core-compiler | P2DiagnosticDeterminismTest | dec-core-compiler/src/test/java/dec/core/compiler/diagnostic/P2DiagnosticDeterminismTest.java`
`FIXTURE | dec-demo | P2RealFixtureIntegrationTest | dec-demo/src/test/java/dec/demo/p2/P2RealFixtureIntegrationTest.java`
`COMPAT | dec-core-compiler | P2DeclarationCompatibilityContractTest | dec-core-compiler/src/test/java/dec/core/compiler/compat/P2DeclarationCompatibilityContractTest.java`

## Oracle notation
Fixture `fx:<case-slug>` means this R26 constructs the minimal positive/negative facts named by that Case/Expected in its registry owner module; it may not import old TestDesign.
Action `a:<case-slug>` means execute that Case assertion through the registry target RED.
Forbidden: `F0` no fallback/global/stale/unauthorized/post-failure effect; `FP` no wrap/rebind/ModelData substitution/metadata-order-selector inference/pre-Guard effect; `FA` no reverse test dep/old Design/package-private cross-module/pre-assert failure; `FW` no duplicate object-path-version/retry/reselection/partial commit/failure receipt; `FD` no illegal dependency.
Refs: `R`=FLOW-R11/DESIGN-P2-R25; `P`=FLOW-R11 failure + P2-IMPACT-R24 CMI-P2-PROTECTED-ACCESS-004; `C`=FLOW-R11 compile + P2-IMPACT-R24 CMI-P2-COMPILE-004.

## 72 current oracles
`Case | Class | Fixture | Action | Expected | Forbidden | Ref`
`CASE-P2-TD-REVISION-DAG-001 | DAG | fx:revision-dag | a:revision-dag | Authority REQAN+Overlay->BM-R20->FLOW-R11->DESIGN-R25->TD-R26; Impact R24 parallel. | F0 | R`
`CASE-P2-TD-SYSTEM-DETERMINISM-001 | SYSTEM | fx:system-determinism | a:system-determinism | Equivalent inputs/orderings compile to same System identities/projection. | F0 | R`
`CASE-P2-TD-SYSTEM-DUPLICATE-001 | SYSTEM | fx:system-duplicate | a:system-duplicate | Duplicate input is rejected deterministically with stable identity-aware diagnostics; publication/effe... | F0 | R`
`CASE-P2-TD-SYSTEM-FORWARD-REF-001 | SYSTEM | fx:system-forward-ref | a:system-forward-ref | Forward reference resolves deterministically when legal; missing/ambiguous target fails with stable co... | F0 | R`
`CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001 | SYSTEM | fx:system-ownership-snapshot | a:system-ownership-snapshot | Compiled System ownership is immutable and unchanged by later mutable source changes. | F0 | R`
`CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001 | SYSTEM | fx:system-version-identity | a:system-version-identity | System identity/version facts compare exactly; revision changes never alias existing identity. | F0 | R`
`CASE-P2-TD-BM-CANONICAL-PAIR-001 | SYSTEM | fx:bm-canonical-pair | a:bm-canonical-pair | Human/canonical BM pair names BM-R20 consistently and no stale downstream exact authority. | F0 | R`
`CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001 | RULEVIEW | fx:ruleview-system-required | a:ruleview-system-required | RuleView without owning System is rejected; no bare/global fallback. | F0 | R`
`CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001 | RULEVIEW | fx:ruleview-same-system-duplicate | a:ruleview-same-system-duplicate | Duplicate input is rejected deterministically with stable identity-aware diagnostics; publication/effe... | F0 | R`
`CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001 | RULEVIEW | fx:ruleview-cross-system-isolation | a:ruleview-cross-system-isolation | Same local RuleView name in different Systems remains isolated by composite identity. | F0 | R`
`CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001 | RULEVIEW | fx:ruleview-view-resolution | a:ruleview-view-resolution | RuleView resolves exact owning System + ViewKey; missing/ambiguous View fails, no bare-name fallback. | F0 | R`
`CASE-P2-TD-RULEKEY-CONTRACT-001 | RULEVIEW | fx:rulekey-contract | a:rulekey-contract | RuleKey is exact owner RuleViewKey + local rule name with structural equality/hash. | F0 | R`
`CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001 | RULEVIEW | fx:ruleview-composite-lookup | a:ruleview-composite-lookup | Lookup requires composite System/RuleView identity; bare name cannot cross System. | F0 | R`
`CASE-P2-TD-KEY-SOURCE-COMPAT-001 | RULEVIEW | fx:key-source-compat | a:key-source-compat | P1 key/source compatibility remains exact; P2 adds no second source identity namespace. | F0 | R`
`CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001 | RULEVIEW | fx:bare-name-compatibility-boundary | a:bare-name-compatibility-boundary | Bare-name compatibility is limited to preserved P1 boundary and never widens P2 authority. | F0 | R`
`CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001 | TARGET | fx:targetkey-source-mapping | a:targetkey-source-mapping | sourceModel resolves through shared ViewKey to TargetKey; owner System/local target remain separate axes. | F0 | R`
`CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001 | TARGET | fx:target-path-orthogonality | a:target-path-orthogonality | TargetKey(shared ViewKey) and exact ModelPath remain independent axes. | F0 | R`
`CASE-P2-TD-MODEL-PATH-UNKNOWN-001 | TARGET | fx:model-path-unknown | a:model-path-unknown | Unknown source path fails at compile time; runtime receives no wildcard/repair authority. | F0 | R`
`CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001 | TARGET | fx:wildcard-finite-expansion | a:wildcard-finite-expansion | Wildcard expands finitely at compile time to exact ModelPath values; runtime wildcard is absent. | F0 | R`
`CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001 | TARGET | fx:model-path-cross-consumer-equivalence | a:model-path-cross-consumer-equivalence | All consumers observe the identical canonical ModelPath segments. | F0 | R`
`CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001 | TARGET | fx:p1-path-operation-migration | a:p1-path-operation-migration | P1 path semantics migrate losslessly to READ/WRITE-only P2 operation authority. | F0 | R`
`CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001 | POLICY | fx:access-read-write-matrix | a:access-read-write-matrix | Exact access read write matrix contract is deterministic and fail-closed; invalid input yields stable... | FW | R`
`CASE-P2-TD-NO-EXECUTE-CONTRACT-001 | POLICY | fx:no-execute-contract | a:no-execute-contract | Only READ/WRITE are accepted; EXECUTE is absent/N-A for current P2. | F0 | R`
`CASE-P2-TD-STATIC-DENY-001 | POLICY | fx:static-deny | a:static-deny | Unauthorized/static mismatch is denied before Guard/model effect; no widening to runtime. | F0 | R`
`CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001 | POLICY | fx:policy-classification-truth-table | a:policy-classification-truth-table | Exactly the two legal policy rows publish; all other status/requirement/plan tuples fail publication. | F0 | R`
`CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001 | POLICY | fx:runtime-plan-exact-binding | a:runtime-plan-exact-binding | P1 targetView+TargetPropertyPath(kind,value) maps losslessly to neutral CompiledTargetBinding; runtime... | F0 | R`
`CASE-P2-TD-RUNTIME-BINDING-PROOF-001 | POLICY | fx:runtime-binding-proof | a:runtime-binding-proof | Binding proof derives from exact current plan+trusted handle; no caller-made proof widens access. | F0 | R`
`CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001 | POLICY | fx:runtime-plan-mismatch | a:runtime-plan-mismatch | Plan/provenance mismatch fails closed before capability/Guard/effect. | F0 | R`
`CASE-P2-TD-CONTEXT-API-SELF-CONTAINED-001 | API_CTX | fx:context-api-self-contained | a:context-api-self-contained | CONTEXT API compiles/reflects in context only; no reverse dep. | FA | R`
`CASE-P2-TD-MODEL-API-SELF-CONTAINED-001 | API_MODEL | fx:model-api-self-contained | a:model-api-self-contained | MODEL API compiles; trusted handle/frame no public ctor/wrap/rebind/ModelData accessor. | FA | R`
`CASE-P2-TD-STARTER-API-SELF-CONTAINED-001 | API_STARTER | fx:starter-api-self-contained | a:starter-api-self-contained | STARTER API compiles and legally consumes CONTEXT+MODEL contracts. | FA | R`
`CASE-P2-TD-RUNTIME-FACT-VALUE-DOMAIN-001 | VALUE | fx:runtime-fact-value-domain | a:runtime-fact-value-domain | RuntimeFactValue remains closed, canonical and deep immutable; no live mutable/raw Object reference le... | F0 | R`
`CASE-P2-TD-RUNTIME-FACT-VALUE-DEEP-IMMUTABILITY-001 | VALUE | fx:runtime-fact-value-deep-immutability | a:runtime-fact-value-deep-immutability | RuntimeFactValue remains closed, canonical and deep immutable; no live mutable/raw Object reference le... | F0 | R`
`CASE-P2-TD-OPAQUE-RUNTIME-ID-VALUE-CONTRACT-001 | ID | fx:opaque-runtime-id-value-contract | a:opaque-runtime-id-value-contract | Opaque IDs reject blank, compare exact/case-sensitive, and encode no permission/session inference. | F0 | R`
`CASE-P2-TD-WRITE-INTENT-NOT-FOUND-001 | INTENT | fx:write-intent-not-found | a:write-intent-not-found | Zero WRITE intent candidates -> WRITE_INTENT_NOT_FOUND before capability/Guard/effect. | FW | R`
`CASE-P2-TD-WRITE-INTENT-AMBIGUOUS-001 | INTENT | fx:write-intent-ambiguous | a:write-intent-ambiguous | More than one WRITE intent candidate -> WRITE_INTENT_AMBIGUOUS; no arbitrary first/last selection. | FW | R`
`CASE-P2-TD-WRITE-INTENT-FREEZE-STABILITY-001 | INTENT | fx:write-intent-freeze-stability | a:write-intent-freeze-stability | Exactly one WRITE intent is immutable before Guard; later frame/owner/cursor/model changes do not trig... | FW | R`
`CASE-P2-TD-WRITE-AUTHORITY-MODEL-ACCESS-RULEKEY-001 | INTENT | fx:write-authority-model-access-rulekey | a:write-authority-model-access-rulekey | ModelAccessRuleKey is the sole WRITE permission authority; RuleKey remains optional provenance only. | FW | R`
`CASE-P2-TD-WRITE-SINGLE-PATH-AUTHORITY-001 | INTENT | fx:write-single-path-authority | a:write-single-path-authority | WRITE has one ModelPath authority from ModelAccessRuleKey; operation port accepts no second path. | FW | R`
`CASE-P2-TD-TYPED-RUNTIME-CONTEXT-001 | INTENT | fx:typed-runtime-context | a:typed-runtime-context | frame/owner/cursor use typed IDs; no null/sentinel/raw-string scope authority. | F0 | R`
`CASE-P2-TD-MUTATION-STAMP-OBJECT-BINDING-001 | INTENT | fx:mutation-stamp-object-binding | a:mutation-stamp-object-binding | Stamp session/object/path/version is atomically derived from the same resolved trusted handle and auth... | FW | R`
`CASE-P2-TD-REAL-READ-OPERATION-001 | ADAPTER | fx:real-read-operation | a:real-read-operation | After Guard ALLOW, MODEL adapter reads actual resolved handle/path and returns deep immutable value; m... | F0 | R`
`CASE-P2-TD-REAL-WRITE-OPERATION-001 | ADAPTER | fx:real-write-operation | a:real-write-operation | After Guard ALLOW, MODEL adapter performs exactly one rollback-safe mutation and returns receipt only... | FW | R`
`CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001 | ADAPTER | fx:production-model-adapter-reachability | a:production-model-adapter-reachability | Normal production composition reaches MODEL operation adapter, not fake callback. | F0 | R`
`CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001 | ADAPTER | fx:operation-port-not-caller-injectable | a:operation-port-not-caller-injectable | Business/consumer cannot inject/replace Guard or model operation implementation through invocation API. | F0 | R`
`CASE-P2-TD-RUNTIME-OBJECT-LOCATOR-SCOPE-001 | LOCATOR | fx:runtime-object-locator-scope | a:runtime-object-locator-scope | Session locate respects exact session/handle provenance; scope mismatch is deterministic without parsi... | FP | R`
`CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001 | LOCATOR | fx:runtime-object-not-found-stale | a:runtime-object-not-found-stale | Active missing object -> NOT_FOUND; closed/revoked prior binding -> STALE; no fallback replacement. | FP | R`
`CASE-P2-TD-RUNTIME-TARGET-SELECTION-001 | LOCATOR | fx:runtime-target-selection | a:runtime-target-selection | Exact plan + trusted handle provenance + frame facts yields 0->NOT_FOUND, 1->one immutable target, N->... | FP | P`
`CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001 | TXN | fx:runtime-write-rollback | a:runtime-write-rollback | Injected MODEL mutation/commit failure restores pre-write observable state; receipt absent; capability... | FW | R`
`CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001 | COMPOSE | fx:production-seam-no-legal-bypass | a:production-seam-no-legal-bypass | All representative production entries share one protected bridge; no public path reaches effect withou... | F0 | R`
`CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001 | COMPOSE | fx:ac007-production-composition | a:ac007-production-composition | AC-007 Option B representative Rule/Change/CustomAction path reaches the same protected composition an... | F0 | R`
`CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001 | COMPOSE | fx:ac007-rule-consumer-integration | a:ac007-rule-consumer-integration | AC-007 Option B representative Rule/Change/CustomAction path reaches the same protected composition an... | F0 | R`
`CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001 | COMPOSE | fx:ac007-change-consumer-integration | a:ac007-change-consumer-integration | AC-007 Option B representative Rule/Change/CustomAction path reaches the same protected composition an... | F0 | R`
`CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001 | COMPOSE | fx:ac007-custom-action-consumer-integration | a:ac007-custom-action-consumer-integration | AC-007 Option B representative Rule/Change/CustomAction path reaches the same protected composition an... | F0 | R`
`CASE-P2-TD-AC007-CONSUMER-PARITY-001 | COMPOSE | fx:ac007-consumer-parity | a:ac007-consumer-parity | AC-007 Option B representative Rule/Change/CustomAction path reaches the same protected composition an... | F0 | R`
`CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001 | COMPOSE | fx:ac007-representative-consumer-structure | a:ac007-representative-consumer-structure | AC-007 Option B representative Rule/Change/CustomAction path reaches the same protected composition an... | F0 | R`
`CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001 | COMPOSE | fx:ac007-real-production-reachability | a:ac007-real-production-reachability | AC-007 Option B representative Rule/Change/CustomAction path reaches the same protected composition an... | F0 | R`
`CASE-P2-TD-COMPOSITION-RUNTIME-CONTEXT-MATCH-001 | COMPOSE | fx:composition-runtime-context-match | a:composition-runtime-context-match | Invocation frame/owner must exactly equal trusted RuntimeModelFrame-derived composition; mismatch deni... | F0 | R`
`CASE-P2-TD-PRODUCTION-RUNTIME-REGISTRATION-BINDING-001 | COMPOSE | fx:production-runtime-registration-binding | a:production-runtime-registration-binding | Trusted RuntimeModelFrame handles validate against captured Context; metadata/list/raw selector infere... | FP | P`
`CASE-P2-TD-RUNTIME-TARGET-SUBSTITUTION-001 | COMPOSE | fx:runtime-target-substitution | a:runtime-target-substitution | A->A legal; A->B/rebind/swap denied before capability/Guard/effect. | FP | P`
`CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001 | CONC | fx:capability-concurrent-consume | a:capability-concurrent-consume | Concurrent calls on one capability cause at most one Guard/operation attempt; loser observes consumed... | FW | R`
`CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001 | CONC | fx:different-capability-concurrency | a:different-capability-concurrency | Same actual ModelData/path/version across different capabilities commits at most once; stale loser mut... | FW | R`
`CASE-P2-TD-CROSS-SESSION-MODELDATA-OWNERSHIP-001 | CONC | fx:cross-session-modeldata-ownership | a:cross-session-modeldata-ownership | Same trusted actual ModelData cannot acquire two active session leases or independent version domains. | F0 | R`
`CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001 | DEP | fx:downstream-dependency-direction | a:downstream-dependency-direction | Only legal planned directions hold; context->starter/model and model->starter remain absent; tests do... | FD | R`
`CASE-P2-TD-ATOMIC-PUBLICATION-001 | PUB | fx:atomic-publication | a:atomic-publication | COMPILER publishes whole immutable Context candidate atomically or leaves previous Context fully visible. | F0 | C`
`CASE-P2-TD-CONTEXT-ISOLATION-001 | PUB | fx:context-isolation | a:context-isolation | Published Context snapshots are immutable/isolated; no global/default mutable current Context appears. | F0 | R`
`CASE-P2-TD-POLICY-INDEX-PUBLICATION-001 | PUB | fx:policy-index-publication | a:policy-index-publication | PolicyIndex appears only as part of a complete validated candidate; no partial row/runtime repair. | F0 | C`
`CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001 | DIAG | fx:diagnostic-determinism | a:diagnostic-determinism | Equivalent failures produce stable deterministic diagnostics with no object/hash/iteration-order leakage. | F0 | R`
`CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001 | DIAG | fx:runtime-denial-diagnostic-determinism | a:runtime-denial-diagnostic-determinism | Runtime denials are stable and non-sensitive; no ModelData contents/object identity leakage. | F0 | R`
`CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001 | FIXTURE | fx:dynamic-classifier-real | a:dynamic-classifier-real | Real P1-style fixture reaches actual compiler classifier/plan; no test-only static-allow shortcut. | F0 | R`
`CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001 | FIXTURE | fx:source-to-read-write-operation | a:source-to-read-write-operation | Real config reaches compiler plan->trusted frame->resolver/Guard->MODEL effect. | FW | R`
`CASE-P2-TD-DECLARATION-BOUNDARY-001 | COMPAT | fx:declaration-boundary | a:declaration-boundary | Current P2 declaration/module boundary preserves active P1 compatibility and introduces no retired mod... | F0 | R`

Critical: target substitution must prove trusted handle A(plan A, internal ModelData A) succeeds while plan A + handle/model B, handle swap/rebind, or cross-frame relabel is impossible through public API or denied before capability/Guard/MODEL effect.
API split is normative: `API_CTX` tests CONTEXT only; `API_MODEL` tests MODEL trusted provenance/session contracts; `API_STARTER` tests STARTER composition/resolver/entry plus legal CONTEXT+MODEL consumption. Tests must not add reverse dependencies.

Gate: `risk_detection.json=NOT_SCANNED`; current execution Evidence none; same-revision BusinessFlow/Impact/API/Architecture/Develop/CrossModule/Concurrency/TestDesign/TDD Review + risk Evidence required. Implementation Plan/TDD/Development BLOCKED.