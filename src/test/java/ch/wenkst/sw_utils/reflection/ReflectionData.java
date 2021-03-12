package ch.wenkst.sw_utils.reflection;

import java.util.HashMap;
import java.util.Map;


public class ReflectionData {
	private int intProp = 0;
	private String strProp = null;
	private int[] intArrProp = null;
	private Map<String, Long> mapProp = null;
	
	
	/**
	 * holds some different properties to test the reflection utils
	 * @param strProp 			string property
	 * @param intArrProp 		int array property
	 * @param mapProp 			map property
	 */
	public ReflectionData() {
	
	}
	
	/**
	 * holds some different properties to test the reflection utils
	 * @param intProp 			int property
	 * @param strProp 			string property
	 * @param intArrProp 		int array property
	 * @param mapProp 			map property
	 */
	public ReflectionData(int intProp, String strProp, int[] intArrProp, HashMap<String, Long> mapProp) {
		this.intProp = intProp;
		this.strProp = strProp;
		this.intArrProp = intArrProp;
		this.mapProp = mapProp;
	}
	
	
	public int getIntProp() {
		return intProp;
	}

	public void setIntProp(int intProp) {
		this.intProp = intProp;
	}

	public String getStrProp() {
		return strProp;
	}
	
	public void setStrProp(String strProp) {
		this.strProp = strProp;
	}
	
	public int[] getIntArrProp() {
		return intArrProp;
	}
	
	public void setIntArrProp(int[] intArrProp) {
		this.intArrProp = intArrProp;
	}
	
	public Map<String, Long> getMapProp() {
		return mapProp;
	}
	
	public void setMapProp(Map<String, Long> mapProp) {
		this.mapProp = mapProp;
	}
}
