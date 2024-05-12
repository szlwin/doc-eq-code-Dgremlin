package dec.external.datasource.sql.mysql.convert.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.external.datasource.sql.convert.AbstractSqlConvert;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import santr.parser.exception.ExecuteInvaildException;
import santr.v4.parser.ParserTree;

//import com.orm.sql.convert.AbstractSqlConvert;

public class MySQLConvert extends AbstractSqlConvert{

	private final static Logger log = LoggerFactory.getLogger(MySQLConvert.class);
	
	@Override
	protected String convert() {
		
		MySQLLexer mySQLLexer = new MySQLLexer();
		try {
			ParserTree parserTree = mySQLLexer.lexer(this.convertParam.getSql());
			MySQLExprVisitor mySQLExprVisitor = new MySQLExprVisitor();
			
			mySQLExprVisitor.setParamer(this.convertParam);
			mySQLExprVisitor.init();
			mySQLExprVisitor.vist(parserTree);
			
			this.dataInfoSet = mySQLExprVisitor.getDataList();
			
			return parserTree.getRuleContext()
					.getValue().toString();
		} catch (ExecuteInvaildException e) {
			log.error(e.getMessage(),e);
		}
		
		return null;
	}

}
