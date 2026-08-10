# FEATURE-DESC-3361AD2E54FC Development Task Plan

> 该文件由 `development_tasks.yaml` 确定性生成，仅用于阅读；YAML 是唯一事实源。

- Plan: `TP-FEATURE-DESC-3361AD2E54FC`
- Revision: `TP-FEATURE-DESC-3361AD2E54FC-R02@ff0f7abd971c`
- Status: `PASSED`
- Execution: `SEQUENTIAL`
- Review round: `2`

## Input Revisions

- requirement_analysis: `REQAN-P2-R01@d08612768131`
- business_model: `BM-R20`
- design: `DESIGN-P2-R30`
- test_design: `TESTDESIGN-P2-R31`

## Tasks

### TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION: System 与 RuleView 复合身份编译闭环

**Goal:** 让 System 显式身份和 RuleView (system,name) 复合身份在编译注册、解析和确定性诊断中形成同一可发布事实。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** None

**Implementation:**

- Module: `dec-core-compiler`
- Component: `system/ruleview symbol compilation`
- 先以 SystemCompilationContractTest 与 RuleViewCompilationContractTest 建立有效 RED，覆盖多源顺序、重复 System、前向引用、缺失 System 与跨 System 同名隔离。
- 进入 development 后先为 dec-core-compiler/system/ruleview symbol compilation 提交 ARCHITECTURE_SKELETON：真实声明方法/类型契约、顶层调用顺序、主要分支、失败与副作用边界；具体业务算法、数据读写和外部副作用保持显式未实现，不得伪造成功。
- 同一 skeleton revision 必须依次由 ArchitectureReviewAgent 与 SpecComplianceReviewAgent 独立 PASSED；未双通过前禁止具体实现。双通过后仅由 ProjectManagerAgent 执行 long_task.py advance-development-step，进入 CONCRETE_IMPLEMENTATION。
- 复用现有 raw/symbol/pass 编译管线和 CONTEXT 的 SystemKey/RuleViewKey，完成显式 owner 注册、复合身份解析与 source-aware 冲突诊断。
- 保持编译输入顺序无关和不可变快照，运行两组 TestClass 及 compiler 模块回归。
- 具体实现只补齐已通过 skeleton 的方法内部逻辑并完成 GREEN→REFACTOR；若证明 skeleton 边界不成立，停止并从 development reopen 回 SKELETON，不得偷偷改已冻结契约。
- 具体实现 finalize 后进入 code_review，至少由 SpecComplianceReviewAgent、EngineeringStandardsReviewAgent、ArchitectureReviewAgent 完成第二轮 Review；ArchitectureReviewAgent 必须核对实现未偏离已通过 skeleton。

**Acceptance Criteria:**

- `AC-P2-SYSTEM-RULEVIEW-001` — 多文件与前向引用能够得到确定一致的 System 身份集合，重复或冲突 System 必须失败且不得发布。
- `AC-P2-SYSTEM-RULEVIEW-002` — 不同 System 下同名 RuleView 能够同时存在并精确解析，同 System 同名或缺失 System 必须拒绝。

**Validation:**

- `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- `./mvnw -pl dec-core-compiler -Dtest=SystemCompilationContractTest,RuleViewCompilationContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-DEV-04-CONTEXT-MATERIALIZATION: Context 物化聚合与中立运行契约

