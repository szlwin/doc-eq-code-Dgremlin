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

- P0：`PASSED`。
- P1：`PASSED / MERGED / ARCHIVED`。历史保持不改写。
- P2：DEV-01～DEV-09 的实际开发结果已经完成，最新 closure 为 `DEV-P2-DEV09-R09@4a82335fbdce7a56b58fd6626af0ec67a7cbebba`。
- PR36 evidence-recovery HEAD `5629e2b60f0a03fb16bb0fe67b75ab8c0b27f92f` 的 P0 Build Gate #1831 / run `31940608244` 已 `SUCCESS`；#1830/#1829/#1828/#1825 继续只作为各自 exact revision 的 provenance。
- P2 当前机器态仍为 `development / IN_PROGRESS / FORMALIZATION_REQUIRED`。禁止在 upstream TestDesign current-profile gate 未闭合时把 development 手工覆盖成 PASSED。
- DEV09 R09 的 `development:TDDReviewAgent` 语义审计已基于 exact-head P0 #1825 完整日志/artifact 得到 `PASSED`，但尚不是 canonical v4 ReviewResult。
- R31 / R07 standalone reviewer 集合可覆盖当前 profile，但 canonical v4 exact-revision 注册仍未执行；该注册现因 upstream R33 blocker 暂缓，而不是缺失事实被忽略。
- **R33 current-profile Review 已实际执行**：`DesignReviewAgent=PASSED`、`RequirementReviewAgent=PASSED`、`TDDReviewAgent=BLOCKED`、`TestEvidenceReviewAgent=BLOCKED`。
- R33 blocker 已收敛为 `R33_TDD_EVIDENCE_REQUIRED`：exact R33 test source 仍是 R32 placeholder，R33 新增/加强的 write-value Case 在该 revision 没有 target-specific executable RED；DEV-07 production write-value implementation 又早于后续 executable write-value oracle test，因此后置 GREEN 不能倒算成 pre-fix RED。
- R33 historical RED Evidence Recovery 已完成：当前仓库历史与注册 Evidence 中未发现可合法恢复的 target-specific pre-development RED；结论为 `HISTORICAL_TARGET_RED_NOT_FOUND`。
- **`P2_CODE_PRESERVATION_LOCK=ACTIVE`**：历史 TDD Evidence 缺口是 lifecycle/conformance blocker，不是当前 P2 功能缺陷；现有 production/test/config 保持不变，不允许为了补历史 RED 而回退、破坏或重写已经 GREEN 的 P2 实现。
- 当前不能仅创建“形式上的”新 TestDesign/TDD revision 来补历史。只有真实尚未实现的新行为或独立验证出的当前功能缺陷被授权后，才能先通过正式工作流退出 `review_only`，建立新语义 TestDesign/TDD revision，并在对应 production change **之前**取得真实 RED。
- 只有 mandatory upstream gates 真正 PASSED 后，才能完成 exact-revision canonical registration，随后 `reconcile -> close development -> PHASE_FINAL_CODE_REVIEW`。
- P3—P8：`TODO`。

## P2 有效业务/设计事实

后续审查仍以已经确认的有效内容 revision 为输入，不回退业务语义：

- Design：`DESIGN-P2-R31@685dc64b1a8bb21438440185f4a25d68d120d75f`
- TestDesign：`TESTDESIGN-P2-R33@ac9c0aecd1bf3ebee325d88d2f1b4027d727761d`
- Implementation Plan：`TP-FEATURE-DESC-3361AD2E54FC-R07@604f26f1641e0cf9d7d70a8ee11e90d1d1ffdf1a`
- TDD：`TDD-P2-R01@3f282bb4e1f6`
- Development closure：`DEV-P2-DEV09-R09@4a82335fbdce7a56b58fd6626af0ec67a7cbebba`

当前 canonical machine state 仍保留较早的 R30/R32/R05 Review 绑定。过程审计链为：

- `rc23_reconciliation_20260816_r01.json`：初始 authority drift；
- `rc23_development_formalization_20260816_r02.json`：DEV09 TDD Evidence 复核；
- `testdesign_r33_current_profile_audit_20260816_r01.json`：R33 当前 mandatory Review Profile 的真实逐项结论；
- `rc23_development_formalization_20260816_r03.json`：R33 current-profile fail-closed development formalization 决策；
- `r33_historical_red_evidence_search_20260816_r01.json`：仓库历史与注册 Evidence 的 target-specific RED 恢复审计；
- `rc23_development_formalization_20260816_r04.json`：historical RED 未恢复后的合法 remediation / lifecycle 决策；
- `p2_code_preservation_decision_20260816_r01.json`：现有 P2 production/test/config 保持冻结的正式决策；
- `rc23_development_formalization_20260816_r05.json`：code-preservation 边界确认后的 fail-closed lifecycle 决策。

## 执行约束

- 当前 reconciliation / formalization 均为 docs/Evidence-only，不修改业务代码、测试或配置。
- 不得仅为了补历史 `RC-TDD-001` RED Evidence 修改、回退、删除或故意破坏当前已经 GREEN 的 P2 production/test/config。
- 任何 P0/P1、authority conflict、provenance/Evidence 缺口继续 fail-closed。
- standalone JSON、旧 reviewer identity 或人工摘要不能替代 canonical exact-revision ReviewResult。
- README / `project_process.md` 只是高层摘要；发生冲突时以 `task_events.jsonl` reducer、Evidence、Review Registry 和 exact Git revision 为准。
- `project_env.md` / `harness_env.md` 可在进入 Testing 前按实际 capability 补齐；它们不是当前 R33 TDD Evidence blocker 的替代品。
