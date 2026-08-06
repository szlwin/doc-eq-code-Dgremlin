# CODEREVIEW-P1-T15-R02 — 退役门禁完整性独立 Review

- Review ID：`REV-000761`
- Code Review：`CODEREVIEW-P1-T15-R02@7c901332b8e5`
- Input：`DEV-P1-T15-R02@7c901332b8e5`
- Iteration：`TASK-P1-T15 / I002`
- Status：`PASSED`
- Open P0/P1/P2：`0 / 0 / 0`

## Historical invalidation

`FND-P1-T15-I001-002` 证明 I001 retirement gate 无法阻止子模块 POM 依赖、完整 Reactor 依赖闭包、class 常量池和中性 Artifact 内容回流。因此以下记录保留历史但由 I002 取代：

- `CODEREVIEW-P1-T15-R01@f36b03e6243`；
- `TESTING-P1-T15-R01@f36b03e6243`；
- `COMPLETION-P1-T15-R01@f36b03e6243`。

`TDD-P1-T15-R01@bff67b86fb55` 仍有效，不失效。

## Review scope

- 全部项目 POM 与 Reactor 模块发现；
- 逐模块 dependency tree 生成、归属验证和聚合证据；
- Source、反射字符串与 ServiceLoader 内容；
- `target/classes`、`target/test-classes` 的 class 常量池和编译资源；
- jar、war、zip 的路径、entry、class/资源内容及嵌套归档；
- 无法读取时 fail-closed；
- 七类 mutation、异常恢复和证据隔离；
- Java 8、既有 `@Override` 格式与中文注释。

## Review findings

### FND-P1-T15-I001-002 — P1 / GATE / RETIREMENT_INTEGRITY / EVIDENCE_INTEGRITY

Disposition：`CLOSED`。

关闭证据：

- 基线扫描 11 个 POM、11/11 Reactor dependency trees、947 个 class、205 个编译资源、10 个 Artifact 和 958 个 Artifact entry，0 违规；
- 每个 Reactor 模块具有独立 `.txt` 与 `.log`，状态 TSV 的 11 行均为 0；
- 每个独立报告必须包含自己的目标坐标，避免再次接受被其他模块覆盖的文件；
- mutation 扩展为 12 个模块并产生七类预期违规；
- 非 Demo POM、root profile/dependencyManagement、完整依赖树、反射资源、ServiceLoader、class 常量池和中性 ZIP 内容均有直接负向 Oracle；
- 恢复后 11/11 模块重新生成并再次 0 违规。

## Code review conclusions

- POM 扫描使用仓库实际文件枚举，不依赖预设模块名；
- Reactor 清单由 XML 模型递归解析，缺失 POM、坐标或 XML 解析失败均记录为扫描失败；
- Maven command、报告存在性和目标坐标三重验证均 fail-closed；
- class 与 Artifact 内容按二进制标识扫描，可覆盖 class 常量池中的点号/斜线包名；
- Archive entry 读取异常和嵌套归档读取异常均阻断；
- Mutation 清理只恢复本脚本备份的两个 POM 和本脚本创建的文件，不删除基线内容；
- summary 的六项 scope 均由真实计数和错误状态驱动；
- 未修改生产 Starter、Projection 或 Compiler 行为，I001 已通过的生产代码结论仍有效；
- 未新增 Java 注解；脚本方法和重要逻辑均有中文注释。

Review 结论：`PASSED / FND-P1-T15-I001-002 CLOSED / NO_OPEN_P0_P1_P2`，允许进入最终 Testing 与 Completion。
