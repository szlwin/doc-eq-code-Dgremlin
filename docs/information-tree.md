# 信息树说明

本文说明 `doc-eq-code-Dgremlin` 中信息树的设计目的、核心概念、XML 格式和执行语义。

本文以以下示例为主要依据：

```text
dec-demo/src/main/resources/directory/order/order-directory-new.xml
```

该示例描述了用户有效性、订单状态、支付结果，以及订单支付成功或失败等业务事实。

> 本文首先定义设计语言应表达的语义。示例中的部分元素属于目标设计，当前 Java 引擎不一定已经完整实现。

## 1. 信息树解决什么问题

从信息角度看，业务可以理解为信息的生产、消费、识别、组合和变化。

信息树主要回答两个问题：

1. 系统需要识别或生产哪些业务信息；
2. 每种业务信息依赖哪些数据或其他业务信息。

传统代码经常直接判断底层字段：

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

调用方只依赖 `user.effective`，不需要知道激活和认证具体使用哪些字段、状态值或规则实现。

## 2. 信息树在整体模型中的位置

```text
数据模型
    定义数据字段及其与数据源的映射
        ↓
业务模型
    按业务视角组织数据对象与关系
        ↓
信息树
    定义业务模型上成立的业务事实
        ↓
目录
    根据业务事实进行分类、查询、执行和状态迁移
```

各层职责应保持分离：

- 数据模型不负责定义完整业务含义；
- 业务模型不负责描述完整业务流程；
- Information 不负责描述目录路径；
- Directory 不应直接使用数据库状态值替代 Information。

## 3. XML 文件整体结构

信息树定义在业务配置文件的 `information-info` 元素中。

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

### 3.1 XML 基本要求

| 项目 | 要求 |
|---|---|
| XML 版本 | 推荐使用 `1.0` |
| 字符编码 | 必须使用 `UTF-8` |
| 根元素 | `business-config` |
| 信息树容器 | `information-info` |
| 信息定义元素 | `information` |
| 大小写 | 元素名、属性名和引用名称均区分大小写 |
| 命名空间 | 当前示例未定义 XML Namespace |
| 文本表达式 | 多行或包含 `<`、`>`、`&` 时建议使用 CDATA |

### 3.2 `business-config`

```xml
<business-config name="order">
    ...
</business-config>
```

| 属性 | 必填 | 说明 |
|---|---:|---|
| `name` | 是 | 当前业务配置的唯一名称，例如 `order` |

同一业务配置中可以同时定义信息树和目录。目录通过 `information-ref` 引用信息树中的 Information。

### 3.3 `information-info`

```xml
<information-info>
    <information .../>
    <information .../>
</information-info>
```

`information-info` 是 Information 的集合容器。

约束：

1. 一个 `business-config` 中最多存在一个 `information-info`；
2. `information-info` 中至少包含一个 `information`；
3. 同一有效配置域中的 Information 名称必须唯一；
4. Information 可以引用同一文件或编译上下文中其他文件定义的 Information；
5. 编译器必须在运行前完成全部引用解析。

## 4. `information` 元素格式

### 4.1 属性表

| 属性 | 必填 | 适用类型 | 说明 |
|---|---:|---|---|
| `name` | 是 | 全部 | Information 的唯一业务名称 |
| `model-ref` | 条件必填 | 原子 Information | 引用识别或物化该信息的业务模型 |
| `rule-ref` | 条件必填 | 规则型原子 Information | 引用用于识别该信息的业务规则 |
| `rule-data` | 条件必填 | 数据表达式型原子 Information | 根据业务模型字段识别该信息 |
| `expression` | 条件必填 | 复合 Information | 使用其他 Information 组合当前信息 |

### 4.2 子元素表

| 子元素 | 数量 | 说明 |
|---|---:|---|
| `change-data` | `0..1` | 修改业务模型数据，使原子 Information 成立 |

### 4.3 三种合法定义形态

一个 Information 应选择以下三种定义形态之一。

#### A. 业务规则型原子 Information

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

#### B. 数据表达式型原子 Information

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

#### C. 复合 Information

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

