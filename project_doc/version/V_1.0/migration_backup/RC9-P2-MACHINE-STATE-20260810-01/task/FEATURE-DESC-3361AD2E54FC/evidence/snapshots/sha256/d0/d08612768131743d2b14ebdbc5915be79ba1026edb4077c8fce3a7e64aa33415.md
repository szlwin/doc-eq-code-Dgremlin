# FEATURE-DESC-3361AD2E54FC System 与 RuleView 归属

> 文档职责：RequirementConfirmationAgent 在需求确认阶段创建并维护第 1～4、12、14 节；RequirementAnalysisAgent 在需求分析阶段补全第 5～13 节。需求文档描述“必须表现为什么”，不得把表名、字段名、API 路径、类名或框架方案作为需求结论；必要的现状证据可引用具体实现，但技术方案进入设计文档。

## 1. 需求信息

| 项目 | 内容 |
|---|---|
| 需求编号 | P2-SYSTEM-RULEVIEW |
| 需求名称 | System 与 RuleView 归属 |
| 版本目标编号 | FEATURE-DESC-3361AD2E54FC |
| 需求类型 | 架构能力增强与权限边界收敛 |
| 主责模块 | dec-core-compiler |
| 协作模块 | dec-core-context、dec-context-config-parse-xml、dec-core-starter、dec-demo |
| 受影响角色 | 配置作者、规则开发者、引擎开发者、测试人员、维护人员 |
| 当前状态 | requirement_analysis I002：当前候选 Revision |
| 对应变更需求编号 | - |

## 2. 背景与问题

### 2.1 当前行为与证据

1. P1 已建立格式无关的 Compiler Pipeline、强类型 `SystemKey` / `RuleViewKey`、不可变 Registry / EngineContext，并把 System 完整权限语义、RuleView 复合归属和运行时权限屏障明确延后到 P2；P1 已完成 `PASSED / MERGED / ARCHIVED` 闭环。
2. 当前真实 `mix` 通过 `dec-demo/src/main/resources/mix/orm-config.xml` 的 `system-file-info` 发现 `mix/system/systems.xml`；该文件声明 `user`、`order`、`payment`、`common` 四个 System，并在 System 下组织 data/view/rule-file/information/model-access 等配置事实。
3. 当前 RuleView 配置已经出现显式 `system` 归属，业务动作也已经出现 `system-ref + rule-ref` 调用事实；这证明目标配置契约需要 System 参与 RuleView 的身份与解析，而不能继续依赖裸名称全局查找。
4. 旧核心 `RuleViewInfo` 仍以 name/view/rules 为主，没有形成 System 归属；旧 XML RuleParser 仍存在通过全局配置上下文按名称解析 View/Rule 的历史路径，因此“配置里出现 system”尚不等于端到端语义已经闭合。
5. P1 编译器已经具备 `modelaccess` 相关编译结构和共享模型路径表示，但 P2 仍需把权限声明收敛为可静态判定、可运行时阻断、不可旁路的统一边界，并保证表达式、change、query 等路径语义不会各自漂移。
6. 以上当前事实以 P2 正式计划、`mix` fixture、P1 已归档 Compiler 事实以及当前源码/测试为依据；本阶段只确认必须表现出的目标与边界，不在需求文档中提前锁定具体类实现方案。

### 2.2 需要解决的问题

1. System 尚未成为贯穿加载、符号注册、引用解析、不可变发布和运行访问边界的一等编译实体，存在被降格为文档字段、上下文附属字段或按包名/路径隐式推断的风险。
2. RuleView 的唯一身份尚未端到端收敛为 `(system, name)`，裸名称全局注册/解析会导致不同 System 下同名 RuleView 冲突或误调用。
3. `model-access` 尚未形成“默认最小权限、写入默认拒绝”的统一静态与运行时权限屏障；仅在 parser/compiler 做声明解析，仍可能被 Rule/change/custom action 等运行路径旁路。
4. 模型路径若由 expression、change、query 各自解释，会产生同一路径在不同阶段含义不一致、静态检查与运行时检查不一致的问题。
5. `systems.xml` 多文件加载、重复 System、无序引用、未知 System/View/Rule/Path 等失败场景必须产生稳定 Diagnostic，且失败不得发布部分 EngineContext。
6. P2 必须明确与 P3～P8 的阶段边界，特别是旧 declaration System 只保留迁移边界说明，不能在 P2 提前删除或演化成第二套运行时。

## 3. 需求目标

1. System 成为一等编译实体，具有稳定身份、版本/来源、所属 Data/View/RuleView/model-access 等可验证关系，并进入不可变编译结果；不得仅作为说明字段，也不得通过包名、目录名或其他隐式规则推断。
2. `system-file-info` 能通过统一 Loader Pipeline 发现并合并 System 定义，支持多文件、前向引用和与输入顺序无关的确定性结果，同时拒绝重复或冲突定义。
3. RuleView 的注册、解析和调用统一使用 `(system, name)` 复合身份；新 `mix` 中 System 归属为必填语义，旧裸名称路径只能存在于明确的只读/兼容 Adapter 边界，不能成为新注册入口。
4. `model-access` 收敛为明确的访问规则：至少区分 System、目标模型/View、路径与 READ/WRITE/EXECUTE 类访问意图；遵循最小权限原则，未声明的共享写入默认拒绝。
5. 建立统一模型路径编译语义，使规则读取、变更、查询等后续能力引用同一条已验证路径语义；能静态确定的非法访问在编译期失败，只有确实依赖运行时值的动态边界进入运行时 Guard。
6. 所有实际模型变更路径都必须经过运行时权限 Guard，Rule、change、custom action 等不得存在绕过通道；权限拒绝必须发生在副作用之前。
7. 同一 RuleView 名称在不同 System 下可以安全共存且互不污染；错误 System、错误 RuleView、错误路径或越权访问产生稳定、带来源定位的诊断/拒绝结果。
8. 以真实 `systems.xml`、同名 RuleView 隔离和合法/非法 model-access 场景形成可观察验收；P2 只固定 declaration System 的迁移边界，最终旧运行时收敛留给 P7。

