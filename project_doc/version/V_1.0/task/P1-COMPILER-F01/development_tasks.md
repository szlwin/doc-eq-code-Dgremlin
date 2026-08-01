# P1-COMPILER-F01 Development Task Plan

> 该文件由 `development_tasks.yaml` 确定性生成，仅用于阅读；YAML 是唯一事实源。

- Plan: `TP-P1-COMPILER-F01`
- Revision: `TP-P1-COMPILER-F01-R01@88b56e6caa64`
- Status: `PASSED`
- Execution: `SEQUENTIAL`
- Review round: `1`

## Input Revisions

- requirement_analysis: `REQAN-R05@7de35e8dc15b`
- business_model: `BM-R05@4ecb1f8c09f4`
- design: `DESIGN-R05@0b37a9b4dd48`
- test_design: `TESTDESIGN-R01@ba7779cf089b`

## Tasks

### TASK-P1-T01: 建立 Context 中立不可变编译契约

**Goal:** 在 dec-core-context 中交付 compiler 与运行上下文共享的 Java 8 不可变类型闭包，使后续模块能够只依赖稳定契约。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** None

**Implementation:**

- Module: `dec-core-context`
- Component: `dec.core.context.model`
- 先创建 SourceRef、DiagnosticCode、Diagnostic、DefinitionKey 与各 TypedKey 的可编译失败测试，固定全部语义字段、稳定 equals/hashCode/toString 和空值拒绝规则。
- 实现 Registry、DeferredKind、RequiredStage、DeferredDefinition、DigestPair、CompiledDefinition 与 CompiledModelSet 的防御性复制和不可变集合。
- 实现 EngineContext 与 CoreConfigProjection 的只读接口骨架，禁止 public mutator、static current 和对 compiler 类型的依赖。
- 运行 context 单元测试、Java 8 编译与 dependency tree，记录 RED、GREEN、REFACTOR 和模块依赖证据。

**Acceptance Criteria:**

- `AC-P1-T01-001` — 所有共享值对象必须通过 Java 8 编译并对输入集合执行防御性复制，外部修改不得改变已构造对象。
- `AC-P1-T01-002` — context dependency tree 中必须不存在 dec-core-compiler，EngineContext 与 Registry 必须不存在 public mutator 和 static current。

**Validation:**

- `./mvnw -pl dec-core-context -am -Dtest=ContextValueContractTest,RegistryImmutabilityTest,EngineContextApiTest test`
- `./mvnw -pl dec-core-context -am -DskipTests verify`
- `./mvnw -pl dec-core-context dependency:tree -Dincludes=doc.eq.code:dec-core-compiler`

### TASK-P1-T02: 创建 Compiler 模块与公共编译入口

**Goal:** 新增 dec-core-compiler Maven 模块并建立 ModelCompiler、CompilationRequest、CompilationOptions、CompilationResult 和 PublicationRequest 的稳定 Java 8 API。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T01`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.api`
- 在父 POM 的 modules 与 dependencyManagement 中登记 dec-core-compiler，并让 compiler 仅依赖 dec-core-context。
- 先创建公共 API 合同测试，验证 compileAndPublish 是唯一公共成功入口，compile-only 只允许包内 PassHarness 使用。
- 实现请求、选项、取消令牌、deadline、PublicationRequest 和 PUBLISHED/FAILED 结果类型的不可变骨架。
- 运行模块编译、API 反射测试和依赖树检查，确认不存在 compiler-context 循环。

**Acceptance Criteria:**

- `AC-P1-T02-001` — 父 Reactor 必须能够构建 dec-core-compiler，公共 API 反射测试必须通过且仅暴露 DESIGN-R05 允许的入口。
- `AC-P1-T02-002` — compiler dependency tree 必须精确包含 context 而 context dependency tree 必须不存在 compiler。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dtest=CompilerApiContractTest,ModuleDependencyTest test`
- `./mvnw -pl dec-core-compiler -am verify`
- `./mvnw -pl dec-core-compiler dependency:tree`

### TASK-P1-T03: 实现安全源发现与精确 SourceGraph

**Goal:** 交付 DocumentSourceProvider、SourcePolicy 和 MixSourceResolver，使固定 mix 入口生成精确 10 个 source 与 7 条声明边。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T02`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.source`
- 先实现 InMemoryDocumentSourceProvider 驱动的 SourceManifest、排序、路径逃逸、缺失源和重复 ID 失败测试。
- 实现 URI 规范化、scheme 白名单、根路径、maxDepth、maxSources、maxTotalBytes 与 contentDigest 去重。
- 实现 ROOT_DATA_FILESET、ROOT_VIEW_FILESET、ROOT_SYSTEM_FILE、ROOT_BUSINESS_FILE 和 SYSTEM_RULE_FILE 类型化边及 declarationSourceRef。
- 使用主资源和测试镜像分别验证 10 个 source、7 条边、稳定顺序和零网络访问。

**Acceptance Criteria:**

- `AC-P1-T03-001` — 固定 mix fixture 必须精确生成 10 个 SourceManifest source 和 7 条 declaration edge，顺序必须稳定一致。
- `AC-P1-T03-002` — 路径逃逸、未知 scheme、缺失源、重复 ID 不同内容和资源上限必须失败并生成稳定 Diagnostic。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dtest=MixSourceResolverContractTest,SourcePolicySecurityTest,SourceGraphFailureTest test`

