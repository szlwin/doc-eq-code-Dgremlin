package dec.expand.declare.conext.desc.produce;

import dec.expand.declare.conext.desc.Desc;

public class ProduceDesc extends Desc{

	public ProduceDesc(){
		
	}
	
	public ProduceDesc(String name){
		this.setName(name);
	}
	
	public ProduceDesc(String name, String comment){
		this.setName(name);
		this.setComment(comment);
	}
	
	private boolean isGetAllData;
	
	private String[] dataTypes;
	
	private String resultDataType;

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

	public String getResultDataType() {
		return resultDataType;
	}

	public void setResultDataType(String resultDataType) {
		this.resultDataType = resultDataType;
	}
	
	
}
