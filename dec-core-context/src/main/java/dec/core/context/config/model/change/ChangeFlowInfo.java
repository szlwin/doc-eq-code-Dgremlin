package dec.core.context.config.model.change;

import dec.core.context.config.model.config.data.ConfigBaseData;
import java.util.List;

public class ChangeFlowInfo extends ConfigBaseData {

    private String viewRef;

    private List<String> startDirectory;

    private List<String> endDirectory;

    public String getViewRef() {
        return viewRef;
    }

    public void setViewRef(String viewRef) {
        this.viewRef = viewRef;
    }
}
