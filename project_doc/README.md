# P1—P8 研发事实目录

本目录是 `doc-eq-code-Dgremlin` P1—P8 使用的 `common-develop` 项目事实根。

## 当前工具与状态事实源

- common-develop：`2.44 RC21`
- Task Storage：storage model `6` / `EVENT_LEDGER_V3`
- 项目事实根：`project_doc`
- Active Task mutable authority：`version/V_1.0/task/{TARGET_ID}/task_events.jsonl`
- `task_plan.md` 仅保存静态执行规格；`cache/` 为可重建投影，不作为 authority。

历史 pre-RC13 runtime 不再作为当前状态输入；本次 P2 re-baseline 已按原 Git blob 冻结到 `task/FEATURE-DESC-3361AD2E54FC/evidence/legacy-runtime/pre-rc13/`。

## 当前阶段

- P0：`PASSED`。
- P1：`PASSED / MERGED / ARCHIVED`。历史保持不改写。
- P2：DEV-01～DEV-09 的实际开发结果已经完成，最新 closure 为 `DEV-P2-DEV09-R09@4a82335fbdce7a56b58fd6626af0ec67a7cbebba`。
- P2 当前 RC21 机器态：`development / IN_PROGRESS / FORMALIZATION_REQUIRED`。原因不是代码未完成，而是 RC21 不允许把后续简单 Review/closure 文件自动冒充为 canonical exact-revision Evidence/Review Registry。
- 当前下一动作：**仅做文档/Evidence 形式化**，把 DEV-09 exact revision、required `TDDReviewAgent` 和 development StageOutcome 正式绑定；完成后进入 `PHASE_FINAL_CODE_REVIEW`。
- P3—P8：`TODO`。

## P2 有效业务/设计事实

后续审查仍以已经确认的有效内容 revision 为输入，不回退业务语义：

- Design：`DESIGN-P2-R31@685dc64b1a8bb21438440185f4a25d68d120d75f`
- TestDesign：`TESTDESIGN-P2-R33@ac9c0aecd1bf3ebee325d88d2f1b4027d727761d`
- Implementation Plan：`TP-FEATURE-DESC-3361AD2E54FC-R07@604f26f1641e0cf9d7d70a8ee11e90d1d1ffdf1a`
- TDD：`TDD-P2-R01@3f282bb4e1f6`

其中 canonical Review Registry 当前仍保留较早的 R30/R32/R05 正式绑定；RC21 re-baseline 明确把这个差异暴露为形式化工作，而不是伪造 PASSED。

## 执行约束

- 当前 re-baseline 以及随后的 formalization 均为 docs/Evidence-only，不修改业务代码。
- 任何 P0/P1、authority conflict、provenance 缺口继续 fail-closed。
- README / `project_process.md` 只是高层摘要；发生冲突时以 `task_events.jsonl` reducer、Evidence、Review Registry 和 exact Git revision 为准。
