package ch.wenkst.sw_utils.map;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import ch.wenkst.sw_utils.conversion.Conversion;

public class MapUtils {
	private static final Logger logger = LoggerFactory.getLogger(MapUtils.class);


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
			if (obj instanceof Number) return ((Number) obj).intValue();
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
	private static Integer strToInt(String str, Integer defaultVal) {
		Integer result = defaultVal;
		try {
			double doubleVal = Double.parseDouble(str.trim());
			result = (int) doubleVal;
		} catch (Exception e) {
			logger.error("string " + str + " could not be parsed to an int: ", e);
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
			if (obj instanceof Number) return ((Number) obj).longValue();
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
	private static Long strToLong(String str, Long defaultVal) {
		Long result = defaultVal;
		try {
			double doubleVal = Double.parseDouble(str.trim());
			result = (long) doubleVal;
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
			if (obj instanceof Number) return ((Number) obj).doubleValue();
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
	private static Double strToDouble(String str, Double defaultVal) {
		Double result = defaultVal;
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
			} else if (obj instanceof Number || obj instanceof Long || obj instanceof Integer) {
				return Conversion.numToBoolean((Number) obj);
			}

		} else {
			logger.error("key " + key + " not found in the passed map");
		}
		return defaultVal;
	}
	
	
	
	/**
	 * converts a string to a boolean
	 * @param str 			string to convert
	 * @param defaultVal 	the default value if a parsing error occurred
	 * @return 				boolean value
	 */
	private static Boolean strToBoolean(String str, Boolean defaultVal) {
		Boolean result = defaultVal;
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
			
			} else if (obj instanceof Number || obj instanceof Boolean) {
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
				return false;
			}
		}
		
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
	 * if the passed keys are not present in the map they are ignored
	 * @param <T>
	 * @param map 		map to filter
	 * @param keys 		all the keys that should be included in the filtered map
	 * @return 			filtered map of the same type than the passed map
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, Object> filterMap(Map<String, T> map, String... keys) {
		try {
			Map<String, Object> result = map.getClass().getDeclaredConstructor().newInstance();
			for (String key : keys) {
				if (map.containsKey(key)) {
					result.put(key, map.get(key));
				}
			}
			return result;
		
		} catch (Exception e) {
			logger.error("error creating the filtered hash map: ", e);
			return null;
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													conversion map <-> object  											       //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * converts a map to a java object
	 * @param map 		a map containing the properties of the object
	 * @param clazz 	the class of the object that is created form the map
	 * @return 			object representing the passed map or null if an error occurred
	 */
	public static <T> T mapToObj(Map<String, Object> map, Class<T> clazz) {
		try {
			Gson gson = new Gson();
			JsonElement jsonElement = gson.toJsonTree(map);
			T pojo = gson.fromJson(jsonElement, clazz);
			return pojo;
			
		} catch (Exception e) {
			logger.error("failed to convert the map to an object: ", e);
			return null;
		}
	}
	
	
	/**
	 * converts a java object to a map
	 * @param pojo 		object that is converted to a map
	 * @return 			map that represents the passed object or null if an error occurred
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, Object> objToMap(T pojo) {
		try {
			Gson gson = new Gson();
			String jsonStr = gson.toJson(pojo); 
			Map<String, Object> map = gson.fromJson(jsonStr, Map.class);
			return map;
			
		} catch (Exception e) {
			logger.error("failed to convert the map to an object: ", e);
			return null;
		}
	}
}