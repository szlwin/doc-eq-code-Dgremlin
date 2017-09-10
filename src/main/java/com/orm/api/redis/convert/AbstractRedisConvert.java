package com.orm.api.redis.convert;

import java.util.Collection;

import com.orm.common.xml.model.data.DataTable;
import com.orm.context.data.BaseData;
import com.orm.api.convert.AbstractApiConvert;
import com.orm.api.redis.convert.RedisConvert;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.util.Util;


public abstract class AbstractRedisConvert extends AbstractApiConvert implements RedisConvert{
	
	//protected Object keyValue;
	
	//protected String keyId;
	
	/*public ConvertInfo convert(ConvertParam convertParam) {
		this.convertParam = convertParam;
		ConvertInfo convertInfo = new ConvertInfo();
		String cmd = convert();
		convertInfo.setCmd(cmd);
		convertInfo.setKeyValue(keyValue);
		convertInfo.setKeyId(keyId);
		convertInfo.setDataInfo(dataInfoSet);
		return convertInfo;
	}*/
	
	@SuppressWarnings("unchecked")
	protected String convert() {
		BaseData baseData = (BaseData) this.convertParam.getData();
		DataTable dataTable = Util.getTableInfo(baseData.getName(), dataSourceName);
		
		this.keyId = dataTable.getPropertyKey();
		this.keyValue = (String) baseData.getValue(dataTable.getPropertyKey());
		
		this.dataInfoSet = (Collection<DataInfo>) RedisConvertUtil.convert(baseData, dataSourceName, convertParam.getConParamMap().get("dataType"));
		return RedisConvertUtil.getCmd(convertParam.getConParamMap().get("dataType"),convertParam.getType());
	}
}
