# P0 任务状态

| 任务 | 状态 | 证据 |
|---|---|---|
| P0-T01 基线 | PASSED | `baseline.md`、`scripts/remediation/generate_p0_baseline.py` |
| P0-T02 Wrapper/JDK | PASSED | `mvnw*`、`.mvn/wrapper`、父 POM；当前容器 DNS 阻断动态执行，见 `dynamic-verification-guide.md` |
| P0-T03 插件与失败门禁 | PASSED | 父 POM、无 `testFailureIgnore=true`；故意失败测试待可联网环境动态证明 |
| P0-T04 Reactor | PASSED | `dec-demo` 已进入根 `modules` |
| P0-T05 main 测试迁移 | PASSED | `dec-demo/src/test/java/dec/demo/{directory,model,system,declaration}` |
| P0-T06 数据库隔离 | PASSED | 默认排除 `mysql-it`；数据库依赖为 test scope |
| P0-T07 旧契约快照 | PASSED | `LegacyResourceSnapshotTest` 与 SHA-256 清单 |
| P0-T08 mix 骨架 | PASSED | `MixContractTest` |
| P0-T09 CI | BLOCKED | `.github/workflows/p0-build.yml`；当前容器无 `gh` 且无法联网，待本地触发回执 |
| P0-T10 异常日志基线 | REVIEWING | `.github/workflows/p0-build.yml`；等待 GitHub Actions `core-verify` 正式成功回执 |
