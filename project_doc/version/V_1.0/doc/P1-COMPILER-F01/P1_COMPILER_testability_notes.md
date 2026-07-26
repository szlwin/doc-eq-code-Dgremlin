# P1-COMPILER-F01 可测试性说明

> Revision：TEST-SEAM-R02-DRAFT。

## 1. 必须注入的接缝

- `DocumentSourceProvider`：内存、classpath、测试文件系统；
- `DocumentFrontend`：XML/YAML；
- `MixSourceResolver`：不直接调用全局 ClassLoader；
- `CompilerPass`：可单独执行并观察输入输出；
- `DigestStrategy`：固定版本；
- `Clock/Timer`：只用于指标，不进入语义摘要。

## 2. 实物 fixture 断言

对 `dec-demo/src/main/resources/mix` 固定以下基线：

- 文件数 10；
- Data 5；View 2；System 3；RuleView 14；BusinessScope 1；
- Information 16；Directory 5；Action 8；Produce 4；
- System→Rule 文件边 3；
- Root→Data/View/System/Business 边各一组。

数量只用于 fixture 回归；新增合法配置时需显式更新基线，不作为生产限制。

## 3. 失败注入

- 删除 `payment-rule.xml`；
- 重复 `SystemKey(order)`；
- 将 payment RuleView 的 `system` 改为 order；
- 将 business action 的 `rule-ref` 改为未知值；
- 使用 `classpath:../` 路径逃逸；
- 打乱目录枚举顺序；
- XML 加入外部实体；
- YAML 加入类型标签；
- 尝试修改 Registry 和 CoreConfigProjection。

## 4. 测试分层

1. 纯单测：Key、CanonicalNode、Raw builder、Diagnostic 排序、digest；
2. Compiler contract：内存 SourceGraph 和 Pass；
3. Fixture contract：真实 `mix`；
4. 架构测试：模块依赖和 parser/runtime 隔离；
5. 退役扫描：目录、POM、dependency tree、ServiceLoader、反射字符串和 artifact。
