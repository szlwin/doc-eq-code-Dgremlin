package dec.expand.declare.business;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.expand.declare.conext.DataStorage;
import dec.expand.declare.conext.DescContext;
import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.data.DataDepend;
import dec.expand.declare.conext.desc.data.DataDesc;
import dec.expand.declare.conext.desc.data.DataTypeEnum;
import dec.expand.declare.conext.desc.process.ProcessDesc;
import dec.expand.declare.conext.desc.process.RollBackPolicy;
import dec.expand.declare.conext.desc.process.TransactionPolicy;
import dec.expand.declare.conext.desc.system.SystemDesc;
import dec.expand.declare.executer.produce.Produce;
import dec.expand.declare.fun.FinalFun;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.ExecuteResult;
import dec.expand.declare.system.SystemContext;
import dec.expand.declare.system.System;

public class DefaultBusinessDeclare implements BusinessDeclare {

	private final static Logger log = LoggerFactory.getLogger(DefaultBusinessDeclare.class);
	
	private DataStorage dataStorage = new DataStorage();
	
	private String name;
	
	private ProcessDesc currentProcess;
	
	private BusinessDesc businessDesc;
	
	private ExecuteResult result;
	
	private String code;
	
	private System currentSystem;
	
	private FinalFun successFun;
	
	private Set<String> conumeRecordSet = new HashSet<>();
	
