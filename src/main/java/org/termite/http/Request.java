package org.termite.http;

import java.util.HashMap;
import java.util.Map;

public class Request {
	private Map<String,String> headers = new HashMap<>();
	
	public Request(String httpStr) {
		// TODO Auto-generated constructor stub
		headers.put("method", httpStr.substring(0,httpStr.indexOf('/')));
		headers.put("protocol", httpStr.substring(httpStr.indexOf("HTTP"), httpStr.indexOf("Host")).trim());
		String [] headersData = httpStr.split("\n");
		headers.put("url", httpStr.substring(httpStr.indexOf('/'), httpStr.indexOf("HTTP")));
		for(String tmp: headersData) {
			if(tmp.indexOf("HTTP")==-1 && tmp.trim().length()!=0) {
				headers.put(tmp.substring(0,tmp.indexOf(":")), tmp.substring(tmp.indexOf(":")+1).trim());
			}
		}
		
		
		for (String key : headers.keySet()) {
			System.out.println(key+"---------"+headers.get(key));
		}
		
		
	}
	
	public String getHeader(String key) {
		return headers.get(key);
	}
}
