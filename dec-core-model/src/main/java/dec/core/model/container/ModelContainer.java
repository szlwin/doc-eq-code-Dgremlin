package dec.core.model.container;

import artoria.beans.BeanUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dec.core.collections.list.SimpleList;
import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.connection.exception.ConectionException;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.connection.DataConnectionFactory;
import dec.core.model.container.listener.ContainerEvent;
import dec.core.model.container.listener.ContainerEventEnum;
import dec.core.model.container.listener.ContainerListener;
import dec.core.model.execute.rule.RuleContainer;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ModelContainer implements Container {
    private final static Logger log = LoggerFactory.getLogger(ModelContainer.class);

    protected ResultInfo resultInfo = new ResultInfo();

    protected Map<String, DataConnection<?, ?>> conMap = new FastMap<String, DataConnection<?, ?>>();

    protected boolean isAuto = true;

    protected ContainerListener listener;

    //protected ContainerEvent containerEvent;

    public ModelContainer() {
        resultInfo.setSuccess(true);
    }

    private List<ModelLoader> list
            = new SimpleList<ModelLoader>(4, 4);

    @Override
    public Container addListener(ContainerListener listener) {
        this.listener = listener;
        return this;
    }

    public Container load(ModelLoader modelLoader) {

        String conName = modelLoader.getConName();

        if (!conMap.containsKey(conName))
            conMap.put(conName, null);

        if (modelLoader.getRuleConnectionInfo() != null) {
            Collection<String> conCol = modelLoader.getRuleConnectionInfo().values();
            conCol.stream().forEach(con -> conMap.put(con, null));
        }
        list.add(modelLoader);
        return this;

    }

    public Container execute() throws ExecuteRuleException {
        boolean isOk = true;
        ResultInfo result = null;
        int i = 0;
        ModelLoader modelLoader = null;
        try {
            begain();

            if (this.resultInfo != null && !this.resultInfo.isSuccess()) {
                return this;
            }

            for (; i < list.size(); i++) {

                modelLoader = list.get(i);

                result = execute(modelLoader);

                if (!result.isSuccess()) {
                    log.error("Execute the rule: {}--{}, error:{},{} false!", modelLoader.getRuleName(), result.getRuleName(),
                            result.getErrorName(), result.getErrorMsg());
                    break;
                }
            }

        } catch (Exception e) {
            isOk = false;
            log.error("11",e);
            log.error("Execute error,rule:{}", modelLoader.getRuleName(), e);

            throw new ExecuteRuleException(e, modelLoader.getRuleName(), modelLoader.getConName());
        } finally {
            copy(result);
            boolean isSuccess = result != null && result.isSuccess() && isOk;
            try {

                end(isSuccess);
            } catch (ConectionException e) {
                log.error(e.getMessage(), e);
                if (isSuccess) {
                    throw new ExecuteRuleException(e, modelLoader.getRuleName(), modelLoader.getConName());
                }
            }
        }

        return this;
    }

    protected ResultInfo execute(ModelLoader modelLoader) throws ExecuteException, ExecuteRuleException {

        log.info("Start Execute the view rule: {}", modelLoader.getRuleName());

        String conName = modelLoader.getConnection(modelLoader.getRuleName());
        if (conName == null) {
            conName = modelLoader.getConName();
        }
        RuleContainer ruleExecute = new RuleContainer(modelLoader, conMap.get(conName));

        ResultInfo resultInfo = ruleExecute.execute();

        if (resultInfo.isSuccess()) {
            if (modelLoader.get().getOriginData() != null && modelLoader.get().getValues() != null) {
                Object obj = JSON.toJavaObject((JSON) modelLoader.get().getValues(), modelLoader.get().getOriginData().getClass());
                BeanUtils.copy(obj, modelLoader.get().getOriginData());
            }
        }
        log.info("End Execute the view rule: {}", modelLoader.getRuleName());

        return resultInfo;
    }

    public ResultInfo getResult() {
        return resultInfo;
    }

    protected void copy(ResultInfo srcInfo) {
        this.resultInfo = srcInfo;
    }


    protected void begain() throws ConectionException {

        if (listener != null) {
            ContainerEvent containerEvent = new ContainerEvent();
            containerEvent.setLoaderList(list);
            containerEvent.setType(ContainerEventEnum.CONTAINER_START);
            ResultInfo resultInfo = listener.notify(containerEvent);

            if (!resultInfo.isSuccess()) {

                copy(resultInfo);

                return;
            }

        }

        Set<String> conNameSet = conMap.keySet();
        Iterator<String> it = conNameSet.iterator();

        while (it.hasNext()) {

            String conName = it.next();

            DataConnection<?, ?> con = DataConnectionFactory.getInstance().getConnection(conName);

            con.connect();
            conMap.put(conName, con);

        }
    }

    protected void end(boolean isSuccess) throws ConectionException {
        boolean isOK = true;
        try {
            if (isSuccess)
                commit();
        } catch (ConectionException e) {
            isOK = false;
            throw e;
        } finally {

            if (listener != null) {
                ContainerEvent containerEvent = new ContainerEvent();
                containerEvent.setLoaderList(list);
                containerEvent.setType(ContainerEventEnum.CONTAINER_END);
                containerEvent.setResultInfo(resultInfo);
                listener.notify(containerEvent);
            }

            try {
                if (!isOK || !isSuccess)
                    roolback();

            } catch (ConectionException e) {
                throw e;
            } finally {
                close();

                clear();
            }

        }
    }

    private void commit() throws ConectionException {
        operator(0);
    }

    private void close() throws ConectionException {
        operator(1);
    }

    private void roolback() throws ConectionException {
        operator(2);
    }

    protected void operator(int type) throws ConectionException {

        Collection<DataConnection<?, ?>> conCollection = conMap.values();
        Iterator<DataConnection<?, ?>> it = conCollection.iterator();

        while (it.hasNext()) {
            DataConnection<?, ?> con = it.next();

            if (con == null)
                continue;

            switch (type) {
                case 0:
                    con.commit();
                    break;
                case 1:
                    try {
                        con.close();
                    } catch (ConectionException e) {
                        log.error("Close connection error", e);
                    }
                    break;
                case 2:
                    try {
                        con.rollback();
                    } catch (ConectionException e) {
                        log.error("Close connection error", e);
                    }
                    break;
                default:
                    con.close();
            }
        }
    }

    private void clear() {
        conMap.clear();
        list.clear();
        resultInfo = new ResultInfo();
    }


}
