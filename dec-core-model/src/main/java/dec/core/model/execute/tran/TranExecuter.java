package dec.core.model.execute.tran;

import java.sql.Savepoint;

import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.execute.exception.ExecuteException;
//import dec.core.common.execute.exception.ExecuteException;
//import dec.core.connection.DataConnection;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.execute.rule.exception.ExecuteRuleException;

public interface TranExecuter {

	public void load(ModelLoader modelLoader);
	
	public ResultInfo execute() throws ExecuteRuleException, ExecuteException;
	
	public void setConnection(DataConnection con);
	
	public DataConnection getConnection();
	
	public void setSavepoint(Savepoint savepoint);
	
	public Savepoint getSavepoint();
	
	public void setTranType(int type);
	
	public int getTranType();
	
	public void setName(String name);
	
	public String getName();
	
	public String getConName();
	
	public long getFlag();
}