## 4. 范围

### 4.1 范围内

- P2-T01：确认并实现 System Raw/Compiled 模型所需的一等身份、来源与所属配置关系。
- P2-T02：确认 `system-file-info` 的统一加载语义，包括多文件、重复检测、前向引用和输入顺序无关。
- P2-T03：确认 `model-access` 的访问主体、目标、路径、访问类型、来源和默认最小权限规则。
- P2-T04：确认统一 Model Path 编译语义，作为 rule/change/query 等后续消费者的共同路径契约。
- P2-T05～T06：确认 RuleView 必须具有 System 归属，Registry/Parser/Diagnostic 使用 `(system,name)` 复合身份并校验相关引用。
- P2-T07：确认可静态判定的 model-access 越权在编译阶段阻断。
- P2-T08：确认运行时模型变更必须经过统一权限 Guard，禁止 Rule/change/custom action 旁路。
- P2-T09～T10：确认 RuleView 调用以 `system-ref + rule-ref` 解析，并验证不同 System 下同名 RuleView 隔离。
- P2-T11：确认未授权 READ/WRITE/EXECUTE、未知路径、动态路径等边界具有完整允许/拒绝矩阵。
- P2-T12：确认旧 declaration System 只作为迁移边界记录，P2 不删除旧入口，最终收敛责任保留到 P7。

### 4.2 范围外

- P3：Information 求值、依赖 DAG、物化、失效与增量重算语义。
- P4：Action/Produce 完整执行状态机；P2 仅要求未来执行入口不能绕过权限 Guard。
- P5：Directory 状态机、case、back 与流程推进语义。
- P6：QueryPlan 的完整编译/执行能力；P2 只冻结其未来必须复用统一路径与权限语义。
- P7：Session、事务、资源生命周期和 declaration 旧运行时的最终删除/收敛。
- P8：XML/YAML 全量业务语义对等与迁移完成。
- 生成大量业务 Java 类、引入新的全局 current Context、把 System 退化为包名/目录名推断规则。
- 与 P2 目标无关的 UI、可视化、数据库 schema 或业务流程重写。

### 4.3 约束与依赖

- P1 已发布的 Compiler Pipeline、强类型 Key、不可变 Registry / EngineContext 与 source-aware Diagnostic 是 P2 的前置基线，不得另起第二套编译模型。
- `mix/system/systems.xml` 是当前契约 fixture 和验收输入之一，但生产加载逻辑不得硬编码 demo、固定路径或固定 System 数量。
- 新 `mix` RuleView 必须显式声明 System；不得通过包名、文件位置、调用上下文或名称碰撞规则推断 System。
- RuleView 的规范身份为 `(system, name)`；裸名称兼容只允许存在于明确 Adapter 边界，且不得允许新代码继续注册裸名称。
- model-access 遵循最小权限；共享模型 WRITE 未显式授权时默认拒绝，不能通过“历史上可写”推导默认允许。
- 静态可判定的路径和权限必须在编译期失败；确实依赖运行时值的动态访问必须携带运行时检查要求，不能因静态不可判定而默认放行。
- 所有模型变更必须经过统一权限判定后才能产生副作用；不同调用入口不得实现各自的权限例外。
- Diagnostic 必须能定位到相关 System / RuleView / model-access / source；错误不得以 null-success、吞异常或仅日志告警方式继续发布。
- 保持 Java 8 与现有不可变 EngineContext 边界；P2 不建立新的全局可变注册中心。

### 4.4 失败语义

- 未知或重复 System、重复 `(system,name)` RuleView、未知 View/Rule 引用、非法模型路径、未声明访问权限：属于编译错误，当前编译结果不得发布。
- 新 `mix` RuleView 缺少 System：属于配置错误，不允许回退为裸名称全局查找。
- 对静态无法完整判定但语法合法的动态访问，只能发布为“必须运行时检查”的受控结果；运行时违反权限时确定性拒绝。
- 运行时权限拒绝必须发生在任何模型写入、外部副作用或状态推进之前；拒绝后模型保持原状。

### 4.5 恢复与兼容边界

- 编译失败时保留调用方已持有的上一份有效不可变 Context，不发布部分 Registry 或半完成 System。
- 旧 Config/RuleView 裸名称访问只可经显式兼容 Adapter 读取；不得允许旧入口向新 Registry 注册新事实。
- declaration System 相关旧入口在 P2 保留；只记录与新 System 模型的映射/差异和 P7 删除条件，不在 P2 直接删除。
- 若迁移映射存在无法无损表达的差异，必须形成后续 P7 可追踪问题，而不是在 P2 隐式改变固定目标语义。

### 4.6 完成标准

- 当前真实 `systems.xml` 能通过统一 System 加载/编译路径形成稳定不可变结果。
- 所有当前 `mix` RuleView 都能解析到明确 System，且 Registry/调用端使用完整复合 Key。
- 至少证明两个不同 System 下同名 RuleView 可以同时存在、分别解析和调用且无污染。
- model-access 至少覆盖允许/拒绝 READ、WRITE、EXECUTE（适用时）以及静态/动态路径边界；未授权共享 WRITE 默认拒绝。
- 静态非法引用/路径/权限失败不会发布 Context；运行时权限失败不会产生模型副作用。
- Rule、change、custom action 等模型变更入口不存在绕过权限 Guard 的可执行路径。
- declaration System 的 P7 迁移/删除边界有明确追踪，P2 不提前完成 P7 工作。

