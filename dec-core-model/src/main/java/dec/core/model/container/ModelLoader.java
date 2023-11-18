package dec.core.model.container;

import java.util.HashMap;
import java.util.Map;

import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.model.container.listener.ViewListener;

//import dec.core.sql.util.Util;

public class ModelLoader {

	private String ruleName;
	
	private Object e;
	
	private String conName;

	private ViewListener listener;
	
	private Map<String,Object> external;
	
	
	public Map<String, Object> getExternal() {
		return external;
	}

	public ModelLoader addExternalParam(Map<String, Object> external) {
		if(this.external == null){
			this.external = new HashMap<>();
		}
		this.external.putAll(external);
		return this;
	}

	public ModelLoader load(String name,Object e){
		load(name,e,ConfigContextUtil.getDefaultCon());
		return this;
	}

	public String getRuleName() {
		return ruleName;
	}

	public Object get() {
		return e;
	}
	
	public ModelLoader load(String name,Object e,String conName){
		this.ruleName = name;
		this.e = e;
		this.conName = conName;
		
		return this;
	}
	
	public ModelLoader addListener(ViewListener listener){
		this.listener = listener;
		return this;
	}
	
	public String getConName() {
		return conName;
	}

	public ViewListener getListener() {
		return listener;
	}
	
	
}