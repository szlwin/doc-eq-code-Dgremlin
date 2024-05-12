package dec.core.model.execute.tran.advance.back;

public class TranProtecter extends Thread{

	private TranQueue tranQueueArray[];
	
	private TranExecuterThread tranExecuterThreadArray[];
	
	/*
	private class UncaughtExceptionHandlerImpl implements UncaughtExceptionHandler{

		private ThreadGroup threadGroup;

		public UncaughtExceptionHandlerImpl(ThreadGroup threadGroup) {
			this.threadGroup = threadGroup;
		}

		public void uncaughtException(Thread t, Throwable e) {
			saveTranQueue();
			threadGroup.uncaughtException(t, e);
		}
	}*/
	
	public TranProtecter(TranQueue tranQueueArray[],TranExecuterThread tranExecuterThreadArray[]){
		super();
		this.tranQueueArray = tranQueueArray;
		this.tranExecuterThreadArray = tranExecuterThreadArray;
		//this.setUncaughtExceptionHandler(new UncaughtExceptionHandlerImpl(this.getThreadGroup()));
	}
	
	public UncaughtExceptionHandler getUncaughtExceptionHandler(){
		saveTranQueue();
		return super.getUncaughtExceptionHandler();
	}
	
	public void run(){
		while(true){
			check();
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void saveTranQueue(){
		TranQueueIO tranQueueIO = new TranQueueIO();
		tranQueueIO.write(tranQueueArray);
	}
	
	private void check(){
		for(int i = 0; i < tranExecuterThreadArray.length; i++){
			TranExecuterThread tranExecuterThread = tranExecuterThreadArray[i];
			if(!tranExecuterThread.isAlive()){
				restart(i);
			}
		}
	}
	
	private void restart(int index){
		TranAdvanceManager.getInstance().reStart(index);
	}
}
