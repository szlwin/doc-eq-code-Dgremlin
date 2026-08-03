# TASK-P1-T06 / I002 — Development Evidence

- Development：`DEV-P1-T06-R02@aec3cd105b15`
- Review：`REV-000275`
- Evidence：`EVD-000517`
- Clean-code Head：`aec3cd105b15a302d8c1c91014c6c16529ef8c6a`
- P0 Run：`30793559695`
- Artifact：`8847970363`
- Artifact SHA-256：`922f8b7dc26245d6f0001ea1b6da86be05aed68ec21c1504634ec9f28ad64ae9`
- Result：`PASSED`

## 实现结果

### Lexical 来源事实

- Builder 的 `attribute`、`optionalAttribute`、owner context 只用 trim 判断空白并返回原始值；
- RawDefinition present optional 和 schemaVersion 保留原始值；
- RawReference role/target 保留原始值；
- RULE owner 使用原始 `system + "/" + ruleViewName`；
- PRODUCE owner 使用原始 `directory + "/" + action`；
- PRODUCE 可选纯空白 `ref` 映射为 absent，且不创建 RawReference；
- name、ownerToken、attributes、body attributes 和 reference target 的非空值保持一致。

### Public invariants

- RawDefinition public 构造器强制完整 14 Kind owner/name 矩阵；
- present-but-blank owner/name 在公开边界拒绝；
- RawBuildResult.failed 的每个 Diagnostic 必须为 ERROR、MIX_STRUCTURE_UNKNOWN、固定 pass；
- WARNING、INFO、错误 code、错误 pass 全部拒绝；
- RawDefinition.toString 输出全部 equals/hashCode 语义字段；
- RawDefinitionSet.toString 能表现 definition 语义差异。

### 两阶段验证与资源边界

- 第一阶段共享 `ValidationBudget`，跨全部文档累计节点数；
- 根深度从 1 开始，在进入任何其他递归逻辑前检查 depth/node count；
- 所有普通 reference 在第一阶段验证非空白并使用当前节点 SourceRef；
- MODEL_ACCESS 的空白 model-ref 优先返回 reference failure，而不是 name failure；
- 第二阶段只提取已验证输入；
- 后续 validate/extract/reference/body 递归深度最多 256；
- 不捕获 StackOverflowError，不使用真实栈溢出测试。

## 测试结果

- T06 Raw：31/31；
- Compiler：114/114；
- XML：30/30；
- YAML：59/59；
- Context 正常测试：26/26；
- Demo：4/4；
- Legacy declaration：1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 透明 Review 循环

- Head `62a4e0dea9d46748fe6c44ada163accb91e00964` 的 Run `30792835716` 首次全绿，Artifact `8847702631`、SHA-256 `b558e885190c6e63ed2e2e58c89336deb98dd881b72b23687f5ea737ad370786`；
- 独立 Review 增加 PRODUCE 可选空白 `ref` Oracle 后，Head `a343998ca1a475d202de5668c29a3bd14b64d805` 的 Run `30793094942` 精确出现 1 个行为失败，Artifact `8847792963`、SHA-256 `679e35c9de65b93ae782a779bf650b3e835177797edb4699750aa6015b568960`；
- 按 R24 特定规则修复并增加最终 Review Oracle 后，形成当前 clean-code Head。

## 范围与编码

- 未修改 Context、Canonical、XML、YAML、Source Graph 生产代码；
- 未实现 TypedKey、Symbol、Pipeline 或 T07；
- `@Override` 独占一行；公共方法、构造器和 lexical、矩阵、limits、reference、失败逻辑均使用中文注释。
