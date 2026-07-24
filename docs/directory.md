# 目录说明

本文说明 `doc-eq-code-Dgremlin` 中目录的设计目的、XML 格式、查询与执行语义，以及 Action 产出数据与信息树 Information 的对应关系。

本文以以下示例为主要依据：

```text
dec-demo/src/main/resources/directory/order/order-directory-new.xml
```

> 本文优先定义设计语言应表达的目标语义。示例中的部分元素和校验规则，当前 Java 引擎不一定已经完整实现。

## 1. 目录解决什么问题

传统业务代码通常使用状态值表达业务阶段：

```java
order.setStatus(2);
```

查询时直接使用：

```sql
select * from order_info where status = 2
```

这种方式存在以下问题：

- 状态值缺少稳定的业务含义；
- 流程、判断、操作和状态变化分散在代码中；
- 产品、设计、开发和测试难以使用同一种语言沟通；
- 很难统一表达路径补齐、回退、分类查询和结果契约；
- Action 产生的数据与后续业务信息之间缺少明确关系。

目录将业务对象看作资源，并使用业务目录描述：

- 当前属于什么业务阶段或分类；
- 可以继续进入哪些目录；
- 进入目录前需要满足哪些 Information；
- 进入时执行哪些 Action；
- Action 后必须产生哪些数据；
- 哪些产出数据还要作为 Information 使用；
- 产出数据与哪个 Information 对应；
- 进入后需要物化什么业务信息；
- 如何沿目录关系返回并执行补偿操作。

调用方可以使用业务语言：

```text
execute("success")
find("PayResult")
find("ordered").start("ordered").end("success")
```

而不必直接依赖 `status = 1、2、3`。

## 2. 目录在整体模型中的位置

```text
业务模型
    提供业务对象及数据结构
        ↓
Information
    定义对象上成立的业务信息
        ↓
Directory
    使用业务信息定义分类、路径和进入条件
        ↓
Action
    执行具体业务操作
        ↓
Produce
    校验数据产出，并在需要时映射到 Information
        ↓
Change
    物化目录最终业务状态
```

职责划分：

- Information：什么业务信息成立；
- Directory：资源位于哪里、可以去哪里；
- Dependency：进入前必须满足什么；
- Action：进入过程中执行什么；
- Produce：执行后必须产生什么数据；
- `produce/@information-ref`：产出数据在信息树中对应哪个 Information；
- Change：进入后物化什么信息；
- Back：如何沿目录关系返回并补偿。

## 3. 示例目录结构

```text
ordered
└── paying
    └── PayResult
        ├── success
        └── error
```

| 目录 | Information | 业务含义 |
|---|---|---|
| `ordered` | `order.ordered` | 订单及明细处于已下单状态 |
| `paying` | `order.paying` | 订单及明细处于支付中状态 |
| `PayResult` | `payment.hasResult` | 当前订单已经存在支付结果 |
| `success` | `order.paySuccess` | 支付成功且订单状态已物化为成功 |
| `error` | `order.payError` | 支付失败且订单状态已物化为失败 |

其中：

1. `ordered → paying → PayResult` 是业务执行路径；
2. `PayResult → success/error` 是支付结果分类；
3. `startPay` 产生 `PaymentInfo`，并映射为 `payment.paymentInfo`；
4. `receivePayResult` 产生 `PayResult`，并映射为 `payment.hasResult`；
5. `recordPyaError` 产生 `PyaError`，但当前只作为记录数据，不映射 Information。

## 4. XML 文件整体结构

```xml
<?xml version="1.0" encoding="UTF-8"?>
<business-config name="order">

    <information-info>
        ...
    </information-info>

    <directory-info>
        <directory ...>
            <subdirectory-info>...</subdirectory-info>
            <dependency-info>...</dependency-info>
            <action-info>...</action-info>
            <change-info .../>
        </directory>
    </directory-info>

</business-config>
```

XML 基本要求：

| 项目 | 要求 |
|---|---|
| XML 版本 | 推荐使用 `1.0` |
| 字符编码 | 必须使用 `UTF-8` |
| 根元素 | `business-config` |
| 目录容器 | `directory-info` |
| 目录元素 | `directory` |
| 大小写 | 元素、属性和引用均区分大小写 |
| 未知元素或属性 | 编译器应拒绝，不能静默忽略 |

