package ch.wenkst.sw_utils.map;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MapUtilsTest {
	private static HashMap<String, Object> numTestMap = null; 	// test mao to extract numbers
	
	
	/**
	 * prepares the test map to read the parameters from
	 */
	@BeforeAll
	public static void prepareTestMap() {
		numTestMap = new HashMap<>();
		numTestMap.put("intVal", 2);
		numTestMap.put("floatVal", 4.11f);
		numTestMap.put("doubleVal", 6.78d);
		
		numTestMap.put("strValInt", "11");
		numTestMap.put("strValDouble", "13.789");
		
		numTestMap.put("mapVal", new HashMap<String, String>());
	}
	
	
	/**
	 * read integers form a map
	 */
	@Test
	@DisplayName("int form map")
	public void intFromMapTest() {
		// read values that are present in the map
		Assertions.assertEquals(2, (int) MapUtils.intFromMap(numTestMap, "intVal", 3), "read int as int val");
		Assertions.assertEquals(4, (int) MapUtils.intFromMap(numTestMap, "floatVal", 3), "read float as int val");
		Assertions.assertEquals(6, (int) MapUtils.intFromMap(numTestMap, "doubleVal", 3), "read double as int val");
		Assertions.assertEquals(11, (int) MapUtils.intFromMap(numTestMap, "strValInt", 3), "read int string as int val");
		Assertions.assertEquals(13, (int) MapUtils.intFromMap(numTestMap, "strValDouble", 3), "read double string as int val");
		
		
		// read a value that is not present in the map
		Assertions.assertEquals(null, MapUtils.intFromMap(numTestMap, "absentVal", null), "read missing value as int val");
		
		
		// read a value that cannot be casted to an int
		Assertions.assertEquals(new Integer(555), MapUtils.intFromMap(numTestMap, "mapVal", 555), "read map as int val");
	}
	
	
	/**
	 * read long values form a map
	 */
	@Test
	@DisplayName("long form map")
	public void longFromMapTest() {
		// read values that are present in the map
		Assertions.assertEquals(2l, (long) MapUtils.longFromMap(numTestMap, "intVal", 3l), "read int as long val");
		Assertions.assertEquals(4l, (long) MapUtils.longFromMap(numTestMap, "floatVal", 3l), "read float as long val");
		Assertions.assertEquals(6l, (long) MapUtils.longFromMap(numTestMap, "doubleVal", 3l), "read double as long val");
		Assertions.assertEquals(11l, (long) MapUtils.longFromMap(numTestMap, "strValInt", 3l), "read int string as long val");
		Assertions.assertEquals(13l, (long) MapUtils.longFromMap(numTestMap, "strValDouble", 3l), "read double string as long val");
		
		
		// read a value that is not present in the map
		Assertions.assertEquals(null, MapUtils.intFromMap(numTestMap, "absentVal", null), "read missing value as long val");
		
		
		// read a value that cannot be casted to an int
		Assertions.assertEquals(new Long(555), MapUtils.longFromMap(numTestMap, "mapVal", 555l), "read map as long val");
	}
	
	
	/**
	 * read double values form a map
	 */
	@Test
	@DisplayName("double form map")
	public void doubleFromMapTest() {
		double tolerance = 0.001;
		
		// read values that are present in the map
		Assertions.assertEquals(2d, (double) MapUtils.doubleFromMap(numTestMap, "intVal", 3d), tolerance, "read int as double val");
		Assertions.assertEquals(4.11d, (double) MapUtils.doubleFromMap(numTestMap, "floatVal", 3d), tolerance, "read float as double val");
		Assertions.assertEquals(6.78d, (double) MapUtils.doubleFromMap(numTestMap, "doubleVal", 3d), tolerance, "read double as double val");
		Assertions.assertEquals(11d, (double) MapUtils.doubleFromMap(numTestMap, "strValInt", 3d), tolerance, "read int string as double val");
		Assertions.assertEquals(13.789d, (double) MapUtils.doubleFromMap(numTestMap, "strValDouble", 3d), tolerance, "read double string as double val");
		
		
		// read a value that is not present in the map
		Assertions.assertEquals(null, MapUtils.doubleFromMap(numTestMap, "absentVal", null), "read missing value as double val");
		
		
		// read a value that cannot be casted to an int
		Assertions.assertEquals(new Double(555), MapUtils.doubleFromMap(numTestMap, "mapVal", 555d), "read map as double val");
	}
	
	
	/**
	 * read boolean values form a map
	 */
	@Test
	@DisplayName("boolean form map")
	public void booleanFromMapTest() {
		// prepare the test map
		HashMap<String, Object> testMap = new HashMap<>();
		testMap.put("intVal1", 1);
		testMap.put("intVal2", 0);
		testMap.put("doubleVal1", 33.3);
		testMap.put("doubleVal2", -33.3);
		
		testMap.put("strVal1", "false");
		testMap.put("strVal2", "False");
		
		testMap.put("booleanVal", true);
		
		testMap.put("mapVal", new HashMap<String, String>());
		
		
		// read values that are present in the map
		Assertions.assertEquals(new Boolean(true), MapUtils.booleanFromMap(testMap, "intVal1", null), "read positive int as boolean val");
		Assertions.assertEquals(new Boolean(false), MapUtils.booleanFromMap(testMap, "intVal2", null), "read negative int as boolean val");
		Assertions.assertEquals(new Boolean(true), MapUtils.booleanFromMap(testMap, "doubleVal1", null), "read positive double as boolean val");
		Assertions.assertEquals(new Boolean(true), MapUtils.booleanFromMap(testMap, "doubleVal2", null), "read negative double as boolean val");
		Assertions.assertEquals(new Boolean(false), MapUtils.booleanFromMap(testMap, "strVal1", null), "read true string as boolean val");
		Assertions.assertEquals(new Boolean(false), MapUtils.booleanFromMap(testMap, "strVal2", null), "read True string as boolean val");
		Assertions.assertEquals(new Boolean(true), MapUtils.booleanFromMap(testMap, "booleanVal", null), "read boolean as boolean val");
		
		
		// read a value that is not present in the map
		Assertions.assertEquals(null, MapUtils.booleanFromMap(testMap, "absentVal", null), "read missing value as boolean val");
		
		
		// read a value that cannot be casted to an int
		Assertions.assertEquals(null, MapUtils.booleanFromMap(testMap, "mapVal", null), "read map as boolean val");
	}
	
	
	/**
	 * read string values form a map
	 */
	@Test
	@DisplayName("string form map")
	public void stringFromMapTest() {
		// prepare the test map
		HashMap<String, Object> testMap = new HashMap<>();
		testMap.put("strVal", "normal string");
		
		testMap.put("intVal1", -67);
		testMap.put("intVal2", 3);
		testMap.put("longVal", 23456l);
		testMap.put("floatVal", 43.11f);
		testMap.put("doubleVal", 81.333d);
		testMap.put("booleanVal", true);
		
		testMap.put("mapVal", new HashMap<String, String>());
		
		
		// read values that are present in the map
		Assertions.assertEquals("normal string", MapUtils.strFromMap(testMap, "strVal", null), "read string as string val");
		Assertions.assertEquals("-67", MapUtils.strFromMap(testMap, "intVal1", null), "read negative int as string val");
		Assertions.assertEquals("3", MapUtils.strFromMap(testMap, "intVal2", null), "read positive int as string val");
		Assertions.assertEquals("23456", MapUtils.strFromMap(testMap, "longVal", null), "read long as string val");
		Assertions.assertEquals("43.11", MapUtils.strFromMap(testMap, "floatVal", null), "read float as string val");
		Assertions.assertEquals("81.333", MapUtils.strFromMap(testMap, "doubleVal", null), "read double as string val");
		Assertions.assertEquals("true", MapUtils.strFromMap(testMap, "booleanVal", null), "read boolean as string val");
		
		
		// read a value that is not present in the map
		Assertions.assertEquals(null, MapUtils.strFromMap(testMap, "absentVal", null), "read missing value as string val");
		
		
		// read a value that cannot be casted to an int
		Assertions.assertEquals(null, MapUtils.strFromMap(testMap, "mapVal", null), "read map as string val");
	}
	
	
	/**
	 * check if a map contains all the passed keys
	 */
	@Test
	@DisplayName("check key presence")
	public void keyPresenceTest() {
		// check keys that are present
		boolean containsAllkeys = MapUtils.containsAllKeys(numTestMap, "intVal", "floatVal", "doubleVal", "strValInt", "strValDouble");
		Assertions.assertEquals(true, containsAllkeys, "map contains all passed keys");
		
		
		// pass some keys that are not present and some that are present
		containsAllkeys = MapUtils.containsAllKeys(numTestMap, "intVal", "floatVal", "doubleVal", "absentVal1", "strValInt", "strValDouble", "absentVal2");
		Assertions.assertEquals(false, containsAllkeys, "map contains not all passed keys");
		
		
		// only pass keys that are not present in the map
		containsAllkeys = MapUtils.containsAllKeys(numTestMap, "absentVal1", "absentVal2");
		Assertions.assertEquals(false, containsAllkeys, "map contains none of the passed keys");
	}
	
	
	/**
	 * check if a map contains all the passed keys
	 */
	@Test
	@DisplayName("create a filtered map")
	public void filterMapTest() {
		// crate a map that only contains the passed filtered values
		Map<String, Object> filteredMap = MapUtils.filterMap(numTestMap, "intVal", "floatVal", "doubleVal", "absentVal");

		// check the size of the map
		Assertions.assertEquals(3, filteredMap.size(), "size of the map");
		
		// check for all the keys that should be present
		Assertions.assertEquals(2, (int) filteredMap.get("intVal"), "filter map contains intVal");
		Assertions.assertEquals(4.11f, (float) filteredMap.get("floatVal"), "filter map contains floatVal");
		Assertions.assertEquals(6.78d, (double) filteredMap.get("doubleVal"), 0.001,"filter map contains doubleVal");
		
		// check the other properties that are present in the original map
		Assertions.assertEquals(null, filteredMap.get("strValInt"), "filter map does not contain strValInt");
		Assertions.assertEquals(null, filteredMap.get("strValDouble"), "filter map does not contain strValDouble");
		Assertions.assertEquals(null, filteredMap.get("mapVal"), "filter map does not contain mapVal");
	
		
		// check for the value that is not present because it is missing in the original map
		Assertions.assertEquals(null, filteredMap.get("absentVal"), "filter map does not contain absentVal");
	}
}
