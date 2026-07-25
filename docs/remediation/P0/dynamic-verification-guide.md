# P0 动态验证执行指南

## 1. 当前正式验收口径

P0 动态验证采用“本地完整验证为正式门禁、GitHub Actions 为非阻断辅助回归”的方式：

- 正式退出命令：`scripts/remediation/run_p0_local_verification.sh`；
- GitHub Actions 不再决定 P0 是否可以退出；
- 正式证据必须绑定同一 Git commit，并记录分支、JDK、Wrapper Maven、命令和退出码；
- 数据库密码只允许通过环境变量提供，证据中仅记录 `DEC_MYSQL_PASSWORD=SET`；
- 正式验证默认要求 Git 工作树干净。

本次调整解决的是 GitHub 临时服务容器、网络和仓库镜像差异造成的环境误报。它不表示可以忽略测试，也不允许用 IDE 单独运行一个 Test 类替代 Maven Reactor 验证。

## 2. 正式本地完整验证

先准备专用测试数据库。不得连接生产数据库，也不得在缺少测试库配置时回退到开发库。

```bash
cd /path/to/doc-eq-code-Dgremlin

export DEC_MYSQL_URL='jdbc:mysql://127.0.0.1:3306/demo-test2'
export DEC_MYSQL_USER='root'
read -s DEC_MYSQL_PASSWORD
export DEC_MYSQL_PASSWORD

./scripts/remediation/run_p0_local_verification.sh
```

该脚本串行执行：

1. 本地核心动态验证；
2. 本地 MySQL 集成验证；
3. 校验执行前后 Git HEAD 未变化；
4. 生成汇总和 SHA-256 校验清单。

正式证据保存到：

```text
docs/remediation/P0/evidence/local-full-{UTC_TIMESTAMP}/
```

正式通过标准：

- 执行前 Git 工作树干净；
- 核心验证返回 `0`；
- MySQL 集成验证返回 `0`；
- 故意失败测试的内部 Maven 命令返回非零，但门禁证明脚本返回 `0`；
- 开始与结束 Git commit SHA 相同。

若只做故障诊断，可显式允许脏工作树：

```bash
P0_REQUIRE_CLEAN_WORKTREE=0 \
  ./scripts/remediation/run_p0_local_verification.sh
```

此类运行不得作为 P0 正式退出证据。

## 3. 本地核心验证

```bash
./scripts/remediation/run_p0_dynamic_verification.sh
```

脚本依次执行：

1. `./mvnw --version`；
2. `scripts/remediation/bootstrap_legacy_dependencies.sh`；
3. `./mvnw --batch-mode --no-transfer-progress clean verify`；
4. `scripts/remediation/prove_test_failure_gate.sh`；
5. `python3 scripts/remediation/validate_p0.py`。

证据目录：

```text
docs/remediation/P0/evidence/dynamic-{UTC_TIMESTAMP}/
```

已有一次成功证据：

```text
docs/remediation/P0/evidence/dynamic-20260725T081205Z/
```

该证据已经证明 Wrapper、全 Reactor、失败测试阻断和静态校验通过，但它没有单独完成本地 MySQL 正式验收。

## 4. 本地 MySQL 集成验证

也可单独执行：

```bash
export DEC_MYSQL_URL='jdbc:mysql://127.0.0.1:3306/demo-test2'
export DEC_MYSQL_USER='root'
read -s DEC_MYSQL_PASSWORD
export DEC_MYSQL_PASSWORD

./scripts/remediation/run_p0_local_mysql_verification.sh
```

脚本执行：

```text
./mvnw --batch-mode --no-transfer-progress -Pmysql-it clean verify
```

证据目录：

```text
docs/remediation/P0/evidence/local-mysql-{UTC_TIMESTAMP}/
```

测试数据库必须预先具备当前集成测试所需 schema 和 fixture。数据库存在但表只存在于另一台机器、另一个 schema 或 IDE 使用的不同连接时，不能视为可重复验证。

## 5. GitHub Actions 辅助回归

远程验证保留，但不属于 P0 正式退出条件：

```bash
gh auth status
./scripts/remediation/verify_p0_github_actions.sh p0-build.yml dev_all
```

脚本只生成辅助证据并报告 `core-verify` 和工作流总体结论：

```text
docs/remediation/P0/evidence/github-actions/
```

GitHub Actions 失败时仍应分析真实代码问题；但因临时 MySQL、网络、镜像或 Runner 差异导致的失败，不再阻断已经由正式本地完整验证证明通过的 P0。

## 6. 历史环境限制

2026-07-25 的受限容器无法解析 Maven/GitHub 外网且未安装 `gh`。该历史阻塞记录继续保留，不再代表当前 P0 验收口径。历史 Evidence 不修改、不覆盖；新的本地正式 Evidence 通过新时间戳目录追加。

## 7. 对 P1 门禁的影响

P0 验收方式调整不自动解除 P1 门禁。P1 仍需处理旧 Evidence digest、Review criterion 和派生状态问题；在 P0 本地完整验证正式通过且 P1 自身门禁通过前，不得进入 `test_design`。
