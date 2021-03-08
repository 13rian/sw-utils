package ch.wenkst.sw_utils.miscellaneous;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataStorage {
	private static final Logger logger = LoggerFactory.getLogger(DataStorage.class);
	
	private static DataStorage instance = null;
	
	private ConcurrentMap<Object, Object> dataMap;
	

	protected DataStorage() {
		dataMap = new ConcurrentHashMap<>();
	}


	/**
	 * data storage with key value pairs
	 * @return 		singleton instance
	 */
	public static DataStorage getInstance() {
		if (instance == null) {
			instance = new DataStorage();
		}
		return instance;
	}
	
	
	/**
	 * adds an object to the data storage
	 * @param key  		the key of the object			
	 * @param value 	the value to store
	 */
	public void addData(Object key, Object value) {
		Object previousVal = dataMap.put(key, value);
		if (previousVal != null) {
			logger.debug("data with the key " + key + " was already present and overwritten with the new value");
		}
	}
	
	
	/**
	 * removes the object with the passed key form the data storage
	 * @param key  		the key of the object			
	 */
	public void removeData(Object key) {
		Object previousVal = dataMap.remove(key);
		if (previousVal == null) {
			logger.debug("data with the key " + key + " was not present in the data storage");
		}
	}
	
	
	/**
	 * returns the value associated with the passed key or null if the key is not present
	 * @param key 	the key of the value to return
	 * @return 		stored data object
	 */
	@SuppressWarnings("unchecked")
	public <T> T getData(Object key) {
		return (T) dataMap.get(key);
	}
}