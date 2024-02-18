package dec.expand.declare.system;

import dec.expand.declare.conext.DataStorage;
import dec.expand.declare.executer.change.Change;
import dec.expand.declare.executer.conume.Conumer;
import dec.expand.declare.executer.produce.Produce;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;

public class SystemBuilder {

	private System system = new System();
	
	
	public static SystemBuilder create(){
		return new SystemBuilder();
	}
	
	public SystemBuilder build(String name) {
		system.setName(name);
		return this;
	}
	
	public SystemBuilder addProduce(Produce<DataStorage> produce){
		
		system.add(produce);
		
		return this;
	}

	public SystemBuilder addChange(Change<DataStorage> change){

		system.add(change);

		return this;
	}

	public SystemBuilder addProduce(String name, Function<ExecuteResult, DataStorage> fun) {
		
		Produce<DataStorage> produce = new Produce<>();
		
		produce.setName(name);
		
		produce.setFun(fun);
		
		system.add(produce);
		
		return this;
	}

	public SystemBuilder addChange(String name, Function<ExecuteResult, DataStorage> fun) {

		Change<DataStorage> change = new Change<>();

		change.setName(name);

		change.setFun(fun);

		this.addChange(change);

		return this;
	}

	public System getSystem() {
		return system;
	}
	
}
