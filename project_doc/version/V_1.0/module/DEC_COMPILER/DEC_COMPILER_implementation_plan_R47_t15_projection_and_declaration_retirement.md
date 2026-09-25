# DEC_COMPILER IMPLEMENTATION PLAN R47 — TASK-P1-T15 / I001

- Plan ID：`TP-P1-COMPILER-F01-R47@P1-T15-I001`
- Design：`DESIGN-R51@P1-T15-I001`
- Status：`FROZEN`

## Sequential workflow

1. 锁定 `dev_all@665dd364975505bb01263885a25b3bb1be767d2b` 与 T14 Completion；
2. 创建 T15 分支和 Draft PR；
3. 冻结 R51/R47 与任务文件，必须早于测试和生产修改；
4. 新增可编译的 Starter API/retirement RED Oracle；
5. 在 P0 中运行 retirement gate，要求正常编译测试后因真实残留失败；
6. 独立读取日志和 Artifact，确认失败不是 testCompile 或环境错误；
7. 冻结 `DEVSKEL-P1-T15-R01`；
8. 新增实例级 `CompilerStarter`，删除 `ConfigUtil`/`DataSourceManager`；
9. Starter POM 删除 XML/YAML Parser 旧依赖并依赖 Compiler 公共 API；
10. 根 POM 删除 `dec-expand-declaration` module 与 dependencyManagement；
11. 从 Git 树整体删除 `dec-expand-declaration/`；
12. 完成仓库、依赖树、ServiceLoader、反射字符串和发布 Artifact 残留扫描；
13. 执行定向 Starter/Context、Compiler、全 Reactor、Java 8、intentional failure 和 T14 mutation 回归；
14. 独立 Review Projection 单一来源、Starter 无全局状态、扫描白名单和 artifact 闭包；
15. 如发现问题，先增加 Review RED 再修复；
16. 冻结 Code/Test Revision；
17. 独立下载 Artifact，核对 ZIP SHA-256、Surefire XML、retirement report 与测试统计；
18. 登记 Architecture、TDD、Development、Code Review、Testing、Revision Lock、Completion、handoff、resume；
19. Code/Test Revision 后仅允许 `project_doc` 更新；
20. 对 final documented Head 重跑 P0 与 Artifact 独立解析；
21. 更新 PR，退出 Draft，提交 Completion Review；
22. 不执行合并。

## TDD matrix

### Starter

- `CompilerStarter` 必须存在、final、无静态可变状态；
- 构造器只接收 `ModelCompiler`；
- 请求和 PublicationRequest 原样委托一次；
- 返回同一个 CompilationResult 实例；
- 成功 Projection 只能来自 Published EngineContext；
- 失败结果不得产生 Projection；
- 不引用 ConfigManager、ConfigContextUtil、旧 Parser 或反射。

### Projection

- sourceModelSet 与 EngineContext.compiledModelSet 为同一实例；
- Data/View/Rule 顺序与 Typed Registry 一致；
- 所有写入口和 List/Iterator 变更均稳定拒绝；
- 两个 Context/Projection 不共享可变事实。

### Retirement

- module 目录、根 POM module、dependencyManagement 均不存在；
- 所有 POM dependency tree 无旧 artifact；
- src/main、src/test、META-INF/services、反射字符串无旧 package；
- target jar/war/zip/class 无旧 artifact/package；
- 不存在 LegacyDeclarationAdapter、复制实现或双轨启动入口。

## Stop conditions

- RED 为 testCompile/环境失败；
- 删除旧模块后复制实现到其他模块；
- Starter 保存 static current Context；
- Projection 成为第二 Registry；
- 默认 Reactor、Java 8、intentional failure 或 T14 mutation gate 回归；
- Open P0/P1/P2 未清零；
- PR 被合并。
