# 目录说明

本文说明 `doc-eq-code-Dgremlin` 中目录的设计目的、XML 格式、查询与执行语义，以及 Information、RuleView、Action、Produce、Change 和 Back 之间的关系。

本文以以下示例为主要依据：

```text
dec-demo/src/main/resources/directory/order/order-directory-new.xml
```

> 本文优先定义设计语言应表达的目标语义。文中标注为“目标能力”的部分，当前 Java 引擎不一定已经完整实现。

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
- Action 产生的数据与后续业务信息之间缺少明确关系；
- 规则驱动操作和项目自定义操作缺少统一的目录表达方式。

目录将业务对象看作资源，并使用业务目录描述：

- 当前属于什么业务阶段或分类；
- 可以继续进入哪些目录；
- 进入目录前需要满足哪些 Information；
- 进入时执行哪些 Action；
- Action 是调用规则视图，还是调用项目注册的自定义实现；
- Action 后必须产生哪些数据；
- 哪些产出数据还要作为 Information 使用；
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
业务模型 / Business View
    提供业务对象及数据结构
        ↓
RuleView
    组织一组可复用规则
        ↓
Information
    定义对象上成立的业务信息
        ↓
Directory
    使用业务信息定义分类、路径和进入条件
        ↓
Action
    调用 RuleView 或全局注册的自定义 Action
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
- `action/@rule-ref`：Action 调用哪个 `rule-view-info`；
- 无 `rule-ref` 的 Action：调用哪个全局注册的自定义 Action；
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
3. `startPay` 通过 `rule-ref="pay"` 调用支付规则视图；
4. `receivePayResult` 通过 `rule-ref="receivePayResult"` 调用支付结果规则视图；
5. `recordPyaError` 通过 `rule-ref="recordPyaError"` 调用错误记录规则视图；
6. `smsNotify` 没有 `rule-ref`，由项目提供全局自定义 Action 实现并注册；
7. `PaymentInfo` 和 `PayResult` 作为后续信息使用，需要 Produce 映射；
8. `PyaError` 当前只作为记录数据，不映射 Information。

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
| 规则引用属性 | 统一使用 `rule-ref` |
| 大小写 | 元素、属性和引用均区分大小写 |
| 未知元素或属性 | 编译器应拒绝，不能静默忽略 |

`ref-rule` 是旧命名，不属于当前目标 XML 规范。Information 和 Action 引用规则视图时均统一使用 `rule-ref`。

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
4. 所有目录、Information、RuleView 和 Produce 引用必须在编译期解析；
5. 编译器必须构建完整目录图并校验路径；
6. 无 `rule-ref` 的 Action 名称必须加入待注册自定义 Action 清单。

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

### 9.1 基本格式

```xml
<action-info>

    <action
            name="startPay"
            rule-ref="pay">
        ...
    </action>

    <action name="smsNotify"/>

</action-info>
```

### 9.2 属性表

| 属性 | 必填 | 说明 |
|---|---:|---|
| `name` | 是 | Action 名称；无 `rule-ref` 时也是全局注册名称 |
| `rule-ref` | 否 | 引用 `rule-view-info/@name` |

Action 有两种互斥的执行模式：

```text
rule-ref 存在
    => 规则视图 Action

rule-ref 不存在
    => 全局注册自定义 Action
```

`ref-rule` 不再合法。解析器应拒绝旧属性，避免新旧字段并存。

## 10. 规则视图 Action

### 10.1 XML 格式

```xml
<action
        name="startPay"
        rule-ref="pay">

    <produce-info>
        <produce
                ref="PaymentInfo"
                information-ref="payment.paymentInfo"/>
    </produce-info>

</action>
```

`rule-ref="pay"` 对应：

```xml
<rule-view-info
        name="pay"
        view-ref="OrderInfo">

    <!-- 发起支付的一组规则 -->
    <rule .../>
    <rule .../>

</rule-view-info>
```

引用关系为：

```text
action/@rule-ref
    └── rule-view-info/@name
```

不是：

```text
action/@rule-ref
    └── rule-view-info/rule/@name
```

### 10.2 RuleView 是规则集合

`rule-view-info` 在一个业务视图上组织一组规则：

