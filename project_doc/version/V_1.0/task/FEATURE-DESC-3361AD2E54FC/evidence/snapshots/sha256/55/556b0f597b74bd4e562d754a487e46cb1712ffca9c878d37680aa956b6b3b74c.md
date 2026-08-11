# P2 TestDesign R32

`TESTDESIGN-P2-R32`; base R31; inputs `REQAN-P2-R01@d08612768131 + Overlay R04 + BM-R20 + FLOW-R11 + P2-IMPACT-R29 + DESIGN-P2-R30`.
Status `IN_REVIEW`. **101 blocking Cases -> 23 exact TestClasses**.

## Exact RED registry

`BOOT(m)=./mvnw -pl m -am -Dmaven.test.skip=true install`
`RED(m,c)=./mvnw -pl m -Dtest=c -Dsurefire.failIfNoSpecifiedTests=true test` (no `-am`). Pre-assert compile/setup/missing-class failure=`INVALID_RED`.

`Key | Module | TestClass | Planned source | Commands`
`DAG | dec-core-compiler | P2RevisionDependencyDagContractTest | dec-core-compiler/src/test/java/dec/core/compiler/contract/P2RevisionDependencyDagContractTest.java | BOOT(dec-core-compiler); RED(dec-core-compiler,P2RevisionDependencyDagContractTest)`
`SYSTEM | dec-core-compiler | SystemCompilationContractTest | dec-core-compiler/src/test/java/dec/core/compiler/system/SystemCompilationContractTest.java | BOOT(dec-core-compiler); RED(dec-core-compiler,SystemCompilationContractTest)`
`RULEVIEW | dec-core-compiler | RuleViewCompilationContractTest | dec-core-compiler/src/test/java/dec/core/compiler/ruleview/RuleViewCompilationContractTest.java | BOOT(dec-core-compiler); RED(dec-core-compiler,RuleViewCompilationContractTest)`
`TARGET | dec-core-compiler | TargetKeyModelPathContractTest | dec-core-compiler/src/test/java/dec/core/compiler/model/access/TargetKeyModelPathContractTest.java | BOOT(dec-core-compiler); RED(dec-core-compiler,TargetKeyModelPathContractTest)`
`POLICY | dec-core-compiler | ModelAccessPolicyContractTest | dec-core-compiler/src/test/java/dec/core/compiler/model/access/ModelAccessPolicyContractTest.java | BOOT(dec-core-compiler); RED(dec-core-compiler,ModelAccessPolicyContractTest)`
`API_CTX | dec-core-context | ProtectedAccessContextApiContractTest | dec-core-context/src/test/java/dec/core/context/runtime/ProtectedAccessContextApiContractTest.java | BOOT(dec-core-context); RED(dec-core-context,ProtectedAccessContextApiContractTest)`
`API_COMPILER | dec-core-compiler | P2CompilerContextConstructibilityContractTest | dec-core-compiler/src/test/java/dec/core/compiler/contract/P2CompilerContextConstructibilityContractTest.java | BOOT(dec-core-compiler); RED(dec-core-compiler,P2CompilerContextConstructibilityContractTest)`
`API_MODEL | dec-core-model | ProtectedAccessModelApiContractTest | dec-core-model/src/test/java/dec/core/model/runtime/ProtectedAccessModelApiContractTest.java | BOOT(dec-core-model); RED(dec-core-model,ProtectedAccessModelApiContractTest)`
`API_STARTER | dec-core-starter | ProtectedAccessStarterApiContractTest | dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessStarterApiContractTest.java | BOOT(dec-core-starter); RED(dec-core-starter,ProtectedAccessStarterApiContractTest)`
`MATERIALIZE | dec-core-model | RuntimeModelMaterializationIntegrationTest | dec-core-model/src/test/java/dec/core/model/runtime/RuntimeModelMaterializationIntegrationTest.java | BOOT(dec-core-model); RED(dec-core-model,RuntimeModelMaterializationIntegrationTest)`
`VALUE | dec-core-context | RuntimeFactValueContractTest | dec-core-context/src/test/java/dec/core/context/runtime/RuntimeFactValueContractTest.java | BOOT(dec-core-context); RED(dec-core-context,RuntimeFactValueContractTest)`
`ID | dec-core-context | OpaqueRuntimeIdContractTest | dec-core-context/src/test/java/dec/core/context/runtime/OpaqueRuntimeIdContractTest.java | BOOT(dec-core-context); RED(dec-core-context,OpaqueRuntimeIdContractTest)`
`INTENT | dec-core-starter | ProtectedWriteIntentResolutionTest | dec-core-starter/src/test/java/dec/core/starter/access/ProtectedWriteIntentResolutionTest.java | BOOT(dec-core-starter); RED(dec-core-starter,ProtectedWriteIntentResolutionTest)`
`ADAPTER | dec-core-starter | ProtectedRuntimeModelAdapterIntegrationTest | dec-core-starter/src/test/java/dec/core/starter/access/ProtectedRuntimeModelAdapterIntegrationTest.java | BOOT(dec-core-starter); RED(dec-core-starter,ProtectedRuntimeModelAdapterIntegrationTest)`
`LOCATOR | dec-core-model | RuntimeObjectLocatorIntegrationTest | dec-core-model/src/test/java/dec/core/model/runtime/RuntimeObjectLocatorIntegrationTest.java | BOOT(dec-core-model); RED(dec-core-model,RuntimeObjectLocatorIntegrationTest)`
`TXN | dec-core-model | ProtectedWriteTransactionIntegrationTest | dec-core-model/src/test/java/dec/core/model/runtime/ProtectedWriteTransactionIntegrationTest.java | BOOT(dec-core-model); RED(dec-core-model,ProtectedWriteTransactionIntegrationTest)`
`COMPOSE | dec-core-starter | ProtectedAccessProductionCompositionTest | dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessProductionCompositionTest.java | BOOT(dec-core-starter); RED(dec-core-starter,ProtectedAccessProductionCompositionTest)`
`CONC | dec-core-starter | ProtectedAccessConcurrencyTest | dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessConcurrencyTest.java | BOOT(dec-core-starter); RED(dec-core-starter,ProtectedAccessConcurrencyTest)`
`DEP | dec-core-starter | ProtectedAccessDependencyDirectionTest | dec-core-starter/src/test/java/dec/core/starter/architecture/ProtectedAccessDependencyDirectionTest.java | BOOT(dec-core-starter); RED(dec-core-starter,ProtectedAccessDependencyDirectionTest)`
`PUB | dec-core-compiler | AtomicPublicationContractTest | dec-core-compiler/src/test/java/dec/core/compiler/publication/AtomicPublicationContractTest.java | BOOT(dec-core-compiler); RED(dec-core-compiler,AtomicPublicationContractTest)`
`DIAG | dec-core-compiler | P2DiagnosticDeterminismTest | dec-core-compiler/src/test/java/dec/core/compiler/diagnostic/P2DiagnosticDeterminismTest.java | BOOT(dec-core-compiler); RED(dec-core-compiler,P2DiagnosticDeterminismTest)`
`FIXTURE | dec-demo | P2RealFixtureIntegrationTest | dec-demo/src/test/java/dec/demo/p2/P2RealFixtureIntegrationTest.java | BOOT(dec-demo); RED(dec-demo,P2RealFixtureIntegrationTest)`
`COMPAT | dec-core-compiler | P2DeclarationCompatibilityContractTest | dec-core-compiler/src/test/java/dec/core/compiler/compat/P2DeclarationCompatibilityContractTest.java | BOOT(dec-core-compiler); RED(dec-core-compiler,P2DeclarationCompatibilityContractTest)`

