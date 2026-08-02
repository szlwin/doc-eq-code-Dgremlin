# TASK-P1-T04 / I001

- 标题：实现安全 XML Canonical Frontend
- 状态：`IN_PROGRESS`
- Base：`dev_all@df5e8c057d9aa8e3e477c54325bc476e7fdc5bee`
- Branch：`feature/p1-t04-xml-canonical-20260802-1744`
- Dependency：`COMPLETION-P1-T03-R05@91271c9a1c20`
- Design：`DESIGN-R18@P1-T04-I001`
- Plan：`TP-P1-COMPILER-F01-R14@P1-T04-I001`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 验收门禁

1. 安全 XML 生成稳定 Canonical root；
2. 属性按 key 稳定，子节点保持文档顺序；
3. SourceRef 指向每个 start tag 的 `<`，nodePath 为完整 local-name 路径；
4. DOCTYPE、实体、外部 schema、网络和文件访问失败；
5. 不安全输入的访问探针为 0；
6. FAILED 不暴露部分 Canonical；
7. XML Frontend 不调用 ConfigFactory、ConfigInfo、Registry 或 EngineContext；
8. compiler 不反向依赖 XML 模块；
9. Java 8、现有 Context/Compiler/XML 回归、完整 Reactor 和失败门禁通过；
10. `@Override` 独占一行，方法和关键逻辑使用中文注释；
11. 开放 P0/P1 为 0；
12. T05 及后续任务保持未启动；
13. PR 未经明确授权不得合并。
