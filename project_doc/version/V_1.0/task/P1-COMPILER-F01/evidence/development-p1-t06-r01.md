# TASK-P1-T06 Development Evidence

- Review：`REV-000262`
- Evidence：`EVD-000504`
- Revision：`DEV-P1-T06-R01@90d483290cf3`
- Clean-code Head：`90d483290cf3943003624f21f19981535ca1408c`
- 结果：`PASSED`

## 实现

`dec.core.compiler.raw` 实现：

1. 两阶段构建：先验证整批 Canonical 文档，再提取并发布完整集合；
2. 六类根完整父子 Grammar 白名单；
3. 14 类 RawDefinition；
4. 输入文档顺序 + 定义先序的连续 `sourceOrdinal`；
5. owner/name lexical context；
6. 稳定 attributes、ordered references 和 recursive body；
7. reference 遍历在嵌套定义边界停止；
8. unknown/null/blank fail closed；
9. `FAILED / MIX_STRUCTURE_UNKNOWN / empty RawDefinitionSet`；
10. parser 类型、I/O、对象加载、表达式执行和跨调用可变状态均不存在。

## Development GREEN 演进

- 首次完整实现 Head：`655d02771fa4d0b84cdcb42b5d333cdafe4f4712`；
- P0 Run：`30788732321`；
- 10 个行为测试通过；唯一失败是测试误判 JaCoCo synthetic 字段；
- 修正 Oracle 后 Head：`1215475a9933d822ff09bba7913f5d0901c91504`；
- P0 Run：`30788945434`；
- Artifact：`8846317500`；
- SHA-256：`1eac315f0a20418260064b153405da2e460324e87dc1999add990eba5df83992`；
- 全仓转绿。

## 独立 Review Rework

### FND-P1-T06-I001-001

- 等级：`P1`
- 原问题：公开 `RawDefinitionSet` 只拒绝重复 ordinal，未强制 `0..size-1` 连续；
- Review RED Head：`1f97be48534cecf70432e24f171f3aace8905ff1`；
- P0 Run：`30789099210`；
- 修复：排序后逐项校验 ordinal 精确等于索引；
- 状态：`CLOSED`。

### FND-P1-T06-I001-002

- 等级：`P1`
- 原问题：`RawBuildResult.failed` 未逐项拒绝 null Diagnostic，也未稳定排序；
- Review RED Head：`1f97be48534cecf70432e24f171f3aace8905ff1`；
- P0 Run：`30789099210`；
- 修复：逐项 non-null、自然排序、不可变冻结；
- 状态：`CLOSED`。

### FND-P2-T06-I001-003

- 等级：`P2 / TEST ORACLE`
- 原问题：架构 Oracle 将 JaCoCo synthetic `$jacocoData` 误判为业务 static registry；
- 修复：忽略 synthetic instrumentation 字段，仍严格审查全部非 synthetic 业务字段；
- 状态：`CLOSED`。

## 完整 Grammar Review

新增 `RawGrammarAndFormatReviewTest`：

- 六类根每条合法父子边均通过；
- 合法节点迁移到错误父节点时 fail closed；
- 等价 XML/YAML Canonical 输入除 format 来源事实外，Raw 语义事实一致。

测试文件首版曾有 3 处括号闭合错误，CI 在 testCompile 阶段阻断；修复仅涉及测试 fixture，不改变生产实现或冻结合同。该失败不作为有效 RED 或生产缺陷证据。
