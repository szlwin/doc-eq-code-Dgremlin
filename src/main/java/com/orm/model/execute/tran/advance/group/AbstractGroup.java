package com.orm.model.execute.tran.advance.group;

import java.util.ArrayList;
import java.util.List;

import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;

public abstract class AbstractGroup implements Group{

	protected int groupNum;
	
	protected int totalRuleNum;
	
	protected int executeNum;
	
	protected boolean isAllFinish;
	
	protected boolean isAllSuccess;

	protected long tranFlag;
	
	private List<ResultInfo> allResultInfo;
	
	private List<Group> groupList;
	
	public AbstractGroup(){
		allResultInfo = new ArrayList<ResultInfo>(15);
		groupList = new ArrayList<Group>();
		//modelList = new ArrayList<ModelLoader>(15);
	}
	
	public int getGroupNum() {
		return groupNum;
	}
	
	public int getTotalRuleNum() {
		return totalRuleNum;
	}

	public boolean isAllFinish() {
		return isAllFinish;
	}

	public boolean isAllSuccess() {
		return isAllSuccess;
	}
	
	protected void addResultInfo(ResultInfo resultInfo){
		allResultInfo.add(resultInfo);
	}
	
	public List<Group> getGroupList(){
		return groupList;
	}
	
	protected void addGroup(Group group){
		groupList.add(group);
	}
	
	public List<ResultInfo> getAllResultInfo() {
		return allResultInfo;
	}
	
	public void setExecuteNum(int num){
		executeNum = num;
	}
	
	public void setTranFlag(long flag){
		tranFlag = flag;
	}
	
	public long getTranFlag(){
		return tranFlag;
	}
	//protected abstract List<ModelLoader> getModelLoadList();
}
