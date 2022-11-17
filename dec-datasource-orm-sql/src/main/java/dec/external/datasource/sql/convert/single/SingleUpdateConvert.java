package dec.external.datasource.sql.convert.single;

import java.util.Iterator;

import dec.core.context.config.model.data.DataTable;
//import com.orm.common.xml.model.data.DataTable;
import dec.core.context.data.BaseData;
import dec.core.context.data.NullData;
import dec.core.datasource.dom.DataInfo;
import dec.external.datasource.sql.utils.Util;

public class SingleUpdateConvert extends SingleConvert{

	public String convert(BaseData baseData) {
		
		StringBuffer sqlBuffer = new StringBuffer("update ");
		
		DataTable dataTable = Util.getTableInfo(baseData.getName(), dataSourceName);
		
		String tableName = dataTable.getName();
				
		sqlBuffer.append(tableName);
		
		sqlBuffer.append(" set ");
		
		StringBuffer dataBuffer = new StringBuffer();
		
		String key = dataTable.getKey();
		
		this.dataInfoSet = Util.convert(baseData, dataSourceName);
		
		convertToSql(key,dataBuffer);
		
		sqlBuffer.append(dataBuffer.toString());
		
		return sqlBuffer.toString();
	}
	
	private void convertToSql(String keyId,StringBuffer dataBuffer){
		Iterator<DataInfo> it = dataInfoSet.iterator();
		
		StringBuffer keySql = new StringBuffer();
		DataInfo keyValue = null;
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			
			Object value = dataInfo.getValue();
			
			String columnName = dataInfo.getColumn();
			
			if(value == null){
				it.remove();
				continue;
			}
			
			if(value instanceof NullData)
				dataInfo.setValue(null);
			
			if(columnName.equals(keyId)){
				keySql.append("where ");
				keySql.append(columnName);
				keySql.append(" = ");
				keySql.append("?");
				keyValue = dataInfo;
				it.remove();
				continue;
			}
			
			dataBuffer.append(columnName);
			dataBuffer.append(" = ");
			dataBuffer.append("?,");
		}
		
		dataBuffer.setCharAt(dataBuffer.length()-1,' ');
		dataBuffer.append(keySql.toString());
		dataInfoSet.add(keyValue);
	}
	
	protected String convert() {
		return convert((BaseData) this.convertParam.getData());
	}

}
