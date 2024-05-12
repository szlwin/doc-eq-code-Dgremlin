package dec.demo.declaration.datasource;

import dec.expand.declare.business.DefaultBusinessDeclare;
import dec.expand.declare.datasorce.ConnecionDesc;
import dec.expand.declare.datasorce.DataSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class MockDataSourceManager implements DataSourceManager {

    private int index = 0;
    private final static Logger log = LoggerFactory.getLogger(MockDataSourceManager.class);

    private List<ConnecionDesc> transactionStatusList = new ArrayList<>();

    public void connect(ConnecionDesc connecionDesc) {

        transactionStatusList.add(connecionDesc);
        index = transactionStatusList.size() - 1;
        log.info("connect group:"+connecionDesc.getGroup());
    }

    public void commit() {
        log.info("commit group:"+transactionStatusList.get(index).getGroup());
        index = index - 1;
    }


    public void close() {

    }

    public void rollBack() {
        System.out.println("rollBack group:"+transactionStatusList.get(index).getGroup());
        index = index - 1;
    }



}
