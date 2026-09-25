# TP-P1-COMPILER-F01-R43 — TASK-P1-T13 I003 实施计划

- Revision：`TP-P1-COMPILER-F01-R43@P1-T13-REWORK-I003`
- Design：`DESIGN-R47@P1-T13-REWORK-I003`
- Status：`PASSED`
- Base：`PR28@f80656c19dd695c92e75a4d8eceb8b54d8e37940`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`

## Workflow

1. 失效并保留 R03 Review 与 R02 Completion；
2. 冻结 R47/R43，创建 I003；
3. 记录 Oracle hardening 的 `RED_NOT_APPLICABLE` 理由与负向变异证明；
4. 只修改 `CompilationObserverIndependentReviewTest.java`；
5. 使用 Control/Observed 两次确定性失败执行；
6. 精确断言原 ERROR 的 code/severity/messageKey/pass；
7. 精确断言 Observer Warning 的 code/severity/messageKey/pass；
8. 比较 state、executedPasses、transitions、timings；
9. 断言两组 publisher=0、artifacts empty；
10. 运行定向 T13、Compiler、T12 和全 Reactor；
11. 独立 Review 证明无 production diff、无后续任务越界；
12. 下载 Artifact 并核对 ZIP SHA-256、Surefire XML 与测试统计；
13. 登记 Review、Testing、Revision Lock、Completion、Handoff；
14. 所有文档提交完成后对最终 Head 执行 P0；
15. 仅通过 PR 元数据登记最终 Run/Artifact，不再修改分支文件；
16. PR #28 保持未合并，T14 保持阻断。

## Validation commands

```text
./mvnw -pl dec-core-compiler -am -Dtest=CompilationObserverIndependentReviewTest test
./mvnw -pl dec-core-compiler -am test
./mvnw --batch-mode --no-transfer-progress clean verify
```

## Stop conditions

- 完整 Oracle 暴露生产缺陷；
- 修改任何 production 文件；
- Control/Observed 使用不同失败位置或不同 Clock 行为；
- 未精确验证 Warning subject；
- 未比较 transitions/timings/executedPasses；
- Open P0/P1/P2 未清零；
- PR #28 被合并或 T14 被提前启动。
