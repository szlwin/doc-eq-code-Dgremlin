# TASK-P1-T13 R01 重新 Review 与失效记录

- Review ID：`CODEREVIEW-P1-T13-R02-INVALIDATION@P1-T13-I002`
- Scope：`TASK-P1-T13 / I001`
- Reviewed Head：`9d180f2d34728cd453c377a6310b01fe1a7659cf`
- Reviewed Code/Test Revision：`74672ee1367bab9de75b4028cd4578b6118f96f0`
- Previous Completion：`COMPLETION-P1-T13-R01@74672ee1367b`
- Result：`NEEDS_CHANGES`
- Open P0/P1/P2：`0 / 1 / 1`

## Gate decision

以下历史事实保留但立即失效，不得继续作为当前通过门禁：

- `CODEREVIEW-P1-T13-R01@74672ee1367b` — `INVALIDATED / PRESERVED`；
- `COMPLETION-P1-T13-R01@74672ee1367b` — `INVALIDATED / PRESERVED`；
- R45、R41、R01 RED、Architecture、Development、Testing、Artifact 与 Revision Lock 均作为 I001 历史不可变保留。

TASK-P1-T13 进入返工迭代 `I002`。PR #28 保持 Open、未合并；`TASK-P1-T14` 继续 `BLOCKED_UNTIL_PR_28_MERGE`。

## FND-P1-T13-I002-001

- Severity：`P1 / BLOCKER`
- Area：`DIGEST / UNICODE / CORRECTNESS`
- Status：`OPEN`

`CompilerDigestService.sourceDigest()` 当前通过 `String.getBytes(StandardCharsets.UTF_8)` 编码 `sourceId`。Java 宽松 UTF-8 编码会把不同未配对 surrogate 替换为相同字节，导致不同 Source 身份在 SHA-256 前发生有损碰撞。

修复合同：

1. 对进入 Source digest identity 域的原始 `sourceId` 使用严格 UTF-8 编码；
2. malformed/unmappable 输入必须 fail-closed；
3. 稳定异常为 `IllegalArgumentException("sourceId must contain valid Unicode")`；
4. 不修改合法 ASCII、BMP 与 supplementary code point 的既有摘要向量；
5. 不把该修复扩大为 T14/T15 或 Source 模型整体重构。

## FND-P1-T13-I002-002

- Severity：`P2`
- Area：`ORACLE / TEST-EVIDENCE`
- Status：`OPEN`

现有测试未冻结 `FAILED` 终态转换时 Observer 抛出异常的组合。必须新增由 Pass ERROR 触发 FAILED 的测试，并断言：

- 最终状态仍为 FAILED；
- 原始 ERROR 保留且 severity 不变；
- 新增 `MIX_OBSERVER_FAILURE / WARNING`；
- Observer 异常不传播；
- publisher 调用次数为 0；
- 失败 artifacts 为空。

## I002 stop conditions

- Design/Plan 晚于有效 RED；
- malformed sourceId 仍被替换或形成相同 sourceDigest；
- 合法 Source digest 已知向量变化；
- Observer Warning 覆盖、降级或删除原始 ERROR；
- FAILED 路径调用 publisher 或保留 artifacts；
- T12 Deadline/Cancel/Publication 原子性回归；
- Open P0/P1/P2 未清零；
- 未完成最终 P0、Artifact 独立 SHA/XML 解析和 Revision Integrity；
- 未经用户明确授权合并 PR #28。
