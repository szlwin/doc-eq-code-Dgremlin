# DEC Compiler Implementation Plan R18 — T05 YAML Scalar Budget / Resolver Rework

- Plan Revision：`TP-P1-COMPILER-F01-R18@P1-T05-REWORK-I003`
- Design Input：`DESIGN-R22@P1-T05-REWORK-I003`
- Task：`TASK-P1-T05 / I003`
- PR：`#20`
- Rework Base：`499b977a773da3e25b776d4debf7abb1391b5192`

> 本 Plan 在 I003 TDD 前创建。提交后内容冻结；执行变化必须创建新的 Plan Revision，不得修改本文件追认代码。

## 1. 生命周期顺序

1. 将 PR #20 切回 Draft；
2. 独立确认三个 P1，推翻 R02 当前有效性但保留 I002 全部历史；
3. 创建并冻结 R22、R18，记录首次 commit 与 blob SHA；
4. 创建 I003 Task / Finding / Review 输入；
5. 提交可编译 RED Oracle；
6. 运行 P0 并记录精确失败、Artifact 与 SHA-256；
7. 建立 Architecture Skeleton：前置单值门禁、Resolver-backed policy、两阶段 scalar 预算；
8. 完成 Development 与正负回归；
9. 执行 Specification、Architecture、Security、Code、TDD 五类独立 Review；
10. 完成 Testing、Completion R03、机器 checkpoint、handoff 与 resume；
11. 对最终文档化 Head 运行独立 P0；
12. 重新读取 R22/R18，验证 blob SHA 与首次创建一致；
13. 更新 PR #20 并恢复 Ready for review，禁止自动合并。

## 2. TDD RED

新增 `YamlScalarBudgetResolverReworkTest`，使用生产 Frontend 和小型注入预算，不使用真实 OOM 或性能阈值。

### 2.1 前置单值门禁

构造 `maxScalarCharsPerNode = 8` 的 Frontend，分别验证：

- 普通节点 scalar；
- `#text`；
- `@attributes` value；
- Sequence item。

每个位置至少包含：

- 超限且词法合法的显式 `!!float`；
- 超限且词法非法的显式 typed scalar，用于证明 `scalar-per-node` 优先于 `invalid-lexeme`；
- status 为 FAILED；
- diagnostic code 为 `MIX_FRONTEND_YAML_UNSAFE`；
- messageKey 为 `yaml.frontend.limit.scalar-per-node`；
- canonicalRoot 为空。

### 2.2 Resolver 兼容

正向：

- 隐式 `1e3`；
- 隐式 `1.2e3`；
- 显式 `!!float 1e3`。

负向：

- `!!int 0b_`；
- `!!int 0x_`；
- `!!int 0_`。

正负值均覆盖普通 scalar、`#text`、属性 value和 Sequence item。正向断言 Canonical 保存原始词法；负向断言 FAILED / `yaml.frontend.scalar.invalid-lexeme` / empty root。

### 2.3 回归控制

- 合法 bool/int/float/null/timestamp 保持通过；
- 非法日期、时间、时区继续失败；
- 严格 UTF-8 和 portable name Oracle 保持通过；
- 每个负向组先验证安全控制样本可解析。

## 3. Architecture Skeleton

### 3.1 Scalar 两阶段预算

在 `BuildContext` 内冻结：

- `requireScalarLength(String, Mark, path)`：只检查原始单值长度；
- `requireAllowedScalarTag(ScalarNode, String, path)`：在长度通过后执行 tag 与词法；
- `reserveCumulativeScalar(int, Mark, path)`：只更新最终 Canonical scalar 累计预算；
- `validateScalarBeforeCanonicalization(...)`：统一四个位置的顺序。

不得在 `requireScalarLength` 前执行 trim、regex、日期或数值处理。

### 3.2 Resolver-backed policy

`YamlScalarLexemePolicy`：

- 删除自定义 BOOL/INT/FLOAT/NULL/TIMESTAMP 接受正则；
- 删除 `BigDecimal`；
- 直接调用 `Resolver.BOOL/INT/FLOAT/NULL/EMPTY/TIMESTAMP`；
- timestamp 在 Resolver 匹配后执行 `LocalDate`、时间和 offset 范围检查；
- 保持 package-private final，不新增 compiler 公共 API。

## 4. Development

- 普通 scalar、`#text`、Sequence item 继续经 `readScalarEntered`；
- 属性 value 经 `readAttributeValue`；
- 两条路径必须先调用同一 `validateScalarBeforeCanonicalization`；
- null 在长度和词法通过后才映射为空；
- 非 null trim 后按 Canonical 长度更新累计预算；
- String 空值不发布 scalar，属性空值保留为空字符串；
- messageKey 保持稳定；
- 所有新增关键方法添加中文注释；
- `@Override` 独占一行；
- Java release 8。

## 5. Revision Integrity Evidence

- 读取 R22/R18 创建后的 blob SHA；
- 记录 Design 首次 commit `ab9ca21cf668aba03f030129022458bbd46304fc`；
- 记录 Plan 首次 commit（由本文件创建结果确定）；
- RED 前写入 revision-lock Evidence；
- clean-code 与最终 Head 分别读取同一路径，blob SHA 必须相同；
- Completion R03 保存四项：Design commit、Design blob、Plan commit、Plan blob；
- R21/R17/R02 只标记 superseded，不修改内容。

## 6. Review 与测试

- Specification：R22/R18 与实际顺序、Resolver source一致；
- Architecture：无公共 API漂移、无 T06、两阶段预算单一入口；
- Security：超限输入在 regex 前失败、无 BigDecimal、四位置一致；
- Code：Java 8、中文注释、`@Override` 独行、无重复自定义数字语法；
- TDD：RED 精确、诊断优先级可证明、无时间/OOM脆弱测试；
- Testing：全仓 P0、Artifact 精确绑定 clean-code 与最终 Head。

## 7. Completion

新建 `COMPLETION-P1-T05-R03@<clean-code-head>`。R01、R02 均作为不可变历史保留。Completion 只有在三个 Finding关闭、R22/R18 blob 未变化、全量 CI通过且开放 P0/P1/P2 为 0 时才能 PASSED。