**Goal:** 先在 CONTEXT 建立 TargetKey/ModelPath/ModelAccessRuleKey、物化聚合与中立运行契约，使 compiler、MODEL、STARTER 只消费捕获的不可变 Context。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION`

**Implementation:**

- Module: `dec-core-context`
- Component: `compiled aggregate and runtime neutral contracts`
- 以 ProtectedAccessContextApiContractTest、P2CompilerContextConstructibilityContractTest、RuntimeFactValueContractTest、OpaqueRuntimeIdContractTest 建立 RED。
- 进入 development 后先为 dec-core-context/compiled aggregate and runtime neutral contracts 提交 ARCHITECTURE_SKELETON：真实声明方法/类型契约、顶层调用顺序、主要分支、失败与副作用边界；具体业务算法、数据读写和外部副作用保持显式未实现，不得伪造成功。
- 同一 skeleton revision 必须依次由 ArchitectureReviewAgent 与 SpecComplianceReviewAgent 独立 PASSED；未双通过前禁止具体实现。双通过后仅由 ProjectManagerAgent 执行 long_task.py advance-development-step，进入 CONCRETE_IMPLEMENTATION。
- 实现 API Contract 定义的 materialization plan/index，并把它作为 CompiledModelSet 强制成员参与 equality/hash/digest；EngineContext 只代理捕获聚合。
- 实现 RuntimeFactValue、opaque runtime IDs、ModelAccessRuleKey/TargetKey/ModelPath 等中立不可变类型的精确值语义，并运行 CONTEXT/COMPILER 构造性测试。
- 具体实现只补齐已通过 skeleton 的方法内部逻辑并完成 GREEN→REFACTOR；若证明 skeleton 边界不成立，停止并从 development reopen 回 SKELETON，不得偷偷改已冻结契约。
- 具体实现 finalize 后进入 code_review，至少由 SpecComplianceReviewAgent、EngineeringStandardsReviewAgent、ArchitectureReviewAgent 完成第二轮 Review；ArchitectureReviewAgent 必须核对实现未偏离已通过 skeleton。

**Acceptance Criteria:**

- `AC-P2-SYSTEM-RULEVIEW-008` — CompiledViewMaterializationIndex 必须随 CompiledModelSet 原子发布并参与值语义，EngineContext 精确暴露捕获聚合。
- `AC-P2-SYSTEM-RULEVIEW-004` — 受保护访问的 key、ID、value/result 类型必须保持精确、不可变且大小写敏感。

**Validation:**

- `./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install`
- `./mvnw -pl dec-core-context -Dtest=ProtectedAccessContextApiContractTest,RuntimeFactValueContractTest,OpaqueRuntimeIdContractTest -Dsurefire.failIfNoSpecifiedTests=true test`
- `./mvnw -pl dec-core-compiler -Dtest=P2CompilerContextConstructibilityContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-DEV-02-RULEVIEW-REFERENCE: RuleView 完整引用与目标解析

**Goal:** 让新调用路径只通过完整 System + RuleView 身份解析，并把未知 System、未知 RuleView 和目标类型错误稳定地阻断在编译期。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION`, `TASK-P2-DEV-04-CONTEXT-MATERIALIZATION`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `reference resolution`
- 以 RuleViewCompilationContractTest 和 TargetKeyModelPathContractTest 中完整引用、缺失 owner 与 unknown target 场景建立 RED。
- 进入 development 后先为 dec-core-compiler/reference resolution 提交 ARCHITECTURE_SKELETON：真实声明方法/类型契约、顶层调用顺序、主要分支、失败与副作用边界；具体业务算法、数据读写和外部副作用保持显式未实现，不得伪造成功。
- 同一 skeleton revision 必须依次由 ArchitectureReviewAgent 与 SpecComplianceReviewAgent 独立 PASSED；未双通过前禁止具体实现。双通过后仅由 ProjectManagerAgent 执行 long_task.py advance-development-step，进入 CONCRETE_IMPLEMENTATION。
- 在现有 symbol/reference resolver 上强制复合 RuleViewKey，并将目标解析结果绑定稳定 SourceRef/Diagnostic。
- 运行目标 TestClass 与 compiler reference/symbol 回归，确认无 bare-name Adapter 写入新 Registry。
- 具体实现只补齐已通过 skeleton 的方法内部逻辑并完成 GREEN→REFACTOR；若证明 skeleton 边界不成立，停止并从 development reopen 回 SKELETON，不得偷偷改已冻结契约。
- 具体实现 finalize 后进入 code_review，至少由 SpecComplianceReviewAgent、EngineeringStandardsReviewAgent、ArchitectureReviewAgent 完成第二轮 Review；ArchitectureReviewAgent 必须核对实现未偏离已通过 skeleton。

**Acceptance Criteria:**

- `AC-P2-SYSTEM-RULEVIEW-003` — system-ref + rule-ref 能够精确解析；未知或归属不一致引用必须失败并包含来源定位。

**Validation:**

- `./mvnw -pl dec-core-compiler -Dtest=RuleViewCompilationContractTest,TargetKeyModelPathContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-DEV-03-MODEL-ACCESS-POLICY: model-access 路径、权限与原子发布

