package com.orm.api.redis.convert;

import java.util.Collection;

import com.orm.api.dom.FieldInfo;
import com.orm.context.data.BaseData;

public interface DataConvert<R,E> {

	public R convert(BaseData data,String dataSource);
	
	public E convertFieldInfo(BaseData data, String dataSourceName);
}
