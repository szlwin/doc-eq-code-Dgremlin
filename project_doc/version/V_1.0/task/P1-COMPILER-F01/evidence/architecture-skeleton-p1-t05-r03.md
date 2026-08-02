# TASK-P1-T05 / I003 — Architecture Skeleton Evidence

- Architecture Skeleton：`DEVSKEL-P1-T05-R03@05873e286c2d`
- Review：`REV-000248`
- Evidence：`EVD-000490`
- Head：`05873e286c2df64e426cb1a6f91c8c25488825cf`
- P0 Run：`30756121600`
- Artifact：`8835973306`
- Artifact SHA-256：`dceff4a14ad50c0227e0def81bfb13aa8fdae268116304bd4594504196b5bc72`
- Result：`PASSED SKELETON`

## 接缝

- `validateScalarBeforeCanonicalization(...)`：四个 scalar 位置共享统一入口；
- `requireScalarLength(...)`：只读取未经 trim 的原始值并执行单值上限；
- `requireAllowedScalarTag(...)`：仅在长度通过后执行 tag 与词法；
- `reserveCumulativeScalar(...)`：只对最终 Canonical 值更新累计预算；
- null 在长度和词法通过后才映射为空。

## 受控部分 GREEN

- I003 资源优先级 4/4 转绿；
- `scalar-per-node` 优先于合法及非法 typed 词法；
- 普通 scalar、`#text`、属性 value 和 Sequence item顺序一致；
- Java release 8 编译通过；
- Context 26/26、Compiler 83/83、XML 30/30、既有 YAML 45/45 继续通过。

## 保持 RED

I003 其余 8 项继续精确失败：

- 4 个位置的 Resolver float 正向；
- 4 个位置的非法进制整数负向。

因此 Skeleton 没有提前声明 Development 完成，Resolver-backed policy仍由后续阶段实现。
