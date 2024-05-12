package dec.core.model.check.rule;

import dec.core.context.config.model.config.ConfigConstanst;
import smarter.common.express.check.PatternCheck;
import smarter.common.express.check.PropertyCheck;
import smarter.common.express.check.RuleCheck;

//import com.orm.common.xml.util.Constanst;

public class CheckFactory {

	public static final PatternCheck patternCheck = new PatternCheck();
	
	public static final PropertyCheck propertyCheck = new PropertyCheck();
	
	private static PatternCheck getPatternCheck(){
		return patternCheck;
	}
	
	private static PropertyCheck getPropertyCheck(){
		return propertyCheck;
	}
	
	public static RuleCheck<?> getCheck(String type){
		if(type.equals(ConfigConstanst.RULE_TYPE_CHECK)){
			return getPropertyCheck();
		}
		
		if(type.equals(ConfigConstanst.RULE_TYPE_CHECK_PATTERN)
				|| type.equals(ConfigConstanst.RULE_TYPE_CHECK_DATA_PATTERN)){
			return getPatternCheck();
		}
		return null;
	}
}
