# DEC Compiler Design R16 — TASK-P1-T03 I004

- Revision：`DESIGN-R16@P1-T03-REWORK-I004`
- 输入基线：`COMPLETION-P1-T03-R03@cedf22bb14ff`（被独立 Review 推翻，历史保留）
- Rework base：`0a845817c90d201b834df6f581c5461b3ebac880`
- 目标：保持 SourceReference canonicalization 前后的 URI 相对/绝对类别，并完成 Resolver 根入口受控失败边界。

## 1. 问题

当前 hierarchical canonicalization 删除前导 `.` 或 `%2e` 后，可能把：

- `./classpath:mix/orm-config.xml`
- `%2e/classpath:mix/orm-config.xml`
- `./file:/mix/root.xml`
- `%2E/file:/mix/root.xml`

提升为允许 scheme 下的绝对 URI，导致 SourcePolicy 无法在 Provider 前拒绝原始相对引用。

同时 `MixSourceResolver.resolve()` 的根 `SourceRef` 创建、策略验证和 Provider 判空仍位于统一 `try/catch` 外，未完全实现 R15 的受控失败边界。

## 2. 冻结合同

### 2.1 URI 类别保持

1. 构造时记录原始 `URI.isAbsolute()`；
2. hierarchical canonicalization 删除独立当前目录段后，必须验证 canonical 文本重新解析后的 `isAbsolute()` 与原始值一致；
3. 原始为相对 URI、canonical 文本首个剩余 segment 含 `:` 而将被解释为 scheme 时，必须在结果前重新添加 `./`；
4. 字面量 `.` 与编码 `%2e` 使用同一规则；
5. 绝对 opaque/hierarchical URI 的既有 canonical key 不变；
6. `..`、编码父目录、query、fragment、`%2F` 等安全证据不隐藏。

### 2.2 Provider 前拒绝

上述四种相对根必须由 SourcePolicy 返回：

- status：`FAILED`
- Diagnostic：`MIX_SOURCE_PATH_ESCAPE`
- graph：empty
- Provider access count：0

不得进入 `provider.resolve()`、`resolveFileSet()`、Edge 或 cycle identity。

### 2.3 Resolver 受控失败边界

`root == null` 与 `policy == null` 保持显式参数异常。其余根入口动作统一放入 `try/catch`：

- 根 `SourceRef` 创建；
- `policy.validateReference()`；
- Provider 判空；
- `Discovery` 创建与执行。

`SourceFailure` 发布原 Diagnostic；其它 RuntimeException 映射为稳定 `MIX_SOURCE_POLICY / source.discovery.unexpected`，且不得暴露部分图。

## 3. 不变量

- `SourceReference` 继续只有一个实例字段 `value`；
- `@Override` 独占一行；
- 方法、构造器及关键 canonical、安全边界逻辑使用中文注释；
- Java release 8；
- 不修改 `dec-core-context` 生产代码；
- 不实现 TASK-P1-T04；
- 开放 P0/P1 阻断 Completion 和 PR Ready 状态。
