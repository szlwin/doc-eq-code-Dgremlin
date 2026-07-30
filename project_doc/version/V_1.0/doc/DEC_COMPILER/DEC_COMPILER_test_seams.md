# DEC_COMPILER 测试接缝

> 候选 Revision：`DESIGN-R05@0b37a9b4dd48`。`DESIGN-R04@1c14c8e89779` 已被 `REV-000038` 退回；当前为 DESIGN I007 返修候选，不复用旧 Review/Evidence。本文件固定返修候选的可测试 seam、数据集、负向断言和阶段边界。

## 1. 测试接缝 {#test-seams}

- `InMemoryDocumentSourceProvider`：按 sourceId 返回字节和 SourceOrigin；
- `DeterministicFileSetProvider`：支持随机/逆序枚举，验证结果稳定；
- `SecureXmlFrontendHarness`、`SafeYamlFrontendHarness`；
- `PassHarness`：单独执行 Pass 并读取 Diagnostic/Builder 快照；
- `SymbolTableFixtureBuilder`；
- `InformationExpressionFixture`；
- `ModelAccessFixture`；
- `DiagnosticSnapshot`、`SemanticDigestSnapshot`；
<<<<<<< HEAD
- `ContextPublisherSpy`：记录同一 compileAndPublish 调用中的发布次数和 expected/candidate；
- `CancellationTokenStub`、`MonotonicClockStub`：使用同一纳秒域确定性触发 Deadline；
- `CompilationObserverSpy`：验证四类 timing、完整状态转换与 Observer 故障隔离。

## 2. 实际 mix 合同 {#test-mix-contract}

AC001 合同测试只向 MixSourceResolver 传入 classpath `mix/orm-config.xml` 根入口。期望 SourceManifest 的 10 个唯一 sourceId 固定为：

```text
classpath:mix/orm-config.xml
classpath:mix/data/User.xml
classpath:mix/data/Order.xml
classpath:mix/data/Pay.xml
classpath:mix/view/orm-view.xml
classpath:mix/system/systems.xml
classpath:mix/rule/user-rule.xml
classpath:mix/rule/order-rule.xml
classpath:mix/rule/payment-rule.xml
classpath:mix/business/order-business.xml
```

期望 declaration edge multiset 固定为 7 条，与 REQAN-R05 的 SourceEdgeType 一致：root 通过 `ROOT_DATA_FILESET` 指向 `classpath:mix/data/`，通过 `ROOT_VIEW_FILESET` 指向 `classpath:mix/view/`，通过 `ROOT_SYSTEM_FILE` 指向 systems，通过 `ROOT_BUSINESS_FILE` 指向 business；systems 通过 3 条 `SYSTEM_RULE_FILE` 分别指向 user/order/payment rule。目录边展开出的 3 个 data 与 1 个 view 只进入 SourceManifest，不伪造额外声明边。每条边断言精确 edgeType/fromSourceId/targetReference，且 declarationSourceRef 指向声明元素的 sourceId、起始行和起始列；root Source 本身的 manifest origin 使用 synthetic root SourceRef。测试 expected 常量不得从 Resolver 实际输出反推。额外 XML、缺边、多边或 SourceRef 不同都必须失败。主资源 10 个与测试镜像 10 个分别验证字节一致和可解析，不能合并计为一个 20 节点源图。现有 MixContractTest 的 `visited >= 10` 仅作为 fixture 安全检查，不满足 AC001；真正的 `MixSourceResolverContractTest` 在 compiler 实现任务中新增。

同一根图继续断言 inventory：5 Data、2 View、4 System、14 RuleView、16 Information、1 Scope、5 Directory、8 Action、4 Produce。

## 3. Source/Frontend 测试 {#test-source-frontend}

- 文件枚举顺序变化不改变 SourceManifest/digest；
- 缺失文件、重复 ID 不同内容、循环、深度/数量超限；
- `../`、编码路径和 symlink 逃逸；
- Provider 的 RESOLVED/FAILED typed result 不变量；`resolve()` 精确返回 1 个 Source，`resolveFileSet()` 返回至少 1 个；null、抛异常、错误基数或 RESOLVED 空列表统一形成 `MIX-SOURCE-POLICY`；
- XML XXE/DTD/外部 schema；
- YAML 任意 tag/type、别名炸弹和节点数超限；
- 未知节点在所有 P1 编译模式精确产生 `MIX-STRUCTURE-UNKNOWN` ERROR，不存在静默 lenient 发布；
- Canonical 不持有解析器对象。

## 4. Symbol/Information 测试 {#test-symbol-information}

- 同类型重复 Key 阻断，不同类型同名隔离；
- 前向引用成功，未知/类型不匹配失败；
- 16 Information 全部 owner 为 System；BusinessScope 无 Information；
- `common.paySuccess/payError` 依赖精确；
- 普通 System 跨 System expression、裸引用、未知引用失败；
- common 声明 Data/View/RuleView/ModelAccess 或非 expression Information 失败；
- P1 只登记 P3 Deferred，不执行表达式和循环判定。

## 5. ModelAccess 测试 {#test-model-access}

