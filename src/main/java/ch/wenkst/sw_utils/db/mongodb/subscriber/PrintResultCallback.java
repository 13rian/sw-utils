package ch.wenkst.sw_utils.db.mongodb.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.db.mongodb.subscriber.value.ValueCallback;

/**
 * Subscriber that only prints out an error if one occurs
 * @param <T>
 */
public class PrintResultCallback<T> implements ValueCallback<T> {
	private static final Logger logger = LoggerFactory.getLogger(PrintResultCallback.class);
	
	protected String name;						// the name to identify this request
		
	
	/**
	 * base subscriber for a reactive stream mongodb operation
	 */
	public PrintResultCallback() {
		name = "";
	}
	
	/**
	 * base subscriber for a reactive stream mongodb operation
	 */
	public PrintResultCallback(String name) {
		this.name = name;
	}
	
	
	@Override
	public void onResult(T result, Exception error) {
		if (error != null) {
			logger.error(name + ": db operation error: ", error);
		} else {
			logger.debug(name + ": db operation successful");
		}		
	}
}
