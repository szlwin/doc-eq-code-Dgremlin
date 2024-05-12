# 数据生产与消费示例

此篇会通过代三个代码实例，说明如何使用，其示例代码及相应的配置文件分别在dec-demo项目中，代码对应的dec.demo.declaration包，配置文件对应resources/declaration/declare-config.xml。

以下为三个示例的配置文件：<br>
```
<declare-config>

<systems>
  <system name="order">
     <datas>
       <data name="subscribeOrderDataSimple" desc="订购订单数据-简单" isCachePrior="false">
	  <depends>
             <depend data="$subscribeOrderData" init="status:1"/>
	  </depends>
       </data>

	<data name="subscribeOrderData" desc="订购订单数据" isCachePrior="false">
          <depends>
             <depend data="$subscribeOrderData" init="status:1"/>
          </depends>
	</data>

	<data name="cancelOrderData" desc="取消订单数据" isCachePrior="false">
          <depends>
            <depend data="$cancelOrderData"/>
            <depend data="orderData" param="orderId:$cancelOrderData.orderId" condition="status=1 or status=2" change="status:3"/>
          </depends>
	</data>

	<data name="orderData" desc="订单数据" type="persistent" isCachePrior="false">
	</data>

	<data name="orderPayResultData" desc="订单支付状态数据"  type="persistent" isCachePrior="false">
          <depends>
	    <depend data="$payResultData"/>
            <depend data="orderData" condition="status=1"/>
	  </depends>
	</data>
     </datas>
  </system>

  <system name="pay" desc="支付">
     <datas>
	<data name="payCmdData" desc="支付指令数据">
	  <depends>
              <depend data="$payData"/>
	  </depends>
	</data>

	<data name="payResultData" desc="支付结果数据"  type="persistent" isCachePrior="true">
           <depends>
               <depend data="payCmdData"/>
           </depends>
	</data>
    </datas>
  </system>

  <system name="common">
     <datas>
	<data name="$cancelOrderData" desc="取消订购操作数据"/>
	<data name="$subscribeOrderData" desc="订购数据"/>
	<data name="$payData" desc="支付数据"/>
	<data name="$payResultData" desc="支付数据"/>
	<data name="$refundOrderData" desc="订单退款数据"/>
     </datas>
   </system>
</systems>



<businesses>
   <business name="subscribeOrderDataWithSimple">
	<datas>
          <data system="order" name="subscribeOrderDataSimple" />
          <data system="order" name="orderPayResultData"/>
	</datas>
   </business>

   <business name="subscribeOrder"  >
	<datas>
	  <data  begin="true"/>
            <data system="order" name="subscribeOrderData" />
              <data  begin="true" transactionPolicy="NEW"/>
                <data name="$payData"/>
                  <data  begin="true"/>
		    <data system="pay" name="payResultData" />
		    <data name="$payResultData"/>
                  <data end="true"/>
               <data end="true"/>
             <data system="order" name="orderPayResultData"/>
	  <data end="true"/>
	</datas>
  </business>

  <business name="cancelOrder"  >
	<datas>
          <data begin="true" transactionPolicy="REQUIRE"/>
            <data system="order" name="cancelOrderData"/>
            <data name="$payData"/>
            <data system="pay" name="payResultData"/>
            <data name="$payResultData"/>
            <data system="order" name="orderPayResultData"/>
           <data end="true"/>
	</datas>
  </business>
  </businesses>
</declare-config>
```
以上配置文件中，定义了三个系统，分别为order、pay及common，其中common为默认通用系统，其系统中的数据可在多个系统中使用，另在order与pay系统中定义数据及其相应的依赖数据。<br>
其在配置文件中另定义了三个业务，分别为subscribeOrderDataWithSimple、subscribeOrder、cancelOrder，其subscribeOrder与cancelOrder较复杂，同时涉及两个系统以及事务。下面示例中会详细说明。

加载配置文件
===
在使用前，需先加载配置文件，具体代码如下：<br>
```
public static void initContext() throws Exception {
    ConfigInit.init();
    ContextUtils.loadConfig(new String[]{"classpath:declaration/declare-config.xml"});

}
```
以上代码在dec.demo.declaration.TestOrderBusiness类中，以上例子中只加载了一个配置文件，可同时加载多个。<br>

