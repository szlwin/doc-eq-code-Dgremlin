package dec.core.model.execute.tran;

import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.execute.rule.exception.ExecuteRuleException;

import java.sql.Savepoint;

public interface TranExecuter {

	void load(ModelLoader modelLoader);

	ResultInfo execute() throws ExecuteRuleException, ExecuteException;

	ResultInfo execute(String startRule, String endRule) throws ExecuteRuleException, ExecuteException;

	void setConnection(DataConnection con);

	DataConnection getConnection();

	void setSavepoint(Savepoint savepoint);

    Savepoint getSavepoint();

    void setTranType(int type);

    int getTranType();

	void setName(String name);

	String getName();

	String getConName();
}
