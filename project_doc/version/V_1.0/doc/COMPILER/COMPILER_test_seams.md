# COMPILER P2 设计测试接缝

> Revision：`DESIGN-P2-R14`。Formal Test Design candidate：`TESTDESIGN-P2-R15`。
> 本 Revision 恢复 AC-001～AC-010 的 System/RuleView/permission/publication/diagnostic/migration seams，并保留 direct bridge + PolicyIndex runtime remediation。No skeleton / no TDD execution。

## 1. Exact module seams

| Seam | Module | Planned production owner |
|---|---|---|
| SystemKey / RuleViewKey / immutable compiled facts | `dec-core-context` | context model |
| System/RuleView/path/access compiler | `dec-core-compiler` | compiler/pass/modelaccess |
| PolicyIndex factory/publication/digest | `dec-core-context` + `dec-core-compiler` | access + compiled pipeline |
| Direct bridge/runtime/Guard/Gateway | `dec-core-starter` | `dec.core.starter.access.*` |
| real configuration E2E | `dec-demo` | existing mix fixture |

## 2. AC-001 System deterministic compile

Required cases：

- `CASE-P2-TD-SYSTEM-DETERMINISM-001`
- `CASE-P2-TD-SYSTEM-DUPLICATE-001`
- `CASE-P2-TD-SYSTEM-FORWARD-REF-001`

Oracles：same semantic source with different discovery order -> same SystemKey set + same semantic digest；duplicate exact key -> `MIX-SYSTEM-DUPLICATE` and no publication；forward refs resolve only after complete symbol registration。

## 3. AC-002 RuleView System ownership/isolation

- `CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001`
- `CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001`
- `CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001`

Missing System -> `MIX-RULEVIEW-SYSTEM-REQUIRED`。Same System/name duplicate -> ERROR。Cross-System same local name -> two distinct RuleViewKey values and no contamination。

## 4. AC-003 composite lookup/call

- `CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001`
- `CASE-P2-TD-RULEVIEW-BARE-NAME-REJECT-001`
- `CASE-P2-TD-LEGACY-NO-NEW-BARE-FALLBACK-001`

Production lookup must use `SystemKey + localName` / `system-ref + rule-ref`。No canonical `find(String)` path。Legacy read-only adapter cannot register or resolve ambiguous cross-System same name。

## 5. AC-004 READ/WRITE/EXECUTE matrix

- `CASE-P2-TD-ACCESS-READ-MATRIX-001`
- `CASE-P2-TD-ACCESS-WRITE-MATRIX-001`
- `CASE-P2-TD-ACCESS-EXECUTE-MATRIX-001`
- `CASE-P2-TD-STATIC-DENY-001`

Current Decision `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` must be applied：caller may choose another exact compiler-published ruleKey/op；that is not a forged-authority oracle in current P2。Missing key/op, undeclared WRITE/EXECUTE, invalid path or invalid rule state must still DENY/compile-error。

## 6. AC-005 ModelPath

- `CASE-P2-TD-MODEL-PATH-UNKNOWN-001`
- `CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001`

Read wildcard must expand to finite exact children before policy publication。Runtime wildcard/fuzzy/parent lookup is forbidden。

## 7. AC-006 dynamic access

- `CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R15`
- `CASE-P2-RUNTIME-BINDING-PROOF-001-R15`

Real `systems.xml` proves direct `status = 1` -> STATIC and `every(orderDetailList,status=1)` -> runtime-object-bound。Verifier proves actual element membership/provenance against exact selected plan。Unsupported dynamic selector compile ERROR。

## 8. AC-007 unified Guard/no bypass

- `CASE-P2-TD-GUARD-NO-BYPASS-001`
- `CASE-P2-STATIC-ALLOW-GUARD-PATH-001-R15`
- `CASE-P2-DIRECT-BRIDGE-REACHABILITY-001-R15`

P2 oracle：all protected operations exposed by P2 runtime go bridge -> issuance -> resolver -> Gateway -> Guard。STATIC_ALLOW still Guard=1/policy lookup=1/verifier=0。Direct Guard/Gateway operation shortcut cannot satisfy E2E。

Important：actual P3-P7 Rule/change/custom-action/query executors are not yet implemented here, so AC-007 final implementation status remains `CONTRACT_ONLY` rather than VERIFIED/PASSED。

## 9. AC-008 atomic publication / Context isolation

- `CASE-P2-TD-ATOMIC-PUBLICATION-001`
- `CASE-P2-TD-CONTEXT-ISOLATION-001`

Any System/RuleView/path/policy ERROR leaves old Context unchanged。Two successful EngineContexts retain independent immutable System/RuleView/PolicyIndex state。No global current mutable registry。

## 10. AC-009 deterministic/source-aware diagnostics

- `CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001`

