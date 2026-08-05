# DEVSKEL-P1-T13-R02 — 严格 UTF-8 Source identity 架构门禁

- Architecture ID：`DEVSKEL-P1-T13-R02@83c66072849c`
- Iteration：`TASK-P1-T13 / I002`
- Input Revision：`TDD-P1-T13-R02@83c66072849c`
- Design：`DESIGN-R46@P1-T13-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R42@P1-T13-REWORK-I002`
- Status：`PASSED`

## 1. 变更位置

生产代码只允许修改：

```text
dec-core-compiler/src/main/java/dec/core/compiler/compiled/CompilerDigestService.java
```

不新增公共 API，不修改 Source 模型构造器，不修改 semantic canonical JSON、Pipeline、Session、Publisher 或 Context。

## 2. 编码边界

新增 private static helper：

```text
strictUtf8(String value, String name)
```

职责：

1. 对参数执行 null 检查；
2. 每次调用创建独立 `CharsetEncoder`；
3. 设置 `onMalformedInput(REPORT)`；
4. 设置 `onUnmappableCharacter(REPORT)`；
5. 编码只读 `CharBuffer`；
6. 从返回 `ByteBuffer` 复制 remaining 字节；
7. 捕获 `CharacterCodingException`；
8. 抛出 `IllegalArgumentException(name + " must contain valid Unicode", cause)`。

使用点仅为：

```text
strictUtf8(checked.sourceId(), "sourceId")
```

## 3. 线程安全

`CharsetEncoder` 是有状态对象，禁止保存为 static 字段或跨调用共享。每次 identity 编码创建局部 Encoder，`CompilerDigestService` 继续保持无状态、可并发复用。

## 4. Digest 兼容性

保持以下字节合同不变：

- `DEC-SOURCE-DIGEST-V1` domain；
- domain 的四字节大端长度前缀；
- Source count；
- sourceId UTF-8 字节及长度前缀；
- content 原始字节及长度前缀；
- Unicode code point Source 排序；
- SHA-256 与小写 hex。

对于合法 Unicode，CharsetEncoder 产生的 UTF-8 必须与 `String.getBytes(StandardCharsets.UTF_8)` 完全一致，因此三组已知 vector 必须保持。

## 5. 错误合同

malformed high/low surrogate 必须在任何 digest update 前失败。稳定外层异常：

```text
IllegalArgumentException
message = sourceId must contain valid Unicode
cause = CharacterCodingException
```

不得：

- 使用替代字节；
- 丢弃非法 code unit；
- 自动修复/正规化 Source ID；
- 返回部分 digest；
- 修改 SourceManifest 或 DocumentSource。

## 6. Observer finding

P2 Finding 已由 RED 阶段新增控制测试冻结，当前实现无需生产修改。Architecture Gate 要求该测试持续证明：FAILED、原 ERROR、Observer Warning、publisher=0 和 empty artifacts 同时成立。

## 7. 代码规范

- 所有 `@Override` 独占一行；
- strict UTF-8 helper、Encoder 独立创建、ByteBuffer 复制和异常转换必须有中文注释；
- 不使用默认 Charset；
- 不使用共享 mutable encoder；
- 不引入反射或额外依赖。

## 8. Review stop conditions

- 生产文件超出一个；
- 合法 digest vector 变化；
- `REPLACE`/`IGNORE` 出现；
- Encoder 被 static 共享；
- CharacterCodingException 被吞掉；
- FAILED Observer 控制回归；
- T14/T15 范围出现。
