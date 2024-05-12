package dec.core.session.factory;

import dec.core.session.SimpleSession;

public class SessionFactory {

	private static final ThreadLocal<SimpleSession> simpleSessionLocal = new ThreadLocal<SimpleSession>();
	
	public static SimpleSession getSimpleSession(){
		SimpleSession simpleSession = simpleSessionLocal.get();
		
		if(simpleSession == null){
			simpleSession = new SimpleSession();
			simpleSessionLocal.set(simpleSession);
		}
		
		return simpleSession;
	}
	
	public static SimpleSession getSimpleSession(String connectionName){
		/*if(connectionName == null)
			return getSimpleSession();
		
		SimpleSession simpleSession = simpleSessionLocal.get();
		
		if(simpleSession == null){
			simpleSession = new SimpleSession(connectionName);
			simpleSessionLocal.set(simpleSession);
		}*/
		
		return new SimpleSession(connectionName);
	}
}
