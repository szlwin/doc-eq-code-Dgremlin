package dec.context.parse.yaml.parse;

import dec.context.parse.yaml.exception.YAMLParseException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class YamlSupport {

    private YamlSupport() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadMap(YamlResource resource) throws YAMLParseException {
        try (InputStream inputStream = open(resource)) {
            Object loaded = new Yaml().load(inputStream);
            if (loaded == null) {
                return Collections.emptyMap();
            }
            if (!(loaded instanceof Map)) {
                throw new YAMLParseException("The yaml root must be a map: " + resource.getPath());
            }
            return (Map<String, Object>) loaded;
        } catch (YAMLParseException e) {
            throw e;
        } catch (Exception e) {
            throw new YAMLParseException("Parse yaml error: " + resource.getPath(), e);
        }
    }

    public static YamlResource findOne(String filePath) throws YAMLParseException {
        if (filePath.startsWith("classpath:")) {
            String rootFilePath = filePath.substring("classpath:".length());
            URL url = YamlSupport.class.getClassLoader().getResource(rootFilePath);
            if (url == null) {
                throw new YAMLParseException("Classpath yaml is not found: " + filePath);
            }
            return new YamlResource(url);
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new YAMLParseException("Yaml file is not found: " + filePath);
        }
        return new YamlResource(file);
    }

    public static List<YamlResource> findAll(String filePath) throws YAMLParseException {
        if (filePath.startsWith("classpath:")) {
            YamlResource resource = findOne(filePath);
            File file = new File(resource.getUrl().getPath());
            if (file.exists()) {
                return findAllInFile(file);
            }
            List<YamlResource> resources = new ArrayList<>();
            resources.add(resource);
            return resources;
        }
        return findAllInFile(new File(filePath));
    }

    public static List<YamlResource> findAllInFile(File file) throws YAMLParseException {
        if (!file.exists()) {
            throw new YAMLParseException("Yaml path is not found: " + file.getPath());
        }
        List<YamlResource> resources = new ArrayList<>();
        collect(file, resources);
        return resources;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> map(Object value, String fieldName) throws YAMLParseException {
        if (value == null) {
            return null;
        }
        if (!(value instanceof Map)) {
            throw new YAMLParseException("The field must be a map: " + fieldName);
        }
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> list(Object value, String fieldName) throws YAMLParseException {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List) {
            return (List<Object>) value;
        }
        List<Object> list = new ArrayList<>();
        list.add(value);
        return list;
    }

    public static Object first(Map<String, Object> map, String... names) {
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

    public static String str(Map<String, Object> map, String... names) {
        Object value = first(map, names);
        return value == null ? null : String.valueOf(value);
    }

    public static String requireStr(Map<String, Object> map, String fieldName, String... aliases) throws YAMLParseException {
        String value = str(map, aliases);
        if (value == null || "".equals(value)) {
            throw new YAMLParseException("The field is required: " + fieldName);
        }
        return value;
    }

    private static InputStream open(YamlResource resource) throws Exception {
        if (resource.getFile() != null) {
            return new FileInputStream(resource.getFile());
        }
        return resource.getUrl().openStream();
    }

    private static void collect(File file, List<YamlResource> resources) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File child : files) {
                collect(child, resources);
            }
            return;
        }
        String name = file.getName();
        if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            resources.add(new YamlResource(file));
        }
    }
}
