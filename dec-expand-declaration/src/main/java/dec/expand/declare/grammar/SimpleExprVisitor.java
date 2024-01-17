package dec.expand.declare.grammar;

import santr.common.context.LexerUtil;
import santr.common.util.collections.SimpleList;
import santr.parser.exception.ExecuteInvaildException;
import santr.v4.execute.AbstractVisitor;
import santr.v4.parser.ParserTree;
import santr.v4.parser.RuleContext;
import smarter.common.express.execute.Error;
import smarter.common.express.execute.SimpleExprParam;
import smarter.common.express.execute.Variable;
import smarter.common.util.CheckUtil;
import smarter.common.util.NotSet;
import smarter.data.number.compute.NumberCompute;

import java.util.*;

public class SimpleExprVisitor extends AbstractVisitor<SimpleExprParam>{
	
	private Error error;

	private Object data;

	public Error getError() {
		return error;
	}


	static
   {
     try
    {
     LexerUtil.load("GrammarExpr",
       SimpleExprVisitor.class
       	.getClassLoader()
        .getResourceAsStream("GrammarExpr.ls"));
    }catch (Exception localException) {
    	 localException.printStackTrace();
	}
   }

    public void execute(RuleContext context) throws ExecuteInvaildException {
    	
        //Get the tree name.
        String name = context.getName();
        switch(name){
    		case "computeExprs":
    			executeComputeExprs(context);
    			break;
    		case "computeExpr":
    			executeComputeExpr(context);
    			break;
    		case "left":
    			executeLeftExpr(context);
    			break;
    		case "right":
    			executeRightExpr(context);
    			break;
    		case "valueParam":
    			executeValueParam(context);
    			break;
    		case "variable":
    			executeVariable(context);
    			break;
    		case "element":
    			executeElement(context);
    			break;    
    		case "arrayScale":
    			executeArrayScale(context);
    			break;      			
        	case "expr":
        		executeExpr(context);
        		break;
        	case "NULL":
        		executeNULL(context);
        		break;        		
        	case "fun":
        		 executeFun(context);
        		 break;
        	case "params":
        		 executeArray(context);
        		 break;
        	case "param":
        		 executeParam(context);
        		 break;
        	case "INT":
        		executeINT(context);
        		break;
        	case "ID":
        		executeID(context);
        		break;
        	case "STRING":
        		executeString(context);
        		break;
        }
        /*if(name.equals("expr")){
        	executeExpr(context);
        }else if(name.equals("fun")){
        	executeFun(context);
        }else if(name.equals("array")){
            executeArray(context);
        }else if(name.equals("param")){
            executeParam(context);
        }else if(name.equals("INT")){
            executeINT(context);
        }else if(name.equals("ID")){
            executeID(context);
        }else if(name.equals("STRING")){
            executeString(context);
        }*/

    }

    //public void setParam(Map<String,Object> paramMap){
    //    this.paramMap.putAll(paramMap);
    //}

    private void executeRightExpr(RuleContext context) throws ExecuteInvaildException {
    	context.setValue(this.getChildValue(context, 0,context.getParam()));
		
	}

	private void executeArrayScale(RuleContext context) {
		// TODO Auto-generated method stub
		//1
    	if(context.getChildCount() == 1){
    		int flag = Integer.valueOf(context.getChild(0).getRuleContext().getText()).intValue();
    		context.setValue(new Variable((String) context.getParam(),flag,flag));
    	}else if(context.getChildCount() == 3){
    		//1...2
    		int startFlag = Integer.valueOf(context.getChild(0).getRuleContext().getText()).intValue();
    		int endFlag = Integer.valueOf(context.getChild(2).getRuleContext().getText()).intValue();
    		context.setValue(new Variable((String) context.getParam(),startFlag,endFlag));
    	}else{
    		if(context.getChild(0).isToken()){
    			//...2
    			int endFlag = Integer.valueOf(context.getChild(1).getRuleContext().getText()).intValue();
        		context.setValue(new Variable((String) context.getParam(), -1, endFlag));
        		
    		}else{
    			//1...
    			int startFlag = Integer.valueOf(context.getChild(0).getRuleContext().getText()).intValue();
        		context.setValue(new Variable((String) context.getParam(), startFlag, -1));
    		}
    	}
	}

