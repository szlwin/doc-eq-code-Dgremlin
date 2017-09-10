package com.orm.sql.convert.single;

import java.util.Iterator;

import com.orm.context.data.BaseData;
import com.orm.context.data.NullData;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.util.Util;

public abstract class AbstractSingleInsertConvert extends SingleConvert{

	//protected String keyType;

	//public String getKeyType() {
	//	return keyType;
	//}

	//public void setKeyType(String keyType) {
	//	this.keyType = keyType;
	//}

	protected void convertToSql(BaseData baseData,StringBuffer columnBuffer,StringBuffer dataBuffer){
		
		this.dataInfoSet = Util.convert(baseData, dataSourceName);
		
		Iterator<DataInfo> it = dataInfoSet.iterator();
		
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			
			Object value = dataInfo.getValue();
			
			if(value == null){
				it.remove();
				continue;
			}
			
			if(value instanceof NullData)
				dataInfo.setValue(null);
			
			columnBuffer.append(dataInfo.getColumn());
			columnBuffer.append(",");
			
			dataBuffer.append("?,");
		}
		
		columnBuffer.setCharAt(columnBuffer.length()-1, ' ');
		dataBuffer.setCharAt(dataBuffer.length()-1,' ');
	}
}
