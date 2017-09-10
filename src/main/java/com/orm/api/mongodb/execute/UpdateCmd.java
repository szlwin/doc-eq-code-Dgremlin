package com.orm.api.mongodb.execute;

import java.util.Iterator;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.api.dom.ConditionInfo;
import com.orm.sql.dom.DataInfo;

public class UpdateCmd extends AbstractMongoDBExecute{

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
		}
		
		BasicDBObject object = new BasicDBObject();
		Iterator<DataInfo> it = this.dataInfoCollection.iterator();
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			if(dataInfo.getProterty().equals(queryInfo.getKeyId())){
				object.append(dataInfo.getColumn(), dataInfo.getValue());
			}
			
		}*/
		//dbC.update(queryObject,dataObject,false,true);
		
		DBCursor cursor = dbC.find(queryObject);
		try {
		   while(cursor.hasNext()) {
			   DBObject qObject = cursor.next();
			   qObject.putAll(dataObject.toMap());
			   WriteResult writeResult = dbC.save(qObject);
			   if(writeResult.getError() != null
					   && !"".equals(writeResult.getError())){
				   throw new ExecuteException(writeResult.getError());
			   }
		   }
		} catch(Exception e){
			throw new ExecuteException(e);
		}finally {
		   cursor.close();
		}
	}

}
