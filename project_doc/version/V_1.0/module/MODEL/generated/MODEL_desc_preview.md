<!-- managed-by: common-develop/wk-d -->
# MODEL 模块说明

> 本文件由版本级模块描述通过 `wk -d` 合并，是模块能力、功能、业务信息、规则、依赖和文档索引的当前事实源。

> 文档导航：[项目文档首页](../../../../../docs/README.md) · [项目需求列表](../../../../../docs/requirement_list.md) · [最后合并版本摘要](../../../version_summary.md)

## 模块信息

| 字段 | 内容 |
| --- | --- |
| 模块编码 | MODEL |
| 模块名称 | dec-core-model |
| 模块说明 | ModelData 创建、命名规则装载、业务执行与连接事务收尾 |
| 职责边界 | 提供易用业务调用链，不向调用者暴露 Scope、Session、Handle、Capability 或 owner identity |
| 最后合并版本 | V_1.0 |

## 业务能力

| 能力编码 | 能力名称 | 能力说明 | 能力边界 | 状态 |
| --- | --- | --- | --- | --- |
| CAP-MODEL-001 | 简单模型执行 | 从当前 ConfigInfo 获取定义，创建 ModelData、装载规则并执行 | 保留既有 DataConnection 事务与资源管理，不建立重型 runtime 权限层 | ACTIVE |
| CAP-MODEL-002 | Information 识别与物化 | 消费已发布的 CompiledInformationSet，提供只读 evaluate 和可选 change-data 物化 | 物化复用既有 ModelContainer；成功提交后不自动重评估 | PLANNED |

## 模块功能

| 功能编码 | 功能名称 | 功能说明 | 所属能力 | 关联需求 | API 引用 | DB 引用 | 功能状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| P2-SYSTEM-RULEVIEW-F03 | 简单模型执行 | 保留 DataUtil、ModelLoader、ModelContainer 的直接业务调用和事务收尾 | CAP-MODEL-001 | P2-SYSTEM-RULEVIEW | 内部 Java API | 业务连接 | ACTIVE |
| P3-INFORMATION-ENGINE-F01 | Information Engine | 统一编译发布后的 Information 只读识别与可选原子物化，区分 TRUE/FALSE 与 ERROR | CAP-MODEL-002 | P3-INFORMATION-ENGINE | 内部 Java API | 业务连接 | PLANNED |

## 业务信息

| 信息编码 | 信息名称 | 信息说明 | DB引用 |
| --- | --- | --- | --- |

## 规则与约束

| 规则编码 | 规则摘要 | 关联需求 | 关联功能 |
| --- | --- | --- | --- |
| BR-P2-SYSTEM-RULEVIEW-006 | createViewData 返回完整可修改 ViewData | P2-SYSTEM-RULEVIEW | P2-SYSTEM-RULEVIEW-F03 |
| BR-P2-SYSTEM-RULEVIEW-007 | 规则继续支持 save-Order 形式的名称调用 | P2-SYSTEM-RULEVIEW | P2-SYSTEM-RULEVIEW-F03 |
| BR-P2-SYSTEM-RULEVIEW-008 | 业务调用者不显式传递 EngineContext | P2-SYSTEM-RULEVIEW | P2-SYSTEM-RULEVIEW-F03 |
| BR-P2-SYSTEM-RULEVIEW-009 | 缺失定义必须在对应副作用前失败 | P2-SYSTEM-RULEVIEW | P2-SYSTEM-RULEVIEW-F03 |
| BR-P2-SYSTEM-RULEVIEW-010 | ModelContainer 保留提交、回滚和关闭 | P2-SYSTEM-RULEVIEW | P2-SYSTEM-RULEVIEW-F03 |
| BR-P3-INFORMATION-ENGINE-001 | 三类 Information 互斥，复合 Information 不可物化 | P3-INFORMATION-ENGINE | P3-INFORMATION-ENGINE-F01 |
| BR-P3-INFORMATION-ENGINE-002 | 正常不满足返回 FALSE，非法路径、普通 null 和表达式错误返回 ERROR | P3-INFORMATION-ENGINE | P3-INFORMATION-ENGINE-F01 |
| BR-P3-INFORMATION-ENGINE-003 | evaluate 每次按配置实时读取且保持只读 | P3-INFORMATION-ENGINE | P3-INFORMATION-ENGINE-F01 |

## 模块依赖

| 依赖模块 | 依赖类型 | 依赖说明 | 关联功能 | 接口引用 |
| --- | --- | --- | --- | --- |
| CONTEXT | 配置读取 | 从当前 ConfigInfo 解析 View、Rule、Connection 和关联 EngineContext | P2-SYSTEM-RULEVIEW-F03 | ConfigManager、ConfigInfo |
| COMPILER | Information 编译事实 | 消费与 EngineContext 同次发布的 CompiledInformationSet 和 exact binding | P3-INFORMATION-ENGINE-F01 | CompiledInformationSet、EngineContext |
| CONTEXT | Information 发布 | 暴露同一发布闭包中的 P3 Information 编译事实 | P3-INFORMATION-ENGINE-F01 | EngineContext |

## 参与业务流程

| 流程编号 | 流程名称 | 模块角色 | 关联功能 | 流程层级 | 状态 | 流程文档 |
| --- | --- | --- | --- | --- | --- | --- |
| FLOW-P3-INFORMATION-EVALUATION | P3 Information compilation, evaluation and materialization | OWNER | P3-INFORMATION-ENGINE-F01 | L1 | PROPOSED | [版本预览](../../_flows/COMPILER/generated/COMPILER_flow.preview.md#flow-p3-information-evaluation) |

## 文档引用

- 项目文档首页：[打开](../../../../../docs/README.md)
- 需求列表：[打开](../../../../../docs/requirement_list.md)
- 最后合并版本摘要：[打开](../../../version_summary.md)
- 业务模型阅读版：`MODEL_business_model.md`（存在时由项目首页提供可点击入口）
- API/DB 增量历史：`../../archive_manifest.yaml`
- API/DB 当前实现：代码、OpenAPI、DDL、Migration 或项目声明的等价来源
- 本版本设计：`../../version/V_1.0/doc/MODEL/MODEL_design.md`（存在时由项目首页和版本摘要提供可点击入口）
- 依赖关系：`../_relations/dependency_graph.md`（存在时由项目首页提供可点击入口）