```xml
<rule-view-info
        name="save-Order"
        view-ref="OrderInfo">

    <rule name="checkName" type="checkPattern" .../>
    <rule name="insertOrder" type="insert" .../>
    <rule name="insertProduct" type="insert" .../>

</rule-view-info>
```

因此 `action/@rule-ref` 引用的是整组规则。除非规则引擎明确声明其他调度方式，组内规则按配置顺序执行。

### 10.3 模型兼容

目录定义：

```xml
<directory
        name="paying"
        model-ref="OrderInfo"
        information-ref="order.paying">
    ...
</directory>
```

规则视图定义：

```xml
<rule-view-info
        name="pay"
        view-ref="OrderInfo">
    ...
</rule-view-info>
```

编译期必须校验：

```text
directory.model-ref
    compatible with
rule-view-info.view-ref
```

### 10.4 规则视图 Action 的执行流程

```text
1. 根据 action.rule-ref 查找 rule-view-info
2. 校验 rule-view-info.view-ref 与目录模型兼容
3. 在当前业务模型上下文中执行整个 RuleView
4. 校验 RuleView 执行结果
5. 校验 produce-info
6. 继续执行后续 Action 或 Change
```

### 10.5 示例

```xml
<action
        name="confirmPaySuccess"
        rule-ref="confirmPaySuccess"/>
```

表示 Action 名称为 `confirmPaySuccess`，执行时调用：

```xml
<rule-view-info
        name="confirmPaySuccess"
        view-ref="OrderInfo">
    ...
</rule-view-info>
```

Action 的 `name` 和 `rule-ref` 可以相同，也可以不同：

```xml
<action
        name="startPay"
        rule-ref="pay"/>
```

其中：

```text
startPay
    是目录中的业务操作名称

pay
    是被调用的 rule-view-info 名称
```

## 11. 无 `rule-ref` 的全局自定义 Action

### 11.1 XML 格式

```xml
<action name="smsNotify"/>
```

该 Action 没有 `rule-ref`，因此不调用 `rule-view-info`。

它表示项目需要：

1. 基于项目提供的自定义 Action 接口实现一个 Action 类；
2. 在类中实现短信通知逻辑；
3. 文档加载后，通过专门的注册接口，把实现注册到全局 Action 注册表；
4. 执行目录时，引擎根据 `name="smsNotify"` 查找并执行该实现。

### 11.2 全局名称

无 `rule-ref` 的 Action 以 `name` 作为全局注册键：

```text
smsNotify
```

要求：

- 名称在全局自定义 Action 注册域中唯一；
- 不应因目录不同而注册多个含义不同的同名实现；
- 重复注册默认应拒绝，除非未来提供显式替换接口；
- 文档中的大小写必须与注册名称完全一致。

### 11.3 注册生命周期

由于项目需要在文档加载后注册自定义实现，推荐生命周期为：

```text
1. 加载和解析业务文档
2. 收集所有无 rule-ref 的 Action 名称
3. 项目注册自定义 Action 实现
4. 执行启动完成校验
5. 所有自定义 Action 均已注册后，目录引擎进入可用状态
```

因此应区分：

```text
文档加载成功
    不等于
目录引擎已经可以执行
```

如果文档声明了 `smsNotify`，但在引擎开始对外服务前仍未注册，则启动完成校验应失败。

### 11.4 注册接口的目标契约

当前不在本文固定具体 Java 方法签名，但注册能力至少应支持：

```text
注册：
    actionName + actionImplementation

查询：
    根据 actionName 获取实现

校验：
    判断文档声明的 Action 是否全部注册

注销或替换：
    由后续生命周期规范决定
```

概念示例：

```java
// 仅表示目标语义，不代表当前已经存在该 API。
actionRegistry.register("smsNotify", smsNotifyAction);
```

### 11.5 自定义 Action 的执行上下文

自定义 Action 执行时至少需要获得：

- 当前目录；
- 当前 Action 名称；
- 当前业务模型数据；
- 当前会话或执行上下文；
- Produce 声明；
- 错误和执行结果返回通道。

具体接口字段和方法由后续实现设计确定。

### 11.6 自定义 Action 与 Produce

自定义 Action 同样可以声明 Produce：

