# P0 阶段交接

## 基线
- 起始提交：`f9424aebabcf8b2350477931f71d98376ef81cd8`
- 构建命令：`./mvnw clean verify`
- MySQL IT：`./mvnw -Pmysql-it verify`

## 已稳定内容
- 根 Reactor 包含 `dec-demo`。
- Maven/JDK/插件版本由父 POM统一。
- 测试失败不可忽略。
- 默认测试不需要 MySQL 或 Docker。
- 旧资源摘要和 mix 文件完整性已形成回归门禁。

## 禁止下一阶段假设
- 不得把 P0 的资源结构快照视为统一 AST。
- 不得认为 `mix` 已被现有 Parser 或 Runtime 支持。
- 不得在 P1 前实现 Information、Directory 新状态机或 RuleView 复合 Key。

## P1 启动条件
- GitHub Actions `core-verify` 成功。
- Wrapper 可重复执行。
- 失败测试确实使 CI 返回非零。
