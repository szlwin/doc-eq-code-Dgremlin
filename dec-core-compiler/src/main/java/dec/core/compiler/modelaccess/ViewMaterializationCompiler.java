package dec.core.compiler.modelaccess;

import dec.core.context.model.CompiledMaterializationNode;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.model.CompiledViewMaterializationIndex;
import dec.core.context.model.CompiledViewMaterializationPlan;
import dec.core.context.model.ModelAccessPolicyIndex;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.ModelPath;
import dec.core.context.model.ViewKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 从已经完成静态校验与有限 wildcard 展开的 exact policy 冻结运行期物化索引。
 *
 * <p>DEV-09：运行时禁止重新读取 declaration/ConfigContext。Materialization field 必须直接来自
 * 同一 Compilation Session 已发布的 exact ModelPath，而不能在 MODEL/STARTER 层重新解释配置文本。</p>
 */
public final class ViewMaterializationCompiler {

    /**
     * 按 RuntimeBindingPlan 的目标 View 聚合精确 ModelPath；READ/WRITE 重复路径只保留一份字段骨架。
     */
    public CompiledViewMaterializationIndex compile(ModelAccessPolicyIndex policyIndex) {
        Objects.requireNonNull(policyIndex, "policyIndex");
        Map<ViewKey, Set<ModelPath>> pathsByView =
                new TreeMap<ViewKey, Set<ModelPath>>();

        for (ModelAccessRuleKey key : policyIndex.keys()) {
            CompiledModelAccessRule rule = policyIndex.find(key).orElseThrow(
                    () -> new IllegalStateException(
                            "model access key has no compiled rule: " + key));
            ViewKey targetView = rule.runtimeBindingPlan()
                    .compiledTargetBinding()
                    .targetViewKey();
            Set<ModelPath> paths = pathsByView.get(targetView);
            if (paths == null) {
                paths = new TreeSet<ModelPath>();
                pathsByView.put(targetView, paths);
            }
            // key.path 已由 ModelPathCompiler 精确验证；这里禁止重新展开 wildcard 或猜测父路径。
            paths.add(key.path());
        }

        List<CompiledViewMaterializationPlan> plans =
                new ArrayList<CompiledViewMaterializationPlan>();
        for (Map.Entry<ViewKey, Set<ModelPath>> entry : pathsByView.entrySet()) {
            List<CompiledMaterializationNode> fields =
                    new ArrayList<CompiledMaterializationNode>();
            for (ModelPath path : entry.getValue()) {
                fields.add(CompiledMaterializationNode.of(path));
            }
            plans.add(CompiledViewMaterializationPlan.of(entry.getKey(), fields));
        }
        return CompiledViewMaterializationIndex.of(plans);
    }
}
