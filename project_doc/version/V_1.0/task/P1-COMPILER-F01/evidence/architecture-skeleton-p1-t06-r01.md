# TASK-P1-T06 Architecture Skeleton Evidence

- Review：`REV-000261`
- Evidence：`EVD-000503`
- Revision：`DEVSKEL-P1-T06-R01@6033e59728e7`
- Head：`6033e59728e75011f3baf89d68c2919bd5ffd947`
- P0 Run：`30788597060`
- Artifact：`8846182393`
- Artifact SHA-256：`d1c866350ab28c76335830e0b03e12e2c79959a4e64edb905aa2c94383a0d451`
- 结果：`PASSED AS SKELETON`

## 已冻结的架构接缝

新增：

- `RawDefinitionKind`
- `RawReference`
- `RawNodeBody`
- `RawDefinition`
- `RawDefinitionSet`
- `RawBuildStatus`
- `RawBuildResult`
- `RawDefinitionBuilder`

值对象构造、不可变集合、`equals/hashCode/toString` 和 `build(List<CanonicalDocumentNode>)` 接缝已经存在。Builder 仅返回受控 `raw.builder.not-implemented`，未提前加入结构提取伪实现。

## Skeleton RED

- 生产与测试源码 Java 8 编译成功；
- 既有 Compiler 测试保持通过；
- T06 11 个行为 Oracle 继续失败；
- Builder 失败不携带部分集合；
- JaCoCo 注入的 synthetic `$jacocoData` 触发了架构测试假阳性，后续测试已限定为只审查非 synthetic 业务字段；该问题不属于生产架构缺陷。
