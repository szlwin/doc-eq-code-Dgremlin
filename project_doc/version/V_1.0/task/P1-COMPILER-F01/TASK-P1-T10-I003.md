# TASK-P1-T10 / I003 — TypedKey Reference Lexical Rework

- Status：`COMPLETED / PASSED`
- Completion：`COMPLETION-P1-T10-R03@336d309f3748`
- Trigger：独立 Review `NEEDS_CHANGES / REWORK`
- Reviewed Head：`7e466e7cf0f28aa4062294923c27b5f59cbd355d`
- Invalidated：`COMPLETION-P1-T10-R02@6f4c7b6f3ec3`
- Preserved History：R01 / R02 全部 Design、Plan、RED、Architecture、Review、Completion、CI、Artifact 与 rejected attempt
- Design：`DESIGN-R35@P1-T10-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R31@P1-T10-REWORK-I003`
- TDD：`TDD-P1-T10-R03@b16d5ee9f9f1`
- Architecture：`DEVSKEL-P1-T10-R03@d3f7225b4ee9`
- Development：`DEV-P1-T10-R03@bc056b7ed1da`
- Code Review：`CODEREVIEW-P1-T10-R03@336d309f3748`
- Testing：`TESTING-P1-T10-R03@336d309f3748`
- Clean-code Head：`336d309f3748328ba4dea18be9944a95751ccc29`
- PR：`#25`
- Open P0/P1/P2：`0 / 0 / 0`

## Result

- padded nonblank `model-ref/ref@view` 保留 Raw lexical，并由 `ViewKey` canonicalize；
- padded `path/ref@property` 与 blank reference 继续 fail-closed；
- Raw name/model-ref 原始 lexical 一致性不变；
- Binding 发布 canonical `SystemKey/ViewKey`；
- I003 `12/12`、T10 `54/54`、Compiler `285/285`、正常测试 `405/405`；
- Java release 8 与 12 模块 Reactor 通过；MySQL `SKIPPED_NOT_APPLICABLE`；
- PR #25 未合并，TASK-P1-T11 继续阻断。
