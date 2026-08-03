# DEC Compiler Implementation Plan R19 — T06 RawDefinitionSet

- Plan Revision：`TP-P1-COMPILER-F01-R19@P1-T06-I001`
- Design Input：`DESIGN-R23@P1-T06-I001`
- Task：`TASK-P1-T06 / I001`
- Base：`dev_all@17ce0834b947a75ff3ccbd24c7b1332fb93e8941`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Branch：`feature/p1-t06-raw-definition-20260803-1334`

> 本 Plan 在 T06 TDD RED 前创建并冻结。后续核心执行变化必须创建新的 Plan Revision，不得原地追认实现。

## 1. 生命周期

1. 验证 PR #20 已合并并重新获取最新 `dev_all`；
2. 从精确 `dev_all` Head 创建 T06 分支；
3. 创建并冻结 R23、R19，记录 first commit 与 blob SHA；
4. 建立 T06 Task、Revision Lock 与依赖 Evidence；
5. 提交 Java 8 可编译的反射式 TDD RED Oracle；
6. 创建 Draft PR 并运行 P0，记录精确 RED、Artifact 与 digest；
7. 建立 Architecture Skeleton：枚举、不可变值对象、结果类型和未完成 Builder 接缝；
8. 运行 Skeleton P0，证明架构合同通过且行为 Oracle 仍 RED；
9. 完成严格结构白名单、14 类提取、owner/name、reference、body、ordinal 与 fail-closed 实现；
10. 执行 Specification、Architecture、Security、Code、TDD 五类独立 Review；
11. 完成全仓 Testing 与 Artifact 独立解析；
12. 生成 Completion R01、机器 checkpoint、handoff 和 resume；
13. 对最终文档化 Head运行独立 P0；
14. 复核 R23/R19 blob 未变化；
15. 更新 PR 并恢复 Ready for review，禁止自动合并。

## 2. TDD RED

新增：

```text
dec-core-compiler/src/test/java/dec/core/compiler/raw/RawDefinitionBuilderContractTest.java
```

测试通过反射加载生产 Raw API，因此在生产类尚不存在时测试源码仍能按 Java 8 编译并形成有效 RED。

冻结 Oracle：

1. `buildsAllFrozenKindsWithStableOrdinals`
   - 构造最小 Canonical 文档集合；
   - 覆盖 14 个 Kind；
   - ordinal 精确为 0..13；
   - 定义顺序稳定。
2. `preservesSourceFactsAttributesScalarAndOrderedBody`
   - SourceRef、format、schemaVersion、排序属性、scalar 与 children 保留。
3. `extractsReferencesWithoutResolvingTargets`
   - role、target、SourceRef 精确；
   - 不创建 TypedKey 或解析结果。
4. `stopsParentReferenceTraversalAtNestedDefinitions`
   - System 不重复拥有 Information/ModelAccess 引用；
   - Directory 不重复拥有 Action/Produce 引用。
5. `repeatedBuildIsDeterministicAndStateless`
   - 同一 builder 重复调用相等；
   - 新 builder 调用相等。
6. `rejectsUnknownDocumentRootWithoutPartialSet`
7. `rejectsUnknownChildWithoutPartialSet`
8. `rejectsMissingNameOrOwnerWithoutPartialSet`
9. `rejectsNullInputAndNullDocumentWithoutPartialSet`
10. `publishedCollectionsAreImmutable`
11. `doesNotExposeParserTypesOrMutableRegistrationApi`

负向结果固定断言：

- `FAILED`；
- rawDefinitionSet empty；
- Diagnostic code `MIX_STRUCTURE_UNKNOWN`；
- ERROR；
- SourceRef 稳定；
- 不发布部分定义。

## 3. Architecture Skeleton

新增生产类型及完整不可变构造合同：

- `RawDefinitionKind`
- `RawReference`
- `RawNodeBody`
- `RawDefinition`
- `RawDefinitionSet`
- `RawBuildStatus`
- `RawBuildResult`
- `RawDefinitionBuilder`

Skeleton 阶段：

- 值对象完整可用；
- 集合 defensive copy 并不可变；
- Builder 公开签名冻结；
- Builder 暂时返回受控 `raw.builder.not-implemented` FAILED；
- 不加入结构白名单或语义提取的伪实现；
- 架构 Oracle 转绿，行为 Oracle 继续 RED。

## 4. Development

### 4.1 两阶段构建

Builder 必须分两阶段：

1. `validateDocuments`：验证输入、root、完整父子白名单和定义必填事实；
2. `extractDefinitions`：仅在全量验证成功后生成 RawDefinition 并冻结集合。

