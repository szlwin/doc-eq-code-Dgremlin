# TASK-P1-T12 I004 Development Evidence

- Development：`DEV-P1-T12-R04@923129b1f20d`
- Evidence：`EVD-000895`～`EVD-000902`
- Review：`REV-000576`～`REV-000580`
- Clean-code Head：`923129b1f20d6bebe589231b770b5c7675b52737`

## Delivered implementation

### Explicit traversal and budgets

- `ArtifactSnapshots` 使用 `ArrayDeque<Task>` 显式 DFS，不再递归调用 freeze；
- 默认预算：depth=256、unique containers=4096、traversed edges=65536、map entries=16384；
- package-private `Limits` 支持精确边界 Oracle，不形成公共 API；
- 任一预算在继续物化或入栈前超限，抛出内部 `ResourceLimitException`。

### Identity memoization

- source identity 状态分为 VISITING 与 FROZEN；
- VISITING 再遇到判定循环；
- FROZEN 再遇到直接复用同一 immutable snapshot；
- 24 层共享 DAG 只遍历唯一图，输出多次引用保持 `assertSame`。

### Structural identity and cached hash

- 标量按 equals/hash 归一化 canonical ID；
- Optional/List/Set/Map 基于 immediate child canonical ID 底向上生成结构 ID；
- Set 元素重复和 Map key collision 通过 canonical ID 判断；
- Frozen List/Set/Map 缓存与 Java 容器合同一致的结构 hash；
- Set/Map 构建不再递归调用共享冻结 DAG 的 `hashCode()`。

### Pipeline failure mapping

- 普通 Pass 和 Publication Pass 分别捕获 `ArtifactSnapshots.ResourceLimitException`；
- 统一映射为 `MIX-PUBLICATION-BLOCKED / pipeline.artifact.resource-exceeded`；
- 资源失败终态为 FAILED，publisher 调用数为 0；
- 非资源型 RuntimeException 继续使用原 `pipeline.pass.failure` 或 publication failure 合同。

## Modified production files

- `dec-core-compiler/src/main/java/dec/core/compiler/pass/ArtifactSnapshots.java`
- `dec-core-compiler/src/main/java/dec/core/compiler/pass/PipelineDiagnostics.java`
- `dec-core-compiler/src/main/java/dec/core/compiler/pass/CompilerPipeline.java`

未修改 Compiler 公共 API、Context 模型、T01～T11 公共合同，也未实现 T13/T14/T15 或 P2～P7 runtime。
