# P1 编译领域概念模型（需求分析视图）

## 统一语言

- **DocumentSource**：带稳定 sourceId、格式和内容的输入值。
- **CanonicalDocumentNode**：格式中立的有序文档节点。
- **RawDeclaration/RawDocumentSet**：保留原始声明与未解析引用的中间事实。
- **CompilationSession**：一次编译的一致性边界，拥有独立 Builder 和 DiagnosticCollector。
- **SymbolTable**：按强类型 Key 登记声明的构建期索引。
- **CompiledRegistry**：引用已解析、不可变的发布注册表。
- **EngineContext**：编译成功后对运行时发布的不可变上下文。
- **LegacyConfigView**：EngineContext 的只读兼容投影。

## 一致性边界

CompilationSession 是唯一构建聚合：只有所有必需 pass 无 ERROR 才能一次性生成 CompiledRegistry 和 EngineContext。任何前端、pass 或 adapter 都不能修改已发布 Context。

## 状态

`CREATED → PARSED → STRUCTURALLY_VALIDATED → SYMBOLS_REGISTERED → REFERENCES_RESOLVED → GRAPH_PREPARED → SEMANTICALLY_VALIDATED → PUBLISHED`；任一阶段出现 ERROR 进入 `FAILED`，且不得再进入 `PUBLISHED`。

## 核心不变量

1. 一个 Session 的可变构建状态不对其他 Session 可见。
2. Compiled 对象不包含 parser 节点或未解析引用。
3. EngineContext、Registry 和 Key 具备值语义或不可变读取语义。
4. 诊断排序与语义 digest 不依赖 HashMap 遍历或线程调度。
5. LegacyConfigView 只读且不拥有独立事实。
6. P2+ 声明可被 Raw AST 保留，但不得在 P1 执行。
