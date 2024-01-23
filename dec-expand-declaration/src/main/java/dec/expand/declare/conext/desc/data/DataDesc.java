package dec.expand.declare.conext.desc.data;

import dec.expand.declare.collections.SimpleList;
import dec.expand.declare.conext.desc.Desc;

import java.util.List;

public class DataDesc extends Desc {

    private String dataMappings[];

    private DataTypeEnum type;

    private boolean isCachePrior;

    private List<DataDependDesc> dataDependDescs;

    private SimpleList<ChangeDesc> changeDescList;

    public DataDesc() {

    }

    public DataDesc(String name, String comment) {
        this(name, comment, DataTypeEnum.CACHE, false);

    }


    public DataDesc(String name, String comment, DataTypeEnum type, boolean isCachePrior) {
        this.setName(name);
        this.setComment(comment);
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

    public void addDepend(DataDependDesc dataDependDesc) {
        if (dataDependDescs == null) {
            dataDependDescs = new SimpleList<DataDependDesc>();
        }
        dataDependDescs.add(dataDependDesc);
    }

    public List<DataDependDesc> getDataDepends() {
        return dataDependDescs;
    }

    public SimpleList<ChangeDesc> getChangeDescList() {
        return changeDescList;
    }

    public void addChange(String name, SimpleList<ValueDesc> changeList) {
        ChangeDesc changeDesc = new ChangeDesc();
        changeDesc.setName(name);
        changeDesc.setValueDescList(changeList);
        if (changeDescList == null) {
            changeDescList = new SimpleList<>();
        }
        changeDescList.add(changeDesc);
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