## 5. 功能列表

> P2 作为一个阶段级能力整体交付：System 身份、RuleView 复合归属和 model-access 权限边界必须同时闭合，任一部分缺失都会导致身份或权限语义不完整，因此本阶段保留一个可独立验收的功能，不拆成可单独发布的半能力。

| 功能编号 | 功能名称 | 主责模块 | 协作模块 | 关联流程 | 功能说明 | 状态 |
|---|---|---|---|---|---|---|
| P2-SYSTEM-RULEVIEW-F01 | System、RuleView 归属与模型访问边界 | dec-core-compiler | dec-core-context、dec-context-config-parse-xml、dec-core-starter、dec-demo | FLOW-CONFIG-COMPILE | 将 System 显式身份、RuleView `(system,name)` 唯一性、统一模型路径和 model-access 静态/运行时权限约束纳入同一编译与运行边界 | 已分析 |

## 6. 功能详细需求

### 6.1 P2-SYSTEM-RULEVIEW-F01 System、RuleView 归属与模型访问边界

#### 6.1.1 功能目标

将现有配置编译流程从“能识别 System/RuleView/model-access 结构”提升为“身份、归属和权限语义完整闭合”：编译输入中的 System 必须形成显式、稳定且可引用的一等身份；RuleView 必须由 `(system,name)` 唯一标识；所有模型路径和访问意图必须经同一规范化语义解析，静态可判定的非法访问在发布前失败，确实依赖运行时值的访问在副作用前由统一 Guard 判定。成功结果继续保持 P1 的不可变、确定性和原子发布属性。

#### 6.1.2 角色与权限

- 配置作者：必须显式声明 System、RuleView 归属和 model-access；不能依赖文件位置、包名、目录名、调用上下文或历史全局名称推断权限。
- 规则/流程开发者：调用 RuleView 时必须提供完整 System 归属；模型访问只能使用所属上下文已声明且被允许的规范化路径与访问类型。
- 编译器：负责确定性发现 System 定义、建立复合身份、解析引用、编译模型路径和执行静态权限校验；发现 ERROR 时不得发布候选 Context。
- 运行时模型访问者：对编译期无法完全判定但允许进入运行期的访问，必须在任何模型变更或外部副作用之前完成权限 Guard；调用入口无权自定义绕过规则。
- 兼容调用者：只能通过明确兼容边界读取旧裸名称事实；兼容入口不得成为新 Registry 的注册或写入入口。

#### 6.1.3 前置条件

- `REQCONF-P2-R02@ef30059b327d` 已通过并锁定 P2 范围、默认权限和兼容边界。
- P1 已提供统一 Source/Canonical/Raw/Compiled 编译流水线、强类型 Key、source-aware Diagnostic、不可变 Registry / EngineContext 与原子发布语义。
- 当前 `mix` 以 `system-file-info` 引用 System 定义，真实 fixture 中存在 `user`、`order`、`payment`、`common` 等显式 System 事实以及带 System 归属的 RuleView/调用事实。
- P3～P8 的完整业务执行语义仍属于后续阶段；P2 只提供其必须复用的 System、RuleView、路径和权限边界。

#### 6.1.4 正常流程

1. 编译请求通过既有 `FLOW-CONFIG-COMPILE` 发现根配置及一个或多个 System 定义源，并按稳定 source identity 形成与输入枚举顺序无关的源集合。
2. 每个 System 定义被解析为带显式名称、来源和版本/摘要参与信息的候选实体；全部 System 符号先注册，再解析其 Data/View/RuleView/model-access 等引用，以支持合法前向引用。
3. RuleView 必须解析到显式 System，并以 `(SystemKey, localName)` 形成唯一身份；同 System 同名冲突被拒绝，不同 System 同名互不冲突。
4. model-access 中的目标、路径和 READ/WRITE/EXECUTE 访问意图被解析为规范化访问事实；所有需要引用模型/View 的路径都使用同一模型路径语义。
5. 编译器对可静态判定的 System、RuleView、目标、路径和访问权限执行完整校验；任何 ERROR 使候选编译结果失败，原 Context 保持不变。
6. 静态校验通过后发布不可变 System/RuleView/access 结果；运行时调用 RuleView 使用完整 `(system,name)`，禁止退回裸名称全局查找。
7. 对确实依赖运行时值的合法动态访问，编译结果必须明确携带“需要运行时校验”的约束；运行时 Guard 在读写/执行或状态推进前决定允许或拒绝。
8. 允许的访问继续执行；拒绝的访问返回确定性拒绝结果，模型和外部状态均保持原状，并保留足以定位 System、RuleView、访问类型、规范化路径和来源的诊断事实。

#### 6.1.5 业务规则

