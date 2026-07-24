# 信息树说明

本文说明 `doc-eq-code-Dgremlin` 中信息树的设计目的、XML 格式、识别与物化语义，以及目录 Action 产出数据与 Information 的对应关系。

本文以以下示例为主要依据：

```text
dec-demo/src/main/resources/directory/order/order-directory-new.xml
```

> 本文优先定义设计语言应表达的目标语义。示例中的部分元素和校验规则，当前 Java 引擎不一定已经完整实现。

## 1. 信息树解决什么问题

从信息角度看，业务可以理解为信息的识别、生产、消费、组合和变化。

信息树主要回答：

1. 系统中存在哪些稳定的业务信息；
2. 每个信息如何根据业务模型数据被识别；
3. 多个信息如何组合成更高层业务信息；
4. 哪些信息可以被系统自动物化；
5. Action 产生的数据是否作为信息继续参与后续业务；
6. 当产出数据作为信息使用时，它与哪个 Information 对应。

传统代码通常直接依赖字段和值：

```java
if (user.getStatus() == 1 && user.getCertified() == 1) {
    // 有效用户
}
```

信息树将其提升为稳定的业务概念：

```text
user.effective
    = user.activated
      AND user.certified
```

调用方只依赖 `user.effective`，不需要知道激活和认证具体使用哪些字段或状态值。

## 2. 信息树在整体模型中的位置

```text
数据模型
    定义字段及其与数据源的映射
        ↓
业务模型
    按业务视角组织数据对象和关系
        ↓
Information
    定义业务模型上成立的业务信息
        ↓
Directory
    根据业务信息进行分类、查询、执行和回退
        ↓
Action
    执行业务操作
        ↓
Produce
    声明实际产出数据，并在需要时映射回 Information
```

各层职责：

- 数据模型描述数据结构；
- 业务模型描述业务对象；
- Information 描述“什么业务信息成立”；
- Directory 描述“资源位于哪里、可以去哪里”；
- Action 描述“进入目录时执行什么”；
- Produce 描述“Action 必须产生什么数据”；
- `produce/@information-ref` 描述“该数据在信息树中对应哪个 Information”。

## 3. XML 文件整体结构

```xml
<?xml version="1.0" encoding="UTF-8"?>
<business-config name="order">

    <information-info>
        <information .../>
        <information ...>
            <change-data>...</change-data>
        </information>
    </information-info>

    <directory-info>
        ...
    </directory-info>

</business-config>
```

XML 基本要求：

| 项目 | 要求 |
|---|---|
| XML 版本 | 推荐使用 `1.0` |
| 字符编码 | 必须使用 `UTF-8` |
| 根元素 | `business-config` |
| 信息树容器 | `information-info` |
| 信息元素 | `information` |
| 大小写 | 元素名、属性名和引用名称均区分大小写 |
| 命名空间 | 当前示例未定义 XML Namespace |
| 未知元素或属性 | 编译器应拒绝，不能静默忽略 |

## 4. `information-info`

```xml
<information-info>
    <information .../>
    <information .../>
</information-info>
```

约束：

1. 一个 `business-config` 中最多存在一个 `information-info`；
2. `information-info` 中至少包含一个 `information`；
3. Information 名称在有效配置域内必须唯一；
4. 所有引用必须在编译期完成解析；
5. Information 依赖图不能存在直接或间接循环。

## 5. `information` 元素

### 5.1 属性表

| 属性 | 必填 | 适用类型 | 说明 |
|---|---:|---|---|
| `name` | 是 | 全部 | Information 的唯一业务名称 |
| `model-ref` | 条件必填 | 原子 Information | 引用识别或物化该信息的业务模型 |
| `rule-ref` | 条件必填 | 规则型原子 Information | 引用识别该信息的业务规则 |
| `rule-data` | 条件必填 | 数据表达式型原子 Information | 根据业务模型字段识别该信息 |
| `expression` | 条件必填 | 复合 Information | 使用其他 Information 组合当前信息 |

### 5.2 子元素表

| 子元素 | 数量 | 说明 |
|---|---:|---|
| `change-data` | `0..1` | 修改业务模型数据，使原子 Information 成立 |

