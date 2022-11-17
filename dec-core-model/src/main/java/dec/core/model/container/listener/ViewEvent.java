package dec.core.model.container.listener;

import dec.core.context.config.model.rule.customer.CustomerInfo;
import dec.core.context.data.ModelData;
import dec.core.model.container.ResultInfo;

public class ViewEvent {

	private String viewName;
	
	private String ruleName;
	
	private ResultInfo ruleResultInfo;
	
	private ModelData modelData;

	private ViewEventEnum type;
	
	private CustomerInfo customerInfo;
	
	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public ResultInfo getRuleResultInfo() {
		return ruleResultInfo;
	}

	public void setRuleResultInfo(ResultInfo ruleResultInfo) {
		this.ruleResultInfo = ruleResultInfo;
	}

	public ModelData getModelData() {
		return modelData;
	}

	public void setModelData(ModelData modelData) {
		this.modelData = modelData;
	}

	public ViewEventEnum getType() {
		return type;
	}

	public void setType(ViewEventEnum type) {
		this.type = type;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public CustomerInfo getCustomerInfo() {
		return customerInfo;
	}

	public void setCustomerInfo(CustomerInfo customerInfo) {
		this.customerInfo = customerInfo;
	}
}
