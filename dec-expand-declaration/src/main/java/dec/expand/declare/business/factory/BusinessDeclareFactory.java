package dec.expand.declare.business.factory;

import dec.expand.declare.business.DefaultBusinessDeclare;
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

        DefaultBusinessDeclare defaultBusinessDeclare = new DefaultBusinessDeclare();
        defaultBusinessDeclare.build(name);
        List<ProcessDesc> processDescList = businessDesc.getProcesses();
        for (ProcessDesc processDesc : processDescList) {
            if (processDesc.isOnlyEnd()) {
                defaultBusinessDeclare.endTx();
                continue;
            }
            if (processDesc.isBegin()) {
                defaultBusinessDeclare.beginTx();
            }
            defaultBusinessDeclare.data(processDesc.getSystem(), processDesc.getData());
            if (processDesc.isEnd()) {
                defaultBusinessDeclare.endTx();
            }
        }
        return defaultBusinessDeclare;
    }
}
