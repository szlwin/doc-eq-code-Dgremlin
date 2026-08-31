<!-- generated-by: common-develop/business_flow.py -->
# Configuration compilation and simple model execution（COMPILER）

- Revision：`FLOW-R04@p2-simple-runtime-model`
- Base Revision：`FLOW-R02@compiler-owned-discovery`
- 层级：L0 端到端场景、L1 业务阶段、L2 关键子流程

## 流程目录

| 流程编号 | 流程名称 | 层级 | 类型 | 主责模块 | 参与模块 | 状态 |
|---|---|---|---|---|---|---|
| [FLOW-CONFIG-COMPILE](#flow-config-compile) | Configuration candidate compilation and publication | L0 | MAIN | COMPILER | CONTEXT, COMPILER, XML, YAML, STARTER | PROPOSED |
| [FLOW-SIMPLE-MODEL-EXECUTE](#flow-simple-model-execute) | Simple model rule execution | L1 | MAIN | MODEL | COMPILER, CONTEXT, MODEL, DEMO | PROPOSED |

<a id="flow-config-compile"></a>
## FLOW-CONFIG-COMPILE Configuration candidate compilation and publication

- 层级/类型：`L0` / `MAIN`
- 父流程：`无`
- 主责模块：`COMPILER`
- 参与模块：[CONTEXT](../../../../../../docs/CONTEXT/CONTEXT_desc.md), [COMPILER](../../../../../../docs/COMPILER/COMPILER_desc.md), [XML](../../../../../../docs/XML/XML_desc.md), [YAML](../../../../../../docs/YAML/YAML_desc.md), [STARTER](../../../../../../docs/STARTER/STARTER_desc.md)
- 目标：Parse XML or YAML into an isolated ConfigInfo, compile an immutable EngineContext and install both as one current object; failures keep the previous installed configuration.
- 触发：starter receives a configuration source to load.
- 状态：`PROPOSED`

### 需求、功能与追踪

| 类型 | 引用 |
|---|---|
| 需求 | [P1-COMPILER](../../../P1-COMPILER-F01/requirement.md) |
| 需求 | [P2-SYSTEM-RULEVIEW](../../../FEATURE-DESC-3361AD2E54FC/requirement.md) |
| 功能 | [P1-COMPILER-F01](../../../../../../docs/COMPILER/COMPILER_desc.md)（COMPILER） |
| 功能 | [P2-SYSTEM-RULEVIEW-F01](../../../../../../docs/COMPILER/COMPILER_desc.md)（COMPILER） |
| 规则 | BR-P1-004, BR-P1-006, BR-P1-011, BR-P2-SYSTEM-RULEVIEW-001, BR-P2-SYSTEM-RULEVIEW-002, BR-P2-SYSTEM-RULEVIEW-003, BR-P2-SYSTEM-RULEVIEW-004, BR-P2-SYSTEM-RULEVIEW-005 |
| 验收 | AC-P1-COMPILER-001, AC-P1-COMPILER-002, AC-P1-COMPILER-003, AC-P1-COMPILER-004, AC-P2-SYSTEM-RULEVIEW-001, AC-P2-SYSTEM-RULEVIEW-002, AC-P2-SYSTEM-RULEVIEW-003, AC-P2-SYSTEM-RULEVIEW-004 |
| 追踪 | TR-P1-COMPILER-001, TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-004, TR-P2-001, TR-P2-002, TR-P2-003, TR-P2-004, TR-P2-009 |

### 前置条件

- parser, compiler and publisher are assembled
- the configuration source has a stable location

### 主流程

| 顺序 | 步骤编号 | 关键动作 | 主责模块 | 关联功能 | 子流程 |
|---|---|---|---|---|---|
| 1 | STEP-CONFIG-COMPILE-01 | Create isolated ConfigInfo candidate | COMPILER | P2-SYSTEM-RULEVIEW-F01 | 无 |
| 2 | STEP-CONFIG-COMPILE-02 | Parse XML or YAML into candidate | COMPILER | P2-SYSTEM-RULEVIEW-F01 | 无 |
| 3 | STEP-CONFIG-COMPILE-03 | Compile and publish EngineContext | COMPILER | P2-SYSTEM-RULEVIEW-F01 | 无 |
| 4 | STEP-CONFIG-COMPILE-04 | Install whole configuration | COMPILER | P2-SYSTEM-RULEVIEW-F01 | 无 |
| 5 | STEP-CONFIG-COMPILE-05 | Resolve candidate references | COMPILER | P2-SYSTEM-RULEVIEW-F01 | 无 |
| 6 | STEP-CONFIG-COMPILE-06 | Build immutable EngineContext | COMPILER | P2-SYSTEM-RULEVIEW-F01 | 无 |
| 7 | STEP-CONFIG-COMPILE-07 | Install ConfigInfo and EngineContext | COMPILER | P2-SYSTEM-RULEVIEW-F01 | 无 |

### 变体

| 变体编号 | 名称 | 适用条件 | 关键差异 |
|---|---|---|---|
| FVAR-CONFIG-FORMAT-XML | XML candidate | configuration format is XML | ConfigFileParser.parseInto populates the candidate |
| FVAR-CONFIG-FORMAT-YAML | YAML candidate | configuration format is YAML | YamlConfigFileParser.parseInto populates the candidate |

### 失败、回退与补偿

| 路径编号 | 发生步骤 | 条件 | 结果 | 后续流程 | 补偿 | 阻塞 |
|---|---|---|---|---|---|---|
| FAIL-CONFIG-COMPILE-001 | STEP-CONFIG-COMPILE-02 | 文档格式错误、XXE 或不受控 YAML 类型 | 生成 ERROR 并拒绝发布。 | 无 | 无数据补偿；调用方继续使用原 Context。 | true |
| FAIL-CONFIG-COMPILE-002 | STEP-CONFIG-COMPILE-04 | 重复强类型 Key | 记录首次和重复位置，拒绝 Registry 发布。 | 无 | 丢弃本次 Session 的 Builder。 | true |
| FAIL-CONFIG-COMPILE-003 | STEP-CONFIG-COMPILE-05 | 未知引用或引用类型错误 | 聚合 Diagnostic，拒绝 Context 发布。 | 无 | 不修改旧 Context。 | true |
| FAIL-P2-CONFIG-PARSE | STEP-CONFIG-COMPILE-02 | source is missing or malformed | candidate is rejected and installed ConfigInfo is unchanged | 无 | discard the candidate | true |
| FAIL-P2-CONFIG-COMPILE | STEP-CONFIG-COMPILE-03 | candidate contains an invalid definition or reference | diagnostics are returned and installed ConfigInfo plus EngineContext remain unchanged | 无 | discard the candidate | true |

### 成功标准

- XML and YAML obey the same candidate isolation semantics
- ConfigInfo and its exact EngineContext become current together
- failed candidates never partially replace the installed configuration

### 下游映射

- 业务模型：AGG-COMPILATION-SESSION
- 影响分析：docs/_relations/dependency_impact.yaml#CMI-P1-COMPILER-001
- 技术设计：待补充
- 测试 Case：CASE-P1-CANONICAL-001, CASE-P1-DIAGNOSTIC-001, CASE-P1-SYMBOL-001, CASE-P1-CONTEXT-001

<a id="flow-simple-model-execute"></a>
## FLOW-SIMPLE-MODEL-EXECUTE Simple model rule execution

- 层级/类型：`L1` / `MAIN`
- 父流程：`无`
- 主责模块：`MODEL`
- 参与模块：[COMPILER](../../../../../../docs/COMPILER/COMPILER_desc.md), [CONTEXT](../../../../../../docs/CONTEXT/CONTEXT_desc.md), [MODEL](../../../../../../docs/MODEL/MODEL_desc.md), [DEMO](../../../../../../docs/DEMO/DEMO_desc.md)
- 目标：Create business ModelData, load a named rule and execute it with clear definition failures and reliable connection cleanup.
- 触发：business code calls DataUtil, ModelLoader and ModelContainer.
- 状态：`PROPOSED`

### 需求、功能与追踪

| 类型 | 引用 |
|---|---|
| 需求 | [P2-SYSTEM-RULEVIEW](../../../FEATURE-DESC-3361AD2E54FC/requirement.md) |
| 功能 | [P2-SYSTEM-RULEVIEW-F03](../../../../../../docs/MODEL/MODEL_desc.md)（MODEL） |
| 规则 | BR-P2-SYSTEM-RULEVIEW-006, BR-P2-SYSTEM-RULEVIEW-007, BR-P2-SYSTEM-RULEVIEW-008, BR-P2-SYSTEM-RULEVIEW-009, BR-P2-SYSTEM-RULEVIEW-010 |
| 验收 | AC-P2-SYSTEM-RULEVIEW-005, AC-P2-SYSTEM-RULEVIEW-006, AC-P2-SYSTEM-RULEVIEW-007, AC-P2-SYSTEM-RULEVIEW-008 |
| 追踪 | TR-P2-005, TR-P2-006, TR-P2-007, TR-P2-008, TR-P2-010 |

### 前置条件

- a compiled ConfigInfo is installed
- requested View, Rule and Connection definitions exist

### 主流程

| 顺序 | 步骤编号 | 关键动作 | 主责模块 | 关联功能 | 子流程 |
|---|---|---|---|---|---|
| 1 | STEP-P2-EXECUTE-01 | Create complete ModelData | MODEL | P2-SYSTEM-RULEVIEW-F03 | 无 |
| 2 | STEP-P2-EXECUTE-02 | Load named rule task | MODEL | P2-SYSTEM-RULEVIEW-F03 | 无 |
| 3 | STEP-P2-EXECUTE-03 | Execute and finish connections | MODEL | P2-SYSTEM-RULEVIEW-F03 | 无 |

### 变体

- 无

### 失败、回退与补偿

| 路径编号 | 发生步骤 | 条件 | 结果 | 后续流程 | 补偿 | 阻塞 |
|---|---|---|---|---|---|---|
| FAIL-P2-EXECUTE-DEFINITION | STEP-P2-EXECUTE-01 | View, Rule or Connection is absent | failure identifies the missing type and name before database side effects | 无 | no database compensation is required | true |
| FAIL-P2-EXECUTE-RULE | STEP-P2-EXECUTE-03 | rule or database execution fails | participating connections roll back and all opened connections close | 无 | ModelContainer performs rollback and close | true |

### 成功标准

- business callers pass no runtime scope, session, handle, owner identity or capability
- rule names such as save-Order remain valid
- connection commit, rollback and close behavior remains visible

### 下游映射

- 业务模型：待补充
- 影响分析：待补充
- 技术设计：待补充
- 测试 Case：待补充
