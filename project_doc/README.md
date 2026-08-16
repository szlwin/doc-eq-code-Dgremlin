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
- PR36 reconciliation HEAD `4bb11919019f455e400c7088b74b7da413fe80b2` 的 P0 Build Gate #1828 / run `31927101610` 已 `SUCCESS`。
- P2 当前机器态仍为 `development / IN_PROGRESS / FORMALIZATION_REQUIRED`。RC23 reconciliation 已识别 R31/R33/R07 为当前 standalone authority，但在 canonical Review Registry 完成 exact-revision 绑定前，不把机器态直接覆盖成 PASSED。
- 2026-08-16 formalization attempt 已完成 DEV09 R09 的 `development:TDDReviewAgent` 语义审计：基于 exact-head P0 #1825 的完整 core/mysql job 日志和两个未过期 artifact，五项 profile criterion 可得出语义 `PASSED`；但该审计尚不是 canonical v4 ReviewResult，因此不作为机器 PASSED 绑定。
- 当前 canonical blocker：R31/R07 standalone reviewer 集合可覆盖当前 profile 但尚未注册；R33 standalone reviewer 集合与当前 `test_design` machine profile 不等价，仍需 exact-revision `DesignReviewAgent`、`TDDReviewAgent`、`TestEvidenceReviewAgent` 等 current-profile Review/Evidence 正式写入 v4 registry。
- 当前下一动作仍是 **Evidence/Review formalization**；只有 `evidence/evidence_index.json` 与 `evidence/reviews.jsonl` 完成 exact-revision 注册、reconcile/close 真正通过后，才能关闭 development 并进入 `PHASE_FINAL_CODE_REVIEW`。
- P3—P8：`TODO`。

## P2 有效业务/设计事实

后续审查仍以已经确认的有效内容 revision 为输入，不回退业务语义：

- Design：`DESIGN-P2-R31@685dc64b1a8bb21438440185f4a25d68d120d75f`
- TestDesign：`TESTDESIGN-P2-R33@ac9c0aecd1bf3ebee325d88d2f1b4027d727761d`
- Implementation Plan：`TP-FEATURE-DESC-3361AD2E54FC-R07@604f26f1641e0cf9d7d70a8ee11e90d1d1ffdf1a`
- TDD：`TDD-P2-R01@3f282bb4e1f6`
- Development closure：`DEV-P2-DEV09-R09@4a82335fbdce7a56b58fd6626af0ec67a7cbebba`

当前 canonical machine state 仍保留较早的 R30/R32/R05 Review 绑定；`rc23_reconciliation_20260816_r01.json` 记录初始 drift，`rc23_development_formalization_20260816_r02.json` 记录本轮真实门禁检查结果。fail-closed 规则禁止通过手工状态覆盖绕过该缺口。

## 执行约束

- 当前 reconciliation / formalization 均为 docs/Evidence-only，不修改业务代码。
- 任何 P0/P1、authority conflict、provenance 缺口继续 fail-closed。
- README / `project_process.md` 只是高层摘要；发生冲突时以 `task_events.jsonl` reducer、Evidence、Review Registry 和 exact Git revision 为准。
- `project_env.md` / `harness_env.md` 可在进入 Testing 前按实际 capability 补齐；它们不是本次 development formalization 的前置条件。
