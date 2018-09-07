package ch.wenkst.sw_utils.conversion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class ConversionTest {
 
	/**
	 * base64 encoding and decoding
	 */
	@Test
	@DisplayName("base 64 conversion")
	public void b64Test() {
		// decode a base 64 string
		String b64Str = "aGVsbG8=";
		String strDec = "hello";
		
		Assertions.assertEquals(strDec, Conversion.base64StrToStr(b64Str));
		
		
		// encode a base 64 string
		b64Str = "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIHRoZSBsYXp5IGRvZw==";
		strDec = "The quick brown fox jumps over the lazy dog";
		
		Assertions.assertEquals(b64Str, Conversion.strToBase64Str(strDec));
		
		
		// convert to byte array and back
		String testStr = "Some string that needs to be converted";
		String b64TestStr = Conversion.strToBase64Str(testStr);
		byte[] dataBytes = testStr.getBytes(StandardCharsets.UTF_8);
		
		Assertions.assertEquals(b64TestStr, Conversion.byteArrayToBase64(dataBytes)); 			// convert to byte array
		Assertions.assertArrayEquals(dataBytes, Conversion.base64StrToByteArray(b64TestStr)); 	// convert to base 64 string
	}
	
	
	
	/**
	 * hex encoding and decoding
	 */
	@Test
	@DisplayName("hex conversion")
	public void hexTest() {
		// decode a hex string
		String hexStr = "68656C6C6F";
		String strDec = "hello";
		
		Assertions.assertEquals(strDec, Conversion.hexStrToStr(hexStr));
		
		
		// encode a hex string
		hexStr = "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f67";
		strDec = "The quick brown fox jumps over the lazy dog";
		
		Assertions.assertEquals(hexStr, Conversion.strToHexStr(strDec).toLowerCase());
		
		
		// convert to byte array and back
		String testStr = "Some string that needs to be converted";
		String hexTestStr = Conversion.strToHexStr(testStr);
		byte[] dataBytes = testStr.getBytes(StandardCharsets.UTF_8);
		
		Assertions.assertTrue(hexTestStr.equalsIgnoreCase(Conversion.byteArrayToHexStr(dataBytes))); 	// convert to byte array
		Assertions.assertArrayEquals(dataBytes, Conversion.hexStrToByteArray(hexTestStr)); 				// convert to hex string
		
		
		// int to hex string and back with no padding
		String hexNum = "6F8DB";
		int numInt = 456923;
		long numLong = 456923L;
		
		Assertions.assertEquals(hexNum, Conversion.intToHexStr(numInt)); 	// int to hex
		Assertions.assertEquals(numInt, Conversion.hexStrToInt(hexNum)); 	// hex to int
		
		Assertions.assertEquals(hexNum, Conversion.longToHexStr(numLong)); 	// long to hex
		Assertions.assertEquals(numLong, Conversion.hexStrToInt(hexNum)); 	// hex to long
		
		
		// int to hex string and back with padding
		String hexNumPadded = "0006F8DB";
		int length = 8;
		
		Assertions.assertEquals(hexNumPadded, Conversion.intToHexStr(numInt, length)); 		// int to hex
		Assertions.assertEquals(numInt, Conversion.hexStrToInt(hexNumPadded)); 				// hex to int
		
		Assertions.assertEquals(hexNumPadded, Conversion.longToHexStr(numLong, length)); 	// long to hex
		Assertions.assertEquals(numLong, Conversion.hexStrToInt(hexNumPadded)); 			// hex to long
	}	
	
	
	/**
	 * conversion of array / array list 
	 */
	@Test
	@DisplayName("array-ArrayList conversion")
	public void arrayTest() {
		// boolean to int
		Assertions.assertEquals(1, Conversion.booleanToInt(true));
		Assertions.assertEquals(0, Conversion.booleanToInt(false));
		
		// int to boolean
		Assertions.assertEquals(true, Conversion.intToBoolean(1));
		Assertions.assertEquals(true, Conversion.intToBoolean(500));
		Assertions.assertEquals(true, Conversion.intToBoolean(-13));
		Assertions.assertEquals(false, Conversion.intToBoolean(0));
	}
	
	
	
	/**
	 * test the concatenation of two or more arrays
	 */
	@Test
	@DisplayName("concat array")
	public void concatArrayTest() {
		int[] arr1 = {1,2,3};
		int[] arr2 = {4,5};
		int[] arr3 = {6};
		boolean[] arr4 = {true, true, false};
		
		// concatenate all three arrays
		Assertions.assertArrayEquals(new int[] {1,2,3,4,5,6}, Conversion.concatArrays(arr1, arr2, arr3));
		
		// pass only one array
		Assertions.assertArrayEquals(new int[] {1,2,3}, Conversion.concatArrays(arr1));
		
		// pass in different array types, this should fail
		Assertions.assertEquals(null, Conversion.concatArrays(arr3, arr4));
		
		
		// concatenate strings
		String[] arr5 = {"banana", "apple"};
		String[] arr6 = {"cherry"};
		
		Assertions.assertArrayEquals(new String[] {"banana", "apple", "cherry"}, Conversion.concatArrays(arr5, arr6));
	}
	
	
	
	/**
	 * test the concatenation of two or more arrays
	 */
	@Test
	@DisplayName("string handling")
	public void strHandlingTest() {
		String str = "hello";
		
		// pad the string on the right side
		Assertions.assertEquals("hello---", Conversion.padRight(str, '-', 8));
		
		// pad the string on the left side
		Assertions.assertEquals("---hello", Conversion.padLeft(str, '-', 8));
		
		// reverse the string
		Assertions.assertEquals("olleh", Conversion.strReverse(str));
	}
	
 
 
}
