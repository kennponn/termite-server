package org.termite.http;

import java.util.HashMap;
import java.util.Map;

public class Request {
	private Map<String,String> headers = new HashMap<>();
	
	public Request(String httpStr) {
		// TODO Auto-generated constructor stub
		headers.put("method", httpStr.substring(0,httpStr.indexOf('/')));
		headers.put("protocol", httpStr.substring(httpStr.indexOf("HTTP"), httpStr.indexOf("Host")));
		
		String [] headersData = httpStr.split("\n");
		for(String tmp: headersData) {
			if(tmp.indexOf("HTTP")!=-1) {
				
			}
		}
		
		System.out.println(headers.get("User-Agent"));
	}
	
	public String getHeader(String key) {
		return headers.get(key);
	}
}