**Goal:** 把 TargetKey、ModelPath、READ/WRITE ModelAccessRuleKey、静态分类与完整候选发布绑定为一个 fail-closed 编译切片。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-DEV-02-RULEVIEW-REFERENCE`, `TASK-P2-DEV-04-CONTEXT-MATERIALIZATION`

**Implementation:**

- Module: `dec-core-compiler`
- Component: `modelaccess compilation and publication`
- 以 TargetKeyModelPathContractTest、ModelAccessPolicyContractTest、AtomicPublicationContractTest、P2DiagnosticDeterminismTest 建立目标 RED。
- 进入 development 后先为 dec-core-compiler/modelaccess compilation and publication 提交 ARCHITECTURE_SKELETON：真实声明方法/类型契约、顶层调用顺序、主要分支、失败与副作用边界；具体业务算法、数据读写和外部副作用保持显式未实现，不得伪造成功。
- 同一 skeleton revision 必须依次由 ArchitectureReviewAgent 与 SpecComplianceReviewAgent 独立 PASSED；未双通过前禁止具体实现。双通过后仅由 ProjectManagerAgent 执行 long_task.py advance-development-step，进入 CONCRETE_IMPLEMENTATION。
- 扩展现有 modelaccess 编译器，统一规范化路径、READ/WRITE 独立权限、STATIC_DENY/STATIC_ALLOW/RUNTIME_GUARD_REQUIRED 分类和 exact binding。
- 将 policy/binding/materialization 完整性接入既有 publication gate，任一 ERROR 保留旧 EngineContext 并保持诊断排序确定。
- 具体实现只补齐已通过 skeleton 的方法内部逻辑并完成 GREEN→REFACTOR；若证明 skeleton 边界不成立，停止并从 development reopen 回 SKELETON，不得偷偷改已冻结契约。
- 具体实现 finalize 后进入 code_review，至少由 SpecComplianceReviewAgent、EngineeringStandardsReviewAgent、ArchitectureReviewAgent 完成第二轮 Review；ArchitectureReviewAgent 必须核对实现未偏离已通过 skeleton。

**Acceptance Criteria:**

- `AC-P2-SYSTEM-RULEVIEW-004` — READ 与 WRITE 权限必须独立精确判定，未声明共享 WRITE 必须拒绝。
- `AC-P2-SYSTEM-RULEVIEW-005` — 模型路径必须规范化且非法或越界路径必须失败，动态路径只能形成运行时 Guard 约束。
- `AC-P2-SYSTEM-RULEVIEW-008` — 候选存在错误或缺失绑定/物化描述时必须保持旧 Context，不得出现部分发布。
- `AC-P2-SYSTEM-RULEVIEW-009` — 等价静态失败必须生成稳定且不泄露敏感值的诊断。

**Validation:**

- `./mvnw -pl dec-core-compiler -Dtest=TargetKeyModelPathContractTest,ModelAccessPolicyContractTest,AtomicPublicationContractTest,P2DiagnosticDeterminismTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-DEV-05-MODEL-TRUSTED-LOAD: MODEL trusted load 与 Scope 生产边界