- BR-P2-SYSTEM-RULEVIEW-001：System 必须具有显式、稳定的一等身份并进入编译结果；禁止把 System 仅保存为说明字段，禁止通过包名、目录、文件位置或调用上下文隐式推断。
- BR-P2-SYSTEM-RULEVIEW-002：`system-file-info` 可发现一个或多个 System 定义源；合法多文件与前向引用必须与输入枚举顺序无关，同一 System 的重复/冲突定义必须产生 ERROR。
- BR-P2-SYSTEM-RULEVIEW-003：新 `mix` 中 RuleView 的 System 归属为必填语义；缺失 System 不得回退为裸名称全局查找。
- BR-P2-SYSTEM-RULEVIEW-004：RuleView 的规范唯一身份为 `(system,name)`；同一 System 内同名为冲突，不同 System 下同名允许共存并必须独立解析。
- BR-P2-SYSTEM-RULEVIEW-005：新调用路径必须以 `system-ref + rule-ref` 或等价完整复合身份解析 RuleView；裸名称兼容只能经显式只读 Adapter，且不得向新 Registry 注册事实。
- BR-P2-SYSTEM-RULEVIEW-006：未知 System、未知 RuleView、RuleView 归属不一致或引用目标类型错误必须形成稳定 ERROR，并包含可定位来源；不得以 null-success、日志警告或名称猜测继续。
- BR-P2-SYSTEM-RULEVIEW-007：model-access 事实至少区分所属 System、目标模型/View、规范化路径、READ/WRITE/EXECUTE 访问类型及来源；任一维度不得由运行入口自行补默认值来扩大权限。
- BR-P2-SYSTEM-RULEVIEW-008：model-access 遵循最小权限原则；未声明的访问默认不获得权限，尤其共享模型 WRITE 未显式授权时必须拒绝。
- BR-P2-SYSTEM-RULEVIEW-009：READ、WRITE、EXECUTE 是独立访问意图；具有一种权限不得隐含获得另一种权限。
- BR-P2-SYSTEM-RULEVIEW-010：规则读取、change、query 及后续消费者必须复用同一规范化模型路径语义；路径只能按明确结构精确解析，不允许模糊匹配、跨目标搜索或静默降级。
- BR-P2-SYSTEM-RULEVIEW-011：静态可判定的未知/非法路径、目标或越权访问必须在编译期产生 ERROR；不得把可静态发现的问题推迟到运行时。
- BR-P2-SYSTEM-RULEVIEW-012：只有语法和静态引用均合法、但最终访问对象确实依赖运行时值的场景可以进入运行时校验；“静态不可完全判定”不得被解释为默认允许。
- BR-P2-SYSTEM-RULEVIEW-013：所有运行时模型读取/写入/执行入口必须遵循同一权限判定语义；Rule、change、custom action 或未来执行器不得存在绕过 Guard 的特权路径。
- BR-P2-SYSTEM-RULEVIEW-014：运行时权限拒绝必须发生在对应模型变更、外部副作用和状态推进之前；拒绝后受保护状态保持不变。
- BR-P2-SYSTEM-RULEVIEW-015：System、RuleView 和 model-access 的编译与发布必须全有或全无；任一错误不得形成部分 Registry 或半发布 Context，调用方已有有效 Context 保持可用。
- BR-P2-SYSTEM-RULEVIEW-016：相同规范化源内容、编译选项和编译器版本应产生稳定的 System/RuleView 身份集合、诊断顺序和语义摘要；文件发现顺序变化不得改变结果。
- BR-P2-SYSTEM-RULEVIEW-017：不同 CompilationSession / EngineContext 的 System、RuleView 与权限事实必须隔离，不得通过全局可变注册表互相污染。
- BR-P2-SYSTEM-RULEVIEW-018：Diagnostic/拒绝结果必须能关联相关 System、RuleView（适用时）、访问类型、规范化路径（适用时）和 SourceRef；不得暴露不必要的敏感配置内容。
- BR-P2-SYSTEM-RULEVIEW-019：P2 继续保留 declaration System 旧入口作为迁移边界，只记录与新 System 模型的映射、差异和 P7 删除条件；P2 不删除、复制或建立第二套 declaration runtime。
- BR-P2-SYSTEM-RULEVIEW-020：P2 不实现 P3 Information 求值、P4 Action/Produce 状态机、P5 Directory、P6 QueryPlan 完整执行或 P7 事务/资源收敛；这些后续能力只能复用 P2 提供的 System/RuleView/路径/权限契约，不得反向改变 P2 已冻结身份与默认权限语义。

#### 6.1.6 输入与输出约束

- 输入：由既有 Source/Frontend 边界提供的 System 定义、RuleView/System 归属、model-access 声明和相关 Data/View/Rule 引用；输入必须保留稳定 SourceRef，不允许核心逻辑依赖 demo 固定路径或固定 System 数量。
- 输出：成功时形成不可变、可确定性摘要的 System/RuleView/访问约束事实及非 ERROR Diagnostic；失败时输出稳定 ERROR/拒绝事实，不发布新 Context；动态合法访问还必须携带运行时校验约束。

#### 6.1.7 状态、幂等、并发与事务语义

- 状态：P2 不新增第二套生命周期；沿用统一编译 Session 的“候选构建 → 校验 → PUBLISHED 或 FAILED”语义。权限 Guard 对一次受保护访问只产生 ALLOW 或 DENY 结果，DENY 不进入副作用阶段。
- 幂等：同一规范化源集合、编译选项和编译器版本重复编译时，System/RuleView 复合 Key 集合、权限事实、Diagnostic 顺序和 semantic digest 保持稳定。
- 并发：并行 CompilationSession 与已发布 EngineContext 各自持有不可变身份/权限事实；禁止依赖全局 current Context 或共享可变 Registry。运行时 Guard 只基于调用方明确持有的 Context 判定。
- 事务/部分失败：编译候选采用原子发布，任一 ERROR 丢弃本次候选并保留旧 Context；运行时权限 DENY 必须在该次受保护操作产生模型或外部副作用前返回。跨多个业务步骤的事务/补偿由 P7 负责，P2 不提前定义其完整事务模型。

#### 6.1.8 异常与禁止副作用

