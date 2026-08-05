# TASK-P1-T13 I002 Design / Plan Evidence

- Evidence：`EVD-001019`
- Iteration：`TASK-P1-T13 / I002`
- Design：`DESIGN-R46@P1-T13-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R42@P1-T13-REWORK-I002`
- Reviewed base：`PR28@9d180f2d34728cd453c377a6310b01fe1a7659cf`

## Revision order

- R46 first commit：`126db598680958cd6f4c4c2bdc8745743402b4ca`；
- R46 blob：`20034ec2cdb4353dbca459df7cdb2335e25b182b`；
- R42 first commit：`3fa56d310286b4f72d4843f2129b5d1906cc21a0`；
- R42 blob：`6a346fe9d15f9ffbca17b0edb6622e1d044b57a0`；
- I002 valid RED：`83c66072849c8017beb74adbb539820a15bb515e`。

R46/R42 均在任何 I002 测试和生产修改之前提交，Revision Gate 通过。

## Scope

- 生产范围：仅 `CompilerDigestService.java`；
- 测试范围：strict Unicode Source identity 与 FAILED Observer failure；
- 排除：Source 模型整体重构、Publisher/CAS、T14/T15、P2～P7 runtime。

## Decision

采用 digest identity 边界的 strict UTF-8 `CharsetEncoder(REPORT/REPORT)`，不改变 canonical JSON 对 malformed code unit 的显式转义合同，也不扩展公共 API。
