package dec.core.model.utils.validater;

import smarter.tool.echeck.validater.ErrorHandler;
import smarter.tool.echeck.validater.ValidateException;

public class SimpleErrorHandler implements ErrorHandler<IErrorCode>{

	//private final static Logger logger = LoggerFactory.getLogger(SimpleValidater.class);
	
	@Override
	public void handler(IErrorCode error) throws ValidateException {
		
		
/*		StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1];
		
		logger.error("#Validater error:"+error.getCode()+","+error.getMsg()+","+error.getUserMsg()
			+String.format(", class:%s, method:%s-%d", stackTraceElement.getClassName(),
					stackTraceElement.getMethodName(),
					stackTraceElement.getLineNumber()));*/

		throw new ValidateException(null);
		
	}

}