	private void executeElement(RuleContext context) throws ExecuteInvaildException {
		if(context.getChildCount() == 1){
			context.setValue(new Variable(context.getChild(0).getRuleContext().getText()));
		}else if(context.getChild(0).isToken()){
			//(ID,ID)
			int count = (context.getChildCount()-1)/2;
			
			String param[] = new String[count];
			for(int i=1; i < count;i++){
				param[i-1] = (String) context.getChild(2*i-1).getRuleContext().getText();
			}
			Variable variable= new Variable(param);
			context.setValue(variable);
		}else if(context.getChild(1).isToken() 
				&& context.getChild(1).getToken().equals("{")){
			
			int index = -1;
			
			if(context.getChildCount() == 4){
				//{INT}
				index = (int)this.getChildValue(context, 2 );
			}
			
			Variable variable= new Variable(context.getChild(0).getRuleContext().getText(),
					index);
			
			context.setValue(variable);
		}else{
			//1...2
			context.setValue(this.getChildValue(context, 
					2, 
					context.getChild(0).getRuleContext().getText())
			);
		}
		
	}

	private void executeLeftExpr(RuleContext context) throws ExecuteInvaildException {
    	/*if(context.getChildCount() == 1){
    		context.setValue(new String[]{(String) context.getValue()});
    	}else{
    		context.setValue(context.getValue());
    	}*/
    	
		int count = (context.getChildCount()+1)/2;
		
		Object variables[] = new Object[count];
		
		for(int i = 1; i <= count;i++){
			variables[i-1] = this.getChildValue(context, 2*i-2);
		}
		
		context.setValue(variables);
		
		/*for(int i =0; i < variables.length;i++){
			if(variables[i] instanceof Collection){
				List<Variable> variableList = (List<Variable>) variables[i];
				
				for(int j =0; j < variableList.size();j++){
					Variable variable = variableList.get(j);
					
					System.out.print(variable+".");
				}
				System.out.println(" ");
			}else{
				Variable variable = (Variable) variables[i];
				System.out.println(variable);
			}
			
		}*/
		//System.out.println(variables);
		
	}

	private void executeVariable(RuleContext context) throws ExecuteInvaildException {
		//error
		if(context.getChild(0).isToken()){
			Variable variable = new Variable(context.getChild(1).getRuleContext().getText(), 
					context.getChild(3).getRuleContext().getText());
			
			context.setValue(variable);
			
		}else{
			if(context.getChildCount() == 1){
				context.setValue(this.getChildValue(context, 0));
			}else{
				List<Variable> list = new SimpleList<>(context.getChildCount(),4);
				
				for(int i = 0; i< context.getChildCount(); i++){
					ParserTree parserTree = context.getChild(i);
					
					if(parserTree.isToken()){
						continue;
					}else{
						//System.out.println(this.getChildValue(context, i));
						list.add((Variable) this.getChildValue(context, i));
						
					}
				}
				
				context.setValue(list);
			}
			
		}
		
		//'error('  STRING ',' STRING ')'
		
	}
	
	private void executeValueParam(RuleContext context) throws ExecuteInvaildException {
		context.setValue(this.getChildValue(context, 0));
		
	}

	private void executeComputeExpr(RuleContext context) throws ExecuteInvaildException {
		
    	 Object variableParam = this.getChildValue(context.getChild(0));
    	 
    	 Object value = this.getChildValue(context.getChild(2), variableParam);
    	 
    	 Object variables[] = (Object[]) variableParam;
    	 
    	 
    	 if(value instanceof NotSet){
    		 return;
    	 }
    	 /*for(int i=0; i< variables.length;i++){
    		 
    		 Object variableElement = variables[i];
    		 
    		 if(variableElement instanceof Variable){
    			 
    			 Variable variableValue = (Variable)variableElement;
    			 
    			 if(variableValue.getType() == 3){
    				 valueMap = ((SimpleExprParam)this.getParamer()).getParamMap();
    			 }
    		 }else{
    			 
    		 }
    	 }*/
    	 
    	 Map<String,Object> valueMap = null;
    	 
    	 Variable lastVariable = null;
    	 
    	 for(int i =0; i < variables.length;i++){
    		
 			if(variables[i] instanceof Collection){
 				valueMap = ((SimpleExprParam)this.getParamer()).getParamMap();
 				
 				List<Variable> variableList = (List<Variable>) variables[i];
 				
 				for(int j =0; j < variableList.size()-1;j++){
 					lastVariable = variableList.get(j);
 					
 					if(lastVariable.getType() == 3){
 	 					valueMap = (Map<String, Object>) valueMap.get(lastVariable.getName());
 	 				}
 					
 				}
 				lastVariable = variableList.get(variableList.size()-1);
 			}else{
 				
 				lastVariable = (Variable) variables[i];
 				
 				if(lastVariable.getType() == 1){
 					boolean checkValue = ((Boolean)value);
 					
 					if(context.getChild(1).getToken().length() >1){
 						//!:
 						checkValue = !checkValue;
 					}
 					
					if(checkValue ){
							
						this.error = (Error)lastVariable.getParam();
							
					}

 					return;
 				}else{
 	 				if(lastVariable.getName().startsWith("#")){
 	 					valueMap = ((SimpleExprParam)this.getParamer()).getExternalMap();
 	 				}else{
 	 					valueMap = ((SimpleExprParam)this.getParamer()).getParamMap();
 	 				}
 				}

 			}
 			
 			setValue(valueMap, lastVariable.getName(), value);
 		}
		
	}

