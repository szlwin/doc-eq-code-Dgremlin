package com.orm.session;

import com.orm.common.xml.util.ConfigManager;
import com.orm.context.data.BaseData;
import com.orm.sql.dom.DataConInfo;

public class DataLoader extends AbstractLoader<BaseData, DataConInfo> {
	
	@Override
	public void load(String con, BaseData data) {
		
		DataConInfo dataConInfo = new DataConInfo();
		dataConInfo.setConnectionName(con);
		dataConInfo.setData(data);
		
		list.add(dataConInfo);
	}

	@Override
	public void load(BaseData data) {
		String conName = ConfigManager.getInstance().getDefaultConName();
		load(conName,data);
		
	}

}
