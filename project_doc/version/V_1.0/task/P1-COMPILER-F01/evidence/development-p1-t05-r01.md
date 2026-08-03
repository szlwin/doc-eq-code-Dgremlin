# TASK-P1-T05 Development Evidence R01

- Revision：`DEV-P1-T05-R01@040f09b80463`
- Review：`REV-000223`
- Evidence：`EVD-000467`
- Production GREEN Head：`68e48e49085def7c707ab294e4e259da9bd015ea`
- Clean-code / Review Oracle Head：`040f09b80463911c092e7693f47814f3904758fd`
- P0 Run：`30750632160`
- Artifact：`8834325522`
- Artifact SHA-256：`dc5bb0b3c4d1505f7f418c34042eb0071e1c770fc5cda489476cc76e91eb576c`
- Result：`PASSED`

## 生产实现

- `SafeYamlDocumentFrontend` 只使用 `SafeConstructor` 和 `composeAll` 表示树，不调用 `load`、`loadAs` 或用户 Bean 构造；
- `LoaderOptions` 关闭 duplicate/recursive key，alias 上限为零，并设置 code point、nesting depth 与不可信 tag inspector；
- Canonical 遍历再次验证允许 tag、anchor metadata、共享对象身份和递归图，形成双重 fail-closed 边界；
- Java/object/local/custom、binary/set/omap/pairs、anchor/alias、merge、复杂/重复 key 和错误文档形状均受控失败；
- YAML 根、`@attributes`、`#text`、普通子节点和 Sequence 重复子节点按照 R20 映射；
- Mark 转换为一基 SourceRef，Mapping 子节点使用 key Mark，Sequence item 使用 item Mark；
- 所有节点继承显式 schemaVersion；
- 文档、code point、深度、节点、路径、Mapping、Sequence、单 scalar、累计 scalar 预算完整实现；
- 累计路径与 scalar 使用 `long` 溢出安全加法；
- 完整根构建成功前不发布部分 Canonical；不捕获 `OutOfMemoryError`，测试不制造真实 OOM。

## 测试增强

在 Development GREEN 后新增 8 项独立 Review Oracle，覆盖独立 anchor、无 alias merge、标准危险 tag、保留 key 类型、嵌套 Sequence、非法预算配置、生产深度 129 和标准 scalar/null。YAML 专项由 27 项增至 35 项。

## 中间兼容性修正

Head `868f58b66428...` 的 Run `30750420184` 仅因仓库实际 SnakeYAML API 不支持 `LoaderOptions.setMergeOnCompose` 而编译失败。该调用已移除；merge key 继续由 Canonical 遍历显式拒绝。此中间运行不作为最终 GREEN Evidence。

## 范围

仅修改 YAML 模块、T05 测试与流程文档；未修改 `dec-core-context` 生产代码、compiler canonical 公共 API或 XML Frontend 生产语义，未实现 T06。
