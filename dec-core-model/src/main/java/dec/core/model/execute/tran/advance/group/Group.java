package dec.core.model.execute.tran.advance.group;

import java.util.List;

import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;

public interface Group {

	List<Group> getGroupList();
	
	int getGroupNum();
	
	int getTotalRuleNum();
	
	List<ResultInfo> getAllResultInfo();
	
	boolean isAllFinish();
	
	boolean isAllSuccess();
	
	public void execute();
	
	public void load(ModelLoader modelLoader,int type);
	
	public void setExecuteNum(int num);
	
	public void setTranFlag(long flag);
	
	public long getTranFlag();
}