### 4.4 属性互斥规则

`rule-ref`、`rule-data` 和 `expression` 是三种不同的识别方式，必须互斥。

```text
合法：只配置 rule-ref
合法：只配置 rule-data
合法：只配置 expression
非法：rule-ref + rule-data
非法：rule-ref + expression
非法：rule-data + expression
```

编译器建议使用以下规则：

```text
recognizerCount =
    countNotEmpty(rule-ref, rule-data, expression)

require recognizerCount == 1
```

## 5. XML 属性详细说明

### 5.1 `name`

```xml
name="order.payable"
```

建议采用：

```text
业务域.业务事实
```

例如：

```text
user.activated
user.effective
order.ordered
order.paying
payment.success
payment.completed
```

命名要求：

1. 使用稳定的业务语言；
2. 表示一个可以判断真假的业务事实；
3. 不使用数据库字段名或状态值命名；
4. 原有业务定义不可改变，新定义应新增 Information；
5. 名称在有效配置域内唯一。

### 5.2 `model-ref`

```xml
model-ref="OrderInfo"
```

`model-ref` 引用一个已定义的业务模型。

它决定：

- `rule-ref` 的业务模型上下文；
- `rule-data` 可以访问的数据节点和字段；
- `change-data` 可以修改的数据节点和字段。

编译期必须校验：

- 业务模型存在；
- 表达式中的数据路径属于该模型；
- 字段名称及类型有效。

### 5.3 `rule-ref`

```xml
rule-ref="isPaySuccess"
```

`rule-ref` 引用一个用于识别 Information 的业务规则。

规则要求：

- 在 `model-ref` 指定的业务模型上下文中执行；
- 返回可解释为真或假的判断结果；
- 不直接组合其他 Information；
- 不应在识别过程中产生与判断无关的副作用。

Information 的组合应由 `expression` 完成，而不是隐藏在 `rule-ref` 中。

### 5.4 `rule-data`

```xml
rule-data="
    order.status = 3
    and
    every(orderDetails, status = 3)
"
```

`rule-data` 直接根据业务模型数据判断 Information 是否成立。

允许访问：

```text
当前 model-ref 业务模型中的数据节点和字段
```

不允许访问：

```text
其他 Information 名称
其他未引入业务模型的数据字段
实现语言对象或数据库专用对象
```

例如，以下写法非法：

```xml
<information
        name="order.paySuccessStatus"
        model-ref="OrderInfo"
        rule-data="payment.success and order.status = 3"/>
```

原因是 `payment.success` 是 Information，只能出现在 `expression` 中。

### 5.5 `expression`

```xml
<information
        name="order.paySuccess"
        expression="
            payment.success
            and
            order.paySuccessStatus
        "/>
```

`expression` 只能引用其他 Information。

推荐支持的逻辑运算包括：

```text
and
or
not
括号
```

编译期必须校验：

- 每个引用都存在；
- 不存在直接或间接循环依赖；
- 表达式最终返回布尔结果；
- 不包含业务模型字段访问；
- 不包含赋值语句。

### 5.6 `change-data`

```xml
<change-data>
    <![CDATA[
        order.status = 3;
        every(orderDetails, status = 3);
    ]]>
</change-data>
```

`change-data` 用于修改业务模型数据，使当前原子 Information 成立。

规则：

1. 只能出现在配置了 `model-ref` 的原子 Information 中；
2. 只能访问和修改 `model-ref` 指向的业务模型；
3. 不能引用其他 Information；
4. 不能作为复合 Information 的直接物化方式；
5. 执行后必须重新验证当前 Information；
6. 验证失败时，当前物化操作应失败。

表达式包含特殊 XML 字符时必须转义，或使用 CDATA。推荐统一使用 CDATA。

不使用 CDATA：

```xml
<change-data>amount = amount &lt; 0 ? 0 : amount;</change-data>
```

使用 CDATA：

```xml
<change-data>
    <![CDATA[
        amount = amount < 0 ? 0 : amount;
    ]]>
</change-data>
```

## 6. 识别与物化

Information 同时涉及两个方向：

