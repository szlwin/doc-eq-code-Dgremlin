# DEC_COMPILER 测试接缝

> Revision：`DESIGN-R04@1c14c8e89779`。本文件固定 DESIGN-R04 的可测试 seam、数据集、负向断言和阶段边界。

## 1. 测试接缝 {#test-seams}

- `InMemoryDocumentSourceProvider`：按 sourceId 返回字节和 SourceOrigin；
- `DeterministicFileSetProvider`：支持随机/逆序枚举，验证结果稳定；
- `SecureXmlFrontendHarness`、`SafeYamlFrontendHarness`；
- `PassHarness`：单独执行 Pass 并读取 Diagnostic/Builder 快照；
- `SymbolTableFixtureBuilder`；
- `InformationExpressionFixture`；
- `ModelAccessFixture`；
- `DiagnosticSnapshot`、`SemanticDigestSnapshot`；
- `ContextPublisherSpy`：记录发布次数和 expected/candidate；
- `CancellationTokenStub`、`DeadlineClockStub`。

## 2. 实际 mix 合同 {#test-mix-contract}

测试从 classpath `mix/orm-config.xml` 启动，禁止生产代码读取源码目录。断言 inventory：5 Data、2 View、4 System、14 RuleView、16 Information、1 Scope、5 Directory、8 Action、4 Produce；主/测试 fixture 字节一致，20 个 XML 均可解析。

## 3. Source/Frontend 测试 {#test-source-frontend}

- 文件枚举顺序变化不改变 SourceManifest/digest；
- 缺失文件、重复 ID 不同内容、循环、深度/数量超限；
- `../`、编码路径和 symlink 逃逸；
- XML XXE/DTD/外部 schema；
- YAML 任意 tag/type、别名炸弹和节点数超限；
- 未知节点在 strict 模式产生稳定 Diagnostic；
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

- SUCCESS 只发布一次完整 Context；
- ERROR、timeout、cancel、Context 构造失败、CAS conflict 均不发布；
- 旧 Context 保持可读且 digest 不变；
- 两个 Session 并发不共享 SymbolTable/Diagnostic；
- 同义 XML/YAML、不同文件枚举和不同线程调度得到相同 semanticDigest；
- Registry/Context 的修改尝试被拒绝。

## 8. Diagnostic 快照 {#test-diagnostic}

覆盖 BM-R04 23 个业务错误的设计 code；断言 code、severity、SourceRef、definitionKey、relatedRefs、recoveryHint 和排序。message 文案变化不应破坏 code 契约。

## 9. 架构与退役测试 {#test-architecture}

- compiler core 无 DOM4J/SnakeYAML/SQL/MySQL/demo 生产依赖；
- frontend 不调用 ConfigFactory/全局 Config；
- context 不反向依赖 compiler；
- 无 static mutable Context/Registry；
- 仓库/POM/dependency tree/ServiceLoader/反射字符串无 `dec-expand-declaration` 与 Adapter；
- production constant 无 `dec-demo/src/main/resources/mix`。

## 10. 追踪到测试 {#test-traceability}

| TR | 关键测试 |
|---|---|
| TR-001 | source discovery/order/security |
| TR-002 | XML/YAML canonical/raw parity |
| TR-003 | typed symbols/System Information |
| TR-004 | Deferred completeness/boundary |
| TR-005 | Context isolation/digest/publication |
| TR-006 | read-only projection/write rejection |
| TR-007 | retirement residue scan |
| TR-008 | common expression owner/qualified refs |
| TR-009 | target-main/property path/ambiguity |

## 11. 验收命令建议 {#test-commands}

```bash
./mvnw -pl dec-core-compiler -am test
./mvnw -pl dec-core-context,dec-core-starter -am test
./mvnw -pl dec-demo -Dtest=MixContractTest test
python3 project_doc/version/V_1.0/task/P1-COMPILER-F01/validation/test_system_information_contract.py
```

依赖下载不可用时，必须记录 SKIPPED 原因；Python/XML 合同和静态结构验证仍需真实执行，不能伪造 Maven 结果。
