<!-- template: common-develop/test-case-v1 -->
# FEATURE-DESC-3361AD2E54FC 测试设计

> Test revision：`TESTDESIGN-P2-R40`
> Requirement：`FEATURE-DESC-3361AD2E54FC`
> Requirement revision：`REQAN-P2-R01`
> Design revision：`DESIGN-P2-R36`
> 状态：测试设计完成

> 文档导航：[项目文档首页](../../../../docs/README.md) · [版本摘要](../../version_summary.md) · [需求文档](requirement.md) · [关联设计](../COMPILER/COMPILER_design.md) · 页面设计不适用（Java 核心运行时，无 UI）

## 1. 测试范围与环境

- 测试目标：证明 DECIMAL Kind/数值保真、AC-009 DENY 定位完整、三类业务入口使用真实 production lifecycle，并保持 P2 全量行为。
- 范围内：dec-core-context、dec-core-model、dec-core-starter 的 value/result/production tests 和 Maven 全量回归。
- 范围外：用户明确排除的 DEC-P2-R34-001/002/003；页面、HTTP、数据库、P3-P7、部署。
- 环境与账号：JDK 17+、Maven Wrapper；根 POM 固定 `maven.compiler.release=8`，产物 class major version 必须为 52；进程内 fixture，无账号、网络或外部数据库。
- 共用前置条件：immutable EngineContext、真实 RuntimeModelExecutionRoot、RuntimeModelLoadRequest、origin object；禁止反射 Scope、手工 Handle、预制 guarded/raw port。

## 2. Case 索引

| Case ID | 名称 | 功能/验收 | 页面/操作 | 流程/步骤 | 测试层级 | 自动化状态 |
|---|---|---|---|---|---|---|
| CASE-P2-R40-DECIMAL-KIND-001 | DECIMAL 保持 BigDecimal Kind 和数值 | Java 8 产物兼容/value exactness | 不适用：无 UI | DESIGN-R36 §4/§6 | 单元/集成 | PLANNED |
| CASE-P2-R40-DENIAL-CONTEXT-001 | DENY 稳定关联适用定位字段 | BR-018、AC-009 | 不适用：无 UI | DESIGN-R36 §4/§11 | 单元/集成 | PLANNED |
| CASE-P2-R40-PRODUCTION-CONSUMERS-001 | 三类入口贯穿真实生产链 | AC-006/007 | 不适用：无 UI | DESIGN-R36 §5/§8 | 生命周期集成 | PLANNED |
| CASE-P2-R40-FULL-REGRESSION-001 | Java 8 兼容产物与 P2 全量回归 | AC-001～010 | 不适用：无 UI | DESIGN-R36 §13 | 回归 | AUTOMATED |

