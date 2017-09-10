package com.orm.common.convert;

import java.util.Collection;

import com.orm.sql.dom.DataInfo;

public abstract class AbstractConvert<E,R> implements DataConvert<E,R>{
	
	protected String dataSourceName;
	
	protected Collection<DataInfo> dataInfoSet;
	
	//protected ConvertParam convertParam;
	
	//protected Object keyValue;
	
	//protected String keyId;
	
	//protected String keyType;
	
	//protected Collection<FieldInfo> fieldInfoSet;
	
	public void setDataSource(String dataSource){
		this.dataSourceName = dataSource;
	}
	
	public String getDataSource(){
		return dataSourceName;
	}
	
	public Collection<DataInfo> get() {
		return dataInfoSet;
	}
	/*
	public ConvertInfo convert(ConvertParam convertParam) {
		this.convertParam = convertParam;
		ConvertInfo convertInfo = new ConvertInfo();
		String cmd = convert();

		convertInfo.setCmd(cmd);
		convertInfo.setDataInfo(dataInfoSet);
		convertInfo.setKeyValue(keyValue);
		convertInfo.setKeyId(keyId);
		convertInfo.setKeyType(keyType);
		convertInfo.setFieldInfoSet(fieldInfoSet);
		return convertInfo;
	}*/
	
	//protected abstract String convert();
}
