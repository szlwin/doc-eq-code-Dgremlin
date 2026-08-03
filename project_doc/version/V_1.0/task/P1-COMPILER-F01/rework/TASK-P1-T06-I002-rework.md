# TASK-P1-T06 / I002 — Raw Invariants Rework

- 状态：`IN_PROGRESS`
- PR：`#21`（Draft）
- Branch：`feature/p1-t06-raw-definition-20260803-1334`
- Rework Base：`1247c024b38e1affe35f38671446187df98f5c34`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Superseded Completion：`COMPLETION-P1-T06-R01@90d483290cf3`
- Design：`DESIGN-R24@P1-T06-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R20@P1-T06-REWORK-I002`
- Independent Review：`REV-000270`
- External Review Evidence：`EVD-000512`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Findings

1. `FND-P1-T06-I002-001`：lexical token 静默 trim；
2. `FND-P1-T06-I002-002`：公开 RawDefinition/RawBuildResult 允许非法状态；
3. `FND-P2-T06-I002-003`：空白 reference 未在第一阶段精确定位；
4. `FND-P2-T06-I002-004`：public Builder 无独立深度/节点边界；
5. `FND-P2-T06-I002-005`：RawDefinition.toString 字段不完整。

## Revision Lock

- R24 first commit：`18f2985171c54d47841e6d8b6733684a4d5d583a`；
- R24 blob：`ff669cc0cf4182a8ec2bf6a7b47389709c8695d8`；
- R20 first commit：`e225ca08625d97e09ce215c878e82f31f2a30427`；
- R20 blob：`7969acbfc877277c7bc605c4b311ce8fa4014e01`。

R24/R20 在 TDD RED 前冻结，后续不得原地修改；如发现合同变化必须新建 Revision。

## 完成门禁

- lexical name/owner/reference 保留原始 token；
- 14 Kind public owner/name matrix；
- FAILED Diagnostic code/severity/pass 合同；
- reference 第一阶段精确 SourceRef；
- Builder depth/node 独立预算；
- RawDefinition 全字段 toString；
- 既有 Grammar、顺序、引用边界和不可变性不回退；
- 五类 Review、全量测试和 Completion R02 完成；
- 开放 P0/P1/P2 为 0；
- `@Override` 独占一行，公共方法、构造器和重要逻辑使用中文注释；
- T07 未启动；未经明确授权不得合并 PR #21。

## 历史保护

I001 的 R23、R19、TDD R01、Skeleton R01、Development R01、Testing R01、Completion R01、Review `REV-000257`～`REV-000269`、Evidence `EVD-000499`～`EVD-000511` 均不可覆盖或删除。
