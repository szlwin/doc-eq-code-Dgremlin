package test.common;
import java.io.File;
import java.util.Iterator;


import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;



public class TestXML {
	
	public static void main(String[] args) throws Exception {
		
		Document document = null; 
		SAXReader saxReader = new SAXReader(); 
        document = saxReader.read(new File("service-view/orm-view1.xml")); 

        Element element = document.getRootElement();
        
        element = element.element("view");
        System.out.println(element.elementText("tt"));
        element = element.element("property-info");
        
        Iterator it = element.elementIterator("property");
        
        while(it.hasNext()){
        	Element element1 = (Element) it.next();
        	String name = element1.attributeValue("name");
        	System.out.println(element1.attributeValue("name"));
        	
        	if(name.equals("user")){
        		 Iterator it1 = element1.elementIterator("property");
        		 
        		 while(it1.hasNext()){
        	        	Element element2 = (Element) it1.next();
        	        	String name1 = element2.attributeValue("name");
        	        	System.out.println(name1);
        		 }
        	}
        }
        
        
        /*
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse("service-view/orm-view1.xml");
		NodeList list = doc.getElementsByTagName("view");
		Element element = (Element)list.item(0);
		list = element.getElementsByTagName("property-info");
		
		element = (Element)list.item(0);
		
		list = element.getElementsByTagName("property");
		System.out.println(list.getLength());*/
	}
}
