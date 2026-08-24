# COMPILER P2 类型保真、拒绝定位与测试有效性整改 技术设计

> Design revision：`DESIGN-P2-R36`
> Base revision：`DESIGN-P2-R35`
> Design topic：`DESIGN-COMPILER-P2-RUNTIME-DIAGNOSTIC-TESTABILITY`
> Requirement：`../FEATURE-DESC-3361AD2E54FC/requirement.md`
> Module：`COMPILER`
> Implementation strategy contract：`1`
> 状态：设计完成

> 文档导航：[项目文档首页](../../../../docs/README.md) · [版本摘要](../../version_summary.md) · [本版本需求列表](../../requirement_list.md) · [模块说明](COMPILER_desc.md)

## 第一部分：设计正文

### 1. 一页设计摘要

<!-- DESIGN-NARRATIVE-SUMMARY -->
根据用户对 P2 Review 范围的最新确认，本轮不处理 package-private 授权边界、runtime owner identity 和 WRITE 暂存/并发顺序，继续保留 R33 现有行为。本轮只整改三个可独立闭合的问题：`RuntimeFactValue` 写回必须按 Kind 保持 DECIMAL 为 `BigDecimal`；production protected-access DENY 必须稳定关联 System、RuleView（适用时）、operation、path（适用时）和 SourceRef；现有安全与生命周期测试必须从真实生产 root→load→scope→composition→Rule/change/custom action 入口执行，不能用反射 Scope、预制 guarded port 或 null 调用代替验证。同步更新 canonical TestDesign、实施计划和追踪事实。

#### 1.1 改造前后对照

| 关注点 | 当前情况 | 调整后 | 带来的价值 |
|---|---|---|---|
| DECIMAL | canonical 文本无小数点时被解析为 Long，超 Long 值失败 | 按 RuntimeFactValue.Kind 转换，DECIMAL 始终为 BigDecimal | 类型和精度稳定 |
| DENY | 只有 invocationId/code/message | production DENY 携带稳定 decision context | 满足 AC-009，可直接定位 |
| 测试 | 部分测试只调用 null、反射 Scope 或预制 port | 使用真实生产链和三类业务入口 | 证据代表真实行为 |
| 范围 | R34 同时讨论三个额外架构问题 | 三项经用户确认移出本轮 | 实现范围明确可控 |

#### 1.2 六个关键结论

| 读者最关心的问题 | 当前结论 |
|---|---|
| 本次改变什么 | typed value conversion、denial decision context、真实生产链测试、文档追踪 |
| 保持什么不变 | package-private/bridge、Context/Scope 比较、WRITE 暂存和锁策略、Java 8 兼容产物、公开 ProtectedAccessPort |
| 主流程如何工作 | 现有 Guard/effect 流程不变；仅在值转换和 DENY 构造处扩充准确事实 |
| 失败时系统留下什么事实 | production DENY 留下稳定、非敏感的定位字段 |
| 最关键的技术决策 | 用 typed getter 代替 canonical 文本反解析；production DENY 强制完整上下文 |
| 如何证明设计完成 | decimal 边界、DENY 字段、三类真实入口和全量 Maven 回归通过 |

### 2. 背景、现状与设计目标

<!-- DESIGN-NARRATIVE-CURRENT -->
当前 `RuntimeFactValueCodec.toJavaValue` 解析 `canonicalForm()` 并根据是否含小数点选择 Long 或 BigDecimal，因此 `decimalValue(new BigDecimal("1.0"))` 规范化后会错误写回 Long，`9223372036854775808` 会因 Long 溢出被拒绝。`ProtectedAccessDenial` 仅含 invocationId、code、stableMessage，无法满足 BR-018/AC-009 的定位要求。另有测试虽然断言 raw port 不暴露，却通过反射或预制 fixture 构造关键对象，且 proofless Case 只调用 `read(null)/write(null)`，没有验证真实业务入口。本设计只关闭这些缺口。

#### 2.1 当前实现证据

| 能力/入口 | 当前代码、配置、表或接口锚点 | 当前调用链 | 已知问题 | 本次处置 |
|---|---|---|---|---|
| 值转换 | `dec-core-model/.../RuntimeModelAccessScope.java#RuntimeFactValueCodec` | RuntimeFactValue → canonical parser → Java value | DECIMAL Kind 丢失或溢出 | typed recursive conversion |
| 值对象 | `dec-core-context/.../RuntimeFactValue.java` | factory → immutable value | 缺 scalar typed getter | additive getter |
| 拒绝结果 | `dec-core-context/.../ProtectedAccessDenial.java` | Guarded port → denial | 缺定位字段 | immutable decision context |
| 测试 fixture | `P2SecurityAuthorityRemediationTest`、`SingleEngineContextRuntimeLifecycleTest` | reflection/prebuilt port/null | 不能证明真实生产路径 | 改为 root→load→scope→composition→consumer |
| 追踪 | canonical design/test_case/traceability | R32-R37 sidecar 与旧锚点 | 当前 revision 不一致 | 脚本生成并重验 |

