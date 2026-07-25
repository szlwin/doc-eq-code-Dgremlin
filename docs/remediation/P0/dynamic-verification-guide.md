# P0 动态验证执行指南

## 1. 本次容器执行结果

执行时间：2026-07-25 UTC。

环境：

- Java：OpenJDK 21.0.10；
- 系统 Maven：未安装；
- Maven Wrapper 缓存：不存在；
- `curl`、`wget`、`unzip`：存在；
- `gh`：未安装；
- 容器 DNS：无法解析 `repo.maven.apache.org` 和 `github.com`。

首次执行原配置：

```bash
./mvnw --version
```

结果：

```text
curl: (6) Could not resolve host: repo.maven.apache.org
exit=6
```

同时发现原配置使用不存在的 Maven `3.9.16`。已将 `.mvn/wrapper/maven-wrapper.properties` 和根 `pom.xml#maven.version` 统一修正为 Maven `3.9.15`。当前容器仍因 DNS 限制无法下载发行包，因此后续动态 Maven 步骤没有伪造为通过。

## 2. 本地一次性验证

在可访问 Maven Central 的本地环境执行：

```bash
cd /path/to/doc-eq-code-Dgremlin
./scripts/remediation/run_p0_dynamic_verification.sh
```

脚本依次执行：

1. `./mvnw --version`；
2. `scripts/remediation/bootstrap_legacy_dependencies.sh`；
3. `./mvnw --batch-mode --no-transfer-progress clean verify`；
4. `scripts/remediation/prove_test_failure_gate.sh`；
5. `python3 scripts/remediation/validate_p0.py`。

每一步都会把命令输出和退出码保存到：

```text
docs/remediation/P0/evidence/dynamic-{UTC_TIMESTAMP}/
```

验证通过标准：

- Wrapper 输出 Maven `3.9.15`；
- `clean verify` 返回 `0`；
- 故意失败测试的内部 Maven 命令必须返回非零，而 `prove_test_failure_gate.sh` 自身返回 `0`；
- 静态验证返回 `0`。

## 3. 分步执行命令

```bash
./mvnw --version
scripts/remediation/bootstrap_legacy_dependencies.sh
./mvnw --batch-mode --no-transfer-progress clean verify
scripts/remediation/prove_test_failure_gate.sh
python3 scripts/remediation/validate_p0.py
```

若 `prove_test_failure_gate.sh` 输出：

```text
P0 failure gate proved: failing test returned status ...
```

且脚本退出码为 `0`，表示测试失败确实阻断 Maven 构建。

## 4. GitHub Actions core-verify

前提：本地已安装并授权 GitHub CLI，且上述修改已经提交并推送到 `dev_all`。

```bash
gh auth status
./scripts/remediation/verify_p0_github_actions.sh p0-build.yml dev_all
```

脚本会：

1. 触发 `workflow_dispatch`；
2. 等待工作流完成；
3. 检查 `core-verify` Job 的 conclusion 必须是 `success`；
4. 保存 run JSON、日志和测试报告 Artifact。

证据目录：

```text
docs/remediation/P0/evidence/github-actions/
```

也可手工执行：

```bash
gh workflow run p0-build.yml --ref dev_all
gh run list --workflow p0-build.yml --branch dev_all --limit 5
gh run watch {RUN_ID} --exit-status
gh run view {RUN_ID} --json headSha,status,conclusion,url,jobs
gh run download {RUN_ID} --name surefire-and-jacoco-reports
```

## 5. 对 P1 门禁的影响

Maven 版本从不存在的 `3.9.16` 修正为 `3.9.15` 后，根 `pom.xml` 的内容发生真实变化。P1 既有三个阶段分别直接引用旧 POM digest，因此当前 common-develop 2.35 `task-health` 阻断数由 162 增加到 165。

新增的 3 条阻断是：

- `EVD-000022`；
- `EVD-000085`；
- `EVD-000135`。

本轮不覆盖旧 digest，也不伪造 Review 通过；这些 Evidence 应在后续 `P1-GATE` 修复中按 supersede / 新 Evidence 流程处理。当前 P1 仍不得进入 `test_design`。
