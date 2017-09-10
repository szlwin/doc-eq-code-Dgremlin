package com.orm.api.mongodb.execute;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import com.orm.common.execute.exception.ExecuteException;

public class RemoveCmd extends AbstractMongoDBExecute{

	public void execute() throws ExecuteException {
		DBCollection dbC = this.mongoClient.getCollection(queryInfo.getDataName());
		/*
		BasicDBObject object = new BasicDBObject();
		
		Iterator<ConditionInfo> it = this.queryInfo.getConditionInfo().iterator();
		
		while(it.hasNext()){
			ConditionInfo conditionInfo = it.next();
			
			if("=".equals(conditionInfo.getFlag()))
				object.append(conditionInfo.getColumn(), conditionInfo.getValue());
			else{
				BasicDBObject cObject = new BasicDBObject();
				cObject.append(CompareFlag.getFlag(conditionInfo.getFlag()), conditionInfo.getValue());
				object.append(conditionInfo.getColumn(), cObject);
			}
		}*/
		try{
			WriteResult writeResult = dbC.remove(queryObject);
			
		   if(writeResult.getError() != null
				   && !"".equals(writeResult.getError())){
			   throw new ExecuteException(writeResult.getError());
		   }
		}catch(Exception e){
			throw new ExecuteException(e);
		}
	}

}
