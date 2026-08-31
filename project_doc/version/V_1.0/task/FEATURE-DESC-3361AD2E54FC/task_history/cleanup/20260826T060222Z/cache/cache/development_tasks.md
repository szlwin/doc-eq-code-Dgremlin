# FEATURE-DESC-3361AD2E54FC Development Task Plan

> 该文件由 `development_tasks.yaml` 确定性生成，仅用于阅读；YAML 是唯一事实源。

- Plan: `TP-FEATURE-DESC-3361AD2E54FC`
- Revision: `TP-FEATURE-DESC-3361AD2E54FC-R02@2e1280efa77b`
- Status: `PASSED`
- Execution: `SEQUENTIAL`
- Review round: `2`

## Input Revisions

- requirement_analysis: `REQAN-P2-R01@d08612768131`
- business_model: `BM-R20`
- design: `DESIGN-P2-R40`
- test_design: `TESTDESIGN-P2-R41`

## Tasks

### TASK-P2-SIMPLE-CONFIG-UNIFIED-LOAD: 统一 XML/YAML 候选配置加载入口

**Goal:** 通过 ConfigUtil 隐藏 ConfigInfo，统一解析 XML/YAML 到独立候选对象，并保持解析失败时当前配置不变。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** None

**Implementation:**

- Module: `dec-core-starter / dec-context-config-parse-xml / dec-context-config-parse-yaml`
- Component: `ConfigUtil.parseConfigInfo and parser parseInto`
- Decision: `IMPL-DEC-P2-R40-006`
- Strategy: `COMPATIBLE_EXTEND`
- Shared logic: `REUSE_SHARED` owned by `ConfigUtil`
- Decision source: `project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md#implementation-strategy-decisions`
- Decision source: `project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md#CASE-P2-R41-UNIFIED-LOAD-001`
- Compatibility verification: 保留单参数 XML 调用签名；XML 与旧 YAML 均通过同一门面加载。
- Compatibility verification: 候选解析失败不得替换已安装 ConfigInfo。
- 按扩展名选择 XML/YAML parser，所有 parser 只写调用方候选 ConfigInfo。
- 通过 ConfigUtil 执行串行候选加载，成功后交给后续编译或兼容安装。
- 验证公开入口不返回 ConfigInfo，旧独立 YAML 门面不形成第二套业务入口。

**Acceptance Criteria:**

- `AC-P2-SIMPLE-UNIFIED-LOAD` — XML、旧 YAML 和 malformed input 均经 ConfigUtil 统一处理；失败时已安装 ConfigInfo 与 EngineContext 对象身份保持不变。

**Validation:**

- `./mvnw -pl dec-demo -Dtest=ConfigUtilCompatibilityTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-SIMPLE-CONFIG-COMPILER-INSTALL: 完成现代 XML 编译与原子配置安装

**Goal:** 将现代 XML 候选编译为 EngineContext，并只在编译发布成功后由 ConfigManager 整体安装。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-SIMPLE-CONFIG-UNIFIED-LOAD`

**Implementation:**

- Module: `dec-core-context / dec-core-compiler / dec-core-starter`
- Component: `ConfigInfo, ConfigManager, CompilerStarter`
- Decision: `IMPL-DEC-P2-R37-004`
- Strategy: `MODIFY`
- Shared logic: `REUSE_SHARED` owned by `ConfigManager`
- Decision source: `project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md#implementation-strategy-decisions`
- Decision source: `project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md#CASE-P2-R41-ATOMIC-COMPAT-003`
- Compatibility verification: EngineContext 与 ConfigInfo 使用同一候选对象绑定；旧配置失败时保持原对象身份。
- Compatibility verification: ConfigManager 不发布半成品或仅替换 Context 的中间状态。
- 编译候选 XML 的 System/Business Source Graph 并绑定返回的同一 EngineContext。
- 通过 ConfigManager.install 原子替换 ConfigInfo；编译或发布失败丢弃候选。
- 覆盖多个 system-file、顺序无关、重复定义和前向引用。

