package dec.context.parse.xml.parse;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.context.parse.xml.exception.XMLParseException;
import dec.core.context.config.model.config.data.ConfigBaseData;

//import com.orm.common.xml.exception.XMLParseException;
//import com.orm.common.xml.model.config.ConfigBaseData;

public abstract class AbstractFileParser implements FileParser<List<ConfigBaseData>>{

	private final static Logger log = LoggerFactory.getLogger(AbstractFileParser.class);
	
	protected AbstarctElementsParser elementsParser;
	
	protected String nodeName;
	
	boolean isClassPath = false;
	
	public List<ConfigBaseData> parse(String filePath) throws XMLParseException {
		log.info("Start parse filePath:"+filePath);
		
		
		elementsParser = getParser();
		
		nodeName = getNodeNme();
		
		//List<ConfigBaseData> list = null;
		/*try{
			if(filePath.endsWith(".xml"))
				list = parseByFile(filePath);
			else{
				
				list = parseByDirectory(filePath);
			}
				
		}catch(Exception e)
		{
			throw new XMLParseException(e);
		}*/
		List<Resource> fileUrls = findAllFileResources(filePath);
		
		
		try {
			return this.parseByFile(fileUrls);
		} catch (Exception e) {
			throw new XMLParseException(e);
		}
	}

	protected List<Resource> findAllFileResources(String filePath) throws XMLParseException{
		
		if(filePath.startsWith("classpath:")){

			String rootFilePath = filePath.substring("classpath:".length());
			
			URL url 											
				= AbstractFileParser.class
					.getClassLoader().getResource(rootFilePath);
			
			if("jar".equals(url.getProtocol()) || "war".equals(url.getProtocol())){
				
				isClassPath = true;
				
			}else{
				filePath = url.getPath();
			}
			
		}
		
		if(isClassPath){
			try {
				return dofindJarResources(filePath);
			} catch (Exception e) {
				throw new XMLParseException(e);
			} 
		}else{
			try {
				return dofindSystemFileResources(new File(filePath));
			} catch (Exception e) {
				throw new XMLParseException(e);
			}
		}

	}
	
	
	
	protected List<Resource> dofindJarResources(String filePath) throws URISyntaxException, IOException{
	  
	  filePath = filePath.substring("classpath:".length());
		
	  List<Resource> list = new ArrayList<>();
	  
	  URL url 											
		 = AbstractFileParser.class
		  .getClassLoader().getResource(filePath);
	   
	   JarFile jarFile;
	   String jarFileUrl;
	   String rootEntryPath;
	   URLConnection con = url.openConnection();
	   
	   if (con instanceof JarURLConnection) {
			JarURLConnection result = (JarURLConnection) con;

			jarFile = result.getJarFile();
			jarFileUrl = result.getJarFileURL().toExternalForm();
			JarEntry entries = result.getJarEntry();
			rootEntryPath = entries != null ? entries.getName() : "";

		}else{

			String result1 = url.getFile();

			int entries1 = result1.indexOf("*/");
			if (entries1 == -1) {
				entries1 = result1.indexOf("!/");
			}

			if (entries1 != -1) {
				jarFileUrl = result1.substring(0, entries1);
				rootEntryPath = result1.substring(entries1 + 2);
				jarFile = new JarFile(jarFileUrl);
			} else {
				jarFile = new JarFile(result1);
				jarFileUrl = result1;
				rootEntryPath = "";
			}
		}
		
		Enumeration<JarEntry> enumeration = jarFile.entries();
		
		while (enumeration.hasMoreElements())  {
			
			JarEntry jarEntry = enumeration.nextElement();
			
			
			if(jarEntry.getName().endsWith(".xml")){
				
				String entryPath = jarEntry.getName();
				
				if (entryPath.startsWith(rootEntryPath)) {
					
					list.add(new Resource(AbstractFileParser.class
							  .getClassLoader().getResource(jarEntry.getName())));
					
				}
				
			}
		}
   
	
		return list;
	}
	
