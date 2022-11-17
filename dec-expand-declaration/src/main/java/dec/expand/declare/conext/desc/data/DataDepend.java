package dec.expand.declare.conext.desc.data;

public class DataDepend {

	private String data;
	
	private String express;

	private Integer type;
	
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

	private void init(){
		if(data.startsWith("$")){
			type =1;
		}else{
			type = 2;
		}
	}
}
