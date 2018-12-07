package ch.wenkst.sw_utils.conversion;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provides several conversion methods. UTF-8 is always used to get the bytes from a String and vice versa
 * character set is important to define how the character are encoded in bytes (e.g. ö can be encoded with one
 * or 2 bytes depending on the character set).
 * binary data can be represented as hex-string or base64 -string. e.g. 0F = 0000 1111,
 * this hex string cannot be converted back with str.getBytes(), since then you have a byte array representation
 * that has an underlying character set, which is not the binary representation you want.
 */
public class Conversion {
	private static final Logger logger = LoggerFactory.getLogger(Conversion.class);
	
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray(); 	// needed for hex conversions
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												Base64 													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * encodes the input String to Base64
	 * @param str 			String to encode
	 * @param withPadding 	true if String should be encoded with a padding (normally padding is used)
	 * @return	 			Base64 String, that holds the binary representation of the string with utf-8 character set
	 */
	public static String strToBase64Str(String str, boolean withPadding) {
		String result = ""; 
		
		if (withPadding) {
			result = Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
		
		} else {
			result = Base64.getEncoder().withoutPadding().encodeToString(str.getBytes(StandardCharsets.UTF_8));
		}
	
		return result;
	}
	
	
	/**
	 * encodes the input String to Base64 with the commonly used padding
	 * @param str 			String to encode
	 * @return	 			Base64 String, that holds the binary representation of the string with utf-8 character set
	 */
	public static String strToBase64Str(String str) {
		String result = Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
			
		return result;
	}
	
	
	/**
	 * decodes Base64 input String
	 * @param str 	Base64 String to decode, that is the binary representation of a string with utf-8 character set
	 * @return	 	a String created from the binary data with character set utf-8
	 */
	public static String base64StrToStr(String str) {
		String result = "";
		byte [] barr = Base64.getDecoder().decode(str);
		result = new String(barr, StandardCharsets.UTF_8);
				
		return result;
	}
	
	
	/**
	 * converts the passed byte array to a Base64 String
	 * @param barr	 	byte array to convert
	 * @return	 		Base64 String
	 */
	public static String byteArrayToBase64(byte[] barr) {
		String result = Base64.getEncoder().encodeToString(barr);
		
		return result;
	}
	
	
	/**
	 * converts a Base64 String to a byte array
	 * @param str 	base64 String
	 * @return	 	resulting byte array
	 */
	public static byte[] base64StrToByteArray(String str) {
		byte[] result = Base64.getDecoder().decode(str.getBytes());		
		
		return result;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												Hex 													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * encodes the input String to a hex String
	 * @param str 	String to encode in UTF8
	 * @return	 	the hex String
	 */
	public static String strToHexStr(String str) {
		String result = "";
		
		// UTF-8 byte representation of string (byte array with utf-8 character set)
		byte[] myBytes = str.getBytes(StandardCharsets.UTF_8);   
		
		// get the binary representation of the byte array
		result = byteArrayToHexStr(myBytes);
			
		return result;
	}
	
	
	/**
	 * decodes hex input String
	 * @param str 	hex String to decode
	 * @return	 	a String with UTF-8 character set
	 */
	public static String hexStrToStr(String str) {
		// get the binary representation of the hex String
		byte[] barr = hexStrToByteArray(str);
		
		// create the string with UTF-8 character set
		String result = new String(barr, StandardCharsets.UTF_8);
		return result;
	}
	
	
	/**
	 * converts the passed byte array to a hex String representing the binary data
	 * @param barr	 	the byte array to convert
	 * @return	 		the hex string
	 */
	public static String byteArrayToHexStr(byte[] barr) {
	    char[] hexChars = new char[barr.length * 2];
	    for ( int j = 0; j < barr.length; j++ ) {
	        int v = barr[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    String result = new String(hexChars);
		
		return result;
	}
	
	
	
	/**
	 * converts a hex String that represents binary data to a byte array
	 * @param str 		the string to convert
	 * @return 			byte array of the hex string
	 */
	public static byte[] hexStrToByteArray(String str) {
	    int len = str.length();
	    byte[] result = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	    	result[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4)
	                             + Character.digit(str.charAt(i+1), 16));
	    }	
		
		return result;
	}
	
	
	/**
	 * converts an int to a hex String
	 * @param i 		integer to convert
	 * @param length 	zeros are added on the left side when the length of the resulting hex-string is smaller than length
	 * @return 			hex string in capital letters
	 */
	public static String intToHexStr(int i, int length) {
		String result = Integer.toHexString(i).toUpperCase();
		
		// add the padding
		result = padLeft(result, '0', length);
		return result;
	}
	
	
	/**
	 * converts an int to a hex String
	 * @param i 	integer to convert
	 * @return		hex string in capital letters
	 */
	public static String intToHexStr(int i) {
		String result = Integer.toHexString(i).toUpperCase();
		return result;
	}
	
	
	/**
	 * converts a hex String to int
	 * @param hexStr 	hex string to convert
	 * @return 			the integer representation of the passed hex string
	 */
	public static long hexStrToInt(String hexStr) {
		long result = Integer.parseInt(hexStr, 16);
		return result;
	}
	
	
	/**
	 * converts an long to a hex String
	 * @param l 		long to convert
	 * @param length 	zeros are added on the left side when the length of the resulting hex-string is smaller than length
	 * @return 			hex string in capital letters
	 */
	public static String longToHexStr(long l, int length) {
		String result = Long.toHexString(l).toUpperCase();
		
		// add the padding
		result = padLeft(result, '0', length);
		return result;
	}
	
	
	/**
	 * converts an long to a hex String
	 * @param l 	long that is converted to a hex string
	 * @return 		hex string in capital letters
	 */
	public static String longToHexStr(long l) {
		String result = Long.toHexString(l).toUpperCase();
		return result;
	}
	
	
	/**
	 * converts a hex String to long
	 * @param hexStr 	the hex string that is converted
	 * @return 			the long representation of the passed hex string
	 */
	public static long hexStrToLong(String hexStr) {
		long result = Long.parseLong(hexStr, 16);
		return result;
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 										Data conversions 												   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											Booleans 													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * converts the passed number to a boolean
	 * @param num 	num to convert to an integer
	 * @return 		boolean representation of the number
	 */
	public static boolean numToBoolean(Number num) {
		if (num.doubleValue() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	
	/**
	 * converts the passed boolean to an integer
	 * @param bool 	the boolean 
	 * @return 		integer representation of the boolean
	 */
	public static int booleanToInt(boolean bool) {
		if (bool) {
			return 1;
		} else {
			return 0;
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												Bytes 													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * converts a byte array to an array list of bytes
	 * @param barr	 	the byte array to convert
	 * @return	 		the converted arrayList
	 */
	public static List<Byte> byteArrayToArrayList(byte[] barr) {
		List<Byte> result = new ArrayList<Byte>();
		for (int i=0; i<barr.length; i++) {
			result.add(barr[i]);
		}
		return result;
	}
	
	
	/**
	 * converts an array list of bytes to a byte array
	 * @param aList 	arrayList to convert
	 * @return 			the converted byte array
	 */
	public static byte[] arrayListToByteArray(List<Byte> aList) {
		byte[] result = new byte[aList.size()];
		for (int i=0; i<aList.size(); i++) {
			result[i] = aList.get(i);
		}
		return result;
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 										concatenate 2 arrays 											   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    
	/**
	 * concatenates an arbitrary amount of passed arrays of primitive or object type
	 * @param <T> 		the array object
	 * @param arrays 	arrays to concatenate
	 * @return	 		the concatenated array or null if an error occurred
	 */
    @SuppressWarnings("unchecked")
	@SafeVarargs
	public static <T> T concatArrays(T... arrays) {
    	try {
    		int length = 0;
    		for (T array : arrays) {
    			length += Array.getLength(array);
    		}

    		T result = (T) Array.newInstance(arrays[0].getClass().getComponentType(), length);

    		int offset = 0;
    		for (T array : arrays) {
    			int arrLength = Array.getLength(array);
    			System.arraycopy(array, 0, result, offset, arrLength);
    			offset += arrLength;
    		}

    		return result;

    	} catch (Exception e) {
    		logger.error("error concatenating the passed arrays: ", e);
    		return null;
    	}
    }
   
    
    
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											string handling  											   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * adds a padding of spaces on the left side of the passed string if it is shorter than the passed length
	 * @param str 		the string that is padded
	 * @param length 	desired length
	 * @return 			the string with the padding on the left side
	 */
	public static String padLeft(String str, int length) {
		return padLeft(str, ' ', length);
	}
    
    
	/**
	 * adds a padding on the left side of the passed string if it is shorter than the passed length
	 * @param str 		the string that is padded
	 * @param padChar 	character to pad the string with
	 * @param length 	desired length
	 * @return 			the string with the padding on the left side
	 */
	public static String padLeft(String str, char padChar, int length) {
		int strLength = str.length();
		if (strLength >= length) {
			return str;
		}
		
		String paddedStr = new String(new char[length - strLength]).replace('\0', padChar) + str;
		return paddedStr;
	}
	
	
	/**
	 * adds a padding of spaces on the right side of the passed string if it is shorter than the passed length
	 * @param str 		the string that is padded
	 * @param length 	desired length
	 * @return 			the string with the padding on the right side
	 */
	public static String padRight(String str, int length) {
		return padRight(str, ' ', length);
	}
	
	
	/**
	 * adds a padding on the right side of the passed string if it is shorter than the passed length
	 * @param str 		the string that is padded
	 * @param padChar 	character to pad the string with
	 * @param length 	desired length
	 * @return 			the string with the padding on the right side
	 */
	public static String padRight(String str, char padChar, int length) {
		int strLength = str.length();
		if (strLength >= length) {
			return str;
		}
		
		String paddedStr = str + new String(new char[length - strLength]).replace('\0', padChar);
		return paddedStr;
	}
	
	
	/**
	 * reverses the order of the character of the passed string
	 * @param str 	the string of which the character sequence is reversed 
	 * @return 		string with the reversed character order
	 */
	public static String strReverse(String str) {
		return new StringBuilder(str).reverse().toString();
	}
	
}