- 重复/未知 System、缺失 RuleView System、同 System 同名 RuleView、未知复合引用、非法规范化路径或静态越权均不得发布新 Context。
- 静态不可完全判定的动态访问若进入运行期，必须经过 Guard；Guard 不可用、上下文不匹配或权限无法确定时按拒绝处理，不允许 fail-open。
- 权限 DENY 不得修改模型、推进状态、调用外部副作用或通过另一调用入口重试为“绕过权限”的成功。
- 兼容 Adapter 不得写入新 Registry；declaration 旧入口不得在 P2 被复制到新模块或悄然升级为第二套运行时。

## 7. 跨功能规则

- CR-P2-SYSTEM-RULEVIEW-001：发布不变量——只有 System 身份、RuleView 复合归属、模型路径和静态权限校验全部成功时，候选编译结果才允许作为同一不可变 Context 发布；任何部分成功都视为整体失败。
- CR-P2-SYSTEM-RULEVIEW-002：身份不变量——编译、Diagnostic、Runtime Lookup 和权限判定必须使用同一个显式 System 身份与 RuleView `(system,name)` 语义，不得在不同层重新推断或转换为裸名称。
- CR-P2-SYSTEM-RULEVIEW-003：权限不变量——兼容 Adapter、动态路径、Rule/change/custom action 或后续执行模块均不得扩大编译时声明的权限；无法证明允许时按拒绝处理，WRITE 尤其遵循显式授权。
- CR-P2-SYSTEM-RULEVIEW-004：阶段边界不变量——P2 提供 P3～P7 可复用的身份/路径/权限契约，但不得以“为后续准备”为由提前实现后续阶段完整运行语义或删除 declaration runtime。

## 8. 异常与边界场景

| 场景编号 | 关联功能 | 场景 | 预期结果 | 禁止副作用 |
|---|---|---|---|---|
| EX-P2-SYSTEM-RULEVIEW-001 | P2-SYSTEM-RULEVIEW-F01 | 多个 System 文件以不同枚举顺序提供相同合法定义 | 编译得到相同 System/RuleView 集合、Diagnostic 顺序和 semantic digest | 不依赖文件系统偶然顺序 |
| EX-P2-SYSTEM-RULEVIEW-002 | P2-SYSTEM-RULEVIEW-F01 | 同一 System 在多个源重复或冲突定义 | 产生带双方来源的稳定 ERROR，候选发布失败 | 不静默覆盖，不部分发布其它 System |
| EX-P2-SYSTEM-RULEVIEW-003 | P2-SYSTEM-RULEVIEW-F01 | 新 RuleView 缺失 System | 产生配置 ERROR | 不回退裸名称查找，不猜测所属 System |
| EX-P2-SYSTEM-RULEVIEW-004 | P2-SYSTEM-RULEVIEW-F01 | 同一 System 内出现同名 RuleView | 产生复合 Key 冲突 ERROR | 不覆盖先定义项，不发布候选 Context |
| EX-P2-SYSTEM-RULEVIEW-005 | P2-SYSTEM-RULEVIEW-F01 | 不同 System 下存在同名 RuleView | 两者同时存在，按各自 `(system,name)` 独立解析 | 不互相覆盖或串用规则 |
| EX-P2-SYSTEM-RULEVIEW-006 | P2-SYSTEM-RULEVIEW-F01 | 调用提供未知 system-ref、错误 rule-ref 或只提供裸 rule name | 解析失败并给出稳定定位 | 不跨 System 搜索同名项，不回退全局 Registry |
| EX-P2-SYSTEM-RULEVIEW-007 | P2-SYSTEM-RULEVIEW-F01 | model-access 的目标或路径可静态判定为不存在/非法 | 编译期 ERROR 并拒绝发布 | 不推迟为运行时“再试一次” |
| EX-P2-SYSTEM-RULEVIEW-008 | P2-SYSTEM-RULEVIEW-F01 | 对共享模型执行未声明 WRITE，或以 READ 权限尝试 WRITE/EXECUTE | 确定性拒绝 | 不修改模型，不扩大权限类型 |
| EX-P2-SYSTEM-RULEVIEW-009 | P2-SYSTEM-RULEVIEW-F01 | 路径静态结构合法但最终对象确实依赖运行时值 | 发布受控 runtime-check-required 事实，运行时由 Guard ALLOW/DENY | 不因静态未知而默认允许 |
| EX-P2-SYSTEM-RULEVIEW-010 | P2-SYSTEM-RULEVIEW-F01 | Rule/change/custom action 尝试从不同入口访问同一受保护路径 | 所有入口得到一致权限判定 | 不存在可执行旁路或入口特权 |
| EX-P2-SYSTEM-RULEVIEW-011 | P2-SYSTEM-RULEVIEW-F01 | 本轮编译在 System/RuleView/access 校验中任一处失败 | 本轮结果 FAILED，调用方继续使用旧有效 Context | 不发布部分 Registry，不污染并存 Context |
| EX-P2-SYSTEM-RULEVIEW-012 | P2-SYSTEM-RULEVIEW-F01 | declaration System 与新 System 模型存在暂时无法无损映射的差异 | 保留旧入口并登记 P7 可追踪迁移差异 | P2 不删除旧入口，不复制旧 runtime 实现 |

## 9. 验收标准

### AC-P2-SYSTEM-RULEVIEW-001 System 定义确定性编译

Given 当前真实 `systems.xml` 以及语义等价的多文件/重排输入
When 通过统一配置编译流程发现并编译 System 定义
Then 当前 fixture 的显式 System 均形成稳定一等身份，多文件和输入顺序变化不改变合法结果或 semantic digest
And 重复/冲突 System 会产生稳定 ERROR，且失败不发布任何部分候选 Context。

### AC-P2-SYSTEM-RULEVIEW-002 RuleView 复合身份与隔离

