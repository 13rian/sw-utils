package ch.wenkst.sw_utils.convert_to_tests;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.file.FileUtils;
import ch.wenkst.sw_utils.logging.Log;
import ch.wenkst.sw_utils.map.Data;
import ch.wenkst.sw_utils.map.MapUtils;

public class Main_SWUtils {
//	// define a class initializer that is executed before any other properties and classes are loaded (since it is the 
//	// initializer for the main program). It sets the needed system property for the logger
//	// no need to set the JVM arguments anymore in the Debug Configurations
	static {
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
	}


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
		Log.initFromFile(loggerConfig);
		log.config("config log");
		log.info("info log");
		log.severe("severe test");
		
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 											utils 													   //
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// startup message
		Utils.logStartupMessage();
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 											new methods 											   //
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
	
		
		log.info("end of main test routine reached");
	}
}
