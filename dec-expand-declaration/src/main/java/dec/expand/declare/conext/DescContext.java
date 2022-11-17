package dec.expand.declare.conext;

import java.util.List;

import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.data.DataDesc;
import dec.expand.declare.conext.desc.system.SystemDesc;

public class DescContext {
	
	private ContextStorage contextStorage = new ContextStorage(2);
	
	private static final DescContext descContext = new DescContext();
	
	private DescContext(){
		
	}
	
	public static DescContext get(){
		return descContext;
	}
	
	public void addDatas(List<DataDesc> datas){
		
		for(DataDesc data : datas){
			contextStorage.add(2, data.getName(), data);
		}
	}
	
	
	public void addBusiness(BusinessDesc business){
		
		contextStorage.add(0, business.getName(), business);
	}
	
	public void addSystem(SystemDesc system){
		
		contextStorage.add(1, system.getName(), system);
	}
	
	
	public BusinessDesc getBusiness(String name){
		
		return (BusinessDesc) contextStorage.get(0, name);
	}
	
	public SystemDesc getSystem(String name){
		
		return (SystemDesc) contextStorage.get(1, name);
	}
	
	public DataDesc getData(String name){
		
		return (DataDesc) contextStorage.get(2, name);
	}
}
