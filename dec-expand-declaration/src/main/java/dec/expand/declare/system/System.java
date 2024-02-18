package dec.expand.declare.system;

import dec.expand.declare.conext.DataStorage;
import dec.expand.declare.executer.change.Change;
import dec.expand.declare.executer.conume.Conumer;
import dec.expand.declare.executer.get.GetExecuter;
import dec.expand.declare.executer.produce.Produce;
import dec.expand.declare.executer.save.SaveExecuter;
import dec.expand.declare.service.ExecuteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class System {

    private final static Logger log = LoggerFactory.getLogger(System.class);

    private String name;

    private Map<String, Produce<DataStorage>> produceMap;

    private Map<String, Change<DataStorage>> changeMap;

    private Map<String, GetExecuter<DataStorage>> getMap;

    private Map<String, SaveExecuter<DataStorage>> saveMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(Produce<DataStorage> produce) {
        if (produceMap == null) {
            produceMap = new HashMap<>();
        }

        produceMap.put(produce.getName(), produce);
    }

    public void add(Change<DataStorage> change) {
        if (changeMap == null) {
            changeMap = new HashMap<>();
        }

        changeMap.put(change.getName(), change);
    }

    public ExecuteResult get(String data, DataStorage dataStorage) {

        if (getMap != null && getMap.containsKey(data)) {
            try {
                GetExecuter<?> getExecuter = getMap.get(data);
                return getExecuter.execute(dataStorage);
            } catch (Exception e) {
                log.error("Get error, data:{}", data, e);
                return ExecuteResult.fail(null, null, e);
            }
        }

        return ExecuteResult.success();
    }

    public ExecuteResult save(String data, DataStorage dataStorage) {

        if (saveMap != null && saveMap.containsKey(data)) {
            try {
                SaveExecuter<?> saveExecuter = saveMap.get(data);
                return saveExecuter.execute(dataStorage);
            } catch (Exception e) {
                log.error("Get error, data:{}", data, e);
                return ExecuteResult.fail(null, null, e);
            }
        }

        return ExecuteResult.success();
    }

    public ExecuteResult produce(String data, DataStorage dataStorage) {

        ExecuteResult executeResult = null;

        if (!produceMap.containsKey(data)) {
            if(dataStorage.containsData(data)){
                return ExecuteResult.success();
            }
            throw new RuntimeException("Undifined data produce:[" + getName() + "]-" + "[" + data + "]");
        }

        try {

            executeResult = produceMap.get(data).execute(dataStorage);

        } catch (Exception e) {

            log.error("Produce error, data:{}", data, e);
            executeResult = ExecuteResult.fail(null, null, e);
        }


        return executeResult;
    }

    public ExecuteResult change(String data, DataStorage dataStorage) {

        ExecuteResult executeResult = null;

        if (!changeMap.containsKey(data)) {
            throw new RuntimeException("Undifined data change:[" + getName() + "]-" + "[" + data + "]");
        }

        try {

            executeResult = changeMap.get(data).execute(dataStorage);

        } catch (Exception e) {

            log.error("change error, data:{}", data, e);
            executeResult = ExecuteResult.fail(null, null, e);
        }


        return executeResult;
    }
}
