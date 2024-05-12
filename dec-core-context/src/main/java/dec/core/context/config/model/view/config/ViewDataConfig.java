package dec.core.context.config.model.view.config;

import java.util.Map;

import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.view.ViewData;
import javolution.util.FastMap;

public class ViewDataConfig implements Config<ViewData>{
	
	private static final ViewDataConfig viewDataConfig =  new ViewDataConfig();
	
	private Map<String,ViewData> viewDataConfigMap = new FastMap<String,ViewData>();
	
	private ViewDataConfig()
	{
		
	}

	public static ViewDataConfig getInstance()
	{
		return viewDataConfig;
	}

	public void add(ViewData viewData)
	{
		viewDataConfigMap.put(viewData.getName(),viewData);
	}
	
	public ViewData get(String name)
	{
		return viewDataConfigMap.get(name);
	}

	public <E extends ConfigBaseData> void add(E viewData) {
		add((ViewData)viewData);
	}
	

}
