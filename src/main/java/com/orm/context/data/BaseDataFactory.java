package com.orm.context.data;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.data.DataProperty;



public class BaseDataFactory {

	private static final BaseDataFactory viewDataFactory =  new BaseDataFactory();
	
	private BaseDataFactory()
	{
		
	}

	public static BaseDataFactory getInstance()
	{
		return viewDataFactory;
	}
	
	public BaseData createData(String name) throws DataNotDefineException{
		
		ConfigInfo configInfo = DataUtil.getConfigInfo();
		Data baseDataConfig = configInfo.getData(name);
		
		if(baseDataConfig == null)
			throw new DataNotDefineException("The view data:"+name+" is not defined!");
		
		//baseData.setKeyName(keyName);
		return createData(baseDataConfig);
	}
	
	public BaseData createData(Data dataInfo) throws DataNotDefineException{
		BaseData baseData = new BaseData();
		
		baseData.setName(dataInfo.getName());
		baseData.setData(dataInfo);
		
		//��ȡ����������Ϣ
		Map<String, DataProperty> map 
			= dataInfo.getPropertyInfo().getProperty();
		
		convert(baseData,map);
		//baseData.setKeyName(keyName);
		return baseData;
	}
	
	private void convert(BaseData baseData,Map<String, DataProperty> map){
		Set<String> keySet = map.keySet();
		Iterator<String> it = keySet.iterator();
		while(it.hasNext()){
			String key = it.next();
			baseData.addKey(key);
		}
		
	}
}
