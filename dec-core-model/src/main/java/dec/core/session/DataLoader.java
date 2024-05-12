package dec.core.session;

import dec.core.context.config.manager.ConfigManager;
//import dec.core.common.xml.util.ConfigManager;
import dec.core.context.data.BaseData;
//import dec.core.sql.dom.DataConInfo;

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
