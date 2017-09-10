package com.orm.api.mongodb.convert.single;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.orm.api.mongodb.convert.AbstractMongoDBConvert;
import com.orm.common.xml.model.data.DataTable;
import com.orm.context.data.BaseData;
import com.orm.sql.util.Util;
import com.orm.api.dom.ConditionInfo;

public class SingleConvert extends AbstractMongoDBConvert{

	private static Map<String,String> cmdMap = new HashMap<String,String>();
	
	static{
		cmdMap.put("insert", "insert");
		cmdMap.put("delete", "remove");
		cmdMap.put("update", "update");
		cmdMap.put("get", "findOne");
	}
	
	@Override
	protected String convert() {
		BaseData baseData = (BaseData) this.convertParam.getData();
		DataTable dataTable = Util.getTableInfo(baseData.getName(), dataSourceName);
		
		this.keyId = dataTable.getPropertyKey();
		this.keyValue = (String) baseData.getValue(dataTable.getPropertyKey());
		this.dataName = dataTable.getName();
		
		String type = this.convertParam.getType();
		
		if(!type.equals("insert")){
			this.conditionInfo = new ArrayList<ConditionInfo>(1);
			ConditionInfo keyInfo = new ConditionInfo();
			keyInfo.setColumn(dataTable.getKey());
			keyInfo.setValue(keyValue);
			keyInfo.setFlag("=");
			conditionInfo.add(keyInfo);
		}
		
		if(type.equals("insert") || type.equals("update")){
			this.dataInfoSet =  Util.convert(baseData,dataSourceName,true);
		}
		
		if(type.equals("get")){
			this.fieldInfo =  Util.convertFieldInfo(baseData,dataSourceName);
			//this.dataInfoSet =  Util.convert(baseData,dataSourceName);
		}
		
		return cmdMap.get(this.convertParam.getType());
	}

}
