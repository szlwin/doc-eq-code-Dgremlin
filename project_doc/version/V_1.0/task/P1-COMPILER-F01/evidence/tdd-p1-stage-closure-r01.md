# TDD-P1-STAGE-CLOSURE-R01 — 生产 Bootstrap RED

## 基线

```text
Base Head: ce1856a53d7a2180b0bf341456740411bf6c9f06
Expected RED: CompilerBootstrap symbol is absent
Test: CompilerBootstrapStageClosureTest
```

## RED Oracle

测试直接要求：

- 通过 Builder 创建生产 Bootstrap；
- 真实 mix 同时包含 XML/YAML；
- 一次调用返回 PUBLISHED；
- PublishedCompilationResult、EngineContext 与 Publisher current identity 一致；
- 定义和 Deferred 非空；
- 第二次失败不调用 Publisher、不污染旧 Context；
- Projection 只来自 Published EngineContext。

本记录将在 GitHub Actions 产生真实失败 Run 后补充 Run ID、失败步骤和精确编译错误。RED 证据完成前不得进入 Development Completion。
