# TASK-P1-T05 / I003 — Development Evidence

- Development：`DEV-P1-T05-R03@30529276cd8f`
- Review：`REV-000249`
- Evidence：`EVD-000491`
- Clean-code Head：`30529276cd8fa35e0eeeafb1256b85cb99820afb`
- P0 Run：`30756293074`
- Artifact：`8836020099`
- Artifact SHA-256：`3362ee5de19129f0a819bb1587e42552077618f7bf43b3011e15540ec0bcd688`
- Result：`PASSED`

## Scalar 两阶段资源门禁

1. 读取一次未经 trim 的 `ScalarNode.getValue()`；
2. `requireScalarLength` 执行原始单值长度门禁；
3. 通过后进入 tag 白名单与 Resolver 词法；
4. 合法 null 映射为空；
5. 非 null trim 后由 `reserveCumulativeScalar` 更新累计 Canonical 字符预算。

普通 scalar、`#text`、属性 value 和 Sequence item全部共享 `validateScalarBeforeCanonicalization`，超限输入不会进入 Resolver Pattern、timestamp 日期解析或其他词法派生处理。

## Resolver-backed policy

- 直接使用 SnakeYAML 2.2 `Resolver.BOOL`；
- 直接使用 `Resolver.INT`；
- 直接使用 `Resolver.FLOAT`；
- 直接使用 `Resolver.NULL` 与 `Resolver.EMPTY`；
- 直接使用 `Resolver.TIMESTAMP`；
- timestamp Pattern 匹配后追加 `LocalDate`、时间和 offset 范围校验；
- 删除项目复制的 bool/int/float/null/timestamp 接受正则；
- 删除 `BigDecimal`、`BigInteger` 及任意精度数值 construction；
- 不调用 `load` / `loadAs`，任意用户类型构造为 0。

## Oracle

- I003 end-to-end：12/12；
- Policy Architecture Review：2/2；
- `1e3`、`1.2e3`、显式 `!!float 1e3` 四位置通过；
- 显式 `!!int 0b_`、`0x_`、`0_` 四位置失败；
- 编译类常量池包含 Resolver，且不包含 BigDecimal/BigInteger；
- 代表性 int/float 结果与固定 Resolver Pattern直接比较一致。

## Clean-code 测试

- YAML：59/59；
- XML：30/30；
- Compiler：83/83；
- Context 正常测试：26/26；
- Demo：4/4；
- Legacy declaration：1/1；
- 故意失败门禁：1 项按预期失败并阻断；
- 12 模块 Reactor、Java release 8：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

Artifact 已独立下载解析，ZIP 实际 SHA-256 与 GitHub digest一致。

## 编码与范围

- 所有 `@Override` 独占一行；
- 新增方法、构造器和资源、Resolver、timestamp、安全逻辑使用中文注释；
- 未修改 Context 生产代码、compiler canonical 公共 API或 XML Frontend；
- 未启动 T06。
