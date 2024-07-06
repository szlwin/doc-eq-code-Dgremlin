package dec.core.model.execute.tran.advance;

import dec.core.collections.list.SimpleList;
import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.connection.exception.ConectionException;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.connection.DataConnectionFactory;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import dec.core.model.execute.tran.DefaultExecuter;
import dec.core.model.execute.tran.TranExecuter;
import dec.core.model.execute.tran.Transaction;
import dec.external.datasource.sql.connection.SqlDBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Savepoint;

public class MultipleTranContainer {

    private final static Logger log = LoggerFactory.getLogger(MultipleTranContainer.class);

    private ModelLoader modelLoader;

    private DataConnection currentDataConnection;

    private SimpleList<DataConnection> dataConnectionList;

    private boolean isNested;

    private int conFlag = 1;

    public void load(ModelLoader modelLoader) {
        this.modelLoader = modelLoader;
    }

    public void begin(String conName, int type) throws ConectionException, SQLException {
        if (dataConnectionList == null) {
            dataConnectionList = new SimpleList<>();
        }
        currentDataConnection = this.createConnection(conName, type);
        if (!currentDataConnection.isConnect()) {
            currentDataConnection.connect();
        }

        currentDataConnection.setFlag(currentDataConnection.getFlag() + 1);
        if (dataConnectionList.getLast() == null && dataConnectionList.size() > 0) {
            dataConnectionList.setLast(currentDataConnection);
        } else {
            dataConnectionList.add(currentDataConnection);
        }
    }

    public ResultInfo execute(String startRule, String endRule) throws ExecuteRuleException, ExecuteException, SQLException {

        log.info("Execute the tran, view rule: " + modelLoader.getRuleName() + " start!");

        TranExecuter tranExecuter = new DefaultExecuter();
        tranExecuter.setName(modelLoader.getRuleName());
        tranExecuter.load(modelLoader);
        tranExecuter.setTranType(currentDataConnection.getTransactionType());
        tranExecuter.setConnection(this.currentDataConnection);
        if (isNested) {
            this.setSavePoint(tranExecuter);
            isNested = false;
        }
        ResultInfo resultInfo = null;
        try {
            resultInfo = tranExecuter.execute(startRule, endRule);
        } catch (Exception ex) {
            log.error("Execute the rule error, view rule: " + startRule + ":" + endRule + "end!");
            this.rollback();
            throw ex;
        }

        log.info("Execute the tran, view rule: " + modelLoader.getRuleName() + " end!");
        if (!resultInfo.isSuccess()) {
            this.rollback();
        }
        return resultInfo;
    }

    public void end() throws ConectionException {
        currentDataConnection.setFlag(currentDataConnection.getFlag() - 1);
        if (currentDataConnection.getFlag() == 0) {
            commit();
        }
        currentDataConnection = dataConnectionList.get(dataConnectionList.size()-2);
        dataConnectionList.setLast(null);
    }

    private void commit() throws ConectionException {
        try {
            currentDataConnection.commit();
        } catch (ConectionException e) {
            log.error("Commit error", e);
            throw e;
        }
    }

    private void rollback() {
        if (dataConnectionList != null) {
            for (DataConnection dataConnection : dataConnectionList) {
                try {
                    dataConnection.rollback();
                } catch (Exception ex) {
                    log.error("Rollback error", ex);
                } finally {
                    try {
                        dataConnection.close();
                    } catch (ConectionException e) {
                        log.error("Close error", e);
                    }
                }
            }
        }
    }

    private DataConnection getRequiredConnection(String conName) throws ConectionException, SQLException {
        DataConnection con = getDefaultConnection(conName);
        con.setAutoCommit(false);
        con.setTransactionType(Transaction.PROPAGATION_REQUIRED);
        return con;
    }

    private DataConnection getDefaultConnection(String conName) throws ConectionException, SQLException {
        DataConnection con = DataConnectionFactory.getInstance().getConnection(conName);
        return con;
    }

