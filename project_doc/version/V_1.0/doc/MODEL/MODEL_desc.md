<!-- template: common-develop/module-desc-v2 -->
# MODEL 模块说明

> 版本级文件记录本版本新增或调整的模块事实；`wk -d` 会自动发现同目录补充文档并维护“文档引用”。

> 文档导航：[项目文档首页](../../../../docs/README.md) · [版本摘要](../../version_summary.md) · [本版本需求列表](../../requirement_list.md)

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

## 模块功能

| 功能编码 | 功能名称 | 功能说明 | 所属能力 | 关联需求 | API 引用 | DB 引用 | 功能状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| P2-SYSTEM-RULEVIEW-F03 | 简单模型执行 | 保留 DataUtil、ModelLoader、ModelContainer 的直接业务调用和事务收尾 | CAP-MODEL-001 | P2-SYSTEM-RULEVIEW | 内部 Java API | 业务连接 | ACTIVE |

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

## 模块依赖

| 依赖模块 | 依赖类型 | 依赖说明 | 关联功能 | 接口引用 |
| --- | --- | --- | --- | --- |
| CONTEXT | 配置读取 | 从当前 ConfigInfo 解析 View、Rule、Connection 和关联 EngineContext | P2-SYSTEM-RULEVIEW-F03 | ConfigManager、ConfigInfo |

## 参与业务流程

| 流程编号 | 流程名称 | 模块角色 | 关联功能 | 流程层级 | 状态 | 流程文档 |
| --- | --- | --- | --- | --- | --- | --- |

## 文档引用

- 项目文档首页：[打开](../../../../docs/README.md)
- 版本摘要：[打开](../../version_summary.md)
- 需求列表：[打开](../../requirement_list.md)
- 业务模型：[打开](MODEL_business_model.yaml)
- API/DB 版本增量：[打开目录](changes/)
- API/DB 当前实现：代码、OpenAPI、DDL、Migration 或项目声明的等价来源
- 模块设计：[打开](MODEL_design.md)
- 依赖关系：[项目关联事实](../../../../docs/_relations/dependency_impact.yaml)