不得边验证边发布可见集合。

### 4.2 Grammar

以 R23 第 7 节为唯一结构白名单。实现使用不可变 `Map<String, Set<String>>` 或等价静态规则；view property 的递归是唯一递归同名特例。

未知 child 使用其自身 SourceRef 报 `raw.structure.unknown`。未知 root 使用 root SourceRef 报 `raw.document.root.unsupported`。

### 4.3 定义发现

采用深度优先先序遍历。进入一个语义定义节点时立即分配 ordinal，再继续发现其嵌套语义定义。

文档 root 产生定义时先于子定义：

- `orm-config` → ROOT_CONFIG；
- `business-config` → BUSINESS_SCOPE。

### 4.4 Lexical Context

遍历携带不可变 lexical context：

```text
rootConfigName
systemName
ruleViewName
a businessScopeName
directoryName
actionName
```

owner/name 按 R23 表格解析。缺必填属性在验证阶段失败。

### 4.5 RawNodeBody

递归复制 Canonical：

- name；
- attributes；
- scalar；
- children；
- SourceRef。

format/schemaVersion 保存在 RawDefinition，不重复放入每个 body child。

### 4.6 Reference

从定义自身节点开始遍历：

- 提取 R23 指定引用属性；
- role 为相对路径加 `@attribute`；
- 进入嵌套语义定义前停止；
- 仍保留嵌套定义节点于父 body；
- reference target 只校验非空，不解析。

### 4.7 结果与 Diagnostic

实现内部 `RawBuildResults` 或等价静态工厂，保证：

- BUILT 与 FAILED 状态互斥；
- BUILT 必须有 set 且无 ERROR；
- FAILED 必须无 set 且至少一个 Diagnostic；
- diagnostics defensive copy、稳定排序和不可变。

Diagnostic：

- code：`MIX_STRUCTURE_UNKNOWN`；
- severity：`ERROR`；
- pass：`raw-definition-builder`；
- recoveryHint：中文说明修复 Canonical 结构或必填 lexical fact。

## 5. Review

### Specification Review

- R23/R19 与 master Design R05、BM R05、TR-002 一致；
- 没有进入 TypedKey/Symbol 范围；
- 14 Kind、owner/name、grammar、reference 与 ordinal 一致。

### Architecture Review

- raw 包只依赖 canonical 和 context model；
- 无 parser 类型；
- 无全局/static 可变 registry；
- 无 public mutator；
- Context、Frontend 与 Source Graph 无生产修改；
- T07 未启动。

### Security Review

- unknown fail closed；
- null/blank fail closed；
- 不返回部分集合；
- 集合不可变；
- 不执行表达式、脚本、反射构造或对象加载；
- reference 不触发 I/O。

### Code Review

- Java 8；
- `@Override` 独行；
- 公共方法、构造器和重要逻辑中文注释；
- equals/hashCode/toString 完整；
- 无 name overwrite；
- 无 HashMap 枚举顺序依赖。

### TDD Review

- RED 是行为失败而非编译失败；
- 正负控制完整；
- SourceRef 和 no-partial-root 可证明；
- 无时间阈值、OOM 或环境脆弱测试。

## 6. Testing

必须运行：

- T06 专项；
- Compiler 全部测试；
- XML T04；
- YAML T05；
- Context；
- Demo；
- legacy declaration gate；
- 12 模块 Reactor；
- Java release 8；
- 故意失败阻断。

MySQL 仅按 workflow 实际状态记录，不得将 `SKIPPED_NOT_APPLICABLE` 表述为通过。

Artifact 必须：

- 与 clean-code Head 精确绑定；
- 独立下载并校验 ZIP SHA-256；
- 解析 Surefire XML 汇总真实 tests/failures/errors/skipped。

## 7. Revision Integrity

记录：

- R23 first commit；
- R23 blob；
- R19 first commit；
- R19 blob。

在 RED、clean-code 和最终文档化 Head 重新读取。任一 blob 变化则 Completion 失败，并创建新 Revision。

## 8. Completion

创建：

- `COMPLETION-P1-T06-R01@<clean-code-head>`；
- `project_doc/version/V_1.0/tdd_p1_t06_r01_completion.json`；
- T06 task、review、evidence、handoff、resume 更新。

只有以下条件同时成立才 PASSED：

- 14 Kind 与 grammar 全部验收；
- 所有 Review PASSED；
- 全量 P0 通过；
- Artifact 已独立验证；
- R23/R19 未变化；
- 开放 P0/P1/P2 为 0；
- PR Open、Ready for review、未合并；
- T07 未启动。
