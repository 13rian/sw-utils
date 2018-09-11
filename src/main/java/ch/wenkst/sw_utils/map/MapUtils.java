package ch.wenkst.sw_utils.map;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.wenkst.sw_utils.conversion.Conversion;

/**
 * contains utility methods to handle maps
 */
public class MapUtils {
	final static Logger logger = LogManager.getLogger(MapUtils.class);    // initialize the logger


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													extract int values 													  	   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * extracts an int value from Map
	 * @param <T>
	 * @param map 			map
	 * @param key 			key of the value
	 * @param defaultVal	the default value if an error occurred or the key is not in the map
	 * @return 				extracted value or the default value if an error occurred
	 */
	public static <T> Integer intFromMap(Map<String, T> map, String key, Integer defaultVal) {
		if (map.containsKey(key)) {
			Object obj = map.get(key);
			if (obj instanceof Double) return (int) Math.round((double) obj);
			else if (obj instanceof Long) return (int) ((long) obj);
			else if (obj instanceof Integer) return (int) obj;
			else if (obj instanceof String) return strToInt((String) obj, defaultVal);

		} else {
			logger.error("key " + key + " not found in the passed map");
		}
		return defaultVal;
	}



	/**
	 * converts a string to an int, if an error occurs defaultVal is returned and the error is logged
	 * @param str 			the value that is converted to a long
	 * @param defaultVal	the default value if a parsing error occurred
	 * @return 				parsed integer from the passed string
	 */
	private static int strToInt(String str, int defaultVal) {
		int result = defaultVal;
		try {
			result = Integer.parseInt(str.trim());
		} catch (Exception e) {
			logger.error("string " + str + " could not be parsed to a long: ", e);
		}
		return result;
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													extract long values 													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * extracts a long value from Map
	 * @param <T>
	 * @param map 			Map
	 * @param key 			key of the value
	 * @param defaultVal	the default value if an error occurred or the key is not in the map
	 * @return 				extracted value or the default value if an error occurred
	 */
	public static <T> Long longFromMap(Map<String, T> map, String key, Long defaultVal) {
		if (map.containsKey(key)) {
			Object obj = map.get(key);
			if (obj instanceof Double) return Math.round((double) obj);
			else if (obj instanceof Long) return (long) obj;
			else if (obj instanceof Integer) return (long) ((int) obj);
			else if (obj instanceof String) return strToLong((String) obj, defaultVal);

		} else {
			logger.error("key " + key + " not found in the passed map");
		}
		return defaultVal;
	}



	/**
	 * converts a string to a long, if an error occurs defaultVal is returned and the error is logged
	 * @param str 			the value that is converted to a long
	 * @param defaultVal	the default value if a parsing error occurred
	 * @return 				parsed long from the passed string
	 */
	private static long strToLong(String str, long defaultVal) {
		long result = defaultVal;
		try {
			result = Long.parseLong(str.trim());
		} catch (Exception e) {
			logger.error("string " + str + " could not be parsed to a long: ", e);
		}
		return result;
	}



	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													extract double values 													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * extracts a double value from Map
	 * @param <T>
	 * @param map 			Map
	 * @param key 			key of the value
	 * @param defaultVal	the default value if an error occurred or the key is not in the map
	 * @return 				extracted value or the default value if an error occurred
	 */
	public static <T> Double doubleFromMap(Map<String, T> map, String key, Double defaultVal) {
		if (map.containsKey(key)) {
			Object obj = map.get(key);
			if (obj instanceof Double) return (double) obj;
			else if (obj instanceof Long) return (double) obj;
			else if (obj instanceof Integer) return (double) obj;
			else if (obj instanceof String) return strToDouble((String) obj, defaultVal);

		} else {
			logger.error("key " + key + " not found in the passed map");
		}
		return defaultVal;
	}



	/**
	 * converts a string to a double, if an error occurs the defaultVal is returned and the error is logged
	 * @param str 			the value that is converted to a long
	 * @param defaultVal	the default value if a parsing error occurred
	 * @return 				parsed double form the passed string
	 */
	private static double strToDouble(String str, double defaultVal) {
		double result = defaultVal;
		try {
			result = Double.parseDouble(str.trim());
		} catch (Exception e) {
			logger.error("string + " + str + " could not be parsed to a double: ", e);
		}
		return result;
	}



	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													extract boolean values 													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * extracts a boolean from Map
	 * @param <T>
	 * @param map 			Map
	 * @param key 			key of the value
	 * @param defaultVal	the default value if an error occurred or the key is not in the map
	 * @return 				extracted value or the default value if an error occurred
	 */
	public static <T> Boolean booleanFromMap(Map<String, T> map, String key, Boolean defaultVal) {
		if (map.containsKey(key)) {
			Object obj = map.get(key);
			if (obj instanceof Boolean) {
				return (Boolean) obj;
			} else if (obj instanceof String) {
				return strToBoolean((String) obj, defaultVal);
			} else if (obj instanceof Double || obj instanceof Long || obj instanceof Integer) {
				return numToBoolean(obj, defaultVal);
			}

		} else {
			logger.error("key " + key + " not found in the passed map");
		}
		return defaultVal;
	}
	
	
	/**
	 * converts a Number object(Integer, Long, Double) to a boolean 0: false, true otherwise
	 * @param numVal 		number value
	 * @param defaultVal	the default value that is returned if an error occurred
	 * @return  			boolean value
	 */
	private static boolean numToBoolean(Object numVal, boolean defaultVal) {
		boolean result = defaultVal;
		
		if (numVal instanceof Double) return Conversion.intToBoolean((int) Math.round((double) numVal));
		else if (numVal instanceof Long) Conversion.intToBoolean((int) ((long) numVal));
		else if (numVal instanceof Integer) Conversion.intToBoolean((int) numVal);
			
		return result;
	}
	
	
	/**
	 * converts a string to a boolean
	 * @param str 			string to convert
	 * @param defaultVal 	the default value if a parsing error occurred
	 * @return 				boolean value
	 */
	private static boolean strToBoolean(String str, boolean defaultVal) {
		boolean result = defaultVal;
		try {
			result = Boolean.parseBoolean(str.trim());
		} catch (Exception e) {
			logger.error("string + " + str + " could not be parsed to a boolean: ", e);
		}
		return result;
	}



	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													extract string values 													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * extracts a long value from Map
	 * @param <T>
	 * @param map 			Map
	 * @param key 			key of the value
	 * @param defaultVal	the default value if an error occurred or the key is not in the map
	 * @return 				extracted value or the default value if an error occurred
	 */
	public static <T> String strFromMap(Map<String, T> map, String key, String defaultVal) {
		if (map.containsKey(key)) {
			Object obj = map.get(key);
			if (obj instanceof String) {
				return (String) obj;
			
			} else if (obj instanceof Double || obj instanceof Long || obj instanceof Integer || obj instanceof Boolean) {
				return String.valueOf(obj);
			}

		} else {
			logger.error("key " + key + " not found in the passed map");
		}
		return defaultVal;
	}
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											check if all keys are present  													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * checks if the passed map contains all passed keys
	 * @param <K> 	key type in the map
	 * @param <V> 	value type in the map
	 * @param map   Map
	 * @param keys  the keys to test in the map
	 * @return 		false if at least one key is missing
	 */
	@SafeVarargs
	public static <K, V> boolean containsAllKeys(Map<K, V> map, K... keys) {
		for (K key : keys) {
			if (!map.containsKey(key)) {
				// one of the passed keys is missing return false
				return false;
			}
		}
		
		// all keys found in the map
		return true;
	}
	
	
	/**
	 * checks if the passed map contains all passed keys
     * @param <K> 			key type in the map
	 * @param <V> 			value type in the map
	 * @param map   		Map
	 * @param printMissing	true if the missing keys should be logged
	 * @param keys  		the keys to test in the map
	 * @return 				false if at least one key is missing
	 */
	@SafeVarargs
	public static <K, V> boolean containsAllKeys(Map<K, V> map, boolean printMissing, K... keys) {
		if (!printMissing) {
			return containsAllKeys(map, keys);
		}
		
		boolean retVal = true;
		for (K key : keys) {
			if (!map.containsKey(key)) {
				// one of the passed keys is missing return false
				logger.error(key + ": is missing from the map");
				retVal = false;
			}
		}
		
		return retVal;
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													create a filtered map 													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * creates a map form the passed map that only contains the passed keys
	 * @param <T>
	 * @param map 		map to filter
	 * @param keys 		all the keys that should be included in the filtered map
	 * @return 			filtered map of the same type than the passed map
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, Object> filterMap(Map<String, T> map, String... keys) {
		try {
			Map<String, Object> result = map.getClass().newInstance();
			for (String key : keys) {
				result.put(key, map.get(key));
			}
			return result;
		
		} catch (Exception e) {
			logger.error("error creating the filtered hash map: ", e);
			return null;
		}
	}
}



