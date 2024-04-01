package dec.expand.declare.conext.desc.process;

import dec.expand.declare.collections.SimpleList;

import java.util.List;

public class ProcessDesc {

    private String data;


    private String transactionGroup;

    private RollBackPolicy rollBackPolicy;

    private TransactionPolicy transaction;

    private String dataSource;

    private String system;

    private boolean begin = false;

    private boolean end = false;

    private String ruleStart;

    private String rule;

    private String ruleEnd;

    private String ruleDataSource;

    private String ruleReplace;

    private List<PropertyDesc> ruleRefreshList;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTransactionGroup() {
        return transactionGroup;
    }

    public void setTransactionGroup(String transactionGroup) {
        this.transactionGroup = transactionGroup;
    }

    public RollBackPolicy getRollBackPolicy() {
        return rollBackPolicy;
    }

    public void setRollBackPolicy(RollBackPolicy rollBackPolicy) {
        this.rollBackPolicy = rollBackPolicy;
    }

    public TransactionPolicy getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionPolicy transaction) {
        this.transaction = transaction;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public boolean isBegin() {
        return begin;
    }

    public void setBegin(boolean begin) {
        this.begin = begin;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public String getRuleStart() {
        return ruleStart;
    }

    public void setRuleStart(String ruleStart) {
        this.ruleStart = ruleStart;
    }

    public String getRuleEnd() {
        return ruleEnd;
    }

    public void setRuleEnd(String ruleEnd) {
        this.ruleEnd = ruleEnd;
    }

    public String getRuleDataSource() {
        return ruleDataSource;
    }

    public void setRuleDataSource(String ruleDataSource) {
        this.ruleDataSource = ruleDataSource;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getRuleReplace() {
        return ruleReplace;
    }

    public void setRuleReplace(String ruleReplace) {
        this.ruleReplace = ruleReplace;
    }

    public List<PropertyDesc> getRuleRefreshList() {
        return ruleRefreshList;
    }

    public void setRuleRefresh(String ruleRefresh) {
        if (ruleRefresh == null) {
            return;
        }
        if (this.ruleRefreshList == null) {
            this.ruleRefreshList = new SimpleList<>();
        }
        String[] propertyArray = ruleRefresh.split(",");
        for (String property : propertyArray) {
            PropertyDesc propertyDesc = new PropertyDesc();
            String[] propertyStr = property.split(":");
            if (propertyStr[0].indexOf(".") > 0) {
                propertyDesc.setSourceProperty(propertyStr[0].split("\\."));
            } else {
                propertyDesc.setSourceProperty(new String[]{propertyStr[0]});
            }

            if (propertyStr[1].indexOf(".") > 0) {
                propertyDesc.setTargetProperty(propertyStr[1].split("\\."));
            } else {
                propertyDesc.setTargetProperty(new String[]{propertyStr[1]});
            }

            this.ruleRefreshList.add(propertyDesc);
        }
    }
}
