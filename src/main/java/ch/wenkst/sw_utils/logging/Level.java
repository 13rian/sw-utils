package ch.wenkst.sw_utils.logging;

/**
 * defines the possible log levels
 */
public class Level {
	public static java.util.logging.Level OFF = java.util.logging.Level.OFF; 				// nothing is logged
	public static java.util.logging.Level TRACE = java.util.logging.Level.FINER;
	public static java.util.logging.Level DEBUG = java.util.logging.Level.FINE;
	public static java.util.logging.Level INFO = java.util.logging.Level.INFO;
	public static java.util.logging.Level WARN = java.util.logging.Level.WARNING;
	public static java.util.logging.Level ERROR = java.util.logging.Level.SEVERE;
	public static java.util.logging.Level FATAL = java.util.logging.Level.SEVERE;
	
	
	/**
	 * returns the level for the logger form the passe string level
	 * @param strLevel 		the logger level
	 * @return
	 */
	public static java.util.logging.Level getLevelFromString(String strLevel) {
		String level = strLevel.toLowerCase();
		
		switch (level) {
			case "off":
				return OFF;
			case "trace":
				return TRACE;
			case "debug":
				return DEBUG;
			case "info":
				return INFO;
			case "warn":
				return WARN;
			case "error":
				return ERROR;
			case "fatal":
				return FATAL;
			default:
				System.out.println("the log level " + level + " was not found, use the info level");
				return INFO;
		}
	}
}