### TASK-P1-T04: 实现安全 XML Canonical Frontend

**Goal:** 将 XML 输入安全解析为 CanonicalDocumentNode，同时拒绝 DOCTYPE、外部实体、网络访问和越权文件读取。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T03`

**Implementation:**

- Module: `dec-context-config-parse-xml`
- Component: `dec.core.compiler.canonical.xml`
- 先创建 XXE、DOCTYPE、外部 schema、网络和越权文件访问的失败测试以及 SourceRef 保真测试。
- 将 XML Parser 从直接写 ConfigInfo 调整为实现 DocumentFrontend，仅产出有序属性、标量、子节点和 SourceRef。
- 通过依赖注入接入 compiler canonical API，不登记 TypedKey、业务默认值或运行时对象。
- 运行 XML security、Canonical contract 和现有 XML 回归测试。

**Acceptance Criteria:**

- `AC-P1-T04-001` — 安全 XML fixture 必须生成稳定 Canonical 节点和精确 SourceRef，属性与子节点顺序必须一致。
- `AC-P1-T04-002` — DOCTYPE、外部实体、网络和根目录外访问必须被拒绝且访问探针必须保持 0 次。

**Validation:**

- `./mvnw -pl dec-context-config-parse-xml,dec-core-compiler -am -Dtest=XmlFrontendSecurityTest,XmlCanonicalContractTest test`

### TASK-P1-T05: 实现安全 YAML Canonical Frontend

**Goal:** 将 YAML 输入安全转换为与 XML 等价的 CanonicalDocumentNode，并固定标签、别名、对象构造和资源上限。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T04`

**Implementation:**

- Module: `dec-context-config-parse-yaml`
- Component: `dec.core.compiler.canonical.yaml`
- 先创建危险 tag、别名膨胀、递归结构、超深层级和对象构造失败测试。
- 配置 SnakeYAML 安全加载选项并实现 DocumentFrontend，只产出 Canonical 节点和 SourceRef。
- 建立同语义 XML/YAML fixture，精确比较 Canonical 树而不是比较序列化文本。
- 运行 YAML security、Canonical parity 和模块依赖测试。

**Acceptance Criteria:**

- `AC-P1-T05-001` — 等价 XML 与 YAML 输入必须生成语义一致的 CanonicalDocumentNode 和稳定 SourceRef。
- `AC-P1-T05-002` — 危险标签、别名膨胀、超深层级和对象构造输入必须失败且不得产生部分 Canonical 结果。

**Validation:**

- `./mvnw -pl dec-context-config-parse-yaml,dec-context-config-parse-xml,dec-core-compiler -am -Dtest=YamlFrontendSecurityTest,CanonicalParityTest test`

### TASK-P1-T06: 构建 Canonical 到 Raw 定义与固定 Inventory

**Goal:** 实现 RawDefinitionBuilder 与 RawDefinitionSet，将 Canonical 输入转为保留 owner、SourceRef、原始引用和稳定顺序的 P1 Raw 定义。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T05`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.raw`
- 先创建固定 mix inventory、未知元素、缺字段、顺序确定性和 SourceRef 保真测试。
- 实现 RootConfig、DataSource、Connection、Data、View、System、RuleView、Rule、BusinessScope、Information、ModelAccess、Directory、Action、Produce Raw 类型。
- 未知元素统一产生 MIX-STRUCTURE-UNKNOWN，不提供可发布的 lenient 模式。
- 验证主资源和测试镜像分别得到 5/2/4/14/16/1/5/8/4 固定 inventory。

**Acceptance Criteria:**