**Goal:** 让 MODEL 通过 captured EngineContext、真实 origin 和 MODEL 自建 Container 完成精确物化，并只在成功后 mint 同一 ModelData 的 Handle/Scope。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-DEV-04-CONTEXT-MATERIALIZATION`

**Implementation:**

- Module: `dec-core-model`
- Component: `runtime model execution root`
- 以 ProtectedAccessModelApiContractTest 与 RuntimeModelMaterializationIntegrationTest 建立 direct-load RED，覆盖 L01-L07 失败矩阵、same-ModelData identity 和 production Container 边界。
- 进入 development 后先为 dec-core-model/runtime model execution root 提交 ARCHITECTURE_SKELETON：真实声明方法/类型契约、顶层调用顺序、主要分支、失败与副作用边界；具体业务算法、数据读写和外部副作用保持显式未实现，不得伪造成功。
- 同一 skeleton revision 必须依次由 ArchitectureReviewAgent 与 SpecComplianceReviewAgent 独立 PASSED；未双通过前禁止具体实现。双通过后仅由 ProjectManagerAgent 执行 long_task.py advance-development-step，进入 CONCRETE_IMPLEMENTATION。
- 实现 RuntimeModelLoadRequest/Result、ExecutionRoot/Roots、ProductionContainerKind 和 Scope 生产；严格按 L01-L07 顺序使用 typed ModelDataFactory、3-arg ModelLoader 与 ContainerFactory。
- 确认所有 pre-scope failure 的 handle/scope/Guard/effect 计数为零，并运行 MODEL 模块目标测试。
- 具体实现只补齐已通过 skeleton 的方法内部逻辑并完成 GREEN→REFACTOR；若证明 skeleton 边界不成立，停止并从 development reopen 回 SKELETON，不得偷偷改已冻结契约。
- 具体实现 finalize 后进入 code_review，至少由 SpecComplianceReviewAgent、EngineeringStandardsReviewAgent、ArchitectureReviewAgent 完成第二轮 Review；ArchitectureReviewAgent 必须核对实现未偏离已通过 skeleton。

**Acceptance Criteria:**

- `AC-P2-SYSTEM-RULEVIEW-006` — 合法动态目标必须由捕获 Context 精确物化；错误 plan/descriptor/origin/container 必须在 Scope 前失败。
- `AC-P2-SYSTEM-RULEVIEW-007` — 生产加载必须冻结同一个真实 ModelData 到 Handle/Scope，不得发生 A-load/B-handle 替换。

**Validation:**

- `./mvnw -pl dec-core-model -am -Dmaven.test.skip=true install`
- `./mvnw -pl dec-core-model -Dtest=ProtectedAccessModelApiContractTest,RuntimeModelMaterializationIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-DEV-06-MODEL-SESSION-EFFECT: MODEL Session、Locator 与 EffectProvider 完整性

**Goal:** 让 Scope 只能创建同源 Session，精确注册 trusted Handle、seal 后绑定 EffectProvider，并由私有 operation port 在 effect 前复核 session/object/handle。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-DEV-05-MODEL-TRUSTED-LOAD`

**Implementation:**

- Module: `dec-core-model`
- Component: `runtime session locator effect provider`
- 以 RuntimeObjectLocatorIntegrationTest、ProtectedWriteTransactionIntegrationTest 和 ProtectedAccessModelApiContractTest 的 session/effect algebra 建立 RED。
- 进入 development 后先为 dec-core-model/runtime session locator effect provider 提交 ARCHITECTURE_SKELETON：真实声明方法/类型契约、顶层调用顺序、主要分支、失败与副作用边界；具体业务算法、数据读写和外部副作用保持显式未实现，不得伪造成功。
- 同一 skeleton revision 必须依次由 ArchitectureReviewAgent 与 SpecComplianceReviewAgent 独立 PASSED；未双通过前禁止具体实现。双通过后仅由 ProjectManagerAgent 执行 long_task.py advance-development-step，进入 CONCRETE_IMPLEMENTATION。
- 实现 session register/seal/locate/currentVersion、ownership/stale/duplicate 错误，以及同 scope sealed-session EffectProvider 绑定。
- 实现 MODEL 私有 read/write port 的 same-object 校验和写失败无 receipt 语义；不扩大 legacy post-copy rollback 范围。
- 具体实现只补齐已通过 skeleton 的方法内部逻辑并完成 GREEN→REFACTOR；若证明 skeleton 边界不成立，停止并从 development reopen 回 SKELETON，不得偷偷改已冻结契约。
- 具体实现 finalize 后进入 code_review，至少由 SpecComplianceReviewAgent、EngineeringStandardsReviewAgent、ArchitectureReviewAgent 完成第二轮 Review；ArchitectureReviewAgent 必须核对实现未偏离已通过 skeleton。

**Acceptance Criteria:**

- `AC-P2-SYSTEM-RULEVIEW-006` — 运行时对象只能从同一 sealed session 的已注册 Handle 精确定位，missing/stale/ownership 冲突必须拒绝。
- `AC-P2-SYSTEM-RULEVIEW-009` — MODEL operation 失败必须返回稳定拒绝且无成功 receipt，诊断不得泄露 ModelData/origin 身份。

**Validation:**

- `./mvnw -pl dec-core-model -Dtest=ProtectedAccessModelApiContractTest,RuntimeObjectLocatorIntegrationTest,ProtectedWriteTransactionIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-DEV-07-STARTER-GUARDED-ACCESS: STARTER 目标解析、Intent、Capability 与 Guard

