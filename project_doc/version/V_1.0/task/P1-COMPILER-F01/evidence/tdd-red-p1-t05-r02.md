# TASK-P1-T05 / I002 — TDD RED Evidence

- TDD：`TDD-P1-T05-R02@c362011eac56`
- Review：`REV-000234`
- Evidence：`EVD-000477`
- Head：`c362011eac56079fdbc7431d8e07420870aabd0b`
- P0 Run：`30752101877`
- Artifact：`8834769356`
- Artifact SHA-256：`bf504630c638f35cdef9b6cdb5d7433c31d76c8ef78a8371566377c71c2d75d7`
- Result：`PASSED RED`

## RED 结果

- Context：26/26 PASSED；
- Compiler：83/83 PASSED；
- XML：30/30 PASSED；
- I001 YAML：35/35 PASSED；
- I002 Source Facts：8 run / 6 expected failures / 0 errors / 2 positive controls passed；
- Java release 8 生产与测试源码编译：PASSED；
- 下游 Reactor 因受控 RED 在 YAML 模块停止，不声明通过。

## 六个预期失败

1. 原始 byte[] 非法 UTF-8；
2. 普通 scalar 的非法 typed 词法；
3. `#text` 的非法 typed 词法；
4. `@attributes` value 的非法 typed 词法；
5. Sequence item 的非法 typed 词法；
6. `/`、换行、冒号和数字开头名称。

## RED 可信性

- 测试直接构造原始 byte[]，未通过 Java String 预先合法化；
- 每个负向场景先验证同一生产 Frontend 能解析安全控制样本；
- 显式 `!!str` 和合法 portable nodePath 在 RED 阶段已作为正向控制通过；
- 六个失败精确对应 Review Findings，不是编译错误或无关回归。
