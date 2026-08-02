# TASK-P1-T04 / I001

- 标题：实现安全 XML Canonical Frontend
- 状态：`COMPLETED`
- Base：`dev_all@df5e8c057d9aa8e3e477c54325bc476e7fdc5bee`
- Branch：`feature/p1-t04-xml-canonical-20260802-1744`
- Dependency：`COMPLETION-P1-T03-R05@91271c9a1c20`
- Design：`DESIGN-R18@P1-T04-I001`
- Plan：`TP-P1-COMPILER-F01-R14@P1-T04-I001`
- TDD：`TDD-P1-T04-R01@1b39f27b972e`
- Architecture Skeleton：`DEVSKEL-P1-T04-R01@70df083e1b8a`
- Development：`DEV-P1-T04-R01@ba472906c719`
- Code Review：`CODEREVIEW-P1-T04-R01@ba472906c719`
- Testing：`TESTING-P1-T04-R01@ba472906c719`
- Completion：`COMPLETION-P1-T04-R01@ba472906c719`
- Review：`REV-000196`～`REV-000206`
- Evidence：`EVD-000439`～`EVD-000450`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 完成事实

1. `SecureXmlDocumentFrontend` 通过 compiler-owned `DocumentFrontend` API 发布 XML Canonical；
2. 元素和属性使用 local-name，属性稳定排序，子节点保持文档顺序；
3. SourceRef 指向每个 start tag 的 `<`，LF、CRLF、CR 均已验证；
4. DOCTYPE、通用实体、参数实体、外部 schema、网络和文件访问均被拒绝；
5. 外部访问探针为 0，XInclude 仅作为普通数据保留；
6. FAILED 不暴露部分 Canonical；
7. Frontend 不持有 ConfigFactory、ConfigInfo、Registry、EngineContext、DOM 或 DOM4J 类型；
8. compiler 未反向依赖 XML 模块；
9. Context 26/26、Compiler 83/83、XML T04 15/15 通过；
10. 12 模块 Reactor、Java release 8 和故意失败门禁通过；
11. MySQL 为 `SKIPPED_NOT_APPLICABLE`；
12. 开放 P0/P1 为 0；
13. `@Override` 独占一行，公共方法、构造器和关键逻辑使用中文注释；
14. T05 及后续任务未启动；
15. PR #19 未经明确授权不得合并。
