# P0 验收证据

> 当前正式验收口径：以 `scripts/remediation/run_p0_local_verification.sh` 生成的同一 commit 本地核心与 MySQL Evidence 为准；GitHub Actions 仅作非阻断辅助回归。下文早期“等待 CI”的内容属于历史执行记录。

## 已执行

```text
python3 scripts/remediation/generate_p0_baseline.py --revision f9424ae
python3 scripts/remediation/scan_exception_baseline.py
python3 scripts/remediation/validate_p0.py
scripts/remediation/bootstrap_legacy_dependencies.sh  # 在 Maven 可用环境执行
sh -n mvnw
scripts/remediation/prove_test_failure_gate.sh  # 在 Maven 可用环境执行
```

本地静态验证结果：`P0 static validation passed`；POM XML、Shell 语法、Python 语法与提交范围检查均通过。

## 远端提交

- P0 实现提交：`8cac10732886b9bce81e233ff23a941083d8548c`
- 分支：`dev_all`
- 推送方式：GitHub Git Data API 非强制快进更新

## 待 CI 执行

```text
./mvnw --version
./mvnw clean verify
```

当前容器没有 Maven且 DNS 受限，无法下载 Maven 3.9.15。该环境限制不能通过忽略测试规避；GitHub Actions `core-verify` 是正式动态证据。

## 当前容器 Wrapper 结果

```text
./mvnw --version
exit=6
curl: (6) Could not resolve host: repo.maven.apache.org
```

这证明 Wrapper 已进入 Maven 发行包下载流程；动态构建需由具备外网的 GitHub Actions 完成。


## 2026-07-25 动态验证重试

本轮先修复了原仓库中不存在的 Maven `3.9.16` 配置，统一改为 Maven `3.9.15`。随后执行：

```bash
P0_EVIDENCE_STAMP=20260725T063000Z ./scripts/remediation/run_p0_dynamic_verification.sh
```

结果：

- `./mvnw --version`：退出码 `6`，容器 DNS 无法解析 `repo.maven.apache.org`；
- legacy 依赖安装、`clean verify` 和故意失败测试门禁：因 Wrapper 未启动而未执行；
- `python3 scripts/remediation/validate_p0.py`：退出码 `0`，静态验证通过；
- `verify_p0_github_actions.sh`：退出码 `2`，当前容器未安装 `gh`。

证据：

- `docs/remediation/P0/evidence/dynamic-20260725T063000Z/`；
- `docs/remediation/P0/evidence/github-actions-current-env/`；
- `docs/remediation/P0/dynamic-verification-guide.md`。

当前不得把 P0-T02、P0-T03 或 P0-T09 标记为 PASSED。根 `pom.xml` 的真实修改使 P1 旧 POM Evidence（EVD-000022、EVD-000085、EVD-000135）失效，当前 P1 `task-health` 阻断数为 165，留待后续 P1-GATE 正式迁移。

## P0 本地动态验证——2026-07-25

证据目录：

`docs/remediation/P0/evidence/dynamic-20260725T081205Z/`

结论：

- Maven Wrapper：PASSED
- Legacy 依赖安装：PASSED
- 全 Reactor `clean verify`：PASSED
- 故意失败测试阻断：PASSED
- P0 静态验证：PASSED

P0-T02、P0-T03 可以关闭；P0-T09 等待 GitHub Actions 回执。

## 2026-07-25 验收口径调整

由于 GitHub Runner、临时 MySQL Service、网络和镜像差异导致远程验证结果不稳定，P0 正式动态验收调整为：

```text
scripts/remediation/run_p0_local_verification.sh
```

正式命令在干净工作树上串行执行核心验证与本地 `mysql-it`，并将两部分 Evidence 绑定到同一 Git commit。数据库密码仅通过环境变量提供，Evidence 只记录密码已设置。

GitHub Actions 及 `verify_p0_github_actions.sh` 继续保留，但仅作为非阻断辅助回归，不再决定 P0 是否退出。本文前述“等待 GitHub Actions 回执”属于历史执行记录，已由本节的新口径取代，不修改原始历史 Evidence。

当前结论：

- P0-T02：PASSED；
- P0-T03：PASSED；
- P0-T09：REVIEWING，等待在干净工作树上执行统一的本地完整验证并固化 MySQL Evidence；
- P1 仍受 P0 本地完整门禁和自身 Evidence/Review 门禁阻断。
