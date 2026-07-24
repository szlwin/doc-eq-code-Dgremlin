package dec.demo.contract;

import org.junit.jupiter.api.Test;
import java.io.*; import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LegacyResourceSnapshotTest {
    @Test void legacyXmlYamlResourcesMatchFrozenDigests() throws Exception {
        Properties expected=new Properties();
        try(InputStream in=getClass().getResourceAsStream("/contract/legacy-resource-sha256.properties")){
            assertNotNull(in); expected.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        for(String path: expected.stringPropertyNames()) {
            try(InputStream in=getClass().getResourceAsStream("/"+path)) {
                assertNotNull(in, "Missing legacy fixture: "+path);
                assertEquals(expected.getProperty(path), sha256(in), "Legacy fixture changed: "+path);
            }
        }
    }
    private static String sha256(InputStream in) throws Exception {
        MessageDigest d=MessageDigest.getInstance("SHA-256"); byte[] b=new byte[8192]; int n;
        while((n=in.read(b))>=0) d.update(b,0,n);
        StringBuilder s=new StringBuilder(); for(byte x:d.digest()) s.append(String.format("%02x",x)); return s.toString();
    }
}
