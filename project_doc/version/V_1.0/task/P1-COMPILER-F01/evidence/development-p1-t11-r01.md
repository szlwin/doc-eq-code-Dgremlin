# TASK-P1-T11 R01 Development Evidence

- Development：`DEV-P1-T11-R01@f09d9786fad8`
- Code Review：`CODEREVIEW-P1-T11-R01@f09d9786fad8`
- Evidence：`EVD-000749`～`EVD-000754`

## Implementation

- 八种 DeferredKind 固定映射至 P2-P7 与稳定 reasonCode；
- 分类输入防御性复制 typed/unresolved references；
- 缺 owner、kind、ordinal、reason、SourceRef、body、typed reference container 分别诊断；
- reason-policy、null typed ref、unresolved lexical、null input 与 duplicate key 全部 fail-closed；
- 使用 compilation-local `TreeMap` 聚合，任一 ERROR 不发布部分 Registry；
- 空批次发布不可变空 Registry；输入乱序不改变输出；
- 4096 个唯一输入完整分类，无部分丢失。

## Scope / coding

生产变更仅位于 `dec.core.compiler.deferred`。新增代码没有 `@Override`；PR 中已有 `@Override` 继续独占一行。所有公开方法、构造器及重要分类、复制、Diagnostic、重复检测和原子发布逻辑均使用中文注释。未实现权限、求值、执行、Directory、Query、SQL、Transaction、DAG、缓存、I/O 或网络。