初始化系统
===
由于各数据的生产由各数据的系统负责，故需先初始化系统，并为系统内添加数据生产者，具体如下：<br>
```
public static void initSystem() {

    SystemBuilder systemBuilder = SystemBuilder.create()
            .build("order")
            .addChange("orderData", storage -> {
                Order order = (Order) storage.get("orderData");
                return ExecuteResult.success(order);
            }).addProduce("orderData", storage -> {
                Long orderId = (Long) storage.get("orderId");

                if (orderId == null) {
                    orderId = (Long) storage.getParam("orderId");

                }
                if (orderId == null) {
                    throw new RuntimeException("orderId is null");
                }

                Order order = new Order();
                order.setId(orderId);
                order.setProductName("Product");
                order.setStatus(1);
                return ExecuteResult.success(order);
            })
            .addProduce("subscribeOrderData", storage -> {
                SubscribeOrderData subscribeOrderData = (SubscribeOrderData) storage.get("$subscribeOrderData");

                Order order = new Order();
                order.setId(1l);
                order.setProductName(subscribeOrderData.getProductName());

                return ExecuteResult.success("orderId",order.getId());
            })
            .addProduce("cancelOrderData", storage -> {
                Order order = (Order) storage.get("orderData");
                return ExecuteResult.success(order);
            })
            .addProduce("orderPayResultData", storage -> {
                PayResultData payResultData = (PayResultData) storage.get("$payResultData");
                OrderPayResultData orderPayResultData = new OrderPayResultData();
                orderPayResultData.setOrderId(payResultData.getOrderId());
                orderPayResultData.setStatus(payResultData.getStatus());
                return ExecuteResult.success(orderPayResultData);
            }).addProduce("subscribeOrderDataSimple", storage -> {
                SubscribeOrderData subscribeOrderData = (SubscribeOrderData) storage.get("$subscribeOrderData");
                Order order = new Order();

                order.setId(1l);
                order.setProductName(subscribeOrderData.getProductName());

                return ExecuteResult.success("orderId",order.getId());
            });


    SystemBuilder systemPayBuilder = SystemBuilder.create()
            .build("pay")
            .addProduce("payCmdData", storage -> {
                PayData payData = (PayData) storage.get("$payData");

                PayCmdData payCmdData = new PayCmdData();

                payCmdData.setAmount(payData.getAmount());

                return ExecuteResult.success(payCmdData);

            }).addProduce("payResultData", storage -> {
                PayResultData payResultData = new PayResultData();
                return ExecuteResult.success(payResultData);
            });

    SystemBuilder systemCommonBuilder = SystemBuilder.create()
            .build("common");

    ContextUtils.load(systemCommonBuilder.getSystem());

    ContextUtils.load(systemBuilder.getSystem());

    ContextUtils.load(systemPayBuilder.getSystem());
}
```
以上代码在dec.demo.declaration.TestOrderBusiness类中，由于代码较长，只展示了order系统与common系统的代码。<br>
在以上代码中，为order系统中定义的数据提供生产方式，同时加载order系统。对于common系统，由于其为通用系统，其即可在系统中添加数据生产者，或是在业务中添加相应的数据生产者，如同时添加，则以业务中定义添加的生产者有效。<br>
以上代码中addProduce方法的storage参数为数据的存储器，在整个业务中生产的数据，都保存在其中，可通过数据名称获取。<br>
<br>

