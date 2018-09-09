package ch.wenkst.sw_utils.math;

public class MathUtils {
	
	/**
	 * returns the decimal places as long of the passed number
	 * @param number: 	the double number to extract the decimal part
	 * @return: 		the decimal part of the number
	 */
	public static long getDecimalPlaces(double number) {
		String strNum = String.valueOf(number);
		return digitsFromStrNum(strNum);	
	}
	
	/**
	 * returns the decimal part of the passed number
	 * @param number 	the float number to extract the decimal part
	 * @return	 		the decimal part of the number
	 */
	public static long getDecimalPlaces(float number) {
		String strNum = String.valueOf(number);
		return digitsFromStrNum(strNum);	
	}
	
	
	/**
	 * returns the digit part of the number as long
	 * @param strNum 	string representation of the decimal number 
	 * @return			decimal places as long 
	 */
	private static long digitsFromStrNum(String strNum) {
		String[] parts = strNum.split("\\.");
		long result = Long.parseLong(parts[1]);
		return result;
	}
}
