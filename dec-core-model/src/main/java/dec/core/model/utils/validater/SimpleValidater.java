package dec.core.model.utils.validater;

import smarter.tool.echeck.validater.Validater;

//import smarter.tool.echeck.validater.ValidateException;
//import smarter.tool.echeck.validater.Validater;

public class SimpleValidater extends Validater<IErrorCode>{
    
	//private static SimpleErrorHandler simpleErrorHandler = new SimpleErrorHandler();
/*	public void executeWithThrowException() throws ValidateException{
		if(!this.execute()){
			
			ErrorCode error = this.getError();
			
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1];
			
			logger.error("#Validater error:"+error.getCode()+","+error.getMsg()+","+error.getUserMsg()
				+String.format(", class:%s, method:%s-%d", stackTraceElement.getClassName(),
						stackTraceElement.getMethodName(),
						stackTraceElement.getLineNumber()));

			throw new ValidateException(new BizException(error));
		}
	}*/
	
/*	public static SimpleValidater create(){
		return new SimpleValidater();
	}*/
	
	public static SimpleValidater get(){
		SimpleValidater simpleValidater =  new SimpleValidater();
		//simpleValidater.setErrorHandler(simpleErrorHandler);
		return simpleValidater;
	}

}
