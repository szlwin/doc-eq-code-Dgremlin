package dec.expand.declare.executer;

import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;

public class Executer<T> {

	private Function<ExecuteResult,T> fun;
	
	private String name;


	public Function<ExecuteResult, T> getFun() {
		return fun;
	}

	public void setFun(Function<ExecuteResult, T> fun) {
		this.fun = fun;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
