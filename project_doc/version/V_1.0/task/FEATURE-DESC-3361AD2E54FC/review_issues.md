# FEATURE-DESC-3361AD2E54FC Review 问题台账

> 2026-08-09 user-authorized design simplification against PR #36 frozen head `95a8398d4c40eda3c50411452c34a3ee76dd454e` / tree `7a28838596410b1c4f2b7864edf77902b1bff5cc`。用户明确要求撤销 execution-token 模型，采用 `bridge.execute(ruleKey, operation, frameId, ...)` direct-argument API，并暂不把 caller 可选择 rule/op 的 authority 扩大作为阻断问题。该决策使上一轮基于 `recognizes(token)` 的 atomic-claim/replay concern 不再适用于当前候选；不新增 FND-020。Existing FND-001..019 全部保持 formal OPEN；effective OPEN P1 = **19**。Candidate `DESIGN-P2-R13 / TESTDESIGN-P2-R14` 不是 closure Evidence，exact specialist Review 与 RC9 machine lifecycle 仍 mandatory。

```json review-issues
[
  {"id":"FND-P2-REV-001","severity":"P1","status":"OPEN","phase":"business_model","title":"Guard coverage narrower than requirement","decision":"FIX_PROPOSED","resolution_revision":"BM-R12 / DESIGN-P2-R13 / TESTDESIGN-P2-R14","defer_reason":"unified Guard path candidate retained; exact machine/specialist Review absent"},
  {"id":"FND-P2-REV-002","severity":"P1","status":"OPEN","phase":"business_model","title":"Business Model misses RuleView System-required error","decision":"FIX_PROPOSED","resolution_revision":"BM-R12","defer_reason":"Business Model candidate not machine-current"},
  {"id":"FND-P2-REV-003","severity":"P1","status":"OPEN","phase":"business_model","title":"P2 declaration boundary points at retired P1 module","decision":"FIX_PROPOSED","resolution_revision":"BM-R12 / DESIGN-P2-R13","defer_reason":"exact Impact/CrossModule review pending"},
  {"id":"FND-P2-REV-004","severity":"P1","status":"OPEN","phase":"design","title":"P2 API contract not implementation-ready","decision":"PARTIAL_FIX_PROPOSED","resolution_revision":"DESIGN-P2-R13 / TESTDESIGN-P2-R14","resolution_evidence":"R13 exposes direct public ProtectedExecutionBridge.execute(ruleKey,operation,frame,owner,cursor), removes external issued-pair/token prerequisite, retains internal issuance/Guard/capability binding; R14 adds direct API/reachability/argument/concurrency cases","defer_reason":"candidate direct API is explicit but exact ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Review and implementation Evidence remain absent"},
  {"id":"FND-P2-REV-005","severity":"P1","status":"OPEN","phase":"design","title":"Risk detection and specialist Review are not machine-closed","decision":"BLOCKED_PENDING_REVIEW","resolution_revision":"DESIGN-P2-R13","defer_reason":"risk_detection remains NOT_SCANNED; exact reviews/waiver absent"},
  {"id":"FND-P2-REV-006","severity":"P1","status":"OPEN","phase":"test_design","title":"Formal future Maven command unreliable in reactor","decision":"FIX_PROPOSED","resolution_revision":"TESTDESIGN-P2-R14","defer_reason":"exact module/TestClass commands frozen; execution not yet legal"},
  {"id":"FND-P2-REV-007","severity":"P1","status":"OPEN","phase":"test_design","title":"Fail-closed matrix incomplete","decision":"FIX_PROPOSED","resolution_revision":"TESTDESIGN-P2-R14","resolution_evidence":"R14 covers direct argument invalidity, policy/proof/adapter/context/stale/target substitution and capability replay; execution-token replay matrix intentionally removed by user-authorized API decision","defer_reason":"exact R14 Review and later execution Evidence pending"},
  {"id":"FND-P2-REV-008","severity":"P1","status":"OPEN","phase":"design","title":"Frozen P2 Java API violates Java8/existing EngineContext compatibility","decision":"FIX_PROPOSED","resolution_revision":"DESIGN-P2-R13 / TESTDESIGN-P2-R14","defer_reason":"Java8 additive API candidate; exact ApiContract review pending"},
  {"id":"FND-P2-REV-009","severity":"P1","status":"OPEN","phase":"design","title":"Selected dynamic requirement not delivered to evaluator","decision":"FIX_PROPOSED","resolution_revision":"DESIGN-P2-R13 / TESTDESIGN-P2-R14","defer_reason":"selected exact rule/plan routed to verifier; exact review pending"},
  {"id":"FND-P2-REV-010","severity":"P1","status":"OPEN","phase":"design","title":"Real read path=* conflicts with exact ModelPath runtime semantics","decision":"FIX_PROPOSED","resolution_revision":"DESIGN-P2-R13 / TESTDESIGN-P2-R14","defer_reason":"compile-time finite exact expansion retained; exact review pending"},
  {"id":"FND-P2-REV-011","severity":"P1","status":"OPEN","phase":"design","title":"RuntimeFactValue not truly framework-closed","decision":"FIX_PROPOSED","resolution_revision":"DESIGN-P2-R13 / TESTDESIGN-P2-R14","defer_reason":"closed immutable typed-value contract carried forward; exact review pending"},
  {"id":"FND-P2-REV-012","severity":"P1","status":"OPEN","phase":"test_design","title":"Test Design did not guarantee a valid TDD RED","decision":"FIX_PROPOSED","resolution_revision":"TESTDESIGN-P2-R14","defer_reason":"exact target-only RED contract retained; execution blocked"},
  {"id":"FND-P2-REV-013","severity":"P1","status":"OPEN","phase":"business_model","title":"Current revisions are not materialized as canonical artifacts","decision":"PARTIAL_FIX_PROPOSED","resolution_revision":"BM-R12 / DESIGN-P2-R13 / TESTDESIGN-P2-R14","defer_reason":"canonical BM and task_state remain historical until real RC9 reopen/publish"},
  {"id":"FND-P2-REV-014","severity":"P1","status":"OPEN","phase":"design","title":"DESIGN-R05 makes AC-006 legal dynamic access unreachable","decision":"FIX_PROPOSED","resolution_revision":"BM-R12 / DESIGN-P2-R13 / TESTDESIGN-P2-R14","defer_reason":"production classifier/runtime/direct bridge path makes legal dynamic access reachable; exact review pending"},
  {"id":"FND-P2-REV-015","severity":"P1","status":"OPEN","phase":"design","title":"RuntimeAccessRequirement construction API conflicts with module boundary","decision":"PARTIAL_FIX_PROPOSED","resolution_revision":"DESIGN-P2-R13 / TESTDESIGN-P2-R14","resolution_evidence":"validated ModelAccessPolicyIndex.empty/of(Iterable), explicit CompiledModelSet.published path, legacy 8-arg empty-policy semantics and digest-bound publication ordering retained from R12","defer_reason":"candidate accepted direction; exact ApiContract/Impact/CrossModule review not yet closed"},
  {"id":"FND-P2-REV-016","severity":"P1","status":"OPEN","phase":"test_design","title":"Test Design does not prove AC-006 Source-to-runtime reachability","decision":"PARTIAL_FIX_PROPOSED","resolution_revision":"DESIGN-P2-R13 / TESTDESIGN-P2-R14","resolution_evidence":"R13 replaces token/receiver flow with public direct bridge reachable from dec-demo; R14 requires dec-demo production E2E without reflection/package-private/test backdoor","defer_reason":"candidate reachability path explicit; exact review/execution pending"},
  {"id":"FND-P2-REV-017","severity":"P1","status":"OPEN","phase":"design","title":"RuntimeAccessBinding cannot prove AC-006 runtime object binding","decision":"PARTIAL_FIX_PROPOSED","resolution_revision":"BM-R12 / DESIGN-P2-R13 / TESTDESIGN-P2-R14","defer_reason":"runtime membership proof + capability-bound target remain candidate; exact review pending"},
  {"id":"FND-P2-REV-018","severity":"P1","status":"OPEN","phase":"design","title":"DynamicBindingClassification production decision rule is not frozen","decision":"FIX_PROPOSED","resolution_revision":"BM-R12 / DESIGN-P2-R13 / TESTDESIGN-P2-R14","defer_reason":"production classifier + real fixture retained; exact Review pending"},
  {"id":"FND-P2-REV-019","severity":"P1","status":"OPEN","phase":"design","title":"Runtime binding proof is not atomically bound to the actual protected operation target","decision":"FIX_PROPOSED","resolution_revision":"BM-R12 / DESIGN-P2-R13 / TESTDESIGN-P2-R14","resolution_evidence":"R13 removes execution-token/recognizes semantics entirely; FND-019 scope returns to one-shot capability binding of exact actual target + operation port with stale revalidation and A/B substitution denial; R14 tests capability replay/concurrency only","defer_reason":"candidate target/operation atomic-binding fix remains formal OPEN pending exact Review/machine closure"}
]
```