`directory` 内推荐固定顺序：

```text
subdirectory-info
    ↓
dependency-info
    ↓
action-info
    ↓
change-info
```

每一部分均可省略，但出现时应保持顺序，便于解析、XSD 校验和人工阅读。

## 5. `directory-info`

```xml
<directory-info>
    <directory .../>
    <directory .../>
</directory-info>
```

约束：

1. 一个 `business-config` 中最多存在一个 `directory-info`；
2. `directory-info` 中至少包含一个 `directory`；
3. 目录名称在有效配置域内必须唯一；
4. 所有目录、Information、Rule 和 Produce 引用必须在编译期解析；
5. 编译器必须构建完整目录图并校验路径。

## 6. `directory`

### 6.1 属性表

| 属性 | 必填 | 说明 |
|---|---:|---|
| `name` | 是 | 目录唯一业务名称 |
| `information-ref` | 是 | 进入目录完成后必须成立的 Information |
| `model-ref` | 是 | 目录操作使用的主要业务模型 |
| `is-root` | 否 | 是否为目录树根，默认 `false` |

### 6.2 子元素表

| 子元素 | 数量 | 说明 |
|---|---:|---|
| `subdirectory-info` | `0..1` | 定义直接子目录、分类和 Back |
| `dependency-info` | `0..1` | 定义进入前置 Information |
| `action-info` | `0..1` | 定义进入目录时执行的 Action |
| `change-info` | `0..1` | 定义进入后物化的 Information |

基本示例：

```xml
<directory
        name="ordered"
        information-ref="order.ordered"
        model-ref="OrderInfo"
        is-root="true">

    <subdirectory-info>
        <subdirectory rel="paying"/>
    </subdirectory-info>

</directory>
```

进入目录完成后，引擎必须验证 `directory/@information-ref` 成立。

## 7. `subdirectory-info` 与 `subdirectory`

### 7.1 属性表

| 属性 | 必填 | 说明 |
|---|---:|---|
| `rel` | 是 | 引用直接子目录 |
| `information-ref` | 否 | 当前父子关系下的分类条件 |
| `any-one` | 否 | 同组子目录是否至少命中一个 |
| `mutual-exclusion` | 否 | 与当前子目录互斥的目录名称 |

### 7.2 执行路径

```xml
<subdirectory-info>
    <subdirectory rel="paying"/>
</subdirectory-info>
```

表示：

```text
ordered → paying
```

### 7.3 分类关系

```xml
<subdirectory-info>

    <subdirectory
            rel="success"
            information-ref="payment.success"
            mutual-exclusion="error"
            any-one="true"/>

    <subdirectory
            rel="error"
            information-ref="payment.error"
            mutual-exclusion="success"
            any-one="true"/>

</subdirectory-info>
```

表示：

- `success` 与 `error` 至少命中一个；
- 两者不能同时命中；
- `payment.success` 成立时归入 `success`；
- `payment.error` 成立时归入 `error`。

支付结果分类应满足：

```text
matched.size == 1
```

不能只校验至少命中一个，还必须校验互斥。

## 8. `dependency-info`

```xml
<dependency-info>
    <dependency information-ref="order.payable"/>
    <dependency information-ref="user.effective"/>
</dependency-info>
```

多个 `dependency` 默认按 AND 处理：

```text
order.payable
AND
user.effective
```

Dependency 必须引用 Information，不能复制底层字段判断。

不推荐：

```xml
<dependency rule-data="order.status = 1 and user.status = 2"/>
```

推荐：

```xml
<dependency information-ref="order.payable"/>
<dependency information-ref="user.effective"/>
```

## 9. `action-info` 与 `action`

```xml
<action-info>

    <action
            name="startPay"
            ref-rule="pay">
        ...
    </action>

</action-info>
```

`action` 属性：

| 属性 | 必填 | 说明 |
|---|---:|---|
| `name` | 是 | 当前目录中的业务操作名称 |
| `ref-rule` | 是 | 引用实际执行业务规则 |

同一目录可以有多个 Action。未声明并行语义时，按 XML 顺序串行执行。

示例中的 Action：