#### 2.2 设计目标与非目标

- 目标：修复 DECIMAL 类型保真、AC-009 DENY 定位、测试代表性和 canonical 文档追踪。
- 非目标：根据用户当前明确决定，`DEC-P2-R34-001`、`DEC-P2-R34-002`、`DEC-P2-R34-003` 不在本轮考虑；不修改 package-private/bridge、owner identity、WRITE 顺序或并发协调。
- 必须保持的兼容行为：源码和字节码兼容 Java 8；构建工具链使用 JDK 17+ 且根 POM 固定 `maven.compiler.release=8`；公开 `ProtectedAccessPort`；现有 READ/WRITE ALLOW/DENY code；现有 Context/Scope/Container 行为。根据 [DEC-P2-ACCESS-OPERATIONS-001](changes/p2-access-operations-decision-r01.md#2-decision)，当前 P2 精确为 READ/WRITE，EXECUTE=N/A。

### 3. 影响范围与明确边界

#### 3.1 影响概览

| 范围 | 是否变化 | 面向人的说明 | 详细位置 |
|---|---|---|---|
| 模块/组件 | 是 | context/model/starter 浅层协同 | §4、§12 |
| 表/字段 | 否 | 无数据库变化 | §6、§10 |
| API/内部契约 | 是 | additive typed getter 和 denial context | §7、§11 |
| 原有功能 | 是 | 数值写回和拒绝观测更准确 | §8、§12 |
| 迁移/发布 | 否 | 无数据迁移 | §8、§10 |

#### 3.2 范围外与明确不变项

- package-private 授权边界、Context/Scope ownership、WRITE 暂存/提交/锁顺序按用户决定保持现状。
- Compiler grammar、System/RuleView 编译、PolicyIndex、Container、declaration 迁移和 P3-P7 不变。

### 4. 目标方案与职责边界

<!-- DESIGN-NARRATIVE-TARGET -->
`RuntimeFactValue` 增加 Java 8 风格 scalar typed getter；MODEL codec 直接按 Kind switch 并递归转换 LIST/OBJECT，不解析 canonical JSON。兼容扩展 `ProtectedAccessInvocation`，只增加 optional RuleViewKey 作为诊断调用上下文，不作为授权事实；旧 factory 保持 RuleView 为空。新增 immutable `ProtectedAccessDecisionContext`；System/operation/path 来自 exact ModelAccessRuleKey，RuleView 来自调用上下文且 owner 必须一致。只有精确命中 `CompiledModelAccessRule` 后发生的运行时拒绝才使用该已发布规则的 SourceRef；`POLICY_NOT_FOUND`、operation/path 不匹配没有适用的已发布访问规则，SourceRef 必须为 `Optional.empty()`，禁止伪造或模糊回查。production `deny` helper 必须调用完整 factory。测试沿公开生产入口构建真实 composition，再分别使用 Rule、change、custom action entry。

#### 4.1 目标结构

```text
RuntimeFactValue
  -> kind + bool/integer/decimal/string/list/object getter
  -> RuntimeFactValueCodec.toJavaValue(kind switch)

ProtectedAccessInvocation(ruleView? diagnostic only) + exact CompiledModelAccessRule?
  -> ProtectedAccessDecisionContext
  -> ProtectedAccessDenial

EngineContext -> production root -> load -> scope -> composition
  -> ruleEntry/changeEntry/customActionEntry
  -> ALLOW or DENY with decision context
```

#### 4.2 组件职责

| 组件/模块 | 当前职责 | 调整后职责 | 对外入口 | 依赖 | 不应承担的职责 |
|---|---|---|---|---|---|
| dec-core-context | 不可变值和结果 DTO | typed scalar getter、decision context、denial getter | context value API | 无反向依赖 | MODEL effect |
| dec-core-model | ModelData value adapter | 按 Kind 恢复 Java 值 | 现有 operation port | context | 解析 canonical 文本 |
| dec-core-starter | Guard 与 composition | production DENY 组装完整 context | ProtectedAccessPort | context+model | 伪造 SourceRef |
| tests | API/行为证据 | 使用真实生产链和三类入口 | Maven/JUnit | production modules | 反射代替验收 |

### 5. 核心流程、状态与失败路径

<!-- DESIGN-NARRATIVE-FLOW -->
ALLOW 主流程保持不变。WRITE 值进入 MODEL 时，codec 按 Kind 转换；DECIMAL 无论 canonical 外观如何都返回 BigDecimal。DENY 时，STARTER 在返回结果前构造 decision context。System、operation、path 始终来自 ModelAccessRuleKey；只有 exact rule 已命中时 SourceRef 才使用该 published rule，找不到规则或 operation/path 不匹配时 SourceRef 为 Optional.empty。RuleView 来自调用上下文，仅用于诊断关联且不参与授权；存在时 owner 必须与 System 一致。旧 invocation factory 保持 RuleView 为空，不合成任何 SourceRef。

#### 5.1 主流程

```text
WRITE RuntimeFactValue
 -> switch(kind)
 -> DECIMAL getter returns BigDecimal
 -> existing MODEL write/effect

Guard denial
 -> collect System/RuleView/operation/path/SourceRef
 -> ProtectedAccessDecisionContext
 -> ProtectedAccessDenial
 -> ProtectedAccessResult.deny
```

#### 5.2 关键失败路径

```text
Kind mismatch -> stable conversion failure -> existing RUNTIME_WRITE_FAILED
policy absent or operation/path mismatch -> DENY with System/operation/path, SourceRef empty
exact rule matched then runtime target/effect failure -> DENY with published rule SourceRef
production test without real root/scope/composition -> INVALID TEST EVIDENCE, not PASS
```

#### 5.3 状态与步骤

| 状态/步骤 | 前置条件 | 执行动作 | 数据变化 | 外部副作用 | 成功结果 | 失败/恢复 |
|---|---|---|---|---|---|---|
| typed conversion | non-null RuntimeFactValue | switch exact Kind | 生成独立 Java value | 无 | Kind 保真 | mismatch 返回现有失败 |
| DENY context | invocation + exact rule（适用时） | 冻结定位字段 | immutable DTO | 无 | stable denial | 不泄露模型值、不伪造来源 |
| production test | Context + real request/origin | 完整构造并以相同 READ/缺策略请求调用三类 entry | 测试内数据 | 受控 fixture，无数据库连接 | 可观察 ALLOW/DENY | setup shortcut 判 INVALID |

### 6. 数据与持久化方案

<!-- DESIGN-NARRATIVE-DATA -->
无数据库变化。新增数据都是不可变进程内事实：typed scalar 值和 denial decision context。DECIMAL 底层继续保存 BigDecimal；LIST/OBJECT 返回深度不可变 RuntimeFactValue，并在 MODEL 转换时创建新的 Java collection。DENY context 只携带身份和来源定位，不携带 write value、originObject 或 ModelData。

#### 6.1 单次请求内的持久化生命周期

1. 读取：RuntimeFactValue 从 immutable value 读取 exact Kind 和 typed value。
2. 校验：getter 校验 Kind；invocation/decision context 校验 System/operation/path 非空及 RuleView owner 一致；SourceRef 仅在 exact published rule 命中后存在。
3. 内存变更：MODEL 创建新的 scalar/list/map replacement；现有 WRITE 顺序保持。
4. 保存：沿用现有 ModelData/Container 行为，无 Repository/Mapper。
5. 副作用：沿用现有 effect；本轮不改变顺序。
6. 提交：沿用现有 mutation version 行为。
7. 失败：转换失败沿用 RUNTIME_WRITE_FAILED；DENY 不包含敏感值。

### 7. 接口、交互与兼容策略

<!-- DESIGN-NARRATIVE-COMPATIBILITY -->
所有变化均为 Java 源码兼容扩展。`RuntimeFactValue` 新 getter 不改变 factory、equals、hashCode、canonicalForm；旧 denial factory 保留，production Guarded port 迁移到新 factory。当前 P2 的 READ/WRITE enum 不变，EXECUTE 由既有决定明确 N/A。无 HTTP、页面或数据库 contract。

#### 7.1 页面业务行为

| 页面/组件编号 | 操作编号 | 功能/流程 | 字段与数据来源 | 权限与可用条件 | 成功/失败状态 | 接口/设计引用 |
|---|---|---|---|---|---|---|
| 不适用 | 不适用 | Java 核心运行时 | 不适用 | 不适用 | 不适用 | §11.1 |

#### 7.2 事务、并发、幂等与一致性

| 关注点 | 设计选择 | 生效边界 | 冲突/重复如何检测 | 失败与恢复 | 验证方式 |
|---|---|---|---|---|---|
| 事务 | 保持现有行为，本轮不调整 | existing MODEL WRITE | existing stamp/version | existing rollback | 既有回归 |
| 并发 | 保持现有行为，本轮不调整 | existing scope/path | existing coordination | existing behavior | 既有回归 |
| 幂等 | immutable value/context | DTO construction | equals/hashCode | 无副作用 | repeated construction test |
| 一致性 | Kind 与 Java 类型一一对应；同输入 denial 相等 | value/result | Kind/source/key exact match | fail closed | decimal/denial tests |

### 8. 开发者交接摘要

<!-- DESIGN-NARRATIVE-HANDOFF -->
先添加 decimal 和 denial context RED；再扩展 context DTO、替换 MODEL codec、迁移 STARTER deny helper；随后改造 production lifecycle 测试，禁止反射和预制 port。最后运行 context/model/starter 专项与全量 Maven 回归，使用脚本重建追踪矩阵。

#### 8.1 建议实施顺序

1. 新增 decimal、denial、真实三入口 RED 并保存失败输出。
2. 修改 context/model/starter，逐项转 GREEN。
3. 执行专项、全量测试和独立 Review，更新追踪事实。

#### 8.2 开发开始前仍需确认

- 无。三个排除项由用户当前明确确认；剩余接口和失败语义均有需求与设计依据。

## 第二部分：开发实施明细

### 9. 需求映射与总变更清单

#### 9.1 功能与验收映射

| 功能/规则/AC/TR | 设计锚点 | DB 版本增量 | API 版本增量 | 验证入口 |
|---|---|---|---|---|
| BR-018 / AC-009 | §4、§5、§11 | 不适用 | denial DTO additive | CASE-P2-R40-DENIAL-CONTEXT-001 |
| RuntimeFactValue exactness / Java 8 产物兼容 | §4、§6 | 不适用 | typed getter additive | CASE-P2-R40-DECIMAL-KIND-001 |
| AC-006/007 测试有效性 | §5、§8 | 不适用 | 无 production API 变化 | CASE-P2-R40-PRODUCTION-CONSUMERS-001 |
| P2 全部 AC 回归 | §13 | 不适用 | 不适用 | CASE-P2-R40-FULL-REGRESSION-001 |

#### 9.2 总变更清单

<!-- DESIGN-CHANGE-INVENTORY -->
| 变更对象 | 变更类型 | 当前事实与证据 | 目标变化 | 作用 | Owner | 兼容要求 |
|---|---|---|---|---|---|---|
| RuntimeFactValue | MODIFY | 缺 scalar typed getter | additive getter | Kind 保真 | dec-core-context | 旧 API 不变 |
| RuntimeFactValueCodec | MODIFY | canonical parser | Kind switch | 正确写回 | dec-core-model | 支持七种 Kind |
| ProtectedAccessInvocation | MODIFY | 无 RuleView | additive optional RuleView 诊断字段 | 拒绝关联适用调用上下文 | dec-core-context | 旧 factory 保持 empty；不携带授权或 SourceRef |
| ProtectedAccessDecisionContext | ADD | 当前不存在 | immutable location DTO | AC-009 | dec-core-context | 不含敏感值 |
| ProtectedAccessDenial/Guarded port | MODIFY | 三字段/minimal deny | additive context/production 强制 | 可定位 | context/starter | 旧 factory 保留 |
| P2 tests | MODIFY | reflection/prebuilt/null coverage | real production chain | 真实证据 | tests | 不删除既有 oracle |

#### 9.3 实现策略决策

<a id="implementation-strategy-decisions"></a>

<!-- DESIGN-IMPLEMENTATION-DECISIONS -->
| 决策 ID | 对象 | 策略 | 现有候选与代码证据 | REUSE 不适用理由 | COMPATIBLE_EXTEND 不适用理由 | MODIFY 不适用理由 | 公共逻辑处理 | 公共逻辑 Owner | 兼容、调用方与验证 | 关联蓝图 |
|---|---|---|---|---|---|---|---|---|---|---|
| IMPL-DEC-P2-R36-001 | RuntimeFactValue typed getter | COMPATIBLE_EXTEND | `dec-core-context/.../RuntimeFactValue.java` 已持有 private Kind/value | 现有 canonicalForm 不能表达原 Kind | 不适用，选择 COMPATIBLE_EXTEND | 不适用 | REUSE_SHARED | dec-core-context | 兼容：旧 API 默认行为不变；调用方：MODEL codec；验证：decimal/nested | BP-P2-R36-001 |
| IMPL-DEC-P2-R36-002 | RuntimeFactValueCodec | MODIFY | `RuntimeModelAccessScope.java#RuntimeFactValueCodec` | 现有 parser 会丢 Kind | 单纯新增分支仍保留错误猜测 | 不适用，选择 MODIFY | REUSE_SHARED | dec-core-model | 兼容：七种 Kind；调用方：MODEL WRITE；验证：round-trip | BP-P2-R36-002 |
| IMPL-DEC-P2-R36-003 | invocation/decision context | CREATE | `ProtectedAccessInvocation.java` 无 RuleView，`ProtectedAccessDenial.java` 只有三字段；建议新增 `ProtectedAccessDecisionContext.java` | 无现有完整定位对象 | 只扩展 invocation 不能形成稳定 denial value | 修改 denial 单类会复制 exact-rule 来源语义 | EXTRACT_NEW | dec-core-context | 兼容：旧 invocation/denial factory 保留；不合成 SourceRef；调用方：STARTER；验证：AC-009 | BP-P2-R36-003 |
| IMPL-DEC-P2-R36-004 | production denial helper | MODIFY | `GuardedProtectedAccessPort.java#deny` 调三参数 factory | 三参数 helper 不能形成 AC-009 定位事实，不能原样复用 | 仅添加 overload 不会迁移 production | 不适用，选择 MODIFY | REUSE_SHARED | dec-core-context | 兼容：DenialCode/message 不变；调用方：全部 production deny；验证：稳定字段 | BP-P2-R36-004 |
| IMPL-DEC-P2-R36-005 | production lifecycle tests | MODIFY | 现有 lifecycle/security tests 使用 reflection/prebuilt/null | 现有证据不代表真实入口 | 增加旁路 Case 仍不修正旧 oracle | 不适用，选择 MODIFY | KEEP_LOCAL | dec-core-starter tests | 兼容：保留旧断言；调用方：三类 consumer entry；验证：真实 root | BP-P2-R36-005 |

### 10. 表、字段与数据读写明细

#### 10.1 表/存储对象变化

<!-- DESIGN-TABLE-CHANGES -->
| 表/存储对象 | 变更类型 | 作用与 Owner | 新增/修改内容 | 主键/唯一性 | 生命周期 | 迁移/回填 | DB 增量与实现锚点 |
|---|---|---|---|---|---|---|---|
| 数据库/外部存储 | UNCHANGED | 本轮仅内存 DTO/adapter，Owner=context/model | schema、表、字段均保持现状 | 不适用，因为无数据库对象 | 随请求/Scope | 无需迁移或回填 | [P2 changeset baseline](changes/p2-business-model-unified-protected-access-remediation-r12.yaml)；`RuntimeFactValue.java` |

#### 10.2 字段变化与业务语义

<!-- DESIGN-FIELD-CHANGES -->
| 表 | 字段 | 变更类型 | 类型/长度 | 空值/默认值 | 业务语义与作用 | 值来源 | 创建时写入 | 更新条件 | 是否可变 | 索引/约束 | 敏感性 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 内存 DTO（无数据库） | invocation/decision context | ADD | typed Java fields | RuleView、SourceRef 按适用性 Optional | 定位 DENY | invocation/exact published rule | invocation/denial 创建时 | 不更新 | immutable | System/operation/path 必填；RuleView owner 一致 | 不含模型值 |
| 内存 value（无数据库） | decimal typed value | REUSE | BigDecimal | 非空 | 保持 DECIMAL Kind/精度 | RuntimeFactValue factory | value 创建时 | 不更新 | immutable | Kind=DECIMAL | 非敏感通用值 |

#### 10.3 业务动作读写矩阵

<a id="data-write"></a>

<!-- DESIGN-DATA-ACCESS -->
| 业务动作 | 触发入口 | 表/聚合 | 操作 | 字段 | 查询/更新条件 | 读取一致性/锁 | 写入时机与保存方式 | 事务 | 失败后的事实 | Trace |
|---|---|---|---|---|---|---|---|---|---|---|
| typed WRITE conversion | existing MODEL port | RuntimeFactValue/ModelData | READ+UPDATE | exact path value | Kind exact | 保持现有锁 | typed conversion 后沿用现有保存 | 保持现有 | existing DENY/version | CASE decimal |
| protected DENY | Rule/change/custom action | invocation/rule/result | READ+CREATE | location fields | denial branch | immutable snapshot | 返回 result，不持久化 | 不涉及数据库事务；只构造 immutable result | stable context/no effect | AC-009 |

#### 10.4 索引、历史数据与上线顺序

- 索引与约束：无数据库索引；DTO 构造器校验 System/operation/path 必填及 RuleView owner 一致，SourceRef 仅在 exact published rule 存在时提供。
- 历史数据处理：不适用，无持久化变化。
- 兼容读写窗口：context getter/additive factory 与旧调用可同版本替换。
- DDL/数据脚本顺序与回滚：不适用；源码 checkpoint 回滚。

### 11. 接口与字段映射明细

#### 11.1 接口变化

<!-- DESIGN-API-CHANGES -->
| 接口/方法 | 类型 | 调用方 | 请求变化 | 响应变化 | 校验与权限 | 幂等 | 错误码 | 事务边界 | 兼容策略 | API 增量与实现锚点 |
|---|---|---|---|---|---|---|---|---|---|---|
| RuntimeFactValue scalar getter | ADD | MODEL codec/tests | 同一 immutable value | typed scalar | Kind mismatch 抛 IllegalStateException | 纯函数 | 不适用 | 无数据库事务 | additive | [baseline](changes/p2-business-model-unified-protected-access-remediation-r12.yaml)；`RuntimeFactValue.java` |
| ProtectedAccessInvocation contextual factory | ADD | Rule/change/custom action adapters | optional RuleView（仅诊断） | immutable invocation | RuleView 存在时 owner 一致；不授予权限 | value equality | IllegalArgumentException | 不涉及数据库事务；构造 immutable invocation | 旧 factory 保持 RuleView empty | [baseline](changes/p2-business-model-unified-protected-access-remediation-r12.yaml)；`ProtectedAccessInvocation.java` |
| ProtectedAccessDecisionContext.of | ADD | STARTER deny helper | System/RuleView/op/path/optional SourceRef | immutable DTO | owner/必填校验；SourceRef 只能取 exact rule | value equality | IllegalArgumentException | 不涉及数据库事务；构造 immutable DTO | 新类型 | [baseline](changes/p2-business-model-unified-protected-access-remediation-r12.yaml)；建议新增 `ProtectedAccessDecisionContext.java` |
| ProtectedAccessDenial.of(...context) | ADD | STARTER | 增加 context | denial.context getter | production 必须使用完整 overload | value equality | 现有 DenialCode | 不涉及数据库事务；构造 immutable result | 旧 overload 保留 | [baseline](changes/p2-business-model-unified-protected-access-remediation-r12.yaml)；`ProtectedAccessDenial.java` |
| GuardedProtectedAccessPort.deny | MODIFY | protected entry | 从 invocation/rule/source 提取 context | DenialCode/message 保持 | 不改变权限判定 | 一次 result | 现有 code | 现有单次访问边界，不增加数据库事务 | internal | [baseline](changes/p2-business-model-unified-protected-access-remediation-r12.yaml)；`GuardedProtectedAccessPort.java#deny` |

#### 11.2 字段端到端映射

<a id="api-mapping"></a>

| 请求/事件字段 | 归一化/校验 | 领域对象/命令字段 | 表字段/外部契约 | 响应/事件字段 | 脱敏/审计 |
|---|---|---|---|---|---|
| ruleKey.owner | exact SystemKey | context.system | 无数据库 | denial.context.system | 仅稳定 key |
| invocation ruleView | owner 与 System 一致；仅作为诊断上下文，不作为 Guard 授权输入 | context.ruleView | 无 | Optional RuleViewKey | 不含配置正文 |
| operation/path | enum/ModelPath | context.operation/path | ModelData exact path | denial context | 不输出值 |
| exact rule SourceRef | exact rule 命中后使用 published source；未命中或 operation/path mismatch 时 empty，禁止模糊查找和调用方伪造 | context.sourceRef | 无 | Optional SourceRef | 只输出可信定位 |
| RuntimeFactValue.DECIMAL | kind check | BigDecimal | ModelData value | existing receipt/read | 不记录值 |

### 12. 原有功能调整与代码改动

#### 12.1 原有功能调整

<!-- DESIGN-LEGACY-ADJUSTMENTS -->
| 原功能/场景 | 当前行为与证据 | 调整后行为 | 受影响入口/调用方 | 数据兼容 | API/UI 兼容 | 回归范围 | 发布/回滚注意事项 |
|---|---|---|---|---|---|---|---|
| DECIMAL write | integral canonical → Long/overflow | 始终 BigDecimal | MODEL WRITE | 值兼容、类型修正 | additive getter | model tests | context+model 同版本 |
| production DENY | 三字段 | context 定位字段 | 三类 entry | 无持久化 | additive | context/starter | 不泄露值 |
| lifecycle test | shortcut fixture | 真实 production root/consumer | tests | 不适用 | production API 不变 | starter/full | setup 失败不算 PASS |
| 三个排除项 | R34 proposed changes | 保持 R33 当前实现 | runtime internals | 保持 | 保持 | 既有回归 | 用户当前授权 |

#### 12.2 代码改动蓝图

<a id="code-blueprint"></a>

<!-- DESIGN-CODE-BLUEPRINT -->
| 蓝图 ID | 顺序 | 仓库/模块 | 文件、类或配置 | 变更类型 | 方法/符号 | 决策 ID | 具体改动 | 作用 | 公共逻辑 Owner | 来源文档 | 依赖 | 对应测试 |
|---|---:|---|---|---|---|---|---|---|---|---|---|---|
| BP-P2-R36-001 | 1 | context | `dec-core-context/src/main/java/dec/core/context/runtime/RuntimeFactValue.java` | MODIFY | bool/integer/decimal/string getter | IMPL-DEC-P2-R36-001 | Java 8-compatible typed getters | 类型边界 | dec-core-context | [需求 Java 8](../FEATURE-DESC-3361AD2E54FC/requirement.md#105-兼容与历史数据)、[本设计 §6](#6-数据与持久化方案) | Kind/value | CASE decimal |
| BP-P2-R36-002 | 2 | model | `dec-core-model/src/main/java/dec/core/model/runtime/RuntimeModelAccessScope.java#RuntimeFactValueCodec` | MODIFY | toJavaValue | IMPL-DEC-P2-R36-002 | Kind switch/recursive conversion，删除 Parser | 写回保真 | dec-core-model | [需求 Java 8](../FEATURE-DESC-3361AD2E54FC/requirement.md#105-兼容与历史数据)、[本设计 §6](#6-数据与持久化方案) | BP-001 | CASE decimal |
| BP-P2-R36-003 | 3 | context | `dec-core-context/src/main/java/dec/core/context/runtime/ProtectedAccessInvocation.java`；建议新增 `ProtectedAccessDecisionContext.java`；修改 denial | ADD/MODIFY | contextual factory/of/getters/equals | IMPL-DEC-P2-R36-003 | invocation 只携带 optional RuleView；decision context 的 SourceRef 仅取 exact published rule | AC-009 | dec-core-context | [需求 AC-009](../FEATURE-DESC-3361AD2E54FC/requirement.md#ac-p2-system-ruleview-009-diagnostic-可定位且确定) | typed keys/SourceRef | CASE denial |
| BP-P2-R36-004 | 4 | starter | `dec-core-starter/src/main/java/dec/core/starter/access/GuardedProtectedAccessPort.java` | MODIFY | deny/invoke/read/write | IMPL-DEC-P2-R36-004 | production deny 传入 exact rule（适用时）并构造 context | 完整定位 | dec-core-context | [需求 BR-018](../FEATURE-DESC-3361AD2E54FC/requirement.md#6115-业务规则) | BP-003 | CASE denial |
| BP-P2-R36-005 | 5 | tests | `P2SecurityAuthorityRemediationTest.java`、`SingleEngineContextRuntimeLifecycleTest.java`、context/model tests | MODIFY | listed Case methods | IMPL-DEC-P2-R36-005 | 去除反射/prebuilt/null 假证明；真实三入口以同一 READ ALLOW/缺策略 DENY 验证 | 证据可信 | test modules | [需求 AC-007](../FEATURE-DESC-3361AD2E54FC/requirement.md#ac-p2-system-ruleview-007-所有变更入口不可旁路)、[本设计 §8](#8-开发者交接摘要) | BP-001..004 | CASE production/full |

### 13. 异常、安全、观测与验证明细

#### 13.1 异常、安全与可观测性

| 场景 | 稳定错误/告警 | 对外表现 | 日志/指标/Trace | 敏感数据处理 | 数据与状态结果 | 人工处置 |
|---|---|---|---|---|---|---|
| Kind mismatch | existing RUNTIME_WRITE_FAILED | DENY | Kind/path | 不输出值 | existing rollback/version | 修正调用值 |
| DECIMAL boundary | 无错误 | existing ALLOW | test evidence | 不记录值 | BigDecimal 保存；保留数值和 Kind，不承诺原始 scale | 无 |
| policy not found | POLICY_NOT_FOUND | DENY + context | System/op/path/source（适用） | 不输出模型值 | no effect | 修正策略 |
| runtime failure | existing DenialCode | DENY + published source | stable context | 不输出 origin/write | existing state | 检查运行对象 |
| invalid test setup | INVALID TEST EVIDENCE | 不计 PASS | command output | 无 | 不推进阶段 | 修正 fixture |

#### 13.2 验证计划

<!-- DESIGN-VERIFICATION -->
| 验证项 | 对应需求/风险 | 测试层级 | 前置数据 | 操作 | 可观察结果 | 失败注入/边界 | 自动化命令或证据入口 |
|---|---|---|---|---|---|---|---|
| decimal Kind | value exactness | unit/integration | 1.0、超 Long、负数、nested、INTEGER | protected WRITE/codec | DECIMAL 为 BigDecimal 且 compareTo=0；INTEGER 仍为 Long；不要求原始 scale | strip zeros/large | model/context tests |
| DENY 定位 | AC-009 | unit/integration | System/RuleView/op/path/SourceRef | 触发 policy/runtime deny | 字段稳定且无敏感值 | rule absent/source N/A | context/starter tests |
| 三类生产入口 | AC-006/007 testability | integration | real Context/root/load/scope/composition | `ruleEntry/changeEntry/customActionEntry` 各执行语义相同的 READ ALLOW 与缺策略 DENY | 三类结果一致，DENY context 的适用字段完整；无数据库 WRITE 依赖 | closed/policy missing | starter lifecycle test |
| EXECUTE 范围契约 | DEC-P2-ACCESS-OPERATIONS-001 | contract | enum/source | API scan | exactly READ/WRITE；EXECUTE 由已确认决定排除 | hidden EXECUTE | compiler/context tests |
| Java 8 产物兼容与全量回归 | P2 AC | regression | JDK 17+ + repository | 校验根 POM `maven.compiler.release=8`，执行 `./mvnw test` 并检查代表 class major version=52 | exit 0；字节码为 Java 8；记录总数、failures/errors/skipped 和全部模块 | no skip | Maven/classfile evidence |

#### 13.3 开发就绪门禁

- [x] 第一部分独立说明现状、目标、流程、失败和范围裁剪。
- [x] 每个范围内功能有设计锚点和验证入口。
- [x] 表/字段变化不适用理由及内存数据语义完整。
- [x] 接口变化有调用方、映射、错误、幂等和兼容说明。
- [x] 原功能调整有当前证据、目标行为和回归范围。
- [x] 每个代码改动有定位、职责、依赖、顺序和测试。
- [x] 每个实现对象有 `IMPL-DEC-*`，CREATE 已排除前序策略并声明 Owner。
- [x] 蓝图关联决策、来源和验证。
- [x] 本轮事务/并发明确保持现状，无未决实现语义。
- [x] 无 API/DB wire changeset；baseline 和代码锚点一致。

## 附录：追踪、决策与未决项

### A.1 追踪矩阵

| TR/AC | 设计章节 | 表/字段 | API/内部契约 | 代码蓝图 | 验证项 | 状态 |
|---|---|---|---|---|---|---|
| AC-009/BR-018 | §4/§11 | decision context | denial additive API | BP-003/004 | CASE denial | COVERED |
| RuntimeFactValue/Java 8 | §4/§6 | decimal | typed getter/codec | BP-001/002 | CASE decimal | COVERED |
| AC-006/007 evidence | §8/§13 | 不适用 | production entries | BP-005 | CASE production | COVERED |
| P2 all AC | §13 | 不适用 | regression | BP-005 | CASE full | COVERED |

### A.2 已确认决策

| 决策 ID | 问题 | 选择 | 理由 | 替代方案与放弃原因 | 决策人/证据 |
|---|---|---|---|---|---|
| DEC-P2-R36-001 | DECIMAL 如何写回 | 按 Kind 返回 BigDecimal | canonical 文本不能承载 Kind | 继续 parser 会丢类型/溢出 | P2 Review P1 |
| DEC-P2-R36-002 | production DENY 定位 | 新 context；SourceRef 只取 exact published rule，未命中时 empty | BR-018/AC-009 要求适用字段且来源必须真实 | 调用方 SourceRef 或 legacy 常量都不能作为可信来源 | P2 Review P1 |
| DEC-P2-R36-003 | 哪些 R34 问题进入本轮 | R34-001/002/003 均不考虑 | 用户当前明确范围授权 | 继续扩展会违背当前指令 | 用户指令 |
| DEC-P2-R36-004 | 当前 operation 集合 | 精确 READ/WRITE；EXECUTE=N/A | 已确认 Requirement delta | 静默新增 EXECUTE 无 source semantics | [既有决定](changes/p2-access-operations-decision-r01.md#2-decision) |
| DEC-P2-R36-005 | 构建与 Java 8 兼容验证 | JDK 17+ 构建、release=8、class major=52 | 根 POM 的 enforcer 与 compiler contract | 要求 Java 8 runtime 会与构建门禁冲突 | P2 TestDesign Review P1 |

### A.3 未决问题

| 问题 | 为什么阻断开发 | 需要谁决定 | 截止条件 | 影响章节 |
|---|---|---|---|---|
| 无 | 当前范围和实现语义已明确 | 不适用 | 不适用 | 不适用 |
