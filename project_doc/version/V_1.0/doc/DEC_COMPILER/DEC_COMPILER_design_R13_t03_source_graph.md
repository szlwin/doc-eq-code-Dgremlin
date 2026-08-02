# DEC Compiler Design R13 — T03 安全源发现与精确 SourceGraph

- Revision：`DESIGN-R13@P1-T03-I001`
- 输入：`DESIGN-R05@0b37a9b4dd48`、`TESTDESIGN-R01@ba7779cf089b`、`TP-P1-COMPILER-F01-R01@88b56e6caa64`、`COMPLETION-P1-T02-R05@35376308b013`
- 任务：`TASK-P1-T03 / I001`
- 状态：`PASSED`

## 1. 目标与范围

本 Revision 只交付 Source discovery 和精确 SourceGraph，不实现 XML/YAML Canonical Frontend、RawDefinitionSet、Symbol、Compiler Pipeline 或发布。固定入口 `classpath:mix/orm-config.xml` 必须产生 10 个唯一 Source 和 7 条真实声明边。

Source discovery 只读取 Provider 返回的字节。生产代码不得硬编码 `dec-demo` 路径，不访问网络，不回退到其它同名资源，也不得在发现阶段登记业务定义。

## 2. 公共类型

新增以下 Java 8 不可变合同：

```text
SourcePolicy
SourceEdgeType
SourceGraphEdge
SourceManifest
MixSourceGraph
SourceGraphResolutionStatus
SourceGraphResolutionResult
SourceGraphResolutionResults
MixSourceResolver
```

`DocumentSourceProvider`、`DocumentSource`、`SourceReference`、`SourceResolutionResult` 和 `AllowedRoot` 复用 T02 已冻结合同，不修改其成功/失败基数语义。

## 3. SourcePolicy

`SourcePolicy` 冻结：

- `allowedSchemes`：非空、稳定排序、全部小写；
- `allowedRoot`：唯一允许根；
- `maxDepth >= 0`；
- `maxSources > 0`；
- `maxTotalBytes > 0`。

在调用 Provider 前必须验证：

1. SourceReference 可解析为绝对 URI；
2. scheme 位于白名单；
3. URI 不含 query、fragment、字面量或编码父目录穿越；
4. URI 位于 allowedRoot；
5. 当前 depth 不超过上限。

根外、未知 scheme、非法 URI 和 traversal 均映射为 `MIX-SOURCE-PATH-ESCAPE`，并且 Provider 访问计数保持 0。Provider 自身返回的路径安全 Diagnostic 原样保留。

## 4. SourceManifest

`SourceManifest` 持有按稳定 discovery key 排序的不可变 `List<DocumentSource>`，并暴露：

- `sources()`；
- `sourceIds()`；
- `totalBytes()`。

每个 sourceId 只允许出现一次。即使 URI、format、digest 和内容完全相同，重复 sourceId 也必须以 `MIX-SOURCE-DUPLICATE-ID` 阻断，禁止静默合并或最后写入覆盖。

累计 Source 数量和内容字节在每次登记前检查。超出 `maxSources` 或 `maxTotalBytes` 使用 `MIX-SOURCE-POLICY`，失败结果不得暴露部分图。

## 5. SourceGraphEdge

冻结边类型：

```text
ROOT_DATA_FILESET
ROOT_VIEW_FILESET
ROOT_SYSTEM_FILE
ROOT_BUSINESS_FILE
SYSTEM_RULE_FILE
```

每条 `SourceGraphEdge` 必须包含：

- `edgeType`；
- `fromSourceId`；
- `targetReference`；
- `declarationSourceRef`。

目录展开得到的 3 个 Data 和 1 个 View 只进入 SourceManifest，不产生额外边。边按 `fromSourceId + edgeType.ordinal + targetReference.value + declarationSourceRef` 稳定排序。

## 6. 固定 Oracle

固定 Source 集合：

