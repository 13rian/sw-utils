package ch.wenkst.sw_utils.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigParser {
	private static final Logger logger = LoggerFactory.getLogger(ConfigParser.class);
	
	protected Config config;
	
	
	public ConfigParser() {
		parseConfigFile();
	}
	

	protected void parseConfigFile() {
		config = ConfigFactory.load();
	}
	
	
	/**
	 * retrieves the configuration value with the passed key, if it is not present or cannot be 
	 * casted the passed default value will be returned
	 * @param key 			key of the configuration value
	 * @param defaultVal 	the default value that is returned if an error occurs
	 * @return 				the configuration value of the passed key or the default value;
	 */
	@SuppressWarnings("unchecked")
	public <T> T getConfigValue(String key, T defaultVal) {
		try {
			T result = (T) config.getAnyRef(key);
			return result;
			
		} catch (Exception e) {
			logger.warn("error reading the configuration value " + key + ", " + e.getMessage());
			return defaultVal;
		}
	}
}
