package dec.core.context.config.model.directory;

import dec.core.context.config.model.rule.RuleDefineInfo;

public class ChangeInfo {
    private String[] property;

    private RuleDefineInfo ruleDefineInfo;

    public RuleDefineInfo getRuleDefineInfo() {
        return ruleDefineInfo;
    }

    public void setRuleDefineInfo(RuleDefineInfo ruleDefineInfo) {
        this.ruleDefineInfo = ruleDefineInfo;
    }

    public String[] getProperty() {
        return property;
    }

    public void setProperty(String[] property) {
        this.property = property;
    }
}