**Goal:** 在 STARTER 中把 exact runtime target、READ/WRITE intent、one-shot capability 与 ModelAccessRuleKey Guard 串成 Guard-before-effect 的同一证明链。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-DEV-03-MODEL-ACCESS-POLICY`, `TASK-P2-DEV-06-MODEL-SESSION-EFFECT`

**Implementation:**

- Module: `dec-core-starter`
- Component: `protected access resolver and guard`
- 以 ProtectedAccessStarterApiContractTest、ProtectedWriteIntentResolutionTest、ProtectedRuntimeModelAdapterIntegrationTest 建立 RED。
- 进入 development 后先为 dec-core-starter/protected access resolver and guard 提交 ARCHITECTURE_SKELETON：真实声明方法/类型契约、顶层调用顺序、主要分支、失败与副作用边界；具体业务算法、数据读写和外部副作用保持显式未实现，不得伪造成功。
- 同一 skeleton revision 必须依次由 ArchitectureReviewAgent 与 SpecComplianceReviewAgent 独立 PASSED；未双通过前禁止具体实现。双通过后仅由 ProjectManagerAgent 执行 long_task.py advance-development-step，进入 CONCRETE_IMPLEMENTATION。
- 实现 RuntimeTargetResolver、write-intent 0/1/N 解析、mutation stamp freeze、one-shot capability 和 exact ModelAccessRuleKey Guard 判定。
- 将 Guard ALLOW 仅委托给 composition 私有的同 session operation port；DENY、stale 或 A->B substitution 全部 effect=0。
- 具体实现只补齐已通过 skeleton 的方法内部逻辑并完成 GREEN→REFACTOR；若证明 skeleton 边界不成立，停止并从 development reopen 回 SKELETON，不得偷偷改已冻结契约。
- 具体实现 finalize 后进入 code_review，至少由 SpecComplianceReviewAgent、EngineeringStandardsReviewAgent、ArchitectureReviewAgent 完成第二轮 Review；ArchitectureReviewAgent 必须核对实现未偏离已通过 skeleton。

**Acceptance Criteria:**

- `AC-P2-SYSTEM-RULEVIEW-004` — READ/WRITE Guard 必须只依据 exact ModelAccessRuleKey，权限类型不得互相隐含。
- `AC-P2-SYSTEM-RULEVIEW-005` — WRITE intent 必须冻结一个 exact target/path/version；0 或 N 个 intent 必须拒绝。
- `AC-P2-SYSTEM-RULEVIEW-006` — Guard DENY 或 stale proof 必须在真实模型副作用前返回。

**Validation:**

- `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- `./mvnw -pl dec-core-starter -Dtest=ProtectedAccessStarterApiContractTest,ProtectedWriteIntentResolutionTest,ProtectedRuntimeModelAdapterIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-DEV-08-PRODUCTION-COMPOSITION-CONCURRENCY: 生产 Composition、消费者边界与并发闭环

**Goal:** 让 Rule/Change/CustomAction 只经 STARTER protected entries 使用同一 Scope/Session/Guard/effect 组合，并在并发 capability/ownership 冲突下最多产生一次合法效果。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-DEV-07-STARTER-GUARDED-ACCESS`

**Implementation:**

