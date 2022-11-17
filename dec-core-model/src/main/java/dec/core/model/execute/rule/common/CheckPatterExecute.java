package dec.core.model.execute.rule.common;


import java.util.Map;

import dec.core.context.config.model.rule.RuleCheckInfo;
import dec.core.datasource.execute.exception.ExecuteException;
//import smarter.common.check.CheckFactory;
/*import dec.core.common.check.rule.CheckFactory;
import dec.core.common.execute.exception.ExecuteException;
import dec.core.common.xml.model.rule.RuleCheckInfo;*/
//import smarter.common.express.check.RuleCheck;
import dec.core.model.check.rule.CheckFactory;
import dec.core.model.utils.validater.SimpleValidater;
import smarter.common.express.check.RuleCheck;

public class CheckPatterExecute extends AbstractRuleExecute{
	
	
	/*public CheckExecute(RuleCheckInfo checkInfo,Object value){
		this.checkInfo = checkInfo;
		this.value = value;
	}*/
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean execute() throws ExecuteException{
		
		
		RuleCheckInfo checkInfo = (RuleCheckInfo)this.ruleInfo;
		
		//SimpleValidater.get().addExpress(checkInfo.getPattern(), (Map<String, Object>) value);
		
		RuleCheck check = CheckFactory.getCheck(checkInfo.getType());
		
		check.setCheckValue(value);
		check.setPattern(checkInfo.getPattern());

		/*try{
			return SimpleValidater.get()
					.addExpress(checkInfo.getPattern(), (Map<String, Object>) value)
					.execute();
		}catch(Exception e){
			throw new ExecuteException(e);
		}*/

		
		try {
			return check.check();
		} catch (smarter.common.exception.ExecuteExpection e) {
			throw new ExecuteException(e);
		}
	}
}
