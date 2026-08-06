# DEC_COMPILER DESIGN R51 — TASK-P1-T15 / I001

- Design ID：`DESIGN-R51@P1-T15-I001`
- Status：`FROZEN`
- Base：`dev_all@665dd364975505bb01263885a25b3bb1be767d2b`
- Dependency：`COMPLETION-P1-T14-R03@37fb814b39c5`

## 1. 目标

完成 P1 最终兼容与退役边界：

1. 旧核心只能通过同一个 `EngineContext` 派生的 `CoreConfigProjection` 读取 Data/View/Rule 事实；
2. Starter 不再写入全局 `ConfigManager`、`ConfigContextUtil` 或旧 XML/YAML Parser；
3. `dec-expand-declaration` 从仓库、默认 Reactor、dependencyManagement、依赖图、ServiceLoader、反射字符串和发布 Artifact 中整体退役；
4. 建立 CI 残留扫描，任何回流都阻断 P1；
5. 不建立 Adapter、复制实现或运行时双轨。

## 2. 已存在且必须保持的事实

- `EngineContext` 由唯一 `CompiledModelSet` 构造；
- `EngineContext.projection()` 返回从同一个模型确定性派生的 `CoreConfigProjection`；
- Projection 的 Data/View/Rule 列表不可变；
- deprecated 写入口和所有 List/Iterator 写方法稳定抛 `ProjectionWriteRejectedException`；
- Projection 不持有静态可变状态，也不创建独立 Registry。

T15 不复制或重写该实现，只补充 Starter 入口和最终验收。

## 3. Starter 新边界

新增实例级 `CompilerStarter`：

- 构造器只接收 `ModelCompiler`；
- `compileAndPublish(CompilationRequest, PublicationRequest)` 只委托同一个 Compiler；
- 返回原始 `CompilationResult`，不改写状态、Diagnostic 或发布事实；
- 成功结果的旧核心投影必须从 `PublishedCompilationResult.engineContext().projection()` 取得；
- 不保存 static current Context，不访问 `ConfigManager`、旧 Parser、ServiceLoader 或反射；
- 不拥有额外 Publisher/CAS 能力。

删除 Starter 中修改全局 Config 的 `ConfigUtil` 和 `DataSourceManager`；删除 XML/YAML 旧 Parser 依赖，新增 `dec-core-compiler` 依赖。

## 4. 临时模块整体退役

必须从 Git 树删除整个 `dec-expand-declaration/`，并从根 POM 删除：

- `<modules>` 中的 module；
- dependencyManagement 中的 artifact；
- 所有直接或传递依赖声明。

不得把旧模块类复制到其他模块，不得新增 `LegacyDeclarationAdapter` 或同义包装。

## 5. 残留扫描

新增 `scripts/remediation/prove_p1_t15_retirement_gate.sh`，扫描：

- 根 POM 和所有模块 POM；
- `src/main`、`src/test` 与 `src/*/resources/META-INF/services`；
- Java/Kotlin/Groovy/Scala import、FQCN、`Class.forName`、反射字符串；
- 编译后的 class、jar、war、zip 和 Maven dependency tree；
- 禁止名称：`dec-expand-declaration`、`doc.eq.code:dec-expand-declaration`、`dec.expand.declare`、`LegacyDeclarationAdapter`。

`project_doc` 中的历史与本 Design/Plan 可以提及旧名称，但不能进入生产、测试、POM、服务发现或发布 Artifact。

## 6. TDD

有效 RED 必须满足：

- Maven testCompile 成功；
- JUnit/扫描脚本实际执行；
- 失败来自旧 module、旧 POM、旧 Starter API 或缺少 `CompilerStarter`；
- 不接受编译错误、ClassNotFound 直接错误或环境失败。

RED Oracle：

1. 反射检查 `CompilerStarter` 缺失时产生 JUnit assertion failure；
2. Starter POM 仍依赖 XML/YAML Parser；
3. `ConfigUtil`/`DataSourceManager` 仍暴露全局写入口；
4. retirement gate 在正常 Reactor 构建后发现旧 module/POM/artifact 残留。

## 7. 失败与安全边界

- CompilationResult 为 FAILED 时不得暴露 Projection；
- Starter 不得缓存 EngineContext 或跨请求共享可变状态；
- 删除旧模块后默认 Reactor 必须全绿；
- intentional failure gate、T14 mutation gate、Java 8 和 MySQL workflow 语义保持；
- 不实现 P2～P7 runtime。

## 8. 编码规范

- 所有新增 `@Override` 独占一行；
- 类、构造器、委托、投影访问、扫描与异常逻辑使用中文注释；
- Java release 8；
- 无新依赖、反射生产逻辑、sleep、wall-clock 或全局 mutable current。
