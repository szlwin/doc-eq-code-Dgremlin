package dec.core.context.config.model.data;

public class Column {

	public static final String NAME = "name";
	
	public static final String REF_PROPERTY = "ref-property";
	
	public static final String TYPE = "type";
	
	private String name;
	
	private String refproperty;

	private String convertFun;
	
	private String type;

	private String originType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRefproperty() {
		return refproperty;
	}

	public void setRefproperty(String refproperty) {
		this.refproperty = refproperty;
	}

	public String getConvertFun() {
		return convertFun;
	}

	public void setConvertFun(String convertFun) {
		this.convertFun = convertFun;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOriginType() {
		return originType;
	}

	public void setOriginType(String originType) {
		this.originType = originType;
	}
}
