package dec.expand.declare.business;

import dec.expand.declare.conext.DescContext;
import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.process.ProcessDesc;
import dec.expand.declare.conext.exception.ContextConfigException;

import java.util.List;

public class BusinessDeclareFactory {

    public static DefaultBusinessDeclare createDefaultBusinessDeclare(String name) {
        BusinessDesc businessDesc = DescContext.get().getBusiness(name);
        if (businessDesc == null) {
            throw new ContextConfigException("The business:" + name + " is not exist!");
        }

        DefaultBusinessDeclare defaultBusinessDeclare = new DefaultBusinessDeclare(businessDesc.getAllViewRuleDesc() != null
                && !businessDesc.getAllViewRuleDesc().isEmpty());
        defaultBusinessDeclare.build(name);

        List<ProcessDesc> processDescList = businessDesc.getProcesses();
        int currentGroup = 0;
        for (ProcessDesc processDesc : processDescList) {

            if (processDesc.isBegin()) {
                currentGroup++;
            }
            if (currentGroup != 0) {
                processDesc.setTransactionGroup(String.valueOf(currentGroup));
            }
            if (processDesc.isEnd()) {
                currentGroup--;
            }
            defaultBusinessDeclare.data(processDesc);
        }
        return defaultBusinessDeclare;
    }
}
