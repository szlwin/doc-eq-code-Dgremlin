# TASK-P1-T08 R01 Testing Evidence

## 有效 RED

- Head：`d7155c4f0bb1c930231671fa3041d532bd17a97f`
- P0 Run：`30827276340`
- Artifact：`8861386414`
- SHA-256：`2765996eac357969a12f4ee72d69d0f8be4ead5180950c9ba16fbbee0509136b`
- Compiler：173 tests / 9 failures / 0 errors；
- T08 failure：9；T07 Symbol 32/32 继续通过；
- Java release 8：编译成功。

## Architecture Skeleton

- Head：`a063504eb209ba575f5e16d6f849a012a65d3f29`
- P0 Run：`30827946835`
- Artifact：`8861663247`
- SHA-256：`8e740bf27e630512d2484108e93eb8623bca7900d14c4ed97a3d693af389d5e2`
- Result：9 controlled failures / 0 errors。

## Clean-code GREEN

- Head：`ab432a3189f45c4267ce32af2e104bd39a8c79d1`
- P0 Run：`30828498760` — SUCCESS
- Artifact：`8861902903`
- Size：`1847794`
- GitHub SHA-256：`0f506c50e3a1e0d4cc25da4ea5da4ef064404d5c8628686739906af08069f244`
- 独立下载实际 SHA-256：完全一致；
- Surefire XML：64 个。

## Surefire 独立解析

- T08：12/12；
- Symbol：44/44；
- Compiler：173/173；
- XML：30/30；
- YAML：59/59；
- Context 正常：26/26；
- Demo：4/4；
- Legacy declaration：1/1；
- 故意失败门禁：1 项预期失败并被工作流识别；
- Reactor：12 modules / PASSED；
- Java release 8：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Oracle 覆盖

Connection/DataSource、View target/data/property、System Data/View、RuleView forward owner/declared View、Action system/rule owner、Directory qualified Information/same-scope rel、Produce Information、unknown/type mismatch/owner mismatch、完整聚合、稳定排序、去重、重复运行一致、失败无部分结果、不可变集合均通过。

- `REV-000350` — TddFinalReview — PASSED；
- `REV-000351` — TestEvidenceReview — PASSED；
- Evidence：`EVD-000597`～`EVD-000598`。