```text
业务模型数据 ──识别──> Information
Information ──物化──> 业务模型数据
```

### 6.1 识别

识别回答：

```text
当前业务事实是否成立？
```

识别方式包括：

- `rule-ref`；
- `rule-data`；
- `expression`。

### 6.2 物化

物化回答：

```text
如何修改业务模型数据，使该业务事实成立？
```

物化方式包括：

- 原子 Information 的 `change-data`；
- 由 Action 引用的业务规则生产数据并保存。

能够识别某个事实，不表示引擎一定知道如何自动产生该事实。

例如：

```xml
<information
        name="payment.hasResult"
        model-ref="OrderInfo"
        rule-ref="hasPayResult"/>
```

它只能识别 `OrderInfo` 中是否存在 `PayResult`。真正创建 `PayResult` 的操作由目录中的 `receivePayResult` Action 负责。

## 7. 完整 XML 示例

以下示例覆盖三种 Information 类型。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<business-config name="order">

    <information-info>

        <!-- 规则型原子 Information -->
        <information
                name="user.activated"
                model-ref="UserInfo"
                rule-ref="isActivated"/>

        <information
                name="user.certified"
                model-ref="UserInfo"
                rule-ref="isCertified"/>

        <!-- 复合 Information -->
        <information
                name="user.effective"
                expression="
                    user.activated
                    and
                    user.certified
                "/>

        <!-- 可识别并可物化的原子 Information -->
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

        <information
                name="order.waitPay"
                model-ref="OrderInfo"
                rule-ref="isWaitPay"/>

        <information
                name="order.payable"
                expression="
                    order.ordered
                    or
                    order.waitPay
                "/>

    </information-info>

</business-config>
```

## 8. 示例中的 Information

| Information | 类型 | 业务含义 | 识别或组成方式 | 可自动物化 |
|---|---|---|---|---:|
| `user.activated` | 原子 | 用户已经激活 | `UserInfo.isActivated` | 否 |
| `user.certified` | 原子 | 用户已经认证 | `UserInfo.isCertified` | 否 |
| `user.effective` | 复合 | 用户有效 | `activated AND certified` | 否 |
| `order.ordered` | 原子 | 订单及明细已下单 | `rule-data` | 是 |
| `order.waitPay` | 原子 | 订单等待支付 | `OrderInfo.isWaitPay` | 否 |
| `order.payable` | 复合 | 订单可以支付 | `ordered OR waitPay` | 否 |
| `order.paying` | 原子 | 订单及明细支付中 | `rule-data` | 是 |
| `payment.hasResult` | 原子 | 已存在支付结果 | `OrderInfo.hasPayResult` | 否 |
| `payment.success` | 原子 | 支付结果成功 | `OrderInfo.isPaySuccess` | 否 |
| `payment.error` | 原子 | 支付结果失败 | `OrderInfo.isPayError` | 否 |
| `payment.completed` | 复合 | 支付结果已确定 | `success OR error` | 否 |
| `order.paySuccessStatus` | 原子 | 订单状态已物化为成功 | `rule-data` | 是 |
| `order.paySuccess` | 复合 | 支付结果和订单状态均成功 | `payment.success AND paySuccessStatus` | 否 |
| `order.payErrorStatus` | 原子 | 订单状态已物化为失败 | `rule-data` | 是 |
| `order.payError` | 复合 | 支付结果和订单状态均失败 | `payment.error AND payErrorStatus` | 否 |

## 9. 信息依赖关系

```text
user.activated ──┐
                 ├── AND ──> user.effective
user.certified ──┘

order.ordered ──┐
                ├── OR ───> order.payable
order.waitPay ──┘

payment.success ──┐
                  ├── OR ───> payment.completed
payment.error ────┘

payment.success ────────┐
                        ├── AND ──> order.paySuccess
order.paySuccessStatus ─┘

payment.error ──────────┐
                        ├── AND ──> order.payError