	public DefaultBusinessDeclare(){
		this.code = UUID.randomUUID().toString();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public <T> BusinessDeclare addEntitys(String data, List<T> t) {
		
		log.info("Code:{}, add entity:{}", code, data);
		
		dataStorage.add(data, t);
		
		return this;
	}

	@Override
	public <T> BusinessDeclare addEntity(String data, T t) {
		
		log.info("Code:{}, add entity:{}", code, data);
		
		dataStorage.add(data, t);
		
		return this;
	}

	@Override
	public BusinessDeclare execute() {
		
		List<ProcessDesc> processes = businessDesc.getProcesses();
		
		for(ProcessDesc process : processes){
			try {
				
				log.info("Code:{}, start produce data, [{}]-[{}]", code, process.getSystem(), process.getData());
				
				executeProcess(process);
				
				if(result != null && !result.isSuccess()){
					break;
				}
			} catch (Exception e) {
				log.error("Code:{}, produce data occur exception, [{}]-[{}]", code, process.getSystem(), process.getData(), e);
			}
			
			log.info("Code:{}, end produce data, [{}]-[{}]", code, process.getSystem(), process.getData());
			
		}
		
		
		return this;
	}

	@Override
	public BusinessDeclare onSuccess(FinalFun fun) {
		return this;
	}

	@Override
	public BusinessDeclare onError(FinalFun fun) {
		return this;
	}

	@Override
	public BusinessDeclare onException(FinalFun fun) {
		return this;
	}

	@Override
	public BusinessDeclare onFinsh(FinalFun fun) {
		return this;
	}

	@Override
	public BusinessDeclare data(String data) {
		
		data(data, "this");
		
		return this;
	}

	@Override
	public BusinessDeclare data(String data, String system) {
		
		currentProcess = new ProcessDesc();
		
		currentProcess.setData(data);
		
		currentProcess.setSystem(system);
		
		this.businessDesc.add(currentProcess);
		

		log.info("Code:{}, prepare produce data, [{}]-[{}]", code, system, data);
		
		return this;
	}

	
	/**
	 * type,group,rollBack
	 */
	@Override
	public BusinessDeclare transaction(TransactionPolicy transactionPolicy, 
			RollBackPolicy rollBackPolicy, String transactionGroup) {
		
		currentProcess.setTransaction(transactionPolicy);
		
		currentProcess.setRollBackPolicy(rollBackPolicy);
		
		currentProcess.setTransactionGroup(transactionGroup);
		
		return this;
	}

	/**
	 * type,group,rollBack
	 */
	@Override
	public BusinessDeclare transaction(TransactionPolicy transactionPolicy, 
			RollBackPolicy rollBackPolicy) {
		
		transaction(transactionPolicy, rollBackPolicy, null);
		
		return this;
	}
	
	@Override
	public ExecuteResult getExecuteResult() {
		return result;
	}

	@Override
	public BusinessDeclare build(String name) {
		
		businessDesc = new BusinessDesc();
		
		businessDesc.setName(name);
		
		return this;
	}

	@Override
	public BusinessDeclare onStop(FinalFun fun) {
		return this;
	}

	@Override
	public BusinessDeclare addProduce(Produce<DataStorage> produce){
		
		if(currentSystem == null){
			currentSystem = new System();
			currentSystem.setName("this");
		}
		/*if(produceMap == null){
			
			produceMap = new HashMap<String, Produce<DataStorage>>();
		}
		
		if(produceMap.containsKey(produce.getName())){
			throw new RuntimeException("The produce is exist:" + produce.getName());
		}
		
		produceMap.put(produce.getName(), produce);*/
		
		currentSystem.add(produce);
		log.info("Code:{}, add produce:{}", code, produce.getName());
		
		return this;
	}
	
	@Override
	public BusinessDeclare addProduce(String name, Function<ExecuteResult, DataStorage> fun) {
		
		Produce<DataStorage> produce = new Produce<DataStorage>();
		
		produce.setName(name);
		
		produce.setFun(fun);
		
		
		this.addProduce(produce);
		
		return this;
	}
	
	public String getCode() {
		return code;
	}

	
	private void executeProcess(ProcessDesc process) throws Exception{
		
		//DataDesc dataDesc = null;
		
		//if(process.getSystem() == null){
			
		//	dataDesc = DescContext.get().getData(process.getData());
			
		//}else{
			
		//	SystemDesc systemDesc = DescContext.get().getSystem(process.getSystem());
			
		//	dataDesc = systemDesc.getData(process.getData());
		//}
		
		if("this".equals(process.getSystem())){
			
			produceData(null, process.getData(), process.getSystem());
			
		}else{
			
			SystemDesc systemDesc = DescContext.get().getSystem(process.getSystem());
			
			DataDesc dataDesc = systemDesc.getData(process.getData());
			
			produceData(dataDesc, process.getData(), process.getSystem());
			
			refreshStorage(dataDesc);
		}
		
		
		
	}
	
	private void produceData(DataDesc dataDesc, String dataName, String system) throws Exception{
		

		List<DataDepend> dataDepends = null;
		
		if(dataDesc != null){
			
			dataDepends = dataDesc.getDataDepends();
			
			if(dataDepends != null){
				for(DataDepend dataDepend:dataDepends){
					
					String data = dataDepend.getData();
					
					if(!dataStorage.containsData(data)){
						
						DataDesc depnedDataDesc = getDataDesc(system, dataDepend);
						
						log.info("Code:{}, start produce depend data, [{}]-[{}]", code, system, data);
						
						produceData(depnedDataDesc, data, system);
						
						if(!result.isSuccess()){
							return;
						}
						
						log.info("Code:{}, end produce depend data,  [{}]-[{}]", code, system, data );
					}
				}
			}
		}
		
		System executeSystem = null;
		
		if("this".equals(system) ){
			
			executeSystem = currentSystem;
			
		}else{
			
			executeSystem = SystemContext.get()
					.get(system);
		}
		
		result = executeSystem.produce(dataName, dataStorage);
		
		if(result.isSuccess() ){
			
			
			if(result.getData() != null){
				
				if(result.getDataType() == null){
					
					dataStorage.add(dataName, result.getData());
					
				}else{
					
					dataStorage.add(result.getDataType(), result.getData());
					
				}
				
				if(!conumeRecordSet.contains(system+":"+dataName)){
					
					try{

						result = executeSystem.conume(dataName, this.dataStorage);
						
						conumeRecordSet.add(system+":"+dataName);
						
					}catch(Exception e){
						
						log.error("Code:{}, conume data,  [{}]-[{}]", code, system, dataName, e);
						
						result = ExecuteResult.fail(null, null, e);
						
					}
				}
				
				if(!result.isSuccess()){
					
					if(dataDepends != null){
						
						for(DataDepend dataDepend:dataDepends){
							
							if(dataDepend.getType() ==1)
								continue;
								
							DataDesc depnedDataDesc = getDataDesc(system, dataDepend.getData());
							
							refreshStorage(depnedDataDesc);
						}
					}
					
				}

			}
			
		}
		
		
	}
	
	
	private DataDesc getDataDesc(String system, DataDepend dataDepend) {


		if(dataDepend.getType() == 1){
			
			return getDataDesc("common", dataDepend.getData());
		}else{
			
			return getDataDesc(system, dataDepend.getData());
		}
		
		
	}

	private DataDesc getDataDesc(String system, String data){
		
		if(system == null){
			
			return DescContext.get().getData(data);
			
		}else{
			
			SystemDesc systemDesc = DescContext.get().getSystem(system);
			
			return systemDesc.getData(data);
		}
	}
	
	
	private void refreshStorage(DataDesc dataDesc){
		
		if(dataDesc!=null && dataDesc.getType() == DataTypeEnum.PERSISTENT && !dataDesc.isCachePrior()){
			
			this.dataStorage.remove(dataDesc.getName());
		}
		
	}
}