Given 两个不同 System 均声明同名 RuleView，并另有同一 System 内重复同名的失败 fixture
When 执行符号注册、引用解析和 Registry 发布
Then 跨 System 同名项以各自 `(system,name)` 同时存在且互不污染，同 System 重复项被拒绝
And 新 RuleView 缺失 System 时不会回退为裸名称全局注册。

### AC-P2-SYSTEM-RULEVIEW-003 RuleView 复合调用

Given 已发布的不同 System 同名 RuleView 和包含 `system-ref + rule-ref` 的调用事实
When 以正确/错误 System、正确/错误 RuleView 以及裸名称分别发起解析
Then 只有完整且存在的复合身份解析到目标 RuleView，错误或缺失身份得到稳定失败
And 不发生跨 System 同名搜索、裸名称 fallback 或错误 RuleView 执行。

### AC-P2-SYSTEM-RULEVIEW-004 model-access 权限矩阵

Given 对同一模型/View/path 分别声明 READ、WRITE、EXECUTE 的允许与未声明组合
When 编译权限事实并对各访问类型执行判定
Then 每种访问意图仅在自身明确授权时允许，未声明共享 WRITE 默认拒绝，权限类型互不隐含
And 拒绝路径不会修改模型、推进状态或触发外部副作用。

### AC-P2-SYSTEM-RULEVIEW-005 统一模型路径与静态阻断

Given rule/change/query 等消费者引用同一组合法路径、未知路径、穿越非法结构或目标不匹配路径
When 路径进入统一规范化和静态校验
Then 合法路径得到一致规范化身份，静态非法路径/目标在编译期产生 source-aware ERROR 并阻断发布
And 不使用模糊匹配、跨 View 搜索或不同消费者各自解释同一路径。

### AC-P2-SYSTEM-RULEVIEW-006 动态访问运行时 Guard

Given 一条静态结构合法但最终对象依赖运行时值的访问，以及允许和拒绝两种运行态
When 该访问到达受保护读取/写入/执行边界
Then 编译结果明确要求 runtime check，Guard 对允许态放行、对拒绝态在副作用前 DENY
And “静态不可完全判定”不会被当作默认允许，Guard 不可用时也不会 fail-open。

### AC-P2-SYSTEM-RULEVIEW-007 所有变更入口不可旁路

Given Rule、change、custom action 三类入口尝试访问相同受保护模型路径
When 分别执行允许和未授权场景
Then 三类入口遵循同一 System、路径和访问类型权限结果，未授权场景全部被阻断
And 不存在跳过 Guard 的可执行分支、兼容入口或二次 fallback。

### AC-P2-SYSTEM-RULEVIEW-008 原子发布与 Context 隔离

Given 一个已发布有效 Context、一个成功新编译、一个含 System/RuleView/access ERROR 的失败编译以及两个并行独立 Context
When 执行编译和发布
Then 成功候选全量发布，失败候选完全不发布且旧 Context 保持可用，并行 Context 的 Registry/权限事实互不污染
And 不创建新的全局 current Context 或共享可变 Registry。

### AC-P2-SYSTEM-RULEVIEW-009 Diagnostic 可定位且确定

Given 重复 System、缺失 RuleView System、未知复合引用、非法路径和越权访问样例
When 连续重复执行编译或运行时拒绝
Then Diagnostic/拒绝结果稳定关联 System、RuleView、访问类型、规范化路径和 SourceRef 中适用字段，排序/错误码可重复
And 不以 null-success、吞异常或仅控制台日志替代失败结果，也不泄露不必要敏感配置。

### AC-P2-SYSTEM-RULEVIEW-010 declaration 迁移边界保持

Given 当前 declaration System 旧入口和 P2 新 System 编译模型
When 完成 P2 并执行兼容/残留检查
Then 旧入口仅作为明确迁移边界保留，其映射/差异和 P7 删除条件可追踪，P2 新代码不依赖第二套 declaration runtime
And P2 不提前删除旧入口、不复制旧实现，也不实现 P3～P7 完整业务执行语义。

## 10. 非功能要求

### 10.1 性能

- System/RuleView/权限身份和规范化模型路径在编译阶段形成可复用结果；运行时权限判定不得为每次访问重新解析原始 XML/YAML 或进行全局模糊名称扫描。
- 相同输入的确定性结果优先于不受控并行优化；后续设计/测试应建立可重复基线，不在需求阶段虚构绝对时延阈值。

### 10.2 安全与权限

- 权限采用 fail-closed 与最小授权；无法证明允许时拒绝，未声明共享 WRITE 默认拒绝。
- READ/WRITE/EXECUTE 分离，任何兼容、动态路径或调用入口都不得扩大权限。
- Diagnostic 只输出定位和判定所需信息，不泄露不必要敏感配置或运行数据。

### 10.3 可靠性、一致性与恢复

- System/RuleView/权限事实随不可变 Context 原子发布；编译失败保留旧有效 Context。
- 相同输入得到稳定身份集合、Diagnostic 顺序与摘要；并存 Context 不共享可变 Registry。
- 运行时权限拒绝必须在副作用前完成，拒绝后模型状态保持不变。

### 10.4 审计与可观测性

- 编译 ERROR 和运行时 DENY 必须能追踪到 System、RuleView、访问类型、规范化路径、SourceRef 中适用字段及稳定错误/拒绝分类。
- 同名 RuleView 隔离、静态越权、动态 Guard 和旁路阻断必须具备可重复测试证据。

### 10.5 兼容与历史数据

- 保持 P1 的 Java 8、不可变 Context 和只读兼容边界。
- 裸 RuleView 名称只限显式兼容读取，不允许新注册/写入。
- declaration System 在 P2 不删除；其迁移差异必须显式进入 P7 追踪，禁止在 P2 隐式改变旧行为或建立第二运行时。

