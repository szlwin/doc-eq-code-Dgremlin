package com.orm.api.redis.convert.container;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.convert.container.ConvertContainer;
import com.orm.api.dom.ApiConvertInfo;
import com.orm.api.redis.convert.RedisConvert;
import com.orm.api.redis.convert.single.DeleteConvert;
import com.orm.api.redis.convert.single.GetConvert;
import com.orm.api.redis.convert.single.UpdateConvert;
import com.orm.sql.dom.ConvertInfo;
import com.orm.sql.dom.ConvertParam;

public class RedisConvertContainer implements ConvertContainer<ConvertParam, ApiConvertInfo>{

	private Map<String,Class<? extends RedisConvert>> convertMap = new HashMap<String,Class<? extends RedisConvert>>(3);
	
	public ApiConvertInfo convert(ConvertParam e) {
		RedisConvert convert = null;
		Class<? extends RedisConvert> convertClass = convertMap.get(e.getType());
		try {
			convert = convertClass.newInstance();
		} catch (Exception e1) {
			//e1.printStackTrace();
		}
		convert.setDataSource(e.getDataSource());
		return convert.convert(e);
	}

	public void init() {
		convertMap.put("delete", DeleteConvert.class);
		convertMap.put("update", UpdateConvert.class);
		convertMap.put("insert", UpdateConvert.class);
		convertMap.put("get", GetConvert.class);
	}

}
