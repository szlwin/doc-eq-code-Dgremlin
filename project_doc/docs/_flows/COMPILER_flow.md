<!-- generated-by: common-develop/business_flow.py -->
# 配置编译与上下文发布（COMPILER）

- Revision：`FLOW-R02@compiler-owned-discovery`
- Base Revision：`FLOW-R00@init`
- 层级：L0 端到端场景、L1 业务阶段、L2 关键子流程

## 流程目录

| 流程编号 | 流程名称 | 层级 | 类型 | 主责模块 | 参与模块 | 状态 |
|---|---|---|---|---|---|---|
| [FLOW-CONFIG-COMPILE](#flow-config-compile) | 配置编译与上下文发布 | L0 | MAIN | COMPILER | CONTEXT, COMPILER, XML, YAML, STARTER, DEMO | ACTIVE |

## FLOW-CONFIG-COMPILE 配置编译与上下文发布

- 层级/类型：`L0` / `MAIN`
- 父流程：`无`
- 主责模块：`COMPILER`
- 参与模块：[CONTEXT](../CONTEXT/CONTEXT_desc.md), [COMPILER](../COMPILER/COMPILER_desc.md), [XML](../XML/XML_desc.md), [YAML](../YAML/YAML_desc.md), [STARTER](../STARTER/STARTER_desc.md), [DEMO](../DEMO/DEMO_desc.md)
- 目标：把 XML/YAML 文档集合确定性编译为可发布的不可变 EngineContext，失败时保持既有 Context 不变。
- 触发：starter 或测试调用者提交根 SourceReference、编译请求和显式发布请求。
- 状态：`ACTIVE`

### 需求、功能与追踪

| 类型 | 引用 |
|---|---|
| 需求 | [P1-COMPILER](../../version/V_1.0/doc/P1-COMPILER-F01/requirement.md) |
| 功能 | [P1-COMPILER-F01](../COMPILER/COMPILER_desc.md)（COMPILER） |
| 规则 | BR-P1-004, BR-P1-006, BR-P1-011 |
| 验收 | AC-P1-COMPILER-001, AC-P1-COMPILER-002, AC-P1-COMPILER-003, AC-P1-COMPILER-004 |
| 追踪 | TR-P1-COMPILER-001, TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-004 |

### 前置条件

- DocumentFrontend 与编译选项已装配；输入源具有稳定 sourceId。

### 主流程

| 顺序 | 步骤编号 | 关键动作 | 主责模块 | 关联功能 | 子流程 |
|---|---|---|---|---|---|
| 1 | STEP-CONFIG-COMPILE-01 | 发现并排序文档源 | COMPILER | P1-COMPILER-F01 | 无 |
| 2 | STEP-CONFIG-COMPILE-02 | 前端解析为 Canonical 节点 | COMPILER | P1-COMPILER-F01 | 无 |
| 3 | STEP-CONFIG-COMPILE-03 | 构建 Raw AST 并结构校验 | COMPILER | P1-COMPILER-F01 | 无 |
| 4 | STEP-CONFIG-COMPILE-04 | 注册强类型符号 | COMPILER | P1-COMPILER-F01 | 无 |
| 5 | STEP-CONFIG-COMPILE-05 | 解析引用并准备图 | COMPILER | P1-COMPILER-F01 | 无 |
| 6 | STEP-CONFIG-COMPILE-06 | 执行语义校验并生成摘要 | COMPILER | P1-COMPILER-F01 | 无 |
| 7 | STEP-CONFIG-COMPILE-07 | 发布或拒绝 EngineContext | COMPILER | P1-COMPILER-F01 | 无 |

### 变体

| 变体编号 | 名称 | 适用条件 | 关键差异 |
|---|---|---|---|
| FVAR-CONFIG-FORMAT-XML | XML 前端 | DocumentFormat=XML | 使用安全 XML 解析并捕获行列位置。 |
| FVAR-CONFIG-FORMAT-YAML | YAML 前端 | DocumentFormat=YAML | 使用受控 YAML Node 并捕获 Mark；P1 仅最小等价路径。 |

### 失败、回退与补偿

| 路径编号 | 发生步骤 | 条件 | 结果 | 后续流程 | 补偿 | 阻塞 |
|---|---|---|---|---|---|---|
| FAIL-CONFIG-COMPILE-001 | STEP-CONFIG-COMPILE-02 | 文档格式错误、XXE 或不受控 YAML 类型 | 生成 ERROR 并拒绝发布。 | 无 | 无数据补偿；调用方继续使用原 Context。 | true |
| FAIL-CONFIG-COMPILE-002 | STEP-CONFIG-COMPILE-04 | 重复强类型 Key | 记录首次和重复位置，拒绝 Registry 发布。 | 无 | 丢弃本次 Session 的 Builder。 | true |
| FAIL-CONFIG-COMPILE-003 | STEP-CONFIG-COMPILE-05 | 未知引用或引用类型错误 | 聚合 Diagnostic，拒绝 Context 发布。 | 无 | 不修改旧 Context。 | true |

### 成功标准

- Data/View/Rule 进入不可变 Registry。
- 两个 Context 可同时存在且无污染。
- 语义 digest 与诊断顺序可重复。

### 下游映射

- 业务模型：AGG-COMPILATION-SESSION
- 影响分析：docs/_relations/dependency_impact.yaml#CMI-P1-COMPILER-001
- 技术设计：待补充
- 测试 Case：CASE-P1-CANONICAL-001, CASE-P1-DIAGNOSTIC-001, CASE-P1-SYMBOL-001, CASE-P1-CONTEXT-001
