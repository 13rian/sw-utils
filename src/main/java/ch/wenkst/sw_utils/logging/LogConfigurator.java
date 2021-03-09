package ch.wenkst.sw_utils.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.wenkst.sw_utils.Utils;

public class LogConfigurator {
	
	/**
	 * initializes the logger if the path to the logger configuration file is defined as a
	 * system property (logger.config.file.path)
	 */
	public void init() {
		String loggerConfigFile = System.getProperty(LogConfigConstants.logConfigFilePath);
		if (loggerConfigFile == null) {
			initDefaultLogger();
			return;
		}

		initFromFile(loggerConfigFile);
	}


	/**
	 * initializes the logger form the passed properties config file
	 * @param loggerConfigFile
	 */
	public void initFromFile(String loggerConfigFile) {
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

		initFromProperties(props);
	}


	/**
	 * initializes the logger from the passed properties
	 * @param props		properties with the logger configuration
	 */
	public void initFromProperties(Properties props) {
		try {
			setupConsoleLogger(props);
			setupFileLogger(props);

		} catch (LogInitializationException e) {
			System.err.println("logger could not be initialized, reason: " + e.getMessage() + " initialize the default logger");
			initDefaultLogger();
		}
	}
	
	
	/**
	 * initializes the default logger, no log file is written and the level of the console log is info
	 */
	private void initDefaultLogger() {
		System.err.println("initialize the default logger, only console log-level is set to info");
		removeAllHandlers();
		Logger rootLogger = Logger.getLogger("");
		StreamHandlerFlush consoleHandler = new StreamHandlerFlush(System.out, new PrettyLogFormatter(true, LogConfigConstants.consoleDateFormat));
		consoleHandler.setLevel(Level.INFO);
		rootLogger.addHandler(consoleHandler);
		rootLogger.setLevel(Level.FINEST);
	}
	
	
	private void setupConsoleLogger(Properties props) throws LogInitializationException {
		Level logLevel = extractConsoleLevel(props);
		boolean isLogLine = extractIsLogLineNumberInConsole(props);
		addConsoleLogHandler(logLevel, isLogLine);
	}
	
	
	private Level extractConsoleLevel(Properties props) throws LogInitializationException {
		if (!props.containsKey(LogConfigConstants.logLevelConsole)) {
			throw new LogInitializationException(LogConfigConstants.logLevelConsole + " is not configured");
		}
		
		String consoleLevelStr = props.getProperty(LogConfigConstants.logLevelConsole);
		Level consoleLevel = levelFromString(consoleLevelStr);
		if (consoleLevel == null) {
			throw new LogInitializationException(consoleLevel + " is not a valid log level");
		}
		return consoleLevel;
	}
	
	
	private boolean extractIsLogLineNumberInConsole(Properties props) {
		if (!props.containsKey(LogConfigConstants.logLineNumberConsole)) {
			return true;
		}
		
		String logLineStr = props.getProperty(LogConfigConstants.logLineNumberConsole);
		try {
			return Boolean.parseBoolean(logLineStr);
		} catch (Exception e) {
			System.err.println("error parsing the " + LogConfigConstants.logLineNumberConsole + ", activate the line number log");
			return true;
		}
	}
	
	
	private void addConsoleLogHandler(Level logLevel, boolean logLineNumber) {
		Logger rootLogger = Logger.getLogger("");
		PrettyLogFormatter formatter = new PrettyLogFormatter(logLineNumber, LogConfigConstants.consoleDateFormat);
		StreamHandlerFlush consoleHandler = new StreamHandlerFlush(System.out, formatter);
		consoleHandler.setLevel(logLevel);
		rootLogger.addHandler(consoleHandler);
	}
	
	
	private void setupFileLogger(Properties props) {
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
	
	
	private String setupLogFile(Properties props) throws LogInitializationException {
		if (!props.containsKey(LogConfigConstants.logFile)) {
			throw new LogInitializationException(LogConfigConstants.logFile + " not configured");
		}
		
		String logFilePath = props.getProperty(LogConfigConstants.logFile);
		logFilePath = Utils.getWorkDir() + File.separator + logFilePath; 
		File logFile = new File(logFilePath);
		File parentDir = logFile.getParentFile();
		parentDir.mkdirs();
		return logFilePath;
	}
	
	
	private Level extractFileLogLevel(Properties props) {
		if (!props.containsKey(LogConfigConstants.logLevelFile)) {
			System.err.println(LogConfigConstants.logLevelFile + " not configured, set the level to info");
			return Level.INFO;
		}
		
		String fileLevelStr = props.getProperty(LogConfigConstants.logLevelFile);
		Level fileLevel = levelFromString(fileLevelStr);
		if (fileLevel == null) {
			System.err.println("file log level " + fileLevelStr + " is not a valid level, set file level to info");
			return Level.INFO;
		}
		
		return fileLevel;
	}
	
	
	
	private boolean extractIsLogLineNumberInFile(Properties props) {
		try {
			String fileLogLineStr = props.getProperty(LogConfigConstants.logLineNumberFile);
			return Boolean.parseBoolean(fileLogLineStr);
			
		} catch (Exception e) {
			System.err.println("error parsing the " + LogConfigConstants.logLineNumberFile + " property, activate the line number log");
			return true;
		}
	}
	
	
	private FileHandler setupFileHandler(Properties props, String logFilePath) throws SecurityException, IOException {
		if (!props.containsKey(LogConfigConstants.maxSizeFile) || !props.containsKey(LogConfigConstants.maxCountFile)) {
			return new FileHandler(logFilePath, true);
		}

		try {
			String fileSizeStr = props.getProperty(LogConfigConstants.maxSizeFile);
			int fileSize = Integer.parseInt(fileSizeStr);
			String fileCountStr = props.getProperty(LogConfigConstants.maxCountFile);
			int fileCount = Integer.parseInt(fileCountStr);
			return new FileHandler(logFilePath, fileSize, fileCount, true);

		} catch (Exception e) {
			System.err.println("error parsing the file.log.max.size and the file.log.max.count, file rolling over deactivated");
			return new FileHandler(logFilePath, true);
		}
	}
	
	
	private void addFileLogHandler(FileHandler fileHandler, Level logLevel, boolean logLineNumber) {
		Logger rootLogger = Logger.getLogger("");
		fileHandler.setFormatter(new PrettyLogFormatter(logLineNumber, LogConfigConstants.fileDateFromat));
		fileHandler.setLevel(logLevel);
		rootLogger.addHandler(fileHandler);
		rootLogger.setLevel(Level.FINEST);
	}


	/**
	 * removes all handlers form the logger
	 */
	private void removeAllHandlers() {
		Logger rootLogger = Logger.getLogger("");
		for (Handler handler : rootLogger.getHandlers()) {
			rootLogger.removeHandler(handler);
		}
	}
	
	
	/**
	 * returns the log level form the passed string or null if the level is not defined
	 * @param levelStr 	the level of the logger
	 * @return
	 */
	private Level levelFromString(String levelStr) {
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
