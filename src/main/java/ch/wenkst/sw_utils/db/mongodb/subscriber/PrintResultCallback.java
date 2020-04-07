package ch.wenkst.sw_utils.db.mongodb.subscriber;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subscriber that only prints out an error if one occurs
 * @param <T>
 */
public class PrintResultCallback<T> implements IResultCallback<T> {
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
	public void onResult(List<T> result, Exception error) {
		if (error != null) {
			logger.error(name + ": db operation error: ", error);
		} else {
			logger.debug(name + ": db operation successful");
		}		
	}
}
