# TASK-P1-T05 / I003 — Design / Plan Evidence

- Design：`DESIGN-R22@P1-T05-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R18@P1-T05-REWORK-I003`
- Review：`REV-000245`、`REV-000246`
- Evidence：`EVD-000488`
- Rework Base：`499b977a773da3e25b776d4debf7abb1391b5192`
- Dependency：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- Result：`PASSED`

## 冻结合同

1. 原始 scalar 长度门禁先于 regex、日期和数值处理；
2. 单值预算与最终 Canonical 累计预算分离；
3. 普通 scalar、`#text`、属性 value 和 Sequence item共享同一入口；
4. bool/int/float/null/timestamp 直接使用 SnakeYAML 2.2 `Resolver` 公开 Pattern；
5. timestamp 在 Resolver 词法后追加真实日期、时间和时区检查；
6. 不创建 `BigDecimal`、`BigInteger` 或 Java 业务对象；
7. `1e3`、`1.2e3` 和显式 `!!float 1e3` 必须通过；
8. 显式 `!!int 0b_`、`0x_`、`0_` 必须失败；
9. R22/R18 在 RED 前创建并锁定，后续不得原地修改；
10. R21/R17/R02 保留为不可变历史；
11. 严格 UTF-8、portable name、R20 安全与资源合同不回退；
12. 不修改 Context、compiler canonical API、XML Frontend，不启动 T06。

## Revision Lock

- Design first commit：`ab9ca21cf668aba03f030129022458bbd46304fc`；
- Design blob：`b8ffb41226866b0854def9d4ce12a6c68c150b3b`；
- Plan first commit：`a2283a8661210e0ebda26a67fad05a60d770a89b`；
- Plan blob：`26adb13c7192e5f7419c59acf445bf8b56b6ceb7`；
- Resolver release commit：`a34989252e6f59e36a3aaf788a903b7a37a73d33`；
- Resolver source blob：`b641960b3669991b555159fd85ce2fba42f76f53`。

clean-code Head `30529276cd8fa35e0eeeafb1256b85cb99820afb` 重新读取后，R22/R18 blob 与上述锁定值完全一致。
