# DEC_COMPILER P1 架构设计

- Revision：`DESIGN-R01@a7a6820a381e`

## 1. 组件图

```text
                +-----------------------+
                | dec-core-starter      |
                | source discovery      |
                | composition           |
                +-----------+-----------+
                            |
          +-----------------+-----------------+
          |                                   |
+---------v-----------+             +---------v-----------+
| XML Frontend        |             | YAML Frontend       |
| secure parse/loc    |             | safe node/mark      |
+---------+-----------+             +---------+-----------+
          | CanonicalDocumentNode             |
          +-----------------+-----------------+
                            v
                +-----------+-----------+
                | dec-core-compiler     |
                | Raw AST / Passes      |
                | Symbol/Registry/Digest|
                +-----------+-----------+
                            | contracts
                            v
                +-----------+-----------+
                | dec-core-context      |
                | immutable Context     |
                | Diagnostic/Keys       |
                | Legacy read-only view |
                +-----------------------+
```

`dec-demo` 仅位于测试端，依赖上述模块；任何生产模块不得依赖 demo。

## 2. 允许依赖矩阵

| From \ To | context | compiler | XML | YAML | starter | demo | SQL/MySQL/runtime |
|---|---:|---:|---:|---:|---:|---:|---:|
| context | - | 否 | 否 | 否 | 否 | 否 | 否 |
| compiler | 是 | - | 否 | 否 | 否 | 否 | 否 |
| XML/YAML | 是 | 是 | - | 否 | 否 | 否 | 否 |
| starter | 是 | 是 | 可组合 | 可组合 | - | 否 | 否 |
| demo/tests | 是 | 是 | 是 | 是 | 是 | - | 测试 profile 可选 |

通过 Maven Enforcer/架构测试锁定 compiler 不依赖 parser、starter、SQL、MySQL、model runtime、demo。

## 3. 生命周期

```text
Builder phase (session-local mutable)
CREATED -> ... -> SEMANTICALLY_VALIDATED
      ERROR --------------------------> FAILED
      no ERROR -> freeze/copy/check -> PUBLISHED EngineContext
```

发布对象不暴露 Builder；旧 Context 与新 Context 可同时存在。starter 不拥有全局 current 指针。

## 4. 扩展点

- `DocumentFrontend`：格式到 Canonical；按 format/schema 支持能力匹配。
- `CompilerPass`：P1 核心 pass 固定、可单测；扩展 pass 只能在明确定义 hook 点加入且不得改变核心顺序。
- `CompilationPluginDescriptor`：只提供版本化、不可变、参与 digest 的元数据；P1 不开放任意执行插件。
- 后续 P2～P6 在 Raw/Compiled contracts 上扩展，不修改 frontend 业务规则。

## 5. 架构风险与控制

| 风险 | 控制 |
|---|---|
| 新全局单例替代旧全局单例 | 构造器注入、无 static mutable、Context isolation test |
| compiler 反向依赖 parser | Maven/Arch test 禁止包和模块依赖 |
| parser 节点泄漏 | publish graph inspection + API 类型限制 |
| HashMap 导致摘要不稳定 | 规范编码、显式排序、property tests |
| P2+ 语义提前固化 | DeferredSemanticPolicy + scope tests |
| Legacy 双写 | read-only interface + mutation rejection tests |
