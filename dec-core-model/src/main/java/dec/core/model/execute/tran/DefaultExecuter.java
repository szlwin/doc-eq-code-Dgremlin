package dec.core.model.execute.tran;

import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.execute.rule.RuleContainer;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Savepoint;

public class DefaultExecuter implements TranExecuter {

    private final static Logger log = LoggerFactory.getLogger(DefaultExecuter.class);

    protected ModelLoader modelLoader;

    protected DataConnection con;

    protected String name;

    protected Savepoint savepoint;

    protected int tranType;

    protected long flag;

    public DefaultExecuter(long paramFalg) {
        this.flag = paramFalg;
    }

    public void load(ModelLoader modelLoader) {
        this.modelLoader = modelLoader;

    }

    public ResultInfo execute(String startRule, String endRule) throws ExecuteRuleException, ExecuteException {

        log.info("Execute the tran: id = " + flag + ", view rule: " + modelLoader.getRuleName() + " start!");

        RuleContainer ruleExecute = new RuleContainer(modelLoader, con);

        ruleExecute.setStartRule(startRule);
        ruleExecute.setEndRule(endRule);
        ResultInfo resultInfo = ruleExecute.execute();

        log.info("Execute the tran: id = " + flag + ", view rule: " + modelLoader.getRuleName() + " end!");

        return resultInfo;
    }

    public ResultInfo execute() throws ExecuteRuleException, ExecuteException {
        return execute(null, null);
    }

    public void setConnection(DataConnection con) {
        this.con = con;

    }

    public DataConnection getConnection() {
        return con;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Savepoint getSavepoint() {
        return savepoint;
    }

    public void setSavepoint(Savepoint savepoint) {
        this.savepoint = savepoint;
    }

    public int getTranType() {
        return tranType;
    }

    public void setTranType(int tranType) {
        this.tranType = tranType;
    }

    public String getConName() {
        return modelLoader.getConName();
    }

    public long getFlag() {
        return flag;
    }


}
