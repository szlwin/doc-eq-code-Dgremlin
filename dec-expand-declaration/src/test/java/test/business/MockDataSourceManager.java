package test.business;

import dec.expand.declare.datasorce.ConnecionDesc;
import dec.expand.declare.datasorce.DataSourceManager;

import java.util.ArrayList;
import java.util.List;


public class MockDataSourceManager implements DataSourceManager {

    private int index = 0;
    private List<ConnecionDesc> transactionStatusList = new ArrayList<>();

    public void connect(ConnecionDesc connecionDesc) {

        transactionStatusList.add(connecionDesc);
        index = transactionStatusList.size() - 1;
        System.out.println("connect group:"+connecionDesc.getGroup());
    }

    public void commit() {
        System.out.println("commit group:"+transactionStatusList.get(index).getGroup());
        index = index - 1;
    }


    public void close() {

    }

    public void rollBack() {

    }



}
