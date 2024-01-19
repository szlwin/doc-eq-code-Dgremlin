package dec.expand.declare.service;

public class ExecuteResult {

    private Error error;

    private String declareName;

    private String processName;

    private String dataType;

    private Object data;

    private boolean isStop;

    public static ExecuteResult success() {
        return new ExecuteResult();
    }

    public static ExecuteResult success(String dataType, Object data) {

        ExecuteResult executeResult = new ExecuteResult();

        executeResult.setDataType(dataType);

        executeResult.setData(data);

        return executeResult;
    }

    public static ExecuteResult success(Object data) {

        ExecuteResult executeResult = new ExecuteResult();

        executeResult.setData(data);

        return executeResult;
    }

    public static ExecuteResult fail(String code, String message) {
        return fail(code, message, null);
    }

    public static ExecuteResult fail() {
        return fail("", "");
    }

    public static ExecuteResult fail(String code, String message, Exception ex) {

        ExecuteResult executeResult = new ExecuteResult();
        Error error = new Error();
        error.setCode(code);
        error.setMessage(message);
        error.setException(ex);
        executeResult.setError(error);
        executeResult.getError().setException(ex);

        return executeResult;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public boolean isException() {
        return this.error != null
                && this.error.getException() != null;
    }

    public String getDeclareName() {
        return declareName;
    }

    public void setDeclareName(String declareName) {
        this.declareName = declareName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean isStop) {
        this.isStop = isStop;
    }

}
