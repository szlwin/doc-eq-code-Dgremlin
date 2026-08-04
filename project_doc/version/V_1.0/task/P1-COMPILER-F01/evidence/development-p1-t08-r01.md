# TASK-P1-T08 R01 Development Evidence

- TDD seam Head：`d7155c4f0bb1c930231671fa3041d532bd17a97f`
- Architecture Skeleton Head：`a063504eb209ba575f5e16d6f849a012a65d3f29`
- First GREEN Head：`82acc9a4350b2fdcae23b729f2dedb3ea52f837a`
- Clean-code Head：`ab432a3189f45c4267ce32af2e104bd39a8c79d1`
- Development：`DEV-P1-T08-R01@ab432a3189f4`
- Architecture：`DEVSKEL-P1-T08-R01@a063504eb209`

## 实现

新增 Java 8 不可变结果模型：`ReferenceResolutionStatus`、`ResolvedReference`、`ResolvedReferenceSet`、`ReferenceResolutionResult`。

`ReferenceResolver` 在完整 SymbolTable 上先建立单次调用只读索引，再解析 Connection、View、System、RuleView、Action、Directory 与 Produce：

- 成功路径只构造期望 TypedKey 并调用 `SymbolTable.find`；
- lexical 索引只用于 unknown/type mismatch/owner mismatch 分类；
- RuleView 以自身 ownerToken 绑定 System，并校验 System 的显式 View 声明；
- Action 以 system-ref 构造 RuleViewKey，不跨 System 使用同名规则；
- View property 只在当前 Data body 内精确校验，不建立 PropertyKey；
- Directory rel 限定同 BusinessScope，Information 使用 `system.name`；
- Diagnostic 与引用均使用 LinkedHashSet 完整聚合、确定性去重；
- 失败不发布部分 ResolvedReferenceSet。

## 失败 attempt 保留

1. 临时源码快照 Run `30825465063`：tar 输出位于工作树导致目录变化，REJECTED；修复后 Run `30825542883` 成功，最终工作流已删除；
2. 首个 TDD attempt Head `2bbf42a57d22...` / Run `30827030425`：9 failures / 3 errors，Optional.get 与夹具顺序错误，REJECTED；
3. 有效 RED Head `d7155c4f0bb1...` / Run `30827276340`：9 failures / 0 errors，PASSED；
4. Architecture Skeleton Head `a063504eb209...` / Run `30827946835`：9 controlled failures / 0 errors，PASSED；
5. First GREEN `82acc9a4350b...` / Run `30828282846`：SUCCESS；
6. 删除临时 workflow 后 clean-code `ab432a3189f4...` / Run `30828498760`：SUCCESS。

## 编码与范围

- 所有新增/修改 `@Override` 独占一行；
- 方法、构造器与关键索引、Role Policy、owner、Diagnostic、资源和失败逻辑使用中文注释；
- 未修改 Context、T06 Raw、T07 Symbol 公共合同；
- 未实现 T09、T10 或 P2～P7；
- 无 I/O、反射、运行时执行、static mutable registry 或模糊搜索。

- `REV-000346` — DevelopmentSpecificationReview — PASSED；
- `REV-000347` — EngineeringStandardsReview — PASSED；
- `REV-000348` — ArchitectureFinalReview — PASSED；
- `REV-000349` — SecurityReview — PASSED；
- Evidence：`EVD-000593`～`EVD-000596`。
