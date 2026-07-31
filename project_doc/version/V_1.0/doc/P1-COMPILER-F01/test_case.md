# P1-COMPILER-F01 可执行测试设计

> Revision：`TESTDESIGN-R01@ba7779cf089b`。Iteration：`TEST_DESIGN-I007`。输入固定为 `REQCONF-R04@c186ce681e1e`、`REQAN-R05@7de35e8dc15b`、`BM-R05@4ecb1f8c09f4`、`DESIGN-R05@0b37a9b4dd48`。
>
> 本阶段只冻结 Case、fixture、稳定接缝、oracle、禁止副作用、RED 合同和证据采集方式；不编写生产实现，不把模块不存在、依赖下载失败或语法错误当作有效 RED。

## 1. 阶段目标与退出门禁

- 9 条 TR 与 9 项 AC 均至少映射一个阻断型可执行 Case；
- BM-R05 的 23 个稳定业务 Diagnostic code 全覆盖，另覆盖 DESIGN-R05 的 7 个编排/可观测性 code；
- 精确冻结 10 个 SourceManifest source、7 条 declaration edge 和 5/2/4/14/16/1/5/8/4 inventory；
- 覆盖正常、边界、异常、安全、并发、幂等/重试边界、超时、取消、CAS conflict、失败不发布和退役；
- 每个 Case 明确真实行为 oracle、禁止副作用、未来 RED 的有效失败原因和证据命令；
- `DesignReviewAgent`、`RequirementReviewAgent`、`TDDReviewAgent`、`TestEvidenceReviewAgent` 必须针对同一 Revision 串行独立通过。

## 2. 测试层级与环境

| 层级 | 目的 | 主要实现位置 | 外部边界 | 证据 |
|---|---|---|---|---|
| unit | TypedKey、selector、ownership、Deferred、状态机策略 | `dec-core-compiler` | 仅稳定 provider/clock/publisher seam | Surefire XML + command-result |
| contract | 真实 mix source graph、Canonical、Raw、Context/Projection API | compiler/context/frontend/demo test fixture | 使用内存 provider 或真实 classpath fixture | 快照 + 数量/摘要断言 |
| security | path/XML/YAML 拒绝边界 | source policy/frontends | 网络、文件访问使用 fail-fast probe | 访问计数 0 + Diagnostic |
| concurrency | Session 隔离、digest、CAS publication | compiler/context | deterministic executor/clock，不使用 sleep | 重复运行快照 |
| architecture | Java 8、模块依赖、退役残留 | Maven reactor + repository scan | 不访问业务数据库 | compile/dependency/static scan |

独立数据库不在 P1 Compiler test_design 的核心范围；若后续 Case 引入持久化，则必须使用显式 `TEST_DB_URL/TEST_DB_USER/TEST_DB_PASSWORD`，缺任一项只跳过该子范围，不得回退到 local/dev/生产库。

## 3. 固定 SourceGraph oracle

### 3.1 SourceManifest

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

### 3.2 Declaration edges

| edgeType | fromSourceId | targetReference |
|---|---|---|
| `ROOT_DATA_FILESET` | `classpath:mix/orm-config.xml` | `classpath:mix/data/` |
| `ROOT_VIEW_FILESET` | `classpath:mix/orm-config.xml` | `classpath:mix/view/` |
| `ROOT_SYSTEM_FILE` | `classpath:mix/orm-config.xml` | `classpath:mix/system/systems.xml` |
| `ROOT_BUSINESS_FILE` | `classpath:mix/orm-config.xml` | `classpath:mix/business/order-business.xml` |
| `SYSTEM_RULE_FILE` | `classpath:mix/system/systems.xml` | `classpath:mix/rule/user-rule.xml` |
| `SYSTEM_RULE_FILE` | `classpath:mix/system/systems.xml` | `classpath:mix/rule/order-rule.xml` |
| `SYSTEM_RULE_FILE` | `classpath:mix/system/systems.xml` | `classpath:mix/rule/payment-rule.xml` |

目录展开出的 3 个 Data 与 1 个 View 只进入 SourceManifest，不新增声明边。主资源和测试镜像分别做字节一致与可解析验证，不合并为 20 节点图。

## 4. Case 总览

| Case | 分组 | 层级 | TR | AC | Diagnostic |
|---|---|---|---|---|---|
| `CASE-P1-TD-SOURCE-MANIFEST-001` | source | contract | TR-P1-COMPILER-001 | AC-P1-COMPILER-001 | — |
| `CASE-P1-TD-SOURCE-ORDER-001` | source | boundary | TR-P1-COMPILER-001, TR-P1-COMPILER-005 | AC-P1-COMPILER-001, AC-P1-COMPILER-005 | MIX-DIGEST-NONDETERMINISTIC |
| `CASE-P1-TD-SOURCE-POLICY-001` | source | negative | TR-P1-COMPILER-001 | AC-P1-COMPILER-001 | MIX-SOURCE-POLICY |
| `CASE-P1-TD-SOURCE-NOT-FOUND-001` | source | negative | TR-P1-COMPILER-001 | AC-P1-COMPILER-001 | MIX-SOURCE-NOT-FOUND |
| `CASE-P1-TD-SOURCE-SECURITY-001` | source | security | TR-P1-COMPILER-001 | AC-P1-COMPILER-001 | MIX-SOURCE-PATH-ESCAPE |
| `CASE-P1-TD-SOURCE-DUPLICATE-001` | source | negative | TR-P1-COMPILER-001 | AC-P1-COMPILER-001 | MIX-SOURCE-DUPLICATE-ID |
| `CASE-P1-TD-FRONTEND-XML-001` | frontend | security | TR-P1-COMPILER-002 | AC-P1-COMPILER-002 | MIX-FRONTEND-XML-UNSAFE |
| `CASE-P1-TD-FRONTEND-YAML-001` | frontend | security | TR-P1-COMPILER-002 | AC-P1-COMPILER-002 | MIX-FRONTEND-YAML-UNSAFE |
| `CASE-P1-TD-CANONICAL-PARITY-001` | frontend | contract | TR-P1-COMPILER-002, TR-P1-COMPILER-005 | AC-P1-COMPILER-002, AC-P1-COMPILER-005 | — |
| `CASE-P1-TD-STRUCTURE-UNKNOWN-001` | raw | negative | TR-P1-COMPILER-002 | AC-P1-COMPILER-002 | MIX-STRUCTURE-UNKNOWN |
| `CASE-P1-TD-RAW-INVENTORY-001` | raw | contract | TR-P1-COMPILER-002 | AC-P1-COMPILER-002 | — |
| `CASE-P1-TD-SYMBOL-DUPLICATE-001` | symbol | negative | TR-P1-COMPILER-003 | AC-P1-COMPILER-003 | MIX-SYMBOL-DUPLICATE |
| `CASE-P1-TD-REFERENCE-001` | symbol | contract | TR-P1-COMPILER-003 | AC-P1-COMPILER-003 | MIX-REF-UNKNOWN |
| `CASE-P1-TD-RULE-SYSTEM-001` | symbol | negative | TR-P1-COMPILER-003, TR-P1-COMPILER-008 | AC-P1-COMPILER-003, AC-P1-COMPILER-008 | MIX-REF-RULE-SYSTEM-MISMATCH |
| `CASE-P1-TD-INFORMATION-OWNER-001` | information | negative | TR-P1-COMPILER-003, TR-P1-COMPILER-008 | AC-P1-COMPILER-003, AC-P1-COMPILER-008 | MIX-INFORMATION-OWNER |
| `CASE-P1-TD-COMMON-SUCCESS-001` | information | contract | TR-P1-COMPILER-004, TR-P1-COMPILER-008 | AC-P1-COMPILER-004, AC-P1-COMPILER-008 | — |
| `CASE-P1-TD-INFORMATION-CROSS-SYSTEM-001` | information | negative | TR-P1-COMPILER-008 | AC-P1-COMPILER-008 | MIX-INFORMATION-CROSS-SYSTEM |
| `CASE-P1-TD-COMMON-MEMBER-001` | information | negative | TR-P1-COMPILER-008 | AC-P1-COMPILER-008 | MIX-COMMON-MEMBER |
| `CASE-P1-TD-COMMON-QUALIFIED-001` | information | negative | TR-P1-COMPILER-008 | AC-P1-COMPILER-008 | MIX-COMMON-UNQUALIFIED |
| `CASE-P1-TD-VIEW-BOUNDARY-001` | model_access | negative | TR-P1-COMPILER-008, TR-P1-COMPILER-009 | AC-P1-COMPILER-008, AC-P1-COMPILER-009 | MIX-REF-VIEW-NOT-DECLARED |
| `CASE-P1-TD-MODEL-ACCESS-TARGET-MAIN-001` | model_access | contract | TR-P1-COMPILER-008, TR-P1-COMPILER-009 | AC-P1-COMPILER-008, AC-P1-COMPILER-009 | — |
| `CASE-P1-TD-MODEL-ACCESS-PATH-001` | model_access | contract | TR-P1-COMPILER-009 | AC-P1-COMPILER-009 | — |
| `CASE-P1-TD-MODEL-ACCESS-NOT-FOUND-001` | model_access | negative | TR-P1-COMPILER-009 | AC-P1-COMPILER-009 | MIX-MODEL-ACCESS-NOT-FOUND |
| `CASE-P1-TD-MODEL-ACCESS-AMBIGUOUS-001` | model_access | negative | TR-P1-COMPILER-009 | AC-P1-COMPILER-009 | MIX-MODEL-ACCESS-AMBIGUOUS |
| `CASE-P1-TD-MODEL-ACCESS-NON-COMPOSITE-001` | model_access | negative | TR-P1-COMPILER-009 | AC-P1-COMPILER-009 | MIX-MODEL-ACCESS-NON-COMPOSITE |
| `CASE-P1-TD-DEFERRED-COMPLETE-001` | deferred | contract | TR-P1-COMPILER-004 | AC-P1-COMPILER-004 | MIX-DEFERRED-INCOMPLETE |
| `CASE-P1-TD-DEFERRED-NO-RUNTIME-001` | deferred | boundary | TR-P1-COMPILER-004, TR-P1-COMPILER-008 | AC-P1-COMPILER-004, AC-P1-COMPILER-008 | — |
| `CASE-P1-TD-PUBLISH-SUCCESS-001` | publication | contract | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | — |
| `CASE-P1-TD-PUBLISH-BLOCKED-001` | publication | negative | TR-P1-COMPILER-003, TR-P1-COMPILER-005 | AC-P1-COMPILER-003, AC-P1-COMPILER-005 | MIX-PUBLICATION-BLOCKED |
| `CASE-P1-TD-PUBLISH-TIMEOUT-001` | publication | failure | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | MIX-COMPILATION-TIMED-OUT |
| `CASE-P1-TD-PUBLISH-CANCEL-001` | publication | failure | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | MIX-COMPILATION-CANCELLED |
| `CASE-P1-TD-CONTEXT-CONSTRUCTION-001` | publication | failure | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | MIX-CONTEXT-CONSTRUCTION-FAILED |
| `CASE-P1-TD-PUBLISH-CONFLICT-001` | publication | concurrency | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | MIX-PUBLICATION-CONFLICT |
| `CASE-P1-TD-PUBLISH-FAILURE-001` | publication | failure | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | MIX-PUBLICATION-FAILURE |
| `CASE-P1-TD-DIGEST-001` | digest | determinism | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | MIX-DIGEST-NONDETERMINISTIC |
| `CASE-P1-TD-CONTEXT-ISOLATION-001` | context | concurrency | TR-P1-COMPILER-005, TR-P1-COMPILER-006 | AC-P1-COMPILER-005, AC-P1-COMPILER-006 | MIX-CONTEXT-MUTATION |
| `CASE-P1-TD-PROJECTION-001` | context | compatibility | TR-P1-COMPILER-006 | AC-P1-COMPILER-006 | MIX-PROJECTION-WRITE |
| `CASE-P1-TD-DIAGNOSTIC-CATALOG-001` | diagnostic | contract | TR-P1-COMPILER-001, TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-004, TR-P1-COMPILER-005, TR-P1-COMPILER-006, TR-P1-COMPILER-007, TR-P1-COMPILER-008, TR-P1-COMPILER-009 | AC-P1-COMPILER-001, AC-P1-COMPILER-002, AC-P1-COMPILER-003, AC-P1-COMPILER-004, AC-P1-COMPILER-005, AC-P1-COMPILER-006, AC-P1-COMPILER-007, AC-P1-COMPILER-008, AC-P1-COMPILER-009 | — |
| `CASE-P1-TD-OBSERVER-TIMING-001` | diagnostic | observability | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | MIX-OBSERVER-FAILURE |
| `CASE-P1-TD-RETIREMENT-001` | architecture | architecture | TR-P1-COMPILER-007 | AC-P1-COMPILER-007 | MIX-RETIREMENT-RESIDUE |
| `CASE-P1-TD-JAVA8-MODULE-001` | architecture | architecture | TR-P1-COMPILER-005, TR-P1-COMPILER-007 | AC-P1-COMPILER-005, AC-P1-COMPILER-007 | — |

