package com.orm.common.config;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.view.ViewData;

public class ViewDataConfig implements Config<ViewData>{
	
	private static final ViewDataConfig viewDataConfig =  new ViewDataConfig();
	
	private Map<String,ViewData> viewDataConfigMap = new HashMap<String,ViewData>();
	
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
