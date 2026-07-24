# 目录说明

本文说明 `doc-eq-code-Dgremlin` 中目录的设计目的、核心概念、配置方式、查询语义和执行语义。

本文以以下示例为主要依据：

```text
dec-demo/src/main/resources/directory/order/order-directory-new.xml
```

该示例描述了订单从已下单、支付中、支付结果，到支付成功或支付失败的业务目录结构。

> 本文首先定义设计语言应表达的语义。示例中的部分元素属于目标设计，当前 Java 引擎不一定已经完整实现。

## 1. 目录解决什么问题

传统业务代码通常使用状态值表达业务阶段：

```java
order.setStatus(2);
```

查询时也直接使用状态值：

```sql
select * from order_info where status = 2
```

这种方式存在以下问题：

- 状态值缺少业务含义；
- 流程、判断、操作和状态变化分散在代码中；
- 产品、设计、开发和测试难以使用同一种语言沟通；
- 状态含义变化后，需要修改大量代码；
- 很难统一表达路径补齐、业务回退、分类查询和结果契约。

目录将业务对象看作一种资源，并使用业务目录描述资源：

- 当前属于什么业务阶段或分类；
- 可以继续进入哪些目录；
- 进入目录前需要满足哪些业务事实；
- 进入时需要执行哪些操作；
- 操作后必须产生哪些数据；
- 进入后需要物化什么业务状态；
- 如何返回上一级目录并执行补偿操作。

调用方可以使用业务语言：

```text
execute("success")
find("PayResult")
find("ordered").start("ordered").end("success")
```

而不必直接依赖 `status = 1、2、3` 等技术细节。

## 2. 目录在整体模型中的位置

目录依赖信息树，但不替代信息树：

```text
业务模型
    提供业务对象及数据结构
        ↓
Information
    定义对象上成立的业务事实
        ↓
Directory
    使用业务事实定义分类、路径和进入条件
        ↓
Action / Rule
    执行具体业务操作
        ↓
Produce / Change
    校验产出并物化业务状态
```

职责划分如下：

- Information 回答“什么业务事实成立”；
- Directory 回答“资源位于哪里、可以去哪里”；
- Dependency 回答“进入前必须满足什么”；
- Action 回答“进入过程中执行什么”；
- Produce 回答“执行后必须产生什么”；
- Change 回答“进入后需要物化什么事实”；
- Back 回答“如何沿目录关系返回并补偿”。

## 3. 示例目录结构

`order-directory-new.xml` 中的目录结构为：

```text
ordered
└── paying
    └── PayResult
        ├── success
        └── error
```

各目录含义如下：

| 目录 | Information | 业务含义 |
|---|---|---|
| `ordered` | `order.ordered` | 订单及订单明细处于已下单状态 |
| `paying` | `order.paying` | 订单及订单明细处于支付中状态 |
| `PayResult` | `payment.hasResult` | 当前订单已经存在支付结果 |
| `success` | `order.paySuccess` | 支付结果成功且订单状态已物化为成功 |
| `error` | `order.payError` | 支付结果失败且订单状态已物化为失败 |

这里同时存在两类关系：

1. `ordered → paying → PayResult` 表示业务执行路径；
2. `PayResult → success/error` 表示支付结果的业务分类。

## 4. Directory 的基本定义

目录定义在 `directory-info` 中：

```xml
<directory-info>
    <directory ...>
        ...
    </directory>
</directory-info>
```

目录示例：

```xml
<directory
        name="ordered"
        information-ref="order.ordered"
        model-ref="OrderInfo"
        is-root="true">
    ...
</directory>
```

主要属性如下。

### 4.1 `name`

目录的唯一名称，例如：

```text
ordered
paying
PayResult
success
error
```

名称应使用稳定的业务语言，不应使用数据库状态值。

### 4.2 `information-ref`

定义该目录对应的业务事实：

```xml
information-ref="order.paying"
```

属于该目录的数据必须满足此 Information。

`information-ref` 同时可作为目录默认的状态物化目标，具体规则见“状态转变”。

### 4.3 `model-ref`

定义目录操作的主要业务模型：

```xml
model-ref="OrderInfo"
```

目录中的依赖判断、Action、Change 和最终验证，应在明确的业务模型上下文中执行。