- Module: `dec-core-starter`
- Component: `production composition and concurrency coordination`
- 以 ProtectedAccessProductionCompositionTest、ProtectedAccessConcurrencyTest、ProtectedAccessDependencyDirectionTest 建立 RED。
- 进入 development 后先为 dec-core-starter/production composition and concurrency coordination 提交 ARCHITECTURE_SKELETON：真实声明方法/类型契约、顶层调用顺序、主要分支、失败与副作用边界；具体业务算法、数据读写和外部副作用保持显式未实现，不得伪造成功。
- 同一 skeleton revision 必须依次由 ArchitectureReviewAgent 与 SpecComplianceReviewAgent 独立 PASSED；未双通过前禁止具体实现。双通过后仅由 ProjectManagerAgent 执行 long_task.py advance-development-step，进入 CONCRETE_IMPLEMENTATION。
- 实现 ProtectedAccessRuntimeFactory/Composition、Rule/Change/CustomAction entry parity、same-scope session/effect binding 和 close/stale 语义。
- 建立 capability one-shot 与同 Handle/path/version 协调域；用结构测试禁止 consumer 直连 MODEL root/effect/ModelData。
- 具体实现只补齐已通过 skeleton 的方法内部逻辑并完成 GREEN→REFACTOR；若证明 skeleton 边界不成立，停止并从 development reopen 回 SKELETON，不得偷偷改已冻结契约。
- 具体实现 finalize 后进入 code_review，至少由 SpecComplianceReviewAgent、EngineeringStandardsReviewAgent、ArchitectureReviewAgent 完成第二轮 Review；ArchitectureReviewAgent 必须核对实现未偏离已通过 skeleton。

**Acceptance Criteria:**

- `AC-P2-SYSTEM-RULEVIEW-007` — Rule、Change、CustomAction 必须使用同一 guarded composition，DENY 为 effect0，ALLOW 作用于同一 bound Handle。
- `AC-P2-SYSTEM-RULEVIEW-008` — 并发 capability/ownership 冲突必须保持 Context/Session 隔离且最多一次合法写入。

**Validation:**

- `./mvnw -pl dec-core-starter -Dtest=ProtectedAccessProductionCompositionTest,ProtectedAccessConcurrencyTest,ProtectedAccessDependencyDirectionTest -Dsurefire.failIfNoSpecifiedTests=true test`

### TASK-P2-DEV-09-REAL-FIXTURE-COMPATIBILITY: 真实 fixture 端到端与 declaration 兼容边界

**Goal:** 用真实 systems.xml 与 originData 跑通 compile -> Context -> MODEL load -> STARTER Guard -> READ/WRITE，并证明 declaration 只保留 P2 兼容边界。

**Owner:** `DevelopAgent`

**Reviewers:** `PlanReviewAgent`, `ArchitectureReviewAgent`, `TestDesignAgent`, `DevelopAgent`

**Depends on:** `TASK-P2-DEV-03-MODEL-ACCESS-POLICY`, `TASK-P2-DEV-08-PRODUCTION-COMPOSITION-CONCURRENCY`

**Implementation:**

- Module: `dec-demo`
- Component: `P2 real fixture integration and compatibility`
- 以 P2RealFixtureIntegrationTest、P2DeclarationCompatibilityContractTest、P2RevisionDependencyDagContractTest 建立最终 RED/回归基线。
- 进入 development 后先为 dec-demo/P2 real fixture integration and compatibility 提交 ARCHITECTURE_SKELETON：真实声明方法/类型契约、顶层调用顺序、主要分支、失败与副作用边界；具体业务算法、数据读写和外部副作用保持显式未实现，不得伪造成功。
- 同一 skeleton revision 必须依次由 ArchitectureReviewAgent 与 SpecComplianceReviewAgent 独立 PASSED；未双通过前禁止具体实现。双通过后仅由 ProjectManagerAgent 执行 long_task.py advance-development-step，进入 CONCRETE_IMPLEMENTATION。
- 使用真实 mix/system/systems.xml 和当前 RuleView/model-access 配置验证 source-order determinism、dynamic classifier、exact binding、real Container、same ModelData effect 与成功 write-back。
- 运行 23 个 R31 TestClass 的全清单和相关 reactor 测试，确认 R29 token API 不存在、declaration runtime 未删除、P3-P7 语义未被拉入。
- 具体实现只补齐已通过 skeleton 的方法内部逻辑并完成 GREEN→REFACTOR；若证明 skeleton 边界不成立，停止并从 development reopen 回 SKELETON，不得偷偷改已冻结契约。
- 具体实现 finalize 后进入 code_review，至少由 SpecComplianceReviewAgent、EngineeringStandardsReviewAgent、ArchitectureReviewAgent 完成第二轮 Review；ArchitectureReviewAgent 必须核对实现未偏离已通过 skeleton。

