package dec.external.datasource.sql.connection;

import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.datasource.connection.AbstractConnection;
import dec.core.datasource.connection.exception.ConectionException;
import dec.core.datasource.execute.dom.ExecuteInfo;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.external.datasource.sql.connection.factory.ConnectionFactory;
import dec.external.datasource.sql.dom.ConvertInfo;
import dec.external.datasource.sql.dom.ConvertParam;
import dec.external.datasource.sql.dom.ExecuteParam;
import dec.external.datasource.sql.dom.QueryInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Map;

public abstract class AbstractCmdConnection extends AbstractConnection<ExecuteParam, ExecuteInfo> implements SqlDBConnection<ExecuteParam, ExecuteInfo> {

    public void connect() throws ConectionException {
        try {
            con = ConnectionFactory.getInstance().getConnection(conName);
            this.setAutoCommit(false);
        } catch (Exception e) {
            throw new ConectionException(e);
        }
    }

    public void commit() throws ConectionException {
        try {
            if (check())
                ((Connection) con).commit();
        } catch (SQLException e) {
            throw new ConectionException(e);
        } finally {
            if (con != null) {
                try {
                    ((Connection) con).close();
                } catch (SQLException e) {

                }
            }
        }
    }

    public void close() throws ConectionException {
        try {
            if (check())
                ((Connection) con).close();
        } catch (SQLException e) {
            throw new ConectionException(e);
        }
    }

    public void rollback() throws ConectionException {
        try {
            if (check())
                ((Connection) con).rollback();
        } catch (SQLException e) {
            throw new ConectionException(e);
        }
    }

    public Connection getConnection() {
        return (Connection) con;
    }

    private boolean check() throws SQLException {
        return con != null && !((Connection) con).isClosed();
    }

    public void setAutoCommit(boolean isAuto) throws ConectionException {
        isAutoCommit = isAuto;
        try {
            if (check())
                ((Connection) con).setAutoCommit(isAuto);
        } catch (SQLException e) {
            throw new ConectionException(e);
        }
    }

    public boolean isAutoCommit() {
        return isAutoCommit;
    }

    public Savepoint getSavepoint(String name) throws SQLException {
        return ((Connection) con).setSavepoint(name);
    }

    public Savepoint getSavepoint() throws SQLException {
        return ((Connection) con).setSavepoint();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        if (check())
            ((Connection) con).rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        if (check())
            ((Connection) con).releaseSavepoint(savepoint);
    }

    public boolean isClosed() throws ConectionException {

        try {
            return ((Connection) con).isClosed();
        } catch (SQLException e) {
            throw new ConectionException(e);
        }
    }

    public ExecuteInfo execute(ExecuteParam param) throws ExecuteException {
        Map<String, String> proMap = ConfigContextUtil.getConfigInfo().getConnection(conName).getPropertyInfo();

        ConvertParam convertParam = new ConvertParam();
        convertParam.setData(param.getValue());
        convertParam.setSql(param.getCmd());
        convertParam.setType(param.getType());
        convertParam.setOrign(param.getIsOrign());
        convertParam.setDataSource(dataSource);
        convertParam.setConParamMap(proMap);

        ConvertInfo convertInfo = (ConvertInfo) sqlConvertContainer.convert(convertParam);

        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setSql(convertInfo.getCmd());
        queryInfo.setType(param.getType());
        queryInfo.setDataInfoCollection(convertInfo.getDataInfo());
        queryInfo.setCon(con);
        queryInfo.setKeyType(convertInfo.getKeyType());
        queryInfo.setKeyValue(convertInfo.getKeyValue());


        queryInfo.setConParamMap(proMap);

        ExecuteInfo executeInfo = (ExecuteInfo) sqlExecuteContainer.execute(queryInfo);

        return executeInfo;

    }

    //protected boolean isNeedConvert() {
    //	return false;
    //}
}
