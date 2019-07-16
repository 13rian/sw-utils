package ch.wenkst.sw_utils.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonElement;

public class JsonDocTest {
	private static JsonDoc jsonReadDoc = null;		// holds the json file to read
	private static String jsonObjWriteStr = null; 	// holds the json string that is produces form the test object
	private static String jsonWriteStr = null;		// holds the json string that is written with a jsonDoc
	
	
	/**
	 * loads the resources that are needed for the test
	 * @throws FileNotFoundException 
	 */
	@BeforeAll
	public static void initializeExternalResources() throws FileNotFoundException {
		// define the directory for the json files
		String jsonDir = System.getProperty("user.dir") + File.separator +
				"resource" + File.separator + 
				"json" + File.separator;
		
		// load the xml file to read from the resources
		String jsonReadFile = jsonDir + "jsonRead.json";
		
		jsonReadDoc = new JsonDoc();
		jsonReadDoc.openJsonFromFile(jsonReadFile);
		
		// define the correct json that needs to be created from an object
		String jsonObjWriteFile = jsonDir + "jsonObjWrite.json";
		
		jsonObjWriteStr = FileUtils.readStrFromFile(jsonObjWriteFile);
		
		
		// define the correct json that needs to be created by the jsonDoc
		String jsonWriteFile = jsonDir + "jsonWrite.json";
		
		jsonWriteStr = FileUtils.readStrFromFile(jsonWriteFile);
	}
	
	
	/**
	 * read primitive types
	 */
	@Test
	@DisplayName("primitive types read")
	public void primitiveTypeReadTest() {
		// get the root element
		JsonElement rootEl = jsonReadDoc.getRootElement();  
		
		// read a string
		String strProp = jsonReadDoc.readString(rootEl, "strProp", "");
		Assertions.assertEquals("stringVal", strProp, "read string value");
		
		// read an integer
		int intProp = jsonReadDoc.readInt(rootEl, "intProp", 0);
		Assertions.assertEquals(67, intProp, "read int value");
		
		// read a long
		long longProp = jsonReadDoc.readLong(rootEl, "longProp", 0L);
		Assertions.assertEquals(1564981316l, longProp, "read long value");
		
		// read a double
		double doubleProp = jsonReadDoc.readDouble(rootEl, "floatProp", 0.0);
		Assertions.assertEquals(23.4, doubleProp, "read double value");
		
		// read a boolean
		boolean booleanProp = jsonReadDoc.readBoolean(rootEl, "booleanProp", false);
		Assertions.assertEquals(true, booleanProp, "read boolean value");
	}
	
	
	/**
	 * read array types
	 */
	@Test
	@DisplayName("array types read")
	public void arrayTypeReadTest() {
		// get the root element
		JsonElement rootEl = jsonReadDoc.getRootElement();  
		
		// read string array
		String[] strArr = jsonReadDoc.readStringArray(rootEl, "strArrProp");
		Assertions.assertArrayEquals(new String[] {"str1", "str2", "str3"}, strArr, "read string array");
		
		// read int array
		int[] intArr = jsonReadDoc.readIntArray(rootEl, "intArrProp");
		Assertions.assertArrayEquals(new int[] {67, 68, 69}, intArr, "read int array");
		
		// read long array
		long[] longArr = jsonReadDoc.readLongArray(rootEl, "longArrProp");
		Assertions.assertArrayEquals(new long[] {1564981316l, 567434245l, 234265245614l}, longArr, "read long array");
		
		// read double array
		double[] doubleArr = jsonReadDoc.readDoubleArray(rootEl, "floatArrProp");
		Assertions.assertArrayEquals(new double[] {23.4d, 3.556d, 2.4546d}, doubleArr, "read double array");
		
		// read boolean array
		boolean[] booleanArr = jsonReadDoc.readBooleanArray(rootEl, "booleanArrProp");
		Assertions.assertArrayEquals(new boolean[] {true, false, false, true}, booleanArr, "read boolean array");
	}
	
	
	/**
	 * read objects
	 */
	@Test
	@DisplayName("object read")
	public void objectReadTest() {
		// get the root element
		JsonElement rootEl = jsonReadDoc.getRootElement();  
		
		JsonElement childElement = jsonReadDoc.getChildElementByName(rootEl, "objProp");
		String nameStr = jsonReadDoc.readString(childElement, "name", "");
		Assertions.assertEquals("Brian", nameStr, "read string obj property");
		
		double weight = jsonReadDoc.readDouble(childElement, "weight", 0.0);
		Assertions.assertEquals(65.2d, weight, "read double obj property");
	}
	
	
	/**
	 * read object arrays
	 */
	@Test
	@DisplayName("object array read")
	public void objectArrayReadTest() {
		// get the root element
		JsonElement rootEl = jsonReadDoc.getRootElement();  
		
		JsonElement[] childElements = jsonReadDoc.getChildElementsByName(rootEl, "objArrProp");
		String nameStr = jsonReadDoc.readString(childElements[2], "name", "");
		Assertions.assertEquals("Freddy", nameStr, "read string from obj array");
		
		double weight = jsonReadDoc.readDouble(childElements[2], "weight", 0.0);
		Assertions.assertEquals(77.7d, weight, "read double from obj array");
	}
	
	
	/**
	 * write object to a json string
	 */
	@Test
	@DisplayName("write object to json string")
	public void objToJsonStrTest() {
		HashMap<String, Object> hm = new HashMap<>();
		hm.put("name", "Brian");
		hm.put("weight", 65.3);
		hm.put("blacklisted", false);
		
		HashMap<String, Object> address = new HashMap<>();
		address.put("city", "LA");
		address.put("street", "Spooner street");
		address.put("zip", 56998);
		hm.put("address", address);
				
		String jsonString = JsonDoc.objToJsonStr(hm, false);
		Assertions.assertEquals(jsonObjWriteStr, jsonString, "write object to json string");
	}
	
	
	