## 5. Case 详细合同

### 5.1 `CASE-P1-TD-SOURCE-MANIFEST-001`

- **追踪**：TR-P1-COMPILER-001；AC-P1-COMPILER-001
- **层级/分组**：`contract` / `source`
- **Fixture**：classpath:mix/orm-config.xml 与主/测试镜像 mix
- **稳定接缝**：InMemoryDocumentSourceProvider + MixSourceResolver
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=MixSourceResolverContractTest#resolvesExactManifestAndEdges test`
- **主要 oracle**：SourceManifest 恰好包含 10 个固定 sourceId；声明边恰好 7 条且 edgeType/from/target/SourceRef 精确匹配；inventory 为 5 Data、2 View、4 System、14 RuleView、16 Information、1 Scope、5 Directory、8 Action、4 Produce。
- **禁止副作用**：不得以 visited>=10、实现输出反推 expected、把主资源与测试镜像合并为 20 节点。
- **有效 RED 合同**：测试骨架可编译后，因 resolver/manifest/typed edge 尚未实现而断言失败；模块不存在、类加载失败或 fixture 缺失不算有效 RED。

### 5.2 `CASE-P1-TD-SOURCE-ORDER-001`

- **追踪**：TR-P1-COMPILER-001, TR-P1-COMPILER-005；AC-P1-COMPILER-001, AC-P1-COMPILER-005
- **层级/分组**：`boundary` / `source`
- **Fixture**：同一文件集的正序、逆序、随机种子 3 组枚举
- **稳定接缝**：DeterministicFileSetProvider + SemanticDigestSnapshot
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=MixSourceResolverDeterminismTest test`
- **主要 oracle**：sourceId、边集合、Diagnostic 顺序和 semanticDigest 完全一致；sourceDigest 可因原文字节变化而独立变化。
- **Diagnostic**：`MIX-DIGEST-NONDETERMINISTIC`
- **禁止副作用**：不得依赖文件系统枚举顺序、线程调度或 Map 迭代顺序。
- **有效 RED 合同**：稳定排序缺失时同一语义输入产生不同快照，形成行为型 RED。

### 5.3 `CASE-P1-TD-SOURCE-POLICY-001`

- **追踪**：TR-P1-COMPILER-001；AC-P1-COMPILER-001
- **层级/分组**：`negative` / `source`
- **Fixture**：null provider、抛异常 provider、RESOLVED 空结果、resolve 返回多项、resolveFileSet 返回零项
- **稳定接缝**：InMemoryDocumentSourceProvider
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=SourceProviderPolicyTest test`
- **主要 oracle**：统一返回 FAILED Diagnostic=MIX-SOURCE-POLICY，SourceRef 指向声明位置。
- **Diagnostic**：`MIX-SOURCE-POLICY`
- **禁止副作用**：不得硬编码 dec-demo 路径、吞异常或继续构建 RawDefinitionSet。
- **有效 RED 合同**：provider typed-result 不变量尚未实现时断言失败。

### 5.4 `CASE-P1-TD-SOURCE-NOT-FOUND-001`

- **追踪**：TR-P1-COMPILER-001；AC-P1-COMPILER-001
- **层级/分组**：`negative` / `source`
- **Fixture**：删除一个显式 rule/system/business source
- **稳定接缝**：InMemoryDocumentSourceProvider
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=SourceNotFoundTest test`
- **主要 oracle**：产生 MIX-SOURCE-NOT-FOUND；CompilationSession FAILED；Publisher 调用 0 次。
- **Diagnostic**：`MIX-SOURCE-NOT-FOUND`
- **禁止副作用**：不得忽略缺失源或回退到 classpath 其它同名文件。
- **有效 RED 合同**：缺失源仍被静默忽略或错误 code 不稳定时失败。

### 5.5 `CASE-P1-TD-SOURCE-SECURITY-001`

- **追踪**：TR-P1-COMPILER-001；AC-P1-COMPILER-001
- **层级/分组**：`security` / `source`
- **Fixture**：../、百分号编码逃逸、symlink、file/http 未授权 scheme
- **稳定接缝**：SourceSecurityPolicy harness
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=SourceSecurityPolicyTest test`
- **主要 oracle**：访问前拒绝并产生 MIX-SOURCE-PATH-ESCAPE；网络/根外读取计数为 0。
- **Diagnostic**：`MIX-SOURCE-PATH-ESCAPE`
- **禁止副作用**：不得规范化后继续访问，也不得只记录 WARN。
- **有效 RED 合同**：安全策略未实现时实际读取探针被调用，形成 RED。

### 5.6 `CASE-P1-TD-SOURCE-DUPLICATE-001`

- **追踪**：TR-P1-COMPILER-001；AC-P1-COMPILER-001
- **层级/分组**：`negative` / `source`
- **Fixture**：两个 URI 规范化为相同 sourceId，内容相同和不同各一组
- **稳定接缝**：MixSourceGraph builder
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=DuplicateSourceIdTest test`
- **主要 oracle**：两组均以 MIX-SOURCE-DUPLICATE-ID 阻断，不覆盖已有 Source。
- **Diagnostic**：`MIX-SOURCE-DUPLICATE-ID`
- **禁止副作用**：不得按最后写入覆盖或按内容相同自动合并。
- **有效 RED 合同**：图允许重复 key 时断言失败。

### 5.7 `CASE-P1-TD-FRONTEND-XML-001`

- **追踪**：TR-P1-COMPILER-002；AC-P1-COMPILER-002
- **层级/分组**：`security` / `frontend`
- **Fixture**：DOCTYPE、外部实体、网络 schema、递归实体
- **稳定接缝**：SecureXmlFrontendHarness
- **执行命令**：`./mvnw -pl dec-xml-frontend -am -Dtest=SecureXmlFrontendContractTest test`
- **主要 oracle**：解析前/解析中拒绝，产生 MIX-FRONTEND-XML-UNSAFE；外部访问计数 0。
- **Diagnostic**：`MIX-FRONTEND-XML-UNSAFE`
- **禁止副作用**：不得解析后再清洗，不得泄露本地文件内容。
- **有效 RED 合同**：不安全解析器会触发访问探针或未产生稳定 Diagnostic。

### 5.8 `CASE-P1-TD-FRONTEND-YAML-001`

- **追踪**：TR-P1-COMPILER-002；AC-P1-COMPILER-002
- **层级/分组**：`security` / `frontend`
- **Fixture**：任意 Java tag、别名炸弹、节点数/深度超限
- **稳定接缝**：SafeYamlFrontendHarness
- **执行命令**：`./mvnw -pl dec-yaml-frontend -am -Dtest=SafeYamlFrontendContractTest test`
- **主要 oracle**：产生 MIX-FRONTEND-YAML-UNSAFE；不实例化任意类型。
- **Diagnostic**：`MIX-FRONTEND-YAML-UNSAFE`
- **禁止副作用**：不得使用通用对象构造器或忽略超限。
- **有效 RED 合同**：类型构造或资源限制未生效时失败。

### 5.9 `CASE-P1-TD-CANONICAL-PARITY-001`

- **追踪**：TR-P1-COMPILER-002, TR-P1-COMPILER-005；AC-P1-COMPILER-002, AC-P1-COMPILER-005
- **层级/分组**：`contract` / `frontend`
- **Fixture**：最小同义 XML/YAML + 格式/注释/属性顺序变体
- **稳定接缝**：SecureXmlFrontendHarness + SafeYamlFrontendHarness + Canonical snapshot
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=CanonicalParityContractTest test`
- **主要 oracle**：CanonicalDocumentNode 和 semanticDigest 等价；SourceRef 保留各自来源；Canonical 不持有 DOM/YAML Node。
- **禁止副作用**：不得比较 parser 私有对象或原文字节摘要代替语义摘要。
- **有效 RED 合同**：格式中立结构缺失或前端泄漏实现对象时失败。

### 5.10 `CASE-P1-TD-STRUCTURE-UNKNOWN-001`

- **追踪**：TR-P1-COMPILER-002；AC-P1-COMPILER-002
- **层级/分组**：`negative` / `raw`
- **Fixture**：各定义层级插入未知节点/属性
- **稳定接缝**：PassHarness(RawDefinitionBuilder)
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=UnknownStructureDiagnosticTest test`
- **主要 oracle**：所有 P1 模式均产生 ERROR MIX-STRUCTURE-UNKNOWN，未知节点 SourceRef 精确。
- **Diagnostic**：`MIX-STRUCTURE-UNKNOWN`
- **禁止副作用**：不得 lenient 静默发布。
- **有效 RED 合同**：未知节点被忽略时失败。

### 5.11 `CASE-P1-TD-RAW-INVENTORY-001`

- **追踪**：TR-P1-COMPILER-002；AC-P1-COMPILER-002
- **层级/分组**：`contract` / `raw`
- **Fixture**：真实 mix 10 source
- **稳定接缝**：PassHarness(RawDefinitionBuilder)
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=RawDefinitionInventoryTest test`
- **主要 oracle**：RawDefinitionSet 数量与类型精确匹配固定 inventory，全部对象有 SourceRef。
- **禁止副作用**：不得从运行时 Registry 反推 Raw 数量，不得提前执行 P2-P7。
- **有效 RED 合同**：Raw 类型覆盖不完整时失败。

### 5.12 `CASE-P1-TD-SYMBOL-DUPLICATE-001`

