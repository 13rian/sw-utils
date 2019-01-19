package ch.wenkst.sw_utils.db.async.subscriber;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class BaseSubscriber<T> implements Subscriber<T> {
	final static Logger logger = LogManager.getLogger(BaseSubscriber.class);    // initialize the logger
	
	protected final List<T> result;					// result list of the db
	protected Exception error = null; 				// the first error that occurred
	protected volatile boolean completed; 			// true if the stream is completed, false otherwise

	/**
	 * base subscriber for a reactive stream mongodb operation
	 */
	public BaseSubscriber() {
		result = new ArrayList<T>();
	}

	@Override
	public void onSubscribe(final Subscription subscription) {
		subscription.request(Integer.MAX_VALUE);
	}

	@Override
	public void onNext(final T t) {
		result.add(t);
	}

	@Override
	public void onError(final Throwable t) {
		// logger.error("db operation error: ", t);
		if (error == null) {
			error = new Exception(t);
		}
		onComplete();
	}

	@Override
	public void onComplete() {
		completed = true;
	}
	

	public List<T> getResult() {
		return result;
	}

	public Throwable getError() {
		return error;
	}

	public boolean isCompleted() {
		return completed;
	}
}
