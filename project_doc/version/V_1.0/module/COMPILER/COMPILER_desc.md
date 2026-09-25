<!-- template: common-develop/module-desc-v2 -->
# COMPILER 模块说明

> 本文件由版本级模块描述通过 `wk -d` 合并，是模块能力、功能、业务信息、规则、依赖和文档索引的当前事实源。

## 模块信息

| 字段 | 内容 |
| --- | --- |
| 模块编码 | COMPILER |
| 模块名称 | dec-core-compiler |
| 模块说明 | DocumentFrontend SPI、Compiler Pipeline、EngineContext，以及 P2 简化配置发布与模型执行的跨模块契约 |
| 职责边界 | 负责 P1 编译事实和 P2 简化运行模型的文档所有权；ConfigInfo、ModelContainer 与解析器实现仍由对应代码模块负责 |
| 最后合并版本 | V_1.0 |

## 业务能力

| 能力编码 | 能力名称 | 能力说明 | 能力边界 | 状态 |
| --- | --- | --- | --- | --- |
| CAP-COMPILER-001 | P1 编译协作 | DocumentFrontend SPI、Raw AST、Compiler Pipeline、SymbolTable、RegistryBuilder 与 digest | 遵循 FLOW-CONFIG-COMPILE 与 CMI-P1-COMPILER-001 | ACTIVE |
| CAP-COMPILER-002 | P2 简化配置发布 | 独立候选 ConfigInfo 编译，并整体安装同次生成的 EngineContext | 业务执行由 MODEL 负责；不引入重型 runtime 权限层 | ACTIVE |

## 模块功能

| 功能编码 | 功能名称 | 功能说明 | 所属能力 | 关联需求 | API 引用 | DB 引用 | 功能状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| P1-COMPILER-F01 | 统一编译上下文骨架 | 建立统一前端、AST、Registry、Compiler Pipeline 与 EngineContext | CAP-COMPILER-001 | P1-COMPILER | CONTRACT-BUSINESS-COMPILER | 无 | ACTIVE |
| P2-SYSTEM-RULEVIEW-F01 | 候选配置加载 | XML/YAML 写入独立 ConfigInfo，编译成功后整体安装对应 EngineContext | CAP-COMPILER-002 | P2-SYSTEM-RULEVIEW | 内部 Java API | 无 | ACTIVE |

## 业务信息

| 信息编码 | 信息名称 | 信息说明 | DB引用 |
| --- | --- | --- | --- |

## 规则与约束

| 规则编码 | 规则摘要 | 关联需求 | 关联功能 |
| --- | --- | --- | --- |
| BR-P1-001 | 编译失败不得发布部分 EngineContext | P1-COMPILER | P1-COMPILER-F01 |
| BR-P2-SYSTEM-RULEVIEW-001 | 候选解析不得修改当前 ConfigInfo | P2-SYSTEM-RULEVIEW | P2-SYSTEM-RULEVIEW-F01 |
| BR-P2-SYSTEM-RULEVIEW-003 | 编译或发布失败不得安装候选配置 | P2-SYSTEM-RULEVIEW | P2-SYSTEM-RULEVIEW-F01 |

## 模块依赖

| 依赖模块 | 依赖类型 | 依赖说明 | 关联功能 | 接口引用 |
| --- | --- | --- | --- | --- |
| CONTEXT | 运行时配置 | ConfigInfo 保存并暴露同次编译的 EngineContext | P2-SYSTEM-RULEVIEW-F01 | ConfigInfo、ConfigManager |
| XML/YAML | 配置解析 | 两种格式写入同一候选 ConfigInfo 语义 | P2-SYSTEM-RULEVIEW-F01 | parseInto |

## 参与业务流程

| 流程编号 | 流程名称 | 模块角色 | 关联功能 | 流程层级 | 状态 | 流程文档 |
| --- | --- | --- | --- | --- | --- | --- |

## 文档引用

- 项目文档首页：[打开](../../../../docs/README.md)
- 版本摘要：[打开](../../version_summary.md)
- 需求列表：[打开](../../requirement_list.md)
- 业务模型：[打开](./COMPILER_business_model.yaml)
- API/DB 版本增量：[打开目录](./changes)
- API/DB 当前实现：代码、OpenAPI、DDL、Migration 或项目声明的等价来源
- 模块设计：[打开](./COMPILER_design.md)
- 依赖关系：[项目关联事实](../../../../docs/_relations/dependency_impact.yaml)
- 需求列表：`../../requirement_list.md`
- 业务模型：`COMPILER_business_model.yaml`
- API 契约：`COMPILER_api.yaml`
- DB 契约：`COMPILER_db.yaml`
- 模块设计：`COMPILER_design.md`
- 依赖关系：`../_relations/dependency_impact.yaml`
- 补充文档（自动）：[COMPILER_api_contract.md](./COMPILER_api_contract.md)
- 补充文档（自动）：[COMPILER_api_contract_security_authority_overlay_r32.md](./COMPILER_api_contract_security_authority_overlay_r32.md)
- 补充文档（自动）：[COMPILER_api_contract_security_authority_single_runtime_context_overlay_r33.md](./COMPILER_api_contract_security_authority_single_runtime_context_overlay_r33.md)
- 补充文档（自动）：[COMPILER_api_contract_write_value_overlay_r31.md](./COMPILER_api_contract_write_value_overlay_r31.md)
- 补充文档（自动）：[COMPILER_architecture.md](./COMPILER_architecture.md)
- 补充文档（自动）：[COMPILER_design_security_authority_overlay_r32.md](./COMPILER_design_security_authority_overlay_r32.md)
- 补充文档（自动）：[COMPILER_design_security_authority_single_runtime_context_overlay_r33.md](./COMPILER_design_security_authority_single_runtime_context_overlay_r33.md)
- 补充文档（自动）：[COMPILER_design_write_value_overlay_r31.md](./COMPILER_design_write_value_overlay_r31.md)
- 补充文档（自动）：[COMPILER_test_seams.md](./COMPILER_test_seams.md)
- 补充文档（自动）：[COMPILER_test_seams_security_authority_overlay_r32.md](./COMPILER_test_seams_security_authority_overlay_r32.md)