## 101 blocking oracles

For Case suffix `X`, fixture=`fx:x` and action=`a:x` (lowercase slug); each row freezes its observable Expected/Forbidden.

`Case | ClassKey | Expected | Forbidden | Ref`
`CASE-P2-TD-CONTEXT-MATERIALIZATION-INDEX-AGGREGATE-001 | API_CTX | index in CompiledModelSet equality/hash/digest; EngineContext delegates | no side index/digest omission | CURRENT-R32`
`CASE-P2-TD-MATERIALIZATION-PUBLICATION-CLOSURE-001 | PUB | missing/duplicate target descriptor blocks publish; old Context stays | no runtime repair/partial publish | CURRENT-R32`
`CASE-P2-TD-ATOMIC-PUBLICATION-001 | PUB | complete candidate publishes atomically or old Context remains | no partial aggregate visibility | CURRENT-R32`
`CASE-P2-TD-PRODUCTION-LOAD-REQUEST-001 | MATERIALIZE | request A -> plan validate -> descriptor -> ModelData A -> 3arg loader -> real Container -> Handle A/scope | no request authority/default Context/existing ModelData/caller Container | CURRENT-R32`
`CASE-P2-TD-PRODUCTION-LOAD-PLAN-MISMATCH-001 | MATERIALIZE | Plan B absent in Context A -> PLAN_NOT_IN_CAPTURED_CONTEXT; creation/load/scope/Guard/effect=0 | no repair/fallback/partial handle | CURRENT-R32`
`CASE-P2-TD-PRODUCTION-MODELDATA-IDENTITY-001 | MATERIALIZE | factory A == loader A == Container A == Handle/session/effect A | no create/load A then freeze/effect B | CURRENT-R32`
`CASE-P2-TD-PRODUCTION-CONTAINER-TRUST-BOUNDARY-001 | MATERIALIZE | production(context,kind) -> MODEL ContainerFactory real Container | no production(context,fakeContainer)/fake evidence | CURRENT-R32`
`CASE-P2-TD-MODEL-EFFECT-PROVIDER-BINDING-001 | COMPOSE | same sealed session binds provider; foreign/unsealed/closed -> stable failure/no composition | no caller provider/port/preseal bind/fallback | CURRENT-R32`
`CASE-P2-TD-MODEL-EFFECT-SAME-HANDLE-001 | ADAPTER | resolved A + Guard A -> port rechecks same session/object/handle -> effect A | no STEP03 A -> STEP06 B | CURRENT-R32`
`CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001 | ADAPTER | public production/consumer APIs expose no provider/port/Guard injection or getter | no business effect replacement/bypass | CURRENT-R32`
`CASE-P2-TD-RUNTIME-TARGET-SUBSTITUTION-001 | COMPOSE | A resolves/Guards/effects A; substitute B fails before protected effect | no A->B repair/first-match/rebind | CURRENT-R32`
`CASE-P2-TD-REVISION-DAG-001 | DAG | authority REQ+R04->BM-R20->FLOW-R11->DESIGN-R30->TEST-R32; Impact R29 parallel | no stale authority/lifecycle rewrite | CURRENT-R32`
`CASE-P2-TD-SYSTEM-DETERMINISM-001 | SYSTEM | source-order variants compile same SystemKey set/digest | no order-dependent identity | CURRENT-R32`
`CASE-P2-TD-SYSTEM-DUPLICATE-001 | SYSTEM | duplicate System -> stable error; publish=0 | no first/last wins | CURRENT-R32`
`CASE-P2-TD-SYSTEM-FORWARD-REF-001 | SYSTEM | legal forward ref resolves; missing/ambiguous -> stable error; publish=0 | no eager-order/global fallback | CURRENT-R32`
`CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001 | SYSTEM | published ownership unaffected by later source mutation | no mutable source alias | CURRENT-R32`
`CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001 | SYSTEM | version change changes exact identity/digest | no version elision/case folding | CURRENT-R32`
`CASE-P2-TD-BM-CANONICAL-PAIR-001 | SYSTEM | human+canonical BM both BM-R20 with same target/path/RW/Guard semantics | no BM-R07/Design seam in BM | CURRENT-R32`
`CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001 | RULEVIEW | RuleView without System -> compile error; publish=0 | no default/bare System | CURRENT-R32`
`CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001 | RULEVIEW | same System+local RuleView duplicate -> stable error | no first/last duplicate | CURRENT-R32`
`CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001 | RULEVIEW | same local name under two Systems -> two distinct composite keys | no cross-System collision | CURRENT-R32`
`CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001 | RULEVIEW | exact owning System+View resolves; 0/N -> compile error | no bare View fallback | CURRENT-R32`
`CASE-P2-TD-RULEKEY-CONTRACT-001 | RULEVIEW | RuleKey equality = owner RuleViewKey + local rule | no global/casefold alias | CURRENT-R32`
`CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001 | RULEVIEW | lookup requires exact System-qualified key | no local-name-only first match | CURRENT-R32`
`CASE-P2-TD-KEY-SOURCE-COMPAT-001 | RULEVIEW | P1 compatibility maps to same shared View/RuleView identities | no second identity namespace | CURRENT-R32`
`CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001 | RULEVIEW | legacy bare name confined to P1 adapter boundary | no new P2 authority by bare name | CURRENT-R32`
`CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001 | TARGET | sourceModel shared ViewKey -> TargetKey; owner System separate | no owner folded into target | CURRENT-R32`
`CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001 | TARGET | TargetKey and canonical ModelPath independent immutable values | no path-in-target inference | CURRENT-R32`
`CASE-P2-TD-MODEL-PATH-UNKNOWN-001 | TARGET | unknown segment -> compile error | no runtime/best-effort repair | CURRENT-R32`
`CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001 | TARGET | supported wildcard -> finite deterministic exact paths at compile | no wildcard reaches runtime | CURRENT-R32`
`CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001 | TARGET | Rule/Change/CustomAction get identical canonical path | no consumer-specific normalization | CURRENT-R32`
`CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001 | TARGET | P1 path maps losslessly to READ/WRITE-only P2 | no EXECUTE synthesis/path loss | CURRENT-R32`
`CASE-P2-TD-NESTED-OBJECT-PATH-001 | TARGET | user.authInfo compiles as canonical exact segments [user,authInfo] when both segments exist and user is composite | no flattening/root guessing/runtime repair | CURRENT-R32`
`CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001 | TARGET | user.authInfo.role compiles as canonical exact segments [user,authInfo,role] when each intermediate is composite | no truncation/prefix fallback | CURRENT-R32`
`CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001 | TARGET | user.id.value fails at compile time when id is a leaf/non-composite segment | no runtime/best-effort repair | CURRENT-R32`
`CASE-P2-TD-NESTED-COLLECTION-PATH-001 | TARGET | payInfo.payDetailList.productId navigates the compiled object/collection path catalog to one canonical exact ModelPath | no wildcard/string-only fallback | CURRENT-R32`
`CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001 | TARGET | with target-main=user, selector user.authInfo is not interpreted as target-main(user)+property(authInfo); target-main exact match and property-root traversal remain separate selectors | no target-main prefix consumption | CURRENT-R32`
`CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001 | POLICY | READ user does not authorize READ user.authInfo unless the child exact ModelAccessRuleKey exists (or source READ wildcard was compile-time expanded to that exact path) | no parent/prefix/ancestor runtime permission fallback | CURRENT-R32`
`CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001 | POLICY | READ and WRITE independently follow exact ModelAccessRuleKey | no implicit/EXECUTE permission | CURRENT-R32`
`CASE-P2-TD-NO-EXECUTE-CONTRACT-001 | POLICY | AccessOperation exactly READ,WRITE; EXECUTE rejected | no hidden compatibility EXECUTE | CURRENT-R32`
`CASE-P2-TD-STATIC-DENY-001 | POLICY | static unauthorized access rejected before Guard/capability/effect | no Guard widening static deny | CURRENT-R32`
`CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001 | POLICY | only frozen valid static/dynamic tuples publish | no incomplete/mixed tuple | CURRENT-R32`
`CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001 | POLICY | dynamic selector compiles once to exact binding/plan used unchanged | no runtime selector parse | CURRENT-R32`
`CASE-P2-TD-RUNTIME-BINDING-PROOF-001 | POLICY | proof derives exact published plan + same Handle provenance | no caller text/metadata proof | CURRENT-R32`
`CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001 | POLICY | plan != Handle provenance -> deny before capability/Guard/effect | no plan repair/relabel | CURRENT-R32`
`CASE-P2-TD-CONTEXT-API-SELF-CONTAINED-001 | API_CTX | CONTEXT public factories/materialization compile without MODEL/STARTER | no reverse dependency/reflection-only pass | CURRENT-R32`
`CASE-P2-TD-MODEL-API-SELF-CONTAINED-001 | API_MODEL | MODEL direct request/root/scope/session/effect API compiles; token credential not current | no required token/replay/caller Container | CURRENT-R32`
`CASE-P2-TD-STARTER-API-SELF-CONTAINED-001 | API_STARTER | STARTER composition/resolver/entry contracts compile via legal deps | no hidden factory/reverse dep | CURRENT-R32`
`CASE-P2-TD-RUNTIME-FACT-VALUE-DOMAIN-001 | VALUE | value domain exactly NULL/BOOL/INTEGER/DECIMAL/STRING/LIST/OBJECT | no arbitrary live Object kind | CURRENT-R32`
`CASE-P2-TD-RUNTIME-FACT-VALUE-DEEP-IMMUTABILITY-001 | VALUE | nested source mutation cannot change frozen value/json | no collection alias | CURRENT-R32`
`CASE-P2-TD-OPAQUE-RUNTIME-ID-VALUE-CONTRACT-001 | ID | IDs reject blank; exact case-sensitive structural equality | no authority/casefold/numeric inference | CURRENT-R32`
`CASE-P2-TD-WRITE-INTENT-NOT-FOUND-001 | INTENT | 0 intent -> WRITE_INTENT_NOT_FOUND before capability/Guard/effect | no fallback intent | CURRENT-R32`
`CASE-P2-TD-WRITE-INTENT-AMBIGUOUS-001 | INTENT | N>1 intent -> WRITE_INTENT_AMBIGUOUS; select none | no first/random intent | CURRENT-R32`
`CASE-P2-TD-WRITE-INTENT-FREEZE-STABILITY-001 | INTENT | one intent freezes target/path/stamp before Guard | no postfreeze reselection/version refresh | CURRENT-R32`
`CASE-P2-TD-WRITE-AUTHORITY-MODEL-ACCESS-RULEKEY-001 | INTENT | WRITE permission solely exact ModelAccessRuleKey | no RuleKey/consumer widening | CURRENT-R32`
`CASE-P2-TD-WRITE-SINGLE-PATH-AUTHORITY-001 | INTENT | WRITE intent/effect carry one exact rule ModelPath | no second path authority | CURRENT-R32`
`CASE-P2-TD-TYPED-RUNTIME-CONTEXT-001 | INTENT | invocation frame/owner/cursor typed IDs equal MODEL-minted facts | no raw/sentinel/caller frame authority | CURRENT-R32`
`CASE-P2-TD-MUTATION-STAMP-OBJECT-BINDING-001 | INTENT | stamp session/object/path/version from same resolved object | no object/version substitution | CURRENT-R32`
`CASE-P2-TD-REAL-READ-OPERATION-001 | ADAPTER | Guard ALLOW -> private port reads same registered handle/path/session | no preGuard/foreign read/caller port | CURRENT-R32`
`CASE-P2-TD-REAL-WRITE-OPERATION-001 | ADAPTER | Guard ALLOW -> private port writes same handle/path once; matching receipt | no preGuard/alternate ModelData/second write | CURRENT-R32`
`CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001 | ADAPTER | real call -> request/root/real Container -> same Handle/Scope -> bind -> Guard -> operation | no fake/detached/token requirement/bypass | CURRENT-R32`
`CASE-P2-TD-RUNTIME-OBJECT-LOCATOR-SCOPE-001 | LOCATOR | locate only exact sealed-session registered objects | no global/name/foreign-session lookup | CURRENT-R32`
`CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001 | LOCATOR | missing -> NOT_FOUND; closed/stale -> STALE | no substitute/stale effect | CURRENT-R32`
`CASE-P2-TD-RUNTIME-TARGET-SELECTION-001 | LOCATOR | exact plan+scope gives 0 NOT_FOUND / 1 target / N AMBIGUOUS | no first/last match | CURRENT-R32`
`CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001 | TXN | mutation failure before success -> no receipt + RUNTIME_WRITE_FAILED | no requirement for excluded later postcopy POJO restore | CURRENT-R32`
`CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001 | COMPOSE | Rule/Change/CustomAction enter STARTER guarded entries only | no direct consumer MODEL effect/load | CURRENT-R32`
`CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001 | COMPOSE | Context+active MODEL scope -> validate/register/seal/bind -> protected entries | no injected frame/session/Guard/port/Container | CURRENT-R32`
`CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001 | COMPOSE | Rule entry DENY -> effect0; ALLOW -> same bound Handle | no direct MODEL import/widening | CURRENT-R32`
`CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001 | COMPOSE | Change entry uses same resolver/capability/Guard/effect semantics | no Change-specific bypass/authority | CURRENT-R32`
`CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001 | COMPOSE | CustomAction entry uses shared guarded composition | no direct port/consumer Guard | CURRENT-R32`
`CASE-P2-TD-AC007-CONSUMER-PARITY-001 | COMPOSE | same key/frame/target -> equivalent Rule/Change/CustomAction outcome/effect | no consumer-specific path/permission | CURRENT-R32`
`CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001 | COMPOSE | consumer classes depend STARTER+CONTEXT only | no MODEL root/effect imports | CURRENT-R32`
`CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001 | COMPOSE | real config -> plan -> request/root -> real ModelData/Container -> Scope -> Guard -> effect -> writeback | no fake ModelData/Container/port/raw repair | CURRENT-R32`
`CASE-P2-TD-COMPOSITION-RUNTIME-CONTEXT-MATCH-001 | COMPOSE | invocation frame facts equal independent MODEL scope and Context contains plans | no self-asserted/global Context/relabel | CURRENT-R32`
`CASE-P2-TD-PRODUCTION-RUNTIME-REGISTRATION-BINDING-001 | COMPOSE | register exactly MODEL-loaded trusted Handles; seal once before bind | no injected existing ModelData/foreign scope/duplicate success | CURRENT-R32`
`CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001 | CONC | concurrent one capability -> at most one Guard/effect; losers consumed | no duplicate effect/reset | CURRENT-R32`
`CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001 | CONC | same Handle/path/version share one coordination domain; stale loser | no independent locks/double commit | CURRENT-R32`
`CASE-P2-TD-CROSS-SESSION-MODELDATA-OWNERSHIP-001 | CONC | same Handle conflicting second session -> OWNERSHIP_CONFLICT | no parallel ownership domains | CURRENT-R32`
`CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001 | DEP | compiler->context; model->context; starter->context+model; consumer->starter+context | no consumer->MODEL/root/effect or reverse deps | CURRENT-R32`
`CASE-P2-TD-CONTEXT-ISOLATION-001 | PUB | separate EngineContexts retain isolated immutable aggregates | no global mutable cross-context contamination | CURRENT-R32`
`CASE-P2-TD-POLICY-INDEX-PUBLICATION-001 | POLICY | policy visible only with complete matching binding/materialization candidate | no partial/missing-plan policy | CURRENT-R32`
`CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001 | DIAG | equivalent compile failure -> same source-aware code/message/order | no identity/hash-order/sensitive leak | CURRENT-R32`
`CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001 | DIAG | equivalent runtime denial -> same stable nonsensitive code/message | no ModelData/origin/JVM identity leak | CURRENT-R32`
`CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001 | FIXTURE | real config -> RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + exact plan | no fake STATIC_ALLOW/fabricated plan | CURRENT-R32`
`CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001 | FIXTURE | real config+origin -> plan -> request/root -> same ModelData -> Scope -> Guard -> READ/WRITE | no sourceSnapshot/fake/global Context/bypass | CURRENT-R32`
`CASE-P2-TD-DECLARATION-BOUNDARY-001 | COMPAT | P2 preserves active P1 compatibility; final convergence stays P7 | no retired authority/P7 pull-in | CURRENT-R32`
`CASE-P2-TD-TRUSTED-MATERIALIZATION-INPUT-001 | MATERIALIZE | MODEL lifecycle request -> validated plan+origin -> internally created trusted ModelData | no request authority/STARTER creation/existing ModelData/default connection | CURRENT-R32`
`CASE-P2-TD-TRUSTED-MATERIALIZATION-EXACT-VIEW-001 | MATERIALIZE | plan target ViewKey -> exact captured descriptor -> same loaded/frozen ModelData | no XML/YAML/ViewData/name/default Context repair | CURRENT-R32`
`CASE-P2-TD-PRODUCTION-FRAME-HANDOFF-001 | COMPOSE | successful load -> root Scope/frame; STARTER validates; root close stales scope | no caller frame/scope/stale reuse | CURRENT-R32`
`CASE-P2-TD-PRODUCTION-SESSION-HANDOFF-001 | COMPOSE | one scope session; exact Handles registered once; seal then same-scope bind | no alternate/injected/presealed session | CURRENT-R32`
`CASE-P2-TD-COMPILED-VIEW-MATERIALIZATION-PLAN-001 | MATERIALIZE | one immutable typed descriptor per dynamic target View | no runtime config parsing/permission in descriptor | CURRENT-R32`
`CASE-P2-TD-PRODUCTION-OBJECT-WRITEBACK-001 | MATERIALIZE | authorized success reaches same ModelData and existing successful origin writeback | no detached-only success; excluded late rollback not required | CURRENT-R32`
`CASE-P2-TD-RUNTIME-SCOPE-PROVENANCE-001 | COMPOSE | scope/frame IDs MODEL-minted for exact trusted Handle set | no caller relabel/global/threadlocal scope | CURRENT-R32`
`CASE-P2-TD-COMPILER-CONTEXT-CONSTRUCTIBILITY-001 | API_COMPILER | compiler constructs plans/policies/CompiledModelSet index via public contracts | no reflection/package-private side channel | CURRENT-R32`
`CASE-P2-TD-CALLER-STARTER-CONSTRUCTIBILITY-001 | API_STARTER | legal STARTER constructs invocation/resolved/result and composition from Context+Scope | no operation injection/hidden factory | CURRENT-R32`
`CASE-P2-TD-R26-FRESH-SNAPSHOT-SEAM-ABSENT-001 | MATERIALIZE | R26 sourceSnapshot/frameRequest/runtime.open types absent from current API | no second fresh-snapshot runtime | CURRENT-R32`
`CASE-P2-TD-TRUSTED-FRAME-PRECONDITION-FAILURE-MATRIX-001 | MATERIALIZE | closed/invalid-plan/missing-desc/bad-origin/container reject -> exact load failure; scope0 | no partial frame/repair/fallback/preGuard effect | CURRENT-R32`
`CASE-P2-TD-MODEL-EXECUTION-ROOT-LOAD-001 | MATERIALIZE | root request -> exact descriptor -> typed factory -> 3arg loader -> real Container -> same Handle | no token requirement/caller Container/2arg load/detach | CURRENT-R32`
`CASE-P2-TD-MODEL-SCOPE-PRODUCER-001 | MATERIALIZE | scope unavailable before load; active after success; stale after root close | no public scope/global recovery | CURRENT-R32`
`CASE-P2-TD-COMPOSITION-FAILURE-ALGEBRA-001 | COMPOSE | setup failure -> created=false, no composition, one exact code; downstream counts0 | no null/unchecked fallback | CURRENT-R32`
`CASE-P2-TD-SESSION-FAILURE-ALGEBRA-001 | API_MODEL | session inactive/closed/sealed/duplicate/ownership -> exact stable code | no duplicate success/string-only error/alternate session | CURRENT-R32`

