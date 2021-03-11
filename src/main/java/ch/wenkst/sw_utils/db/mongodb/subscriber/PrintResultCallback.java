package ch.wenkst.sw_utils.db.mongodb.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.db.mongodb.subscriber.value.ValueCallback;

public class PrintResultCallback<T> implements ValueCallback<T> {
	private static final Logger logger = LoggerFactory.getLogger(PrintResultCallback.class);
	
	protected String name;
		
	
	public PrintResultCallback() {
		name = "";
	}
	

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
