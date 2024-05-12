package dec.expand.declare.executer.conume;

import dec.expand.declare.executer.Executer;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;

public class Conumer<T> extends Executer<T> {
	
	private boolean isGetAllData;
	
	private String[] dataTypes;
	
	public Conumer(){
		
	}
	
	@SuppressWarnings("unchecked")
	public ExecuteResult execute(Object t) throws Exception{
		return getFun().execute((T) t);
	}
	
	public Conumer(String name){
		this.setName(name);
	}
	
	public Conumer(String name, Function<ExecuteResult,T> fun){
		this.setName(name);
		this.setFun(fun);
	}
	
	public boolean isGetAllData() {
		return isGetAllData;
	}

	public void setGetAllData(boolean isGetAllData) {
		this.isGetAllData = isGetAllData;
	}

	public String[] getDataTypes() {
		return dataTypes;
	}

	public void setDataTypes(String[] dataTypes) {
		this.dataTypes = dataTypes;
	}
	
	
}
