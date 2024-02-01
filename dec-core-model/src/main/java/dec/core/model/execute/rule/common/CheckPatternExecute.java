package dec.core.model.execute.rule.common;


import dec.core.context.config.model.rule.RuleCheckInfo;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.check.rule.CheckFactory;
import smarter.common.express.check.RuleCheck;

public class CheckPatternExecute extends AbstractRuleExecute{
	
	
	/*public CheckExecute(RuleCheckInfo checkInfo,Object value){
		this.checkInfo = checkInfo;
		this.value = value;
	}*/

	public boolean execute() throws ExecuteException{
		
		RuleCheckInfo checkInfo = (RuleCheckInfo)this.ruleInfo;

		RuleCheck check = CheckFactory.getCheck(checkInfo.getType());
		
		check.setCheckValue(value);
		check.setPattern(checkInfo.getPattern());
		
		try {
			return check.check();
		} catch (smarter.common.exception.ExecuteExpection e) {
			throw new ExecuteException(e);
		}
	}
}
