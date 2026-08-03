# TASK-P1-T06 TDD RED Evidence

- Review：`REV-000260`
- Evidence：`EVD-000502`
- Revision：`TDD-P1-T06-R01@8c5efd3dcbee`
- Head：`8c5efd3dcbeea49b9a4e7a68e07aba5825e15618`
- P0 Run：`30788394890`
- Artifact：`8846107158`
- Artifact SHA-256：`0dd3625a723587d957640b518af5f6408ae0a695276e55e7f59f72268035f691`
- 结果：`PASSED AS RED`

## RED Oracle

`RawDefinitionBuilderContractTest` 通过反射加载生产 Raw API，因此生产类型尚不存在时：

- 测试源码按 Java release 8 编译成功；
- 不是 testCompile 失败；
- 11 个测试方法全部以缺失目标生产类型形成行为 RED；
- Failures：11；
- Errors：0。

覆盖：

1. 14 Kind 与连续 ordinal；
2. SourceRef、format、schemaVersion、属性、scalar 和有序 body；
3. raw references；
4. 嵌套定义引用边界；
5. 确定性和无状态；
6. unknown root；
7. unknown child；
8. 缺 name/owner；
9. null 输入；
10. 不可变集合；
11. parser 类型和 mutable registry 隔离。

## 回归控制

- Context 正常测试：26/26；
- 既有 Compiler 测试：83/83；
- 生产和测试源码 Java 8 编译通过；
- RED 精确归因于 T06 目标行为尚不存在。
