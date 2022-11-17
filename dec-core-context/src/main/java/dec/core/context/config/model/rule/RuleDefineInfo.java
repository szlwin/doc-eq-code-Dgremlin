package dec.core.context.config.model.rule;

import dec.core.context.config.model.rule.customer.CustomerInfo;
import dec.core.context.config.model.rule.error.ErrorInfo;

public class RuleDefineInfo {

	public static String TYPE = "type";
	
	public static String NAME = "name";
	
	public static String PROPERTY = "property";
	
	private String type;
	
	private String name;
	
	private String property;
	
	private ErrorInfo errorInfo;
	
	private CustomerInfo customerInfo;
	
	private String grammer;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public ErrorInfo getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(ErrorInfo errorInfo) {
		this.errorInfo = errorInfo;
	}

	public CustomerInfo getCustomerInfo() {
		return customerInfo;
	}

	public void setCustomerInfo(CustomerInfo customerInfo) {
		this.customerInfo = customerInfo;
	}

	public String getGrammer() {
		return grammer;
	}

	public void setGrammer(String grammer) {
		this.grammer = grammer;
	}
	
}
