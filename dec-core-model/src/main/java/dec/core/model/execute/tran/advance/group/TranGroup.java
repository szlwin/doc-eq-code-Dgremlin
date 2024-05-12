package dec.core.model.execute.tran.advance.group;

import dec.core.model.container.ModelLoader;
import dec.core.model.execute.tran.Transaction;

public class TranGroup extends AbstractGroup{

	private boolean isHasMain;
	
	private TranLevelGroup mainTranGroup;
	
	private TranLevelGroup noTranGroup;
	
	private TranLevelGroup reNewTranGroup;
	
	public void execute() {
		// TODO Auto-generated method stub
	}

	public void load(ModelLoader modelLoader,int type){
	
		if(type == Transaction.PROPAGATION_REQUIRED
				|| type == Transaction.PROPAGATION_REQUIRED_NESTED){
			addMainTran(modelLoader,type);
			isHasMain = true;
			this.totalRuleNum++;
			return;
		}
		
		if(type == Transaction.PROPAGATION_NOT_SUPPORTED){
			addNoTran(modelLoader);
			this.totalRuleNum++;
			return;
		}
		
		if(type == Transaction.PROPAGATION_REQUIRES_NEW){
			addRewTran(modelLoader,null);
			this.totalRuleNum++;
			return;
		}
		
		if(type == Transaction.PROPAGATION_SUPPORTS){
			if(isHasMain){
				addMainTran(modelLoader,Transaction.PROPAGATION_REQUIRED);
			}else{
				addNoTran(modelLoader);
			}
			this.totalRuleNum++;
		}
	}
	
	private void addMainTran(ModelLoader modelLoader,int type){
		if(mainTranGroup == null){
			mainTranGroup = new MainTranGroup();
			this.addGroup(mainTranGroup);
			groupNum++;
		}
		mainTranGroup.load(modelLoader, type);
	}

	private void addNoTran(ModelLoader modelLoader){
		if(noTranGroup == null){
			noTranGroup = new NoTranGroup();
			this.addGroup(noTranGroup);
			groupNum++;
		}
		noTranGroup.load(modelLoader, 
				Transaction.PROPAGATION_NOT_SUPPORTED);
	}
	
	private void addRewTran(ModelLoader modelLoader,String name){
		
	}
	
	

}