- **追踪**：TR-P1-COMPILER-003；AC-P1-COMPILER-003
- **层级/分组**：`negative` / `symbol`
- **Fixture**：同类型同命名空间重复；异类型同名对照组
- **稳定接缝**：SymbolTableFixtureBuilder
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=TypedSymbolTableTest#rejectsDuplicateTypedKey test`
- **主要 oracle**：同 TypedKey 产生 MIX-SYMBOL-DUPLICATE；异类型同名可共存。
- **Diagnostic**：`MIX-SYMBOL-DUPLICATE`
- **禁止副作用**：不得覆盖首定义或使用全局字符串命名空间。
- **有效 RED 合同**：重复覆盖或类型隔离缺失时失败。

### 5.13 `CASE-P1-TD-REFERENCE-001`

- **追踪**：TR-P1-COMPILER-003；AC-P1-COMPILER-003
- **层级/分组**：`contract` / `symbol`
- **Fixture**：合法跨文件前向引用与未知 key
- **稳定接缝**：SymbolTableFixtureBuilder + PassHarness(ReferenceResolution)
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=ForwardReferenceResolutionTest test`
- **主要 oracle**：全部注册后合法前向引用成功；未知 key 产生 MIX-REF-UNKNOWN。
- **Diagnostic**：`MIX-REF-UNKNOWN`
- **禁止副作用**：不得按发现顺序解析或猜测同名目标。
- **有效 RED 合同**：单遍顺序依赖或未知引用被忽略时失败。

### 5.14 `CASE-P1-TD-RULE-SYSTEM-001`

- **追踪**：TR-P1-COMPILER-003, TR-P1-COMPILER-008；AC-P1-COMPILER-003, AC-P1-COMPILER-008
- **层级/分组**：`negative` / `symbol`
- **Fixture**：来源 System、RuleView.system、view-ref owner 三者任一冲突
- **稳定接缝**：PassHarness(ReferenceResolution)
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=RuleViewOwnershipTest test`
- **主要 oracle**：每个冲突均产生 MIX-REF-RULE-SYSTEM-MISMATCH。
- **Diagnostic**：`MIX-REF-RULE-SYSTEM-MISMATCH`
- **禁止副作用**：不得以文件路径或名称猜测 owner。
- **有效 RED 合同**：归属不一致仍解析成功时失败。

### 5.15 `CASE-P1-TD-INFORMATION-OWNER-001`

- **追踪**：TR-P1-COMPILER-003, TR-P1-COMPILER-008；AC-P1-COMPILER-003, AC-P1-COMPILER-008
- **层级/分组**：`negative` / `information`
- **Fixture**：Information 位于 BusinessScope/System 外或错误 namespace
- **稳定接缝**：InformationExpressionFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=InformationOwnershipPolicyTest test`
- **主要 oracle**：产生 MIX-INFORMATION-OWNER；BusinessScope Information 数为 0。
- **Diagnostic**：`MIX-INFORMATION-OWNER`
- **禁止副作用**：不得自动迁移到任意 System。
- **有效 RED 合同**：非法 owner 仍注册 InformationKey 时失败。

### 5.16 `CASE-P1-TD-COMMON-SUCCESS-001`

- **追踪**：TR-P1-COMPILER-004, TR-P1-COMPILER-008；AC-P1-COMPILER-004, AC-P1-COMPILER-008
- **层级/分组**：`contract` / `information`
- **Fixture**：common.paySuccess/common.payError
- **稳定接缝**：InformationExpressionFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=CommonInformationExpressionTest#resolvesQualifiedDependencies test`
- **主要 oracle**：仅解析 system-qualified InformationKey，登记 P3 Deferred；common 无 Data/View/RuleView/ModelAccess。
- **禁止副作用**：不得求值表达式或在 P1 检测 DAG 循环。
- **有效 RED 合同**：依赖 key/Deferred 不完整时失败。

### 5.17 `CASE-P1-TD-INFORMATION-CROSS-SYSTEM-001`

- **追踪**：TR-P1-COMPILER-008；AC-P1-COMPILER-008
- **层级/分组**：`negative` / `information`
- **Fixture**：order System expression 引用 payment.error
- **稳定接缝**：InformationExpressionFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=InformationOwnershipPolicyTest#rejectsCrossSystemExpression test`
- **主要 oracle**：产生 MIX-INFORMATION-CROSS-SYSTEM。
- **Diagnostic**：`MIX-INFORMATION-CROSS-SYSTEM`
- **禁止副作用**：不得自动迁移到 common 或降级为 Deferred 成功。
- **有效 RED 合同**：普通 System 可组合跨 System key 时失败。

### 5.18 `CASE-P1-TD-COMMON-MEMBER-001`

- **追踪**：TR-P1-COMPILER-008；AC-P1-COMPILER-008
- **层级/分组**：`negative` / `information`
- **Fixture**：common 声明 Data/View/RuleView/ModelAccess 或非 expression Information
- **稳定接缝**：InformationExpressionFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=CommonSystemBoundaryTest test`
- **主要 oracle**：逐类产生 MIX-COMMON-MEMBER。
- **Diagnostic**：`MIX-COMMON-MEMBER`
- **禁止副作用**：不得只忽略非法成员。
- **有效 RED 合同**：common 边界未校验时失败。

### 5.19 `CASE-P1-TD-COMMON-QUALIFIED-001`

- **追踪**：TR-P1-COMPILER-008；AC-P1-COMPILER-008
- **层级/分组**：`negative` / `information`
- **Fixture**：裸 localName、未知 system、未知 localName
- **稳定接缝**：InformationExpressionFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=CommonInformationExpressionTest#rejectsUnqualifiedReferences test`
- **主要 oracle**：产生 MIX-COMMON-UNQUALIFIED，relatedRefs 指向问题 token。
- **Diagnostic**：`MIX-COMMON-UNQUALIFIED`
- **禁止副作用**：不得在所有 System 全局搜索同名 Information。
- **有效 RED 合同**：未限定引用被解析时失败。

### 5.20 `CASE-P1-TD-VIEW-BOUNDARY-001`

- **追踪**：TR-P1-COMPILER-008, TR-P1-COMPILER-009；AC-P1-COMPILER-008, AC-P1-COMPILER-009
- **层级/分组**：`negative` / `model_access`
- **Fixture**：Information/ModelAccess ref@view 未在当前 System.view-info
- **稳定接缝**：ModelAccessFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=SystemViewBoundaryTest test`
- **主要 oracle**：产生 MIX-REF-VIEW-NOT-DECLARED。
- **Diagnostic**：`MIX-REF-VIEW-NOT-DECLARED`
- **禁止副作用**：不得跨 System/View 搜索。
- **有效 RED 合同**：目标 View 通过全局名称找到时失败。

### 5.21 `CASE-P1-TD-MODEL-ACCESS-TARGET-MAIN-001`

- **追踪**：TR-P1-COMPILER-008, TR-P1-COMPILER-009；AC-P1-COMPILER-008, AC-P1-COMPILER-009
- **层级/分组**：`contract` / `model_access`
- **Fixture**：OrderInfo.user -> UserInfo，target-main=user，同时存在同名 property path
- **稳定接缝**：ModelAccessFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorPolicyTest#targetMainWins test`
- **主要 oracle**：selector 首先且仅命中 target-main 根目标，sourcePath 仍为 user。
- **禁止副作用**：不得优先 property path、root-property、模糊或跨 View 匹配。
- **有效 RED 合同**：优先级错误时 resolvedTarget 快照不同。

### 5.22 `CASE-P1-TD-MODEL-ACCESS-PATH-001`

- **追踪**：TR-P1-COMPILER-009；AC-P1-COMPILER-009
- **层级/分组**：`contract` / `model_access`
- **Fixture**：target-main 不匹配，唯一嵌套 property path
- **稳定接缝**：ModelAccessFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorPolicyTest#fallsBackToExactPropertyPath test`
- **主要 oracle**：区分大小写逐段命中唯一目标。
- **禁止副作用**：不得模糊、大小写折叠或全局搜索。
- **有效 RED 合同**：fallback 未实现或非精确匹配时失败。

### 5.23 `CASE-P1-TD-MODEL-ACCESS-NOT-FOUND-001`

- **追踪**：TR-P1-COMPILER-009；AC-P1-COMPILER-009
- **层级/分组**：`negative` / `model_access`
- **Fixture**：target-main 与 property path 均未命中/大小写不同/缺段
- **稳定接缝**：ModelAccessFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorPolicyTest#rejectsMissingSelector test`
- **主要 oracle**：产生 MIX-MODEL-ACCESS-NOT-FOUND。
- **Diagnostic**：`MIX-MODEL-ACCESS-NOT-FOUND`
- **禁止副作用**：不得猜测最相近名称或回退根属性。
- **有效 RED 合同**：错误匹配成功或 code 不稳定时失败。

### 5.24 `CASE-P1-TD-MODEL-ACCESS-AMBIGUOUS-001`

- **追踪**：TR-P1-COMPILER-009；AC-P1-COMPILER-009
- **层级/分组**：`negative` / `model_access`
- **Fixture**：重复 ref、多候选、重叠 write
- **稳定接缝**：ModelAccessFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorPolicyTest#rejectsAmbiguity test`
- **主要 oracle**：逐种产生 MIX-MODEL-ACCESS-AMBIGUOUS，零绑定发布。
- **Diagnostic**：`MIX-MODEL-ACCESS-AMBIGUOUS`
- **禁止副作用**：不得按首个候选获胜。
- **有效 RED 合同**：冲突映射被接受时失败。

### 5.25 `CASE-P1-TD-MODEL-ACCESS-NON-COMPOSITE-001`

- **追踪**：TR-P1-COMPILER-009；AC-P1-COMPILER-009
- **层级/分组**：`negative` / `model_access`
- **Fixture**：property path 中间段为 scalar
- **稳定接缝**：ModelAccessFixture
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorPolicyTest#rejectsNonCompositeIntermediate test`
- **主要 oracle**：产生 MIX-MODEL-ACCESS-NON-COMPOSITE。
- **Diagnostic**：`MIX-MODEL-ACCESS-NON-COMPOSITE`
- **禁止副作用**：不得截断路径或将 scalar 当对象。
- **有效 RED 合同**：中间段类型未验证时失败。

### 5.26 `CASE-P1-TD-DEFERRED-COMPLETE-001`

- **追踪**：TR-P1-COMPILER-004；AC-P1-COMPILER-004
- **层级/分组**：`contract` / `deferred`
- **Fixture**：P2-P8 各一 Deferred；分别缺 requiredStage/reason/SourceRef/resolvedKeys
- **稳定接缝**：PassHarness(DeferredClassification)
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=DeferredDefinitionCompletenessTest test`
- **主要 oracle**：完整项冻结成功；任一字段缺失产生 MIX-DEFERRED-INCOMPLETE。
- **Diagnostic**：`MIX-DEFERRED-INCOMPLETE`
- **禁止副作用**：不得静默忽略或以 null 占位发布。
- **有效 RED 合同**：不完整 Deferred 可进入 Registry 时失败。

### 5.27 `CASE-P1-TD-DEFERRED-NO-RUNTIME-001`

- **追踪**：TR-P1-COMPILER-004, TR-P1-COMPILER-008；AC-P1-COMPILER-004, AC-P1-COMPILER-008
- **层级/分组**：`boundary` / `deferred`
- **Fixture**：注入 evaluator/executor/query/transaction fail-fast spies
- **稳定接缝**：PassHarness + fail-fast runtime spies
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=P1RuntimeBoundaryTest test`
- **主要 oracle**：Information evaluator、Action/Directory executor、Query planner、Transaction manager 调用均为 0。
- **禁止副作用**：不得提前执行 P2-P7。
- **有效 RED 合同**：任一运行服务被调用即失败。

