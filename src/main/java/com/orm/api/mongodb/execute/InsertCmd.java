package com.orm.api.mongodb.execute;

import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.util.Constanst;

public class InsertCmd extends AbstractMongoDBExecute{

	public void execute() throws ExecuteException {
		DBCollection dbC = this.mongoClient.getCollection(queryInfo.getDataName());
		/*
		BasicDBObject object = new BasicDBObject();

		Iterator<DataInfo> it = this.dataInfoCollection.iterator();
		
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			object.append(dataInfo.getColumn(), dataInfo.getValue());
		}*/
		try{
			WriteResult writeResult = dbC.insert(dataObject);
			
		   if(writeResult.getError() != null
				   && !"".equals(writeResult.getError())){
			   throw new ExecuteException(writeResult.getError());
		   }
		   if(keyType == null || keyType.equals(Constanst.KEY_TYPE_AUTO)){
			   this.executeInfo.setKeyValue(dataObject.get("_id"));
		   }
		}catch(Exception e){
			throw new ExecuteException(e);
		}
	}

}
