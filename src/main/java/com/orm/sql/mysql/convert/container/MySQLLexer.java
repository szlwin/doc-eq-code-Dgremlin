package com.orm.sql.mysql.convert.container;

import santr.common.context.LexerUtil;
import santr.parser.exception.ExecuteInvaildException;
import santr.v4.parser.ExpressParser;
import santr.v4.parser.ParserTree;
import santr.view.parser.TreeViewer;

public class MySQLLexer {

	static{
		load();
	}
	
	private static void load(){
		try {
			LexerUtil.load("mySqlExpr", Thread.currentThread().getContextClassLoader().loadClass("com.orm.sql.mysql.convert.container.MySQLLexer")
					.getResource("ExprCSQL.ls").getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ParserTree lexer(String sql) throws ExecuteInvaildException{
		ExpressParser lexerExecuter = new ExpressParser();
		lexerExecuter.parser("mySqlExpr", sql);
		return lexerExecuter.getTree();
		
	}
	public static void main(String args[]) throws ExecuteInvaildException{
		MySQLLexer mySQLLexer = new MySQLLexer();
		String sql ="select a.id as u_id from userT a where a.id = #userT.id";
		
		//String sql= "select	a.df as aa,ss as df,fun(a.id)  from   c left join (select * from  b) b on c.id=b.id where c  like '%' #asd '%' "
		//		+ "and aa like '%' aa '%' group by a.sd,sd.dd having sd.dd>1 and df.dd =2 offset 1 limit 1 "
		//		+ "union all select * from a union all select * from a";
		
		ParserTree parserTree = mySQLLexer.lexer(sql);
		
	    TreeViewer viewer = new TreeViewer(parserTree);
	    viewer.open();
	}
}
