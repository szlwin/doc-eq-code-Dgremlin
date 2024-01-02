package dec.expand.declare.conext.desc.data;

public class DataDepend {

	private String data;
	
	private String express;

	private Integer type;

	private String param;

	private String init;

	private String condition;

	private String change;

	public DataDepend(String data, String express){
		
		this.data = data;
		
		this.express = express;
		
		init();
	}
	
	public DataDepend(String data){
		this(data, null);
	}
	public String getData() {
		return data;
	}


	public String getExpress() {
		return express;
	}
	
	public Integer getType() {
		return type;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getInit() {
		return init;
	}

	public void setInit(String init) {
		this.init = init;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getChange() {
		return change;
	}

	public void setChange(String change) {
		this.change = change;
	}

	private void init(){
		if(data.startsWith("$")){
			type =1;
		}else{
			type = 2;
		}
	}
}
