package dec.expand.declare.executer.get;

import dec.expand.declare.executer.Executer;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;

public class GetExecuter<T> extends Executer<T> {

	public GetExecuter(){

	}

	public ExecuteResult execute(Object t) throws Exception{
		return getFun().execute((T) t);
	}

	public GetExecuter(String name){
		this.setName(name);
	}

	public GetExecuter(String name, Function<ExecuteResult,T> fun){
		this.setName(name);
		this.setFun(fun);
	}
	
	
}
