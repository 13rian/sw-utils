package ch.wenkst.sw_utils.map;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.BaseTest;

public class MapUtilsTest extends BaseTest {

	@Test
	public void existingIntFromMap() {
		Map<String, Object> map = TestMaps.numberMap();
		Assertions.assertEquals(2, (int) MapUtils.intFromMap(map, "intVal", 3));
		Assertions.assertEquals(4, (int) MapUtils.intFromMap(map, "floatVal", 3));
		Assertions.assertEquals(6, (int) MapUtils.intFromMap(map, "doubleVal", 3));
		Assertions.assertEquals(11, (int) MapUtils.intFromMap(map, "strValInt", 3));
		Assertions.assertEquals(13, (int) MapUtils.intFromMap(map, "strValDouble", 3));		
	}
	
	
	@Test
	public void nonExistingIntFromMap() {
		Map<String, Object> map = TestMaps.numberMap();
		Assertions.assertEquals(130, MapUtils.intFromMap(map, "absentVal", 130));
	}
	
	
	@Test
	public void nonCastableIntFromMap() {
		Map<String, Object> map = TestMaps.numberMap();
		Assertions.assertEquals(Integer.valueOf(555), MapUtils.intFromMap(map, "mapVal", 555));
	}
	
	
	@Test
	public void existingLongFromMap() {
		Map<String, Object> map = TestMaps.numberMap();
		Assertions.assertEquals(2l, (long) MapUtils.longFromMap(map, "intVal", 3l));
		Assertions.assertEquals(4l, (long) MapUtils.longFromMap(map, "floatVal", 3l));
		Assertions.assertEquals(6l, (long) MapUtils.longFromMap(map, "doubleVal", 3l));
		Assertions.assertEquals(11l, (long) MapUtils.longFromMap(map, "strValInt", 3l));
		Assertions.assertEquals(13l, (long) MapUtils.longFromMap(map, "strValDouble", 3l));
	}
	
	
	@Test
	public void nonExistingLongFromMap() {
		Map<String, Object> map = TestMaps.numberMap();
		Assertions.assertEquals(null, MapUtils.longFromMap(map, "absentVal", null));
	}
	
	
	@Test
	public void nonCastableLongFromMap() {
		Map<String, Object> map = TestMaps.numberMap();
		Assertions.assertEquals(Long.valueOf(555), MapUtils.longFromMap(map, "mapVal", 555l), "read map as long val");
	}
	
	
	
	@Test
	public void existingDoubleFromMap() {
		Map<String, Object> map = TestMaps.numberMap();
		double tolerance = 0.001;
		Assertions.assertEquals(2d, (double) MapUtils.doubleFromMap(map, "intVal", 3d), tolerance);
		Assertions.assertEquals(4.11d, (double) MapUtils.doubleFromMap(map, "floatVal", 3d), tolerance);
		Assertions.assertEquals(6.78d, (double) MapUtils.doubleFromMap(map, "doubleVal", 3d), tolerance);
		Assertions.assertEquals(11d, (double) MapUtils.doubleFromMap(map, "strValInt", 3d), tolerance);
		Assertions.assertEquals(13.789d, (double) MapUtils.doubleFromMap(map, "strValDouble", 3d), tolerance);
		
	}
	
	
	@Test
	public void nonExistingDoubleFromMap() {
		Map<String, Object> map = TestMaps.numberMap();
		Assertions.assertEquals(null, MapUtils.doubleFromMap(map, "absentVal", null));
		
	}
	
	
	@Test
	public void nonCastableDoubleFromMap() {
		Map<String, Object> map = TestMaps.numberMap();
		Assertions.assertEquals(Double.valueOf(555), MapUtils.doubleFromMap(map, "mapVal", 555d));
	}
	
	
	@Test
	public void existingBooleanFromMap() {
		Map<String, Object> map = TestMaps.booleanMap();
		Assertions.assertEquals(Boolean.valueOf(true), MapUtils.booleanFromMap(map, "intVal1", null));
		Assertions.assertEquals(Boolean.valueOf(false), MapUtils.booleanFromMap(map, "intVal2", null));
		Assertions.assertEquals(Boolean.valueOf(true), MapUtils.booleanFromMap(map, "doubleVal1", null));
		Assertions.assertEquals(Boolean.valueOf(true), MapUtils.booleanFromMap(map, "doubleVal2", null));
		Assertions.assertEquals(Boolean.valueOf(false), MapUtils.booleanFromMap(map, "strVal1", null));
		Assertions.assertEquals(Boolean.valueOf(false), MapUtils.booleanFromMap(map, "strVal2", null));
		Assertions.assertEquals(Boolean.valueOf(true), MapUtils.booleanFromMap(map, "booleanVal", null));
	}
	

