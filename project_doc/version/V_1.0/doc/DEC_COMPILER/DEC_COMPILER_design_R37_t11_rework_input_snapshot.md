# DESIGN-R37 — TASK-P1-T11 I002 完整性与批次快照返工

- Revision：`DESIGN-R37@P1-T11-REWORK-I002`
- Status：`PASSED`
- Base：`dev_all@f97b7e47ac0fb40209c4dc512aa15d67c19be44b`
- Reviewed Head：`ae35e1cc745bf096f35c20ba73dc4909286e7a3b`
- Trigger：独立 Review `NEEDS_CHANGES / REWORK`
- Invalidated Completion：`COMPLETION-P1-T11-R01@f09d9786fad8`
- Preserved History：I001 的 Design、Plan、RED、Architecture、Review、Completion、CI 与 Artifact 全部不可变保留

## 1. Finding closure scope

本 Revision 只关闭：

- `FND-P1-T11-I002-001`：`resolvedReferences(null)` 被错误归一化为显式空列表；
- `FND-P1-T11-I002-002`：批次 `inputs` 未在分类前形成防御性快照。

不得修改 T06～T10 公共合同、Compiler API、Context 模型、P2～P7 runtime、SQL、事务、I/O、网络、DAG、缓存或全局状态。

## 2. resolvedReferences 容器语义

`DeferredClassificationInput.Builder` 必须区分三种状态：

1. 未调用 `resolvedReferences(...)`：未提供，分类失败；
2. 调用 `resolvedReferences(null)`：仍视为未提供，分类失败；
3. 调用 `resolvedReferences(emptyList())`：显式完成类型化且无引用，允许分类。

Builder 先设置合法列表后再设置 null，最终状态必须回到“未提供”。输入对象可以继续向读取方暴露不可变空列表，但 `resolvedReferencesProvided()` 必须保持真实容器语义，不能因 null 归一化而变为 true。

失败合同：

- DiagnosticCode：`MIX-DEFERRED-INCOMPLETE`
- messageKey：`deferred.incomplete.resolved-references`
- status：`FAILED`
- registry：缺席

含 null 元素的非 null 列表继续使用 `deferred.incomplete.resolved-reference-null`。

## 3. 批次快照时点

`DeferredDefinitionBuilder.build(inputs)` 必须：

1. 先处理 null 批次；
2. 在创建候选 Registry 和遍历任何元素前，复制整个批次；
3. 后续只遍历快照，不再读取调用方 List；
4. snapshot 内的 null 元素继续转换为 `input-null` Diagnostic；
5. snapshot 失败或调用方 List 在复制期间产生运行时异常时，转换为稳定 `inputs-snapshot` Diagnostic，不允许异常越过结果边界；
6. 任一错误继续丢弃全部候选，不发布部分 Registry。

快照实现必须保持 Java 8，可使用局部 `ArrayList` 和不可变包装；不得引入 static/thread-local 可变状态。

## 4. 阻断 Oracle

I002 至少覆盖：

- null `resolvedReferences` 容器；
- Builder 合法列表后设置 null；
- null 容器与显式空列表结果不同；
- null 元素继续使用独立 Diagnostic；
- null 容器阻断同批次其他合法 Deferred；
- 分类开始后调用方批次变化不影响已形成的快照；
- 自定义可变 List 不得让运行时异常越过 `build()`；
- snapshot 内 null 元素稳定失败。

## 5. 架构与发布约束

- 不新增公共 API；
- Input 仍是不可变请求快照，Builder 允许表达不完整请求；
- Builder 仍使用局部候选 Map 保证原子发布；
- 分类器只构造 Deferred，不执行任何后续阶段语义；
- 所有新增或修改的方法与重要逻辑使用中文注释；
- 如出现 `@Override`，注解必须独占一行。

## 6. Completion 规则

只有在 I002 有效 RED、架构审查、GREEN、T11 定向测试、T07～T10 回归、Compiler 全量、12 模块 clean verify、故意失败门禁、最终 P0、Artifact SHA-256、Surefire XML、Revision Integrity 与独立 Review全部通过后，才能形成新的 R02 Completion。R01 只能标记失效并保留，禁止覆盖或删除。
