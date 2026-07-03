package dec.core.context.config.model.directory;

import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.rule.RuleDefineInfo;

import java.util.List;

public class Action extends ConfigBaseData {

    private String name;

    private String refRule;
    //private List<RuleDefineInfo> ruleList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRefRule() {
        return refRule;
    }

    public void setRefRule(String refRule) {
        this.refRule = refRule;
    }

    //public List<RuleDefineInfo> getRuleList() {
    //    return ruleList;
    //}

    //public void setRuleList(List<RuleDefineInfo> ruleList) {
    //    this.ruleList = ruleList;
    //}
}
