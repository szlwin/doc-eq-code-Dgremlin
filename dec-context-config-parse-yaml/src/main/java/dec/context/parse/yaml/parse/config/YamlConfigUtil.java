package dec.context.parse.yaml.parse.config;

import dec.context.parse.yaml.exception.YAMLParseException;
import dec.core.context.config.model.config.ConfigInfo;

public final class YamlConfigUtil {

    private YamlConfigUtil() {
    }

    public static ConfigInfo parseConfigInfo(String filePath) throws YAMLParseException {
        return new YamlConfigFileParser().parse(filePath);
    }
}
