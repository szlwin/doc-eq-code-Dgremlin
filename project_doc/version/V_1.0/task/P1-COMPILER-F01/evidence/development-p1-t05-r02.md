# TASK-P1-T05 / I002 — Development Evidence

- Development：`DEV-P1-T05-R02@27d566714f5c`
- Review：`REV-000236`
- Evidence：`EVD-000479`
- Clean-code Head：`27d566714f5c4e521a969b92d4642111971bb96e`
- P0 Run：`30752686888`
- Artifact：`8834954051`
- Artifact SHA-256：`44ca69b67e75e46278f8b622fe864293e7251154456f1809d75d97a44e7f0090`
- Result：`PASSED`

## 实现

### 严格 UTF-8

- 原始 `DocumentSource.content()` 在 parser 创建前进入 `CharsetDecoder`；
- malformed 与 unmappable 均使用 `CodingErrorAction.REPORT`；
- `CharacterCodingException` 转换为 `yaml.frontend.encoding.invalid-utf8`；
- 禁止 U+FFFD 替换后继续解析。

### 标准 typed scalar

- 新增 `YamlScalarLexemePolicy`，不调用 `load`、`loadAs` 或任意用户对象构造；
- bool/null 使用封闭词法；
- int 覆盖二、八、十、十六和六十进制，并拒绝 `09` 等非法前导零；
- float 覆盖普通、指数、特殊和六十进制，并拒绝孤立 `.`；
- timestamp 校验真实日期、时间与时区范围；
- 合法显式和隐式 typed scalar 保留原始词法；
- 只有合法 null 映射为空 scalar/空属性值。

### 名称与路径

- 根、子节点和属性名使用 `[A-Za-z_][A-Za-z0-9._-]*`；
- 节点名在 nodePath 拼接及 Canonical 分配前校验；
- `/`、CR、LF、冒号、空白、非法首字符及非 ASCII 名称均失败；
- 合法路径保持 `/root/child-name_1`。

## 验证

- Context：26/26；
- Compiler：83/83；
- XML：30/30；
- YAML：45/45，其中 I001 35、I002 Source Facts 9、近似词法 Review 1；
- Demo：4/4；
- legacy declaration：1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 编码规范

- 所有新增和修改的 `@Override` 独占一行；
- 公共方法、构造器和严格解码、词法、路径、资源、失败逻辑均使用中文注释；
- 未修改 Context 生产代码、compiler canonical 公共 API或 XML 生产语义；
- 未启动 T06。