**Acceptance Criteria:**

- `AC-P2-SYSTEM-RULEVIEW-001` — 真实 systems.xml 能够通过统一加载/编译路径形成稳定不可变结果。
- `AC-P2-SYSTEM-RULEVIEW-006` — 真实动态路径必须分类为运行时 Guard 并在同一 trusted runtime object 上执行。
- `AC-P2-SYSTEM-RULEVIEW-007` — 真实生产路径能够经 real Container 和 STARTER guarded composition 完成 READ/WRITE 且无 bypass。
- `AC-P2-SYSTEM-RULEVIEW-010` — declaration 旧入口必须保持兼容边界且 P2 不提前执行 P7 删除。

**Validation:**

- `./mvnw -pl dec-demo -am -Dmaven.test.skip=true install`
- `./mvnw -pl dec-demo -Dtest=P2RealFixtureIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`
- `./mvnw -pl dec-core-compiler -Dtest=P2DeclarationCompatibilityContractTest,P2RevisionDependencyDagContractTest -Dsurefire.failIfNoSpecifiedTests=true test`
- `./mvnw -pl dec-core-compiler,dec-core-context,dec-core-model,dec-core-starter,dec-demo test`

## Review Status

- `PlanReviewAgent`: **PASSED** — tasks: TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION, TASK-P2-DEV-04-CONTEXT-MATERIALIZATION, TASK-P2-DEV-02-RULEVIEW-REFERENCE, TASK-P2-DEV-03-MODEL-ACCESS-POLICY, TASK-P2-DEV-05-MODEL-TRUSTED-LOAD, TASK-P2-DEV-06-MODEL-SESSION-EFFECT, TASK-P2-DEV-07-STARTER-GUARDED-ACCESS, TASK-P2-DEV-08-PRODUCTION-COMPOSITION-CONCURRENCY, TASK-P2-DEV-09-REAL-FIXTURE-COMPATIBILITY
- `ArchitectureReviewAgent`: **PASSED** — tasks: TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION, TASK-P2-DEV-04-CONTEXT-MATERIALIZATION, TASK-P2-DEV-02-RULEVIEW-REFERENCE, TASK-P2-DEV-03-MODEL-ACCESS-POLICY, TASK-P2-DEV-05-MODEL-TRUSTED-LOAD, TASK-P2-DEV-06-MODEL-SESSION-EFFECT, TASK-P2-DEV-07-STARTER-GUARDED-ACCESS, TASK-P2-DEV-08-PRODUCTION-COMPOSITION-CONCURRENCY, TASK-P2-DEV-09-REAL-FIXTURE-COMPATIBILITY
- `TestDesignAgent`: **PASSED** — tasks: TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION, TASK-P2-DEV-04-CONTEXT-MATERIALIZATION, TASK-P2-DEV-02-RULEVIEW-REFERENCE, TASK-P2-DEV-03-MODEL-ACCESS-POLICY, TASK-P2-DEV-05-MODEL-TRUSTED-LOAD, TASK-P2-DEV-06-MODEL-SESSION-EFFECT, TASK-P2-DEV-07-STARTER-GUARDED-ACCESS, TASK-P2-DEV-08-PRODUCTION-COMPOSITION-CONCURRENCY, TASK-P2-DEV-09-REAL-FIXTURE-COMPATIBILITY
- `DevelopAgent`: **PASSED** — tasks: TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION, TASK-P2-DEV-04-CONTEXT-MATERIALIZATION, TASK-P2-DEV-02-RULEVIEW-REFERENCE, TASK-P2-DEV-03-MODEL-ACCESS-POLICY, TASK-P2-DEV-05-MODEL-TRUSTED-LOAD, TASK-P2-DEV-06-MODEL-SESSION-EFFECT, TASK-P2-DEV-07-STARTER-GUARDED-ACCESS, TASK-P2-DEV-08-PRODUCTION-COMPOSITION-CONCURRENCY, TASK-P2-DEV-09-REAL-FIXTURE-COMPATIBILITY