### 4.4 `is-root`

表示目录树的根目录：

```xml
is-root="true"
```

示例中 `ordered` 是订单目录的根。

一个可独立查询或执行的目录树应有明确根目录。加载时应校验：

- 根目录存在；
- 同一目录树不能存在多个冲突根；
- 除根目录外，每个目录应能从根到达；
- 目录关系不能形成非法循环。

## 5. Subdirectory：子目录关系

子目录通过 `subdirectory-info` 声明：

```xml
<subdirectory-info>
    <subdirectory rel="paying"/>
</subdirectory-info>
```

`rel` 引用另一个目录名称，并建立父子关系。

示例中：

```text
ordered 的直接子目录是 paying
paying 的直接子目录是 PayResult
PayResult 的直接子目录是 success 和 error
```

父子关系可承担两类语义。

### 5.1 执行路径

```text
ordered
    ↓
paying
    ↓
PayResult
```

执行目标目录时，引擎可以从当前目录或指定起点解析到目标目录的路径，并按顺序执行中间目录。

例如：

```text
execute("PayResult")
```

在订单当前只处于 `ordered` 时，可以推导：

```text
ordered
    → paying
    → PayResult
```

每个目录的依赖、Action、Produce、Change 和最终 Information 校验都应按顺序执行。

### 5.2 业务分类

```text
PayResult
    ├── success
    └── error
```

`success` 和 `error` 是 `PayResult` 的两个业务分类。

因此：

```text
find("PayResult")
```

应覆盖两个子目录中的数据，而：

```text
find("success")
```

只查询支付成功的数据。

## 6. `any-one` 与互斥分类

示例中：

```xml
<subdirectory
        rel="success"
        information-ref="payment.success"
        any-one="true"/>

<subdirectory
        rel="error"
        information-ref="payment.error"
        any-one="true"/>
```

其业务含义是：

- `success` 与 `error` 至少应命中一个；
- 支付结果确定后，只能归入其中一个分类。

`any-one="true"` 表示同组子目录至少满足一个。

互斥语义可以通过以下方式保证：

1. `payment.success` 与 `payment.error` 的识别规则本身互斥；
2. 编译器对分类 Information 执行互斥校验；
3. DSL 如保留 `mutual-exclusion` 属性，可显式声明互斥目录。

当前示例 XML 只显式配置了 `any-one="true"`，但注释定义了“必须且只能命中一个”的业务要求。因此引擎不能只校验“至少一个”，还应校验不能同时命中。

建议的归类规则为：

```text
matched = 所有 information-ref 成立的 any-one 子目录

require matched.size >= 1
require matched.size == 1      // 当前支付结果分类要求互斥
```

## 7. Dependency：进入目录的前提条件

目录依赖通过 `dependency-info` 定义：

```xml
<dependency-info>
    <dependency information-ref="order.payable"/>
    <dependency information-ref="user.effective"/>
</dependency-info>
```

示例中进入 `paying` 必须同时满足：

```text
order.payable
AND
user.effective
```

即：

- 订单必须已经下单或处于等待支付状态；
- 用户必须已经激活并完成认证。

多个 `dependency` 默认按 AND 处理。任何一个依赖不成立，当前目录都不能继续执行。

Dependency 应引用 Information，而不应直接复制底层字段判断：

不推荐：

```xml
<dependency rule-data="order.status = 1 and user.status = 2"/>
```

推荐：

```xml
<dependency information-ref="order.payable"/>
<dependency information-ref="user.effective"/>
```

这样业务判断只有一个定义来源。

## 8. Action：进入目录时执行的操作

Action 通过 `action-info` 定义：

```xml
<action-info>
    <action
            name="startPay"
            ref-rule="pay">
        ...
    </action>
</action-info>
```

主要属性如下：

- `name`：目录内的操作名称；
- `ref-rule`：引用实际执行的业务规则。

Action 表达的是业务操作，不应直接绑定某个 Java 类或数据库实现。

示例中的 Action：

