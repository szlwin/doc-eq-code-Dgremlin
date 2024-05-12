package dec.expand.declare.conext.desc.produce;

import java.util.ArrayList;
import java.util.List;

public class ProduceDescBuilder {
	
	private List<ProduceDesc> produceList = new ArrayList<>();
	
	public static ProduceDescBuilder create(){
		return new ProduceDescBuilder();
	}
	
	public ProduceDescBuilder build(String... args){
		
		for(String name : args){
			addProduce(new ProduceDesc(name));
		}
		
		return this;
	}
	
	public ProduceDescBuilder build(String name, String desc){
		
		addProduce(new ProduceDesc(name, desc));
		
		return this;
	}
	
	public List<ProduceDesc> getProduces() {
		return produceList;
	}

	private void addProduce(ProduceDesc produce){
		produceList.add(produce);
	}
}
