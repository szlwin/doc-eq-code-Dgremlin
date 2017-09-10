package com.orm.model.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.connection.DataConnection;
import com.orm.connection.exception.ConectionException;
import com.orm.connection.factory.DataConnectionFactory;
import com.orm.model.execute.rule.RuleContainer;
import com.orm.model.execute.rule.exception.ExecuteRuleException;

public class ModelContainer implements Container{
	private Log log = LogFactory.getLog(ModelContainer.class);
	
	protected ResultInfo resultInfo = new ResultInfo();
	
	protected Map<String,DataConnection> conMap = new HashMap<String,DataConnection>();
	
	protected boolean isAuto = true;
	
	public ModelContainer(){
		resultInfo.setSuccess(true);
	}
	
	private List<ModelLoader> list 
		= new ArrayList<ModelLoader>(8);
	
	public void load(ModelLoader modelLoader){
		
		String conName = modelLoader.getConName();
		
		if(!conMap.containsKey(conName))
			conMap.put(conName, null);
		
		list.add(modelLoader);
	}
	
	public void execute() throws ExecuteRuleException{
		boolean isOk = true;
		ResultInfo result = null;
		int i = 0;
		try{
			begain();
			for(; i < list.size(); i++){
				result = execute(list.get(i));
				if(!result.isSuccess()){
					log.error("Execute the rule: "+result.getRuleName()+" false!");
					break;
				}
			}
			
		}catch(Exception e){
			isOk = false;
			log.error(e);
			ModelLoader modelLoader = list.get(i);
			throw new ExecuteRuleException(e,modelLoader.getRuleName(),modelLoader.getConName());
		}finally{
			copy(result);
			boolean isSuccess = result!=null && result.isSuccess() && isOk;
			try {
				
				end(isSuccess);
			} catch (ConectionException e) {
				log.error(e.getMessage(),e);
				if(isSuccess){
					ModelLoader modelLoader = list.get(i);
					throw new ExecuteRuleException(e,modelLoader.getRuleName(),modelLoader.getConName());
				}
			}
		}
	}
	
	protected ResultInfo execute(ModelLoader modelLoader) throws ExecuteException, ExecuteRuleException{
		
		log.info("Execute the view rule: "+modelLoader.getRuleName()+" start!");
		
		RuleContainer ruleExecute = new RuleContainer(modelLoader,conMap.get(modelLoader.getConName()));
		
		ResultInfo resultInfo = ruleExecute.execute();
		
		log.info("Execute the view rule: "+modelLoader.getRuleName()+" end!");
		
		return resultInfo;
	}
	
	public ResultInfo getResult(){
		return resultInfo;
	}
	
	protected void copy(ResultInfo srcInfo){
		this.resultInfo = srcInfo;
	}
	
	
	protected void begain() throws ConectionException{
		Set<String> conNameSet = conMap.keySet();
		Iterator<String> it  = conNameSet.iterator();
		
		while(it.hasNext()){
			
			String conName = it.next();
			
			DataConnection con = DataConnectionFactory.getInstance().getConnection(conName);
				
			con.connect();
			conMap.put(conName, con);
			
		}
	}
	
	protected void end(boolean isSuccess) throws ConectionException{
		boolean isOK = true;
		try{
			if(isSuccess)
				commit();
		}catch(ConectionException e){
			isOK = false;
			throw e;
		}finally{
			clear();
			try{
				if(!isOK || !isSuccess)
					roolback();
				close();
			}catch(ConectionException e){
				throw e;
			}
			
		}
	}
	
	private void commit() throws ConectionException{
		operator(0);
	}
	
	private void close() throws ConectionException{
		operator(1);
	}
	
	private void roolback() throws ConectionException{
		operator(2);
	}
	
	protected void operator(int type) throws ConectionException{
		
		Collection<DataConnection> conCollection = conMap.values();
		Iterator<DataConnection> it = conCollection.iterator();
		
		while(it.hasNext()){
			DataConnection con = it.next();
			
			if(con == null)
				continue;
			
			switch(type){
				case 0:
					con.commit();
					break;
				case 1:
					try{
						con.close();
					}catch(ConectionException e){
						log.error(e);
					}
					break;
				case 2:
					try{
						con.rollback();
					}catch(ConectionException e){
						log.error(e);
					}
					break;
				default:
					con.close();
			}
		}
	}
	
	private void clear(){
		conMap.clear();
		list.clear();
	}
}