## 11. 模块职责边界

### 11.1 dec-core-compiler 模块

负责：

- 在现有统一编译流水线中建立 System 一等身份、RuleView 复合归属、规范化模型路径、model-access 静态校验和可发布的不可变约束事实。
- 保持确定性 Source/Diagnostic/digest 与全有或全无发布语义。

不负责：

- P3 Information 求值、P4 Action/Produce 完整执行、P5 Directory、P6 QueryPlan 完整执行、P7 事务/资源生命周期和 declaration 最终删除。
- 数据库访问、demo 固定路径、业务 Java 代码生成或新的全局可变 Context。

### 11.2 协作模块

| 模块 | 负责内容 | 输入/输出业务事实 | 失败责任 |
|---|---|---|---|
| dec-core-context | 承载不可变 SystemKey/RuleViewKey 及 Context 可消费的编译事实边界 | 编译后的稳定身份/权限事实 → 不可变 Context 视图 | 不可变性、Key 值语义和 Context 隔离失败 |
| dec-context-config-parse-xml | 把显式 System/RuleView/model-access 配置事实交给统一编译输入，不自行决定最终权限 | 配置节点/SourceRef → 可编译结构事实 | 格式、缺失必填归属和来源提取失败 |
| dec-core-starter | 组装 SourceProvider/编译请求并持有调用方 Context 发布边界 | 根 Source/发布期望 → CompilationResult/Context | 组装、错误传播或错误二次发布 |
| dec-demo | 提供真实 `mix` System、RuleView、model-access 与负向契约 fixture | fixture/测试预期，不作为核心生产依赖 | fixture 漂移或契约测试缺失 |
| 后续 P3～P7 消费者 | 复用 P2 System、RuleView、规范化路径和权限结果 | 已编译身份/权限契约 → 后续执行语义 | 不得自行推断身份、重解释路径或扩大权限；完整运行语义由对应阶段负责 |

### 11.3 协作边界

P2 继续复用 `FLOW-CONFIG-COMPILE`：Source/frontends/starter 提供明确输入，compiler 拥有候选构建、身份/引用/路径/静态权限校验和原子发布协调，context 只承载不可变结果。运行时权限判定是后续模型访问的前置边界，不允许任何消费模块把 P2 的编译事实重新解释为更宽权限。核心 compiler 不反向依赖 demo、数据库或具体业务模块；兼容 Adapter 和 declaration 旧入口不得写入新 Registry。

## 12. 待确认事项与已确认决策

### 12.1 待确认事项

| 决策编号 | 问题 | 可选方案 | 推荐方案 | 是否阻塞 | 责任人/Agent | 状态 |
|---|---|---|---|---|---|---|
| - | 当前没有会改变 P2 范围、默认权限或兼容策略的阻塞决策 | - | - | 否 | RequirementConfirmationAgent | 已确认 |

> P2 的核心目标语义已经由正式 P0—P8 计划、P1 Deferred 边界、已登记 Request Intake 以及用户在 PR #33 合并后明确要求继续执行共同锁定。后续若发现必须在“改变固定目标语义”与“保留当前语义”之间选择，则立即停止 auto 推进并重新进入授权决策。

### 12.2 已确认决策

| 决策编号 | 结论 | 原因 | 证据/确认来源 | supersedes |
|---|---|---|---|---|
| DEC-P2-SYSTEM-RULEVIEW-001 | System 是一等编译实体；禁止仅作为说明字段或按包名/目录隐式推断 | System 必须成为可注册、可引用、可诊断、可发布的稳定身份，否则 RuleView 与权限边界无法确定 | P2 正式计划、P1 Deferred 边界、Request Intake | - |
| DEC-P2-SYSTEM-RULEVIEW-002 | RuleView 唯一身份为 `(system,name)`；新 mix 必须显式 System，裸名称只限兼容 Adapter | 防止跨 System 同名冲突和误调用，保持配置契约显式可验证 | P2 正式计划、当前 mix `system` / `system-ref` 事实 | - |
| DEC-P2-SYSTEM-RULEVIEW-003 | model-access 采用最小权限；共享 WRITE 未声明即拒绝；静态不可判定的合法动态访问必须运行时检查 | 权限缺省放行会把配置遗漏升级为数据越权，且无法保证所有变更入口一致 | P2 正式计划、Request Intake 权限影响评估 | - |
| DEC-P2-SYSTEM-RULEVIEW-004 | rule/change/query 等后续消费者必须复用统一模型路径语义 | 避免同一路径在不同编译/运行入口产生不一致解释 | P2-T04 目标、P1 model-access 路径基线 | - |
| DEC-P2-SYSTEM-RULEVIEW-005 | P2 只定义 declaration System 迁移边界，不删除旧入口；最终 runtime 收敛留给 P7 | 避免 P2 越权完成后续阶段并破坏可回归迁移顺序 | P2-T12、P7 阶段边界 | - |

## 13. 追踪关系

> P2 复用既有 `FLOW-CONFIG-COMPILE`，不创建第二条“System 编译流程”。每条功能、业务规则、跨功能规则和验收标准至少被一条 trace 引用；业务模型、设计和测试 Case 在对应后续阶段补充并通过同一 trace 更新。

