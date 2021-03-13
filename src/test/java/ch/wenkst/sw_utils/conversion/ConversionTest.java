package ch.wenkst.sw_utils.conversion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.BaseTest;

import java.nio.charset.StandardCharsets;

public class ConversionTest extends BaseTest {	

	@Test
	public void base64ToString() {
		String encoded = "aGVsbG8=";
		String decoded = "hello";
		Assertions.assertEquals(decoded, Conversion.base64StrToStr(encoded));
	}


	@Test
	public void stringToBase64() {
		String encoded = "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIHRoZSBsYXp5IGRvZw==";
		String decoded = "The quick brown fox jumps over the lazy dog";
		Assertions.assertEquals(encoded, Conversion.strToBase64Str(decoded));
	}


	@Test
	public void byteArrayToBase64() {
		String decoded = "hello";
		String encoded = "aGVsbG8=";
		byte[] decodedBytes = decoded.getBytes(StandardCharsets.UTF_8);
		Assertions.assertEquals(encoded, Conversion.byteArrayToBase64(decodedBytes));
	}


	@Test
	public void base64ToByteArray() {
		String decoded = "hello";
		String encoded = "aGVsbG8=";
		byte[] decodedBytes = decoded.getBytes(StandardCharsets.UTF_8);
		Assertions.assertArrayEquals(decodedBytes, Conversion.base64StrToByteArray(encoded)); 
	}


	@Test
	public void hexToString() {
		String encoded = "68656C6C6F";
		String decoded = "hello";
		Assertions.assertEquals(decoded, Conversion.hexStrToStr(encoded));
	}


	@Test
	public void stringToHex() {
		String encoded = "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f67";
		String decoded = "The quick brown fox jumps over the lazy dog";
		Assertions.assertEquals(encoded, Conversion.strToHexStr(decoded).toLowerCase());
	}


	@Test
	public void byteArrayToHex() {
		String encoded = "68656C6C6F";
		String decoded = "hello";
		byte[] decodedBytes = decoded.getBytes(StandardCharsets.UTF_8);
		Assertions.assertEquals(encoded, Conversion.byteArrayToHexStr(decodedBytes));
	}


	@Test
	public void hexToByteArray() {
		String encoded = "68656C6C6F";
		String decoded = "hello";
		byte[] decodedBytes = decoded.getBytes(StandardCharsets.UTF_8);
		Assertions.assertArrayEquals(decodedBytes, Conversion.hexStrToByteArray(encoded)); 
	}


	@Test
	public void intToHexWithNoPadding() {
		String hex = "6F8DB";
		int number = 456923;
		Assertions.assertEquals(hex, Conversion.intToHexStr(number)); 	 
	}


	@Test
	public void noPaddingHexToInt() {
		String hex = "6F8DB";
		int number = 456923;
		Assertions.assertEquals(number, Conversion.hexStrToInt(hex)); 
	}


	@Test
	public void longToHexWithNoPadding() {
		String hex = "6F8DB";
		long number = 456923L;
		Assertions.assertEquals(hex, Conversion.longToHexStr(number)); 	
	}


	@Test
	public void noPaddingHexToLong() {
		String hex = "6F8DB";
		long number = 456923L;
		Assertions.assertEquals(number, Conversion.hexStrToLong(hex)); 
	}


	@Test
	public void intToHexWithPadding() {
		String hex = "0006F8DB";
		int number = 456923;
		int length = 8;
		Assertions.assertEquals(hex, Conversion.intToHexStr(number, length)); 
	}


	@Test
	public void paddedHexToInt() {
		String hex = "0006F8DB";
		int number = 456923;
		Assertions.assertEquals(number, Conversion.hexStrToInt(hex)); 
	}


	@Test
	public void longToHexWithPadding() {
		String hex = "0006F8DB";
		long number = 456923;
		int length = 8;
		Assertions.assertEquals(hex, Conversion.longToHexStr(number, length)); 	
	}


	@Test
	public void paddedHexToLong() {
		String hex = "0006F8DB";
		long number = 456923;
		Assertions.assertEquals(number, Conversion.hexStrToInt(hex)); 	
	}


	@Test
	public void booleanToInt() {
		Assertions.assertEquals(1, Conversion.booleanToInt(true));
		Assertions.assertEquals(0, Conversion.booleanToInt(false));
	}


	@Test
	public void intToBoolean() {
		Assertions.assertEquals(true, Conversion.numToBoolean(1));
		Assertions.assertEquals(true, Conversion.numToBoolean(500));
		Assertions.assertEquals(true, Conversion.numToBoolean(-13));
		Assertions.assertEquals(false, Conversion.numToBoolean(0));
	}


	@Test
	public void concatIntArrays() {
		int[] arr1 = {1,2,3};
		int[] arr2 = {4,5};
		int[] arr3 = {6};
		Assertions.assertArrayEquals(new int[] {1,2,3}, Conversion.concatArrays(arr1));
		Assertions.assertArrayEquals(new int[] {1,2,3,4,5,6}, Conversion.concatArrays(arr1, arr2, arr3));
	}
	
	
	@Test
	public void concatStringArrays() {
		String[] arr5 = {"banana", "apple"};
		String[] arr6 = {"cherry"};
		Assertions.assertArrayEquals(new String[] {"banana", "apple", "cherry"}, Conversion.concatArrays(arr5, arr6));
	}

	
	@Test
	public void padStringRight() {
		Assertions.assertEquals("hello---", Conversion.padRight("hello", '-', 8));
	}
	
	
	@Test
	public void padStringLeft() {
		Assertions.assertEquals("---hello", Conversion.padLeft("hello", '-', 8));
	}

	
	@Test
	public void reverseString() {
		Assertions.assertEquals("olleh", Conversion.strReverse("hello"));
	}
}
