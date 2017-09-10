package com.orm.api.mongodb.execute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.api.dom.ConditionInfo;

public class FindOneCmd extends AbstractMongoDBExecute{

	public void execute() throws ExecuteException {
		DBCollection dbC = this.mongoClient.getCollection(queryInfo.getDataName());
		
		/*
		BasicDBObject qObject = new BasicDBObject();
		
		Iterator<ConditionInfo> itC = this.queryInfo.getConditionInfo().iterator();
		while(itC.hasNext()){
			ConditionInfo conditionInfo = itC.next();
			
			if("=".equals(conditionInfo.getFlag()))
				qObject.append(conditionInfo.getColumn(), conditionInfo.getValue());
			else{
				BasicDBObject cObject = new BasicDBObject();
				cObject.append(CompareFlag.getFlag(conditionInfo.getFlag()), conditionInfo.getValue());
				qObject.append(conditionInfo.getColumn(), cObject);
			}
		}*/
		DBObject rObject = null;

		try{
			rObject = dbC.findOne(queryObject,fieldObject);
		}catch(Exception e){
			throw new ExecuteException(e);
		}
		
		Collection dataCollection = new ArrayList();
		
		dataCollection.add(rObject.toMap());
		
		this.executeInfo.setDataCollection(dataCollection);
	}

}
