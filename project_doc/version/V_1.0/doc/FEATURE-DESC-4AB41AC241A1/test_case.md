# FEATURE-DESC-4AB41AC241A1 测试设计

> Test revision：`TESTDESIGN-P3-R01`
> Requirement：`FEATURE-DESC-4AB41AC241A1`
> Requirement revision：`REQAN-P3-R01@5b4727fc5db4`
> 状态：测试设计完成

> 范围：为 P3 Information Engine 定义可执行 Case；测试实现和执行证据留待 development/testing 阶段。

## 1. 测试范围与环境

- 测试目标：验证 16 个 mix Information 的编译、模型路径读取、表达式/DAG 求值和 change-data 物化边界。
- 范围内：三类 Information、Model Expression 与 Information Expression、实时读取、错误语义、空集合和订单数据归属。
- 范围外：MutationSet/reverse-DAG 缓存、幂等、并发、独立事务、P2 文档治理和 XML fixture 改造。
- 环境与账号：`dec-demo` mix 配置；使用受控 ModelContext、PublishedBindings 和可注入写入端，不依赖页面或外部账号。
- 共用前置条件：已发布 `DESIGN-P3-R02`；加载 `systems.xml` 的 16 个 Information；`OrderInfo` 的 target-main 为 `order`，明细为独立 `orderDetail`。

## 2. Case 索引

| Case ID | 名称 | 功能/验收 | 页面/操作 | 流程/步骤 | 测试层级 | 自动化状态 |
|---|---|---|---|---|---|---|
| CASE-FEATURE-DESC-4AB41AC241A1-001 | 16 项 Information 编译与 DAG | AC-P3-INFORMATION-ENGINE-001 / TR-P3-INFORMATION-ENGINE-001 | N/A | FLOW-P3-INFORMATION-EVALUATION | 单元 | 待 development |
| CASE-FEATURE-DESC-4AB41AC241A1-002 | 原子与复合表达式实时重读 | AC-P3-INFORMATION-ENGINE-001 / TR-P3-INFORMATION-ENGINE-001 | N/A | STEP-P3-INFORMATION-EVALUATION-IDENTIFY | 集成 | 待 development |
| CASE-FEATURE-DESC-4AB41AC241A1-003 | 路径归属与 target-main 优先 | AC-P3-INFORMATION-ENGINE-001 / TR-P3-INFORMATION-ENGINE-001 | N/A | STEP-P3-INFORMATION-EVALUATION-IDENTIFY | 单元 | 待 development |
| CASE-FEATURE-DESC-4AB41AC241A1-004 | null 与非法路径错误 | AC-P3-INFORMATION-ENGINE-001 / TR-P3-INFORMATION-ENGINE-001 | N/A | STEP-P3-INFORMATION-EVALUATION-IDENTIFY | 单元 | 待 development |
| CASE-FEATURE-DESC-4AB41AC241A1-005 | 空集合 every 与订单明细非空 | AC-P3-INFORMATION-ENGINE-001 / TR-P3-INFORMATION-ENGINE-001 | N/A | STEP-P3-INFORMATION-EVALUATION-IDENTIFY | 单元 | 待 development |
| CASE-FEATURE-DESC-4AB41AC241A1-006 | change-data 写入与异常回滚 | AC-P3-INFORMATION-ENGINE-001 / TR-P3-INFORMATION-ENGINE-001 | N/A | STEP-P3-INFORMATION-EVALUATION-MATERIALIZE | 集成 | 待 development |

