# DESIGN-R34 — TASK-P1-T10 I002 结构与资源边界返工

- Revision：`DESIGN-R34@P1-T10-REWORK-I002`
- Status：`PASSED`
- Supersedes：R33 的有效业务范围，不覆盖 R33 历史
- Base：PR #25 `f38644ee0497ae981619761b65d91be3ba0006fc`
- Review Input：独立 Review `NEEDS_CHANGES / REWORK`，Open P0/P1/P2=`0/4/1`
- Invalidated Completion：`COMPLETION-P1-T10-R01@9e94bc68d9a8`
- Owner：`dec-core-compiler / dec.core.compiler.modelaccess`

## 1. Rework goal

I002 关闭嵌入式通配符、多 `property-info`、ModelAccess 根结构一致性和 WRITE overlap 资源放大四个 P1，并把缺失 Oracle 转为阻断性回归。R01 的 Design、Plan、RED、Architecture、Completion、Review、CI 与 Artifact 作为不可变历史保留。

## 2. SharedModelPath grammar

```text
SharedModelPath := "*" | segment ("." segment)*
segment         := nonblank trimmed token excluding "*"
```

`*` 只能作为完整路径。`a.*`、`*.a`、`a.*.b`、`*.*` 必须在构造阶段拒绝，并由 Compiler 转为 `modelaccess.structure.invalid`。失败不得调用 resolver，不得发布 Binding 或 Deferred。

## 3. View property sections

同一 View 的所有直接 `property-info` section 共同组成根候选层，按文档顺序聚合其直接 `property`：

- 目标仅在第二或后续 section 时必须解析成功；
- 跨 section 同名候选为 ambiguous；
- 空 section 不屏蔽后续 section；
- 嵌套路径在命中根 property 后仍只遍历该 property 的直接子 property；
- `target-main` 始终先于全部 property section。

## 4. ModelAccess structural gate

在 owner、source View、access、resolver、Binding 和 Deferred 工作之前，单个 Raw ModelAccess 必须满足：

- `kind == MODEL_ACCESS`；
- `body.name == model-access`，root scalar 缺失；
- definition attributes 与 body attributes 完全一致；
- attributes 必须且只能包含非空白 `model-ref`；
- definition.name 必须存在，且与 `model-ref` lexical 完全一致；
- read/write 只能包含 `path`，不得有 scalar；
- ref 只能包含 `view` 与 `property`，不得有 scalar 或 child；
- root/read/write/ref 不得包含额外非法结构。

任何失败统一返回 `modelaccess.structure.invalid`。禁止从 `definition.name()` 回退补偿缺失 `model-ref`。根结构失败时 resolver 调用数必须为 0。

## 5. WRITE overlap resource contract

重叠检测使用 segment trie，复杂度按所有 WRITE segment 总数近似线性：

- 完整 `*` 单独作为全局路径；
- 插入途中遇到 terminal：已有祖先；
- 插入完成已有 terminal：完全重复；
- 插入完成存在 child：当前路径为祖先；
- prefix 必须以 segment 边界判断，不使用裸字符串前缀；
- 不允许 `W × (W-1) / 2` pair scan。

提供 package-private、每次 compilation 局部创建的 `WritePathOverlapIndex`。测试可读取不可变操作计数，用结构查询数量而非耗时阈值证明 N 条互不重叠路径不退化为 O(N²)。该对象不进入公共 API，不跨 compile 保存状态。

## 6. Selector resource review

property sibling 查找仍以完整 T06 节点预算为上界；I002 将所有 property-info 聚合一次后执行每段精确扫描，不引入跨调用缓存、全局状态或模糊索引。后续若 selector 规模出现独立预算需求，应新建 Revision，不在本次隐式扩展公共合同。

## 7. Atomic publication

任一结构、selector、duplicate 或 overlap ERROR 存在时，最终 Result 不携带 Compilation；因此无 Binding、无 Deferred。内部阶段也不得在根结构失败后调用 resolver。

## 8. Coding contract

- Java 8；
- 所有 `@Override` 独占一行；
- 方法、构造器、trie、结构门禁、聚合、失败与资源逻辑使用中文注释；
- 无权限执行、SQL、I/O、网络、缓存、DAG、运行时查询或全局状态；
- 不修改 Context、T06/T07/T08/T09 公共合同与 Compiler API。

## 9. TDD gate

有效 I002 RED 必须：测试源码 Java 8 编译；errors=0；旧正常测试保持绿色；新增失败只来自本 Revision Oracle。Architecture 阶段建立结构验证与 trie seam，但保持业务受控 RED。GREEN 后执行 T10 定向、T09/T08/T07、Compiler 全量、12 模块 P0、故意失败门禁、Artifact SHA 与 Surefire 独立解析。