```xml
<action name="createNotice">

    <produce-info>
        <produce
                ref="NoticeInfo"
                information-ref="notice.created"/>
    </produce-info>

</action>
```

即使 Action 不是 RuleView，Produce 的数据和 Information 后置条件仍然必须校验。

`smsNotify` 当前没有 Produce：

```xml
<action name="smsNotify"/>
```

表示文档只要求执行通知操作，没有声明必须新增业务模型数据。

## 12. 两类 Action 的选择规则

| 场景 | XML | 执行方式 |
|---|---|---|
| 可由规则视图表达的业务逻辑 | `<action name="startPay" rule-ref="pay"/>` | 执行 `rule-view-info name="pay"` |
| 项目自定义外部能力 | `<action name="smsNotify"/>` | 查找全局注册 Action |

选择原则：

- 数据检查、数据赋值、持久化、规则计算等，可优先使用 RuleView；
- 短信、邮件、第三方 SDK、特殊硬件、项目私有协议等，可使用自定义 Action；
- 不应为了调用自定义 Java 类而伪造一个空 RuleView；
- 也不应把可由文档规则表达的普通操作全部退回 Java 自定义 Action。

解析和执行决策：

```text
if action.rule-ref is not empty:
    execute RuleView(action.rule-ref)
else:
    execute GlobalActionRegistry.get(action.name)
```

不存在第三种隐式回退方式。RuleView 不存在时，不能自动尝试同名自定义 Action；自定义 Action 未注册时，也不能自动尝试同名 RuleView。

## 13. `produce-info` 与 `produce`

### 13.1 Produce 的职责

```xml
<produce-info>
    <produce ref="PaymentInfo"/>
</produce-info>
```

`produce` 首先表示 Action 成功后必须产生的数据后置条件。

它不自动表示该数据已经成为信息树中的 Information：

```text
ref
    指向实际产生的数据、模型节点或结果对象

information-ref
    指向该数据在信息树中对应的 Information
```

### 13.2 属性表

| 属性 | 必填 | 说明 |
|---|---:|---|
| `ref` | 是 | Action 必须产生的数据、模型节点或结果对象 |
| `information-ref` | 条件必填 | 该产出数据在信息树中对应的 Information |

### 13.3 何时必须配置 `information-ref`

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

### 13.4 条件必填规则

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

### 13.5 PaymentInfo

```xml
<action
        name="startPay"
        rule-ref="pay">

    <produce-info>
        <produce
                ref="PaymentInfo"
                information-ref="payment.paymentInfo"/>
    </produce-info>

</action>
```

执行语义：

```text
1. 执行 rule-view-info(name=pay)
2. 验证 PaymentInfo 已产生
3. 将 PaymentInfo 放入 OrderInfo 或执行上下文
4. 执行 payment.paymentInfo 的识别规则视图
5. 要求 payment.paymentInfo 成立
6. 重新计算依赖它的其他 Information
7. 允许后续目录和依赖继续执行
```

### 13.6 PayResult

```xml
<action
        name="receivePayResult"
        rule-ref="receivePayResult">

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

### 13.7 PyaError

```xml
<action
        name="recordPyaError"
        rule-ref="recordPyaError">

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

### 13.8 映射约束

1. `produce/@ref` 必须可以解析；
2. `produce/@information-ref` 存在时，必须引用已定义 Information；
3. 映射应指向可根据该产出直接识别的原子 Information；
4. Information 的模型上下文必须与 Action 兼容；
5. Action 执行后必须能够验证对应 Information；
6. 不允许把纯记录数据强制映射到无关 Information；
7. 一个 Produce 当前只声明一个主要对应 Information；
8. 其他衍生信息由信息树继续识别或通过 `expression` 推导；
9. 不应直接映射到 Action 单独无法保证成立的复合 Information。

## 14. `change-info`

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

## 15. `back`

Back 定义在父目录指向子目录的关系上：

```xml
<subdirectory rel="PayResult">

    <back name="returnPaying">
        <action-info>
            <action
                    name="resetPayResult"
                    rule-ref="resetPayResult"/>
        </action-info>
    </back>

</subdirectory>
```

含义：

```text
PayResult → paying
```