<!-- TEST-CASE: CASE-FEATURE-DESC-4AB41AC241A1-001 -->
<a id="CASE-FEATURE-DESC-4AB41AC241A1-001"></a>
## CASE-FEATURE-DESC-4AB41AC241A1-001 16 项 Information 编译与 DAG

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | FEATURE-DESC-4AB41AC241A1; AC-P3-INFORMATION-ENGINE-001; TR-P3-INFORMATION-ENGINE-001 | [需求文档](requirement.md#需求范围) |
| 设计/契约 | DESIGN-P3-R02; BP-P3-001; IMPL-DEC-P3-003 | [设计文档](FEATURE-DESC-4AB41AC241A1_design.md#9-需求映射与总变更清单) |
| 页面/操作 | N/A（无页面） | 无页面设计，因为本需求仅内部引擎 |
| 流程/步骤 | FLOW-P3-INFORMATION-EVALUATION; STEP-P3-INFORMATION-EVALUATION-IDENTIFY | [需求文档](requirement.md#流程与验收) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| Information 声明数 | `16` | RawInformationSet fixture | `systems.xml` mix 基线 |
| Information 类型 | `MODEL=8, RULE=4, COMPOSITE=4` | 编译器输入 | 三类 Information 分类 |
| 依赖图 | `A -> B -> C`，无环 | Raw dependency refs | BM-P3-R01 依赖不变量 |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 加载 16 项 RawInformationSet 与 PublishedBindings | `InformationCompiler.compile` | 读取声明并校验 Key/ref/path |
| 2 | 编译并发布候选集合 | `InformationCompiler.compile` | 输出不可变集合，包含 16 个唯一 Key 和拓扑序 |
| 3 | 检查复合节点依赖顺序 | `CompiledInformationSet` | 所有依赖在使用前已求值，无循环节点 |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| 编译结果 | `size=16`，Key 唯一，DAG 拓扑稳定，集合不可变 | 单元测试断言集合大小、Key 集合和拓扑序 | 三项断言全部成立 |
| 编译诊断 | 合法输入无 `INFORMATION_DEFINITION_INVALID` | 捕获 compile 返回值和诊断列表 | 诊断列表为空 |

### 禁止副作用与清理

- 禁止副作用：编译不得写模型、修改 XML 或改变已发布旧集合。
- Case 完成后的状态：保留候选集合供后续 evaluate Case 使用。
- 清理要求：释放 fixture 和 reader；不写数据库。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| `systems.xml` 复制为内存 RawInformationSet | 不适用，因为 fixture 仅内存 | 待 development：`InformationCompilerTest.compileMix16` | 待 testing：项目声明的单元测试命令 | 测试代码尚未开发 |

<!-- TEST-CASE: CASE-FEATURE-DESC-4AB41AC241A1-002 -->
<a id="CASE-FEATURE-DESC-4AB41AC241A1-002"></a>
## CASE-FEATURE-DESC-4AB41AC241A1-002 原子与复合表达式实时重读

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | FEATURE-DESC-4AB41AC241A1; AC-P3-INFORMATION-ENGINE-001; TR-P3-INFORMATION-ENGINE-001 | [需求文档](requirement.md#需求范围) |
| 设计/契约 | DESIGN-P3-R02; BP-P3-003; IMPL-DEC-P3-004 | [设计文档](FEATURE-DESC-4AB41AC241A1_design.md#7-接口交互与兼容策略) |
| 页面/操作 | N/A（无页面） | 无页面设计，因为本需求仅内部引擎 |
| 流程/步骤 | STEP-P3-INFORMATION-EVALUATION-IDENTIFY | [设计文档](FEATURE-DESC-4AB41AC241A1_design.md#5-核心流程状态与失败路径) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| 原子 Information Key | `order.status` | `InformationEngine.evaluate` | 已编译 MODEL Information |
| 第一次模型值 | `status="PAID"` | ModelContext reader | 当前事务值 |
| 第二次模型值 | `status="CLOSED"` | 同一 reader 更新后再次调用 | 验证实时读取 |
| 复合表达式 | `order.status == "CLOSED" AND payment.success == TRUE` | Information Expression | DAG 依赖原子节点 |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 以 `PAID` 调用原子 evaluate | `InformationEngine.evaluate` | 返回 FALSE 或对应原子值证据，readPaths 含 `order.status` |
| 2 | 将同一 ModelContext 的 `status` 改为 `CLOSED` | ModelContext reader | reader 下一次读取到 `CLOSED` |
| 3 | 再次调用原子和复合 evaluate | `InformationEngine.evaluate` | 复合节点按拓扑读取最新值并短路求值 |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| 第二次原子结果 | 反映 `status="CLOSED"`，不复用第一次值 | 对 reader 调用次数和返回结果做断言 | 第二次结果与 `CLOSED` 一致 |
| 复合结果 | 依赖节点按 DAG 顺序求值，无重复无关读取 | 断言 readPaths 顺序和最终 TRUE/FALSE | 只读取声明 read-set，结果正确 |

### 禁止副作用与清理

- 禁止副作用：evaluate 不得写模型或维护 MutationSet/reverse-DAG 缓存。
- Case 完成后的状态：reader 调用记录可供断言后清空。
- 清理要求：释放 ModelContext。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| 受控 ModelContext 初始值 `PAID` | 不适用，因为仅内存上下文 | 待 development：`InformationEngineTest.rereadsCurrentValue` | 待 testing：项目声明的集成测试命令 | 测试代码尚未开发 |

<!-- TEST-CASE: CASE-FEATURE-DESC-4AB41AC241A1-003 -->
<a id="CASE-FEATURE-DESC-4AB41AC241A1-003"></a>
## CASE-FEATURE-DESC-4AB41AC241A1-003 路径归属与 target-main 优先

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | FEATURE-DESC-4AB41AC241A1; AC-P3-INFORMATION-ENGINE-001; TR-P3-INFORMATION-ENGINE-001 | [需求文档](requirement.md#数据归属) |
| 设计/契约 | DESIGN-P3-R02; BP-P3-002; IMPL-DEC-P3-002 | [设计文档](FEATURE-DESC-4AB41AC241A1_design.md#4-目标方案与职责边界) |
| 页面/操作 | N/A（无页面） | 无页面设计，因为本需求仅内部引擎 |
| 流程/步骤 | STEP-P3-INFORMATION-EVALUATION-IDENTIFY | [设计文档](FEATURE-DESC-4AB41AC241A1_design.md#7-接口交互与兼容策略) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| target-main order | `{id:"O-1", status:"PAID", userId:"U-1"}` | ModelContext | OrderInfo 基础 Data |
| relation orderDetail | `{id:"D-1", quantity:2}` | ModelContext relation | 独立 orderDetail Data |
| 读取路径 | `status`, `orderDetailList` | ModelPathResolver.resolve | 显式 Binding/ref |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 解析 `status` | `ModelPathResolver.resolve` | 命中 target-main `order.status` |
| 2 | 解析 `orderDetailList` | `ModelPathResolver.resolve` | 命中独立 `orderDetail` relation Data，不归入 order 基础字段 |
| 3 | 读取两个路径 | `ConfiguredModelReader.read` | 返回各自对象的当前值 |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| `status` read evidence | `source=target-main.order`, `value=PAID` | 断言 resolver source 和值 | 不从其他 Data 覆盖基础字段 |
| `orderDetailList` read evidence | `source=relation.orderDetail`, `value=[D-1]` | 断言 relation source 和列表值 | 归属独立且可读取 |

### 禁止副作用与清理

- 禁止副作用：路径解析不得模糊搜索或改写 Binding。
- Case 完成后的状态：保留 read evidence 供审计。
- 清理要求：释放上下文对象。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| 内存构造 order/orderDetail 两个 Data | 不适用，因为不落库 | 待 development：`ModelPathResolverTest.resolvesOwnership` | 待 testing：项目声明的单元测试命令 | 测试代码尚未开发 |

<!-- TEST-CASE: CASE-FEATURE-DESC-4AB41AC241A1-004 -->
<a id="CASE-FEATURE-DESC-4AB41AC241A1-004"></a>
## CASE-FEATURE-DESC-4AB41AC241A1-004 null 与非法路径错误

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | FEATURE-DESC-4AB41AC241A1; AC-P3-INFORMATION-ENGINE-001; TR-P3-INFORMATION-ENGINE-001 | [需求文档](requirement.md#错误语义) |
| 设计/契约 | DESIGN-P3-R02; `INFORMATION_PATH_INVALID` | [设计文档](FEATURE-DESC-4AB41AC241A1_design.md#5-核心流程状态与失败路径) |
| 页面/操作 | N/A（无页面） | 无页面设计，因为本需求仅内部引擎 |
| 流程/步骤 | STEP-P3-INFORMATION-EVALUATION-IDENTIFY | [设计文档](FEATURE-DESC-4AB41AC241A1_design.md#5-核心流程状态与失败路径) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| 普通模型值 | `order.totalPrice=null` | ModelContext reader | 普通求值输入 |
| 显式 Information 值 | `payment.success=null` | Information result fixture | 唯一允许 null 的比较例外 |
| 非法模型路径 | `order.missingField` | Compiled read-set | 声明路径不存在 |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 求值 `order.totalPrice` | `InformationEngine.evaluate` | 检测普通 null |
| 2 | 比较 `payment.success=null` | Information Expression evaluator | 进入显式 InformationKey null 例外分支 |
| 3 | 求值 `order.missingField` | `ConfiguredModelReader.read` | 记录非法路径诊断 |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| 普通 null 结果 | `ERROR`，包含 Key 和路径 | 断言结果状态与 diagnostic.path | 不返回 TRUE/FALSE |
| 显式 Information null 比较 | 按比较表达式返回布尔结果 | 断言 `payment.success = null` 的比较结果 | 不抛普通 null 错误 |
| 非法路径结果 | `ERROR`，错误码 `INFORMATION_PATH_INVALID` | 断言异常/结果诊断 | 立即失败且不写模型 |

### 禁止副作用与清理

- 禁止副作用：错误求值不得写入 change-data 或吞掉异常。
- Case 完成后的状态：保留三条诊断供测试报告。
- 清理要求：清空内存诊断。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| 内存构造 null 和非法路径输入 | 不适用，因为不落库 | 待 development：`InformationEngineTest.nullAndInvalidPath` | 待 testing：项目声明的单元测试命令 | 测试代码尚未开发 |

<!-- TEST-CASE: CASE-FEATURE-DESC-4AB41AC241A1-005 -->
<a id="CASE-FEATURE-DESC-4AB41AC241A1-005"></a>
## CASE-FEATURE-DESC-4AB41AC241A1-005 空集合 every 与订单明细非空

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | FEATURE-DESC-4AB41AC241A1; AC-P3-INFORMATION-ENGINE-001; TR-P3-INFORMATION-ENGINE-001 | [需求文档](requirement.md#空集合与订单约束) |
| 设计/契约 | DESIGN-P3-R02; BP-P3-003 | [设计文档](FEATURE-DESC-4AB41AC241A1_design.md#2-背景现状与设计目标) |
| 页面/操作 | N/A（无页面） | 无页面设计，因为本需求仅内部引擎 |
| 流程/步骤 | STEP-P3-INFORMATION-EVALUATION-IDENTIFY | [需求文档](requirement.md#流程与验收) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| 通用集合 | `items=[]` | Information Expression evaluator | every 的数学语义 |
| 订单明细 | `orderDetailList=[]` | ModelContext reader | 订单 Information 的额外非空约束 |
| 非空订单明细 | `orderDetailList=[D-1]` | ModelContext reader | 对照组 |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 求值 `every([], predicate)` | Information Expression evaluator | 通用 every 返回 TRUE |
| 2 | 求值订单 Information，明细为空 | `InformationEngine.evaluate` | 触发订单明细非空约束 |
| 3 | 将明细改为 `[D-1]` 后重读 | `InformationEngine.evaluate` | 约束通过并继续 predicate 求值 |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| 通用 every 结果 | `TRUE` | 断言布尔结果 | 空集合不导致 FALSE |
| 订单明细为空 | `ERROR`，诊断说明订单明细必须非空 | 断言结果状态和诊断 | 订单规则覆盖通用 every 结果 |
| 订单明细非空 | 返回 predicate 的 TRUE/FALSE | 断言 `[D-1]` 输入下无非空错误 | 仅按 predicate 结果判定 |

### 禁止副作用与清理

- 禁止副作用：不得把订单非空约束改成全局 every 语义。
- Case 完成后的状态：保留通用与订单两组结果。
- 清理要求：释放集合 fixture。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| 内存构造空集合和 `[D-1]` | 不适用，因为不落库 | 待 development：`InformationExpressionTest.everyEmptyAndOrderNonEmpty` | 待 testing：项目声明的单元测试命令 | 测试代码尚未开发 |

<!-- TEST-CASE: CASE-FEATURE-DESC-4AB41AC241A1-006 -->
<a id="CASE-FEATURE-DESC-4AB41AC241A1-006"></a>
## CASE-FEATURE-DESC-4AB41AC241A1-006 change-data 写入与异常回滚

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | FEATURE-DESC-4AB41AC241A1; AC-P3-INFORMATION-ENGINE-001; TR-P3-INFORMATION-ENGINE-001 | [需求文档](requirement.md#物化) |
| 设计/契约 | DESIGN-P3-R02; BP-P3-004; IMPL-DEC-P3-001 | [设计文档](FEATURE-DESC-4AB41AC241A1_design.md#6-数据与持久化方案) |
| 页面/操作 | N/A（无页面） | 无页面设计，因为本需求仅内部引擎 |
| 流程/步骤 | STEP-P3-INFORMATION-EVALUATION-MATERIALIZE | [设计文档](FEATURE-DESC-4AB41AC241A1_design.md#5-核心流程状态与失败路径) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| 模型原子 Key | `order.totalAmount` | Materializer request | 必须声明为 change-data |
| change-data 值 | `199.90` | Materializer request | 仅写声明值 |
| 写入端 | 成功端；失败端抛出 `PersistenceException` | 注入 ModelContainer writer | 复用既有事务回滚 |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 调用成功物化 | `InformationMaterializer.materialize` | writer 写入 `199.90` |
| 2 | 写后按配置重新读取并识别 | `ModelContainer` + `InformationEngine.evaluate` | 读到新值，返回物化结果 |
| 3 | 注入 writer 异常并再次物化 | `InformationMaterializer.materialize` | 异常直接抛出，事务进入回滚 |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| 成功写入 | `order.totalAmount=199.90`，写后 reread 读到 `199.90` | 断言 writer 参数和 reread 值 | 值与声明完全一致 |
| 失败调用 | 抛出原始 `PersistenceException`，模型恢复写入前值 | 断言异常类型、事务状态和旧值 | 不返回成功结果 |
| 下游调用记录 | 失败分支下调用次数为 `0` | 断言下游 spy 没有调用 | 物化失败不继续下游 |

### 禁止副作用与清理

- 禁止副作用：不得维护 MutationSet、吞异常、部分提交或继续下游重算。
- Case 完成后的状态：成功场景提交由既有 ModelContainer 负责；失败场景恢复原值。
- 清理要求：关闭 ModelContainer 并清除 writer spy。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| 事务内 order 初始 `totalAmount=100.00` | 不适用，因为沿用事务回滚清理 | 待 development：`InformationMaterializerTest.rollsBackOnWriteFailure` | 待 testing：项目声明的集成测试命令 | 测试代码尚未开发 |

## 4. 完成门禁

- [x] 每个 Case 都绑定本需求的 Requirement、Feature 和 Acceptance ID。
- [x] 页面型 Case 同时绑定 `PAGE-*` 与 `PAGEACT-*`，页面设计反向登记 Case ID；本需求无页面，已说明 N/A。
- [x] 每个 Case 都写明具体输入值、具体预期输出、输出观察位置、验证方式和通过标准。
- [x] 需要数据准备的 Case 链接初始化与清理脚本；不需要时写明理由。
- [x] 可自动化 Case 链接测试文件、测试方法和执行命令；测试代码尚未开发，已明确延期状态。
- [x] 初始化脚本、测试代码和流程事实按适用性反向引用 Case ID。
- [x] 正常、边界、异常、权限、事务、幂等、并发、兼容和禁止副作用已按适用性覆盖；幂等与并发按已确认范围不适用。
