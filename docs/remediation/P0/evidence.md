# P0 验收证据

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

当前容器没有 Maven且 DNS 受限，无法下载 Maven 3.9.16。该环境限制不能通过忽略测试规避；GitHub Actions `core-verify` 是正式动态证据。

## 当前容器 Wrapper 结果

```text
./mvnw --version
exit=6
curl: (6) Could not resolve host: repo.maven.apache.org
```

这证明 Wrapper 已进入 Maven 发行包下载流程；动态构建需由具备外网的 GitHub Actions 完成。
