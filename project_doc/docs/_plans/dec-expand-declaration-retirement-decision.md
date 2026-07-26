# `dec-expand-declaration` 整体退役决策

- 状态：已确认
- 适用阶段：P1—P8
- 目标事实源：`dec-demo/src/main/resources/mix`

## 1. 决策

`dec-expand-declaration` 是历史临时验证模块，不属于目标架构，也不是 System、Business、Information、Directory、Action、Produce、事务或运行时能力的依赖来源。

后续采用以下处置：

1. 整个 Maven 模块删除，不保留独立发布物；
2. 不抽取其代码作为核心 SPI，不建立 `LegacyDeclarationAdapter`；
3. 不迁移旧 declaration XML/YAML，不兼容其第二套 Context、System、Business、Service、事务和 parser；
4. 仅允许把仍有价值的业务场景重新表述为 `mix` fixture 与 `dec-demo` 端到端测试，不能复制旧模块实现；
5. `mix` 是新架构唯一业务配置契约；
6. `Business` 如继续存在，只是统一 AST/EngineContext 中的逻辑作用域和命名空间，不是独立 Maven 项目或第二套运行时；
7. Produce、输入依赖、事务、回滚、回调等能力必须根据 `mix` 和核心引擎职责重新设计，不从旧模块继承语义。

## 2. 阶段映射

| 目标能力 | 新归属 | 说明 |
|---|---|---|
| 文档前端、Raw/Compiled AST | P1 / `dec-core-compiler` | 直接覆盖 `mix`，无 declaration adapter |
| System、RuleView、模型访问 | P2 | 以 `mix/system` 为唯一输入 |
| Information、依赖和识别 | P3 | 不建立独立 Consumer runtime |
| Action、Produce、输入输出契约 | P4 | 直接进入统一 Action Runtime |
| Directory、分类、Back | P5 | 由统一 DirectoryEngine 解释执行 |
| QueryPlan、SQL | P6 | 只读取统一 Compiled 模型 |
| Session、事务、错误和资源生命周期 | P7 | 仅收敛核心运行时，不吸收旧模块代码 |
| 清理、对等、发布 | P8 | 验证仓库、Reactor、依赖和文档均无旧模块残留 |

## 3. 删除范围

P1 的退役任务至少覆盖：

- 根 `pom.xml` 的 `<module>dec-expand-declaration</module>`；
- `dependencyManagement` 或其他 POM 中对该 artifact 的引用；
- `dec-demo` 对该模块的依赖；
- `dec-expand-declaration/` 源码、资源和测试目录；
- 以旧 declaration 配置作为正式兼容输入的测试和文档；
- 任何计划中的 Adapter、迁移器或双运行时入口。

历史 P0 Evidence 可以保留原始事实，但不得被解释为后续阶段仍需保留该模块。

## 4. 禁止事项

- 禁止重新引入 `dec-expand-declaration` Maven 模块；
- 禁止建立 `LegacyDeclarationAdapter`；
- 禁止复制其 ContextStorage、SystemBuilder、ServiceDeclare、BusinessDeclare 或 parser 形成新包；
- 禁止因旧样例存在而改变 `mix` 语义；
- 禁止把“参考旧思想”转化为对旧代码的编译或运行依赖。

## 5. 验收

最终必须同时满足：

- 仓库不存在 `dec-expand-declaration/`；
- Reactor 和依赖树不存在 `doc.eq.code:dec-expand-declaration`；
- `dec-demo` 端到端测试只使用 `mix` 及统一核心运行时；
- P1—P8 计划不再包含旧模块能力抽取、Adapter 或迁移任务；
- 全仓文本扫描中，除本决策、删除报告和历史 Evidence 外，不存在把该模块描述为现役能力的内容。