- `AC-P1-T06-001` — 固定 mix 必须精确生成 5 Data、2 View、4 System、14 RuleView、16 Information、1 Scope、5 Directory、8 Action、4 Produce。
- `AC-P1-T06-002` — 未知元素或缺少阻断字段必须失败并生成稳定 SourceRef Diagnostic，RawSet 不得被部分发布。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dtest=RawDefinitionInventoryTest,StructuralValidationTest test`

### TASK-P1-T07: 实现 TypedKey 与两遍 Symbol 注册

**Goal:** 建立全部 P1 TypedKey 和有序 SymbolTable，使 owner 边界、前向引用准备和重复定义失败可独立验证。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T06`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.symbol`
- 先创建 TypedKey 类型隔离、InformationKey System owner、无名 Produce sourceOrdinal 和重复定义失败测试。
- 实现第一遍顶层与 owner Key 注册，第二遍子定义注册，保持稳定 canonical key 顺序。
- Registry 使用 context 不可变有序 map，禁止最后写入覆盖。
- 运行重复 Key、不同类型同名、稳定序列化和前向引用准备测试。

**Acceptance Criteria:**

- `AC-P1-T07-001` — TypedKey 必须按类型和 owner 精确区分，同类型重复 Key 必须失败且不同类型同名不得互相覆盖。
- `AC-P1-T07-002` — SymbolTable 重复运行必须生成一致有序快照并保留全部定义 SourceRef。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dtest=TypedKeyContractTest,SymbolRegistrationTest test`

### TASK-P1-T08: 实现 P1 强类型引用解析

**Goal:** 解析 connection、view、system、rule、business action、directory 和 produce 的 P1 引用，未知或类型不匹配时聚合稳定 Diagnostic。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T07`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.symbol.ReferenceResolver`
- 先创建正常引用、未知引用、类型不匹配、owner 不一致和 rule-system mismatch 测试。
- 实现按 TypedKey 的精确查询和两遍前向引用解析，不跨类型、System 或 View 搜索。
- 聚合 Diagnostic 后按稳定 SourceRef/code/definition key 排序，不因发现顺序提前终止。
- 运行 ReferenceResolver 合同和固定 mix 全引用测试。

**Acceptance Criteria:**

- `AC-P1-T08-001` — 固定 mix 的全部 P1 引用必须解析为预期 TypedKey，未知或类型不匹配引用必须精确失败。
- `AC-P1-T08-002` — 多错误输入必须生成稳定排序的 Diagnostic 集合且不暴露部分 Context。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dtest=ReferenceResolverContractTest,DiagnosticOrderTest test`

### TASK-P1-T09: 实现 System-owned Information 与 common 表达式绑定

**Goal:** 将 Information 归属、表达式 AST、限定引用和 common 跨 System 规则转为强类型 P3 Deferred，而不执行求值。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T08`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.information`
- 先创建普通 System 本地引用、common.paySuccess/common.payError、未限定引用、非法跨 System、common 非 expression 成员测试。
- 实现 InformationExpressionAst 解析和 InformationReferenceResolver，将引用绑定为 InformationKey。
- 实现 InformationOwnershipValidator 与 CommonSystemValidator，普通 System 只引用自身，跨 System 仅由 common expression 持有。
- 将 AST、resolved keys、SourceRef 和 requiredStage=P3 写入 DeferredDefinition，验证无求值缓存或运行时执行。

**Acceptance Criteria:**

- `AC-P1-T09-001` — common.paySuccess 与 common.payError 必须精确绑定四个 system-qualified InformationKey 并生成 P3 Deferred。
- `AC-P1-T09-002` — 未限定引用、非法 owner、普通 System 跨 System 引用和 common 非 expression 成员必须失败且不得发布。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dtest=InformationOwnershipTest,CommonInformationExpressionTest test`

### TASK-P1-T10: 实现 ModelAccess 精确 Selector

**Goal:** 实现 source path 与 target selector 分离的 ModelAccessBinding，严格执行 target-main 优先和 property path 逐段回退。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T09`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.modelaccess`
- 先创建 target-main 精确命中、property path 回退、缺失、歧义、非复合中间段、View 未声明和重叠 WRITE 测试。
- 实现 SharedModelPath、SystemViewSelector、TargetPropertyPath 和 ModelAccessBinding。
- 解析时先精确选择当前 System 声明的 View，再区分大小写完整匹配 target-main，未命中才逐段解析 property path。
- 输出 requiredStage=P2 的完整 Deferred，并运行所有禁止模糊/跨 View/root-property 降级断言。

**Acceptance Criteria:**

