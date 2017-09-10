package com.orm.common.check.rule;

import smarter.common.express.check.AbstractCheck;
import smarter.common.util.SimpleCheckFactory;
import smarter.data.string.check.StringCheck;

public class StringPropertyCheck extends AbstractCheck<String>{

	public boolean check() {
		return  checkPatten();
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
		 StringCheck check = (StringCheck) SimpleCheckFactory.getSimpleCheck(tag);
		 
		return check.check(this.value,checkValue);
	}
}
