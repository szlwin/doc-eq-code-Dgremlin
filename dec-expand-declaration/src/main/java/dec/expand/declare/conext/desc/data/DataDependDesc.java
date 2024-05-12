package dec.expand.declare.conext.desc.data;

import dec.expand.declare.collections.SimpleList;

public class DataDependDesc {

    private final String data;

    private Integer type;

    private SimpleList<ValueDesc> param;

    private SimpleList<ValueDesc> init;

    private String condition;

    private SimpleList<ValueDesc> change;

    private String paramExpress;

    private String initExpress;

    public DataDependDesc(String data) {
        this.data = data;

        init();
    }

    public String getData() {
        return data;
    }


    public Integer getType() {
        return type;
    }

    public SimpleList<ValueDesc> getParam() {
        return param;
    }

    public void setParam(String param) {
        this.paramExpress = param;
        if (this.param == null) {
            this.param = new SimpleList<>();
        }
        String[] statusArray = param.split(",");
        for (String status : statusArray) {
            ValueDesc statusDesc = new ValueDesc();
            String[] statusStr = status.split(":");
            if (statusStr[1].indexOf(".") > 1) {
                statusDesc.setProperty(statusStr[1].split("\\."));
            } else {
                statusDesc.setProperty(new String[]{statusStr[1]});
            }
            statusDesc.setValue(statusStr[0]);
            statusDesc.setExpress(status);
            this.param.add(statusDesc);
        }
    }

    public void setInit(String init) {
        this.initExpress = init;
        if (this.init == null) {
            this.init = new SimpleList<>();
        }
        String[] statusArray = init.split(",");
        for (String status : statusArray) {
            ValueDesc statusDesc = new ValueDesc();
            String[] statusStr = status.split(":");
            if (statusStr[0].indexOf(".") > 0) {
                statusDesc.setProperty(statusStr[0].split("\\."));
            } else {
                statusDesc.setProperty(new String[]{statusStr[0]});
            }

            if (statusStr[1].matches("\\d+")) {
                statusDesc.setValue(Integer.valueOf(statusStr[1]));
            } else {
                statusDesc.setValue(statusStr[1]);
            }

            this.init.add(statusDesc);
        }
    }

    public SimpleList<ValueDesc> getInit() {
        return init;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public SimpleList<ValueDesc> getChange() {
        return change;
    }

    public String getParamExpress() {
        return paramExpress;
    }

    public String getInitExpress() {
        return initExpress;
    }

    public void setChange(String change) {
        if (this.change == null) {
            this.change = new SimpleList<>();
        }
        String[] statusArray = change.split(",");
        for (String status : statusArray) {
            ValueDesc statusDesc = new ValueDesc();
            String[] statusStr = status.split(":");
            if (statusStr[0].indexOf(".") > 0) {
                statusDesc.setProperty(statusStr[0].split("\\."));
            } else {
                statusDesc.setProperty(new String[]{statusStr[0]});
            }
            if (statusStr[1].matches("\\d+")) {
                statusDesc.setValue(Integer.valueOf(statusStr[1]));
            } else {
                statusDesc.setValue(statusStr[1]);
            }
            this.change.add(statusDesc);
        }
    }

    private void init() {
        if (data.startsWith("$")) {
            type = 1;
        } else {
            type = 2;
        }
    }
}
