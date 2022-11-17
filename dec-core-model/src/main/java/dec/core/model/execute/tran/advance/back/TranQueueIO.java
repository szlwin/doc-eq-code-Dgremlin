package dec.core.model.execute.tran.advance.back;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranQueueIO {
	
	private static Logger log = LoggerFactory.getLogger(TranQueueIO.class);
	
	private static String fileName = "tran/tranQueue";
	
	public void write(TranQueue tranQueueArray[]){
		for(int i = 0; i < tranQueueArray.length;i++){
			FileOutputStream fos = null;
			ObjectOutputStream os = null;
			try{
		        fos = new FileOutputStream(fileName+"i");
		        os = new ObjectOutputStream(fos);
		        
		        TranQueue tranQueue = tranQueueArray[i];
		        
		        List<TranQueueNode> list = new ArrayList<TranQueueNode>(tranQueue.getSize());
		        
		        TranQueueNode node = null;
		        
		        while((node = tranQueue.get()) != tranQueue.getEnd()){
		        	list.add(node);
		        }
		        os.writeObject(list);
	        	os.flush();
	        	fos.flush();

			}catch(IOException e){
				log.error("Write error", e);
			}finally{
		        try {
					os.close();
					fos.close();
				} catch (IOException e) {
					log.error("Close error", e);
				}
		        
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public TranQueue[] read(){
		
		File file = new File("fileName");
		
		File fileArray[] = file.listFiles();
		
		if(fileArray == null || fileArray.length == 0){
			return null;
		}
		
		TranQueue[] tranQueueArray =  new TranQueue[fileArray.length];
		
		for(int i = 0; i < fileArray.length;i++){
			
			 FileInputStream fs = null;
			 ObjectInputStream os = null;
			try{
		        fs = new FileInputStream(fileName+"i");
		        os = new ObjectInputStream(fs);
		        
		        List<TranQueueNode> list  = (List<TranQueueNode>)os.readObject();
		        
		        if(list == null || list.isEmpty())
		        	continue;
		        
		        TranQueue tranQueue = new TranQueue();
		        
		        for(int j = 0; j < list.size(); j++){
		        	tranQueue.add(list.get(i));
		        }
		        
		        tranQueueArray[i] = tranQueue;
		        
			}catch(IOException e){
				log.error("Read error", e);
			} catch (ClassNotFoundException e) {
				log.error("Read error", e);
			}finally{
				try {
					fs.close();
					os.close();
				} catch (IOException e) {
					log.error("Close error", e);
				}
			}
		}
		return tranQueueArray;
		
	}
	
	public void remove(){
		File file = new File("fileName");
		File fileArray[] = file.listFiles();
		for(int i = 0; i < fileArray.length;i++){
			fileArray[i].delete();
		}
	}
}