### 5.3 三种合法定义形式

#### 业务规则型原子 Information

```xml
<information
        name="user.activated"
        model-ref="UserInfo"
        rule-ref="isActivated"/>
```

必须配置：

- `name`；
- `model-ref`；
- `rule-ref`。

不得同时配置：

- `rule-data`；
- `expression`。

#### 数据表达式型原子 Information

```xml
<information
        name="order.ordered"
        model-ref="OrderInfo"
        rule-data="
            order.status = 1
            and
            every(orderDetails, status = 1)
        ">

    <change-data>
        <![CDATA[
            order.status = 1;
            every(orderDetails, status = 1);
        ]]>
    </change-data>

</information>
```

必须配置：

- `name`；
- `model-ref`；
- `rule-data`。

可以配置：

- `change-data`。

不得同时配置：

- `rule-ref`；
- `expression`。

#### 复合 Information

```xml
<information
        name="user.effective"
        expression="
            user.activated
            and
            user.certified
        "/>
```

必须配置：

- `name`；
- `expression`。

不得配置：

- `model-ref`；
- `rule-ref`；
- `rule-data`；
- `change-data`。

`rule-ref`、`rule-data` 和 `expression` 是三种互斥的识别方式：

```text
recognizerCount =
    countNotEmpty(rule-ref, rule-data, expression)

require recognizerCount == 1
```

## 6. Information 的识别与物化

Information 包含两个不同方向的能力：

```text
业务模型数据 ──识别──> Information
Information ──物化──> 业务模型数据
```

### 6.1 识别

识别回答：

```text
当前业务信息是否成立？
```

识别方式：

- `rule-ref`；
- `rule-data`；
- `expression`。

### 6.2 物化

物化回答：

```text
如何修改业务模型数据，使该业务信息成立？
```

物化方式：

- 原子 Information 的 `change-data`；
- Action 引用的业务规则产生并保存数据。

能识别一个 Information，并不代表引擎一定知道如何自动产生它。

例如：

```xml
<information
        name="payment.hasResult"
        model-ref="OrderInfo"
        rule-ref="hasPayResult"/>
```

该 Information 可以识别 `OrderInfo` 中是否存在 `PayResult`，但真正创建 `PayResult` 的操作由目录 Action 完成。

## 7. Action 产出数据与 Information 的对应关系

### 7.1 Produce 的两层语义

`produce/@ref` 声明实际产生的数据：

```xml
<produce ref="PaymentInfo"/>
```

它只表达：

```text
Action 成功后必须产生 PaymentInfo
```

它不能表达：

```text
PaymentInfo 在信息树中对应哪个 Information
```

当该数据后续还要作为信息参与 Dependency、Directory、Information 表达式、自动分类或路径判断时，必须通过 `information-ref` 显式建立对应关系：

```xml
<produce
        ref="PaymentInfo"
        information-ref="payment.paymentInfo"/>
```

其中：

- `ref="PaymentInfo"`：实际产生的数据、模型节点或结果对象；
- `information-ref="payment.paymentInfo"`：该数据在信息树中对应的 Information。

对应的信息定义：

```xml
<information
        name="payment.paymentInfo"
        model-ref="OrderInfo"
        rule-ref="hasPaymentInfo"/>
```

Produce 因而可以同时具有两层后置条件：

```text
数据后置条件：
    PaymentInfo 已产生

信息后置条件：
    payment.paymentInfo 成立
```

### 7.2 `information-ref` 的条件必填规则

| 产出数据用途 | `information-ref` |
|---|---:|
| 仅作为内部临时数据 | 可省略 |
| 仅作为日志或记录数据 | 可省略 |
| 仅作为返回值且不参与后续业务判断 | 可省略 |
| 后续被 Dependency 引用 | 必填 |
| 后续被 Directory 引用 | 必填 |
| 后续参与 Information 表达式 | 必填 |
| 用于自动分类或路径选择 | 必填 |
| 用于判断流程能否继续 | 必填 |

判断原则：

```text
producedDataUsedAsInformation == true
    => produce.information-ref 必填
```