```text
classpath:mix/orm-config.xml
classpath:mix/data/User.xml
classpath:mix/data/Order.xml
classpath:mix/data/Pay.xml
classpath:mix/view/orm-view.xml
classpath:mix/system/systems.xml
classpath:mix/rule/user-rule.xml
classpath:mix/rule/order-rule.xml
classpath:mix/rule/payment-rule.xml
classpath:mix/business/order-business.xml
```

固定声明边：

1. root → `classpath:mix/data/`，`ROOT_DATA_FILESET`；
2. root → `classpath:mix/view/`，`ROOT_VIEW_FILESET`；
3. root → systems，`ROOT_SYSTEM_FILE`；
4. root → business，`ROOT_BUSINESS_FILE`；
5. systems → user rule，`SYSTEM_RULE_FILE`；
6. systems → order rule，`SYSTEM_RULE_FILE`；
7. systems → payment rule，`SYSTEM_RULE_FILE`。

根 Source 的 origin 使用 synthetic `SourceRef(root, 0, 0, "/root")`。声明边 SourceRef 指向父 Source 内声明元素的起始行、列和节点路径。

## 7. 最小声明解析

T03 允许使用 JDK StAX 做“声明提取”，但不产出 Canonical：

- 关闭 DTD 和外部实体；
- 只识别 root 文件的 `orm-data-file-info/orm-file`、`orm-view-file-info/orm-file`、`system-file-info/system-file`、`business-file-info/business-file`；
- 只识别 system 文件的 `rule-file-info/rule-file`；
- 其它 XML 内容全部忽略；
- 声明结构不完整、重复阻断声明或解析异常统一形成 `MIX-SOURCE-POLICY`；
- 不允许网络、外部 schema 或文件系统回退。

T04 仍负责完整 XML Canonical Frontend；本解析器不得被当作业务 XML Frontend 使用。

## 8. Resolver 算法

1. 验证 root reference；
2. 调用 `provider.resolve` 并通过 `SourceResolutionResults.validateSingle` 防御性验证；
3. 登记 root；
4. 提取 root 的 4 类声明，按声明类型固定顺序处理；
5. file-set 调用 `resolveFileSet`，验证后按 sourceId 排序再登记；
6. system 文件登记后立即提取 rule 声明，再继续 business 文件，使规则 Source 在业务 Source 之前稳定登记；
7. 每次 Provider 调用前执行 SourcePolicy；每次 Source 登记前执行 sourceId、数量、字节和 allowedRoot 检查；
8. 显式引用缺失保留 `MIX-SOURCE-NOT-FOUND`；Provider null、抛异常、错误基数和非法 typed result 映射 `MIX-SOURCE-POLICY`；
9. 检测当前解析路径中的重复引用和 cycle，使用 `MIX-SOURCE-POLICY`；
10. 无 ERROR 时冻结 `SourceManifest` 与 `MixSourceGraph`，否则返回不含图的 FAILED。

## 9. 结果合同

`SourceGraphResolutionResult`：

- `RESOLVED`：恰好一个 `MixSourceGraph`，Diagnostic 不含 ERROR；
- `FAILED`：graph 为空，至少一个 ERROR；
- Diagnostic 稳定排序且集合不可变；
- 不得以 null、未分类异常或部分图表达失败。

## 10. TDD Oracle

必须覆盖：

- 精确 10 Source / 7 Edge；
- 正序、逆序、随机文件集枚举得到完全相同图；
- 主资源和测试镜像字节一致，并分别解析为同一图；
- unknown scheme、`../`、编码 traversal 在 Provider 调用前拒绝；
- missing explicit source；
- duplicate sourceId，同内容和不同内容都拒绝；
- null/throwing Provider、单源错误基数、空文件集；
- maxDepth、maxSources、maxTotalBytes；
- 所有失败均无部分 graph；
- Java release 8、Compiler → Context 单向依赖和完整 Reactor 保持通过。

## 11. 编码规则

- 所有 `@Override` 注解独占一行；
- 公共方法、构造器和重要安全/排序逻辑使用中文注释；
- 不使用 Java 9+ API；
- 不修改 `dec-core-context` 生产代码；
- 不实现 TASK-P1-T04 及后续任务。
