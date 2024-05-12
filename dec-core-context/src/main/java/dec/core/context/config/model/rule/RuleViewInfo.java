package dec.core.context.config.model.rule;

import dec.core.context.collections.list.SimpleList;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.view.ViewData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleViewInfo extends ConfigBaseData {

    public final static String NODE_NAME = "rule-view-info";

    public final static String RULE = "rule";

    public final static String NAME = "name";

    public final static String VIEW_REF = "view-ref";

    private String name;

    private ViewData viewData;

    private List<RuleDefineInfo> ruleList = new SimpleList<RuleDefineInfo>(6, 2);

    private Map<String, Integer> ruleNameMap = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String viewName) {
        this.name = viewName;
    }

    //public String getViewRef() {
    //	return viewRef;
    //}

    //public void setViewRef(String viewRef) {
    //	this.viewRef = viewRef;
    //}

    public void addRule(RuleDefineInfo ruleDefineInfo) {
        ruleNameMap.put(ruleDefineInfo.getName(), ruleList.size());
        ruleList.add(ruleDefineInfo);
    }

    public List<RuleDefineInfo> getRules() {
        return ruleList;
    }

    public ViewData getViewData() {
        return viewData;
    }

    public void setViewData(ViewData viewData) {
        this.viewData = viewData;
    }

	public Integer getRuleIndex(String name){
		return ruleNameMap.get(name);
	}
}
