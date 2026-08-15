# V_1.0 项目过程

## 当前生命周期摘要

| 目标 | 当前结论 | 说明 |
|---|---|---|
| P1-COMPILER-F01 | PASSED / MERGED / ARCHIVED | 历史保持原样，不迁移重写 |
| FEATURE-DESC-3361AD2E54FC (P2) | development / FORMALIZATION_REQUIRED | DEV-01～DEV-09 实际开发已完成；RC21 先补 exact-revision Registry/StageOutcome，再进入 Final Code Review |
| P3—P8 | TODO | 未开始 |

## P2 当前有效 authority

```text
Requirement Confirmation  REQCONF-P2-R02@ef30059b327d
Requirement Analysis      REQAN-P2-R01@d08612768131
Business Model            BM-R20
Design                    DESIGN-P2-R31@685dc64b1a8bb21438440185f4a25d68d120d75f
TestDesign                TESTDESIGN-P2-R33@ac9c0aecd1bf3ebee325d88d2f1b4027d727761d
Implementation Plan       TP-FEATURE-DESC-3361AD2E54FC-R07@604f26f1641e0cf9d7d70a8ee11e90d1d1ffdf1a
TDD                       TDD-P2-R01@3f282bb4e1f6
Development closure       DEV-P2-DEV09-R09@4a82335fbdce7a56b58fd6626af0ec67a7cbebba
```

Development execution DAG 已按以下顺序完成：

```text
DEV-01 -> DEV-04 -> DEV-02 -> DEV-03 -> DEV-05 -> DEV-06 -> DEV-07 -> DEV-08 -> DEV-09
```

DEV-09 closure 已记录 `open_p0_p1=0`，并指向 `PHASE_FINAL_CODE_REVIEW`。旧 PR HEAD `4e56e5308cf674e822387c49277d93574f8db370` 的 P0 #1826 / run `31858840200` 为 SUCCESS；本次 docs-only commit 会形成新的 PR HEAD，因此该 P0 只能作为前一 HEAD 的 provenance，新 HEAD 仍需遵守 exact-HEAD gate。

## RC21 Runtime Re-baseline

P2 active runtime 已切换到 storage model 6：

```text
task.json                       static identity
task_plan.md                    remaining static execution spec
task_events.jsonl               mutable lifecycle authority
cache/*                         derived / Git ignored
evidence/legacy-runtime/...     immutable pre-RC13 provenance
```

旧 `task_state.md`、`task_attempts.md`、`stage_outcomes.md`、`review_issues.md`、`resume_context.md`、`development_tasks.md` 和旧完整 `task_plan.md` 不再留在 active task 根目录；它们以原 blob 冻结到 legacy evidence。版本级旧 `work_record.md` 同样冻结，当前 work record 重新从 RC21 re-baseline event 开始索引。

## 为什么没有把 development 直接写成 PASSED

后续 R31/R33/R07、DEV-09 closure 与独立 Review 文件是有效项目事实，但它们没有全部以 RC21 所要求的 exact-revision Evidence/Review Registry 形式进入 canonical machine registry。RC21 的 fail-closed 规则不允许通过手工状态覆盖把这个缺口伪装成 PASSED。

因此当前机器态保持：

```text
development = IN_PROGRESS / FORMALIZATION_REQUIRED
next_agent = DevelopAgent
next_action = docs/Evidence-only formalization
then = PHASE_FINAL_CODE_REVIEW
```

这一步不涉及业务代码变更；如 formalization 发现真实 P0/P1，则停止前进并进入 remediation，而不是修改状态绕过门禁。