Shuffle input discovery order and assert same ordered diagnostic tuples `(code, definition identity, SourceRef)`。Cover System duplicate, missing RuleView System, same-system duplicate, unknown composite ref, invalid path and denied access。

## 11. AC-010 declaration migration boundary

- `CASE-P2-TD-DECLARATION-BOUNDARY-001`

P2 must not restore retired `dec-expand-declaration`；surviving read-only declaration/System compatibility remains unchanged；no write/register into canonical System/RuleView registry；final retirement remains P7。

## 12. PolicyIndex construction/publication

- `CASE-P2-POLICY-INDEX-CONSTRUCTION-001-R15`
- `CASE-P2-POLICY-PUBLICATION-COMPATIBILITY-001-R15`
- `CASE-P2-POLICY-INDEX-PUBLICATION-001-R15`
- `CASE-P2-POLICY-INDEX-AUTHORITY-001-R15`

Must prove：validated `of(Iterable)` duplicate/state/path checks；legacy 8-arg CompiledModelSet -> empty policy；new `published(...)` retains exact same index；System/RuleView/PolicyIndex enter digest-bound input before digest；Guard lookup exactly once；no second policy map/rebuild。

## 13. Direct bridge API / argument validation

- `CASE-P2-DIRECT-BRIDGE-CONTRACT-001-R15`
- `CASE-P2-DIRECT-BRIDGE-ARGUMENT-VALIDATION-001-R15`
- `CASE-P2-DIRECT-BRIDGE-CONCURRENCY-001-R15`

API accepts exact ruleKey/op/frame/owner/optional cursor。No token/recognizes/claim API。Null/invalid/mismatched scalar facts fail before operation。Different concurrent calls must not cross-wire frame/owner/cursor/target/capability。Identical scalar calls are independent and Test Design must not assert replay suppression。

## 14. Capability target/operation binding — FND-019

- `CASE-P2-OPERATION-BINDING-001-R15`

Capability A cannot execute target B or operation B。Same capability concurrent terminal execution success <= 1。Stale Context/frame/cursor/plan/membership before operation -> DENY/effects=0。Use barriers/latches, never `Thread.sleep` as concurrency oracle。

## 15. Full real E2E

- `CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R15`

```text
real systems.xml
 -> production parser/compiler
 -> System registry + RuleView composite registry
 -> exact rules + PolicyIndex
 -> digest-bound CompiledModelSet.published
 -> EngineContext
 -> direct public bridge
 -> Gateway/Guard
 -> static or runtime-proof branch
 -> same target protected operation
```

No manual compiled rule、manual PolicyIndex bypass、manual issued pair、classifier stub or direct Guard invocation may satisfy this E2E。

## 16. Exact TestClass map

| Purpose | Module | Planned TestClass |
|---|---|---|
| System compile | `dec-core-compiler` | `dec.core.compiler.p2.SystemCompilationContractTest` |
| RuleView composite | `dec-core-compiler` | `dec.core.compiler.p2.RuleViewCompositeContractTest` |
| Model access matrix/path | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessRuleCompilationContractTest` |
| Policy factory | `dec-core-context` | `dec.core.context.model.access.ModelAccessPolicyIndexContractTest` |
| Publication compatibility | `dec-core-context` | `dec.core.context.model.ModelAccessPolicyPublicationCompatibilityTest` |
| Policy publication/digest | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest` |
| Direct bridge contract | `dec-core-starter` | `dec.core.starter.access.ProtectedExecutionBridgeContractTest` |
| Direct bridge concurrency | `dec-core-starter` | `dec.core.starter.access.ProtectedExecutionBridgeConcurrencyTest` |
| Guard authority | `dec-core-starter` | `dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest` |
| Static path | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessStaticAllowPathTest` |
| runtime proof | `dec-core-starter` | `dec.core.starter.access.RuntimeBindingProofIntegrationTest` |
| target binding | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessOperationBindingTest` |
| atomic publication/diagnostic | `dec-core-compiler` | `dec.core.compiler.p2.P2PublicationDiagnosticContractTest` |
| real classifier | `dec-demo` | `dec.demo.p2.P2DynamicClassifierRealFixtureTest` |
| direct bridge reachability | `dec-demo` | `dec.demo.p2.P2DirectBridgeReachabilityTest` |
| full E2E | `dec-demo` | `dec.demo.p2.P2DynamicSourceToOperationTest` |

## 17. Valid RED contract

For every planned target：

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

Second command MUST NOT use `-am`。Missing module/test/symbol/setup/compile failure = INVALID_RED。Before skeleton exists, API-shape cases must remain compilable through reflection/source inspection when needed。

## 18. Gate

These seams only define `DESIGN-P2-R14 / TESTDESIGN-P2-R15` candidate coverage。No TDD skeleton and no execution Evidence is claimed。Test Design remains BLOCKED_BY_DESIGN_REVIEW until exact Design Review passes。