不是所有 Produce 都必须建立 Information。是否配置 `information-ref`，取决于产出数据是否具有后续业务信息语义。

### 7.3 运行时语义

带 `information-ref` 的 Produce 应按以下顺序执行：

```text
1. 执行 Action 引用的业务规则
2. 检查 ref 指定的数据已经产生
3. 将产出数据放入业务模型或执行上下文
4. 根据产出后的业务模型重新识别 information-ref
5. 要求对应 Information 成立
6. 重新计算依赖该 Information 的复合 Information
7. 允许后续 Dependency、Directory 或分类继续执行
```

失败条件：

- 规则执行成功，但 `ref` 数据不存在：Produce 失败；
- `ref` 数据存在，但 `information-ref` 无法识别为成立：Produce 失败；
- `information-ref` 不存在或模型上下文不兼容：编译失败；
- 数据后续作为信息使用却没有配置 `information-ref`：编译或流程校验失败。

### 7.4 PaymentInfo 示例

信息定义：

```xml
<information
        name="payment.paymentInfo"
        model-ref="OrderInfo"
        rule-ref="hasPaymentInfo"/>
```

目录 Action：

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

含义：

```text
pay 规则执行成功后：
    必须产生 PaymentInfo
    并且 payment.paymentInfo 必须成立
```

`PaymentInfo` 后续可以通过 `payment.paymentInfo` 被其他 Information、Dependency 或 Directory 使用，而不应再次直接判断底层数据是否存在。

### 7.5 PayResult 示例

信息定义：

```xml
<information
        name="payment.hasResult"
        model-ref="OrderInfo"
        rule-ref="hasPayResult"/>
```

目录 Action：

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

`PayResult` 与 `payment.hasResult` 建立对应关系后，引擎继续根据 PayResult 内容识别：

```text
payment.success
payment.error
```

再根据两者对 `success/error` 子目录进行自动分类。

### 7.6 仅作为数据使用的 PyaError

```xml
<produce-info>
    <produce ref="PyaError"/>
</produce-info>
```

`PyaError` 当前只是一条错误记录数据：

- 用于保存或返回支付错误详情；
- 不作为 Information 被 Dependency、Directory 或表达式引用；
- 不参与自动分类或路径判断。

因此不配置 `information-ref`。

这并不表示 `PyaError` 不能在未来成为信息。如果后续增加：

```xml
<information
        name="payment.hasErrorRecord"
        model-ref="OrderInfo"
        rule-ref="hasPyaError"/>
```

则对应 Produce 必须同步改为：

```xml
<produce
        ref="PyaError"
        information-ref="payment.hasErrorRecord"/>
```

## 8. Produce 与 Information 的映射约束

`produce/@information-ref` 应满足：

1. 必须引用已经定义的 Information；
2. 应引用可直接根据产出数据识别的原子 Information；
3. 被引用 Information 的 `model-ref` 必须与 Action 的业务模型上下文兼容；
4. Produce 数据与 Information 之间必须存在明确、可验证的业务对应关系；
5. Action 完成后必须能够重新识别该 Information；
6. 不允许为了通过校验而映射到无关 Information；
7. 一个 Produce 当前只声明一个主要对应 Information；
8. 更多衍生事实应由其他原子 Information 或 `expression` 推导；
9. 不应直接映射到 Action 单独无法保证成立的复合 Information。

正确示例：

```xml
<produce
        ref="PayResult"
        information-ref="payment.hasResult"/>
```

错误示例：

```xml
<produce
        ref="PayResult"
        information-ref="order.paySuccess"/>
```

`order.paySuccess` 是复合 Information：

```text
order.paySuccess
    = payment.success
      AND order.paySuccessStatus
```

单独产生 PayResult 不能保证订单成功状态已经物化，因此不能直接声明 `order.paySuccess` 已成立。

## 9. 示例中的主要 Information

