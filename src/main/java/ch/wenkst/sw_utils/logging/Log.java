package ch.wenkst.sw_utils.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.wenkst.sw_utils.Utils;

/**
 * wrapper around the java.util.logger
 * if the logger is initialized without a configuration file the, the relative path to the working
 * directory needs to be set as system property called swutils.logger.config.file.path
 * <p>
 * sometimes the locale is not defined and the new SimpleDateFormat() call will log something on the 
 * PlatformLogger. If this is the case a StackOverflowError error will occur. To avoid this set the default locale:
 * <ul>
 * <li> Locale locale = new Locale("de", "DE");
 * <li> Locale.setDefault(locale);
 * <p>
 * the following properties can be defined in the properties config file:
 * <ul> 
 * <li> console.log.level 		console log level, needs to be a valid java.util.level 
 * <li> console.log.logline 	true if the line number should be logged to the console, false if not
 * <li> file.log.path 			relative path to the working dir for the log-file
 * <li> file.log.level 			file log level, needs to be a valid java.util.level
 * <li> file.log.logline 		true if the line number should be logged in the file, false if not
 * <li> file.log.max.size 		the maximal size in bytes one log-file should have
 * <li> file.log.max.count 		the maximal number of log file that are writtens
 */
public class Log {
	private static boolean isInitialized = false;

	private Logger logger = null;
	private static Map<String, Log> loggerInstanceMap = new HashMap<>();


	/**
	 * private constructor to create a new logger instance
	 * @param name 	the name of the logger
	 */
	private Log(String name) {
		logger = Logger.getLogger(name);
	}
	
	
	/**
	 * returns a Log instance if the log with the passed name was not used. if the Log instance
	 * with the passed name was already used the same instance will be returned	 
	 * @param clazz 	the class in which the logger is used (is used as name of the logger)
	 * @return
	 */
	public static Log getLogger(Class<?> clazz) {
		String name = clazz.getSimpleName();
		return getLogger(name);
	}


	/**
	 * returns a Log instance if the log with the passed name was not used. if the Log instance
	 * with the passed name was already used the same instance will be returned
	 * @param name 	the name of the logger
	 * @return
	 */
	public static Log getLogger(String name) {
		if (!isInitialized) {
			new LogConfigurator().init();
			isInitialized = true;
		}

		return getLogInstance(name);
	}	
	

	/**
	 * returns a new log instance if the log instance with the passed name was not used so far.
	 * if the log instance with the passed name was already used it will be returned
	 * @param name 	name of the log instance
	 * @return
	 */
	private static Log getLogInstance(String name) {
		Log log = loggerInstanceMap.get(name);
		if (log == null) {
			log = new Log(name);
			loggerInstanceMap.put(name, log);
			return log;
		} 

		return log;
	}
	
	
	
	/**
	 * initializes the logger form the passed properties config file
	 * @param loggerConfigFile
	 */
	public static void initFromFile(String loggerConfigFile) {
		new LogConfigurator().initFromFile(loggerConfigFile);
		isInitialized = true;
	}


	/**
	 * initializes the logger from the passed properties
	 * @param props		properties with the logger configuration
	 */
	public static void initFromProperties(Properties props) {
		new LogConfigurator().initFromProperties(props);
		isInitialized = true;
	}


	/**
	 * sets the level of the logger
	 * @param level 	level of the logger to set
	 */
	public void setLevel(Level level) {
		logger.setLevel(level);
	}


	/**
	 * disables the logger of the passed names, note that if you disable org.test,
	 * org.test.test1 will also be disabled
	 * @param names 	the names of the logger to disable
	 */
	public static void disableLoggers(String... names) {
		for (String name : names) {
			Logger bcLogger = Logger.getLogger(name);
			bcLogger.setLevel(Level.OFF);
		}
	}



	public void finest(String msg) {
		logger.finest(msg);
	}

	public void finer(String msg) {
		logger.finer(msg);
	}

	public void fine(String msg) {
		logger.fine(msg);
	}

	public void config(String msg) {
		logger.config(msg);
	}

	public void info(String msg) {
		logger.info(msg);
	}

	public void warning(String msg) {
		logger.warning(msg);
	}

	public void severe(String msg) {
		logger.severe(msg);
	}



	public void finest(String msg, Throwable th) {
		logger.finest(msg + Utils.exceptionToString(th));
	}

	public void finer(String msg, Throwable th) {
		logger.finer(msg + Utils.exceptionToString(th));
	}

	public void fine(String msg, Throwable th) {
		logger.fine(msg + Utils.exceptionToString(th));
	}

	public void config(String msg, Throwable th) {
		logger.config(msg + Utils.exceptionToString(th));
	}

	public void info(String msg, Throwable th) {
		logger.info(msg + Utils.exceptionToString(th));
	}

	public void warning(String msg, Throwable th) {
		logger.warning(msg + Utils.exceptionToString(th));
	}

	public void severe(String msg, Throwable th) {
		logger.severe(msg + Utils.exceptionToString(th));
	}
}
