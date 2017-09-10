package com.orm.model.execute.tran;

import java.sql.Savepoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.connection.DataConnection;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.execute.rule.RuleContainer;
import com.orm.model.execute.rule.exception.ExecuteRuleException;

public class DefaultExecuter implements TranExecuter{
	
	private Log log = LogFactory.getLog(DefaultExecuter.class);
	
	protected ModelLoader modelLoader;
	
	protected DataConnection con;
	
	protected String name;
	
	protected Savepoint savepoint;
	
	protected int tranType;
	
	protected long flag;
	
	public DefaultExecuter(long paramFalg){
		this.flag = paramFalg;
	}
	
	public void load(ModelLoader modelLoader) {
		this.modelLoader = modelLoader;
		
	}

	public ResultInfo execute() throws ExecuteRuleException, ExecuteException {

		log.info("Execute the tran: id = "+flag+", view rule: "+modelLoader.getRuleName()+" start!");
		
		RuleContainer ruleExecute = new RuleContainer(modelLoader,con);
		
		ResultInfo resultInfo = ruleExecute.execute();
		
		log.info("Execute the tran: id = "+flag+", view rule: "+modelLoader.getRuleName()+" end!");
		
		return resultInfo;
	}

	public void setConnection(DataConnection con) {
		this.con = con;
		
	}

	public DataConnection getConnection() {
		return con;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Savepoint getSavepoint() {
		return savepoint;
	}

	public void setSavepoint(Savepoint savepoint) {
		this.savepoint = savepoint;
	}

	public int getTranType() {
		return tranType;
	}

	public void setTranType(int tranType) {
		this.tranType = tranType;
	}

	public String getConName() {
		return modelLoader.getConName();
	}

	public long getFlag() {
		return flag;
	}
	

}