### 5.28 `CASE-P1-TD-PUBLISH-SUCCESS-001`

- **追踪**：TR-P1-COMPILER-005；AC-P1-COMPILER-005
- **层级/分组**：`contract` / `publication`
- **Fixture**：无 ERROR 的完整 Session + expectedCurrent
- **稳定接缝**：ContextPublisherSpy
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=AtomicPublicationContractTest#publishesExactlyOnce test`
- **主要 oracle**：同一 compileAndPublish 调用内 Publisher 恰好 1 次，返回 PUBLISHED 与完整不可变 Context；返回后不再调用。
- **禁止副作用**：不得由 starter 二次发布或先暴露未发布模型。
- **有效 RED 合同**：发布缺失/重复/延迟时失败。

### 5.29 `CASE-P1-TD-PUBLISH-BLOCKED-001`

- **追踪**：TR-P1-COMPILER-003, TR-P1-COMPILER-005；AC-P1-COMPILER-003, AC-P1-COMPILER-005
- **层级/分组**：`negative` / `publication`
- **Fixture**：任一语义 ERROR 或 Registry 冻结失败
- **稳定接缝**：ContextPublisherSpy
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=AtomicPublicationContractTest#blocksOnError test`
- **主要 oracle**：FAILED/MIX-PUBLICATION-BLOCKED；model/context/digest 不可取得；Publisher 0 次；旧 Context digest 不变。
- **Diagnostic**：`MIX-PUBLICATION-BLOCKED`
- **禁止副作用**：不得部分发布或返回 success-with-errors。
- **有效 RED 合同**：ERROR 后 Publisher 被调用或结果暴露模型时失败。

### 5.30 `CASE-P1-TD-PUBLISH-TIMEOUT-001`

- **追踪**：TR-P1-COMPILER-005；AC-P1-COMPILER-005
- **层级/分组**：`failure` / `publication`
- **Fixture**：MonotonicClockStub 在指定 pass 越过 Deadline
- **稳定接缝**：MonotonicClockStub + ContextPublisherSpy
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=CompilationDeadlineTest test`
- **主要 oracle**：FAILED/MIX-COMPILATION-TIMED-OUT；Publisher 0 次；状态机结束 FAILED。
- **Diagnostic**：`MIX-COMPILATION-TIMED-OUT`
- **禁止副作用**：不得使用墙钟 sleep 或继续后续 pass。
- **有效 RED 合同**：deadline 未检查或 code 不稳定时失败。

### 5.31 `CASE-P1-TD-PUBLISH-CANCEL-001`

- **追踪**：TR-P1-COMPILER-005；AC-P1-COMPILER-005
- **层级/分组**：`failure` / `publication`
- **Fixture**：CancellationTokenStub 在各 pass 边界取消
- **稳定接缝**：CancellationTokenStub + ContextPublisherSpy
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=CompilationCancellationTest test`
- **主要 oracle**：FAILED/MIX-COMPILATION-CANCELLED；Publisher 0 次；旧 Context 保持。
- **Diagnostic**：`MIX-COMPILATION-CANCELLED`
- **禁止副作用**：不得把 cancel 当异常重试或部分结果。
- **有效 RED 合同**：取消后仍执行/发布时失败。

### 5.32 `CASE-P1-TD-CONTEXT-CONSTRUCTION-001`

- **追踪**：TR-P1-COMPILER-005；AC-P1-COMPILER-005
- **层级/分组**：`failure` / `publication`
- **Fixture**：Context factory 在冻结后抛受控异常
- **稳定接缝**：ContextPublisherSpy + failing Context factory
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=AtomicPublicationContractTest#failsContextConstruction test`
- **主要 oracle**：FAILED/MIX-CONTEXT-CONSTRUCTION-FAILED；Publisher 0 次；无 model/context/digest。
- **Diagnostic**：`MIX-CONTEXT-CONSTRUCTION-FAILED`
- **禁止副作用**：不得发布半构造 Context。
- **有效 RED 合同**：异常泄漏或 Publisher 被调用时失败。

### 5.33 `CASE-P1-TD-PUBLISH-CONFLICT-001`

- **追踪**：TR-P1-COMPILER-005；AC-P1-COMPILER-005
- **层级/分组**：`concurrency` / `publication`
- **Fixture**：Publisher 返回 CONFLICT，expectedCurrent 已被其它线程替换
- **稳定接缝**：ContextPublisherSpy
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=AtomicPublicationContractTest#mapsCasConflict test`
- **主要 oracle**：FAILED/MIX-PUBLICATION-CONFLICT；Publisher 1 次；现有新 Context 不被覆盖。
- **Diagnostic**：`MIX-PUBLICATION-CONFLICT`
- **禁止副作用**：不得自动重试无上限或强制覆盖。
- **有效 RED 合同**：冲突被视为 PUBLISHED 时失败。

### 5.34 `CASE-P1-TD-PUBLISH-FAILURE-001`

- **追踪**：TR-P1-COMPILER-005；AC-P1-COMPILER-005
- **层级/分组**：`failure` / `publication`
- **Fixture**：Publisher 返回 null 或抛异常
- **稳定接缝**：ContextPublisherSpy
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=AtomicPublicationContractTest#mapsPublisherFailure test`
- **主要 oracle**：FAILED/MIX-PUBLICATION-FAILURE；Publisher 1 次；旧 Context 保持。
- **Diagnostic**：`MIX-PUBLICATION-FAILURE`
- **禁止副作用**：不得传播未分类异常或假定发布成功。
- **有效 RED 合同**：null/异常未统一映射时失败。

### 5.35 `CASE-P1-TD-DIGEST-001`

- **追踪**：TR-P1-COMPILER-005；AC-P1-COMPILER-005
- **层级/分组**：`determinism` / `digest`
- **Fixture**：同义 XML/YAML、注释/格式变化、不同线程调度；非同义变体对照
- **稳定接缝**：SemanticDigestSnapshot
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=SemanticDigestContractTest test`
- **主要 oracle**：同义输入 semanticDigest 相同，非同义不同；sourceDigest 反映原文；digest 输入不含 DigestPair 自身。
- **Diagnostic**：`MIX-DIGEST-NONDETERMINISTIC`
- **禁止副作用**：不得使用不稳定对象 hashCode 或自引用摘要。
- **有效 RED 合同**：重跑/并发结果不一致时失败并映射 MIX-DIGEST-NONDETERMINISTIC。

### 5.36 `CASE-P1-TD-CONTEXT-ISOLATION-001`

- **追踪**：TR-P1-COMPILER-005, TR-P1-COMPILER-006；AC-P1-COMPILER-005, AC-P1-COMPILER-006
- **层级/分组**：`concurrency` / `context`
- **Fixture**：两组配置并发编译、并行读取、修改尝试、global current 探针
- **稳定接缝**：ContextPublisherSpy + immutable collection probes
- **执行命令**：`./mvnw -pl dec-core-context -am -Dtest=EngineContextIsolationTest test`
- **主要 oracle**：Registry/Diagnostic/digest 无交叉污染；修改拒绝并产生/映射 MIX-CONTEXT-MUTATION；无 static mutable current。
- **Diagnostic**：`MIX-CONTEXT-MUTATION`
- **禁止副作用**：不得共享 builder、Registry 或可变集合。
- **有效 RED 合同**：任一 Context 可观察到另一 Session 数据或可写时失败。

### 5.37 `CASE-P1-TD-PROJECTION-001`

- **追踪**：TR-P1-COMPILER-006；AC-P1-COMPILER-006
- **层级/分组**：`compatibility` / `context`
- **Fixture**：从同一 Context 读取 Data/View/Rule；注册/修改/删除尝试
- **稳定接缝**：CoreConfigProjection public API
- **执行命令**：`./mvnw -pl dec-core-context -am -Dtest=CoreConfigProjectionContractTest test`
- **主要 oracle**：读取与 CompiledModelSet 一致；写操作产生 MIX-PROJECTION-WRITE；不存在第二 Registry。
- **Diagnostic**：`MIX-PROJECTION-WRITE`
- **禁止副作用**：不得双写、缓存独立事实或提供 mutation API。
- **有效 RED 合同**：投影可写或与源 Registry 不一致时失败。

### 5.38 `CASE-P1-TD-DIAGNOSTIC-CATALOG-001`

- **追踪**：TR-P1-COMPILER-001, TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-004, TR-P1-COMPILER-005, TR-P1-COMPILER-006, TR-P1-COMPILER-007, TR-P1-COMPILER-008, TR-P1-COMPILER-009；AC-P1-COMPILER-001, AC-P1-COMPILER-002, AC-P1-COMPILER-003, AC-P1-COMPILER-004, AC-P1-COMPILER-005, AC-P1-COMPILER-006, AC-P1-COMPILER-007, AC-P1-COMPILER-008, AC-P1-COMPILER-009
- **层级/分组**：`contract` / `diagnostic`
- **Fixture**：BM-R05 23 个业务错误各一最小 fixture，多错误乱序输入
- **稳定接缝**：DiagnosticSnapshot
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=DiagnosticCatalogContractTest test`
- **主要 oracle**：23 个稳定 code 均有 code/severity/SourceRef/definitionKey/relatedRefs/pass/recoveryHint；排序为 sourceId,line,column,code,entityKey,pass。
- **禁止副作用**：不得用 message 文案作为契约、缺 key 时不得用 null 影响排序。
- **有效 RED 合同**：目录缺码、字段缺失或重跑排序不同即失败。

### 5.39 `CASE-P1-TD-OBSERVER-TIMING-001`

- **追踪**：TR-P1-COMPILER-005；AC-P1-COMPILER-005
- **层级/分组**：`observability` / `diagnostic`
- **Fixture**：MonotonicClockStub + 正常/抛异常 Observer
- **稳定接缝**：CompilationObserverSpy
- **执行命令**：`./mvnw -pl dec-core-compiler -am -Dtest=CompilationObserverContractTest test`
- **主要 oracle**：discovery/parse/每 pass/digest 各一次非负 elapsedNanos；状态转换完整；Observer 异常仅增加非 ERROR MIX-OBSERVER-FAILURE，不改变原结果/context/digest。
- **Diagnostic**：`MIX-OBSERVER-FAILURE`
- **禁止副作用**：不得使用真实时长阈值或让 Observer 控制业务结果。
- **有效 RED 合同**：观测失败改变状态或摘要时失败。

### 5.40 `CASE-P1-TD-RETIREMENT-001`

- **追踪**：TR-P1-COMPILER-007；AC-P1-COMPILER-007
- **层级/分组**：`architecture` / `architecture`
- **Fixture**：仓库树、根 POM、dependency:tree、ServiceLoader、反射字符串、构建 artifact
- **稳定接缝**：repository scan + Maven dependency output
- **执行命令**：`python3 project_doc/version/V_1.0/task/P1-COMPILER-F01/validation/test_retirement_contract.py`
- **主要 oracle**：dec-expand-declaration、LegacyDeclarationAdapter、复制实现和第二运行时残留数为 0；否则 MIX-RETIREMENT-RESIDUE。
- **Diagnostic**：`MIX-RETIREMENT-RESIDUE`
- **禁止副作用**：不得以排除构建或保留 Adapter 视为退役。
- **有效 RED 合同**：P1 开发前当前残留可作为表征基线；真正 TDD/开发门禁必须在删除任务后转绿。

