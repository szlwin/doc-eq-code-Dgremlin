package com.orm.common.check.rule;

import smarter.common.express.check.AbstractCheck;
import smarter.common.util.SimpleCheckFactory;
import smarter.data.number.check.NumberCheck;

public class NumberPropertyCheck extends AbstractCheck<Number>{

	public boolean check() {
		return checkPatten();
	}
	
	private boolean checkPatten(){
		boolean checkResult = true;
		String checkArray[] = this.pattern.split(";");
		
		for(int i= 0; i < checkArray.length; i++){
			 String checkInfo[] = checkArray[i].split(":");
			 String checkValue = null;
			 if(checkInfo.length > 1){
				 checkValue = checkInfo[1].trim();
			 }
			 
			 checkResult = checkResult & checkValue(checkInfo[0].trim(),checkValue);
		}

		return checkResult;
		
	}
	
	private boolean checkValue(String tag,String checkValue){
		 NumberCheck check = (NumberCheck) SimpleCheckFactory.getSimpleCheck(tag);
		 
		 if(checkValue != null)
			 return check.check(this.value,new Double(checkValue));
		 else
			 return check.check(this.value,null);
			 
	}

}
