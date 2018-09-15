package ch.wenkst.sw_utils.reflection;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReflectionUtilsTest {
	/**
	 * set some object properties with reflection
	 */
	@Test()
	@DisplayName("set properties with reflection")
	public void reflectionSetTest() {
		ReflectionData data = new ReflectionData();
		
		// set an int property
		ReflectionUtils.setObjProperty(data, "intProp", 89);
		Assertions.assertEquals(89, data.getIntProp(), "set int property");
		
		// set a string property
		ReflectionUtils.setObjProperty(data, "strProp", "strVal");
		Assertions.assertEquals("strVal", data.getStrProp(), "set string property");
		
		// set an int array property
		ReflectionUtils.setObjProperty(data, "intArrProp", new int[] {1, 3, 4, 5});
		Assertions.assertArrayEquals(new int[] {1, 3, 4, 5}, data.getIntArrProp(), "set int array property");
		
		// set an map property
		HashMap<String, Long> map = new HashMap<>();
		map.put("mapKey", 34567l);
		ReflectionUtils.setObjProperty(data, "mapProp", map);
		Assertions.assertEquals(34567l, (long) data.getMapProp().get("mapKey"), "set map property");
		
		
		// set the wrong type
		boolean result = ReflectionUtils.setObjProperty(data, "mapProp", 4);
		Assertions.assertEquals(false, result, "set the wrong data type");
		
		// set a filed that is not present
		result = ReflectionUtils.setObjProperty(data, "absentProp", "someVal");
		Assertions.assertEquals(false, result, "set a field that is not present");
	}
	
	
	
	/**
	 * get some object properties with reflection
	 */
	@SuppressWarnings("unchecked")
	@Test()
	@DisplayName("set properties with reflection")
	public void reflectionGetTest() {
		HashMap<String, Long> map = new HashMap<>();
		map.put("mapKey", 334455l);
		ReflectionData data = new ReflectionData(76, "strVal", new int[] {5, 6, 7}, map);
		
		// get an int property
		int intVal = (int) ReflectionUtils.getObjProperty(data, "intProp");
		Assertions.assertEquals(76, intVal, "get int property");
		
		// get a string property
		String strVal = (String) ReflectionUtils.getObjProperty(data, "strProp");
		Assertions.assertEquals("strVal", strVal, "get string property");
		
		// get an int array property
		int[]  intArrVal = (int[]) ReflectionUtils.getObjProperty(data, "intArrProp");
		Assertions.assertArrayEquals(new int[] {5, 6, 7}, intArrVal, "get int array property");
		
		// get an map property
		HashMap<String, Long> mapVal = (HashMap<String, Long>) ReflectionUtils.getObjProperty(data, "mapProp");
		Assertions.assertEquals(334455l, (long) mapVal.get("mapKey"), "get map property");
		
		
		// get a filed that is not present
		Object result = ReflectionUtils.getObjProperty(data, "absentProp");
		Assertions.assertEquals(null, result, "get a field that is not present");
	}
}
