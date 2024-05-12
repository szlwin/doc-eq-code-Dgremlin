package dec.external.datasource.sql.convert.single;

import java.util.Iterator;

import dec.core.context.data.BaseData;
import dec.core.context.data.NullData;
import dec.core.datasource.dom.DataInfo;
import dec.external.datasource.sql.utils.Util;

public abstract class AbstractSingleInsertConvert extends SingleConvert{

	//protected String keyType;

	//public String getKeyType() {
	//	return keyType;
	//}

	//public void setKeyType(String keyType) {
	//	this.keyType = keyType;
	//}

	protected void convertToSql(BaseData baseData,StringBuffer columnBuffer,StringBuffer dataBuffer){
		
		this.dataInfoSet = Util.convert(baseData, dataSourceName, true);
		
		Iterator<DataInfo> it = dataInfoSet.iterator();
		
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			
			Object value = dataInfo.getValue();
			
			//if(value == null){
			//	it.remove();
			//	continue;
			//}
			
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
