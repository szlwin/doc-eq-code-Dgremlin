package dec.expand.declare.conext.parser.yaml.parser;

import dec.expand.declare.conext.desc.system.SystemDesc;
import dec.expand.declare.conext.parser.yaml.exception.YAMLParseException;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class YamlSupport {

    private YamlSupport() {
    }

    static Map<String, Object> loadMap(String filePath) throws YAMLParseException {
        try {
            InputStream inputStream;
            if (filePath.startsWith("classpath:")) {
                String resourcePath = filePath.substring("classpath:".length());
                inputStream = SystemDesc.class.getClassLoader().getResourceAsStream(resourcePath);
                if (inputStream == null) {
                    throw new YAMLParseException("Classpath yaml is not found: " + filePath);
                }
            } else {
                inputStream = new FileInputStream(filePath);
            }
            Object value = new Yaml().load(inputStream);
            return map(value, filePath);
        } catch (YAMLParseException e) {
            throw e;
        } catch (Exception e) {
            throw new YAMLParseException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Object value, String name) throws YAMLParseException {
        if (value == null) {
            return null;
        }
        if (!(value instanceof Map)) {
            throw new YAMLParseException("The yaml node must be map: " + name);
        }
        return (Map<String, Object>) value;
    }

    static List<Object> list(Object value, String name) throws YAMLParseException {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List) {
            return (List<Object>) value;
        }
        List<Object> list = new ArrayList<Object>();
        list.add(value);
        return list;
    }

    static Object first(Map<String, Object> map, String... names) {
        if (map == null) {
            return null;
        }
        for (String name : names) {
            if (map.containsKey(name)) {
                return map.get(name);
            }
        }
        return null;
    }

    static String str(Map<String, Object> map, String... names) {
        Object value = first(map, names);
        return value == null ? null : String.valueOf(value);
    }

    static String requireStr(Map<String, Object> map, String node, String... names) throws YAMLParseException {
        String value = str(map, names);
        if (value == null || "".equals(value)) {
            throw new YAMLParseException("The property is required: " + node);
        }
        return value;
    }
}