| 目录 | Action | 规则 | 作用 |
|---|---|---|---|
| `paying` | `startPay` | `pay` | 发起支付并产生 PaymentInfo |
| `PayResult` | `receivePayResult` | `receivePayResult` | 接收支付结果并产生 PayResult |
| `success` | `confirmPaySuccess` | `confirmPaySuccess` | 完成支付成功处理 |
| `error` | `recordPyaError` | `recordPyaError` | 记录支付错误并产生 PyaError 数据 |

## 10. `produce-info` 与 `produce`

### 10.1 Produce 的职责

```xml
<produce-info>
    <produce ref="PaymentInfo"/>
</produce-info>
```

`produce` 首先表示 Action 成功后必须产生的数据后置条件。

它不自动表示该数据已经成为信息树中的 Information。数据和 Information 是两个不同层次：

```text
ref
    指向实际产生的数据、模型节点或结果对象

information-ref
    指向该数据在信息树中对应的 Information
```

### 10.2 属性表

| 属性 | 必填 | 说明 |
|---|---:|---|
| `ref` | 是 | Action 必须产生的数据、模型节点或结果对象 |
| `information-ref` | 条件必填 | 该产出数据在信息树中对应的 Information |

### 10.3 何时必须配置 `information-ref`

只配置：

```xml
<produce ref="PaymentInfo"/>
```

只能表达：

```text
Action 必须产生 PaymentInfo
```

不能表达：

```text
PaymentInfo 后续以哪个 Information 名称参与业务
```

当产出数据后续还要被以下结构使用时，必须建立对应关系：

- Information 表达式；
- Dependency；
- Directory；
- 子目录自动分类；
- 执行路径选择；
- 后续流程门禁。

格式：

```xml
<produce
        ref="PaymentInfo"
        information-ref="payment.paymentInfo"/>
```

对应信息树定义：

```xml
<information
        name="payment.paymentInfo"
        model-ref="OrderInfo"
        rule-ref="hasPaymentInfo"/>
```

### 10.4 条件必填规则

| 产出用途 | `information-ref` |
|---|---:|
| 仅作为内部临时数据 | 可省略 |
| 仅作为日志或记录数据 | 可省略 |
| 仅作为返回结果，不参与后续判断 | 可省略 |
| 后续被 Information 引用 | 必填 |
| 后续被 Dependency 引用 | 必填 |
| 后续被 Directory 引用 | 必填 |
| 用于自动分类或路径选择 | 必填 |
| 用于判断后续流程能否执行 | 必填 |

```text
producedDataUsedAsInformation == true
    => produce.information-ref 必填
```

不是所有 Produce 都必须映射 Information。纯数据结果不应被强制包装成信息。

### 10.5 PaymentInfo：数据后续作为 Information 使用

```xml
<action
        name="startPay"
        ref-rule="pay">

    <produce-info>
        <produce
                ref="PaymentInfo"
                information-ref="payment.paymentInfo"/>
    </produce-info>

</action>
```

执行语义：

```text
1. 执行 pay
2. 验证 PaymentInfo 已产生
3. 将 PaymentInfo 放入 OrderInfo 或执行上下文
4. 识别 payment.paymentInfo
5. 要求 payment.paymentInfo 成立
6. 重新计算依赖它的其他 Information
7. 允许后续目录和依赖继续执行
```

Produce 同时形成两层后置条件：

```text
数据后置条件：
    PaymentInfo 已产生

信息后置条件：
    payment.paymentInfo 成立
```

### 10.6 PayResult：产生数据并驱动分类

```xml
<action
        name="receivePayResult"
        ref-rule="receivePayResult">

    <produce-info>
        <produce
                ref="PayResult"
                information-ref="payment.hasResult"/>
    </produce-info>

</action>
```

执行后先确认：

```text
payment.hasResult
```

随后根据 PayResult 内容重新识别：

```text
payment.success
payment.error
```

最后自动归类到：

```text
success XOR error
```

### 10.7 PyaError：仅作为记录数据

```xml
<action
        name="recordPyaError"
        ref-rule="recordPyaError">

    <produce-info>
        <produce ref="PyaError"/>
    </produce-info>

</action>
```

`PyaError` 当前只是一条支付错误记录：

- 需要被 Action 创建并保存；
- 可作为查询或返回数据；
- 不作为 Information 被后续业务引用；
- 不参与 Dependency、Directory、表达式、分类或路径判断。