### 5.41 `CASE-P1-TD-JAVA8-MODULE-001`

- **追踪**：TR-P1-COMPILER-005, TR-P1-COMPILER-007；AC-P1-COMPILER-005, AC-P1-COMPILER-007
- **层级/分组**：`architecture` / `architecture`
- **Fixture**：compiler/context/starter/frontend POM 与生产源码
- **稳定接缝**：Maven compile + rg static scan + dependency:tree
- **执行命令**：`./mvnw -pl dec-core-compiler,dec-core-context,dec-core-starter -am -Dmaven.compiler.source=1.8 -Dmaven.compiler.target=1.8 test`
- **主要 oracle**：Java 8 编译通过；无 record/List.of/Map.of/Optional.isEmpty/var；compiler core 无 DOM4J/SnakeYAML/SQL/MySQL/demo 生产依赖；context 不反向依赖 compiler。
- **禁止副作用**：不得通过提高 source level 或 scope=provided 隐藏依赖。
- **有效 RED 合同**：未来模块建立后，非法 API/依赖由编译或静态扫描稳定失败。

## 6. Diagnostic 目录覆盖

| BM-R05 code | Case |
|---|---|
| `MIX-SOURCE-POLICY` | `CASE-P1-TD-SOURCE-POLICY-001` |
| `MIX-SOURCE-NOT-FOUND` | `CASE-P1-TD-SOURCE-NOT-FOUND-001` |
| `MIX-SOURCE-PATH-ESCAPE` | `CASE-P1-TD-SOURCE-SECURITY-001` |
| `MIX-SOURCE-DUPLICATE-ID` | `CASE-P1-TD-SOURCE-DUPLICATE-001` |
| `MIX-FRONTEND-XML-UNSAFE` | `CASE-P1-TD-FRONTEND-XML-001` |
| `MIX-FRONTEND-YAML-UNSAFE` | `CASE-P1-TD-FRONTEND-YAML-001` |
| `MIX-SYMBOL-DUPLICATE` | `CASE-P1-TD-SYMBOL-DUPLICATE-001` |
| `MIX-REF-UNKNOWN` | `CASE-P1-TD-REFERENCE-001` |
| `MIX-REF-RULE-SYSTEM-MISMATCH` | `CASE-P1-TD-RULE-SYSTEM-001` |
| `MIX-INFORMATION-OWNER` | `CASE-P1-TD-INFORMATION-OWNER-001` |
| `MIX-INFORMATION-CROSS-SYSTEM` | `CASE-P1-TD-INFORMATION-CROSS-SYSTEM-001` |
| `MIX-COMMON-MEMBER` | `CASE-P1-TD-COMMON-MEMBER-001` |
| `MIX-COMMON-UNQUALIFIED` | `CASE-P1-TD-COMMON-QUALIFIED-001` |
| `MIX-REF-VIEW-NOT-DECLARED` | `CASE-P1-TD-VIEW-BOUNDARY-001` |
| `MIX-MODEL-ACCESS-NOT-FOUND` | `CASE-P1-TD-MODEL-ACCESS-NOT-FOUND-001` |
| `MIX-MODEL-ACCESS-AMBIGUOUS` | `CASE-P1-TD-MODEL-ACCESS-AMBIGUOUS-001` |
| `MIX-MODEL-ACCESS-NON-COMPOSITE` | `CASE-P1-TD-MODEL-ACCESS-NON-COMPOSITE-001` |
| `MIX-DEFERRED-INCOMPLETE` | `CASE-P1-TD-DEFERRED-COMPLETE-001` |
| `MIX-PUBLICATION-BLOCKED` | `CASE-P1-TD-PUBLISH-BLOCKED-001` |
| `MIX-DIGEST-NONDETERMINISTIC` | `CASE-P1-TD-SOURCE-ORDER-001`, `CASE-P1-TD-DIGEST-001` |
| `MIX-CONTEXT-MUTATION` | `CASE-P1-TD-CONTEXT-ISOLATION-001` |
| `MIX-PROJECTION-WRITE` | `CASE-P1-TD-PROJECTION-001` |
| `MIX-RETIREMENT-RESIDUE` | `CASE-P1-TD-RETIREMENT-001` |

附加 DESIGN-R05 code：`MIX-STRUCTURE-UNKNOWN`、`MIX-COMPILATION-TIMED-OUT`、`MIX-COMPILATION-CANCELLED`、`MIX-CONTEXT-CONSTRUCTION-FAILED`、`MIX-PUBLICATION-CONFLICT`、`MIX-PUBLICATION-FAILURE`、`MIX-OBSERVER-FAILURE`。

## 7. TDD RED 移交

1. Implementation Plan 必须把 Case 分配到 P1-T01～T15，并在每个开发任务前建立最小公共 seam 与对应测试类；
2. RED 只有在测试类可编译、fixture 可加载、命令环境正常后，因目标行为缺失或错误 oracle 不满足而失败才有效；
3. `module not found`、依赖下载失败、编译语法错误、fixture 路径错误、测试未被发现均为环境/测试实现失败，不能作为 RED Evidence；
4. Mock 仅用于 SourceProvider、Clock、CancellationToken、ContextPublisher、Observer 等外部或不稳定边界；TypedKey、解析策略、Deferred、Diagnostic、Context 不可变性必须由真实实现验证；
5. TDD 阶段保存完整命令、退出码、通过/失败数和关键输出；Development 阶段在同 Case 上完成 GREEN/REFACTOR。

## 8. 证据采集

| 证据 | 要求 |
|---|---|
| test-design validation | 解析本文件机器区块；41 个 Case 唯一；9 TR/9 AC/23 BM code 全覆盖；Source/edge/inventory 精确 |
| static fixture contract | `test_system_information_contract.py` 与现有 `MixContractTest` 只证明当前 fixture，不外推 compiler 已实现 |
| future unit/contract | 每条命令保存 schema v2 command-result、Surefire 报告和当前 revision |
| skipped | 明确子范围、原因和未验证风险；不得表述为通过 |
| failure | 保存最早失败阶段、Diagnostic、旧 Context/禁止副作用状态 |

## 9. 覆盖摘要

| 追踪 | Case 数 |
|---|---:|
| `TR-P1-COMPILER-001` | 7 |
| `TR-P1-COMPILER-002` | 6 |
| `TR-P1-COMPILER-003` | 6 |
| `TR-P1-COMPILER-004` | 4 |
| `TR-P1-COMPILER-005` | 14 |
| `TR-P1-COMPILER-006` | 3 |
| `TR-P1-COMPILER-007` | 3 |
| `TR-P1-COMPILER-008` | 10 |
| `TR-P1-COMPILER-009` | 7 |

| 验收 | Case 数 |
|---|---:|
| `AC-P1-COMPILER-001` | 7 |
| `AC-P1-COMPILER-002` | 6 |
| `AC-P1-COMPILER-003` | 6 |
| `AC-P1-COMPILER-004` | 4 |
| `AC-P1-COMPILER-005` | 14 |
| `AC-P1-COMPILER-006` | 3 |
| `AC-P1-COMPILER-007` | 3 |
| `AC-P1-COMPILER-008` | 10 |
| `AC-P1-COMPILER-009` | 7 |

## 10. 机器可读测试设计

