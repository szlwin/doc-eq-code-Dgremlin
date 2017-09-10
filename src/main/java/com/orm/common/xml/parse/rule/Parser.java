package com.orm.common.xml.parse.rule;

public interface Parser {
	
	public boolean isMacth(String string);
	
	public Express parser(String string[],int offset);
	
	public void addParser(Parser parser);
	
	public void setSuperParser(Parser parser);
}
