package dec.core.context.config.model.directory;

import dec.core.context.config.model.config.data.ConfigBaseData;

import java.util.Set;

public class SubDirectory extends ConfigBaseData {
    private String rel;

    private Set<String> mutualExclusions;

    private boolean anyOne;

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public Set<String> getMutualExclusions() {
        return mutualExclusions;
    }

    public void setMutualExclusions(Set<String> mutualExclusions) {
        this.mutualExclusions = mutualExclusions;
    }

    public boolean isAnyOne() {
        return anyOne;
    }

    public void setAnyOne(boolean anyOne) {
        this.anyOne = anyOne;
    }

}

