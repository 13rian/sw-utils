package ch.wenkst.sw_utils.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class TimeoutFuture<T> {
	private static Logger logger = LogManager.getLogger(TimeoutFuture.class); 		// initialize the logger
	
	private CompletableFuture<T> future = null;
	private long timeout = 0;

		
	/**
	 * A Wrapper for a future that is completed with null after the passed timeout expired.
	 * @param timeout		the timeout of the future in ms
	 */
	public TimeoutFuture(long timeout) {
		this.timeout = timeout;
		this.future = new CompletableFuture<T>();
	}

	
	/**
	 * calls the get of the future and returns null if the timeout is reached or another error occurred.
	 * if an error occurred it is printed
	 * @return 		the result of the future
	 */
	public T get() {
		T result = null;
		try {
			result = future.get(timeout, TimeUnit.MILLISECONDS);
		
		} catch (TimeoutException ex1) {
			// catch the timeout exception and do nothing
		
		} catch (Exception ex2) {
			// log an unexpected error that is not a timeout error
			logger.error("error calling the get of the timeout future: ", ex2);
		}
		
		return result;
	}


	/**
	 * completes the future with the passed value
	 * @param value 	value that is used to complete the future
	 */
	public void complete(T value) {
		future.complete(value);
	}

}