	/**
	 * write a json file that demonstrates how you would manipulate a json document
	 */
	@Test
	@DisplayName("write json string")
	public void writeJsonStrTest() {
		// write a new json document
		JsonDoc jsonDoc = new JsonDoc();
		jsonDoc.createNewDocument();
		JsonElement rootEl = jsonDoc.getRootElement();
		
		
		// add some simple properties
		jsonDoc.addString(rootEl, "strProp", "strVal");
		jsonDoc.addNumber(rootEl, "intProp", 50);
		jsonDoc.addNumber(rootEl, "floatProp", 44.36);
		jsonDoc.addBoolean(rootEl, "booleanProp", false);
		
		
		// add some array properties
		ArrayList<Object> strList = new ArrayList<>();
		strList.add("val1");
		strList.add("val2");
		strList.add("val3");
		jsonDoc.addArray(rootEl, "strArr", strList);
		
		ArrayList<Object> mixedList = new ArrayList<>();
		mixedList.add("val1");
		mixedList.add(53.69);
		
		JsonElement element = jsonDoc.createElement();
		jsonDoc.addString(element, "strProp", "string val");
		jsonDoc.addNumber(element, "intProp", 56.367);
		mixedList.add(element);
		jsonDoc.addArray(rootEl, "mixedArr", mixedList);
		
		
		// add an object property
		JsonElement objEl = jsonDoc.addElement(rootEl, "objProp");
		jsonDoc.addString(objEl, "name", "Brian");
		jsonDoc.addNumber(objEl, "weight", 68.6);
		
		
		// add a test element and remove it again
		jsonDoc.addElement(rootEl, "testProp");
		jsonDoc.removeElementByName(rootEl, "testProp");
		
		
		// write the jsonDoc to a string
		String jsonString = jsonDoc.writeToString(false);
		Assertions.assertEquals(jsonWriteStr, jsonString, "write jsonDoc to json string");
	}
	
}
