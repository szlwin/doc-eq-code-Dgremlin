# DEC Compiler Design R21 — T05 YAML Source Facts Rework

- Design Revision：`DESIGN-R21@P1-T05-REWORK-I002`
- Task：`TASK-P1-T05 / I002`
- Supersedes current validity of：`COMPLETION-P1-T05-R01@040f09b80463`
- Immutable history：I001 及其全部 Review、Evidence、Completion 不覆盖、不删除
- Base：PR #20 Head `52fe48d46dd2c4ac9c822d5be141d47c03ae955f`
- Dependency：`COMPLETION-P1-T04-R02@0699c6bc2ed4`

## 1. Rework 原因

独立 Review 确认三个来源事实缺口：

1. `new String(byte[], UTF_8)` 会把非法 UTF-8 替换为 U+FFFD 后继续解析，输入未 fail closed；
2. compose 表示树只解析 tag，不执行类型 construction，原实现允许标准 typed tag 携带非法词法；
3. Canonical 名称可包含 `/`、换行等字符，使直接拼接的 nodePath 不能无歧义表达层级。

Finding：

- `FND-P1-T05-I002-001`：严格 UTF-8 输入；
- `FND-P1-T05-I002-002`：标准 typed scalar 来源词法；
- `FND-P1-T05-I002-003`：nodePath segment 无歧义。

## 2. 严格 UTF-8 合同

`DocumentSource.content()` 的原始 byte[] 是唯一解析输入。创建 `StringReader` 前必须使用：

- `StandardCharsets.UTF_8.newDecoder()`；
- `CodingErrorAction.REPORT` 处理 malformed input；
- `CodingErrorAction.REPORT` 处理 unmappable character；
- `ByteBuffer.wrap(content)`；
- `CharsetDecoder.decode(...)`。

任何 `CharacterCodingException` 必须转换为稳定失败：

- status：`FAILED`；
- diagnostic：`MIX_FRONTEND_YAML_UNSAFE`；
- messageKey：`yaml.frontend.encoding.invalid-utf8`；
- canonicalRoot：empty；
- SnakeYAML parser 不得接收替换后的字符串。

必须直接覆盖原始 byte[]：非法 continuation、截断多字节、overlong、UTF-8 surrogate。

## 3. Scalar tag 来源事实合同

R21 采用“标准 tag 全量词法校验”策略：

- `str/bool/int/float/null/timestamp` 均需同时通过 tag 白名单与原始词法校验；
- 合法隐式和显式标准 typed tag 均可进入 Canonical；
- 验证通过后仍保存原始词法，不构造成 Java 业务对象；
- `null` 仅在词法为 `~`、大小写 null 或空值时映射为空 scalar；
- 非法 typed 词法统一返回 `yaml.frontend.scalar.invalid-lexeme`；
- 不调用 `load` / `loadAs`，不执行任意用户类型 construction；
- `!!null attacker-data`、`!!int not-an-int` 等不得通过 tag 名白名单；
- 前导零非法十进制、孤立小数点、越界日期/时间/时区等近似词法也必须失败。

兼容性决策：仓库实际 SnakeYAML 2.2 不公开 `ScalarNode.isResolved()`，因此不依赖实现私有状态区分显式/隐式；使用独立 `YamlScalarLexemePolicy` 对两类输入执行同一来源事实规则。

Oracle 覆盖普通 scalar、`#text`、`@attributes` value、Sequence item，并覆盖 null/int/bool/float/timestamp 的正负词法。

## 4. Canonical 名称与 nodePath 合同

R21 不改变 compiler `SourceRef` 公共 API，而在 YAML Frontend 输入边界限制进入 nodePath 的 Canonical 节点名称。

允许节点名称为可移植 XML-compatible ASCII NCName 子集：

```text
[A-Za-z_][A-Za-z0-9._-]*
```

因此节点名称不得包含：

- `/`、`~`、反斜线；
- `:`；
- 空格、制表符、CR、LF及其他控制字符；
- 以数字、点或连字符开头的名称；
- 非 ASCII 字符。

`@attributes` 与 `#text` 仍只作为 Mapping 保留 key，不是 Canonical 节点名称。属性 key 不参与 nodePath，因此不因本 Finding 新增路径限制，继续沿用 R20 的非空、非保留、非 merge、非重复规则。

节点名称不符合合同必须在 nodePath 拼接和 Canonical 节点分配前失败，messageKey 为 `yaml.frontend.node.invalid-name`。合法名称的路径继续使用 `/segment`，因为每个 segment 已保证不包含分隔符或换行。

## 5. 不变量

- 继续只使用 `SafeConstructor + composeAll`；
- tag/object/anchor/alias/递归/merge/duplicate/complex key 边界不回退；
- R20 资源预算值不变化；
- 预算检查顺序不回退；
- 失败不发布部分 root；
- XML Frontend 生产语义不变化；
- compiler canonical 公共 API不变化；
- Context 生产代码不变化；
- T06 不启动。

## 6. 验收门禁

- RED 可按 Java 8 编译，新增 Oracle 精确暴露 3 个 Finding；
- Architecture Skeleton 建立严格解码、scalar 词法 policy、名称 policy 接缝；
- Development 后原有 35 项 YAML、I002 来源事实 Oracle 和 Review 近似词法 Oracle 全绿；
- Context、Compiler、XML、Demo、legacy declaration 和 12 模块 Reactor 全绿；
- Specification、Architecture、Security、Code、TDD 五类独立 Review 全部 PASSED；
- 开放 P0/P1/P2 为 0；
- `@Override` 独占一行；
- 公共方法、构造器和关键来源事实逻辑使用中文注释；
- PR #20 最终恢复 Ready for review，但未经明确授权不得合并。
