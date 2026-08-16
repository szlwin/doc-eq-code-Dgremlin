# P1—P8 研发事实目录

本目录是 `doc-eq-code-Dgremlin` P1—P8 使用的 `common-develop` 项目事实根。

## 当前工具与状态事实源

- common-develop 正式 release contract：`2.44 RC21`；项目文档中的 `RC23` 是本项目后续 reconciliation 标签，不替代 Skill 的正式发布身份。
- Task Storage：storage model `6` / `EVENT_LEDGER_V3`
- 项目事实根：`project_doc`
- Active Task mutable authority：`version/V_1.0/task/{TARGET_ID}/task_events.jsonl`
- `task_plan.md` 仅保存静态执行规格；`cache/` 为可重建投影，不作为 authority。
- 当前 P2 work mode：`review_only=true`；只允许 task facts / project docs 写入，禁止 tests、config 与 production code 修改。

历史 pre-RC13 runtime 不再作为当前状态输入；P2 的 RC21 re-baseline 已按原 Git blob 冻结到 `task/FEATURE-DESC-3361AD2E54FC/evidence/legacy-runtime/pre-rc13/`。项目 reconciliation 标签 RC23 在该 storage model 上继续执行检查，不改写历史 event。

## 当前阶段

- P0：`PASSED`（冻结 P2 代码最后已验证 HEAD `7925ec4f218c167240fc12571336244e1f7849ad` 的 P0 #1832 / run `31941036385` 为 `SUCCESS`；本轮仅 `project_doc` 提交的新 HEAD 会重新触发 P0，不改变 P2 代码内容）。
- P1：`PASSED / MERGED / ARCHIVED`。历史保持不改写。
- P2：DEV-01～DEV-09 的实际开发结果已经完成，冻结 closure 为 `DEV-P2-DEV09-R09@4a82335fbdce7a56b58fd6626af0ec67a7cbebba`。
- DEV09 closure 已明确 `development_overall=PASSED / DEVELOPMENT_TASKS_COMPLETE / open_p0_p1=0 / next_task=PHASE_FINAL_CODE_REVIEW`；`development:TDDReviewAgent` exact-revision 语义审计也已 PASSED。
- **生命周期解释已纠正**：canonical machine state 从未通过 `reopen-phase` 把 standalone TestDesign R33 替换为 current TestDesign；当前 canonical TestDesign 仍是 `TESTDESIGN-P2-R32=PASSED`。因此 R33 历史 RED Evidence 缺口保留为历史 process/Evidence non-conformance，但不再作为当前 P2 代码正确性或 Development Closure 的永久 blocker。
- R33 historical RED Evidence Recovery 结论仍保留：`HISTORICAL_TARGET_RED_NOT_FOUND`。不得把后置 GREEN 冒充历史 RED，也不得为了补历史流程回退、破坏或重写已经 GREEN 的 P2 实现。
- **`P2_CODE_PRESERVATION_LOCK=ACTIVE`**：现有 production/test/config 保持不变；除非发现真实当前缺陷或授权真实新行为，不允许进行 P2 代码修改。
- Final Code Review 已按当前 risk contract 完成语义审查：`SpecCompliance / EngineeringStandards / Security / Concurrency / Performance / Architecture / ImpactAnalysis / CrossModuleIntegration` 全部 `PASSED`，开放 P0/P1 finding 为 0，且不需要 production/test/config 修改。
- Final Review 的 P0 #1832 focused Evidence：14 个相关 suite、57 tests、0 failure、0 error、0 skipped；详细 Evidence 已内嵌在 `p2_final_code_review_20260816_r01.json`。
- **当前唯一剩余阻塞是 canonical 写入传输**：可用 GitHub 写接口只能整文件替换，而 `evidence/evidence_index.json` 是大型单行 append-only Registry；在无法获得完整本地 checkout/append-capable transport 时，不允许截断、重建或伪造 Registry。因此语义 Development Closure / Final Code Review 已 PASSED，但 canonical `StageOutcome/current_phase` 仍保持原值，未伪造 `wk close` 或 `advance-phase`。
- 后续只需在获得安全完整 checkout/append-capable transport 后，使用官方 `evidence/manual_review/wk` machinery 将已完成的 DEV09 TDD Review 与 Final Code Review 结果写入 canonical Registry，再执行 `wk reconcile -> wk close development -> advance code_review`；机器接受后即可发布 canonical Final Code Review StageOutcome 并进入 Testing。
- P3—P8：`TODO`。

## P2 有效业务/设计事实

Final Code Review 使用的内容 authority / traceability 输入为：

- Design：`DESIGN-P2-R31@685dc64b1a8bb21438440185f4a25d68d120d75f`
- TestDesign：`TESTDESIGN-P2-R33@ac9c0aecd1bf3ebee325d88d2f1b4027d727761d`（standalone 后续语义输入；不是 canonical reopen 后的 current TestDesign）
- Implementation Plan：`TP-FEATURE-DESC-3361AD2E54FC-R07@604f26f1641e0cf9d7d70a8ee11e90d1d1ffdf1a`
- TDD：`TDD-P2-R01@3f282bb4e1f6`
- Development closure：`DEV-P2-DEV09-R09@4a82335fbdce7a56b58fd6626af0ec67a7cbebba`
- Dependency / cross-module impact：`P2-IMPACT-R29`

当前 canonical machine state 仍保留较早的 R30/R32/R05 Review 绑定。过程审计链为：

- `rc23_reconciliation_20260816_r01.json`：初始 authority drift；
- `rc23_development_formalization_20260816_r02.json`：DEV09 TDD Evidence 复核；
- `testdesign_r33_current_profile_audit_20260816_r01.json`：R33 standalone current-profile 审计；
- `rc23_development_formalization_20260816_r03.json`：早期 fail-closed formalization；
- `r33_historical_red_evidence_search_20260816_r01.json`：历史 target-specific RED 恢复审计；
- `rc23_development_formalization_20260816_r04.json`：historical RED 未恢复后的旧 lifecycle 决策；
- `p2_code_preservation_decision_20260816_r01.json`：现有 P2 production/test/config 保持冻结的正式决策；
- `rc23_development_formalization_20260816_r05.json`：code-preservation 边界确认；
- `p2_final_code_review_20260816_r01.json`：8 个 Reviewer 的 criterion-level Final Code Review；
- `rc23_development_formalization_20260816_r06.json`：纠正 lifecycle 解释并记录 canonical write transport blocker，**supersedes R05 对 R33 blocker 的 Development Closure 推导**。

## 执行约束

- 当前 formalization / Final Review 为 docs/Evidence-only，不修改业务代码、测试或配置。
- `R33_TDD_EVIDENCE_REQUIRED` 继续作为历史审计债务保留，但不得再被解释为当前功能缺陷，也不得用于制造人工 RED。
- `semanticConclusion=PASSED` 不等于 canonical Registry 已写入；只有官方 Registry/Event reducer 接受后才能写 `StageOutcome=PASSED` 或推进 `current_phase`。
- 不得截断、整体重建或伪造 `evidence/evidence_index.json`、`evidence/reviews.jsonl` 或 `task_events.jsonl` 来绕过 append-only / exact-revision 门禁。
- README 只是高层摘要；机器状态以 `task_events.jsonl` reducer、canonical Evidence/Review Registry 和 exact Git revision 为最终 authority。
- `project_env.md` / `harness_env.md` 在进入 Testing 前按实际 capability 补齐。