| Information | 类型 | 业务含义 | 主要来源 | 可自动物化 |
|---|---|---|---|---:|
| `user.activated` | 原子 | 用户已经激活 | `isActivated` | 否 |
| `user.certified` | 原子 | 用户已经认证 | `isCertified` | 否 |
| `user.effective` | 复合 | 用户有效 | 激活且认证 | 否 |
| `order.ordered` | 原子 | 订单及明细已下单 | `rule-data` | 是 |
| `order.waitPay` | 原子 | 订单等待支付 | `isWaitPay` | 否 |
| `order.payable` | 复合 | 订单可以支付 | 已下单或等待支付 | 否 |
| `order.paying` | 原子 | 订单及明细支付中 | `rule-data` | 是 |
| `payment.paymentInfo` | 原子 | PaymentInfo 已产生并可作为信息使用 | `startPay` Produce | 否 |
| `payment.hasResult` | 原子 | PayResult 已产生 | `receivePayResult` Produce | 否 |
| `payment.success` | 原子 | PayResult 表示成功 | `isPaySuccess` | 否 |
| `payment.error` | 原子 | PayResult 表示失败 | `isPayError` | 否 |
| `payment.completed` | 复合 | 支付结果已经确定 | 成功或失败 | 否 |
| `order.paySuccessStatus` | 原子 | 订单状态已物化为成功 | `rule-data` | 是 |
| `order.paySuccess` | 复合 | 支付结果成功且订单状态已更新 | 结果成功且状态成功 | 否 |
| `order.payErrorStatus` | 原子 | 订单状态已物化为失败 | `rule-data` | 是 |
| `order.payError` | 复合 | 支付结果失败且订单状态已更新 | 结果失败且状态失败 | 否 |

主要关系：

```text
PaymentInfo
    └── Produce mapping ──> payment.paymentInfo

PayResult
    └── Produce mapping ──> payment.hasResult
                                ├── payment.success
                                └── payment.error

user.activated ──┐
                 ├── AND ──> user.effective
user.certified ──┘

order.ordered ──┐
                ├── OR ───> order.payable
order.waitPay ──┘

payment.success ────────┐
                        ├── AND ──> order.paySuccess
order.paySuccessStatus ─┘

payment.error ──────────┐
                        ├── AND ──> order.payError
order.payErrorStatus ───┘
```

## 10. 编译期校验

### 10.1 XML 结构

- 根元素和容器数量正确；
- 未知元素、属性被拒绝；
- 必填和条件必填属性完整；
- `rule-ref`、`rule-data`、`expression` 互斥；
- `change-data` 只用于可物化原子 Information。

### 10.2 引用

- `model-ref` 存在；
- `rule-ref` 存在；
- `expression` 中的 Information 存在；
- `produce/@information-ref` 存在；
- Directory、Dependency、Change 引用的 Information 存在。

### 10.3 产出映射

- 后续作为信息使用的数据必须配置 `information-ref`；
- 纯记录、日志或返回数据允许不配置；
- 映射应指向原子 Information；
- 数据与 Information 模型上下文兼容；
- Action 执行后能够验证该 Information；
- 不允许将单一数据产出直接映射到无法由其独立保证的复合 Information。

### 10.4 依赖图

- Information 不存在循环；
- 不存在未定义引用；
- 原子 Information 先于复合 Information 计算；
- 产出数据写入上下文后，受影响 Information 可以被重新计算。

## 11. 运行时校验

运行时至少应校验：

1. Action 是否成功；
2. 每个 `produce/@ref` 是否实际产生；
3. 产出数据是否写入正确业务模型或执行上下文；
4. 存在 `information-ref` 时，对应 Information 是否成立；
5. 依赖该 Information 的复合 Information 是否重新计算；
6. 后续 Directory 和 Dependency 是否使用更新后的信息结果。

## 12. 设计原则

1. Information 表示稳定的业务信息；
2. 新业务定义应新增 Information，不应改变旧定义；
3. `rule-data` 只能访问业务模型数据；
4. `expression` 只能组合 Information；
5. 识别和物化必须分离；
6. Action 负责执行操作；
7. Produce 负责声明数据后置条件；
8. 产出数据作为信息使用时，必须声明 `information-ref`；
9. 纯数据产出不应被强制包装成 Information；
10. Produce 映射原子 Information，复合事实由信息树继续推导；
11. Directory 只引用 Information，不复制底层字段判断。
