# TASK-P1-T05 / I001 — 安全 YAML Canonical Frontend

- 状态：`IN_PROGRESS`
- Base：`dev_all@09edf814bdf0800e7e9633545ca743200169b377`
- Branch：`feature/p1-t05-yaml-canonical-20260802-2106`
- Dependency：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- Design：`DESIGN-R20@P1-T05-I001`
- Plan：`TP-P1-COMPILER-F01-R16@P1-T05-I001`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 冻结目标

1. 新增 compiler-owned `SafeYamlDocumentFrontend`；
2. 使用安全表示树，不执行任意 Java/object 构造；
3. 拒绝 custom tag、anchor、alias、递归结构、merge、复杂/重复 key；
4. 冻结文档、code point、深度、节点、路径、Mapping、Sequence 和 scalar 预算；
5. YAML 到 Canonical 映射仅使用 `@attributes`、`#text`、子节点与重复节点；
6. SourceRef 保留 YAML key/item 的一基行列和完整 nodePath；
7. 等价 XML/YAML 产生相同语义树，同时保留各自格式和物理来源；
8. 所有失败返回 `FAILED / MIX_FRONTEND_YAML_UNSAFE / empty root`；
9. 不修改 Context 生产代码、compiler canonical 公共 API或 XML 生产语义；
10. 不启动 T06 或后续任务。

## 生命周期门禁

- TDD RED 必须可编译，且只因目标 Frontend/limits 接缝或行为缺失而失败；
- Architecture Skeleton 只建立模块依赖、limits、构造器和参数失败边界；
- Development 必须使安全、资源、Canonical parity、SourceRef 和架构 Oracle 全绿；
- Specification、Architecture、Security、Code、TDD 五类独立 Review 均需 PASSED；
- Context、Compiler、XML、YAML、Demo、12 模块 Reactor、Java 8 和故意失败门禁均需通过；
- 开放 P0/P1 必须为 0；
- `@Override` 独占一行，公共方法、构造器和重要逻辑使用中文注释；
- 完成后创建独立 PR，未经明确授权不得合并。
