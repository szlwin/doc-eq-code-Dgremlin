# DESIGN-R35 — TASK-P1-T10 I003 Raw lexical 与 TypedKey 规范化返工

- Revision：`DESIGN-R35@P1-T10-REWORK-I003`
- Status：`PASSED`
- Supersedes：R34 的有效 T10 结构范围，不覆盖 R34/R33 历史
- Base：PR #25 `7e466e7cf0f28aa4062294923c27b5f59cbd355d`
- Review Input：`NEEDS_CHANGES / REWORK`，Open P0/P1/P2=`0/1/1`
- Invalidated Completion：`COMPLETION-P1-T10-R02@6f4c7b6f3ec3`
- Owner：`dec-core-compiler / dec.core.compiler.modelaccess`

## 1. 设计目标

修复 I002 结构门禁对 T06/T07/T08 已冻结合同的错误收紧：Raw 层保留 reference 原始 lexical，TypedKey 层负责安全 canonicalization；精确路径和 selector 仍在 T10 结构/值对象边界执行严格 lexical 语法。

## 2. Lexical 策略分离

### 2.1 TypedKey reference lexical

适用于：

- `model-access@model-ref`
- `ref@view`

结构门禁只验证：

- lexical 非 `null`；
- `lexical.trim()` 非空。

结构门禁不得要求 `lexical.equals(lexical.trim())`，不得改写 RawDefinition、RawNodeBody 或 attributes。后续必须通过 `new ViewKey(rawLexical)` 取得 canonical key。

### 2.2 精确 path / selector lexical

适用于：

- `read/write@path`
- `ref@property`

继续验证：

- lexical 非 `null`；
- trim 后非空；
- lexical 已经 trim；
- 无空 segment；
- `path` 仅允许完整 `*`，禁止嵌入式 wildcard；
- selector 继续由 `SystemViewSelector` 冻结精确语法。

## 3. Raw 与 Canonical 身份

- `definition.name` 与 `attributes[model-ref]` 必须使用原始 lexical 完全相等，禁止先 trim 后比较。
- Raw `model-ref`、definition name、`ref@view` 必须保持输入原值。
- `sourceViewKey` 与 `targetViewKey` 只发布 canonical `ViewKey`。
- Binding 中 `sourceModel/targetView` 必须为 trim 后 canonical name。
- padded System `view-ref@ref`、padded System declaration 与 padded ModelAccess reference 通过同一 `ViewKey` 对齐。

## 4. 失败边界

- blank `model-ref/ref@view`：结构失败，resolver 调用数为 0。
- padded `path/ref@property`：结构失败，resolver 调用数为 0。
- 原始 `definition.name` 与 `model-ref` 不一致：结构失败。
- 任一 ERROR 不发布部分 Binding 或 Deferred。

## 5. TDD Oracle

必须通过真实 Canonical pipeline 覆盖：

1. padded `model-ref` 成功；
2. padded `ref@view` 成功；
3. padded System `view-ref@ref` 与 unpadded ModelAccess ref 成功；
4. padded System declaration 与 padded ModelAccess ref 成功；
5. Raw `model-ref`、name、ref lexical 保持原值；
6. Binding `sourceModel/targetView` 为 canonical `ViewKey`；
7. padded `ref@property` 失败；
8. padded `path` 失败；
9. blank `model-ref/ref@view` 失败；
10. I001/I002 全部 wildcard、multi-section、structure、trie 与资源 Oracle 不回退。

## 6. 范围

只允许修改 T10 `ModelAccessStructureValidator` 及 I003 测试/证据；不得修改 T06 Raw builder、T07 SymbolTable/TypedKey、T08 resolver 公共合同、Compiler API、Context、XML/YAML Frontend、权限、SQL、I/O、网络、缓存、DAG 或运行时状态。
