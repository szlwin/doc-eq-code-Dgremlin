package dec.external.datasource.sql.convert.single;

import java.util.Iterator;

import dec.core.context.config.model.data.DataTable;
//import com.orm.common.xml.model.data.DataTable;
import dec.core.context.data.BaseData;
import dec.core.datasource.dom.DataInfo;
import dec.external.datasource.sql.utils.Util;

public class SingleDeleteConvert extends SingleConvert{

	public String convert(BaseData baseData) {
		
		StringBuffer sqlBuffer = new StringBuffer("delete from ");
		
		DataTable dataTable = Util.getTableInfo(baseData.getName(), dataSourceName);
		
		String tableName = dataTable.getName();
				
		sqlBuffer.append(tableName);
		
		StringBuffer dataBuffer = new StringBuffer();
		
		this.dataInfoSet = Util.convert(baseData, dataSourceName,true);
		
		String key = dataTable.getKey();
		
		convertToSql(key,dataBuffer);
		
		sqlBuffer.append(dataBuffer.toString());
		
		return sqlBuffer.toString();
	}
	
	protected void convertToSql(String keyId,StringBuffer dataBuffer){
		Iterator<DataInfo> it = dataInfoSet.iterator();
		
		StringBuffer keySql = new StringBuffer();
		DataInfo keyValue = null;
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			
			//Object value = dataInfo.getValue();
			
			String columnName = dataInfo.getColumn();
			
			if(columnName.equals(keyId)){
				keySql.append(" where ");
				keySql.append(columnName);
				keySql.append(" = ");
				keySql.append("?");
				keyValue = dataInfo;
			}
		}
		
		dataInfoSet.clear();
		dataInfoSet.add(keyValue);
		dataBuffer.append(keySql.toString());
	}


	protected String convert() {
		return convert((BaseData) this.convertParam.getData());
	}
}
