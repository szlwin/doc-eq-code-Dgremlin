package dec.context.parse.yaml.parse;

import java.io.File;
import java.net.URL;

public class YamlResource {

    private final File file;

    private final URL url;

    public YamlResource(File file) {
        this.file = file;
        this.url = null;
    }

    public YamlResource(URL url) {
        this.file = null;
        this.url = url;
    }

    public File getFile() {
        return file;
    }

    public URL getUrl() {
        return url;
    }

    public String getPath() {
        if (file != null) {
            return file.getPath();
        }
        return url == null ? "" : url.getPath();
    }
}
