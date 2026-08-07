<!-- template: common-develop/module-desc-v2 -->
# COMPILER 模块说明

> 本文件由版本级模块描述通过 `wk -d` 合并，是模块能力、功能、业务信息、规则、依赖和文档索引的当前事实源。

## 模块信息

| 字段 | 内容 |
| --- | --- |
| 模块编码 | COMPILER |
| 模块名称 | dec-core-compiler |
| 模块说明 | DocumentFrontend SPI、Raw AST、Compiler Pipeline、SymbolTable、RegistryBuilder 与 digest |
| 职责边界 | 仅承担 P1 编译骨架内明确职责，不提前实现 P2～P8 业务语义 |
| 最后合并版本 | V_1.0 |

## 业务能力

| 能力编码 | 能力名称 | 能力说明 | 能力边界 | 状态 |
| --- | --- | --- | --- | --- |
| CAP-COMPILER-001 | P1 编译协作 | DocumentFrontend SPI、Raw AST、Compiler Pipeline、SymbolTable、RegistryBuilder 与 digest | 遵循 FLOW-CONFIG-COMPILE 与 CMI-P1-COMPILER-001 | ACTIVE |

## 模块功能

| 功能编码 | 功能名称 | 功能说明 | 所属能力 | 关联需求 | API 引用 | DB 引用 | 功能状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| P1-COMPILER-F01 | 统一编译上下文骨架 | 建立统一前端、AST、Registry、Compiler Pipeline 与 EngineContext | CAP-COMPILER-001 | P1-COMPILER | CONTRACT-BUSINESS-COMPILER | 无 | ACTIVE |

## 业务信息

| 信息编码 | 信息名称 | 信息说明 | DB引用 |
| --- | --- | --- | --- |

## 规则与约束

| 规则编码 | 规则摘要 | 关联需求 | 关联功能 |
| --- | --- | --- | --- |
| BR-P1-001 | 编译失败不得发布部分 EngineContext | P1-COMPILER | P1-COMPILER-F01 |

## 模块依赖

| 依赖模块 | 依赖类型 | 依赖说明 | 关联功能 | 接口引用 |
| --- | --- | --- | --- | --- |

## 参与业务流程

| 流程编号 | 流程名称 | 模块角色 | 关联功能 | 流程层级 | 状态 | 流程文档 |
| --- | --- | --- | --- | --- | --- | --- |

## 文档引用

- 需求列表：`../../requirement_list.md`
- 业务模型：`COMPILER_business_model.yaml`
- API 契约：`COMPILER_api.yaml`
- DB 契约：`COMPILER_db.yaml`
- 模块设计：`COMPILER_design.md`
- 依赖关系：`../_relations/dependency_impact.yaml`
- 补充文档（自动）：[COMPILER_api_contract.md](COMPILER_api_contract.md)
