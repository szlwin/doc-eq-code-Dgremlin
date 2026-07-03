package dec.context.parse.yaml.parse;

import dec.context.parse.yaml.exception.YAMLParseException;

public interface FileParser<E> {

    E parse(String filePath) throws YAMLParseException;
}