**Acceptance Criteria:**

- `AC-P2-SIMPLE-ATOMIC-INSTALL` — 现代 XML 编译成功才整体安装候选 ConfigInfo+EngineContext；编译失败、重复定义和前向引用错误均保留旧对象身份。

**Validation:**

- `./mvnw -pl dec-demo -Dtest=MixTest -Dsurefire.failIfNoSpecifiedTests=true test`
- `./mvnw -pl dec-core-starter -Dtest=CompilerStarterBehaviorT15Test -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-SIMPLE-YAML-BOUNDARY: 收口旧 YAML 兼容与 P8 现代声明边界

**Goal:** 让 YAML 复用统一入口，保留旧格式兼容加载，并在 P2 明确拒绝 System/Business 现代声明而不安装部分配置。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-SIMPLE-CONFIG-UNIFIED-LOAD`

**Implementation:**

- Module: `dec-context-config-parse-yaml / dec-core-starter`
- Component: `YamlConfigFileParser and ConfigUtil YAML dispatch`
- Decision: `IMPL-DEC-P2-R40-007`
- Strategy: `MODIFY`
- Shared logic: `REUSE_SHARED` owned by `dec-context-config-parse-yaml`
- Decision source: `project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md#implementation-strategy-decisions`
- Decision source: `project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md#CASE-P2-R41-YAML-BOUNDARY-004`
- Compatibility verification: legacy YAML 继续加载；现代 YAML System/Business 在安装前稳定失败并指向 P8。
- Compatibility verification: 不保留独立公开 ConfigInfo 返回入口，不在 Starter 复制第二套 YAML compiler。
- 在 YAML parser 中检测 system-file-info/business-file-info 等现代声明。
- 检测到现代声明时在 install 前抛出稳定边界异常，current ConfigInfo 与 EngineContext 不变。
- 保留旧 YAML 候选解析，并以 ConfigUtil 作为唯一公开加载门面。

**Acceptance Criteria:**

- `AC-P2-SIMPLE-YAML-BOUNDARY` — 旧 YAML 通过 ConfigUtil 成功加载；现代 YAML System/Business 在安装前明确失败，旧 ConfigInfo 与 EngineContext 身份不变。

**Validation:**

- `./mvnw -pl dec-demo -Dtest=ConfigUtilCompatibilityTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-SIMPLE-MODEL-EXECUTION: 验证直接 ModelContainer 业务执行与事务收尾

**Goal:** 沿用 DataUtil、ModelLoader 和 ModelContainer 的简单业务链，验证缺失定义失败、提交回滚和资源关闭。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-SIMPLE-CONFIG-COMPILER-INSTALL`

**Implementation:**

- Module: `dec-core-context / dec-core-model / dec-demo`
- Component: `DataUtil, ModelLoader, ModelContainer`
- Decision: `IMPL-DEC-P2-R37-001`
- Strategy: `REUSE`
- Shared logic: `REUSE_SHARED` owned by `dec-core-model`
- Decision source: `project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md#implementation-strategy-decisions`
- Decision source: `project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md#CASE-P2-R41-SIMPLE-EXECUTE-004`
- Compatibility verification: 业务调用方不传递 ConfigInfo、EngineContext 或权限证明。
- Compatibility verification: 成功提交、失败回滚和 finally close 保持既有 RuleTests 调用方式。
- 从已安装配置创建 ModelData 并通过 ModelLoader 加载定义。
- 调用 ModelContainer.execute 覆盖成功提交、定义缺失、业务异常回滚和连接关闭。
- 确认配置发布与数据库事务不混合，且无旧 runtime/access 入口残留。

**Acceptance Criteria:**

- `AC-P2-SIMPLE-MODEL-EXECUTE` — 简单业务链可执行；缺失定义在数据库副作用前失败，成功提交、失败回滚和所有连接关闭均可观察。

**Validation:**

