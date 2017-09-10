package com.orm.common.xml.parse.rule;

public interface Express {

	public void setType(int type);
	
	public void setParseToken(int offset,String token);
	
	public void setExpress(Express express);
}
