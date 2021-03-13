package ch.wenkst.sw_utils.map;

import java.util.HashMap;
import java.util.Map;

public class TestMaps {
	public static Map<String, Object> numberMap() {
		Map<String, Object> numTestMap = new HashMap<>();
		numTestMap.put("intVal", 2);
		numTestMap.put("floatVal", 4.11f);
		numTestMap.put("doubleVal", 6.78d);
		
		numTestMap.put("strValInt", "11");
		numTestMap.put("strValDouble", "13.789");
		
		numTestMap.put("mapVal", new HashMap<String, String>());
		return numTestMap;
	}
	
	
	public static Map<String, Object> booleanMap() {
		Map<String, Object> booleanMap = new HashMap<>();
		booleanMap.put("intVal1", 1);
		booleanMap.put("intVal2", 0);
		booleanMap.put("doubleVal1", 33.3);
		booleanMap.put("doubleVal2", -33.3);
		
		booleanMap.put("strVal1", "false");
		booleanMap.put("strVal2", "False");
		
		booleanMap.put("booleanVal", true);
		
		booleanMap.put("mapVal", new HashMap<String, String>());
		return booleanMap;
	}
	
	
	public static Map<String, Object> stringMap() {
		HashMap<String, Object> stringMap = new HashMap<>();
		stringMap.put("strVal", "normal string");
		
		stringMap.put("intVal1", -67);
		stringMap.put("intVal2", 3);
		stringMap.put("longVal", 23456l);
		stringMap.put("floatVal", 43.11f);
		stringMap.put("doubleVal", 81.333d);
		stringMap.put("booleanVal", true);
		
		stringMap.put("mapVal", new HashMap<String, String>());
		return stringMap;
	}
}
