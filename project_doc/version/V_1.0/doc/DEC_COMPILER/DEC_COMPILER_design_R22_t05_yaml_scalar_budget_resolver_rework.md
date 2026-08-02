# DEC Compiler Design R22 — T05 YAML Scalar Budget / Resolver Rework

- Design Revision：`DESIGN-R22@P1-T05-REWORK-I003`
- Task：`TASK-P1-T05 / I003`
- Rework Base：PR #20 Head `499b977a773da3e25b776d4debf7abb1391b5192`
- Dependency：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- Superseded current Completion：`COMPLETION-P1-T05-R02@27d566714f5c`
- Immutable history：I001、I002 及 R20/R21、R16/R17、R01/R02 全部记录不覆盖、不删除
- Fixed parser dependency：`org.yaml:snakeyaml:2.2`
- Resolver source baseline：`snakeyaml/snakeyaml@a34989252e6f59e36a3aaf788a903b7a37a73d33`

> 本 Design 在 I003 TDD 前创建。提交后内容冻结；任何核心合同变化必须创建新的 Design Revision，不得修改本文件追认实现。

## 1. Rework 原因

独立 Review 确认三个 P1：

1. 单 scalar 上限在 typed 词法正则和 `BigDecimal` 之后执行，可在受控失败前消耗大量 CPU；
2. I002 自定义 int/float 词法与 SnakeYAML 2.2 Resolver 不一致；
3. R21/R17 在 clean-code Head 后发生核心语义变化但 Revision ID 未变化，导致 R02 revision 链不可证明。

Finding：

- `FND-P1-T05-I003-001`：scalar 资源门禁顺序；
- `FND-P1-T05-I003-002`：SnakeYAML 2.2 Resolver 词法一致性；
- `FND-P1-T05-I003-003`：Design / Plan Revision 完整性。

I002 回归状态：严格 UTF-8 与 portable name / nodePath 继续保持 CLOSED；typed scalar 来源事实重新打开并由 I003 收敛。

## 2. Scalar 资源门禁顺序

每个 `ScalarNode` 必须按以下固定顺序处理：

1. 只读取一次 `node.getValue()`，得到未经 trim 的原始 scalar；
2. 使用原始 scalar 字符长度执行 `maxScalarCharsPerNode` 廉价门禁；
3. 通过后才执行 tag 白名单和 typed 词法校验；
4. 合法 null 映射为空；非 null 再生成 trim 后的 Canonical scalar；
5. 使用最终 Canonical scalar 长度更新累计 scalar 预算；
6. 预算或词法失败均不得发布部分 root。

单值门禁必须先于：

- 任何 `Pattern.matcher(...).matches()`；
- 日期解析；
- 数值构造；
- 字符串复制、下划线删除等与词法相关的派生操作。

普通节点 scalar、`#text`、`@attributes` value、Sequence item 必须共享同一前置长度入口。超限时固定返回：

- status：`FAILED`；
- diagnostic：`MIX_FRONTEND_YAML_UNSAFE`；
- messageKey：`yaml.frontend.limit.scalar-per-node`；
- canonicalRoot：empty。

不得使用真实 OOM、接近 1 MiB 的 CI 性能输入或执行时间阈值作为证明；使用小型注入预算和诊断优先级 Oracle 证明顺序。

## 3. SnakeYAML 2.2 词法合同

### 3.1 单一语法来源

项目不再复制或自行改写 bool/int/float/null/timestamp 的正则。`YamlScalarLexemePolicy` 必须直接使用 SnakeYAML 2.2 公开常量：

- `Resolver.BOOL`；
- `Resolver.INT`；
- `Resolver.FLOAT`；
- `Resolver.NULL` 与 `Resolver.EMPTY`；
- `Resolver.TIMESTAMP`。

`Tag.STR` 始终保留原始字符串词法。其他标准 scalar tag 必须与对应 Resolver Pattern 完全匹配。custom/object/local 及非冻结标准 tag继续拒绝。

