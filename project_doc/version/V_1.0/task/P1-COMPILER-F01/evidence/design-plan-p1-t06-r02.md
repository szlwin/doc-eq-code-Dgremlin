# TASK-P1-T06 / I002 — Design / Plan Evidence

- Design Review：`REV-000271`
- Plan Review：`REV-000272`
- Evidence：`EVD-000513`、`EVD-000514`
- Design：`DESIGN-R24@P1-T06-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R20@P1-T06-REWORK-I002`
- Rework Base：`1247c024b38e1affe35f38671446187df98f5c34`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Result：`PASSED`

## Revision Lock

- R24 first commit：`18f2985171c54d47841e6d8b6733684a4d5d583a`；
- R24 blob：`ff669cc0cf4182a8ec2bf6a7b47389709c8695d8`；
- R20 first commit：`e225ca08625d97e09ce215c878e82f31f2a30427`；
- R20 blob：`7969acbfc877277c7bc605c4b311ce8fa4014e01`；
- 两个 Revision 均在 TDD RED 前创建；
- clean-code Head `aec3cd105b15a302d8c1c91014c6c16529ef8c6a` 重新读取后 blob 未变化。

## 冻结合同

1. owner/name/reference 只使用 trim 判断空白，存储时保留原始 lexical token；
2. PRODUCE 可选 `ref` 缺失或纯空白映射为 empty；
3. public RawDefinition 强制 14 Kind owner/name 矩阵；
4. public RawBuildResult.failed 只允许 ERROR、MIX_STRUCTURE_UNKNOWN、`raw-definition-builder`；
5. 所有必填 reference 在第一阶段以当前 Canonical 节点 SourceRef 验证；
6. Builder 自身深度 256、节点数 65,536；
7. RawDefinition.toString 与 equals/hashCode 覆盖相同全部语义字段；
8. 六类 Grammar、顺序、ordinal、父引用边界、不可变性不回退；
9. 不修改 Context、Canonical、XML、YAML、Source Graph；
10. 不启动 TypedKey、Symbol、Pipeline 或 T07。

## 历史保护

I001 的 R23/R19、TDD/Skeleton/Development/Testing R01、Completion R01、Review `REV-000257`～`REV-000269`、Evidence `EVD-000499`～`EVD-000511` 均保留，未覆盖或删除。
