package dec.expand.declare.conext.desc;

import dec.expand.declare.executer.Executer;

public class ExecuterDesc extends Desc {
	
	private Executer<?> executer;

	public Executer<?> getExecuter() {
		return executer;
	}

	public void setExecuter(Executer<?> executer) {
		this.executer = executer;
	}
	
	
}
