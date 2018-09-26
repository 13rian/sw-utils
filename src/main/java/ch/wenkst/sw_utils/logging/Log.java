package ch.wenkst.sw_utils.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
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
 * the following properties can be defined in the properties config file:
 * <ul> 
 * <li> console.log.level 		console log level, needs to be a valid java.util.level 
 * <li> console.log.logline 	true if the line number should be logged to the console, false if not
 * <li> file.log.path 			relative path to the working dir for the log-file
 * <li> file.log.level 			file log leve, needs to be a valid java.util.level
 * <li> file.log.logline 		true if the line number should be logged in the file, false if not
 * <li> file.log.max.size 		the maximal size in bytes one log-file should have
 * <li> file.log.max.count 		the maximal number of log file that are writtens
 */
public class Log {
	// flag that indicates if the logger is initialized or not
	private static boolean isInitialized = false;
	
	private Logger logger = null; 					// jul logger instance
	
	// contains a map with all the Log instances
	private static HashMap<String, Log> loggerMap = new HashMap<>();
			
			
	/**
	 * private constructor to create a new logger instance
	 * @param name 	the name of the logger
	 */
	private Log(String name) {
		// this method returns the same logger instance if called twice with the same name
		logger = Logger.getLogger(name);
	}
	
	
	/**
	 * returns a Log instance if the log with the passed name was not used. if the Log instance
	 * with the passed name was already used the same instance will be returned
	 * @param name 	the name of the logger
	 * @return
	 */
	public static Log getLogger(String name) {
		// initialize the logger if not already initialized
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
		Log log = loggerMap.get(name);
		if (log == null) {
			log = new Log(name);
			loggerMap.put(name, log);
			return log;
		
		} 
		
		return log;
	}
	

	
	/**
	 * initializes the logger if the path to the logger configuration file is defined as a
	 * system property (logger.config.file.path)
	 */
	private static void initLogger() {
		// check if the path of the properties file is set in the system properties
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
		// remove all pre-configured handlers from the logger 
		removeAllHandlers();
		
		// open the properties file
		Properties props = new Properties();	
		try (InputStream loggerConfig = new FileInputStream(loggerConfigFile) ) {
			props.load(loggerConfig);	

		} catch (Exception e) {
			System.err.println("error parsing the logger config file: ");
			e.printStackTrace();
			initDefaultLogger();
			return;
		}
		
		
		// ----------------------------- initialize the console logger
		// get the console level
		String consoleLevelStr = props.getProperty("console.log.level");
		if (consoleLevelStr == null) {
			System.err.println("console.log.level not configured");
			initDefaultLogger();
			return;
		}
		Level consoleLevel = levelFromString(consoleLevelStr);
		if (consoleLevel == null) {
			System.err.println("console.log.level " + consoleLevelStr + " is not a valid level");
			initDefaultLogger();
			return;
		}
		
		// get the flag if the line number should be logged to the console
		String consoleLogLineStr = props.getProperty("console.log.logline");
		boolean consoleLogLine;
		if (consoleLogLineStr == null) {
			consoleLogLine = true;
		} else {
			try {
				consoleLogLine = Boolean.parseBoolean(consoleLogLineStr);
			} catch (Exception e) {
				System.err.println("error parsing the console.log.logline, activate the line number log");
				consoleLogLine = true;
			}
		}
		
        // add the new console handler to log to the console
		Logger rootLogger = Logger.getLogger("");
        StreamHandlerFlush consoleHandler = new StreamHandlerFlush(System.out, new ConsoleFormatter(consoleLogLine));
        consoleHandler.setLevel(consoleLevel);
        rootLogger.addHandler(consoleHandler);
		
		
		// ------------------------------- initialize the file logger
		// get the path of the log file
        String logFilePath = props.getProperty("file.log.path");
        if (logFilePath == null) {
        	System.err.println("log file path not not configured, ignore file logger");
        	return;
        }
        
        
        // ensure that all the directories of the logFilePath are created
        logFilePath = System.getProperty("user.dir") + File.separator + logFilePath; 
		File logFile = new File(logFilePath);
		File parentDir = logFile.getParentFile();
		parentDir.mkdirs();
        
        // get the file level
		String fileLevelStr = props.getProperty("file.log.level");
		Level fileLevel = Level.INFO;
		if (fileLevelStr == null) {
			System.err.println("file log level not configured, set the level to info");
		} else {
			fileLevel = levelFromString(consoleLevelStr);
			if (fileLevel == null) {
				System.err.println("file log level " + fileLevelStr + " is not a valid level, set file level to info");
				fileLevel = Level.INFO;
			}
		}
		
		// get the flag if the line number should be logged
		String fileLogLineStr = props.getProperty("file.log.logline");
		boolean fileLogLine;
		if (fileLogLineStr == null) {
			fileLogLine = true;
		} else {
			try {
				fileLogLine = Boolean.parseBoolean(fileLogLineStr);
			} catch (Exception e) {
				System.err.println("error parsing the file.log.logline, activate the line number log");
				fileLogLine = true;
			}
		}
        
        
        // get the size limit and the file count and create the file handler
		FileHandler fileHandler;
        String fileSizeStr = props.getProperty("file.log.max.size");
        String fileCountStr = props.getProperty("file.log.max.count");
        try {
        	if (fileSizeStr == null || fileSizeStr == null) {
        		fileHandler = new FileHandler(logFilePath, true);

        	} else {
        		try {
        			long fileSize = Long.parseLong(fileSizeStr);
        			int fileCount = Integer.parseInt(fileCountStr);
        			fileHandler = new FileHandler(logFilePath, fileSize, fileCount, true);

        		} catch (Exception e) {
        			System.err.println("error parsing the file.log.max.size and the file.log.max.count, file rolling over deactivated");
        			fileHandler = new FileHandler(logFilePath, true);
        		}
        	}
        
        } catch (Exception e) {
        	System.err.print("error setting up the file handler for the logger");
        	e.printStackTrace();
        	initDefaultLogger();
        	return;
        }
        
        // configure the file handler
        fileHandler.setFormatter(new FileFormatter(fileLogLine));
        fileHandler.setLevel(fileLevel);
        rootLogger.addHandler(fileHandler);
        
        
        // set the level of the root logger
        rootLogger.setLevel(Level.FINEST);
        
        isInitialized = true;
	}
	
	
	/**
	 * initializes the default logger, no log file is written and the level of the console log is info
	 */
	private static void initDefaultLogger() {
		System.err.println("initialize the default logger, only console log-level is set to info");
		
		// remove all pre-configured handlers from the logger 
		removeAllHandlers();
		
		Logger rootLogger = Logger.getLogger("");
        StreamHandlerFlush consoleHandler = new StreamHandlerFlush(System.out, new ConsoleFormatter(true));
        consoleHandler.setLevel(Level.INFO);
        rootLogger.addHandler(consoleHandler);
        
        // set the level of the root logger
        rootLogger.setLevel(Level.FINEST);
        
        isInitialized = true;
	}
	
	
	/**
	 * removes all handlers form the logger
	 */
	private static void removeAllHandlers() {
		// remove the default console handler
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
