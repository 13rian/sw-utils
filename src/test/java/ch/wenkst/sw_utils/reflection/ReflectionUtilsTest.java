package ch.wenkst.sw_utils.reflection;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ch.wenkst.sw_utils.BaseTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReflectionUtilsTest extends BaseTest {	
	private ReflectionData data;
	
	
	@BeforeAll
	public void createDataObject() {
		data = new ReflectionData();
	}
	
	
	@Test
	public void setIntProperty() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ReflectionUtils.setObjProperty(data, "intProp", 89);
		Assertions.assertEquals(89, data.getIntProp());
	}
	
	
	@Test
	public void setStringProperty() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ReflectionUtils.setObjProperty(data, "strProp", "strVal");
		Assertions.assertEquals("strVal", data.getStrProp());
	}
	
	
	@Test
	public void setIntArrayProperty() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ReflectionUtils.setObjProperty(data, "intArrProp", new int[] {1, 3, 4, 5});
		Assertions.assertArrayEquals(new int[] {1, 3, 4, 5}, data.getIntArrProp());
	}
	
	
	@Test
	public void setMapProperty() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Map<String, Long> map = new HashMap<>();
		map.put("mapKey", 34567l);
		ReflectionUtils.setObjProperty(data, "mapProp", map);
		Assertions.assertEquals(34567l, (long) data.getMapProp().get("mapKey"));
	}
	
	
	@Test
	public void setValueWithWrongFieldType() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			 ReflectionUtils.setObjProperty(data, "mapProp", 4);
	    });
	}
	
	
	@Test
	public void setNonExistingField() {
		Assertions.assertThrows(NoSuchFieldException.class, () -> {
			ReflectionUtils.setObjProperty(data, "absentProp", "someVal");
	    });		
	}
	
	
	@Test
	public void getIntProperty() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		data.setIntProp(77);
		int intVal = (int) ReflectionUtils.getObjProperty(data, "intProp");
		Assertions.assertEquals(77, intVal);
	}
	
	
	@Test
	public void getStringProperty() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		data.setStrProp("strVal");
		String strVal = (String) ReflectionUtils.getObjProperty(data, "strProp");
		Assertions.assertEquals("strVal", strVal);
	}
	
	
	@Test
	public void getIntArrayProperty() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		data.setIntArrProp(new int[] {5, 6, 7});
		int[]  intArrVal = (int[]) ReflectionUtils.getObjProperty(data, "intArrProp");
		Assertions.assertArrayEquals(new int[] {5, 6, 7}, intArrVal, "get int array property");
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void getMapProperty() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Map<String, Long> map = new HashMap<>();
		map.put("mapKey", 552L);
		data.setMapProp(map);
		Map<String, Long> mapVal = (Map<String, Long>) ReflectionUtils.getObjProperty(data, "mapProp");
		Assertions.assertEquals(552L, (long) mapVal.get("mapKey"), "get map property");
	}
	
	
	@Test
	public void getNonExistingField() {
		Assertions.assertThrows(NoSuchFieldException.class, () -> {
			ReflectionUtils.getObjProperty(data, "absentProp");
	    });	
	}
}
