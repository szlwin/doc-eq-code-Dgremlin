# DEC_COMPILER 架构设计

> Revision：DESIGN-R02-DRAFT。基于实际 `mix` 源图。

## 1. 总体架构

```text
DocumentSourceProvider
        |
        v
MixSourceResolver ----> MixSourceGraph
        |
        v
XML/YAML Frontend ----> CanonicalDocumentSet
        |
        v
RawDefinitionBuilder -> RawDefinitionSet
        |
        v
Compiler Pipeline
  structural validation
  symbol registration
  reference resolution
  graph preparation
  semantic validation/deferred classification
        |
        v
CompiledModelSet ----> EngineContext
                         |
                         +--> CoreConfigProjection (read-only, deprecated)
```

## 2. Maven 边界

```text
dec-core-context   <- dec-core-compiler
       ^                    ^
       |                    |
xml/yaml frontend ----------+
       ^
       |
dec-core-starter
       ^
       |
dec-demo (fixture/tests only)
```

- `dec-core-compiler` 不依赖 frontend 实现、SQL、MySQL、demo；
- frontend 依赖 compiler SPI/context model，不依赖 starter/runtime；
- `dec-demo` 只能在测试/示例方向依赖核心；
- `dec-expand-declaration` 从图中删除。

## 3. 源图架构

`MixSourceResolver` 接受一个根 `DocumentSourceRef`，由可注入 Provider 解析 classpath/file/memory。它构建有类型边，并对 sourceId 去重、标准化和稳定排序。

发现阶段允许先解析 root/system 以获得后续文件引用；发现完成后才进入统一 Raw build 和符号处理，避免把发现顺序误当作语义顺序。

## 4. P1 与后续阶段边界

| Registry | P1 | 后续阶段 |
|---|---|---|
| Data/View | 完整结构与引用 | Query/runtime 使用 |
| System | 结构和引用 | P2 权限与执行归属 |
| RuleView | System 作用域和结构 | P2/P4 执行 |
| Information | 结构、直接引用、deferred body | P3 求值 |
| Directory | 结构、关系、deferred body | P5 状态机 |
| Action/Produce | 类型引用、deferred body | P4 执行 |

## 5. 安全边界

- XML frontend 禁用 DTD/外部实体；
- YAML frontend 使用安全 Node 模式；
- SourceResolver 只接受白名单 scheme；
- 标准化路径不得逃逸配置根；
- 文档不能指定任意 Java 类实例化。

## 6. 架构测试

- compiler core 无 parser/demo/SQL 依赖；
- context 无 compiler/frontend 反向依赖；
- 无静态可变 Registry 或 current Context；
- 无 `dec-expand-declaration`、LegacyDeclarationAdapter、BusinessEngine 第二实现；
- demo fixture 路径不出现在生产常量中。