因此只配置 `ref`，不配置 `information-ref`。

若未来把它提升为业务信息，例如：

```xml
<information
        name="payment.hasErrorRecord"
        model-ref="OrderInfo"
        rule-ref="hasPyaError"/>
```

则 Produce 必须同步调整：

```xml
<produce
        ref="PyaError"
        information-ref="payment.hasErrorRecord"/>
```

### 10.8 映射约束

1. `produce/@ref` 必须可以解析；
2. `produce/@information-ref` 存在时，必须引用已定义 Information；
3. 映射应指向可根据该产出直接识别的原子 Information；
4. Information 的模型上下文必须与 Action 兼容；
5. Action 执行后必须能够验证对应 Information；
6. 不允许把纯记录数据强制映射到无关 Information；
7. 一个 Produce 当前只声明一个主要对应 Information；
8. 其他衍生事实由信息树继续识别或通过 `expression` 推导；
9. 不应直接映射到 Action 单独无法保证成立的复合 Information。

错误示例：

```xml
<produce
        ref="PayResult"
        information-ref="order.paySuccess"/>
```

`order.paySuccess` 除支付结果成功外，还要求订单状态完成成功物化。单独产生 PayResult 不能保证该复合信息成立。

正确示例：

```xml
<produce
        ref="PayResult"
        information-ref="payment.hasResult"/>
```

## 11. `change-info`

显式物化：

```xml
<change-info
        information-ref="order.paySuccessStatus"/>
```

表示目录 Action 和 Produce 完成后，物化 `order.paySuccessStatus`。

默认物化规则：

```text
如果 directory 没有 change-info：
    尝试物化 directory/@information-ref
```

但仅当该 Information 具有 `change-data` 时才能自动物化。

例如：

- `order.paying` 有 `change-data`，可以自动物化；
- `payment.hasResult` 没有 `change-data`，必须由 `receivePayResult` Action 产生 PayResult；
- `payment.paymentInfo` 没有 `change-data`，必须由 `startPay` Action 产生 PaymentInfo。

## 12. `back`

Back 定义在父目录指向子目录的关系上：

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

含义：

```text
PayResult → paying
```

执行顺序：

1. 执行 Back Action；
2. 清理或重置子目录产生的数据和相关 Information；
3. 物化父目录 Information；
4. 验证父目录 Information 成立；
5. 返回父目录。

多级 Back 必须逐级执行。

例如：

```text
C → B → A
```

从 C 返回 A 时：

```text
先执行 C → B 的 Back
再执行 B → A 的 Back
```

不能直接跳过中间目录的补偿动作。

## 13. 目录执行语义

进入一个目录时，推荐顺序：

```text
1. 解析从当前目录到目标目录的路径
2. 校验当前步骤的 Dependency
3. 按顺序执行 Action
4. 校验每个 produce/@ref 数据已经产生
5. 存在 produce/@information-ref 时，识别对应 Information
6. 重新计算受影响的其他 Information
7. 执行 Change
8. 验证目标目录 directory/@information-ref
9. 根据 subdirectory 信息进行自动分类
10. 记录执行结果
```

### 13.1 进入 `paying`

```text
依赖：
    order.payable
    user.effective

Action：
    startPay

Produce 数据：
    PaymentInfo

Produce Information：
    payment.paymentInfo

Change：
    order.paying

最终验证：
    order.paying
```

### 13.2 进入 `PayResult`

```text
Action：
    receivePayResult

Produce 数据：
    PayResult

Produce Information：
    payment.hasResult

重新识别：
    payment.success
    payment.error

自动分类：
    success XOR error
```

### 13.3 进入 `error`

```text
依赖：
    payment.error

Action：
    recordPyaError

Produce 数据：
    PyaError

Produce Information：
    无

Change：
    order.payErrorStatus

最终验证：
    order.payError
```

`PyaError` 的产生是数据后置条件，但不是目录完成所需的独立 Information。目录最终仍通过 `order.payError` 验证支付失败业务状态。

## 14. 直接进入目录的规则

外部直接执行：

```text
execute("success")
```

不能绕过：

- 从当前目录到 `success` 的中间路径；
- `paying` 的 Dependency；
- `startPay` 的 Action 与 Produce；
- `PayResult` 的 Action 与 Produce；
- `payment.success` 依赖；
- `success` 的 Change 和最终 Information 校验。

