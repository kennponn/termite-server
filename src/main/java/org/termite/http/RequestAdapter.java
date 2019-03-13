package org.termite.http;

public class RequestAdapter {
	
	public void adapter(Request req) {
		if(MappingContainer.exists(req.getHeader("url"))) {
			
		}else {
			
		}
	}
}
