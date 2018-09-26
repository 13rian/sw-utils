package ch.wenkst.sw_utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * holds a collection of frequently used methods
 */
public class Utils {
		
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
		boolean allPresent = true;
		
		// check if all properties are present
		for (String prop : property) {
			if (!props.containsKey(prop)) {
				System.err.println("missing property in the logger-config prooperties file");
				allPresent = false;
			}
		}
		
		return allPresent;
	}
	
}
