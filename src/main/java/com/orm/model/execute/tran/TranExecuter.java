package com.orm.model.execute.tran;

import java.sql.Savepoint;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.connection.DataConnection;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.execute.rule.exception.ExecuteRuleException;

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
