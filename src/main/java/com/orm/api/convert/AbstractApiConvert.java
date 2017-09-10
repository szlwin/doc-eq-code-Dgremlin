package com.orm.api.convert;

import java.util.Collection;

import com.orm.common.convert.AbstractConvert;
import com.orm.api.dom.ApiConvertInfo;
import com.orm.api.dom.ConditionInfo;
import com.orm.sql.dom.ConvertParam;
import com.orm.api.dom.FieldInfo;

public abstract class AbstractApiConvert extends AbstractConvert<ConvertParam ,ApiConvertInfo> implements ApiConvert{

	protected ConvertParam convertParam;
	
	protected String keyType;
	
	protected Object keyValue;
	
	protected String keyId;
	
	protected Collection<FieldInfo> fieldInfo;
	
	protected Collection<ConditionInfo> conditionInfo;
	
	protected String dataName;
	
	public ApiConvertInfo convert(ConvertParam convertParam) {
		this.convertParam = convertParam;
		ApiConvertInfo convertInfo = new ApiConvertInfo();
		String sql = convert();
		convertInfo.setCmd(sql);
		convertInfo.setDataInfo(dataInfoSet);
		convertInfo.setKeyType(keyType);
		convertInfo.setKeyValue(keyValue);
		convertInfo.setConditionInfo(conditionInfo);
		convertInfo.setFieldInfo(fieldInfo);
		convertInfo.setKeyId(keyId);
		convertInfo.setDataName(dataName);
		return convertInfo;
	}

	protected abstract String convert();
}