### 3.2 隐式与显式 tag

- 隐式 typed scalar 先由 SnakeYAML 2.2 Resolver 赋 tag，再由相同 Resolver Pattern 复核；
- 显式 `!!bool/!!int/!!float/!!null/!!timestamp` 在项目单 scalar 门禁通过后，使用相同 Pattern 校验；
- 合法显式与隐式 typed scalar均保留原始词法，不执行 Java 业务对象 construction；
- 显式非法 typed scalar 返回 `yaml.frontend.scalar.invalid-lexeme`；
- 不调用 `load` / `loadAs`，不反射或实例化用户类型。

固定兼容示例：

应通过：

```yaml
root: 1e3
root: 1.2e3
root: !!float 1e3
```

应失败：

```yaml
root: !!int 0b_
root: !!int 0x_
root: !!int 0_
```

这些正负例必须覆盖普通 scalar、`#text`、属性 value 与 Sequence item。

### 3.3 Timestamp 语义

`Resolver.TIMESTAMP` 决定词法接受范围；匹配后追加以下真实值检查：

- `LocalDate` 必须可构造；
- hour 为 0～23；
- minute / second 为 0～59；
- offset hour 为 0～23；
- offset minute 为 0～59。

该语义检查只能收紧真实日期/时间值，不得改变 Resolver 的指数、进制、特殊 float 或 null 词法。

### 3.4 移除昂贵数值构造

词法验证不得创建 `BigDecimal`、`BigInteger` 或其他任意精度数值。Resolver Pattern 已定义 frozen lexical contract；Canonical 只保存原始词法，不需要构造数值对象。

## 4. Revision 完整性合同

- R21/R17/R02 保留为不可变历史，并由本轮独立 Review 标记失效；
- R22 与 R18 必须在 I003 RED 前创建；
- Design blob SHA、Plan blob SHA、各自首次提交 SHA必须写入 I003 Evidence；
- RED、Skeleton、Development、五类 Review、Testing、Completion 均引用 R22/R18；
- clean-code Head 与最终文档化 Head 必须重新读取 R22/R18，并证明 blob SHA 与首次提交时一致；
- 若内容变化，I003 Completion 必须失败并创建新 Revision，不得原地修订。

## 5. 保持的不变量

- 严格 UTF-8 decoder 在 parser 前使用 REPORT；
- 根、子节点和属性名继续使用 `[A-Za-z_][A-Za-z0-9._-]*`；
- nodePath 不接受路径分隔符、换行或非法名称；
- 继续只使用 `SafeConstructor + composeAll`；
- Java/object/local/custom、binary/set/omap/pairs tag拒绝；
- anchor、alias、共享/递归图、merge、duplicate/complex key拒绝；
- 单 Mapping root、`@attributes`、`#text`、Sequence 同名重复子节点映射不变；
- R20 生产资源预算值不变；
- 失败不发布部分 root；
- Context 生产代码、compiler canonical 公共 API和 XML Frontend 生产语义不变化；
- T06 不启动。

## 6. 验收门禁

- I003 RED 可按 Java 8 编译，精确暴露资源顺序和 Resolver 差异；
- 小型预算 Oracle 证明所有四个 scalar 位置优先返回 `scalar-per-node`；
- `1e3`、`1.2e3`、显式 `!!float 1e3` 正向通过；
- `!!int 0b_`、`0x_`、`0_` 四位置负向失败；
- R22/R18 blob SHA 从创建到最终 Head 不变；
- I001/I002 YAML、XML、Compiler、Context、Demo、legacy declaration和 12 模块 Reactor 回归通过；
- Specification、Architecture、Security、Code、TDD 五类独立 Review 全部 PASSED；
- 开放 P0/P1/P2 为 0；
- `@Override` 独占一行；
- 公共方法、构造器和重要资源、词法、安全逻辑使用中文注释；
- PR #20 最终恢复 Ready for review，但未经明确授权不得合并。
