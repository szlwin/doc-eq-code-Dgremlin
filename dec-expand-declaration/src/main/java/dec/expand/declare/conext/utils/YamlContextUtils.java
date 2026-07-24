package dec.expand.declare.conext.utils;

import dec.expand.declare.conext.parser.yaml.exception.YAMLParseException;
import dec.expand.declare.conext.parser.yaml.parser.ContextDescYamlParser;

public class YamlContextUtils {

    public static void loadConfig(String filePathArray[]) throws YAMLParseException {
        ContextDescYamlParser parser = new ContextDescYamlParser();
        for (String filePath : filePathArray) {
            parser.parser(filePath);
        }
    }
}
