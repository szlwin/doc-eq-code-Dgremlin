package dec.core.model.execute.rule.grammer;

import java.util.Map;

import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.execute.rule.common.AbstractRuleExecute;
import santr.parser.exception.ExecuteInvaildException;
import santr.v4.parser.ExpressParser;
import smarter.common.express.execute.SimpleExprParam;
import smarter.common.express.execute.SimpleExprVisitor;

public class GrammerExecute extends AbstractRuleExecute{

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() throws ExecuteException {
		
		SimpleExprParam simpleExprParam = new SimpleExprParam();
		
		simpleExprParam.setParamMap((Map<String, Object>) this.value);
		
		simpleExprParam.setExternalMap(this.externalParam);
		
		SimpleExprVisitor simpleExprVisitor = new SimpleExprVisitor();
		
		simpleExprVisitor.setParamer(simpleExprParam);
		
		ExpressParser lexerExecuter = new ExpressParser();
		
		lexerExecuter.addVisitor(simpleExprVisitor);
		
		try {
			lexerExecuter.parser("SimpleExpr", this.ruleInfo.getGrammer());
		} catch (ExecuteInvaildException e) {
			e.printStackTrace();
			throw new ExecuteException(e);
		}
		
		return true;
	}

}
