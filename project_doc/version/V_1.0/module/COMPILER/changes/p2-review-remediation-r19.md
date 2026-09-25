# P2 Review Remediation R19 — AC-007 Option B user decision

> Frozen input head：`d4a72e87ce91f2d07cf98fea427c73737b781e61`  
> User decision time：`2026-08-09T11:55:00+08:00`  
> Decision：`DEC-P2-AC007-STAGE-BOUNDARY-001 = ACTIVE / OPTION_B`  
> Candidate output：`REQAN-P2-R01+DEC-OVERLAY-20260809-R03 / BM-R15 / FLOW-R05 / DESIGN-P2-R17 / TESTDESIGN-P2-R18`  
> Gate：`NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`

## Decision applied

User explicitly selected Option B：P2 must provide concrete representative production consumers sufficient to execute original AC-007 rather than treating seam-only evidence as final acceptance。

R19 therefore freezes three main-source production entry categories：
- RuleProtectedAccessEntry；
- ChangeProtectedAccessEntry；
- CustomActionProtectedAccessEntry。

All three use one immutable ProtectedAccessInvocation and one ProtectedExecutionBridge authority funnel。Consumer category is provenance only and cannot alter ModelAccessRuleKey/AccessOperation/Guard semantics。

## Acceptance materialized

Current AC-007 candidate requires：
- real production Rule allow/deny；
- real production change allow/deny；
- real production custom-action allow/deny；
- same-facts consumer authorization parity；
- entry dependency/structure no-bypass；
- cross-module real production reachability；
- DENY before operation/effects；
- no reflection/package-private/manual issued-pair/capability/test-only consumer as execution evidence。

## Stage boundary retained

P2 representative entries do not implement P3 full Rule/Information semantics、P4 full Action/Produce state machine or P6 QueryPlan full execution。Those later stages must reuse the P2 Bridge/Gateway/Guard authority seam and cannot create a bypass。

## Machine truth preserved

This remediation does not update `risk_detection.json`、`task_state.md`、historical StageOutcome/Assertion/Evidence。All `FND-P2-REV-001..019` remain OPEN；no FND-020；Implementation Plan/TDD/Development remain BLOCKED until exact reviews and machine gates permit。
