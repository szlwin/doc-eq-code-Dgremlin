# P1-COMPILER-F01 恢复上下文

- 版本：V_1.0
- Schema：2
- 当前阶段：requirement_analysis
- 当前轮次：REQUIREMENT_ANALYSIS-I005
- 任务状态：PARTIAL
- 执行模式：SEQUENTIAL
- 当前执行 Agent：ProjectManagerAgent
- 项目管理 Agent：ProjectManagerAgent
- 下一 Agent：BusinessModelAgent
- 下一动作：由 BusinessModelAgent 审查 requirement_analysis@REQAN-R05@7de35e8dc15b
- 最近门禁：business_model
- 最新 handoff：project_doc/version/V_1.0/task/P1-COMPILER-F01/handoff/2026-07-26-reqconf-r04-passed.md

## 运行中的任务

- 无

## 当前与最近执行

- 当前：无
- 最近：ATTEMPT-TASK-P1-REQAN-001-I005-A001 [PASSED] 形成 REQAN-R05：修正 Compiler-owned 原子发布与源发现责任，迁移 2.43 dependency impact/CMI，保持 20 BR、9 AC、9 TR 与 fixture 合同。

## 可执行任务

- 无

## 开放问题

- ISSUE-MR-0001 [P1/OPEN] P1：ModelCompiler.compile 只接收 CompilationRequest，无法访问 ContextPublisher 或 expectedCurrent；但详细设计要求同一 CompilationSession 原子暴露后进入 PUBLISHED，架构又把 CAS 发布放在 compile 返回后的 Starter，组件职责和终态所有权冲突。；责任人：DesignAgent
- ISSUE-MR-0002 [P1/OPEN] P1：失败结果被要求强制携带 DigestPair，但 Source discovery、parse 等早期失败无法产生完整 semanticDigest；CompilationResult 又引入 CANCELLED/TIMED_OUT，而业务状态仅允许 PUBLISHED/FAILED，缺少确定映射。另有 CompiledModelSet 包含 DigestPair、semanticDigest 又基于 CompiledModelSet 计算的循环定义。；责任人：DesignAgent
- ISSUE-MR-0003 [P1/OPEN] P1：需求要求 discovery、parse、pass、digest 计时接缝，但 CompilationMetrics 未定义组成、时钟和观察接口；需求还要求每条源图边保留 SourceRef，当前 MixSourceGraph edge 没有字段契约与验证接缝。；责任人：DesignAgent
- ISSUE-MR-0004 [P1/OPEN] P1：AC-P1-COMPILER-001 要求从根入口恰好发现 10 个 XML 和固定类型边，测试接缝只说明主/测试共 20 个 XML 可解析，不能证明 Resolver 的源图边界；Diagnostic 排序中的 entityKey 与 definitionKey/passId 也没有显式映射。；责任人：DesignAgent

## 活跃决策

- DEC-P1-COMPILER-001: 不实现，仅建立可扩展结构与明确 deferred 边界
- DEC-P1-COMPILER-002: 不允许；仅实例级不可变对象
- DEC-P1-COMPILER-003: 仅提供只读投影视图，新代码禁止注册
- DEC-P1-COMPILER-004: 保持 Java 8，使用 final 类和值语义
- DEC-P1-COMPILER-005: dec-expand-declaration 整体废弃，不抽取、不迁移、不建立 Adapter；mix 是唯一目标配置契约，BusinessScope 仅为统一模型内的逻辑作用域，不是独立项目。
- DEC-P1-COMPILER-006: Information 归属 System，只能关联本 System view-info 中声明的 View；BusinessScope 仅编排 Directory/Action/Produce；共享模型路径通过 model-access/read|write/ref 显式映射到一个或多个 System View。
- DEC-P1-COMPILER-007: ref@property 先与目标 View 的 target-main 做区分大小写完整匹配；未匹配时再按点分隔 property path 在该 View 属性树中精确查找。两步失败或歧义均报错；不保留 root-property 根别名。

## 最近阶段结果

- SO-P1-COMPILER-F01-REQUIREMENT_ANALYSIS-I005 [RUNNING] requirement_analysis → REQAN-R05@7de35e8dc15b

## 失效产物

- business_model
- design
- test_design
- implementation_plan
- tdd
- development
- code_review
- testing
- completion_verification

## 恢复检查

1. 优先调用 task-context 核对当前任务、最新 attempt、最近失败和开放问题。
2. 核对最新 handoff、输入 revision、实际 Git diff 和工作区。
3. 核对验证命令是否对应当前 revision，旧证据不得沿用。
4. 检查任务依赖、共享文件和可变状态是否符合声明顺序。
5. 状态一致后从 next_action 继续，不重复已完成步骤。
