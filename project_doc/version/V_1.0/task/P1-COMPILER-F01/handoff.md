# P1-COMPILER-F01 阶段交接

> T01～T05 已合并到 `dev_all`。T05 当前有效 Completion 为 `COMPLETION-P1-T05-R03@30529276cd8f`，merge / T06 base 为 `17ce0834b947a75ff3ccbd24c7b1332fb93e8941`。T06 I001 Completion R01 已被 `REV-000270` 推翻并作为不可变历史保留；当前有效任务为 `TASK-P1-T06 / I002`。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`；
- T02：`COMPLETION-P1-T02-R05@35376308b013`；
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`；
- T04：`COMPLETION-P1-T04-R02@0699c6bc2ed4`；
- T05：`COMPLETION-P1-T05-R03@30529276cd8f`，merge / dev_all Head `17ce0834b947a75ff3ccbd24c7b1332fb93e8941`。

## T06 历史 Revision

- I001 Completion：`COMPLETION-P1-T06-R01@90d483290cf3`；
- 推翻 Review：`REV-000270`；
- R23/R19、R01、Review `REV-000257`～`REV-000269`、Evidence `EVD-000499`～`EVD-000511` 全部不可变保留；
- I001 不能作为 T07 当前前置输入。

## T06 I002（当前有效）

- Design：`DESIGN-R24@P1-T06-REWORK-I002`；
- Plan：`TP-P1-COMPILER-F01-R20@P1-T06-REWORK-I002`；
- TDD：`TDD-P1-T06-R02@895d907b1980`；
- Architecture Skeleton：`DEVSKEL-P1-T06-R02@a90d4cf220d0`；
- Development：`DEV-P1-T06-R02@aec3cd105b15`；
- Code Review：`CODEREVIEW-P1-T06-R02@aec3cd105b15`；
- Testing：`TESTING-P1-T06-R02@aec3cd105b15`；
- Completion：`COMPLETION-P1-T06-R02@aec3cd105b15`；
- Review：`REV-000270`～`REV-000282`；
- Evidence：`EVD-000512`～`EVD-000524`；
- Clean-code Head：`aec3cd105b15a302d8c1c91014c6c16529ef8c6a`；
- P0 Run：`30793559695`；
- Artifact：`8847970363`；
- Artifact SHA-256：`922f8b7dc26245d6f0001ea1b6da86be05aed68ec21c1504634ec9f28ad64ae9`；
- T06 Raw 31/31；Compiler 114/114；XML 30/30；YAML 59/59；Context 正常 26/26；Demo 4/4；Legacy 1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1/P2：无。

## Revision Integrity

- R24 first commit：`18f2985171c54d47841e6d8b6733684a4d5d583a`；
- R24 blob：`ff669cc0cf4182a8ec2bf6a7b47389709c8695d8`；
- R20 first commit：`e225ca08625d97e09ce215c878e82f31f2a30427`；
- R20 blob：`7969acbfc877277c7bc605c4b311ce8fa4014e01`；
- R24/R20 在 RED 前创建，clean-code Head 复核 blob 未变化。

## I002 Raw 合同

- owner/name/reference 只使用 trim 判断空白，保存时保留原始 lexical token；
- RULE owner 使用原始 system/ruleViewName；
- PRODUCE owner 使用原始 directory/action；
- PRODUCE 可选空白 ref 映射为 absent；
- name、ownerToken、attributes、body attributes、reference target 保持一致；
- public RawDefinition 强制完整 14 Kind owner/name 矩阵；
- public RawBuildResult.failed 只允许 ERROR、MIX_STRUCTURE_UNKNOWN、固定 pass；
- WARNING、INFO、错误 code/pass 在公开边界拒绝；
- reference 在第一阶段验证并使用当前节点 SourceRef；
- MODEL_ACCESS 的空白 model-ref 优先报告 reference failure；
- public Builder 独立限制 Canonical 深度 256、节点数 65,536；
- limits 在其他递归逻辑和 Raw 对象分配前执行；
- 不捕获 StackOverflowError，不使用真实栈溢出测试；
- RawDefinition equals/hashCode/toString 覆盖同一全部语义字段；
- RawDefinitionSet.toString 能表现定义差异；
- 失败保持 `FAILED / MIX_STRUCTURE_UNKNOWN / empty set`。

## 保持的 I001 合同

- 六类根完整父子 Grammar；
- 14 Kind 按输入文档顺序和定义先序生成；
- sourceOrdinal 从 0 连续；
- parent definition 遇到 nested definition 时停止收集子树 reference；
- Raw body 递归复制，attributes 稳定排序，references/children 保持顺序；
- reference 不解析、不执行 I/O；
- Builder 先整批验证，再提取和发布；
- unknown/null/缺必填事实全部 fail closed；
- 集合和 diagnostics defensive copy、不可变；
- 等价 XML/YAML Canonical 除 format 外 Raw 语义一致。

## PR、恢复与下一步

- 当前 PR：`#21`；
- Branch：`feature/p1-t06-raw-definition-20260803-1334`；
- Base：`dev_all@17ce0834b947a75ff3ccbd24c7b1332fb93e8941`；
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t06-r02/completion-report.json`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t06_r02_completion.json`；
- `@Override` 独占一行，公共方法、构造器和关键逻辑使用中文注释；
- 未修改 Context、Source Graph、Canonical API、XML/YAML Frontend 生产代码；
- 未启动 TypedKey、SymbolTable、引用解析、Pipeline 或 T07；
- 未经明确授权不得合并 PR #21；
- PR #21 合并前 `TASK-P1-T07` 保持阻断。
