# TASK-P1-T05 / I002 — Independent Review R02

- Code Input：`27d566714f5c4e521a969b92d4642111971bb96e`
- Design：`DESIGN-R21@P1-T05-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R17@P1-T05-REWORK-I002`
- Result：`PASSED`
- Open P0/P1/P2：`0 / 0 / 0`

## Review Ledger

| Review | ID | Evidence | Result |
|---|---|---|---|
| Specification | `REV-000237` | `EVD-000480` | PASSED |
| Architecture | `REV-000238` | `EVD-000481` | PASSED |
| Security | `REV-000239` | `EVD-000482` | PASSED |
| Code | `REV-000240` | `EVD-000483` | PASSED |
| TDD | `REV-000241` | `EVD-000484` | PASSED |

## Specification Review

- 严格 UTF-8 行为与 R21 一致；
- 标准 typed tag 采用全量词法校验，合法显式/隐式值保留原始词法；
- null 只有合法词法才映射为空；
- 根、子节点和属性名使用冻结 portable name 子集；
- R20 Canonical 主映射、资源预算和失败边界未回退；
- I001 Completion R01 被 `REV-000231` 推翻但历史未覆盖。

## Architecture Review

- 变更仅位于 YAML 模块及 T05 文档；
- `SafeYamlDocumentFrontend` 继续实现 compiler-owned `DocumentFrontend`；
- YAML 模块继续单向依赖 compiler canonical API；
- `YamlScalarLexemePolicy` 为 package-private final 内部策略，无新增公共 API；
- 未读取 SnakeYAML 私有状态，未使用反射；
- 未修改 Context、compiler canonical 公共 API或 XML 生产语义；
- 未实现 RawDefinitionSet、Symbol、Pipeline 或 T06。

## Security Review

- 非法 continuation、截断、overlong、UTF-8 surrogate 在 parser 前 fail closed；
- parser 不接收替换字符构成的字符串；
- custom/object/local tag、危险标准 tag、anchor、alias、共享/递归图、merge、complex/duplicate key边界保持通过；
- typed tag 在普通 scalar、`#text`、属性 value 和 Sequence item 四个位置执行同一词法门禁；
- `!!null attacker-data` 不再静默丢失；
- `09`、`.`、不存在日期、24 点时间、越界时区等近似词法失败；
- 合法 typed tag 正向控制通过，排除“拒绝全部 typed tag”伪实现；
- 路径 segment 不允许 `/`、CR/LF 或其他歧义名称；
- 失败不发布部分 root；
- 文档最大 1 MiB，词法 regex 均锚定且受现有 code point/scalar 预算约束。

## Code Review

- Java release 8 兼容；
- `@Override` 独占一行；
- 公共方法、构造器、严格解码、词法、路径、资源和失败逻辑均有中文注释；
- `CharsetDecoder`、Pattern、BigDecimal、LocalDate 均为有界局部或静态不可变对象；
- 累计 node/path/scalar 计数继续使用溢出安全 long；
- 不捕获 `OutOfMemoryError`，不使用真实 OOM 测试；
- 无通用对象加载或不受控外部访问。

## TDD Review

- RED Head `c362011eac56...` 可按 Java 8 编译；
- I002 8 run / 6 expected failures / 0 errors，两个正向控制通过；
- 原始 byte[] Oracle 不经 String 合法化；
- 每个负向测试先验证安全控制样本；
- Development 增加合法显式 typed 正向控制；
- Review 增加七类近似词法负向控制；
- Clean-code Head YAML 45/45；
- Artifact 与各阶段 Head 精确绑定。

## Finding Closure

- `FND-P1-T05-I002-001`：`CLOSED` — 严格 UTF-8 decoder + 4 类原始字节 Oracle；
- `FND-P1-T05-I002-002`：`CLOSED` — 标准 tag 全量词法策略 + 四位置正负 Oracle；
- `FND-P1-T05-I002-003`：`CLOSED` — portable name policy + path 正负 Oracle。

## 范围门禁

PR #20 未合并；T06 未启动；未经用户明确授权不得合并。
