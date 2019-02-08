package ch.wenkst.sw_utils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.crypto.CryptoUtils;
import ch.wenkst.sw_utils.file.FileUtils;
import ch.wenkst.sw_utils.logging.Log;
import ch.wenkst.sw_utils.map.MapUtils;

public class Main_SWUtils {
//	// define a class initializer that is executed before any other properties and classes are loaded (since it is the 
//	// initializer for the main program). It sets the needed system property for the logger
//	// no need to set the JVM arguments anymore in the Debug Configurations
	static {
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
	}
	
	private static final Logger logger = LoggerFactory.getLogger(Main_SWUtils.class);

	public static void main(String[] args) {		
		/////////////////////////////////////////////////////////////////////////////////////////////
		// 										test the logger 								   //
		/////////////////////////////////////////////////////////////////////////////////////////////
		System.err.println("gg");
		Log log = Log.getLogger(Main_SWUtils.class);
		log.config("config log");
		log.info("info log");
		log.severe("severe test");
	
		String loggerConfig = "resource" + File.separator + "log" + File.separator + "log_config.properties";
		Log.initLogger(loggerConfig);
		log.config("config log");
		log.info("info log");
		log.severe("severe test");
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 										test Security Utils									   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
	
		
		
		// print all registered providers
		logger.info(CryptoUtils.getRegisteredProviders());
		
		// print the default providers
		logger.info(CryptoUtils.getDefaultProviders());
		
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 											utils 													   //
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// startup message
		Utils.logStartupMessage();
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 											new methods 											   //
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		String fileExtension = FileUtils.fileExtension("hallo/test.qq");
		System.out.println("file-extension: " + fileExtension);
		
		String rawName = FileUtils.rawFileName("hallo/test");
		System.out.println("file-extension: " + rawName);
		
		
		Map<String, Object> hm = new HashMap<>();
		hm.put("name", "Temperature");
		hm.put("timestamp", 1532274524000L);
		hm.put("value", 29.3);
		Data data = MapUtils.mapToObj(hm, Data.class);
		
		
		Map<String, Object> dataHm = MapUtils.objToMap(data);
		


		
		logger.info("end of main test routine reached");
	}
	
	
	
}
