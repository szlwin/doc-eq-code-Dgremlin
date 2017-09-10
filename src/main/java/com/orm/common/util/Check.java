package com.orm.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Check {

	

	public static double complute(String patten)
	{ 
		
		List<String> strList = new ArrayList<String>(20);
		
		StringTokenizer stringToken = new StringTokenizer(patten,"()",true);
		
		StringBuffer strBuffer = new StringBuffer();
		
		while(stringToken.hasMoreTokens())
		{
			String str = stringToken.nextToken();
			
			strList.add(compluteNumber(str));
		}
		
		int flag = -1;
		
		for(int i = 0; i< strList.size();i++)
		{
			String tempStr = strList.get(i);
			
			if(tempStr.equals("(") &&
					strList.get(i+2).equals(")"))
			{
				flag = i+2;
				continue;
			}
				
			if(flag != i)
				strBuffer.append(strList.get(i));
			
		}
		
		if(strList.size() == 1)
			return Double.valueOf(strList.get(0));
		else
		{
			strList.clear();
			return complute(strBuffer.toString());
		}
		
		//return 0;
		
	}
	
	
	public static String compluteNumber(String patten)
	{
		if(patten.equals("(") || patten.equals(")"))
			return patten;
		
		if(patten.startsWith("*") || patten.startsWith("/") ||
				patten.endsWith("*") || patten.endsWith("/"))  
			return patten;
		
		if(patten.startsWith("+") || patten.startsWith("-") ||
				patten.endsWith("+") || patten.endsWith("-"))  
			return patten;
		
		StringTokenizer stringToken = new StringTokenizer(patten,"+-",true);
		
		List<String> strNumberList = new ArrayList<String>();
		
		while(stringToken.hasMoreTokens())
		{
			String str = stringToken.nextToken();
			
			if(str.indexOf("*") >0)
				strNumberList.add(mulite(str));
			else if(str.indexOf("/") >0)	
				strNumberList.add(devide(str));
			else
				strNumberList.add(str);
		}
		
		double value = Double.valueOf(strNumberList.get(0));
		for(int i = 1; i < strNumberList.size();i++)
		{
			String tempStr = strNumberList.get(i);
			
			if(tempStr.equals("+"))
				value = value + Double.valueOf(strNumberList.get(i+1));
			
			if(tempStr.equals("-"))
				value = value - Double.valueOf(strNumberList.get(i+1));
		}
		
		return String.valueOf(value);
	}
	
	private static String mulite(String str)
	{
		String[] strArray = str.split("\\*");
		double value = Double.valueOf(strArray[0]) * Double.valueOf(strArray[1]);
		return String.valueOf(value);
	}
	
	private static String devide(String str)
	{
		String[] strArray = str.split("/");
		double value = Double.valueOf(strArray[0]) / Double.valueOf(strArray[1]);
		return String.valueOf(value);
	}
	
	public static void  main(String[] args)
	{
		//Complute.complute("(1+2)*3");
		
		double value = Check.complute("(3*(2+4)+(1+4*2-6/2)+2)*2+5");
		System.out.println(value);
		//String str = "4*2";
		//String[] strArray = str.split("\\*");
		//Complute.compluteNumber("1+4*2-6/2");
	}
	
}
