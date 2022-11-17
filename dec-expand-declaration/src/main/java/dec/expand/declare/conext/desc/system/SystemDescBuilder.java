package dec.expand.declare.conext.desc.system;

import dec.expand.declare.conext.desc.data.DataDepend;
import dec.expand.declare.conext.desc.data.DataDesc;
import dec.expand.declare.conext.desc.data.DataTypeEnum;

public class SystemDescBuilder {
	
	private SystemDesc systemDesc;
	
	private DataDesc currentDataDesc;
	
	public static SystemDescBuilder create(){
		return new SystemDescBuilder();
	}
	
	
	public SystemDescBuilder build(String name, String desc){
		
		systemDesc = new SystemDesc(name, desc);
		
		return this;
	}
	
	public SystemDescBuilder data(String name, String desc) {
		
		currentDataDesc = new DataDesc(name, desc);
		
		this.addData(currentDataDesc);
		
		return this;
	}
	
	public SystemDescBuilder data(String name, String desc, String dataMappings[]) {
		
		currentDataDesc = new DataDesc(name, desc, dataMappings);
		
		this.addData(currentDataDesc);
		
		return this;
	}
	
	public SystemDescBuilder addData(DataDesc dataDesc) {
		currentDataDesc = dataDesc;
		
		systemDesc.addData(dataDesc);
		
		return this;
	}
	
	public SystemDescBuilder type(DataTypeEnum type){
		currentDataDesc.setType(type);
		return this;
	}
	
	public SystemDescBuilder cachePrior(boolean cachePrior){
		currentDataDesc.setCachePrior(cachePrior);
		return this;
	}
	
	public SystemDescBuilder depend(String data){
		currentDataDesc.addDepend(new DataDepend(data));
		
		return this;
	}
	
	public SystemDescBuilder depend(String data, String express){
		currentDataDesc.addDepend(new DataDepend(data, express));
		
		return this;
	}
	
	public SystemDesc getSystem(){
		return systemDesc;
	}
	
}
