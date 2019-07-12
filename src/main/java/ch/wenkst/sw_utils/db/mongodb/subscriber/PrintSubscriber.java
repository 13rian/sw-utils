package ch.wenkst.sw_utils.db.mongodb.subscriber;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subscriber that only prints out an error if one occurs
 * @param <T>
 */
public class PrintSubscriber<T> implements Subscriber<T> {
	private static final Logger logger = LoggerFactory.getLogger(PrintSubscriber.class);
	
	protected String name;						// the name to identify this request
	protected Exception error = null; 			// the first error that occurred


	/**
	 * base subscriber for a reactive stream mongodb operation
	 */
	public PrintSubscriber() {
		name = "";
	}
	
	/**
	 * base subscriber for a reactive stream mongodb operation
	 */
	public PrintSubscriber(String name) {
		this.name = name;
	}

	@Override
	public void onSubscribe(final Subscription subscription) {
		subscription.request(Integer.MAX_VALUE);
	}

	@Override
	public void onNext(final T t) {

	}

	@Override
	public void onError(final Throwable t) {
		if (error == null) {
			error = new Exception(t);
		}
		onComplete();
	}

	@Override
	public void onComplete() {
		if (error != null) {
			logger.info(name + ": db operation error: ", error);
		} else {
			logger.debug(name + ": db operation successful");
		}
	}
}