- `AC-P1-T10-001` — target-main 完整匹配必须优先于 property path，未命中时才允许逐段精确回退。
- `AC-P1-T10-002` — 缺失、歧义、非复合中间段、未声明 View 和重叠 WRITE 必须失败且不得猜测降级。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dtest=ModelAccessSelectorTest,ModelAccessFailureTest test`

### TASK-P1-T11: 完成 P2 至 P7 Deferred 分类

**Goal:** 把 P1 已解析但后续阶段执行的 System、ModelAccess、Information、Action、Produce、Directory、Query 和 Transaction 定义登记为完整 Deferred。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T10`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.deferred`
- 先创建 requiredStage、reasonCode、SourceRef、NormalizedBody 和 resolvedReferences 完整性测试。
- 实现 DeferredClassificationPolicy 与 DeferredDefinitionBuilder，按 P2-P7 映射稳定 kind 和 requiredStage。
- 对可解析但未类型化引用、缺 owner/reason/SourceRef/body 的条目生成 MIX-DEFERRED-INCOMPLETE。
- 验证 P1 不执行权限、Information 求值、Action/Produce、Directory、Query 或事务语义。

**Acceptance Criteria:**

- `AC-P1-T11-001` — 固定 mix 的所有后续语义必须生成 owner、kind、requiredStage、reason、SourceRef、body 和强类型引用完整的 Deferred。
- `AC-P1-T11-002` — 任一必填字段缺失或引用未类型化必须失败并生成 MIX-DEFERRED-INCOMPLETE。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dtest=DeferredClassificationTest,DeferredCompletenessTest test`

### TASK-P1-T12: 实现十阶段 Compiler Pipeline 与 Session 状态机

**Goal:** 交付 CompilerPass、PassContext、CompilationSession 和十阶段顺序编排，使任一失败稳定进入 FAILED 且不执行 PublicationPass。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T11`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.pass`
- 先创建 CREATED 到 PUBLISHED 的合法转换、任一阶段到 FAILED、终态拒绝和 pass 顺序测试。
- 实现 Session 内 builder、DiagnosticCollector、Deferred builder 和 timing collector，禁止 static/thread-local。
- 按 DESIGN-R05 固定顺序组装 10 个 Pass，并在 ERROR、timeout 或 cancel 时停止后续成功路径。
- 实现稳定 Diagnostic 排序后再构造 CompilationResult，验证多 Session 隔离。

**Acceptance Criteria:**

- `AC-P1-T12-001` — 十个 Pass 必须按固定顺序执行，合法输入必须经历唯一状态路径并在发布成功后进入 PUBLISHED。
- `AC-P1-T12-002` — 任一 ERROR、timeout 或 cancel 必须进入 FAILED，后续 PublicationPass 必须不执行且旧 Context 保持。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dtest=CompilerPipelineOrderTest,CompilationSessionStateTest,SessionIsolationTest test`

### TASK-P1-T13: 实现确定性 Digest、Deadline 与 Observer

**Goal:** 实现 DEC-SEMANTIC-DIGEST-V1、MonotonicClock、deadline/cancel 和只读 Observer，使重复编译结果稳定且观测失败不改变状态。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T12`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.compiled`
- 先创建文件枚举乱序、Map 插入乱序、SourceRef 行列变化、重复编译和时钟推进测试。
- 实现 SemanticDigestInput canonical JSON 编码，排除 DigestPair、metrics 和物理行列，包含版本域。
- 实现 MonotonicClock、CompilationTiming、SessionStateTransition 和 CompilationObserver 注入。
- Observer 异常转换为非 ERROR MIX-OBSERVER-FAILURE，并验证 status、context 和 digest 保持一致。

**Acceptance Criteria:**

- `AC-P1-T13-001` — 同语义输入在乱序、重复运行和 SourceRef 行列变化下必须生成精确一致的 semanticDigest。
- `AC-P1-T13-002` — deadline、cancel 和 Observer 异常必须生成稳定结果，Observer 失败不得改变原 status、context 或 digest。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dtest=SemanticDigestDeterminismTest,CompilationDeadlineTest,CompilationObserverTest test`

### TASK-P1-T14: 实现候选 Context 构造与原子发布

