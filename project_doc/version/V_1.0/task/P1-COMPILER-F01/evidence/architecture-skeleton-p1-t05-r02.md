# TASK-P1-T05 / I002 — Architecture Skeleton Evidence

- Architecture Skeleton：`DEVSKEL-P1-T05-R02@122f8ddc37df`
- Review：`REV-000235`
- Evidence：`EVD-000478`
- Head：`122f8ddc37df4d8d1768929536146ec647327654`
- P0 Run：`30752458442`
- Artifact：`8834883206`
- Artifact SHA-256：`eb8d10c87a4f95e919cba70306a192e833085da75ebebb97c161fc0df4a6c621`
- Result：`PASSED`

## 冻结接缝

1. `decodeUtf8(byte[])`：在 parser 前执行严格 UTF-8 REPORT；
2. `YamlScalarLexemePolicy.isValid(Tag, String)`：独立无对象构造词法策略；
3. `requireAllowedScalarTag(...)`：tag 白名单与词法策略单一入口；
4. `requireName(...)`：Canonical 分配和 nodePath 拼接前执行 portable name policy；
5. 失败继续通过 `YamlUnsafeException` 发布稳定 `MIX_FRONTEND_YAML_UNSAFE`，无部分 root。

## 验证

- Context：26/26；
- Compiler：83/83；
- XML：30/30；
- YAML：43/43；
- Demo：4/4；
- legacy declaration：1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 透明中间记录

Head `2818ba66186f86d518648690cba80d48d8ce575d` 的 P0 Run `30752272973` 因仓库 SnakeYAML 2.2 不存在公开 `ScalarNode.isResolved()` 而在 YAML 生产编译失败。该运行不是有效 Skeleton Evidence；R21/R17 随后改用标准 tag 全量词法校验，并由本 Head 重新验证通过。
