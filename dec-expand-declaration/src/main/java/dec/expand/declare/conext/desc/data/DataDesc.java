package dec.expand.declare.conext.desc.data;

import java.util.List;

import dec.expand.declare.collections.SimpleList;
import dec.expand.declare.conext.desc.Desc;

public class DataDesc extends Desc{

	//private ContextStorage contextStorage = new ContextStorage(2);
	
	private String dataMappings[];
	
	private DataTypeEnum type;
	
	private boolean isCachePrior;
	
	private List<DataDepend> dataDepends;
	
	public DataDesc(){
		
	}
	
	public DataDesc(String name, String comment, String dataMappings[]){
		this(name, comment, dataMappings, DataTypeEnum.CACHE, false);
		
	}
	
	public DataDesc(String name, String comment){
		this(name, comment, null);
	}
	
	public DataDesc(String name, String comment, String dataMappings[], DataTypeEnum type, boolean isCachePrior){
		this.setName(name);
		this.setComment(comment);
		this.setDataMappings(dataMappings);
		this.setCachePrior(isCachePrior);
		this.setType(type);
	}
	
	public String[] getDataMappings() {
		return dataMappings;
	}

	public void setDataMappings(String[] dataMappings) {
		this.dataMappings = dataMappings;
	}

	public DataTypeEnum getType() {
		return type;
	}

	public void setType(DataTypeEnum type) {
		this.type = type;
	}

	public boolean isCachePrior() {
		return isCachePrior;
	}

	public void setCachePrior(boolean isCachePrior) {
		this.isCachePrior = isCachePrior;
	}

	public void addDepend(DataDepend dataDepend){
		if(dataDepends == null){
			dataDepends = new SimpleList<DataDepend>();
		}
		dataDepends.add(dataDepend);
		
	}

	public List<DataDepend> getDataDepends() {
		return dataDepends;
	}
	
	
	/*public void add(ProduceDesc produceDesc){
		
		contextStorage.add(0, produceDesc.getName(), produceDesc);
		
	}
	
	public void add(ConumerDesc conumerDesc){
		
		contextStorage.add(1, conumerDesc.getName(), conumerDesc);
	}
	
	public void addProduces(List<ProduceDesc> produceDescs){
		
		for(ProduceDesc produceDesc : produceDescs){
			add(produceDesc);
		}
		
	}

	public void addConumers(List<ConumerDesc> conumers){
		
		for(ConumerDesc conumerDesc : conumers){
			add(conumerDesc);
		}
		
	}
	
	
	public ProduceDesc getProduce(String name) {
		return (ProduceDesc) contextStorage.get(0, name);
	}
	
	public ConumerDesc getConumer(String name) {
		return (ConumerDesc) contextStorage.get(1, name);
	}*/
	
	
}
