package com.orm.api.redis.convert.single;

import com.orm.api.redis.convert.AbstractRedisConvert;
import com.orm.api.redis.convert.RedisConvertUtil;
import com.orm.common.xml.model.data.DataTable;
import com.orm.context.data.BaseData;
import com.orm.sql.util.Util;

public class DeleteConvert extends AbstractRedisConvert{

	@Override
	protected String convert() {
		BaseData baseData = (BaseData) this.convertParam.getData();
		DataTable dataTable = Util.getTableInfo(baseData.getName(), dataSourceName);		
		this.keyId = dataTable.getPropertyKey();
		this.keyValue = (String) baseData.getValue(dataTable.getPropertyKey());
		
		return RedisConvertUtil.getCmd(convertParam.getConParamMap().get("dataType"),convertParam.getType());
	}

}
