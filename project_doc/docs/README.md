<!-- generated-by: common-develop/document_portal.py -->
# 项目研发文档

> 本页是人类阅读入口，只汇总可打开的当前文档和版本历史；结构化事实、Review、Evidence 与长任务事件仍由各自 authority 保存。

## 快速入口

- [当前需求与功能](requirement_list.md)
- [归档记录](../archive_manifest.yaml)

## 模块与当前设计

| 模块 | 文档 |
| --- | --- |
| COMPILER | [模块说明](module/COMPILER/COMPILER_desc.md) / [当前设计](module/COMPILER/COMPILER_design.md) / [业务模型](module/COMPILER/COMPILER_business_model.md) |
| CONTEXT | [模块说明](module/CONTEXT/CONTEXT_desc.md) |
| DEMO | [模块说明](module/DEMO/DEMO_desc.md) |
| MODEL | 暂无可读文档 |
| STARTER | [模块说明](module/STARTER/STARTER_desc.md) |
| XML | [模块说明](module/XML/XML_desc.md) |
| YAML | [模块说明](module/YAML/YAML_desc.md) |

## 业务流程与关联影响

- [业务流程索引](_flows/flow_index.md)
- [跨模块关联图](_relations/dependency_graph.md)
- [追踪矩阵](_relations/traceability_matrix.md)

## 版本历史

| 版本 | 文档 |
| --- | --- |
| V_1.0 | [版本摘要](../version/V_1.0/version_summary.md) |

## 阅读说明

- 想了解“为什么做、必须表现为什么”：先读需求。
- 想了解“当前如何实现、改哪些表/接口/代码”：读模块设计。
- 想了解“跨模块怎样协作、失败如何处理”：读业务流程和关联影响。
- 想了解“某个版本改了什么”：读对应版本摘要。
- API/DB 只在版本 changeset 中记录增量；当前实现事实读取代码、OpenAPI、DDL、Migration、Schema 或项目声明的等价来源。