	private void setValue(Map<String,Object> valueMap, String key, Object value){
		Object originValue = valueMap.get(key);
		
		if(originValue != null && value!=null){
			boolean isMatchType = true;
			
			if(originValue instanceof Number && !(value instanceof Number)){
				isMatchType = false;
				
			}else if(originValue instanceof String && !(value instanceof String)){
				isMatchType = false;
				
			}else if(originValue instanceof Date && !(value instanceof Date)){
				isMatchType = false;
			}else if(originValue instanceof Boolean && !(value instanceof Boolean)){
				isMatchType = false;
			}
			
			/*if(originValue instanceof Number && originValue.getClass() != value.getClass()){
				
				if(originValue instanceof BigDecimal){
					value = new BigDecimal(value.toString());
				}else if(value instanceof BigInteger){
					value = ((BigInteger)value).longValue();
				}else if(value instanceof BigDecimal){
					value = ((BigDecimal)value).doubleValue();
				}
			}*/
			
			if(!isMatchType){
				throw new RuntimeException("The ["+key+"],is not match type,oldValue:"+originValue.getClass()+"newValue:"+value.getClass());
			}
		}
		valueMap.put(key, value);

	}
	
	private void executeComputeExprs(RuleContext context) throws ExecuteInvaildException {
    	
    	for(int i=0; i<context.getChildCount(); i=i+2){
    		this.getChildValue(context.getChild(i), context.getParam());
    		
    		if(this.error != null){
    			break;
    		}
    	}
	}

	private void executeNULL(RuleContext context) throws ExecuteInvaildException{
    	context.setValue(null);
    }
    
    private void executeExpr(RuleContext context) throws ExecuteInvaildException{
        if(context.getChildCount() == 1){
            context.setValue(this.getChildValue(context,0));
        }else if (context.getChildCount() == 2)
        {
            String token = (String)getChildValue(context, 0);
            if (token.equals("!"))
            {
              context.setValue(Boolean.valueOf(!((Boolean)getChildValue(context, 1)).booleanValue()));
            }
            else
            {
              Number num = (Number)getChildValue(context, 1);
              context.setValue(Double.valueOf(0.0D - num.doubleValue()));
            }
          }
        
        else{
            //Get all the tree info.
        	ParserTree[] treeInfoList =  this.getAllChild(context);
        	
            if(treeInfoList[0].isToken()){
                //if expr then expr else expr
            	if(treeInfoList[0].getToken().equals("if")){
            		context.setValue(doWithIf(context));
            	}else{
            		//( expr )
            		context.setValue(this.getChildValue(treeInfoList[1]));
            	}
                
            }else{
            	String token = treeInfoList[1].getToken();
            	if(token.equals("+") 
            			|| token.equals("-") 
            			|| token.equals("*")
            			|| token.equals("/")){
            		//expr ('+'|'-') expr
            		// expr ('*'|'/') expr
                	/*context.setValue(
                			compluteMap.get(token).eval(
                					((Double)this.getChildValue(treeInfoList[0]))
                						.doubleValue(),
                					((Double)this.getChildValue(treeInfoList[2]))
                						.doubleValue()));*/
            		context.setValue(
            				NumberCompute.compute((Number)this.getChildValue(treeInfoList[0]), 
            						token, 
            				(Number)this.getChildValue(treeInfoList[2]))
            		);
            	}else if(token.equals("and")){
            		//expr and expr
            		context.setValue((Boolean)this.getChildValue(treeInfoList[0]) && 
                					(Boolean)this.getChildValue(treeInfoList[2]));
            	}else if(token.equals("or")){
            		//expr or expr
            		context.setValue((Boolean)this.getChildValue(treeInfoList[0]) || 
        					(Boolean)this.getChildValue(treeInfoList[2]));
            	}else if(token.equals(".")){
            		
            		Object value = this.getChildValue(context, 0);
            		
            		if(value instanceof Double){
            			Double value1 = (Double) this.getChildValue(context, 2);

            			context.setValue((Double)value+value1*0.1);
            		}else{
                        Map<String, Object> subParam = null;
                        
                        String startKey = treeInfoList[0].getRuleContext().getChild(0).getRuleContext().getText();
                        
                        if(startKey.startsWith("#")){
                        	subParam = ((SimpleExprParam)this.getParamer())
                    				.getExternalMap();
                        }else{
                        	subParam = ((SimpleExprParam)this.getParamer())
                    				.getParamMap();
                        }

                        
                        subParam = (Map)subParam.get(startKey);
                        for (int i = 1; i < context.getChildCount() - 1; i=i+2)
                        {
                          ParserTree tmpTree = context.getChild(i);
                          if (!tmpTree.isToken())
                          {
                            String key = tmpTree.getRuleContext().getChild(0).getRuleContext().getText();
                            subParam = (Map)subParam.get(key);
                          }
                        }
                        ParserTree lastTree = context.getChild(context.getChildCount() - 1);
                        String lastKey = lastTree.getRuleContext().getChild(0).getRuleContext().getText();
                        Object object = subParam.get(lastKey);
                        context.setValue(object);
            		}

            	}
            	else{
            		//expr ('='|'!='|'>='|'<='|'>'|'<') expr
            		Object left = this.getChildValue(treeInfoList[0]);
            		Object right = this.getChildValue(treeInfoList[2]);
            		/*if(left instanceof String){
            			context.setValue(
                    			stringCompareMap.get(token).eval((String)left,(String)right));
            		}else{
            			context.setValue(
                    			numberCompareMap.get(token).eval((Double)left,(Double)right));
            		}*/
            		context.setValue(CheckUtil.compareByTag(left, token, right));
            	}
            }
        }
    }