- `./mvnw -pl dec-core-model -Dtest=ModelContainerLifecycleTest -Dsurefire.failIfNoSpecifiedTests=true test`
- `./mvnw -pl dec-demo -Dtest=DirectModelDataContainerDemoTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-SIMPLE-REGRESSION-CLOSURE: 完成 P2 简化模型回归、Java 8 与退役目录闭环

**Goal:** 冻结 DEV-P2-SIMPLE-R43 的验证结果，完成针对性回归、Java 8 门禁、退役目录扫描和追踪证据闭环。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-SIMPLE-YAML-BOUNDARY`, `TASK-P2-SIMPLE-MODEL-EXECUTION`

**Implementation:**

- Module: `all P2 modules / project_doc`
- Component: `P2 regression and traceability closure`
- Decision: `IMPL-DEC-P2-R40-006`
- Strategy: `MODIFY`
- Shared logic: `KEEP_LOCAL` owned by `common-develop document-facts`
- Decision source: `project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md#implementation-strategy-decisions`
- Decision source: `project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/test_case.md#CASE-P2-R41-JAVA8-RETIRE-005`
- Compatibility verification: 全部 Evidence、Review 和测试绑定 exact DEV-P2-SIMPLE-R43@a5ecf75d5169。
- Compatibility verification: maven.compiler.release=8，代表 class major version=52，退役 runtime/access 目录无源码。
- 运行针对性 ConfigUtil、Compiler、ModelContainer 和 demo 测试，登记命令 Evidence。
- 执行完整 Maven verify、Java 8 release/help:evaluate、javap major-version 和退役目录检查。
- 重建并校验 artifact relations/traceability，提交当前 revision 的独立 Review 输入。

**Acceptance Criteria:**

- `AC-P2-SIMPLE-REGRESSION` — 针对性与完整 Maven 测试、Java 8 编译门禁、退役目录扫描和追踪校验均绑定 DEV-P2-SIMPLE-R43，且失败不会被表述为通过。

**Validation:**

- `./mvnw --batch-mode --no-transfer-progress clean verify`
- `./mvnw help:evaluate -Dexpression=maven.compiler.release -q -DforceStdout`
- `./mvnw -q -DskipTests test-compile`
- `python3 /Users/shazhoulin/.agents/skills/common-develop/scripts/artifact_relations.py validate -g ProjectManagerAgent --doc-root project_doc --json`

## Review Status

- `PlanReviewAgent`: **PASSED** — tasks: TASK-P2-SIMPLE-CONFIG-UNIFIED-LOAD, TASK-P2-SIMPLE-CONFIG-COMPILER-INSTALL, TASK-P2-SIMPLE-YAML-BOUNDARY, TASK-P2-SIMPLE-MODEL-EXECUTION, TASK-P2-SIMPLE-REGRESSION-CLOSURE
- `ArchitectureReviewAgent`: **PASSED** — tasks: TASK-P2-SIMPLE-CONFIG-UNIFIED-LOAD, TASK-P2-SIMPLE-CONFIG-COMPILER-INSTALL, TASK-P2-SIMPLE-YAML-BOUNDARY, TASK-P2-SIMPLE-MODEL-EXECUTION, TASK-P2-SIMPLE-REGRESSION-CLOSURE
- `TestDesignAgent`: **PASSED** — tasks: TASK-P2-SIMPLE-CONFIG-UNIFIED-LOAD, TASK-P2-SIMPLE-CONFIG-COMPILER-INSTALL, TASK-P2-SIMPLE-YAML-BOUNDARY, TASK-P2-SIMPLE-MODEL-EXECUTION, TASK-P2-SIMPLE-REGRESSION-CLOSURE
- `DevelopAgent`: **PASSED** — tasks: TASK-P2-SIMPLE-CONFIG-UNIFIED-LOAD, TASK-P2-SIMPLE-CONFIG-COMPILER-INSTALL, TASK-P2-SIMPLE-YAML-BOUNDARY, TASK-P2-SIMPLE-MODEL-EXECUTION, TASK-P2-SIMPLE-REGRESSION-CLOSURE