**Goal:** 完整构造 CompiledModelSet 和 EngineContext 后执行单次 CAS 原子暴露，任何构造或发布失败都保持旧 Context。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T13`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `dec.core.compiler.compiled.PublicationPass`
- 先创建发布成功、Diagnostic 阻断、Context 构造失败、publisher null/异常、CAS conflict、timeout 和 cancel 测试。
- 实现 SemanticDigestInput 冻结、DigestPair 计算、CompiledModelSetBuilder 和 EngineContext 候选构造。
- 实现 ContextPublisher 版本/CAS 契约，由 compileAndPublish 同一次调用协调单次发布。
- 验证失败候选不可见可回收、旧 Context 始终有效、成功结果只暴露完整不可变对象。

**Acceptance Criteria:**

- `AC-P1-T14-001` — 无 ERROR 的候选必须通过一次 CAS 原子发布并返回 PUBLISHED，外部只能观察完整 EngineContext。
- `AC-P1-T14-002` — 构造失败、publisher null/异常、CAS conflict、timeout、cancel 或任一 ERROR 必须返回 FAILED 且旧 Context 精确保持。

**Validation:**

- `./mvnw -pl dec-core-compiler,dec-core-context -am -Dtest=AtomicPublicationTest,PublicationFailureTest,ContextIsolationTest test`

### TASK-P1-T15: 接入 Starter、只读投影并退役旧 Declaration 模块

**Goal:** 将 XML/YAML Frontend、Compiler 和显式 Publisher 接入 Starter，完成真实 mix 端到端验证与 dec-expand-declaration 全量退役。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P1-T14`

**Implementation:**

- Module: `dec-core-starter`
- Component: `dec.core.starter.CompilerBootstrap`
- 先创建 Starter 单次 compileAndPublish、CoreConfigProjection 只读、真实 mix 端到端和旧模块残留扫描测试。
- 调整 Starter 依赖 compiler、XML/YAML Frontend 与 context，显式组装 Provider、Registry、Clock、Observer 和 Publisher。
- 实现 CoreConfigProjection 对旧 Data/View/Rule 读取面的只读映射，写操作必须失败且不建立双写。
- 删除 dec-expand-declaration 模块、父 POM/dependencyManagement、依赖、服务注册、反射字符串、文档和测试引用。
- 运行全 Reactor、41 个 TESTDESIGN-R01 Case、dependency tree、仓库残留扫描和双 MySQL 既有回归。

**Acceptance Criteria:**

- `AC-P1-T15-001` — Starter 必须在一次调用内完成真实 mix 编译和原子发布，CoreConfigProjection 读取一致且全部写尝试必须失败。
- `AC-P1-T15-002` — Reactor、dependency tree、服务注册、反射字符串、文档和测试中必须不存在 dec-expand-declaration 残留。
- `AC-P1-T15-003` — 全 Reactor、41 个 P1 Case 和既有核心/MySQL 回归必须通过且失败门禁保持有效。

**Validation:**

- `./mvnw clean verify`
- `./mvnw -pl dec-core-compiler,dec-core-starter,dec-demo -am -Dtest=*Compiler*,*Frontend*,*Context*,*Projection* test`
- `./mvnw -Pmysql-it -pl dec-demo -am verify`
- `! grep -R "dec-expand-declaration\|LegacyDeclarationAdapter" -n --exclude-dir=.git --exclude-dir=target .`

## Review Status

- `PlanReviewAgent`: **PASSED** — tasks: TASK-P1-T01, TASK-P1-T02, TASK-P1-T03, TASK-P1-T04, TASK-P1-T05, TASK-P1-T06, TASK-P1-T07, TASK-P1-T08, TASK-P1-T09, TASK-P1-T10, TASK-P1-T11, TASK-P1-T12, TASK-P1-T13, TASK-P1-T14, TASK-P1-T15
- `ArchitectureReviewAgent`: **PASSED** — tasks: TASK-P1-T01, TASK-P1-T02, TASK-P1-T03, TASK-P1-T04, TASK-P1-T05, TASK-P1-T06, TASK-P1-T07, TASK-P1-T08, TASK-P1-T09, TASK-P1-T10, TASK-P1-T11, TASK-P1-T12, TASK-P1-T13, TASK-P1-T14, TASK-P1-T15
- `TestDesignAgent`: **PASSED** — tasks: TASK-P1-T01, TASK-P1-T02, TASK-P1-T03, TASK-P1-T04, TASK-P1-T05, TASK-P1-T06, TASK-P1-T07, TASK-P1-T08, TASK-P1-T09, TASK-P1-T10, TASK-P1-T11, TASK-P1-T12, TASK-P1-T13, TASK-P1-T14, TASK-P1-T15
- `DevelopAgent`: **PASSED** — tasks: TASK-P1-T01, TASK-P1-T02, TASK-P1-T03, TASK-P1-T04, TASK-P1-T05, TASK-P1-T06, TASK-P1-T07, TASK-P1-T08, TASK-P1-T09, TASK-P1-T10, TASK-P1-T11, TASK-P1-T12, TASK-P1-T13, TASK-P1-T14, TASK-P1-T15
