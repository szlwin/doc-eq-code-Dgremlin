# V_1.0 项目过程

## 当前生命周期摘要

| 目标 | 当前结论 | 说明 |
|---|---|---|
| P1-COMPILER-F01 | PASSED / MERGED / ARCHIVED | 历史保持原样，不迁移重写 |
| FEATURE-DESC-3361AD2E54FC (P2) | development / FORMALIZATION_REQUIRED | DEV-01～DEV-09 实际开发已完成；RC23 已执行 authority reconciliation 检查，但 exact-revision Review Registry 门禁尚未闭合 |
| P3—P8 | TODO | 未开始 |

## P2 当前有效内容 authority

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

DEV-09 closure 已记录 `open_p0_p1=0`，并指向 `PHASE_FINAL_CODE_REVIEW`。旧 PR HEAD `4e56e5308cf674e822387c49277d93574f8db370` 的 P0 #1826 / run `31858840200` 为 SUCCESS；任何新的 docs-only HEAD 仍需遵守 exact-HEAD gate，旧 run 只作为 provenance。

## RC21 Runtime Re-baseline（历史基线）

P2 active runtime 已切换到 storage model 6：

```text
task.json                       static identity
task_plan.md                    remaining static execution spec
task_events.jsonl               mutable lifecycle authority
cache/*                         derived / Git ignored
evidence/legacy-runtime/...     immutable pre-RC13 provenance
```

旧 `task_state.md`、`task_attempts.md`、`stage_outcomes.md`、`review_issues.md`、`resume_context.md`、`development_tasks.md` 和旧完整 `task_plan.md` 不再留在 active task 根目录；它们以原 blob 冻结到 legacy evidence。版本级旧 `work_record.md` 同样冻结，当前 work record 从 event ledger 投影恢复。

## RC23 Reconciliation（2026-08-16）

本轮只调整 `project_doc` 的 lifecycle/runtime 兼容层，不修改 production code。

已确认：

1. standalone finalization 已证明当前内容 authority 为 Design R31、TestDesign R33、Implementation Plan R07；
2. 当前 `task_events.jsonl` 的 RC21 seed state 仍保存 R30/R32/R05 的 canonical collaboration-review 绑定；
3. `evidence/reviews.jsonl` 当前没有 DEV-09 exact revision 的 `development:TDDReviewAgent` 结论；
4. 因此不能把 R31/R33/R07 直接写成新的 machine PASSED 并同时把 development 关闭，否则会制造 revision/review 不一致；
5. `work.md` 已启用 RC23 `review_only=true`，当前 formalization 只允许 task facts / project docs 写入；
6. 当前 closure task 的静态 Implementation Plan 输入已由历史 R05 重绑定到 R07。

本轮 event ledger 仅 append RC23 reconciliation patch，用于同步 work mode 和明确 fail-closed next action；不重写 RC21 历史 event，也不伪造 Review。

## 为什么 development 仍没有写成 PASSED

DEV-09 的 SpecCompliance、EngineeringStandards、Architecture Review 与 P0/closure 材料都已存在，但当前 development gate 明确要求 `development:TDDReviewAgent` 对同一 exact revision 给出可验证结论。该 canonical Review 尚未存在。

因此当前机器态保持：

```text
development = IN_PROGRESS / FORMALIZATION_REQUIRED
review_only = true
next_agent = DevelopAgent
next_action = canonicalize current authority + development:TDDReviewAgent exact-revision Review
then = close development -> PHASE_FINAL_CODE_REVIEW
```

这一步不涉及业务代码变更；如 formalization 发现真实 P0/P1 或 provenance 缺口，则停止前进并进入 remediation，而不是修改状态绕过门禁。

## 后续环境文档

`project_env.md` / `harness_env.md` 暂不作为本轮 closure 的必改项。进入 Testing 前，再基于项目实际环境补齐 capability-based validation facts（如 fast/integration/runtime/persistence validation），避免把工具名硬编码成生命周期语义。
