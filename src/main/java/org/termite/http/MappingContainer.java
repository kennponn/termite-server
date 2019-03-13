package org.termite.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MappingContainer {
	private static Map<String,Method> mappings = new HashMap<>();
	
	public static boolean exists(String url) {
		return mappings.get(url)==null?false:true;
	}
}
