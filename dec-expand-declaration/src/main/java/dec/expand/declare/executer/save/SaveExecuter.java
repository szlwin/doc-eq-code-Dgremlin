package dec.expand.declare.executer.save;

import dec.expand.declare.executer.Executer;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;

public class SaveExecuter<T> extends Executer<T> {

	public SaveExecuter(){

	}

	public ExecuteResult execute(Object t) throws Exception{
		return getFun().execute((T) t);
	}

	public SaveExecuter(String name){
		this.setName(name);
	}

	public SaveExecuter(String name, Function<ExecuteResult,T> fun){
		this.setName(name);
		this.setFun(fun);
	}
	
	
}