| 目录 | Action | 规则 | 作用 |
|---|---|---|---|
| `paying` | `startPay` | `pay` | 发起支付并创建 PaymentInfo |
| `PayResult` | `receivePayResult` | `receivePayResult` | 接收外部支付回调并创建 PayResult |
| `success` | `confirmPaySuccess` | `confirmPaySuccess` | 完成支付成功后的业务处理 |
| `error` | `recordPyaError` | `recordPyaError` | 记录支付失败信息 |

同一个目录可以配置多个 Action。除非显式定义并行语义，否则应按文档顺序串行执行。

## 9. Produce：Action 的结果契约

Action 可以通过 `produce-info` 声明执行后必须产生的数据：

```xml
<produce-info>
    <produce ref="PaymentInfo"/>
</produce-info>
```

Produce 不是“调用哪个方法”的描述，而是执行结果契约。

例如 `startPay`：

```xml
<action
        name="startPay"
        ref-rule="pay">
    <produce-info>
        <produce ref="PaymentInfo"/>
    </produce-info>
</action>
```

其含义是：

```text
pay 规则执行成功后，当前业务上下文必须新增 PaymentInfo。
```

如果规则返回成功但没有产生 `PaymentInfo`，Action 仍应视为失败。

其他示例：

```text
receivePayResult
    必须产生 PayResult

recordPyaError
    必须产生 PyaError
```

Produce 可用于：

- 运行时后置条件校验；
- 自动化测试；
- 流程继续执行的输入；
- 幂等和重试判断；
- 设计文档与代码实现的一致性检查。

## 10. Change：进入目录后的状态物化

目录通过 `change-info` 声明进入后需要物化的 Information。

### 10.1 默认 Change

如果目录没有显式配置 `change-info`，默认规则为：

```text
directory.change-info
    = directory.information-ref
```

例如 `ordered`：

```xml
<directory
        name="ordered"
        information-ref="order.ordered">
</directory>
```

进入 `ordered` 时默认物化 `order.ordered`。

`paying` 同理，默认物化：

```text
order.paying
```

默认 Change 只有在目录的 `information-ref` 是可物化的原子 Information 时才有效。

### 10.2 显式 Change

如果目录的最终 Information 是复合 Information，则必须显式指定可物化的原子 Information。

例如 `success` 最终要求：

```text
order.paySuccess
    = payment.success
      AND order.paySuccessStatus
```

`order.paySuccess` 是复合 Information，没有 `change-data`，因此配置：

```xml
<change-info
        information-ref="order.paySuccessStatus"/>
```

执行顺序为：

1. `payment.success` 已由 PayResult 判断成立；
2. 执行 `confirmPaySuccess`；
3. 物化 `order.paySuccessStatus`；
4. 验证完整的 `order.paySuccess` 成立；
5. 订单最终属于 `success` 目录。

`error` 目录同理：

```xml
<change-info
        information-ref="order.payErrorStatus"/>
```

## 11. 目录最终验证

进入目录不能只以 Action 没有抛出异常作为成功条件。

目录执行完成后，应重新计算目录的 `information-ref`：

```text
Action 成功
    ↓
Produce 契约满足
    ↓
Change 完成
    ↓
重新识别 directory.information-ref
    ↓
成立后，目录执行成功
```

例如 `success`：

```text
require payment.success
execute confirmPaySuccess
materialize order.paySuccessStatus
require order.paySuccess
```

只有最后的 `order.paySuccess` 成立，才表示真正进入支付成功目录。

## 12. PayResult 的执行和自动分类

进入 `PayResult` 的完整过程可以描述为：

```text
1. 执行 receivePayResult
2. 校验必须产生 PayResult
3. 识别 payment.hasResult
4. 计算 payment.success
5. 计算 payment.error
6. 根据子目录 information-ref 进行分类
7. 校验 success/error 至少命中一个
8. 校验 success/error 不能同时命中
9. 自动继续执行命中的子目录
```

支付成功时：

```text
PayResult
    → payment.success 成立
    → 自动进入 success
    → confirmPaySuccess
    → 物化 order.paySuccessStatus
    → 验证 order.paySuccess
```

支付失败时：

```text
PayResult
    → payment.error 成立
    → 自动进入 error
    → recordPyaError
    → 校验产生 PyaError
    → 物化 order.payErrorStatus
    → 验证 order.payError
```

## 13. Back：沿父子关系返回

`back` 定义在父目录与子目录的关系中：

