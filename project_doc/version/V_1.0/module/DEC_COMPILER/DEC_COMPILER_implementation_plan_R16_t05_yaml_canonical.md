# DEC_COMPILER Implementation Plan R16 — T05 安全 YAML Canonical Frontend

- Revision：`TP-P1-COMPILER-F01-R16@P1-T05-I001`
- 输入 Design：`DESIGN-R20@P1-T05-I001`
- 前置 Completion：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- Base：`dev_all@09edf814bdf0800e7e9633545ca743200169b377`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- 状态：`PASSED`

## 1. TDD RED R01

在 `dec-context-config-parse-yaml` 增加反射加载型 Oracle，使测试源码在生产 Frontend 尚不存在时仍可按 Java release 8 编译并形成有效行为 RED。

RED 必须覆盖：

- `SafeYamlDocumentFrontend` 的公开 final `DocumentFrontend` 合同；
- 最小 YAML Mapping 到 Canonical；
- Java/object tag 与 local tag 拒绝，用户类型构造次数保持 0；
- anchor、alias bomb、递归 alias、duplicate key、merge key 拒绝；
- malformed、空、多 document、错误 root、复杂 key、null source/options、错误格式稳定失败；
- 文档、code point、nesting depth、节点、路径、Mapping、Sequence、单 scalar、累计 scalar 预算；
- 等价 XML/YAML 语义树、重复子节点、属性、标量、schemaVersion 和 SourceRef nodePath。

有效 RED 必须满足：

- Context、Compiler、XML T04 和 YAML 旧回归继续通过；
- 新测试源码可编译；
- 失败仅来自 T05 Frontend/limits 接缝不存在或行为未实现；
- 不允许通过缺少依赖、语法错误、fixture 缺失或类加载链异常伪造 RED。

## 2. Architecture Skeleton R01

- YAML POM 增加 `dec-core-compiler` 生产依赖和 XML 模块 test 依赖；
- 新增 package-private 不可变 `YamlFrontendLimits`，冻结全部生产常量和正数参数校验；
- 新增 public final `SafeYamlDocumentFrontend`；
- 公共无参构造器使用 production limits；
- package-private 构造器注入 limits，供小型确定性资源测试；
- `format()` 返回 YAML；
- `parse()` 先建立稳定参数失败与空 root 边界；
- Skeleton 不提前实现对象 tag、alias、Canonical 转换或资源行为，相关 Oracle 保持 RED。

## 3. Development GREEN R01

### 3.1 Parser 安全

- 使用 `LoaderOptions` 显式关闭 duplicate key、recursive key、merge、alias，并设置 code point 和 nesting depth；
- 使用 `SafeConstructor` 与不可信 tag inspector；
- 只调用 compose 表示树入口，不调用 `load`、`loadAs` 或任意 Bean/object 构造入口；
- require exactly one YAML document；
- 遍历时再次验证允许 tag、AnchorNode、anchor metadata 和对象身份，形成双重 fail-closed 边界。

### 3.2 Canonical 转换

- root 必须是单 key Mapping；
- 实现 `@attributes`、`#text`、普通子节点和 Sequence 重复节点映射；
- key 只接受非空字符串 Scalar；
- scalar trim，null/空值映射为 Optional.empty；
- 属性稳定排序由 `CanonicalDocumentNode` 完成；
- 子节点保持文档顺序；
- schemaVersion 传播到所有后代；
- 不在任何字段、结果或 Diagnostic 中暴露 SnakeYAML Node。

### 3.3 SourceRef 与预算

- Mark 零基位置转换为一基 line/column；
- Mapping 子节点使用 key Mark，Sequence item 使用 item Mark；
- nodePath 使用父路径单次拼接；
- parser 前检查文档字节；
- Canonical 分配前检查节点、路径、Mapping、Sequence；
- scalar 保存前检查单值及累计字符；
- 累计计数执行溢出安全加法；
- 任何失败统一映射 `MIX_FRONTEND_YAML_UNSAFE`，无部分 root。

## 4. Refactor 与编码规范

- 消除递归之外的不必要重复遍历；最大深度 128 保证递归有界；
- 资源统计、tag 检查、SourceRef 定位和 Canonical 构造职责分离；
- 生产类与 limits 保持 package 内聚，不扩散 compiler 公共 API；
- 所有 `@Override` 独占一行；
- 公共方法、构造器和重要安全/资源/映射逻辑使用中文注释；
- 保持 Java release 8。

## 5. 五类独立 Review

依次执行：

1. Specification Review：R20 映射、保留 key、scalar、SourceRef 与 XML/YAML parity；
2. Architecture Review：模块依赖、public API、无旧 Config/Runtime/Raw/T06 泄漏；
3. Security Review：无对象构造、tag/anchor/alias/recursion/merge fail closed、预算在危险分配前；
4. Code Review：复杂度、溢出安全、异常边界、Java 8、中文注释、`@Override`；
5. TDD Review：有效 RED、每类攻击和预算有独立 Oracle、旧测试不回归、无真实 OOM。

任一开放 P0/P1 必须进入 Rework，不得生成 Completion。

## 6. Testing 与 Completion

阶段验证：

```bash
./mvnw -pl dec-context-config-parse-yaml,dec-context-config-parse-xml,dec-core-compiler -am test
./mvnw --batch-mode --no-transfer-progress clean verify
scripts/remediation/prove_test_failure_gate.sh
```

必须记录：

- Context、Compiler、XML、YAML 和 Demo 的测试计数；
- YAML 安全、资源预算、Canonical parity 专项计数；
- 12 模块 Reactor、Java release 8、故意失败门禁；
- clean-code Head、P0 Run、Artifact 与 SHA-256；
- 最终文档化 Head 的独立 P0；
- MySQL 只可记录为 `SKIPPED_NOT_APPLICABLE`。

Completion R01 必须绑定 R20、R16、TDD、Skeleton、Development、五类 Review、Testing 和最终 Evidence，明确开放 P0/P1 为 0、T06 未启动、PR 未经明确授权不得合并。

## 7. 允许与禁止范围

允许：

- `dec-context-config-parse-yaml` 的 POM、T05 生产/测试包；
- XML 模块仅作为 parity 测试依赖，不修改 XML 生产实现；
- T05 Design、Plan、Task、Review、Evidence、Testing、Completion、handoff、resume、机器 checkpoint；
- T05 PR 描述。

禁止：

- 修改 `dec-core-context` 生产代码；
- 修改 compiler canonical 公共 API；
- 修改 XML Frontend 生产语义；
- 实现 RawDefinitionSet、TypedKey、引用解析或 Pipeline；
- 调用通用 YAML 对象加载；
- 捕获或制造真实 OOM；
- 自动合并 PR。