<!-- TEST-CASE: CASE-P2-R40-DECIMAL-KIND-001 -->
<a id="CASE-P2-R40-DECIMAL-KIND-001"></a>
## CASE-P2-R40-DECIMAL-KIND-001 DECIMAL 保持 BigDecimal Kind 和数值

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | FEATURE-DESC-3361AD2E54FC；Java 8；RuntimeFactValue exactness | [需求兼容](requirement.md#105-兼容与历史数据) |
| 设计/契约 | DESIGN-P2-R36；DEC-P2-R36-001；BP-P2-R36-001/002 | [数据设计](../COMPILER/COMPILER_design.md#6-数据与持久化方案) |
| 页面/操作 | 不适用：值对象和 ModelData 无页面 | [页面不适用](../COMPILER/COMPILER_design.md#71-页面业务行为) |
| 流程/步骤 | RuntimeFactValue Kind → typed getter → MODEL codec | [主流程](../COMPILER/COMPILER_design.md#51-主流程) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| integral decimal | `decimalValue(new BigDecimal("1.0"))` | RuntimeFactValue factory | Kind=DECIMAL；规范化允许 scale 改变 |
| large decimal | `decimalValue(new BigDecimal("9223372036854775808"))` | factory | 超过 Long.MAX_VALUE |
| negative decimal | `decimalValue(new BigDecimal("-12.3400"))` | factory | 负数和尾零 |
| nested decimal | list/object 内包含上述三个值 | listValue/objectValue | 递归转换 |
| integer control | `integerValue(7L)` | factory | Kind=INTEGER |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 调用 scalar typed getter 和 MODEL codec | RuntimeFactValue/RuntimeFactValueCodec | 不解析 canonicalForm |
| 2 | 经现有 protected WRITE 写入 scalar/nested 值 | MODEL operation port | 合法 DECIMAL 不触发 NumberFormatException |
| 3 | 从 ModelData 读取实际 Java 类型和值 | ModelData fixture | DECIMAL leaf=BigDecimal；INTEGER=Long |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| scalar DECIMAL | Java class 为 BigDecimal | `assertInstanceOf` 或 Java 8 `instanceof` assertion | 三个 DECIMAL 均不是 Long |
| numeric values | 分别与 1、9223372036854775808、-12.34 数值相等 | `BigDecimal.compareTo()==0` | 不使用 equals 要求原始 scale |
| nested leaves | 每个 DECIMAL leaf 为 BigDecimal | 递归 exact assertions | 无类型降级 |
| integer control | Java class 为 Long，值=7 | exact assertion | INTEGER 行为不变 |

### 禁止副作用与清理

- 禁止副作用：不得按 canonical 小数点猜类型，不得拒绝超 Long 的合法 DECIMAL。
- Case 完成后的状态：成功 WRITE 沿用现有 version/effect 行为；不承诺保留输入 BigDecimal 原始 scale。
- 清理要求：关闭 fixture；数据仅在测试进程内，无脚本。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| Java 代码构造 BigDecimal/list/object；无需脚本 | fixture.close；无需脚本 | 扩展 `dec-core-context/src/test/java/dec/core/context/runtime/RuntimeFactValueContractTest.java#scalarTypedAccessorsPreserveKind`；`dec-core-model/src/test/java/dec/core/model/runtime/ProtectedWriteTransactionIntegrationTest.java#decimalKindSurvivesIntegralCanonicalAndLargeValue` | `./mvnw -pl dec-core-context -Dtest=RuntimeFactValueContractTest -Dsurefire.failIfNoSpecifiedTests=true test`；`./mvnw -pl dec-core-model -Dtest=ProtectedWriteTransactionIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test` | PLANNED |

<!-- TEST-CASE: CASE-P2-R40-DENIAL-CONTEXT-001 -->
<a id="CASE-P2-R40-DENIAL-CONTEXT-001"></a>
## CASE-P2-R40-DENIAL-CONTEXT-001 DENY 稳定关联适用定位字段

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | FEATURE-DESC-3361AD2E54FC；BR-018；AC-009 | [需求 AC-009](requirement.md#ac-p2-system-ruleview-009-diagnostic-可定位且确定) |
| 设计/契约 | DESIGN-P2-R36；DEC-P2-R36-002；BP-P2-R36-003/004 | [接口映射](../COMPILER/COMPILER_design.md#112-字段端到端映射) |
| 页面/操作 | 不适用：运行时拒绝 DTO 无页面 | [页面不适用](../COMPILER/COMPILER_design.md#71-页面业务行为) |
| 流程/步骤 | invocation/rule source → decision context → denial | [失败路径](../COMPILER/COMPILER_design.md#52-关键失败路径) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| call identity | System=`order`、RuleView=`order:submit`、operation=READ、path=`amount` | contextual ProtectedAccessInvocation | RuleView 仅作诊断，owner 必须为 order，不参与 Guard 授权 |
| published source | `SourceRef("mix/order/access.xml",8,2,"/model-access/read")` | exact READ CompiledModelAccessRule | denial 中必须与 `rule.sourceRef()` 完全相同 |
| failures | POLICY_NOT_FOUND、operation/path mismatch、runtime effect failure | protected entries | 每类重复执行两次 |
| non-RuleView control | framework internal invocation，RuleView empty | explicit fixture | 只有此类场景 RuleView 可不适用 |
| owner mismatch | System=`order`、RuleView=`inventory:submit` | contextual factory | 必须在构造时拒绝，不能进入 Guard |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 用不存在的 exact policy 或 operation/path mismatch 调用 | Rule entry | DENY source=Optional.empty，不模糊回查 |
| 2 | 用 exact rule 命中后发生 runtime target/effect failure 调用 | change/custom action entry | DENY source=published source |
| 3 | 重复每类失败并比较结果 | ProtectedAccessDenial | code/message/context 稳定 |
| 4 | 构造 RuleView owner mismatch 并扫描 denial 文本和字段 | contextual factory/getters/toString | mismatch 被拒绝；denial 不含 write value、origin 或配置正文 |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| policy-not-found context | order/order:submit/READ/amount/SourceRef empty | exact getter assertions | System/RuleView/operation/path 存在，SourceRef 明确不适用 |
| matched-rule context | System/RuleView/op/path + published SourceRef | exact assertions | published source 优先 |
| non-RuleView context | RuleView Optional.empty，其余字段存在 | optional assertion | 只在明确框架场景为空 |
| owner mismatch | IllegalArgumentException | exact exception assertion | 不产生可执行 invocation |
| repeated denial | 两次 code/message/context 相等 | equals/hash assertions | 完全稳定 |
| sensitive scan | 不含模型值/origin/配置正文 | negative assertions | 无敏感泄露 |

### 禁止副作用与清理

- 禁止副作用：不得 null-success、吞异常或只写日志；不得输出 write value。
- Case 完成后的状态：所有 DENY 的 ModelData/effect/version 沿用现有无成功结果语义。
- 清理要求：关闭 production fixture；无外部脚本。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| Java builder 创建 keys/RuleView/published SourceRef；无需脚本 | fixture.close；无需脚本 | 扩展 `dec-core-context/src/test/java/dec/core/context/runtime/ProtectedAccessContextApiContractTest.java#denialCarriesStableDecisionContext`；`dec-core-starter/src/test/java/dec/core/starter/access/P2SecurityAuthorityRemediationTest.java#productionDenialsUseOnlyExactPublishedSource` | `./mvnw -pl dec-core-context -Dtest=ProtectedAccessContextApiContractTest -Dsurefire.failIfNoSpecifiedTests=true test`；`./mvnw -pl dec-core-starter -Dtest=P2SecurityAuthorityRemediationTest -Dsurefire.failIfNoSpecifiedTests=true test` | PLANNED |

<!-- TEST-CASE: CASE-P2-R40-PRODUCTION-CONSUMERS-001 -->
<a id="CASE-P2-R40-PRODUCTION-CONSUMERS-001"></a>
## CASE-P2-R40-PRODUCTION-CONSUMERS-001 三类入口贯穿真实生产链

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | FEATURE-DESC-3361AD2E54FC；AC-006/007 | [需求 AC-007](requirement.md#ac-p2-system-ruleview-007-所有变更入口不可旁路) |
| 设计/契约 | DESIGN-P2-R36；IMPL-DEC-P2-R36-005；BP-P2-R36-005 | [开发交接](../COMPILER/COMPILER_design.md#8-开发者交接摘要) |
| 页面/操作 | 不适用：生产 Java API 无页面 | [页面不适用](../COMPILER/COMPILER_design.md#71-页面业务行为) |
| 流程/步骤 | production→load→accessScope→create→consumer invoke→close | [目标结构](../COMPILER/COMPILER_design.md#41-目标结构) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| EngineContext | READ amount policy + exact materialization plan | public context/compiler fixture | immutable published aggregate |
| root/request | `production(context, SYNCHRONIZED)`；amount=10；exact plan/rule | public MODEL API | 不注入 Container/ModelData，不依赖数据库连接 |
| invocation | contextual RuleView；同一个 READ amount 请求与同一个缺策略 READ 请求 | public context API | 不使用 null，不携带调用方 SourceRef |
| entries | composition 的 `ruleEntry/changeEntry/customActionEntry` | public STARTER API | 不取得 fixture guarded/raw port |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | root.load(request) 后 accessScope | RuntimeModelExecutionRoot | load 成功，scope available |
| 2 | `ProtectedAccessRuntimeFactory.production(context).create(scope)` | STARTER factory | composition created |
| 3 | Rule、change、custom action 分别执行语义相同的允许 READ | 三个 public entry | 均得到相同 ALLOW value |
| 4 | 三个入口分别执行语义相同的 policy absent READ DENY | 三个 public entry | DENY 的适用 context 字段一致，readValue/writeReceipt 均 empty，公开 origin/ModelData 状态不变 |
| 5 | 关闭 root/session 后再次访问 | public lifecycle | 稳定 closed/stale failure |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| load/scope/composition | loaded/available/created 均为 true | exact result assertions | 完整链路每步成功 |
| three ALLOW entries | READ=10；三类行为一致 | public result assertions | 三个入口使用同一 operation/path，不调用预制 port |
| three DENY entries | stable code + applicable context；SourceRef、readValue、writeReceipt 均 empty；origin/ModelData 不变 | 每个入口分别执行 exact empty assertions + public state assertions | 三个入口使用同一缺策略 operation/path，均真实调用且无副作用 |
| closed lifecycle | stable failure，不恢复旧 scope | exact code assertion | 无后续成功 |

### 禁止副作用与清理

- 禁止副作用：不得反射构造 Scope/Handle，不得预制 binding/guarded port，不得以 `read(null)/write(null)` 作为验收。
- Case 完成后的状态：root/session 关闭，READ/DENY 不修改 origin/ModelData。
- 清理要求：try/finally 关闭 root/session；无外部脚本。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| Java production fixture 构造 Context/origin/request；无需脚本 | root/session close；无需脚本 | 扩展 `dec-core-starter/src/test/java/dec/core/starter/access/SingleEngineContextRuntimeLifecycleTest.java#allConsumersTraverseRealProductionLifecycle` | `./mvnw -pl dec-core-starter -Dtest=SingleEngineContextRuntimeLifecycleTest -Dsurefire.failIfNoSpecifiedTests=true test` | PLANNED |

<!-- TEST-CASE: CASE-P2-R40-FULL-REGRESSION-001 -->
<a id="CASE-P2-R40-FULL-REGRESSION-001"></a>
## CASE-P2-R40-FULL-REGRESSION-001 Java 8 兼容产物与 P2 全量回归

### 关联事实

<!-- TEST-CASE-TRACE -->
| 对象 | 稳定引用 | 来源文档 |
|---|---|---|
| 需求/验收 | FEATURE-DESC-3361AD2E54FC；AC-001～010；Java 8 产物兼容 | [需求验收](requirement.md#9-验收标准) |
| 设计/契约 | DESIGN-P2-R36；范围裁剪与全量验证 | [验证计划](../COMPILER/COMPILER_design.md#132-验证计划) |
| 页面/操作 | 不适用：核心模块无页面 | [页面不适用](../COMPILER/COMPILER_design.md#71-页面业务行为) |
| 流程/步骤 | Java version → Maven reactor tests → summary | [兼容要求](../COMPILER/COMPILER_design.md#22-设计目标与非目标) |

### 前置条件和具体输入

<!-- TEST-CASE-INPUT -->
| 输入项 | 输入值 | 输入方式 | 来源/约束 |
|---|---|---|---|
| build JDK | JDK 17+ | `java -version` | 满足根 POM enforcer，Evidence 保存实际版本输出 |
| compiler target | `maven.compiler.release=8` | 根 `pom.xml` | 不得被子模块覆盖为更高版本 |
| repository | 当前工作区全部 modules/tests | Maven Wrapper | 不跳过测试 |
| baseline | 原 HEAD 777 tests，0 failure/error | previous fresh run | 新总数必须不少于 baseline，差异需解释 |

### 执行步骤

<!-- TEST-CASE-STEPS -->
| 序号 | 操作 | 页面/接口 | 预期中间结果 |
|---:|---|---|---|
| 1 | 执行 `java -version` 并检查根 POM release | shell/POM | JDK≥17 且 release=8 |
| 2 | 先安装依赖，再按模块执行 context/model/starter 精确 TestClass | Maven Wrapper | 每个指定测试被发现，0 failure/error |
| 3 | 执行 `./mvnw test` | Maven reactor | 全部模块结束且退出码 0 |
| 4 | 检查代表 production class 字节码版本并汇总 Surefire | `javap -verbose`/reports | major version=52；数量可审计 |

### 预期输出与验证方式

<!-- TEST-CASE-OUTPUT -->
| 输出位置 | 预期输出 | 验证方式 | 通过标准 |
|---|---|---|---|
| build JDK | 17 或更高 | 保存命令 Evidence | 满足 Maven Enforcer |
| Java target | POM release=8 且代表 class major version=52 | POM exact assertion + `javap -verbose` | 证明 Java 8-compatible output |
| Maven exit | 0/BULD SUCCESS | shell exit + reactor summary | 所有模块完成 |
| Surefire totals | tests≥777、failures=0、errors=0；skipped 明确登记 | 聚合 XML/report | skipped 不表述为通过 |
| worktree | 仅预期 docs/tests/code 改动 | git status/diff | 未删除或禁用既有测试 |

### 禁止副作用与清理

- 禁止副作用：不得使用 `-DskipTests`、删除测试或只跑新 Case 代替全量。
- Case 完成后的状态：测试输出注册为 exact revision Evidence。
- 清理要求：fixture teardown；无数据库/服务脚本。

### 测试数据与自动化映射

<!-- TEST-CASE-AUTOMATION -->
| 测试数据初始化 | 清理脚本 | 自动化测试文件与方法 | 执行命令 | 状态/不自动化理由 |
|---|---|---|---|---|
| 现有 Maven fixtures；无需独立脚本 | 各测试 teardown；无需独立脚本 | `dec-core-*/src/test/java/**` 和 `dec-demo/src/test/java/**` | `source ~/.bash_profile && java -version`；`source ~/.bash_profile && ./mvnw -DskipTests install`；按模块 `-Dtest=... -Dsurefire.failIfNoSpecifiedTests=true test`；`source ~/.bash_profile && ./mvnw test`；`source ~/.bash_profile && javap -verbose dec-core-context/target/classes/dec/core/context/runtime/RuntimeFactValue.class` | AUTOMATED |

## 4. 完成门禁

- [x] 每个 Case 绑定当前 Requirement、Feature、Acceptance 和 DESIGN-P2-R36。
- [x] 页面型 Case 不适用；每个 Case 记录无 UI 理由。
- [x] 每个 Case 写明具体输入、输出位置、验证方式和通过标准。
- [x] 全部使用进程内 fixture，不需要数据脚本；初始化和清理理由完整。
- [x] 每个 Case 指定自动化测试文件、方法和命令。
- [x] 测试代码在 TDD 阶段以 Case ID 注释反向引用本文件。
- [x] 数值边界、权限拒绝、生命周期、兼容和禁止副作用已覆盖；排除项由用户明确授权。