    private Object doWithIf(RuleContext context) throws ExecuteInvaildException {
    	
    	//if expr then expr else expr
    	boolean result = (boolean) this.getChildValue(context, 1);
    	
    	if(result){
    		return this.getChildValue(context, 3);
    	}else{
    		//if expr then expr
    		if(context.getChildCount() == 4){
    			return NotSet.get();
    		}else{
    			
    			return this.getChildValue(context, 5);
    		}
    	}
	}
    
	private void executeFun(RuleContext context) throws ExecuteInvaildException{
        //Get the all param value.
        List<Double> list = (List<Double>) getChildValue(context,2);

        int flag = 0;
        String funName = (String)this.getChildValue(context,0);

        if(funName.equals("max")){
            flag = 1;
        }else if(funName.equals("min")){
            flag = -1;
        }

        double value = list.get(0);
        for(int i = 1;i < list.size();i++){
            double num = list.get(i);
            if((num - value)*flag>0 ){
                value = num;
            }
        }
        context.setValue(value);
    }

    private void executeArray(RuleContext context) throws ExecuteInvaildException{
        List<Object> list = new ArrayList<Object>();
        ParserTree[] treeInfoList =  this.getAllChild(context);
        //param (',' param)*;
        for(int i =0; i < treeInfoList.length;i++){
            if(!treeInfoList[i].isToken()){
              list.add(this.getChildValue(treeInfoList[i]));
            }
        }
        context.setValue(list);
    }

    private void executeParam(RuleContext context) throws ExecuteInvaildException{
        if(context.getChildCount()==1){
            //ID | INT| fun| expr 
            context.setValue(this.getChildValue(context,0));
        }else{
            // ID  '[' INT ']'
            List<Double> paramList = (List<Double>)this.getChildValue(context,0);
            context.setValue(paramList.get((Integer) this.getChildValue(context,2)));
        }
    }

    private void executeINT(RuleContext context){
        //Save the value to this tree.
    	if(context.getText() != null){
    		context.setValue(Double.valueOf(context.getText()));
    	}
        
    }

    private void executeID(RuleContext context){
    	//Save the value to this tree.
    	if(context.getText().startsWith("#"))
    		context.setValue(((SimpleExprParam)this.getParamer())
            		.getExternalMap()
            		.get(context.getText()));
    	else
    		context.setValue(((SimpleExprParam)this.getParamer())
    				.getParamMap()
        			.get(context.getText()));
    }

    private void executeString(RuleContext context){
        //Save the value to this tree.
        context.setValue(context.getText());
    }
}
