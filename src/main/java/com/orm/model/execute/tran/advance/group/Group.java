package com.orm.model.execute.tran.advance.group;

import java.util.List;

import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;

public interface Group {

	public List<Group> getGroupList();
	
	public int getGroupNum();
	
	public int getTotalRuleNum();
	
	public List<ResultInfo> getAllResultInfo();
	
	public boolean isAllFinish();
	
	public boolean isAllSuccess();
	
	public void execute();
	
	public void load(ModelLoader modelLoader,int type);
	
	public void setExecuteNum(int num);
	
	public void setTranFlag(long flag);
	
	public long getTranFlag();
}
