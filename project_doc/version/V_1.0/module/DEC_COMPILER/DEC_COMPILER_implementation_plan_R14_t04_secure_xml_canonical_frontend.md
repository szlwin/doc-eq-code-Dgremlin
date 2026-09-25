# DEC_COMPILER Implementation Plan R14 — T04 安全 XML Canonical Frontend

- Revision：`TP-P1-COMPILER-F01-R14@P1-T04-I001`
- 输入 Design：`DESIGN-R18@P1-T04-I001`
- 前置 Completion：`COMPLETION-P1-T03-R05@91271c9a1c20`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- 状态：`PASSED`

## 1. TDD RED

新增 `XmlCanonicalContractTest` 与 `XmlFrontendSecurityTest`。测试通过反射加载预期 Frontend，使生产类缺失时测试源码仍可 Java 8 编译，并以明确行为断言形成 RED。

RED 必须证明：

- Context 与既有 Compiler 全绿；
- 新 XML Oracle 因 Frontend 尚不存在而失败；
- 失败不是依赖下载、编译错误或测试未被发现；
- Artifact 与 RED Head 精确绑定。

## 2. Architecture Skeleton

- XML POM 增加 `dec-core-compiler` 单向依赖；
- 新增 `SecureXmlDocumentFrontend implements DocumentFrontend`；
- 冻结构造器、`format()` 和 `parse(...)` 入口；
- 建立安全 StAX 工厂、节点 builder、SourceRef locator 与受控失败边界；
- 不调用旧 Config API，不修改 compiler canonical 公共签名。

Skeleton 阶段允许安全/位置 Oracle 部分保持 RED，但必须 Java 8 编译且不存在错误模块依赖。

## 3. Development GREEN

- 实现 local-name Canonical、属性稳定排序、文本/CDATA 标量、子节点文档顺序；
- 每个节点发布 start tag `<` 的精确 SourceRef 与完整 nodePath；
- 显式拒绝 DTD，关闭外部实体和实体替换，XMLResolver 拒绝外部资源；
- schemaLocation 只作为普通属性，不触发 SchemaFactory 或外部访问；
- 任何异常映射为 `MIX_FRONTEND_XML_UNSAFE`，FAILED 不携带部分 root；
- 对错误 `DocumentFormat` 在读取内容前失败。

## 4. 独立 Review

依次执行：

1. Specification Review：Canonical 字段、顺序、SourceRef、失败状态；
2. Architecture Review：compiler-owned API、XML 单向依赖、无全局配置写入；
3. Security Review：DTD/XXE/schema/network/file/XInclude 零访问边界；
4. Code Review：Java 8、不可变性、中文注释、`@Override` 格式；
5. TDD Review：RED 有效、Oracle 独立、负向测试完整。

开放 P0/P1 必须重开，不得进入 Completion。

## 5. Testing 与 Completion

验证命令：

```bash
./mvnw -pl dec-context-config-parse-xml,dec-core-compiler -am -Dtest=XmlFrontendSecurityTest,XmlCanonicalContractTest test
./mvnw --batch-mode --no-transfer-progress clean verify
scripts/remediation/prove_test_failure_gate.sh
```

记录 Context、Compiler、XML、Reactor、Java 8、故意失败门禁和 Artifact。MySQL 仅可记录为 `SKIPPED_NOT_APPLICABLE`。

## 6. 允许范围

- `dec-context-config-parse-xml/pom.xml`
- `dec-context-config-parse-xml/src/main/java/dec/core/compiler/canonical/xml/**`
- `dec-context-config-parse-xml/src/test/java/dec/core/compiler/canonical/xml/**`
- T04 Design、Plan、task、Evidence、handoff、resume 和机器 checkpoint

## 7. 禁止范围与停止条件

- 不修改 `dec-core-context` 生产代码；
- 不改变 T02/T03 公共 Source/Canonical API；
- 不实现 YAML、RawDefinitionSet、Symbol 或 Pipeline；
- 不让 XML Frontend 写 ConfigFactory、ConfigInfo、Registry 或 EngineContext；
- 若现有 Canonical 契约无法表达合法 XML，停止并登记 Finding，不私自改语义；
- PR 未经明确授权不得合并。
