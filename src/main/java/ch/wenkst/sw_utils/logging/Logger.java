package ch.wenkst.sw_utils.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

public class Logger {
	// defines the logger instance 
	private java.util.logging.Logger logger = java.util.logging.Logger.getLogger("main");
	
	// flag that indicates if the logger is initialized or not
	private static boolean isInitialized = false;
	
	
	/**
	 * private constructor to create a new logger instance
	 * @param name 	the name of the logger
	 */
	private Logger(String name) {
		// this method returns the same logger instance if called twice with the same name
		logger = java.util.logging.Logger.getLogger(name);
	}
	
	
	/**
	 * returns a new logger instance
	 * @param name 	the name of the logger
	 * @return
	 */
	public static Logger getLogger(String name) {
		// initialize the logger if not already initialized
		if (!isInitialized) {
			initLogger();
		}
		
		return new Logger(name);
	}
	
	
	/**
	 * returns a new logger instance
	 * @param clazz 	the class in which the logger is used (is used as name of the logger)
	 * @return
	 */
	public static Logger getLogger(Class<?> clazz) {
		String name = clazz.getSimpleName();
		return getLogger(name);
	}
	
	
	
	/**
	 * initializes the logger, log messages are printed to a file and to the console
	 * @param loggerConfigFile 		the path to the properties file that contains the config values for the logger
	 * @param logFilePath 			the path to the log-file
	 * @return 						true if the logger could be successfully initialized, false if an error occurred
	 */
	public static boolean initLogger(String loggerConfigFile, String logFilePath) {
		// define all the properties that need to be present in the logger config file
		String[] propsList = {
				"console.log.level", 
				"console.log.logline",
				"file.log.level",
				"file.log.logline",
				"file.log.max.size", 
				"file.log.max.count"
		};		
		
		
		// parse the logger config file
		Properties props = new Properties();	
		try (InputStream loggerConfig = new FileInputStream(loggerConfigFile) ) {
			// load a properties file
			props.load(loggerConfig);
			
			// check if all properties are present
			String missingProp = allPropsPresent(props, propsList);
			if (missingProp != null) {
				System.err.println("failed to initialize the logger, config property key " + missingProp + " is missing.");
			}
			

			// read out the configuration properties file of the logger
			Level consoleLogLevel = ch.wenkst.sw_utils.logging.Level.getLevelFromString(props.getProperty("console.log.level"));
			boolean consoleLogLine = Boolean.parseBoolean(props.getProperty("console.log.logline"));
			Level fileLogLevel = ch.wenkst.sw_utils.logging.Level.getLevelFromString(props.getProperty("file.log.level"));
			boolean fileLogLine = Boolean.parseBoolean(props.getProperty("file.log.logline"));
			int maxFileSize = Integer.parseInt(props.getProperty("file.log.max.size"));
			int maxFileCount = Integer.parseInt(props.getProperty("file.log.max.count"));

			
			// initialize the logger
			initLogger(consoleLogLevel, consoleLogLine, fileLogLevel, fileLogLine, logFilePath, maxFileCount, maxFileSize);
			
	

		} catch (IOException ex) {
			System.err.println("logger could not be initialized: ");
			ex.printStackTrace();
			System.err.println("terminate the program");
			return false;			
		}
		
		return true;
	}
	
	
	/**
	 * initializes the logger, log messages are only printed to the console
	 * @param loggerConfigFile 		the path to the properties file that contains the config values for the logger
	 * @param logFilePath 			the path to the log-file
	 * @return 						true if the logger could be successfully initialized, false if an error occurred
	 */
	public static boolean initLogger(String loggerConfigFile) {
		// define all the properties that need to be present in the logger config file
		String[] propsList = {
				"console.log.level", 
				"console.log.logline",
		};		
		
		
		// parse the logger config file
		Properties props = new Properties();	
		try (InputStream loggerConfig = new FileInputStream(loggerConfigFile) ) {
			// load a properties file
			props.load(loggerConfig);
			
			// check if all properties are present
			// check if all properties are present
			String missingProp = allPropsPresent(props, propsList);
			if (missingProp != null) {
				System.err.println("failed to initialize the logger, config property key " + missingProp + " is missing.");
				return false;
			}
			

			// read out the configuration properties file of the logger
			Level consoleLogLevel = ch.wenkst.sw_utils.logging.Level.getLevelFromString(props.getProperty("console.log.level"));
			boolean consoleLogLine = Boolean.parseBoolean(props.getProperty("console.log.logline"));

			
			// initialize the logger
			initLogger(consoleLogLevel, consoleLogLine);
			
	

		} catch (IOException ex) {
			System.err.println("logger could not be initialized: ");
			ex.printStackTrace();
			System.err.println("terminate the program");
			return false;			
		}
		
		return true;
	}
	
	
	