| 追踪编号 | 功能编号 | 业务规则/跨功能规则 | 验收标准 | 关联流程 | 影响分析 | 后续业务模型 | 后续设计 | 测试 Case | 状态 |
|---|---|---|---|---|---|---|---|---|---|
| TR-P2-SYSTEM-RULEVIEW-001 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-001、BR-P2-SYSTEM-RULEVIEW-002、BR-P2-SYSTEM-RULEVIEW-016、CR-P2-SYSTEM-RULEVIEW-001 | AC-P2-SYSTEM-RULEVIEW-001 | FLOW-CONFIG-COMPILE | REQAN-P2-R01：System 加载、重复定义、确定性与原子发布 | 待业务建模 | 待设计 | 待 TestDesign | COVERED |
| TR-P2-SYSTEM-RULEVIEW-002 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-003、BR-P2-SYSTEM-RULEVIEW-004、BR-P2-SYSTEM-RULEVIEW-017、CR-P2-SYSTEM-RULEVIEW-002 | AC-P2-SYSTEM-RULEVIEW-002 | FLOW-CONFIG-COMPILE | REQAN-P2-R01：RuleView 复合 Key、同名隔离与 Context 隔离 | 待业务建模 | 待设计 | 待 TestDesign | COVERED |
| TR-P2-SYSTEM-RULEVIEW-003 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-005、BR-P2-SYSTEM-RULEVIEW-006、CR-P2-SYSTEM-RULEVIEW-002 | AC-P2-SYSTEM-RULEVIEW-003 | FLOW-CONFIG-COMPILE | REQAN-P2-R01：复合调用、错误引用与禁止裸名称 fallback | 待业务建模 | 待设计 | 待 TestDesign | COVERED |
| TR-P2-SYSTEM-RULEVIEW-004 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-007、BR-P2-SYSTEM-RULEVIEW-008、BR-P2-SYSTEM-RULEVIEW-009、CR-P2-SYSTEM-RULEVIEW-003 | AC-P2-SYSTEM-RULEVIEW-004 | FLOW-CONFIG-COMPILE | REQAN-P2-R01：READ/WRITE/EXECUTE 最小权限矩阵 | 待业务建模 | 待设计 | 待 TestDesign | COVERED |
| TR-P2-SYSTEM-RULEVIEW-005 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-010、BR-P2-SYSTEM-RULEVIEW-011 | AC-P2-SYSTEM-RULEVIEW-005 | FLOW-CONFIG-COMPILE | REQAN-P2-R01：统一 ModelPath 与静态非法路径阻断 | 待业务建模 | 待设计 | 待 TestDesign | COVERED |
| TR-P2-SYSTEM-RULEVIEW-006 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-012、BR-P2-SYSTEM-RULEVIEW-014、CR-P2-SYSTEM-RULEVIEW-003 | AC-P2-SYSTEM-RULEVIEW-006 | FLOW-CONFIG-COMPILE | REQAN-P2-R01：动态访问 runtime-check-required 与 fail-closed | 待业务建模 | 待设计 | 待 TestDesign | COVERED |
| TR-P2-SYSTEM-RULEVIEW-007 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-013、BR-P2-SYSTEM-RULEVIEW-014、CR-P2-SYSTEM-RULEVIEW-003 | AC-P2-SYSTEM-RULEVIEW-007 | FLOW-CONFIG-COMPILE | REQAN-P2-R01：Rule/change/custom action 统一 Guard 与旁路阻断 | 待业务建模 | 待设计 | 待 TestDesign | COVERED |
| TR-P2-SYSTEM-RULEVIEW-008 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-015、BR-P2-SYSTEM-RULEVIEW-016、BR-P2-SYSTEM-RULEVIEW-017、CR-P2-SYSTEM-RULEVIEW-001 | AC-P2-SYSTEM-RULEVIEW-008 | FLOW-CONFIG-COMPILE | REQAN-P2-R01：原子发布、旧 Context 保留和并行隔离 | 待业务建模 | 待设计 | 待 TestDesign | COVERED |
| TR-P2-SYSTEM-RULEVIEW-009 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-006、BR-P2-SYSTEM-RULEVIEW-018、CR-P2-SYSTEM-RULEVIEW-002 | AC-P2-SYSTEM-RULEVIEW-009 | FLOW-CONFIG-COMPILE | REQAN-P2-R01：source-aware Diagnostic/拒绝结果与确定性 | 待业务建模 | 待设计 | 待 TestDesign | COVERED |
| TR-P2-SYSTEM-RULEVIEW-010 | P2-SYSTEM-RULEVIEW-F01 | BR-P2-SYSTEM-RULEVIEW-019、BR-P2-SYSTEM-RULEVIEW-020、CR-P2-SYSTEM-RULEVIEW-004 | AC-P2-SYSTEM-RULEVIEW-010 | FLOW-CONFIG-COMPILE | REQAN-P2-R01：declaration→P7 迁移边界及 P3～P7 阶段依赖 | 待业务建模 | 待设计 | 待 TestDesign | COVERED |

## 14. 变更记录

| 文档 revision | 日期 | 阶段 | 变更内容 | 责任 Agent |
|---|---|---|---|---|
| REQCONF-P2-R01 | 2026-08-07 | 需求确认 | 基于已合并 P2 Request Intake 锁定 System 一等实体、RuleView `(system,name)`、model-access 最小权限、失败/恢复语义及 P2 与 P3～P8 边界 | RequirementConfirmationAgent |
| REQCONF-P2-R02 | 2026-08-07 | 需求确认 | 仅规范化模板 Markdown 行尾以满足 Git checkpoint；P2 目标、范围、决策、失败与恢复语义与 R01 完全一致 | RequirementConfirmationAgent |
| REQAN-P2-R01 | 2026-08-07 | 需求分析 | 补全 System/RuleView/model-access 业务规则、异常、验收、非功能、模块边界及 FLOW-CONFIG-COMPILE 追踪；不改变 REQCONF-P2-R02 核心语义 | RequirementAnalysisAgent |
