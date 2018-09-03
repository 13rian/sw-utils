package ch.wenkst.sw_utils.math;

public class MathOperations {
	
	/**
	 * returns the decimal part of the passed number
	 * @param number: 	the double number to extract the decimal part
	 * @return: 		the decimal part of the number
	 */
	public static double getDecimalPart(double number) {
		long iPart = (long) number;  		// integer part
		double fPart = number - iPart; 		// decimal part of the passed number
		
		return fPart;	
	}
	
	/**
	 * returns the decimal part of the passed number
	 * @param number 	the float number to extract the decimal part
	 * @return	 		the decimal part of the number
	 */
	public static float getDecimalPart(float number) {
		long iPart = (long) number;  		// integer part
		float fPart = number - iPart; 		// decimal part of the passed number
		
		return fPart;	
	}
}
