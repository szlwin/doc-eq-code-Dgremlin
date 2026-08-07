# DEV-P1-STAGE-CLOSURE-R01 — 生产组装与真实 mix 集成

## 一、实现范围

- 新增 `dec-core-frontends`，迁出已验证的安全 XML/YAML `DocumentFrontend`，避免 Starter 依赖旧 Parser Artifact 或复制实现；
- 新增 `ClasspathDocumentSourceProvider`，支持 exploded directory 与 jar、稳定排序、SHA-256、安全根和重复 SourceId fail-closed；
- 新增 `StandardCompilerPasses`、`StandardModelCompiler` 与工厂，接线固定十阶段 Pipeline；
- 新增 `CompilerBootstrap`，从根 Source、CompilationOptions 和 expectedCurrent 一键 compile-and-publish；
- 新增真实 XML/YAML mix fixture 与 Stage Closure E2E；
- 保留 `CompilerStarter` 的实例级委托边界和 T15 Runtime Retirement 合同。

## 二、端到端 Oracle

1. 真实 mix 发现 10 个 Source，同时经过 XML/YAML Frontend；
2. 固定十阶段 Timing 顺序与 `CompilerPipeline.fixedPassOrder()` 完全一致；
3. Definitions、Deferred、SourceManifest、Digest、Candidate Context 均由同一次编译形成；
4. Publisher current、PublishedCompilationResult.engineContext 和 CompiledModelSet 保持 identity；
5. 两次独立编译产生相同 Source/Semantic Digest；
6. 第二次非法根编译 FAILED，Publisher 调用次数和此前 Context identity 均不变；
7. 旧 Config 全局入口及旧 XML/YAML Parser 类型继续不可见。

## 三、本地验证

```text
Environment: complete source snapshot bound to PR Head ce1856a53d7a2180b0bf341456740411bf6c9f06
Maven: 3.9.15 wrapper cache / offline
Java: runtime 21, project release 8, enforcer Java >= 17
Command: ./mvnw --batch-mode --no-transfer-progress clean verify
Reactor: 12/12 SUCCESS
Surefire XML: 110
Tests: 638
Failures / Errors / Skipped: 0 / 0 / 0
Starter: 13/13
Stage Closure E2E: 3/3
Classpath Provider: 3/3
```

Intentional failure gate 已独立观察到预期 JUnit failure 和 Maven status 1。T14/T15 的最终有效证据以 GitHub Java 17 P0 Run 为准；本地多次串行 mutation 执行在当前 Java 21 容器中出现进程超时，因此不以本地结果替代远端门禁。

## 四、状态

```text
Development: IMPLEMENTED
Local clean verify: PASSED
Remote P0: PENDING
MySQL workflow_dispatch: PENDING
Code Review / Completion: BLOCKED_BY_REMOTE_EVIDENCE
```