## 当前 Gate

- Requirement Analysis `REQAN-P2-R01`: **VALID**。
- Business Model `BM-R12`: **CONTENT_ACCEPTABLE / NOT_YET_CANONICAL / MACHINE_BLOCKED**。
- Reviewed Design `DESIGN-P2-R12`: superseded by user-authorized API simplification。
- New Design candidate `DESIGN-P2-R13`: **NEEDS_REVIEW / MACHINE_BLOCKED**。
- New Test Design candidate `TESTDESIGN-P2-R14`: **BLOCKED_BY_DESIGN_REVIEW / NEEDS_REVIEW / MACHINE_BLOCKED**。
- P0 on reviewed head `95a8398d...`: **0** / Build Gate #1488 SUCCESS。
- Effective OPEN P1: **19**。
- FND-020: **not created**。
- Implementation Plan / TDD / Development: **BLOCKED**。

## User-authorized direct-call decision

Current R13 intentionally accepts this shape：

```java
bridge.execute(
    orderStatusRuleKey,
    READ,
    frameId,
    ownerResolutionId,
    optionalCursorId);
```

No execution token、`recognizes()`、token claim/replay gate。Same arguments called twice are two independent invocations；only each produced capability is one-shot。Caller-selected rule/op authority hardening is explicitly deferred by current user instruction and is not recorded as a new P1/FND-020 in this revision。