	/**
	 * initializes the logger if the path to the logger configuration file is defined as a
	 * system property (logger.config.file.path)
	 */
	private static void initLogger() {
		// check if the path of the properties file is set in the system properties
		String loggerConfigFile = System.getProperty("logger.config.file.path");
		if (loggerConfigFile == null) {
			return;
		}
		
		// define all the properties that need to be present in the logger config file
		String[] propsList = {
				"file.log.path",
		};		
		
		
		// parse the logger config file
		Properties props = new Properties();	
		try (InputStream loggerConfig = new FileInputStream(loggerConfigFile) ) {
			// load a properties file
			props.load(loggerConfig);
			
			// check if all properties are present
			String missingProp = allPropsPresent(props, propsList);
			if (missingProp != null) {
				System.err.println("failed to initialize the logger, config property key " + missingProp + " is missing.");
			}
			isInitialized = true;
	

		} catch (IOException ex) {
			System.err.println("logger could not be initialized: ");
			ex.printStackTrace();
			System.err.println("terminate the program");			
		}
		
		// initialize the logger
		String logFilePath = System.getProperty("user.dir") + File.separator + props.getProperty("file.log.path");
		initLogger(loggerConfigFile, logFilePath);
	}
	
	
	
	/**
	 * initializes the logger, i.e. sets the handlers for the file and the console
	 * @param consoleLogLevel 		the log level of the console logger
	 * @param consoleLogLine 		true if the line of the logger call should be logged onto the console
	 * @param fileLogLevel 			the log level of the file logger
	 * @param fileLogLine 			true if the line of the logger call should be logged into the file
	 * @param logFilePath 			the path of the log file
	 * @param fileCount 			the number of log files to keep
	 * @param fileSize 				the approximate size of a file in bytes before a new log-file is started
	 * @throws IOException  		
	 * @throws SecurityException 
	 */
	private static void initLogger(Level consoleLogLevel, boolean consoleLogLine, Level fileLogLevel, boolean fileLogLine, String logFilePath, int fileCount, int fileSize) throws SecurityException, IOException {
		// ensure that all the directories of the logFilePath are created
		File logFile = new File(logFilePath);
		File parentDir = logFile.getParentFile();
		parentDir.mkdirs();
		
		// remove the default console handler
		java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }
        
        
        // add the new console handler to log to the console
        StreamHandlerFlush consoleHandler = new StreamHandlerFlush(System.out, new ConsoleFormatter(consoleLogLine));
        consoleHandler.setLevel(consoleLogLevel);
        rootLogger.addHandler(consoleHandler);
        
        
        // add a file hander to log to a file
        FileHandler fileHandler = new FileHandler(logFilePath, fileSize, fileCount, true);
        fileHandler.setFormatter(new FileFormatter(fileLogLine));
        fileHandler.setLevel(fileLogLevel);
        rootLogger.addHandler(fileHandler);
        
        
        // set the level of the root logger
        rootLogger.setLevel(Level.FINEST);
        
        isInitialized = true;
	}
	
	
	
	/**
	 * initializes the logger, i.e. sets the handlers for the console
	 * @param consoleLogLevel 		the log level of the console logger
	 * @param consoleLogLine 		true if the line of the logger call should be logged onto the console
	 */
	private static void initLogger(Level consoleLogLevel, boolean consoleLogLine) {
		// remove the default console handler
		java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }
        
        
        // add the new console handler to log to the console
        StreamHandlerFlush consoleHandler = new StreamHandlerFlush(System.out, new ConsoleFormatter(consoleLogLine));
        consoleHandler.setLevel(consoleLogLevel);
        rootLogger.addHandler(consoleHandler);
        
        
        // set the level of the root logger
        rootLogger.setLevel(Level.FINEST);
        
        isInitialized = true;
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
	public void trace(String msg) {
		logger.finer(msg);
	}
	
	public void debug(String msg) {
		logger.fine(msg);
	}
	
	public void info(String msg) {
		logger.info(msg);
	}
	
	public void warn(String msg) {
		logger.warning(msg);
	}
	
	public void error(String msg) {
		logger.severe(msg);
	}
	
	public void fatal(String msg) {
		logger.severe(msg);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 									log the stack trace as well												  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void trace(String msg, Throwable th) {
		logger.finer(msg + exceptionToString(th));
	}
	
	public void debug(String msg, Throwable th) {
		logger.fine(msg + exceptionToString(th));
	}
	
	public void info(String msg, Throwable th) {
		logger.info(msg + exceptionToString(th));
	}
	
	public void warn(String msg, Throwable th) {
		logger.warning(msg + exceptionToString(th));
	}
	
	public void error(String msg, Throwable th) {
		logger.severe(msg + exceptionToString(th));
	}
	
	public void fatal(String msg, Throwable th) {
		logger.severe(msg + exceptionToString(th));
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											helper methods 													   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * checks if all passed properties are present in the properties file
	 * @param props 		the properties file
	 * @param property 		properties that should be present in the properties file
	 * @return 				null if no property is missing, or the name of the first property that is missing
	 */
	private static String allPropsPresent(Properties props, String... property) {
		// check if all properties are present
		for (String prop : property) {
			if (!props.containsKey(prop)) {
				return prop;
			}
		}
		
		// all properties present
		return null;
	}
	
	
	/**
	 * returns the stack trace of the passed exception as string
	 * @param e 	an exception
	 * @return 		the stack trace of the passed exception as string
	 */
	private String exceptionToString(Throwable e) {		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw); 						// print the stack trace to the specified printer
		String result = sw.toString(); 				// get the stack trace from the printer
		return result;
	}


	
}
