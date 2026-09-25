<!-- generated-by: common-develop/merge_docs.py -->
# MODEL业务模型

- Revision：`BM-P3-R01@information-rebaseline`
- Base Revision：`BM-R00@init`

## 术语

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| TERM-INFORMATION | Information | {"definition": "可被限定 Key 识别、组合或作为模型表达式原子的业务事实。", "id": "TERM-INFORMATION", "name": "Information"} |
| TERM-INFORMATION-KEY | InformationKey | {"definition": "System 归属与本地名称组成的稳定 Information 身份。", "id": "TERM-INFORMATION-KEY", "name": "InformationKey"} |
| TERM-MODEL-EXPRESSION | Model Expression | {"definition": "只读取模型路径，并可通过 change-data 声明物化目标值的原子 Information。", "id": "TERM-MODEL-EXPRESSION", "name": "Model Expression"} |
| TERM-INFORMATION-EXPRESSION | Information Expression | {"definition": "只引用 InformationKey 的复合 Information 表达式。", "id": "TERM-INFORMATION-EXPRESSION", "name": "Information Expression"} |
| TERM-REALTIME-READ | 实时读取 | {"definition": "每次判断按 Information 配置重新读取当前模型或事务上下文中的值。", "id": "TERM-REALTIME-READ", "name": "实时读取"} |
| TERM-MATERIALIZATION-TARGET-FACT | MaterializationTargetFact | {"definition": "P3 编译并发布的不可变目标事实，只描述目标 Information、change-data、路径、权限和规则约束，供 Directory 解析使用，不包含 ChangeInfo 或 RuleViewInfo 映射。", "id": "TERM-MATERIALIZATION-TARGET-FACT", "name": "MaterializationTargetFact"} |
| TERM-DIRECTORY-MATERIALIZATION-OWNERSHIP | Directory materialization ownership | {"definition": "Directory 拥有 ChangeInfo、ChangeInfo 持有的 RuleViewInfo 映射，以及对象生成、注册、不变量、执行顺序和生命周期；这些事实与 Information model 无关。", "id": "TERM-DIRECTORY-MATERIALIZATION-OWNERSHIP", "name": "Directory materialization ownership"} |
| TERM-DIRECTORY-ACTION-REF-RULE | Directory Action refRule handoff | {"definition": "Directory 按声明顺序提供 Action；Action.refRule 是 ModelLoader.load 的规则名输入，既有执行链再通过 ConfigInfo.getRuleViewInfo(refRule) 获取对应 RuleViewInfo。Directory 不创建 Loader、不执行规则或事务。", "id": "TERM-DIRECTORY-ACTION-REF-RULE", "name": "Directory Action refRule handoff"} |

