# TASK-P1-T11 R01 TDD Evidence

- TDD：`TDD-P1-T11-R01@7fd853fca405`
- Evidence：`EVD-000741`～`EVD-000744`

## Rejected attempt

- Head：`4240f2ea2c10baea5383a2cd9366babdc845e7bf`
- P0 Run：`30913300380`
- Artifact：`8893985665`
- SHA-256：`5d354df2f4eef73afb4a656079f492bf22278a4d82af7a7a34d6ba1120c7a563`
- Result：`17 failures / 1 error`
- 原因：Oracle 在受控失败结果上直接读取缺席 Registry，产生 `NoSuchElementException`；该结果不作为有效 RED。

## Valid RED

- Head：`7fd853fca4055c7bf4f3049443d594b286d597fa`
- P0 Run：`30913711698`
- Artifact：`8894144140`
- SHA-256：`48512b0edbfe1eb40bf883aea1a4d77d6fd36db40e765a67235e16478258eb61`
- Result：`18 failures / 0 errors`
- Java release 8：105 个生产源、58 个测试源编译通过。
- 既有 T01-T10 全部绿色；失败仅来自 T11 分类、完整性、确定性与原子发布 Oracle。
- MySQL：`SKIPPED_NOT_APPLICABLE`。
