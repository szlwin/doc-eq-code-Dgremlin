# DEC Compiler Design R23 — T06 RawDefinitionSet

- Design Revision：`DESIGN-R23@P1-T06-I001`
- Task：`TASK-P1-T06 / I001`
- Base：`dev_all@17ce0834b947a75ff3ccbd24c7b1332fb93e8941`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Master Design：`DESIGN-R05@0b37a9b4dd48`
- Traceability：`TR-P1-COMPILER-002`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

> 本 Design 在 T06 TDD RED 前创建并冻结。提交后不得原地修改核心合同；任何核心语义变化必须创建新的 Design Revision。

## 1. 目标与范围

T06 在已经完成的 Source Graph、XML Frontend 和 YAML Frontend之上，实现从 `CanonicalDocumentNode` 到格式中立 `RawDefinitionSet` 的严格、确定性、fail-closed 转换。

本任务只完成：

1. Canonical 全树结构白名单验证；
2. 14 类 Raw 定义提取；
3. 稳定 `sourceOrdinal`；
4. SourceRef、owner token、name、属性、原始引用与 normalized body保留；
5. 成功结果冻结及失败结果不发布部分集合。

本任务明确不实现：

- TypedKey；
- SymbolTable；
- 重复 TypedKey 判定；
- 引用解析或 owner 语义验证；
- Deferred 分类；
- Compiler Pass / Pipeline 编排；
- Digest、CompiledModelSet 或发布；
- `TASK-P1-T07` 及后续任务。

## 2. 包与类型

新增 `dec.core.compiler.raw`：

```text
RawDefinitionKind
RawReference
RawNodeBody
RawDefinition
RawDefinitionSet
RawBuildStatus
RawBuildResult
RawDefinitionBuilder
```

所有类型必须：

- Java 8 可编译；
- 不依赖 XML DOM、SAX、SnakeYAML Node 或其它 parser 类型；
- 对外集合不可变；
- 构造参数拒绝 null，必填文本拒绝空白；
- 重要构造、转换和失败逻辑使用中文注释；
- `@Override` 独占一行；
- 值对象基于全部语义字段实现 `equals/hashCode/toString`。

## 3. RawDefinitionKind

冻结 14 类定义：

1. `ROOT_CONFIG`
2. `DATA_SOURCE`
3. `CONNECTION`
4. `DATA`
5. `VIEW`
6. `SYSTEM`
7. `RULE_VIEW`
8. `RULE`
9. `BUSINESS_SCOPE`
10. `INFORMATION`
11. `MODEL_ACCESS`
12. `DIRECTORY`
13. `ACTION`
14. `PRODUCE`

结构容器、property、column、read/write/ref、dependency、change、subdirectory 和 back 等节点只进入 normalized body 或 raw reference，不单独成为定义。

## 4. RawDefinition

每个 `RawDefinition` 保存：

```text
kind: RawDefinitionKind
sourceOrdinal: long
sourceRef: SourceRef
ownerToken: Optional<String>
name: Optional<String>
attributes: Map<String,String>
references: List<RawReference>
body: RawNodeBody
format: DocumentFormat
schemaVersion: String
```

约束：

- `sourceOrdinal` 从 0 开始连续递增；
- ordinal 顺序为输入文档顺序，再按每个文档 Canonical 子树的先序定义顺序；
- 属性按 key 稳定排序；
- reference 保持 Canonical 文档顺序；
- body 递归保留节点名称、稳定属性、可选 scalar、SourceRef 和有序 children；
- body 不包含 parser 对象；
- 不在 T06 解析引用、不生成 TypedKey、不因同名定义覆盖或丢弃记录。

## 5. owner 与 name 冻结规则

| Kind | name | ownerToken |
|---|---|---|
| ROOT_CONFIG | `name` 必填 | empty |
| DATA_SOURCE | `name` 必填 | RootConfig name |
| CONNECTION | `name` 必填 | RootConfig name |
| DATA | `name` 必填 | empty |
| VIEW | `name` 必填 | empty |
| SYSTEM | `name` 必填 | empty |
| INFORMATION | `name` 必填 | System name |
| MODEL_ACCESS | `model-ref` 作为 name，必填 | System name |
| RULE_VIEW | `name` 必填 | `system` 必填 |
| RULE | `name` 必填 | `system/ruleViewName` |
| BUSINESS_SCOPE | `name` 必填 | empty |
| DIRECTORY | `name` 必填 | BusinessScope name |
| ACTION | `name` 必填 | Directory name |
| PRODUCE | `ref` 作为可选 name | `directory/action` |

这些值只是未解析 lexical token。T06 不验证它们是否命中其它定义。

## 6. RawReference

`RawReference` 保存：

```text
role: String
target: String
sourceRef: SourceRef
```

从定义自身 lexical scope 中提取以下属性：

- `ref`
- `data-source`
- `ref-property`
- `rel-value`
- `data`
- `view-ref`
- `rule-ref`
- `model-ref`
- `system-ref`
- `information-ref`
- `property`
- `view`
- `rel`
- 任意以 `-ref` 结尾的属性

