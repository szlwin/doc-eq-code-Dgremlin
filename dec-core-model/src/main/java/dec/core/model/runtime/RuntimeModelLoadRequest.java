package dec.core.model.runtime;

import dec.core.context.model.RuntimeBindingPlan;
import java.util.Objects;

/** MODEL production load 的输入 DTO；持有请求事实但不携带任何授权能力。 */
public final class RuntimeModelLoadRequest {
    private final RuntimeBindingPlan runtimeBindingPlan;
    private final Object originObject;
    private final String ruleName;
    private final String connectionName;

    private RuntimeModelLoadRequest(
            RuntimeBindingPlan runtimeBindingPlan,
            Object originObject,
            String ruleName,
            String connectionName) {
        this.runtimeBindingPlan = Objects.requireNonNull(runtimeBindingPlan, "runtimeBindingPlan");
        this.originObject = Objects.requireNonNull(originObject, "originObject");
        this.ruleName = requireText(ruleName, "ruleName");
        this.connectionName = requireText(connectionName, "connectionName");
    }

    /** 创建精确 plan + 真实 origin 的加载请求；请求本身不是 authority token。 */
    public static RuntimeModelLoadRequest of(
            RuntimeBindingPlan plan,
            Object originObject,
            String ruleName,
            String connectionName) {
        return new RuntimeModelLoadRequest(plan, originObject, ruleName, connectionName);
    }

    public RuntimeBindingPlan runtimeBindingPlan() { return runtimeBindingPlan; }
    public Object originObject() { return originObject; }
    public String ruleName() { return ruleName; }
    public String connectionName() { return connectionName; }

    /** 禁止运行时 trim/补全生产 rule/connection 名称，避免请求与真实加载事实分离。 */
    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isEmpty() || !value.equals(value.trim())) {
            throw new IllegalArgumentException(name + " must be non-blank and trimmed");
        }
        return value;
    }
}
