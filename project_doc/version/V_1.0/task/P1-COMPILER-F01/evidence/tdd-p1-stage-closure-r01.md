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

## 实际 RED 证据

```text
Commit: 0c68b04326aa7c22b2c7a2677dc180409b1f064d
Run: 31107261916
Job: core-verify / 92635554925
Step: Core build and tests
Result: FAILURE
Failure phase: dec-core-starter:testCompile
Primary cause: cannot find symbol CompilerBootstrap
```

失败发生在生产入口缺失，而不是 Maven、依赖下载或测试环境错误。RED 同时暴露了测试初稿中的 Projection import 包名错误；GREEN 已按仓库现有 `dec.core.context.CoreConfigProjection` 修正，该次要错误不改变 Bootstrap 缺失这一主要 RED 事实。

```text
TDD RED Gate: PASSED
Development Entry: ALLOWED
```
