package dec.expand.declare.flow;

import dec.expand.declare.business.BusinessDeclare;

public interface WorkFlowDeclare {

    void addBusiness(BusinessDeclare businessDeclare);

    void begin(String business, String step);

    void end(String business, String step);

    void execute();


}
