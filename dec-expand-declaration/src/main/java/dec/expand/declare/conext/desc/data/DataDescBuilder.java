package dec.expand.declare.conext.desc.data;

import java.util.ArrayList;
import java.util.List;

public class DataDescBuilder {
	
	private List<DataDesc> dataList = new ArrayList<>();
	
	private DataDesc currentDataDesc = new DataDesc();
	
	public static DataDescBuilder create(){
		return new DataDescBuilder();
	}
	
	public DataDescBuilder build(String name, String desc){
		
		currentDataDesc = new DataDesc("$"+name, desc);
		
		currentDataDesc.setType(DataTypeEnum.COMMON);
		
		addData(currentDataDesc);
		
		return this;
	}
	
	/*public DataDescBuilder depend(String... data){
		currentDataDesc.addDepend(new DataDepend(data));
		
		return this;
	}
	
	public DataDescBuilder depend(String express, String... data){
		currentDataDesc.addDepend(new DataDepend(data, express));
		
		return this;
	}*/
	
	public List<DataDesc> getDatas() {
		return dataList;
	}

	private void addData(DataDesc data){
		dataList.add(data);
	}
	
	
}
