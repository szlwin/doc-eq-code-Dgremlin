# P1-COMPILER-F01 分析测试矩阵

| Case | AC | 输入 | 主要断言 | 失败断言 |
|---|---|---|---|---|
| CASE-P1-MIX-DISCOVERY-001 | AC-001 | 实际 `mix` fixture | 发现 10 文件及正确边类型 | 缺文件、重复 sourceId、路径逃逸失败 |
| CASE-P1-MIX-RAW-001 | AC-002 | 实际 XML + 最小等价 YAML | 定义数量与 Canonical/Raw 等价 | 未知节点不被静默忽略 |
| CASE-P1-SYMBOL-001 | AC-003 | 前向引用、同名异空间、重复 Key | 强类型解析与稳定诊断 | 不允许最后覆盖前值 |
| CASE-P1-RULE-SYSTEM-001 | AC-003 | Rule 文件来源 System 与属性冲突 | `MIX-REF-RULE-SYSTEM-MISMATCH` | 不发布 Context |
| CASE-P1-DEFERRED-001 | AC-004 | Information/Directory/Action/Produce | requiredStage、SourceRef、类型引用完整 | 不执行后续语义 |
| CASE-P1-CONTEXT-001 | AC-005 | 两组配置并发编译 | Context 隔离、摘要稳定、不可变 | 无静态 current Context |
| CASE-P1-PROJECTION-001 | AC-006 | Data/View/Rule 旧读取 | 只读结果一致 | 写入明确拒绝 |
| CASE-P1-RETIREMENT-001 | AC-007 | 仓库/POM/依赖/artifact 扫描 | 无废弃模块和 Adapter | 任何残留阻断 |
