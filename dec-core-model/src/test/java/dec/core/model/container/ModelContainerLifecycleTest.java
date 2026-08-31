package dec.core.model.container;

import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.data.ModelData;
import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.connection.exception.ConectionException;
import dec.core.datasource.convert.container.ConvertContainer;
import dec.core.datasource.execute.container.ExecuteContainer;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.execute.rule.RuleContainer;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import dec.core.model.container.listener.ContainerListener;
import dec.core.model.utils.DataUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证简化业务入口的明确失败和事务收尾。
 *
 * <p>需求：FEATURE-DESC-3361AD2E54FC 的 AC-006、AC-007；
 * 测试设计：test_case.md 中 CASE-P2-R41-SIMPLE-EXECUTE-004。</p>
 */
class ModelContainerLifecycleTest {

    @Test
    void missingViewRuleAndConnectionReportTheirNames() throws Exception {
        ConfigManager manager = ConfigManager.getInstance();
        ConfigInfo previous = manager.getInstalledConfigInfo();
        manager.setConfigInfo(new ConfigInfo());
        try {
            DataNotDefineException viewFailure = assertThrows(
                    DataNotDefineException.class,
                    () -> DataUtil.createViewData("missing-view"));
            assertTrue(viewFailure.getMessage().contains("missing-view"));

            ExecuteRuleException ruleFailure = assertThrows(
                    ExecuteRuleException.class,
                    () -> new RuleContainer(loader("missing-rule", "unused-connection"))
                            .execute());
            assertTrue(ruleFailure.getMessage().contains("missing-rule"));

            ModelContainer container = new ModelContainer();
            container.load(loader("missing-rule", "missing-connection"));
            ExecuteRuleException connectionFailure = assertThrows(
                    ExecuteRuleException.class,
                    container::execute);
            assertTrue(connectionFailure.getMessage().contains("missing-connection"));
        } finally {
            manager.setConfigInfo(previous);
        }
    }

    @Test
    void successfulExecutionCommitsAndClosesOnce() throws Exception {
        RecordingConnection connection = new RecordingConnection();
        RecordingContainer container = new RecordingContainer(
                connection,
                ResultInfo.success(),
                false);

        container.load(loader("test-rule", "con1"));
        container.execute();

        assertEquals(1, connection.commitCount);
        assertEquals(0, connection.rollbackCount);
        assertEquals(1, connection.closeCount);
    }

    @Test
    void failedRuleRollsBackAndClosesOnce() throws Exception {
        RecordingConnection connection = new RecordingConnection();
        RecordingContainer container = new RecordingContainer(
                connection,
                ResultInfo.fail("RULE_FAILED", "规则执行失败"),
                false);

        container.load(loader("test-rule", "con1"));
        container.execute();

        assertEquals(0, connection.commitCount);
        assertEquals(1, connection.rollbackCount);
        assertEquals(1, connection.closeCount);
    }

    @Test
    void executionExceptionRollsBackAndClosesOnce() {
        RecordingConnection connection = new RecordingConnection();
        RecordingContainer container = new RecordingContainer(
                connection,
                ResultInfo.success(),
                true);

        container.load(loader("test-rule", "con1"));
        assertThrows(ExecuteRuleException.class, container::execute);

        assertEquals(0, connection.commitCount);
        assertEquals(1, connection.rollbackCount);
        assertEquals(1, connection.closeCount);
    }

    @Test
    void commitExceptionStillRollsBackAndClosesOnce() {
        RecordingConnection connection = new RecordingConnection();
        connection.failCommit = true;
        RecordingContainer container = new RecordingContainer(
                connection,
                ResultInfo.success(),
                false);

        container.load(loader("test-rule", "con1"));
        ExecuteRuleException failure = assertThrows(
                ExecuteRuleException.class,
                container::execute);

        assertTrue(failure.getMessage().contains("提交失败"));
        assertEquals(1, connection.commitCount);
        assertEquals(1, connection.rollbackCount);
        assertEquals(1, connection.closeCount);
    }

    @Test
    void endListenerExceptionDoesNotSkipConnectionClose() {
        RecordingConnection connection = new RecordingConnection();
        RecordingContainer container = new RecordingContainer(
                connection,
                ResultInfo.success(),
                false);
        container.addListener(new ContainerListener() {
            @Override
            public ResultInfo notify(
                    dec.core.model.container.listener.ContainerEvent event) {
                throw new IllegalStateException("结束监听器失败");
            }
        });

        container.load(loader("test-rule", "con1"));
        ExecuteRuleException failure = assertThrows(
                ExecuteRuleException.class,
                container::execute);

        assertTrue(failure.getMessage().contains("容器结束监听器执行失败"));
        assertEquals(1, connection.commitCount);
        assertEquals(1, connection.closeCount);
    }

