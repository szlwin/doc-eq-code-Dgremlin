package dec.expand.declare.business;

import artoria.reflect.ReflectUtils;
import com.alibaba.fastjson.JSONObject;
import dec.core.context.config.model.rule.RuleViewInfo;
import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.context.data.ModelData;
import dec.core.model.container.ModelLoader;
import dec.core.model.execute.tran.Transaction;
import dec.core.model.execute.tran.advance.MultipleTranContainer;
import dec.core.model.utils.DataUtil;
import dec.expand.declare.business.exception.ExecuteException;
import dec.expand.declare.conext.DataStorage;
import dec.expand.declare.conext.DescContext;
import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.data.*;
import dec.expand.declare.conext.desc.process.ProcessDesc;
import dec.expand.declare.conext.desc.process.PropertyDesc;
import dec.expand.declare.conext.desc.process.RollBackPolicy;
import dec.expand.declare.conext.desc.process.TransactionPolicy;
import dec.expand.declare.conext.desc.system.SystemDesc;
import dec.expand.declare.conext.utils.DataUtils;
import dec.expand.declare.datasorce.ConnecionDesc;
import dec.expand.declare.datasorce.DataSourceManager;
import dec.expand.declare.executer.produce.Produce;
import dec.expand.declare.fun.FinalFun;
import dec.expand.declare.fun.Function;
import dec.expand.declare.service.Error;
import dec.expand.declare.service.ExecuteResult;
import dec.expand.declare.system.System;
import dec.expand.declare.system.SystemContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

    private FinalFun errorFun;

    private FinalFun exceptionFun;

    private FinalFun finshFun;

    private FinalFun stopFun;

    private DataSourceManager dataSourceManager;

    private Map<String, ModelLoader> modelLoaderMap;

    private MultipleTranContainer multipleTranContainer;
    //private TransactionDesc currentTransactionDesc;

    //private List<TransactionDesc> transactionDescList;

    private boolean isRefRule;

    public DefaultBusinessDeclare() {
        this(false);
    }

    public DefaultBusinessDeclare(boolean isRefRule) {
        this.isRefRule = isRefRule;
        this.code = UUID.randomUUID().toString();
        this.multipleTranContainer = new MultipleTranContainer();
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

        //transactionFlagArray = new int[transactionDescList.size()];
        for (int i = 0; i < processes.size(); i++) {
            ProcessDesc process = processes.get(i);
            try {

                if (process.isBegin()) {
                    beginTx(process);
                    continue;
                }

                if (process.isEnd()) {
                    endTx(process, 0);
                    continue;
                }

                log.info("Code:{}, start produce data, [{}]-[{}]", code, process.getSystem(), process.getData());

                executeProcess(process, i);

                if (result != null && (!result.isSuccess() || result.isStop())) {
                    break;
                }
            } catch (Exception e) {
                log.error("Code:{}, produce data occur exception, [{}]-[{}]", code, process.getSystem(), process.getData(), e);
                if (result == null) {
                    result = ExecuteResult.fail();
                }

                Error error = new Error();
                error.setException(e);
                result.setError(error);
                break;
            }
            log.info("Code:{}, end produce data, [{}]-[{}]", code, process.getSystem(), process.getData());
        }

        if (result.isStop() && this.stopFun != null) {
            stopFun.execute(this.result);
        }
        if (result.isSuccess()) {
            if (this.successFun != null) {
                successFun.execute(this.result);
            }
        } else {
            if (result.getError() != null && this.errorFun != null) {
                this.errorFun.execute(this.result);
            }

            if (result.isException() && this.exceptionFun != null) {
                this.exceptionFun.execute(this.result);
            }
        }

        if (this.finshFun != null) {
            finshFun.execute(this.result);
        }
        return this;
    }

    @Override
    public BusinessDeclare onSuccess(FinalFun fun) {
        this.successFun = fun;
        return this;
    }

    @Override
    public BusinessDeclare onError(FinalFun fun) {
        this.errorFun = fun;
        return this;
    }

    @Override
    public BusinessDeclare onException(FinalFun fun) {
        this.exceptionFun = fun;
        return this;
    }

    @Override
    public BusinessDeclare onFinsh(FinalFun fun) {
        this.finshFun = fun;
        return this;
    }


    protected BusinessDeclare data(String data) {
        data("this", data);
        return this;
    }

    protected BusinessDeclare data(ProcessDesc processDesc) {
        currentProcess = processDesc;

        this.businessDesc.add(currentProcess);

        //(this.currentTransactionDesc.getTransaction(), this.currentTransactionDesc.getRollBackPolicy(),
        //        this.currentTransactionDesc.getTransactionGroup());
        return this;
    }


    protected BusinessDeclare data(String system, String data) {

        validate(system, data);

        currentProcess = new ProcessDesc();

        currentProcess.setData(data);

        currentProcess.setSystem(system);

        this.businessDesc.add(currentProcess);

        //this.transaction(this.currentTransactionDesc.getTransaction(), this.currentTransactionDesc.getRollBackPolicy(),
        //        this.currentTransactionDesc.getTransactionGroup());
        //log.info("Code:{}, prepare produce data, [{}]-[{}]", code, system, data);

        return this;
    }

    /*protected BusinessDeclare beginTx(TransactionPolicy transactionPolicy) {
        if (this.transactionDescList == null)
            transactionDescList = new ArrayList<>();
        this.currentTransactionDesc = new TransactionDesc();
        currentTransactionDesc.setTransaction(transactionPolicy);
        currentTransactionDesc.setTransactionGroup(String.valueOf(transactionDescList.size()));
        currentTransactionDesc.setIndex(transactionDescList.size());
        currentTransactionDesc.setRollBackPolicy(RollBackPolicy.ROLL);
        transactionDescList.add(currentTransactionDesc);
        List<ProcessDesc> processDescList = this.businessDesc.getProcesses();
        if (processDescList == null) {
            currentTransactionDesc.setBegin(0);
        } else {
            currentTransactionDesc.setBegin(processDescList.size());
        }
        return this;
    }*/

    //@Override
    //public BusinessDeclare rollback() {
    //    currentTransactionDesc.setRollBackPolicy(RollBackPolicy.ROLL);
    //    return this;
    //}


    /*protected BusinessDeclare endTx() {
        List<ProcessDesc> processDescList = this.businessDesc.getProcesses();
        if (processDescList != null)
            currentTransactionDesc.setEnd(processDescList.size());

        //if (currentTransactionDesc.getIndex() > 0) {
       //     currentTransactionDesc = transactionDescList.get(currentTransactionDesc.getIndex() - 1);
        //}
        return this;
    }*/

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
        this.stopFun = fun;
        return this;
    }

    @Override
    public BusinessDeclare addProduce(Produce<DataStorage> produce) {

        if (currentSystem == null) {
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
    public BusinessDeclare addProduce(String system, String dataName, Function<ExecuteResult, DataStorage> fun) {
        validate(system, dataName);
        Produce<DataStorage> produce = new Produce<DataStorage>();
        produce.setName(dataName);
        produce.setSystem(system);
        produce.setFun(fun);
        this.addProduce(produce);

        return this;
    }

    @Override
    public BusinessDeclare addProduce(String dataName, Function<ExecuteResult, DataStorage> fun) {
        Produce<DataStorage> produce = new Produce<DataStorage>();
        produce.setName(dataName);
        produce.setFun(fun);
        this.addProduce(produce);
        return this;
    }

    @Override
    public BusinessDeclare transactionManager(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
        return this;
    }

    @Override
    public void setRuleModel(String name, Object obj) {
        if (modelLoaderMap == null) {
            modelLoaderMap = new HashMap<>();
        }

        if (!modelLoaderMap.containsKey(name)) {
            ModelLoader modelLoader = new ModelLoader();
            RuleViewInfo ruleViewInfo = ConfigContextUtil.getConfigInfo().getRuleViewInfo(name);
            if (obj instanceof ModelData) {
                modelLoader.load(name, (ModelData) obj);
            }
            try {
                ModelData modelData = DataUtil.createViewData(ruleViewInfo.getViewData().getName());
                if (obj instanceof Map) {
                    modelData.getAllValues().putAll((Map<String, Object>) ((Map) obj).values());
                } else {
                    modelData.getAllValues().putAll((Map<? extends String, ?>) ((Map<? extends String, ?>) JSONObject.toJSON(obj)).values());
                }
                modelData.setOriginData(obj);
                modelLoader.load(name, modelData);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            modelLoaderMap.put(name, modelLoader);
        }
    }

    public String getCode() {
        return code;
    }

    private void connect(ProcessDesc process) {
        ConnecionDesc connecionDesc = new ConnecionDesc();
        connecionDesc.setGroup(process.getTransactionGroup());
        connecionDesc.setTransactionPolicy(process.getTransaction());
        this.dataSourceManager.connect(connecionDesc);
    }

    private void beginTx(ProcessDesc process) {
        if (!this.isRefRule) {
            connect(process);
        } else {
            try {
                multipleTranContainer.begin(process.getDataSource(), getTransactionPolicy(process.getTransaction()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void endTx(ProcessDesc process, int index) {
        if (!this.isRefRule) {
            dealWithTx(process);
        } else {
            try {
                multipleTranContainer.end();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void dealWithTx(ProcessDesc process) {
        if (this.result.isSuccess()) {
            this.dataSourceManager.commit();
        } else {
            if (process.getRollBackPolicy() == RollBackPolicy.ROLL) {
                this.dataSourceManager.rollBack();
            }
        }
    }

    private void executeProcess(ProcessDesc process, int index) throws Exception {

        //DataDesc dataDesc = null;

        //if(process.getSystem() == null){

        //	dataDesc = DescContext.get().getData(process.getData());

        //}else{

        //	SystemDesc systemDesc = DescContext.get().getSystem(process.getSystem());

        //	dataDesc = systemDesc.getData(process.getData());
        //}
        if (process.getRule() != null) {
            executeRule(process);
        }
        if ("this".equals(process.getSystem())) {

            produceData(null, process.getData(), process.getSystem());

            refreshModeldata(process);
        } else {

            SystemDesc systemDesc = DescContext.get().getSystem(process.getSystem());

            DataDesc dataDesc = systemDesc.getData(process.getData());

            produceData(dataDesc, process.getData(), process.getSystem());

            this.change(systemDesc, dataDesc);

            refreshModeldata(process);

            refreshStorage(dataDesc);
        }

    }

    private void refreshModeldata(ProcessDesc process) {
        if (process.getRuleRefreshList() == null || process.getRuleRefreshList().isEmpty()) {
            return;
        }
        Object object = this.dataStorage.get(process.getData());
        for (PropertyDesc propertyDesc : process.getRuleRefreshList()) {
            try {
                Object sourceObject = null;
                if ("this".equals(propertyDesc.getSourceProperty()[0])) {
                    sourceObject = object;
                } else {
                    sourceObject = DataUtils.getValue(object, propertyDesc.getSourceProperty(), 0);
                }

                Object targetObject = this.modelLoaderMap.get(process.getRule());

                if (!"this".equals(propertyDesc.getSourceProperty()[0])) {
                    targetObject = DataUtils.getValue(targetObject, propertyDesc.getTargetProperty(), 0);
                }
                convertData(sourceObject, targetObject);
            } catch (Exception ex) {
                throw new ExecuteException(ex);
            }


        }
    }

    private void convertData(Object sourceObject, Object targetObject) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {

        PropertyDescriptor[] fromDescriptors = null;
        PropertyDescriptor[] toDescriptors = null;
        if (!(sourceObject instanceof Map)) {
            fromDescriptors = ReflectUtils.getPropertyDescriptors(sourceObject.getClass());

        }
        if (!(targetObject instanceof Map) && !(targetObject instanceof ModelData)) {
            toDescriptors = ReflectUtils.getPropertyDescriptors(targetObject.getClass());

        }

        Map<String, PropertyDescriptor> propertyMap = null;
        Set<String> keys = null;
        if (fromDescriptors != null) {
            propertyMap = getSourceProperty(fromDescriptors);
        } else {
            keys = this.getSourceKey(sourceObject);
        }

        if (toDescriptors != null) {
            for (PropertyDescriptor propertyDescriptor : toDescriptors) {
                if ((propertyMap != null && propertyMap.containsKey(propertyDescriptor.getName()))
                        || (keys != null && keys.contains(propertyDescriptor.getName()))) {
                    Object value = getValue(sourceObject, propertyDescriptor.getName(), keys, propertyMap);
                    if (validateData(value)) {
                        propertyDescriptor.getWriteMethod().invoke(targetObject, value);
                    }
                }
            }
        } else {
            Iterator<String> targetKeys = ((Map<String, ?>) targetObject).keySet().iterator();

            while (targetKeys.hasNext()) {
                String key = targetKeys.next();
                if ((propertyMap != null && propertyMap.containsKey(key))
                        || (keys != null && keys.contains(key))) {
                    Object value = getValue(sourceObject, key, keys, propertyMap);
                    if (validateData(value)) {
                        DataUtils.setValue(targetObject, key, sourceObject);
                    }
                }
            }
        }
    }

    private Object getValue(Object sourceObject, String key, Set<String> keySet, Map<String, PropertyDescriptor> propertyDescriptorMap) throws InvocationTargetException, IllegalAccessException {
        if (keySet != null) {
            return ((Map<String, ?>) sourceObject).get(key);
        }
        return propertyDescriptorMap.get(key).getReadMethod().invoke(sourceObject);
    }

    private Map<String, PropertyDescriptor> getSourceProperty(PropertyDescriptor[] descriptors) {
        Map<String, PropertyDescriptor> map = new HashMap<>();
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            map.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        return map;
    }

    private Set<String> getSourceKey(Object sourceObject) {
        if (sourceObject instanceof Map) {
            return ((Map) sourceObject).keySet();
        }else if(sourceObject instanceof ModelData){
            return ((ModelData) sourceObject).getValues().keySet();
        }
        return null;
    }

    private boolean validateData(Object value) {

        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Collections) {
            return true;
        }
        return false;
    }

    private void executeRule(ProcessDesc process) {
        try {
            multipleTranContainer.load(this.modelLoaderMap.get(process.getRule()));
            multipleTranContainer.execute(process.getRuleStart(), process.getRuleEnd());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void change(SystemDesc systemDesc, DataDesc dataDesc) throws Exception {
        if (dataDesc.getChangeDescList() != null) {
            for (ChangeDesc changeDesc : dataDesc.getChangeDescList()) {
                Object dataObject = dataStorage.get(changeDesc.getName());
                DataUtils.setValue(dataObject, changeDesc.getValueDescList());
                this.result = SystemContext.get()
                        .get(systemDesc.getName()).change(changeDesc.getName(), this.dataStorage);
                if (!result.isSuccess()) {
                    log.error(String.format("Change status error,depend:[%]", changeDesc.getName()));
                    break;
                }
            }
        }
    }

    private void produceData(DataDesc dataDesc, String dataName, String system) throws Exception {

        List<DataDependDesc> dataDependDescs = null;

        if (dataDesc != null) {

            dataDependDescs = dataDesc.getDataDepends();

            if (dataDependDescs != null) {
                for (DataDependDesc dataDependDesc : dataDependDescs) {

                    String data = dataDependDesc.getData();

                    if (!dataStorage.containsData(data)) {

                        if (dataDependDesc.getParam() != null) {
                            for (ValueDesc valueDesc : dataDependDesc.getParam()) {
                                Object dataObject = dataStorage.get(valueDesc.getProperty()[0]);
                                if (dataObject == null) {
                                    String msg = String.format("Get param error, the [%s] is not exist, depend:[%s], paramConfig:[%s], error express:[%s]",
                                            valueDesc.getProperty()[0], data, dataDependDesc.getParamExpress(), valueDesc.getExpress());
                                    log.error(msg);
                                    throw new ExecuteException(msg);
                                }
                                try {
                                    dataStorage.addParam((String) valueDesc.getValue(), DataUtils.getValue(dataObject, valueDesc.getProperty(), 1));
                                } catch (Exception e) {
                                    log.error(String.format("Get param error, the param value can't get, depend:[%s], paramConfig:[%s], error express:[%s]",
                                            data, dataDependDesc.getParamExpress(), valueDesc.getExpress()));
                                    throw new ExecuteException(e);
                                }
                            }
                        }
                        DataDesc depnedDataDesc = getDataDesc(system, dataDependDesc);

                        log.info("Code:{}, start produce depend data, [{}]-[{}]", code, system, data);

                        produceData(depnedDataDesc, data, system);

                        if (!result.isSuccess()) {
                            return;
                        }

                        log.info("Code:{}, end produce depend data,  [{}]-[{}]", code, system, data);
                    }

                    if (dataDependDesc.getInit() != null) {
                        try {
                            DataUtils.setValue(dataStorage.getDataMap().get(data), dataDependDesc.getInit());
                        } catch (Exception e) {
                            log.error(String.format("Init status error, depend:[%s], initConfig:[%s]",
                                    data, dataDependDesc.getInitExpress()));
                            throw new ExecuteException(e);
                        }
                    }

                    if (dataDependDesc.getCondition() != null) {
                        if (!DataUtils.check(dataStorage.getDataMap().get(data), dataDependDesc.getCondition())) {
                            String message = String.format("Check [%s]-[%s] condition error, condition:[%s]", system, data, dataDependDesc.getCondition());
                            log.error(message);
                            throw new ExecuteException(message);
                        }
                    }

                }
            }
        }

        System executeSystem = null;

        if ("this".equals(system)) {

            executeSystem = currentSystem;

        } else {

            executeSystem = SystemContext.get()
                    .get(system);
        }

        result = executeSystem.produce(dataName, dataStorage);

        if (result.isSuccess()) {


            if (result.getData() != null) {

                if (result.getDataType() == null) {

                    dataStorage.add(dataName, result.getData());

                } else {
                    dataStorage.add(result.getDataType(), result.getData());
                }

                /*if (!conumeRecordSet.contains(system + ":" + dataName)) {

                    try {

                        result = executeSystem.conume(dataName, this.dataStorage);

                        conumeRecordSet.add(system + ":" + dataName);

                    } catch (Exception e) {

                        log.error("Code:{}, conume data,  [{}]-[{}]", code, system, dataName, e);

                        result = ExecuteResult.fail(null, null, e);

                    }
                }*/

                if (!result.isSuccess()) {

                    if (dataDependDescs != null) {

                        for (DataDependDesc dataDependDesc : dataDependDescs) {

                            if (dataDependDesc.getType() == 1)
                                continue;

                            DataDesc depnedDataDesc = getDataDesc(system, dataDependDesc.getData());

                            refreshStorage(depnedDataDesc);
                        }
                    }
                }
            }
        }
    }


    private DataDesc getDataDesc(String system, DataDependDesc dataDependDesc) {


        if (dataDependDesc.getType() == 1) {

            return getDataDesc("common", dataDependDesc.getData());
        } else {

            return getDataDesc(system, dataDependDesc.getData());
        }


    }

    private DataDesc getDataDesc(String system, String data) {

        if (system == null) {

            return DescContext.get().getData(data);

        } else {

            SystemDesc systemDesc = DescContext.get().getSystem(system);

            return systemDesc.getData(data);
        }
    }


    private void refreshStorage(DataDesc dataDesc) {

        if (dataDesc != null && dataDesc.getType() == DataTypeEnum.PERSISTENT && !dataDesc.isCachePrior()) {

            this.dataStorage.remove(dataDesc.getName());
        }

    }

    /**
     * type,group,rollBack
     */
    //@Override
    /*private void transaction(TransactionPolicy transactionPolicy,
                             RollBackPolicy rollBackPolicy, String transactionGroup) {

        currentProcess.setTransaction(transactionPolicy);

        currentProcess.setRollBackPolicy(rollBackPolicy);

        currentProcess.setTransactionGroup(transactionGroup);
    }*/
    private void validate(String system, String data) {
        SystemDesc systemDesc = null;
        if ("this".equals(system)) {
            systemDesc = DescContext.get().getSystem("common");
        } else {
            systemDesc = DescContext.get().getSystem(system);
        }

        if (systemDesc == null) {
            log.error("The system is not exist:" + system);
            throw new ExecuteException("The system is not exist:" + system);
        }
        DataDesc dataDesc = systemDesc.getData(data);
        if (dataDesc == null) {
            log.error("The data is not exist:" + system + "-" + data);
            throw new ExecuteException("The data is not exist:" + system + "-" + data);
        }
    }

    private int getTransactionPolicy(TransactionPolicy transactionPolicy) {
        switch (transactionPolicy) {
            case REQUIRE:
                return Transaction.PROPAGATION_REQUIRED;
            case NEW:
                return Transaction.PROPAGATION_REQUIRES_NEW;
            case NESTED:
                return Transaction.PROPAGATION_REQUIRED_NESTED;
            case SUPPORTED:
                return Transaction.PROPAGATION_SUPPORTS;
            case NOSUPPORTED:
                return Transaction.PROPAGATION_NOT_SUPPORTED;
        }
        return -1;
    }
}