```xml
<subdirectory rel="PayResult">
    <back name="returnPaying">
        <action-info>
            <action
                    name="resetPayResult"
                    ref-rule="resetPayResult"/>
        </action-info>
    </back>
</subdirectory>
```

因为该配置位于 `paying → PayResult` 的父子关系中，所以来源和目标可以直接推导：

```text
PayResult
    --returnPaying-->
paying
```

执行 Back 时：

1. 确认当前对象曾进入或正处于子目录 `PayResult`；
2. 执行 `resetPayResult`；
3. 校验 Back Action 的结果；
4. 执行 Back 的显式 Change；
5. 如果没有显式 Change，默认物化父目录 `paying` 的 `information-ref`；
6. 验证 `order.paying` 成立；
7. 将当前业务位置返回 `paying`。

本例中默认恢复：

```text
order.paying
```

### 13.1 多级 Back

如果当前目录为 C，要返回祖先目录 A：

```text
A
└── B
    └── C
```

引擎必须按相邻父子关系顺序执行：

```text
C → B 的 Back
B → A 的 Back
```

不能直接跳过 B，也不能只执行最终 A 的状态变化。

这样可以保证每一级补偿、清理和状态恢复都被执行。

## 14. Execute 的路径解析

目录执行可以支持：

```text
execute(target)
execute(target).start(startDirectory)
```

推荐执行流程如下。

### 14.1 确定起点

起点可来自：

1. 用户显式指定的 `start`；
2. 当前对象已经匹配的最深目录；
3. 当前目录树的根目录。

### 14.2 查找路径

从起点到目标目录查找唯一或可选择路径：

```text
ordered → paying → PayResult → success
```

如存在多条路径，必须：

- 通过 Information 和 Dependency 消除无效路径；
- 或要求调用方指定起点、分支或操作；
- 不能随机选择路径。

### 14.3 顺序执行节点

每个节点按照以下阶段执行：

```text
DEPENDENCY
    ↓
ACTION
    ↓
PRODUCE VALIDATION
    ↓
CHANGE
    ↓
INFORMATION VALIDATION
    ↓
SUBDIRECTORY CLASSIFICATION
```

任一阶段失败，应停止后续执行，并根据事务、重试或补偿策略处理。

## 15. Find 的查询语义

目录查询应将业务目录转换为 Information 条件，而不是直接暴露状态值。

### 15.1 查询单个目录

```text
find("success")
```

应使用：

```text
order.paySuccess
```

查询完整支付成功订单。

### 15.2 查询父目录

```text
find("PayResult")
```

父目录查询覆盖其有效子目录：

```text
success OR error
```

同时应满足父目录自身 Information：

```text
payment.hasResult
```

概念条件为：

```text
payment.hasResult
AND
(order.paySuccess OR order.payError)
```

具体查询优化可以由编译器和数据源插件完成。

### 15.3 查询范围

```text
find("ordered")
    .start("ordered")
    .end("success")
```

表示查询从已下单到支付成功路径范围内的数据。

范围可展开为：

```text
ordered
OR paying
OR PayResult
OR success
```

但最终条件不能简单拼接固定状态值，应根据每个目录引用的 Information 生成。

### 15.4 附带关联模型

```text
find("ordered")
    .with("user")
    .start("ordered")
    .end("success")
```

表示在查询订单目录范围时，同时加载关联用户业务模型。

`with` 应引用业务模型或业务关系，不应直接暴露表连接实现。

## 16. 直接进入子目录

调用方可能直接请求：

```text
execute("success")
```

即使不是通过 `PayResult` 自动分类进入，也必须执行相同校验：

1. 目标目录可从当前目录到达；
2. `payment.success` 依赖成立；
3. 所有必经中间目录已经完成或需要自动补齐；
4. 执行 `confirmPaySuccess`；
5. 物化 `order.paySuccessStatus`；
6. 验证 `order.paySuccess`。

直接调用不能绕过 Dependency、Action、Produce、Change 或 Information 校验。

## 17. 目录不是普通状态机

目录与传统有限状态机有相似之处，但范围更广。

传统状态机主要描述：

```text
状态 + 事件 → 新状态
```

目录还描述：

