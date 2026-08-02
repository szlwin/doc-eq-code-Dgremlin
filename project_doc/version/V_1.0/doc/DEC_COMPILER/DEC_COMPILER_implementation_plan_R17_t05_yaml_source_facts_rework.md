# DEC Compiler Implementation Plan R17 — T05 YAML Source Facts Rework

- Plan Revision：`TP-P1-COMPILER-F01-R17@P1-T05-REWORK-I002`
- Design Input：`DESIGN-R21@P1-T05-REWORK-I002`
- Task：`TASK-P1-T05 / I002`
- PR：`#20`

## 1. 执行顺序

1. 将 PR #20 切回 Draft；
2. 保留 I001 Completion R01 与全部历史记录；
3. 建立 I002 Design、Plan、Task、Finding；
4. 先提交 I002 RED Oracle；
5. 以 P0 Artifact 证明新增 Oracle 仅因三个目标缺口失败；
6. 建立严格解码、typed scalar 词法、ASCII NCName 接缝；
7. 实现并补齐正向、负向和近似词法 Oracle；
8. 执行五类独立 Review；
9. 全量 P0、Evidence、Completion R02、handoff 和 resume；
10. 更新 PR #20 并恢复 Ready for review，禁止自动合并。

## 2. TDD Oracle

新增 `YamlFrontendSourceFactsReworkTest`：

- 原始 byte[]：非法 continuation、截断多字节、overlong、UTF-8 surrogate；
- typed scalar：普通节点、`#text`、`@attributes` value、Sequence item；
- 非法 typed 词法：null/int/bool/float/timestamp；
- 合法显式 str、bool、int、float、null、timestamp 正向控制；
- node name：`a/b`、包含换行、包含冒号、数字开头均失败；
- 合法 NCName 的 nodePath 保持原有 `/root/child-name` 语义。

新增 `YamlScalarLexemePolicyReviewTest`：

- `!!int 09`；
- `!!float .`；
- 不存在日期；
- 24 点时间；
- 越界时区；
- 伪 bool/null。

每个负向 Oracle 必须先验证安全控制样本可解析，防止“拒绝所有 YAML”伪实现。

## 3. Architecture Skeleton

冻结三个小型接缝：

- `decodeUtf8(byte[])`：严格 decoder；
- `YamlScalarLexemePolicy.isValid(Tag, String)`：无对象构造的标准 typed 词法规则；
- `requireName(...)`：在 path 拼接前执行 ASCII NCName 校验。

不得引入通用对象构造、反射加载或新的 compiler 公共 API。

## 4. Development

### 4.1 UTF-8

- 使用 `ByteBuffer`、`CharsetDecoder`、`CodingErrorAction`、`CharacterCodingException`；
- parser 输入只来自 `decodeUtf8(content)`；
- CharacterCodingException 转 `YamlUnsafeException`；
- 不使用替换字符继续解析。

### 4.2 Scalar tag

- 继续允许 `STR/BOOL/INT/FLOAT/NULL/TIMESTAMP`；
- 所有非字符串标准 tag 均执行冻结词法校验；
- bool/null 使用封闭枚举词法；
- int 覆盖二、八、十、十六和六十进制格式并拒绝非法前导零；
- float 覆盖普通、指数、特殊值和六十进制格式，并拒绝孤立小数点；
- timestamp 同时校验结构、真实日期、时间和时区范围；
- 验证后保留原始词法，不执行 Java 业务对象 construction；
- 合法 null 才映射为空 Optional/空属性值。

### 4.3 Name/path

- 使用字符级判断实现 `[A-Za-z_][A-Za-z0-9._-]*`，避免 regex 隐藏分配；
- 仅对进入 nodePath 的根和子节点名称应用该规则；
- 在 `reserveNode` 和 nodePath 拼接前拒绝非法名称；
- 属性 key 继续使用 R20 规则，不因路径 Finding 扩大限制；
- 保留简单合法名称的 XML/YAML parity。

## 5. Review

- Specification：R21 与行为一致；
- Architecture：依赖方向、API 边界、T06 范围；
- Security：原始字节 fail closed、typed tag 不丢失来源事实、路径无歧义；
- Code：Java 8、中文注释、`@Override` 独行、无无界分配；
- TDD：RED 可信、正负控制完整、Artifact 精确绑定 Head。

## 6. Completion

新建 `COMPLETION-P1-T05-R02@<clean-code-head>`，R01 保留并标记被 I002 Review 推翻。最终文档化 Head 必须再跑独立 P0，并记录 Run、Artifact 与 SHA-256。