    private DataConnection getNewConnection(String conName) throws ConectionException, SQLException {
        DataConnection con = getDefaultConnection(conName);
        con.setAutoCommit(false);
        con.setTransactionType(Transaction.PROPAGATION_REQUIRES_NEW);
        return con;
    }

    private DataConnection getNoSupportConnection(String conName) throws ConectionException, SQLException {
        DataConnection con = getDefaultConnection(conName);
        con.setAutoCommit(true);
        con.setTransactionType(Transaction.PROPAGATION_NOT_SUPPORTED);
        return con;
    }

    private DataConnection getSupportConnection(String conName) throws ConectionException, SQLException {
        DataConnection con = getDefaultConnection(conName);
        con.setAutoCommit(true);
        con.setTransactionType(Transaction.PROPAGATION_SUPPORTS);
        return con;
    }

    private DataConnection getNestedConnection(String conName) throws ConectionException, SQLException {
        DataConnection con = getDefaultConnection(conName);
        con.setAutoCommit(false);
        con.setTransactionType(Transaction.PROPAGATION_REQUIRED_NESTED);
        return con;
    }

    private DataConnection getConnection(String conName, int type) throws ConectionException, SQLException {
        switch (type) {
            case Transaction.PROPAGATION_REQUIRED:
                return getRequiredConnection(conName);
            case Transaction.PROPAGATION_REQUIRES_NEW:
                return getNewConnection(conName);
            case Transaction.PROPAGATION_NOT_SUPPORTED:
                return getNoSupportConnection(conName);
            case Transaction.PROPAGATION_SUPPORTS:
                return getSupportConnection(conName);
            case Transaction.PROPAGATION_REQUIRED_NESTED:
                return getNestedConnection(conName);
        }
        throw new ConectionException("Error transaction type");
    }

    private DataConnection createConnection(String conName, int type) throws ConectionException, SQLException {
        if (type == Transaction.PROPAGATION_REQUIRES_NEW || currentDataConnection == null) {
            return getConnection(conName, type);
        }

        DataConnection preDataConnection = null;
        for (int i = dataConnectionList.size() - 1; i >= 0; i--) {
            DataConnection dataConnection = dataConnectionList.get(i);
            if (dataConnection.getConName().equals(conName)) {
                switch (dataConnection.getTransactionType()) {
                    case Transaction.PROPAGATION_REQUIRED:
                    case Transaction.PROPAGATION_REQUIRES_NEW:
                    case Transaction.PROPAGATION_REQUIRED_NESTED:
                        if (type == Transaction.PROPAGATION_REQUIRED
                                || type == Transaction.PROPAGATION_SUPPORTS
                                || type == Transaction.PROPAGATION_REQUIRED_NESTED) {
                            preDataConnection = dataConnection;
                        }
                        if (type == Transaction.PROPAGATION_REQUIRED_NESTED)
                            isNested = true;
                        break;
                    case Transaction.PROPAGATION_NOT_SUPPORTED:
                    case Transaction.PROPAGATION_SUPPORTS:
                        if (type == Transaction.PROPAGATION_SUPPORTS) {
                            preDataConnection = dataConnection;
                        } else if (type == Transaction.PROPAGATION_NOT_SUPPORTED) {
                            if (!dataConnection.isAutoCommit()) {
                                preDataConnection = dataConnection;
                            }
                        }
                        break;
                }
            }
        }
        if (preDataConnection != null) {
            return preDataConnection;
        } else {
            if (type == Transaction.PROPAGATION_REQUIRED_NESTED) {
                return getConnection(conName, Transaction.PROPAGATION_REQUIRED);
            }
        }
        return getConnection(conName, type);
    }

    private void setSavePoint(TranExecuter tranExecuter) throws SQLException {
        SqlDBConnection con = (SqlDBConnection) tranExecuter.getConnection();
        String flag = String.valueOf(conFlag);

        Savepoint savepoint = con.getSavepoint(flag);
        tranExecuter.setSavepoint(savepoint);
        tranExecuter.setConnection(con);
        conFlag++;
    }
}