Back 中的 Action 与普通目录 Action 使用同一规则：

- 有 `rule-ref`：执行 RuleView；
- 无 `rule-ref`：执行全局注册自定义 Action。

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

## 16. 目录执行语义

进入一个目录时，推荐顺序：

```text
1. 解析从当前目录到目标目录的路径
2. 校验当前步骤的 Dependency
3. 按 XML 顺序执行 Action
4. 对有 rule-ref 的 Action 执行 RuleView
5. 对无 rule-ref 的 Action执行全局注册实现
6. 校验每个 produce/@ref 数据已经产生
7. 存在 produce/@information-ref 时，识别对应 Information
8. 重新计算受影响的其他 Information
9. 执行 Change
10. 验证目标目录 directory/@information-ref
11. 根据 subdirectory 信息进行自动分类
12. 记录执行结果
```

### 16.1 进入 `paying`

```text
依赖：
    order.payable
    user.effective

Action：
    startPay

Action 类型：
    RuleView Action

RuleView：
    pay

Produce 数据：
    PaymentInfo

Produce Information：
    payment.paymentInfo

Change：
    order.paying

最终验证：
    order.paying
```

### 16.2 进入 `PayResult`

```text
Action：
    receivePayResult

Action 类型：
    RuleView Action

RuleView：
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

### 16.3 进入 `error`

```text
依赖：
    payment.error

Action 1：
    recordPyaError
    类型：RuleView Action
    RuleView：recordPyaError

Produce 数据：
    PyaError

Produce Information：
    无

Action 2：
    smsNotify
    类型：全局注册自定义 Action

Change：
    order.payErrorStatus

最终验证：
    order.payError
```

Action 默认按 XML 顺序执行，因此：

```text
先记录 PyaError
再发送短信通知
```

如果 `recordPyaError` 失败，默认不应继续执行 `smsNotify`，除非未来显式引入“失败后继续”等执行策略。

## 17. 直接进入目录的规则

外部直接执行：

```text
execute("success")
```

不能绕过：

- 从当前目录到 `success` 的中间路径；
- `paying` 的 Dependency；
- `startPay` 的 RuleView 和 Produce；
- `PayResult` 的 RuleView 和 Produce；
- `payment.success` 依赖；
- `success` 的 Change 和最终 Information 校验。

直接指定目标目录只表示执行目标，不表示跳过过程。

## 18. 查询语义

### 18.1 查询父目录

```text
find("PayResult")
```

查询范围应覆盖：

```text
success
error
```

### 18.2 查询单个分类

```text
find("success")
```

只查询支付成功数据。

### 18.3 查询路径范围

```text
find("ordered")
    .start("ordered")
    .end("success")
```

应根据目录路径及其 Information 生成业务查询条件，而不是把目录名称直接替换为固定状态值。

### 18.4 关联模型

```text
find("ordered")
    .with("UserInfo")
    .start("ordered")
    .end("success")
