# TASK-P1-T06 / I002 — Raw Invariants Rework

- 状态：`COMPLETED`
- PR：`#21`
- Branch：`feature/p1-t06-raw-definition-20260803-1334`
- Rework Base：`1247c024b38e1affe35f38671446187df98f5c34`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Superseded Completion：`COMPLETION-P1-T06-R01@90d483290cf3`
- Design：`DESIGN-R24@P1-T06-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R20@P1-T06-REWORK-I002`
- TDD：`TDD-P1-T06-R02@895d907b1980`
- Architecture Skeleton：`DEVSKEL-P1-T06-R02@a90d4cf220d0`
- Development：`DEV-P1-T06-R02@aec3cd105b15`
- Code Review：`CODEREVIEW-P1-T06-R02@aec3cd105b15`
- Testing：`TESTING-P1-T06-R02@aec3cd105b15`
- Completion：`COMPLETION-P1-T06-R02@aec3cd105b15`
- Review：`REV-000270`～`REV-000282`
- Evidence：`EVD-000512`～`EVD-000524`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## External Review

- Review：`REV-000270`
- Evidence：`EVD-000512`
- 结论：I001 Completion R01 当前有效性被推翻，但全部历史不可变保留。

## Finding Closure

1. `FND-P1-T06-I002-001`：`CLOSED` — owner/name/reference 保留原始 lexical token；
2. `FND-P1-T06-I002-002`：`CLOSED` — public RawDefinition matrix 与 RawBuildResult Diagnostic policy；
3. `FND-P2-T06-I002-003`：`CLOSED` — reference 第一阶段精确 SourceRef；
4. `FND-P2-T06-I002-004`：`CLOSED` — Builder depth 256/node 65,536 独立预算；
5. `FND-P2-T06-I002-005`：`CLOSED` — RawDefinition/Set 完整 toString 表现。

## Revision Lock

- R24 first commit：`18f2985171c54d47841e6d8b6733684a4d5d583a`；
- R24 blob：`ff669cc0cf4182a8ec2bf6a7b47389709c8695d8`；
- R20 first commit：`e225ca08625d97e09ce215c878e82f31f2a30427`；
- R20 blob：`7969acbfc877277c7bc605c4b311ce8fa4014e01`；
- R24/R20 在 RED 前冻结，clean-code Head 复核 blob 未变化。

## 完成事实

- 六类根 Grammar 和 14 Kind 均保持；
- 非空 owner/name/reference 只判空白并保存原值；
- RULE/PRODUCE composite owner 使用原始组件；
- PRODUCE 可选空白 ref 映射为 absent；
- name、owner、attributes、body、references 来源事实一致；
- 14 Kind public owner/name matrix 强制执行；
- FAILED 只接受 ERROR + MIX_STRUCTURE_UNKNOWN + `raw-definition-builder`；
- 空白 reference 在第一阶段以当前节点 SourceRef 失败；
- MODEL_ACCESS 双重 name/reference 优先返回 reference failure；
- Builder production depth 256、node count 65,536；
- package-private limits 支持小型边界 Oracle；
- 不捕获 StackOverflowError，不使用真实栈溢出测试；
- RawDefinition.toString 覆盖所有语义字段；
- RawDefinitionSet.toString 能表现定义差异；
- T06 Raw 31/31、Compiler 114/114、XML 30/30、YAML 59/59、Context 26/26、Demo 4/4、Legacy 1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- Specification、Architecture、Security、Code、TDD Review 全部 PASSED；
- 开放 P0/P1/P2 为 0；
- `@Override` 独占一行，公共方法、构造器和关键逻辑使用中文注释；
- Context、Canonical、XML、YAML、Source Graph 未修改；
- TypedKey、Symbol、Pipeline、T07 未启动；
- PR #21 未经明确授权不得合并。

## Evidence 入口

- Design/Plan：`../evidence/design-plan-p1-t06-r02.md`；
- RED：`../evidence/tdd-red-p1-t06-r02.md`；
- Skeleton：`../evidence/architecture-skeleton-p1-t06-r02.md`；
- Development：`../evidence/development-p1-t06-r02.md`；
- Reviews：`../review/review-p1-t06-r02.md`；
- Testing：`../evidence/testing-p1-t06-r02.md`；
- Completion：`../evidence/commands/completion-p1-t06-r02/completion-report.json`；
- 机器恢复：`../../../tdd_p1_t06_r02_completion.json`。

## 历史保护

I001 的 R23、R19、TDD R01、Skeleton R01、Development R01、Testing R01、Completion R01、Review `REV-000257`～`REV-000269`、Evidence `EVD-000499`～`EVD-000511` 均未覆盖或删除。
