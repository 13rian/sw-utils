package ch.wenkst.sw_utils;

public class BaseTest {
	private static boolean started = false;
	static {
	    if (!started) {
	    	started = true;
	    	runGlobalInitializations();
	    }
	}
	
	
	private static void runGlobalInitializations() {
		setLoggerFile();
	}
	
	private static void setLoggerFile() {
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
	}
}
