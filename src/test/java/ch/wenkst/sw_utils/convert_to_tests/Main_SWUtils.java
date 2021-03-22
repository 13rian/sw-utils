package ch.wenkst.sw_utils.convert_to_tests;
import java.io.File;
import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.logging.Log;

public class Main_SWUtils {
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
