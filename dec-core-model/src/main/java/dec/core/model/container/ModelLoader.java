package dec.core.model.container;

import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.context.data.ModelData;
import dec.core.model.container.listener.ViewListener;

import java.util.HashMap;
import java.util.Map;

//import dec.core.sql.util.Util;

public class ModelLoader {

    private String ruleName;

    private ModelData e;

    private String conName;

    private ViewListener listener;

    private Map<String, Object> external;

    private Map<String, String> ruleConMap;

    public Map<String, Object> getExternal() {
        return external;
    }

    public ModelLoader addExternalParam(Map<String, Object> external) {
        if (this.external == null) {
            this.external = new HashMap<>();
        }
        this.external.putAll(external);
        return this;
    }

    public ModelLoader load(String name, ModelData e) {
        load(name, e, ConfigContextUtil.getDefaultCon());
        return this;
    }

    public String getRuleName() {
        return ruleName;
    }

    public ModelData get() {
        return e;
    }

    public ModelLoader load(String name, ModelData e, String conName) {
        this.ruleName = name;
        this.e = e;
        this.conName = conName;

        return this;
    }

    public void addConnectionWithRule(String ruleName, String conName) {
        ruleConMap.put(ruleName, conName);
    }

    public Map<String, String> getRuleConnectionInfo() {
        return ruleConMap;
    }

    public String getConnection(String ruleName) {
        if (ruleConMap == null) {
            return null;
        }
        return ruleConMap.get(ruleName);
    }

    public ModelLoader addListener(ViewListener listener) {
        this.listener = listener;
        return this;
    }

    public String getConName() {
        return conName;
    }

    public ViewListener getListener() {
        return listener;
    }


}