order.payErrorStatus ───┘
```

支付成功和失败各拆分为两层：

- `payment.success/error` 表示外部支付结果；
- `order.paySuccessStatus/payErrorStatus` 表示本地订单状态已经完成物化；
- 两者同时成立，完整的订单支付成功或失败事实才成立。

这种拆分适合异步回调、最终一致性、重试和补偿场景。

## 10. XML 编译期校验

加载 XML 后，编译器至少应执行以下校验。

### 10.1 结构校验

- 根元素必须为 `business-config`；
- `information-info` 数量合法；
- `information` 的必填属性存在；
- 不允许未知元素和未知属性；
- `change-data` 最多出现一次；
- XML 文本和 CDATA 可以正确解析。

### 10.2 定义校验

- `name` 唯一；
- `rule-ref`、`rule-data`、`expression` 必须且只能配置一个；
- 原子 Information 必须配置 `model-ref`；
- 复合 Information 不得配置 `model-ref` 和 `change-data`；
- `change-data` 只能用于可物化的原子 Information。

### 10.3 引用校验

- `model-ref` 指向已存在的业务模型；
- `rule-ref` 指向已存在且类型兼容的规则；
- `expression` 中的 Information 全部存在；
- 目录中的 `information-ref` 全部存在；
- Information 依赖图不存在循环。

### 10.4 表达式校验

- `rule-data` 只访问对应业务模型；
- `expression` 只引用 Information；
- `change-data` 只修改对应业务模型；
- 字段和数据节点存在；
- 操作符和字段类型兼容；
- 判断表达式最终返回布尔值。

## 11. 运行时语义

建议的运行流程如下：

```text
1. 加载业务模型实例
2. 按依赖顺序计算原子 Information
3. 计算复合 Information
4. Directory 使用计算结果执行依赖判断和分类
5. 需要物化时执行 change-data 或业务规则
6. 重新计算受影响的 Information
7. 验证目标 Information 最终成立
```

当业务模型数据发生变化时，不应无差别重算全部信息。编译器可建立：

```text
字段 → 原子 Information → 复合 Information → Directory
```

依赖索引，只重新计算受影响的节点。

## 12. 非法 XML 示例

### 12.1 同时配置两种识别方式

```xml
<information
        name="order.invalid"
        model-ref="OrderInfo"
        rule-ref="checkOrder"
        rule-data="order.status = 1"/>
```

错误原因：`rule-ref` 与 `rule-data` 互斥。

### 12.2 复合 Information 直接访问字段

```xml
<information
        name="order.invalid"
        expression="order.status = 1 and payment.success"/>
```

错误原因：`expression` 只能引用 Information。

### 12.3 `rule-data` 引用 Information

```xml
<information
        name="order.invalid"
        model-ref="OrderInfo"
        rule-data="payment.success and order.status = 3"/>
```

错误原因：`rule-data` 只能访问业务模型数据。

### 12.4 复合 Information 配置 `change-data`

```xml
<information
        name="order.invalid"
        expression="payment.success and order.paySuccessStatus">
    <change-data>order.status = 3;</change-data>
</information>
```

错误原因：复合 Information 不直接物化业务模型。

## 13. 与目录的关系

Information 定义业务事实，Directory 使用这些事实。

```xml
<directory
        name="paying"
        information-ref="order.paying"
        model-ref="OrderInfo">

    <dependency-info>
        <dependency information-ref="order.payable"/>
        <dependency information-ref="user.effective"/>
    </dependency-info>

</directory>
```

这里：

- `order.paying` 定义属于 `paying` 目录的最终事实；
- `order.payable` 和 `user.effective` 定义进入目录的前提；
- Directory 不重复编写这些事实的底层字段判断。

## 14. 设计原则

1. Information 必须使用稳定业务语言；
2. 原子 Information 负责连接业务事实与业务模型数据；
3. 复合 Information 只组合其他 Information；
4. 识别和物化必须分离；
5. `rule-data` 禁止引用 Information；
6. Information 的组合只能出现在 `expression`；
7. 没有 `change-data` 的 Information 只能识别，不能自动物化；
8. Action 负责实际生产数据，Produce 负责声明结果契约；
9. Directory 应引用 Information，不应复制底层判断；
10. XML 必须经过编译校验后才能交由引擎执行。
