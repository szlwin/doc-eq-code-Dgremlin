package com.orm.api.redis.convert.single;

import java.util.Collection;

import com.orm.api.dom.FieldInfo;
import com.orm.api.redis.convert.AbstractRedisConvert;
import com.orm.api.redis.convert.RedisConvertUtil;
import com.orm.common.xml.model.data.DataTable;
import com.orm.context.data.BaseData;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.util.Util;

public class GetConvert extends AbstractRedisConvert{

	@SuppressWarnings("unchecked")
	protected String convert() {
		BaseData baseData = (BaseData) this.convertParam.getData();
		DataTable dataTable = Util.getTableInfo(baseData.getName(), dataSourceName);
		
		this.keyId = dataTable.getPropertyKey();
		this.keyValue = (String) baseData.getValue(dataTable.getPropertyKey());
		this.fieldInfo = (Collection<FieldInfo>) RedisConvertUtil.convertFieldInfo(baseData, dataSourceName, convertParam.getConParamMap().get("dataType"));
		this.dataInfoSet = (Collection<DataInfo>) RedisConvertUtil.convert(baseData, dataSourceName, convertParam.getConParamMap().get("dataType"));
		return RedisConvertUtil.getCmd(convertParam.getConParamMap().get("dataType"),convertParam.getType());
	}
}
