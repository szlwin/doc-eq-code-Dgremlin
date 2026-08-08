# P2 Test Design R03 — Independent Review Remediation

> Revision: `TESTDESIGN-P2-R03`.
> Base: `TESTDESIGN-P2-R02@d0514b9ac591`.
> Inputs: `BM-R08`, `DESIGN-P2-R02` review-remediation deltas.
> Status: `NEEDS_REVIEW`.
> This is the normative P2 test-design delta; it extends/supersedes conflicting R02 cases without deleting historical R02 evidence.

## 1. Command contract

All repository-level future test commands use the checked-in Maven Wrapper. Bare `mvn` is not a reproducible P2 oracle for this repository.

Forbidden example from R02:

```text
mvn ... -Dtest=P2ModelAccessGuardTest + P2LegacyBoundaryArchitectureTest test
```

The `+` token is not a valid Surefire multi-test separator.

Required form when tests are in different modules is separate commands:

```bash
./mvnw -pl dec-core-context -am -Dtest=P2ModelAccessGuardTest test
./mvnw -pl dec-core-compiler -am -Dtest=P2LegacyBoundaryArchitectureTest test
```

If two test classes are in the same module, use Surefire's supported comma-separated pattern, for example:

```bash
./mvnw -pl <module> -am -Dtest=P2ModelAccessGuardTest,P2AnotherGuardTest test
```

Final TDD must replace `<module>` placeholders with the actual owning module before a case can be marked executable.

## 2. Guard no-bypass case

### CASE-P2-TD-GUARD-NO-BYPASS-001-R03

**Purpose:** prove that every protected READ/WRITE/EXECUTE reaches the same Guard seam.

Parameterized dimensions:

- caller: `RULE | CHANGE | CUSTOM_ACTION | QUERY_READ`
- operation: `READ | WRITE | EXECUTE`
- policy compilation status: `STATIC_ALLOW | RUNTIME_GUARD_REQUIRED`

Oracles:

1. Guard invocation count is exactly `1` for every protected access.
2. `STATIC_ALLOW` -> Guard returns ALLOW and RuntimeFactEvaluator invocation count is exactly `0`.
3. `RUNTIME_GUARD_REQUIRED` -> evaluator may be invoked only from Guard.
4. No caller may branch around Guard based on `PolicyCompilationStatus`.
5. DENY -> target read/mutation/execute invocation count is `0`; state version and external-effect spy are unchanged.

Future command:

```bash
./mvnw -pl dec-core-context -am -Dtest=P2ModelAccessGuardTest test
```

## 3. Fail-closed matrix

### CASE-P2-TD-RUNTIME-GUARD-FAIL-CLOSED-001-R03

The runtime seam must cover all security-significant failure modes, not only THROW/UNKNOWN.

| Scenario | Policy | Expected decision | Required reason/oracle |
|---|---|---|---|
| evaluator returns ALLOW | RUNTIME_GUARD_REQUIRED | ALLOW | evaluator called once; operation may execute |
| evaluator returns DENY | RUNTIME_GUARD_REQUIRED | DENY | operation not executed |
| evaluator throws | RUNTIME_GUARD_REQUIRED | DENY | `RUNTIME_EVALUATOR_EXCEPTION`; no side effect |
| evaluator returns null | RUNTIME_GUARD_REQUIRED | DENY | `RUNTIME_EVALUATOR_NULL`; no side effect |
| evaluator exceeds deadline | RUNTIME_GUARD_REQUIRED | DENY | `RUNTIME_EVALUATOR_TIMEOUT`; no side effect |
| evaluator result undecidable/unknown | RUNTIME_GUARD_REQUIRED | DENY | `RUNTIME_EVALUATOR_UNKNOWN`; no side effect |
| evaluator unavailable | RUNTIME_GUARD_REQUIRED | DENY | `RUNTIME_EVALUATOR_UNAVAILABLE`; no side effect |
| Guard unavailable/not composed | any protected policy | DENY / access cannot start | `GUARD_UNAVAILABLE`; protected operation not executed |
| EngineContext identity differs from Guard/policy index | any | DENY | `CONTEXT_IDENTITY_MISMATCH`; evaluator not called |
| exact policy missing / permission cannot be determined | any | DENY | `POLICY_NOT_FOUND`; evaluator not called |
| STATIC_ALLOW | STATIC_ALLOW | ALLOW | Guard called once; evaluator called zero times |

Timeout test uses an injected deterministic time source/fake executor controlled by the Guard test seam; tests must not rely on arbitrary wall-clock sleeps.

Future command:

```bash
./mvnw -pl dec-core-context -am -Dtest=P2ModelAccessGuardFailClosedTest test
```

## 4. RuleView System-required diagnostic

### CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001-R03

Given a new RuleView without an explicit System owner, compilation must produce `MIX-RULEVIEW-SYSTEM-REQUIRED`, map to BM error `ERR-MIX-RULEVIEW-SYSTEM-REQUIRED`, publish no candidate Context, and perform no bare-name fallback.

```bash
./mvnw -pl dec-core-compiler -am -Dtest=P2RuleViewSystemRequiredTest test
```

## 5. Legacy declaration boundary architecture case

### CASE-P2-TD-LEGACY-DECLARATION-BOUNDARY-001-R03

Oracles:

- `DEC-EXPAND-DECLARATION` is absent from reactor/module dependencies and is never restored.
- allowed legacy surface is limited to read-only compatibility around `ConfigInfo.getRuleViewInfo(String)` / `DataUtil.getRuleViewInfo(String)` until P7.
- legacy read adapters cannot register `RuleViewKey`, `CompiledSystem`, `ModelAccessRule`, or a second runtime authority.

```bash
./mvnw -pl dec-core-compiler -am -Dtest=P2LegacyBoundaryArchitectureTest test
```

## 6. Review gate for this revision

Before `TESTDESIGN-P2-R03` may be PASSED, execute independent Review against this exact revision by:

- `RequirementReviewAgent`
- `DesignReviewAgent`
- `TDDReviewAgent`
- `TestEvidenceReviewAgent`

Security risk must also be handled at an RC9-allowed stage; the fail-closed matrix above is required security evidence, not a substitute for an independent reviewer when the contract requires one.

Until all P0/P1 findings are closed against the new revision, `IMPLEMENTATION_PLAN` remains `BLOCKED`; no TDD or Development work is authorized.