    @Test
    void closeExceptionIsVisibleAfterAllConnectionsAreVisited() {
        RecordingConnection failed = new RecordingConnection();
        failed.failClose = true;
        RecordingConnection closed = new RecordingConnection();
        MultiConnectionContainer container = new MultiConnectionContainer(
                failed,
                closed,
                ResultInfo.success());

        container.load(loader("test-rule", "con1"));
        ExecuteRuleException failure = assertThrows(
                ExecuteRuleException.class,
                container::execute);

        assertTrue(failure.getMessage().contains("关闭失败"));
        assertEquals(1, failed.closeCount);
        assertEquals(1, closed.closeCount);
    }

    private static ModelLoader loader(String ruleName, String connectionName) {
        return new ModelLoader().load(ruleName, new TestModelData(), connectionName);
    }

    /** 只为测试构造最小 ModelData，不绕过生产执行逻辑。 */
    private static final class TestModelData extends ModelData {
        private static final long serialVersionUID = 1L;
    }

    /**
     * 将规则结果固定下来，使测试只观察 ModelContainer 的事务分支。
     * 连接仍交给生产 end() 执行 commit、rollback 和 close。
     */
    private static final class RecordingContainer extends ModelContainer {
        private final RecordingConnection connection;
        private final ResultInfo result;
        private final boolean failRule;

        private RecordingContainer(
                RecordingConnection connection,
                ResultInfo result,
                boolean failRule) {
            this.connection = connection;
            this.result = result;
            this.failRule = failRule;
        }

        @Override
        protected void begain() throws ConectionException {
            connection.connect();
            conMap.put("con1", connection);
        }

        @Override
        protected ResultInfo execute(ModelLoader modelLoader)
                throws ExecuteException, ExecuteRuleException {
            if (failRule) {
                throw new ExecuteRuleException("规则执行异常");
            }
            return result;
        }
    }

    /** 验证一个连接关闭失败时，容器仍继续关闭其余连接。 */
    private static final class MultiConnectionContainer extends ModelContainer {
        private final RecordingConnection first;
        private final RecordingConnection second;
        private final ResultInfo result;

        private MultiConnectionContainer(
                RecordingConnection first,
                RecordingConnection second,
                ResultInfo result) {
            this.first = first;
            this.second = second;
            this.result = result;
        }

        @Override
        protected void begain() throws ConectionException {
            first.connect();
            second.connect();
            conMap.put("con1", first);
            conMap.put("con2", second);
        }

        @Override
        protected ResultInfo execute(ModelLoader modelLoader) {
            return result;
        }
    }

    /** 记录事务调用次数，避免测试依赖真实数据库。 */
    private static final class RecordingConnection
            implements DataConnection<Object, Object> {
        private int commitCount;
        private int closeCount;
        private int rollbackCount;
        private boolean connected;
        private boolean autoCommit;
        private boolean failCommit;
        private boolean failClose;
        private String connectionName;
        private String dataSource;
        private int transactionType;
        private int flag;

        @Override
        public void connect() {
            connected = true;
        }

        @Override
        public void commit() throws ConectionException {
            commitCount++;
            if (failCommit) {
                throw new ConectionException("提交失败");
            }
        }

        @Override
        public void close() throws ConectionException {
            closeCount++;
            connected = false;
            if (failClose) {
                throw new ConectionException("关闭失败");
            }
        }

        @Override
        public Object execute(Object value) {
            return null;
        }

        @Override
        public void rollback() {
            rollbackCount++;
        }

        @Override
        public void setConvertContainer(ConvertContainer<?, ?> convertContainer) {
        }

        @Override
        public void setExecuteContainer(ExecuteContainer<?, ?> executeContainer) {
        }

        @Override
        public String getConName() {
            return connectionName;
        }

        @Override
        public String getDataSource() {
            return dataSource;
        }

        @Override
        public void setConName(String name) {
            connectionName = name;
        }

        @Override
        public void setDataSource(String value) {
            dataSource = value;
        }

        @Override
        public boolean isClosed() {
            return !connected;
        }

        @Override
        public boolean isConnect() {
            return connected;
        }

        @Override
        public void setAutoCommit(boolean value) {
            autoCommit = value;
        }

        @Override
        public boolean isAutoCommit() {
            return autoCommit;
        }

        @Override
        public void setTransactionType(int value) {
            transactionType = value;
        }

        @Override
        public int getTransactionType() {
            return transactionType;
        }

        @Override
        public void setFlag(int value) {
            flag = value;
        }

        @Override
        public int getFlag() {
            return flag;
        }
    }
}
