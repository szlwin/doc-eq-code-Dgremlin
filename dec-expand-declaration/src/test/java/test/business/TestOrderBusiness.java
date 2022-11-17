package test.business;

import java.math.BigDecimal;

import dec.expand.declare.business.DefaultBusinessDeclare;
import dec.expand.declare.conext.desc.data.DataTypeEnum;
import dec.expand.declare.conext.desc.system.SystemDescBuilder;
import dec.expand.declare.service.ExecuteResult;
import dec.expand.declare.system.SystemBuilder;
import dec.expand.declare.utils.ContextUtils;

public class TestOrderBusiness {

	public static void main(String[] args) {
		initContext();

		initSystem();
		
		DefaultBusinessDeclare defaultBusinessDeclare = new DefaultBusinessDeclare();
		
		defaultBusinessDeclare.addEntity("111",  new Object());
		
		SubscribeOrderData subscribeOrderData = new SubscribeOrderData();
		
		subscribeOrderData.setProductName("test");
		
		subscribeOrderData.setAmount(new BigDecimal(1000));
		
		defaultBusinessDeclare.build("order").addEntity("$subscribeOrderData", subscribeOrderData)
		.data("orderData", "order")
		.data("$payData")
		.data("payCmdData", "pay")
		.data("$payResultData")
		.data("orderPayResultData", "order")
		.addProduce("$payResultData", storage->{
			
			PayCmdData payCmdData= (PayCmdData) storage.get("payCmdData");
			
			System.out.println("Produce $payResultData");
			
			PayResultData payResultData = new PayResultData();
			
			payResultData.setStatus(1);
			
			return ExecuteResult.success(payResultData);
			
		}).addProduce("$payData", storage->{
	
			System.out.println("Produce $payData");
			
			SubscribeOrderData subscribeOrderData1 = (SubscribeOrderData) storage.get("$subscribeOrderData");
			
			PayData payData = new PayData();
			
			payData.setProductName(subscribeOrderData1.getProductName());
			
			payData.setAmount(subscribeOrderData1.getAmount());
			
			return ExecuteResult.success(payData);
			
		})
		.execute();
	}
	
	public static void initContext(){
		//ContextUtils.load(new OrderBusiness());
		
		
		ContextUtils.load(SystemDescBuilder.create()
				.build("common", "common")
					.data("$subscribeOrderData", "")
					.data("$payData", "")
					.data("$payResultData", "")
						.getSystem());
		
		ContextUtils.load(SystemDescBuilder.create()
				.build("order", "订单")
				.data("subscribeOrderData", "订购数据")
				.data("payResultData", "支付结果数据")
				.data("orderData", "订单数据")
					.type(DataTypeEnum.PERSISTENT)
					.cachePrior(false)
					.depend("$subscribeOrderData")
					
				.data("orderPayResultData", "订单支付状态数据")
					.type(DataTypeEnum.PERSISTENT)
					.cachePrior(false)
					.depend("$payResultData")
					.depend("orderData")
					.getSystem());
		
		ContextUtils.load(SystemDescBuilder.create()
				.build("pay", "支付")
					.data("payCmdData", "支付指令数据")
						.depend("$payData")
					.data("payResultData", "支付结果数据")
						.depend("payCmdData")
						.type(DataTypeEnum.PERSISTENT)
						.cachePrior(true)
						.getSystem());
		
		
		
		/**.addMapping("saveOrderData", "order", "generateOrder")
		.addMapping("saveOrderData", "order", "saveOrder")
		.addMapping("savePay", "order", "generateOrderPayStatus")
		.addMapping("savePay", "order", "saveOrderPayResutl")
		.addMapping("operateWithPay", "pay", "generatePayCmd")
		.addMapping("operateWithPay", "pay", "executePayCmd")
		.addMapping("operateWithPay", "pay", "savePayCmdResutl")
		*/
		
	}
	
	public static void initSystem(){
		SystemBuilder systemBuilder = SystemBuilder.create()
			.build("order")
				.addProduce("orderData", storage->{
					
					System.out.println("Produce orderData");
					Long orderId = (Long) storage.get("orderData-id");
					
					if(orderId != null){
						Order order = new Order();
						
						order.setId(orderId);
						
						return ExecuteResult.success(order);
					}else{
						
						SubscribeOrderData subscribeOrderData = (SubscribeOrderData) storage.get("$subscribeOrderData");
						
						Order order = new Order();
						
						order.setId(1l);
						order.setProductName(subscribeOrderData.getProductName());
						
						return ExecuteResult.success(order);
					}

				})
				.addConumer("orderData", storage->{
					
					java.lang.System.out.println("Conume orderData");
					
					Order order = (Order) storage.get("orderData");
					
					java.lang.System.out.println(order.getId());
					
					
					return ExecuteResult.success("orderData-id", order.getId());
					
				})
				.addProduce("orderPayResultData", storage->{
					
					System.out.println("Produce orderPayResultData");
					
					Order order = (Order) storage.get("orderData");
					
					PayResultData payResultData = (PayResultData) storage.get("$payResultData");;
					
					return ExecuteResult.success();
				});
		
		
		SystemBuilder systemPayBuilder = SystemBuilder.create()
				.build("pay")
				.addProduce("payCmdData", storage->{
				
					System.out.println("Produce payCmdData");
					
						PayData payData= (PayData) storage.get("$payData");
						
						PayCmdData payCmdData = new PayCmdData();
						
						payCmdData.setAmount(payData.getAmount());
						
						return ExecuteResult.success(payCmdData);
						
					});
		
		SystemBuilder systemCommonBuilder = SystemBuilder.create()
				.build("common");
		
		ContextUtils.load(systemCommonBuilder.getSystem());
		
		ContextUtils.load(systemBuilder.getSystem());
		
		ContextUtils.load(systemPayBuilder.getSystem());
	}
}
