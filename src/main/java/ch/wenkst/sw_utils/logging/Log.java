package ch.wenkst.sw_utils.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
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


	private static final String consoleDateFormat = "HH:mm:ss.SSS";
	private static final String fileDateFromat = "dd.MM.yyyy HH:mm:ss.SSS"; 


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
	 * @param name 	the name of the logger
	 * @return
	 */
	public static Log getLogger(String name) {
		if (!isInitialized) {
			initLogger();
		}

		return getLogInstance(name);
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
	 * initializes the logger if the path to the logger configuration file is defined as a
	 * system property (logger.config.file.path)
	 */
	private static void initLogger() {
		String loggerConfigFile = System.getProperty("swutils.logger.config.file.path");
		if (loggerConfigFile == null) {
			initDefaultLogger();
			return;
		}

		initLogger(loggerConfigFile);
	}


	/**
	 * initializes the logger form the passed properties config file
	 * @param loggerConfigFile
	 */
	public static void initLogger(String loggerConfigFile) {
		removeAllHandlers();

		Properties props = new Properties();	
		try (InputStream loggerConfig = new FileInputStream(loggerConfigFile) ) {
			props.load(loggerConfig);	

		} catch (Exception e) {
			System.err.println("error parsing the logger config file: ");
			e.printStackTrace();
			initDefaultLogger();
			return;
		}

		initLogger(props);
	}


	/**
	 * initializes the logger from the passed properties
	 * @param props		properties with the logger configuration
	 */
	public static void initLogger(Properties props) {
		try {
			setupConsoleLogger(props);
			setupFileLogger(props);
			isInitialized = true;

		} catch (LogInitializationException e) {
			System.err.println("logger could not be initialized, reason: " + e.getMessage() + " initialize the default logger");
			initDefaultLogger();
		}
	}
	
	
	private static void setupConsoleLogger(Properties props) throws LogInitializationException {
		Level logLevel = extractConsoleLevel(props);
		boolean isLogLine = extractIsLogLineNumberInConsole(props);
		addConsoleLogHandler(logLevel, isLogLine);
	}
	
	
	private static Level extractConsoleLevel(Properties props) throws LogInitializationException {
		if (!props.containsKey("console.log.level")) {
			throw new LogInitializationException("console.log.level not configured");
		}
		
		String consoleLevelStr = props.getProperty("console.log.level");
		Level consoleLevel = levelFromString(consoleLevelStr);
		if (consoleLevel == null) {
			throw new LogInitializationException(consoleLevel + " is not a valid log level");
		}
		return consoleLevel;
	}
	
	
	private static boolean extractIsLogLineNumberInConsole(Properties props) {
		if (!props.containsKey("console.log.logline")) {
			return true;
		}
		
		String logLineStr = props.getProperty("console.log.logline");
		try {
			return Boolean.parseBoolean(logLineStr);
		} catch (Exception e) {
			System.err.println("error parsing the console.log.logline, activate the line number log");
			return true;
		}
	}
	
	
	private static void addConsoleLogHandler(Level logLevel, boolean logLineNumber) {
		Logger rootLogger = Logger.getLogger("");
		PrettyLogFormatter formatter = new PrettyLogFormatter(logLineNumber, consoleDateFormat);
		StreamHandlerFlush consoleHandler = new StreamHandlerFlush(System.out, formatter);
		consoleHandler.setLevel(logLevel);
		rootLogger.addHandler(consoleHandler);
	}
	
	
	private static void setupFileLogger(Properties props) {
		try {
			String logFilePath = setupLogFile(props);
			Level fileLevel = extractFileLogLevel(props);
			boolean fileLogLine = extractIsLogLineNumberInFile(props);
			FileHandler fileHandler = setupFileHandler(props, logFilePath);
			addFileLogHandler(fileHandler, fileLevel, fileLogLine);

		} catch (Exception e) {
			System.err.println("file logger could not be initialized");
			e.printStackTrace();
		}
	}
	
	
	private static String setupLogFile(Properties props) throws LogInitializationException {
		if (!props.containsKey("file.log.path")) {
			throw new LogInitializationException("file.log.path not configured");
		}
		
		String logFilePath = props.getProperty("file.log.path");
		logFilePath = Utils.getWorkDir() + File.separator + logFilePath; 
		File logFile = new File(logFilePath);
		File parentDir = logFile.getParentFile();
		parentDir.mkdirs();
		return logFilePath;
	}
	
	
	private static Level extractFileLogLevel(Properties props) {
		if (!props.containsKey("file.log.level")) {
			System.err.println("file log level not configured, set the level to info");
			return Level.INFO;
		}
		
		String fileLevelStr = props.getProperty("file.log.level");
		Level fileLevel = levelFromString(fileLevelStr);
		if (fileLevel == null) {
			System.err.println("file log level " + fileLevelStr + " is not a valid level, set file level to info");
			return Level.INFO;
		}
		
		return fileLevel;
	}
	
	
	
	private static boolean extractIsLogLineNumberInFile(Properties props) {
		try {
			String fileLogLineStr = props.getProperty("file.log.logline");
			return Boolean.parseBoolean(fileLogLineStr);
			
		} catch (Exception e) {
			System.err.println("error parsing the file.log.logline, activate the line number log");
			return true;
		}
	}
	
	
	private static FileHandler setupFileHandler(Properties props, String logFilePath) throws SecurityException, IOException {
		if (!props.containsKey("file.log.max.size") || !props.containsKey("file.log.max.count")) {
			return new FileHandler(logFilePath, true);
		}

		try {
			String fileSizeStr = props.getProperty("file.log.max.size");
			int fileSize = Integer.parseInt(fileSizeStr);
			String fileCountStr = props.getProperty("file.log.max.count");
			int fileCount = Integer.parseInt(fileCountStr);
			return new FileHandler(logFilePath, fileSize, fileCount, true);

		} catch (Exception e) {
			System.err.println("error parsing the file.log.max.size and the file.log.max.count, file rolling over deactivated");
			return new FileHandler(logFilePath, true);
		}
	}
	
	
	private static void addFileLogHandler(FileHandler fileHandler, Level logLevel, boolean logLineNumber) {
		Logger rootLogger = Logger.getLogger("");
		fileHandler.setFormatter(new PrettyLogFormatter(logLineNumber, fileDateFromat));
		fileHandler.setLevel(logLevel);
		rootLogger.addHandler(fileHandler);
		rootLogger.setLevel(Level.FINEST);
	}


	/**
	 * initializes the default logger, no log file is written and the level of the console log is info
	 */
	private static void initDefaultLogger() {
		System.err.println("initialize the default logger, only console log-level is set to info");
		removeAllHandlers();
		Logger rootLogger = Logger.getLogger("");
		StreamHandlerFlush consoleHandler = new StreamHandlerFlush(System.out, new PrettyLogFormatter(true, consoleDateFormat));
		consoleHandler.setLevel(Level.INFO);
		rootLogger.addHandler(consoleHandler);
		rootLogger.setLevel(Level.FINEST);
		isInitialized = true;
	}


	/**
	 * removes all handlers form the logger
	 */
	private static void removeAllHandlers() {
		Logger rootLogger = Logger.getLogger("");
		for (Handler handler : rootLogger.getHandlers()) {
			rootLogger.removeHandler(handler);
		}
	}



	/**
	 * sets the level of the logger
	 * @param level 	level of the logger to set
	 */
	public void setLevel(Level level) {
		logger.setLevel(level);
	}


	/**
	 * disables the logger of the passed names, note that if you disable org.test, org.test.test1 will also be disabled
	 * @param names 	the names of the logger to disable
	 */
	public static void disableLoggers(String... names) {
		for (String name : names) {
			Logger bcLogger = Logger.getLogger(name);
			bcLogger.setLevel(Level.OFF);
		}
	}



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 									simply log one message 													  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 									log the stack trace as well												  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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


	///////////////////////////////////////////////////////////////////////////////////////////////
	// 									helper methods 											 //
	///////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * returns the log level form the passed string or null if the level is not defined
	 * @param levelStr 	the level of the logger
	 * @return
	 */
	private static Level levelFromString(String levelStr) {
		String level = levelStr.toLowerCase().trim();
		switch (level) {
		case "off" :
			return Level.OFF;

		case "finest" :
			return Level.FINEST;

		case "finer" :
			return Level.FINER;

		case "fine" :
			return Level.FINE;

		case "config" :
			return Level.CONFIG;

		case "info" :
			return Level.INFO;

		case "warning" :
			return Level.WARNING;

		case "severe" :
			return Level.SEVERE;

		default : 
			return null;
		}
	}
}
