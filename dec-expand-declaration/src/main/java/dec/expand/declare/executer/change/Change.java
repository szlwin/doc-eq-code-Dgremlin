package dec.expand.declare.executer.change;

import dec.expand.declare.executer.Executer;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;

public class Change<DataStorage> extends Executer<DataStorage> {

	private boolean isGetAllData;

	private String[] dataTypes;

	private String system;

	public Change(){

	}

	public ExecuteResult execute(DataStorage t) throws Exception{
		return getFun().execute(t);
	}

	public Change(String name){
		this.setName(name);
	}

	public Change(String name, Function<ExecuteResult, DataStorage> fun){
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

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}
}