	protected List<Resource> dofindSystemFileResources(File rootFile) throws URISyntaxException{
		
		List<Resource> list = new ArrayList<>();
		
		if(rootFile.isDirectory()){
			
			File[] files = rootFile.listFiles();
			
			for(File file : files){
				list.addAll(dofindSystemFileResources(file));
			}
		}else{
			if(rootFile.getName().endsWith(".xml")){
				
				list.add(new Resource(rootFile));
			}
			
		}
		
		return list;
	}
	
	
	
	
	@SuppressWarnings("rawtypes")
	protected List<ConfigBaseData> parseByFile(List<Resource> resources) throws Exception
	{
		List<ConfigBaseData> list = new ArrayList<ConfigBaseData>();
		for(Resource resource:resources){
			
			log.info("Load the file:"+resource.getURL().getPath()+" Start!");
			
			
			SAXReader saxReader = new SAXReader();
			Document doc = saxReader.read(resource.getURL());
			
			Iterator it = doc.getRootElement().elementIterator(nodeName);
			
			while(it.hasNext()){
				ConfigBaseData data = parseElements((Element)it.next());
				
				list.add(data);
			}
			log.info("Load the file:"+resource.getURL().getPath()+" End!");
			
		}
		
		/*if(isClassPath){
			
			InputStream fileStream 											
				= AbstractFileParser.class
				.getClassLoader()
				.getResourceAsStream(filePath);
			 
			 doc = saxReader.read(fileStream); 
		}else{
			 doc = saxReader.read(new File(filePath)); 
		}*/
		return list;

		
	}
	
	/**
	protected List<ConfigBaseData> parseByDirectory(String filePath) throws Exception
	{
		List<ConfigBaseData> dataList = new ArrayList<ConfigBaseData>();
		
		if(isClassPath){
			URL url 											
				= AbstractFileParser.class
				.getClassLoader().getResource(filePath);
			
			
			URI uri = new URI(filePath.replace(" ", "%20"));



			File file =  new File(uri.getSchemeSpecificPart());
			
			SAXReader saxReader = new SAXReader();
			Document doc = saxReader.read(file); 
			
			/*filePath = url.getFile();
			
			JarFile jf =  new JarFile(filePath);
			
			Enumeration<JarEntry> enumeration = jf.entries();
			
			while (enumeration.hasMoreElements())  {
				
				JarEntry jarEntry = enumeration.nextElement();
				
				if(jarEntry.getName().endsWith(".xml")){
						
					InputStream fileStream 	= jf.getInputStream(jarEntry);
					 
						
					SAXReader saxReader = new SAXReader();
					Document doc = null;
						
					doc = saxReader.read(fileStream); 
				
			
					Iterator it = doc.getRootElement().elementIterator(nodeName);
				
					while(it.hasNext()){
						ConfigBaseData data = parseElements((Element)it.next());
						
						dataList.add(data);
					}
				}
			}*/

			/**isClassPath = false;
		}/*else{
			File file =  new File(filePath);
			if(file.isDirectory())
			{
				File fileArray[] = file.listFiles();
				
				for(int i = 0; i < fileArray.length; i++)
				{
					String tempFilePath = fileArray[i].getCanonicalPath();
					if(tempFilePath.endsWith(".xml"))
					{
						List<ConfigBaseData> list = parseByFile(tempFilePath);
						dataList.addAll(list);
					}
				}
				
			}else
			{
				throw new XMLParseException("The file Directory: "+filePath+" is error!");
			}
		}*/
		

		
		
		//return dataList;
		
	//}
	
	protected ConfigBaseData parseElements(Element element) throws XMLParseException
	{
		return elementsParser.parse(element);
	}
	
	protected abstract AbstarctElementsParser getParser();
	
	protected abstract String getNodeNme();
}
