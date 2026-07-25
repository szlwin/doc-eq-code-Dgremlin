# P0 任务状态

| 任务 | 状态 | 证据                                                                                                              |
|---|---|-----------------------------------------------------------------------------------------------------------------|
| P0-T01 基线 | PASSED | `baseline.md`、`scripts/remediation/generate_p0_baseline.py`                                                     |
| P0-T02 Wrapper/JDK | PASSED | `evidence/dynamic-20260725T081205Z/01-mvnw-version.log`；Wrapper 动态执行成功                                          |
| P0-T03 插件与失败门禁 | PASSED | `evidence/dynamic-20260725T081205Z/03-clean-verify.log`、`04-failure-gate.log`；正常 Reactor 返回 0，故意失败测试返回 1 并被门禁识别 |
| P0-T04 Reactor | PASSED | `dec-demo` 已进入根 `modules`                                                                                       |
| P0-T05 main 测试迁移 | PASSED | `dec-demo/src/test/java/dec/demo/{directory,model,system,declaration}`                                          |
| P0-T06 数据库隔离 | PASSED | 默认 `clean verify` 排除 `mysql-it`；数据库依赖为 test scope                                                               |
| P0-T07 旧契约快照 | PASSED | `LegacyResourceSnapshotTest` 与 SHA-256 清单                                                                       |
| P0-T08 mix 骨架 | PASSED | `MixContractTest`                                                                                               |
| P0-T09 可重复自动化验证门禁 | PASSED | `evidence/local-full-20260725T142126Z/summary.txt`；核心验证与 MySQL 验证均返回 0，开始与结束 Git SHA 一致                                    |
| P0-T10 异常日志基线 | PASSED | `exception-log-baseline.md`、`scripts/remediation/scan_exception_baseline.py`                                    |
