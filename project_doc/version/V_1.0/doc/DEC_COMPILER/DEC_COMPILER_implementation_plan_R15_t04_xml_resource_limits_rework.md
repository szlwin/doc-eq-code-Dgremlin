# DEC_COMPILER Implementation Plan R15 — T04 XML 资源预算 Rework

- Revision：`TP-P1-COMPILER-F01-R15@P1-T04-REWORK-I002`
- 输入 Design：`DESIGN-R19@P1-T04-REWORK-I002`
- 输入 Review：`REV-000207`
- 历史 Plan：`TP-P1-COMPILER-F01-R14@P1-T04-I001`（保留，不再作为当前执行规则）
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- 状态：`PASSED`

## 1. 合同纠正

R14 Development GREEN 中“schemaLocation 作为普通属性”的描述与 Design R18 冲突。R15 明确规定：检测到 `xsi:schemaLocation` 或 `xsi:noNamespaceSchemaLocation` 时立即返回受控失败，不触发 SchemaFactory、网络或文件访问。当前实现方向保持，旧 R14 仅作为历史输入。

## 2. TDD RED R02

新增 `XmlFrontendResourceLimitTest`，通过反射加载 package-private 限制策略和构造器，使测试源码在生产策略尚不存在时仍可 Java 8 编译，并形成有效行为 RED。

RED Oracle：

- 预算边界内成功；
- 文档字节、深度、节点数、累计 nodePath 字符、属性数、单节点直接文本、累计直接文本超限分别失败；
- 所有超限结果均为 `FAILED`、`MIX_FRONTEND_XML_UNSAFE`、无 canonical root、外部访问计数 0；
- 不构造真实 OOM。

同时补充已明确合同的直接 Oracle：

- 文本—CDATA—文本顺序拼接；
- 子节点和孙节点 schemaVersion；
- null source、null options 稳定失败。

有效 RED 必须保持 Context、Compiler 和原 T04 Oracle 全绿，新资源预算 Oracle 因限制策略/构造器缺失而失败，且不是编译或依赖错误。

## 3. Architecture Skeleton R02

- 新增 package-private 不可变 `XmlFrontendLimits`；
- 冻结生产默认常量和正数参数校验；
- `SecureXmlDocumentFrontend` 增加 limits 字段及同包测试构造器；
- 公共构造器继续零参数并使用 production limits；
- Skeleton 只建立策略和依赖接缝，不提前宣称预算行为完成；预算超限 Oracle 保持 RED。

## 4. Development GREEN R02

- reader 创建前检查文档字节；
- `START_ELEMENT` 创建 `SourceRef`、属性 Map、`NodeBuilder` 前检查深度、节点数、属性数和累计路径字符；
- nodePath 改为父路径单次拼接，删除祖先栈遍历实现；
- 文本追加前检查单节点及累计直接文本；
- 每个 start tag 只调用一次 locator；
- 行列换算使用二分查找；
- 每项预算使用稳定 messageKey，统一映射 `MIX_FRONTEND_XML_UNSAFE`；
- 所有现有安全、Canonical、SourceRef 和架构行为保持。

## 5. 独立 Review

依次执行：

1. Specification Review：预算值、边界语义、CDATA/schemaVersion/null 合同；
2. Architecture Review：limits 内部封装、公共 API 不扩散、无 compiler 反向依赖；
3. Security Review：危险分配前门禁、无 OOM 测试、无外部 I/O、无部分 root；
4. Code Review：复杂度、溢出安全、Java 8、中文注释、`@Override` 格式；
5. TDD Review：R02 RED 有效、每个预算独立命中、旧 Oracle 不回归。

开放 P0/P1 必须继续 Rework，不得创建新 Completion。

## 6. Testing 与 Completion

验证：

```bash
./mvnw -pl dec-context-config-parse-xml,dec-core-compiler -am test
./mvnw --batch-mode --no-transfer-progress clean verify
scripts/remediation/prove_test_failure_gate.sh
```

必须记录 Context、Compiler、XML T04 总数、资源预算专项、12 模块 Reactor、Java release 8、故意失败门禁、Artifact 和最终 Head。MySQL 只能记录为 `SKIPPED_NOT_APPLICABLE`。

Completion R02 必须明确：

- R01 被 `REV-000207` 推翻并保留；
- R19/R15 为当前设计和计划；
- 全部 P1 已关闭；
- P2 计划冲突和测试缺口已关闭；
- T05 未启动；
- PR #19 未经明确授权不得合并。

## 7. 允许与禁止范围

允许：XML Frontend 生产/测试包、T04 文档、task、Evidence、handoff、resume、机器 checkpoint、PR #19 描述。

禁止：修改 `dec-core-context` 生产代码，修改 compiler canonical 公共 API，实现 YAML、RawDefinitionSet、Symbol、Pipeline，捕获或制造真实 OOM，自动合并 PR。