直接指定目标目录只表示执行目标，不表示跳过过程。

## 15. 查询语义

### 15.1 查询父目录

```text
find("PayResult")
```

查询范围应覆盖：

```text
success
error
```

### 15.2 查询单个分类

```text
find("success")
```

只查询支付成功数据。

### 15.3 查询路径范围

```text
find("ordered")
    .start("ordered")
    .end("success")
```

应根据目录路径及其 Information 生成业务查询条件，而不是把目录名称直接替换为固定状态值。

### 15.4 关联模型

```text
find("ordered")
    .with("UserInfo")
    .start("ordered")
    .end("success")
```

`with` 表示查询时需要关联其他业务模型。

## 16. 编译期校验

### 16.1 XML 结构

- 元素顺序正确；
- 未知元素和属性被拒绝；
- 必填和条件必填属性完整；
- 容器数量有效。

### 16.2 目录图

- 根目录存在；
- 非根目录可以从根到达；
- 不存在非法环；
- `rel` 目标存在；
- 多父目录路径不存在无法消解的歧义。

### 16.3 引用

- `information-ref` 存在；
- `model-ref` 存在；
- `ref-rule` 存在；
- Produce 的 `ref` 可以解析；
- Produce 的 `information-ref` 可以解析。

### 16.4 Produce 映射

- 后续作为 Information 使用的数据必须配置映射；
- 纯日志、记录或返回数据允许不配置映射；
- 映射指向可直接验证的原子 Information；
- 数据与 Information 模型上下文兼容；
- Action 执行后能够验证该 Information；
- 不允许把单一数据产出直接映射到无法由其独立保证的复合 Information。

### 16.5 分类

- `any-one` 组至少命中一个；
- `mutual-exclusion` 引用存在；
- 互斥目录不能同时成立；
- 当前支付结果分类必须且只能命中 `success/error` 之一。

### 16.6 Back

- Back 只定义在有效父子关系上；
- Back Action 引用有效；
- 返回后的父目录 Information 可恢复；
- 多级 Back 路径完整。

## 17. 运行时校验

运行时至少应校验：

1. Dependency 是否全部成立；
2. Action 是否成功；
3. `produce/@ref` 数据是否实际产生；
4. 存在 `produce/@information-ref` 时，对应 Information 是否成立；
5. 纯数据 Produce 是否写入正确位置；
6. Change 是否成功；
7. 目标目录 Information 是否成立；
8. 分类是否唯一；
9. Back 后父目录 Information 是否恢复；
10. 执行记录是否包含路径、Action、Produce、Information 和 Change 结果。

## 18. 完整关键片段

```xml
<information
        name="payment.paymentInfo"
        model-ref="OrderInfo"
        rule-ref="hasPaymentInfo"/>

<directory
        name="paying"
        information-ref="order.paying"
        model-ref="OrderInfo">

    <dependency-info>
        <dependency information-ref="order.payable"/>
        <dependency information-ref="user.effective"/>
    </dependency-info>

    <action-info>
        <action
                name="startPay"
                ref-rule="pay">

            <produce-info>
                <produce
                        ref="PaymentInfo"
                        information-ref="payment.paymentInfo"/>
            </produce-info>

        </action>
    </action-info>

</directory>
```

与纯数据产出对比：

```xml
<action
        name="recordPyaError"
        ref-rule="recordPyaError">

    <produce-info>
        <produce ref="PyaError"/>
    </produce-info>

</action>
```

二者区别：

```text
PaymentInfo
    是数据
    后续还作为业务信息使用
    因此必须映射 payment.paymentInfo

PyaError
    是数据
    当前只作为错误记录使用
    因此不配置 information-ref
```

## 19. 设计原则

1. Directory 使用业务语言，不直接暴露状态值；
2. Directory 通过 Information 判断和验证业务状态；
3. Dependency 只引用 Information；
4. Action 执行业务操作，不承担目录路径定义；
5. Produce 声明实际数据产出；
6. 数据后续作为信息使用时，Produce 必须声明 `information-ref`；
7. 纯数据产出不应被强制定义为 Information；
8. Change 负责物化目录状态；
9. Back 沿相邻目录关系逐级执行；
10. 查询和执行使用同一套目录与 Information 语义。