- 业务分类和包含关系；
- 目录范围查询；
- Information 依赖；
- Action 执行；
- Produce 结果契约；
- Change 状态物化；
- 自动路径补齐；
- 结果自动分类；
- 多级 Back；
- 关联业务模型查询。

因此目录更接近：

```text
业务资源的分类树
+
业务执行路径
+
业务事实约束
+
可执行设计文档
```

## 18. 编译期校验建议

加载目录配置时，建议至少执行以下校验：

1. Directory 名称全局唯一；
2. `model-ref` 指向的业务模型必须存在；
3. `information-ref` 指向的 Information 必须存在；
4. `subdirectory.rel` 指向的目录必须存在；
5. 根目录必须存在且定义明确；
6. 不允许非法父子循环；
7. 从根目录应能到达所有非独立目录；
8. 一个目录存在多个父目录时，语义必须明确；
9. `dependency` 引用的 Information 必须存在；
10. `action.ref-rule` 引用的业务规则必须存在；
11. `produce.ref` 引用的数据或模型必须存在；
12. 默认 Change 引用的 Information 必须可物化；
13. 显式 `change-info` 必须引用可物化的原子 Information；
14. Back 必须定义在有效父子关系中；
15. 多级 Back 路径中的每一级都必须可执行；
16. `any-one` 子目录组至少包含两个候选目录；
17. 互斥分类的 Information 不能同时成立；
18. 目标目录不能通过路径执行绕过必需 Dependency。

## 19. 运行期执行记录建议

每次目录执行建议记录：

- 执行编码；
- 根目录；
- 起点和目标目录；
- 解析出的完整路径；
- 每个 Dependency 的判断结果；
- 每个 Action 的输入、输出和执行结果；
- Produce 契约校验结果；
- Change 前后的数据变化；
- 目录 Information 最终判断结果；
- 自动分类结果；
- Back 执行路径；
- 事务、重试和补偿信息。

这样可以让目录设计同时成为：

- 业务文档；
- 执行计划；
- 自动化测试依据；
- 审计记录结构；
- 故障定位依据。

## 20. 示例完整执行过程

订单支付成功的完整过程如下：

```text
当前：ordered

1. 请求进入 paying
   - 校验 order.payable
   - 校验 user.effective
   - 执行 startPay / pay
   - 校验产生 PaymentInfo
   - 物化 order.paying
   - 验证 order.paying

2. 外部支付结果进入 PayResult
   - 执行 receivePayResult
   - 校验产生 PayResult
   - 验证 payment.hasResult
   - 计算 payment.success
   - 计算 payment.error

3. 自动分类为 success
   - 校验 payment.success
   - 执行 confirmPaySuccess
   - 物化 order.paySuccessStatus
   - 验证 order.paySuccess

最终：success
```

订单支付失败的过程如下：

```text
当前：ordered

1. 进入 paying
2. 接收 PayResult
3. payment.error 成立
4. 自动分类为 error
5. 执行 recordPyaError
6. 校验产生 PyaError
7. 物化 order.payErrorStatus
8. 验证 order.payError

最终：error
```

支付结果需要重置时：

```text
当前：PayResult / success / error

1. 解析返回 paying 的 Back 路径
2. 执行 resetPayResult
3. 清理或重置支付结果
4. 默认物化 order.paying
5. 验证 order.paying

最终：paying
```

## 21. 设计原则总结

目录设计应遵循以下原则：

1. 使用业务目录名称，不使用状态值作为公共接口；
2. Directory 通过 Information 定义业务含义；
3. Dependency 只引用业务事实，不复制底层字段判断；
4. Action 描述业务操作，具体实现由规则和引擎提供；
5. Produce 是强制结果契约，不是说明性注释；
6. Change 负责物化原子 Information；
7. 复合 Information 不能直接物化；
8. 进入目录后必须重新验证目录 Information；
9. 父目录查询应覆盖其有效子目录；
10. 自动分类必须校验至少命中和互斥约束；
11. Back 必须沿相邻父子关系逐级执行；
12. 直接进入目录不能绕过中间路径和门禁；
13. 配置应同时服务业务、开发、测试和运行审计；
14. 目录负责表达设计，引擎负责解释执行，不应为每份目录生成大量业务代码。
