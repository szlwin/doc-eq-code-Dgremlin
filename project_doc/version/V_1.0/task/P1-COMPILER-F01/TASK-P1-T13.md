# TASK-P1-T13 — 确定性 Digest、Deadline 与 Observer

- Current Iteration：`I002`
- Status：`IN_PROGRESS / TDD_RED_PENDING`
- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Rework Base：`PR28@9d180f2d34728cd453c377a6310b01fe1a7659cf`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / OPEN / REWORK / DO_NOT_MERGE`
- Current Design：`DESIGN-R46@P1-T13-REWORK-I002`
- Current Plan：`TP-P1-COMPILER-F01-R42@P1-T13-REWORK-I002`
- Previous Code Review：`CODEREVIEW-P1-T13-R01@74672ee1367b` — `INVALIDATED / PRESERVED`
- Previous Completion：`COMPLETION-P1-T13-R01@74672ee1367b` — `INVALIDATED / PRESERVED`
- Open P0/P1/P2：`0 / 1 / 1`

## I002 findings

- `FND-P1-T13-I002-001`：P1，`sourceDigest` 使用宽松 UTF-8 编码，允许不同 malformed UTF-16 sourceId 在 SHA-256 前形成相同替代字节；
- `FND-P1-T13-I002-002`：P2，缺少 FAILED 终态 transition Observer exception 的冻结测试。

## I002 target contract

- Source identity 使用独立 `CharsetEncoder`，malformed/unmappable 均 `REPORT`；
- malformed sourceId 稳定抛 `IllegalArgumentException("sourceId must contain valid Unicode")`；
- 合法 ASCII、BMP、supplementary sourceDigest 已知向量不变；
- FAILED transition Observer exception 只追加 `MIX_OBSERVER_FAILURE / WARNING`；
- 原始 ERROR、FAILED 状态、publisher=0 与空 artifacts 保持；
- T12 Deadline/Cancel/Clock/Publication 原子性保持；
- 不实现 T14/T15 或 P2～P7 runtime。

## Preserved I001 history

- Design：`DESIGN-R45@P1-T13-I001`；
- Plan：`TP-P1-COMPILER-F01-R41@P1-T13-I001`；
- TDD：`TDD-P1-T13-R01@4f3d444f779f`；
- Architecture：`DEVSKEL-P1-T13-R01@4f3d444f779f`；
- Development：`DEV-P1-T13-R01@74672ee1367b`；
- Testing：`TESTING-P1-T13-R01@74672ee1367b`；
- Completion：`COMPLETION-P1-T13-R01@74672ee1367b` — INVALIDATED / PRESERVED；
- Final I001 Head：`9d180f2d34728cd453c377a6310b01fe1a7659cf`；
- Final I001 P0：`31008895114`；Artifact：`8931548482`；SHA-256：`f4b69e424dec4395bc09cbced7642d320ce0791dee9a9e6c22e7207ecb944cc4`。

## I002 stop conditions

- R46/R42 晚于 RED；
- malformed sourceId 被替换、忽略或规范化；
- 合法 digest vector 漂移；
- FAILED Observer Warning 覆盖原 ERROR 或异常传播；
- publisher>0 或失败 artifacts 非空；
- Open P0/P1/P2 未清零；
- final P0、Artifact 独立解析或 Revision Integrity 未完成；
- PR #28 被合并；
- T14 被提前启动。

PR #28 未经用户明确授权不得合并；`TASK-P1-T14` 保持 `BLOCKED_UNTIL_PR_28_MERGE`。
