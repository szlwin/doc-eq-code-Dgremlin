package dec.core.model.execute.rule.common;


import dec.core.context.config.model.rule.RuleCheckInfo;
import dec.core.datasource.execute.exception.ExecuteException;
//import smarter.common.check.CheckFactory;
/*import dec.core.common.check.rule.CheckFactory;
import dec.core.common.execute.exception.ExecuteException;
import dec.core.common.xml.model.rule.RuleCheckInfo;*/
//import smarter.common.express.check.RuleCheck;
import dec.core.model.check.rule.CheckFactory;
import smarter.common.express.check.RuleCheck;

public class CheckExecute extends AbstractRuleExecute{
	
	
	/*public CheckExecute(RuleCheckInfo checkInfo,Object value){
		this.checkInfo = checkInfo;
		this.value = value;
	}*/
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean execute() throws ExecuteException{
		RuleCheckInfo checkInfo = (RuleCheckInfo)this.ruleInfo;
		RuleCheck check = CheckFactory.getCheck(checkInfo.getType());
		
		//check.setCheckValue(value);
		//check.setPattern(checkInfo.getPattern());

		try {
			return check.check(checkInfo.getPattern(), value);
		} catch (smarter.common.exception.ExecuteExpection e) {
			throw new ExecuteException(e);
		}
	}
}