```json test-design
{
  "schema_version": 1,
  "target_id": "P1-COMPILER-F01",
  "phase": "test_design",
  "iteration_id": "ITER-P1-COMPILER-F01-TEST-DESIGN-007",
  "input_revisions": {
    "requirement_confirmation": "REQCONF-R04@c186ce681e1e",
    "requirement_analysis": "REQAN-R05@7de35e8dc15b",
    "business_model": "BM-R05@4ecb1f8c09f4",
    "design": "DESIGN-R05@0b37a9b4dd48"
  },
  "source_manifest": [
    "classpath:mix/orm-config.xml",
    "classpath:mix/data/User.xml",
    "classpath:mix/data/Order.xml",
    "classpath:mix/data/Pay.xml",
    "classpath:mix/view/orm-view.xml",
    "classpath:mix/system/systems.xml",
    "classpath:mix/rule/user-rule.xml",
    "classpath:mix/rule/order-rule.xml",
    "classpath:mix/rule/payment-rule.xml",
    "classpath:mix/business/order-business.xml"
  ],
  "declaration_edges": [
    {
      "type": "ROOT_DATA_FILESET",
      "from": "classpath:mix/orm-config.xml",
      "target": "classpath:mix/data/"
    },
    {
      "type": "ROOT_VIEW_FILESET",
      "from": "classpath:mix/orm-config.xml",
      "target": "classpath:mix/view/"
    },
    {
      "type": "ROOT_SYSTEM_FILE",
      "from": "classpath:mix/orm-config.xml",
      "target": "classpath:mix/system/systems.xml"
    },
    {
      "type": "ROOT_BUSINESS_FILE",
      "from": "classpath:mix/orm-config.xml",
      "target": "classpath:mix/business/order-business.xml"
    },
    {
      "type": "SYSTEM_RULE_FILE",
      "from": "classpath:mix/system/systems.xml",
      "target": "classpath:mix/rule/user-rule.xml"
    },
    {
      "type": "SYSTEM_RULE_FILE",
      "from": "classpath:mix/system/systems.xml",
      "target": "classpath:mix/rule/order-rule.xml"
    },
    {
      "type": "SYSTEM_RULE_FILE",
      "from": "classpath:mix/system/systems.xml",
      "target": "classpath:mix/rule/payment-rule.xml"
    }
  ],
  "inventory": {
    "data": 5,
    "view": 2,
    "system": 4,
    "rule_view": 14,
    "information": 16,
    "business_scope": 1,
    "directory": 5,
    "action": 8,
    "produce": 4
  },
  "business_diagnostic_codes": [
    "MIX-SOURCE-POLICY",
    "MIX-SOURCE-NOT-FOUND",
    "MIX-SOURCE-PATH-ESCAPE",
    "MIX-SOURCE-DUPLICATE-ID",
    "MIX-FRONTEND-XML-UNSAFE",
    "MIX-FRONTEND-YAML-UNSAFE",
    "MIX-SYMBOL-DUPLICATE",
    "MIX-REF-UNKNOWN",
    "MIX-REF-RULE-SYSTEM-MISMATCH",
    "MIX-INFORMATION-OWNER",
    "MIX-INFORMATION-CROSS-SYSTEM",
    "MIX-COMMON-MEMBER",
    "MIX-COMMON-UNQUALIFIED",
    "MIX-REF-VIEW-NOT-DECLARED",
    "MIX-MODEL-ACCESS-NOT-FOUND",
    "MIX-MODEL-ACCESS-AMBIGUOUS",
    "MIX-MODEL-ACCESS-NON-COMPOSITE",
    "MIX-DEFERRED-INCOMPLETE",
    "MIX-PUBLICATION-BLOCKED",
    "MIX-DIGEST-NONDETERMINISTIC",
    "MIX-CONTEXT-MUTATION",
    "MIX-PROJECTION-WRITE",
    "MIX-RETIREMENT-RESIDUE"
  ],
  "additional_design_codes": [
    "MIX-STRUCTURE-UNKNOWN",
    "MIX-COMPILATION-TIMED-OUT",
    "MIX-COMPILATION-CANCELLED",
    "MIX-CONTEXT-CONSTRUCTION-FAILED",
    "MIX-PUBLICATION-CONFLICT",
    "MIX-PUBLICATION-FAILURE",
    "MIX-OBSERVER-FAILURE"
  ],
  "cases": [
    {
      "id": "CASE-P1-TD-SOURCE-MANIFEST-001",
      "group": "source",
      "level": "contract",
      "tr": [
        "TR-P1-COMPILER-001"
      ],
      "ac": [
        "AC-P1-COMPILER-001"
      ],
      "codes": [],
      "fixture": "classpath:mix/orm-config.xml 与主/测试镜像 mix",
      "seam": "InMemoryDocumentSourceProvider + MixSourceResolver",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=MixSourceResolverContractTest#resolvesExactManifestAndEdges test",
      "expected": "SourceManifest 恰好包含 10 个固定 sourceId；声明边恰好 7 条且 edgeType/from/target/SourceRef 精确匹配；inventory 为 5 Data、2 View、4 System、14 RuleView、16 Information、1 Scope、5 Directory、8 Action、4 Produce。",
      "forbidden": "不得以 visited>=10、实现输出反推 expected、把主资源与测试镜像合并为 20 节点。",
      "red": "测试骨架可编译后，因 resolver/manifest/typed edge 尚未实现而断言失败；模块不存在、类加载失败或 fixture 缺失不算有效 RED。"
    },
    {
      "id": "CASE-P1-TD-SOURCE-ORDER-001",
      "group": "source",
      "level": "boundary",
      "tr": [
        "TR-P1-COMPILER-001",
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-001",
        "AC-P1-COMPILER-005"
      ],
      "codes": [
        "MIX-DIGEST-NONDETERMINISTIC"
      ],
      "fixture": "同一文件集的正序、逆序、随机种子 3 组枚举",
      "seam": "DeterministicFileSetProvider + SemanticDigestSnapshot",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=MixSourceResolverDeterminismTest test",
      "expected": "sourceId、边集合、Diagnostic 顺序和 semanticDigest 完全一致；sourceDigest 可因原文字节变化而独立变化。",
      "forbidden": "不得依赖文件系统枚举顺序、线程调度或 Map 迭代顺序。",
      "red": "稳定排序缺失时同一语义输入产生不同快照，形成行为型 RED。"
    },
    {
      "id": "CASE-P1-TD-SOURCE-POLICY-001",
      "group": "source",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-001"
      ],
      "ac": [
        "AC-P1-COMPILER-001"
      ],
      "codes": [
        "MIX-SOURCE-POLICY"
      ],
      "fixture": "null provider、抛异常 provider、RESOLVED 空结果、resolve 返回多项、resolveFileSet 返回零项",
      "seam": "InMemoryDocumentSourceProvider",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=SourceProviderPolicyTest test",
      "expected": "统一返回 FAILED Diagnostic=MIX-SOURCE-POLICY，SourceRef 指向声明位置。",
      "forbidden": "不得硬编码 dec-demo 路径、吞异常或继续构建 RawDefinitionSet。",
      "red": "provider typed-result 不变量尚未实现时断言失败。"
    },
    {
      "id": "CASE-P1-TD-SOURCE-NOT-FOUND-001",
      "group": "source",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-001"
      ],
      "ac": [
        "AC-P1-COMPILER-001"
      ],
      "codes": [
        "MIX-SOURCE-NOT-FOUND"
      ],
      "fixture": "删除一个显式 rule/system/business source",
      "seam": "InMemoryDocumentSourceProvider",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=SourceNotFoundTest test",
      "expected": "产生 MIX-SOURCE-NOT-FOUND；CompilationSession FAILED；Publisher 调用 0 次。",
      "forbidden": "不得忽略缺失源或回退到 classpath 其它同名文件。",
      "red": "缺失源仍被静默忽略或错误 code 不稳定时失败。"
    },
    {
      "id": "CASE-P1-TD-SOURCE-SECURITY-001",
      "group": "source",
      "level": "security",
      "tr": [
        "TR-P1-COMPILER-001"
      ],
      "ac": [
        "AC-P1-COMPILER-001"
      ],
      "codes": [
        "MIX-SOURCE-PATH-ESCAPE"
      ],
      "fixture": "../、百分号编码逃逸、symlink、file/http 未授权 scheme",
      "seam": "SourceSecurityPolicy harness",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=SourceSecurityPolicyTest test",
      "expected": "访问前拒绝并产生 MIX-SOURCE-PATH-ESCAPE；网络/根外读取计数为 0。",
      "forbidden": "不得规范化后继续访问，也不得只记录 WARN。",
      "red": "安全策略未实现时实际读取探针被调用，形成 RED。"
    },
    {
      "id": "CASE-P1-TD-SOURCE-DUPLICATE-001",
      "group": "source",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-001"
      ],
      "ac": [
        "AC-P1-COMPILER-001"
      ],
      "codes": [
        "MIX-SOURCE-DUPLICATE-ID"
      ],
      "fixture": "两个 URI 规范化为相同 sourceId，内容相同和不同各一组",
      "seam": "MixSourceGraph builder",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=DuplicateSourceIdTest test",
      "expected": "两组均以 MIX-SOURCE-DUPLICATE-ID 阻断，不覆盖已有 Source。",
      "forbidden": "不得按最后写入覆盖或按内容相同自动合并。",
      "red": "图允许重复 key 时断言失败。"
    },
    {
      "id": "CASE-P1-TD-FRONTEND-XML-001",
      "group": "frontend",
      "level": "security",
      "tr": [
        "TR-P1-COMPILER-002"
      ],
      "ac": [
        "AC-P1-COMPILER-002"
      ],
      "codes": [
        "MIX-FRONTEND-XML-UNSAFE"
      ],
      "fixture": "DOCTYPE、外部实体、网络 schema、递归实体",
      "seam": "SecureXmlFrontendHarness",
      "command": "./mvnw -pl dec-xml-frontend -am -Dtest=SecureXmlFrontendContractTest test",
      "expected": "解析前/解析中拒绝，产生 MIX-FRONTEND-XML-UNSAFE；外部访问计数 0。",
      "forbidden": "不得解析后再清洗，不得泄露本地文件内容。",
      "red": "不安全解析器会触发访问探针或未产生稳定 Diagnostic。"
    },
    {
      "id": "CASE-P1-TD-FRONTEND-YAML-001",
      "group": "frontend",
      "level": "security",
      "tr": [
        "TR-P1-COMPILER-002"
      ],
      "ac": [
        "AC-P1-COMPILER-002"
      ],
      "codes": [
        "MIX-FRONTEND-YAML-UNSAFE"
      ],
      "fixture": "任意 Java tag、别名炸弹、节点数/深度超限",
      "seam": "SafeYamlFrontendHarness",
      "command": "./mvnw -pl dec-yaml-frontend -am -Dtest=SafeYamlFrontendContractTest test",
      "expected": "产生 MIX-FRONTEND-YAML-UNSAFE；不实例化任意类型。",
      "forbidden": "不得使用通用对象构造器或忽略超限。",
      "red": "类型构造或资源限制未生效时失败。"
    },
    {
      "id": "CASE-P1-TD-CANONICAL-PARITY-001",
      "group": "frontend",
      "level": "contract",
      "tr": [
        "TR-P1-COMPILER-002",
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-002",
        "AC-P1-COMPILER-005"
      ],
      "codes": [],
      "fixture": "最小同义 XML/YAML + 格式/注释/属性顺序变体",
      "seam": "SecureXmlFrontendHarness + SafeYamlFrontendHarness + Canonical snapshot",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=CanonicalParityContractTest test",
      "expected": "CanonicalDocumentNode 和 semanticDigest 等价；SourceRef 保留各自来源；Canonical 不持有 DOM/YAML Node。",
      "forbidden": "不得比较 parser 私有对象或原文字节摘要代替语义摘要。",
      "red": "格式中立结构缺失或前端泄漏实现对象时失败。"
    },
    {
      "id": "CASE-P1-TD-STRUCTURE-UNKNOWN-001",
      "group": "raw",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-002"
      ],
      "ac": [
        "AC-P1-COMPILER-002"
      ],
      "codes": [
        "MIX-STRUCTURE-UNKNOWN"
      ],
      "fixture": "各定义层级插入未知节点/属性",
      "seam": "PassHarness(RawDefinitionBuilder)",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=UnknownStructureDiagnosticTest test",
      "expected": "所有 P1 模式均产生 ERROR MIX-STRUCTURE-UNKNOWN，未知节点 SourceRef 精确。",
      "forbidden": "不得 lenient 静默发布。",
      "red": "未知节点被忽略时失败。"
    },
    {
      "id": "CASE-P1-TD-RAW-INVENTORY-001",
      "group": "raw",
      "level": "contract",
      "tr": [
        "TR-P1-COMPILER-002"
      ],
      "ac": [
        "AC-P1-COMPILER-002"
      ],
      "codes": [],
      "fixture": "真实 mix 10 source",
      "seam": "PassHarness(RawDefinitionBuilder)",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=RawDefinitionInventoryTest test",
      "expected": "RawDefinitionSet 数量与类型精确匹配固定 inventory，全部对象有 SourceRef。",
      "forbidden": "不得从运行时 Registry 反推 Raw 数量，不得提前执行 P2-P7。",
      "red": "Raw 类型覆盖不完整时失败。"
    },
    {
      "id": "CASE-P1-TD-SYMBOL-DUPLICATE-001",
      "group": "symbol",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-003"
      ],
      "ac": [
        "AC-P1-COMPILER-003"
      ],
      "codes": [
        "MIX-SYMBOL-DUPLICATE"
      ],
      "fixture": "同类型同命名空间重复；异类型同名对照组",
      "seam": "SymbolTableFixtureBuilder",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=TypedSymbolTableTest#rejectsDuplicateTypedKey test",
      "expected": "同 TypedKey 产生 MIX-SYMBOL-DUPLICATE；异类型同名可共存。",
      "forbidden": "不得覆盖首定义或使用全局字符串命名空间。",
      "red": "重复覆盖或类型隔离缺失时失败。"
    },
    {
      "id": "CASE-P1-TD-REFERENCE-001",
      "group": "symbol",
      "level": "contract",
      "tr": [
        "TR-P1-COMPILER-003"
      ],
      "ac": [
        "AC-P1-COMPILER-003"
      ],
      "codes": [
        "MIX-REF-UNKNOWN"
      ],
      "fixture": "合法跨文件前向引用与未知 key",
      "seam": "SymbolTableFixtureBuilder + PassHarness(ReferenceResolution)",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=ForwardReferenceResolutionTest test",
      "expected": "全部注册后合法前向引用成功；未知 key 产生 MIX-REF-UNKNOWN。",
      "forbidden": "不得按发现顺序解析或猜测同名目标。",
      "red": "单遍顺序依赖或未知引用被忽略时失败。"
    },
    {
      "id": "CASE-P1-TD-RULE-SYSTEM-001",
      "group": "symbol",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-003",
        "TR-P1-COMPILER-008"
      ],
      "ac": [
        "AC-P1-COMPILER-003",
        "AC-P1-COMPILER-008"
      ],
      "codes": [
        "MIX-REF-RULE-SYSTEM-MISMATCH"
      ],
      "fixture": "来源 System、RuleView.system、view-ref owner 三者任一冲突",
      "seam": "PassHarness(ReferenceResolution)",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=RuleViewOwnershipTest test",
      "expected": "每个冲突均产生 MIX-REF-RULE-SYSTEM-MISMATCH。",
      "forbidden": "不得以文件路径或名称猜测 owner。",
      "red": "归属不一致仍解析成功时失败。"
    },
    {
      "id": "CASE-P1-TD-INFORMATION-OWNER-001",
      "group": "information",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-003",
        "TR-P1-COMPILER-008"
      ],
      "ac": [
        "AC-P1-COMPILER-003",
        "AC-P1-COMPILER-008"
      ],
      "codes": [
        "MIX-INFORMATION-OWNER"
      ],
      "fixture": "Information 位于 BusinessScope/System 外或错误 namespace",
      "seam": "InformationExpressionFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=InformationOwnershipPolicyTest test",
      "expected": "产生 MIX-INFORMATION-OWNER；BusinessScope Information 数为 0。",
      "forbidden": "不得自动迁移到任意 System。",
      "red": "非法 owner 仍注册 InformationKey 时失败。"
    },
    {
      "id": "CASE-P1-TD-COMMON-SUCCESS-001",
      "group": "information",
      "level": "contract",
      "tr": [
        "TR-P1-COMPILER-004",
        "TR-P1-COMPILER-008"
      ],
      "ac": [
        "AC-P1-COMPILER-004",
        "AC-P1-COMPILER-008"
      ],
      "codes": [],
      "fixture": "common.paySuccess/common.payError",
      "seam": "InformationExpressionFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=CommonInformationExpressionTest#resolvesQualifiedDependencies test",
      "expected": "仅解析 system-qualified InformationKey，登记 P3 Deferred；common 无 Data/View/RuleView/ModelAccess。",
      "forbidden": "不得求值表达式或在 P1 检测 DAG 循环。",
      "red": "依赖 key/Deferred 不完整时失败。"
    },
    {
      "id": "CASE-P1-TD-INFORMATION-CROSS-SYSTEM-001",
      "group": "information",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-008"
      ],
      "ac": [
        "AC-P1-COMPILER-008"
      ],
      "codes": [
        "MIX-INFORMATION-CROSS-SYSTEM"
      ],
      "fixture": "order System expression 引用 payment.error",
      "seam": "InformationExpressionFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=InformationOwnershipPolicyTest#rejectsCrossSystemExpression test",
      "expected": "产生 MIX-INFORMATION-CROSS-SYSTEM。",
      "forbidden": "不得自动迁移到 common 或降级为 Deferred 成功。",
      "red": "普通 System 可组合跨 System key 时失败。"
    },
    {
      "id": "CASE-P1-TD-COMMON-MEMBER-001",
      "group": "information",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-008"
      ],
      "ac": [
        "AC-P1-COMPILER-008"
      ],
      "codes": [
        "MIX-COMMON-MEMBER"
      ],
      "fixture": "common 声明 Data/View/RuleView/ModelAccess 或非 expression Information",
      "seam": "InformationExpressionFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=CommonSystemBoundaryTest test",
      "expected": "逐类产生 MIX-COMMON-MEMBER。",
      "forbidden": "不得只忽略非法成员。",
      "red": "common 边界未校验时失败。"
    },
    {
      "id": "CASE-P1-TD-COMMON-QUALIFIED-001",
      "group": "information",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-008"
      ],
      "ac": [
        "AC-P1-COMPILER-008"
      ],
      "codes": [
        "MIX-COMMON-UNQUALIFIED"
      ],
      "fixture": "裸 localName、未知 system、未知 localName",
      "seam": "InformationExpressionFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=CommonInformationExpressionTest#rejectsUnqualifiedReferences test",
      "expected": "产生 MIX-COMMON-UNQUALIFIED，relatedRefs 指向问题 token。",
      "forbidden": "不得在所有 System 全局搜索同名 Information。",
      "red": "未限定引用被解析时失败。"
    },
    {
      "id": "CASE-P1-TD-VIEW-BOUNDARY-001",
      "group": "model_access",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-008",
        "TR-P1-COMPILER-009"
      ],
      "ac": [
        "AC-P1-COMPILER-008",
        "AC-P1-COMPILER-009"
      ],
      "codes": [
        "MIX-REF-VIEW-NOT-DECLARED"
      ],
      "fixture": "Information/ModelAccess ref@view 未在当前 System.view-info",
      "seam": "ModelAccessFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=SystemViewBoundaryTest test",
      "expected": "产生 MIX-REF-VIEW-NOT-DECLARED。",
      "forbidden": "不得跨 System/View 搜索。",
      "red": "目标 View 通过全局名称找到时失败。"
    },
    {
      "id": "CASE-P1-TD-MODEL-ACCESS-TARGET-MAIN-001",
      "group": "model_access",
      "level": "contract",
      "tr": [
        "TR-P1-COMPILER-008",
        "TR-P1-COMPILER-009"
      ],
      "ac": [
        "AC-P1-COMPILER-008",
        "AC-P1-COMPILER-009"
      ],
      "codes": [],
      "fixture": "OrderInfo.user -> UserInfo，target-main=user，同时存在同名 property path",
      "seam": "ModelAccessFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorPolicyTest#targetMainWins test",
      "expected": "selector 首先且仅命中 target-main 根目标，sourcePath 仍为 user。",
      "forbidden": "不得优先 property path、root-property、模糊或跨 View 匹配。",
      "red": "优先级错误时 resolvedTarget 快照不同。"
    },
    {
      "id": "CASE-P1-TD-MODEL-ACCESS-PATH-001",
      "group": "model_access",
      "level": "contract",
      "tr": [
        "TR-P1-COMPILER-009"
      ],
      "ac": [
        "AC-P1-COMPILER-009"
      ],
      "codes": [],
      "fixture": "target-main 不匹配，唯一嵌套 property path",
      "seam": "ModelAccessFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorPolicyTest#fallsBackToExactPropertyPath test",
      "expected": "区分大小写逐段命中唯一目标。",
      "forbidden": "不得模糊、大小写折叠或全局搜索。",
      "red": "fallback 未实现或非精确匹配时失败。"
    },
    {
      "id": "CASE-P1-TD-MODEL-ACCESS-NOT-FOUND-001",
      "group": "model_access",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-009"
      ],
      "ac": [
        "AC-P1-COMPILER-009"
      ],
      "codes": [
        "MIX-MODEL-ACCESS-NOT-FOUND"
      ],
      "fixture": "target-main 与 property path 均未命中/大小写不同/缺段",
      "seam": "ModelAccessFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorPolicyTest#rejectsMissingSelector test",
      "expected": "产生 MIX-MODEL-ACCESS-NOT-FOUND。",
      "forbidden": "不得猜测最相近名称或回退根属性。",
      "red": "错误匹配成功或 code 不稳定时失败。"
    },
    {
      "id": "CASE-P1-TD-MODEL-ACCESS-AMBIGUOUS-001",
      "group": "model_access",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-009"
      ],
      "ac": [
        "AC-P1-COMPILER-009"
      ],
      "codes": [
        "MIX-MODEL-ACCESS-AMBIGUOUS"
      ],
      "fixture": "重复 ref、多候选、重叠 write",
      "seam": "ModelAccessFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorPolicyTest#rejectsAmbiguity test",
      "expected": "逐种产生 MIX-MODEL-ACCESS-AMBIGUOUS，零绑定发布。",
      "forbidden": "不得按首个候选获胜。",
      "red": "冲突映射被接受时失败。"
    },
    {
      "id": "CASE-P1-TD-MODEL-ACCESS-NON-COMPOSITE-001",
      "group": "model_access",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-009"
      ],
      "ac": [
        "AC-P1-COMPILER-009"
      ],
      "codes": [
        "MIX-MODEL-ACCESS-NON-COMPOSITE"
      ],
      "fixture": "property path 中间段为 scalar",
      "seam": "ModelAccessFixture",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorPolicyTest#rejectsNonCompositeIntermediate test",
      "expected": "产生 MIX-MODEL-ACCESS-NON-COMPOSITE。",
      "forbidden": "不得截断路径或将 scalar 当对象。",
      "red": "中间段类型未验证时失败。"
    },
    {
      "id": "CASE-P1-TD-DEFERRED-COMPLETE-001",
      "group": "deferred",
      "level": "contract",
      "tr": [
        "TR-P1-COMPILER-004"
      ],
      "ac": [
        "AC-P1-COMPILER-004"
      ],
      "codes": [
        "MIX-DEFERRED-INCOMPLETE"
      ],
      "fixture": "P2-P8 各一 Deferred；分别缺 requiredStage/reason/SourceRef/resolvedKeys",
      "seam": "PassHarness(DeferredClassification)",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=DeferredDefinitionCompletenessTest test",
      "expected": "完整项冻结成功；任一字段缺失产生 MIX-DEFERRED-INCOMPLETE。",
      "forbidden": "不得静默忽略或以 null 占位发布。",
      "red": "不完整 Deferred 可进入 Registry 时失败。"
    },
    {
      "id": "CASE-P1-TD-DEFERRED-NO-RUNTIME-001",
      "group": "deferred",
      "level": "boundary",
      "tr": [
        "TR-P1-COMPILER-004",
        "TR-P1-COMPILER-008"
      ],
      "ac": [
        "AC-P1-COMPILER-004",
        "AC-P1-COMPILER-008"
      ],
      "codes": [],
      "fixture": "注入 evaluator/executor/query/transaction fail-fast spies",
      "seam": "PassHarness + fail-fast runtime spies",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=P1RuntimeBoundaryTest test",
      "expected": "Information evaluator、Action/Directory executor、Query planner、Transaction manager 调用均为 0。",
      "forbidden": "不得提前执行 P2-P7。",
      "red": "任一运行服务被调用即失败。"
    },
    {
      "id": "CASE-P1-TD-PUBLISH-SUCCESS-001",
      "group": "publication",
      "level": "contract",
      "tr": [
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-005"
      ],
      "codes": [],
      "fixture": "无 ERROR 的完整 Session + expectedCurrent",
      "seam": "ContextPublisherSpy",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=AtomicPublicationContractTest#publishesExactlyOnce test",
      "expected": "同一 compileAndPublish 调用内 Publisher 恰好 1 次，返回 PUBLISHED 与完整不可变 Context；返回后不再调用。",
      "forbidden": "不得由 starter 二次发布或先暴露未发布模型。",
      "red": "发布缺失/重复/延迟时失败。"
    },
    {
      "id": "CASE-P1-TD-PUBLISH-BLOCKED-001",
      "group": "publication",
      "level": "negative",
      "tr": [
        "TR-P1-COMPILER-003",
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-003",
        "AC-P1-COMPILER-005"
      ],
      "codes": [
        "MIX-PUBLICATION-BLOCKED"
      ],
      "fixture": "任一语义 ERROR 或 Registry 冻结失败",
      "seam": "ContextPublisherSpy",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=AtomicPublicationContractTest#blocksOnError test",
      "expected": "FAILED/MIX-PUBLICATION-BLOCKED；model/context/digest 不可取得；Publisher 0 次；旧 Context digest 不变。",
      "forbidden": "不得部分发布或返回 success-with-errors。",
      "red": "ERROR 后 Publisher 被调用或结果暴露模型时失败。"
    },
    {
      "id": "CASE-P1-TD-PUBLISH-TIMEOUT-001",
      "group": "publication",
      "level": "failure",
      "tr": [
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-005"
      ],
      "codes": [
        "MIX-COMPILATION-TIMED-OUT"
      ],
      "fixture": "MonotonicClockStub 在指定 pass 越过 Deadline",
      "seam": "MonotonicClockStub + ContextPublisherSpy",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=CompilationDeadlineTest test",
      "expected": "FAILED/MIX-COMPILATION-TIMED-OUT；Publisher 0 次；状态机结束 FAILED。",
      "forbidden": "不得使用墙钟 sleep 或继续后续 pass。",
      "red": "deadline 未检查或 code 不稳定时失败。"
    },
    {
      "id": "CASE-P1-TD-PUBLISH-CANCEL-001",
      "group": "publication",
      "level": "failure",
      "tr": [
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-005"
      ],
      "codes": [
        "MIX-COMPILATION-CANCELLED"
      ],
      "fixture": "CancellationTokenStub 在各 pass 边界取消",
      "seam": "CancellationTokenStub + ContextPublisherSpy",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=CompilationCancellationTest test",
      "expected": "FAILED/MIX-COMPILATION-CANCELLED；Publisher 0 次；旧 Context 保持。",
      "forbidden": "不得把 cancel 当异常重试或部分结果。",
      "red": "取消后仍执行/发布时失败。"
    },
    {
      "id": "CASE-P1-TD-CONTEXT-CONSTRUCTION-001",
      "group": "publication",
      "level": "failure",
      "tr": [
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-005"
      ],
      "codes": [
        "MIX-CONTEXT-CONSTRUCTION-FAILED"
      ],
      "fixture": "Context factory 在冻结后抛受控异常",
      "seam": "ContextPublisherSpy + failing Context factory",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=AtomicPublicationContractTest#failsContextConstruction test",
      "expected": "FAILED/MIX-CONTEXT-CONSTRUCTION-FAILED；Publisher 0 次；无 model/context/digest。",
      "forbidden": "不得发布半构造 Context。",
      "red": "异常泄漏或 Publisher 被调用时失败。"
    },
    {
      "id": "CASE-P1-TD-PUBLISH-CONFLICT-001",
      "group": "publication",
      "level": "concurrency",
      "tr": [
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-005"
      ],
      "codes": [
        "MIX-PUBLICATION-CONFLICT"
      ],
      "fixture": "Publisher 返回 CONFLICT，expectedCurrent 已被其它线程替换",
      "seam": "ContextPublisherSpy",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=AtomicPublicationContractTest#mapsCasConflict test",
      "expected": "FAILED/MIX-PUBLICATION-CONFLICT；Publisher 1 次；现有新 Context 不被覆盖。",
      "forbidden": "不得自动重试无上限或强制覆盖。",
      "red": "冲突被视为 PUBLISHED 时失败。"
    },
    {
      "id": "CASE-P1-TD-PUBLISH-FAILURE-001",
      "group": "publication",
      "level": "failure",
      "tr": [
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-005"
      ],
      "codes": [
        "MIX-PUBLICATION-FAILURE"
      ],
      "fixture": "Publisher 返回 null 或抛异常",
      "seam": "ContextPublisherSpy",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=AtomicPublicationContractTest#mapsPublisherFailure test",
      "expected": "FAILED/MIX-PUBLICATION-FAILURE；Publisher 1 次；旧 Context 保持。",
      "forbidden": "不得传播未分类异常或假定发布成功。",
      "red": "null/异常未统一映射时失败。"
    },
    {
      "id": "CASE-P1-TD-DIGEST-001",
      "group": "digest",
      "level": "determinism",
      "tr": [
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-005"
      ],
      "codes": [
        "MIX-DIGEST-NONDETERMINISTIC"
      ],
      "fixture": "同义 XML/YAML、注释/格式变化、不同线程调度；非同义变体对照",
      "seam": "SemanticDigestSnapshot",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=SemanticDigestContractTest test",
      "expected": "同义输入 semanticDigest 相同，非同义不同；sourceDigest 反映原文；digest 输入不含 DigestPair 自身。",
      "forbidden": "不得使用不稳定对象 hashCode 或自引用摘要。",
      "red": "重跑/并发结果不一致时失败并映射 MIX-DIGEST-NONDETERMINISTIC。"
    },
    {
      "id": "CASE-P1-TD-CONTEXT-ISOLATION-001",
      "group": "context",
      "level": "concurrency",
      "tr": [
        "TR-P1-COMPILER-005",
        "TR-P1-COMPILER-006"
      ],
      "ac": [
        "AC-P1-COMPILER-005",
        "AC-P1-COMPILER-006"
      ],
      "codes": [
        "MIX-CONTEXT-MUTATION"
      ],
      "fixture": "两组配置并发编译、并行读取、修改尝试、global current 探针",
      "seam": "ContextPublisherSpy + immutable collection probes",
      "command": "./mvnw -pl dec-core-context -am -Dtest=EngineContextIsolationTest test",
      "expected": "Registry/Diagnostic/digest 无交叉污染；修改拒绝并产生/映射 MIX-CONTEXT-MUTATION；无 static mutable current。",
      "forbidden": "不得共享 builder、Registry 或可变集合。",
      "red": "任一 Context 可观察到另一 Session 数据或可写时失败。"
    },
    {
      "id": "CASE-P1-TD-PROJECTION-001",
      "group": "context",
      "level": "compatibility",
      "tr": [
        "TR-P1-COMPILER-006"
      ],
      "ac": [
        "AC-P1-COMPILER-006"
      ],
      "codes": [
        "MIX-PROJECTION-WRITE"
      ],
      "fixture": "从同一 Context 读取 Data/View/Rule；注册/修改/删除尝试",
      "seam": "CoreConfigProjection public API",
      "command": "./mvnw -pl dec-core-context -am -Dtest=CoreConfigProjectionContractTest test",
      "expected": "读取与 CompiledModelSet 一致；写操作产生 MIX-PROJECTION-WRITE；不存在第二 Registry。",
      "forbidden": "不得双写、缓存独立事实或提供 mutation API。",
      "red": "投影可写或与源 Registry 不一致时失败。"
    },
    {
      "id": "CASE-P1-TD-DIAGNOSTIC-CATALOG-001",
      "group": "diagnostic",
      "level": "contract",
      "tr": [
        "TR-P1-COMPILER-001",
        "TR-P1-COMPILER-002",
        "TR-P1-COMPILER-003",
        "TR-P1-COMPILER-004",
        "TR-P1-COMPILER-005",
        "TR-P1-COMPILER-006",
        "TR-P1-COMPILER-007",
        "TR-P1-COMPILER-008",
        "TR-P1-COMPILER-009"
      ],
      "ac": [
        "AC-P1-COMPILER-001",
        "AC-P1-COMPILER-002",
        "AC-P1-COMPILER-003",
        "AC-P1-COMPILER-004",
        "AC-P1-COMPILER-005",
        "AC-P1-COMPILER-006",
        "AC-P1-COMPILER-007",
        "AC-P1-COMPILER-008",
        "AC-P1-COMPILER-009"
      ],
      "codes": [],
      "fixture": "BM-R05 23 个业务错误各一最小 fixture，多错误乱序输入",
      "seam": "DiagnosticSnapshot",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=DiagnosticCatalogContractTest test",
      "expected": "23 个稳定 code 均有 code/severity/SourceRef/definitionKey/relatedRefs/pass/recoveryHint；排序为 sourceId,line,column,code,entityKey,pass。",
      "forbidden": "不得用 message 文案作为契约、缺 key 时不得用 null 影响排序。",
      "red": "目录缺码、字段缺失或重跑排序不同即失败。"
    },
    {
      "id": "CASE-P1-TD-OBSERVER-TIMING-001",
      "group": "diagnostic",
      "level": "observability",
      "tr": [
        "TR-P1-COMPILER-005"
      ],
      "ac": [
        "AC-P1-COMPILER-005"
      ],
      "codes": [
        "MIX-OBSERVER-FAILURE"
      ],
      "fixture": "MonotonicClockStub + 正常/抛异常 Observer",
      "seam": "CompilationObserverSpy",
      "command": "./mvnw -pl dec-core-compiler -am -Dtest=CompilationObserverContractTest test",
      "expected": "discovery/parse/每 pass/digest 各一次非负 elapsedNanos；状态转换完整；Observer 异常仅增加非 ERROR MIX-OBSERVER-FAILURE，不改变原结果/context/digest。",
      "forbidden": "不得使用真实时长阈值或让 Observer 控制业务结果。",
      "red": "观测失败改变状态或摘要时失败。"
    },
    {
      "id": "CASE-P1-TD-RETIREMENT-001",
      "group": "architecture",
      "level": "architecture",
      "tr": [
        "TR-P1-COMPILER-007"
      ],
      "ac": [
        "AC-P1-COMPILER-007"
      ],
      "codes": [
        "MIX-RETIREMENT-RESIDUE"
      ],
      "fixture": "仓库树、根 POM、dependency:tree、ServiceLoader、反射字符串、构建 artifact",
      "seam": "repository scan + Maven dependency output",
      "command": "python3 project_doc/version/V_1.0/task/P1-COMPILER-F01/validation/test_retirement_contract.py",
      "expected": "dec-expand-declaration、LegacyDeclarationAdapter、复制实现和第二运行时残留数为 0；否则 MIX-RETIREMENT-RESIDUE。",
      "forbidden": "不得以排除构建或保留 Adapter 视为退役。",
      "red": "P1 开发前当前残留可作为表征基线；真正 TDD/开发门禁必须在删除任务后转绿。"
    },
    {
      "id": "CASE-P1-TD-JAVA8-MODULE-001",
      "group": "architecture",
      "level": "architecture",
      "tr": [
        "TR-P1-COMPILER-005",
        "TR-P1-COMPILER-007"
      ],
      "ac": [
        "AC-P1-COMPILER-005",
        "AC-P1-COMPILER-007"
      ],
      "codes": [],
      "fixture": "compiler/context/starter/frontend POM 与生产源码",
      "seam": "Maven compile + rg static scan + dependency:tree",
      "command": "./mvnw -pl dec-core-compiler,dec-core-context,dec-core-starter -am -Dmaven.compiler.source=1.8 -Dmaven.compiler.target=1.8 test",
      "expected": "Java 8 编译通过；无 record/List.of/Map.of/Optional.isEmpty/var；compiler core 无 DOM4J/SnakeYAML/SQL/MySQL/demo 生产依赖；context 不反向依赖 compiler。",
      "forbidden": "不得通过提高 source level 或 scope=provided 隐藏依赖。",
      "red": "未来模块建立后，非法 API/依赖由编译或静态扫描稳定失败。"
    }
  ],
  "revision": "TESTDESIGN-R01@ba7779cf089b"
}
```
