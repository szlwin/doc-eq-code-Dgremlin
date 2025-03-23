package dec.core.context.config.model.directory.config;

import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.directory.DirectoryInfo;
import javolution.util.FastMap;

import java.util.Map;

public class DirectoryConfig implements Config<DirectoryInfo> {

    private static final DirectoryConfig ruleConfig = new DirectoryConfig();

    private Map<String, DirectoryInfo> ruleMap = new FastMap();

    private DirectoryConfig() {

    }

    public static DirectoryConfig getInstance() {
        return ruleConfig;
    }

    public void add(DirectoryInfo directoryInfo) {
        ruleMap.put(directoryInfo.getName(), directoryInfo);
    }

    public DirectoryInfo get(String name) {
        return ruleMap.get(name);
    }

    public <E extends ConfigBaseData> void add(E directoryInfo) {
        add((DirectoryInfo)directoryInfo);
    }


}
