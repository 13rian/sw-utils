package ch.wenkst.sw_utils.db.async.subscriber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CallbackSubscriber<T> extends BaseSubscriber<T> {
	final static Logger logger = LogManager.getLogger(CallbackSubscriber.class);    // initialize the logger

	private IResultCallback<T> callback;
	
	public CallbackSubscriber(IResultCallback<T> callback) {
		this.callback = callback;
	}
			

	@Override
	public void onComplete() {
		super.onComplete();
		callback.onResult(result, error);
	}
}
