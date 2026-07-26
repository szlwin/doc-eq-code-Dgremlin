# DEC_COMPILER 测试接缝

> Revision：TEST-SEAM-R02-DRAFT。

## 1. 单元接缝

- InMemoryDocumentSourceProvider；
- DeterministicFileSetProvider（可打乱枚举顺序）；
- StubFrontend；
- PassHarness；
- DiagnosticSnapshot；
- SemanticDigestSnapshot。

## 2. 实际 `mix` 合同

`MixContractTest` 必须从测试 classpath 的 `mix/orm-config.xml` 启动，不直接读取源码路径。断言源图和定义数量，且对业务语义只验证 Deferred 分类。

## 3. 架构测试

- compiler core 无 DOM4J/SnakeYAML/SQL/MySQL/demo 依赖；
- frontend 不调用 ConfigFactory；
- context 无 compiler 反向依赖；
- 无静态可变 Context；
- 无废弃模块包、artifact、Adapter；
- 无生产常量包含 `dec-demo/src/main/resources/mix`。

## 4. 变异测试方向

- root 删除 business-file-info；
- System 缺 rule-file；
- RuleView system 属性冲突；
- Information 引用未知 rule；
- Directory subdirectory 指向未知目录；
- 同名 BusinessScope；
- Deferred 缺 requiredStage；
- Registry 修改尝试；
- source order shuffle。
