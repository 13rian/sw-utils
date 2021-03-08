package ch.wenkst.sw_utils;

import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.conversion.Conversion;

public class Utils {
	private static final Logger logger = LoggerFactory.getLogger(Utils.class);
	
	/**
	 * pints the startup message with the maven version and some java properties
	 * @return 		true if the pom-file was successfully parsed, false if an error occurred
	 */
	public static boolean logStartupMessage() {
		try {
			MavenXpp3Reader reader = new MavenXpp3Reader();
	        Model model = reader.read(new FileReader("pom.xml"));
	        int formatLength = 23;	        
	        
	        logger.info("--------------------------------------------------------------------------------------------");
	        logger.info("starting up " + model.getName());
	        logger.info(Conversion.padRight("groupId: ", formatLength) + model.getGroupId());
	        logger.info(Conversion.padRight("artifactId: ", formatLength) + model.getArtifactId());
	        logger.info(Conversion.padRight("version: ", formatLength) + model.getVersion());
	        
			logger.info(Conversion.padRight("java version: ", formatLength) + System.getProperty("java.version"));
			logger.info(Conversion.padRight("java runtime name: ", formatLength) + System.getProperty("java.runtime.name"));
			logger.info(Conversion.padRight("java runtime version: ", formatLength) + System.getProperty("java.runtime.version"));
			logger.info(Conversion.padRight("java home: ", formatLength) + System.getProperty("java.home"));
	        logger.info("--------------------------------------------------------------------------------------------");
	        return true;
	        
		} catch (Exception e) {
			logger.error("error parsing the maven pom file: ", e);
			return false;
		}
	}
	
		
	/**
	 * put the caller to sleep for the passed amount of time in ms
	 * @param sleepTime 	sleep time in ms
	 */
	public static void sleep(int sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// do nothing
		}
	}
	

	/**
	 * puts the caller to an infinity sleep. this method is intended to keep the main thread alive
	 * @param sleepInterval		interval for the periodic sleep
	 */
	public static void infinitySleep(int sleepInterval) {
		while (true) {
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				logger.error("sleep interrupted: ", e);
			}
		}
	}
	
	
	/**
	 * returns the current working directory of the running program
	 * @return 		absolute path to the working directory
	 */
	public static String getWorkDir() {
		return System.getProperty("user.dir");
	}
	
	
	/** 
	 * returns true if the operating system is windows
	 * @return: 	true if the operating system is windows
	 */
	public static boolean isOSWindows() {
		String osName = System.getProperty("os.name");
		return osName.toUpperCase().contains("WIN") == true;
	}

	/** 
	 * returns true if the operating system is linux
	 * @return: 	true if the operating system is linux
	 */
	public static boolean isOSLinux() {
		String osName = System.getProperty("os.name");
		return osName.toUpperCase().contains("LINUX") == true;
	}
	
	
	/**
	 * returns a log string containing information about free memory, total memory and the number of threads
	 * @return 		string that contains the runtime informations
	 */
	public static String getMemoryInfoStr() {
		int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();
		long freeMem = Runtime.getRuntime().freeMemory()/1024;
		long totMem = Runtime.getRuntime().totalMemory()/1024;
		double usedMemory = (totMem - freeMem) / totMem;
		String usedMemPercent = String.format("%.2f%%", 100*usedMemory);
		
		
		String result = "Thread Count = " + threadCount + ", Total Memory = " + totMem + ", used Memory: " + usedMemPercent;
		return result;		
	}
	
	
	/**
	 * returns the stack trace of the passed exception as string
	 * @param th 	a throwable
	 * @return 		the stack trace of the passed exception as string
	 */
	public static String exceptionToString(Throwable th) {		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		th.printStackTrace(pw); 					// print the stack trace to the specified printer
		String result = sw.toString(); 				// get the stack trace from the printer
		return result;
	}
	
	
	/**
	 * waits for all passed futures to finish. The returned future holds a list of all the results
	 * If one of the futures completes exceptionally the combined future does as well 
	 * @param futures 	futures that are combined
	 * @return 			list with the results of the passed futures
	 */
	public static CompletableFuture<List<Object>> allOfCombletableFuture(CompletableFuture<?>... futures) {
	     return CompletableFuture.allOf(futures)
	            .thenApply((x) -> Arrays.stream(futures)
	                    .map(f -> (Object) f.join())
	                    .collect(Collectors.toList())
	            );
	}
	
	
	/**
	 * checks if all passed properties are present in the properties file
	 * @param props 		the properties file
	 * @param property 		properties that should be present in the properties file
	 * @return 				true if no property is missing, and false if one property is not present
	 */
	public static boolean allPropsPresent(Properties props, String... property) {
		for (String prop : property) {
			if (!props.containsKey(prop)) {
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * checks if the passed ip is reachable
	 * @param ip 		ip to test
	 * @param timeout	the timeout in ms to wait for the server to respond
	 * @return 			true if the ip is reachable, false if not
	 */
	public static boolean ipReachable(String ip, int timeout) {
		try {
			InetAddress address = InetAddress.getByName(ip);
			return address.isReachable(timeout);

		} catch (Exception e) {
			logger.error("error testing for the ip " + ip, e);
			return false;
		}
	}
}
