package dec.expand.declare.conext;

import java.util.HashMap;
import java.util.Map;

public class DataStorage {

    private Map<String, Object> dataMap = new HashMap<>();

    private Map<String, Object> paramMap = new HashMap<>();

    private Map<String, Object> statusMap = new HashMap<>();

    public void remove(String key) {
        dataMap.remove(key);
    }

    public void addParam(String key, Object data) {
        paramMap.put(key, data);
    }

    public Object getParam(String key) {
        return paramMap.get(key);
    }

    public void add(String key, Object data) {
        dataMap.put(key, data);
    }

    public Object get(String key) {
        return dataMap.get(key);
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public boolean containsData(String data) {
        return dataMap.containsKey(data);
    }

    public Object getStatus(String key) {
        if (statusMap == null) {
            return null;
        }
        return statusMap.get(key);
    }

    public void setStatusMap(Map<String, Object> statusMap) {
        this.statusMap = statusMap;
    }
}