- `OrderInfo.user -> UserInfo(target-main=user)` 命中 target-main；
- target-main 优先于同名 property path；
- 未命中 target-main 后精确解析嵌套 path；
- 大小写差异、缺段、中间段非复合失败；
- View 未在当前 System 声明失败；
- 多候选、重复 ref、重叠 write 失败；
- 证明没有跨 View/System 搜索、root-property 或模糊降级。

## 6. Deferred 与阶段边界 {#test-deferred}

每个 Deferred 断言 ownerKey、kind、requiredStage、reasonCode、SourceRef、resolvedReferences。P2～P7 对象覆盖完整；缺字段阻断。测试明确证明 P1 不调用 Information evaluator、Action executor、Directory engine、Query planner 或 Transaction manager。

## 7. Publication/并发测试 {#test-publication}

- PUBLISHED 只在同一 compileAndPublish 调用中发布一次完整 Context，调用返回后 Publisher 调用次数不再增加；
- PublicationResult 仅允许 PUBLISHED/CONFLICT；null、异常和 CONFLICT 的映射逐项验证；
- ERROR、timeout、cancel、Context 构造失败、CAS conflict、Publisher null/异常均返回 `status=FAILED`；分别断言精确 code 为 `MIX-PUBLICATION-BLOCKED`、`MIX-COMPILATION-TIMED-OUT`、`MIX-COMPILATION-CANCELLED`、`MIX-CONTEXT-CONSTRUCTION-FAILED`、`MIX-PUBLICATION-CONFLICT`、`MIX-PUBLICATION-FAILURE`；
- 上述 FailedCompilationResult 不提供 model/context/digest；Publisher 在语义 ERROR/timeout/cancel/构造失败时调用 0 次，在 CAS conflict 或 Publisher failure 时调用 1 次；
- 旧 Context 保持可读且 digest 不变；
- 两个 Session 并发不共享 SymbolTable/Diagnostic；
- 同义 XML/YAML、不同文件枚举和不同线程调度得到相同 semanticDigest；
- semanticDigest 的输入快照不含 DigestPair，证明摘要构造无自引用；
- Registry/Context 的修改尝试被拒绝。

## 8. Diagnostic 快照 {#test-diagnostic}

覆盖 BM-R05 23 个业务错误的设计 code；断言 code、severity、SourceRef、definitionKey、relatedRefs、recoveryHint 和排序。排序快照显式断言 `entityKey=definitionKey.canonical()`、无 key 时为空字符串、末列为 `pass`。message 文案变化不应破坏 code 契约。

### 8.1 Timing/Observer 测试 {#test-timing}

- 注入 MonotonicClockStub，断言 discovery、parse、每个 pass、digest 均产生一次非负 elapsedNanos，并用相同 clock 推进到 Deadline；
- 不断言真实墙钟时长；
- Observer/Timing 不进入 semanticDigest；
- Observer 记录需求状态机中所有实际转换；ERROR、cancel、timeout、CAS conflict 必须以从发生状态到 FAILED 结束；
- Observer 抛异常时产生非 ERROR `MIX-OBSERVER-FAILURE`；允许 diagnostics 增加，但原 status、context、digest 不改变。

## 9. 架构与退役测试 {#test-architecture}

- compiler、context、starter 与 frontend 均以 `maven.compiler.source=1.8`、`maven.compiler.target=1.8` 编译；生产源码和公共契约不得使用 `record`、`List.of`、`Map.of`、`Optional.isEmpty`、`var` 等 Java 9+ 语法或 API；
- compiler core 无 DOM4J/SnakeYAML/SQL/MySQL/demo 生产依赖；
- frontend 不调用 ConfigFactory/全局 Config；
- context 不反向依赖 compiler；
- 无 static mutable Context/Registry；
- 仓库/POM/dependency tree/ServiceLoader/反射字符串无 `dec-expand-declaration` 与 Adapter；
- production constant 无 `dec-demo/src/main/resources/mix`。

## 10. 追踪到测试 {#test-traceability}

| TR | 关键测试 |
|---|---|
| TR-P1-COMPILER-001 | source discovery/order/security |
| TR-P1-COMPILER-002 | XML/YAML canonical/raw parity |
| TR-P1-COMPILER-003 | typed symbols/System Information |
| TR-P1-COMPILER-004 | Deferred completeness/boundary |
| TR-P1-COMPILER-005 | Context isolation/digest/publication |
| TR-P1-COMPILER-006 | read-only projection/write rejection |
| TR-P1-COMPILER-007 | retirement residue scan |
| TR-P1-COMPILER-008 | common expression owner/qualified refs |
| TR-P1-COMPILER-009 | target-main/property path/ambiguity |

## 11. 验收命令建议 {#test-commands}

```bash
./mvnw -pl dec-core-compiler -am -Dmaven.compiler.source=1.8 -Dmaven.compiler.target=1.8 test
./mvnw -pl dec-core-context,dec-core-starter -am test
./mvnw -pl dec-demo -am -Dtest=MixContractTest -Dsurefire.failIfNoSpecifiedTests=false test
python3 project_doc/version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py
rg -n '\brecord\b|List\.of|Map\.of|Optional\.isEmpty|\bvar\s+[A-Za-z_]' \
  dec-core-compiler dec-core-context dec-core-starter
```

最后一条扫描预期无输出。依赖下载不可用时，必须记录 SKIPPED 原因；Python/XML 合同和静态结构验证仍需真实执行，不能伪造 Maven 结果。
