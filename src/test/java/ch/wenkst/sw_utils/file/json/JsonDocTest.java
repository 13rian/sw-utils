package ch.wenkst.sw_utils.file.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.google.gson.JsonElement;

import ch.wenkst.sw_utils.file.JsonDoc;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonDocTest {
	private JsonTestFiles testFiles;
	
	
	@BeforeAll
	public void setupTestFiles() throws IOException {
		testFiles = new JsonTestFiles();
		testFiles.setup();
	}
	
	
	@Test
	public void readString() {
		JsonElement rootEl = testFiles.rootElementForRead();
		String strProp = testFiles.jsonReadDoc.readString(rootEl, "strProp", "");
		Assertions.assertEquals("stringVal", strProp);
	}
	
	
	@Test
	public void readInteger() {
		JsonElement rootEl = testFiles.rootElementForRead();
		int intProp = testFiles.jsonReadDoc.readInt(rootEl, "intProp", 0);
		Assertions.assertEquals(67, intProp);
	}
	
	
	@Test
	public void readLong() {
		JsonElement rootEl = testFiles.rootElementForRead();
		long longProp = testFiles.jsonReadDoc.readLong(rootEl, "longProp", 0L);
		Assertions.assertEquals(1564981316l, longProp);
	}
	
	
	@Test
	public void readDouble() {
		JsonElement rootEl = testFiles.rootElementForRead();
		double doubleProp = testFiles.jsonReadDoc.readDouble(rootEl, "floatProp", 0.0);
		Assertions.assertEquals(23.4, doubleProp);
	}
	
	
	@Test
	public void readBoolean() {
		JsonElement rootEl = testFiles.rootElementForRead();
		boolean booleanProp = testFiles.jsonReadDoc.readBoolean(rootEl, "booleanProp", false);
		Assertions.assertTrue(booleanProp);
	}
	
	
	@Test
	public void readStringArray() {
		JsonElement rootEl = testFiles.rootElementForRead();
		String[] strArr = testFiles.jsonReadDoc.readStringArray(rootEl, "strArrProp");
		Assertions.assertArrayEquals(new String[] {"str1", "str2", "str3"}, strArr);
	}
	
	
	@Test
	public void readIntegerArray() {
		JsonElement rootEl = testFiles.rootElementForRead();
		int[] intArr = testFiles.jsonReadDoc.readIntArray(rootEl, "intArrProp");
		Assertions.assertArrayEquals(new int[] {67, 68, 69}, intArr);
	}
	
	
	@Test
	public void readLongArray() {
		JsonElement rootEl = testFiles.rootElementForRead();
		long[] longArr = testFiles.jsonReadDoc.readLongArray(rootEl, "longArrProp");
		Assertions.assertArrayEquals(new long[] {1564981316l, 567434245l, 234265245614l}, longArr);
	}
	
	
	@Test
	public void readDoubleArray() {
		JsonElement rootEl = testFiles.rootElementForRead();
		double[] doubleArr = testFiles.jsonReadDoc.readDoubleArray(rootEl, "floatArrProp");
		Assertions.assertArrayEquals(new double[] {23.4d, 3.556d, 2.4546d}, doubleArr);
	}
	
	
	@Test
	public void readBooleanArray() {
		JsonElement rootEl = testFiles.rootElementForRead();
		boolean[] booleanArr = testFiles.jsonReadDoc.readBooleanArray(rootEl, "booleanArrProp");
		Assertions.assertArrayEquals(new boolean[] {true, false, false, true}, booleanArr);
	}


	@Test
	public void readObject() {
		JsonElement rootEl = testFiles.rootElementForRead();
		
		JsonElement childElement = testFiles.jsonReadDoc.getChildElementByName(rootEl, "objProp");
		String nameStr = testFiles.jsonReadDoc.readString(childElement, "name", "");
		Assertions.assertEquals("Brian", nameStr);
		
		double weight = testFiles.jsonReadDoc.readDouble(childElement, "weight", 0.0);
		Assertions.assertEquals(65.2d, weight);
	}
	
	
	@Test
	public void readObjectArray() {
		JsonElement rootEl = testFiles.rootElementForRead();
		
		JsonElement[] childElements = testFiles.jsonReadDoc.getChildElementsByName(rootEl, "objArrProp");
		String nameStr = testFiles.jsonReadDoc.readString(childElements[2], "name", "");
		Assertions.assertEquals("Freddy", nameStr);
		
		double weight = testFiles.jsonReadDoc.readDouble(childElements[2], "weight", 0.0);
		Assertions.assertEquals(77.7d, weight);
	}
	
	
	@Test
	public void writeString() {
		JsonDoc jsonDoc = testFiles.newJsonDoc();
		jsonDoc.addString(jsonDoc.getRootElement(), "strProp", "strVal");
		testFiles.documentCorrect(jsonDoc, "{\"strProp\":\"strVal\"}");
	}
	
	
	@Test
	public void writeInteger() {
		JsonDoc jsonDoc = testFiles.newJsonDoc();
		jsonDoc.addNumber(jsonDoc.getRootElement(), "intProp", 50);
		testFiles.documentCorrect(jsonDoc, "{\"intProp\":50}");
	}
	
	
	@Test
	public void writeFloat() {
		JsonDoc jsonDoc = testFiles.newJsonDoc();
		jsonDoc.addNumber(jsonDoc.getRootElement(), "floatProp", 44.36);
		testFiles.documentCorrect(jsonDoc, "{\"floatProp\":44.36}");
	}
	
	
	@Test
	public void writeBoolean() {
		JsonDoc jsonDoc = testFiles.newJsonDoc();
		jsonDoc.addBoolean(jsonDoc.getRootElement(), "booleanProp", false);
		testFiles.documentCorrect(jsonDoc, "{\"booleanProp\":false}");
	}
	
	
	@Test
	public void writeStringArray() {
		JsonDoc jsonDoc = testFiles.newJsonDoc();
		List<String> strList = Arrays.asList("val1", "val2", "val3");
		jsonDoc.addArray(jsonDoc.getRootElement(), "strArr", strList);
		testFiles.documentCorrect(jsonDoc, "{\"strArr\":[\"val1\",\"val2\",\"val3\"]}");
	}
	
	
	@Test
	public void writeMixedObjectArray() {
		JsonDoc jsonDoc = testFiles.newJsonDoc();
		
		JsonElement element = jsonDoc.createElement();
		jsonDoc.addString(element, "strProp", "string val");
		jsonDoc.addNumber(element, "intProp", 56);
		List<Object> mixedList = Arrays.asList("val1", 53.69, element);
		
		jsonDoc.addArray(jsonDoc.getRootElement(), "mixedArr", mixedList);
		testFiles.documentCorrect(jsonDoc, "{\"mixedArr\":[\"val1\",53.69,{\"strProp\":\"string val\",\"intProp\":56}]}");
	}
	
	
	@Test
	public void writeObject() {
		JsonDoc jsonDoc = testFiles.newJsonDoc();
		JsonElement objEl = jsonDoc.addElement(jsonDoc.getRootElement(), "objProp");
		jsonDoc.addString(objEl, "name", "Brian");
		jsonDoc.addNumber(objEl, "weight", 68.6);
		testFiles.documentCorrect(jsonDoc, "{\"objProp\":{\"name\":\"Brian\",\"weight\":68.6}}");
	}
	
	
	@Test
	public void writeAndDeleteElement() {
		JsonDoc jsonDoc = testFiles.newJsonDoc();
		jsonDoc.addElement(jsonDoc.getRootElement(), "testProp");
		jsonDoc.removeElementByName(jsonDoc.getRootElement(), "testProp");
		testFiles.documentCorrect(jsonDoc, "{}");
	}
}
