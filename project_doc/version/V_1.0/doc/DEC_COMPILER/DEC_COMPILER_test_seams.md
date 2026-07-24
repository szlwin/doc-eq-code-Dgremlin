# DEC_COMPILER P1 测试接缝设计

- Revision：`DESIGN-R01@a7a6820a381e`

## 1. 测试分层

| 层 | 被测接缝 | 不允许外部依赖 |
|---|---|---|
| Value contract | Key、SourceRef、Canonical node、Diagnostic order | 文件系统、数据库 |
| Frontend contract | XML/YAML -> Canonical | 网络、DTD、任意类型构造 |
| Pass unit | 每个 CompilerPass | 具体 parser、starter |
| Compiler contract | sources -> CompilationResult | MySQL/Docker |
| Context contract | immutable Registry/Context/Legacy view | 全局 Config 写入 |
| Architecture | Maven/包依赖方向 | 运行时业务执行 |

## 2. 关键测试替身

- `InMemoryDocumentSource`：提供固定 sourceId/content/digest。
- `StubDocumentFrontend`：直接返回 Canonical 或指定 Diagnostic。
- `RecordingCompilerPass`：记录调用顺序和输入输出。
- `FakeClock/FixedCompilerVersion`：避免时间进入语义摘要。
- `MutationProbe`：验证公开集合与对象不可修改。
- `LegacyWriteProbe`：验证全部写入口明确失败且快照不变。

## 3. Case 矩阵

| Case | 目标 | 核心断言 |
|---|---|---|
| CASE-P1-CANONICAL-001 | XML/YAML 同义 | Canonical/Raw 等价、semanticDigest 相同、位置不同 |
| CASE-P1-DIAGNOSTIC-001 | 多错误聚合 | code/location/entity/pass 完整、稳定排序、无 Context |
| CASE-P1-SYMBOL-001 | 命名空间/前向引用 | 合法解析、同名不同 namespace 并存、重复拒绝 |
| CASE-P1-CONTEXT-001 | 隔离/摘要/不可变 | 两 Context 无污染、重复 digest 一致、mutation 失败 |
| CASE-P1-LEGACY-001 | 只读兼容 | 读取一致、add/remove/clear/set 明确拒绝 |
| CASE-P1-SCOPE-001 | P2+ deferred | Raw 保留、诊断明确、不执行后续语义 |

## 4. 失败与边界

- 空 source 集合、重复 sourceId、不可读源；
- XML 语法、XXE/DTD、深度/文本限制；
- YAML 语法、未知标签、alias bomb、深度限制；
- 未知元素/属性、缺失必填；
- 重复 Key、未知引用、类型不匹配、跨文件前向引用；
- pass 抛基础设施异常；
- ERROR 与 WARN 混合时发布门禁；
- parser Node/RawReference/可变集合泄漏；
- 多线程并发 session 和并发只读 Context；
- stable digest 对属性顺序、文件发现顺序和 HashMap 顺序不敏感；
- Legacy 返回集合和成员对象的深层修改尝试。

## 5. 安全测试

- XML 外部实体和外部 DTD 必须被拒绝且不发起网络请求；
- YAML 任意 Java tag 必须被拒绝；
- Diagnostic 不包含 fixture 中的 secret scalar；
- sourceId/path 不得突破配置的资源根；
- frontend 不调用 Class.forName、URL.openStream 或反射构造业务类。

## 6. 架构测试

- `dec-core-compiler` 不依赖 XML/YAML、starter、SQL/MySQL、runtime、demo；
- `dec-core-context` 不依赖 compiler/parser/runtime；
- Compiled/API 包签名不含 DOM4J/SnakeYAML/JDBC 类型；
- 无新增 static mutable Registry、Config、current Context；
- core tests 在无 MySQL、无 Docker环境运行。

## 7. 通过标准

1. 六项 AC 至少各有一条自动化 contract case；
2. 每个 pass 有正常与失败单测；
3. 故意引入失败使 Maven/CI 非零；
4. 重复运行无 flaky diagnostic/digest；
5. 不通过注释、禁用或 `testFailureIgnore` 让测试变绿。
