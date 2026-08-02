# DEC Compiler Implementation Plan R08 — T02 Source Policy Closure

- Revision：`TP-P1-COMPILER-F01-R08@P1-T02-REWORK-I005`
- 设计输入：`DESIGN-R12@P1-T02-REWORK-I005`
- 任务：`TASK-P1-T02 / I005`
- 状态：`PASSED`

## 1. TDD RED

新增 Java 8 可编译的 R05 负向 Oracle，禁止通过缺少类型或编译错误形成 RED：

1. `AllowedRoot` 原始字面量 traversal 根必须抛出；
2. opaque 根 query 必须抛出，opaque 候选 query 必须返回 false；
3. 反射确认 `resolvedSingle/resolvedFileSet/validateSingle/validateFileSet` 公共签名存在；
4. 反射确认通用 `resolved(List, List)` 不再属于最终 API；
5. 单源错误基数、文件集重复 sourceId、不同 digest 冲突和第三方 FAILED 部分候选必须被拒绝或转换。

RED 期望：既有 Context 与 Compiler 测试保持绿色，新增 R05 Oracle 仅因目标行为缺失而失败，0 errors、0 skipped。

## 2. Architecture Skeleton

- 在 `SourceResolutionResults` 建立四个冻结公共方法签名；
- `resolvedSingle` 可直接复用现有不可变结果骨架；
- `resolvedFileSet` 和两个 validator 允许以显式 `Architecture skeleton only` 保持受控 RED；
- 不提前修改 `AllowedRoot` 行为，确保 Skeleton 仍能证明安全实现尚未完成；
- 所有新增方法提供中文 Javadoc，新增 `@Override` 必须独占一行。

## 3. Development GREEN

### AllowedRoot

- 原始 URI 在 normalize 前执行 raw/decoded traversal 检查；
- 对 opaque scheme-specific part 检查 raw 和 decoded `?`；
- 构造器非法输入抛出 `IllegalArgumentException`；
- `contains` 对非法候选返回 false；
- 保留等价尾斜杠和路径段边界行为。

### SourceResolutionResults

- 删除通用 `resolved(List, List)`；
- 实现 `resolvedSingle`、`resolvedFileSet`；
- 稳定排序后检测相邻重复 sourceId，禁止静默去重；
- 实现 `validateSingle`、`validateFileSet`；
- 第三方合同违规统一转换为无 Source 的 FAILED，并生成 `MIX_SOURCE_POLICY` ERROR；
- 合法第三方结果也必须重新防御性复制，不能直接透传可变集合。

## 4. Review

独立执行：

- Specification Review：逐项核对 R12 不变量和停止条件；
- Architecture Review：检查 T03 是否无需新增公共签名，检查 opaque URI 与层次 URI 语义没有混用；
- Code Review：检查异常边界、重复 sourceId、Diagnostic 构造、不可变集合和 Java 8 兼容性；
- TDD Review：确认 RED 是行为失败，GREEN 覆盖负向场景且没有弱化旧测试。

开放 P0/P1 必须阻断 Testing 与 Completion。

## 5. Testing

最终阻断验证：

- Context 全量正常测试；
- Compiler 全量正常测试；
- 完整 12 模块 `clean verify`；
- Java release 8 生产与测试编译；
- 故意失败测试能够阻断构建；
- MySQL 继续按适用性报告，不得把跳过表述为通过；
- 最终 Artifact 必须绑定 clean-code Head。

## 6. Completion

Completion 必须记录：

- I004 Completion 被 I005 Review 推翻但历史保留；
- 两个 P1 和重新打开的 Oracle 子项全部关闭；
- clean-code Head、P0 Run、测试计数、Review/Evidence ID；
- `t03_public_signature_change_required=false` 只能在 validators 和负向 Oracle 均通过后声明；
- PR #17 只恢复 Ready for review，不执行合并；
- TASK-P1-T03 继续保持未启动和阻断。
