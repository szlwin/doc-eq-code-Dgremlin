# TASK-P1-T05 / I002 — Design / Plan Evidence

- Evidence：`EVD-000476`
- Review：`REV-000232`、`REV-000233`
- Design：`DESIGN-R21@P1-T05-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R17@P1-T05-REWORK-I002`
- Rework Base：`52fe48d46dd2c4ac9c822d5be141d47c03ae955f`
- Dependency：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- Result：`PASSED`

## 冻结决策

1. 原始 byte[] 必须使用 `CharsetDecoder + REPORT` 严格 UTF-8 解码；
2. 标准 scalar tag 同时执行 tag 白名单与无对象构造词法校验；
3. 合法显式和隐式 typed scalar 保留原始词法；
4. 非法 null/int/bool/float/timestamp 稳定失败且不发布 root；
5. 节点名和属性名使用 `[A-Za-z_][A-Za-z0-9._-]*`；
6. nodePath 在路径拼接前完成 segment 校验；
7. R20 安全、资源和 Canonical 主映射合同不回退；
8. 不修改 Context、compiler canonical 公共 API、XML 生产语义；
9. 不启动 T06；
10. I001 Completion R01 与全部历史记录不可变保留。

## 兼容性决策

仓库 SnakeYAML 2.2 不公开 `ScalarNode.isResolved()`。R21/R17 已选择 Review 允许的“标准 tag 全量词法校验”方案，不读取私有状态、不反射、不执行通用对象 construction。
