# TASK-P1-T06 I003 Architecture Skeleton Evidence

- Revision：`DEVSKEL-P1-T06-R03@35357c213fdc`
- Accepted Skeleton Head：`35357c213fdc6f0e485303db91084797f1078dbc`
- Rejected broad attempt：`3ef4c681fe1f051da3435319916721ec83b9dbc9`
- Accepted P0 Run：`30798641149` — `FAILURE`（受控 Skeleton RED）
- Artifact：`8849914316`
- Artifact SHA-256：`1d03449a290a5cf853b8f32094f18040e664143ed4a96e875f9eef8268c3e79e`
- Reviews：`REV-000287` ArchitectureReviewAgent — `PASSED`；`REV-000288` SpecComplianceReviewAgent — `PASSED`
- Evidence：`EVD-000529`、`EVD-000530`

## 被推翻的 Attempt

`3ef4c681fe1f...` 使 `snapshotDocuments` 全局抛出未实现异常，导致上一轮 Raw 合同大面积回退。该 attempt 的 P0 Run `30798358390` 为失败，Review 判定不满足“Skeleton 不得破坏既有合同”，因此只作为不可变失败历史保留，不作为有效 Skeleton revision。

## 有效 Skeleton

`35357c213fdc...` 冻结以下主线：

1. public `build` 入口先调用 `snapshotDocuments(documents)`；
2. `validateDocuments(snapshot)`；
3. extraction 只遍历同一 `snapshot`；
4. `snapshotDocuments` 对原 List 执行单次 enhanced-for 复制；
5. null 输入、空复制结果、复制项 null 进入既有稳定失败；
6. 正常 snapshot 路径保留 I002 全部合同；
7. 仅 RuntimeException catch 仍调用 `firstSourceRef(documents)`，作为下一阶段必须关闭的显式受控 RED。

## P0 结果

- Java release 8 production/test compile：PASSED；
- Context：26/26 PASSED；
- I002 Raw：31/31 PASSED；
- I003：7 run / 0 failures / 1 error / 0 skipped；
- Compiler：121 run / 0 failures / 1 error；
- 唯一 error：`snapshotReadFailureDoesNotReaccessOriginalList`，栈定位到 `firstSourceRef(documents)` 通过 `size/isEmpty` 重访原 List；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

该 Skeleton 已将问题收敛为单一异常边界，不通过线程、时间、兼容分支或测试专用生产逻辑伪造结果，允许进入 Development GREEN。