示例1
===
以下为subscribeOrderBySimple业务的示例代码，其模拟订单的订购业务，具体如下：<br>
```
    public static void subscribeOrderBySimple() {

        //1.创建业务，可只创建一次
        DefaultBusinessDeclare defaultBusinessDeclare = BusinessDeclareFactory
                .createDefaultBusinessDeclare("subscribeOrderDataWithSimple");

        SubscribeOrderData subscribeOrderData = new SubscribeOrderData();

        subscribeOrderData.setProductName("test");

        subscribeOrderData.setAmount(new BigDecimal(1000));

        defaultBusinessDeclare
                //2.添加参数
                .addEntity("$subscribeOrderData", subscribeOrderData)
                //3.添加common系统中$payResultData数据的生产者
                .addProduce("$payResultData", storage -> {

                    PayResultData payResultData = (PayResultData) storage.get("payResultData");

                    PayResultData resultData = new PayResultData();

                    resultData.setStatus(1);

                    return ExecuteResult.success(resultData);
                })
                //4.添加common系统中$payData数据的生产者
                .addProduce("$payData", storage -> {

                    SubscribeOrderData subscribeOrderData1 = (SubscribeOrderData) storage.get("$subscribeOrderData");

                    PayData payData = new PayData();

                    payData.setProductName(subscribeOrderData1.getProductName());

                    payData.setAmount(subscribeOrderData1.getAmount());

                    return ExecuteResult.success(payData);
                }).execute();

        log.info("subscribeOrderDataWithSimple result:"+defaultBusinessDeclare.getExecuteResult().isSuccess());
    }
```
在以上代码中，为'subscribeOrderDataWithSimple'业务创建了一个对象，并对common中的$payResultData与$payData数据添加了生产者。其addProduce方法的storage参数为数据的存储器，在整个业务中生产的数据，都保存在其中，可通过数据名称获取。<br>
在配置文件中'<depend data="$subscribeOrderData"  init="status:1"/>'，其'init="status:1"'会将第2处代码中传入的参数，自动将status设置为1。其init可设置多个值，如：'init="status:1,price:0"'
其业务执行的结果可通过getExecuteResult()方法获取，其中含有业务执行结果的详细信息，后续会详细说明。<br>
<br>

示例2
===
此示例依旧是模拟订单的订购业务，相对于第一个示例中，增加了事务的配置，其配置信息中增加事务配置信息，具体如下：<br>
```
<business name="subscribeOrder"  >
    <datas>
        <data  begin="true"/>
            <data system="order" name="subscribeOrderData" />
                <data  begin="true" transactionPolicy="NEW"/>
                    <data name="$payData"/>
                        <data  begin="true"/>
                            <data system="pay" name="payResultData" />
                            <data name="$payResultData"/>
                        <data end="true"/>
                <data end="true"/>
            <data system="order" name="orderPayResultData"/>
        <data end="true"/>
    </datas>
</business>
```
在以上配置中，对数据的生产增加了多个事务嵌套，且配置了不同的事务隔离级别(如transactionPolicy未配置，默认为REQUIRE)，以下是具体代码：<br>
```
public static void subscribeOrder() {
    DefaultBusinessDeclare defaultBusinessDeclare = BusinessDeclareFactory
        .createDefaultBusinessDeclare("subscribeOrder");

    SubscribeOrderData subscribeOrderData = new SubscribeOrderData();

    subscribeOrderData.setProductName("test");

    subscribeOrderData.setAmount(new BigDecimal(1000));

    defaultBusinessDeclare
        .addEntity("$subscribeOrderData", subscribeOrderData)
        //1.指定事务管理器
        .transactionManager(new MockDataSourceManager())
        .addProduce("$payResultData", storage -> {

            PayResultData payResultData = (PayResultData) storage.get("payResultData");

            PayResultData resultData = new PayResultData();

            resultData.setStatus(1);

            return ExecuteResult.success(resultData);

        }).addProduce("$payData", storage -> {

            SubscribeOrderData subscribeOrderData1 = (SubscribeOrderData) storage.get("$subscribeOrderData");

            PayData payData = new PayData();

            payData.setProductName(subscribeOrderData1.getProductName());

            payData.setAmount(subscribeOrderData1.getAmount());

            return ExecuteResult.success(payData);

        }).execute();
}
```
以上代码基本和示例1一样，区别在指定了事务管理器，用于进行事务处理。如在配置文件中设置了事务，但在代码中没有指定事务管理器，则事务不生效<br>
本例中为便于演示编写了一个Mock事务管理器，实际中需用户自己实现(需实现DataSourceManager接口)。另本框架提供了一个默认的Spring事务管理器(SpringDataSourceManager)，其可直接使用，具体使用方式如下：<br>
```
    SpringDataSourceManager springDataSourceManager = new SpringDataSourceManager();
    springDataSourceManager.setDstManager(dstManager);
    defaultBusinessDeclare
        .transactionManager(springDataSourceManager)
```
其"dstManager"参数为Spring的事务管理器，即org.springframework.jdbc.datasource.DataSourceTransactionManager类。

