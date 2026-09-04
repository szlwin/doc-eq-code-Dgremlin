<!-- template: common-develop/test-case-v1 -->
# FEATURE-DESC-4AB41AC241A1 测试设计

> Test revision：`TESTDESIGN-P3-R03`
> Requirement：`FEATURE-DESC-4AB41AC241A1`
> Requirement revision：`REQAN-P3-R05`
> Design revision：`DESIGN-P3-R08`
> Business Model revision：`BM-P3-R04`
> Business Flow revision：`FLOW-R08@p3-information-evaluation`
> 状态：草稿；本阶段只登记可执行 Case，不执行生产代码测试

## 1. 测试范围与环境

- 测试目标：验证 P3 Information 编译、识别、ChangeInfo RuleView 生成/注册/映射以及物化边界。
- 范围内：16 个 Information 的分类与 DAG、TRUE/FALSE/ERROR、`change-data`、`change-info@information-ref`、`grammer → update`、最后一个 Action 成功后的 P5 调用接缝和失败阻断。
- 范围外：P4 Action/Produce 实现、P5 Directory 实现本身、多目录/多 ChangeInfo/并发隔离、数据库连接选择、物化后的独立 evaluate。
- 环境与账号：Java 8；仓库现有 `mix` fixture；数据库连接由现有 ModelContainer 测试夹具管理。
- 共用前置条件：P1/P2 System、View、RuleView 和显式 model-access binding 可解析；`dec-demo/src/test/resources/mix/business/order-business.xml` 保持当前声明。

## 2. Case 索引

| Case ID | 名称 | 功能/验收 | 页面/操作 | 流程/步骤 | 测试层级 | 自动化状态 |
|---|---|---|---|---|---|---|
| CASE-P3-MIX-001 | 16 项 Information 编译与只读识别 | AC-P3-INFORMATION-ENGINE-001 | N/A | FLOW-P3-INFORMATION-EVALUATION / COMPILE, IDENTIFY | 单元+集成 | 待实现后自动化 |
| CASE-P3-ERROR-001 | 编译/识别错误与物化失败阻断 | AC-P3-INFORMATION-ENGINE-001 | N/A | FLOW-P3-INFORMATION-EVALUATION / failure paths | 单元+集成 | 待实现后自动化 |
| CASE-P3-CHANGE-INFO-001 | ChangeInfo RuleView 编译期生成注册与 Directory 映射 | AC-P3-INFORMATION-ENGINE-001 | N/A | FLOW-P3-INFORMATION-EVALUATION / COMPILE | 集成 | 待实现后自动化 |
| CASE-P3-DIRECTORY-001 | 最后 Action 成功后复用 ChangeInfo RuleView | AC-P3-INFORMATION-ENGINE-001 | N/A | FLOW-P3-INFORMATION-EVALUATION / MATERIALIZE | 集成 | P5 实现后自动化 |

## 3. 具体测试 Case

