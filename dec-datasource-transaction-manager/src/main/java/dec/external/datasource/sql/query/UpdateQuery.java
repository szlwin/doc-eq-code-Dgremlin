package dec.external.datasource.sql.query;

import dec.core.datasource.dom.DataInfo;
import dec.external.datasource.sql.dom.UpdateInfo;
import dec.external.datasource.sql.utils.SQLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;


//import com.orm.sql.dom.DataInfo;
//import com.orm.sql.dom.UpdateInfo;
//import com.orm.sql.util.SQLUtil;

public class UpdateQuery extends AbstractQuery<UpdateInfo> {

    private final static Logger log = LoggerFactory.getLogger(UpdateQuery.class);

    protected PreparedStatement preStatement;

    protected UpdateInfo updateInfo;

    public UpdateQuery(Connection con, String sql, Collection<DataInfo> param) {
        super(con, sql, param);

    }

    public void executeSQL() throws SQLException {
        ResultSet rs = null;
        try {
            preStatement = createStatement();

            String paramString = SQLUtil.convert(preStatement, paramCollection, log.isDebugEnabled());

            if (log.isDebugEnabled()) {
                log.debug("Execute the SQL: {}", sqlString);

                if (paramString != null) {
                    log.debug("SQL Paramer==> {}", paramString);
                }
            }
            int updateCount = preStatement.executeUpdate();

            updateInfo = new UpdateInfo();

            updateInfo.setCount(updateCount);

            if (log.isDebugEnabled()) {
                log.debug("Row count: " + updateCount);
                //log.debug("Execute the SQL: "+sqlString+" end!");
            }
        } catch (SQLException e) {
            log.error("Execute the SQLerror: {} ", sqlString, e);
            throw e;
        } finally {
            try {
                if (rs != null)
                    rs.close();

            } catch (SQLException e) {
                log.error("Close rs error", e);
            }
        }
    }

    public UpdateInfo getResult() {
        return updateInfo;
    }

    public void close() throws SQLException {
        preStatement.close();

    }

    protected int execute() throws SQLException {
        return preStatement.executeUpdate();
    }

    protected PreparedStatement createStatement() throws SQLException {
        PreparedStatement preStatement = connection.prepareStatement(sqlString);

        return preStatement;
    }
}
