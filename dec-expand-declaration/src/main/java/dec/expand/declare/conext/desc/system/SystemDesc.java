package dec.expand.declare.conext.desc.system;

import dec.expand.declare.conext.ContextStorage;
import dec.expand.declare.conext.desc.Desc;
import dec.expand.declare.conext.desc.data.DataDesc;

public class SystemDesc extends Desc{

	private ContextStorage contextStorage = new ContextStorage(1);

	public SystemDesc(){
		
	}
	
	public SystemDesc(String name){
		this(name, null);
	}
	
	public SystemDesc(String name, String comment){
		this.setName(name);
		this.setComment(comment);
	}
	
	public void addData(DataDesc dataDesc){
		contextStorage.add(1, dataDesc.getName(), dataDesc);
	
	}
	
	public DataDesc getData(String name){
		return (DataDesc) contextStorage.get(1, name);
	}
	
}
