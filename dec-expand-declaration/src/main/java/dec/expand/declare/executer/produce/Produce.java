package dec.expand.declare.executer.produce;

import dec.expand.declare.executer.Executer;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;

public class Produce<DataStorage> extends Executer<DataStorage> {
	
	private boolean isGetAllData;
	
	private String[] dataTypes;
	
	public Produce(){
		
	}
	
	public ExecuteResult execute(DataStorage t) throws Exception{
		return getFun().execute(t);
	}
	
	public Produce(String name){
		this.setName(name);
	}
	
	public Produce(String name, Function<ExecuteResult, DataStorage> fun){
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