示例3
===
此例为取消订单的一个例子，同样与示例2一样配置了数据，且其在数据依赖配置中，增加了"param"属性以及"change"属性，具体如下：<br>
```
........
    <data name="cancelOrderData" desc="取消订单数据" isCachePrior="false">
        <depends>
            <depend data="$cancelOrderData"/>
            <depend data="orderData" param="orderId:$cancelOrderData.orderId" condition="status=1 or status=2" change="status:3"/>
        </depends>
    </data>
........    
```
在以上配置中，其"orderData"配置了param属性，表示将"$cancelOrderData"中的orderId参数赋予orderId参数，其参数可通过storage.getParam方法获取。而change表示，会将orderData的status属性值设置为2，通过会通知到order系统，可参考代码具体如下：<br>
```
public static void initSystem() {

    SystemBuilder systemBuilder = SystemBuilder.create()
            .build("order")
            .addChange("orderData", storage -> {
                Order order = (Order) storage.get("orderData");
                return ExecuteResult.success(order);
            }).addProduce("orderData", storage -> {
                Long orderId = (Long) storage.get("orderId");

                if (orderId == null) {
                    orderId = (Long) storage.getParam("orderId");

                }
                if (orderId == null) {
                    throw new RuntimeException("orderId is null");
                }

                Order order = new Order();
                order.setId(orderId);
                order.setProductName("Product");
                order.setStatus(1);
                return ExecuteResult.success(order);
            })
    .........        
}
```
在以上代码中，初始化系统order时，添加了一个"orderData"的change(即:addChange("orderData"))，如有配置change属性，会调用此方法，此时从存储器中取出的orderData数据的status属性值为3。<br>
另其change可配置多个参数，如：change="status:3,xxxStatus:1,xxxStatus:2"，可同时设置多个值。<br>
如果要获取变更之前原始的数据，可通过storage.getStatus方法，其参数为change中配置的参数名。以下为取消订单的业务配置信息及代码：<br>
配置信息：<br>
```
<business name="cancelOrder"  >
    <datas>
        <data begin="true" transactionPolicy="REQUIRE"/>
            <data system="order" name="cancelOrderData"/>
            <data name="$payData"/>
            <data system="pay" name="payResultData"/>
            <data name="$payResultData"/>
            <data system="order" name="orderPayResultData"/>
        <data end="true"/>
    </datas>
</business>
```
代码：
```
public static void cancelOrderData() {
    DefaultBusinessDeclare defaultBusinessDeclare = BusinessDeclareFactory
            .createDefaultBusinessDeclare("cancelOrder");

    SubscribeOrderData subscribeOrderData = new SubscribeOrderData();

    subscribeOrderData.setProductName("test");
    subscribeOrderData.setOrderId(11l);
    subscribeOrderData.setAmount(new BigDecimal(1000));

    defaultBusinessDeclare
            .addEntity("$cancelOrderData", subscribeOrderData)
            .transactionManager(new MockDataSourceManager())

            .addProduce("order", "cancelOrderData", storage -> {
                Long cancelOrderData = (Long) storage.get("$cancelOrderData");
                Order order = new Order();
                order.setId(cancelOrderData);
                order.setStatus(1);
                return ExecuteResult.success();
            }).addProduce("$payResultData", storage -> {

                PayResultData payResultData = (PayResultData) storage.get("payResultData");

                PayResultData resultData = new PayResultData();

                resultData.setStatus(1);

                return ExecuteResult.success(resultData);

            }).addProduce("$payData", storage -> {

                SubscribeOrderData subscribeOrderData1 = (SubscribeOrderData) storage.get("$cancelOrderData");

                PayData payData = new PayData();

                payData.setProductName(subscribeOrderData1.getProductName());

                payData.setAmount(subscribeOrderData1.getAmount());

                return ExecuteResult.success(payData);

            })
            .execute();
}
```