```

`with` 表示查询时需要关联其他业务模型。

## 19. 编译期校验

### 19.1 XML 结构

- 元素顺序正确；
- 未知元素和属性被拒绝；
- `ref-rule` 被拒绝；
- 必填和条件必填属性完整；
- 容器数量有效。

### 19.2 目录图

- 根目录存在；
- 非根目录可以从根到达；
- 不存在非法环；
- `rel` 目标存在；
- 多父目录路径不存在无法消解的歧义。

### 19.3 RuleView Action

- `action/@rule-ref` 指向的 `rule-view-info` 存在；
- `rule-ref` 不解析为内部 `rule/@name`；
- `rule-view-info/@view-ref` 与目录 `model-ref` 兼容；
- RuleView 中的规则定义合法。

### 19.4 自定义 Action

文档编译期：

- 无 `rule-ref` 的 Action 必须有 `name`；
- 收集全部自定义 Action 名称；
- 同一业务文档中不允许名称语义冲突。

启动完成校验：

- 每个文档声明的自定义 Action 都已注册；
- 全局注册名称唯一；
- 注册实现符合目标 Action 接口；
- 未注册 Action 阻止引擎进入可用状态。

### 19.5 Produce 映射

- 后续作为 Information 使用的数据必须配置映射；
- 纯日志、记录或返回数据允许不配置映射；
- 映射指向可直接验证的原子 Information；
- 数据与 Information 模型上下文兼容；
- Action 执行后能够验证该 Information；
- 不允许把单一数据产出直接映射到无法由其独立保证的复合 Information。

### 19.6 分类

- `any-one` 组至少命中一个；
- `mutual-exclusion` 引用存在；
- 互斥目录不能同时成立；
- 当前支付结果分类必须且只能命中 `success/error` 之一。

### 19.7 Back

- Back 只定义在有效父子关系上；
- Back Action 的 RuleView 或自定义实现有效；
- 返回后的父目录 Information 可恢复；
- 多级 Back 路径完整。

## 20. 运行时校验

运行时至少应校验：

1. Dependency 是否全部成立；
2. RuleView Action 引用是否可执行；
3. 自定义 Action 是否已注册；
4. Action 是否成功；
5. `produce/@ref` 数据是否实际产生；
6. 存在 `produce/@information-ref` 时，对应 Information 是否成立；
7. 纯数据 Produce 是否写入正确位置；
8. Change 是否成功；
9. 目标目录 Information 是否成立；
10. 分类是否唯一；
11. Back 后父目录 Information 是否恢复；
12. 执行记录是否包含路径、Action 类型、RuleView 或注册实现、Produce、Information 和 Change 结果。

## 21. 当前实现边界

目标 XML 已统一使用：

```xml
rule-ref="..."
```

但已有 Java 配置对象和解析器仍可能使用：

```text
refRule
ref-rule
```

后续代码需要统一调整：

```text
XML: ref-rule → rule-ref
Java field: refRule → ruleRef
parser: attributeValue("ref-rule") → attributeValue("rule-ref")
```

当前代码中已有的目录 Action 接口更接近规则执行前后钩子，不等同于本文定义的全局自定义 Action SPI。仍需新增或重构：

- 自定义 Action 执行接口；
- 全局 Action 注册表；
- 文档加载后的注册接口；
- 启动完成校验；
- 自定义 Action 执行上下文；
- Action 结果和 Produce 数据返回契约。

## 22. 完整关键片段

规则视图 Action：

```xml
<action
        name="startPay"
        rule-ref="pay">

    <produce-info>
        <produce
                ref="PaymentInfo"
                information-ref="payment.paymentInfo"/>
    </produce-info>

</action>
```

对应规则视图：

```xml
<rule-view-info
        name="pay"
        view-ref="OrderInfo">
    ...
</rule-view-info>
```

全局自定义 Action：

```xml
<action name="smsNotify"/>
```

组合示例：

```xml
<action-info>

    <action
            name="recordPyaError"
            rule-ref="recordPyaError">

        <produce-info>
            <produce ref="PyaError"/>
        </produce-info>

    </action>

    <action name="smsNotify"/>

</action-info>
```

执行含义：

```text
recordPyaError
    调用 rule-view-info(name=recordPyaError)
    产生 PyaError 记录数据

smsNotify
    从全局 Action 注册表查找 smsNotify 实现
    执行项目自定义短信通知逻辑
```

## 23. 设计原则

1. Directory 使用业务语言，不直接暴露状态值；
2. Directory 通过 Information 判断和验证业务状态；
3. Dependency 只引用 Information；
4. Information 和 Action 的 `rule-ref` 均引用 `rule-view-info/@name`；
5. `rule-ref` 不引用内部单条 `rule/@name`；
6. `ref-rule` 不再属于目标 XML；
7. 有 `rule-ref` 的 Action 执行 RuleView；
8. 无 `rule-ref` 的 Action 执行全局注册实现；
9. 自定义 Action 名称在全局注册域中唯一；
10. RuleView 不存在时不回退到同名自定义 Action；
11. 自定义 Action 未注册时不回退到同名 RuleView；
12. Produce 声明实际数据产出；
13. 数据后续作为信息使用时，Produce 必须声明 `information-ref`；
14. 纯数据产出不应被强制定义为 Information；
15. Change 负责物化目录状态；
16. Back 沿相邻目录关系逐级执行；
17. 查询和执行使用同一套目录与 Information 语义。
