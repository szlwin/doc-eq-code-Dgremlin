package com.orm.context.data;

import java.util.Map;

public class ToolUtil {

	public static Map<String,Object> getValues(BaseData data){
		if(data == null)
			return null;
		return data.getValues();
	}
}