`role` 使用 `相对节点路径@属性名`，例如：

```text
/data-source-info/data-source@ref
@view-ref
/produce-info/produce@information-ref
```

提取遍历在遇到嵌套语义定义时停止，防止父定义重复拥有子定义的引用。定义自身节点属性始终属于自身。

`path`、`key`、`rel-key`、`type` 等普通 lexical 属性只保留在 body，不被提前解释为 Typed reference。

## 7. 严格结构白名单

### 7.1 orm-config

```text
orm-config
  orm-datasource-info
    orm-datasource
      name
  orm-data-file-info
    orm-file
  orm-view-file-info
    orm-file
  system-file-info
    system-file
  business-file-info
    business-file
  orm-connection-info
    orm-connection
      data-source-info
        data-source
```

`orm-config`、`orm-datasource`、`orm-connection` 产生定义。

### 7.2 orm-data-mapping

```text
orm-data-mapping
  data
    property-info
      property
    table-info
      table
        column
```

`data` 产生定义。

### 7.3 orm-view-mapping

```text
orm-view-mapping
  view
    property-info
      property
        property (recursive)
```

`view` 产生定义。

### 7.4 systems

```text
systems
  system
    data-info -> data-ref
    view-info -> view-ref
    rule-file-info -> rule-file
    information-info -> information -> change-data
    model-access-info -> model-access -> read|write -> ref
```

`system`、`information`、`model-access` 产生定义。

### 7.5 orm-rule-mapping

```text
orm-rule-mapping
  rule-view-info
    rule
      customer-process
```

`rule-view-info`、`rule` 产生定义。

### 7.6 business-config

```text
business-config
  directory-info
    directory
      subdirectory-info -> subdirectory -> back -> action-info
      dependency-info -> dependency
      action-info -> action -> produce-info -> produce
      change-info
```

`business-config`、`directory`、任意 `action-info` 下的 `action`、任意 `produce-info` 下的 `produce` 产生定义。

空容器允许存在。任何未在对应父节点白名单中的 child 均失败，不提供 lenient 模式。

## 8. 构建结果与失败边界

`RawDefinitionBuilder.build(List<CanonicalDocumentNode>)` 返回 `RawBuildResult`：

成功：

- status=`BUILT`；
- `rawDefinitionSet` present；
- diagnostics empty。

失败：

- status=`FAILED`；
- `rawDefinitionSet` empty；
- 至少一个 ERROR Diagnostic；
- code=`MIX_STRUCTURE_UNKNOWN`；
- SourceRef 指向第一个确定失败节点；
- 不返回已构建的部分定义。

稳定 messageKey：

- `raw.input.required`
- `raw.document.required`
- `raw.document.root.unsupported`
- `raw.structure.unknown`
- `raw.definition.name.required`
- `raw.definition.owner.required`
- `raw.definition.attribute.required`

Builder 必须先完成整批结构和必填事实验证，再冻结并发布 `RawDefinitionSet`。任何 runtime failure 也转为受控 `raw.build.failed`，不泄露部分集合。

## 9. RawDefinitionSet

`RawDefinitionSet`：

- definitions 按 `sourceOrdinal` 升序且不可变；
- 支持读取全部定义；
- 支持按 `RawDefinitionKind` 读取不可变子列表；
- 拒绝负 ordinal、重复 ordinal、null definition；
- 不按 name 覆盖；
- 不暴露 add/register/remove 等 public mutator。

## 10. 确定性与格式一致性

对于语义等价且 SourceRef、format、schemaVersion 相同的 XML/YAML Canonical 树：

- RawDefinitionSet 必须相等；
- 定义顺序、attributes、references 和 body 必须相等。

`DocumentFormat` 仍保存为来源事实，因此 XML/YAML format 不同时完整 RawDefinition 可不同；除 format 外的语义视图必须一致。

同一输入重复构建结果必须相等，且 builder 不持有跨调用可变状态。

## 11. 保持的不变量

- 不修改 Canonical 公共 API；
- 不修改 Source Graph、XML Frontend、YAML Frontend 生产语义；
- 不修改 `dec-core-context` 生产代码；
- 不引入 parser 依赖；
- 不恢复 `dec-expand-declaration`；
- T05 历史及 Evidence 不覆盖、不删除；
- T07 不启动。

## 12. 验收门禁

- Design/Plan 在 RED 前冻结并记录 blob SHA；
- RED 可按 Java 8 编译并精确失败；
- Architecture Skeleton 只建立不可变值对象、结果边界和构建接缝；
- Development 完成 14 类提取、严格白名单、引用提取和确定性；
- 未知 root、未知 child、缺 name/owner、null 输入均 FAILED 且无部分集合；
- 返回集合不可修改；
- 重复构建确定；
- Specification、Architecture、Security、Code、TDD 五类独立 Review 全部 PASSED；
- 全仓 P0、Artifact、Java release 8、故意失败门禁通过；
- 开放 P0/P1/P2 为 0；
- PR 创建后恢复 Ready for review，但未经明确授权不得合并。