	@Test
	public void nonExistingBooleanFromMap() {
		Map<String, Object> map = TestMaps.booleanMap();
		Assertions.assertEquals(null, MapUtils.booleanFromMap(map, "absentVal", null));
	}
	
	
	@Test
	public void nonCastableBooleanFromMap() {
		Map<String, Object> map = TestMaps.booleanMap();
		Assertions.assertEquals(null, MapUtils.booleanFromMap(map, "mapVal", null));
	}
	
	
	@Test
	public void existingStringFromMap() {
		Map<String, Object> map = TestMaps.stringMap();
		Assertions.assertEquals("normal string", MapUtils.strFromMap(map, "strVal", null));
		Assertions.assertEquals("-67", MapUtils.strFromMap(map, "intVal1", null));
		Assertions.assertEquals("3", MapUtils.strFromMap(map, "intVal2", null));
		Assertions.assertEquals("23456", MapUtils.strFromMap(map, "longVal", null));
		Assertions.assertEquals("43.11", MapUtils.strFromMap(map, "floatVal", null));
		Assertions.assertEquals("81.333", MapUtils.strFromMap(map, "doubleVal", null));
		Assertions.assertEquals("true", MapUtils.strFromMap(map, "booleanVal", null));
	}
	
	
	@Test
	public void nonExistingStringFromMap() {
		Map<String, Object> map = TestMaps.stringMap();
		Assertions.assertEquals(null, MapUtils.strFromMap(map, "absentVal", null));
	}
	
	
	@Test
	public void nonCastableStringFromMap() {
		Map<String, Object> map = TestMaps.stringMap();
		Assertions.assertEquals(null, MapUtils.strFromMap(map, "mapVal", null));
	}
	
	
	@Test
	public void mapContainsAllKeys() {
		Map<String, Object> map = TestMaps.numberMap();
		boolean containsAllkeys = MapUtils.containsAllKeys(map, "intVal", "floatVal", "doubleVal", "strValInt", "strValDouble");
		Assertions.assertEquals(true, containsAllkeys);
	}
	
	
	@Test
	public void mapContainsNotAllKeys() {
		Map<String, Object> map = TestMaps.numberMap();
		boolean containsAllkeys = MapUtils.containsAllKeys(map, "intVal", "floatVal", "doubleVal", "absentVal1", "strValInt", "strValDouble", "absentVal2");
		Assertions.assertEquals(false, containsAllkeys);
		
	}
	
	
	@Test
	public void createdFilteredMap() {
		Map<String, Object> map = TestMaps.numberMap();
		Map<String, Object> filteredMap = MapUtils.filterMap(map, "intVal", "floatVal", "doubleVal", "absentVal");
		
		Assertions.assertEquals(3, filteredMap.size());
		Assertions.assertEquals(2, (int) filteredMap.get("intVal"));
		Assertions.assertEquals(4.11f, (float) filteredMap.get("floatVal"));
		Assertions.assertEquals(6.78d, (double) filteredMap.get("doubleVal"));
	}
}
