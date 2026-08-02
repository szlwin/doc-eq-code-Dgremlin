# TASK-P1-T05 TDD RED Evidence R01

- Revision：`TDD-P1-T05-R01@859c7aacae91`
- Review：`REV-000221`
- Evidence：`EVD-000465`
- Head：`859c7aacae915f3bcc98868fa296ab132d39ad3b`
- P0 Run：`30750109629`
- Artifact：`8834157191`
- Artifact SHA-256：`0b99478c65fb7b958e31cbafe4f49ee8fc8fdfa2bd252112dc79287ccc41c71c`
- Result：`PASSED`

## 有效 RED 证明

- Context：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler：83 run / 0 failures / 0 errors / 0 skipped；
- XML T04：30 run / 0 failures / 0 errors / 0 skipped；
- YAML 生产与测试源码均以 Java release 8 编译成功；
- T05 Oracle：27 run / 27 expected failures / 0 errors / 0 skipped；
- 失败仅来自 `SafeYamlDocumentFrontend` / `YamlFrontendLimits` 尚不存在；
- 不存在依赖缺失、语法错误、fixture 缺失或测试类加载 Error。

## Oracle 独立性

安全和资源负向 Oracle 在断言目标输入失败前，先使用同一 Frontend 解析边界内安全控制样本。因此“拒绝所有 YAML”的伪实现不能使负向测试转绿。

覆盖范围：安全 10、资源 11、Canonical parity 3、架构 3。未制造真实 OOM，用户类型构造通过独立计数探针验证。

## 非正式中间运行

- Run `30749857616` 的 3 个架构用例被 JUnit 记为 Error，已修正为明确 Assertion failure，不作为 Evidence；
- Run `30749918529` 被加入安全控制样本后的最终 RED 取代。
