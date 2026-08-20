package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** 随 CompiledModelSet 原子发布的 View 物化索引。 */
public final class CompiledViewMaterializationIndex {
    private final Map<ViewKey,CompiledViewMaterializationPlan> plans;
    private final Set<ViewKey> viewKeys;
    private CompiledViewMaterializationIndex(Collection<CompiledViewMaterializationPlan> values){Objects.requireNonNull(values,"plans");List<CompiledViewMaterializationPlan> sorted=new ArrayList<CompiledViewMaterializationPlan>(values.size());for(CompiledViewMaterializationPlan plan:values)sorted.add(Objects.requireNonNull(plan,"plans contains null"));Collections.sort(sorted);Map<ViewKey,CompiledViewMaterializationPlan> copy=new LinkedHashMap<ViewKey,CompiledViewMaterializationPlan>();for(CompiledViewMaterializationPlan plan:sorted){if(copy.put(plan.viewKey(),plan)!=null)throw new IllegalArgumentException("duplicate materialization plan: "+plan.viewKey());}this.plans=Collections.unmodifiableMap(copy);this.viewKeys=Collections.unmodifiableSet(new LinkedHashSet<ViewKey>(copy.keySet()));}
    public static CompiledViewMaterializationIndex of(Collection<CompiledViewMaterializationPlan> plans){return new CompiledViewMaterializationIndex(plans);}
    public static CompiledViewMaterializationIndex empty(){return new CompiledViewMaterializationIndex(Collections.<CompiledViewMaterializationPlan>emptyList());}
    public Optional<CompiledViewMaterializationPlan> find(ViewKey key){return Optional.ofNullable(plans.get(Objects.requireNonNull(key,"key")));}
    public Set<ViewKey> viewKeys(){return viewKeys;}
    /** 返回稳定文本，供聚合语义摘要组合使用。 */ public String canonicalForm(){return plans.toString();}
    @Override
    public boolean equals(Object other){return this==other || other instanceof CompiledViewMaterializationIndex && plans.equals(((CompiledViewMaterializationIndex)other).plans);}
    @Override
    public int hashCode(){return plans.hashCode();}
    @Override
    public String toString(){return plans.toString();}
}
