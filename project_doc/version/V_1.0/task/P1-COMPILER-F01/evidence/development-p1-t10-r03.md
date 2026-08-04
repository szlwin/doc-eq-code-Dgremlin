# TASK-P1-T10 Development Evidence R03

- Revision：`DEV-P1-T10-R03@bc056b7ed1da`
- Production commit：`bc056b7ed1da2cf2d47c8a3e66c24947f5cc695c`
- First GREEN Head：`33a536d5a574e738e65d041ecd21a403145a2c7e`
- Independent Review / Clean-code Head：`336d309f3748328ba4dea18be9944a95751ccc29`

## Production Change

仅修改：

`dec-core-compiler/src/main/java/dec/core/compiler/modelaccess/ModelAccessStructureValidator.java`

- `model-ref/ref@view` 使用 `hasTypedKeyReferenceLexical`：只要求非 null 且 trim 后非空，不改写 Raw lexical；
- `read/write@path` 与 `ref@property` 使用 `hasExactPathLexical`：继续要求 nonblank 且已经 trim；
- `definition.name` 与 `model-ref` 继续按原始 lexical 完全一致；
- 后续 `ViewKey` 负责 canonicalization，Binding 发布 canonical name。

## Test Change

- `ModelAccessTypedKeyLexicalReworkTest`：9 项真实 Canonical T06/T07/T10 Oracle；
- `ModelAccessI003IndependentReviewTest`：3 项 lexical 策略与无状态白盒 Oracle。

## Scope / Style

- 未修改 T06 Raw builder、T07 Symbol/TypedKey、T08 resolver、Context、Compiler API、XML/YAML Frontend；
- 未新增权限执行、SQL、I/O、网络、缓存、DAG 或运行时状态；
- 方法与重要职责使用中文注释；
- I003 新增/修改代码不包含 `@Override`，PR 既有 `@Override` 仍保持独占一行；
- 无临时 workflow 或 publish trigger。