<!-- TEST-CASE: CASE-P3-MIX-001 -->
<a id="CASE-P3-MIX-001"></a>
## CASE-P3-MIX-001 16 项 Information 编译与只读识别

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | AC-P3-INFORMATION-ENGINE-001、BR-P3-INFORMATION-ENGINE-001～003 | [需求文档](requirement.md#AC-P3-INFORMATION-ENGINE-001) |
| 设计/契约 | DESIGN-P3-R08、IMPL-DEC-P3-003、BP-P3-001～003 | [关联设计](FEATURE-DESC-4AB41AC241A1_design.md#实施策略决策) |
| 流程/步骤 | FLOW-P3-INFORMATION-EVALUATION、STEP-P3-INFORMATION-EVALUATION-COMPILE、STEP-P3-INFORMATION-EVALUATION-IDENTIFY | [Flow](../../_flows/COMPILER/generated/COMPILER_flow.preview.md#flow-p3-information-evaluation) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| Information fixture | user/order/payment/common 共 16 项 | 加载 `dec-demo/src/main/resources/mix` | Requirement 2.1 |
| Order status | 1、2、3、4 | ModelData | ordered/paying/success/error 的 change-data |
| Order detail list | 至少一条；另准备空集合 | ModelData | 订单明细非空规则 |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 编译 mix 全部 Information | InformationCompiler.compile | 7 个 RuleView 原子、4 个模型表达式原子、5 个复合 Information 形成唯一 Key 和无环 DAG |
| 2 | 识别满足条件的原子及复合 Information | InformationEngine.evaluate | 返回 TRUE，并带 readPaths、依赖结果和版本证据；无模型写入 |
| 3 | 将当前模型值改为不满足条件后再次识别 | InformationEngine.evaluate | 返回 FALSE，结果反映本次新读取，不消费旧缓存或 MutationSet |
| 4 | 对关系字段使用 `orderDetailList` | InformationEngine.evaluate | 按独立 `orderDetail` Data 解析；空集合的通用 `every` 为 TRUE，但订单 Information 返回 FALSE 并可带 `ORDER_DETAIL_REQUIRED` |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| 编译结果 | 16 个唯一 Key、无环 DAG、准确 read/write path | 断言编译对象和诊断 | 数量、归属、拓扑和路径全部匹配 fixture |
| 识别结果 | TRUE/FALSE；错误不伪装为 FALSE | 断言 IdentificationResult、read evidence 和模型写入计数 | 两次调用读取当前值且 evaluate 无写入 |

### 禁止副作用与清理

- 禁止副作用：evaluate 修改模型、生成 ChangeInfo RuleView、调用 materialize 或把 ERROR 转为 FALSE。
- Case 完成后的状态：编译集合可供后续 Case 使用；模型恢复初始值。
- 清理要求：释放测试 ModelContainer，恢复 fixture 模型值。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| `dec-demo/src/main/resources/mix` | 现有 ModelContainer fixture cleanup | 待创建 `InformationCompilerTest`、`InformationEngineTest` | `./mvnw -pl dec-core-compiler,dec-core-model,dec-core-context -Dtest=InformationCompilerTest,InformationEngineTest test` | 待实现后自动化 |

<!-- TEST-CASE: CASE-P3-ERROR-001 -->
<a id="CASE-P3-ERROR-001"></a>
## CASE-P3-ERROR-001 编译/识别错误与物化失败阻断

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | AC-P3-INFORMATION-ENGINE-001、EX-P3-INFORMATION-ENGINE-001～003 | [需求文档](requirement.md#AC-P3-INFORMATION-ENGINE-001) |
| 设计/契约 | DESIGN-P3-R08、IMPL-DEC-P3-001、IMPL-DEC-P3-004 | [关联设计](FEATURE-DESC-4AB41AC241A1_design.md#需求映射与总变更清单) |
| 流程/步骤 | FAIL-P3-INFORMATION-COMPILE、FAIL-P3-INFORMATION-EVALUATE、FAIL-P3-INFORMATION-MATERIALIZE | [Flow](../../_flows/COMPILER/generated/COMPILER_flow.preview.md#flow-p3-information-evaluation) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| 缺失引用 | `common.missing` | 非法 Information fixture | 发布前拒绝 |
| 非法模型路径 | `OrderInfo.notExists` | 非法 model expression | ERROR + exception |
| 普通 null | `OrderInfo.status = null` | ModelData | 显式 InformationKey=null 除外 |
| 写入失败 | update writer 抛 PersistenceException | 受控 ModelContainer fixture | 触发 rollback |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 编译缺失引用、循环、非法路径或越权 change-data | InformationCompiler.compile | 候选被拒绝，旧 EngineContext 不变，产生可定位 ERROR |
| 2 | 对非法路径、普通 null、表达式或只读 RuleView 错误调用 evaluate | InformationEngine.evaluate | 抛 InformationEvaluationException，状态为 ERROR，不进入物化或下游 |
| 3 | 让 grammer、update 或 commit 失败 | InformationMaterializer.materialize | 抛 InformationMaterializationException，ModelContainer rollback，后续步骤调用计数为 0 |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| 编译/识别 | ERROR、sourceLocation、Key/path、异常类型 | 断言诊断与异常 | ERROR 不被转换为 FALSE |
| 模型事务 | 回滚至写入前值 | 查询 ModelData 和容器生命周期事件 | 无部分提交、无下游继续 |

### 禁止副作用与清理

- 禁止副作用：错误发布候选、写入部分成功、吞异常、继续 Directory 上行/下游或调用 evaluate。
- Case 完成后的状态：旧配置和写入前模型值保持不变。
- 清理要求：关闭受控连接并清除失败 fixture。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| 非法 Information fixture、失败 writer | ModelContainer rollback/clear | 待创建 `InformationFailureTest`、`InformationMaterializerTest` | `./mvnw -pl dec-core-compiler,dec-core-model -Dtest=InformationFailureTest,InformationMaterializerTest test` | 待实现后自动化 |

<!-- TEST-CASE: CASE-P3-CHANGE-INFO-001 -->
<a id="CASE-P3-CHANGE-INFO-001"></a>
## CASE-P3-CHANGE-INFO-001 ChangeInfo RuleView 编译期生成注册与 Directory 映射

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | BR-P3-INFORMATION-ENGINE-004、DEC-P3-INFORMATION-ENGINE-010 | [需求文档](requirement.md#BR-P3-INFORMATION-ENGINE-004) |
| 设计/契约 | DESIGN-P3-R08、IMPL-DEC-P3-005、BP-P3-005 | [关联设计](FEATURE-DESC-4AB41AC241A1_design.md#需求映射与总变更清单) |
| 流程/步骤 | STEP-P3-INFORMATION-EVALUATION-COMPILE | [Flow](../../_flows/COMPILER/generated/COMPILER_flow.preview.md#flow-p3-information-evaluation) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| business-config name | `order-payment` | `order-business.xml` | 名称生成规则 |
| directory name | `paying` | `order-business.xml` | DirectoryInfo |
| change-info information-ref | `order.paying` | `<change-info information-ref="order.paying"/>` | ChangeInfo |
| target view | `OrderInfo` | 关联 Information 的 `view-ref` | model binding |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 编译解析 Directory 与 ChangeInfo | XML compiler | ChangeInfo 保存 `information-ref`，并与 DirectoryInfo 绑定 |
| 2 | 生成 RuleViewInfo 并注册 | ConfigInfo.addRuleViewInfo | 仅生成一次，名称为 `##order-payment.paying.order.paying` |
| 3 | 检查规则顺序和目标绑定 | RuleViewInfo.rules | 只有 `grammer`、`update` 两条规则且顺序固定；ViewData 绑定 `OrderInfo` |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| ChangeInfo | RuleViewInfo 映射 | 检查 ChangeInfo 映射对象身份 | 映射非空且与 ConfigInfo 注册对象相同 |
| ConfigInfo | `##order-payment.paying.order.paying` | 查询 RuleViewInfo name | 名称、规则数、顺序和 ViewData 全部匹配 |

### 禁止副作用与清理

- 禁止副作用：在 Action 完成后才生成 RuleViewInfo、运行时重复生成、改变 `directory@information-ref` 语义。
- Case 完成后的状态：编译候选包含 ChangeInfo 映射并可被 P5 消费。
- 清理要求：丢弃候选 ConfigInfo，保留当前已安装配置。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| `dec-demo/src/test/resources/mix/business/order-business.xml` | 丢弃候选 ConfigInfo | 待创建 `ChangeInfoParserTest` | `./mvnw -pl dec-context-config-parse-xml,dec-core-context -Dtest=ChangeInfoParserTest test` | 待实现后自动化 |

<!-- TEST-CASE: CASE-P3-DIRECTORY-001 -->
<a id="CASE-P3-DIRECTORY-001"></a>
## CASE-P3-DIRECTORY-001 最后 Action 成功后复用 ChangeInfo RuleView

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | BR-P3-INFORMATION-ENGINE-004、DEC-P3-INFORMATION-ENGINE-011 | [需求文档](requirement.md#AC-P3-INFORMATION-ENGINE-001) |
| 设计/契约 | DESIGN-P3-R08、BP-P3-005 | [关联设计](FEATURE-DESC-4AB41AC241A1_design.md#行为调整与代码改动) |
| 流程/步骤 | STEP-P3-INFORMATION-EVALUATION-MATERIALIZE | [Flow](../../_flows/COMPILER/generated/COMPILER_flow.preview.md#flow-p3-information-evaluation) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| Action sequence | 一个或多个 Action，最后一个为成功的 `startPay` | P5 Directory fixture | P5 按 `DirectoryInfo.actions` 顺序编排 |
| ChangeInfo mapping | `order.paying → ##order-payment.paying.order.paying` | 编译结果 | P3 编译期事实 |
| ModelLoader.value | 同一物化调用内的共享 value | ModelContainer fixture | grammer/update 共用 |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 依次执行所有 Action | P5 Directory executor | 前序 Action 成功后才执行下一个；最后一个 Action 成功 |
| 2 | 追加映射的 ChangeInfo RuleView loader | DirectoryChangeInfoHook | 使用已存在 RuleViewInfo，不重新生成，不调用 evaluate |
| 3 | 调用 ModelContainer.execute | InformationMaterializer | `grammer → update → commit`，成功返回 MaterializationResult |
| 4 | 让最后一个 Action 失败重复执行 | P5 Directory executor | 不追加 ChangeInfo loader，不写目标模型，不继续下游 |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| 调用事件 | Action 与 ChangeInfo loader 顺序 | 事件记录/调用计数 | ChangeInfo 仅在最后 Action 成功后执行一次 |
| 物化结果 | commit + 变更路径和提交结果 | 断言 MaterializationResult 与 ModelData | 规则顺序正确，evaluate 次数为 0 |

### 禁止副作用与清理

- 禁止副作用：最后 Action 前物化、Action 失败后写入、物化成功后自动 evaluate 或目标/下游重识别。
- Case 完成后的状态：成功分支提交目标值；失败分支恢复原值。
- 清理要求：使用既有 ModelContainer close/clear，恢复订单 fixture。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| `order-business.xml` 与受控 Action fixture | ModelContainer cleanup | P5 实现后创建 `DirectoryChangeInfoHookTest` | `./mvnw -pl dec-core-model,dec-core-context -Dtest=DirectoryChangeInfoHookTest test` | 等 P5 接入后自动化 |

## 4. 完成门禁

- [x] 每个 Case 绑定 Requirement、Feature、Acceptance、Design 和 Flow 稳定引用。
- [x] 每个 Case 写明具体输入值、预期输出、观察位置、验证方式和通过标准。
- [x] 正常、边界、异常、权限、事务和禁止副作用按当前范围覆盖。
- [x] 多目录、多 ChangeInfo、并发隔离和数据库连接选择明确列为范围外；不以未实现内容宣称通过。
- [ ] 实现后补齐测试文件、测试方法和真实 command Evidence。