## Mandatory assertions

- Request is transport data, not authority; MODEL production lifecycle is its trusted formation/use boundary.
- Invalid plan fails before ModelData/Container/scope; descriptor only from captured typed index.
- Same ModelData A is created, loaded, frozen, registered, resolved and affected.
- Container is MODEL-created; EffectProvider binds same sealed session; operation port stays private to STARTER.
- `resolve A -> Guard A -> effect A`; business consumers have no MODEL load/effect bypass.
- Opaque production invocation credential is `NOT_ADOPTED_IN_P2 / DEFERRED`.
- Successful originData write-back remains; excluded later post-copy POJO/Map restoration is not a blocker.

Gate: `risk_detection.json=NOT_SCANNED`; execution Evidence none; same-revision specialist Reviews + risk Evidence required. Implementation Plan/TDD/Development remain BLOCKED.


## R32 nested ModelPath clarification

R32 is a TestDesign-only increment over R31. It does not reopen P1, BM-R20, FLOW-R11 or DESIGN-P2-R30. It freezes the already-designed distinction that `target-main` is an exact root selector, not a prefix consumed from a dotted property path; nested object/collection paths are canonical segment sequences; non-composite intermediate segments fail closed; runtime authorization remains exact-only with no parent/prefix fallback. The six R32 oracles reuse existing `TARGET` / `POLICY` TestClasses, so the registry remains 23 exact TestClasses.
