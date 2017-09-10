package com.orm.api.mongodb.execute;

import java.util.Iterator;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.orm.api.dom.ApiQueryInfo;
import com.orm.api.dom.ConditionInfo;
import com.orm.api.dom.FieldInfo;
import com.orm.api.execute.AbstractApiExecute;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.sql.dom.DataInfo;

public abstract class AbstractMongoDBExecute extends AbstractApiExecute<DB> implements MongoDBExecute{
	
	protected DB mongoClient;
	
	protected BasicDBObject dataObject = new BasicDBObject();
	
	protected BasicDBObject queryObject = new BasicDBObject();
	
	protected BasicDBObject fieldObject = new BasicDBObject();
	
	public void setConnection(DB con) {
		this.mongoClient = con;
	}

	public void execute(ApiQueryInfo e) throws ExecuteException {
		this.mongoClient = (DB) e.getCon();
		this.dataInfoCollection = e.getDataInfoCollection();
		this.queryInfo = e;
		
		
		if(dataInfoCollection != null && !dataInfoCollection.isEmpty()){
			Iterator<DataInfo> it = this.dataInfoCollection.iterator();
			
			while(it.hasNext()){
				DataInfo dataInfo = it.next();
				dataObject.append(dataInfo.getColumn(), dataInfo.getValue());
			}
		}

		if(queryInfo.getConditionInfo() != null && !queryInfo.getConditionInfo().isEmpty()){
			Iterator<ConditionInfo> itC = this.queryInfo.getConditionInfo().iterator();
			while(itC.hasNext()){
				ConditionInfo conditionInfo = itC.next();
				
				if("=".equals(conditionInfo.getFlag()))
					queryObject.append(conditionInfo.getColumn(), conditionInfo.getValue());
				else{
					BasicDBObject cObject = new BasicDBObject();
					cObject.append(CompareFlag.getFlag(conditionInfo.getFlag()), conditionInfo.getValue());
					queryObject.append(conditionInfo.getColumn(), cObject);
				}
			}
		}

		if(queryInfo.getFieldInfo() != null && !queryInfo.getFieldInfo().isEmpty()){
			Iterator<FieldInfo> itC = this.queryInfo.getFieldInfo().iterator();
			while(itC.hasNext()){
				FieldInfo fieldInfo = itC.next();
				fieldObject.put(fieldInfo.getColumn(), 1);
			}
		}

		execute();
	}

}
