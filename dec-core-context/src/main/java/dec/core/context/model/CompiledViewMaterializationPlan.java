package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 单个 View 的编译期物化计划；字段列表在 Context 发布前一次性冻结。 */
public final class CompiledViewMaterializationPlan implements Comparable<CompiledViewMaterializationPlan> {
    private final ViewKey viewKey;
    private final List<CompiledMaterializationNode> fields;
    private CompiledViewMaterializationPlan(ViewKey viewKey,List<CompiledMaterializationNode> fields) {
        this.viewKey=Objects.requireNonNull(viewKey,"viewKey"); Objects.requireNonNull(fields,"fields");
        List<CompiledMaterializationNode> copy=new ArrayList<CompiledMaterializationNode>(fields.size());
        for(CompiledMaterializationNode field:fields) copy.add(Objects.requireNonNull(field,"fields contains null"));
        Collections.sort(copy); this.fields=Collections.unmodifiableList(copy);
    }
    public static CompiledViewMaterializationPlan of(ViewKey viewKey,List<CompiledMaterializationNode> fields) { return new CompiledViewMaterializationPlan(viewKey,fields); }
    public ViewKey viewKey(){return viewKey;} public List<CompiledMaterializationNode> fields(){return fields;}
    @Override
    public int compareTo(CompiledViewMaterializationPlan other){return viewKey.compareTo(Objects.requireNonNull(other,"other").viewKey);}
    @Override
    public boolean equals(Object other){if(this==other)return true;if(!(other instanceof CompiledViewMaterializationPlan))return false;CompiledViewMaterializationPlan that=(CompiledViewMaterializationPlan)other;return viewKey.equals(that.viewKey)&&fields.equals(that.fields);}
    @Override
    public int hashCode(){return Objects.hash(viewKey,fields);}
    @Override
    public String toString(){return viewKey+"="+fields;}
}