## 场景

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| SCN-P3-IDENTIFY |  | {"given": ["已发布的 16 个 Information 定义和可读模型上下文"], "id": "SCN-P3-IDENTIFY", "then": ["先识别原子再按 DAG 计算复合，返回 TRUE/FALSE/ERROR 和证据"], "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "when": "按限定 InformationKey 请求识别"} |
| SCN-P3-PUBLISH-MATERIALIZATION-TARGET |  | {"given": ["模型表达式原子声明 change-data，且目标 Information、路径和精确写权限合法"], "id": "SCN-P3-PUBLISH-MATERIALIZATION-TARGET", "then": ["发布不含 ChangeInfo/RuleViewInfo 映射的 MaterializationTargetFact，供 Directory 解析阶段消费"], "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "when": "P3 编译配置候选"} |
| SCN-DIRECTORY-MATERIALIZE |  | {"given": ["Directory 已在编译期完成合法 ChangeInfo/RuleViewInfo 生成、注册和映射，并打开共享回滚边界"], "id": "SCN-DIRECTORY-MATERIALIZE", "then": ["RuleViewInfo 沿 DataUtil → ConfigInfo.getRuleViewInfo(refRule) 获取；全部 Loader 成功后按 grammer → update → commit 正常返回；任一步失败则停止后续 Loader、整体回滚并中断；成功路径不调用 evaluate"], "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "when": "Directory 按顺序提供带 refRule 的 Action，调用方将每个 refRule 装载为 ModelLoader，并在同一 ModelContainer 上完成一次 execute"} |

## 实体

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| ENT-INFORMATION | Information | {"attributes": [{"name": "key", "required": true, "type": "InformationKey"}, {"name": "kind", "required": true, "type": "RULE_ATOMIC|MODEL_ATOMIC|COMPOSITE"}, {"name": "ownerSystem", "required": true, "type": "SystemKey"}], "behaviors": ["按 kind 执行互斥校验", "只读识别", "为合法模型表达式原子发布物化目标事实"], "id": "ENT-INFORMATION", "identity": "InformationKey", "name": "Information", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| ENT-IDENTIFICATION-RESULT | IdentificationResult | {"attributes": [{"name": "state", "required": true, "type": "TRUE|FALSE|ERROR"}, {"name": "evidence", "required": true, "type": "EvidenceSet"}, {"name": "readPaths", "required": true, "type": "List<ModelPath>"}], "behaviors": ["结果绑定本次读取版本", "ERROR 不降级为 FALSE"], "id": "ENT-IDENTIFICATION-RESULT", "identity": "informationKey+modelVersion+configurationRevision", "name": "IdentificationResult", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |

## 值对象

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| VO-INFORMATION-KEY | InformationKey | {"attributes": [{"name": "systemKey", "required": true, "type": "SystemKey"}, {"name": "localName", "required": true, "type": "String"}], "behaviors": ["限定引用", "大小写和归属稳定"], "id": "VO-INFORMATION-KEY", "identity": "systemKey+localName", "name": "InformationKey", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| VO-MODEL-PATH | ModelPath | {"attributes": [{"name": "canonicalSegments", "required": true, "type": "List<String>"}, {"name": "dataOwner", "required": true, "type": "target-main Data|relation Data"}], "behaviors": ["target-main 基础字段优先", "relation Data 独立解析"], "id": "VO-MODEL-PATH", "identity": "canonicalSegments", "name": "ModelPath", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| VO-DEPENDENCY-EDGE | InformationDependency | {"attributes": [{"name": "fromKey", "required": true, "type": "InformationKey"}, {"name": "toKey", "required": true, "type": "InformationKey"}], "behaviors": ["发布前拒绝缺失引用和循环"], "id": "VO-DEPENDENCY-EDGE", "identity": "fromKey+toKey", "name": "InformationDependency", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| VO-MATERIALIZATION-TARGET-FACT | MaterializationTargetFact | {"attributes": [{"name": "informationKey", "required": true, "type": "InformationKey"}, {"name": "changeData", "required": true, "type": "DeclaredValue"}, {"name": "writePath", "required": true, "type": "ModelPath"}, {"name": "writePermission", "required": true, "type": "ExactPermission"}, {"name": "ruleConstraints", "required": true, "type": "MaterializationRuleConstraints"}], "behaviors": ["配置编译期完成目标事实校验", "作为 Directory 的只读编译输入", "不持有 ChangeInfo 或 RuleViewInfo"], "id": "VO-MATERIALIZATION-TARGET-FACT", "identity": "informationKey+writePath", "name": "MaterializationTargetFact", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |

## 聚合

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| AGG-INFORMATION-MODEL | Information model | {"id": "AGG-INFORMATION-MODEL", "invariantIds": ["INV-P3-KIND-EXCLUSIVE", "INV-P3-DAG", "INV-P3-TARGET-FACT-COMPILE"], "members": ["VO-INFORMATION-KEY", "VO-MODEL-PATH", "VO-DEPENDENCY-EDGE", "VO-MATERIALIZATION-TARGET-FACT"], "name": "Information model", "root": "ENT-INFORMATION", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "transactionBoundary": "配置发布时整体校验并原子发布 Information 与 MaterializationTargetFact；不包含 Directory 映射"} |
| AGG-INFORMATION-EVALUATION | Information evaluation | {"id": "AGG-INFORMATION-EVALUATION", "invariantIds": ["INV-P3-PATH-RESOLUTION", "INV-P3-REALTIME-READ", "INV-P3-NULL-ERROR", "INV-P3-EVALUATE-ERROR", "INV-P3-EVERY-EMPTY"], "members": ["VO-INFORMATION-KEY", "ENT-IDENTIFICATION-RESULT"], "name": "Information evaluation", "root": "ENT-IDENTIFICATION-RESULT", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "transactionBoundary": "识别只读；不拥有 Directory 物化事务"} |

## 不变量

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| INV-P3-KIND-EXCLUSIVE | Information 必须且只能属于 RuleView 原子、模型表达式原子或复合表达式之一；复合表达式只引用 InformationKey。 | {"failure": "INFORMATION_DEFINITION_INVALID", "id": "INV-P3-KIND-EXCLUSIVE", "statement": "Information 必须且只能属于 RuleView 原子、模型表达式原子或复合表达式之一；复合表达式只引用 InformationKey。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "trigger": "compile"} |
| INV-P3-DAG | Information 依赖图必须无环，引用必须存在且符合 System 归属规则。 | {"failure": "INFORMATION_DEPENDENCY_INVALID", "id": "INV-P3-DAG", "statement": "Information 依赖图必须无环，引用必须存在且符合 System 归属规则。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "trigger": "publish"} |
| INV-P3-TARGET-FACT-COMPILE | 只有声明 change-data 的模型表达式原子可产生 MaterializationTargetFact；目标 Information、change-data、规范写路径和精确写权限必须完整且合法，否则候选配置不得发布。 | {"failure": "INFORMATION_MATERIALIZATION_TARGET_INVALID", "id": "INV-P3-TARGET-FACT-COMPILE", "statement": "只有声明 change-data 的模型表达式原子可产生 MaterializationTargetFact；目标 Information、change-data、规范写路径和精确写权限必须完整且合法，否则候选配置不得发布。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "trigger": "compile"} |
| INV-P3-PATH-RESOLUTION | 模型路径不存在为 ERROR；target-main 对应基础字段优先，关系字段按自身 Data 归属解析。 | {"failure": "INFORMATION_PATH_INVALID", "id": "INV-P3-PATH-RESOLUTION", "statement": "模型路径不存在为 ERROR；target-main 对应基础字段优先，关系字段按自身 Data 归属解析。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "trigger": "identify"} |
| INV-P3-REALTIME-READ | 每次判断按 Information 配置重新读取当前值，不维护或消费 MutationSet。 | {"failure": "INFORMATION_READ_FAILED", "id": "INV-P3-REALTIME-READ", "statement": "每次判断按 Information 配置重新读取当前值，不维护或消费 MutationSet。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "trigger": "identify"} |
| INV-P3-NULL-ERROR | 普通求值遇到 null 为 ERROR，只有显式 InformationKey = null 比较例外。 | {"failure": "INFORMATION_NULL_ERROR", "id": "INV-P3-NULL-ERROR", "statement": "普通求值遇到 null 为 ERROR，只有显式 InformationKey = null 比较例外。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "trigger": "identify"} |
| INV-P3-EVALUATE-ERROR | 非法路径、普通 null、权限、表达式或只读 RuleView 执行错误必须记录 ERROR 诊断并抛出 InformationEvaluationException，不得降级为 FALSE、进入 Directory 物化或继续下游。 | {"failure": "INFORMATION_EVALUATION_FAILED", "id": "INV-P3-EVALUATE-ERROR", "statement": "非法路径、普通 null、权限、表达式或只读 RuleView 执行错误必须记录 ERROR 诊断并抛出 InformationEvaluationException，不得降级为 FALSE、进入 Directory 物化或继续下游。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "trigger": "identify"} |
| INV-P3-EVERY-EMPTY | every(emptyCollection, predicate) 为 TRUE；订单相关 Information 还必须满足订单明细非空，明细为空时结果为 FALSE 并可附 ORDER_DETAIL_REQUIRED 诊断。 | {"failure": "ORDER_DETAIL_REQUIRED", "id": "INV-P3-EVERY-EMPTY", "statement": "every(emptyCollection, predicate) 为 TRUE；订单相关 Information 还必须满足订单明细非空，明细为空时结果为 FALSE 并可附 ORDER_DETAIL_REQUIRED 诊断。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "trigger": "identify"} |

## 状态机

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| SM-P3-IDENTIFICATION | Information identification | {"id": "SM-P3-IDENTIFICATION", "initialState": "REQUESTED", "name": "Information identification", "states": ["REQUESTED", "EVALUATING", "TRUE", "FALSE", "ERROR"], "traceIds": ["TR-P3-INFORMATION-ENGINE-001"], "transitions": [{"command": "evaluate", "failure": "INFORMATION_NOT_PUBLISHED", "from": "REQUESTED", "id": "TRANS-P3-EVALUATE", "preconditions": ["InformationKey 已发布"], "to": "EVALUATING"}, {"command": "resultTrue", "failure": "REQUIRED_CONDITION_NOT_SATISFIED", "from": "EVALUATING", "id": "TRANS-P3-TRUE", "preconditions": ["所有必需读取满足"], "to": "TRUE"}, {"command": "resultFalse", "failure": "EVALUATION_ERROR_PRESENT", "from": "EVALUATING", "id": "TRANS-P3-FALSE", "preconditions": ["条件不满足且无错误"], "to": "FALSE"}, {"command": "raiseEvaluationError", "failure": "INFORMATION_EVALUATION_FAILED", "from": "EVALUATING", "id": "TRANS-P3-ERROR", "preconditions": ["路径/null/权限/表达式/RuleView 错误"], "to": "ERROR"}]} |

## 领域服务

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| SVC-P3-INFORMATION-EVALUATOR | Information evaluator | {"id": "SVC-P3-INFORMATION-EVALUATOR", "inputs": ["InformationKey + current model context"], "name": "Information evaluator", "outputs": ["IdentificationResult"], "reason": "识别需要统一处理三类 Information 和 DAG 拓扑。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| SVC-P3-TARGET-FACT-COMPILER | Materialization target fact compiler | {"id": "SVC-P3-TARGET-FACT-COMPILER", "inputs": ["model-atomic Information + change-data + path + permission + rule constraints"], "name": "Materialization target fact compiler", "outputs": ["MaterializationTargetFact"], "reason": "P3 必须在不拥有 Directory 映射的前提下，向 Directory 提供完整且合法的物化目标事实。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |

## 策略

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| POL-P3-READ-SET | configured read policy | {"id": "POL-P3-READ-SET", "inputs": ["P3 Information and downstream boundary facts"], "name": "configured read policy", "outputs": ["Enforced policy decision"], "reason": "只读取当前 Information 及其依赖声明的模型路径，不读取无关字段。", "statement": "只读取当前 Information 及其依赖声明的模型路径，不读取无关字段。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| POL-P3-DIRECTORY-OWNERSHIP | Directory ownership policy | {"id": "POL-P3-DIRECTORY-OWNERSHIP", "inputs": ["P3 Information and downstream boundary facts"], "name": "Directory ownership policy", "outputs": ["Enforced policy decision"], "reason": "ChangeInfo、ChangeInfo 持有的 RuleViewInfo 映射，以及映射的生成、注册、不变量和生命周期统一归 Directory；P3 只发布 MaterializationTargetFact。", "statement": "ChangeInfo、ChangeInfo 持有的 RuleViewInfo 映射，以及映射的生成、注册、不变量和生命周期统一归 Directory；P3 只发布 MaterializationTargetFact。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| POL-P3-COMPILE-REJECTION | configuration compile rejection policy | {"id": "POL-P3-COMPILE-REJECTION", "inputs": ["P3 Information and downstream boundary facts"], "name": "configuration compile rejection policy", "outputs": ["Enforced policy decision"], "reason": "P3 拒绝非法目标引用、Information 类型、change-data、路径或权限；Directory 解析拒绝非法 RuleViewInfo 规则或 ChangeInfo 映射；任一错误均阻止候选配置发布。", "statement": "P3 拒绝非法目标引用、Information 类型、change-data、路径或权限；Directory 解析拒绝非法 RuleViewInfo 规则或 ChangeInfo 映射；任一错误均阻止候选配置发布。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| POL-DIRECTORY-SHARED-ROLLBACK | ModelContainer shared rollback contract | {"id": "POL-DIRECTORY-SHARED-ROLLBACK", "inputs": ["P3 Information and downstream boundary facts"], "name": "ModelContainer shared rollback contract", "outputs": ["Enforced policy decision"], "reason": "Directory 只提供按顺序排列的 Action；调用方按 Action.refRule 装载多个 ModelLoader 到同一 ModelContainer。全部 Action 与 change-data 物化共享一个由 ModelContainer 承载的可回滚事务；任一 Action、grammer、update、持久化或 commit 失败都停止后续 Loader、整体回滚并中断，不继续下游。", "statement": "Directory 只提供按顺序排列的 Action；调用方按 Action.refRule 装载多个 ModelLoader 到同一 ModelContainer。全部 Action 与 change-data 物化共享一个由 ModelContainer 承载的可回滚事务；任一 Action、grammer、update、持久化或 commit 失败都停止后续 Loader、整体回滚并中断，不继续下游。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| POL-DIRECTORY-MATERIALIZE-TERMINAL | ModelContainer terminal materialization contract | {"id": "POL-DIRECTORY-MATERIALIZE-TERMINAL", "inputs": ["P3 Information and downstream boundary facts"], "name": "ModelContainer terminal materialization contract", "outputs": ["Enforced policy decision"], "reason": "只有全部 Loader 成功后才执行 grammer → update → commit；ModelContainer.execute() 正常返回后结束，不新增任何物化结果对象，evaluate 调用次数为 0，不识别或返回目标及下游结果。", "statement": "只有全部 Loader 成功后才执行 grammer → update → commit；ModelContainer.execute() 正常返回后结束，不新增任何物化结果对象，evaluate 调用次数为 0，不识别或返回目标及下游结果。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |

## 事件

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| EVT-P3-MATERIALIZATION-TARGET-PUBLISHED | Materialization target fact published | {"id": "EVT-P3-MATERIALIZATION-TARGET-PUBLISHED", "inputs": ["Validated P3 compilation candidate"], "meaning": "合法 MaterializationTargetFact 已随 Information 事实原子发布，可由 Directory 解析消费；该事件不创建 ChangeInfo 或 RuleViewInfo 映射。", "name": "Materialization target fact published", "outputs": ["Published MaterializationTargetFact event"], "reason": "合法 MaterializationTargetFact 已随 Information 事实原子发布，可由 Directory 解析消费；该事件不创建 ChangeInfo 或 RuleViewInfo 映射。", "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |

## 业务错误

| 标识 | 名称/定义 | 内容 |
|---|---|---|
| ERR-P3-DEFINITION-INVALID |  | {"condition": "类型混用/缺失引用/循环", "id": "ERR-P3-DEFINITION-INVALID", "meaning": "配置编译失败且候选不发布", "owner": "P3", "retryable": false, "stateChanged": false, "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| ERR-P3-TARGET-FACT-INVALID |  | {"condition": "目标引用/Information 类型/change-data/路径/权限非法", "id": "ERR-P3-TARGET-FACT-INVALID", "meaning": "配置编译失败且候选不发布", "owner": "P3", "retryable": false, "stateChanged": false, "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| ERR-P3-PATH-INVALID |  | {"condition": "模型声明路径不存在", "id": "ERR-P3-PATH-INVALID", "meaning": "记录 ERROR 并抛 InformationEvaluationException，禁止物化和下游", "owner": "P3", "retryable": false, "stateChanged": false, "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| ERR-P3-NULL-VALUE |  | {"condition": "普通求值读取到 null", "id": "ERR-P3-NULL-VALUE", "meaning": "记录 ERROR 并抛 InformationEvaluationException，禁止物化和下游", "owner": "P3", "retryable": false, "stateChanged": false, "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| ERR-P3-EVALUATE-FAILED |  | {"condition": "权限、表达式或只读 RuleView 执行失败", "id": "ERR-P3-EVALUATE-FAILED", "meaning": "记录 ERROR 并抛 InformationEvaluationException，禁止物化和下游", "owner": "P3", "retryable": false, "stateChanged": false, "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| ERR-DIRECTORY-MAPPING-INVALID |  | {"condition": "RuleViewInfo 规则或 ChangeInfo 映射非法", "id": "ERR-DIRECTORY-MAPPING-INVALID", "meaning": "Directory 配置编译失败且候选不发布", "owner": "Directory", "retryable": false, "stateChanged": false, "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
| ERR-DIRECTORY-EXECUTION-FAILED |  | {"condition": "Action.refRule 装载或任一 Action/grammer/update/持久化/commit 失败", "id": "ERR-DIRECTORY-EXECUTION-FAILED", "meaning": "停止后续 Loader，已建立连接逐一回滚、关闭和清理，并通过既有异常边界中断，不继续下游", "owner": "ModelContainer", "retryable": false, "stateChanged": false, "traceIds": ["TR-P3-INFORMATION-ENGINE-001"]} |
