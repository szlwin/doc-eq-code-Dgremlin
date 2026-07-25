# P0 阶段交接

## 基线

- 起始提交：`f9424aebabcf8b2350477931f71d98376ef81cd8`
- 正式本地退出命令：`./scripts/remediation/run_p0_local_verification.sh`
- 核心诊断命令：`./scripts/remediation/run_p0_dynamic_verification.sh`
- MySQL 诊断命令：`./scripts/remediation/run_p0_local_mysql_verification.sh`
- GitHub Actions：非阻断辅助回归

## 已稳定内容

- 根 Reactor 包含 `dec-demo`。
- Maven/JDK/插件版本由父 POM统一。
- 测试失败不可忽略。
- 默认测试不需要 MySQL 或 Docker。
- 旧资源摘要和 mix 文件完整性已形成回归门禁。
- 本地核心动态验证已取得成功证据：`evidence/dynamic-20260725T081205Z/`。

## 正式本地验证约束

- 使用专用测试数据库，不得连接生产数据库；
- `DEC_MYSQL_URL`、`DEC_MYSQL_USER`、`DEC_MYSQL_PASSWORD` 必须显式提供；
- 正式运行前 Git 工作树必须干净；
- 核心和 MySQL Evidence 必须由统一入口生成并绑定同一 commit；
- 密码不得写入日志或文档。

## 禁止下一阶段假设

- 不得把 P0 的资源结构快照视为统一 AST。
- 不得认为 `mix` 已被现有 Parser 或 Runtime 支持。
- 不得在 P1 前实现 Information、Directory 新状态机或 RuleView 复合 Key。
- 不得用 IDE 单个 Test 类成功替代 Maven Reactor 与 `mysql-it` profile 验证。

## P1 启动条件

- `run_p0_local_verification.sh` 在干净工作树上返回 `0`；
- Wrapper 可重复执行；
- 核心无 MySQL 构建通过；
- 本地 MySQL 集成测试通过；
- 故意失败测试确实使内部 Maven 命令返回非零；
- P1 自身 Evidence/Review 门禁通过。
