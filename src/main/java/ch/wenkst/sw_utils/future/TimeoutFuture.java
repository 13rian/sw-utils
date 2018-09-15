package ch.wenkst.sw_utils.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class TimeoutFuture<T> {
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
	 * if another error occurrs it will still be thrown
	 * @return 		the result of the future
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public T get() throws InterruptedException, ExecutionException {
		T result = null;
		try {
			result = future.get(timeout, TimeUnit.MILLISECONDS);
		
		} catch (TimeoutException ex1) {
			// only catch the timeout exception, the other exceptions will still be thrown
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
