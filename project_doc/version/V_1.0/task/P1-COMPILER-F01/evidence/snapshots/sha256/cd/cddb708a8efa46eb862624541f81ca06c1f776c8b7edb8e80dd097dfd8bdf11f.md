# P1 需求分析测试矩阵

| Case ID | AC | 类型 | 主要断言 |
|---|---|---|---|
| CASE-P1-CANONICAL-001 | AC-001 | 正常 | XML/YAML 同义节点语义相等 |
| CASE-P1-CANONICAL-002 | AC-001 | 边界 | 属性和文件输入顺序规范化稳定 |
| CASE-P1-XXE-001 | AC-001 | 失败 | 外部实体被拒绝且无网络/文件副作用 |
| CASE-P1-DIAGNOSTIC-001 | AC-002 | 正常 | 多错误按稳定键排序 |
| CASE-P1-DIAGNOSTIC-002 | AC-002 | 失败 | 任一 ERROR 无 EngineContext |
| CASE-P1-SYMBOL-001 | AC-003 | 正常 | 跨文件前向引用解析成功 |
| CASE-P1-SYMBOL-002 | AC-003 | 失败 | 重复 Key 不能后值覆盖 |
| CASE-P1-CONTEXT-001 | AC-004 | 正常 | 两个 Context 并存且隔离 |
| CASE-P1-CONTEXT-002 | AC-004 | 并发 | 并发读取无共享可变状态 |
| CASE-P1-DIGEST-001 | AC-004 | 边界 | 同义输入 digest 相同，语义变化 digest 变化 |
| CASE-P1-LEGACY-001 | AC-005 | 正常 | 只读投影与 Context 一致 |
| CASE-P1-LEGACY-002 | AC-005 | 失败 | 注册/删除/修改被拒绝且状态不变 |
| CASE-P1-SCOPE-001 | AC-006 | 正常 | P2+ 声明保留为 Raw |
| CASE-P1-SCOPE-002 | AC-006 | 失败 | 未支持语义执行得到明确诊断而非空成功 |
