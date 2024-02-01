package dec.context.parse.xml.parse;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class Resource {

	private URI uri;
	
	private File file;
	
	private URL url;
	
	public Resource(File file){
		this.file = file;
	}
	
	public Resource(URL url){
		this.url = url;
	}
	
	public Resource(URI uri){
		this.uri = uri;
	}
	
	public URI getUri() {
		return uri;
	}

	public File getFile() {
		return file;

	}

	public URL getURL(){
		
		try {
			
			if(url != null){
				return url;
			}
			
			if(uri != null){
				return uri.toURL();
			}
				
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	
	
}
