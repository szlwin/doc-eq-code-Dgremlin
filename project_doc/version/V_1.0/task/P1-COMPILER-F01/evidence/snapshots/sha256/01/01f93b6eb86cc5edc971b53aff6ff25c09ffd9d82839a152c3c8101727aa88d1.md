# P1 编译骨架可测试性清单

## 成功路径
- XML 与 YAML 最小 Data/View/Rule fixture 归一化后语义相等。
- 前向引用在符号注册完成后可解析。
- 两个独立 EngineContext 同时存在且注册表互不影响。
- 同一输入重复编译得到相同语义 digest 和诊断顺序。

## 边界路径
- 空文档集、空文件、目录多文件确定性排序。
- 重复 Key、大小写差异、缺失可选位置、未知 schemaVersion。
- 相同实体名称位于不同强类型命名空间。
- 源位置缺失时仍保留路径指针并稳定排序。

## 失败路径
- 未知元素、未知属性、结构错误、未知引用和循环依赖形成 ERROR。
- 任一 ERROR 时不返回可发布 EngineContext。
- Legacy Adapter 的写操作被拒绝，不修改旧全局 Config 或新 Context。
- P2+ 未支持语义不得返回空成功；以明确 deferred/unsupported 诊断表示。

## 后续 Case 方向
- CASE-COMPILER-CANONICAL-EQUIVALENCE-001
- CASE-COMPILER-DUPLICATE-SYMBOL-001
- CASE-COMPILER-FORWARD-REFERENCE-001
- CASE-COMPILER-CONTEXT-ISOLATION-001
- CASE-COMPILER-DIAGNOSTIC-ORDER-001
- CASE-COMPILER-LEGACY-READONLY-001
