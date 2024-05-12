package test.business;

import java.util.List;

import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.data.DataDesc;
import dec.expand.declare.conext.desc.data.DataDescBuilder;

public class OrderBusiness extends BusinessDesc{

	
	protected List<DataDesc> createDatas(){
		
		return DataDescBuilder.create()
			.build("orderInputData", "订购输入数据")
			.build("payData", "支付数据")
			.build("orderData", "订购数据")
			.build("payCmdData", "支付指令数据")
			.build("payResult", "支付结果")
			.getDatas();
	}
	
	/*protected List<ProduceDesc> createProduces(){
		
		return ProduceDescBuilder.create()
				.build("payAmountData")
				.build("orderData")
				.build("payCmdData")
				.getProduces();
	}
	
	/*protected List<EventDesc> createEvents(){
		
		return EventDescBuilder.create()
			//.build("computePayAmountData", "计算支付金额")
			//.build("generateOrderData", "生成订购数据")
			.build("saveOrderData", "保存订购数据")
				.addMapping("order", "generateOrder","saveOrder")
			//.build("generatePayCmdData", "生成支付指令")
			.build("operateWithPay", "支付操作")
				.addMapping("pay","executePayCmd","savePayCmdResutl")
			.build("savePay", "保存支付结果")
				.addMapping("order", "generateOrderPayStatus","saveOrderPayResutl")
			.getEvents();
	}*/
	

}
