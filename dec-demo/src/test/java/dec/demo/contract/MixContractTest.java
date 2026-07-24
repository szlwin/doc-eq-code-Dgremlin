package dec.demo.contract;

import org.junit.jupiter.api.Test;
import org.w3c.dom.*; import javax.xml.parsers.*; import java.io.*; import java.net.*; import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MixContractTest {
    private static final List<String> ROOTS=Arrays.asList(
        "mix/orm-config.xml", "mix/data/User.xml", "mix/data/Order.xml", "mix/data/Pay.xml",
        "mix/view/orm-view.xml", "mix/system/systems.xml", "mix/rule/user-rule.xml",
        "mix/rule/order-rule.xml", "mix/rule/payment-rule.xml", "mix/business/order-business.xml");
    @Test void mixFilesAreWellFormedAndReferencesExist() throws Exception {
        Set<String> visited=new LinkedHashSet<>(); for(String root:ROOTS) validate(root,visited);
        assertTrue(visited.size()>=10, "Expected the complete mix document set");
    }
    private void validate(String path, Set<String> visited) throws Exception {
        if(!visited.add(path)) return;
        URL url=getClass().getClassLoader().getResource(path); assertNotNull(url,"Missing mix resource: "+path);
        DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true); f.setExpandEntityReferences(false);
        Document doc; try(InputStream in=url.openStream()){ doc=f.newDocumentBuilder().parse(in); }
        NodeList all=doc.getElementsByTagName("*");
        for(int i=0;i<all.getLength();i++){
            Element e=(Element)all.item(i); String ref=e.getAttribute("path");
            if(ref.startsWith("classpath:")) {
                String target=ref.substring("classpath:".length());
                if(target.endsWith("/")) assertNotNull(getClass().getClassLoader().getResource(target),"Missing directory: "+target);
                else validate(target,visited);
            }
        }
    }
}
